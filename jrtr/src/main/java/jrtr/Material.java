package jrtr;

import javax.vecmath.*;

import jrtr.swrenderer.SWTexture;

/**
 * Stores the properties of a material.
 */
public class Material {

	// Material properties
	public Texture diffuseMap, normalMap, specularMap, ambientMap, alphaMap;
	public Vector3f diffuse;
	public Vector3f specular;
	public Vector3f ambient;
	public float shininess;
	public Shader shader;
	public SWTexture swTexture;
	
	public Material()
	{
		diffuse = new Vector3f(1.f, 1.f, 1.f);
		specular = new Vector3f(1.f, 1.f, 1.f);
		ambient = new Vector3f(1.f, 1.f, 1.f);
		shininess = 1.f;
		diffuseMap = null;
		normalMap = null;
		specularMap = null;
		ambientMap = null;
		alphaMap = null;
		shader = null;
		swTexture=null;
	}
}
