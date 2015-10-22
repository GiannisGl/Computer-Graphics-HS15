package jrtr;

import javax.vecmath.Matrix4f;

/**
 * Stores the specification of a viewing frustum, or a viewing
 * volume. The viewing frustum is represented by a 4x4 projection
 * matrix. You will extend this class to construct the projection 
 * matrix from intuitive parameters.
 * <p>
 * A scene manager (see {@link SceneManagerInterface}, {@link SimpleSceneManager}) 
 * stores a frustum.
 */
public class Frustum {

	private Matrix4f projectionMatrix;
	
	/**
	 * Construct a default viewing frustum. The frustum is given by a 
	 * default 4x4 projection matrix.
	 */
	public Frustum()
	{
		projectionMatrix = new Matrix4f();
		float f[] = {2.f, 0.f, 0.f, 0.f, 
					 0.f, 2.f, 0.f, 0.f,
				     0.f, 0.f, -1.02f, -2.02f,
				     0.f, 0.f, -1.f, 0.f};
		projectionMatrix.set(f);
	}
	
	public void setProjectionMatrix(float near, float far, float aspect, float fov){
		projectionMatrix = new Matrix4f();	
		projectionMatrix.setRow(0, (float) (1/(aspect*Math.tan(fov/2))), 0, 0, 0);
		projectionMatrix.setRow(1, 0, (float) (1/Math.tan(fov/2)), 0, 0);
		projectionMatrix.setRow(2, 0, 0, (float) ((near+far)/(near-far)), 2*near*far/(near-far));
		projectionMatrix.setRow(3, 0 , 0, -1, 0); 
	}
	
	/**
	 * Return the 4x4 projection matrix, which is used for example by 
	 * the renderer.
	 * 
	 * @return the 4x4 projection matrix
	 */
	public Matrix4f getProjectionMatrix()
	{
		return projectionMatrix;
	}
}
