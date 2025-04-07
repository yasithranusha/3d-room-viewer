package com.modelviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;
import com.jogamp.opengl.util.Animator;
import com.modelviewer.SoftwareRenderer.Model3D;

/**
 * Model Manager Window providing both 3D and 2D views of models
 * with functionality to add, update, view, and delete models.
 */
public class ModelManagerWindow extends JFrame {
    private static final long serialVersionUID = 1L;
    
    // Main components
    private JSplitPane mainSplitPane;
    private JPanel leftPanel;
    private JSplitPane rightSplitPane;
    private ThreeDView threeDView;
    private TwoDView twoDView;
    
    // Model list
    private DefaultListModel<String> modelsListModel;
    private JList<String> modelsList;
    private List<Model3D> models = new ArrayList<>();
    private int selectedModelIndex = -1;
    
    // Controls
    private JTextField modelNameField;
    private JButton addButton;
    private JButton updateButton;
    private JButton deleteButton;
    private JButton closeButton;
    
    // Reference to main application
    private SoftwareRenderer mainApp;
    
    /**
     * Constructor for the Model Manager Window
     * 
     * @param mainApp Reference to the main application
     */
    public ModelManagerWindow(SoftwareRenderer mainApp) {
        super("Model Manager");
        this.mainApp = mainApp;
        
        setSize(1000, 700);
        setLocationRelativeTo(mainApp);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        createComponents();
        layoutComponents();
        setupListeners();
        
        // Make sure OpenGL rendering stops when window is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                threeDView.stopAnimator();
            }
        });
    }
    
    /**
     * Creates all UI components
     */
    private void createComponents() {
        // Create model list
        modelsListModel = new DefaultListModel<>();
        modelsList = new JList<>(modelsListModel);
        modelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create 3D view
        threeDView = new ThreeDView();
        
        // Create 2D view
        twoDView = new TwoDView();
        
        // Create controls
        modelNameField = new JTextField(20);
        addButton = new JButton("Add Model");
        updateButton = new JButton("Update");
        deleteButton = new JButton("Delete");
        closeButton = new JButton("Close");
        
        // Disable update/delete initially
        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }
    
    /**
     * Lays out all UI components
     */
    private void layoutComponents() {
        // Set main layout
        setLayout(new BorderLayout());
        
        // Create left panel with model list and controls
        leftPanel = new JPanel(new BorderLayout());
        leftPanel.setBorder(BorderFactory.createTitledBorder("Models"));
        
        // Add model list with scroll pane
        JScrollPane listScrollPane = new JScrollPane(modelsList);
        listScrollPane.setPreferredSize(new Dimension(200, 400));
        leftPanel.add(listScrollPane, BorderLayout.CENTER);
        
        // Create control panel
        JPanel controlPanel = new JPanel(new GridLayout(6, 1, 5, 5));
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add name field
        JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        namePanel.add(new JLabel("Model Name:"));
        namePanel.add(modelNameField);
        controlPanel.add(namePanel);
        
        // Add buttons
        controlPanel.add(addButton);
        controlPanel.add(updateButton);
        controlPanel.add(deleteButton);
        controlPanel.add(new JPanel()); // Spacer
        controlPanel.add(closeButton);
        
        leftPanel.add(controlPanel, BorderLayout.SOUTH);
        
        // Set up right split pane with 3D and 2D views
        rightSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        rightSplitPane.setTopComponent(threeDView);
        rightSplitPane.setBottomComponent(twoDView);
        rightSplitPane.setResizeWeight(0.7); // 70% to 3D view
        
        // Set up main split pane
        mainSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        mainSplitPane.setLeftComponent(leftPanel);
        mainSplitPane.setRightComponent(rightSplitPane);
        mainSplitPane.setResizeWeight(0.3); // 30% to model list
        
        // Add to frame
        add(mainSplitPane, BorderLayout.CENTER);
    }
    
    /**
     * Sets up event listeners for UI components
     */
    private void setupListeners() {
        // Model list selection listener
        modelsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    int index = modelsList.getSelectedIndex();
                    if (index >= 0 && index < models.size()) {
                        selectedModelIndex = index;
                        Model3D model = models.get(index);
                        
                        // Update fields
                        modelNameField.setText(model.name);
                        
                        // Update views
                        threeDView.setModel(model);
                        twoDView.setModel(model);
                        
                        // Enable update/delete
                        updateButton.setEnabled(true);
                        deleteButton.setEnabled(true);
                    } else {
                        selectedModelIndex = -1;
                        modelNameField.setText("");
                        
                        // Clear views
                        threeDView.setModel(null);
                        twoDView.setModel(null);
                        
                        // Disable update/delete
                        updateButton.setEnabled(false);
                        deleteButton.setEnabled(false);
                    }
                }
            }
        });
        
        // Add button
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addNewModel();
            }
        });
        
        // Update button
        updateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateSelectedModel();
            }
        });
        
        // Delete button
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteSelectedModel();
            }
        });
        
        // Close button
        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
    }
    
    /**
     * Adds a new model from file
     */
    private void addNewModel() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files", "obj"));
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Get model name from user
            String name = JOptionPane.showInputDialog(this, 
                                                     "Enter model name:", 
                                                     file.getName());
            if (name == null || name.trim().isEmpty()) {
                name = file.getName();
            }
            
            // Create new model
            Model3D model = new Model3D(name);
            
            // Load model data
            if (mainApp.loadModelFromObjFile(model, file.getAbsolutePath())) {
                // Add to models list
                models.add(model);
                modelsListModel.addElement(name);
                
                // Select the new model
                modelsList.setSelectedIndex(models.size() - 1);
                
                JOptionPane.showMessageDialog(this, 
                                             "Model added successfully.", 
                                             "Success", 
                                             JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, 
                                             "Failed to load model.", 
                                             "Error", 
                                             JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    /**
     * Updates the selected model's properties
     */
    private void updateSelectedModel() {
        if (selectedModelIndex >= 0 && selectedModelIndex < models.size()) {
            Model3D model = models.get(selectedModelIndex);
            
            // Update model name
            String newName = modelNameField.getText().trim();
            if (!newName.isEmpty() && !newName.equals(model.name)) {
                model.name = newName;
                modelsListModel.setElementAt(newName, selectedModelIndex);
            }
            
            JOptionPane.showMessageDialog(this, 
                                         "Model updated successfully.", 
                                         "Success", 
                                         JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    /**
     * Deletes the selected model
     */
    private void deleteSelectedModel() {
        if (selectedModelIndex >= 0 && selectedModelIndex < models.size()) {
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this model?",
                "Confirm Delete",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                models.remove(selectedModelIndex);
                modelsListModel.remove(selectedModelIndex);
                
                // Select a new model if available
                if (!models.isEmpty()) {
                    int newIndex = Math.min(selectedModelIndex, models.size() - 1);
                    modelsList.setSelectedIndex(newIndex);
                } else {
                    modelsList.clearSelection();
                }
            }
        }
    }
    
    /**
     * 3D view component for displaying models
     */
    private class ThreeDView extends JPanel implements GLEventListener {
        private static final long serialVersionUID = 1L;
        
        private GLJPanel canvas;
        private Animator animator;
        private Model3D model;
        
        // View parameters
        private float rotX = 20.0f;
        private float rotY = 30.0f;
        private float zoom = -5.0f;
        private int lastX = 0;
        private int lastY = 0;
        
        public ThreeDView() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("3D View"));
            
            try {
                // Use GL2 profile for compatibility
                GLProfile glProfile = GLProfile.get(GLProfile.GL2);
                GLCapabilities glCapabilities = new GLCapabilities(glProfile);
                glCapabilities.setHardwareAccelerated(false);
                glCapabilities.setDoubleBuffered(true);
                
                // Create a GLJPanel for software rendering
                canvas = new GLJPanel(glCapabilities);
                canvas.addGLEventListener(this);
                canvas.setPreferredSize(new Dimension(400, 300));
                
                // Add mouse listeners for rotation
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
                
                // Start animator
                animator = new Animator(canvas);
                animator.setUpdateFPSFrames(20, null);
                animator.start();
                
            } catch (Exception e) {
                System.err.println("Error creating 3D view: " + e.getMessage());
                e.printStackTrace();
                add(new JLabel("Error creating 3D view: " + e.getMessage()), BorderLayout.CENTER);
            }
        }
        
        public void setModel(Model3D model) {
            this.model = model;
            
            // Reset view for new model
            if (model != null) {
                rotX = 20.0f;
                rotY = 30.0f;
                zoom = -5.0f;
            }
            
            if (canvas != null) {
                canvas.repaint();
            }
        }
        
        public void stopAnimator() {
            if (animator != null && animator.isAnimating()) {
                animator.stop();
            }
        }
        
        // GLEventListener implementation
        @Override
        public void init(GLAutoDrawable drawable) {
            GL2 gl = drawable.getGL().getGL2();
            gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
            gl.glEnable(GL.GL_DEPTH_TEST);
            
            // Basic lighting
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glEnable(GL2.GL_LIGHT0);
            
            float[] lightPos = {1.0f, 1.0f, 1.0f, 0.0f};
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPos, 0);
            
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
            gl.glEnable(GL2.GL_NORMALIZE);
        }
        
        @Override
        public void dispose(GLAutoDrawable drawable) {
            // Nothing to dispose
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
            
            // Enable lighting for solid rendering
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glEnable(GL2.GL_LIGHT0);
            
            // Draw model if available
            if (model != null && !model.vertices.isEmpty()) {
                // Calculate model center for proper centering
                float centerX = (model.minX + model.maxX) / 2;
                float centerY = (model.minY + model.maxY) / 2;
                float centerZ = (model.minZ + model.maxZ) / 2;
                gl.glTranslatef(-centerX, -centerY, -centerZ);
                
                // Draw model - both solid and wireframe
                renderModelSolid(gl, model);
                
                // Add wireframe overlay
                gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
                gl.glPolygonOffset(1.0f, 1.0f);
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
                gl.glLineWidth(0.8f);
                gl.glColor3f(0.2f, 0.2f, 0.2f); // Dark gray wireframe
                gl.glDisable(GL2.GL_LIGHTING);
                renderModelWireframe(gl, model);
                gl.glEnable(GL2.GL_LIGHTING);
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
                gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
            } else {
                // Draw coordinate axes
                gl.glDisable(GL2.GL_LIGHTING);
                drawAxes(gl);
            }
            
            // Disable lighting when done
            gl.glDisable(GL2.GL_LIGHTING);
        }
        
        /**
         * Renders a model in solid mode with lighting
         */
        private void renderModelSolid(GL2 gl, Model3D model) {
            // Set default material properties
            float[] ambient = {0.4f, 0.4f, 0.4f, 1.0f};
            float[] diffuse = {0.7f, 0.7f, 0.7f, 1.0f};
            float[] specular = {0.3f, 0.3f, 0.3f, 1.0f};
            float shininess = 32.0f;
            
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
            
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
            gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
            
            gl.glColor3f(0.7f, 0.7f, 0.7f); // Light gray default color
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
            
            for (SoftwareRenderer.Face face : model.faces) {
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
                    if (face.normalIndices != null && face.normalIndices[i] >= 0 && 
                        face.normalIndices[i] < model.normals.size() / 3) {
                        
                        int normIndex = face.normalIndices[i];
                        gl.glNormal3f(
                            model.normals.get(normIndex * 3), 
                            model.normals.get(normIndex * 3 + 1), 
                            model.normals.get(normIndex * 3 + 2)
                        );
                    }
                    
                    // Set vertex
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
         * Renders a model in wireframe mode
         */
        private void renderModelWireframe(GL2 gl, Model3D model) {
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
            
            for (SoftwareRenderer.Face face : model.faces) {
                if (face.vertexIndices.length == 3) {
                    gl.glBegin(GL.GL_TRIANGLES);
                } else if (face.vertexIndices.length == 4) {
                    gl.glBegin(GL2.GL_QUADS);
                } else {
                    gl.glBegin(GL2.GL_POLYGON);
                }
                
                for (int i = 0; i < face.vertexIndices.length; i++) {
                    int vertIndex = face.vertexIndices[i];
                    
                    // Set vertex
                    gl.glVertex3f(
                        model.vertices.get(vertIndex * 3),
                        model.vertices.get(vertIndex * 3 + 1),
                        model.vertices.get(vertIndex * 3 + 2)
                    );
                }
                
                gl.glEnd();
            }
            
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
        
        /**
         * Draws coordinate axes
         */
        private void drawAxes(GL2 gl) {
            gl.glBegin(GL.GL_LINES);
            
            // X axis - red
            gl.glColor3f(1.0f, 0.0f, 0.0f);
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(1.0f, 0.0f, 0.0f);
            
            // Y axis - green
            gl.glColor3f(0.0f, 1.0f, 0.0f);
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(0.0f, 1.0f, 0.0f);
            
            // Z axis - blue
            gl.glColor3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(0.0f, 0.0f, 0.0f);
            gl.glVertex3f(0.0f, 0.0f, 1.0f);
            
            gl.glEnd();
        }
    }
    
    /**
     * 2D view component for displaying model projections
     */
    private class TwoDView extends JPanel {
        private static final long serialVersionUID = 1L;
        
        private Model3D model;
        private int viewMode = 0; // 0=top, 1=front, 2=side, 3=multi
        private JTabbedPane viewTabs;
        private JPanel multiView;
        private JPanel topView;
        private JPanel frontView;
        private JPanel sideView;
        
        public TwoDView() {
            setLayout(new BorderLayout());
            setBorder(BorderFactory.createTitledBorder("2D View"));
            
            // Create view panels
            topView = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    renderTopView((Graphics2D)g);
                }
            };
            topView.setBackground(Color.BLACK);
            
            frontView = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    renderFrontView((Graphics2D)g);
                }
            };
            frontView.setBackground(Color.BLACK);
            
            sideView = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    renderSideView((Graphics2D)g);
                }
            };
            sideView.setBackground(Color.BLACK);
            
            multiView = new JPanel(new GridLayout(2, 2));
            multiView.add(topView);
            multiView.add(sideView);
            multiView.add(frontView);
            multiView.add(new JPanel()); // Empty panel
            
            // Create tabbed pane
            viewTabs = new JTabbedPane();
            viewTabs.addTab("Top View", topView);
            viewTabs.addTab("Front View", frontView);
            viewTabs.addTab("Side View", sideView);
            viewTabs.addTab("Multi View", multiView);
            
            viewTabs.addChangeListener(e -> {
                viewMode = viewTabs.getSelectedIndex();
                repaint();
            });
            
            add(viewTabs, BorderLayout.CENTER);
        }
        
        public void setModel(Model3D model) {
            this.model = model;
            repaint();
        }
        
        /**
         * Renders top view (X-Z plane)
         */
        private void renderTopView(Graphics2D g) {
            if (model == null || model.vertices.isEmpty()) {
                drawEmptyViewMessage(g, "Top View (X-Z)");
                return;
            }
            
            g.setColor(Color.GREEN);
            
            int width = topView.getWidth();
            int height = topView.getHeight();
            
            // Calculate scale and offset for centered view
            float modelWidth = model.maxX - model.minX;
            float modelDepth = model.maxZ - model.minZ;
            float scale = Math.min(width / modelWidth, height / modelDepth) * 0.8f;
            
            // Draw model outline from top view
            for (SoftwareRenderer.Face face : model.faces) {
                for (int i = 0; i < face.vertexIndices.length; i++) {
                    int curr = face.vertexIndices[i];
                    int next = face.vertexIndices[(i + 1) % face.vertexIndices.length];
                    
                    float x1 = model.vertices.get(curr * 3);
                    float z1 = model.vertices.get(curr * 3 + 2);
                    float x2 = model.vertices.get(next * 3);
                    float z2 = model.vertices.get(next * 3 + 2);
                    
                    // Convert to screen coordinates
                    int sx1 = (int)(width / 2 + (x1 - (model.minX + model.maxX) / 2) * scale);
                    int sz1 = (int)(height / 2 + (z1 - (model.minZ + model.maxZ) / 2) * scale);
                    int sx2 = (int)(width / 2 + (x2 - (model.minX + model.maxX) / 2) * scale);
                    int sz2 = (int)(height / 2 + (z2 - (model.minZ + model.maxZ) / 2) * scale);
                    
                    g.drawLine(sx1, sz1, sx2, sz2);
                }
            }
            
            // Draw axes
            g.setColor(Color.RED);
            g.drawLine(width / 2, height / 2, width / 2 + 50, height / 2); // X-axis
            g.drawString("X", width / 2 + 55, height / 2);
            
            g.setColor(Color.BLUE);
            g.drawLine(width / 2, height / 2, width / 2, height / 2 - 50); // Z-axis
            g.drawString("Z", width / 2, height / 2 - 55);
        }
        
        /**
         * Renders front view (X-Y plane)
         */
        private void renderFrontView(Graphics2D g) {
            if (model == null || model.vertices.isEmpty()) {
                drawEmptyViewMessage(g, "Front View (X-Y)");
                return;
            }
            
            g.setColor(Color.GREEN);
            
            int width = frontView.getWidth();
            int height = frontView.getHeight();
            
            // Calculate scale and offset for centered view
            float modelWidth = model.maxX - model.minX;
            float modelHeight = model.maxY - model.minY;
            float scale = Math.min(width / modelWidth, height / modelHeight) * 0.8f;
            
            // Draw model outline from front view
            for (SoftwareRenderer.Face face : model.faces) {
                for (int i = 0; i < face.vertexIndices.length; i++) {
                    int curr = face.vertexIndices[i];
                    int next = face.vertexIndices[(i + 1) % face.vertexIndices.length];
                    
                    float x1 = model.vertices.get(curr * 3);
                    float y1 = model.vertices.get(curr * 3 + 1);
                    float x2 = model.vertices.get(next * 3);
                    float y2 = model.vertices.get(next * 3 + 1);
                    
                    // Convert to screen coordinates
                    int sx1 = (int)(width / 2 + (x1 - (model.minX + model.maxX) / 2) * scale);
                    int sy1 = (int)(height / 2 - (y1 - (model.minY + model.maxY) / 2) * scale);
                    int sx2 = (int)(width / 2 + (x2 - (model.minX + model.maxX) / 2) * scale);
                    int sy2 = (int)(height / 2 - (y2 - (model.minY + model.maxY) / 2) * scale);
                    
                    g.drawLine(sx1, sy1, sx2, sy2);
                }
            }
            
            // Draw axes
            g.setColor(Color.RED);
            g.drawLine(width / 2, height / 2, width / 2 + 50, height / 2); // X-axis
            g.drawString("X", width / 2 + 55, height / 2);
            
            g.setColor(Color.GREEN);
            g.drawLine(width / 2, height / 2, width / 2, height / 2 - 50); // Y-axis
            g.drawString("Y", width / 2, height / 2 - 55);
        }
        
        /**
         * Renders side view (Y-Z plane)
         */
        private void renderSideView(Graphics2D g) {
            if (model == null || model.vertices.isEmpty()) {
                drawEmptyViewMessage(g, "Side View (Z-Y)");
                return;
            }
            
            g.setColor(Color.GREEN);
            
            int width = sideView.getWidth();
            int height = sideView.getHeight();
            
            // Calculate scale and offset for centered view
            float modelDepth = model.maxZ - model.minZ;
            float modelHeight = model.maxY - model.minY;
            float scale = Math.min(width / modelDepth, height / modelHeight) * 0.8f;
            
            // Draw model outline from side view
            for (SoftwareRenderer.Face face : model.faces) {
                for (int i = 0; i < face.vertexIndices.length; i++) {
                    int curr = face.vertexIndices[i];
                    int next = face.vertexIndices[(i + 1) % face.vertexIndices.length];
                    
                    float z1 = model.vertices.get(curr * 3 + 2);
                    float y1 = model.vertices.get(curr * 3 + 1);
                    float z2 = model.vertices.get(next * 3 + 2);
                    float y2 = model.vertices.get(next * 3 + 1);
                    
                    // Convert to screen coordinates
                    int sz1 = (int)(width / 2 + (z1 - (model.minZ + model.maxZ) / 2) * scale);
                    int sy1 = (int)(height / 2 - (y1 - (model.minY + model.maxY) / 2) * scale);
                    int sz2 = (int)(width / 2 + (z2 - (model.minZ + model.maxZ) / 2) * scale);
                    int sy2 = (int)(height / 2 - (y2 - (model.minY + model.maxY) / 2) * scale);
                    
                    g.drawLine(sz1, sy1, sz2, sy2);
                }
            }
            
            // Draw axes
            g.setColor(Color.BLUE);
            g.drawLine(width / 2, height / 2, width / 2 + 50, height / 2); // Z-axis
            g.drawString("Z", width / 2 + 55, height / 2);
            
            g.setColor(Color.GREEN);
            g.drawLine(width / 2, height / 2, width / 2, height / 2 - 50); // Y-axis
            g.drawString("Y", width / 2, height / 2 - 55);
        }
        
        /**
         * Draws a message when no model is available
         */
        private void drawEmptyViewMessage(Graphics2D g, String viewName) {
            g.setColor(Color.WHITE);
            String message = "No model selected - " + viewName;
            int width = getWidth();
            int height = getHeight();
            int strWidth = g.getFontMetrics().stringWidth(message);
            g.drawString(message, (width - strWidth) / 2, height / 2);
        }
    }
} 