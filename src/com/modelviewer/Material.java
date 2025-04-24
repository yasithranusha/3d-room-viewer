package com.modelviewer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import java.awt.Color;

/**
 * Represents a material with color and lighting properties
 */
public class Material {
    public String name;
    
    // Color properties
    public float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
    public float[] diffuse = {0.8f, 0.8f, 0.8f, 1.0f};
    public float[] specular = {0.0f, 0.0f, 0.0f, 1.0f};
    public float shininess = 0.0f;
    
    // Texture properties
    public String ambientTexture = null;
    public String diffuseTexture = null;
    public int textureId = -1;
    
    /**
     * Create a new material with the specified name
     */
    public Material(String name) {
        this.name = name;
    }
    
    /**
     * Set the ambient color
     */
    public void setAmbientColor(float r, float g, float b, float a) {
        ambient[0] = r;
        ambient[1] = g;
        ambient[2] = b;
        ambient[3] = a;
    }
    
    /**
     * Set the diffuse color
     */
    public void setDiffuseColor(float r, float g, float b, float a) {
        diffuse[0] = r;
        diffuse[1] = g;
        diffuse[2] = b;
        diffuse[3] = a;
    }
    
    /**
     * Set the specular color
     */
    public void setSpecularColor(float r, float g, float b, float a) {
        specular[0] = r;
        specular[1] = g;
        specular[2] = b;
        specular[3] = a;
    }
    
    /**
     * Apply this material to the current OpenGL context
     */
    public void apply(GL2 gl) {
        // Apply material properties
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
        
        // Apply texture if available
        if (textureId > 0) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        } else {
            gl.glDisable(GL.GL_TEXTURE_2D);
        }
    }
    
    /**
     * Create a duplicate of this material
     */
    public Material duplicate() {
        Material copy = new Material(this.name + " (copy)");
        
        // Copy properties
        System.arraycopy(this.ambient, 0, copy.ambient, 0, 4);
        System.arraycopy(this.diffuse, 0, copy.diffuse, 0, 4);
        System.arraycopy(this.specular, 0, copy.specular, 0, 4);
        copy.shininess = this.shininess;
        
        // Copy texture references
        copy.ambientTexture = this.ambientTexture;
        copy.diffuseTexture = this.diffuseTexture;
        copy.textureId = this.textureId;
        
        return copy;
    }
} 