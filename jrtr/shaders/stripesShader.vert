#version 150
// GLSL version 1.50 
// Vertex shader for diffuse shading in combination with a texture map

// Uniform variables, passed in from host program via suitable 
// variants of glUniform*
uniform mat4 projection;
uniform mat4 modelview;

// Input vertex attributes; passed in from host program to shader
// via vertex buffer objects
in vec4 position;
in vec2 texcoord;

// Output variables for fragment shader
out vec2 frag_texcoord;

void main()
{	
	frag_texcoord = texcoord;
	
	// Transform position, including projection matrix
	// Note: gl_Position is a default output variable containing
	// the transformed vertex position
	gl_Position = projection * modelview * position;
}
