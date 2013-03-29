package cdn;

import java.util.*;

/*
 * discovery node is responsible for assigning and updating weights connecting to CDN
 * weight for each link is an integer between 1-10 and randomly computed by discovery node when refresh-interval elapses
 * new weights for links are forwarded to router nodes by discovery node
 * 
 * info encoded in weight update message:
 * 		Message Type: Link_Weight_Update
 * 		Number of links: L
 * 		Linkinfo1
 * 		Linkinfo2
 * 		...
 * 		Linkinfo L
 * 
 */

public class LinkWeightUpdate {
	
	private int numbLinks;
	private ArrayList<LinkInfo> links;
	
	LinkWeightUpdate(int numb, ArrayList<LinkInfo> linkList){
		links = linkList;
		numbLinks = numb;
	}
	
	LinkWeightUpdate(byte[] array){
		links = new ArrayList<LinkInfo>();
		int pos=0;
		//weight
		byte[] weightBytes = new byte[4];
		for(int i=0; i<4; i++){
			weightBytes[i] = array[pos];
			pos++;
		}
		numbLinks = Helper.byteArrayToInt(weightBytes);
		//get each link
		for(int i=0; i<numbLinks; i++){
			//size of link info
			byte[] sizeInfoBytes = new byte[4];
			for(int j=0; j<4; j++){
				sizeInfoBytes[j] = array[pos];
				pos++;
			}
			int sizeInfo = Helper.byteArrayToInt(sizeInfoBytes);
			//get info
			byte[] infoBytes = new byte[sizeInfo];
			for(int j=0; j<sizeInfo; j++){
				infoBytes[j] = array[pos];
				pos++;
			}
			LinkInfo newLink = new LinkInfo(infoBytes);
			links.add(newLink);
		}
	}
	
	public byte[] toByteArray(){
		//numb links, size link info, encoding for each link info
		int sizeArray = 4 + 4*numbLinks;
		for(int i=0; i<links.size(); i++){
			sizeArray += links.get(i).toByteArray().length;
		}
		byte[] result = new byte[sizeArray];
		int pos = 0;
		//encode size
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(numbLinks)[i];
			pos++;
		}
		//encode each link info
		for(int i=0; i<links.size();i++){
			//encode linkinfo size
			for(int j=0; j<4; j++){
				result[pos] = Helper.intToByteArray(links.get(i).toByteArray().length)[j];
				pos++;
			}
			//encode link info
			for(int j=0; j<links.get(i).toByteArray().length;j++){
				result[pos] = links.get(i).toByteArray()[j];
				pos++;
			}
		}
		
		return result;
	}
	
	public int getNumbLinks(){
		return numbLinks;
	}
	
	public ArrayList<LinkInfo> getLinkList(){
		return links;
	}
	

}
