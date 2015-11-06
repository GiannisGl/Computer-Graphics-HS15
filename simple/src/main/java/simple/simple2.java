package simple;

import jrtr.*;
import jrtr.glrenderer.*;
import jrtr.swrenderer.SWRenderPanel;

import javax.swing.*;
import java.awt.event.*;
import java.io.IOException;

import javax.vecmath.*;

import java.util.Scanner;
/**
 * Implements a simple application that opens a 3D rendering window and 
 * shows a rotating cube.
 */
public class simple2
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
				
				int pictureNr = 2;
				
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
	
	public static final class FractalLandscapeRenderPanel extends SWRenderPanel
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
			//shape = fractal;
			
			if(withObj)
			{
			VertexData vertexData = r.makeVertexData(0);
				try{
				vertexData = ObjReader.read("C:\\Users\\Giannis\\Computer-Graphics\\Computergrafik-Basecode\\obj\\airplane.obj",1f,r);
				}
				catch(IOException e1){
					e1.printStackTrace();
				}
				
				Shape airplane = new Shape(vertexData);
				shape = airplane;
				Matrix4f t = shape.getTransformation();
				AxisAngle4f axis = new AxisAngle4f(new Vector3f(0,1,0), (float) Math.PI/2);
				t.set(axis);
	    		t.setScale(5f);
				Matrix4f trans = new Matrix4f();
	    		Vector3f vector = new Vector3f((float) length/2, (float) (maxHeight*0.75f-2), (float) -width/4+10);
	    		trans.setTranslation(vector);
	    		t.add(trans);
	    		shape.setTransformation(t);
	    		this.renderer(r, airplane);
			}
			
			this.renderer(r,fractal);
		}
		
		public void renderer(RenderContext r, Shape fractalLandscape)
		{
			renderContext = r;
			sceneManager.getFrustum().setProjectionMatrix(1, 500, 1, (float) Math.PI/2);
			sceneManager.getCamera().setCenterOfProjection(new Vector3f((float) length/2, (float) (maxHeight*0.75f), (float) -width/4));
			sceneManager.getCamera().setLookAtPoint( new Vector3f((float) length/2, (float) (maxHeight*0.75f), (float) width/2));
			sceneManager.getCamera().setUpVector(new Vector3f(0f,1f,0f));
			
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

			
			// Add the object to the scene Manager
			sceneManager.addShape(fractalLandscape);
					
			// Add the scene to the renderer
			renderContext.setSceneManager(sceneManager);
		}
	}
	
	public static class MyMouseListener implements MouseListener
	{
		@Override
    	public void mousePressed(MouseEvent e) {
			
    		if(isIn==1)
    		{
	    		int x = e.getX();
	            int y = e.getY();
	            p1 = new Vector2f(x,y);
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
	public static class TrackballMouseMotionListener implements MouseMotionListener
	{		
		
		@Override
		public void mouseDragged(MouseEvent e) {

    		
			if(isIn==1)
    		{

				Matrix4f cam = new Matrix4f(camera.getCameraMatrix());
				//cam.invert();
				
	    		int x = e.getX();
	            int y = e.getY();
	            
	            p2 = new Vector2f(x,y);
	            
	            
	            Vector2f dp = new Vector2f();
	            dp.sub(p2, p1);
	            float thetaX = dp.x/width;
	            float thetaY = dp.y/height; 
	            Vector3f axisX = camera.getUpVector();
	            Vector3f axisY = axisY();

	            AxisAngle4f axisAngleX = new AxisAngle4f(axisX, (float) (-currentstep*thetaX));
	            AxisAngle4f axisAngleY = new AxisAngle4f(axisY, (float) (-currentstep*thetaY));
	            
	           
	            Vector3f lap = camera.getLookAtPoint();
	            Vector3f cop = camera.getCenterOfProjection();
	            Vector3f uv = camera.getUpVector();
	            Vector3f diff = new Vector3f();
	            diff.sub(lap, cop);
	            //Vector3f diff1=new Vector3f(diff);
	            
	            
	            Matrix4f rotX = new Matrix4f();
	            rotX.set(axisAngleX);
	            Matrix4f rotY = new Matrix4f();
	            rotY.set(axisAngleY);
	            
				rotX.transform(diff);
				rotY.transform(diff);
				diff.add(cop);
				camera.setLookAtPoint(diff);
				
				rotY.transform(uv);
				camera.setUpVector(uv);
				
	            
				if(withObj)
				{
					/*
					Vector3f axisPlane = axisX;
					axisPlane.cross(diff, diff1);
					float angle = (float) diff.angle(diff1);
					AxisAngle4f axisAnglePlane = new AxisAngle4f(axisPlane, angle);
					Matrix4f t = new Matrix4f();
					t.set(axisAnglePlane);
					*/
					Matrix4f plane = shape.getTransformation();
					
					//plane.mul(t, plane);;
					
					plane.mul(cam, plane);
					rotY.invert();
					plane.mul(rotY, plane);
					plane.mul(rotX, plane);
					cam.invert();
					plane.mul(cam, plane);
					shape.setTransformation(plane);
				}
				
	            p1=new Vector2f(p2);
    		}
			

			// Trigger redrawing
			renderPanel.getCanvas().repaint();
		}
		@Override
		public void mouseMoved(MouseEvent e) {}
		
		public Vector3f axisY()
		{
			Vector3f cop = camera.getCenterOfProjection();
			Vector3f lap = camera.getLookAtPoint();
			Vector3f uv = camera.getUpVector();
			
			Vector3f z = new Vector3f();
			z.sub(cop, lap);
			z.normalize();
			
			Vector3f x =  new Vector3f();
			x.cross(uv, z);
			x.normalize();
			
			return x;
		}
		
	}
	
	
	// Key listener for exercise 2.4
	public static class MyKeyListener implements KeyListener
	{
		public void keyPressed(KeyEvent e)
		{
			switch(e.getKeyChar())
			{
				case 'w': {
					// move forward
					moveX(currentstep);
					break;
				}
				case 's': {
					// move backwards
					moveX(-currentstep);
					break;
				}
				case 'a': {
					// move left
					moveY(-currentstep);
					break;
				}
				case 'd': {
					// move right
					moveY(currentstep);
					break;
				}
				case 'e':{
					// move up
					moveZ(currentstep);
					break;
				}
				case 'q':{
					// move down
					moveZ(-currentstep);
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
		
		
		public void moveX(float n)
		{

			Matrix4f cam = new Matrix4f(camera.getCameraMatrix());
			cam.invert();
			Vector3f cop = camera.getCenterOfProjection();
			Vector3f lap = camera.getLookAtPoint();
			
			Vector3f translationAxis = new Vector3f();
			translationAxis.sub(lap, cop);
			translationAxis.scale(n);
			translationAxis.normalize();
			
			cop.add(translationAxis);
			camera.setCenterOfProjection(cop);
			lap.add(translationAxis);
			camera.setLookAtPoint(lap);
			
			if(withObj)
			{
				Matrix4f plane = shape.getTransformation();
				Matrix4f trans = new Matrix4f();
				trans.set(1);
				//translationAxis.negate();
				trans.setTranslation(translationAxis);
				//plane.mul(cam, plane);
				plane.mul(trans, plane);
				cam.invert();
				//plane.mul(cam, plane);
				shape.setTransformation(plane);
			}
		}
		
		public void moveY(float n)
		{
			Vector3f cop = camera.getCenterOfProjection();
			Vector3f lap = camera.getLookAtPoint();
			Vector3f uv = camera.getUpVector();
			
			Vector3f z = new Vector3f();
			z.sub(cop, lap);
			z.normalize();
			
			Vector3f translationAxis =  new Vector3f();
			translationAxis.cross(uv, z);
			translationAxis.scale(n);
			
			cop.add(translationAxis);
			camera.setCenterOfProjection(cop);
			lap.add(translationAxis);
			camera.setLookAtPoint(lap);
			
			if(withObj)
			{
				Matrix4f plane = shape.getTransformation();
				Matrix4f trans = new Matrix4f();
				trans.set(1);
				trans.setTranslation(translationAxis);
				plane.mul(trans, plane);
				shape.setTransformation(plane);
			}
		}
		
		public void moveZ(float n)
		{
			Vector3f cop = camera.getCenterOfProjection();
			Vector3f lap = camera.getLookAtPoint();
			Vector3f uv = camera.getUpVector();
						
			Vector3f translationAxis =  new Vector3f();
			translationAxis = uv;
			//translationAxis.scale(n);
			
			if(n>0)
			{
				cop.add(translationAxis);
				lap.add(translationAxis);
			}
			else
			{
				cop.sub(translationAxis);
				lap.sub(translationAxis);
			}
			//cop.add(translationAxis);
			camera.setCenterOfProjection(cop);
			//lap.add(translationAxis);
			camera.setLookAtPoint(lap);
			
			if(withObj)
			{
				Matrix4f plane = shape.getTransformation();
				Matrix4f trans = new Matrix4f();
				trans.set(1);
				translationAxis.negate();
				trans.setTranslation(translationAxis);
				plane.mul(trans, plane);
				shape.setTransformation(plane);
			}
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
				// exercise 2.1
				renderPanel = new HouseRenderPanel();
				break;
			}
			case 3:{
				// exercise 2.3
				int fractalN=10;
				renderPanel = new FractalLandscapeRenderPanel(fractalN, 100, 100, 100);
				break;
			}
			case 4:{
				// exercise 2.4
				int fractalN=10;
				renderPanel = new FractalLandscapeRenderPanel(fractalN, 500, 500, 500);

				currentstep = 1f;
				// Add a mouse and key listener
			    renderPanel.getCanvas().addMouseListener(new MyMouseListener());
			    renderPanel.getCanvas().addMouseMotionListener(new TrackballMouseMotionListener());
			    renderPanel.getCanvas().addKeyListener(new MyKeyListener());
				break;
			}
		}
		
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
