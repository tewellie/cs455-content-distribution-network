package cdn;

import java.io.*;
import java.net.*;
import java.util.*;

public class RouterReceiverThread extends Thread {
	
	private Socket socket;
	private String socketIP;
	private Router routerNode;
	private boolean inCDN;
	private boolean debug = true;
	
	RouterReceiverThread(Socket s, Router node){
		socket = s;
		routerNode = node;
		inCDN = true;
	}
	
	public void exitCDN(){
		inCDN = false;
		System.exit(0);
	}
	

	@Override
	public void run() {
		
		while(inCDN){
		//create an input stream for the socket
				InputStream socketInputStream = null;
				
				//try to get input from the socket
				try {
					socketInputStream = socket.getInputStream();
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				//get the input stream
				DataInputStream din = new DataInputStream(socketInputStream);
				
				//get the socketIP address to check where packets came from
				socketIP = socket.getInetAddress().getHostAddress();
				
				

				//determine the type of the message
				byte[] tempType = new byte[4];
				try {
					din.read(tempType,0,4);
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				
				int messageType = Helper.byteArrayToInt(tempType);
				
				//determine size of the message
				byte[] tempSize = new byte[4];
				try {
					din.read(tempSize,0,4);
				} catch (IOException e3) {
					// TODO Auto-generated catch block
					e3.printStackTrace();
				}
				int streamSize = Helper.byteArrayToInt(tempSize);


				//read the message
				byte [] data = new byte[streamSize];
				int numReads = 0;
				int available = 0;
				try {
					available = din.available();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//read rest of bytes from inputstream
				while(numReads != streamSize){
					try {
						numReads += din.read(data, numReads, available);
						try {
							available = din.available();
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				//handle the message depending on what tpe it is....
				switch(messageType){
				case MessageType.TEST:
					System.out.println(new String(data));
				break;
				case MessageType.REGISTER_RESPONSE: 
					RegisterResponse response = new RegisterResponse(data);
					System.out.println(response.getInfo());
					if(response.getStatus()==RegisterResponse.failure) System.exit(0);					
				break;
				case MessageType.PACKET: 
					//increment tracker number
					routerNode.incTracker();
					//packet has tracker number
					//followed by mst bytes
					//get tracker number
					byte[] trackerBytes = new byte[4];
					int pos = 0;
					for(int i=0; i<4; i++){
						trackerBytes[i] = data[pos];
						pos++;
					}
					int tracker = Helper.byteArrayToInt(trackerBytes);
					//get mst bytes
					byte[] mstBytes = new byte[data.length-4];
					for(int i=0; i<mstBytes.length; i++){
						mstBytes[i] = data[pos];
						pos++;
					}
					//System.out.println("size data: " + data.length + " size mst " + mstBytes.length);
					//create tree
					MSTPrim mst = new MSTPrim(new Tree(mstBytes));
					//send data along through tree
					routerNode.sendData(tracker, mst);
				break;
				case MessageType.PEER_ROUTER_LIST: 
					//new peer list means tracker needs to be reset
					routerNode.resetTracker();
					routerNode.setPeerList(new PeerRouterList(data));
					//if(debug) System.out.println(routerNode.getPeerList().toString());
					//create socket for each peer
					for(RouterInfo peer:routerNode.getPeerList().getPeerList()){
						Socket peerSocket = null;
						try {
							peerSocket = new Socket(peer.getIP(), peer.getPort());
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
						routerNode.addConnection(peer.getID(), peerSocket);
						//create message to tell other router to add to socket list
						PeerID routerID = new PeerID(routerNode.getID());
						//connect to exact port?
						//encode size id
						//encode id
						byte [] array = new byte[routerID.toByteArray().length + 4];
						int pos2 = 0;
						for(int i=0; i<routerID.toByteArray().length; i++){
							array[pos2] = routerID.toByteArray()[i];
							pos2++;
						}
						for(int i=0; i<4; i++){
							array[pos2] = Helper.intToByteArray(routerNode.getPort())[i];
							pos2++;
						}
						Message message = new Message(array,MessageType.SOCKET_REGISTRATION);
						message.send(peerSocket);
					}
					
				break;
				case MessageType.SOCKET_REGISTRATION:
					byte[] peerData = new byte[data.length-4];
					int pos2 = 0;
					for(int i=0; i<peerData.length; i++){
						peerData[i] = data[pos2];
						pos2++;
					}
					PeerID peer = new PeerID(peerData);
					byte[] portBytes = new byte[4];
					for (int i=0; i<4; i++){
						portBytes[i] = data[pos2];
						pos2++;
					}
					int port = Helper.byteArrayToInt(portBytes);
//					if(debug) System.out.println("creating socket entry: " + peer.getID() + 						
//							" " + socket.getInetAddress().getCanonicalHostName() + ":" + socket.getPort() + " / " + port);
					Socket newSocket = null;
					try {
						newSocket = new Socket(socket.getInetAddress().getCanonicalHostName(), port);
					} catch (UnknownHostException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					routerNode.addConnection(peer.getID(),newSocket);
//					if(debug) System.out.println(routerNode.printConnectionList());
				break;
				case MessageType.LINK_WEIGHT_UPDATE:
					LinkWeightUpdate update = new LinkWeightUpdate(data);
					routerNode.setLinkList(Helper.updateLinks(update.getLinkList()));
				break;
				case MessageType.DEREGISTER_RESPONSE:
					RegisterResponse response2 = new RegisterResponse(data);
					System.out.println(response2.getInfo());
					if(response2.getStatus()==RegisterResponse.failure) System.exit(0);	
				break;
				case MessageType.EXIT:
					routerNode.exitCDN();
					System.exit(0);
				}
				
		}
		
	}

}
