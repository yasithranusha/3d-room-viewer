package com.modelviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class LoginWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private Color accentColor = new Color(52, 152, 219);
    private BufferedImage backgroundImage;
    
    public LoginWindow() {
        setTitle("Login - 3D Model Viewer");
        setSize(500, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        
        // Try to load background image
        try {
            backgroundImage = ImageIO.read(new File("resources/login.jpg"));
        } catch (Exception e) {
            System.out.println("Login background image not found, using gradient background");
        }
        
        // Main panel with background image or gradient
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (backgroundImage != null) {
                    // Draw background image
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                    
                    // Add semi-transparent overlay for better text readability
                    g2d.setColor(new Color(0, 0, 0, 150));
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                } else {
                    // Fallback gradient background
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        0, getHeight(), new Color(44, 62, 80)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        
        // Title
        JLabel titleLabel = new JLabel("Login");
        titleLabel.setFont(new Font("Segoe UI Light", Font.PLAIN, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(50, 40, 400, 50);
        mainPanel.add(titleLabel);
        
        // Username field
        usernameField = createStyledTextField("Username");
        usernameField.setBounds(50, 150, 400, 45);
        mainPanel.add(usernameField);
        
        // Password field
        passwordField = createStyledPasswordField("Password");
        passwordField.setBounds(50, 220, 400, 45);
        mainPanel.add(passwordField);
        
        // Login button
        JButton loginButton = createStyledButton("Login");
        loginButton.setBounds(50, 300, 400, 45);
        loginButton.addActionListener(e -> handleLogin());
        mainPanel.add(loginButton);
        
        // Back button
        JButton backButton = createStyledButton("Back to Welcome");
        backButton.setBounds(50, 370, 400, 45);
        backButton.addActionListener(e -> {
            dispose();
            new WelcomeScreen().setVisible(true);
        });
        mainPanel.add(backButton);
        
        // Close button with X icon
        JLabel closeButton = new JLabel("✕");
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.PLAIN, 18));
        closeButton.setBounds(460, 10, 30, 30);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                dispose();
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                closeButton.setForeground(new Color(255, 255, 255, 200));
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                closeButton.setForeground(Color.WHITE);
            }
        });
        mainPanel.add(closeButton);
        
        // Add main panel
        add(mainPanel);
        
        // Make window draggable
        addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                if (e.getY() <= 50) { // Only drag from top area
                    setComponentZOrder(mainPanel, 0);
                    Point point = e.getPoint();
                    getComponentAt(point);
                }
            }
        });
        
        addMouseMotionListener(new MouseMotionAdapter() {
            public void mouseDragged(MouseEvent e) {
                if (e.getY() <= 50) { // Only drag from top area
                    Point p = getLocation();
                    setLocation(p.x + e.getX() - getWidth() / 2, p.y + e.getY() - 25);
                }
            }
        });
    }
    
    private JTextField createStyledTextField(String placeholder) {
        JTextField field = new JTextField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                super.paintComponent(g);
            }
        };
        
        field.setOpaque(false);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add placeholder
        field.setText(placeholder);
        field.setForeground(new Color(200, 200, 200));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (field.getText().equals(placeholder)) {
                    field.setText("");
                    field.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getText().isEmpty()) {
                    field.setText(placeholder);
                    field.setForeground(new Color(200, 200, 200));
                }
            }
        });
        
        return field;
    }
    
    private JPasswordField createStyledPasswordField(String placeholder) {
        JPasswordField field = new JPasswordField() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background
                g2d.setColor(new Color(255, 255, 255, 20));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                
                super.paintComponent(g);
            }
        };
        
        field.setOpaque(false);
        field.setForeground(Color.WHITE);
        field.setCaretColor(Color.WHITE);
        field.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        field.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        // Add placeholder
        field.setEchoChar((char) 0);
        field.setText(placeholder);
        field.setForeground(new Color(200, 200, 200));
        
        field.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (String.valueOf(field.getPassword()).equals(placeholder)) {
                    field.setText("");
                    field.setEchoChar('●');
                    field.setForeground(Color.WHITE);
                }
            }
            
            @Override
            public void focusLost(FocusEvent e) {
                if (field.getPassword().length == 0) {
                    field.setEchoChar((char) 0);
                    field.setText(placeholder);
                    field.setForeground(new Color(200, 200, 200));
                }
            }
        });
        
        return field;
    }
    
    private JButton createStyledButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, accentColor,
                    0, getHeight(), accentColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fill(new RoundRectangle2D.Float(0, 0, getWidth(), getHeight(), 10, 10));
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(getFont());
                FontMetrics fm = g2d.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() + fm.getAscent() - fm.getDescent()) / 2;
                g2d.drawString(getText(), x, y);
            }
        };
        
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorderPainted(false);
        button.setContentAreaFilled(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        return button;
    }
    
    private void handleLogin() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());
        
        if (username.isEmpty() || password.isEmpty() || 
            username.equals("Username") || password.equals("Password")) {
            JOptionPane.showMessageDialog(this,
                "Please enter both username and password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (DatabaseHelper.validateLogin(username, password)) {
            dispose();
            SwingUtilities.invokeLater(() -> {
                try {
                    // Set software rendering properties
                    System.setProperty("jogl.disable.openglcore", "true");
                    System.setProperty("sun.java2d.noddraw", "true");
                    System.setProperty("sun.java2d.opengl", "false");
                    System.setProperty("jogl.gljpanel.nohw", "true");
                    
                    // Launch main application
                    SoftwareRenderer renderer = new SoftwareRenderer();
                    renderer.setSize(1024, 768);
                    renderer.setLocationRelativeTo(null);
                    renderer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                    renderer.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null,
                        "Error launching application: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            });
        } else {
            JOptionPane.showMessageDialog(this,
                "Invalid username or password",
                "Login Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 