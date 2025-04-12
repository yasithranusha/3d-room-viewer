package com.modelviewer.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Model3D {
    private String name;
    private List<Float> vertices = new ArrayList<>();
    private List<Float> normals = new ArrayList<>();
    private List<Float> textureCoords = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();
    private Map<String, Material> materials = new HashMap<>();

    // Transform properties
    private float x, y, z; // Position
    private float rotY; // Y-axis rotation (in degrees)
    private float scale = 1.0f; // Scale factor

    // Appearance properties
    private boolean useCustomColor = false;
    private Color customColor = new Color(180, 180, 180);

    // Bounding box
    private float minX, maxX, minY, maxY, minZ, maxZ;

    public Model3D(String name) {
        this.name = name;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Float> getVertices() {
        return vertices;
    }

    public List<Float> getNormals() {
        return normals;
    }

    public List<Float> getTextureCoords() {
        return textureCoords;
    }

    public List<Face> getFaces() {
        return faces;
    }

    public Map<String, Material> getMaterials() {
        return materials;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public float getZ() {
        return z;
    }

    public void setZ(float z) {
        this.z = z;
    }

    public float getRotY() {
        return rotY;
    }

    public void setRotY(float rotY) {
        this.rotY = rotY;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public boolean isUseCustomColor() {
        return useCustomColor;
    }

    public void setUseCustomColor(boolean useCustomColor) {
        this.useCustomColor = useCustomColor;
    }

    public Color getCustomColor() {
        return customColor;
    }

    public void setCustomColor(Color customColor) {
        this.customColor = customColor;
    }

    // Bounding box methods
    public void updateBounds(float x, float y, float z) {
        minX = Math.min(minX, x);
        maxX = Math.max(maxX, x);
        minY = Math.min(minY, y);
        maxY = Math.max(maxY, y);
        minZ = Math.min(minZ, z);
        maxZ = Math.max(maxZ, z);
    }

    public void resetBounds() {
        minX = Float.MAX_VALUE;
        maxX = Float.MIN_VALUE;
        minY = Float.MAX_VALUE;
        maxY = Float.MIN_VALUE;
        minZ = Float.MAX_VALUE;
        maxZ = Float.MIN_VALUE;
    }

    public float[] getBounds() {
        return new float[] { minX, maxX, minY, maxY, minZ, maxZ };
    }

    @Override
    public String toString() {
        return name + " [vertices: " + vertices.size() / 3 + ", faces: " + faces.size() + "]";
    }
}