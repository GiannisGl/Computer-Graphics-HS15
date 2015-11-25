#version 150
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

#define MAX_LIGHTS 8

// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;
uniform vec4[MAX_LIGHTS] lightDirection;
uniform vec4[MAX_LIGHTS] light_positions;
uniform int dLights;
uniform int pLights;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec3 normal;
in vec4 color;
in vec4 position;
in vec2 texcoord;

// Output variables for fragment shader
out vec4 frag_color;
out vec4 mVnormal4f;
out vec4 mVposition;
out float[MAX_LIGHTS] ndotPl;
out float[MAX_LIGHTS] ndotDl;
out float[MAX_LIGHTS] sqDistanceToPL;
out vec2 frag_texcoord;

void main()
{		
	// Compute dot product of normal and directional Light
	// Note: here we assume "lightDirection" is specified in camera coordinates,
	// so we transform the normal to camera coordinates, and we don't transform
	// the light direction, i.e., it stays in camera coordinates
	
	for(int i=0; i<dLights; i++)
	{
		ndotDl[i]=max(dot(modelview*vec4(normal,0),lightDirection[i]),0);
	}
	
	// Compute dot product of normal and point Light
	// Note: here we assume "light_positions" is specified in camera coordinates,
	// so we transform the normal and the vertex_position to camera coordinates, 
	//and we don't transform the light position, i.e., it stays in camera coordinates
	for(int i=0; i<pLights; i++)
	{
		ndotPl[i]=max(dot(modelview*vec4(normal,0),normalize(light_positions[i]-modelview*position)),0);
		sqDistanceToPL[i]=length(light_positions[i]-modelview*position);//dot(light_positions[i]-modelview*position,light_positions[i]-modelview*position);
	}

	// Pass texture coordiantes to fragment shader, OpenGL automatically
	// interpolates them to each pixel  (in a perspectively correct manner) 
	frag_texcoord = texcoord;
	
	frag_color = color;

	mVposition = modelview*position;
	mVnormal4f = modelview*vec4(normal,0);
	
	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
