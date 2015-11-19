package jrtr;

import java.util.LinkedList;

public abstract class Leaf implements Node{
	
	@Override
	public final LinkedList<Node> getChildren() {
		return null;
	}
}