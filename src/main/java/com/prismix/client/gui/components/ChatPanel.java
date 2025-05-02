package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends ThemedPanel {
    private final Room room;
//    private final JTextArea messageArea;
//    private final JTextField inputField;
    
    public ChatPanel(Room room) {
//        super(Variant.OUTLINE, true);
        this.room = room;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        c.weighty = 1;
        c.fill = GridBagConstraints.BOTH;
//        c.insets = new Insets(2, 2, 2, 2);

        JPanel panel = new ThemedPanel(Variant.SURFACE_ALT, true);
//        panel.setBackground(Color.ORANGE);
        JScrollPane scrollPane = new JScrollPane(panel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(scrollPane, c);

        // Message display area
//        messageArea = new JTextArea();
//        messageArea.setEditable(false);
//        messageArea.setLineWrap(true);
//        messageArea.setWrapStyleWord(true);
//        JScrollPane scrollPane = new JScrollPane(messageArea);
//        add(scrollPane, BorderLayout.CENTER);
//
//        // Input area
//        JPanel inputPanel = new ThemedPanel();
//        inputPanel.setLayout(new BorderLayout());
//
//        inputField = new JTextField();
//        inputPanel.add(inputField, BorderLayout.CENTER);
//
//        JButton sendButton = new JButton("Send");
//        inputPanel.add(sendButton, BorderLayout.EAST);
//
//        add(inputPanel, BorderLayout.SOUTH);
    }
} 