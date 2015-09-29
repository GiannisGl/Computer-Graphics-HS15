package simple;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;

import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager = new SimpleSceneManager();
	static Shape shape, shape2, shape3, shape4;
	static float currentstep, basicstep;
	static int exerciseNr;

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	public final static class SimpleRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			this.renderer(r, cube());			
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
			
			public void renderer(RenderContext r, Shape cube)
			{
				shape = cube;
				sceneManager.addShape(cube);
				
				// Add the scene to the renderer
				renderContext.setSceneManager(sceneManager);
				
				// Load some more shaders
			    normalShader = renderContext.makeShader();
			    try {
			    	normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
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
				material.shader = diffuseShader;
				material.diffuseMap = renderContext.makeTexture();
				try {
					material.diffuseMap.load("../textures/plant.jpg");
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
	}
		
		public final static class CylinderRenderPanel extends GLRenderPanel
		{
			/**
			 * Initialization call-back. We initialize our renderer here.
			 * 
			 * @param r	the render context that is associated with this render panel
			 */
			public final  void init(RenderContext r)
			{
				renderContext = r;
				int segments = 6;
				this.renderer(r,cylinder(segments));
			}
			
		
			public static final Shape cylinder(int segments)
			{			
				
				// Make a simple geometric object: a cylinder
				
				// The vertex positions of the cylinder:
				float[] v = new float[2*3*segments+2*3];
				// The vertex positions of the round faces
				for(int i=0; i<segments; i++)
				{
					v[6*i]=(float) Math.cos(2*Math.PI*i/segments);
					v[6*i+1]=-1;
					v[6*i+2]=(float) Math.sin(2*Math.PI*i/segments);
					
					v[6*i+3]=(float) Math.cos(2*Math.PI*i/segments);
					v[6*i+3+1]=1;
					v[6*i+3+2]=(float) Math.sin(2*Math.PI*i/segments);
				}
				
				// Center of the bottom face
				v[6*segments]= 0;
				v[6*segments+1]=-1;
				v[6*segments+2]=0;
							
				// Center of the top face
				v[6*segments+3]=0;
				v[6*segments+3+1]=1;
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
				
				
				
				VertexData vertexData = renderContext.makeVertexData(2*segments+2);
				vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
				vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
				
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
			
			public void renderer(RenderContext r, Shape cylinder)
			{
				renderContext = r;
				
				shape = cylinder;
				// Add the object to the scene Manager
				sceneManager.addShape(cylinder);

				// Add the scene to the renderer
				renderContext.setSceneManager(sceneManager);
								
				// Register a timer task
			    Timer timer = new Timer();
			    basicstep = 0.01f;
			    currentstep = basicstep;
			    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
			}
		
		}
		
		public final static class TorusRenderPanel extends GLRenderPanel
		{
			/**
			 * Initialization call-back. We initialize our renderer here.
			 * 
			 * @param r	the render context that is associated with this render panel
			 */
			public final  void init(RenderContext r)
			{
				renderContext = r;
				int segments = 20;
				this.renderer(r, torus(segments));
			}
			
		
			public static final Shape torus(int segments)
			{			
				
				// Make a simple geometric object: a torus
				
				// The vertex positions of the cylinder:
				float[] v = new float[4*3*segments];
				for(int i=0; i<segments; i++)
				{
					v[12*i]=(float) (2*Math.cos(2*Math.PI*i/segments));
					v[12*i+1]=-1;
					v[12*i+2]=(float) (2*Math.sin(2*Math.PI*i/segments));
					
					v[12*i+3]=(float) (3*Math.cos(2*Math.PI*i/segments));
					v[12*i+3+1]=0;
					v[12*i+3+2]=(float) (3*Math.sin(2*Math.PI*i/segments));
					
					v[12*i+6]=(float) (2*Math.cos(2*Math.PI*i/segments));
					v[12*i+6+1]=1;
					v[12*i+6+2]=(float) (2*Math.sin(2*Math.PI*i/segments));
					
					v[12*i+9]=(float) (1*Math.cos(2*Math.PI*i/segments));
					v[12*i+9+1]=0;
					v[12*i+9+2]=(float) (1*Math.sin(2*Math.PI*i/segments));
				}
				
				// The vertex colors
				float[] c = new float[4*3*segments];
				for(int i=0; i<segments; i++)
				{
					c[12*i]=(float) Math.floorMod(i, 2);
					c[12*i+1]=(float) Math.floorMod(i, 2);
					c[12*i+2]= (float)Math.floorMod(i, 2);
					c[12*i+3]=(float) Math.floorMod(i, 2);
					c[12*i+4]=(float) Math.floorMod(i, 2);
					c[12*i+5]=(float) Math.floorMod(i, 2);
					c[12*i+6]=(float) Math.floorMod(i, 2);
					c[12*i+7]=(float) Math.floorMod(i, 2);
					c[12*i+8]=(float) Math.floorMod(i, 2);
					c[12*i+9]=(float) Math.floorMod(i, 2);
					c[12*i+10]=(float) Math.floorMod(i, 2);
					c[12*i+11]=(float) Math.floorMod(i, 2);
				}
								
				
				VertexData vertexData = renderContext.makeVertexData(4*segments);
				vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
				vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
				
				// The triangles (three vertex indices for each triangle)
				int[] indices = new int[8*3*segments];
				for(int i=0; i<segments; i++)
				{
					indices[24*i]=Math.floorMod(4*i, 4*segments);
					indices[24*i+1]=Math.floorMod(4*(i+1), 4*segments);
					indices[24*i+2]=Math.floorMod(4*(i+1)+1, 4*segments);
					
					indices[24*i+3]=Math.floorMod(4*i, 4*segments);
					indices[24*i+4]=Math.floorMod(4*(i+1)+1, 4*segments);
					indices[24*i+5]=Math.floorMod(4*i+1, 4*segments);
					
					indices[24*i+6]=Math.floorMod(4*i+1, 4*segments);
					indices[24*i+7]=Math.floorMod(4*(i+1)+1, 4*segments);
					indices[24*i+8]=Math.floorMod(4*(i+1)+2, 4*segments);
					
					indices[24*i+9]=Math.floorMod(4*i+1, 4*segments);
					indices[24*i+10]=Math.floorMod(4*(i+1)+2, 4*segments);
					indices[24*i+11]=Math.floorMod(4*i+2, 4*segments);
					
					indices[24*i+12]=Math.floorMod(4*i+2, 4*segments);
					indices[24*i+13]=Math.floorMod(4*(i+1)+2, 4*segments);
					indices[24*i+14]=Math.floorMod(4*(i+1)+3, 4*segments);
					
					indices[24*i+15]=Math.floorMod(4*i+2, 4*segments);
					indices[24*i+16]=Math.floorMod(4*(i+1)+3, 4*segments);
					indices[24*i+17]=Math.floorMod(4*i+3, 4*segments);
					
					indices[24*i+18]=Math.floorMod(4*i+3, 4*segments);
					indices[24*i+19]=Math.floorMod(4*(i+1)+3, 4*segments);
					indices[24*i+20]=Math.floorMod(4*(i+1), 4*segments);
					
					indices[24*i+21]=Math.floorMod(4*i+3, 4*segments);
					indices[24*i+22]=Math.floorMod(4*(i+1), 4*segments);
					indices[24*i+23]=Math.floorMod(4*i, 4*segments);
				}
				
				vertexData.addIndices(indices);
				
				Shape torus = new Shape(vertexData);
				return torus;
			}
			
			public void renderer(RenderContext r, Shape torus)
			{
				renderContext = r;
				
				shape = torus;
				// Add the object to the scene Manager
				sceneManager.addShape(torus);
						
				// Add the scene to the renderer
				renderContext.setSceneManager(sceneManager);
						
				// Register a timer task
			    Timer timer = new Timer();
			    basicstep = 0.01f;
			    currentstep = basicstep;
			    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
			}
		
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
			// shape1, inner torus
			shape = TorusRenderPanel.torus(10);
    		Matrix4f t = shape.getTransformation();
    		t.setScale(0.5f);
			Matrix4f trans = new Matrix4f();
    		Vector3f vector = new Vector3f(3f, 0f, 0f);
    		trans.setTranslation(vector);
    		t.add(trans);
    		shape.setTransformation(t);
    				
    		// shape2, outer torus
			shape2 = TorusRenderPanel.torus(10);
    		Matrix4f t2 = shape2.getTransformation();
    		t2.setScale(0.5f);
			Matrix4f trans2 = new Matrix4f();
    		Vector3f vector2 = new Vector3f(7f, 0f, 0f);
    		trans2.setTranslation(vector2);
    		t2.add(trans2);
    		shape2.setTransformation(t2);
			
			// shape3, middle cylinder
			shape3 = CylinderRenderPanel.cylinder(10);
    		Matrix4f t3 = shape3.getTransformation();
    		Matrix4f rotX = new Matrix4f();
    		rotX.rotX((float) Math.PI/2);
    		rotX.mul(t3);
			Matrix4f trans3 = new Matrix4f();
    		Vector3f vector3 = new Vector3f(5f, -0.5f, 0f);
    		trans3.setTranslation(vector3);
    		rotX.add(trans3);
    		shape3.setTransformation(rotX);
    		
    		// shape4, bottom square
			shape4 = SimpleRenderPanel.cube();
    		Matrix4f t4 = shape4.getTransformation();
			Matrix4f trans4 = new Matrix4f();
    		Vector3f vector4 = new Vector3f(5f, -2.5f, 0f);
    		trans4.setTranslation(vector4);
    		t4.add(trans4);
    		shape4.setTransformation(t4);
    		

			Shape[] shapes = {shape, shape2, shape3, shape4};
			renderer(r, shapes);
		}
		
		public void renderer(RenderContext r, Shape[] shapes)
		{
			for(Shape shape: shapes)
			{
				sceneManager.addShape(shape);
			}
	
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
					
			// Register a timer task
		    Timer timer = new Timer();
		    basicstep = 0.01f;
		    currentstep = basicstep;
		    timer.scheduleAtFixedRate(new AnimationTask(), 0, 10);
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
			
			if(exerciseNr>=0 && exerciseNr <=2)
			{
				// Update transformation by rotating with angle "currentstep"
	    		Matrix4f t = shape.getTransformation();
	    		Matrix4f rotX = new Matrix4f();
	    		rotX.rotX(currentstep);
	    		Matrix4f rotY = new Matrix4f();
	    		rotY.rotY(currentstep);
	    		t.mul(rotX);
	    		t.mul(rotY);	
	    		shape.setTransformation(t);
			}
			else{
				// Update transformations
				
				Matrix4f rotY0 = new Matrix4f();	
				rotY0.setRow(0, (float) Math.cos(Math.PI/180*100*currentstep) , 0, (float) Math.sin(Math.PI/180*100*currentstep), 0);
				rotY0.setRow(1, 0, 1, 0, 0);
				rotY0.setRow(2, (float) -Math.sin(Math.PI/180*100*currentstep) , 0, (float) Math.cos(Math.PI/180*100*currentstep), 0);
				rotY0.setRow(3, 0 , 0, 0, 1); 
				
				// shape1, inner torus
				Matrix4f t = shape.getTransformation();
				Matrix4f rotY01 = (Matrix4f) rotY0.clone();   
				rotY01.mul(t);
				
				Matrix4f rotY = new Matrix4f();
				rotY.rotY((float)-Math.PI/180*100*5*currentstep);
				rotY01.mul(rotY);

	    		Matrix4f rotX = new Matrix4f();
	    		rotX.rotX(currentstep);
	    		rotY01.mul(rotX);
				shape.setTransformation(rotY01);
				
				// shape2, outer torus
				Matrix4f t2 = shape2.getTransformation();
				Matrix4f rotY2 = new Matrix4f();
				rotY2.rotY((float)Math.PI/180*100*5*currentstep);
				
				Matrix4f rotY02 = (Matrix4f) rotY0.clone();
				rotY02.mul(t2);
				rotY02.mul(rotY2);
	    		rotY02.mul(rotX);
				shape2.setTransformation(rotY02);
				
				// shape3, middle cylinder
				Matrix4f t3 = shape3.getTransformation();
				Matrix4f rotZ = new Matrix4f();
				rotZ.rotY((float) Math.PI/180*100*5*currentstep);
				t3.mul(rotZ);
				Matrix4f rotY03 = (Matrix4f) rotY0.clone();   
				rotY03.mul(t3);
				shape3.setTransformation(rotY03);
				
				// shape4, bottom cube
				Matrix4f t4 = shape4.getTransformation();
				Matrix4f rotX2 = new Matrix4f();
				rotX2.rotX((float)-Math.PI/180*100*5*currentstep);
				t4.mul(rotX2);
				Matrix4f rotY04 = (Matrix4f) rotY0.clone();
				rotY04.mul(t4);
				shape4.setTransformation(rotY04);
			}
				
				// Trigger redrawing of the render window
				renderPanel.getCanvas().repaint(); 
		}
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
				case 'n': {
					// Remove material from shape, and set "normal" shader
					shape.setMaterial(null);
					renderContext.useShader(normalShader);
					break;
				}
				case 'd': {
					// Remove material from shape, and set "default" shader
					shape.setMaterial(null);
					renderContext.useDefaultShader();
					break;
				}
				case 'm': {
					// Set a material for more complex shading of the shape
					if(shape.getMaterial() == null) {
						shape.setMaterial(material);
					} else
					{
						shape.setMaterial(null);
						renderContext.useDefaultShader();
					}
					break;
				}
			}
			
			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		
		public void keyReleased(KeyEvent e)
		{
		}

		public void keyTyped(KeyEvent e)
        {
        }

	}
	
	/**
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed 
	 * for initialization automatically. It then constructs a simple 3D scene, 
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		
		System.out.println("Which exercise?");
		do{
			exerciseNr = new Scanner(System.in).nextInt();
		}
		while(exerciseNr<0 && exerciseNr>3);

		switch(exerciseNr)
		{
			case 1:{
				renderPanel = new CylinderRenderPanel();
				break;
			}
			case 2:{
				renderPanel = new TorusRenderPanel();
				break;
			}
			case 3:{
				renderPanel = new SceneRenderPanel();
				break;
			}
			default:
				renderPanel = new SimpleRenderPanel();
				break;
		}
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(500, 500);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		// Add a mouse and key listener
	    renderPanel.getCanvas().addMouseListener(new SimpleMouseListener());
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
