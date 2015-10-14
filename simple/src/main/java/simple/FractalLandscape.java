package simple;

import jrtr.*;

public final class FractalLandscape
{
	
	RenderContext renderContext;
	int sizeN;
	int size;
	
	
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
		
		double diffLength = length/(size-1);
		double diffWidth = width/(size-1); 
		
		
		// The heights array:
		double[][] heightsArray = new double[size][size];
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
			v[3*i+1]=(float) diffWidth*Math.floorMod(i, size);
			v[3*i+2]=(float) heightsArray[Math.floorDiv(i, size)][Math.floorMod(i, size)];
		}
		
		
		
		// The colors of the vertices:
		float[] c = new float[3*sqSize];
		for(int i=0; i<sqSize; i++)
		{
			double height = heightsArray[Math.floorDiv(i, size)][Math.floorMod(i, size)];
			float ratio = (float) (height/(maxHeight+1f));
			c[3*i]= ratio;
			c[3*i+1]= 0.3f;
			c[3*i+2]= ratio;
		}
		
		
		// The vertex normals:
		float[] n = new float[3*sqSize];
		/*
		for(int i=0; i<sqSize; i++)
		{
			double height = heightsArray[Math.floorMod(i, size)][Math.floorDiv(i, size)];
			double ratio = height/maxHeight;
			c[3*i]=(float) (2*Math.sqrt(ratio)-ratio);
			c[3*i+1]=(float) (2*Math.sqrt(ratio)-ratio)+0.1f;
			c[3*i+2]=(float) (2*Math.sqrt(ratio)-ratio)+0.2f;
		}
		*/
				
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
				indices[2*3*(row*totalNrHorSquares+col)+1]= (row+1)*size+col+1;
				indices[2*3*(row*totalNrHorSquares+col)+2]= row*size+col+1;
				
				indices[2*3*(row*totalNrHorSquares+col)+3]= row*size+col;
				indices[2*3*(row*totalNrHorSquares+col)+4]= (row+1)*size+col;
				indices[2*3*(row*totalNrHorSquares+col)+5]= (row+1)*size+col+1;
			}
		}
		
		
			
		vertexData.addIndices(indices);
										
		// Add the object to the scene Manager
		Shape fractal = new Shape(vertexData);  		
		
		return fractal;
	}
	
	
	// Diamonds and Squares Algorithm
	// Assumes corner values are already put in
	public final double[][]	diamondsAndSquaresAlg(double[][] heightsArray, double maxHeight, int iteration)
	{
		int size = heightsArray.length;
		int nrHorSquares = (int) Math.pow(2, (iteration-1));
		int sqRadius = (int) size/(2*nrHorSquares);
		double divRange = (double) 1/nrHorSquares;
		
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
				heightsArray[x][y]=(tlCorner+trCorner+dlCorner+drCorner)/4+((float) Math.random())*divRange;
				
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
				
				heightsArray[x][y]=(lDiamond+tDiamond+rDiamond+dDiamond)/diamondVertices+((float) Math.random())*divRange;
				

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
				
				heightsArray[x][y]=(lDiamond+tDiamond+rDiamond+dDiamond)/diamondVertices+((float) Math.random())*divRange;
				

				x= (int) x+2*sqRadius*Math.floorDiv(y+2*sqRadius, size+1);
				y= (int) Math.floorMod(y+2*sqRadius, size+1);
			}
			
			return diamondsAndSquaresAlg(heightsArray, maxHeight, iteration+1);
		}
	}
	
}