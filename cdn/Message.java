package cdn;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

public class Message {
	


	private byte[] data;
	private int type;
	
	
	/*construct a byte array encoding of hte message*/
	public Message(String text, int messageType){
		data = text.getBytes();
		type = messageType;
	}
	
	public Message(byte[] dataInput, int messageType){
		data = dataInput;
		type = messageType;
	}
	
	public byte[] getByteArray(){
		return data;
	}
	
	public byte[] getSizeOfData(){
		return Helper.intToByteArray(data.length);
	}
	
	//send the message across a socket
	public void send(Socket socket){
		//create an output stream for the socket
		OutputStream socketOutputStream = null;
		try {
			socketOutputStream = socket.getOutputStream();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		//write the message to the output stream for the socket
		//format for output stream: message type, message size, message
		try {
			socketOutputStream.write(Helper.intToByteArray(this.type));
			socketOutputStream.write(this.getSizeOfData());
			socketOutputStream.write(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}
