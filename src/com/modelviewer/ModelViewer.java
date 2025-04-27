package com.modelviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.util.Animator;

public class ModelViewer extends JFrame implements GLEventListener {
    private GLCanvas glCanvas;
    private Animator animator;
    private JButton loadButton;
    private JButton clearButton;
    
    public ModelViewer() {
        super("3D Model Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        // Create main panel
        JPanel mainPanel = new JPanel(new BorderLayout());
        
        // Create OpenGL canvas
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCanvas = new GLCanvas(glCapabilities);
        glCanvas.addGLEventListener(this);
        
        // Create control panel
        JPanel controlPanel = new JPanel();
        loadButton = new JButton("Load Model");
        clearButton = new JButton("Clear");
        
        loadButton.addActionListener(e -> loadModel());
        clearButton.addActionListener(e -> clearModel());
        
        controlPanel.add(loadButton);
        controlPanel.add(clearButton);
        
        // Add components to frame
        mainPanel.add(glCanvas, BorderLayout.CENTER);
        mainPanel.add(controlPanel, BorderLayout.SOUTH);
        
        add(mainPanel);
        
        // Set up animation
        animator = new Animator(glCanvas);
        animator.start();
        
        // Set up window
        setSize(800, 600);
        setLocationRelativeTo(null);
        
        // Handle window closing
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (animator != null) {
                    animator.stop();
                }
                System.exit(0);
            }
        });
    }
    
    private void loadModel() {
        // TODO: Implement model loading
        JOptionPane.showMessageDialog(this, "Model loading not implemented yet");
    }
    
    private void clearModel() {
        // TODO: Implement model clearing
        JOptionPane.showMessageDialog(this, "Model clearing not implemented yet");
    }
    
    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glEnable(GL2.GL_DEPTH_TEST);
    }
    
    @Override
    public void display(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        
        // TODO: Add rendering code
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
        // Cleanup
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new ModelViewer().setVisible(true);
        });
    }
} 