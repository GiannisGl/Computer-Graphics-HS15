package jrtr.swrenderer;

import jrtr.RenderContext;
import jrtr.RenderItem;
import jrtr.SceneManagerInterface;
import jrtr.SceneManagerIterator;
import jrtr.Shader;
import jrtr.Texture;
import jrtr.VertexData;
import jrtr.glrenderer.GLRenderPanel;

import java.awt.Color;
import java.awt.image.*;
import java.util.ListIterator;

import javax.vecmath.Matrix4f;
import javax.vecmath.Tuple4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector4f;


/**
 * A skeleton for a software renderer. It works in combination with
 * {@link SWRenderPanel}, which displays the output image. In project 3 
 * you will implement your own rasterizer in this class.
 * <p>
 * To use the software renderer, you will simply replace {@link GLRenderPanel} 
 * with {@link SWRenderPanel} in the user application.
 */
public class SWRenderContext implements RenderContext {

	private SceneManagerInterface sceneManager;
	private BufferedImage colorBuffer;
		
	public void setSceneManager(SceneManagerInterface sceneManager)
	{
		this.sceneManager = sceneManager;
	}
	
	/**
	 * This is called by the SWRenderPanel to render the scene to the 
	 * software frame buffer.
	 */
	public void display()
	{
		if(sceneManager == null) return;
		
		beginFrame();
	
		SceneManagerIterator iterator = sceneManager.iterator();	
		while(iterator.hasNext())
		{
			draw(iterator.next());
		}		
		
		endFrame();
	}

	/**
	 * This is called by the {@link SWJPanel} to obtain the color buffer that
	 * will be displayed.
	 */
	public BufferedImage getColorBuffer()
	{
		return colorBuffer;
	}
	
	/**
	 * Set a new viewport size. The render context will also need to store
	 * a viewport matrix, which you need to reset here. 
	 */
	public void setViewportSize(int width, int height)
	{
		colorBuffer = new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
	}
		
	/**
	 * Clear the framebuffer here.
	 */
	private void beginFrame()
	{
	}
	
	private void endFrame()
	{		
	}
	
	/**
	 * The main rendering method. You will need to implement this to draw
	 * 3D objects.
	 */
	private void draw(RenderItem renderItem)
	{
		Matrix4f objMatrix = renderItem.getT();
		Matrix4f cam = sceneManager.getCamera().getCameraMatrix();
		Matrix4f projMatrix = sceneManager.getFrustum().getProjectionMatrix();
		Matrix4f viewportMatrix = new Matrix4f();
		int startX = colorBuffer.getMinTileX();
		int startY = colorBuffer.getMinTileY();
		int height = colorBuffer.getHeight();
		int width = colorBuffer.getWidth();
		viewportMatrix.setRow(0, width/2, 0, 0, (2*startX+width)/2);
		viewportMatrix.setRow(1, 0, -height/2, 0, (2*startY+height)/2);
		viewportMatrix.setRow(2, 0, 0, 0.5f, 0.5f);
		viewportMatrix.setRow(3, 0, 0, 0, 1);
		
		VertexData vertexData = renderItem.getShape().getVertexData();
		ListIterator<VertexData.VertexElement> itr = vertexData.getElements()
				.listIterator(0);
		int nrVertices = vertexData.getNumberOfVertices();
		
		float[] vertices = new float[3*nrVertices];
		float[] colors = new float[3*nrVertices];
		float[] normals = new float[3*nrVertices];
		float[] textcoord = new float[2*nrVertices];
		
		while(itr.hasNext())
		{
			VertexData.VertexElement e = itr.next();
			switch(e.getSemantic())
			{
				case  POSITION:
				{
					vertices = e.getData();
					break;
				}
				case COLOR:
				{
					colors = e.getData();
					break;
				}
				case NORMAL:
				{
					normals = e.getData();
					break;
				}
				case TEXCOORD:
				{
					textcoord = e.getData();
					break;
				}
			}
		}
		
		for(int i=0; i<nrVertices; i++)
		{
			Vector4f vertex = new Vector4f(vertices[3*i],vertices[3*i+1],vertices[3*i+2],1f);
			objMatrix.transform(vertex);
			cam.transform(vertex);
			projMatrix.transform(vertex);
			viewportMatrix.transform(vertex);
			
			Vector2f imageVertex = new Vector2f(vertex.x/vertex.w, vertex.y/vertex.w);
			colorBuffer.setRGB((int) imageVertex.x, (int) imageVertex.y, (int) (Math.pow(2, 24)-1));
			
		}
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public Shader makeShader()	
	{
		return new SWShader();
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useShader(Shader s)
	{
	}
	
	/**
	 * Does nothing. We will not implement shaders for the software renderer.
	 */
	public void useDefaultShader()
	{
	}

	/**
	 * Does nothing. We will not implement textures for the software renderer.
	 */
	public Texture makeTexture()
	{
		return new SWTexture();
	}
	
	public VertexData makeVertexData(int n)
	{
		return new SWVertexData(n);		
	}
}
