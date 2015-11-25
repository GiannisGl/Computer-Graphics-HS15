package jrtr;

import javax.vecmath.Matrix4f;

public class LightNode extends Leaf {
	
	Light light;
	Matrix4f transformationMatrix;

	@Override
	public Matrix4f getTransformationMatrix() {
		return transformationMatrix;
	}
	
	public void setTransformationMatrix(Matrix4f matrix){
		transformationMatrix=new Matrix4f(matrix);
	}

	@Override
	public Shape getShape() {
		return null;
	}
	
	public Light getLight(){
		return light;
	}
	
	public void setLight(Light light){
		this.light=light;
	}

}
