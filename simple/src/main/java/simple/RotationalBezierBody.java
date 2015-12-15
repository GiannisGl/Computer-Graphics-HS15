package simple;

import javax.vecmath.Matrix4d;
import javax.vecmath.Matrix4f;
import javax.vecmath.Vector3f;
import javax.vecmath.Vector4f;

import jrtr.*;
import jrtr.VertexData.Semantic;
import jrtr.glrenderer.GLVertexData;

public final class RotationalBezierBody {
	
	int bezierSegments;
	Vector3f[] controlPoints;
	int nrControlPoints;
	int nrEvPoints;
	int nrRotSteps;
	int nrVertices;
	int nrCurvePoints;
	Matrix4f bernstein;
	
	public RotationalBezierBody(int bezierSegments, Vector3f[] controlPoints, int nrEvPoints, int nrRotSteps) {
		super();
		this.bezierSegments = bezierSegments;
		this.controlPoints = controlPoints;
		this.nrEvPoints = nrEvPoints;
		this.nrRotSteps = nrRotSteps;
		nrCurvePoints = (bezierSegments*(nrEvPoints+1)+1);
		nrVertices = nrCurvePoints*(nrRotSteps+1);
		nrControlPoints = controlPoints.length;
		bernstein = new Matrix4f(-1, 3, -3, 1, 3, -6, 3, 0, -3, 3, 0, 0, 1, 0, 0, 0);
	}
	
	public Shape createBody(){
		if(nrControlPoints!=bezierSegments*3+1){
			System.out.println("incorrect number of control points");
			return null;
		}
		
		Vector3f[][] vertices = rotationalBodyVertices();
		
		// vertices
		float[] floatVertices = new float[3*nrVertices];
		for(int i=0; i<nrVertices; i++){
			floatVertices[3*i] = vertices[i][0].x;
			floatVertices[3*i+1] = vertices[i][0].y;
			floatVertices[3*i+2] = vertices[i][0].z;
		}
		
		// normals
		float[] floatNormals = new float[3*nrVertices];
		for(int i=0; i<nrVertices; i++){
			floatNormals[3*i] = vertices[i][1].x;
			floatNormals[3*i+1] = vertices[i][1].y;
			floatNormals[3*i+2] = vertices[i][1].z;
		}
		
		// colors
		float[] colors = new float[3*nrVertices];
		for(int i=0; i<nrVertices; i++)
		{
			colors[3*i]= (float) Math.floorMod(Math.floorDiv(i, nrCurvePoints),2);
			colors[3*i+1]= (float) Math.floorMod(Math.floorDiv(i, nrCurvePoints),2);
			colors[3*i+2]= (float) Math.floorMod(Math.floorDiv(i, nrCurvePoints),2);
		}
		
		// textCoords
		float[] textCoords = new float[2*nrVertices];
		for(int i=0; i<nrVertices; i++)
		{
			textCoords[2*i]= vertices[i][2].x;
			textCoords[2*i+1]= vertices[i][2].y;
		}
		
		VertexData vertexData = new GLVertexData(nrVertices);
		vertexData.addElement(floatVertices, Semantic.POSITION, 3);
		vertexData.addElement(floatNormals, Semantic.NORMAL, 3);
		vertexData.addElement(colors, Semantic.COLOR, 3);
		vertexData.addElement(textCoords, Semantic.TEXCOORD, 2);
		
		int indices[] = new int[2*3*(nrVertices-nrRotSteps)];
		for(int i=0; i<nrRotSteps; i++)
		{
			for(int j=0; j<nrCurvePoints-1; j++)
			{
				indices[6*((nrCurvePoints-1)*i+j)]=nrCurvePoints*i+j;
				indices[6*((nrCurvePoints-1)*i+j)+1]=nrCurvePoints*(i+1)+j;
				indices[6*((nrCurvePoints-1)*i+j)+2]=nrCurvePoints*(i+1)+j+1;
				
				indices[6*((nrCurvePoints-1)*i+j)+3]=nrCurvePoints*i+j;
				indices[6*((nrCurvePoints-1)*i+j)+4]=nrCurvePoints*(i+1)+j+1;
				indices[6*((nrCurvePoints-1)*i+j)+5]=nrCurvePoints*i+j+1;
			}
		}
		
		vertexData.addIndices(indices);
		
		
		Shape rotationalBody = new Shape(vertexData);  
		
		return rotationalBody;
	}
	
	public Vector3f[][] rotationalBodyVertices(){
		Vector3f[][] curveVertices = curveVertices();
		Vector3f[][] rotationalBodyVertices = new Vector3f[nrVertices][3];
		
		for(int i=0; i<=nrRotSteps; i++){
			double angle = 2*Math.PI*i/nrRotSteps;
			Matrix4d rotY = new Matrix4d();
			rotY.rotY(angle);
			for(int j=0; j<nrCurvePoints; j++){
				// rotate point
				Vector3f pInit = new Vector3f(curveVertices[j][0]);
				rotY.transform(pInit);
				rotationalBodyVertices[nrCurvePoints*i+j][0] = new Vector3f(pInit);
				
				// rotate normal
				Vector3f nInit = new Vector3f(curveVertices[j][1]);
				rotY.transform(nInit);
				rotationalBodyVertices[nrCurvePoints*i+j][1] = new Vector3f(nInit);
				
				Vector3f textCoord = new Vector3f();
				textCoord.x = (float) i/nrRotSteps;
				textCoord.y = (float) j/(nrCurvePoints-1);
				textCoord.z = 0;
				rotationalBodyVertices[nrCurvePoints*i+j][2] = new Vector3f(textCoord);
			}
		}
		
		return rotationalBodyVertices;
	}
	
	public Vector3f[][] curveVertices(){
		// array of coords and normals
		Vector3f[][] vertices = new Vector3f[nrCurvePoints][2];
		
		for(int i=0; i<bezierSegments; i++){
			Vector3f p0 = controlPoints[3*i];
			Vector3f p1 = controlPoints[3*i+1];
			Vector3f p2 = controlPoints[3*i+2];
			Vector3f p3 = controlPoints[3*i+3];
			
			for(int j=0; j<=nrEvPoints; j++){
				float t = (float) j/(nrEvPoints+1);
				Vector3f[] point = pointOnSegment(t, p0, p1, p2, p3);
				vertices[(nrEvPoints+1)*i+j][0] = point[0];
				vertices[(nrEvPoints+1)*i+j][1] = point[1];
			}
			if( i == bezierSegments-1){
				Vector3f[] point = pointOnSegment(1, p0, p1, p2, p3);
				vertices[nrCurvePoints-1][0] = point[0];
				vertices[nrCurvePoints-1][1] = point[1];
			}
		}
		
		
		return vertices;
	}
	
	public Vector3f[] pointOnSegment(float t, Vector3f p0, Vector3f p1, Vector3f p2, Vector3f p3){
		// array of coords and normals
		Vector3f[] point = new Vector3f[2];
		
		// C matrix
		Matrix4f vectors = new Matrix4f();
		vectors.setColumn(0, p0.x, p0.y, p0.z, 1);
		vectors.setColumn(1, p1.x, p1.y, p1.z, 1);
		vectors.setColumn(2, p2.x, p2.y, p2.z, 1);
		vectors.setColumn(3, p3.x, p3.y, p3.z, 1);
		vectors.mul(bernstein);
		
		// coordinates
		Vector4f tVector = new Vector4f((float) Math.pow(t, 3), (float) Math.pow(t, 2), (float) Math.pow(t, 1), 1);
		vectors.transform(tVector);
		Vector3f xt = new Vector3f(tVector.x, tVector.y, tVector.z);
		point[0] = xt;
		
		// normals
		Vector4f tangentVector = new Vector4f((float) Math.pow(t, 2)*3, 2*t, 1, 0);
		vectors.transform(tangentVector);
		Vector3f nt = new Vector3f(tangentVector.y, -tangentVector.x, 0);
		nt.normalize();
		point[1] = nt;
		
		return point;
	}
}