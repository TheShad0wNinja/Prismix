package com.tavern.client.gui.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.components.themed.ThemedButton;
import com.tavern.client.gui.components.themed.ThemedPanel;
import com.tavern.client.gui.components.themed.ThemedTextArea;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicLong;

public class InputBar extends ThemedPanel implements EventListener {
    private static final Logger logger = LoggerFactory.getLogger(InputBar.class);
    private final ThemedTextArea messageInput;
    private final boolean isDirect;
    private static final AtomicLong messageSerial = new AtomicLong(0);

    public InputBar(boolean isDirect) {
        super(Variant.SURFACE_ALT);
        this.isDirect = isDirect;

        setLayout(new BorderLayout(5, 5));
        setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        // Create a scrollable message input area for wrapped text
        messageInput = new ThemedTextArea("", this::sendMessage);
        
        // Limit initial height but allow expansion
        messageInput.setRows(1);
        
        // Create a scroll pane to handle overflow
        JScrollPane scrollPane = new JScrollPane(messageInput);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(null); // Remove the scroll pane border
        
        // Set preferred size for the scroll pane
        scrollPane.setPreferredSize(new Dimension(0, 35));
        
        add(scrollPane, BorderLayout.CENTER);

        JPanel inputsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        inputsPanel.setOpaque(false);

        JButton uploadButton = new ThemedButton("Upload", ThemedButton.Variant.PRIMARY, ThemedButton.Size.SMALLER);
        uploadButton.setPreferredSize(new Dimension(60, 30));

        JButton sendButton = new ThemedButton("Send", ThemedButton.Variant.PRIMARY, ThemedButton.Size.SMALLER);
        sendButton.setPreferredSize(new Dimension(60, 30));

        inputsPanel.add(uploadButton);
        inputsPanel.add(sendButton);

        add(inputsPanel, BorderLayout.EAST);

        ApplicationContext.getEventBus().subscribe(this);
        sendButton.addActionListener(_ -> {
            sendMessage(messageInput.getText());
            messageInput.setText("");
        });
        uploadButton.addActionListener((_) -> {
            if (isDirect)
                ApplicationContext.getFileTransferHandler().selectAndSendFileToUser(ApplicationContext.getMessageHandler().getCurrentDirectUser().getId());
            else
                ApplicationContext.getFileTransferHandler().selectAndSendFileToRoom(ApplicationContext.getRoomHandler().getCurrentRoom().getId());
        });
    }

    private void sendMessage(String message) {
        String content = message.trim();
        if (content.isEmpty()) {
            return;
        }

        // Create message with unique serial number
        long messageId = messageSerial.incrementAndGet();
        Message newMsg = new Message(
                (int) messageId,
                ApplicationContext.getUserHandler().getUser().getId(),
                isDirect ? ApplicationContext.getMessageHandler().getCurrentDirectUser().getId() : -1,
                isDirect ? -1 : ApplicationContext.getRoomHandler().getCurrentRoom().getId(),
                content,
                isDirect,
                Timestamp.valueOf(LocalDateTime.now())
        );

        try {
            // Send the message using the client's message handler
            ApplicationContext.getMessageHandler().sendTextMessage(newMsg);

            // Clear input
            messageInput.setText("");
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {

    }
}
