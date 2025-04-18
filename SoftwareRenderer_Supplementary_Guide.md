# SoftwareRenderer Supplementary Guide

## Build and Run Instructions

### Compilation
```bash
# Compile all source files
javac -cp "lib/*" -d bin src/com/modelviewer/*.java

# Create JAR file
jar cvf ModelViewer.jar -C bin .
```

### Running the Application
```bash
# Run from compiled classes
java -cp "bin;lib/*" com.modelviewer.SoftwareRenderer

# Run from JAR
java -jar ModelViewer.jar
```

### Maven Configuration
```xml
<dependencies>
    <dependency>
        <groupId>org.jogamp.jogl</groupId>
        <artifactId>jogl-all-main</artifactId>
        <version>2.3.2</version>
    </dependency>
    <dependency>
        <groupId>org.jogamp.gluegen</groupId>
        <artifactId>gluegen-rt-main</artifactId>
        <version>2.3.2</version>
    </dependency>
</dependencies>
```

## ModelManagerWindow Implementation

```java
public class ModelManagerWindow extends JFrame {
    private SoftwareRenderer parent;
    private DefaultListModel<String> modelsListModel;
    private JList<String> modelsList;
    
    public ModelManagerWindow(SoftwareRenderer parent) {
        super("Model Manager");
        this.parent = parent;
        
        setSize(400, 300);
        setLocationRelativeTo(parent);
        
        // Create UI components
        modelsListModel = new DefaultListModel<>();
        modelsList = new JList<>(modelsListModel);
        
        // Add controls
        JPanel controlPanel = new JPanel();
        JButton addButton = new JButton("Add Model");
        JButton removeButton = new JButton("Remove");
        JButton duplicateButton = new JButton("Duplicate");
        
        // Layout setup
        setLayout(new BorderLayout());
        add(new JScrollPane(modelsList), BorderLayout.CENTER);
        add(controlPanel, BorderLayout.SOUTH);
        
        // Event handlers
        addButton.addActionListener(e -> addModel());
        removeButton.addActionListener(e -> removeModel());
        duplicateButton.addActionListener(e -> duplicateModel());
    }
    
    private void addModel() {
        // Implementation for adding new model
    }
    
    private void removeModel() {
        // Implementation for removing selected model
    }
    
    private void duplicateModel() {
        // Implementation for duplicating selected model
    }
}
```

## Advanced Material Handling

### Texture Loading
```java
private void loadTexture(String texturePath) {
    try {
        // Load texture image
        BufferedImage image = ImageIO.read(new File(texturePath));
        
        // Convert to OpenGL texture
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        GLDrawableFactory factory = GLDrawableFactory.getFactory(glProfile);
        
        // Create offscreen drawable
        GLOffscreenAutoDrawable drawable = factory.createOffscreenAutoDrawable(
            null, glCapabilities, null, 1, 1);
        drawable.display();
        
        // Generate texture
        GL2 gl = drawable.getGL().getGL2();
        int[] textureIds = new int[1];
        gl.glGenTextures(1, textureIds, 0);
        
        // Bind and setup texture
        gl.glBindTexture(GL.GL_TEXTURE_2D, textureIds[0]);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MIN_FILTER, GL.GL_LINEAR);
        gl.glTexParameteri(GL.GL_TEXTURE_2D, GL.GL_TEXTURE_MAG_FILTER, GL.GL_LINEAR);
        
        // Upload texture data
        byte[] imageData = ((DataBufferByte) image.getRaster().getDataBuffer()).getData();
        gl.glTexImage2D(GL.GL_TEXTURE_2D, 0, GL.GL_RGBA, 
            image.getWidth(), image.getHeight(), 0, 
            GL.GL_RGBA, GL.GL_UNSIGNED_BYTE, ByteBuffer.wrap(imageData));
            
    } catch (IOException e) {
        e.printStackTrace();
    }
}
```

### Enhanced Material System
```java
class EnhancedMaterial extends Material {
    private int textureId = -1;
    private float transparency = 1.0f;
    private boolean hasSpecularMap = false;
    private boolean hasNormalMap = false;
    
    public void applyMaterial(GL2 gl) {
        // Apply basic material properties
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, ambient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, diffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, specular, 0);
        gl.glMaterialf(GL.GL_FRONT_AND_BACK, GL2.GL_SHININESS, shininess);
        
        // Apply texture if available
        if (textureId != -1) {
            gl.glEnable(GL.GL_TEXTURE_2D);
            gl.glBindTexture(GL.GL_TEXTURE_2D, textureId);
        }
        
        // Handle transparency
        if (transparency < 1.0f) {
            gl.glEnable(GL.GL_BLEND);
            gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        }
    }
}
```

## Room Planning Features

### Room Grid System
```java
class RoomGrid {
    private float cellSize = 0.5f; // Grid cell size in meters
    private boolean[][] occupiedCells;
    
    public RoomGrid(float roomWidth, float roomLength) {
        int gridWidth = (int)(roomWidth / cellSize);
        int gridLength = (int)(roomLength / cellSize);
        occupiedCells = new boolean[gridWidth][gridLength];
    }
    
    public boolean canPlaceModel(Model3D model, float x, float z) {
        // Check if model fits in grid position
        int gridX = (int)(x / cellSize);
        int gridZ = (int)(z / cellSize);
        
        // Calculate model bounds
        float modelWidth = model.maxX - model.minX;
        float modelLength = model.maxZ - model.minZ;
        
        int cellsNeededX = (int)(modelWidth / cellSize) + 1;
        int cellsNeededZ = (int)(modelLength / cellSize) + 1;
        
        // Check if space is available
        for (int i = gridX; i < gridX + cellsNeededX; i++) {
            for (int j = gridZ; j < gridZ + cellsNeededZ; j++) {
                if (i >= occupiedCells.length || j >= occupiedCells[0].length || 
                    occupiedCells[i][j]) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    public void placeModel(Model3D model, float x, float z) {
        // Mark cells as occupied
        int gridX = (int)(x / cellSize);
        int gridZ = (int)(z / cellSize);
        
        float modelWidth = model.maxX - model.minX;
        float modelLength = model.maxZ - model.minZ;
        
        int cellsNeededX = (int)(modelWidth / cellSize) + 1;
        int cellsNeededZ = (int)(modelLength / cellSize) + 1;
        
        for (int i = gridX; i < gridX + cellsNeededX; i++) {
            for (int j = gridZ; j < gridZ + cellsNeededZ; j++) {
                if (i < occupiedCells.length && j < occupiedCells[0].length) {
                    occupiedCells[i][j] = true;
                }
            }
        }
    }
}
```

### Advanced Room Features

```java
class RoomFeatures {
    // Lighting zones
    private List<LightZone> lightZones = new ArrayList<>();
    
    // Wall features (windows, doors)
    private List<WallFeature> wallFeatures = new ArrayList<>();
    
    // Room divisions
    private List<RoomDivider> dividers = new ArrayList<>();
    
    public void addLightZone(float x, float y, float z, float radius, float intensity) {
        lightZones.add(new LightZone(x, y, z, radius, intensity));
    }
    
    public void addWindow(float x, float y, float width, float height) {
        wallFeatures.add(new Window(x, y, width, height));
    }
    
    public void addDoor(float x, float y, float width, float height) {
        wallFeatures.add(new Door(x, y, width, height));
    }
    
    public void render(GL2 gl) {
        // Render all room features
        for (LightZone zone : lightZones) {
            zone.render(gl);
        }
        
        for (WallFeature feature : wallFeatures) {
            feature.render(gl);
        }
        
        for (RoomDivider divider : dividers) {
            divider.render(gl);
        }
    }
}
```

## Performance Optimization

### View Frustum Culling
```java
class ViewFrustum {
    private float[] planes = new float[24]; // 6 planes, 4 coefficients each
    
    public void update(GL2 gl) {
        // Get current modelview and projection matrices
        float[] modelview = new float[16];
        float[] projection = new float[16];
        gl.glGetFloatv(GL2.GL_MODELVIEW_MATRIX, modelview, 0);
        gl.glGetFloatv(GL2.GL_PROJECTION_MATRIX, projection, 0);
        
        // Calculate frustum planes
        calculatePlanes(modelview, projection);
    }
    
    public boolean isVisible(Model3D model) {
        // Check if model's bounding box intersects with frustum
        float[] bbox = {
            model.minX, model.minY, model.minZ,
            model.maxX, model.maxY, model.maxZ
        };
        
        return checkBoundingBox(bbox);
    }
    
    private boolean checkBoundingBox(float[] bbox) {
        // Implementation of bounding box check against frustum planes
        return true; // Placeholder
    }
}
```

### Level of Detail (LOD)
```java
class ModelLOD {
    private List<Model3D> lodLevels = new ArrayList<>();
    private float[] distanceThresholds;
    
    public void addLODLevel(Model3D model, float distance) {
        lodLevels.add(model);
        // Update distance thresholds
    }
    
    public Model3D getAppropriateModel(float viewerDistance) {
        // Select appropriate LOD based on distance
        for (int i = 0; i < distanceThresholds.length; i++) {
            if (viewerDistance <= distanceThresholds[i]) {
                return lodLevels.get(i);
            }
        }
        return lodLevels.get(lodLevels.size() - 1);
    }
}
```

## Additional Features

### Color Override System
```java
class ColorOverride {
    private boolean enabled = false;
    private Color overrideColor = Color.RED;
    private float[] overrideAmbient = new float[4];
    private float[] overrideDiffuse = new float[4];
    private float[] overrideSpecular = new float[4];
    
    public void apply(GL2 gl) {
        if (!enabled) return;
        
        // Convert color to OpenGL format
        overrideAmbient[0] = overrideColor.getRed() / 255.0f * 0.3f;
        overrideAmbient[1] = overrideColor.getGreen() / 255.0f * 0.3f;
        overrideAmbient[2] = overrideColor.getBlue() / 255.0f * 0.3f;
        overrideAmbient[3] = 1.0f;
        
        // Apply override material
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_AMBIENT, overrideAmbient, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_DIFFUSE, overrideDiffuse, 0);
        gl.glMaterialfv(GL.GL_FRONT_AND_BACK, GL2.GL_SPECULAR, overrideSpecular, 0);
    }
}
```

### Advanced Transparency System
```java
class TransparencyManager {
    private List<TransparentObject> transparentObjects = new ArrayList<>();
    
    public void addTransparentObject(Model3D model, float alpha) {
        transparentObjects.add(new TransparentObject(model, alpha));
    }
    
    public void render(GL2 gl) {
        // Sort transparent objects back-to-front
        Collections.sort(transparentObjects, 
            (a, b) -> Float.compare(b.getDistanceToCamera(), a.getDistanceToCamera()));
        
        // Enable blending
        gl.glEnable(GL.GL_BLEND);
        gl.glBlendFunc(GL.GL_SRC_ALPHA, GL.GL_ONE_MINUS_SRC_ALPHA);
        
        // Render transparent objects
        for (TransparentObject obj : transparentObjects) {
            obj.render(gl);
        }
        
        gl.glDisable(GL.GL_BLEND);
    }
}
```

## Testing and Debugging

### Performance Testing
```java
class PerformanceMonitor {
    private long frameStartTime;
    private int frameCount;
    private float fps;
    private List<Long> frameTimes = new ArrayList<>();
    
    public void startFrame() {
        frameStartTime = System.nanoTime();
    }
    
    public void endFrame() {
        long frameTime = System.nanoTime() - frameStartTime;
        frameTimes.add(frameTime);
        frameCount++;
        
        if (frameCount >= 60) {
            calculateAverageFPS();
            frameCount = 0;
            frameTimes.clear();
        }
    }
    
    private void calculateAverageFPS() {
        long totalTime = 0;
        for (Long time : frameTimes) {
            totalTime += time;
        }
        float averageFrameTime = totalTime / (float)frameTimes.size();
        fps = 1_000_000_000.0f / averageFrameTime;
    }
}
``` 