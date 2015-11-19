package jrtr;

import java.util.LinkedList;

public abstract class Group implements Node{
	
	private LinkedList<Node> children = new LinkedList<Node>();

	public final void addChild(Node child){
		children.addFirst(child);
	}
	
	public final void removeChild(Node child){
		children.remove(child);
	}

	@Override
	public final LinkedList<Node> getChildren() {
		return children;
	}
}