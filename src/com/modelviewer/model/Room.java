package com.modelviewer.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class Room {
    private float width = 5.0f;
    private float height = 3.0f;
    private float length = 5.0f;

    private Color wallColor = new Color(220, 220, 220);
    private Color floorColor = new Color(180, 140, 100);
    private Color ceilingColor = new Color(240, 240, 240);

    private float wallTransparency = 1.0f;
    private float floorTransparency = 1.0f;
    private float ceilingTransparency = 1.0f;

    private List<Model3D> models = new ArrayList<>();

    // Getters
    public float getWidth() {
        return width;
    }

    public float getHeight() {
        return height;
    }

    public float getLength() {
        return length;
    }

    public Color getWallColor() {
        return wallColor;
    }

    public Color getFloorColor() {
        return floorColor;
    }

    public Color getCeilingColor() {
        return ceilingColor;
    }

    public float getWallTransparency() {
        return wallTransparency;
    }

    public float getFloorTransparency() {
        return floorTransparency;
    }

    public float getCeilingTransparency() {
        return ceilingTransparency;
    }

    public List<Model3D> getModels() {
        return models;
    }

    // Setters
    public void setWidth(float width) {
        this.width = width;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public void setLength(float length) {
        this.length = length;
    }

    public void setWallColor(Color wallColor) {
        this.wallColor = wallColor;
    }

    public void setFloorColor(Color floorColor) {
        this.floorColor = floorColor;
    }

    public void setCeilingColor(Color ceilingColor) {
        this.ceilingColor = ceilingColor;
    }

    public void setWallTransparency(float transparency) {
        this.wallTransparency = transparency;
    }

    public void setFloorTransparency(float transparency) {
        this.floorTransparency = transparency;
    }

    public void setCeilingTransparency(float transparency) {
        this.ceilingTransparency = transparency;
    }

    // Model management
    public void addModel(Model3D model) {
        models.add(model);
    }

    public void removeModel(Model3D model) {
        models.remove(model);
    }

    public void clearModels() {
        models.clear();
    }

    // Utility methods
    public float[] getDimensions() {
        return new float[] { width, height, length };
    }

    public void setDimensions(float width, float height, float length) {
        this.width = width;
        this.height = height;
        this.length = length;
    }
}