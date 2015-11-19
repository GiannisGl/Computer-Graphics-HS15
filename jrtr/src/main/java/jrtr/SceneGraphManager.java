package jrtr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import javax.vecmath.Matrix4f;

public class SceneGraphManager implements SceneManagerInterface{
	private Node root;
	private LinkedList<Light> lights;
	private Camera camera;
	private Frustum frustum;
	
	public SceneGraphManager(Node root)
	{
		this.root = root;
		lights = new LinkedList<Light>();
		camera = new Camera();
		frustum = new Frustum();
	}
	

	public Node getRoot() {
		return root;
	}


	public void setRoot(Node root) {
		this.root = root;
	}

	public Camera getCamera()
	{
		return camera;
	}
	
	public Frustum getFrustum()
	{
		return frustum;
	}
	
	public void addLight(Light light)
	{
		lights.add(light);
	}
	
	public Iterator<Light> lightIterator()
	{
		return lights.iterator();
	}
	
	public SceneManagerIterator iterator()
	{
		return new GraphSceneManagerItr(this);
	}
			
	private class GraphSceneManagerItr implements SceneManagerIterator{

		Stack<Node> nodeStack;
		Stack<Matrix4f> matrixStack;
		Node root;
		
		public GraphSceneManagerItr(SceneGraphManager sceneManager)
		{
			nodeStack = new Stack<Node>();
			matrixStack = new Stack<Matrix4f>();
			this.root = sceneManager.getRoot();
			nodeStack.push(this.root);
			Matrix4f identity = new Matrix4f();
			identity.setIdentity();
			matrixStack.push(identity);
		}
		
		public boolean hasNext()
		{
			return !nodeStack.isEmpty();
		}
		
		public RenderItem next()
		{
			Node current;
			Matrix4f currentMatrix;
			Matrix4f parentMatrix;
			

			current = nodeStack.pop();
			currentMatrix = current.getTransformationMatrix();
			parentMatrix = matrixStack.pop();
			currentMatrix.mul(parentMatrix,currentMatrix);
			
			while(current.getChildren()!=null)
			{
				ListIterator<Node> childrenItr = current.getChildren().listIterator();
				while(childrenItr.hasNext())
				{
					Node currentChild = childrenItr.next();
					nodeStack.push(currentChild);
					matrixStack.push(currentMatrix);
				}
				current = nodeStack.pop();
				currentMatrix = current.getTransformationMatrix();
				parentMatrix = matrixStack.pop();
				currentMatrix.mul(parentMatrix,currentMatrix);
			}
			
			/*
			do{
				current = nodeStack.pop();
				currentMatrix = current.getTransformationMatrix();
				parentMatrix = matrixStack.pop();
				currentMatrix.mul(parentMatrix,currentMatrix);
				ListIterator<Node> childrenItr = current.getChildren().listIterator();
				while(childrenItr.hasNext())
				{
					Node currentChild = childrenItr.next();
					nodeStack.push(currentChild);
					matrixStack.push(currentMatrix);
				}
			}
			while(nodeStack.peek().getChildren()!=null);
			*/
			return new RenderItem(current.getShape(), currentMatrix);
			
		}
		
	}
}
