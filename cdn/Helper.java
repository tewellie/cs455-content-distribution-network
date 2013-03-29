package cdn;

import java.util.*;

public class Helper {

	
	public static final byte[] intToByteArray(int value) {
	    return new byte[] {
	            (byte)(value >>> 24),
	            (byte)(value >>> 16),
	            (byte)(value >>> 8),
	            (byte)value};
	}
	
	public static final int byteArrayToInt(byte[] array){
		int value = 0;
		for (int i = 0; i < 4; i++)
		{
		   value = (value << 8) + (array[i] & 0xff);
		}
		return value;
	}
	
	public static Hashtable<Pair<String, String>, LinkInfo> updateLinks(ArrayList<LinkInfo> newLinks){
		Hashtable<Pair<String, String>, LinkInfo> newTable = new Hashtable<Pair<String, String>, LinkInfo>();
		for(LinkInfo link:newLinks){
			newTable.put(link.getID(), link);
		}
		return newTable;
	}
}
