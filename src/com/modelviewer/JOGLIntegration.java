package com.modelviewer;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;

import java.awt.Color;
import java.util.List;

/**
 * Helper class for JOGL (OpenGL) integration with the SoftwareRendererForm
 */
public class JOGLIntegration implements GLEventListener {
    
    private Model3D currentModel;
    private List<Model3D> roomModels;
    private Animator animator;
    private GLJPanel glPanel;
    
    // Rendering settings
    private boolean wireframeMode = false;
    private boolean useColorOverride = false;
    private Color overrideColor = Color.RED;
    
    // Room properties
    private boolean showRoom = false;
    private float roomWidth = 5.0f;
    private float roomHeight = 3.0f;
    private float roomLength = 5.0f;
    private Color wallColor = new Color(220, 220, 220);
    private Color floorColor = new Color(180, 140, 100);
    private Color ceilingColor = new Color(240, 240, 240);
    private float wallTransparency = 1.0f;
    private float floorTransparency = 1.0f;
    private float ceilingTransparency = 1.0f;
    
    // Camera settings
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    private float zoom = -5.0f;
    
    // Add smooth camera transitions and better control options
    private float cameraDistance = 5.0f;
    private float cameraAngleX = 30.0f;
    private float cameraAngleY = 45.0f;
    private float targetZoom = -5.0f;
    private float zoomSpeed = 0.1f;
    
    // Add grid for better object placement
    private boolean showGrid = true;
    private float gridSize = 0.5f;
    private boolean enableSnapping = true;
    
    /**
     * Creates a GLJPanel for 3D rendering
     */
    public GLJPanel createGLPanel() {
        // Use GL2 profile for better compatibility
        GLProfile glProfile = GLProfile.get(GLProfile.GL2);
        
        // Configure for software rendering
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setHardwareAccelerated(false);
        glCapabilities.setDoubleBuffered(true);
        
        // Create a GLJPanel - better software rendering support
        glPanel = new GLJPanel(glCapabilities);
        glPanel.addGLEventListener(this);
        
        // Set up animator
        animator = new Animator(glPanel);
        animator.setUpdateFPSFrames(20, null);
        
        return glPanel;
    }
    
    /**
     * Starts the animation
     */
    public void startAnimation() {
        if (animator != null && !animator.isAnimating()) {
            animator.start();
        }
    }
    
    /**
     * Stops the animation
     */
    public void stopAnimation() {
        if (animator != null && animator.isAnimating()) {
            animator.stop();
        }
    }
    
    /**
     * Sets the current model for rendering
     */
    public void setCurrentModel(Model3D model) {
        this.currentModel = model;
        refreshDisplay();
    }
    
    /**
     * Sets the room models collection
     */
    public void setRoomModels(List<Model3D> models) {
        this.roomModels = models;
        refreshDisplay();
    }
    
    /**
     * Refreshes the display
     */
    public void refreshDisplay() {
        if (glPanel != null) {
            glPanel.repaint();
        }
    }
    
    /**
     * Sets wireframe mode
     */
    public void setWireframeMode(boolean wireframe) {
        this.wireframeMode = wireframe;
        refreshDisplay();
    }
    
    /**
     * Sets color override
     */
    public void setColorOverride(boolean override, Color color) {
        this.useColorOverride = override;
        this.overrideColor = color;
        refreshDisplay();
    }
    
    /**
     * Sets room visibility
     */
    public void setShowRoom(boolean show) {
        this.showRoom = show;
        refreshDisplay();
    }
    
    /**
     * Sets room properties
     */
    public void setRoomProperties(boolean show, float width, float height, float length) {
        this.showRoom = show;
        this.roomWidth = width;
        this.roomHeight = height;
        this.roomLength = length;
        refreshDisplay();
    }
    
    /**
     * Sets wall color
     */
    public void setWallColor(Color color) {
        this.wallColor = color;
        refreshDisplay();
    }
    
    /**
     * Sets floor color
     */
    public void setFloorColor(Color color) {
        this.floorColor = color;
        refreshDisplay();
    }
    
    /**
     * Sets ceiling color
     */
    public void setCeilingColor(Color color) {
        this.ceilingColor = color;
        refreshDisplay();
    }
    
    /**
     * Sets room colors
     */
    public void setRoomColors(Color wall, Color floor, Color ceiling) {
        this.wallColor = wall;
        this.floorColor = floor;
        this.ceilingColor = ceiling;
        refreshDisplay();
    }
    
    /**
     * Sets room transparencies
     */
    public void setRoomTransparency(float wall, float floor, float ceiling) {
        this.wallTransparency = wall;
        this.floorTransparency = floor;
        this.ceilingTransparency = ceiling;
        refreshDisplay();
    }
    
    /**
     * Gets the current X rotation
     */
    public float getRotX() {
        return rotX;
    }
    
    /**
     * Gets the current Y rotation
     */
    public float getRotY() {
        return rotY;
    }
    
    /**
     * Gets the current zoom value
     */
    public float getZoom() {
        return zoom;
    }
    
    /**
     * Sets camera rotation
     */
    public void setCameraRotation(float rotX, float rotY) {
        this.rotX = rotX;
        this.rotY = rotY;
        refreshDisplay();
    }
    
    /**
     * Sets camera zoom
     */
    public void setCameraZoom(float zoom) {
        this.zoom = zoom;
        refreshDisplay();
    }
    
    /**
     * Resets the view
     */
    public void resetView() {
        rotX = 0.0f;
        rotY = 0.0f;
        zoom = -5.0f;
        refreshDisplay();
    }
    
    // GLEventListener implementation
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        // Basic OpenGL setup
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        gl.glEnable(GL.GL_DEPTH_TEST);
        
        // Enable transparency
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        // Setup lighting
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);
        
        float[] lightPosition = {1.0f, 1.0f, 1.0f, 0.0f};
        float[] lightAmbient = {0.4f, 0.4f, 0.4f, 1.0f};
        float[] lightDiffuse = {0.8f, 0.8f, 0.8f, 1.0f};
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
        
        // Enable materials
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
        gl.glEnable(GL2.GL_NORMALIZE);
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
        gl.glLoadIdentity();
        
        // Apply camera transformations
        gl.glTranslatef(0, 0, zoom);
        gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        
        // Set drawing mode based on wireframe setting
        if (wireframeMode) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        } else {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }
        
        // Draw the main model if available
        if (currentModel != null && !currentModel.vertices.isEmpty()) {
            renderModel(gl, currentModel);
        }
        
        // Draw the room if enabled
        if (showRoom) {
            renderRoom(gl);
            
            // Draw the models inside the room
            if (roomModels != null && !roomModels.isEmpty()) {
                for (Model3D model : roomModels) {
                    gl.glPushMatrix();
                    gl.glTranslatef(model.x, model.y, model.z);
                    gl.glRotatef(model.rotY, 0.0f, 1.0f, 0.0f);
                    gl.glScalef(model.scale, model.scale, model.scale);
                    renderModel(gl, model);
                    gl.glPopMatrix();
                }
            }
        }
        
        // Reset polygon mode
        gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
    }
    
    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        
        float aspect = (float) width / height;
        gl.glFrustum(-aspect, aspect, -1.0, 1.0, 1.0, 100.0);
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Clean up resources if needed
        stopAnimation();
    }
    
    /**
     * Renders a 3D model using OpenGL
     */
    private void renderModel(GL2 gl, Model3D model) {
        if (model.vertices.isEmpty()) {
            return;
        }
        
        // Apply color override if enabled
        if (useColorOverride) {
            float[] overrideAmbient = {
                overrideColor.getRed() / 255.0f * 0.3f,
                overrideColor.getGreen() / 255.0f * 0.3f,
                overrideColor.getBlue() / 255.0f * 0.3f,
                1.0f
            };
            
            float[] overrideDiffuse = {
                overrideColor.getRed() / 255.0f,
                overrideColor.getGreen() / 255.0f,
                overrideColor.getBlue() / 255.0f,
                1.0f
            };
            
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, overrideAmbient, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, overrideDiffuse, 0);
            gl.glColor3f(
                overrideColor.getRed() / 255.0f,
                overrideColor.getGreen() / 255.0f,
                overrideColor.getBlue() / 255.0f
            );
        }
        
        // Draw each face
        Material lastMaterial = null;
        for (Face face : model.faces) {
            // Apply material if available and not using color override
            if (!useColorOverride && !wireframeMode && face.materialName != null && 
                model.materials.containsKey(face.materialName)) {
                
                Material material = model.materials.get(face.materialName);
                if (material != lastMaterial) {
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, material.ambient, 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, material.diffuse, 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, material.specular, 0);
                    gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, material.shininess);
                    
                    // Set color directly for better visual feedback
                    gl.glColor3f(material.diffuse[0], material.diffuse[1], material.diffuse[2]);
                    lastMaterial = material;
                }
            } else if (wireframeMode) {
                // For wireframe, use white color
                gl.glColor3f(1.0f, 1.0f, 1.0f);
            } else if (!useColorOverride) {
                // Default color if no material or override
                gl.glColor3f(0.8f, 0.8f, 0.8f);
            }
            
            // Draw the face with the correct primitive type
            if (face.vertexIndices.size() == 3) {
                gl.glBegin(GL2.GL_TRIANGLES);
            } else if (face.vertexIndices.size() == 4) {
                gl.glBegin(GL2.GL_QUADS);
            } else {
                gl.glBegin(GL2.GL_POLYGON);
            }
            
            // Render each vertex of the face
            for (int i = 0; i < face.vertexIndices.size(); i++) {
                int vertIndex = face.vertexIndices.get(i);
                
                // Handle normals
                if (face.normalIndices != null && i < face.normalIndices.size() &&
                    face.normalIndices.get(i) >= 0 &&
                    face.normalIndices.get(i) * 3 + 2 < model.normals.size()) {
                    
                    int normIndex = face.normalIndices.get(i);
                    gl.glNormal3f(
                        model.normals.get(normIndex * 3),
                        model.normals.get(normIndex * 3 + 1),
                        model.normals.get(normIndex * 3 + 2)
                    );
                }
                
                // Handle texture coordinates
                if (face.texCoordIndices != null && i < face.texCoordIndices.size() &&
                    face.texCoordIndices.get(i) >= 0 && 
                    !model.texCoords.isEmpty() &&
                    face.texCoordIndices.get(i) * 2 + 1 < model.texCoords.size()) {
                    
                    int texIndex = face.texCoordIndices.get(i);
                    gl.glTexCoord2f(
                        model.texCoords.get(texIndex * 2),
                        model.texCoords.get(texIndex * 2 + 1)
                    );
                }
                
                // Draw vertex
                gl.glVertex3f(
                    model.vertices.get(vertIndex * 3),
                    model.vertices.get(vertIndex * 3 + 1),
                    model.vertices.get(vertIndex * 3 + 2)
                );
            }
            
            gl.glEnd();
        }
    }
    
    /**
     * Renders a 3D room with the specified dimensions and colors
     */
    private void renderRoom(GL2 gl) {
        // Save current matrix
        gl.glPushMatrix();
        
        // Calculate room dimensions (centered on origin)
        float halfWidth = roomWidth / 2.0f;
        float halfLength = roomLength / 2.0f;
        float halfHeight = roomHeight / 2.0f;
        
        // Render opaque surfaces first
        if (floorTransparency >= 1.0f) {
            // Floor (bottom)
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(floorColor.getRed()/255f, floorColor.getGreen()/255f, floorColor.getBlue()/255f, floorTransparency);
            gl.glNormal3f(0.0f, 1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glEnd();
        }
        
        if (ceilingTransparency >= 1.0f) {
            // Ceiling (top)
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(ceilingColor.getRed()/255f, ceilingColor.getGreen()/255f, ceilingColor.getBlue()/255f, ceilingTransparency);
            gl.glNormal3f(0.0f, -1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glEnd();
        }
        
        if (wallTransparency >= 1.0f) {
            // Walls
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed()/255f, wallColor.getGreen()/255f, wallColor.getBlue()/255f, wallTransparency);
            
            // Front wall
            gl.glNormal3f(0.0f, 0.0f, -1.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            
            // Back wall
            gl.glNormal3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            
            // Left wall
            gl.glNormal3f(1.0f, 0.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            
            // Right wall
            gl.glNormal3f(-1.0f, 0.0f, 0.0f);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            
            gl.glEnd();
        }
        
        // Now render transparent surfaces
        gl.glDepthMask(false); // Disable depth writes for transparency
        
        // Render transparent walls, floor, ceiling if needed
        if (wallTransparency < 1.0f) {
            // Transparent walls
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed()/255f, wallColor.getGreen()/255f, wallColor.getBlue()/255f, wallTransparency);
            
            // All walls (same vertices as above)
            // ...
            
            gl.glEnd();
        }
        
        if (floorTransparency < 1.0f) {
            // Transparent floor
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(floorColor.getRed()/255f, floorColor.getGreen()/255f, floorColor.getBlue()/255f, floorTransparency);
            gl.glNormal3f(0.0f, 1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glEnd();
        }
        
        if (ceilingTransparency < 1.0f) {
            // Transparent ceiling
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(ceilingColor.getRed()/255f, ceilingColor.getGreen()/255f, ceilingColor.getBlue()/255f, ceilingTransparency);
            gl.glNormal3f(0.0f, -1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glEnd();
        }
        
        // Re-enable depth writes
        gl.glDepthMask(true);
        
        // Restore matrix
        gl.glPopMatrix();
    }
} 