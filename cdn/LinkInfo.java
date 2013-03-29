package cdn;

import java.net.InetAddress;
import java.net.UnknownHostException;

/*
 * A LinkInfo connecting routers A and B contains the following fields: A B weight hostnameA:portnumbA hostnameB:portnumbB
 */



public class LinkInfo {
	private Pair<String, String> linkID;
	private int weight;
	private String ipA;
	private String ipB;
	
	LinkInfo(Pair<String, String> ID, int linkWeight, String IPA, String IPB){
		linkID = ID;
		weight = linkWeight;
		ipA = IPA;
		ipB = IPB;
	}
	
	LinkInfo(byte[] info){
		//idA size, idA, idBsize, idB, weight, string1 size, string1, string2 size, string2
		int pos=0;
		//idA size
		byte[] idASizeBytes = new byte[4];
		for(int i=0; i<4; i++){
			idASizeBytes[i] = info[pos];
			pos++;
		}
		int idASize = Helper.byteArrayToInt(idASizeBytes);
		//idA
		byte[] idABytes = new byte[idASize];
		for(int i=0; i<idASize; i++){
			idABytes[i] = info[pos];
			pos++;
		}
		String idA = new String(idABytes);
		//idB size
		byte[] idBSizeBytes = new byte[4];
		for(int i=0; i<4; i++){
			idBSizeBytes[i] = info[pos];
			pos++;
		}
		int idBSize = Helper.byteArrayToInt(idBSizeBytes);
		//idB
		byte[] idBBytes = new byte[idBSize];
		for(int i=0; i<idBSize; i++){
			idBBytes[i] = info[pos];
			pos++;
		}
		String idB = new String(idBBytes);
		linkID = new Pair<String, String>(idA, idB);
		//weight
		byte[] weightBytes = new byte[4];
		for(int i=0; i<4; i++){
			weightBytes[i] = info[pos];
			pos++;
		}
		weight = Helper.byteArrayToInt(weightBytes);
		//ipA size
		byte[] string1SizeBytes = new byte[4];
		for(int i=0; i<4; i++){
			string1SizeBytes[i] = info[pos];
			pos++;
		}
		int string1Size = Helper.byteArrayToInt(string1SizeBytes);
		//ipA
		byte[] string1Bytes = new byte[string1Size];
		for(int i=0; i<string1Size; i++){
			string1Bytes[i] = info[pos];
			pos++;
		}
		ipA = new String(string1Bytes);
		//ipB size
		byte[] string2SizeBytes = new byte[4];
		for(int i=0; i<4; i++){
			string2SizeBytes[i] = info[pos];
			pos++;
		}
		int string2Size = Helper.byteArrayToInt(string2SizeBytes);
		//ipB
		byte[] string2Bytes = new byte[string2Size];
		for(int i=0; i<string2Size; i++){
			string2Bytes[i] = info[pos];
			pos++;
		}
		ipB = new String(string2Bytes);
	}
	
	public byte[] toByteArray(){
		//idA size, idA, idB size, idB, weight, string1 size, string1, string2 size, string2
		int sizeArray = 5*4;
		sizeArray += linkID.A.getBytes().length + linkID.B.getBytes().length;
		sizeArray += ipA.getBytes().length + ipB.getBytes().length;
		int pos=0;
		byte[] result = new byte[sizeArray];
		//size IDA
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(linkID.A.getBytes().length)[i];
			pos++;
		}
		//IDA
		for(int i=0; i<linkID.A.getBytes().length; i++){
			result[pos] = linkID.A.getBytes()[i];
			pos++;
		}
		//size IDB
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(linkID.B.getBytes().length)[i];
			pos++;
		}
		//IDB
		for(int i=0; i<linkID.B.getBytes().length; i++){
			result[pos] = linkID.B.getBytes()[i];
			pos++;
		}
		//weight
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(weight)[i];
			pos++;
		}
		//size string1
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(ipA.getBytes().length)[i];
			pos++;
		}
		//string1
		for(int i=0; i<ipA.getBytes().length; i++){
			result[pos] = ipA.getBytes()[i];
			pos++;
		}
		//size string2
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(ipB.getBytes().length)[i];
			pos++;
		}
		//string2
		for(int i=0; i<ipB.getBytes().length; i++){
			result[pos] = ipB.getBytes()[i];
			pos++;
		}
		return result;
		
	}
	
	public Pair<String, String> getID(){
		return linkID;
	}
	
	public int getWeight(){
		return weight;
	}
	
	public void setWeight(int newWeight){
		weight = newWeight;
	}
	
	public String getIPA(){
		return ipA;
	}
	
	public String getIPB(){
		return ipB;
	}

	public String toString(){
		String result = linkID.toString();
		result +=  " " + weight + " " 
				+ ipA + " " + ipB;
		return result;
	}
	
//	public String getIDByHost(String host){
//		if(host.equals(ipA)) return linkID.A;
//		else if(host.equals(ipB)) return linkID.B;
//		else return null;
//	}
}
