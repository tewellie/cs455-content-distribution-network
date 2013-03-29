package cdn;

import java.net.*;

/*
 * Upon starting up, each router registers its IP addresss, port number, and assigned-ID with discovery node.
 * It should be possible for your system to register routers that are running on same host but listening to communications on different ports.
 * 4 fields in registration request:
 * 		Messsage Type (int): REGISTER_REQUEST
 * 		IP address (String)
 * 		Port number (int)
 * 		Assigned ID (String)
 * 
 */

public class RegisterRequest {
	private String IP;
	private int portNumb;
	private String ID;
	
	RegisterRequest(String ip, int pn, String id){
		IP = ip;
		portNumb = pn;
		ID=id;
	}
	
	RegisterRequest(byte[] array){
		//parse out array for components of request
		//IP
		byte[] ipSize = new byte [4];
		int pos = 0;
		for (int i=0; i<4;i++){
			ipSize[i] = array[pos];
			pos++;
		}
		int IPSize = Helper.byteArrayToInt(ipSize);
		byte[] ip = new byte[IPSize];
		for(int i=0; i<IPSize; i++){
			ip[i] = array[pos];
			pos++;
		}
		IP = new String(ip);
		//port
		byte[] port = new byte [4];
		for (int i=0; i<4;i++){
			port[i] = array[pos];
			pos++;
		}
		portNumb = Helper.byteArrayToInt(port);
		//ID
		byte[] idSize = new byte [4];
		for (int i=0; i<4;i++){
			idSize[i] = array[pos];
			pos++;
		}
		int IDSize = Helper.byteArrayToInt(idSize);
		byte[] id = new byte[IDSize];
		for(int i=0; i<IDSize; i++){
			id[i] = array[pos];
			pos++;
		}
		ID = new String(id);
	}
	
	
	public String getID(){
		return ID;
	}
	
	public String getIP(){
		return IP;
	}
	
	public int getPort(){
		return portNumb;
	}
	
	public byte[] toByteArray(){
		//pos in temp array
		int pos=0;
		//size of temp is 4 ints + size of IP + size of ID
		byte[] temp = new byte[16+ID.getBytes().length+IP.getBytes().length];

		//first 4 bytes are size of IP string
		for(int i=0; i<4; i++){
			temp[pos] = this.getIPSizeBytes()[i];
			pos++;
		}
		//then the actual IP address
		for(int i=0; i<IP.getBytes().length;i++){
			temp[pos]=this.getIPBytes()[i];
			pos++;
		}
		//port number
		for(int i=0; i<4; i++){
			temp[pos] = this.getPortNumbBytes()[i];
			pos++;
		}
		//ID length
		for(int i=0; i<4; i++){
			temp[pos] = this.getIDSizeBytes()[i];
			pos++;
		}
		//ID
		for(int i=0; i<ID.getBytes().length;i++){
			temp[pos] = this.getIDBytes()[i];
		}
		return temp;
	}
	
	private byte[] getIPSizeBytes(){
		return Helper.intToByteArray(IP.getBytes().length);
	}
	
	private byte[] getIPBytes(){
		return IP.getBytes();
	}
	
	private byte[] getPortNumbBytes(){
		return Helper.intToByteArray(portNumb);
	}
	
	private byte[] getIDSizeBytes(){
		return Helper.intToByteArray(ID.getBytes().length);
	}
	
	private byte[] getIDBytes(){
		return ID.getBytes();
	}
	
	//for use in router registration
	public boolean equals(RegisterRequest other){
		if(this.IP.equals(other.IP) && this.portNumb == other.portNumb) return true;
		else return false;
	}
	
	public String toString(){
		return ID + " " + IP + ":" + portNumb;
	}
	
}
