package simple;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.gldeferredrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;
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
	static SimpleSceneManager sceneManager;
	static Shape shape, shape2;
	static float currentstep, basicstep;
	static int exerciseNr = 2;

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
			
			// Make a simple geometric object: a cube
			
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
											
			// Make a scene manager and add the object
			sceneManager = new SimpleSceneManager();
			shape = new Shape(vertexData);
			sceneManager.addShape(shape);
			

    		Matrix4f trans = new Matrix4f();
    		Vector3f vector = new Vector3f((float) 1/100, (float) 0, (float) 0);
    		trans.setTranslation(vector);
    		Matrix4f t2 = shape.getTransformation();
    		t2.add(trans);
    		shape.setTransformation(t2);
    		
    		


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
				this.renderer(r, this.cylinder(segments));
			}
			
		
			public final VertexData cylinder(int segments)
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
				
				
				return vertexData;
			}
			
			public void renderer(RenderContext r, VertexData vertexData)
			{
				renderContext = r;
												
				// Make a scene manager and add the object
				sceneManager = new SimpleSceneManager();
				shape = new Shape(vertexData);
				sceneManager.addShape(shape);
		
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
				this.renderer(r, this.torus(segments));
			}
			
		
			public final VertexData torus(int segments)
			{			
				
				// Make a simple geometric object: a torus
				
				// The vertex positions of the cylinder:
				float[] v = new float[4*3*segments];
				for(int i=0; i<segments; i++)
				{
					v[12*i]=(float) (3*Math.cos(2*Math.PI*i/segments));
					v[12*i+1]=-1;
					v[12*i+2]=(float) (3*Math.sin(2*Math.PI*i/segments));
					
					v[12*i+3]=(float) (4*Math.cos(2*Math.PI*i/segments));
					v[12*i+3+1]=0;
					v[12*i+3+2]=(float) (4*Math.sin(2*Math.PI*i/segments));
					
					v[12*i+6]=(float) (3*Math.cos(2*Math.PI*i/segments));
					v[12*i+6+1]=1;
					v[12*i+6+2]=(float) (3*Math.sin(2*Math.PI*i/segments));
					
					v[12*i+9]=(float) (2*Math.cos(2*Math.PI*i/segments));
					v[12*i+9+1]=0;
					v[12*i+9+2]=(float) (2*Math.sin(2*Math.PI*i/segments));
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
				
				
				return vertexData;
			}
			
			public void renderer(RenderContext r, VertexData vertexData)
			{
				renderContext = r;
												
				// Make a scene manager and add the object
				sceneManager = new SimpleSceneManager();
				shape = new Shape(vertexData);
				sceneManager.addShape(shape);
		
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

	/**
	 * A timer task that generates an animation. This task triggers
	 * the redrawing of the 3D scene every time it is executed.
	 */
	public static class AnimationTask extends TimerTask
	{
		public void run()
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
