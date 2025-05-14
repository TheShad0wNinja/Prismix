package com.tavern.client.components;

import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Message;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class InputBar implements Initializable {
    @FXML
    private TextField messageField;

    @FXML
    private Button sendButton;

    @FXML
    private Button uploadButton;

    private static final Logger logger = LoggerFactory.getLogger(InputBar.class);

    private boolean isDirect;
    private final AtomicLong messageSerial = new AtomicLong(0);


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isDirect = ApplicationContext.getMessageHandler().getCurrentDirectUser() != null;

        sendButton.setDisable(true);

        messageField.textProperty().addListener((_, _, newValue) -> {
            sendButton.setDisable(newValue.trim().isEmpty());
        });
    }

    @FXML
    private void sendMessage() {
        String content = messageField.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        // Create a new message
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

            // Clear input field
            Platform.runLater(() -> {
                messageField.clear();
            });
        } catch (Exception e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
        }

    }

    public void uploadFile() {
        if (isDirect)
            ApplicationContext.getFileTransferHandler().selectAndSendFileToUser(ApplicationContext.getMessageHandler().getCurrentDirectUser().getId());
        else
            ApplicationContext.getFileTransferHandler().selectAndSendFileToRoom(ApplicationContext.getRoomHandler().getCurrentRoom().getId());
    }
}
