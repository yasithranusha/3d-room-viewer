package com.modelviewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class WelcomeScreen extends JFrame {
    private Timer fadeTimer;
    private float opacity = 0f;
    private JButton loginButton;
    private JButton registerButton;
    private BufferedImage backgroundImage;
    
    public WelcomeScreen() {
        setTitle("3D Model Viewer - Welcome");
        setSize(800, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setUndecorated(true); // Remove window decorations for modern look
        
        // Set up the main panel with custom painting
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw background image if available
                if (backgroundImage != null) {
                    g2d.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
                } else {
                    // Fallback gradient background
                    GradientPaint gradient = new GradientPaint(
                        0, 0, new Color(41, 128, 185),
                        0, getHeight(), new Color(44, 62, 80)
                    );
                    g2d.setPaint(gradient);
                    g2d.fillRect(0, 0, getWidth(), getHeight());
                }
                
                // Add semi-transparent overlay for better text readability
                g2d.setColor(new Color(0, 0, 0, 150));
                g2d.fillRect(0, 0, getWidth(), getHeight());
                
                // Set font and color for text with fade effect
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, opacity));
                g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI Light", Font.PLAIN, 48));
                
                // Draw main title
                String title = "3D Model Viewer";
                FontMetrics fm = g2d.getFontMetrics();
                int titleWidth = fm.stringWidth(title);
                g2d.drawString(title, (getWidth() - titleWidth) / 2, getHeight() / 3);
                
                // Draw subtitle
                g2d.setFont(new Font("Segoe UI Light", Font.PLAIN, 24));
                String subtitle = "Interactive 3D Model Visualization";
                fm = g2d.getFontMetrics();
                int subtitleWidth = fm.stringWidth(subtitle);
                g2d.drawString(subtitle, (getWidth() - subtitleWidth) / 2, getHeight() / 3 + 50);
                
                g2d.dispose();
            }
        };
        mainPanel.setLayout(null); // Use absolute positioning
        
        // Create login button
        loginButton = createStyledButton("Login", new Color(52, 152, 219));
        loginButton.setBounds(250, 400, 140, 50);
        loginButton.addActionListener(e -> {
            fadeOut();
            new LoginWindow().setVisible(true);
        });
        mainPanel.add(loginButton);
        
        // Create register button
        registerButton = createStyledButton("Register", new Color(46, 204, 113));
        registerButton.setBounds(410, 400, 140, 50);
        registerButton.addActionListener(e -> {
            fadeOut();
            new RegisterWindow().setVisible(true);
        });
        mainPanel.add(registerButton);
        
        // Close button
        JButton closeButton = new JButton("×");
        closeButton.setForeground(Color.WHITE);
        closeButton.setFont(new Font("Arial", Font.PLAIN, 18));
        closeButton.setBounds(760, 10, 30, 30);
        closeButton.setContentAreaFilled(false);
        closeButton.setBorderPainted(false);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> System.exit(0));
        mainPanel.add(closeButton);
        
        // Try to load background image
        try {
            backgroundImage = ImageIO.read(new File("resources/welcome_screen.jpg"));
        } catch (Exception e) {
            // Silently fail if image not found - gradient will be used
            System.out.println("Background image not found, using gradient background");
        }
        
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
        
        // Set up fade-in effect
        fadeTimer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                opacity += 0.1f;
                if (opacity >= 1f) {
                    opacity = 1f;
                    fadeTimer.stop();
                }
                repaint();
            }
        });
        
        fadeTimer.start();
    }
    
    private JButton createStyledButton(String text, Color baseColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, baseColor,
                    0, getHeight(), baseColor.darker()
                );
                g2d.setPaint(gradient);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                
                // Draw text
                g2d.setColor(Color.WHITE);
                g2d.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                FontMetrics fm = g2d.getFontMetrics();
                int textWidth = fm.stringWidth(getText());
                int textHeight = fm.getHeight();
                g2d.drawString(getText(),
                    (getWidth() - textWidth) / 2,
                    (getHeight() + textHeight / 2) / 2 - 2);
            }
        };
        
        button.addMouseListener(new MouseAdapter() {
            public void mouseEntered(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
            
            public void mouseExited(MouseEvent e) {
                button.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
            }
        });
        
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setFocusPainted(false);
        
        return button;
    }
    
    private void fadeOut() {
        Timer fadeOutTimer = new Timer(50, new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                opacity -= 0.1f;
                if (opacity <= 0f) {
                    opacity = 0f;
                    ((Timer)e.getSource()).stop();
                    dispose();
                }
                repaint();
            }
        });
        fadeOutTimer.start();
    }
    
    public static void main(String[] args) {
        try {
            // Set system look and feel
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        SwingUtilities.invokeLater(() -> {
            new WelcomeScreen().setVisible(true);
        });
    }
} 