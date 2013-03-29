package cdn;

/*
 * When a discovery node receives a request, it checks to see:
 *   if node is already registered
 *   ensures IP address in message matches where request was originated.
 * 
 * Discovery node issues an error message if:
 *   1. router had previously registered with it and has valid entry in its registry
 *   2. there is a mismatch in address that is specified in registration request and IP address of request (socket's input stream)
 * 
 * Contents of the message - success of failure of registration request should be indicated in status field of response
 * 		Message Type (int): REGISTER_RESPONSE
 * 		Status Code (byte): SUCCESS or FAILURE
 * 		Additional Info (String):
 * 
 * In case of successful registration, discovery node should include message that indicates number of entries currently present in registry
 * 		i.e. "Registration request successful. The number of routers currently constituting the CDN is (5)"
 * If registration unsuccessful, discovery node should indicate why the request was unsuccessful
 * 
 * Note: in the rare case that a router node fails just after it sends registration request, discovery node will be unable
 * to communicate with it; the entry for the router node should be removed from router-registery maintained at discovery node
 *   
 */

public class RegisterResponse {
	private byte status;
	private String info;
	public final static byte success = 0x1;
	public final static byte failure = 0x0;
	
	
	RegisterResponse(byte stat){
		status = stat;
	}
	
	RegisterResponse(byte stat, String information){
		status = stat;
		info = information;
	}
	
	RegisterResponse(byte[] data){
		//get status
		status = data[0];
		int pos =1;
		//get size of info string
		byte[] infosize = new byte[4];
		for(int i=0; i<4; i++){
			infosize[i]=data[pos];
			pos++;
		}
		int infoSize = Helper.byteArrayToInt(infosize);
		//get info string
		byte[]temp = new byte[data.length-1];
		for(int i=0;i<infoSize;i++){
			temp[i] = data[pos];
			pos++;
		}
		info = new String(temp);
	}
	
	public byte[] toByteArray(){
		int pos =0;
		//status code, size of message, message
		byte[] response = new byte[1+4+info.getBytes().length];
		response[0] = status;
		pos++;
		//first 4 bytes are size of info string
		for(int i=0; i<4; i++){
			response[pos] = Helper.intToByteArray(info.getBytes().length)[i];
			pos++;
		}
		//then the string
		for(int i=0; i<info.getBytes().length;i++){
			response[pos]=info.getBytes()[i];
			pos++;
		}		
		return response;
	}

	public String getInfo(){
		return info;
	}
	
	public byte getStatus(){
		return status;
	}
}
