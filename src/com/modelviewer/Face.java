package com.modelviewer;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a polygon face in a 3D model
 */
public class Face {
    // Lists of indices (1-based as per OBJ format)
    public List<Integer> vertexIndices = new ArrayList<>();
    public List<Integer> texCoordIndices = new ArrayList<>();
    public List<Integer> normalIndices = new ArrayList<>();
    
    // Material name for this face
    public String materialName = null;
    
    /**
     * Create an empty face
     */
    public Face() {
    }
    
    /**
     * Add a vertex to the face (with optional texture and normal)
     */
    public void addVertex(int vertexIndex, int texCoordIndex, int normalIndex) {
        vertexIndices.add(vertexIndex);
        texCoordIndices.add(texCoordIndex);
        normalIndices.add(normalIndex);
    }
    
    /**
     * Create a duplicate of this face
     */
    public Face duplicate() {
        Face copy = new Face();
        
        // Copy indices
        copy.vertexIndices.addAll(this.vertexIndices);
        copy.texCoordIndices.addAll(this.texCoordIndices);
        copy.normalIndices.addAll(this.normalIndices);
        
        // Copy material
        copy.materialName = this.materialName;
        
        return copy;
    }
} 