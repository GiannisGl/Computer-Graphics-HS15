package simple;

import jrtr.*;
import jrtr.glrenderer.*;

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
	static int exerciseNr=2;

	public final static class HouseRenderPanel extends GLRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public final void init(RenderContext r)
		{
			renderContext = r;
			this.renderer(r,makeHouse());
		}
	
			public static final Shape makeHouse()
			{
				// A house
				float vertices[] = {-4,-4,4, 4,-4,4, 4,4,4, -4,4,4,		// front face
									-4,-4,-4, -4,-4,4, -4,4,4, -4,4,-4, // left face
									4,-4,-4,-4,-4,-4, -4,4,-4, 4,4,-4,  // back face
									4,-4,4, 4,-4,-4, 4,4,-4, 4,4,4,		// right face
									4,4,4, 4,4,-4, -4,4,-4, -4,4,4,		// top face
									-4,-4,4, -4,-4,-4, 4,-4,-4, 4,-4,4, // bottom face
			
									-20,-4,20, 20,-4,20, 20,-4,-20, -20,-4,-20, // ground floor
									-4,4,4, 4,4,4, 0,8,4,				// the roof
									4,4,4, 4,4,-4, 0,8,-4, 0,8,4,
									-4,4,4, 0,8,4, 0,8,-4, -4,4,-4,
									4,4,-4, -4,4,-4, 0,8,-4};
			
				float normals[] = {0,0,1,  0,0,1,  0,0,1,  0,0,1,		// front face
								   -1,0,0, -1,0,0, -1,0,0, -1,0,0,		// left face
								   0,0,-1, 0,0,-1, 0,0,-1, 0,0,-1,		// back face
								   1,0,0,  1,0,0,  1,0,0,  1,0,0,		// right face
								   0,1,0,  0,1,0,  0,1,0,  0,1,0,		// top face
								   0,-1,0, 0,-1,0, 0,-1,0, 0,-1,0,		// bottom face
			
								   0,1,0,  0,1,0,  0,1,0,  0,1,0,		// ground floor
								   0,0,1,  0,0,1,  0,0,1,				// front roof
								   0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, 0.707f,0.707f,0, // right roof
								   -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, -0.707f,0.707f,0, // left roof
								   0,0,-1, 0,0,-1, 0,0,-1};				// back roof
								   
				float colors[] = {1,0,0, 1,0,0, 1,0,0, 1,0,0,
								  0,1,0, 0,1,0, 0,1,0, 0,1,0,
								  1,0,0, 1,0,0, 1,0,0, 1,0,0,
								  0,1,0, 0,1,0, 0,1,0, 0,1,0,
								  0,0,1, 0,0,1, 0,0,1, 0,0,1,
								  0,0,1, 0,0,1, 0,0,1, 0,0,1,
				
								  0,0.5f,0, 0,0.5f,0, 0,0.5f,0, 0,0.5f,0,			// ground floor
								  0,0,1, 0,0,1, 0,0,1,							// roof
								  1,0,0, 1,0,0, 1,0,0, 1,0,0,
								  0,1,0, 0,1,0, 0,1,0, 0,1,0,
								  0,0,1, 0,0,1, 0,0,1,};
			
				// Set up the vertex data
				VertexData vertexData = renderContext.makeVertexData(42);;
			
				// Specify the elements of the vertex data:
				// - one element for vertex positions
				vertexData.addElement(vertices, VertexData.Semantic.POSITION, 3);
				// - one element for vertex colors
				vertexData.addElement(colors, VertexData.Semantic.COLOR, 3);
				// - one element for vertex normals
				vertexData.addElement(normals, VertexData.Semantic.NORMAL, 3);
				
				// The index data that stores the connectivity of the triangles
				int indices[] = {0,2,3, 0,1,2,			// front face
								 4,6,7, 4,5,6,			// left face
								 8,10,11, 8,9,10,		// back face
								 12,14,15, 12,13,14,	// right face
								 16,18,19, 16,17,18,	// top face
								 20,22,23, 20,21,22,	// bottom face
				                 
								 24,26,27, 24,25,26,	// ground floor
								 28,29,30,				// roof
								 31,33,34, 31,32,33,
								 35,37,38, 35,36,37,
								 39,40,41};	
			
				vertexData.addIndices(indices);
			
				Shape house = new Shape(vertexData);
				
				return house;
			}
			
			public void renderer(RenderContext r, Shape house)
			{
				shape = house;
				sceneManager.addShape(house);

				sceneManager.getFrustum().setProjectionMatrix(1, 100, 1, (float) Math.PI/3);
				
				int pictureNr = 0;
				System.out.println("which picture?");
				do{
					pictureNr = new Scanner(System.in).nextInt();
				}
				while(pictureNr<1 && pictureNr>2);

				switch(pictureNr)
				{
					case 1:{
						sceneManager.getCamera().setCenterOfProjection(new Vector3f(0f,0f,40f));
						sceneManager.getCamera().setLookAtPoint( new Vector3f(0f,0f,0f));
						sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
						break;
					}
					case 2:{
						sceneManager.getCamera().setCenterOfProjection(new Vector3f(-10f,40f,40f));
						sceneManager.getCamera().setLookAtPoint( new Vector3f(-5f,0f,0f));
						sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
						break;
					}
				}
				
				
				
				// Add the scene to the renderer
				renderContext.setSceneManager(sceneManager);
						
			}
	}
	
	public static final class FractalLandscapeRenderPanel extends GLRenderPanel
	{
		
		int fractalSizeN;
		double maxHeight;
		double length;
		double width;
		FractalLandscape fractalLandscape;
		Shape fractal;
		
		
		public FractalLandscapeRenderPanel(int fractalSizeN, double maxHeight, double length, double width)
		{
			this.fractalSizeN=fractalSizeN;
			this.maxHeight=maxHeight;
			this.length=length;
			this.width=width;
		}
		
		public final void init(RenderContext r)
		{
			renderContext = r;
			this.fractalLandscape = new FractalLandscape(r);
			this.fractal = fractalLandscape.fractal(fractalSizeN, maxHeight, length, width);
			this.renderer(r,fractal);
		}
		
		public void renderer(RenderContext r, Shape fractalLandscape)
		{
			renderContext = r;
			sceneManager.getFrustum().setProjectionMatrix(1, 100, 1, (float) Math.PI/2);
			sceneManager.getCamera().setCenterOfProjection(new Vector3f((float) length/2, 0,(float) (3*maxHeight)));
			sceneManager.getCamera().setLookAtPoint( new Vector3f((float) length/2,(float) width/2,0));
			sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
			
			// Add the object to the scene Manager
			sceneManager.addShape(fractalLandscape);
					
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
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
		/*
		System.out.println("Which exercise?");
		do{
			exerciseNr = new Scanner(System.in).nextInt();
		}
		while(exerciseNr<0 && exerciseNr>3);
		 */
		switch(exerciseNr)
		{
			case 1:{
				renderPanel = new HouseRenderPanel();
				break;
			}
			case 2:{
				int fractalN=1;
				renderPanel = new FractalLandscapeRenderPanel(fractalN, 1, 2, 2);
				break;
			}
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
