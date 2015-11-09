#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8

// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform vec4 diffuse;
uniform vec4[MAX_LIGHTS] pointLight_colors;
uniform vec4[MAX_LIGHTS] directionalLight_colors;
uniform int pLights;
uniform int dLights;

// Variables passed in from the vertex shader
in float[MAX_LIGHTS] ndotPl;
in float[MAX_LIGHTS] ndotDl;
in float[MAX_LIGHTS] sqDistanceToPL;
in vec2 frag_texcoord;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{				
	frag_shaded.xyzw = vec4(0.0,0.0,0.0,0.0);
	
	// The built-in GLSL function "texture" performs the texture lookup
	// For directional Lights
	for(int i=0; i<dLights; i++)
	{
		frag_shaded+= directionalLight_colors[i]*ndotDl[i];
	}
	
	// The built-in GLSL function "texture" performs the texture lookup
	// For point Lights
	for(int i=0; i<pLights; i++)
	{
		frag_shaded+= pointLight_colors[i]/sqDistanceToPL[i]*ndotPl[i];
	}
	
	frag_shaded*=diffuse;
	
	frag_shaded*=texture(myTexture,frag_texcoord);
}

