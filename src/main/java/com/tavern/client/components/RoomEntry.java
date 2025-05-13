package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.Room;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoomEntry extends Button implements EventListener, Initializable, Cleanable {
    @FXML
    private ImageView roomAvatar;

    private final Room room;
    private boolean isSelected;

    public RoomEntry(Room room) {
        this.room = room;
        this.isSelected = ApplicationContext.getRoomHandler().getCurrentRoom() != null &&
                ApplicationContext.getRoomHandler().getCurrentRoom().equals(room);

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/RoomEntry.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        this.getStylesheets().add(getClass().getResource("/client/styles/room-entry.css").toExternalForm());

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Tooltip tooltip = new Tooltip(room.getName());
        tooltip.setShowDelay(Duration.millis(200));
        tooltip.setStyle("""
                    -fx-font-size: 15px;
                    -fx-padding: 4 10 4 10;
                    -fx-background-color: -fx-surface-variant-color;
                    -fx-border-radius: 8px;
                    -fx-background-radius: 8px;
                    -fx-text-fill: -fx-on-surface-variant-color;
                    -fx-border-color: -fx-outline-color;
                """);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        this.setTooltip(tooltip);

        // Set room avatar if available
        if (room.getAvatar() != null && room.getAvatar().length > 0) {
            try {
                Image avatar = SwingFXUtils.toFXImage((BufferedImage) AvatarHelper.getRoundedImageIcon(AvatarHelper.getAvatarImageIcon(room.getAvatar(), 40, 40), 40, 40, 10).getImage(), null);
                roomAvatar.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Failed to load room avatar: " + e.getMessage());
            }
        }

        // Apply selected state if this room is selected
        updateSelectedState();

        // Subscribe to events
        ApplicationContext.getEventBus().subscribe(this);

        this.setOnAction(e -> {
            ApplicationContext.getEventBus().publish(new ApplicationEvent(
                    ApplicationEvent.Type.ROOM_SELECTED,
                    room
            ));
        });
    }

    private void updateSelectedState() {
        if (isSelected) {
            if (!this.getStyleClass().contains("selected")) {
                this.getStyleClass().add("selected");
            }
        } else {
            this.getStyleClass().remove("selected");
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            Room selectedRoom = (Room) event.data();
            boolean wasSelected = isSelected;

            isSelected = selectedRoom != null && selectedRoom.equals(this.room);

            if (wasSelected != isSelected) {
                Platform.runLater(this::updateSelectedState);
            }
        } else if (event.type() == ApplicationEvent.Type.DIRECT_SCREEN_SELECTED) {
            if (isSelected) {
                isSelected = false;
                Platform.runLater(this::updateSelectedState);
            }
        }
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }

}
