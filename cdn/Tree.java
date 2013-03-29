package cdn;

import java.util.*;

public class Tree {
	
	private TreeNode root;
	//keep track of links connecting children (adjacency list)
	private Hashtable<String,ArrayList<LinkInfo>> links;
	private Hashtable<String,TreeNode> nodes;
	
	Tree(TreeNode rootNode){
		links = new Hashtable<String, ArrayList<LinkInfo>>();
		root = rootNode;
		nodes = new Hashtable<String, TreeNode>();
		nodes.put(rootNode.getID(), root);
		links.put(rootNode.getID(), new ArrayList<LinkInfo>());
	}
	
	
	//note: link weight not encoded currently so weight is 0, but weight not needed in routing plan
	Tree(byte[] bytes){

		links = new Hashtable<String, ArrayList<LinkInfo>>();
		nodes = new Hashtable<String, TreeNode>();
		
		//size root id, root id number routers, length router ID, router ID, number peers, length peer id, peer id
		int pos = 0;
		//get size root id
		byte[] sizeRootBytes = new byte[4];
		for(int i=0; i<4; i++){
			sizeRootBytes[i] = bytes[pos];
			pos++;
		}
		int sizeRoot = Helper.byteArrayToInt(sizeRootBytes);
		//get root id
		byte[] rootIDBytes = new byte[sizeRoot];
		for(int i=0; i<sizeRoot; i++){
			rootIDBytes[i] = bytes[pos];
			pos++;
		}
		root = new TreeNode(new String(rootIDBytes));
		//get number routers
		byte[] numRoutersBytes = new byte[4];
		for(int i=0; i<4; i++){
			numRoutersBytes[i] = bytes[pos];
			pos++;
		}
		int numRouters = Helper.byteArrayToInt(numRoutersBytes);
		//for each router get ID and place in links and nodes
		for(int i=0; i<numRouters; i++){
			//size routerID
			byte[] sizeIDBytes = new byte[4];
			for(int j=0; j<4; j++){
				sizeIDBytes[j] = bytes[pos];
				pos++;
			}
			int sizeID = Helper.byteArrayToInt(sizeIDBytes);
			//router ID
			byte[] idBytes = new byte[sizeID];
			for(int j=0; j<sizeID; j++){
				idBytes[j] = bytes[pos];
				pos++;
			}
			String id = new String(idBytes);
			//place in links and nodes;
			addNode(id, new TreeNode(id));
			//number peers
			byte[] numPeersBytes = new byte[4];
			for(int j=0; j<4; j++){
				numPeersBytes[j] = bytes[pos];
				pos++;
			}
			int numPeers = Helper.byteArrayToInt(numPeersBytes);

			//for each peer
			for(int j=0; j<numPeers; j++){
				//get size peer ID
				byte[] sizePeerIDBytes = new byte[4];
				for(int k=0; k<4; k++){
					sizePeerIDBytes[k] = bytes[pos];
					pos++;
				}
				int sizePeerID = Helper.byteArrayToInt(sizePeerIDBytes);
				//get peer ID
				byte[] peerIDBytes = new byte[sizePeerID];
				for(int k=0; k<sizePeerID; k++){
					peerIDBytes[k] = bytes[pos];
					pos++;
				}
				String peerID = new String(peerIDBytes);
				//create link
				LinkInfo newlink = new LinkInfo(new Pair<String, String>(id, peerID), 0, id, peerID);
				//add to links
				addLink(id, newlink);
			}
		}
	}
	
	public TreeNode getRoot(){
		return root;
	}
	
	public boolean contains(String node){
		return nodes.keySet().contains(node);
	}
	
	public boolean contains(TreeNode node){
		return nodes.contains(node);
	}
	
	public TreeNode getNode(String id){
		return nodes.get(id);
	}
	
	public void addNode(String id, TreeNode node){
		nodes.put(id, node);
		links.put(id, new ArrayList<LinkInfo>());
	}
	
	public void addLink(String id, LinkInfo link){
 		links.get(id).add(link);
	}
	
	public int size(){
		return nodes.size();
	}
	
	public ArrayList<LinkInfo> getLinks(String id){
		return links.get(id);
	}
	
	public void print(){
		//perform depth first search on tree eventually
		for(String node:links.keySet()){
			System.out.println("--" +node + " links--");
			for(LinkInfo link:links.get(node)){
				System.out.println(link.toString());
			}
		}
	}
	
	public byte[] toByteArray(){
		//encoding of tree
		//size rootID, rootID, number routers, length router ID, router ID, number peers, length peer id, peer id, peer
		//-4, -4*number routers, -length each routerID, -4*number routers, -4*number peers for each router, -length each peer id
		//size root ID, number routers, size router IDs, number peers for each router
		int sizeByteArray = 4 + 4 + 4*links.size()*2;
		sizeByteArray += root.getID().getBytes().length;
		for(String key:links.keySet()){
			//length each router ID
			sizeByteArray += key.getBytes().length;
			ArrayList<LinkInfo> temp = links.get(key);
			//4*number peers (size peer id)
			sizeByteArray += 4*temp.size();
			//space for each peer id
			for(LinkInfo link:temp){
				Pair<String, String> tempID = link.getID();
				//determine which peer ID To encode
				if(link.getID().A.equals(key)) sizeByteArray += link.getID().B.getBytes().length;
				else sizeByteArray += link.getID().A.getBytes().length;
			}					
		}
		
		byte[] result = new byte[sizeByteArray];
		int pos = 0;
		//encode size root
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(root.getID().getBytes().length)[i];
			pos++;
		}
		//encode root ID
		for(int i=0; i<root.getID().getBytes().length; i++){
			result[pos] = root.getID().getBytes()[i];
			pos++;
		}
		
		//encode number of routers
		for(int i=0; i<4; i++){
			result[pos] = Helper.intToByteArray(links.size())[i];
			pos++;
		}
		//for each router
		for(String key:links.keySet()){
			ArrayList<LinkInfo> peers = links.get(key);
			//size id
			for(int i=0; i<4; i++){
				result[pos] = Helper.intToByteArray(key.getBytes().length)[i];
				pos++;
			}
			//id
			for(int i=0; i<key.getBytes().length; i++){
				result[pos] = key.getBytes()[i];
				pos++;
			}
			
			//number peers
			for(int i=0; i<4; i++){
				result[pos] = Helper.intToByteArray(peers.size())[i];
				pos++;
			}
			//for each peer
			for(LinkInfo peer:peers){
				//determine which peer string to use
				String peerID;
				if(peer.getID().A.equals(key)) peerID = peer.getID().B;
				else peerID = peer.getID().A;
				//size peer id
				for(int i=0; i<4; i++){
					result[pos] = Helper.intToByteArray(peerID.getBytes().length)[i];
					pos++;
				}
				//peer id
				for(int i=0; i<peerID.getBytes().length; i++){
					result[pos] = peerID.getBytes()[i];
					pos++;
				}
			}
			
			
			
		}
		
		return result;
		
	}
	
	

}
