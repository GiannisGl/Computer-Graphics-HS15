package jrtr;

import javax.vecmath.Matrix4f;

public class TransformGroup extends Group{
	
	private Matrix4f transformationMatrix;
	
	public void setTransformationMatrix(Matrix4f transformationMatrix) {
		this.transformationMatrix = transformationMatrix;
	}

	@Override
	public Matrix4f getTransformationMatrix() {
		return transformationMatrix;
	}
	
	@Override
	public Shape getShape() {
		return null;
	}
}