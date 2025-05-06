package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.components.themed.ThemedTextField;
import com.prismix.client.handlers.ApplicationContext;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

public class CreateRoomDialog extends JDialog {
    private ThemedTextField roomNameField;
    private JLabel avatarPreview;
    private byte[] avatarData;
    private boolean isConfirmed = false;

    public CreateRoomDialog(Frame parent) {
        super(parent, "Create a New Room", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel mainPanel = new ThemedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new ThemedLabel("Create a New Room", ThemedLabel.Size.TITLE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new ThemedPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Room name field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel roomNameLabel = new ThemedLabel("Room Name:", ThemedLabel.Size.DEFAULT);
        formPanel.add(roomNameLabel, gbc);

        gbc.gridx = 1;
        roomNameField = new ThemedTextField();
        formPanel.add(roomNameField, gbc);

        // Avatar selection
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel avatarLabel = new ThemedLabel("Room Avatar:", ThemedLabel.Size.DEFAULT);
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

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new ThemedPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        ThemedButton createButton = new ThemedButton("Create");
        createButton.addActionListener(e -> {
            if (validateForm()) {
                isConfirmed = true;
                dispose();
            }
        });

        ThemedButton cancelButton = new ThemedButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonsPanel.add(createButton);
        buttonsPanel.add(cancelButton);
        
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
        setSize(400, 300);
    }

    private boolean validateForm() {
        String roomName = roomNameField.getText().trim();
        
        if (roomName.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Room name is required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        if (roomName.length() > 30) {
            JOptionPane.showMessageDialog(this,
                    "Room name can only be 30 characters long.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }

        // If no avatar selected, create a default one
        if (avatarData == null) {
            avatarData = createDefaultAvatar(roomName);
        }
        
        return true;
    }

    private void selectAvatar() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select Room Avatar");
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

    private byte[] createDefaultAvatar(String roomName) {
        try {
            // Create a blank image with the room's first letter
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
            
            // Get first letter from room name
            String letter = roomName.length() > 0 ? 
                    roomName.substring(0, 1).toUpperCase() : "R";
            
            // Center text
            FontMetrics metrics = g.getFontMetrics();
            int x = (64 - metrics.stringWidth(letter)) / 2;
            int y = ((64 - metrics.getHeight()) / 2) + metrics.getAscent();
            
            g.drawString(letter, x, y);
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

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public String getRoomName() {
        return roomNameField.getText().trim();
    }

    public byte[] getAvatarData() {
        return avatarData;
    }
} 