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

}