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
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.function.Function;

public class RoomEntryButton extends Button implements EventListener, Initializable, Cleanable {
    @FXML
    private ImageView roomAvatar;

    private boolean isSelected;
    private final String name;
    private final byte[] icon;
    private final Runnable action;
    private final Function<Object, Boolean> isSelectedCallable;

    public RoomEntryButton(String name, byte[] icon, Runnable action, Function<Object, Boolean> isSelected) {
        try {
            this.isSelected = isSelected.apply(null);
        } catch (Exception e) {
            this.isSelected = false;
        }
        this.name = name;
        this.icon = icon;
        this.action = action;
        this.isSelectedCallable = isSelected;

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
        Tooltip tooltip = new Tooltip(name);
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
        Image avatar = SwingFXUtils.toFXImage((BufferedImage) AvatarHelper.getRoundedImageIcon(AvatarHelper.getAvatarImageIcon(icon, 40, 40), 40, 40, 10).getImage(), null);
        roomAvatar.setImage(avatar);

        // Apply selected state if this room is selected
        updateSelectedState();

        // Subscribe to events
        ApplicationContext.getEventBus().subscribe(this);

        this.setOnAction(e -> {
            action.run();
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
        if (event.type() == ApplicationEvent.Type.DIRECT_SCREEN_SELECTED || event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            boolean newState = isSelected;
            try {
                newState = isSelectedCallable.apply(event.type());
            } catch (Exception _) {
            }

            if (newState != this.isSelected) {
                this.isSelected = newState;
                Platform.runLater(this::updateSelectedState);
            }
        }
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }

}
