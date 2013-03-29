package cdn;

import java.io.IOException;
import java.net.*;
import java.util.*;



public class LinkReceiverThread implements Runnable {
	
	private Socket socket;
	private Router routerNode;
	private ServerSocket routerServer;
	
	LinkReceiverThread(Router node, ServerSocket server){
		routerNode = node;
		routerServer = server;
	}

	@Override
	public void run() {


		//create a socket to listen on the server socket
		socket = null;
		try {
			socket = routerServer.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		//if a socket has been created it means a Router is connected on the socket 
		//spawn a new thread to create another socket to listen for more connection requests
		Thread linkReceiverThread = new Thread(new LinkReceiverThread(routerNode, routerServer));
		linkReceiverThread.start();
		
		//start waiting for messages on this socket
		//thread for listening for communication with discovery node
		RouterReceiverThread routerReceiverThread = new RouterReceiverThread(socket, routerNode);
		routerReceiverThread.start();
		
	}
	
	

}
