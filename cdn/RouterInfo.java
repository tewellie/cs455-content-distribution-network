package cdn;

/*
 * Router Info contains the following fields: router_id  router_hostname:portnum
 * 		
 */

public class RouterInfo {
	
	private String ID;
	private String hostName;
	
	RouterInfo(String routerID, String routerName){
		ID = routerID;
		hostName = routerName;
	}
	
	RouterInfo(byte[] routerInfo){
		//ID Size
		byte[] sizeIDBytes = new byte[4];
		int pos =0;
		for(int i=0; i<4; i++){
			sizeIDBytes[i] = routerInfo[pos];
			pos++;
		}
		int sizeID = Helper.byteArrayToInt(sizeIDBytes);
		//ID
		byte[] IDBytes = new byte[sizeID];
		for(int i=0; i<sizeID; i++){
			IDBytes[i] = routerInfo[pos];
			pos++;
		}
		ID = new String(IDBytes);
		//hostName size
		byte[] sizeHostNameBytes = new byte[4];
		for(int i=0; i<4; i++){
			sizeHostNameBytes[i] = routerInfo[pos];
			pos++;
		}
		int sizeHostName = Helper.byteArrayToInt(sizeHostNameBytes);
		//hostName
		byte[] hostNameBytes = new byte[sizeHostName];
		for(int i=0; i<sizeHostName; i++){
			hostNameBytes[i] = routerInfo[pos];
			pos++;
		}
		hostName = new String(hostNameBytes);
		
	}
	
	public byte[] toByteArray(){
		//sizeID ID sizeHostName HostName
		int sizeArray = 8 + ID.getBytes().length + hostName.getBytes().length;
		byte[] result = new byte[sizeArray];
		int pos=0;
		//size ID
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(ID.getBytes().length)[i];
			pos++;
		}
		//encode ID
		for(int i=0; i<ID.getBytes().length; i++){
			result[pos] = ID.getBytes()[i];
			pos++;
		}
		//hostName size
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(hostName.getBytes().length)[i];
			pos++;
		}
		//encode hostName
		for(int i=0; i<hostName.getBytes().length; i++){
			result[pos] = hostName.getBytes()[i];
			pos++;
		}
		return result;
		
		
	}
	
	public String toString(){
		return ID + " " + hostName;
	}
	
	public String getIP(){
		int posColon = hostName.lastIndexOf(":");
		return hostName.substring(0, posColon);
	}
	
	public int getPort(){
		int posColon = hostName.lastIndexOf(":");
		return Integer.parseInt(hostName.substring(posColon+1,hostName.length()));
	}
	
	public String getID(){
		return ID;
	}

}
