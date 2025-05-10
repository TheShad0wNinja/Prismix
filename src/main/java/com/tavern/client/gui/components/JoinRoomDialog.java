package com.tavern.client.gui.components;

import com.tavern.client.gui.components.themed.ThemedButton;
import com.tavern.client.gui.components.themed.ThemedLabel;
import com.tavern.client.gui.components.themed.ThemedPanel;
import com.tavern.client.gui.components.themed.ThemedTextField;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class JoinRoomDialog extends JDialog {
    private ThemedTextField roomIdField;
    private boolean isConfirmed = false;

    public JoinRoomDialog(Frame parent) {
        super(parent, "Join a Room", true);
        initComponents();
        pack();
        setLocationRelativeTo(parent);
    }

    private void initComponents() {
        JPanel mainPanel = new ThemedPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        // Title
        JLabel titleLabel = new ThemedLabel("Join an Existing Room", ThemedLabel.Size.TITLE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Form panel
        JPanel formPanel = new ThemedPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Room ID field
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel roomIdLabel = new ThemedLabel("Room ID:", ThemedLabel.Size.DEFAULT);
        formPanel.add(roomIdLabel, gbc);

        gbc.gridx = 1;
        roomIdField = new ThemedTextField();
        formPanel.add(roomIdField, gbc);

        // Help text
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.gridwidth = 2;
        JLabel helpLabel = new ThemedLabel("Enter the ID of the room you want to join", ThemedLabel.Size.SMALLER);
        formPanel.add(helpLabel, gbc);

        mainPanel.add(formPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonsPanel = new ThemedPanel();
        buttonsPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 10));

        ThemedButton joinButton = new ThemedButton("Join");
        joinButton.addActionListener(e -> {
            if (validateForm()) {
                isConfirmed = true;
                dispose();
            }
        });

        ThemedButton cancelButton = new ThemedButton("Cancel");
        cancelButton.addActionListener(e -> dispose());

        buttonsPanel.add(joinButton);
        buttonsPanel.add(cancelButton);
        
        mainPanel.add(buttonsPanel, BorderLayout.SOUTH);

        setContentPane(mainPanel);
        setResizable(false);
        setSize(350, 220);
    }

    private boolean validateForm() {
        String roomIdText = roomIdField.getText().trim();
        
        if (roomIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Room ID is required.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        try {
            int roomId = Integer.parseInt(roomIdText);
            if (roomId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Room ID must be a positive number.",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Room ID must be a valid number.",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
        
        return true;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public int getRoomId() {
        return Integer.parseInt(roomIdField.getText().trim());
    }
} 