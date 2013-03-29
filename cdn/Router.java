package cdn;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Hashtable;

/* Command line args:
 * 		java cdn.Router portnum assigned-id discovery-host discovery-port
 * 
 * There are multiple router nodes (minimum of 10) in the system providing two functionalities:
 * 1. Route data packets within the CDN
 * 2. Update routes based on changes to the link weights between the routers that comprise the CDN
 * 
 * When a router starts up it first sends a registration request to the discovery node. 
 * The routers organize themselves into a CDN based on info provided by discovery node.
 * Upon receipt of link weights from discovery node, each router computes the MST which is then used to route any packets originating from it.
 * Weights updated based on refresh interval at discovery node.
 * 
 * Each router assigned a unique ID - should be a string passed in as a command-line argument associated with router node
 * 
 * Send message:
 * Data can be fed into network from any router within the network from any router within the network
 * Every router will maintain a variable tracker (initialized to 0) that is incremented by 1 every time a packet is received or a new packet is pushed into the CDN by that router
 * the tracker variable is never reset. Payload for packets that a router sends into CDN will simply be the variable tracker. 
 * When a packet is ready to be sent, the router should use the MST that it has computed to generate a routing plan that will be included in the packet
 * Routing plan indicated how packet must be routed; A may have a direct connection to B, but the routing plan may call for packet to be sent A-> C -> E -> D -> B
 * Must account for cases where a fork appears and packet needs to be sent along two different links originating from a router
 * For each packet that is received at a router node you should print out the count that was encoded into the original message and the router that the data was received from
 * 
 * Two requirements for dissemination of packets within CDN are:
 * 		1. no router should receive same packet more than once
 * 		2. every router in CDN should receive every packet that has been published/sent into the network by other routers
 *
 * Commands supported while running:
 *    print-MST: print MST that has been computed by the router; listing should also indicate weights associated with links
 *    	listing printed out in terms of assigned-IDs on the routers.
 *    	e.g.  A--8--B--4--D--2--F--1--E
 *    		  B--2--C--1--G
 *    		  C--2--I--1--J
 *    send-data: result in router incrementing tracker and creating a data packet that contains this variable along with the routing plan
 *    exit-cdn: allows router node to exit CDN; router should first send a deregistration message to discovery node and await a response before exiting and terminating the process
 */

public class Router {
	
	private boolean debug = true;
	
	//router port number
	private int portNum;
	//router ID
	private String id;
	//discovery node host name
	private String discoveryHost;
	//discovery node port
	private int discoveryPort;
	//router hostname
	private String hostName;
	public boolean inCDN;
	private int tracker;
	private PeerRouterList peerList;
	private Hashtable<Pair<String, String>, LinkInfo> links; //keep track of links
	//keep a hashtable of sockets for connections made and received with other routers
	private Hashtable<String, Socket> connections;
	private MSTPrim mst;
	
	/**
	 * Router node constructor
	 * @param portNumber: the router is running on
	 * @param ID: of the router
	 * @param discHost: name of the discovery node
	 * @param discPort: discovery node port number
	 */
	private Router(int portNumber, String ID, String discHost, int discPort){
		portNum = portNumber;
		id = ID;
		discoveryPort = discPort;
		links = null;
		peerList = null;
		connections = new Hashtable<String, Socket>();
		try {
			hostName = InetAddress.getLocalHost().getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			discoveryHost = InetAddress.getByName(discHost).getCanonicalHostName();
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		inCDN = true;
		if(debug){
			System.out.println("Router node created: " + hostName + ":" + portNum);
		}
		tracker = 0;
	}
	
	
	/**
	 * Routers with the same port number and IP address are equal
	 * @param other Router to compare with
	 * @return true if the routers are equal
	 */
	public boolean equals(Router other){
		if(this.hostName.equals(other.hostName)&&this.portNum == other.portNum) return true;
		else return false;
	}
	
	/**
	 * 
	 * @param args args[0] = port number, args[1] = assigned ID, args[2] = discovery hostName, args[3] = discoveryPort
	 */
	public static void main(String args[]){
		//incorrect number of arguments
		if(!(args.length==4)){
			System.out.println("Correct usage: java cdn.Router portnum assigned-id discovery-host discovery-port");
			return;
		}
		
		//create new router node
		Router routerNode = new Router(Integer.parseInt(args[0]),args[1],args[2],Integer.parseInt(args[3]));


		
		//create socket for message passing with discovery node
		Socket discoverySocket = null;
		try {
			discoverySocket = new Socket(routerNode.discoveryHost, routerNode.discoveryPort);
		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		//create registration request
		RegisterRequest request = new RegisterRequest(routerNode.hostName, routerNode.portNum, routerNode.id);
		//write the registration request to the discovery node
		Message registerRequest = new Message(request.toByteArray(),MessageType.REGISTER_REQUEST);
		registerRequest.send(discoverySocket);
		
		//create server socket to wait for connections from other peers
		ServerSocket server = null;
		try {
			server = new ServerSocket(routerNode.getPort());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//create link receiver thread
		Thread linkReceiverThread = new Thread(new LinkReceiverThread(routerNode, server));
		linkReceiverThread.start();
		
		
		
		//thread for listening for communication with discovery node
		RouterReceiverThread routerReceiverThread = new RouterReceiverThread(discoverySocket, routerNode);
		routerReceiverThread.start();
		
		//wait for commands
		BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
		while(routerNode.inCDN){
			String command = "";
			try {
				command = buffer.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(command.equals("send-data")){
				//increment tracker since packet originated from here
				if(routerNode.inCDN){
					routerNode.incTracker();
					routerNode.setMST();
					routerNode.sendData(routerNode.tracker, routerNode.mst);
				}
				else System.out.println("CDN not setup");
			}
			else if(command.equals("exit-cdn")){
				routerNode.inCDN=false;
				RegisterRequest deregister = new RegisterRequest(routerNode.hostName, routerNode.portNum, routerNode.id);
				Message deregisterMessage = new Message(deregister.toByteArray(), MessageType.DEREGISTER_REQUEST);
				deregisterMessage.send(discoverySocket);
			}
			else if(command.equals("print-MST")){
				if(routerNode.inCDN){
					routerNode.setMST();
					routerNode.getMST().getTree().print();
				}
				else System.out.println("CDN not setup");
			}
			else if(command.equals("test-links")){
				for(String key:routerNode.connections.keySet()){
					System.out.println("sending a test to " + key);
					byte [] temp = routerNode.id.getBytes();
					Message tempM = new Message(temp, MessageType.TEST);
					tempM.send(routerNode.connections.get(key));
				}
			}
			else{
				System.out.println("Command not recognized. Available commands:");
				System.out.println('\t'+"send-data");
				System.out.println('\t'+"exit-cdn");
				System.out.println('\t'+"print-MST");
			}
		}
		
	}
	
	public void incTracker(){
		tracker++;
	}
	
	public void setPeerList(PeerRouterList list){
		peerList = list;
	}
	
	public PeerRouterList getPeerList(){
		return peerList;
	}
	
	public void setLinkList(Hashtable<Pair<String, String>, LinkInfo> newLinks){
		links = newLinks;
	}
	
	public Hashtable<Pair<String, String>, LinkInfo> getLinks(){
		return links;
	}
	
	public void addConnection(String id, Socket socket){
		connections.put(id, socket);
	}
	
	public Hashtable<String, Socket> getConnections(){
		return connections;
	}
	
	public int getPort(){
		return portNum;
	}
	
	public String getID(){
		return id;
	}
	
	public String printConnectionList(){
		String result = "";
		for(String key:connections.keySet()){
			result += key + " " + connections.get(key).getInetAddress().getCanonicalHostName() + ":" + connections.get(key).getPort();
			result += "\\n";
		}
		return result;
	}
	
	public void setMST(){
		//MSTPrim(String rootID, ArrayList<String> nodes, ArrayList<LinkInfo> linkInfo)
		ArrayList<String> nodes = new ArrayList<String>();
		for(Pair<String,String> linkIDs:links.keySet()){
			if(!nodes.contains(linkIDs.A)) nodes.add(linkIDs.A);
			if(!nodes.contains(linkIDs.B)) nodes.add(linkIDs.B);
		}
		ArrayList<LinkInfo> linkInfo = new ArrayList<LinkInfo>(links.values());
		mst = new MSTPrim(id, nodes, linkInfo);
	}
	
	public void setMST(Tree tree){
		mst = new MSTPrim(tree);
	}
	
	public MSTPrim getMST(){
		return mst;
	}
	
	public void resetTracker(){
		tracker = 0;
	}
	
	public void sendData(int trackCount, MSTPrim tree){
		//for each link in list of connections, send data packet
		//data packet = message (tracker number, mst)
		int sizePacket = 4 + tree.getTree().toByteArray().length;
		byte[] packetBytes = new byte[sizePacket];
		int pos=0;
		//encode tracker#
		for(int i=0; i<4; i++){
			packetBytes[pos] = Helper.intToByteArray(trackCount)[i];
			pos++;
		}
		//encode mst
		byte[] temp = tree.getTree().toByteArray();
		for(int i=0; i<temp.length; i++){
			packetBytes[pos] = temp[i];
			pos++;
		}
		//create message
		Message packet = new Message(packetBytes, MessageType.PACKET);
		//for each peer on MST, send message
		ArrayList<LinkInfo> peerList = tree.getPeers(id);
		for(LinkInfo peer:peerList){
			String peerID;
			if(id.equals(peer.getID().A)) peerID = peer.getID().B;
			else peerID = peer.getID().A;
			packet.send(connections.get(peerID));
		}
		System.out.println("Data packets sent/received in this CDN: " + tracker);
	}
	
	public void exitCDN(){
		while(inCDN){
			
		}
		System.out.println("Exiting CDN");
		System.exit(0);
	}

}
