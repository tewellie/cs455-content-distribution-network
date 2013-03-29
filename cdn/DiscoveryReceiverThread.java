package cdn;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.*;

public class DiscoveryReceiverThread implements Runnable {
	
	private Socket socket;
	private String socketIP;
	private Discovery discoveryNode;
	private ServerSocket discoveryServer;
	
	DiscoveryReceiverThread(Discovery node, ServerSocket server){
		discoveryNode = node;
		discoveryServer = server;
	}

	@Override
	public void run() {
		
		
		//create a socket to listen on the server socket
		socket = null;
		try {
			socket = discoveryServer.accept();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		
		//if a socket has been created it means a Router is connected on the socket 
		//spawn a new thread to create another socket to listen for more connection requests
		Thread discoveryReceiverThread = new Thread(new DiscoveryReceiverThread(discoveryNode, discoveryServer));
		discoveryReceiverThread.start();
		
		while(true){
			
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
				
				//handle the message depending on what type it is....
				switch(messageType){
				case MessageType.REGISTER_REQUEST: 
					RegisterRequest request = new RegisterRequest(data);
					RegisterResponse response = discoveryNode.register(request,socketIP, socket);
					Message regResponse = new Message(response.toByteArray(), MessageType.REGISTER_RESPONSE);
					regResponse.send(socket);
				break;
				case MessageType.DEREGISTER_REQUEST:
					RegisterRequest deregister = new RegisterRequest(data);
					RegisterResponse response2 = discoveryNode.deregister(deregister,socketIP, socket);
					Message deregRepsonse = new Message(response2.toByteArray(), MessageType.DEREGISTER_RESPONSE);
					deregRepsonse.send(socket);
				}
				
		}
		//end of while loop
		
		//close the socket - unreachable until loop has a terminating conditions
//		try {
//			socket.close();
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		//close server socket
//		try {
//			server.close();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
	}

}
