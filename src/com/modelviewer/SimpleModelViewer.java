package com.modelviewer;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jogamp.common.nio.Buffers;
import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GL2ES1;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.FPSAnimator;

public class SimpleModelViewer extends JFrame implements GLEventListener {
    private static final long serialVersionUID = 1L;
    private GLCanvas canvas;
    private FPSAnimator animator;
    
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    private float zoom = -5.0f;
    private int lastX = 0;
    private int lastY = 0;
    
    private List<Float> vertices = new ArrayList<>();
    private List<Float> normals = new ArrayList<>();
    private List<Float> textureCoords = new ArrayList<>();
    private List<Face> faces = new ArrayList<>();
    private Map<String, Material> materials = new HashMap<>();
    private String currentMaterial = null;
    
    private JTextField objFileText;
    private JTextField mtlFileText;
    private JCheckBox wireframeCheckbox;
    private JCheckBox smoothShadingCheckbox;
    private JComboBox<String> renderQualityCombo;
    
    // VBO buffers for improved performance
    private int[] vboHandles = new int[3]; // 0: vertices, 1: normals, 2: colors
    private boolean useVBO = true;
    
    // Bounding box for auto-centering
    private float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
    private float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;
    
    // Render settings
    private boolean wireframeMode = false;
    private boolean smoothShading = true;
    private int quality = 2; // 0: low, 1: medium, 2: high
    
    static class Face {
        int[] vertexIndices;
        int[] normalIndices;
        int[] texCoordIndices;
        String materialName;
        
        public Face(int[] vertexIndices, int[] texCoordIndices, int[] normalIndices, String materialName) {
            this.vertexIndices = vertexIndices;
            this.texCoordIndices = texCoordIndices;
            this.normalIndices = normalIndices;
            this.materialName = materialName;
        }
    }
    
    static class Material {
        float[] ambient = {0.2f, 0.2f, 0.2f, 1.0f};
        float[] diffuse = {0.8f, 0.8f, 0.8f, 1.0f};
        float[] specular = {1.0f, 1.0f, 1.0f, 1.0f};
        float shininess = 0.0f;
        String name;
        
        public Material(String name) {
            this.name = name;
        }
    }
    
    public static void main(String[] args) {
        // Force load native libraries early
        try {
            System.setProperty("jogamp.gluegen.UseTempJarCache", "false");
            System.setProperty("java.awt.headless", "false");
            System.loadLibrary("jogl_desktop");
            System.loadLibrary("nativewindow_awt");
            System.loadLibrary("gluegen-rt");
        } catch (Exception e) {
            System.out.println("Warning: Unable to preload native libraries: " + e.getMessage());
        }
        
        SwingUtilities.invokeLater(() -> {
            SimpleModelViewer viewer = new SimpleModelViewer();
            viewer.setVisible(true);
        });
    }
    
    public SimpleModelViewer() {
        super("Enhanced 3D Model Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1024, 768);
        
        // Create the UI
        JPanel controlPanel = new JPanel();
        
        JLabel objLabel = new JLabel("OBJ File:");
        controlPanel.add(objLabel);
        
        objFileText = new JTextField(20);
        controlPanel.add(objFileText);
        
        JButton objBrowseButton = new JButton("Browse");
        objBrowseButton.addActionListener(e -> browseObjFile());
        controlPanel.add(objBrowseButton);
        
        JLabel mtlLabel = new JLabel("MTL File:");
        controlPanel.add(mtlLabel);
        
        mtlFileText = new JTextField(20);
        controlPanel.add(mtlFileText);
        
        JButton mtlBrowseButton = new JButton("Browse");
        mtlBrowseButton.addActionListener(e -> browseMtlFile());
        controlPanel.add(mtlBrowseButton);
        
        // Add rendering options
        wireframeCheckbox = new JCheckBox("Wireframe");
        wireframeCheckbox.addActionListener(e -> {
            wireframeMode = wireframeCheckbox.isSelected();
        });
        controlPanel.add(wireframeCheckbox);
        
        smoothShadingCheckbox = new JCheckBox("Smooth Shading", true);
        smoothShadingCheckbox.addActionListener(e -> {
            smoothShading = smoothShadingCheckbox.isSelected();
        });
        controlPanel.add(smoothShadingCheckbox);
        
        JLabel qualityLabel = new JLabel("Quality:");
        controlPanel.add(qualityLabel);
        
        String[] qualityOptions = {"Low", "Medium", "High"};
        renderQualityCombo = new JComboBox<>(qualityOptions);
        renderQualityCombo.setSelectedIndex(2); // High quality default
        renderQualityCombo.addActionListener(e -> {
            quality = renderQualityCombo.getSelectedIndex();
            updateCanvasQuality();
        });
        controlPanel.add(renderQualityCombo);
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Create OpenGL canvas with anti-aliasing
        GLProfile glProfile;
        try {
            // Try explicitly setting the GL2 profile which is widely supported
            GLProfile.initSingleton();
            glProfile = GLProfile.get(GLProfile.GL2);
        } catch (Exception e) {
            System.out.println("Warning: Unable to get GL2 profile: " + e.getMessage());
            try {
                // Fall back to a more compatible profile
                glProfile = GLProfile.getDefault();
            } catch (Exception e2) {
                throw new RuntimeException("No OpenGL profile available", e2);
            }
        }
        
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCapabilities.setDoubleBuffered(true);
        glCapabilities.setHardwareAccelerated(true);
        
        // Only enable multisampling if we're not using a fallback profile
        if (glProfile.getName().contains("GL2")) {
            try {
                glCapabilities.setSampleBuffers(true);
                glCapabilities.setNumSamples(2); // Reduced from 4 for better compatibility
            } catch (Exception e) {
                System.out.println("Warning: Multisampling not supported: " + e.getMessage());
            }
        }
        
        canvas = new GLCanvas(glCapabilities);
        canvas.addGLEventListener(this);
        canvas.setPreferredSize(new Dimension(1024, 650));
        
        // Add mouse interaction
        canvas.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastX = e.getX();
                lastY = e.getY();
            }
        });
        
        canvas.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                int dx = e.getX() - lastX;
                int dy = e.getY() - lastY;
                
                rotY += dx * 0.5f;
                rotX += dy * 0.5f;
                
                lastX = e.getX();
                lastY = e.getY();
            }
        });
        
        canvas.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                zoom += e.getWheelRotation() * 0.5f;
            }
        });
        
        add(canvas, BorderLayout.CENTER);
        
        // Set up animator with higher frame rate
        animator = new FPSAnimator(canvas, 60);
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (animator.isAnimating()) {
                    animator.stop();
                }
                System.exit(0);
            }
        });
        
        pack();
        setLocationRelativeTo(null);
        
        // Start the animator
        animator.start();
    }
    
    private void updateCanvasQuality() {
        if (canvas != null) {
            GLCapabilities caps = (GLCapabilities) canvas.getChosenGLCapabilities().cloneMutable();
            
            switch (quality) {
                case 0: // Low
                    caps.setSampleBuffers(false);
                    caps.setNumSamples(0);
                    break;
                case 1: // Medium
                    caps.setSampleBuffers(true);
                    caps.setNumSamples(2);
                    break;
                case 2: // High
                    caps.setSampleBuffers(true);
                    caps.setNumSamples(4);
                    break;
            }
            
            // Need to recreate the canvas for the changes to take effect
            // This is a simplification - in a real app, you would need to preserve the GL state
            canvas.setRealized(false);
            canvas.setRealized(true);
        }
    }
    
    private void browseObjFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files", "obj"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            objFileText.setText(file.getAbsolutePath());
            loadObjFile(file.getAbsolutePath());
        }
    }
    
    private void browseMtlFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MTL Files", "mtl"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            mtlFileText.setText(file.getAbsolutePath());
            loadMtlFile(file.getAbsolutePath());
        }
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        try {
            GL2 gl = drawable.getGL().getGL2();
            System.out.println("OpenGL Vendor: " + gl.glGetString(GL.GL_VENDOR));
            System.out.println("OpenGL Renderer: " + gl.glGetString(GL.GL_RENDERER));
            System.out.println("OpenGL Version: " + gl.glGetString(GL.GL_VERSION));
            System.out.println("OpenGL Extensions: " + gl.glGetString(GL.GL_EXTENSIONS));
            
            gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
            gl.glEnable(GL.GL_DEPTH_TEST);
            
            // Use back face culling if available
            try {
                gl.glEnable(GL.GL_CULL_FACE);
                gl.glCullFace(GL.GL_BACK);
            } catch (Exception e) {
                System.out.println("Warning: Face culling not supported: " + e.getMessage());
            }
            
            // Setup lighting
            try {
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glEnable(GL2.GL_LIGHT0);
                gl.glEnable(GL2.GL_NORMALIZE);
                
                setupLighting(gl);
            } catch (Exception e) {
                System.out.println("Warning: Lighting setup failed: " + e.getMessage());
                useVBO = false; // Disable VBO if lighting fails
            }
            
            // Enable multisampling if supported
            try {
                if (quality > 0) {
                    gl.glEnable(GL.GL_MULTISAMPLE);
                }
            } catch (Exception e) {
                System.out.println("Warning: Multisampling not supported: " + e.getMessage());
            }
            
            // Generate VBO handles if supported
            try {
                if (useVBO) {
                    int[] vboArray = new int[3];
                    gl.glGenBuffers(3, vboArray, 0);
                    vboHandles = vboArray;
                    
                    System.out.println("VBO enabled with handles: " + 
                                      vboHandles[0] + ", " + 
                                      vboHandles[1] + ", " + 
                                      vboHandles[2]);
                }
            } catch (Exception e) {
                System.out.println("Warning: VBO not supported: " + e.getMessage());
                useVBO = false;
            }
        } catch (Exception e) {
            System.err.println("Error during OpenGL initialization: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private void setupLighting(GL2 gl) {
        // Create a more pleasing lighting setup
        float[] lightAmbient = {0.3f, 0.3f, 0.3f, 1.0f};
        float[] lightDiffuse = {0.7f, 0.7f, 0.7f, 1.0f};
        float[] lightSpecular = {1.0f, 1.0f, 1.0f, 1.0f};
        
        // Main light from above
        float[] lightPosition0 = {1.0f, 10.0f, 5.0f, 0.0f}; // Directional light
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);
        gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition0, 0);
        
        // Enable secondary light for fill
        gl.glEnable(GL2.GL_LIGHT1);
        float[] lightAmbient1 = {0.1f, 0.1f, 0.15f, 1.0f};  // Slight blue tint
        float[] lightDiffuse1 = {0.3f, 0.3f, 0.4f, 1.0f};   // For shadow fill
        float[] lightPosition1 = {-10.0f, -4.0f, -2.0f, 0.0f}; // From opposite direction
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_AMBIENT, lightAmbient1, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_DIFFUSE, lightDiffuse1, 0);
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_SPECULAR, new float[]{0,0,0,1}, 0); // No specular
        gl.glLightfv(GL2.GL_LIGHT1, GL2.GL_POSITION, lightPosition1, 0);
        
        // Global ambient light
        gl.glLightModelfv(GL2.GL_LIGHT_MODEL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL.GL_TRUE);
        gl.glLightModeli(GL2.GL_LIGHT_MODEL_TWO_SIDE, GL.GL_FALSE);
        
        // Material defaults
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, new float[]{0.2f, 0.2f, 0.2f, 1.0f}, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, new float[]{0.8f, 0.8f, 0.8f, 1.0f}, 0);
        gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, new float[]{0.5f, 0.5f, 0.5f, 1.0f}, 0);
        gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, 64.0f);
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
        
        // Apply model centering based on bounding box
        float centerX = (minX + maxX) / 2;
        float centerY = (minY + maxY) / 2;
        float centerZ = (minZ + maxZ) / 2;
        gl.glTranslatef(-centerX, -centerY, -centerZ);
        
        // Set shading model based on user preference
        if (smoothShading) {
            gl.glShadeModel(GL2.GL_SMOOTH);
        } else {
            gl.glShadeModel(GL2.GL_FLAT);
        }
        
        // Set drawing mode
        if (wireframeMode) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
        } else {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        }
        
        // Draw the model
        renderModel(gl);
        
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
        
        // Adjust field of view and clipping planes based on quality setting
        float fov = 45.0f;
        if (quality >= 2) {
            // Higher quality can afford a wider FOV and further clipping planes
            gl.glFrustum(-aspect, aspect, -1.0, 1.0, 1.0, 200.0);
        } else {
            gl.glFrustum(-aspect, aspect, -1.0, 1.0, 1.0, 100.0);
        }
        
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }
    
    @Override
    public void dispose(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        
        // Clean up VBOs when done
        if (useVBO && vboHandles[0] != 0) {
            gl.glDeleteBuffers(3, vboHandles, 0);
        }
    }
    
    private void loadObjFile(String filePath) {
        try {
            // Clear previous data
            vertices.clear();
            normals.clear();
            textureCoords.clear();
            faces.clear();
            
            // Reset bounding box
            minX = minY = minZ = Float.MAX_VALUE;
            maxX = maxY = maxZ = Float.MIN_VALUE;
            
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                String type = parts[0];
                
                if ("v".equals(type)) {
                    // Vertex
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    
                    vertices.add(x);
                    vertices.add(y);
                    vertices.add(z);
                    
                    // Update bounding box
                    minX = Math.min(minX, x);
                    maxX = Math.max(maxX, x);
                    minY = Math.min(minY, y);
                    maxY = Math.max(maxY, y);
                    minZ = Math.min(minZ, z);
                    maxZ = Math.max(maxZ, z);
                    
                } else if ("vn".equals(type)) {
                    // Normal
                    float x = Float.parseFloat(parts[1]);
                    float y = Float.parseFloat(parts[2]);
                    float z = Float.parseFloat(parts[3]);
                    
                    normals.add(x);
                    normals.add(y);
                    normals.add(z);
                    
                } else if ("vt".equals(type)) {
                    // Texture coordinate
                    float u = Float.parseFloat(parts[1]);
                    float v = parts.length > 2 ? Float.parseFloat(parts[2]) : 0.0f;
                    
                    textureCoords.add(u);
                    textureCoords.add(v);
                    
                } else if ("f".equals(type)) {
                    // Face
                    int[] vertIndices = new int[parts.length - 1];
                    int[] texIndices = new int[parts.length - 1];
                    int[] normIndices = new int[parts.length - 1];
                    
                    for (int i = 1; i < parts.length; i++) {
                        String[] indices = parts[i].split("/");
                        
                        vertIndices[i - 1] = Integer.parseInt(indices[0]) - 1;
                        texIndices[i - 1] = indices.length > 1 && !indices[1].isEmpty() ? Integer.parseInt(indices[1]) - 1 : -1;
                        normIndices[i - 1] = indices.length > 2 ? Integer.parseInt(indices[2]) - 1 : -1;
                    }
                    
                    faces.add(new Face(vertIndices, texIndices, normIndices, currentMaterial));
                    
                } else if ("mtllib".equals(type)) {
                    // Material library
                    if (parts.length > 1) {
                        File objFile = new File(filePath);
                        File mtlFile = new File(objFile.getParent(), parts[1]);
                        if (mtlFile.exists()) {
                            mtlFileText.setText(mtlFile.getAbsolutePath());
                            loadMtlFile(mtlFile.getAbsolutePath());
                        }
                    }
                    
                } else if ("usemtl".equals(type)) {
                    // Use material
                    if (parts.length > 1) {
                        currentMaterial = parts[1];
                    }
                }
            }
            
            reader.close();
            
            // Prepare VBO data if using VBOs
            if (useVBO && !vertices.isEmpty()) {
                prepareVBOs(canvas.getGL().getGL2());
            }
            
            // Calculate auto-scale factor for consistent model size
            float modelSize = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
            float scaleFactor = 4.0f / modelSize;
            zoom = -5.0f * scaleFactor; // Adjust zoom based on model size
            
            System.out.println("Loaded model with " + (vertices.size() / 3) + " vertices and " + faces.size() + " faces");
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void prepareVBOs(GL2 gl) {
        // Convert ArrayList to arrays for VBO
        float[] vertexArray = new float[vertices.size()];
        for (int i = 0; i < vertices.size(); i++) {
            vertexArray[i] = vertices.get(i);
        }
        
        float[] normalArray = null;
        if (!normals.isEmpty()) {
            normalArray = new float[normals.size()];
            for (int i = 0; i < normals.size(); i++) {
                normalArray[i] = normals.get(i);
            }
        }
        
        // Create and populate vertex buffer
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandles[0]);
        FloatBuffer vertexBuffer = Buffers.newDirectFloatBuffer(vertexArray);
        gl.glBufferData(GL.GL_ARRAY_BUFFER, vertexArray.length * Float.BYTES, vertexBuffer, GL.GL_STATIC_DRAW);
        
        // Create and populate normal buffer if available
        if (normalArray != null) {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandles[1]);
            FloatBuffer normalBuffer = Buffers.newDirectFloatBuffer(normalArray);
            gl.glBufferData(GL.GL_ARRAY_BUFFER, normalArray.length * Float.BYTES, normalBuffer, GL.GL_STATIC_DRAW);
        }
        
        // Unbind buffer
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }
    
    private void loadMtlFile(String filePath) {
        try {
            materials.clear();
            
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line;
            
            Material currentMaterial = null;
            
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                
                String[] parts = line.split("\\s+");
                String type = parts[0];
                
                if ("newmtl".equals(type)) {
                    // New material
                    if (parts.length > 1) {
                        currentMaterial = new Material(parts[1]);
                        materials.put(parts[1], currentMaterial);
                    }
                    
                } else if (currentMaterial != null) {
                    if ("Ka".equals(type)) {
                        // Ambient color
                        currentMaterial.ambient[0] = Float.parseFloat(parts[1]);
                        currentMaterial.ambient[1] = Float.parseFloat(parts[2]);
                        currentMaterial.ambient[2] = Float.parseFloat(parts[3]);
                        
                    } else if ("Kd".equals(type)) {
                        // Diffuse color
                        currentMaterial.diffuse[0] = Float.parseFloat(parts[1]);
                        currentMaterial.diffuse[1] = Float.parseFloat(parts[2]);
                        currentMaterial.diffuse[2] = Float.parseFloat(parts[3]);
                        
                    } else if ("Ks".equals(type)) {
                        // Specular color
                        currentMaterial.specular[0] = Float.parseFloat(parts[1]);
                        currentMaterial.specular[1] = Float.parseFloat(parts[2]);
                        currentMaterial.specular[2] = Float.parseFloat(parts[3]);
                        
                    } else if ("Ns".equals(type)) {
                        // Shininess
                        currentMaterial.shininess = Float.parseFloat(parts[1]);
                    }
                }
            }
            
            reader.close();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    private void renderModel(GL2 gl) {
        if (vertices.isEmpty()) {
            return;
        }
        
        if (useVBO && vboHandles[0] != 0) {
            renderWithVBO(gl);
        } else {
            renderImmediate(gl);
        }
    }
    
    private void renderWithVBO(GL2 gl) {
        // Enable vertex and normal arrays
        gl.glEnableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glEnableClientState(GL2.GL_NORMAL_ARRAY);
        
        // Bind vertex buffer
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandles[0]);
        gl.glVertexPointer(3, GL.GL_FLOAT, 0, 0);
        
        // Bind normal buffer if available
        if (!normals.isEmpty()) {
            gl.glBindBuffer(GL.GL_ARRAY_BUFFER, vboHandles[1]);
            gl.glNormalPointer(GL.GL_FLOAT, 0, 0);
        }
        
        // Render each face
        Material lastMaterial = null;
        
        for (Face face : faces) {
            // Apply material if available
            if (face.materialName != null && materials.containsKey(face.materialName)) {
                Material material = materials.get(face.materialName);
                if (material != lastMaterial) {
                    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, material.ambient, 0);
                    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, material.diffuse, 0);
                    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, material.specular, 0);
                    gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, material.shininess);
                    lastMaterial = material;
                }
            }
            
            // Draw the face (supports triangles and quads)
            if (face.vertexIndices.length == 3) {
                gl.glDrawArrays(GL.GL_TRIANGLES, face.vertexIndices[0], 3);
            } else if (face.vertexIndices.length == 4) {
                gl.glDrawArrays(GL2.GL_QUADS, face.vertexIndices[0], 4);
            } else {
                gl.glBegin(GL2.GL_POLYGON);
                for (int i = 0; i < face.vertexIndices.length; i++) {
                    int vertIndex = face.vertexIndices[i];
                    if (face.normalIndices != null && face.normalIndices[i] >= 0) {
                        int normIndex = face.normalIndices[i];
                        gl.glNormal3f(normals.get(normIndex * 3), normals.get(normIndex * 3 + 1), normals.get(normIndex * 3 + 2));
                    }
                    gl.glVertex3f(vertices.get(vertIndex * 3), vertices.get(vertIndex * 3 + 1), vertices.get(vertIndex * 3 + 2));
                }
                gl.glEnd();
            }
        }
        
        // Disable arrays and unbind buffers
        gl.glDisableClientState(GL2.GL_VERTEX_ARRAY);
        gl.glDisableClientState(GL2.GL_NORMAL_ARRAY);
        gl.glBindBuffer(GL.GL_ARRAY_BUFFER, 0);
    }
    
    private void renderImmediate(GL2 gl) {
        // Direct immediate mode rendering (slower but compatible with all systems)
        Material lastMaterial = null;
        
        for (Face face : faces) {
            // Apply material if available
            if (face.materialName != null && materials.containsKey(face.materialName)) {
                Material material = materials.get(face.materialName);
                if (material != lastMaterial) {
                    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_AMBIENT, material.ambient, 0);
                    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_DIFFUSE, material.diffuse, 0);
                    gl.glMaterialfv(GL.GL_FRONT, GL2.GL_SPECULAR, material.specular, 0);
                    gl.glMaterialf(GL.GL_FRONT, GL2.GL_SHININESS, material.shininess);
                    lastMaterial = material;
                }
            }
            
            // Draw the face
            if (face.vertexIndices.length == 3) {
                gl.glBegin(GL.GL_TRIANGLES);
            } else if (face.vertexIndices.length == 4) {
                gl.glBegin(GL2.GL_QUADS);
            } else {
                gl.glBegin(GL2.GL_POLYGON);
            }
            
            for (int i = 0; i < face.vertexIndices.length; i++) {
                int vertIndex = face.vertexIndices[i];
                
                // Apply normal if available
                if (face.normalIndices != null && face.normalIndices[i] >= 0) {
                    int normIndex = face.normalIndices[i];
                    gl.glNormal3f(normals.get(normIndex * 3), normals.get(normIndex * 3 + 1), normals.get(normIndex * 3 + 2));
                }
                
                // Apply texture coordinate if available
                if (face.texCoordIndices != null && face.texCoordIndices[i] >= 0 && !textureCoords.isEmpty()) {
                    int texIndex = face.texCoordIndices[i];
                    gl.glTexCoord2f(textureCoords.get(texIndex * 2), textureCoords.get(texIndex * 2 + 1));
                }
                
                // Set vertex
                gl.glVertex3f(
                    vertices.get(vertIndex * 3),
                    vertices.get(vertIndex * 3 + 1),
                    vertices.get(vertIndex * 3 + 2)
                );
            }
            
            gl.glEnd();
        }
    }
} 