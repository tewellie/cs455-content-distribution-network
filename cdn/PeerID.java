package cdn;

public class PeerID {
	
	private String ID;
	
	PeerID(String id){
		ID = id;
	}
	
	PeerID(byte[] id){
		byte[] sizeIDBytes = new byte[4];
		int pos = 0;
		for(int i=0; i<4; i++){
			sizeIDBytes[i] = id[pos];
			pos++;
		}
		int sizeID = Helper.byteArrayToInt(sizeIDBytes);
		byte[] idBytes = new byte[sizeID];
		for(int i=0; i<sizeID; i++){
			idBytes[i] = id[pos];
			pos++;
		}
		ID = new String (idBytes);
	}
	
	public byte[] toByteArray(){
		byte[] result = new byte[4 + ID.getBytes().length];
		int pos=0;
		for(int i=0; i<4;i++){
			result[pos] = Helper.intToByteArray(ID.getBytes().length)[i];
			pos++;
		}
		for(int i=0; i<ID.getBytes().length; i++){
			result[pos] = ID.getBytes()[i];
			pos++;
		}
		return result;
	}
	
	public String getID(){
		return ID;
	}

}
