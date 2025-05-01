package com.modelviewer;

import com.jogamp.opengl.awt.GLJPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.RenderingHints;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.JComponent;

/**
 * Main application form for the 3D Software Renderer
 */
public class SoftwareRendererForm extends JFrame {
    
    // JOGL integration
    private JOGLIntegration joglIntegration;
    private GLJPanel renderPanel;
    
    // UI Components
    private JMenuBar menuBar;
    private JTabbedPane tabbedPane;
    private JPanel mainPanel;
    private JPanel controlPanel;
    
    // Model data
    private Model3D currentModel;
    private DefaultListModel<Model3D> roomModelListModel;
    private JList<Model3D> roomModelList;
    private List<Model3D> roomModels = new ArrayList<>();
    
    // Mouse interaction state
    private int lastMouseX;
    private int lastMouseY;
    private boolean mouseDown = false;
    
    // Enhanced UI Constants
    private static final Color HEADER_COLOR = new Color(33, 37, 41);      // Darker header
    private static final Color PANEL_COLOR = new Color(248, 249, 250);    // Lighter panel background
    private static final Color ACCENT_COLOR = new Color(0, 123, 255);     // Bootstrap primary blue
    private static final Color TEXT_COLOR = new Color(33, 37, 41);        // Bootstrap dark
    private static final Color SUCCESS_COLOR = new Color(40, 167, 69);    // Bootstrap success
    private static final Color DANGER_COLOR = new Color(220, 53, 69);     // Bootstrap danger
    private static final Color WARNING_COLOR = new Color(255, 193, 7);    // Bootstrap warning
    private static final Color INFO_COLOR = new Color(23, 162, 184);      // Bootstrap info
    private static final Color LIGHT_ACCENT = new Color(240, 242, 245);   // Light blue-gray
    private static final Color BORDER_COLOR = new Color(222, 226, 230);   // Bootstrap border
    
    private static final Font HEADER_FONT = new Font("Segoe UI", Font.BOLD, 15);
    private static final Font REGULAR_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final Font SMALL_FONT = new Font("Segoe UI", Font.PLAIN, 12);
    
    private static final int BORDER_RADIUS = 8;
    private static final int PANEL_SPACING = 15;
    private static final int CONTROL_HEIGHT = 30;
    private static final Dimension BUTTON_SIZE = new Dimension(100, CONTROL_HEIGHT);
    private static final Dimension SPINNER_SIZE = new Dimension(80, CONTROL_HEIGHT);
    
    private static final Border PANEL_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(PANEL_SPACING, PANEL_SPACING, PANEL_SPACING, PANEL_SPACING)
    );
    
    private static final Border CONTROL_BORDER = BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(BORDER_COLOR, 1),
        BorderFactory.createEmptyBorder(5, 8, 5, 8)
    );
    
    // Interface for updating model info
    private interface ModelInfoUpdater {
        void update(Model3D model);
    }
    
    private ModelInfoUpdater updateModelInfo;
    
    /**
     * Constructor
     */
    public SoftwareRendererForm() {
        initComponents();
        applyTheme();
    }
    
    /**
     * Apply a custom theme to the UI components
     */
    private void applyTheme() {
        // Set custom fonts
        UIManager.put("Label.font", REGULAR_FONT);
        UIManager.put("Button.font", REGULAR_FONT);
        UIManager.put("TabbedPane.font", HEADER_FONT);
        UIManager.put("TextField.font", REGULAR_FONT);
        UIManager.put("TitledBorder.font", HEADER_FONT);
        UIManager.put("ComboBox.font", REGULAR_FONT);
        UIManager.put("CheckBox.font", REGULAR_FONT);
        
        // Set custom colors
        UIManager.put("Panel.background", PANEL_COLOR);
        UIManager.put("Button.background", ACCENT_COLOR);
        UIManager.put("Button.foreground", Color.WHITE);
        UIManager.put("TabbedPane.selected", ACCENT_COLOR);
        UIManager.put("TabbedPane.background", PANEL_COLOR);
        UIManager.put("TabbedPane.foreground", TEXT_COLOR);
        UIManager.put("Label.foreground", TEXT_COLOR);
        
        // Update components with new UI settings
        SwingUtilities.updateComponentTreeUI(this);
    }
    
    /**
     * Initialize UI components
     */
    private void initComponents() {
        // Set up window
        setTitle("3D Model Viewer");
        setSize(1200, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Set modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        // Initialize JOGL integration
        joglIntegration = new JOGLIntegration();
        
        // Create main UI layout
        mainPanel = new JPanel(new BorderLayout(10, 10)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Create subtle gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(245, 245, 250),
                    0, getHeight(), new Color(230, 230, 240)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        mainPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        getContentPane().add(mainPanel);
        
        // Create menu bar
        createMenuBar();
        
        // Create 3D rendering panel
        createRenderPanel();
        
        // Create control panel with tabs
        createControlPanel();
        
        // Set up window close handler
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanupResources();
            }
        });
    }
    
    /**
     * Creates the application menu bar
     */
    private void createMenuBar() {
        menuBar = new JMenuBar();
        menuBar.setBackground(HEADER_COLOR);
        menuBar.setBorder(BorderFactory.createEmptyBorder());
        
        // File menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setForeground(Color.WHITE);
        fileMenu.setFont(HEADER_FONT);
        
        JMenuItem openItem = new JMenuItem("Open Model...");
        openItem.setFont(REGULAR_FONT);
        openItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openModelFile();
            }
        });
        
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.setFont(REGULAR_FONT);
        exitItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                cleanupResources();
                System.exit(0);
            }
        });
        
        fileMenu.add(openItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);
        
        // View menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setForeground(Color.WHITE);
        viewMenu.setFont(HEADER_FONT);
        
        JMenuItem resetViewItem = new JMenuItem("Reset View");
        resetViewItem.setFont(REGULAR_FONT);
        resetViewItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.resetView();
            }
        });
        
        JCheckBoxMenuItem wireframeItem = new JCheckBoxMenuItem("Wireframe Mode");
        wireframeItem.setFont(REGULAR_FONT);
        wireframeItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.setWireframeMode(wireframeItem.isSelected());
            }
        });
        
        viewMenu.add(resetViewItem);
        viewMenu.add(wireframeItem);
        
        // Help menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setForeground(Color.WHITE);
        helpMenu.setFont(HEADER_FONT);
        
        JMenuItem aboutItem = new JMenuItem("About...");
        aboutItem.setFont(REGULAR_FONT);
        aboutItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(SoftwareRendererForm.this,
                        "3D Model Viewer\n" +
                        "Version 1.0\n\n" +
                        "Uses JOGL for software rendering of 3D models.\n" +
                        "Supports OBJ files with materials.",
                        "About", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        
        helpMenu.add(aboutItem);
        
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(helpMenu);
        
        setJMenuBar(menuBar);
    }
    
    /**
     * Creates the 3D rendering panel
     */
    private void createRenderPanel() {
        // Create JOGL panel
        renderPanel = joglIntegration.createGLPanel();
        renderPanel.setPreferredSize(new Dimension(800, 600));
        
        // Add mouse listeners for camera control
        renderPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                lastMouseX = e.getX();
                lastMouseY = e.getY();
                mouseDown = true;
            }
            
            @Override
            public void mouseReleased(MouseEvent e) {
                mouseDown = false;
            }
        });
        
        renderPanel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (mouseDown) {
                    int deltaX = e.getX() - lastMouseX;
                    int deltaY = e.getY() - lastMouseY;
                    
                    // Update view rotation
                    float rotX = joglIntegration.getRotX() + deltaY * 0.5f;
                    float rotY = joglIntegration.getRotY() + deltaX * 0.5f;
                    
                    joglIntegration.setCameraRotation(rotX, rotY);
                    
                    lastMouseX = e.getX();
                    lastMouseY = e.getY();
                }
            }
        });
        
        renderPanel.addMouseWheelListener(new MouseWheelListener() {
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                // Update zoom level
                float zoom = joglIntegration.getZoom();
                zoom += e.getWheelRotation() * 0.5f;
                joglIntegration.setCameraZoom(zoom);
            }
        });
        
        // Create a wrapper panel with stylish border
        JPanel renderWrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Subtle gradient background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(240, 240, 245),
                    0, getHeight(), new Color(225, 225, 235)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
            }
        };
        
        // Create header panel with title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(HEADER_COLOR);
        headerPanel.setPreferredSize(new Dimension(30, 30));
        JLabel titleLabel = new JLabel("3D View", SwingConstants.CENTER);
        titleLabel.setFont(HEADER_FONT);
        titleLabel.setForeground(Color.WHITE);
        headerPanel.add(titleLabel, BorderLayout.CENTER);
        
        // Add components to render wrapper
        renderWrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR, 2, true),
            BorderFactory.createEmptyBorder(1, 1, 1, 1)
        ));
        renderWrapper.add(headerPanel, BorderLayout.NORTH);
        renderWrapper.add(renderPanel, BorderLayout.CENTER);
        
        // Add navigation hint panel
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        navPanel.setOpaque(false);
        JLabel navLabel = new JLabel("Drag to rotate • Scroll to zoom");
        navLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        navLabel.setForeground(new Color(100, 100, 120));
        navPanel.add(navLabel);
        renderWrapper.add(navPanel, BorderLayout.SOUTH);
        
        mainPanel.add(renderWrapper, BorderLayout.CENTER);
    }
    
    /**
     * Creates the control panel with tabs
     */
    private void createControlPanel() {
        // Create tabbed pane with custom styling
        tabbedPane = new JTabbedPane(JTabbedPane.TOP) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Modern gradient for tabs background
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(248, 249, 250),
                    0, 30, new Color(233, 236, 239)
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), 30);
                g2d.dispose();
            }
        };
        
        // Enhanced tab design
        tabbedPane.setFont(HEADER_FONT);
        tabbedPane.setBackground(PANEL_COLOR);
        tabbedPane.setForeground(TEXT_COLOR);
        tabbedPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        // Create tabs with modern styling
        JPanel modelPanel = createModelPanel();
        modelPanel.setBorder(PANEL_BORDER);
        modelPanel.setBackground(PANEL_COLOR);
        
        JPanel planningPanel = createRoomPlanningPanel();
        planningPanel.setBorder(PANEL_BORDER);
        planningPanel.setBackground(PANEL_COLOR);
        
        JPanel displayPanel = createDisplayOptionsPanel();
        displayPanel.setBorder(PANEL_BORDER);
        displayPanel.setBackground(PANEL_COLOR);
        
        tabbedPane.addTab("Model", modelPanel);
        tabbedPane.addTab("Room Planning", planningPanel);
        tabbedPane.addTab("Display Options", displayPanel);
        
        // Modern control panel design
        controlPanel = new JPanel(new BorderLayout());
        controlPanel.setPreferredSize(new Dimension(350, 600));
        controlPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(ACCENT_COLOR, 2, true),
            BorderFactory.createEmptyBorder(0, 0, 0, 0)
        ));
        controlPanel.setBackground(PANEL_COLOR);
        controlPanel.add(tabbedPane, BorderLayout.CENTER);
        
        mainPanel.add(controlPanel, BorderLayout.EAST);
    }
    
    /**
     * Creates a styled titled border
     */
    private TitledBorder createStyledTitledBorder(String title) {
        TitledBorder border = BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 220), 1, true),
            title
        );
        border.setTitleFont(HEADER_FONT);
        border.setTitleColor(ACCENT_COLOR);
        return border;
    }
    
    /**
     * Creates a styled button
     */
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                
                // Draw modern button background
                if (getModel().isPressed()) {
                    g2d.setColor(ACCENT_COLOR.darker());
                } else if (getModel().isRollover()) {
                    g2d.setColor(ACCENT_COLOR.brighter());
                } else {
                    g2d.setColor(ACCENT_COLOR);
                }
                
                // Draw rounded rectangle background
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), BORDER_RADIUS, BORDER_RADIUS);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(REGULAR_FONT);
                FontMetrics fm = g2d.getFontMetrics();
                int textX = (getWidth() - fm.stringWidth(getText())) / 2;
                int textY = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2d.drawString(getText(), textX, textY);
                
                g2d.dispose();
            }
        };
        
        // Modern button styling
        button.setPreferredSize(BUTTON_SIZE);
        button.setBorder(BorderFactory.createEmptyBorder(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setFont(REGULAR_FONT);
        
        return button;
    }
    
    /**
     * Creates the model control panel
     */
    private JPanel createModelPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(PANEL_COLOR);
        
        // Model Information Section
        JPanel modelInfoPanel = new JPanel(new GridBagLayout());
        modelInfoPanel.setBackground(PANEL_COLOR);
        modelInfoPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "Model Information",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                HEADER_FONT,
                TEXT_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Position controls
        gbc.gridx = 0; gbc.gridy = 0;
        modelInfoPanel.add(createStyledLabel("Position X:"), gbc);
        gbc.gridx = 1;
        modelInfoPanel.add(createStyledSpinner(new SpinnerNumberModel(0.0, -100.0, 100.0, 0.1)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 1;
        modelInfoPanel.add(createStyledLabel("Position Y:"), gbc);
        gbc.gridx = 1;
        modelInfoPanel.add(createStyledSpinner(new SpinnerNumberModel(0.0, -100.0, 100.0, 0.1)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 2;
        modelInfoPanel.add(createStyledLabel("Position Z:"), gbc);
        gbc.gridx = 1;
        modelInfoPanel.add(createStyledSpinner(new SpinnerNumberModel(0.0, -100.0, 100.0, 0.1)), gbc);
        
        // Rotation and Scale
        gbc.gridx = 0; gbc.gridy = 3;
        modelInfoPanel.add(createStyledLabel("Rotation:"), gbc);
        gbc.gridx = 1;
        modelInfoPanel.add(createStyledSpinner(new SpinnerNumberModel(0.0, 0.0, 360.0, 1.0)), gbc);
        
        gbc.gridx = 0; gbc.gridy = 4;
        modelInfoPanel.add(createStyledLabel("Scale:"), gbc);
        gbc.gridx = 1;
        modelInfoPanel.add(createStyledSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1)), gbc);
        
        // Add model info panel with some padding
        panel.add(Box.createVerticalStrut(10));
        panel.add(modelInfoPanel);
        panel.add(Box.createVerticalStrut(15));
        
        // File Controls Section
        JPanel fileControlsPanel = new JPanel(new GridBagLayout());
        fileControlsPanel.setBackground(PANEL_COLOR);
        fileControlsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "File Controls",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                HEADER_FONT,
                TEXT_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Load/Save buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonPanel.setBackground(PANEL_COLOR);
        
        JButton loadButton = createStyledButton("Load Model");
        loadButton.addActionListener(e -> openModelFile());
        
        JButton saveButton = createStyledButton("Save Model");
        saveButton.addActionListener(e -> {
            // Save functionality
        });
        
        buttonPanel.add(loadButton);
        buttonPanel.add(saveButton);
        
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        fileControlsPanel.add(buttonPanel, gbc);
        
        // Add file controls panel
        panel.add(fileControlsPanel);
        
        // Display Options Section
        JPanel displayOptionsPanel = new JPanel(new GridBagLayout());
        displayOptionsPanel.setBackground(PANEL_COLOR);
        displayOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(BORDER_COLOR),
                "Display Options",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                HEADER_FONT,
                TEXT_COLOR
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        // Add checkboxes with modern styling
        JCheckBox wireframeCheckbox = new JCheckBox("Wireframe Mode");
        wireframeCheckbox.setFont(REGULAR_FONT);
        wireframeCheckbox.setBackground(PANEL_COLOR);
        wireframeCheckbox.setForeground(TEXT_COLOR);
        
        JCheckBox texturesCheckbox = new JCheckBox("Show Textures");
        texturesCheckbox.setFont(REGULAR_FONT);
        texturesCheckbox.setBackground(PANEL_COLOR);
        texturesCheckbox.setForeground(TEXT_COLOR);
        
        gbc.gridx = 0; gbc.gridy = 0;
        displayOptionsPanel.add(wireframeCheckbox, gbc);
        gbc.gridy = 1;
        displayOptionsPanel.add(texturesCheckbox, gbc);
        
        // Add display options panel
        panel.add(displayOptionsPanel);
        
        // Add final padding
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Creates the room planning panel
     */
    private JPanel createRoomPlanningPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Room display toggle
        JCheckBox showRoomCheckbox = new JCheckBox("Show Room");
        showRoomCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.setShowRoom(showRoomCheckbox.isSelected());
            }
        });
        
        // Room dimensions
        JPanel dimensionsPanel = new JPanel();
        dimensionsPanel.setLayout(new BoxLayout(dimensionsPanel, BoxLayout.Y_AXIS));
        dimensionsPanel.setBorder(BorderFactory.createTitledBorder("Room Dimensions"));
        
        // Width control
        JPanel widthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        widthPanel.add(new JLabel("Width:"));
        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 20.0, 0.1));
        widthSpinner.setPreferredSize(new Dimension(70, 25));
        widthPanel.add(widthSpinner);
        
        // Height control
        JPanel heightPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        heightPanel.add(new JLabel("Height:"));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(3.0, 1.0, 10.0, 0.1));
        heightSpinner.setPreferredSize(new Dimension(70, 25));
        heightPanel.add(heightSpinner);
        
        // Length control
        JPanel lengthPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        lengthPanel.add(new JLabel("Length:"));
        JSpinner lengthSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 20.0, 0.1));
        lengthSpinner.setPreferredSize(new Dimension(70, 25));
        lengthPanel.add(lengthSpinner);
        
        // Apply button for dimensions
        JButton applyDimensionsButton = new JButton("Apply Dimensions");
        applyDimensionsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                float width = ((Number)widthSpinner.getValue()).floatValue();
                float height = ((Number)heightSpinner.getValue()).floatValue();
                float length = ((Number)lengthSpinner.getValue()).floatValue();
                
                joglIntegration.setRoomProperties(
                    showRoomCheckbox.isSelected(),
                    width,
                    height,
                    length
                );
            }
        });
        
        dimensionsPanel.add(widthPanel);
        dimensionsPanel.add(heightPanel);
        dimensionsPanel.add(lengthPanel);
        dimensionsPanel.add(Box.createVerticalStrut(5));
        dimensionsPanel.add(applyDimensionsButton);
        
        // Room colors panel
        JPanel colorsPanel = new JPanel();
        colorsPanel.setLayout(new BoxLayout(colorsPanel, BoxLayout.Y_AXIS));
        colorsPanel.setBorder(BorderFactory.createTitledBorder("Room Colors"));
        
        // Wall color
        JPanel wallColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        wallColorPanel.add(new JLabel("Wall Color:"));
        JButton wallColorButton = new JButton("   ");
        wallColorButton.setBackground(new Color(220, 220, 220));
        wallColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(
                    SoftwareRendererForm.this,
                    "Select Wall Color",
                    wallColorButton.getBackground()
                );
                
                if (newColor != null) {
                    wallColorButton.setBackground(newColor);
                    joglIntegration.setWallColor(newColor);
                }
            }
        });
        wallColorPanel.add(wallColorButton);
        
        // Floor color
        JPanel floorColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        floorColorPanel.add(new JLabel("Floor Color:"));
        JButton floorColorButton = new JButton("   ");
        floorColorButton.setBackground(new Color(180, 140, 100));
        floorColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(
                    SoftwareRendererForm.this,
                    "Select Floor Color",
                    floorColorButton.getBackground()
                );
                
                if (newColor != null) {
                    floorColorButton.setBackground(newColor);
                    joglIntegration.setFloorColor(newColor);
                }
            }
        });
        floorColorPanel.add(floorColorButton);
        
        // Ceiling color
        JPanel ceilingColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ceilingColorPanel.add(new JLabel("Ceiling Color:"));
        JButton ceilingColorButton = new JButton("   ");
        ceilingColorButton.setBackground(new Color(240, 240, 240));
        ceilingColorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Color newColor = JColorChooser.showDialog(
                    SoftwareRendererForm.this,
                    "Select Ceiling Color",
                    ceilingColorButton.getBackground()
                );
                
                if (newColor != null) {
                    ceilingColorButton.setBackground(newColor);
                    joglIntegration.setCeilingColor(newColor);
                }
            }
        });
        ceilingColorPanel.add(ceilingColorButton);
        
        // Apply colors button
        JButton applyColorsButton = new JButton("Apply Colors");
        applyColorsButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.setRoomColors(
                    wallColorButton.getBackground(),
                    floorColorButton.getBackground(),
                    ceilingColorButton.getBackground()
                );
            }
        });
        
        colorsPanel.add(wallColorPanel);
        colorsPanel.add(floorColorPanel);
        colorsPanel.add(ceilingColorPanel);
        colorsPanel.add(Box.createVerticalStrut(5));
        colorsPanel.add(applyColorsButton);
        
        // Room models panel
        JPanel modelsPanel = new JPanel();
        modelsPanel.setLayout(new BoxLayout(modelsPanel, BoxLayout.Y_AXIS));
        modelsPanel.setBorder(BorderFactory.createTitledBorder("Room Models"));
        
        // Models list
        roomModelListModel = new DefaultListModel<>();
        roomModelList = new JList<>(roomModelListModel);
        JScrollPane modelScrollPane = new JScrollPane(roomModelList);
        modelScrollPane.setPreferredSize(new Dimension(250, 150));
        
        roomModelList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    Model3D selectedModel = roomModelList.getSelectedValue();
                    if (selectedModel != null) {
                        updateModelInfo.update(selectedModel);
                    }
                }
            }
        });
        
        // Buttons for model management
        JPanel modelButtonsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        // Create styled buttons with custom colors
        JButton addModelButton = createStyledButton("Add Current");
        addModelButton.setBackground(new Color(46, 204, 113));     // Current: Green
        addModelButton.setForeground(Color.WHITE);
        
        JButton removeModelButton = createStyledButton("Remove");
        removeModelButton.setBackground(new Color(231, 76, 60));   // Current: Red
        removeModelButton.setForeground(Color.WHITE);
        
        JButton duplicateButton = createStyledButton("Duplicate");
        duplicateButton.setBackground(new Color(52, 152, 219));    // Current: Blue
        duplicateButton.setForeground(Color.WHITE);
        
        addModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (currentModel != null) {
                    Model3D copy = currentModel.duplicate();
                    roomModels.add(copy);
                    roomModelListModel.addElement(copy);
                    joglIntegration.setRoomModels(roomModels);
                }
            }
        });
        
        removeModelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = roomModelList.getSelectedIndex();
                if (selectedIndex != -1) {
                    roomModels.remove(selectedIndex);
                    roomModelListModel.remove(selectedIndex);
                    joglIntegration.setRoomModels(roomModels);
                }
            }
        });
        
        duplicateButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedIndex = roomModelList.getSelectedIndex();
                if (selectedIndex != -1) {
                    Model3D original = roomModels.get(selectedIndex);
                    Model3D copy = original.duplicate();
                    // Offset the copy slightly to make it visible
                    copy.x += 0.5f;
                    copy.z += 0.5f;
                    roomModels.add(copy);
                    roomModelListModel.addElement(copy);
                    joglIntegration.setRoomModels(roomModels);
                }
            }
        });
        
        modelButtonsPanel.add(addModelButton);
        modelButtonsPanel.add(removeModelButton);
        modelButtonsPanel.add(duplicateButton);
        
        modelsPanel.add(modelScrollPane);
        modelsPanel.add(modelButtonsPanel);
        
        // Add all components to the main panel
        panel.add(showRoomCheckbox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(dimensionsPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(colorsPanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(modelsPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Creates the display options panel
     */
    private JPanel createDisplayOptionsPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Wireframe mode
        JCheckBox wireframeCheckbox = new JCheckBox("Wireframe Mode");
        wireframeCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.setWireframeMode(wireframeCheckbox.isSelected());
            }
        });
        
        // Color override
        JPanel colorOverridePanel = new JPanel();
        colorOverridePanel.setLayout(new BoxLayout(colorOverridePanel, BoxLayout.Y_AXIS));
        colorOverridePanel.setBorder(BorderFactory.createTitledBorder("Color Override"));
        
        JCheckBox overrideCheckbox = new JCheckBox("Override Model Colors");
        
        JPanel colorButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        colorButtonPanel.add(new JLabel("Override Color:"));
        JButton colorButton = new JButton("   ");
        colorButton.setBackground(Color.RED);
        colorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
        Color newColor = JColorChooser.showDialog(
                    SoftwareRendererForm.this,
                    "Select Override Color",
                    colorButton.getBackground()
        );
        
        if (newColor != null) {
                    colorButton.setBackground(newColor);
                    joglIntegration.setColorOverride(
                        overrideCheckbox.isSelected(),
                        newColor
                    );
                }
            }
        });
        colorButtonPanel.add(colorButton);
        
        overrideCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.setColorOverride(
                    overrideCheckbox.isSelected(),
                    colorButton.getBackground()
                );
            }
        });
        
        colorOverridePanel.add(overrideCheckbox);
        colorOverridePanel.add(colorButtonPanel);
        
        // Camera controls panel
        JPanel cameraPanel = new JPanel();
        cameraPanel.setLayout(new BoxLayout(cameraPanel, BoxLayout.Y_AXIS));
        cameraPanel.setBorder(BorderFactory.createTitledBorder("Camera Controls"));
        
        JLabel cameraInstructions = new JLabel(
            "<html>Mouse controls:<br>" +
            "- Drag to rotate view<br>" +
            "- Scroll to zoom in/out</html>"
        );
        
        JButton resetViewButton = new JButton("Reset View");
        resetViewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                joglIntegration.resetView();
            }
        });
        
        cameraPanel.add(cameraInstructions);
        cameraPanel.add(Box.createVerticalStrut(10));
        cameraPanel.add(resetViewButton);
        
        // Add all components to the main panel
        panel.add(wireframeCheckbox);
        panel.add(Box.createVerticalStrut(10));
        panel.add(colorOverridePanel);
        panel.add(Box.createVerticalStrut(10));
        panel.add(cameraPanel);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    /**
     * Opens a model file dialog and loads the selected model
     */
    private void openModelFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Open 3D Model");
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files (*.obj)", "obj"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                // Load model
                File selectedFile = fileChooser.getSelectedFile();
                Model3D model = OBJLoader.loadOBJModel(selectedFile.getAbsolutePath());
                
                // Center model at origin
                float[] center = model.getCenter();
                float size = model.getSize();
                
                // Apply initial transformation
                model.x = 0;
                model.y = 0;
                model.z = 0;
                model.rotY = 0;
                
                // Scale model to reasonable size
                model.scale = 2.0f / size;
                
                // Update the model info
                currentModel = model;
                updateModelInfo.update(model);
                
                // Update renderer
                joglIntegration.setCurrentModel(model);
                
                // Reset view
                joglIntegration.resetView();
                
            } catch (IOException ex) {
        JOptionPane.showMessageDialog(this,
                    "Error loading model: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
                ex.printStackTrace();
            }
        }
    }
    
    /**
     * Cleans up resources when closing the application
     */
    private void cleanupResources() {
        // Stop animation and release resources
        if (joglIntegration != null) {
            joglIntegration.stopAnimation();
        }
    }
    
    /**
     * Main entry point
     */
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Failed to set system look and feel: " + e.getMessage());
        }
        
        // Launch application on EDT
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SoftwareRendererForm form = new SoftwareRendererForm();
                form.setVisible(true);
                
                // Start rendering
                form.joglIntegration.startAnimation();
            }
        });
    }
    
    // Helper method to create styled spinners
    private JSpinner createStyledSpinner(SpinnerNumberModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setPreferredSize(SPINNER_SIZE);
        spinner.setBorder(CONTROL_BORDER);
        spinner.setFont(REGULAR_FONT);
        
        // Style the editor component
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JSpinner.DefaultEditor spinnerEditor = (JSpinner.DefaultEditor)editor;
            spinnerEditor.getTextField().setBackground(Color.WHITE);
            spinnerEditor.getTextField().setFont(REGULAR_FONT);
            spinnerEditor.getTextField().setBorder(BorderFactory.createEmptyBorder(2, 5, 2, 5));
        }
        
        return spinner;
    }
    
    // Helper method to create styled text fields
    private JTextField createStyledTextField(int columns) {
        JTextField textField = new JTextField(columns);
        textField.setPreferredSize(new Dimension(columns * 10, CONTROL_HEIGHT));
        textField.setBorder(CONTROL_BORDER);
        textField.setFont(REGULAR_FONT);
        textField.setBackground(Color.WHITE);
        return textField;
    }
    
    // Helper method to create styled labels
    private JLabel createStyledLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(REGULAR_FONT);
        label.setForeground(TEXT_COLOR);
        return label;
    }
    
    // Helper method to create section headers
    private JLabel createSectionHeader(String text) {
        JLabel header = new JLabel(text);
        header.setFont(HEADER_FONT);
        header.setForeground(HEADER_COLOR);
        header.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        return header;
    }
} 