package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.Message;
import com.tavern.common.model.User;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ResourceBundle;

import static com.tavern.client.gui.components.InputBar.messageSerial;

public class UserEntry extends HBox implements Initializable {
    private static final int AVATAR_SIZE = 24;
    private static final Logger logger = LoggerFactory.getLogger(UserEntry.class);

    @FXML
    private ImageView avatar;

    @FXML
    private Label username;

    @FXML
    private Label displayName;

    private final User user;

    public UserEntry(User user) {
        this.user = user;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/UserEntry.fxml"));
        loader.setController(this);
        loader.setRoot(this);

        this.getStylesheets().add(getClass().getResource("/client/styles/user-entry.css").toExternalForm());

        try {
            loader.load();
        } catch (IOException _) {
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        avatar.setFitHeight(AVATAR_SIZE);
        avatar.setFitWidth(AVATAR_SIZE);
        avatar.setImage(SwingFXUtils.toFXImage((BufferedImage) AvatarHelper.getCircleImageIcon(AvatarHelper.getAvatarImageIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE), AVATAR_SIZE, AVATAR_SIZE).getImage(), null));

        displayName.setText(user.getDisplayName());
        username.setText("@" + user.getUsername());

        Tooltip.install(displayName, new Tooltip(user.getDisplayName()));
        Tooltip.install(username, new Tooltip(user.getUsername()));

        if (!user.equals(ApplicationContext.getUserHandler().getUser())) {
            this.getStyleClass().add("hoverable");
            this.setOnMouseClicked(_ -> {
                logger.debug("Click on user: {}", user);

                // Check if user has existing direct messages
                List<Message> directMessages = ApplicationContext.getMessageHandler().getDirectMessageHistory(user);

                if (directMessages == null || directMessages.isEmpty()) {
                    // First time: Send an invisible message to initiate the direct chat
                    long messageId = messageSerial.incrementAndGet();
                    Message initMessage = new Message(
                            (int) messageId,
                            ApplicationContext.getUserHandler().getUser().getId(),
                            user.getId(),
                            -1,
                            "👋", // Invisible / greeting emoji message to initiate
                            true,
                            Timestamp.valueOf(LocalDateTime.now())
                    );

                    try {
                        ApplicationContext.getMessageHandler().sendTextMessage(initMessage);
                        logger.info("Sent initial direct message to: {}", user.getDisplayName());
                    } catch (Exception ex) {
                        logger.error("Error sending initial direct message: {}", ex.getMessage(), ex);
                    }
                }

                // Navigate to direct message screen with this user
                ApplicationContext.getEventBus().publish(new ApplicationEvent(
                        ApplicationEvent.Type.DIRECT_SCREEN_SELECTED
                ));

                // Add a delay to ensure the direct message screen is loaded before selecting the user
                Platform.runLater(() -> {
                    Platform.runLater(() -> {
                        ApplicationContext.getEventBus().publish(new ApplicationEvent(
                                ApplicationEvent.Type.DIRECT_USER_SELECTED,
                                user
                        ));
                    });
                });
            });
        }

    }
}
