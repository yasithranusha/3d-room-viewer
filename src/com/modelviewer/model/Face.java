package com.modelviewer.model;

public class Face {
    private int[] vertexIndices;
    private int[] normalIndices;
    private int[] texCoordIndices;
    private String materialName;

    public Face(int[] vertexIndices, int[] texCoordIndices, int[] normalIndices, String materialName) {
        this.vertexIndices = vertexIndices;
        this.texCoordIndices = texCoordIndices;
        this.normalIndices = normalIndices;
        this.materialName = materialName;
    }

    // Getters
    public int[] getVertexIndices() {
        return vertexIndices;
    }

    public int[] getNormalIndices() {
        return normalIndices;
    }

    public int[] getTexCoordIndices() {
        return texCoordIndices;
    }

    public String getMaterialName() {
        return materialName;
    }

    // Setters
    public void setVertexIndices(int[] vertexIndices) {
        this.vertexIndices = vertexIndices;
    }

    public void setNormalIndices(int[] normalIndices) {
        this.normalIndices = normalIndices;
    }

    public void setTexCoordIndices(int[] texCoordIndices) {
        this.texCoordIndices = texCoordIndices;
    }

    public void setMaterialName(String materialName) {
        this.materialName = materialName;
    }
}