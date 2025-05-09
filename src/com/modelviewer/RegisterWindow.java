package com.modelviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class RegisterWindow extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JTextField emailField;
    private Color accentColor = new Color(46, 204, 113);
    private BufferedImage backgroundImage;
    
    public RegisterWindow() {
        setTitle("Register - 3D Model Viewer");
        setSize(500, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true);
        
        // Try to load background image
        try {
            backgroundImage = ImageIO.read(new File("resources/register.jpg"));
        } catch (Exception e) {
            System.out.println("Register background image not found, using gradient background");
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
                        0, 0, new Color(46, 204, 113),
                        0, getHeight(), new Color(27, 188, 155)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
            }
        };
        mainPanel.setLayout(null);
        
        // Title
        JLabel titleLabel = new JLabel("Register");
        titleLabel.setFont(new Font("Segoe UI Light", Font.PLAIN, 36));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setBounds(50, 40, 400, 50);
        mainPanel.add(titleLabel);
        
        // Username field
        usernameField = createStyledTextField("Username");
        usernameField.setBounds(50, 150, 400, 45);
        mainPanel.add(usernameField);
        
        // Email field
        emailField = createStyledTextField("Email");
        emailField.setBounds(50, 220, 400, 45);
        mainPanel.add(emailField);
        
        // Password field
        passwordField = createStyledPasswordField("Password");
        passwordField.setBounds(50, 290, 400, 45);
        mainPanel.add(passwordField);
        
        // Confirm Password field
        confirmPasswordField = createStyledPasswordField("Confirm Password");
        confirmPasswordField.setBounds(50, 360, 400, 45);
        mainPanel.add(confirmPasswordField);
        
        // Register button
        JButton registerButton = createStyledButton("Register");
        registerButton.setBounds(50, 440, 400, 45);
        registerButton.addActionListener(e -> handleRegistration());
        mainPanel.add(registerButton);
        
        // Back button
        JButton backButton = createStyledButton("Back to Welcome");
        backButton.setBounds(50, 510, 400, 45);
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
    
    private void handleRegistration() {
        String username = usernameField.getText();
        String email = emailField.getText();
        String password = new String(passwordField.getPassword());
        String confirmPassword = new String(confirmPasswordField.getPassword());
        
        // Validate input
        if (username.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() ||
            username.equals("Username") || email.equals("Email") || 
            password.equals("Password") || confirmPassword.equals("Confirm Password")) {
            JOptionPane.showMessageDialog(this,
                "Please fill in all fields",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Validate email format
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            JOptionPane.showMessageDialog(this,
                "Please enter a valid email address",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check password match
        if (!password.equals(confirmPassword)) {
            JOptionPane.showMessageDialog(this,
                "Passwords do not match",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check username availability
        if (!DatabaseHelper.isUsernameAvailable(username)) {
            JOptionPane.showMessageDialog(this,
                "Username is already taken",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check email availability
        if (!DatabaseHelper.isEmailAvailable(email)) {
            JOptionPane.showMessageDialog(this,
                "Email is already registered",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Register user
        if (DatabaseHelper.registerUser(username, password, email)) {
            JOptionPane.showMessageDialog(this,
                "Registration successful! Please login.",
                "Success",
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
            new LoginWindow().setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this,
                "Registration failed. Please try again.",
                "Registration Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
} 