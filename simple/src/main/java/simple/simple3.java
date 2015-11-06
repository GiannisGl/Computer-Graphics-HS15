package simple;

import jrtr.*;
import jrtr.glrenderer.GLRenderPanel;
import jrtr.swrenderer.SWRenderPanel;
import jrtr.swrenderer.SWTexture;
import simple.VirtualTrackball.PointToSphere;
import simple.VirtualTrackball.SimpleKeyListener;
import simple.VirtualTrackball.TrackBallMouseListener;
import simple.VirtualTrackball.TrackBallMouseMotionListener;
import simple.simple.AnimationTask;

import javax.swing.*;
import java.awt.event.*;
import java.util.Timer;

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
	static Shape shape;
	static float currentstep, basicstep;
	static int width=500;
	static int height=500;
	static int exerciseNr=4;
	static boolean withObj=true;
	static int radius=Math.min(width,height);
	static Vector3f p1;
	static Vector3f p2;
	static int isIn=0;
	static Vector3f axis = new Vector3f();
	static float theta;
	static Camera camera = sceneManager.getCamera();
	
	public final static class CubeRenderPanel extends SWRenderPanel
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
				float c[] = {1,0,0, 0,1,0, 0,1,0, 1,0,0,
						     0,1,0, 1,0,0, 1,0,0, 0,1,0,
							 0,0,1, 0,1,0, 0,1,0, 0,0,1,
							 0,1,0, 0,0,1, 0,0,1, 0,1,0,
							 0,1,0, 0,0,1, 0,1,0, 1,0,0,
							 1,0,0, 0,1,0, 0,0,1, 0,1,0};
	
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
				material.swTexture = (SWTexture) renderContext.makeTexture();
				try {
					material.swTexture.load("C:/Users/Giannis/Computer-Graphics/Computergrafik-Basecode/textures/plant.jpg");
				} catch(Exception e) {				
					System.out.print("Could not load texture.\n");
					System.out.print(e.getMessage());
				}
			}
	}

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
								  0,0,1, 0,0,1, 0,0,1, 0,0,1,
								  0,1,1, 0,1,1, 0,1,1, 0,1,1,
								  1,1,1, 1,1,1, 1,1,1, 1,1,1,
								  1,0,1, 1,0,1, 1,0,1, 1,0,1,
				
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

	public final static class CylinderRenderPanel extends SWRenderPanel
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
			

			// Texture coordinates of the round faces
			float[] t = new float[2*2*segments+2*2];
			for(int i=0; i<segments; i++)
			{
				t[4*i]= (float) i/segments;
				t[4*i+1]= (float) 1;
				t[4*i+2]= (float) i/segments;
				t[4*i+3]= (float) 0;	
			}
			
			// Texture coordinates  of the top vertex
			t[4*segments]= 0.5f;
			t[4*segments+1]=0;
						
			// Texture coordinates  of the bottom vertex 
			t[4*segments+2]=0.5f;
			t[4*segments+2+1]=1;
			
			
			
			VertexData vertexData = renderContext.makeVertexData(2*segments+2);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
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
		
		public void renderer(RenderContext r, Shape cylinder)
		{
			renderContext = r;
			
			shape = cylinder;
			// Add the object to the scene Manager
			sceneManager.addShape(cylinder);

		    // Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.swTexture = (SWTexture) renderContext.makeTexture();
			try {
				material.swTexture.load("C:/Users/Giannis/Computer-Graphics/Computergrafik-Basecode/textures/plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
			


			sceneManager.getFrustum().setProjectionMatrix(1, 100, 1, (float) Math.PI/3);
					
			sceneManager.getCamera().setCenterOfProjection(new Vector3f(0f,0f,3f));
			sceneManager.getCamera().setLookAtPoint( new Vector3f(0f,0f,0f));
			sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
			
			
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
		}
	
	}
	
	public final static class TorusRenderPanel extends SWRenderPanel
	{
		/**
		 * Initialization call-back. We initialize our renderer here.
		 * 
		 * @param r	the render context that is associated with this render panel
		 */
		public final  void init(RenderContext r)
		{
			renderContext = r;
			int segments = 40;
			int hSegments = 40;
			int centralRadius = 3;
			float radius = 1f;
			this.renderer(r, torus(segments, hSegments, centralRadius, radius));
		}
		
	
		public static final Shape torus(int segments, int hSegments, int centralRadius, float radius)
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
			
			// Texture coordinates 
			float[] t = new float[hSegments*2*segments];
			for(int i=0; i<segments; i++)
			{
				for(int j=0; j<hSegments; j++)
				{
					t[hSegments*2*i+2*j] = (float) i/segments;
					t[hSegments*2*i+2*j+1] = (float) j/hSegments;
				}
			}
							
			
			VertexData vertexData = renderContext.makeVertexData(hSegments*segments);
			vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
			vertexData.addElement(v, VertexData.Semantic.POSITION, 3);
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
		
		public void renderer(RenderContext r, Shape torus)
		{
			renderContext = r;

			shape = torus;
			// Add the object to the scene Manager
			sceneManager.addShape(torus);
			

		    // Make a material that can be used for shading
			material = new Material();
			material.shader = diffuseShader;
			material.swTexture = (SWTexture) renderContext.makeTexture();
			try {
				material.swTexture.load("C:/Users/Giannis/Computer-Graphics/Computergrafik-Basecode/textures/plant.jpg");
			} catch(Exception e) {				
				System.out.print("Could not load texture.\n");
				System.out.print(e.getMessage());
			}
					
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
		
		renderPanel = new CylinderRenderPanel();
		
		// Add a key listener
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
