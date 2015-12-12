package simple;

import jrtr.*;
import jrtr.Light.Type;
import jrtr.glrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import java.util.Timer;
import java.util.TimerTask;

import javax.vecmath.*;


/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple5
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Shader colorDiffuseShader;
	static Material material, material2, material3, materialC;
	static SceneGraphManager sceneManager;
	static float currentstep, basicstep;
	static int width=500;
	static int height=500;
	static int radius=Math.min(width,height);
	static Vector3f p1;
	static Vector3f p2;
	static int isIn=0;
	static Vector3f axis = new Vector3f();
	static float theta;
	static boolean withMaterial=false;
	static Shape ground, body, leg, arm, head, legJoint, armJoint;
	static TransformGroup root, groundTransform, bodyTransform, leftLegJointTransform, leftLegTransform, rightLegJointTransform, rightLegTransform,
						leftArmJointTransform, leftArmTransform, rightArmJointTransform, rightArmTransform;
	static float time=0;

	public static final Shape ground()
	{
		// A house
		float vertices[] = {-100,0,100, 100,0,100, 100,0,-100, -100,0,-100};
	
		float normals[] = {0,1,0,  0,1,0,  0,1,0,  0,1,0};
						   
		float colors[] = { 0,0.1f,0, 0,0.1f,0, 0,0.1f,0, 0,0.1f,0};
	
		// Set up the vertex data
		VertexData vertexData = renderContext.makeVertexData(4);;
	
		vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
		int indices[] = { 0,2,1, 0,2,3};	
		vertexData.addIndices(indices);
	
		Shape ground = new Shape(vertexData);
		
		return ground;
	}
	
	
	// Make a simple geometric object: a cube
	public final static Shape cube()
	{	
		// The vertex positions of the cube
		float v[] = {-1,-1,1, 1,-1,1, 1,1,1, -1,1,1,		// front face
			         -1,-1,-1, -1,-1,1, -1,1,1, -1,1,-1,	// left face
				  	 1,-1,-1,-1,-1,-1, -1,1,-1, 1,1,-1,		// back face
					 1,-1,1, 1,-1,-1, 1,1,-1, 1,1,1,		// right face
					 1,1,1, 1,1,-1, -1,1,-1, -1,1,1,		// top face
					-1,-1,1, -1,-1,-1, 1,-1,-1, 1,-1,1};	// bottom face

		// The vertex normals 
		float n[] = {0,0,1, 0,0,1, 0,0,1, 0,0,1,			// front face
			         -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
				  	 0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
					 1,0,0, 1,0,0, 1,0,0, 1,0,0,			// right face
					 0,1,0, 0,1,0, 0,1,0, 0,1,0,			// top face
					 0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0};		// bottom face

		// The vertex colors
		float c[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
				     0,1,0, 0,1,0, 0,1,0, 0,1,0,
					 1,0,0, 1,0,0, 1,0,0, 1,0,0,
					 0,1,0, 0,1,0, 0,1,0, 0,1,0,
					 0,0,1, 0,0,1, 0,0,1, 0,0,1,
					 0,0,1, 0,0,1, 0,0,1, 0,0,1};

		// Texture coordinates 
		float uv[] = {0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1,
				  0,0, 1,0, 1,1, 0,1};

		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(24);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(uv, VertexData.Semantic.TEXCOORD, 2);
		
		// The triangles (three vertex indices for each triangle)
		int indices[] = {0,2,3, 0,1,2,			// front face
						 4,6,7, 4,5,6,			// left face
						 8,10,11, 8,9,10,		// back face
						 12,14,15, 12,13,14,	// right face
						 16,18,19, 16,17,18,	// top face
						 20,22,23, 20,21,22};	// bottom face

		vertexData.addIndices(indices);
										
		// Add the object to the scene Manager
		Shape cube = new Shape(vertexData);  		
		
		return cube;
	}

	public static final Shape cylinder(int segments, float radius, float height)
	{			
		
		// Make a simple geometric object: a cylinder
		
		// The vertex positions of the cylinder:
		float[] v = new float[2*3*segments+2*3];
		// The vertex positions of the round faces
		for(int i=0; i<segments; i++)
		{
			v[6*i]=(float) Math.cos(2*Math.PI*i/segments)*radius;
			v[6*i+1]=-height;
			v[6*i+2]=(float) Math.sin(2*Math.PI*i/segments)*radius;
			
			v[6*i+3]=(float) Math.cos(2*Math.PI*i/segments)*radius;
			v[6*i+3+1]=height;
			v[6*i+3+2]=(float) Math.sin(2*Math.PI*i/segments)*radius;
		}
		
		// Center of the bottom face
		v[6*segments]= 0;
		v[6*segments+1]=-height;
		v[6*segments+2]=0;
					
		// Center of the top face
		v[6*segments+3]=0;
		v[6*segments+3+1]=height;
		v[6*segments+3+2]=0;
		
		
		// The vertex colors
		// The vertex colors of the round faces
		float[] c = new float[2*3*segments+2*3];
		for(int i=0; i<segments; i++)
		{
			c[6*i]=(float) Math.floorMod(i, 2);
			c[6*i+1]=(float) Math.floorMod(i, 2);
			c[6*i+2]= (float)Math.floorMod(i, 2);
			c[6*i+3]=(float) Math.floorMod(i, 2);
			c[6*i+4]=(float) Math.floorMod(i, 2);
			c[6*i+5]=(float) Math.floorMod(i, 2);				
		}
		
		// The vertex colors of the top vertex
		c[6*segments]= 0;
		c[6*segments+1]=0;
		c[6*segments+2]=0;
					
		// The vertex colors of the bottom vertex 
		c[6*segments+3]=0;
		c[6*segments+3+1]=0;
		c[6*segments+3+2]=0;
		
		
	
		// Normals
		// Normals of the round faces
		float[] n = new float[2*3*segments+2*3];
		for(int i=0; i<segments; i++)
		{
			n[6*i]=(float) Math.cos(2*Math.PI*i/segments);
			n[6*i+1]=0.5f;
			n[6*i+2]=(float) Math.sin(2*Math.PI*i/segments);
			
			n[6*i+3]=(float) Math.cos(2*Math.PI*i/segments);
			n[6*i+3+1]=0.5f;
			n[6*i+3+2]=(float) Math.sin(2*Math.PI*i/segments);			
		}
		
		// Center of the bottom face
		n[6*segments]= 0;
		n[6*segments+1]=-1;
		n[6*segments+2]=0;
				
		// Center of the top face
		n[6*segments+3]=0;
		n[6*segments+3+1]=1;
		n[6*segments+3+2]=0;
		
		
		
		
		// Texture coordinates of the round faces
		float[] t = new float[2*2*segments+2*2];
		for(int i=0; i<segments; i++)
		{
			t[4*i]= (float) i/segments;
			t[4*i+1]= (float) 0;
			t[4*i+2]= (float) i/segments;
			t[4*i+3]= (float) 1;	
		}
		
		// Texture coordinates  of the top vertex
		t[4*segments]= 0.5f;
		t[4*segments+1]=1;
					
		// Texture coordinates  of the bottom vertex 
		t[4*segments+2]=0.5f;
		t[4*segments+2+1]=0;
		
		
		
		VertexData vertexData = renderContext.makeVertexData(2*segments+2);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(t, VertexData.Semantic.TEXCOORD, 2);
		
		// The triangles (three vertex indices for each triangle)
		int[] indices = new int[4*3*segments];
		// The triangles of the round faces
		for(int i=0; i<segments; i++)
		{
			indices[6*i]=Math.floorMod(2*i, 2*segments);
			indices[6*i+1]=Math.floorMod(2*i+3, 2*segments);
			indices[6*i+2]=Math.floorMod(2*i+1, 2*segments);
			
			indices[6*i+3]=Math.floorMod(2*i, 2*segments);
			indices[6*i+4]=Math.floorMod(2*i+2, 2*segments);
			indices[6*i+5]=Math.floorMod(2*i+3, 2*segments);
		}
		
		// The triangles of the bottom face
		for(int i=0; i<segments; i++)
		{
			indices[6*segments+3*i]=Math.floorMod(2*i, 2*segments);
			indices[6*segments+3*i+1]=2*segments;
			indices[6*segments+3*i+2]=Math.floorMod(2*(i+1), 2*segments);
		}
		
		// The triangles of the top face
		for(int i=0; i<segments; i++)
		{
			indices[9*segments+3*i]=Math.floorMod(2*(i+1)+1, 2*segments);
			indices[9*segments+3*i+1]=2*segments+1;
			indices[9*segments+3*i+2]=Math.floorMod(2*i+1, 2*segments);
		}
		
		vertexData.addIndices(indices);
		Shape cylinder = new Shape(vertexData);
		
		return cylinder;
	}
	
	public static final Shape torus(int segments, int hSegments, float centralRadius, float radius)
	{			
		
		// Make a simple geometric object: a torus
		
		// The vertex positions of the cylinder:
		float[] v = new float[hSegments*3*segments];
		for(int i=0; i<segments; i++)
		{
			for(int j=0; j<hSegments; j++)
			{
				v[hSegments*3*i+3*j] = (float) ((radius*Math.cos(2*Math.PI*j/hSegments)+centralRadius)*Math.cos(2*Math.PI*i/segments));
				v[hSegments*3*i+3*j+1] = (float) (radius*Math.sin(2*Math.PI*j/hSegments));
				v[hSegments*3*i+3*j+2] = (float) ((radius*Math.cos(2*Math.PI*j/hSegments)+centralRadius)*Math.sin(2*Math.PI*i/segments));
			}
		}
		
		// The vertex colors
		float[] c = new float[hSegments*3*segments];
		for(int i=0; i<segments; i++)
		{
			for(int j=0; j<hSegments; j++)
			{
				c[hSegments*3*i+3*j]=(float) Math.floorMod(i, 2);
				c[hSegments*3*i+3*j+1]=(float) Math.floorMod(i, 2);
				c[hSegments*3*i+3*j+2]=(float) Math.floorMod(i, 2);
			}
		}
		
		// The normals of the cylinder:
		float[] n = new float[hSegments*3*segments];
		for(int i=0; i<segments; i++)
		{
			for(int j=0; j<hSegments; j++)
			{
				n[hSegments*3*i+3*j] = (float) ((radius*Math.cos(2*Math.PI*j/hSegments))*Math.cos(2*Math.PI*i/segments));
				n[hSegments*3*i+3*j+1] = (float) (radius*Math.sin(2*Math.PI*j/hSegments));
				n[hSegments*3*i+3*j+2] = (float) ((radius*Math.cos(2*Math.PI*j/hSegments))*Math.sin(2*Math.PI*i/segments));
			}
		}

		
		// Texture coordinates 
		float[] t = new float[hSegments*2*segments];
		for(int i=0; i<segments; i++)
		{
			for(int j=0; j<hSegments; j++)
			{
				t[hSegments*2*i+2*j] = (float) i/segments;
				t[hSegments*2*i+2*j+1] = 1-(float) j/hSegments;
			}
		}
						
		
		VertexData vertexData = renderContext.makeVertexData(hSegments*segments);
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		vertexData.addElement(t, VertexData.Semantic.TEXCOORD, 2);
		
		// The triangles (three vertex indices for each triangle)
		int[] indices = new int[2*hSegments*3*segments];
		for(int i=0; i<segments; i++)
		{
			for(int j=0; j<hSegments; j++)
			{
				indices[2*hSegments*3*i+6*j]=Math.floorMod(hSegments*Math.floorMod(i, segments)+Math.floorMod(j,hSegments),hSegments*segments);
				indices[2*hSegments*3*i+6*j+1]=Math.floorMod(hSegments*Math.floorMod(i+1, segments)+Math.floorMod(j,hSegments),hSegments*segments);
				indices[2*hSegments*3*i+6*j+2]=Math.floorMod(hSegments*Math.floorMod(i+1, segments)+Math.floorMod(j+1,hSegments),hSegments*segments);
				
				indices[2*hSegments*3*i+6*j+3]=Math.floorMod(hSegments*Math.floorMod(i, segments)+Math.floorMod(j,hSegments),hSegments*segments);
				indices[2*hSegments*3*i+6*j+4]=Math.floorMod(hSegments*Math.floorMod(i+1, segments)+Math.floorMod(j+1,hSegments),hSegments*segments);
				indices[2*hSegments*3*i+6*j+5]=Math.floorMod(hSegments*Math.floorMod(i, segments)+Math.floorMod(j+1,hSegments),hSegments*segments);
			}
		}
		
		vertexData.addIndices(indices);
		
		Shape torus = new Shape(vertexData);
		return torus;
	}

	public final static class SceneRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public final void init(RenderContext r)
		{
			renderContext = r;

			ground = ground();
			body = cube();
			head = torus(40 , 40, 0, 2);
			leg = cylinder(20, 0.5f, 4);
    		arm = cylinder(20, 0.5f, 3.5f);
			Matrix4f bodyM = new Matrix4f();
			bodyM.setIdentity();
			bodyM.setScale(4);
			body.setTransformation(bodyM);
			legJoint = torus(20, 20, 0, 0.5f);
			armJoint = torus(20, 20, 0, 0.5f);
			
			renderer(r);
		}
		
		public void renderer(RenderContext r)
		{
			// ground transform
			groundTransform = new TransformGroup();
			Matrix4f groundM = new Matrix4f();
			groundM.setIdentity();
			groundM.setTranslation(new Vector3f(0,-8.5f,0));
			groundTransform.setInitialTransformationMatrix(groundM);
			groundTransform.setTransformationMatrix(groundM);
						
			// ground node
			ShapeNode groundNode = new ShapeNode();
			groundNode.setShape(ground);
			groundTransform.addChild(groundNode);
			
			bodyTransform = makeRobot();
			groundTransform.addChild(bodyTransform);
			
			root = groundTransform;
			sceneManager = new SceneGraphManager(root);
			sceneManager.getFrustum().setProjectionMatrix(1, 100, 1, (float) Math.PI/2);
			sceneManager.getCamera().setCenterOfProjection(new Vector3f(20f,10f,15f));
			sceneManager.getCamera().setLookAtPoint( new Vector3f(0f,0f,0f));
			sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
	
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
			
			
		    // load shader
		    colorDiffuseShader = renderContext.makeShader();
		    try {
		    	colorDiffuseShader.load("../jrtr/shaders/colorDiffuse.vert", "../jrtr/shaders/colorDiffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }
		    
		    diffuseShader = renderContext.makeShader();
		    try {
		    	diffuseShader.load("../jrtr/shaders/diffuse.vert", "../jrtr/shaders/diffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }		


		    // Make a material that can be used for shading
			material = new Material();
			//material.shader = colorDiffuseShader;
			material.shader = diffuseShader;
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/wood.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.01f;
		    currentstep = basicstep;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
		}
		
		public TransformGroup makeRobot(){
			
			// body transform
			TransformGroup bodyTransform = new TransformGroup();
			Matrix4f bodyM = new Matrix4f();
			bodyM.setIdentity();
			bodyM.setTranslation(new Vector3f(20,12f,0));
			bodyTransform.setInitialTransformationMatrix(bodyM);
			bodyTransform.setTransformationMatrix(bodyM);
			
			// body node
			ShapeNode bodyNode = new ShapeNode();
			bodyNode.setShape(body);
			bodyTransform.addChild(bodyNode);
			
			// head transform
			TransformGroup headTransform = new TransformGroup();
			Matrix4f headM = new Matrix4f();
			headM.setIdentity();
			headM.setTranslation(new Vector3f(0,6,0));
			headTransform.setInitialTransformationMatrix(headM);
			headTransform.setTransformationMatrix(headM);
			bodyTransform.addChild(headTransform);
			
			// head node
			ShapeNode headNode = new ShapeNode();
			headNode.setShape(head);
			headTransform.addChild(headNode);
			
			// light transform
			TransformGroup lightTransform = new TransformGroup();
			Matrix4f lightM = new Matrix4f();
			lightM.setIdentity();
			lightM.setTranslation(new Vector3f(-1f,0f,0f));
			lightTransform.setInitialTransformationMatrix(lightM);
			lightTransform.setTransformationMatrix(lightM);
			headTransform.addChild(lightTransform);
			//bodyTransform.addChild(lightTransform);
			
			// light node
			LightNode lightNode = new LightNode();
			Light light = new Light();
			light.position= new Vector3f(0f,0f, 0f);
			light.type=Type.POINT;
			light.color= new Vector4f(1.f,1.f,1.f,1.f);
			lightNode.setLight(light);
			Matrix4f lightNodeM = new Matrix4f();
			lightNodeM.setIdentity();
			lightNode.setTransformationMatrix(lightNodeM);
			lightTransform.addChild(lightNode);
			
			
			// leg node
			ShapeNode legNode = new ShapeNode();
			legNode.setShape(leg);
			// leg joint node
			ShapeNode legJointNode = new ShapeNode();
			legJointNode.setShape(legJoint);
			
			// left leg joint
			leftLegJointTransform = new TransformGroup();
			Matrix4f leftLegJointM = new Matrix4f();
			leftLegJointM.setIdentity();
			leftLegJointM.setTranslation(new Vector3f(-3.5f,-4.5f,0));
			leftLegJointTransform.setInitialTransformationMatrix(leftLegJointM);
			leftLegJointTransform.setTransformationMatrix(leftLegJointM);
			leftLegJointTransform.addChild(legJointNode);
			bodyTransform.addChild(leftLegJointTransform);
			
			// left leg transform
			leftLegTransform = new TransformGroup();
			Matrix4f leftLegM = new Matrix4f();
			leftLegM.setIdentity();
			leftLegM.setTranslation(new Vector3f(0,-4.5f,0));
			leftLegTransform.setInitialTransformationMatrix(leftLegM);
			leftLegTransform.setTransformationMatrix(leftLegM);
			leftLegJointTransform.addChild(leftLegTransform);
			leftLegTransform.addChild(legNode);
			//leftLegTransform.addChild(lightTransform);
			
			// right leg joint
			rightLegJointTransform = new TransformGroup();
			Matrix4f rightLegJointM = new Matrix4f();
			rightLegJointM.setIdentity();
			rightLegJointM.setTranslation(new Vector3f(3.5f, -4.5f, 0));
			rightLegJointTransform.setInitialTransformationMatrix(rightLegJointM);
			rightLegJointTransform.setTransformationMatrix(rightLegJointM);
			rightLegJointTransform.addChild(legJointNode);
			bodyTransform.addChild(rightLegJointTransform);
			
			// right leg transform
			rightLegTransform = new TransformGroup();
			Matrix4f rightLegM = new Matrix4f();
			rightLegM.setIdentity();
			rightLegM.setTranslation(new Vector3f(0,-4.5f,0));
			rightLegTransform.setInitialTransformationMatrix(rightLegM);
			rightLegTransform.setTransformationMatrix(rightLegM);
			rightLegJointTransform.addChild(rightLegTransform);
			rightLegTransform.addChild(legNode);

			// arm node
			ShapeNode armNode = new ShapeNode();
			armNode.setShape(arm);
			// arm joint node
			ShapeNode armJointNode = new ShapeNode();
			armJointNode.setShape(armJoint);
			
			// left arm joint
			leftArmJointTransform = new TransformGroup();
			Matrix4f leftArmJointM = new Matrix4f();
			leftArmJointM.setIdentity();
			Matrix4f rotZ = new Matrix4f();
    		rotZ.rotZ((float) -Math.PI/4);
			leftArmJointM.mul(rotZ, leftArmJointM);
			leftArmJointM.setTranslation(new Vector3f(-4.5f,1f,0));
			leftArmJointTransform.setInitialTransformationMatrix(leftArmJointM);
			leftArmJointTransform.setTransformationMatrix(leftArmJointM);
			leftArmJointTransform.addChild(armJointNode);
			bodyTransform.addChild(leftArmJointTransform);
			
			// left arm transform
			leftArmTransform = new TransformGroup();
			Matrix4f leftArmM = new Matrix4f();
			leftArmM.setIdentity();
			leftArmM.setTranslation(new Vector3f(0,-4f,0));
			leftArmTransform.setInitialTransformationMatrix(leftArmM);
			leftArmTransform.setTransformationMatrix(leftArmM);
			leftArmTransform.addChild(armNode);
			leftArmTransform.addChild(lightTransform);
			leftArmJointTransform.addChild(leftArmTransform);
			
			
			// right arm joint
			rightArmJointTransform = new TransformGroup();
			Matrix4f rightArmJointM = new Matrix4f();
			rightArmJointM.setIdentity();
    		rotZ.rotZ((float) Math.PI/4);
    		rightArmJointM.mul(rotZ, rightArmJointM);
			rightArmJointM.setTranslation(new Vector3f(4.5f, 1f, 0));
			rightArmJointTransform.setInitialTransformationMatrix(rightArmJointM);
			rightArmJointTransform.setTransformationMatrix(rightArmJointM);
			rightArmJointTransform.addChild(armJointNode);
			
			// right arm transform
			rightArmTransform = new TransformGroup();
			Matrix4f rightArmM = new Matrix4f();
			rightArmM.setIdentity();
			rightArmM.setTranslation(new Vector3f(0,-4f,0));
			rightArmTransform.setInitialTransformationMatrix(rightArmM);
			rightArmTransform.setTransformationMatrix(rightArmM);
			rightArmJointTransform.addChild(rightArmTransform);
			rightArmTransform.addChild(armNode);
			bodyTransform.addChild(rightArmJointTransform);
			
			return bodyTransform;
		}
	}
	


	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
		{
			time+=3*currentstep;
    		Matrix4f rotY = new Matrix4f();
    		rotY.rotY(currentstep);
    		
    		Matrix4f rotX = new Matrix4f();
    		float leftAngle = (float) (Math.cos(time)*Math.PI/3);
    		rotX.rotX(leftAngle);
			
			
			Matrix4f t = bodyTransform.getTransformationMatrix();
    		t.mul(rotY, t);    		
    		bodyTransform.setTransformationMatrix(t);

			Matrix4f leftLegM = new Matrix4f(leftLegTransform.getInitialTransformationMatrix());
			leftLegM.mul(rotX, leftLegM);    		
    		leftLegTransform.setTransformationMatrix(leftLegM);
    		
    		Matrix4f rightArmM = new Matrix4f(rightArmTransform.getInitialTransformationMatrix());
    		rightArmM.mul(rotX, rightArmM);
    		rightArmTransform.setTransformationMatrix(rightArmM);
    		
    		float rightAngle = (float) -(Math.cos(time)*Math.PI/4);
    		rotX.rotX(rightAngle);
			Matrix4f rightLegM = new Matrix4f(rightLegTransform.getInitialTransformationMatrix());
			rightLegM.mul(rotX, rightLegM);    		
			rightLegTransform.setTransformationMatrix(rightLegM);
			
			Matrix4f leftArmM = new Matrix4f(leftArmTransform.getInitialTransformationMatrix());
			leftArmM.mul(rotX, leftArmM);
			leftArmTransform.setTransformationMatrix(leftArmM);
    		
			// Trigger redrawing of the render window
			renderPanel.getCanvas().repaint(); 
		}
	}

	public static class PointToSphere
	{
		public static Vector3f getVectorFromPoint(MouseEvent e)
		{
			int width = renderPanel.getCanvas().getWidth();
	        int height = renderPanel.getCanvas().getHeight();
			int x = e.getX();
            int y = e.getY();
            float x3D = (float) x/((float) radius/2);
            float y3D = (float) y/((float) radius/2);
            x3D=x3D-(float) width/radius;
            y3D= (float)height/radius-y3D;
            float z = 1-x3D*x3D-y3D*y3D;
            float z3D = z>0? (float) Math.sqrt(z):0.1f;
            Vector3f v = new Vector3f(x3D,y3D,z3D);
            v.normalize();
            return v;
		}
	}
	
	public static class TrackBallMouseListener implements MouseListener
	{
		@Override
    	public void mousePressed(MouseEvent e) {
			
    		if(isIn==1)
    		{
	    		p1=PointToSphere.getVectorFromPoint(e);
    		}
    		
    	}
    	@Override
    	public void mouseReleased(MouseEvent e) {}
    	@Override
    	public void mouseEntered(MouseEvent e) {isIn=1;}
    	@Override
    	public void mouseExited(MouseEvent e) {isIn=0;}
    	public void mouseClicked(MouseEvent e) {}
	}
	
	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class TrackBallMouseMotionListener implements MouseMotionListener
	{		
		
		@Override
		public void mouseDragged(MouseEvent e) {

    		
			if(isIn==1)
    		{
				p2=PointToSphere.getVectorFromPoint(e);
	            
	            axis.cross(p1, p2);
	            theta = (float) (p1.angle(p2));
	            
	            Matrix4f rot = new Matrix4f();
	            AxisAngle4f axisAngle = new AxisAngle4f(axis, theta);
	            rot.set(axisAngle);
	            
	            root.getTransformationMatrix().mul(rot, root.getTransformationMatrix());
	            
	            p1=new Vector3f(p2);
    		}
			

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		@Override
		public void mouseMoved(MouseEvent e) {}

	}
	/**
	 * A mouse listener for the main window of this application. This can be
	 * used to process mouse events.
	 */
	public static class SimpleMouseListener implements MouseListener
	{
    	public void mousePressed(MouseEvent e) {}
    	public void mouseReleased(MouseEvent e) {}
    	public void mouseEntered(MouseEvent e) {}
    	public void mouseExited(MouseEvent e) {}
    	public void mouseClicked(MouseEvent e) {}
	}
	
	/**
	 * A key listener for the main window. Use this to process key events.
	 * Currently this provides the following controls:
	 * 's': stop animation
	 * 'p': play animation
	 * '+': accelerate rotation
	 * '-': slow down rotation
	 * 'd': default shader
	 * 'n': shader using surface normals
	 * 'm': use a material for shading
	 */
	public static class SimpleKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{
				case 's': {
					// Stop animation
					currentstep = 0;
					break;
				}
				case 'p': {
					// Resume animation
					currentstep = basicstep;
					break;
				}
				case '+': {
					// Accelerate roation
					currentstep += basicstep;
					break;
				}
				case '-': {
					// Slow down rotation
					currentstep -= basicstep;
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(!withMaterial) {
						arm.setMaterial(material);
						ground.setMaterial(material);
						body.setMaterial(material);
						leg.setMaterial(material);
						head.setMaterial(material);
						armJoint.setMaterial(material);
						legJoint.setMaterial(material);
						renderContext.useShader(material.shader);
						withMaterial=true;
					} else
					{
						arm.setMaterial(null);
						ground.setMaterial(null);
						body.setMaterial(null);
						leg.setMaterial(null);
						head.setMaterial(null);
						armJoint.setMaterial(null);
						legJoint.setMaterial(null);
						renderContext.useDefaultShader();
						withMaterial=false;
					}
					break;
				}
			}
			
			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		
		public void keyReleased(KeyEvent e){}

		public void keyTyped(KeyEvent e){}

	}
	
	/**
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed 
	 * for initialization automatically. It then constructs a simple 3D scene, 
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
		// make a scene render panel
		renderPanel = new SceneRenderPanel();
				
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(width, height);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new TrackBallMouseListener());
	    renderPanel.getCanvas().addMouseMotionListener(new TrackBallMouseMotionListener());
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	      	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
