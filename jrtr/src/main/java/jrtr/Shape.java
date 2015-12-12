package jrtr;
import java.util.ListIterator;

import javax.vecmath.*;

import jrtr.VertexData.Semantic;

/**
 * Represents a 3D object. The shape references its geometry, 
 * that is, a triangle mesh stored in a {@link VertexData} 
 * object, its {@link Material}, and a transformation {@link Matrix4f}.
 */
public class Shape {

	private Material material;
	private VertexData vertexData;
	private Matrix4f t;
	private float radius;
	private Vector3f center;
	
	/**
	 * Make a shape from {@link VertexData}. A shape contains the geometry 
	 * (the {@link VertexData}), material properties for shading (a 
	 * refernce to a {@link Material}), and a transformation {@link Matrix4f}.
	 *  
	 *  
	 * @param vertexData the vertices of the shape.
	 */
	public Shape(VertexData vertexData)
	{
		this.vertexData = vertexData;
		t = new Matrix4f();
		t.setIdentity();
		
		material = null;
	}
	
	public VertexData getVertexData()
	{
		return vertexData;
	}
	
	public void setTransformation(Matrix4f t)
	{
		this.t = t;
	}
	
	public Matrix4f getTransformation()
	{
		return t;
	}
	
	public float getRadius() {
		return radius;
	}

	public void setRadius(float radius) {
		this.radius = radius;
	}

	public Vector3f getCenter() {
		return center;
	}

	public void setCenter(Vector3f center) {
		this.center = center;
	}
	
	public void computeBoundingSphere(){
		ListIterator<VertexData.VertexElement> itr = vertexData.getElements()
				.listIterator(0);
		int nrVertices = vertexData.getNumberOfVertices();
		float[] vertices = new float[nrVertices];
		while (itr.hasNext()) {
			VertexData.VertexElement e = itr.next();
			if(e.getSemantic()==Semantic.POSITION){
				vertices = e.getData();
			}
		}
		
		float avgX = 0;
		float avgY = 0;
		float avgZ = 0;
		
		for(int i=0; i<nrVertices; i++)
		{
			avgX+=vertices[3*i];
			avgY+=vertices[3*i+1];
			avgZ+=vertices[3*i+2];
		}
		avgX/=nrVertices;
		avgY/=nrVertices;
		avgZ/=nrVertices;
		center = new Vector3f(avgX, avgY, avgZ);
		
		float maxDistance = 0;
		for(int i=0; i<nrVertices; i++)
		{
			Vector3f point = new Vector3f(vertices[3*i],vertices[3*i+1],vertices[3*i+2]);
			point.sub(center);
			float distance = point.length();
			if(distance>maxDistance)
			{
				maxDistance = distance;
			}
		}
		radius = maxDistance;
	}

	/**
	 * Set a reference to a material for this shape.
	 * 
	 * @param material
	 * 		the material to be referenced from this shape
	 */
	public void setMaterial(Material material)
	{
		this.material = material;
	}

	/**
	 * To be implemented in the "Textures and Shading" project.
	 */
	public Material getMaterial()
	{
		return material;
	}

}
