package com.tavern.client.components;

import com.tavern.client.gui.components.themed.ThemedButton;
import com.tavern.client.gui.components.themed.ThemedLabel;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.Message;
import com.tavern.common.model.User;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class MessageEntry extends HBox implements Initializable {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final int AVATAR_SIZE=35;

    @FXML
    private Label content;
    @FXML
    private Label name;
    @FXML
    private Label timestamp;
    @FXML
    private ImageView avatar;

    private final User user;
    private final Message message;

    public MessageEntry(User user, Message message) {
        this.user = user;
        this.message = message;

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/MessageEntry.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException _) { }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Check if the message content is a file message
        if (message.getContent().startsWith("FILE:")) {
//            String fileName = message.getContent().substring(5); // Remove "FILE:" prefix
//            JPanel filePanel = new JPanel(new BorderLayout(5, 0));
//            filePanel.setOpaque(false);
//
//            // File icon and name
//            JLabel fileIcon = new JLabel("📎");
//            filePanel.add(fileIcon, BorderLayout.WEST);
//
//            JLabel fileNameLabel = new ThemedLabel(fileName);
//            filePanel.add(fileNameLabel, BorderLayout.CENTER);
//
//            // Download button
//            ThemedButton downloadButton = new ThemedButton("Download");
//            downloadButton.addActionListener(e -> {
//                ApplicationContext.getFileTransferHandler().downloadFile(
//                        fileName,
//                        message.getRoomId());
//            });
//            filePanel.add(downloadButton, BorderLayout.EAST);
//
//            contentPanel.add(filePanel);
        } else {
            content.setText(message.getContent());
        }

        Image img = SwingFXUtils.toFXImage((BufferedImage) AvatarHelper.getRoundedImageIcon(AvatarHelper.getAvatarImageIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE), AVATAR_SIZE, AVATAR_SIZE, 10).getImage(), null);
        avatar.setFitHeight(AVATAR_SIZE);
        avatar.setFitWidth(AVATAR_SIZE);
        avatar.setImage(img);

        name.setText(user.getDisplayName());
        timestamp.setText(formatter.format(message.getTimestamp().toLocalDateTime()));
    }
}
