package jrtr.swrenderer;

import jrtr.RenderContext;
import jrtr.RenderItem;
import jrtr.SceneManagerInterface;
import jrtr.SceneManagerIterator;
import jrtr.Shader;
import jrtr.Texture;
import jrtr.VertexData;
import jrtr.glrenderer.GLRenderPanel;

import java.awt.image.*;
import java.util.ListIterator;

import javax.vecmath.Matrix3f;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;
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
		int startX = colorBuffer.getMinX();
		int startY = colorBuffer.getMinY();
		int height = colorBuffer.getHeight();
		int width = colorBuffer.getWidth();
		viewportMatrix.setRow(0, width/2, 0, 0, (2*startX+width)/2);
		viewportMatrix.setRow(1, 0, -height/2, 0, (2*startY+height)/2);
		viewportMatrix.setRow(2, 0, 0, 0.5f, 0.5f);
		viewportMatrix.setRow(3, 0, 0, 0, 1);
		
		Matrix4f totalM = new Matrix4f(objMatrix);
		totalM.mul(cam, totalM);
		totalM.mul(projMatrix, totalM);
		totalM.mul(viewportMatrix, totalM);
		
		
		VertexData vertexData = renderItem.getShape().getVertexData();
		ListIterator<VertexData.VertexElement> itr = vertexData.getElements()
				.listIterator(0);
		int nrVertices = vertexData.getNumberOfVertices();

		int[] indices = vertexData.getIndices();
		float[] vertices = new float[3*nrVertices];
		float[] colorsArray = new float[3*nrVertices];
		float[] normalsArray = new float[3*nrVertices];
		float[] textcoordsArray = new float[2*nrVertices];
		
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
					colorsArray = e.getData();
					break;
				}
				case NORMAL:
				{
					normalsArray = e.getData();
					break;
				}
				case TEXCOORD:
				{
					textcoordsArray = e.getData();
					break;
				}
			}
		}

		float[][] positions = new float[3][4];
		float[][] colors = new float[3][3];
		float[][] normals = new float[3][3];
		float[][] textcoords = new float[3][2];
		
		int k=0;
		int triang=0;

		for(int j=0; j<indices.length; j++)
		{

			int i = indices[j];

			Vector4f vertex = new Vector4f(vertices[3*i],vertices[3*i+1],vertices[3*i+2], 1f);
			totalM.transform(vertex);
			
			positions[k][0]=vertex.x;
			positions[k][1]=vertex.y;
			positions[k][2]=vertex.z;
			positions[k][3]=vertex.w;
			
			colors[k][0]=colorsArray[3*i];
			colors[k][1]=colorsArray[3*i+1];
			colors[k][2]=colorsArray[3*i+2];
			
			normals[k][0]=normalsArray[3*i];
			normals[k][1]=normalsArray[3*i+1];
			normals[k][2]=normalsArray[3*i+2];
			
			textcoords[k][0]=textcoordsArray[2*i];
			textcoords[k][1]=textcoordsArray[2*i+1]; 
			
			k++;
			
			Vector2f imageVertex = new Vector2f(vertex.x/vertex.w, vertex.y/vertex.w);
			colorBuffer.setRGB((int) imageVertex.x, (int) imageVertex.y, (int) (Math.pow(2, 24)-1));

			if(k==3)
			{
				rasterizeTriangle(positions, colors, normals, textcoords);
				k=0;
				triang++;
			}
			if(triang==19)
				break;
		}
	}
	
	
	
	public void rasterizeTriangle(float[][] positions, float[][] colors, float[][] normals, float[][] textcoords)
	{
		float[][] positions2D = new float[3][3];
		for(int i=0; i<3; i++){
			positions2D[i][0]=positions[i][0];
			positions2D[i][1]=positions[i][1];
			positions2D[i][2]=positions[i][3];
		}
		
		Matrix3f barCoordMatrix = new Matrix3f();
		barCoordMatrix.setColumn(0, positions2D[0]);
		barCoordMatrix.setColumn(1, positions2D[1]);
		barCoordMatrix.setColumn(2, positions2D[2]);
		barCoordMatrix.invert();
		
		if(positions[0][2]>0 && positions[1][2]>0 && positions[2][2]>0)
		{
			int minX=getPixelMinCoord(positions2D,0);
			int minY=getPixelMinCoord(positions2D, 1);
			int maxX=getPixelMaxCoord(positions2D, 0);
			int maxY=getPixelMaxCoord(positions2D, 1);
			
			for(int x=minX; x<=maxX; x++)
			{
				for(int y=minY; y<=maxY; y++)
				{
					Vector3f pixel = new Vector3f(x,y,1);
					barCoordMatrix.transform(pixel);
										
					float alpha_w = pixel.x;
					float bita_w = pixel.y;
					float gamma_w = pixel.z; 
					if(alpha_w>0 && bita_w>0 && gamma_w>0){
						colorBuffer.setRGB(x, y, (int) (colors[0][0]*(Math.pow(2, 24)-1)+colors[0][1]*(Math.pow(2, 16)-1)+colors[0][2]*(Math.pow(2,8)-1)));
					}
				}
			}
			
			
		}
		
	}
	
	public int getPixelMinCoord(float[][] positions, int coord)
	{
		int lastCoord= positions[0].length-1;
		float min = positions[0][coord]/positions[0][lastCoord];
		for(int i=1; i<positions.length; i++)
		{
			float xW = positions[i][coord]/positions[i][lastCoord];
			min= xW<min? xW: min;
		}
		
		return (int) min;
	}
	
	public int getPixelMaxCoord(float[][] positions, int coord)
	{
		int lastCoord= positions[0].length-1;
		float max = positions[0][coord]/positions[0][lastCoord];
		for(int i=1; i<positions.length; i++)
		{
			float yW = positions[i][coord]/positions[i][lastCoord];
			max= yW>max? yW: max;
		}
		
		return (int) max+1;
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
