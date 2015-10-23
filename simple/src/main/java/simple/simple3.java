package simple;

import jrtr.*;
import jrtr.glrenderer.GLRenderPanel;
import jrtr.swrenderer.SWRenderPanel;

import javax.swing.*;
import java.awt.event.*;
import javax.vecmath.*;

/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple3
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
	static Vector2f p1;
	static Vector2f p2;
	static int isIn=0;
	static Vector3f axis = new Vector3f();
	static float theta;
	static int width=500;
	static int height=500;
	static int radius=Math.min(width,height);
	static int exerciseNr=4;
	static boolean withObj=true;

	public final static class HouseRenderPanel extends SWRenderPanel
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
						
				sceneManager.getCamera().setCenterOfProjection(new Vector3f(-10f,40f,40f));
				sceneManager.getCamera().setLookAtPoint( new Vector3f(-5f,0f,0f));
				sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
				
				
				// Add the scene to the renderer
				renderContext.setSceneManager(sceneManager);
						
			}
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
		
		renderPanel = new HouseRenderPanel();
		
		// Add a key listener
	    renderPanel.getCanvas().addKeyListener(new SimpleKeyListener());
			
		
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
