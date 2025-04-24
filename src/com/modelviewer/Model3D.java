package com.modelviewer;

import com.jogamp.opengl.GL2;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a 3D model with vertices, faces, and materials
 */
public class Model3D {
    // Model data
    public String name;
    public List<Float> vertices = new ArrayList<>();
    public List<Float> normals = new ArrayList<>();
    public List<Float> texCoords = new ArrayList<>();
    public List<Face> faces = new ArrayList<>();
    public Map<String, Material> materials = new HashMap<>();
    public String activeMaterial = null;
    
    // Transformation properties
    public float x = 0.0f;
    public float y = 0.0f;
    public float z = 0.0f;
    public float rotX = 0.0f;
    public float rotY = 0.0f;
    public float rotZ = 0.0f;
    public float scale = 1.0f;
    
    // Bounding box
    public float minX, minY, minZ;
    public float maxX, maxY, maxZ;
    
    /**
     * Create a new empty 3D model
     */
    public Model3D(String name) {
        this.name = name;
        resetBounds();
    }
    
    /**
     * Reset the bounding box
     */
    public void resetBounds() {
        minX = minY = minZ = Float.MAX_VALUE;
        maxX = maxY = maxZ = -Float.MAX_VALUE;
    }
    
    /**
     * Add a vertex to the model
     */
    public void addVertex(float x, float y, float z) {
        vertices.add(x);
        vertices.add(y);
        vertices.add(z);
        
        // Update bounding box
        minX = Math.min(minX, x);
        minY = Math.min(minY, y);
        minZ = Math.min(minZ, z);
        maxX = Math.max(maxX, x);
        maxY = Math.max(maxY, y);
        maxZ = Math.max(maxZ, z);
    }
    
    /**
     * Add a normal vector
     */
    public void addNormal(float x, float y, float z) {
        normals.add(x);
        normals.add(y);
        normals.add(z);
    }
    
    /**
     * Add a texture coordinate
     */
    public void addTexCoord(float u, float v) {
        texCoords.add(u);
        texCoords.add(v);
    }
    
    /**
     * Add a face to the model
     */
    public void addFace(Face face) {
        face.materialName = activeMaterial;
        faces.add(face);
    }
    
    /**
     * Add a material to the model
     */
    public void addMaterial(Material material) {
        materials.put(material.name, material);
    }
    
    /**
     * Set the active material
     */
    public void setActiveMaterial(String materialName) {
        activeMaterial = materialName;
    }
    
    /**
     * Get the center point of the model
     */
    public float[] getCenter() {
        float centerX = (minX + maxX) / 2.0f;
        float centerY = (minY + maxY) / 2.0f;
        float centerZ = (minZ + maxZ) / 2.0f;
        return new float[]{centerX, centerY, centerZ};
    }
    
    /**
     * Get the size of the model (longest dimension)
     */
    public float getSize() {
        float sizeX = Math.abs(maxX - minX);
        float sizeY = Math.abs(maxY - minY);
        float sizeZ = Math.abs(maxZ - minZ);
        return Math.max(Math.max(sizeX, sizeY), sizeZ);
    }
    
    /**
     * Render the model using OpenGL
     */
    public void render(GL2 gl, boolean wireframe, boolean colorOverride) {
        gl.glPushMatrix();
        
        // Apply transformations
        gl.glTranslatef(x, y, z);
        gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(rotZ, 0.0f, 0.0f, 1.0f);
        gl.glScalef(scale, scale, scale);
        
        // Set wireframe or solid mode
        if (wireframe) {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        } else {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }
        
        // Render each face
        String currentMaterial = null;
        
        for (Face face : faces) {
            // Apply material if it has changed and not overriding color
            if (!colorOverride && face.materialName != null && 
                !face.materialName.equals(currentMaterial) && 
                materials.containsKey(face.materialName)) {
                
                currentMaterial = face.materialName;
                Material mat = materials.get(currentMaterial);
                mat.apply(gl);
            }
            
            // Draw the face
            if (face.vertexIndices.size() == 3) {
                gl.glBegin(GL2.GL_TRIANGLES);
            } else if (face.vertexIndices.size() == 4) {
                gl.glBegin(GL2.GL_QUADS);
            } else {
                gl.glBegin(GL2.GL_POLYGON);
            }
            
            for (int i = 0; i < face.vertexIndices.size(); i++) {
                // Apply normal if available
                if (i < face.normalIndices.size() && face.normalIndices.get(i) > 0) {
                    int normalIdx = (face.normalIndices.get(i) - 1) * 3;
                    if (normalIdx >= 0 && normalIdx < normals.size()) {
                        gl.glNormal3f(
                            normals.get(normalIdx),
                            normals.get(normalIdx + 1),
                            normals.get(normalIdx + 2)
                        );
                    }
                }
                
                // Apply texture coordinate if available
                if (i < face.texCoordIndices.size() && face.texCoordIndices.get(i) > 0) {
                    int texIdx = (face.texCoordIndices.get(i) - 1) * 2;
                    if (texIdx >= 0 && texIdx < texCoords.size()) {
                        gl.glTexCoord2f(
                            texCoords.get(texIdx),
                            texCoords.get(texIdx + 1)
                        );
                    }
                }
                
                // Draw vertex
                int vertexIdx = (face.vertexIndices.get(i) - 1) * 3;
                if (vertexIdx >= 0 && vertexIdx < vertices.size()) {
                    gl.glVertex3f(
                        vertices.get(vertexIdx),
                        vertices.get(vertexIdx + 1),
                        vertices.get(vertexIdx + 2)
                    );
                }
            }
            
            gl.glEnd();
        }
        
        // Reset polygon mode
        gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        
        gl.glPopMatrix();
    }
    
    /**
     * Create a duplicate of this model
     */
    public Model3D duplicate() {
        Model3D copy = new Model3D(this.name + " (copy)");
        
        // Copy geometry data
        copy.vertices.addAll(this.vertices);
        copy.normals.addAll(this.normals);
        copy.texCoords.addAll(this.texCoords);
        
        // Copy materials
        for (Map.Entry<String, Material> entry : this.materials.entrySet()) {
            copy.materials.put(entry.getKey(), entry.getValue().duplicate());
        }
        
        // Copy faces
        for (Face face : this.faces) {
            copy.faces.add(face.duplicate());
        }
        
        // Copy transform
        copy.x = this.x;
        copy.y = this.y;
        copy.z = this.z;
        copy.rotX = this.rotX;
        copy.rotY = this.rotY;
        copy.rotZ = this.rotZ;
        copy.scale = this.scale;
        
        // Copy bounds
        copy.minX = this.minX;
        copy.minY = this.minY;
        copy.minZ = this.minZ;
        copy.maxX = this.maxX;
        copy.maxY = this.maxY;
        copy.maxZ = this.maxZ;
        
        return copy;
    }
} 