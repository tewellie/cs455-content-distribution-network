package cdn;

import java.util.*;

import javax.xml.ws.Holder;

/*
 * Once the setup-cdn command is specified at the disvoery node, it must perform a series of actions that create the CDN
 * via router nodes initiating connections with each other. Routers await instructions from discovery node regarding other routers that they must establish connections to.
 * Discovery node must ensure:
 * 		number of links to/from (links are bidirectional) every router in CDN is identical; this is a configurable metric (With a default of 4) and is specified as part of the setup-cdn command
 * 		there is no partition within the CDN (every router must be able to reach every other router)
 * 
 * If the connection requirement for the CDN is Cr, each router will have Cr links to other routers in the CDN
 * **Discovery nodes selects these Cr routers that constitute peer-router list for a router randomly
 * However, a check should be performed to ensure that the peer-router list for a router does not include the targeted router (router should not connect to itself)
 * 
 * Discovery node keeps track of connections that are being created, 
 * for example, if router A is asked to connect to router B, counts for both are incremented
 * Discovery node must ensure connection counts are met and not breached.
 * 
 * Discovery node sends a different list of routers to each router in the CDN
 * depending on connections that were previously set up, number of peer routers included in message to routers may vary from Cr through 1
 * If a router's connection limit was reached through previous messages sent to other routers in the CDN, no message needs to be sent to that router
 * 
 * Peer-list message will have following format:
 * 		Message Type: PEER_ROUTER_LIST
 * 		Number of peer routers: X
 * 		Router1 Info
 * 		Router2 Info
 * 		...
 * 		RouterX Info
 * 
 * Upon receiving the PEER_ROUTER_LIST message a router should initate connections to the specified routers
 */

public class PeerRouterList {
	
	private int numPeers;
	//arrayList to keep of router info
	//need info about Router IP and Port Number
	private ArrayList<RouterInfo> peerList;
	
	//need to construct the peerrouterlist and a message for and deconstrct into a peer router list again
	PeerRouterList(ArrayList<RouterInfo> peers, int noPeers){
		peerList = peers;
		numPeers = noPeers;
	}
	
	//unmarshal a peerrouterlist
	PeerRouterList(byte[] list){
		byte[] tempNumPeers = new byte[4];
		int pos = 0;
		for(int i=0; i<4; i++){
			tempNumPeers[i] = list[pos];
			pos++;
		}
		numPeers = Helper.byteArrayToInt(tempNumPeers);
		peerList = new ArrayList<RouterInfo>();
		
		//get peers
		for(int i=0; i<numPeers; i++){
			//get size routerinfo
			byte[] sizeInfoBytes = new byte[4];
			for(int j=0; j<4; j++){
				sizeInfoBytes[j] = list[pos];
				pos++;
			}
			int sizeInfo = Helper.byteArrayToInt(sizeInfoBytes);
			//get routerInfo
			byte[] infoBytes = new byte[sizeInfo];
			for(int j=0; j<sizeInfo; j++){
				infoBytes[j] = list[pos];
				pos++;
			}
			RouterInfo info = new RouterInfo(infoBytes);
			peerList.add(info);
		}
	}
	
	/*
	 * format: numPeers lengthIP IP Port lengthIP IP Port lengthIP IP Port...
	 */
	public byte[] toByteArray(){
		//have 4bytes for number peers, 4bytes per peer for size of routerInfo and size of each routerInfo
		int numBytesNeeded = 4 + 4*numPeers;
		for(int i=0; i<numPeers; i++){
			numBytesNeeded += peerList.get(i).toByteArray().length;	
		}
		
		byte[] temp = new byte[numBytesNeeded];
		int pos=0;
		//encode number of peers
		for(int i=0; i<4; i++){
			temp[pos] = Helper.intToByteArray(numPeers)[i];
			pos++;
		}
		//encode each peer in list
		for(int i=0; i<numPeers; i++){
			//encode size of routerinfo
			byte[] sizeRouterInfo = Helper.intToByteArray(peerList.get(i).toByteArray().length);
			for(int j=0; j<4; j++){
				temp[pos] = sizeRouterInfo[j];
				pos++;
			}
			//encode routerinfo
			byte[] tempBytes = peerList.get(i).toByteArray();
			for(int j=0; j<tempBytes.length; j++){
				temp[pos] = tempBytes[j];
				pos++;
			}
			
		}
		
		return temp;
	}
	
	public void incPeerCount(){
		numPeers++;
	}

	public void addPeer(RouterInfo newPeer){
		peerList.add(newPeer);
	}
	
	public String toString(){
		String peerString = "Number peers: " + numPeers;
		for(int i=0; i<numPeers; i++){
			peerString+= " " + peerList.get(i).toString();
		}
		return peerString;
	}
	
	public ArrayList<RouterInfo> getPeerList(){
		return peerList;
	}

}
