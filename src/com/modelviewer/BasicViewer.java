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
            
            addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    scale += -e.getWheelRotation() * 10.0f;
                    if (scale < 10.0f) scale = 10.0f;
                    repaint();
                }
            });
        }
        
        public void loadObjFile(String filePath) {
            try {
                // Clear previous data
                vertices.clear();
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
                        
                        vertices.add(new Vertex(x, y, z));
                        
                        // Update bounding box
                        minX = Math.min(minX, x);
                        maxX = Math.max(maxX, x);
                        minY = Math.min(minY, y);
                        maxY = Math.max(maxY, y);
                        minZ = Math.min(minZ, z);
                        maxZ = Math.max(maxZ, z);
                        
                    } else if ("f".equals(type)) {
                        // Face
                        int[] vertexIndices = new int[parts.length - 1];
                        
                        for (int i = 0; i < vertexIndices.length; i++) {
                            String[] indices = parts[i + 1].split("/");
                            vertexIndices[i] = Integer.parseInt(indices[0]) - 1; // OBJ indices start at 1
                        }
                        
                        faces.add(new Face(vertexIndices, currentMaterial));
                        
                    } else if ("mtllib".equals(type)) {
                        // Material library reference
                        String mtlFilePath = parts[1];
                        File objFile = new File(filePath);
                        File mtlFile = new File(objFile.getParentFile(), mtlFilePath);
                        if (mtlFile.exists()) {
                            mtlFileText.setText(mtlFile.getAbsolutePath());
                            loadMtlFile(mtlFile.getAbsolutePath());
                        }
                        
                    } else if ("usemtl".equals(type)) {
                        // Use material
                        currentMaterial = parts[1];
                    }
                }
                
                reader.close();
                System.out.println("Loaded OBJ: " + vertices.size() + " vertices, " + faces.size() + " faces");
                
                repaint();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        public void loadMtlFile(String filePath) {
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
                        String name = parts[1];
                        currentMaterial = new Material(name);
                        materials.put(name, currentMaterial);
                        
                    } else if (currentMaterial != null) {
                        if ("Kd".equals(type)) {
                            // Diffuse color
                            float r = Float.parseFloat(parts[1]);
                            float g = Float.parseFloat(parts[2]);
                            float b = Float.parseFloat(parts[3]);
                            currentMaterial.color = new Color(r, g, b);
                        }
                    }
                }
                
                reader.close();
                System.out.println("Loaded MTL: " + materials.size() + " materials");
                
                repaint();
                
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            
            if (vertices.isEmpty() || faces.isEmpty()) {
                g2d.setColor(Color.WHITE);
                g2d.drawString("No model loaded. Use the Browse button to load an OBJ file.", 20, getHeight() / 2);
                return;
            }
            
            // Center of panel
            int centerX = getWidth() / 2;
            int centerY = getHeight() / 2;
            
            // Calculate model center
            float modelCenterX = (minX + maxX) / 2;
            float modelCenterY = (minY + maxY) / 2;
            float modelCenterZ = (minZ + maxZ) / 2;
            
            // Create transformed vertices
            List<Point2D> projectedPoints = new ArrayList<>(vertices.size());
            
            double sinX = Math.sin(Math.toRadians(rotX));
            double cosX = Math.cos(Math.toRadians(rotX));
            double sinY = Math.sin(Math.toRadians(rotY));
            double cosY = Math.cos(Math.toRadians(rotY));
            
            for (Vertex v : vertices) {
                // Center the model
                float x = v.x - modelCenterX;
                float y = v.y - modelCenterY;
                float z = v.z - modelCenterZ;
                
                // Rotate around X axis
                float y2 = (float) (y * cosX - z * sinX);
                float z2 = (float) (y * sinX + z * cosX);
                
                // Rotate around Y axis
                float x3 = (float) (x * cosY + z2 * sinY);
                float z3 = (float) (-x * sinY + z2 * cosY);
                
                // Project to 2D
                float depth = 5; // Perspective strength
                float projX = x3 * scale / (z3 + depth);
                float projY = y2 * scale / (z3 + depth);
                
                projectedPoints.add(new Point2D(centerX + projX, centerY + projY, z3));
            }
            
            // Sort faces by depth (painter's algorithm)
            List<Face> sortedFaces = new ArrayList<>(faces);
            sortedFaces.sort((f1, f2) -> {
                float z1 = 0;
                for (int idx : f1.vertexIndices) {
                    z1 += projectedPoints.get(idx).z;
                }
                z1 /= f1.vertexIndices.length;
                
                float z2 = 0;
                for (int idx : f2.vertexIndices) {
                    z2 += projectedPoints.get(idx).z;
                }
                z2 /= f2.vertexIndices.length;
                
                return Float.compare(z2, z1);
            });
            
            // Draw faces
            for (Face face : sortedFaces) {
                Path2D path = new Path2D.Float();
                
                for (int i = 0; i < face.vertexIndices.length; i++) {
                    Point2D p = projectedPoints.get(face.vertexIndices[i]);
                    if (i == 0) {
                        path.moveTo(p.x, p.y);
                    } else {
                        path.lineTo(p.x, p.y);
                    }
                }
                path.closePath();
                
                // Set color based on material or use default color
                if (face.materialName != null && materials.containsKey(face.materialName)) {
                    g2d.setColor(materials.get(face.materialName).color);
                } else {
                    g2d.setColor(new Color(200, 200, 200));
                }
                
                g2d.fill(path);
                g2d.setColor(Color.BLACK);
                g2d.draw(path);
            }
        }
    }
    
    // Basic 3D classes
    static class Vertex {
        float x, y, z;
        
        public Vertex(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    static class Point2D {
        float x, y, z; // z is used for depth sorting
        
        public Point2D(float x, float y, float z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
    
    static class Face {
        int[] vertexIndices;
        String materialName;
        
        public Face(int[] vertexIndices, String materialName) {
            this.vertexIndices = vertexIndices;
            this.materialName = materialName;
        }
    }
    
    static class Material {
        String name;
        Color color = new Color(0.8f, 0.8f, 0.8f); // Default color
        
        public Material(String name) {
            this.name = name;
        }
    }
} 