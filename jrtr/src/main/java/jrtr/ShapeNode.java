package jrtr;

import javax.vecmath.Matrix4f;

public class ShapeNode extends Leaf {

	private Shape shape;
	
	@Override
	public Matrix4f getTransformationMatrix() {
		return shape.getTransformation();
	}

	@Override
	public Shape getShape() {
		return shape;
	}

	public void setShape(Shape shape) {
		this.shape = shape;
	}
}
