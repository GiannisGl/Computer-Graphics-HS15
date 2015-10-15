package simple;

import java.util.Random;

import javax.vecmath.Vector3f;

import jrtr.*;

public final class FractalLandscape
{
	
	RenderContext renderContext;
	int sizeN;
	int size;
	double diffLength;
	double diffWidth;
	double[][] heightsArray;
	
	public FractalLandscape(RenderContext r)
	{
		renderContext = r;
	}
	
	
	// Make a fractal Landscape
	public final Shape fractal(int fractalSizeN, double maxHeight, double length, double width)
	{	
		this.sizeN=fractalSizeN;
		this.size=(int) Math.pow(2, sizeN)+1;
		int sqSize= size*size;
		
		diffLength = length/(size-1);
		diffWidth = width/(size-1); 
		
		
		// The heights array:
		heightsArray = new double[size][size];
		// Initialize the corners
		heightsArray[0][0]= Math.random()*maxHeight;
		heightsArray[0][size-1]=Math.random()*maxHeight;
		heightsArray[size-1][0]=Math.random()*maxHeight;
		heightsArray[size-1][size-1]=Math.random()*maxHeight;
		
		heightsArray = diamondsAndSquaresAlg(heightsArray,maxHeight, 1);
		
		// The vertex positions:
		float[] v = new float[3*sqSize];
		for(int i=0; i<sqSize; i++)
		{
			v[3*i]=(float) diffLength*Math.floorDiv(i, size);
			v[3*i+1]=(float) heightsArray[Math.floorDiv(i, size)][Math.floorMod(i, size)];
			v[3*i+2]=(float) diffWidth*Math.floorMod(i, size);
		}
		
		
		
		// The colors of the vertices:
		float[] c = new float[3*sqSize];
		for(int i=0; i<sqSize; i++)
		{
			double height = heightsArray[Math.floorDiv(i, size)][Math.floorMod(i, size)];
			float ratio = (float) (height/(maxHeight+2f));
			c[3*i]= ratio;//height<maxHeight*0.6f ? 0f:  (float) (2*Math.sqrt(ratio)-ratio)*0.5f+0.5f;
			c[3*i+1]= ratio;//height<maxHeight*0.6f ? (float) (ratio*ratio*0.2f+0.3f): (float) (2*Math.sqrt(ratio)-ratio)*0.5f+0.5f;
			c[3*i+2]= ratio;//height<maxHeight*0.6f ? 0f: (float) (2*Math.sqrt(ratio)-ratio)*0.5f+0.5f;
		}
		
		
		// The vertex normals:
		float[] n = new float[3*sqSize];
		for(int x=0; x<size; x++)
		{
			for(int y=0; y<size; y++)
			{	
				setNormal(n, x, y);
			}
		}
				
		// Construct a data structure that stores the vertices, their
		// attributes, and the triangle mesh connectivity
		VertexData vertexData = renderContext.makeVertexData(sqSize);	
		vertexData.addElement(v, VertexData.Semantic.POSITION, 3);	
		vertexData.addElement(c, VertexData.Semantic.COLOR, 3);
		vertexData.addElement(n, VertexData.Semantic.NORMAL, 3);
		
		// The triangles (three vertex indices for each triangle)
		int totalNrHorSquares = size-1;
		int totalNrSquares = totalNrHorSquares*totalNrHorSquares;
		int indices[] = new int[2*3*totalNrSquares];
		for(int row=0; row<totalNrHorSquares; row++)
		{
			for(int col=0; col<totalNrHorSquares; col++)
			{
				indices[2*3*(row*totalNrHorSquares+col)]= row*size+col;
				indices[2*3*(row*totalNrHorSquares+col)+1]= row*size+col+1;
				indices[2*3*(row*totalNrHorSquares+col)+2]= (row+1)*size+col+1;
				
				indices[2*3*(row*totalNrHorSquares+col)+3]= row*size+col;
				indices[2*3*(row*totalNrHorSquares+col)+4]= (row+1)*size+col+1;
				indices[2*3*(row*totalNrHorSquares+col)+5]= (row+1)*size+col;
			}
		}
		
		
			
		vertexData.addIndices(indices);
										
		// Add the object to the scene Manager
		Shape fractal = new Shape(vertexData);  		
		
		return fractal;
	}
	
	public final void setNormal(float[] n, int x, int y)
	{

		Vector3f w = getVector(x, y);
		Vector3f nm = new Vector3f();
		Vector3f n1 = new Vector3f();
		Vector3f n2 = new Vector3f();
		Vector3f n3 = new Vector3f();
		Vector3f n4 = new Vector3f();
		Vector3f v1 = new Vector3f();
		Vector3f v2 = new Vector3f();
		Vector3f v3 = new Vector3f();
		Vector3f v4 = new Vector3f();
		Vector3f v0 = new Vector3f();
		if(y-1>=0){
			v1=getVector(x, y-1);
			v1.sub(v1, w);
		}
		if(x-1>=0){
			v2=getVector(x-1, y);
			v2.sub(v2, w);
		}
		if(y+1<size){
			v3=getVector(x, y+1);
			v3.sub(v3, w);
		}
		if(x+1<size){
			v4=getVector(x+1, y);
			v4.sub(v4, w);
		}
		
		if(!v1.equals(v0) && !v2.equals(v0)){
			n1.cross(v2, v1);
			nm.add(n1);
		}
		if(!v2.equals(v0) && !v3.equals(v0)){
			n2.cross(v3, v2);
			nm.add(n2);
		}
		if(!v3.equals(v0) && !v4.equals(v0)){
			n3.cross(v4, v3);
			nm.add(n3);
		}
		if(!v4.equals(v0) && !v1.equals(v0)){
			n4.cross(v1, v4);
			nm.add(n4);
		}
		
		nm.normalize();
		
		n[3*(x*size+y)]=nm.x;
		n[3*(x*size+y)+1]=nm.y;
		n[3*(x*size+y)+2]=nm.z;
	}
	
	public final Vector3f getVector(int x, int y)
	{
		Vector3f v = new Vector3f();
		v.x = (float) diffLength*x;
		v.y =(float) heightsArray[x][y];
		v.z =(float) diffWidth*y;
		return v;
	}
	
	
	// Diamonds and Squares Algorithm
	// Assumes corner values are already put in
	public final double[][]	diamondsAndSquaresAlg(double[][] heightsArray, double maxHeight, int iteration)
	{
		int size = heightsArray.length;
		int nrHorSquares = (int) Math.pow(2, (iteration-1));
		int sqRadius = (int) size/(2*nrHorSquares);
		double divRange = (double) maxHeight/(4*nrHorSquares);
		
		if(heightsArray.length!=heightsArray[0].length)
		{
			System.out.println("the array is not square. Method returns null");
			return null;
		}
		else if(sqRadius==0)
		{
			return heightsArray;
		}
		else
		{	
			// Square step
			int x = sqRadius;
			int y = sqRadius;
			
			while(x>=0&&x<size&&y>=0&&y<size)
			{								
				double tlCorner = heightsArray[x-sqRadius][y-sqRadius];
				double trCorner = heightsArray[x-sqRadius][y+sqRadius];
				double dlCorner = heightsArray[x+sqRadius][y-sqRadius];
				double drCorner = heightsArray[x+sqRadius][y+sqRadius];
								
				// Square step
				heightsArray[x][y]=(tlCorner+trCorner+dlCorner+drCorner)/4+(float) (2*Math.random()-1)*divRange;
				
				x= (int) x+2*sqRadius*Math.floorDiv(y+2*sqRadius, size-1);
				y= (int) Math.floorMod(y+2*sqRadius, size-1);
			}
			
			x = 0;
			y = sqRadius;
			
			// Diamond step
			while(x>=0&&x<size&&y>=0&&y<size)
			{
				// Addapt for vertices at the border of the array 
				int diamondVertices =4;
				double lDiamond = y-sqRadius>=0? heightsArray[x][y-sqRadius]: 0;
				diamondVertices = y-sqRadius>=0? diamondVertices:--diamondVertices;
				double tDiamond = x-sqRadius>=0? heightsArray[x-sqRadius][y]: 0;
				diamondVertices = (x-sqRadius)>=0? diamondVertices:--diamondVertices;
				double rDiamond = y+sqRadius<size? heightsArray[x][y+sqRadius]: 0;
				diamondVertices = y+sqRadius<size? diamondVertices:--diamondVertices;
				double dDiamond = x+sqRadius<size? heightsArray[x+sqRadius][y]: 0;
				diamondVertices = x+sqRadius<size? diamondVertices:--diamondVertices;
				
				heightsArray[x][y]=(lDiamond+tDiamond+rDiamond+dDiamond)/diamondVertices+(float) (2*Math.random()-1)*divRange;
				

				x= (int) x+2*sqRadius*Math.floorDiv(y+2*sqRadius, size-1);
				y= (int) Math.floorMod(y+2*sqRadius, size-1);
			}
			
			x = sqRadius;
			y = 0;
			
			while(x>=0&&x<size&&y>=0&&y<size)
			{
				// Addapt for vertices at the border of the array 
				int diamondVertices =4;
				double lDiamond = y-sqRadius>=0? heightsArray[x][y-sqRadius]: 0;
				diamondVertices = y-sqRadius>=0? diamondVertices:--diamondVertices;
				double tDiamond = x-sqRadius>=0? heightsArray[x-sqRadius][y]: 0;
				diamondVertices = (x-sqRadius)>=0? diamondVertices:--diamondVertices;
				double rDiamond = y+sqRadius<size? heightsArray[x][y+sqRadius]: 0;
				diamondVertices = y+sqRadius<size? diamondVertices:--diamondVertices;
				double dDiamond = x+sqRadius<size? heightsArray[x+sqRadius][y]: 0;
				diamondVertices = x+sqRadius<size? diamondVertices:--diamondVertices;
				
				heightsArray[x][y]=(lDiamond+tDiamond+rDiamond+dDiamond)/diamondVertices+(float) (2*Math.random()-1)*divRange;
				

				x= (int) x+2*sqRadius*Math.floorDiv(y+2*sqRadius, size+2*sqRadius-1);
				y= (int) Math.floorMod(y+2*sqRadius, size+2*sqRadius-1);
			}
			
			return diamondsAndSquaresAlg(heightsArray, maxHeight, iteration+1);
		}
	}
	
}