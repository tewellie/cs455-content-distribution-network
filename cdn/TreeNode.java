package cdn;

import java.util.*;

public class TreeNode {
	
	private String id;
	private TreeNode parent;
	private ArrayList<TreeNode> children;
	
	TreeNode(String ID, TreeNode parentNode){
		id = ID;
		parent = parentNode;
		children = new ArrayList<TreeNode>();
	}
	
	TreeNode(String ID){
		id = ID;
		children = new ArrayList<TreeNode>();
	}
	
	
	public String getID(){
		return id;
	}
	
	public TreeNode getParent(){
		return parent;
	}
	
	public ArrayList<TreeNode> getChildren(){
		return children;
	}
	
	public int getNumbChildren(){
		return children.size();
	}
	
	public void addChild(TreeNode child){
		children.add(child);
	}
	
	public boolean equals(TreeNode other){
		if(other.id.equals(this.id)) return true;
		else return false;
	}
	


}
