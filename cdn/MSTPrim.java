package cdn;

import java.util.*;

public class MSTPrim {
	
	//1. create a new tree with root node (in constructor)
	//2. consider edges in ascending order of weight, add edge if adding it adds a new node to graph
	
	// or create with tree
	// add min edge such that connects a new node...
	
	private Tree minSpanningTree;
	private ArrayList<LinkInfo> links; //updated to represent links not yet considered in MST
	private ArrayList<LinkInfo> potentialConnections;

	
	MSTPrim(String rootID, ArrayList<String> nodes, ArrayList<LinkInfo> linkInfo){
		minSpanningTree = new Tree(new TreeNode(rootID));
		links = linkInfo;
		potentialConnections = new ArrayList<LinkInfo>();
		updatePotentialConnectionList(rootID);
		while(potentialConnections.size()!=0){
			growTree();
		}
	}
	
	MSTPrim(Tree mst){
		minSpanningTree = mst;
	}
	
	public ArrayList<LinkInfo> getPeers(String id){
		return minSpanningTree.getLinks(id);
	}
	
	public Tree getTree(){
		return minSpanningTree;
	}
	
	private void growTree(){
		//find min edge with root in it... then min edge including ones with root in it
		//maybe keep list of potential edges?
		
		//add edges from A to list of potentialConnections, pick minimum one, add to tree, update potentialConnections list
		//find min edge
		LinkInfo minEdge = potentialConnections.get(0);
		String idA; //in tree
		String idB; //not
		if(minSpanningTree.contains(minEdge.getID().A)){
			idA = minEdge.getID().A;
			idB = minEdge.getID().B;
		}
		else{
			idA = minEdge.getID().B;
			idB = minEdge.getID().A;
		}

		
		//new node tree
		TreeNode newNode = new TreeNode(idB,minSpanningTree.getNode(idA));
		//add new node to child of node already in tree
		minSpanningTree.getNode(idA).addChild(newNode);
		//add node to tree
		minSpanningTree.addNode(idB, newNode);
		//add link to tree
		minSpanningTree.addLink(idA, minEdge);
		//remove links to new node as potential connection options
		removePotentialConnections(idB);
		//remove links that contain two nodes in graph
		updateLinks();
		//add links from new node as potential connections
		updatePotentialConnectionList(idB);
		

	}
	
	//remove link if both nodes are in tree already
	private void updateLinks(){
		int i=0;
		while(i<links.size()){
			if(minSpanningTree.contains(links.get(i).getID().A) && minSpanningTree.contains(links.get(i).getID().B)){
				links.remove(i);
			}
			else i++;
		}
	}
	
	//remove connections if both nodes in tree
	private void removePotentialConnections(String id){
		int i=0;
		while(i<potentialConnections.size()){
			if(minSpanningTree.contains(potentialConnections.get(i).getID().A) && minSpanningTree.contains(potentialConnections.get(i).getID().B)){
				potentialConnections.remove(i);
			}
			else i++;
		}
	}
	
	//add connection in sorted order (By weight)
	private void addPotentialConnection(LinkInfo newConnection){
		
		for(int i=0; i<potentialConnections.size();i++){
			if(newConnection.getWeight()<potentialConnections.get(i).getWeight()){
				potentialConnections.add(i, newConnection);
				return;
			}
		}
		potentialConnections.add(newConnection);
	}
	
	//moves links containing String id from links to potentialConnections 
	private void updatePotentialConnectionList(String id){
		for(int i=0; i<links.size(); i++){
			//if the link contains this id, add it to potential list, remove from link list
			if(links.get(i).getID().contains(id)){
				addPotentialConnection(links.get(i));
			}
		}
		
		int i=0;
		while(i!=links.size()){
			if(potentialConnections.contains(links.get(i))){
				links.remove(i);
			}
			else i++;
		}
		
	}
	
	
	
	
	
	

}
