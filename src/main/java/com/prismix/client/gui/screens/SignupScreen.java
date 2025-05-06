package com.prismix.client.gui.screens;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.components.themed.ThemedTextField;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class SignupScreen extends ThemedPanel {
    private ThemedTextField usernameField;
    private ThemedTextField displayNameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel avatarPreview;
    private byte[] avatarData;
    
    public SignupScreen() {
        setLayout(new BorderLayout());
        
        // Create title panel
        JPanel titlePanel = new ThemedPanel();
        titlePanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new ThemedLabel("Create Your Account", ThemedLabel.Size.TITLE);
        titlePanel.add(titleLabel);
        titlePanel.setBorder(new EmptyBorder(20, 0, 20, 0));
        
        // Create form panel
        JPanel formPanel = new ThemedPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // Username field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel usernameLabel = new ThemedLabel("Username:", ThemedLabel.Size.DEFAULT);
        formPanel.add(usernameLabel, gbc);
        
        gbc.gridx = 1;
        usernameField = new ThemedTextField();
        formPanel.add(usernameField, gbc);
        
        // Display name field
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel displayNameLabel = new ThemedLabel("Display Name:", ThemedLabel.Size.DEFAULT);
        formPanel.add(displayNameLabel, gbc);
        
        gbc.gridx = 1;
        displayNameField = new ThemedTextField();
        formPanel.add(displayNameField, gbc);
        
        // Avatar selection
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel avatarLabel = new ThemedLabel("Profile Picture:", ThemedLabel.Size.DEFAULT);
        formPanel.add(avatarLabel, gbc);
        
        gbc.gridx = 1;
        JPanel avatarPanel = new ThemedPanel();
        avatarPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
        
        // Avatar preview panel (64x64)
        avatarPreview = new JLabel();
        avatarPreview.setPreferredSize(new Dimension(64, 64));
        avatarPreview.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        avatarPreview.setBackground(Color.LIGHT_GRAY);
        avatarPreview.setOpaque(true);
        
        // Choose avatar button
        ThemedButton chooseAvatarButton = new ThemedButton("Choose Image");
        chooseAvatarButton.addActionListener(e -> selectAvatar());
        
        avatarPanel.add(avatarPreview);
        avatarPanel.add(chooseAvatarButton);
        formPanel.add(avatarPanel, gbc);
        
        // Create buttons panel
        JPanel buttonsPanel = new ThemedPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));
        
        ThemedButton signupButton = new ThemedButton("Sign Up");
        signupButton.addActionListener(e -> handleSignup());
        
        ThemedButton cancelButton = new ThemedButton("Cancel");
        cancelButton.addActionListener(e -> goBackToLogin());
        
        buttonsPanel.add(signupButton);
        buttonsPanel.add(cancelButton);
        
        // Add all panels to main layout
        add(titlePanel, BorderLayout.NORTH);
        add(formPanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void selectAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Profile Picture");
        fileChooser.setFileFilter(new FileNameExtensionFilter("Image files", "jpg", "jpeg", "png", "gif"));
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                // Read image and resize it
                BufferedImage originalImage = ImageIO.read(selectedFile);
                BufferedImage resizedImage = resizeImage(originalImage, 64, 64);
                
                // Update preview
                avatarPreview.setIcon(new ImageIcon(resizedImage));
                
                // Convert to byte array for storage
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(resizedImage, "png", baos);
                avatarData = baos.toByteArray();
                
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, 
                        "Error loading image: " + ex.getMessage(), 
                        "Image Error", 
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resizedImage;
    }
    
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String displayName = displayNameField.getText().trim();

        // Validate inputs
        if (username.isEmpty() || displayName.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                    "Username, display name",
                    "Registration Error", 
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.length() > 30) {
            JOptionPane.showMessageDialog(this,
                    "Username can only be 30 characters long",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;

        }

        if (displayName.length() > 50) {
            JOptionPane.showMessageDialog(this,
                    "Display can only be 50 characters long",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (username.contains(" ")) {
            JOptionPane.showMessageDialog(this,
                    "Username can't have spaces",
                    "Registration Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // If no avatar selected, create a default one with initials
        if (avatarData == null) {
            avatarData = createDefaultAvatar(displayName);
        }
        
        // Send signup request
        ApplicationContext.getUserHandler().signup(username, displayName, avatarData);
    }
    
    private byte[] createDefaultAvatar(String displayName) {
        try {
            // Create a blank image with user's initials
            BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = image.createGraphics();
            
            // Fill background with a random color
            int r = 100 + (int)(Math.random() * 155);
            int g2 = 100 + (int)(Math.random() * 155);
            int b = 100 + (int)(Math.random() * 155);
            g.setColor(new Color(r, g2, b));
            g.fillRect(0, 0, 64, 64);
            
            // Add text
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.BOLD, 24));
            
            // Get initials from display name
            String initials = displayName.length() > 0 ? 
                    displayName.substring(0, 1).toUpperCase() : "?";
            if (displayName.contains(" ")) {
                String[] parts = displayName.split(" ");
                if (parts.length > 1 && parts[1].length() > 0) {
                    initials += parts[1].substring(0, 1).toUpperCase();
                }
            }
            
            // Center text
            FontMetrics metrics = g.getFontMetrics();
            int x = (64 - metrics.stringWidth(initials)) / 2;
            int y = ((64 - metrics.getHeight()) / 2) + metrics.getAscent();
            
            g.drawString(initials, x, y);
            g.dispose();
            
            // Convert to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "png", baos);
            return baos.toByteArray();
            
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        }
    }
    
    private void goBackToLogin() {
        ApplicationContext.getEventBus().publish(new ApplicationEvent(
                ApplicationEvent.Type.SWITCH_SCREEN,
                MainFrame.AppScreen.LOGIN_SCREEN
        ));
    }
} 