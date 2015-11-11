#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Uniform variables passed in from host program

// Variables passed in from the vertex shader
in vec4 frag_position;
in vec2 frag_texcoord;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{
	float row, col;
	float s = frag_texcoord.x;
	float t = frag_texcoord.y;
	
	float stride = 1;
	float size = 10;
	
	row = floor(t*size);
	col = floor(s*size);
	
	float superRow = row/stride;
	float superCol = col/stride;
	//float sum= superRow+superCol;
	float sum = superRow;
	
	float index =  mod(sum, 3);
	
	if(index<0.5f)
	{
		frag_shaded = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if(index<=1.5f)
	{
		frag_shaded = vec4(0.0, 0.0, 1.0, 1.0);
	}
	else if(index<=2.5f)
	{
		frag_shaded = vec4(0.0, 1.0, 0.0, 1.0);
	}
}

