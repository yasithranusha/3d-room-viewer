package com.modelviewer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Handles loading of OBJ and MTL files
 */
public class OBJLoader {
    
    /**
     * Load a 3D model from an OBJ file
     */
    public static Model3D loadOBJModel(String objFilePath) throws IOException {
        return loadOBJModel(objFilePath, null);
    }
    
    /**
     * Load a 3D model from an OBJ file with optional MTL file
     */
    public static Model3D loadOBJModel(String objFilePath, String mtlFilePath) throws IOException {
        File objFile = new File(objFilePath);
        String modelName = objFile.getName();
        if (modelName.toLowerCase().endsWith(".obj")) {
            modelName = modelName.substring(0, modelName.length() - 4);
        }
        
        Model3D model = new Model3D(modelName);
        
        // Load materials if MTL path is provided
        if (mtlFilePath != null && !mtlFilePath.isEmpty()) {
            loadMTLFile(model, mtlFilePath);
        }
        
        // Open the OBJ file
        BufferedReader reader = new BufferedReader(new FileReader(objFile));
        String line;
        
        // Parse each line of the file
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            if (line.isEmpty() || line.startsWith("#")) {
                // Skip empty lines and comments
                continue;
            }
            
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                String type = tokenizer.nextToken();
                
                switch (type) {
                    case "v":
                        // Vertex
                        parseVertex(model, tokenizer);
                        break;
                    case "vn":
                        // Normal
                        parseNormal(model, tokenizer);
                        break;
                    case "vt":
                        // Texture coordinate
                        parseTexCoord(model, tokenizer);
                        break;
                    case "f":
                        // Face
                        parseFace(model, tokenizer);
                        break;
                    case "mtllib":
                        // Material library
                        if (mtlFilePath == null || mtlFilePath.isEmpty()) {
                            // Only load MTL if not already specified
                            String mtlFile = tokenizer.nextToken();
                            File objDir = objFile.getParentFile();
                            String mtlPath = objDir != null ? 
                                new File(objDir, mtlFile).getAbsolutePath() : mtlFile;
                            loadMTLFile(model, mtlPath);
                        }
                        break;
                    case "usemtl":
                        // Use material
                        if (tokenizer.hasMoreTokens()) {
                            model.setActiveMaterial(tokenizer.nextToken());
                        }
                        break;
                    // Ignore other types
                }
            }
        }
        
        reader.close();
        
        // If no vertices, create a simple cube as a placeholder
        if (model.vertices.isEmpty()) {
            createDefaultCube(model);
        }
        
        return model;
    }
    
    /**
     * Load materials from a MTL file
     */
    private static void loadMTLFile(Model3D model, String mtlFilePath) throws IOException {
        File mtlFile = new File(mtlFilePath);
        if (!mtlFile.exists()) {
            System.out.println("MTL file not found: " + mtlFilePath);
            return;
        }
        
        BufferedReader reader = new BufferedReader(new FileReader(mtlFile));
        String line;
        Material currentMaterial = null;
        
        // Parse each line of the file
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            
            if (line.isEmpty() || line.startsWith("#")) {
                // Skip empty lines and comments
                continue;
            }
            
            StringTokenizer tokenizer = new StringTokenizer(line);
            if (tokenizer.hasMoreTokens()) {
                String type = tokenizer.nextToken();
                
                switch (type) {
                    case "newmtl":
                        // New material
                        if (tokenizer.hasMoreTokens()) {
                            String materialName = tokenizer.nextToken();
                            currentMaterial = new Material(materialName);
                            model.addMaterial(currentMaterial);
                        }
                        break;
                    case "Ka":
                        // Ambient color
                        if (currentMaterial != null && tokenizer.countTokens() >= 3) {
                            float r = Float.parseFloat(tokenizer.nextToken());
                            float g = Float.parseFloat(tokenizer.nextToken());
                            float b = Float.parseFloat(tokenizer.nextToken());
                            currentMaterial.setAmbientColor(r, g, b, 1.0f);
                        }
                        break;
                    case "Kd":
                        // Diffuse color
                        if (currentMaterial != null && tokenizer.countTokens() >= 3) {
                            float r = Float.parseFloat(tokenizer.nextToken());
                            float g = Float.parseFloat(tokenizer.nextToken());
                            float b = Float.parseFloat(tokenizer.nextToken());
                            currentMaterial.setDiffuseColor(r, g, b, 1.0f);
                        }
                        break;
                    case "Ks":
                        // Specular color
                        if (currentMaterial != null && tokenizer.countTokens() >= 3) {
                            float r = Float.parseFloat(tokenizer.nextToken());
                            float g = Float.parseFloat(tokenizer.nextToken());
                            float b = Float.parseFloat(tokenizer.nextToken());
                            currentMaterial.setSpecularColor(r, g, b, 1.0f);
                        }
                        break;
                    case "Ns":
                        // Shininess
                        if (currentMaterial != null && tokenizer.hasMoreTokens()) {
                            float shininess = Float.parseFloat(tokenizer.nextToken());
                            // Scale to OpenGL range [0, 128]
                            currentMaterial.shininess = Math.min(shininess, 128.0f);
                        }
                        break;
                    case "map_Kd":
                        // Diffuse texture map
                        if (currentMaterial != null && tokenizer.hasMoreTokens()) {
                            currentMaterial.diffuseTexture = tokenizer.nextToken();
                        }
                        break;
                    case "map_Ka":
                        // Ambient texture map
                        if (currentMaterial != null && tokenizer.hasMoreTokens()) {
                            currentMaterial.ambientTexture = tokenizer.nextToken();
                        }
                        break;
                    // Ignore other types
                }
            }
        }
        
        reader.close();
        
        System.out.println("Loaded MTL: " + model.materials.size() + " materials");
    }
    
    /**
     * Parse a vertex line
     */
    private static void parseVertex(Model3D model, StringTokenizer tokenizer) {
        if (tokenizer.countTokens() >= 3) {
            float x = Float.parseFloat(tokenizer.nextToken());
            float y = Float.parseFloat(tokenizer.nextToken());
            float z = Float.parseFloat(tokenizer.nextToken());
            model.addVertex(x, y, z);
        }
    }
    
    /**
     * Parse a normal line
     */
    private static void parseNormal(Model3D model, StringTokenizer tokenizer) {
        if (tokenizer.countTokens() >= 3) {
            float x = Float.parseFloat(tokenizer.nextToken());
            float y = Float.parseFloat(tokenizer.nextToken());
            float z = Float.parseFloat(tokenizer.nextToken());
            model.addNormal(x, y, z);
        }
    }
    
    /**
     * Parse a texture coordinate line
     */
    private static void parseTexCoord(Model3D model, StringTokenizer tokenizer) {
        if (tokenizer.countTokens() >= 2) {
            float u = Float.parseFloat(tokenizer.nextToken());
            float v = Float.parseFloat(tokenizer.nextToken());
            model.addTexCoord(u, v);
        }
    }
    
    /**
     * Parse a face line
     */
    private static void parseFace(Model3D model, StringTokenizer tokenizer) {
        Face face = new Face();
        
        // Process each vertex of the face
        while (tokenizer.hasMoreTokens()) {
            String vertexData = tokenizer.nextToken();
            String[] indices = vertexData.split("/");
            
            int vertexIndex = 0;
            int texCoordIndex = 0;
            int normalIndex = 0;
            
            if (indices.length > 0 && !indices[0].isEmpty()) {
                vertexIndex = Integer.parseInt(indices[0]);
            }
            
            if (indices.length > 1 && !indices[1].isEmpty()) {
                texCoordIndex = Integer.parseInt(indices[1]);
            }
            
            if (indices.length > 2 && !indices[2].isEmpty()) {
                normalIndex = Integer.parseInt(indices[2]);
            }
            
            face.addVertex(vertexIndex, texCoordIndex, normalIndex);
        }
        
        model.addFace(face);
    }
    
    /**
     * Create a default cube model when no vertices are found
     */
    private static void createDefaultCube(Model3D model) {
        System.out.println("Creating default cube model");
        
        // Define the 8 vertices of a cube
        model.addVertex(-0.5f, -0.5f, -0.5f);  // 0
        model.addVertex(0.5f, -0.5f, -0.5f);   // 1
        model.addVertex(0.5f, 0.5f, -0.5f);    // 2
        model.addVertex(-0.5f, 0.5f, -0.5f);   // 3
        model.addVertex(-0.5f, -0.5f, 0.5f);   // 4
        model.addVertex(0.5f, -0.5f, 0.5f);    // 5
        model.addVertex(0.5f, 0.5f, 0.5f);     // 6
        model.addVertex(-0.5f, 0.5f, 0.5f);    // 7
        
        // Define normals for each face
        model.addNormal(0.0f, 0.0f, -1.0f);    // Front
        model.addNormal(0.0f, 0.0f, 1.0f);     // Back
        model.addNormal(1.0f, 0.0f, 0.0f);     // Right
        model.addNormal(-1.0f, 0.0f, 0.0f);    // Left
        model.addNormal(0.0f, 1.0f, 0.0f);     // Top
        model.addNormal(0.0f, -1.0f, 0.0f);    // Bottom
        
        // Create default material
        Material defaultMaterial = new Material("default");
        defaultMaterial.setDiffuseColor(0.8f, 0.8f, 0.8f, 1.0f);
        model.addMaterial(defaultMaterial);
        model.setActiveMaterial("default");
        
        // Create the 6 faces of the cube (each face consists of 2 triangles)
        
        // Front face (z = -0.5)
        Face front1 = new Face();
        front1.addVertex(1, 0, 1);
        front1.addVertex(2, 0, 1);
        front1.addVertex(3, 0, 1);
        model.addFace(front1);
        
        Face front2 = new Face();
        front2.addVertex(1, 0, 1);
        front2.addVertex(3, 0, 1);
        front2.addVertex(4, 0, 1);
        model.addFace(front2);
        
        // Back face (z = 0.5)
        Face back1 = new Face();
        back1.addVertex(5, 0, 2);
        back1.addVertex(8, 0, 2);
        back1.addVertex(7, 0, 2);
        model.addFace(back1);
        
        Face back2 = new Face();
        back2.addVertex(5, 0, 2);
        back2.addVertex(7, 0, 2);
        back2.addVertex(6, 0, 2);
        model.addFace(back2);
        
        // Right face (x = 0.5)
        Face right1 = new Face();
        right1.addVertex(2, 0, 3);
        right1.addVertex(6, 0, 3);
        right1.addVertex(7, 0, 3);
        model.addFace(right1);
        
        Face right2 = new Face();
        right2.addVertex(2, 0, 3);
        right2.addVertex(7, 0, 3);
        right2.addVertex(3, 0, 3);
        model.addFace(right2);
        
        // Left face (x = -0.5)
        Face left1 = new Face();
        left1.addVertex(1, 0, 4);
        left1.addVertex(5, 0, 4);
        left1.addVertex(8, 0, 4);
        model.addFace(left1);
        
        Face left2 = new Face();
        left2.addVertex(1, 0, 4);
        left2.addVertex(8, 0, 4);
        left2.addVertex(4, 0, 4);
        model.addFace(left2);
        
        // Top face (y = 0.5)
        Face top1 = new Face();
        top1.addVertex(3, 0, 5);
        top1.addVertex(7, 0, 5);
        top1.addVertex(8, 0, 5);
        model.addFace(top1);
        
        Face top2 = new Face();
        top2.addVertex(3, 0, 5);
        top2.addVertex(8, 0, 5);
        top2.addVertex(4, 0, 5);
        model.addFace(top2);
        
        // Bottom face (y = -0.5)
        Face bottom1 = new Face();
        bottom1.addVertex(1, 0, 6);
        bottom1.addVertex(5, 0, 6);
        bottom1.addVertex(6, 0, 6);
        model.addFace(bottom1);
        
        Face bottom2 = new Face();
        bottom2.addVertex(1, 0, 6);
        bottom2.addVertex(6, 0, 6);
        bottom2.addVertex(2, 0, 6);
        model.addFace(bottom2);
    }
} 