#version 150
// GLSL version 1.50
// Fragment shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8
#define PHONG_PARAM 1

// Uniform variables passed in from host program
uniform sampler2D myTexture;
uniform vec4 materialDiffuse;
uniform vec4 materialSpecular;
uniform vec4[MAX_LIGHTS] lightDirection;
uniform vec4[MAX_LIGHTS] light_positions;
uniform vec4[MAX_LIGHTS] pointLight_colors;
uniform vec4[MAX_LIGHTS] directionalLight_colors;
uniform int pLights;
uniform int dLights;

// Variables passed in from the vertex shader
in vec4 mVnormal4f;
in vec4 mVposition;
in float[MAX_LIGHTS] ndotPl;
in float[MAX_LIGHTS] ndotDl;
in float[MAX_LIGHTS] sqDistanceToPL;
in vec2 frag_texcoord;

// Output variable, will be written to framebuffer automatically
out vec4 frag_shaded;

void main()
{				
	vec4[MAX_LIGHTS] reflextionPL;
	vec4[MAX_LIGHTS] reflextionDL;
	vec4 ambientLightColor = vec4(0.1,0.1,0.1,1.0);
	float specular = 0.5f*(texture(myTexture,frag_texcoord).x+texture(myTexture,frag_texcoord).y+texture(myTexture,frag_texcoord).z);
	frag_shaded.xyzw = vec4(0.0,0.0,0.0,0.0);
	
	// The built-in GLSL function "texture" performs the texture lookup
	// For directional Lights
	for(int i=0; i<dLights; i++)
	{
		frag_shaded+= directionalLight_colors[i]*ndotDl[i]*texture(myTexture,frag_texcoord);
		reflextionDL[i]=2*ndotDl[i]*mVnormal4f-lightDirection[i];
		frag_shaded+= directionalLight_colors[i]*pow(max(dot(reflextionDL[i],(-mVposition)),0),PHONG_PARAM)*specular;
	}
	
	// The built-in GLSL function "texture" performs the texture lookup
	// For point Lights
	for(int i=0; i<pLights; i++)
	{
		vec4 radiance = pointLight_colors[i]/sqDistanceToPL[i];
		frag_shaded+= (radiance*ndotPl[i]*texture(myTexture,frag_texcoord));
		reflextionPL[i]=normalize(2*ndotPl[i]*mVnormal4f-normalize(light_positions[i]-mVposition));
		frag_shaded+= radiance*pow(max(dot(reflextionPL[i],normalize(-mVposition)),0),PHONG_PARAM)*specular;
	}
		
	frag_shaded*=texture(myTexture,frag_texcoord);
	
	frag_shaded+=texture(myTexture,frag_texcoord)*ambientLightColor;
}

