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
    