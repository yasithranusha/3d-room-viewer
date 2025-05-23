package com.modelviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Path2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

public class BasicViewer extends JFrame {
    private static final long serialVersionUID = 1L;
    
    private ModelPanel modelPanel;
    private JTextField objFileText;
    private JTextField mtlFileText;
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            BasicViewer viewer = new BasicViewer();
            viewer.setVisible(true);
        });
    }
    
    public BasicViewer() {
        super("Java 3D Model Viewer");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        
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
        
        add(controlPanel, BorderLayout.NORTH);
        
        // Create 3D view panel
        modelPanel = new ModelPanel();
        add(modelPanel, BorderLayout.CENTER);
        
        setLocationRelativeTo(null);
    }
    
    private void browseObjFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files", "obj"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            objFileText.setText(file.getAbsolutePath());
            modelPanel.loadObjFile(file.getAbsolutePath());
        }
    }
    
    private void browseMtlFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("MTL Files", "mtl"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            mtlFileText.setText(file.getAbsolutePath());
            modelPanel.loadMtlFile(file.getAbsolutePath());
        }
    }
    
    // Class to handle 3D model rendering
    class ModelPanel extends JPanel {
        private static final long serialVersionUID = 1L;
        
        private List<Vertex> vertices = new ArrayList<>();
        private List<Face> faces = new ArrayList<>();
        private Map<String, Material> materials = new HashMap<>();
        private String currentMaterial = null;
        
        private float rotX = 30.0f;
        private float rotY = 30.0f;
        private float scale = 100.0f;
        private int lastX, lastY;
        
        // Bounding box for auto-centering
        private float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
        private float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
        private float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;
        
        public ModelPanel() {
            setBackground(Color.BLACK);
            
            // Add mouse listeners for rotation and zooming
            addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    lastX = e.getX();
                    lastY = e.getY();
                }
            });
            
            addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    int dx = e.getX() - lastX;
                    int dy = e.getY() - lastY;
                    
                    rotY += dx * 0.5f;
                    rotX += dy * 0.5f;
                    
                    lastX = e.getX();
                    lastY = e.getY();
                    
                    repaint();
                }
            });
 