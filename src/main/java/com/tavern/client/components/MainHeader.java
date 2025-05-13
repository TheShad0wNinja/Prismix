package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.Room;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainHeader extends HBox implements Initializable, EventListener {
    private static final int ICON_SIZE = 20;

    @FXML
    private ImageView icon;

    @FXML
    private Label label;

    private SimpleStringProperty currentLabel = new SimpleStringProperty("General");

    private enum CurrentState {
        ROOM,
        GENERAL,
        DM,
    }

    private CurrentState currentState = CurrentState.GENERAL;

    public MainHeader() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/MainHeader.fxml"));

        loader.setRoot(this);
        loader.setController(this);

        try{
            loader.load();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        label.textProperty().bind(currentLabel);

        icon.setFitHeight(ICON_SIZE);
        icon.setFitWidth(ICON_SIZE);

        setIcon(null);
        currentLabel.set("General");

        ApplicationContext.getEventBus().subscribe(this);
    }

    private void updateInfo(Room room) {
        Platform.runLater(() -> {
            currentLabel.set(room.getName());
            setIcon(room.getAvatar());
        });
    }

    private void updateInfo() {
        Platform.runLater(() -> {
            currentLabel.set("Direct Messages");
            setIcon(null);
        });
    }

    private void setIcon(byte[] iconData) {
        ImageIcon i = AvatarHelper.getAvatarImageIcon(iconData, ICON_SIZE, ICON_SIZE);
        BufferedImage rounded = (BufferedImage) AvatarHelper.getRoundedImageIcon(i, ICON_SIZE, ICON_SIZE, ICON_SIZE / 3).getImage();
        icon.setImage(SwingFXUtils.toFXImage(rounded, null));
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case ROOM_SELECTED -> {
                updateInfo((Room) event.data());
            }
            case DIRECT_SCREEN_SELECTED -> {
                updateInfo();
            }
        }
    }
}
