package jrtr;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

public class SceneGraphManager implements SceneManagerInterface{
	private Node root;
	private Camera camera;
	private LinkedList<Light> lights;
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

	public void addLight(Light light)
	{
		lights.add(light);
	}

	public Camera getCamera()
	{
		return camera;
	}
	
	public Frustum getFrustum()
	{
		return frustum;
	}
	
	public Iterator<Light> lightIterator()
	{
		LinkedList<Light> totalLights = new LinkedList<Light>();
		totalLights.addAll(lights);
		totalLights.addAll(graphSceneManagerLightItr(this));
		return totalLights.iterator();
	}
	
	public SceneManagerIterator iterator()
	{
		return new GraphSceneManagerItr(this);
	}
	
	private LinkedList<Light> graphSceneManagerLightItr(SceneGraphManager sceneManager)
	{
		LinkedList<Light> lights = new LinkedList<Light>();
		Stack<Node> nodeStack = new Stack<Node>();
		Stack<Matrix4f> matrixStack = new Stack<Matrix4f>();
		Node root = sceneManager.getRoot();
		nodeStack.push(root);
		Matrix4f identity = new Matrix4f();
		identity.setIdentity();
		matrixStack.push(identity);
		
		Node current;
		Matrix4f currentMatrix;
		Matrix4f parentMatrix;
		
		while(!nodeStack.isEmpty())
		{
			current = nodeStack.pop();
			parentMatrix = matrixStack.pop();
			currentMatrix = new Matrix4f(current.getTransformationMatrix());
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
				parentMatrix = matrixStack.pop();
				currentMatrix = new Matrix4f(current.getTransformationMatrix());
				currentMatrix.mul(parentMatrix,currentMatrix);
			}
			
			if(current instanceof LightNode){
				Light currentLight = ((LightNode) current).getLight();
				Light light = new Light();
				light.color = currentLight.color;
				light.type = currentLight.type;
				Vector4f lightPosition = new Vector4f(currentLight.position);
				lightPosition.w=1;
				parentMatrix.transform(lightPosition);
				light.position= new Vector3f(lightPosition.x, lightPosition.y, lightPosition.z);				
				lights.add(light);
			}
		}
		return lights;		
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
			parentMatrix = matrixStack.pop();
			currentMatrix = new Matrix4f(current.getTransformationMatrix());
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
				parentMatrix = matrixStack.pop();
				currentMatrix = new Matrix4f(current.getTransformationMatrix());
				currentMatrix.mul(parentMatrix,currentMatrix);
			}
			
			return new RenderItem(current.getShape(), new Matrix4f(currentMatrix));
			
		}
		
	}
}
