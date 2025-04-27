package com.modelviewer;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility class for generating test 3D objects with procedural geometry
 */
public class TestObjects {
    
    /**
     * Main method to run the test object generator
     */
    public static void main(String[] args) {
        String outputDir = "advanced_samples";
        
        if (args.length > 0) {
            outputDir = args[0];
        }
        
        System.out.println("Generating test objects in directory: " + outputDir);
        generateTestObjects(outputDir);
    }
    
    /**
     * Generates a smooth sphere OBJ file
     * 
     * @param filename The output filename
     * @param radius The sphere radius
     * @param slices Number of horizontal slices
     * @param stacks Number of vertical stacks
     */
    public static void generateSphere(String filename, float radius, int slices, int stacks) {
        try {
            File file = new File(filename);
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            // Write header
            writer.println("# Procedurally generated sphere");
            writer.println("# Radius: " + radius);
            writer.println("# Slices: " + slices);
            writer.println("# Stacks: " + stacks);
            writer.println();
            
            // Generate MTL filename
            String mtlFilename = filename.substring(0, filename.lastIndexOf('.')) + ".mtl";
            writer.println("mtllib " + new File(mtlFilename).getName());
            writer.println();
            
            // Generate vertices
            writer.println("# Vertices");
            
            // Add top vertex
            writer.println("v 0.0 " + radius + " 0.0");
            
            // Add middle vertices
            for (int i = 1; i < stacks; i++) {
                double phi = Math.PI * i / stacks;
                double y = radius * Math.cos(phi);
                double r = radius * Math.sin(phi);
                
                for (int j = 0; j < slices; j++) {
                    double theta = 2.0 * Math.PI * j / slices;
                    double x = r * Math.cos(theta);
                    double z = r * Math.sin(theta);
                    
                    writer.printf("v %f %f %f\n", x, y, z);
                }
            }
            
            // Add bottom vertex
            writer.println("v 0.0 " + (-radius) + " 0.0");
            writer.println();
            
            // Generate normals
            writer.println("# Normals");
            
            // Add top normal
            writer.println("vn 0.0 1.0 0.0");
            
            // Add middle normals (same as vertices but normalized)
            for (int i = 1; i < stacks; i++) {
                double phi = Math.PI * i / stacks;
                double y = Math.cos(phi);
                double r = Math.sin(phi);
                
                for (int j = 0; j < slices; j++) {
                    double theta = 2.0 * Math.PI * j / slices;
                    double x = r * Math.cos(theta);
                    double z = r * Math.sin(theta);
                    
                    writer.printf("vn %f %f %f\n", x, y, z);
                }
            }
            
            // Add bottom normal
            writer.println("vn 0.0 -1.0 0.0");
            writer.println();
            
            // Generate texture coordinates
            writer.println("# Texture coordinates");
            
            // Add top texture coordinate
            writer.println("vt 0.5 1.0");
            
            // Add middle texture coordinates
            for (int i = 1; i < stacks; i++) {
                double v = 1.0 - (double)i / stacks;
                
                for (int j = 0; j < slices; j++) {
                    double u = (double)j / slices;
                    writer.printf("vt %f %f\n", u, v);
                }
            }
            
            // Add bottom texture coordinate
            writer.println("vt 0.5 0.0");
            writer.println();
            
            // Generate faces
            writer.println("# Faces");
            
            // Use material
            writer.println("usemtl smooth");
            
            // Top faces
            int middleOffset = 2;
            for (int j = 0; j < slices; j++) {
                int j2 = (j + 1) % slices;
                writer.printf("f 1//1 %d//%d %d//%d\n", 
                        j + middleOffset, j + middleOffset, 
                        j2 + middleOffset, j2 + middleOffset);
            }
            
            // Middle faces
            for (int i = 0; i < stacks - 2; i++) {
                int i1 = i * slices + middleOffset;
                int i2 = (i + 1) * slices + middleOffset;
                
                for (int j = 0; j < slices; j++) {
                    int j2 = (j + 1) % slices;
                    
                    writer.printf("f %d//%d %d//%d %d//%d %d//%d\n",
                            i1 + j, i1 + j,
                            i1 + j2, i1 + j2,
                            i2 + j2, i2 + j2,
                            i2 + j, i2 + j);
                }
            }
            
            // Bottom faces
            int lastVertex = 1 + slices * (stacks - 1) + 1;
            int bottomOffset = 1 + slices * (stacks - 2) + middleOffset;
            for (int j = 0; j < slices; j++) {
                int j2 = (j + 1) % slices;
                writer.printf("f %d//%d %d//%d %d//%d\n",
                        bottomOffset + j, bottomOffset + j,
                        lastVertex, lastVertex,
                        bottomOffset + j2, bottomOffset + j2);
            }
            
            writer.close();
            System.out.println("Generated sphere OBJ: " + file.getAbsolutePath());
            
            // Generate MTL file
            generateMaterialFile(mtlFilename);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a torus OBJ file
     * 
     * @param filename The output filename
     * @param majorRadius The major radius (from center to tube center)
     * @param minorRadius The minor radius (tube radius)
     * @param majorSegments Number of segments around the major radius
     * @param minorSegments Number of segments around the minor radius
     */
    public static void generateTorus(String filename, float majorRadius, float minorRadius, 
                                     int majorSegments, int minorSegments) {
        try {
            File file = new File(filename);
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            // Write header
            writer.println("# Procedurally generated torus");
            writer.println("# Major Radius: " + majorRadius);
            writer.println("# Minor Radius: " + minorRadius);
            writer.println("# Major Segments: " + majorSegments);
            writer.println("# Minor Segments: " + minorSegments);
            writer.println();
            
            // Generate MTL filename
            String mtlFilename = filename.substring(0, filename.lastIndexOf('.')) + ".mtl";
            writer.println("mtllib " + new File(mtlFilename).getName());
            writer.println();
            
            // Generate vertices
            writer.println("# Vertices");
            
            for (int i = 0; i < majorSegments; i++) {
                double theta = 2.0 * Math.PI * i / majorSegments;
                double cosTheta = Math.cos(theta);
                double sinTheta = Math.sin(theta);
                
                for (int j = 0; j < minorSegments; j++) {
                    double phi = 2.0 * Math.PI * j / minorSegments;
                    double cosPhi = Math.cos(phi);
                    double sinPhi = Math.sin(phi);
                    
                    double x = (majorRadius + minorRadius * cosPhi) * cosTheta;
                    double y = minorRadius * sinPhi;
                    double z = (majorRadius + minorRadius * cosPhi) * sinTheta;
                    
                    writer.printf("v %f %f %f\n", x, y, z);
                }
            }
            writer.println();
            
            // Generate normals
            writer.println("# Normals");
            
            for (int i = 0; i < majorSegments; i++) {
                double theta = 2.0 * Math.PI * i / majorSegments;
                double cosTheta = Math.cos(theta);
                double sinTheta = Math.sin(theta);
                
                for (int j = 0; j < minorSegments; j++) {
                    double phi = 2.0 * Math.PI * j / minorSegments;
                    double cosPhi = Math.cos(phi);
                    double sinPhi = Math.sin(phi);
                    
                    double nx = cosTheta * cosPhi;
                    double ny = sinPhi;
                    double nz = sinTheta * cosPhi;
                    
                    writer.printf("vn %f %f %f\n", nx, ny, nz);
                }
            }
            writer.println();
            
            // Generate texture coordinates
            writer.println("# Texture coordinates");
            
            for (int i = 0; i < majorSegments; i++) {
                double u = (double)i / majorSegments;
                
                for (int j = 0; j < minorSegments; j++) {
                    double v = (double)j / minorSegments;
                    writer.printf("vt %f %f\n", u, v);
                }
            }
            writer.println();
            
            // Generate faces
            writer.println("# Faces");
            writer.println("usemtl smooth");
            
            for (int i = 0; i < majorSegments; i++) {
                int i2 = (i + 1) % majorSegments;
                
                for (int j = 0; j < minorSegments; j++) {
                    int j2 = (j + 1) % minorSegments;
                    
                    int v1 = i * minorSegments + j + 1;
                    int v2 = i * minorSegments + j2 + 1;
                    int v3 = i2 * minorSegments + j2 + 1;
                    int v4 = i2 * minorSegments + j + 1;
                    
                    writer.printf("f %d//%d %d//%d %d//%d %d//%d\n",
                            v1, v1, v2, v2, v3, v3, v4, v4);
                }
            }
            
            writer.close();
            System.out.println("Generated torus OBJ: " + file.getAbsolutePath());
            
            // Generate MTL file
            generateMaterialFile(mtlFilename);
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Generates a simple material file with basic materials
     */
    private static void generateMaterialFile(String filename) {
        try {
            File file = new File(filename);
            PrintWriter writer = new PrintWriter(new FileWriter(file));
            
            writer.println("# Generated materials");
            writer.println();
            
            writer.println("newmtl smooth");
            writer.println("Ka 0.2 0.2 0.2");
            writer.println("Kd 0.8 0.8 0.8");
            writer.println("Ks 1.0 1.0 1.0");
            writer.println("Ns 100.0");
            
            writer.close();
            System.out.println("Generated material file: " + file.getAbsolutePath());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * Helper method to generate and save test objects
     */
    public static void generateTestObjects(String directory) {
        File dir = new File(directory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        // Generate a high-resolution sphere
        generateSphere(new File(dir, "sphere.obj").getAbsolutePath(), 1.0f, 32, 16);
        
        // Generate a torus
        generateTorus(new File(dir, "torus.obj").getAbsolutePath(), 1.0f, 0.3f, 48, 24);
    }
} 