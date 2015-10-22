package simple;

import jrtr.*;
import jrtr.glrenderer.*;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;

import java.util.Timer;
import java.util.TimerTask;


/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class VirtualTrackballCamera
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager = new SimpleSceneManager();
	static Shape shape;
	static float currentstep, basicstep;
	static int exerciseNr;
	static int width=500;
	static int height=500;
	static int radius=Math.min(width,height);
	static Vector3f p1;
	static Vector3f p2;
	static int isIn=0;
	static Vector3f axis = new Vector3f();
	static float theta;
	static Camera camera = sceneManager.getCamera();
	

	/**
	 * An extension of {@link GLRenderPanel} or {@link SWRenderPanel} to 
	 * provide a call-back function for initialization. Here we construct
	 * a simple 3D scene and start a timer task to generate an animation.
	 */ 
	public final static class MyRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public void init(RenderContext r)
		{
			renderContext = r;
			VertexData vertexData = r.makeVertexData(0);
			try{
			vertexData = ObjReader.read("C:\\Users\\Giannis\\Computer-Graphics\\Computergrafik-Basecode\\obj\\teapot.obj",1f,r);
			}
			catch(IOException e1){
				e1.printStackTrace();
			}
			
			Shape shape1 = new Shape(vertexData);
			this.renderer(r, shape1);			
		}
			
					
			public void renderer(RenderContext r, Shape shape1)
			{
				shape = shape1;
				sceneManager.addShape(shape1);
				
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

				shape.setMaterial(material);
				
				
				// Register a timer task
			    Timer timer = new Timer();
			    basicstep = 0.01f;
			    currentstep = basicstep;
			    timer.scheduleAtFixedRate(new AnimationTask(), 0, 100);
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
			// Trigger redrawing of the render window
			renderPanel.getCanvas().repaint(); 
		}
	}
	
	public static class TrackBallMouseListener implements MouseListener
	{
		@Override
    	public void mousePressed(MouseEvent e) {
			
    		if(isIn==1)
    		{
	    		int x = e.getX();
	            int y = e.getY();
	            float x3D = (float) x/((float) radius/2);
	            float y3D = (float) y/((float) radius/2);
	            x3D=x3D-(float) width/radius;
	            y3D= (float)height/radius-y3D;
	            float z = 1-x3D*x3D-y3D*y3D;
	            float z3D = z>0? (float) Math.sqrt(z):0.1f;
	            p1 = new Vector3f(x3D,y3D,z3D);
	            p1.normalize();
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
	    		int x = e.getX();
	            int y = e.getY();
	            float x3D = (float) x/((float) radius/2);
	            float y3D = (float) y/((float) radius/2);
	            x3D=x3D-(float) width/radius;
	            y3D=(float) height/radius-y3D;
	            float z = 1-x3D*x3D-y3D*y3D;
	            float z3D =z>0? (float) Math.sqrt(z):0.1f;
	            p2 = new Vector3f(x3D,y3D,z3D);
	            p2.normalize();
	            
	            axis.cross(p1, p2);
	            theta = (float) (p1.angle(p2));
	            
	            Matrix4f cam = new Matrix4f(camera.getCameraMatrix());
	            cam.invert();
	            cam.setColumn(2, 0, 0, 0, 0);
	            cam.setColumn(3, 0, 0, 0, 1);
	            cam.transform(axis);
	            
	            Matrix4f rot = new Matrix4f();
	            AxisAngle4f axisAngle = new AxisAngle4f(axis, -theta);
	            rot.set(axisAngle);
	            
	            Vector3f cop = camera.getCenterOfProjection();
	            Vector3f uv = camera.getUpVector();
	            
	            rot.transform(cop);
	            rot.transform(uv);
	            uv.normalize();
	            camera.setCenterOfProjection(cop);
	            camera.setUpVector(uv);
	      		
	            
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
		renderPanel = new MyRenderPanel();
		// Make the main window of this application and add the renderer to it
		JFrame jframe = new JFrame("VirtualTrackballCamera");
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
	