package com.modelviewer.render;

import java.awt.Color;
import java.util.List;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.glu.GLU;
import com.modelviewer.model.Face;
import com.modelviewer.model.Material;
import com.modelviewer.model.Model3D;
import com.modelviewer.model.Room;

public class GLRenderer implements GLEventListener {
    private GLU glu;
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    private float zoom = -5.0f;
    private boolean wireframeMode = false;
    private boolean use2DView = false;

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        glu = new GLU();

        // Basic OpenGL setup
        gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);

        // Enable transparency
        gl.glEnable(GL2.GL_BLEND);
        gl.glBlendFunc(GL2.GL_SRC_ALPHA, GL2.GL_ONE_MINUS_SRC_ALPHA);

        // Setup lighting
        gl.glEnable(GL2.GL_LIGHTING);
        gl.glEnable(GL2.GL_LIGHT0);

        // Enable materials
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);

        setupCamera(gl);
        setupLighting(gl);

        if (currentRoom != null) {
            renderRoom(gl, currentRoom);
        }

        if (currentModel != null) {
            renderModel(gl, currentModel);
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();

        if (height <= 0) {
            height = 1;
        }

        float aspect = (float) width / (float) height;

        gl.glViewport(0, 0, width, height);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(45.0f, aspect, 0.1f, 100.0f);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Cleanup resources
    }

    private void setupCamera(GL2 gl) {
        gl.glLoadIdentity();
        if (use2DView) {
            // Top-down view
            gl.glTranslatef(0, -zoom, 0);
            gl.glRotatef(90, 1.0f, 0.0f, 0.0f);
        } else {
            // 3D view
            gl.glTranslatef(0, 0, zoom);
            gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
            gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
        }
    }

    private void setupLighting(GL2 gl) {
        float[] lightPosition = { 0.0f, 10.0f, 0.0f, 1.0f };
        float[] lightAmbient = { 0.2f, 0.2f, 0.2f, 1.0f };
        float[] lightDiffuse = { 1.0f, 1.0f, 1.0f, 1.0f };
        float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };

        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
    }

    // Getters and setters for camera control
    public void setRotation(float x, float y) {
        this.rotX = x;
        this.rotY = y;
    }

    public void setZoom(float zoom) {
        this.zoom = zoom;
    }

    public void setWireframeMode(boolean wireframe) {
        this.wireframeMode = wireframe;
    }

    public void setUse2DView(boolean use2D) {
        this.use2DView = use2D;
    }

    private Room currentRoom;
    private Model3D currentModel;

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }

    public void setCurrentModel(Model3D model) {
        this.currentModel = model;
    }

    private void renderRoom(GL2 gl, Room room) {
        // Save current matrix
        gl.glPushMatrix();

        // Set room colors and transparency
        float[] dimensions = room.getDimensions();
        float halfWidth = dimensions[0] / 2.0f;
        float height = dimensions[1];
        float halfLength = dimensions[2] / 2.0f;

        // Render floor
        setColorWithTransparency(gl, room.getFloorColor(), room.getFloorTransparency());
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-halfWidth, 0.0f, -halfLength);
        gl.glVertex3f(-halfWidth, 0.0f, halfLength);
        gl.glVertex3f(halfWidth, 0.0f, halfLength);
        gl.glVertex3f(halfWidth, 0.0f, -halfLength);
        gl.glEnd();

        // Render ceiling
        setColorWithTransparency(gl, room.getCeilingColor(), room.getCeilingTransparency());
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, -1.0f, 0.0f);
        gl.glVertex3f(-halfWidth, height, -halfLength);
        gl.glVertex3f(halfWidth, height, -halfLength);
        gl.glVertex3f(halfWidth, height, halfLength);
        gl.glVertex3f(-halfWidth, height, halfLength);
        gl.glEnd();

        // Render walls
        setColorWithTransparency(gl, room.getWallColor(), room.getWallTransparency());

        // Front wall
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 0.0f, 1.0f);
        gl.glVertex3f(-halfWidth, 0.0f, -halfLength);
        gl.glVertex3f(halfWidth, 0.0f, -halfLength);
        gl.glVertex3f(halfWidth, height, -halfLength);
        gl.glVertex3f(-halfWidth, height, -halfLength);
        gl.glEnd();

        // Back wall
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(0.0f, 0.0f, -1.0f);
        gl.glVertex3f(-halfWidth, 0.0f, halfLength);
        gl.glVertex3f(-halfWidth, height, halfLength);
        gl.glVertex3f(halfWidth, height, halfLength);
        gl.glVertex3f(halfWidth, 0.0f, halfLength);
        gl.glEnd();

        // Left wall
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(1.0f, 0.0f, 0.0f);
        gl.glVertex3f(-halfWidth, 0.0f, -halfLength);
        gl.glVertex3f(-halfWidth, height, -halfLength);
        gl.glVertex3f(-halfWidth, height, halfLength);
        gl.glVertex3f(-halfWidth, 0.0f, halfLength);
        gl.glEnd();

        // Right wall
        gl.glBegin(GL2.GL_QUADS);
        gl.glNormal3f(-1.0f, 0.0f, 0.0f);
        gl.glVertex3f(halfWidth, 0.0f, -halfLength);
        gl.glVertex3f(halfWidth, 0.0f, halfLength);
        gl.glVertex3f(halfWidth, height, halfLength);
        gl.glVertex3f(halfWidth, height, -halfLength);
        gl.glEnd();

        // Render room models
        for (Model3D model : room.getModels()) {
            renderModel(gl, model);
        }

        // Restore matrix
        gl.glPopMatrix();
    }

    private void renderModel(GL2 gl, Model3D model) {
        gl.glPushMatrix();

        // Apply model transformations
        gl.glTranslatef(model.getX(), model.getY(), model.getZ());
        gl.glRotatef(model.getRotY(), 0.0f, 1.0f, 0.0f);
        gl.glScalef(model.getScale(), model.getScale(), model.getScale());

        // Set rendering mode
        if (wireframeMode) {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_LINE);
        }

        // Render faces
        for (Face face : model.getFaces()) {
            // Apply material if available
            if (face.getMaterialName() != null && !model.isUseCustomColor()) {
                Material material = model.getMaterials().get(face.getMaterialName());
                if (material != null) {
                    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_AMBIENT, material.getAmbient(), 0);
                    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_DIFFUSE, material.getDiffuse(), 0);
                    gl.glMaterialfv(GL2.GL_FRONT, GL2.GL_SPECULAR, material.getSpecular(), 0);
                    gl.glMaterialf(GL2.GL_FRONT, GL2.GL_SHININESS, material.getShininess());
                }
            } else if (model.isUseCustomColor()) {
                Color color = model.getCustomColor();
                float[] colorComponents = color.getComponents(null);
                gl.glColor4f(colorComponents[0], colorComponents[1], colorComponents[2], colorComponents[3]);
            }

            // Render face
            gl.glBegin(GL2.GL_POLYGON);
            int[] vertexIndices = face.getVertexIndices();
            int[] normalIndices = face.getNormalIndices();
            int[] texCoordIndices = face.getTexCoordIndices();

            for (int i = 0; i < vertexIndices.length; i++) {
                if (normalIndices != null && normalIndices.length > i) {
                    List<Float> normals = model.getNormals();
                    int normalIndex = normalIndices[i] * 3;
                    gl.glNormal3f(
                            normals.get(normalIndex),
                            normals.get(normalIndex + 1),
                            normals.get(normalIndex + 2));
                }

                if (texCoordIndices != null && texCoordIndices.length > i) {
                    List<Float> texCoords = model.getTextureCoords();
                    int texCoordIndex = texCoordIndices[i] * 2;
                    gl.glTexCoord2f(
                            texCoords.get(texCoordIndex),
                            texCoords.get(texCoordIndex + 1));
                }

                List<Float> vertices = model.getVertices();
                int vertexIndex = vertexIndices[i] * 3;
                gl.glVertex3f(
                        vertices.get(vertexIndex),
                        vertices.get(vertexIndex + 1),
                        vertices.get(vertexIndex + 2));
            }
            gl.glEnd();
        }

        // Reset polygon mode if in wireframe
        if (wireframeMode) {
            gl.glPolygonMode(GL2.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }

        gl.glPopMatrix();
    }

    private void setColorWithTransparency(GL2 gl, Color color, float transparency) {
        float[] components = color.getComponents(null);
        gl.glColor4f(components[0], components[1], components[2], transparency);
    }
}