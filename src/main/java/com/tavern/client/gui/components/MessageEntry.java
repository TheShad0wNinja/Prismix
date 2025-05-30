package com.tavern.client.gui.components;

import com.tavern.client.gui.components.themed.ThemedIcon;
import com.tavern.client.gui.components.themed.ThemedLabel;
import com.tavern.client.gui.components.themed.ThemedPanel;
import com.tavern.client.gui.components.themed.ThemedButton;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Message;
import com.tavern.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.time.format.DateTimeFormatter;

public class MessageEntry extends ThemedPanel {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public MessageEntry(User user, Message message) {
        super(Variant.BACKGROUND);
        setLayout(new GridLayout(1, 1));

        JPanel wrapperPanel = new JPanel();
        wrapperPanel.setOpaque(false);
        wrapperPanel.setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.insets = new Insets(0, 0, 0, 10);
        JLabel icon = new ThemedIcon(user.getAvatar(), 35, 35, ThemedIcon.Variant.CIRCLE);
        wrapperPanel.add(icon, c);

        JPanel contentPanel = new JPanel();
        contentPanel.setOpaque(false);
        contentPanel.setLayout(new GridLayout(2, 1));

        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new GridBagLayout());

        c.insets = new Insets(0, 0, 0, 0);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        infoPanel.setOpaque(false);
        infoPanel.add(new ThemedLabel(user.getDisplayName(), ThemedLabel.Size.SMALLER), c);

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        infoPanel.add(new ThemedLabel(formatter.format(message.getTimestamp().toLocalDateTime()), 12), c);

        contentPanel.add(infoPanel);

        // Check if the message content is a file message
        if (message.getContent().startsWith("FILE:")) {
            String fileName = message.getContent().substring(5); // Remove "FILE:" prefix
            JPanel filePanel = new JPanel(new BorderLayout(5, 0));
            filePanel.setOpaque(false);

            // File icon and name
            JLabel fileIcon = new JLabel("📎");
            filePanel.add(fileIcon, BorderLayout.WEST);

            JLabel fileNameLabel = new ThemedLabel(fileName);
            filePanel.add(fileNameLabel, BorderLayout.CENTER);

            // Download button
            ThemedButton downloadButton = new ThemedButton("Download");
            downloadButton.addActionListener(e -> {
                ApplicationContext.getFileTransferHandler().downloadFile(
                        fileName,
                        message.getRoomId());
            });
            filePanel.add(downloadButton, BorderLayout.EAST);

            contentPanel.add(filePanel);
        } else {
            // Create a text area for message content with word wrapping
//            JTextArea messageTextArea = new JTextArea(message.getContent());
//            messageTextArea.setLineWrap(true);
//            messageTextArea.setWrapStyleWord(true);
//            messageTextArea.setEditable(false);
//            messageTextArea.setCursor(null);
//            messageTextArea.setFocusable(false);
//            messageTextArea.setOpaque(false);
//
//            // Style to match ThemedLabel appearance
//            Font labelFont = UIManager.getFont("Label.font");
//            if (labelFont != null) {
//                messageTextArea.setFont(new Font(labelFont.getName(), labelFont.getStyle(), 18));
//            } else {
//                messageTextArea.setFont(new Font("SansSerif", Font.PLAIN, 18));
//            }
//
//            // Match text color to ThemedLabel
//            messageTextArea.setForeground(getForeground());
//
//            // Remove border
//            messageTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));
//
//            // Set alignment
//            messageTextArea.setAlignmentX(Component.LEFT_ALIGNMENT);

            JLabel messageText = new ThemedLabel(message.getContent(), ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND);
            // Add to content panel
            contentPanel.add(messageText);
        }

        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(0, 10, 0, 0);
        wrapperPanel.add(contentPanel, c);

        add(wrapperPanel);
    }
}
