package simple;

import jrtr.*;
import jrtr.glrenderer.*;

import javax.swing.*;
import java.awt.event.*;

import javax.vecmath.*;
/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple6
{	
	static RenderPanel renderPanel;
	static RenderContext renderContext;
	static Shader normalShader;
	static Shader diffuseShader;
	static Material material;
	static SimpleSceneManager sceneManager = new SimpleSceneManager();
	static Camera camera = sceneManager.getCamera();
	static Shape shape;
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
	static int exerciseNr=4;

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
			this.body = new RotationalBezierBody(bezierSegments, controlPoints, nrEvPoints, nrRotStepts);
			this.rotationalBody = body.createBody();
			
			shape = rotationalBody;
			
			this.renderer(r, rotationalBody);
		}
		
		public void renderer(RenderContext r, Shape shape)
		{
			renderContext = r;
						
			// Add the object to the scene Manager
			sceneManager.addShape(shape);
					
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
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
	 * The main function opens a 3D rendering window, implemented by the class
	 * {@link SimpleRenderPanel}. {@link SimpleRenderPanel} is then called backed 
	 * for initialization automatically. It then constructs a simple 3D scene, 
	 * and starts a timer task to generate an animation.
	 */
	public static void main(String[] args)
	{		
		// Make a render panel. The init function of the renderPanel
		// (see above) will be called back for initialization.
		
		Vector3f[] controlPoints = new Vector3f[4];
		controlPoints[0] = new Vector3f(1,0,0);
		controlPoints[1] = new Vector3f(2,1,0);
		controlPoints[2] = new Vector3f(2,2,0);
		controlPoints[3] = new Vector3f(1,3,0);
		
		renderPanel = new RotationalBodyRenderPanel(1, controlPoints, 2, 10);

		currentstep = 1f;
		// Add a mouse and key listener

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
