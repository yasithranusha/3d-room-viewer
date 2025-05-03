package com.modelviewer;

import java.awt.AlphaComposite;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
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
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.TitledBorder;
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

/**
 * A 3D model viewer using JOGL with software rendering
 * for systems without compatible OpenGL hardware acceleration
 */
public class SoftwareRenderer extends JFrame implements GLEventListener {
   private static final long serialVersionUID = 1L;
    private GLJPanel canvas;
    private Animator animator;

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
    private JCheckBox colorOverrideCheckbox;
    private JButton colorPickerButton;
    private Color userSelectedColor = Color.RED;
    private boolean useColorOverride = false;

    // Bounding box for auto-centering
    private float minX = Float.MAX_VALUE, maxX = Float.MIN_VALUE;
    private float minY = Float.MAX_VALUE, maxY = Float.MIN_VALUE;
    private float minZ = Float.MAX_VALUE, maxZ = Float.MIN_VALUE;

    // Render settings
    private boolean wireframeMode = false;

    // Room model properties
    private boolean showRoom = false;
    private float roomWidth = 5.0f;
    private float roomHeight = 3.0f;
    private float roomLength = 5.0f;
    private Color wallColor = new Color(220, 220, 220);
    private Color floorColor = new Color(180, 140, 100);
    private Color ceilingColor = new Color(240, 240, 240);
    private float wallTransparency = 1.0f; // 1.0 = fully opaque, 0.0 = fully transparent
    private float floorTransparency = 1.0f;
    private float ceilingTransparency = 1.0f;

    // Room file for save/load operations
    private File currentRoomFile = null;

    // Model library
    private List<Model3D> modelLibrary = new ArrayList<>();
    private File modelLibraryFile = new File("model_library.dat");

    // Room view mode
    private boolean use2DView = false; // Toggle between 3D and 2D (top-down) views

    private JSpinner roomWidthSpinner;
    private JSpinner roomHeightSpinner;
    private JSpinner roomLengthSpinner;
    private JButton wallColorButton;
    private JButton floorColorButton;
    private JButton ceilingColorButton;
    private JCheckBox showRoomCheckbox;
    private JSpinner wallTransparencySpinner;
    private JSpinner floorTransparencySpinner;
    private JSpinner ceilingTransparencySpinner;

    // Room planning fields
    private List<Model3D> roomModels = new ArrayList<>();
    private Model3D selectedModel = null;
    private int selectedModelIndex = -1;
    private DefaultListModel<String> modelsListModel = new DefaultListModel<>();
    private JList<String> modelsList;

    // Model position controls
    private JSpinner modelXSpinner;
    private JSpinner modelYSpinner;
    private JSpinner modelZSpinner;
    private JSpinner modelScaleSpinner;
    private JSpinner modelRotationSpinner;

    // Model color controls
    private JCheckBox modelColorCheckbox;
    private JButton modelColorButton;

    // Object manipulation
    private boolean isDraggingObject = false;
    private Point lastMousePosition = null;
    private float moveSpeed = 0.05f; // Increased speed for keyboard movement
    private float rotationSpeed = 5.0f; // Increased speed for keyboard rotation
    private boolean objectManipulationEnabled = true;
    private JCheckBoxMenuItem enableObjectManipulationItem;

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
        float[] ambient = { 0.2f, 0.2f, 0.2f, 1.0f };
        float[] diffuse = { 0.8f, 0.8f, 0.8f, 1.0f };
        float[] specular = { 1.0f, 1.0f, 1.0f, 1.0f };
        float shininess = 0.0f;
        String name;

        public Material(String name) {
            this.name = name;
        }
    }

    /**
     * Class representing a 3D model that can be placed in the room
     */
    static class Model3D {
        String name;
        List<Float> vertices = new ArrayList<>();
        List<Float> normals = new ArrayList<>();
        List<Float> textureCoords = new ArrayList<>();
        List<Face> faces = new ArrayList<>();
        Map<String, Material> materials = new HashMap<>();

        // Position and orientation
        float x, y, z; // Position
        float rotY; // Y-axis rotation (in degrees)
        float scale = 1.0f; // Scale factor

        // Custom color override
        boolean useCustomColor = false;
        Color customColor = new Color(180, 180, 180);

        // Bounding box
        float minX, maxX, minY, maxY, minZ, maxZ;

        public Model3D(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        SwingUtilities.invokeLater(() -> {
            // Show welcome screen first
            WelcomeScreen welcomeScreen = new WelcomeScreen();
            welcomeScreen.setVisible(true);

            // After welcome screen closes, show main application
            welcomeScreen.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    SoftwareRenderer renderer = new SoftwareRenderer();
                    renderer.setSize(1024, 768);
                    renderer.setLocationRelativeTo(null);
                    renderer.setVisible(true);
                    renderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                }
            });
        });
    }

   public SoftwareRenderer() {
        super("3D Model Viewer (Software Rendering)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);

        // Define common UI fonts and styles
        Font labelFont = new Font("Segoe UI", Font.PLAIN, 13);
        Font buttonFont = new Font("Segoe UI", Font.BOLD, 13);

        // Ensure wireframe mode is off by default
        wireframeMode = false;

        // Create menu bar
        createMenuBar();

        // Create the tabbed UI
        JTabbedPane tabbedPane = new JTabbedPane();

        // First tab - Model loading
        JPanel modelPanel = new JPanel(new BorderLayout(10, 10));
        modelPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        modelPanel.setBackground(new Color(51, 122, 183)); // Nice blue background like Room Planning

        // Create a container panel for the form elements
        JPanel modelFormPanel = new JPanel();
        modelFormPanel.setLayout(new BoxLayout(modelFormPanel, BoxLayout.Y_AXIS));
        modelFormPanel.setBackground(new Color(248, 250, 252)); // Light background color

        // File selection panel with modern styling
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.Y_AXIS));
        filePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Model Files",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        filePanel.setBackground(new Color(255, 255, 255));

        // OBJ file panel
        JPanel objPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        objPanel.setOpaque(false);
        JLabel objLabel = new JLabel("OBJ File:");
        objLabel.setFont(labelFont);
        objLabel.setForeground(new Color(40, 40, 40));
        objPanel.add(objLabel);

        objFileText = new JTextField(20);
        objFileText.setFont(labelFont);
        objFileText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        objPanel.add(objFileText);

        JButton objBrowseButton = createRoundedButton("Browse", new Color(52, 152, 219), buttonFont);
        // Ensure browse buttons have consistent width
        objBrowseButton.setPreferredSize(new Dimension(80, objBrowseButton.getPreferredSize().height));
        objBrowseButton.addActionListener(e -> browseObjFile());
        objPanel.add(objBrowseButton);
        filePanel.add(objPanel);

        // MTL file panel
        JPanel mtlPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        mtlPanel.setOpaque(false);
        JLabel mtlLabel = new JLabel("MTL File:");
        mtlLabel.setFont(labelFont);
        mtlLabel.setForeground(new Color(40, 40, 40));
        mtlPanel.add(mtlLabel);

        mtlFileText = new JTextField(20);
        mtlFileText.setFont(labelFont);
        mtlFileText.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        mtlPanel.add(mtlFileText);

        JButton mtlBrowseButton = createRoundedButton("Browse", new Color(52, 152, 219), buttonFont);
        // Ensure browse buttons have consistent width
        mtlBrowseButton.setPreferredSize(new Dimension(80, mtlBrowseButton.getPreferredSize().height));
        mtlBrowseButton.addActionListener(e -> browseMtlFile());
        mtlPanel.add(mtlBrowseButton);
        filePanel.add(mtlPanel);

        modelFormPanel.add(filePanel);
        modelFormPanel.add(Box.createVerticalStrut(15));

        // Render options panel with modern styling
        JPanel renderOptionsPanel = new JPanel();
        renderOptionsPanel.setLayout(new BoxLayout(renderOptionsPanel, BoxLayout.Y_AXIS));
        renderOptionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Render Options",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        renderOptionsPanel.setBackground(new Color(255, 255, 255));

        // Checkbox panel
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        checkboxPanel.setOpaque(false);

        wireframeCheckbox = new JCheckBox("Wireframe");
        wireframeCheckbox.setFont(labelFont);
        wireframeCheckbox.setBackground(new Color(255, 255, 255));
        wireframeCheckbox.setForeground(new Color(40, 40, 40));
        wireframeCheckbox.addActionListener(e -> {
            wireframeMode = wireframeCheckbox.isSelected();
            // Update menu item
            updateWireframeMenuItem();
            refreshDisplay();
        });
        checkboxPanel.add(wireframeCheckbox);

        colorOverrideCheckbox = new JCheckBox("Override Color");
        colorOverrideCheckbox.setFont(labelFont);
        colorOverrideCheckbox.setBackground(new Color(255, 255, 255));
        colorOverrideCheckbox.setForeground(new Color(40, 40, 40));
        colorOverrideCheckbox.addActionListener(e -> {
            useColorOverride = colorOverrideCheckbox.isSelected();
            refreshDisplay();
        });
        checkboxPanel.add(colorOverrideCheckbox);

        renderOptionsPanel.add(checkboxPanel);

        // Color picker panel
        JPanel modelOptionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 10));
        modelOptionsPanel.setOpaque(false);

        colorPickerButton = createRoundedButton("Choose Color", userSelectedColor, buttonFont);
        colorPickerButton.setPreferredSize(new Dimension(120, colorPickerButton.getPreferredSize().height));
        colorPickerButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(
                    this,
                    "Choose Model Color",
                    userSelectedColor);

            if (newColor != null) {
                userSelectedColor = newColor;
                colorPickerButton.setBackground(userSelectedColor);
                colorPickerButton.setForeground(
                        getBrightness(userSelectedColor) > 128 ? Color.BLACK : Color.WHITE);
                refreshDisplay();
            }
        });
        modelOptionsPanel.add(colorPickerButton);

        JButton clearModelButton = createRoundedButton("Clear Model", new Color(220, 53, 69), buttonFont);
        clearModelButton.setPreferredSize(new Dimension(120, clearModelButton.getPreferredSize().height));
        clearModelButton.addActionListener(e -> clearModel());
        modelOptionsPanel.add(clearModelButton);

        renderOptionsPanel.add(modelOptionsPanel);

        modelFormPanel.add(renderOptionsPanel);

        // Add the form panel to the main panel
        modelPanel.add(modelFormPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Load Model", modelPanel);

        // Second tab - Room creator
        JPanel roomPanel = new JPanel(new BorderLayout(10, 10));
        roomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        roomPanel.setBackground(new Color(51, 122, 183)); // Nice blue background like Room Planning

        // Create a container panel for the form elements
        JPanel roomFormPanel = new JPanel();
        roomFormPanel.setLayout(new BoxLayout(roomFormPanel, BoxLayout.Y_AXIS));
        roomFormPanel.setBackground(new Color(248, 250, 252)); // Light background color

        // Room dimensions
        JPanel dimensionsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        dimensionsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Room Dimensions",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        dimensionsPanel.setBackground(new Color(255, 255, 255));

        // Styled label and spinner creation helper
        Dimension spinnerSize = new Dimension(100, 28);

        JLabel widthLabel = new JLabel("Width (m):");
        widthLabel.setFont(labelFont);
        widthLabel.setForeground(new Color(40, 40, 40));
        roomWidthSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 20.0, 0.1));
        roomWidthSpinner.setPreferredSize(spinnerSize);
        roomWidthSpinner.setFont(labelFont);
        roomWidthSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        roomWidthSpinner.addChangeListener(e -> {
            roomWidth = ((Number) roomWidthSpinner.getValue()).floatValue();
            if (showRoom)
                refreshDisplay();
        });
        dimensionsPanel.add(widthLabel);
        dimensionsPanel.add(roomWidthSpinner);

        JLabel lengthLabel = new JLabel("Length (m):");
        lengthLabel.setFont(labelFont);
        lengthLabel.setForeground(new Color(40, 40, 40));
        roomLengthSpinner = new JSpinner(new SpinnerNumberModel(5.0, 1.0, 20.0, 0.1));
        roomLengthSpinner.setPreferredSize(spinnerSize);
        roomLengthSpinner.setFont(labelFont);
        roomLengthSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        roomLengthSpinner.addChangeListener(e -> {
            roomLength = ((Number) roomLengthSpinner.getValue()).floatValue();
            if (showRoom)
                refreshDisplay();
        });
        dimensionsPanel.add(lengthLabel);
        dimensionsPanel.add(roomLengthSpinner);

        JLabel heightLabel = new JLabel("Height (m):");
        heightLabel.setFont(labelFont);
        heightLabel.setForeground(new Color(40, 40, 40));
        roomHeightSpinner = new JSpinner(new SpinnerNumberModel(3.0, 1.0, 10.0, 0.1));
        roomHeightSpinner.setPreferredSize(spinnerSize);
        roomHeightSpinner.setFont(labelFont);
        roomHeightSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        roomHeightSpinner.addChangeListener(e -> {
            roomHeight = ((Number) roomHeightSpinner.getValue()).floatValue();
            if (showRoom)
                refreshDisplay();
        });
        dimensionsPanel.add(heightLabel);
        dimensionsPanel.add(roomHeightSpinner);

        roomFormPanel.add(dimensionsPanel);
        roomFormPanel.add(Box.createVerticalStrut(15));

        // Room colors with modern styling
        JPanel colorsPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        colorsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Room Colors",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        colorsPanel.setBackground(new Color(255, 255, 255));

        // Color button creation helper
        Dimension colorButtonSize = new Dimension(100, 28);

        JLabel wallLabel = new JLabel("Wall Color:");
        wallLabel.setFont(labelFont);
        wallLabel.setForeground(new Color(40, 40, 40));
        wallColorButton = new JButton();
        wallColorButton.setPreferredSize(colorButtonSize);
        wallColorButton.setBackground(wallColor);
        wallColorButton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        wallColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Wall Color", wallColor);
            if (newColor != null) {
                wallColor = newColor;
                wallColorButton.setBackground(wallColor);
                if (showRoom)
                    refreshDisplay();
            }
        });
        colorsPanel.add(wallLabel);
        colorsPanel.add(wallColorButton);

        JLabel floorLabel = new JLabel("Floor Color:");
        floorLabel.setFont(labelFont);
        floorLabel.setForeground(new Color(40, 40, 40));
        floorColorButton = new JButton();
        floorColorButton.setPreferredSize(colorButtonSize);
        floorColorButton.setBackground(floorColor);
        floorColorButton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        floorColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Floor Color", floorColor);
            if (newColor != null) {
                floorColor = newColor;
                floorColorButton.setBackground(floorColor);
                if (showRoom)
                    refreshDisplay();
            }
        });
        colorsPanel.add(floorLabel);
        colorsPanel.add(floorColorButton);

        JLabel ceilingLabel = new JLabel("Ceiling Color:");
        ceilingLabel.setFont(labelFont);
        ceilingLabel.setForeground(new Color(40, 40, 40));
        ceilingColorButton = new JButton();
        ceilingColorButton.setPreferredSize(colorButtonSize);
        ceilingColorButton.setBackground(ceilingColor);
        ceilingColorButton.setBorder(BorderFactory.createLineBorder(new Color(180, 180, 180)));
        ceilingColorButton.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Ceiling Color", ceilingColor);
            if (newColor != null) {
                ceilingColor = newColor;
                ceilingColorButton.setBackground(ceilingColor);
                if (showRoom)
                    refreshDisplay();
            }
        });
        colorsPanel.add(ceilingLabel);
        colorsPanel.add(ceilingColorButton);

        roomFormPanel.add(colorsPanel);
        roomFormPanel.add(Box.createVerticalStrut(15));

        // Room transparency with modern styling
        JPanel transparencyPanel = new JPanel(new GridLayout(3, 2, 10, 5));
        transparencyPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Transparency",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        transparencyPanel.setBackground(new Color(255, 255, 255));

        JLabel wallTransLabel = new JLabel("Walls:");
        wallTransLabel.setFont(labelFont);
        wallTransLabel.setForeground(new Color(40, 40, 40));
        wallTransparencySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        wallTransparencySpinner.setPreferredSize(spinnerSize);
        wallTransparencySpinner.setFont(labelFont);
        wallTransparencySpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        wallTransparencySpinner.addChangeListener(e -> {
            wallTransparency = ((Number) wallTransparencySpinner.getValue()).floatValue();
            if (showRoom)
                refreshDisplay();
        });
        transparencyPanel.add(wallTransLabel);
        transparencyPanel.add(wallTransparencySpinner);

        JLabel floorTransLabel = new JLabel("Floor:");
        floorTransLabel.setFont(labelFont);
        floorTransLabel.setForeground(new Color(40, 40, 40));
        floorTransparencySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        floorTransparencySpinner.setPreferredSize(spinnerSize);
        floorTransparencySpinner.setFont(labelFont);
        floorTransparencySpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        floorTransparencySpinner.addChangeListener(e -> {
            floorTransparency = ((Number) floorTransparencySpinner.getValue()).floatValue();
            if (showRoom)
                refreshDisplay();
        });
        transparencyPanel.add(floorTransLabel);
        transparencyPanel.add(floorTransparencySpinner);

        JLabel ceilingTransLabel = new JLabel("Ceiling:");
        ceilingTransLabel.setFont(labelFont);
        ceilingTransLabel.setForeground(new Color(40, 40, 40));
        ceilingTransparencySpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.0, 1.0, 0.1));
        ceilingTransparencySpinner.setPreferredSize(spinnerSize);
        ceilingTransparencySpinner.setFont(labelFont);
        ceilingTransparencySpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        ceilingTransparencySpinner.addChangeListener(e -> {
            ceilingTransparency = ((Number) ceilingTransparencySpinner.getValue()).floatValue();
            if (showRoom)
                refreshDisplay();
        });
        transparencyPanel.add(ceilingTransLabel);
        transparencyPanel.add(ceilingTransparencySpinner);

        roomFormPanel.add(transparencyPanel);
        roomFormPanel.add(Box.createVerticalStrut(15));

        // Room controls panel with modern styling
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        controlsPanel.setBackground(new Color(248, 250, 252));
        controlsPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        showRoomCheckbox = new JCheckBox("Show Room");
        showRoomCheckbox.setFont(new Font("Segoe UI", Font.BOLD, 12));
        showRoomCheckbox.setBackground(new Color(248, 250, 252));
        showRoomCheckbox.setForeground(new Color(40, 40, 40));
        showRoomCheckbox.addActionListener(e -> {
            showRoom = showRoomCheckbox.isSelected();
            showRoomCheckbox.setSelected(showRoom);
            refreshDisplay();
        });
        controlsPanel.add(showRoomCheckbox);

        JButton createRoomButton = createRoundedButton("Create/Update Room", new Color(0, 123, 255), buttonFont);
        // Set a consistent width for longer buttons
        createRoomButton.setPreferredSize(new Dimension(160, createRoomButton.getPreferredSize().height));
        createRoomButton.addActionListener(e -> {
            roomWidth = ((Number) roomWidthSpinner.getValue()).floatValue();
            roomHeight = ((Number) roomHeightSpinner.getValue()).floatValue();
            roomLength = ((Number) roomLengthSpinner.getValue()).floatValue();
            showRoom = true;
            showRoomCheckbox.setSelected(true);
            initializeRoomView();
        });
        controlsPanel.add(createRoomButton);

        roomFormPanel.add(controlsPanel);

        // Add the form panel to the main panel
        roomPanel.add(roomFormPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Create Room", roomPanel);

        // Third tab - Room Planning
        JPanel planningPanel = new JPanel(new BorderLayout(10, 10));
        planningPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        planningPanel.setBackground(new Color(51, 122, 183)); // Changed to a nice blue color

        // Create the models list panel
        JPanel modelsListPanel = new JPanel(new BorderLayout(0, 5));
        modelsListPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Models in Room",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        modelsListPanel.setBackground(new Color(248, 250, 252)); // Lighter background for panel

        // Create the list and scrollpane
        modelsList = new JList<>(modelsListModel);
        modelsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        modelsList.setFont(labelFont); // Reuse existing labelFont
        modelsList.setBackground(new Color(255, 255, 255));
        modelsList.setForeground(new Color(40, 40, 40));
        modelsList.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        modelsList.setFixedCellHeight(25); // Consistent cell height
        modelsList.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                if (!e.getValueIsAdjusting()) {
                    handleModelSelection();
                }
            }
        });

        JScrollPane listScrollPane = new JScrollPane(modelsList);
        listScrollPane.setPreferredSize(new Dimension(220, 180)); // Larger list area
        listScrollPane.setBorder(BorderFactory.createLineBorder(new Color(180, 200, 220), 1));
        listScrollPane.getViewport().setBackground(Color.WHITE);
        modelsListPanel.add(listScrollPane, BorderLayout.CENTER);

        // Create buttons panel for model operations
        JPanel modelsButtonPanel = new JPanel(new GridLayout(1, 3, 8, 5));
        modelsButtonPanel.setBorder(BorderFactory.createEmptyBorder(8, 0, 0, 0));
        modelsButtonPanel.setOpaque(false);

        // Create styled buttons
        JButton addModelButton = createRoundedButton("Add", new Color(60, 179, 113), buttonFont);
        addModelButton.setPreferredSize(new Dimension(80, addModelButton.getPreferredSize().height));
        addModelButton.addActionListener(e -> addModelToRoom());

        JButton removeModelButton = createRoundedButton("Remove", new Color(220, 53, 69), buttonFont);
        removeModelButton.setPreferredSize(new Dimension(80, removeModelButton.getPreferredSize().height));
        removeModelButton.addActionListener(e -> removeSelectedModel());

        JButton duplicateModelButton = createRoundedButton("Duplicate", new Color(0, 123, 255), buttonFont);
        duplicateModelButton.setPreferredSize(new Dimension(100, duplicateModelButton.getPreferredSize().height));
        duplicateModelButton.addActionListener(e -> duplicateSelectedModel());

        modelsButtonPanel.add(addModelButton);
        modelsButtonPanel.add(removeModelButton);
        modelsButtonPanel.add(duplicateModelButton);

        modelsListPanel.add(modelsButtonPanel, BorderLayout.SOUTH);
        planningPanel.add(modelsListPanel, BorderLayout.WEST);

        // Create model properties panel
        JPanel propertiesPanel = new JPanel();
        propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.Y_AXIS));
        propertiesPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Model Properties",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        propertiesPanel.setBackground(new Color(248, 250, 252));

        // Position controls with improved styling
        JPanel positionPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        positionPanel.setOpaque(false);

        // Style for all labels - use existing labelFont
        Color labelColor = new Color(60, 60, 90);

        // X Position
        JLabel xLabel = new JLabel("X Position:");
        xLabel.setFont(labelFont);
        xLabel.setForeground(labelColor);
        positionPanel.add(xLabel);

        modelXSpinner = new JSpinner(new SpinnerNumberModel(0.0, -20.0, 20.0, 0.1));
        JSpinner.NumberEditor xEditor = new JSpinner.NumberEditor(modelXSpinner, "0.00");
        modelXSpinner.setEditor(xEditor);
        modelXSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) modelXSpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        ((JSpinner.DefaultEditor) modelXSpinner.getEditor()).getTextField()
                .setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelXSpinner.addChangeListener(e -> {
            if (selectedModel != null) {
                selectedModel.x = ((Number) modelXSpinner.getValue()).floatValue();
                refreshDisplay();
            }
        });
        positionPanel.add(modelXSpinner);

        // Y Position
        JLabel yLabel = new JLabel("Y Position:");
        yLabel.setFont(labelFont);
        yLabel.setForeground(labelColor);
        positionPanel.add(yLabel);

        modelYSpinner = new JSpinner(new SpinnerNumberModel(0.0, -20.0, 20.0, 0.1));
        JSpinner.NumberEditor yEditor = new JSpinner.NumberEditor(modelYSpinner, "0.00");
        modelYSpinner.setEditor(yEditor);
        modelYSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) modelYSpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        ((JSpinner.DefaultEditor) modelYSpinner.getEditor()).getTextField()
                .setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelYSpinner.addChangeListener(e -> {
            if (selectedModel != null) {
                selectedModel.y = ((Number) modelYSpinner.getValue()).floatValue();
                refreshDisplay();
            }
        });
        positionPanel.add(modelYSpinner);

        // Z Position
        JLabel zLabel = new JLabel("Z Position:");
        zLabel.setFont(labelFont);
        zLabel.setForeground(labelColor);
        positionPanel.add(zLabel);

        modelZSpinner = new JSpinner(new SpinnerNumberModel(0.0, -20.0, 20.0, 0.1));
        JSpinner.NumberEditor zEditor = new JSpinner.NumberEditor(modelZSpinner, "0.00");
        modelZSpinner.setEditor(zEditor);
        modelZSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) modelZSpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        ((JSpinner.DefaultEditor) modelZSpinner.getEditor()).getTextField()
                .setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelZSpinner.addChangeListener(e -> {
            if (selectedModel != null) {
                selectedModel.z = ((Number) modelZSpinner.getValue()).floatValue();
                refreshDisplay();
            }
        });
        positionPanel.add(modelZSpinner);

        // Rotation
        JLabel rotLabel = new JLabel("Rotation (Y):");
        rotLabel.setFont(labelFont);
        rotLabel.setForeground(labelColor);
        positionPanel.add(rotLabel);

        modelRotationSpinner = new JSpinner(new SpinnerNumberModel(0.0, 0.0, 360.0, 5.0));
        JSpinner.NumberEditor rotEditor = new JSpinner.NumberEditor(modelRotationSpinner, "0.0");
        modelRotationSpinner.setEditor(rotEditor);
        modelRotationSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) modelRotationSpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        ((JSpinner.DefaultEditor) modelRotationSpinner.getEditor()).getTextField()
                .setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelRotationSpinner.addChangeListener(e -> {
            if (selectedModel != null) {
                selectedModel.rotY = ((Number) modelRotationSpinner.getValue()).floatValue();
                refreshDisplay();
            }
        });
        positionPanel.add(modelRotationSpinner);

        // Scale
        JLabel scaleLabel = new JLabel("Scale:");
        scaleLabel.setFont(labelFont);
        scaleLabel.setForeground(labelColor);
        positionPanel.add(scaleLabel);

        modelScaleSpinner = new JSpinner(new SpinnerNumberModel(1.0, 0.1, 10.0, 0.1));
        JSpinner.NumberEditor scaleEditor = new JSpinner.NumberEditor(modelScaleSpinner, "0.0");
        modelScaleSpinner.setEditor(scaleEditor);
        modelScaleSpinner.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 200, 220), 1),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)));
        ((JSpinner.DefaultEditor) modelScaleSpinner.getEditor()).getTextField().setBackground(Color.WHITE);
        ((JSpinner.DefaultEditor) modelScaleSpinner.getEditor()).getTextField()
                .setFont(new Font("Segoe UI", Font.PLAIN, 12));
        modelScaleSpinner.addChangeListener(e -> {
            if (selectedModel != null) {
                selectedModel.scale = ((Number) modelScaleSpinner.getValue()).floatValue();
                refreshDisplay();
            }
        });
        positionPanel.add(modelScaleSpinner);

        // Add a color customization section
        JPanel modelColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        modelColorPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                        BorderFactory.createLineBorder(new Color(100, 130, 180), 2, true),
                        "Color Settings",
                        TitledBorder.LEFT,
                        TitledBorder.TOP,
                        new Font("Segoe UI", Font.BOLD, 14),
                        new Color(50, 80, 120)),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)));
        modelColorPanel.setBackground(new Color(255, 255, 255));
        modelColorPanel.setOpaque(true);

        modelColorCheckbox = new JCheckBox("Custom Color");
        modelColorCheckbox.setFont(labelFont);
        modelColorCheckbox.setBackground(new Color(255, 255, 255));
        modelColorCheckbox.addActionListener(e -> {
            if (selectedModel != null) {
                selectedModel.useCustomColor = modelColorCheckbox.isSelected();
                modelColorButton.setEnabled(selectedModel.useCustomColor);
                refreshDisplay();
            }
        });
        modelColorPanel.add(modelColorCheckbox);

        modelColorButton = createRoundedButton("Choose", new Color(52, 152, 219), buttonFont);
        modelColorButton.setPreferredSize(new Dimension(80, modelColorButton.getPreferredSize().height));
        modelColorButton.setEnabled(false); // Initially disabled
        modelColorButton.addActionListener(e -> {
            if (selectedModel != null) {
                Color newColor = JColorChooser.showDialog(
                        this,
                        "Choose Model Color",
                        selectedModel.customColor);

                if (newColor != null) {
                    selectedModel.customColor = newColor;
                    modelColorButton.setBackground(newColor);
                    refreshDisplay();
                }
            }
        });
        modelColorPanel.add(modelColorButton);

        propertiesPanel.add(modelColorPanel);
        propertiesPanel.add(Box.createVerticalStrut(10));

        // Disable property controls initially
        setModelControlsEnabled(false);

        propertiesPanel.add(positionPanel);
        propertiesPanel.add(Box.createVerticalStrut(20));

        // Add controls help with improved styling
        JPanel helpPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        helpPanel.setOpaque(false);

        JButton controlsHelpButton = createRoundedButton("Keyboard/Mouse Controls Help", new Color(52, 58, 64),
                buttonFont);
        controlsHelpButton.setPreferredSize(new Dimension(200, controlsHelpButton.getPreferredSize().height));
        controlsHelpButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "<html><h2 style='color:#007bff'>Object Manipulation Controls</h2>" +
                            "<div style='font-family:Segoe UI,Arial,sans-serif;'>" +
                            "<p><b style='color:#495057'>Mouse Controls:</b></p>" +
                            "<ul>" +
                            "<li><b>Double-click:</b> Select next object</li>" +
                            "<li><b>Ctrl+Drag:</b> Move selected object horizontally</li>" +
                            "<li><b>Ctrl+Mouse Wheel:</b> Scale selected object</li>" +
                            "</ul>" +
                            "<p><b style='color:#495057'>Keyboard Controls:</b></p>" +
                            "<ul>" +
                            "<li><b>W/S/A/D:</b> Move object along X/Z axes</li>" +
                            "<li><b>Q/E:</b> Move object up/down (Y axis)</li>" +
                            "<li><b>R/F:</b> Rotate object left/right</li>" +
                            "<li><b>Tab:</b> Select next object</li>" +
                            "<li><b>Delete:</b> Remove selected object</li>" +
                            "</ul>" +
                            "<p><i style='color:#6c757d'>Note: Click on the 3D view to ensure keyboard focus before using keys</i></p>"
                            +
                            "</div></html>",
                    "Object Manipulation Controls",
                    JOptionPane.INFORMATION_MESSAGE);
        });

        helpPanel.add(controlsHelpButton);
        propertiesPanel.add(helpPanel);

        // Add 2D/3D view toggle
        JPanel viewTogglePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        viewTogglePanel.setOpaque(false);

        JCheckBox view2DCheckbox = new JCheckBox("2D Top-Down View");
        view2DCheckbox.setFont(labelFont);
        view2DCheckbox.setBackground(new Color(248, 250, 252));
        view2DCheckbox.setForeground(new Color(40, 40, 40));
        view2DCheckbox.addActionListener(e -> {
            use2DView = view2DCheckbox.isSelected();
            if (use2DView) {
                // Set up 2D top-down view
                rotX = 90.0f; // Look straight down
                rotY = 0.0f; // No horizontal rotation
                zoom = -Math.max(roomWidth, roomLength) * 1.5f; // Zoom out to see whole room
            } else {
                // Restore 3D perspective view
                initializeRoomView();
            }
            refreshDisplay();
        });
        viewTogglePanel.add(view2DCheckbox);

        propertiesPanel.add(viewTogglePanel);
        propertiesPanel.add(Box.createVerticalStrut(10));

        planningPanel.add(propertiesPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Room Planning", planningPanel);

        add(tabbedPane, BorderLayout.NORTH);

        // Create the software-rendered canvas
        try {
            // Use GL2 profile which has the best compatibility
            GLProfile glProfile = GLProfile.get(GLProfile.GL2);

            // Configure for software rendering
            GLCapabilities glCapabilities = new GLCapabilities(glProfile);
            glCapabilities.setHardwareAccelerated(false);
            glCapabilities.setDoubleBuffered(true);

            // Create a GLJPanel instead of GLCanvas - better software rendering support
            canvas = new GLJPanel(glCapabilities);
            canvas.addGLEventListener(this);

            // Set canvas size to 50% width while maintaining full height
            Dimension screenSize = getSize();
            int canvasWidth = screenSize.width / 2;
            canvas.setPreferredSize(new Dimension(canvasWidth, screenSize.height));

            // Make sure canvas can receive focus and key events
            canvas.setFocusable(true);

            // Request focus for the canvas when clicked
            canvas.addMouseListener(new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    // Request focus to receive keyboard events
                    if (!canvas.hasFocus()) {
                        canvas.requestFocusInWindow();
                        System.out.println("Canvas focus requested");
                    }

                    lastX = e.getX();
                    lastY = e.getY();

                    // For object manipulation
                    if (objectManipulationEnabled && e.isControlDown() && showRoom && !roomModels.isEmpty()) {
                        isDraggingObject = true;
                        lastMousePosition = e.getPoint();
                        System.out.println("Object drag started");
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isDraggingObject = false;
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    // Request focus to receive keyboard events
                    canvas.requestFocusInWindow();

                    // Double-click to select next object
                    if (e.getClickCount() == 2 && showRoom && !roomModels.isEmpty()) {
                        System.out.println("Double-click detected, selecting next model");

                        // If no model is currently selected, select the first one
                        int newIndex = 0;
                        if (selectedModelIndex >= 0) {
                            // Select next model in the list
                            newIndex = (selectedModelIndex + 1) % roomModels.size();
                        }

                        modelsList.setSelectedIndex(newIndex);

                        // Make sure we refresh the display
                        refreshDisplay();
                    }
                }
            });

            canvas.addMouseMotionListener(new MouseMotionAdapter() {
                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDraggingObject && selectedModel != null) {
                        // Handle object dragging (Ctrl + drag)
                        handleObjectDrag(e);
                    } else {
                        // Regular camera rotation
                        int dx = e.getX() - lastX;
                        int dy = e.getY() - lastY;

                        rotY += dx * 0.5f;
                        rotX += dy * 0.5f;

                        lastX = e.getX();
                        lastY = e.getY();
                    }
                }
            });

            canvas.addMouseWheelListener(new MouseWheelListener() {
                @Override
                public void mouseWheelMoved(MouseWheelEvent e) {
                    // If Ctrl is pressed, adjust model scale
                    if (objectManipulationEnabled && e.isControlDown() && selectedModel != null) {
                        // Adjust scale of selected model
                        float scaleChange = e.getWheelRotation() * -0.05f;
                        float newScale = selectedModel.scale + scaleChange;

                        // Enforce limits
                        newScale = Math.max(0.1f, Math.min(10.0f, newScale));
                        selectedModel.scale = newScale;

                        // Update UI spinner
                        modelScaleSpinner.setValue(Double.valueOf(newScale));
                        refreshDisplay();
                    } else {
                        // Normal camera zoom
                        zoom += e.getWheelRotation() * 0.5f;
                    }
                }
            });

            // Add keyboard interaction for object manipulation
            canvas.setFocusable(true);
            canvas.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    System.out.println("Key pressed: " + KeyEvent.getKeyText(e.getKeyCode()));

                    // Check if we should handle object manipulation
                    if (selectedModel != null && showRoom) {
                        handleObjectKeyControl(e);
                    }
                }
            });

            // Add the canvas to a panel to ensure it receives focus properly
            JPanel canvasPanel = new JPanel(new BorderLayout());
            canvasPanel.add(canvas, BorderLayout.CENTER);
            add(canvasPanel, BorderLayout.CENTER);

            // Add focus listener to debug focus issues
            canvas.addFocusListener(new FocusListener() {
                @Override
                public void focusGained(FocusEvent e) {
                    System.out.println("Canvas gained focus");
                }

                @Override
                public void focusLost(FocusEvent e) {
                    System.out.println("Canvas lost focus");
                }
            });

            // Add a special control to help with keyboard focus
            JButton focusButton = createRoundedButton("Click to focus canvas for keyboard controls",
                    new Color(52, 152, 219), buttonFont);
            focusButton.setPreferredSize(new Dimension(300, focusButton.getPreferredSize().height));
            focusButton.addActionListener(e -> {
                canvas.requestFocusInWindow();
                System.out.println("Focus requested for canvas");
            });

            // Add focus button to a panel
            JPanel focusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            focusPanel.add(focusButton);
            add(focusPanel, BorderLayout.SOUTH);

            // Set up animator with low frame rate for better compatibility
            animator = new Animator(canvas);
            animator.setUpdateFPSFrames(20, null);

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

            // Add listener to focus the canvas when window is activated
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowActivated(WindowEvent e) {
                    // Request focus for the canvas when window becomes active
                    SwingUtilities.invokeLater(() -> {
                        canvas.requestFocusInWindow();
                        System.out.println("Canvas focus requested on window activation");
                    });
                }

                @Override
                public void windowOpened(WindowEvent e) {
                    // Request focus for the canvas when window first opens
                    SwingUtilities.invokeLater(() -> {
                        canvas.requestFocusInWindow();
                        System.out.println("Canvas focus requested on window opening");
                    });
                }

                @Override
                public void windowClosing(WindowEvent e) {
                    if (animator.isAnimating()) {
                        animator.stop();
                    }
                    System.exit(0);
                }
            });

            // Start the animator
            animator.start();

            // Request focus for the canvas after UI is visible
            SwingUtilities.invokeLater(() -> {
                canvas.requestFocusInWindow();
                System.out.println("Initial canvas focus requested");
            });

        } catch (Exception e) {
            System.err.println("Error creating OpenGL context: " + e.getMessage());
            e.printStackTrace();

            // Display error in the UI
            JLabel errorLabel = new JLabel("<html><h2>Error initializing OpenGL</h2>" +
                    "<p>" + e.getMessage() + "</p>" +
                    "<p>Please make sure your graphics drivers are up to date.</p></html>");
            add(errorLabel, BorderLayout.CENTER);
        }
    }

       private void browseObjFile() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files", "obj"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            objFileText.setText(file.getAbsolutePath());

            // Make sure wireframe mode is off when loading a new model
            wireframeMode = false;
            wireframeCheckbox.setSelected(false);

            // Update the menu item if it exists
            updateWireframeMenuItem();

            // Load the model file
            loadObjFile(file.getAbsolutePath());
        }
    }

    private void updateWireframeMenuItem() {
        // Update menu item if it exists
        for (int i = 0; i < getJMenuBar().getMenu(1).getItemCount(); i++) {
            if (getJMenuBar().getMenu(1).getItem(i) instanceof JCheckBoxMenuItem &&
                    "Wireframe Mode".equals(getJMenuBar().getMenu(1).getItem(i).getText())) {
                ((JCheckBoxMenuItem) getJMenuBar().getMenu(1).getItem(i)).setSelected(wireframeMode);
                break;
            }
        }
    }

    private void clearModel() {
        // Clear all model data
        vertices.clear();
        normals.clear();
        textureCoords.clear();
        faces.clear();
        materials.clear();
        currentMaterial = null;

        // Clear file paths
        objFileText.setText("");
        mtlFileText.setText("");

        // Reset view
        rotX = 0.0f;
        rotY = 0.0f;
        zoom = -5.0f;

        // Reset bounding box
        minX = minY = minZ = Float.MAX_VALUE;
        maxX = maxY = maxZ = Float.MIN_VALUE;

        refreshDisplay();
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
            gl.glClearColor(0.2f, 0.2f, 0.2f, 1.0f);
            gl.glEnable(GL.GL_DEPTH_TEST);

            // Enable transparency
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);

            // Basic lighting setup
            gl.glEnable(GL2.GL_LIGHTING);
            gl.glEnable(GL2.GL_LIGHT0);

            // Main light position and properties
            float[] lightPosition = { 1.0f, 1.0f, 1.0f, 0.0f };
            float[] lightAmbient = { 0.4f, 0.4f, 0.4f, 1.0f };
            float[] lightDiffuse = { 0.8f, 0.8f, 0.8f, 1.0f };
            float[] lightSpecular = { 1.0f, 1.0f, 1.0f, 1.0f };

            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_POSITION, lightPosition, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_AMBIENT, lightAmbient, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_DIFFUSE, lightDiffuse, 0);
            gl.glLightfv(GL2.GL_LIGHT0, GL2.GL_SPECULAR, lightSpecular, 0);

            // Enable materials
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
            gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);
            gl.glEnable(GL2.GL_NORMALIZE);

            // Enable smooth shading
            gl.glShadeModel(GL2.GL_SMOOTH);

            // Log OpenGL information
            System.out.println("OpenGL Vendor: " + gl.glGetString(GL.GL_VENDOR));
            System.out.println("OpenGL Renderer: " + gl.glGetString(GL.GL_RENDERER));
            System.out.println("OpenGL Version: " + gl.glGetString(GL.GL_VERSION));
        } catch (Exception e) {
            System.err.println("Error in init: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void display(GLAutoDrawable drawable) {
        try {
            GL2 gl = drawable.getGL().getGL2();
            gl.glClear(GL.GL_COLOR_BUFFER_BIT | GL.GL_DEPTH_BUFFER_BIT);
            gl.glLoadIdentity();

            // Apply camera transformations
            gl.glTranslatef(0, 0, zoom);

            // If in 2D view mode for room planning and room is shown, use orthographic
            // projection
            if (use2DView && showRoom) {
                // Fixed 90-degree top-down view for 2D mode
                gl.glRotatef(90.0f, 1.0f, 0.0f, 0.0f);
            } else {
                // Regular 3D view with user-controlled rotation
                gl.glRotatef(rotX, 1.0f, 0.0f, 0.0f);
                gl.glRotatef(rotY, 0.0f, 1.0f, 0.0f);
            }

            // Center model based on bounding box if we have a model
            if (!vertices.isEmpty()) {
                float centerX = (minX + maxX) / 2;
                float centerY = (minY + maxY) / 2;
                float centerZ = (minZ + maxZ) / 2;
                gl.glTranslatef(-centerX, -centerY, -centerZ);
            }

            // Set drawing mode based on wireframe setting
            if (wireframeMode) {
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
            } else {
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
            }

            // Draw the model if available
            if (!vertices.isEmpty()) {
                renderModel(gl);
            }

            // Draw the room if enabled
            if (showRoom) {
                renderRoom(gl);

                // Draw the models inside the room
                renderRoomModels(gl);

                // In 2D mode, overlay a grid for better positioning reference
                if (use2DView) {
                    renderGrid(gl);
                }
            }

            // Reset polygon mode
            gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);
        } catch (Exception e) {
            System.err.println("Error in display: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        try {
            GL2 gl = drawable.getGL().getGL2();
            gl.glViewport(0, 0, width, height);
            gl.glMatrixMode(GL2.GL_PROJECTION);
            gl.glLoadIdentity();

            float aspect = (float) width / height;
            gl.glFrustum(-aspect, aspect, -1.0, 1.0, 1.0, 100.0);

            gl.glMatrixMode(GL2.GL_MODELVIEW);
            gl.glLoadIdentity();
        } catch (Exception e) {
            System.err.println("Error in reshape: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {
        // Nothing to clean up in this simplified viewer
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
                if (parts.length == 0)
                    continue;

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
                        texIndices[i - 1] = indices.length > 1 && !indices[1].isEmpty()
                                ? Integer.parseInt(indices[1]) - 1
                                : -1;
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

            // Calculate auto-scale factor for consistent model size
            float modelSize = Math.max(maxX - minX, Math.max(maxY - minY, maxZ - minZ));
            float scaleFactor = 3.0f / modelSize;
            zoom = -5.0f * scaleFactor; // Adjust zoom based on model size

            System.out
                    .println("Loaded model with " + (vertices.size() / 3) + " vertices and " + faces.size() + " faces");

        } catch (IOException e) {
            System.err.println("Error loading OBJ file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading OBJ: " + e.getMessage());
            e.printStackTrace();
        }
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
                        currentMaterial.shininess = Float.parseFloat(parts[1]) / 1000.0f * 128.0f;
                    }
                }
            }

            reader.close();

        } catch (IOException e) {
            System.err.println("Error loading MTL file: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error loading MTL: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void renderModel(GL2 gl) {
        if (vertices.isEmpty()) {
            return;
        }

        // Set a default material if none available
        if (!wireframeMode) {
            // Default material if none specified
            float[] defaultAmbient = { 0.4f, 0.4f, 0.4f, 1.0f };
            float[] defaultDiffuse = { 0.8f, 0.8f, 0.8f, 1.0f };
            float[] defaultSpecular = { 0.5f, 0.5f, 0.5f, 1.0f };
            float defaultShininess = 32.0f;

            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, defaultAmbient, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, defaultDiffuse, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, defaultSpecular, 0);
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, defaultShininess);
        }

        // Apply global color override if enabled
        if (useColorOverride) {
            float[] overrideAmbient = {
                    userSelectedColor.getRed() / 255.0f * 0.3f,
                    userSelectedColor.getGreen() / 255.0f * 0.3f,
                    userSelectedColor.getBlue() / 255.0f * 0.3f,
                    1.0f
            };

            float[] overrideDiffuse = {
                    userSelectedColor.getRed() / 255.0f,
                    userSelectedColor.getGreen() / 255.0f,
                    userSelectedColor.getBlue() / 255.0f,
                    1.0f
            };

            float[] overrideSpecular = { 0.5f, 0.5f, 0.5f, 1.0f };

            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, overrideAmbient, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, overrideDiffuse, 0);
            gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, overrideSpecular, 0);
            gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, 32.0f);

            // Set color directly
            gl.glColor3f(
                    userSelectedColor.getRed() / 255.0f,
                    userSelectedColor.getGreen() / 255.0f,
                    userSelectedColor.getBlue() / 255.0f);
        }

        Material lastMaterial = null;

        // Draw each face
        for (Face face : faces) {
            // Apply material if available and not using color override
            if (!useColorOverride && !wireframeMode && face.materialName != null
                    && materials.containsKey(face.materialName)) {
                Material material = materials.get(face.materialName);
                if (material != lastMaterial) {
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, material.ambient, 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, material.diffuse, 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, material.specular, 0);
                    gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS,
                            material.shininess > 0 ? material.shininess : 32.0f);

                    // Set the color directly too for better visual feedback
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
            if (face.vertexIndices.length == 3) {
                gl.glBegin(GL.GL_TRIANGLES);
            } else if (face.vertexIndices.length == 4) {
                gl.glBegin(GL2.GL_QUADS);
            } else {
                gl.glBegin(GL2.GL_POLYGON);
            }

            for (int i = 0; i < face.vertexIndices.length; i++) {
                int vertIndex = face.vertexIndices[i];

                // Apply normal if available (important for lighting)
                if (face.normalIndices != null && face.normalIndices[i] >= 0) {
                    int normIndex = face.normalIndices[i];
                    if (normIndex * 3 + 2 < normals.size()) {
                        gl.glNormal3f(
                                normals.get(normIndex * 3),
                                normals.get(normIndex * 3 + 1),
                                normals.get(normIndex * 3 + 2));
                    }
                }

                // Apply texture coordinate if available
                if (face.texCoordIndices != null && face.texCoordIndices[i] >= 0 && !textureCoords.isEmpty()) {
                    int texIndex = face.texCoordIndices[i];
                    if (texIndex * 2 + 1 < textureCoords.size()) {
                        gl.glTexCoord2f(
                                textureCoords.get(texIndex * 2),
                                textureCoords.get(texIndex * 2 + 1));
                    }
                }

                // Set vertex
                if (vertIndex * 3 + 2 < vertices.size()) {
                    gl.glVertex3f(
                            vertices.get(vertIndex * 3),
                            vertices.get(vertIndex * 3 + 1),
                            vertices.get(vertIndex * 3 + 2));
                }
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

        // Enable color material for room
        gl.glEnable(GL2.GL_COLOR_MATERIAL);
        gl.glColorMaterial(GL2.GL_FRONT_AND_BACK, GL2.GL_AMBIENT_AND_DIFFUSE);

        // Calculate room dimensions (centered on origin)
        float halfWidth = roomWidth / 2.0f;
        float halfLength = roomLength / 2.0f;
        float halfHeight = roomHeight / 2.0f;

        // Draw the room using quads
        // Room is drawn with the inside faces visible (counter-clockwise winding)

        // First render fully opaque surfaces
        if (floorTransparency >= 1.0f) {
            // Floor (bottom) - +Y normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(floorColor.getRed() / 255f, floorColor.getGreen() / 255f, floorColor.getBlue() / 255f,
                    floorTransparency);
            gl.glNormal3f(0.0f, 1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glEnd();
        }

        if (ceilingTransparency >= 1.0f) {
            // Ceiling (top) - -Y normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(ceilingColor.getRed() / 255f, ceilingColor.getGreen() / 255f, ceilingColor.getBlue() / 255f,
                    ceilingTransparency);
            gl.glNormal3f(0.0f, -1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glEnd();
        }

        if (wallTransparency >= 1.0f) {
            // Wall 1 (front) - +Z normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(0.0f, 0.0f, -1.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glEnd();

            // Wall 2 (back) - -Z normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glEnd();

            // Wall 3 (left) - +X normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(1.0f, 0.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glEnd();

            // Wall 4 (right) - -X normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(-1.0f, 0.0f, 0.0f);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glEnd();
        }

        // Now render semi-transparent surfaces (back to front)
        gl.glDepthMask(false); // Disable depth buffer writes for transparent surfaces

        if (wallTransparency < 1.0f) {
            // Wall 2 (back) - -Z normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(0.0f, 0.0f, 1.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glEnd();

            // Wall 3 (left) - +X normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(1.0f, 0.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glEnd();

            // Wall 4 (right) - -X normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(-1.0f, 0.0f, 0.0f);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glEnd();

            // Wall 1 (front) - +Z normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(wallColor.getRed() / 255f, wallColor.getGreen() / 255f, wallColor.getBlue() / 255f,
                    wallTransparency);
            gl.glNormal3f(0.0f, 0.0f, -1.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glEnd();
        }

        if (ceilingTransparency < 1.0f) {
            // Ceiling (top) - -Y normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(ceilingColor.getRed() / 255f, ceilingColor.getGreen() / 255f, ceilingColor.getBlue() / 255f,
                    ceilingTransparency);
            gl.glNormal3f(0.0f, -1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, -halfLength);
            gl.glVertex3f(halfWidth, halfHeight, halfLength);
            gl.glVertex3f(-halfWidth, halfHeight, halfLength);
            gl.glEnd();
        }

        if (floorTransparency < 1.0f) {
            // Floor (bottom) - +Y normal
            gl.glBegin(GL2.GL_QUADS);
            gl.glColor4f(floorColor.getRed() / 255f, floorColor.getGreen() / 255f, floorColor.getBlue() / 255f,
                    floorTransparency);
            gl.glNormal3f(0.0f, 1.0f, 0.0f);
            gl.glVertex3f(-halfWidth, -halfHeight, -halfLength);
            gl.glVertex3f(-halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, halfLength);
            gl.glVertex3f(halfWidth, -halfHeight, -halfLength);
            gl.glEnd();
        }

        // Re-enable depth buffer writes
        gl.glDepthMask(true);

        // Reset color material
        gl.glDisable(GL2.GL_COLOR_MATERIAL);

        // Restore matrix
        gl.glPopMatrix();
    }

    /**
     * Renders all models placed in the room
     */
    private void renderRoomModels(GL2 gl) {
        if (roomModels.isEmpty()) {
            return;
        }

        // Iterate through all models
        for (int i = 0; i < roomModels.size(); i++) {
            Model3D model = roomModels.get(i);

            // Skip if model has no geometry
            if (model.vertices.isEmpty()) {
                continue;
            }

            // Save transformation state
            gl.glPushMatrix();

            // Apply model transformation
            gl.glTranslatef(model.x, model.y, model.z);
            gl.glRotatef(model.rotY, 0.0f, 1.0f, 0.0f);
            gl.glScalef(model.scale, model.scale, model.scale);

            // Highlight selected model
            boolean isSelected = (i == selectedModelIndex);

            // Draw with wireframe overlay if selected
            if (isSelected && !wireframeMode) {
                // First draw the model normally
                renderModelGeometry(gl, model, false);

                // Then draw wireframe on top with slight offset to avoid z-fighting
                gl.glEnable(GL2.GL_POLYGON_OFFSET_FILL);
                gl.glPolygonOffset(1.0f, 1.0f);

                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_LINE);
                gl.glLineWidth(1.5f);
                gl.glColor3f(1.0f, 1.0f, 0.0f); // Yellow wireframe for selected model
                gl.glEnable(GL2.GL_COLOR_MATERIAL);
                renderModelGeometry(gl, model, true);
                gl.glDisable(GL2.GL_COLOR_MATERIAL);
                gl.glPolygonMode(GL.GL_FRONT_AND_BACK, GL2.GL_FILL);

                gl.glDisable(GL2.GL_POLYGON_OFFSET_FILL);
            } else {
                // Draw the model normally
                renderModelGeometry(gl, model, wireframeMode);
            }

            gl.glPopMatrix();
        }
    }

    /**
     * Renders the geometry of a specific 3D model
     */
    private void renderModelGeometry(GL2 gl, Model3D model, boolean wireframeMode) {
        Material lastMaterial = null;

        // Apply color override if enabled (also applies to wireframe)
        if (wireframeMode || useColorOverride || model.useCustomColor) {
            gl.glEnable(GL2.GL_COLOR_MATERIAL);
            if (useColorOverride) {
                gl.glColor3f(
                        userSelectedColor.getRed() / 255.0f,
                        userSelectedColor.getGreen() / 255.0f,
                        userSelectedColor.getBlue() / 255.0f);
            } else if (model.useCustomColor) {
                gl.glColor3f(
                        model.customColor.getRed() / 255.0f,
                        model.customColor.getGreen() / 255.0f,
                        model.customColor.getBlue() / 255.0f);
            } else if (wireframeMode) {
                gl.glColor3f(0.7f, 0.7f, 0.7f); // Light gray for wireframe
            }
        }

        for (Face face : model.faces) {
            // Apply material if available and not wireframe/color override
            if (!wireframeMode && !useColorOverride && face.materialName != null &&
                    model.materials.containsKey(face.materialName)) {

                Material material = model.materials.get(face.materialName);
                if (material != lastMaterial) {
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, material.ambient, 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, material.diffuse, 0);
                    gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, material.specular, 0);
                    gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, material.shininess);
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
                if (face.normalIndices != null && face.normalIndices[i] >= 0 &&
                        face.normalIndices[i] < model.normals.size() / 3) {

                    int normIndex = face.normalIndices[i];
                    gl.glNormal3f(
                            model.normals.get(normIndex * 3),
                            model.normals.get(normIndex * 3 + 1),
                            model.normals.get(normIndex * 3 + 2));
                }

                // Apply texture coordinate if available
                if (face.texCoordIndices != null && face.texCoordIndices[i] >= 0 &&
                        face.texCoordIndices[i] < model.textureCoords.size() / 2) {

                    int texIndex = face.texCoordIndices[i];
                    gl.glTexCoord2f(
                            model.textureCoords.get(texIndex * 2),
                            model.textureCoords.get(texIndex * 2 + 1));
                }

                // Set vertex
                gl.glVertex3f(
                        model.vertices.get(vertIndex * 3),
                        model.vertices.get(vertIndex * 3 + 1),
                        model.vertices.get(vertIndex * 3 + 2));
            }

            gl.glEnd();
        }

        // Reset material state
        if (wireframeMode || useColorOverride || model.useCustomColor) {
            gl.glDisable(GL2.GL_COLOR_MATERIAL);
        }
    }

    // Helper method to determine text color based on background brightness
    private int getBrightness(Color color) {
        return (int) Math.sqrt(
                color.getRed() * color.getRed() * 0.241 +
                        color.getGreen() * color.getGreen() * 0.691 +
                        color.getBlue() * color.getBlue() * 0.068);
    }

    /**
     * Creates a rounded button with consistent styling
     * 
     * @param text    Button text
     * @param bgColor Background color
     * @param font    Button font
     * @return Styled JButton
     */
    private JButton createRoundedButton(String text, Color bgColor, Font font) {
        final Color backgroundColor = bgColor;

        JButton button = new JButton(text) {
            private static final long serialVersionUID = 1L;

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Determine the color based on button state
                Color baseColor = backgroundColor;
                if (getModel().isPressed()) {
                    baseColor = new Color(
                            Math.max(0, backgroundColor.getRed() - 20),
                            Math.max(0, backgroundColor.getGreen() - 20),
                            Math.max(0, backgroundColor.getBlue() - 20));
                } else if (getModel().isRollover()) {
                    baseColor = new Color(
                            Math.min(255, backgroundColor.getRed() + 15),
                            Math.min(255, backgroundColor.getGreen() + 15),
                            Math.min(255, backgroundColor.getBlue() + 15));
                }

                // Create gradient for 3D effect
                GradientPaint gradient = new GradientPaint(
                        0, 0, baseColor,
                        0, getHeight(),
                        new Color(
                                Math.max(0, baseColor.getRed() - 30),
                                Math.max(0, baseColor.getGreen() - 30),
                                Math.max(0, baseColor.getBlue() - 30)));

                g2d.setPaint(gradient);

                // Create pill shape with fully rounded ends
                int radius = getHeight();
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

                // Add highlight effect on top
                GradientPaint highlightGradient = new GradientPaint(
                        0, 0, new Color(255, 255, 255, 100),
                        0, getHeight() / 2, new Color(255, 255, 255, 0));
                g2d.setPaint(highlightGradient);
                g2d.setComposite(AlphaComposite.getInstance(
                        AlphaComposite.SRC_OVER, 0.3f));
                g2d.fillRoundRect(2, 2, getWidth() - 4, getHeight() / 2, radius, radius);

                g2d.dispose();

                // Draw text
                setForeground(getBrightness(baseColor) > 128 ? Color.BLACK : Color.WHITE);
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
                // No border
            }
        };

        // Set button properties
        button.setFont(font);
        button.setOpaque(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);

        // Use smaller margins to match input fields
        button.setMargin(new java.awt.Insets(5, 14, 5, 14));

        // Calculate appropriate dimensions based on text
        FontMetrics metrics = button.getFontMetrics(font);
        int textWidth = metrics.stringWidth(text);
        int buttonWidth = textWidth + 28; // Add some padding
        int buttonHeight = metrics.getHeight() + 10; // Match input field height

        // Set preferred size to be more compact
        button.setPreferredSize(new Dimension(buttonWidth, buttonHeight));

        return button;
    }

    // Helper to force a complete refresh of the display
    private void refreshDisplay() {
        if (canvas != null) {
            canvas.repaint();
            // Add a small delay to ensure repaint is processed
            new Thread(() -> {
                try {
                    Thread.sleep(50);
                    canvas.repaint();
                } catch (InterruptedException e) {
                    // Ignore
                }
            }).start();
        }
    }

    // Initialize view for room visualization
    private void initializeRoomView() {
        // Set default view parameters for room
        rotX = 20.0f; // Slight angle from above
        rotY = 45.0f; // Angled corner view
        zoom = -10.0f; // Zoomed out to see the whole room
        refreshDisplay();
    }

    /**
     * Creates the application menu bar
     */
    private void createMenuBar() {
        JMenuBar menuBar = new JMenuBar();

        // File Menu
        JMenu fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem openObjItem = new JMenuItem("Open OBJ File...", KeyEvent.VK_O);
        openObjItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, ActionEvent.CTRL_MASK));
        openObjItem.addActionListener(e -> browseObjFile());
        fileMenu.add(openObjItem);

        JMenuItem openMtlItem = new JMenuItem("Open MTL File...", KeyEvent.VK_M);
        openMtlItem.addActionListener(e -> browseMtlFile());
        fileMenu.add(openMtlItem);

        fileMenu.addSeparator();

        // Add room save/load menu items
        JMenuItem saveRoomItem = new JMenuItem("Save Room...", KeyEvent.VK_S);
        saveRoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, ActionEvent.CTRL_MASK));
        saveRoomItem.addActionListener(e -> saveRoom());
        fileMenu.add(saveRoomItem);

        JMenuItem saveRoomAsItem = new JMenuItem("Save Room As...");
        saveRoomAsItem.addActionListener(e -> saveRoomAs());
        fileMenu.add(saveRoomAsItem);

        JMenuItem loadRoomItem = new JMenuItem("Load Room...", KeyEvent.VK_L);
        loadRoomItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_L, ActionEvent.CTRL_MASK));
        loadRoomItem.addActionListener(e -> loadRoom());
        fileMenu.add(loadRoomItem);

        fileMenu.addSeparator();

        // Add model library menu items
        JMenu libraryMenu = new JMenu("Model Library");

        JMenuItem addToLibraryItem = new JMenuItem("Add Current Model to Library");
        addToLibraryItem.addActionListener(e -> addCurrentModelToLibrary());
        libraryMenu.add(addToLibraryItem);

        JMenuItem browseLibraryItem = new JMenuItem("Browse Model Library...");
        browseLibraryItem.addActionListener(e -> openModelLibrary());
        libraryMenu.add(browseLibraryItem);

        fileMenu.add(libraryMenu);

        fileMenu.addSeparator();

        JMenuItem exitItem = new JMenuItem("Exit", KeyEvent.VK_X);
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(exitItem);

        // View Menu
        JMenu viewMenu = new JMenu("View");
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JCheckBoxMenuItem wireframeItem = new JCheckBoxMenuItem("Wireframe Mode");
        wireframeItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));
        wireframeItem.addActionListener(e -> {
            wireframeMode = wireframeItem.isSelected();
            wireframeCheckbox.setSelected(wireframeMode);
            refreshDisplay();
        });
        viewMenu.add(wireframeItem);

        JMenuItem modelManagerItem = new JMenuItem("Model Manager...", KeyEvent.VK_M);
        modelManagerItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
        modelManagerItem.addActionListener(e -> openModelManager());
        viewMenu.add(modelManagerItem);

        JMenuItem resetViewItem = new JMenuItem("Reset View", KeyEvent.VK_R);
        resetViewItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, ActionEvent.CTRL_MASK));
        resetViewItem.addActionListener(e -> {
            rotX = 0.0f;
            rotY = 0.0f;
            zoom = -5.0f;
            refreshDisplay();
        });
        viewMenu.add(resetViewItem);

        viewMenu.addSeparator();

        JMenu colorMenu = new JMenu("Color Settings");

        JCheckBoxMenuItem colorOverrideItem = new JCheckBoxMenuItem("Override Model Color");
        colorOverrideItem.addActionListener(e -> {
            useColorOverride = colorOverrideItem.isSelected();
            colorOverrideCheckbox.setSelected(useColorOverride);
            refreshDisplay();
        });
        colorMenu.add(colorOverrideItem);

        JMenuItem chooseColorItem = new JMenuItem("Choose Model Color...");
        chooseColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Model Color", userSelectedColor);
            if (newColor != null) {
                userSelectedColor = newColor;
                colorPickerButton.setBackground(userSelectedColor);
                colorPickerButton.setForeground(
                        getBrightness(userSelectedColor) > 128 ? Color.BLACK : Color.WHITE);
                refreshDisplay();
            }
        });
        colorMenu.add(chooseColorItem);

        viewMenu.add(colorMenu);

        // Room Menu
        JMenu roomMenu = new JMenu("Room");
        roomMenu.setMnemonic(KeyEvent.VK_R);

        JCheckBoxMenuItem showRoomItem = new JCheckBoxMenuItem("Show Room");
        showRoomItem.addActionListener(e -> {
            showRoom = showRoomItem.isSelected();
            showRoomCheckbox.setSelected(showRoom);
            refreshDisplay();
        });
        roomMenu.add(showRoomItem);

        JMenuItem roomDimensionsItem = new JMenuItem("Set Room Dimensions...");
        roomDimensionsItem.addActionListener(e -> {
            JPanel dimensionsPanel = new JPanel(new GridLayout(3, 2, 10, 5));

            JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(roomWidth, 1.0, 20.0, 0.1));
            JSpinner lengthSpinner = new JSpinner(new SpinnerNumberModel(roomLength, 1.0, 20.0, 0.1));
            JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(roomHeight, 1.0, 10.0, 0.1));

            dimensionsPanel.add(new JLabel("Width (m):"));
            dimensionsPanel.add(widthSpinner);
            dimensionsPanel.add(new JLabel("Length (m):"));
            dimensionsPanel.add(lengthSpinner);
            dimensionsPanel.add(new JLabel("Height (m):"));
            dimensionsPanel.add(heightSpinner);

            int result = JOptionPane.showConfirmDialog(
                    this, dimensionsPanel, "Room Dimensions",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                roomWidth = ((Number) widthSpinner.getValue()).floatValue();
                roomLength = ((Number) lengthSpinner.getValue()).floatValue();
                roomHeight = ((Number) heightSpinner.getValue()).floatValue();

                // Update spinner values in the tab UI
                roomWidthSpinner.setValue(roomWidth);
                roomLengthSpinner.setValue(roomLength);
                roomHeightSpinner.setValue(roomHeight);

                if (showRoom) {
                    refreshDisplay();
                }
            }
        });
        roomMenu.add(roomDimensionsItem);

        JMenu roomColorsMenu = new JMenu("Room Colors");

        JMenuItem wallColorItem = new JMenuItem("Set Wall Color...");
        wallColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Wall Color", wallColor);
            if (newColor != null) {
                wallColor = newColor;
                wallColorButton.setBackground(wallColor);
                if (showRoom)
                    refreshDisplay();
            }
        });
        roomColorsMenu.add(wallColorItem);

        JMenuItem floorColorItem = new JMenuItem("Set Floor Color...");
        floorColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Floor Color", floorColor);
            if (newColor != null) {
                floorColor = newColor;
                floorColorButton.setBackground(floorColor);
                if (showRoom)
                    refreshDisplay();
            }
        });
        roomColorsMenu.add(floorColorItem);

        JMenuItem ceilingColorItem = new JMenuItem("Set Ceiling Color...");
        ceilingColorItem.addActionListener(e -> {
            Color newColor = JColorChooser.showDialog(this, "Choose Ceiling Color", ceilingColor);
            if (newColor != null) {
                ceilingColor = newColor;
                ceilingColorButton.setBackground(ceilingColor);
                if (showRoom)
                    refreshDisplay();
            }
        });
        roomColorsMenu.add(ceilingColorItem);

        roomMenu.add(roomColorsMenu);

        // Add transparency submenu
        JMenu transparencyMenu = new JMenu("Transparency");

        JMenuItem wallTransparencyItem = new JMenuItem("Set Wall Transparency...");
        wallTransparencyItem.addActionListener(e -> {
            SpinnerNumberModel model = new SpinnerNumberModel(wallTransparency, 0.0, 1.0, 0.1);
            JSpinner spinner = new JSpinner(model);

            int result = JOptionPane.showConfirmDialog(
                    this, spinner, "Wall Transparency (0.0-1.0)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                wallTransparency = ((Number) spinner.getValue()).floatValue();
                wallTransparencySpinner.setValue(wallTransparency);
                if (showRoom)
                    refreshDisplay();
            }
        });
        transparencyMenu.add(wallTransparencyItem);

        JMenuItem floorTransparencyItem = new JMenuItem("Set Floor Transparency...");
        floorTransparencyItem.addActionListener(e -> {
            SpinnerNumberModel model = new SpinnerNumberModel(floorTransparency, 0.0, 1.0, 0.1);
            JSpinner spinner = new JSpinner(model);

            int result = JOptionPane.showConfirmDialog(
                    this, spinner, "Floor Transparency (0.0-1.0)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                floorTransparency = ((Number) spinner.getValue()).floatValue();
                floorTransparencySpinner.setValue(floorTransparency);
                if (showRoom)
                    refreshDisplay();
            }
        });
        transparencyMenu.add(floorTransparencyItem);

        JMenuItem ceilingTransparencyItem = new JMenuItem("Set Ceiling Transparency...");
        ceilingTransparencyItem.addActionListener(e -> {
            SpinnerNumberModel model = new SpinnerNumberModel(ceilingTransparency, 0.0, 1.0, 0.1);
            JSpinner spinner = new JSpinner(model);

            int result = JOptionPane.showConfirmDialog(
                    this, spinner, "Ceiling Transparency (0.0-1.0)",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                ceilingTransparency = ((Number) spinner.getValue()).floatValue();
                ceilingTransparencySpinner.setValue(ceilingTransparency);
                if (showRoom)
                    refreshDisplay();
            }
        });
        transparencyMenu.add(ceilingTransparencyItem);

        JMenuItem quickTransparencyItem = new JMenuItem("Quick Transparency Presets");
        quickTransparencyItem.addActionListener(e -> {
            String[] options = {
                    "Fully Opaque",
                    "Semi-Transparent Walls",
                    "Transparent Ceiling",
                    "Only Floor Visible",
                    "All Semi-Transparent"
            };

            int choice = JOptionPane.showOptionDialog(
                    this,
                    "Choose a transparency preset:",
                    "Transparency Presets",
                    JOptionPane.DEFAULT_OPTION,
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]);

            switch (choice) {
                case 0: // Fully Opaque
                    wallTransparency = floorTransparency = ceilingTransparency = 1.0f;
                    break;
                case 1: // Semi-Transparent Walls
                    wallTransparency = 0.5f;
                    floorTransparency = ceilingTransparency = 1.0f;
                    break;
                case 2: // Transparent Ceiling
                    ceilingTransparency = 0.3f;
                    wallTransparency = floorTransparency = 1.0f;
                    break;
                case 3: // Only Floor Visible
                    floorTransparency = 1.0f;
                    wallTransparency = ceilingTransparency = 0.2f;
                    break;
                case 4: // All Semi-Transparent
                    wallTransparency = floorTransparency = ceilingTransparency = 0.5f;
                    break;
                default:
                    return;
            }

            // Update spinners
            wallTransparencySpinner.setValue(wallTransparency);
            floorTransparencySpinner.setValue(floorTransparency);
            ceilingTransparencySpinner.setValue(ceilingTransparency);

            if (showRoom)
                refreshDisplay();
        });
        transparencyMenu.add(quickTransparencyItem);

        roomMenu.add(transparencyMenu);

        JMenuItem createRoomItem = new JMenuItem("Create/Update Room", KeyEvent.VK_C);
        createRoomItem.addActionListener(e -> {
            showRoom = true;
            showRoomItem.setSelected(true);
            showRoomCheckbox.setSelected(true);
            initializeRoomView();
        });
        roomMenu.add(createRoomItem);

        // Add Room Planning submenu
        JMenu planningMenu = new JMenu("Room Planning");

        JMenuItem addModelItem = new JMenuItem("Add Model to Room...");
        addModelItem.addActionListener(e -> addModelToRoom());
        planningMenu.add(addModelItem);

        JMenuItem removeModelItem = new JMenuItem("Remove Selected Model");
        removeModelItem.addActionListener(e -> removeSelectedModel());
        planningMenu.add(removeModelItem);

        JMenuItem duplicateModelItem = new JMenuItem("Duplicate Selected Model");
        duplicateModelItem.addActionListener(e -> duplicateSelectedModel());
        planningMenu.add(duplicateModelItem);

        planningMenu.addSeparator();

        enableObjectManipulationItem = new JCheckBoxMenuItem("Enable Object Manipulation", true);
        enableObjectManipulationItem.addActionListener(e -> {
            objectManipulationEnabled = enableObjectManipulationItem.isSelected();
        });
        planningMenu.add(enableObjectManipulationItem);

        JMenuItem controlsHelpItem = new JMenuItem("Object Manipulation Controls...");
        controlsHelpItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "<html><h3>Object Manipulation Controls</h3>" +
                            "<p><b>Mouse Controls:</b></p>" +
                            "<ul>" +
                            "<li><b>Double-click:</b> Select next object</li>" +
                            "<li><b>Ctrl+Drag:</b> Move selected object horizontally</li>" +
                            "<li><b>Ctrl+Mouse Wheel:</b> Scale selected object</li>" +
                            "</ul>" +
                            "<p><b>Keyboard Controls:</b></p>" +
                            "<ul>" +
                            "<li><b>W/S/A/D:</b> Move object along X/Z axes</li>" +
                            "<li><b>Q/E:</b> Move object up/down (Y axis)</li>" +
                            "<li><b>R/F:</b> Rotate object left/right</li>" +
                            "<li><b>Tab:</b> Select next object</li>" +
                            "<li><b>Delete:</b> Remove selected object</li>" +
                            "</ul>" +
                            "<p><i>Note: Click on the 3D view to ensure keyboard focus before using keys</i></p></html>",
                    "Object Manipulation Controls",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        planningMenu.add(controlsHelpItem);

        roomMenu.add(planningMenu);

        // Help Menu
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem aboutItem = new JMenuItem("About", KeyEvent.VK_A);
        aboutItem.addActionListener(e -> {
            JOptionPane.showMessageDialog(this,
                    "3D Model Viewer with Room Creator\n" +
                            "Version 1.0\n\n" +
                            "Features:\n" +
                            "- Load and view 3D OBJ/MTL models\n" +
                            "- Create customizable 3D rooms\n" +
                            "- Change colors and dimensions\n" +
                            "- Software rendering for compatibility",
                    "About 3D Viewer",
                    JOptionPane.INFORMATION_MESSAGE);
        });
        helpMenu.add(aboutItem);

        // Add all menus to the menu bar
        menuBar.add(fileMenu);
        menuBar.add(viewMenu);
        menuBar.add(roomMenu);
        menuBar.add(helpMenu);

        // Set the menu bar for this frame
        setJMenuBar(menuBar);
    }

    /**
     * Enables or disables the model property controls
     */
    private void setModelControlsEnabled(boolean enabled) {
        modelXSpinner.setEnabled(enabled);
        modelYSpinner.setEnabled(enabled);
        modelZSpinner.setEnabled(enabled);
        modelRotationSpinner.setEnabled(enabled);
        modelScaleSpinner.setEnabled(enabled);
        modelColorCheckbox.setEnabled(enabled);
        modelColorButton.setEnabled(enabled && modelColorCheckbox.isSelected());
    }

    /**
     * Handles when a model is selected in the list
     */
    private void handleModelSelection() {
        int index = modelsList.getSelectedIndex();
        if (index >= 0 && index < roomModels.size()) {
            selectedModel = roomModels.get(index);
            selectedModelIndex = index;

            // Update UI controls with model properties
            modelXSpinner.setValue(Double.valueOf(selectedModel.x));
            modelYSpinner.setValue(Double.valueOf(selectedModel.y));
            modelZSpinner.setValue(Double.valueOf(selectedModel.z));
            modelRotationSpinner.setValue(Double.valueOf(selectedModel.rotY));
            modelScaleSpinner.setValue(Double.valueOf(selectedModel.scale));

            // Update color checkbox and button
            modelColorCheckbox.setSelected(selectedModel.useCustomColor);
            modelColorButton.setBackground(selectedModel.customColor);
            modelColorButton.setEnabled(selectedModel.useCustomColor);

            setModelControlsEnabled(true);
        } else {
            selectedModel = null;
            selectedModelIndex = -1;
            setModelControlsEnabled(false);
        }

        refreshDisplay();
    }

    /**
     * Adds a model to the room
     */
    private void addModelToRoom() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter("OBJ Files", "obj"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String modelName = file.getName();

            // Create a new model
            Model3D model = new Model3D(modelName);

            // Load OBJ into the model
            if (loadModelFromObjFile(model, file.getAbsolutePath())) {
                // Position model in room
                model.y = -roomHeight / 2 + 0.001f; // Place just above floor

                // Add to models list
                roomModels.add(model);
                modelsListModel.addElement(modelName);

                // Select the new model
                int newIndex = roomModels.size() - 1;
                modelsList.setSelectedIndex(newIndex);

                // Make sure room is visible
                if (!showRoom) {
                    showRoom = true;
                    showRoomCheckbox.setSelected(true);
                    // Update menu item if it exists
                    for (int i = 0; i < getJMenuBar().getMenu(2).getItemCount(); i++) {
                        if (getJMenuBar().getMenu(2).getItem(i) instanceof JCheckBoxMenuItem &&
                                "Show Room".equals(getJMenuBar().getMenu(2).getItem(i).getText())) {
                            ((JCheckBoxMenuItem) getJMenuBar().getMenu(2).getItem(i)).setSelected(true);
                            break;
                        }
                    }
                }

                refreshDisplay();
            }
        }
    }

    /**
     * Removes the selected model from the room
     */
    private void removeSelectedModel() {
        int index = modelsList.getSelectedIndex();
        if (index >= 0) {
            roomModels.remove(index);
            modelsListModel.remove(index);

            // Update selection
            if (!roomModels.isEmpty()) {
                modelsList.setSelectedIndex(Math.min(index, roomModels.size() - 1));
            } else {
                handleModelSelection(); // Will reset selection state
            }

            refreshDisplay();
        }
    }

}