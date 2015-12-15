package simple;

import jrtr.*;
import jrtr.Light.Type;
import jrtr.VertexData.Semantic;
import jrtr.glrenderer.*;
import simple.simple4.CylinderRenderPanel;
import simple.simple4.TorusRenderPanel;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;
/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple6
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader, diffuseShader, diffuseShaderInit, colorDiffuseShader;
	static Material material, material2, material3, materialC;
	static SimpleSceneManager sceneManager = new SimpleSceneManager();
	static Camera camera = sceneManager.getCamera();
	static Shape shape, shape2, shape3;
	static float currentstep, basicstep;
	static int width=500;
	static int height=500;
	static boolean withObj=true;
	static int radius=Math.min(width,height);
	static Vector3f p1;
	static Vector3f p2;
	static int isIn=0;
	static Vector3f axis = new Vector3f();
	static float theta;
	static int exerciseNr=2;

	public static final class RotationalBodyRenderPanel extends GLRenderPanel
	{
		
		int bezierSegments;
		Vector3f[] controlPoints;
		int nrEvPoints;
		int nrRotStepts;
		RotationalBezierBody body;
		Shape rotationalBody;
		
		public RotationalBodyRenderPanel(int bezierSegments, Vector3f[] controlPoints, int nrEvPoints, int nrRotStepts) {
			this.bezierSegments = bezierSegments;
			this.controlPoints = controlPoints;
			this.nrEvPoints = nrEvPoints;
			this.nrRotStepts = nrRotStepts;
		}
		
		public final void init(RenderContext r)
		{
			renderContext = r;
			rotationalBody = rotationalBezierBody(bezierSegments, controlPoints, nrEvPoints, nrRotStepts);
			
			this.renderer(r, rotationalBody);
		}
		
		public final static Shape rotationalBezierBody(int bezierSegments, Vector3f[] controlPoints, int nrEvPoints, int nrRotStepts) {
			RotationalBezierBody body = new RotationalBezierBody(bezierSegments, controlPoints, nrEvPoints, nrRotStepts);
			Shape rotationalBody = body.createBody();
			return rotationalBody;
		}
		
		public void renderer(RenderContext r, Shape rotationalBody)
		{
			renderContext = r;
			
			shape = rotationalBody;

			sceneManager.getCamera().setCenterOfProjection(new Vector3f(0f,0f,10f));
			sceneManager.getCamera().setLookAtPoint( new Vector3f(0f,0f,0f));
			sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
			
			// Lights
			Light light1 = new Light();
			light1.direction= new Vector3f(0f,0f, 1.f);
			light1.type=Type.DIRECTIONAL;
			light1.color= new Vector4f(0.5f,0.5f,0.5f,0.2f);
			sceneManager.addLight(light1);
			
			Light light2 = new Light();
			light2.position= new Vector3f(0f, 0f, 0f); 
			light2.type=Type.POINT;
			light2.color= new Vector4f(0.f,0.f,1.f,1.f);
			sceneManager.addLight(light2);
			
			
			// load shader
			normalShader = renderContext.makeShader();
		    try {
		    	normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }
		    
		    diffuseShaderInit = renderContext.makeShader();
		    try {
		    	diffuseShaderInit.load("../jrtr/shaders/diffuseInit.vert", "../jrtr/shaders/diffuseInit.frag");
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
				material.diffuseMap.load("../textures/plant2.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			
			// Add the object to the scene Manager
			sceneManager.addShape(shape);
					
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
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
			
			shape = quad();
    		

			Shape[] shapes = {shape};
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
			
			sceneManager.getCamera().setCenterOfProjection(new Vector3f(0f,2f,10f));
			sceneManager.getCamera().setLookAtPoint( new Vector3f(0f,0f,0f));
			sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
			
			// Load some more shaders
		    normalShader = renderContext.makeShader();
		    try {
		    	normalShader.load("../jrtr/shaders/normal.vert", "../jrtr/shaders/normal.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    diffuseShaderInit = renderContext.makeShader();
		    try {
		    	diffuseShaderInit.load("../jrtr/shaders/diffuseInit.vert", "../jrtr/shaders/diffuseInit.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }	
		    
		    colorDiffuseShader = renderContext.makeShader();
		    try {
		    	colorDiffuseShader.load("../jrtr/shaders/colorDiffuse.vert", "../jrtr/shaders/colorDiffuse.frag");
		    } catch(Exception e) {
		    	System.out.print("Problem with shader:\n");
		    	System.out.print(e.getMessage());
		    }

		    // Make a material that can be used for shading
			materialC = new Material();
			materialC.shader = colorDiffuseShader;
			//material.diffuse = new Vector3f(0.1f, 0.1f, 1.f);
		    
		    
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
			//material.diffuse = new Vector3f(0.1f, 0.1f, 1.f);
			material.diffuseMap = renderContext.makeTexture();
			try {
				material.diffuseMap.load("../textures/plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			
			// Make a material that can be used for shading
			material2 = new Material();
			material2.shader = colorDiffuseShader;
			//material2.diffuse = new Vector3f(0.1f, 0.1f, 1.f);
			

			// Make a material that can be used for shading
			material3 = new Material();
			material3.shader = diffuseShader;
			material3.diffuseMap = renderContext.makeTexture();
			//material3.diffuse = new Vector3f(0.1f, 0.1f, 1.f);
			try {
				material3.diffuseMap.load("../textures/wood.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
				
		}
		
		public final static Shape quad(){

			float v[] = { 	1,1,1, 		1,1,-1, 	-1,1,-1, 	-1,1,1,		// top
					-1,-1,1,	-1,-1,-1, 	1,-1,-1, 	1,-1,1};	// bottom
			
			float c[] = {	1,0,0,	1,1,0, 	1,0,0, 	1,1,0,
				 	1,1,0, 	1,0,0, 	1,1,0, 	1,0,0 }; 
			
			
			int indices[] = {	0,2,3, 	0,1,2, //top
						7,3,4, 	7,0,3, //front
						6,0,7, 	6,1,0, //right
						5,1,6, 	5,2,1, //back
						4,2,5, 	4,3,2, //left
						6,4,5, 	6,7,4  //bottom
					};
			
			VertexData vertexData = renderContext.makeVertexData(8);
			vertexData.addElement(v, Semantic.POSITION, 3);
			vertexData.addElement(c, Semantic.COLOR, 3);
			vertexData.addIndices(indices);
			
			MeshData mesh = new MeshData(vertexData, renderContext);
			vertexData = mesh.vertexData;

			mesh.loop();
			vertexData = mesh.vertexData;
			
			Shape quad = new Shape(vertexData);  
			
			return quad;
		}
		
		public final static Shape spikedQuad(){
			float v[] = { 	1,1,1, 		1,1,-1, 	-1,1,-1, 	-1,1,1,		// top
					-1,-1,1,	-1,-1,-1, 	1,-1,-1, 	1,-1,1,     	// bottom
					0,-4,0,		0,4,0, 		4,0,0,				//spikes
					-4,0,0,		0,0,4,		0,0,-4};   			// spikes

					
			float c[] = {	1,0,0,	1,1,0, 	1,0,0, 	1,1,0,
					1,1,0, 	1,0,0, 	1,1,0, 	1,0,0, 
					0,0,1, 	0,0,1, 	0,0,1,	
					0,0,1,	0,0,1,	0,0,1};

			int indices[] = {	7,4,8,	4,5,8,	5,6,8, 	6,7,8, 	//bottom
						0,1,9,	1,2,9,	2,3,9,	3,0,9, 	//top
						6,1,10,	1,0,10,	0,7,10,	7,6,10,	//right
						4,3,11,	3,2,11,	2,5,11,	5,4,11,	//left
						7,0,12,	0,3,12,	3,4,12,	4,7,12,	//front
						5,2,13,	2,1,13,	1,6,13,	6,5,13	//back					
					};
			
			VertexData vertexData = renderContext.makeVertexData(14);
			vertexData.addElement(v, Semantic.POSITION, 3);
			vertexData.addElement(c, Semantic.COLOR, 3);
			vertexData.addIndices(indices);
			
			MeshData mesh = new MeshData(vertexData, renderContext);
			vertexData = mesh.vertexData;	
			
			mesh.loop();
			mesh.loop();
			mesh.loop();
			vertexData = mesh.vertexData;
			
			Shape spikedQuad = new Shape(vertexData);  
			
			return spikedQuad;
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
	            
	            shape.getTransformation().mul(rot, shape.getTransformation());

	            p1=new Vector3f(p2);
    		}
			

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		@Override
		public void mouseMoved(MouseEvent e) {}
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
						shape.setMaterial(material2);
					} else
					{
						shape.setMaterial(null);
						renderContext.useDefaultShader();
					}
					break;
				}
				case 'l': {
					// loop subdivision
					VertexData vertexData = shape.getVertexData();
					MeshData mesh = new MeshData(vertexData, renderContext);
					mesh.loop();
					vertexData = mesh.vertexData;
					shape = new Shape(vertexData);  
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
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		
		Vector3f[] controlPoints = new Vector3f[7];
		controlPoints[0] = new Vector3f(1,-3,0);
		controlPoints[1] = new Vector3f(2,-2,0);
		controlPoints[2] = new Vector3f(2,-1,0);
		controlPoints[3] = new Vector3f(1,0,0);
		controlPoints[4] = new Vector3f(2,1,0);
		controlPoints[5] = new Vector3f(2,2,0);
		controlPoints[6] = new Vector3f(1,3,0);
		
		if(exerciseNr==1)
			renderPanel = new RotationalBodyRenderPanel(2, controlPoints, 2, 10);
		else if(exerciseNr==2)
			renderPanel = new SceneRenderPanel();

		currentstep = 1f;
		// Add a mouse and key listener
		renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
	    renderPanel.getCanvas().addMouseListener(new TrackBallMouseListener());
	    renderPanel.getCanvas().addMouseMotionListener(new TrackBallMouseMotionListener());
		
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("simple");
		jframe.setSize(width, height);
		jframe.setLocationRelativeTo(null); // center of screen
		jframe.getContentPane().add(renderPanel.getCanvas());// put the canvas into a JFrame window

		renderPanel.getCanvas().setFocusable(true);   	    	    
	    
	    jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    jframe.setVisible(true); // show window
	}
}
