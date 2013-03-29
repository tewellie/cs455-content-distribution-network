package cdn;

import java.io.*;
import java.net.*;
import java.util.*;

/*
 * Command-line arg for running Discovery node:
 * 		java cdn.Discovery portnum refresh_interval
 * 
 * Exactly one discovery node in the system providing following functionality:
 * 1. Allow routers to register themselves (performed when router starts up for first time)
 * 2. Allow routers to deregister themselves (performed when router leaves CDN)
 * 3. enable construction of CDN by orchestrating connections that a router initiates with other routers in the system.
 *    based on knowledge of routers (through function 1) the discovery node informs routers about other routers they should connect to.
 * 4. Assign and publish weights to the links connecting any two routers in the CDN. The weights these links take will range from 1-10.
 *    discovery node updates weights at regular intervals; interval is a configurable parameter with a default of 120s
 *    
 *    discovery node maintains info about registered routers in a registry
 *    discovery node does not play any role in routing data within the CDN.
 *    Interactions between routers and discovery node are via request-response messages. For each request that it receives from the routers, 
 *    the discovery node will send a response back to the router (Based on IP address associated with Socket's input stream) where request originated. 
 *    Contents of response depend on type of request and outcome of processing request
 *    
 * Commands supported while running:
 *    list-routers: result in info about the routers (assigned-ID, hostname, port-number) being listed; one router/line
 *    list-weights: info about links comprising CDN; one link/line; include info about nodes it connects and weights
 *    	i.e. A Z 8 carrot.cs.colostate.edu:2000 broccoli.cs.colostate.edu:5001
 *    setup-cdn number-of-connections: result in discovery nodes setting up CDN; 
 *    	does so by sending routers messages containing info about routers that it should connect to
 *    	discovery node tracks connection counts for each router and will send PEER_ROUTER_LIST message to only those routers that have no reached specified connection limits
 *    	possible that one or more router nodes will not get PEER_ROUTER_MESSAGE
 *    	i.e. setup-cdn 4 results in creation of CDN where each router is connected to exactly 4 other routers in CDN
 *    	handle error condition where number of routers is less than connection limit that is specified
 *    	note: not required to deal with cases where router is added or removed after CDN has been set up, 
 *    	but must deal with case where router registers and deregisters from the discovery node before CDN is set up
 *    	CDN tracker variable at each router keeps track of total number of unique messages that were disseminated within the CDN
 *    
 */

public class Discovery {
	
	boolean debug = true;
	
	private int portNum;
	private int refreshInterval;
	private String hostName;	
	private Hashtable<String, RegisterRequest> nodes; //keep track of registered routers
	private int numReqConnections;
	private Hashtable<String, Integer> connections; //keep track of number of connections for each router
	private Hashtable<String, Socket> sockets; //keep track of discovery node's connections with routers
	private Hashtable<Pair<String, String>, LinkInfo> links; //keep track of links
	private Hashtable<String, PeerRouterList> peerRouterLists; //keep track of peerRouterList for each router
	private boolean cdnSetUp;
	private Hashtable<String, RegisterRequest> newNodes; //keep track of nodes added after in CDN
	private Hashtable<String, RegisterRequest> removedNodes; //keep track of nodes removed after CDN setup
	
	/**
	 *  Discovery node constructor
	 * @param portNumber: portNumber the node is running on
	 * @param refreshInt: refresh interval for link weights
	 */
	private Discovery(int portNumber, int refreshInt){
		portNum = portNumber;
		refreshInterval = refreshInt;
		cdnSetUp=false;
		//retrieve hostname
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//print out for debug - says discovery node was created
		if(debug){
			System.out.println("Discovery node created: " + hostName + ":" + portNum);
		}
		
		//create new hashTable for keeping track of registered routers
		nodes = new Hashtable<String, RegisterRequest>();
		newNodes = new Hashtable<String, RegisterRequest>();
		removedNodes = new Hashtable<String, RegisterRequest>();
		numReqConnections = 4;
		connections = new Hashtable<String, Integer>();
		sockets = new Hashtable<String, Socket>();

	}
	
	
	/**
	 * Main method - creates a discovery node and starts a DiscoveryReceiverThread to listen for connections
	 * @param args args[0] = port number, args[1] = refresh_interval
	 */
	public static void main (String args[]){
		//incorrect number of arguments
		if(!(args.length==2)){
			System.out.println("Correct usage: java cdn.Discovery portnum refresh_interval");
			return;
		}
		
		//create discovery node
		Discovery discoveryNode = new Discovery(Integer.parseInt(args[0]),Integer.parseInt(args[1]));
		
		//create ServerSocket and Socket
				ServerSocket server = null;
				try {
					server = new ServerSocket(discoveryNode.getPort());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		
		//create receiver thread
		Thread discoveryReceiverThread = new Thread(new DiscoveryReceiverThread(discoveryNode, server));
		discoveryReceiverThread.start();
		
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		//need condition for termination still
		while(true){
			String command = "";
			try {
				command = buffer.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(command.equals("list-routers")){
				if(discoveryNode.cdnSetUp)discoveryNode.listRouters();
				else System.out.println("CDN not setup");
			}
			else if(command.equals("list-weights")){
				if(discoveryNode.cdnSetUp)discoveryNode.listLinks();
				else System.out.println("CDN not setup");
			}
			//allows for <number-of-connections> to remain unspecified - uses default of 4
			else if(command.startsWith("setup-cdn ") || command.equals("setup-cdn")){
				if(!command.equals("setup-cdn")){
					discoveryNode.numReqConnections = Integer.parseInt(command.substring(10));
				}
				discoveryNode.setupCDN();
			}
			else{
				System.out.println("Command not recognized. Available commands:");
				System.out.println('\t'+"list-routers");
				System.out.println('\t'+"list-weights");
				System.out.println('\t'+"setup-cdn <number-of-connections>");
			}
		}
		
	}
	

	/**
	 * Allows a node to register if it's not already registered and if the IP addresses match correctly
	 * @param request RegisterRequest containing information about Router requesting registration
	 * @param socketIP - socket for message passing with the Router
	 */
	public RegisterResponse register(RegisterRequest request, String socketIP, Socket routerSocket){

		RegisterResponse response;
		
		//so a node can register, deregister and register again b4 cdn set up
		if(!cdnSetUp) updateNodeList();
		String ipString = "";
		try {
			ipString = InetAddress.getByName(socketIP).getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!ipString.equals(request.getIP())){			
			response = new RegisterResponse(RegisterResponse.failure,"Registration request unsucessful. IP address specified in registration does not match IP address of request");
		}
		else if(inTable(request) || (request.getIP().equals(hostName) && request.getPort()==portNum)){
			response = new RegisterResponse(RegisterResponse.failure,"Registration request unsucessful. Router previously registered");
		}
		else if(nodes.containsKey(request.getID())){
			response = new RegisterResponse(RegisterResponse.failure, "Registration request unsucessful. ID previously registered.");
		}
		else{
			if(!cdnSetUp){
				//add request to hashtable using ID as key
				nodes.put(request.getID(), request);
			}
			else newNodes.put(request.getID(), request);
			//add an entry to connections for the new router
			connections.put(request.getID(), 0);
			//add an entry to sockets for the router
			sockets.put(request.getID(), routerSocket);
			
			String successString = "Registration request successful. The number of routers currently constituting the CDN is (" +
					nodes.size()+")";
			response = new RegisterResponse(RegisterResponse.success, successString);
		}
		

		return response;
	}

	
	public int getPort(){
		return portNum;
	}
	
	public String getHost(){
		return hostName;
	}
	
	private boolean inTable(RegisterRequest request){
		for(RegisterRequest tableRequest:nodes.values()){
			if (tableRequest.equals(request)) return true;
		}
		return false;
	}
	
	//ensure links are bidirectional
	//no partition in CDN (able to reach any router from any other router
	//keep track of numb of connections - make sure Cr is achieved
	//send peer-router list to each router
	//handle error case where # routers less than connections specified
	private void setupCDN(){
		/*
		 * case 0 even number connections:
		 * 		1. connect to neighbor (create circle) - number connections = 2
		 * 		2. for(i=1;;i++) (while connectionLimit not reached) - number connections increases by 2 each loop
		 * 			connect to neighbor+i
		 * case 1 even nodes, odd connections:
		 * 		1. connect to neighbor (create circle) - number connections = 2
		 * 		2. for(i=1;;i++) (while connectionLimi-1 not reached)- number connections increases by 2 each loop
		 * 			connect to neighbor+i
		 * 		3. connect to neighbor+numbNodes/2
		 * case 2 non-configurable:
		 * 		odd# nodes with odd# connections
		 * 		#connections+1 => number of nodes
		 * 			
		 */
		cdnSetUp = true;
		
		updateNodeList();
		
		peerRouterLists = new Hashtable<String, PeerRouterList>();
		links = new Hashtable<Pair<String, String>, LinkInfo>();
		
		//case 2: non-configurable
		if( (numReqConnections >= nodes.size()) || (numReqConnections%2==1 && nodes.size()%2==1) ||(numReqConnections<2) ){
			System.out.println("CDN not configurable based on specifications. Register more routers and change the number of required connections");
			return;
		}
		
		//initialize peer lists
		for(RegisterRequest router:nodes.values()){
			//create new peer lists for each node
			ArrayList<RouterInfo> tempPeerList = new ArrayList<RouterInfo>();
			PeerRouterList list = new PeerRouterList(tempPeerList, 0);
			peerRouterLists.put(router.getID(), list);
		}
		
		//create an array representing the routers
		Collection<RegisterRequest> routerCollection = nodes.values();
		RegisterRequest[] routers = collectionToArray(routerCollection);

		Random numberGenerator = new Random();
		
		//connect to neighbor, create circle
		for(int i=0; i<routers.length; i++){
			//make sure it loops back by %routers.length
			RegisterRequest router1 = routers[i];
			RegisterRequest router2 = routers[(i+1)%routers.length];
			LinkInfo newLink = new LinkInfo(new Pair<String,String>(router1.getID(),router2.getID()), getLinkWeight(numberGenerator),
					router1.getIP()+":"+router1.getPort(), router2.getIP()+":"+router2.getPort());
			links.put(newLink.getID(), newLink);
			updateConnectionNumber(newLink);
			//if(debug) System.out.println("link (circle): " + newLink.toString());
			//update peerList for fist router making connection
			updatePeerList(router1.getID(), router2.getID(), router2.getIP(), router2.getPort());
		}
		

		//add 2 connections each time so need to loop #connections/2
		for(int i=2;i<((numReqConnections/2)+1);i++){
			//for each nodes, connect to neighbor+i
			for(int j=0; j<routers.length;j++){
				RegisterRequest router1 = routers[j];
				RegisterRequest router2 = routers[(j+i)%routers.length];
				LinkInfo newLink = new LinkInfo(new Pair<String,String>(router1.getID(),router2.getID()), getLinkWeight(numberGenerator),
						router1.getIP()+":"+router1.getPort(), router2.getIP()+":"+router2.getPort());
				links.put(newLink.getID(), newLink);
				updateConnectionNumber(newLink);
//				if(debug) System.out.println("link("+ i+ "away): " + newLink.toString());
				//update peerList for fist router making connection
				updatePeerList(router1.getID(),router2.getID(), router2.getIP(), router2.getPort());
			}	
		}
		
		//case 1: even number nodes, odd number connections - add one more connections for each
		if(nodes.values().size()%2==0 && numReqConnections%2==1){
			//make connections (for half of routers else get duplicates)
			for (int i=0; i<routers.length/2;i++){
				RegisterRequest router1 = routers[i];
				RegisterRequest router2 = routers[(i+(routers.length/2))%routers.length];
				LinkInfo newLink = new LinkInfo(new Pair<String,String>(router1.getID(),router2.getID()), getLinkWeight(numberGenerator),
						router1.getIP()+":"+router1.getPort(), router2.getIP()+":"+router2.getPort());
				links.put(newLink.getID(), newLink);
				updateConnectionNumber(newLink);
				//if(debug) System.out.println("link(case1): " + newLink.toString());
				//update peerList for fist router making connection
				updatePeerList(router1.getID(), router2.getID(), router2.getIP(), router2.getPort());
			}
		}
		
		//send peer lists
		sendPeerLists();
		//send link lists to routers
		sendLinkList();
		//start linkWeightUpdates
		long interval = refreshInterval;
		UpdateLinks updates = new UpdateLinks(this, interval);
		updates.start();
		
	}
	
	private void updateNodeList(){
		for(String key:newNodes.keySet()){
			nodes.put(key, newNodes.get(key));
			newNodes.remove(key);
		}
		for(String key:removedNodes.keySet()){
			nodes.remove(key);
			removedNodes.remove(key);
			byte[] temparray = new byte[1];
			Message temp = new Message(temparray,MessageType.EXIT);
			temp.send(sockets.get(key));
			sockets.remove(key);
			connections.remove(key);
		}
	}
	
	
	
	private void sendPeerLists(){
		for(String key:peerRouterLists.keySet()){
			//get peer router list
			PeerRouterList list = peerRouterLists.get(key);
			//create message
			Message listMessage = new Message(list.toByteArray(),MessageType.PEER_ROUTER_LIST);
			//send list
			listMessage.send(sockets.get(key));
		}
	}
	
	private void updatePeerList(String ID, String peerID, String peerIP, int peerPort){
		//add new pair to peerlist
		String info = peerIP + ":" + peerPort;
		RouterInfo tempInfo = new RouterInfo(peerID, info);
		PeerRouterList tempList = peerRouterLists.get(ID);
		tempList.addPeer(tempInfo);
		tempList.incPeerCount();
		peerRouterLists.remove(ID);
		peerRouterLists.put(ID, tempList);
	}
	
	private void listRouters(){
		for(RegisterRequest router:nodes.values()){
			
			String hostName = "";
			try {
				hostName =  InetAddress.getByName(router.getIP()).getCanonicalHostName();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println(router.getID() + " " + hostName + ":" + router.getPort());
		}
	}
	
	private void listLinks(){
		for(LinkInfo link:links.values()){
			RegisterRequest regA = nodes.get(link.getID().A);
			RegisterRequest regB = nodes.get(link.getID().B);
			String hostName1 = "";
			String hostName2 = "";
			try {
				hostName1 =  InetAddress.getByName(regA.getIP()).getCanonicalHostName();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			try {
				hostName2 =  InetAddress.getByName(regB.getIP()).getCanonicalHostName();
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			String linkString = "";
			linkString += link.getID().A + " " + link.getID().B + " " + link.getWeight() + " ";
			linkString += hostName1 + ":" + regA.getPort() + " " + hostName2 + ":" + regB.getPort();
			System.out.println(linkString);
		}
	}
	
	private int getLinkWeight(Random numberGenerator){
		//will generate a number between 0 and 9, then add 1 to be in range of 1 and 10
		return numberGenerator.nextInt(10) +1;
	}
	
	public void updateLinkWeights(){
		Random gen = new Random();
		//for each link, get a new weight
		for(Pair<String,String> key:links.keySet()){
			LinkInfo temp = links.get(key);
			temp.setWeight(getLinkWeight(gen));
			links.put(key, temp);
		}
		//send link lists to routers
		sendLinkList();
	}
	
	//updates connection number for both nodes in the link
	private void updateConnectionNumber(LinkInfo link){
		String IDa = link.getID().A;
		String IDb = link.getID().B;
		int temp = connections.get(IDa);
		connections.remove(IDa);
		connections.put(IDa,temp+1);		
		temp = connections.get(IDb);
		connections.remove(IDb);
		connections.put(IDb,temp+1);	
	}
	
	private RegisterRequest[] collectionToArray(Collection<RegisterRequest> collection){
		Iterator<RegisterRequest> iterator = collection.iterator();
		RegisterRequest[] temp = new RegisterRequest[collection.size()];
		int i=0;
		while(iterator.hasNext()){
			temp[i] = iterator.next();
			i++;
		}
		return temp;
	}
	
	private void sendLinkList(){
		ArrayList<LinkInfo> linkInfoList = new ArrayList<LinkInfo>();
		
		for(LinkInfo link:links.values()){
			linkInfoList.add(link);
		}
		LinkWeightUpdate updateMessage = new LinkWeightUpdate(linkInfoList.size(), linkInfoList);
		//create message
		Message linkMessage = new Message(updateMessage.toByteArray(),MessageType.LINK_WEIGHT_UPDATE);
		for(Socket socket:sockets.values()){
			//send list
			linkMessage.send(socket);
		}
	}
	
	public int getNumReqConnections(){
		return numReqConnections;
	}
	
	public RegisterResponse deregister(RegisterRequest request,String socketIP, Socket routerSocket){
		
		
		RegisterResponse response;
		
		String ipString = "";
		try {
			ipString = InetAddress.getByName(socketIP).getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(!ipString.equals(request.getIP())){			
			response = new RegisterResponse(RegisterResponse.failure,"Deregistration request unsucessful. IP address specified in registration does not match IP address of request");
		}
		else if(!nodes.containsKey(request.getID())){
			response = new RegisterResponse(RegisterResponse.failure, "Deegistration request unsucessful. ID not registered.");
		}
		else{
			removedNodes.put(request.getID(), request);
			//add an entry to connections for the new router
			
			String successString = "Deregistration request successful. Deregistration will occur next CDN setup";
			response = new RegisterResponse(RegisterResponse.success, successString);
		}
		
		return response;
	}
	
}



