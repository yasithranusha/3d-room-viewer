package com.modelviewer;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * 3D Model Loader and Viewer implementation
 */
public class Load3DModel extends JFrame implements GLEventListener {
    
    // OpenGL components
    private GL2 gl;
    private GLU glu;
    private GLJPanel glPanel;
    private FPSAnimator animator;
    
    // Model data
    private Model3D currentModel = null;
    private boolean wireframeMode = false;
    private boolean colorOverride = false;
    private Color overrideColor = new Color(200, 100, 100);
    
    // Camera properties
    private float rotX = 0.0f;
    private float rotY = 0.0f;
    private float zoom = 5.0f;
    private int lastMouseX;
    private int lastMouseY;
    private boolean mouseDown = false;
    
    // UI components
    private JPanel controlPanel;
    
    /**
     * Main constructor
     */
    public Load3DModel() {
        super("3D Model Viewer");
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        setupUI();
        setupOpenGL();
        setupInputHandlers();
        
        // Handle window close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (animator != null && animator.isAnimating()) {
                    animator.stop();
                }
                System.exit(0);
            }
        });
        
        // Set window properties
        setSize(1024, 768);
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    /**
     * Set up the user interface
     */
    private void setupUI() {
        setLayout(new BorderLayout());
        
        // Create the main 3D panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Add to the frame
        add(mainPanel, BorderLayout.CENTER);
    }
    
    /**
     * Set up OpenGL rendering
     */
    private void setupOpenGL() {
        // Initialize OpenGL
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities capabilities = new GLCapabilities(glProfile);
        
        // Create OpenGL panel
        glPanel = new GLJPanel(capabilities);
        glPanel.addGLEventListener(this);
        glPanel.setPreferredSize(new Dimension(800, 600));
        
        // Add the panel to the frame
        add(glPanel, BorderLayout.CENTER);
        
        // Set up animator
        animator = new FPSAnimator(glPanel, 60);
        animator.start();
        
        // Initialize GLU
        glu = new GLU();
    }
    
    /**
     * Set up input handlers for mouse control
     */
    private void setupInputHandlers() {
        // Mouse rotation
        glPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                mouseDown = true;
            }
            
            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                mouseDown = false;
            }
        });
        
        // Mouse drag for rotation
        glPanel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            @Override
            public void mouseDragged(java.awt.event.MouseEvent e) {
                if (mouseDown) {
                    int deltaX = e.getX() - lastMouseX;
                    int deltaY = e.getY() - lastMouseY;
                    
                    rotY += deltaX * 0.5f;
                    rotX += deltaY * 0.5f;
                    
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }
        });
        
        // Mouse wheel for zoom
        glPanel.addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            @Override
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                zoom += e.getWheelRotation() * 0.5f;
                if (zoom < 1.0f) zoom = 1.0f;
                if (zoom > 20.0f) zoom = 20.0f;
            }
        });
    }
    
    /**
     * Load a 3D model from an OBJ file
     */
    public void loadModel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open 3D Model");
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files (*.obj)", "obj"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            
            try {
                // Load the selected OBJ file
                currentModel = OBJLoader.loadOBJModel(selectedFile.getAbsolutePath());
                
                // Center and scale the model
                float[] center = currentModel.getCenter();
                float size = currentModel.getSize();
                
                // Apply transform
                currentModel.x = -center[0];
                currentModel.y = -center[1];
                currentModel.z = -center[2];
                currentModel.scale = 2.0f / Math.max(0.1f, size);
                
                // Reset view
                rotX = 0;
                rotY = 0;
                zoom = 5.0f;
                
                // Update window title
                setTitle("3D Model Viewer - " + selectedFile.getName());
                
                JOptionPane.showMessageDialog(this,
                    "Model loaded: " + currentModel.name + "\n" +
                    "Vertices: " + (currentModel.vertices.size() / 3) + "\n" +
                    "Faces: " + currentModel.faces.size() + "\n" +
                    "Materials: " + currentModel.materials.size(),
                    "Model Loaded", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(null,
                    "Error loading model: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Toggle wireframe mode
     */
    public void toggleWireframe() {
        wireframeMode = !wireframeMode;
    }
    
    /**
     * Toggle color override
     */
    public void toggleColorOverride() {
        colorOverride = !colorOverride;
        
        if (colorOverride) {
            // Let user pick a color
            Color newColor = JColorChooser.showDialog(this, 
                "Choose Override Color", overrideColor);
            
            if (newColor != null) {
                overrideColor = newColor;
            }
        }
    }
    