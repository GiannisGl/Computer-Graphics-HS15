package jrtr;

import javax.vecmath.Matrix4f;

public class TransformGroup extends Group{
	
	private Matrix4f transformationMatrix;
	private Matrix4f initialTransformationMatrix;
	
	public Matrix4f getInitialTransformationMatrix() {
		return initialTransformationMatrix;
	}

	public void setInitialTransformationMatrix(Matrix4f initialTransformationMatrix) {
		this.initialTransformationMatrix = initialTransformationMatrix;
	}

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