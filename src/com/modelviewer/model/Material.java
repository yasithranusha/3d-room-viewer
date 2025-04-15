package com.modelviewer.model;

public class Material {
    private String name;
    private float[] ambient = { 0.2f, 0.2f, 0.2f, 1.0f };
    private float[] diffuse = { 0.8f, 0.8f, 0.8f, 1.0f };
    private float[] specular = { 1.0f, 1.0f, 1.0f, 1.0f };
    private float shininess = 0.0f;

    public Material(String name) {
        this.name = name;
    }

    // Getters
    public String getName() {
        return name;
    }

    public float[] getAmbient() {
        return ambient;
    }

    public float[] getDiffuse() {
        return diffuse;
    }

    public float[] getSpecular() {
        return specular;
    }

    public float getShininess() {
        return shininess;
    }

    // Setters
    public void setName(String name) {
        this.name = name;
    }

    public void setAmbient(float[] ambient) {
        this.ambient = ambient;
    }

    public void setDiffuse(float[] diffuse) {
        this.diffuse = diffuse;
    }

    public void setSpecular(float[] specular) {
        this.specular = specular;
    }

    public void setShininess(float shininess) {
        this.shininess = shininess;
    }

    // Utility methods
    public void setAmbient(float r, float g, float b, float a) {
        ambient = new float[] { r, g, b, a };
    }

    public void setDiffuse(float r, float g, float b, float a) {
        diffuse = new float[] { r, g, b, a };
    }

    public void setSpecular(float r, float g, float b, float a) {
        specular = new float[] { r, g, b, a };
    }
}