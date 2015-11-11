#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

// Uniform variables passed in from host program

// Variables passed in from the vertex shader
in vec4 frag_position;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{		
	float modDistance = 0.05f;
	vec2 planarPosition = vec2(frag_position.x, frag_position.z);
	float distance = length(planarPosition);
	float index = mod(distance,3*modDistance);
	
	if(index<modDistance)
	{
		frag_shaded = vec4(1.0, 0.0, 0.0, 1.0);
	}
	else if(index>=modDistance && index <2*modDistance)
	{
		frag_shaded = vec4(0.0, 0.0, 1.0, 1.0);
	}
	else
	{
		frag_shaded = vec4(1.0, 1.0, 0.0, 1.0);
	}
}

