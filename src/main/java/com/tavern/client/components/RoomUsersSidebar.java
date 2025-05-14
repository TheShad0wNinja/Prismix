package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.components.themed.ThemedIcon;
import com.tavern.client.gui.components.themed.ThemedLabel;
import com.tavern.client.gui.components.themed.ThemedPanel;
import com.tavern.client.gui.themes.ThemeManager;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.Message;
import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.net.URL;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static com.tavern.client.gui.components.InputBar.messageSerial;

public class RoomUsersSidebar implements Initializable, Cleanable, EventListener {
    private final static Logger logger = LoggerFactory.getLogger(RoomUsersSidebar.class);

    @FXML
    public ScrollPane scrollPane;

    @FXML
    public VBox usersArea;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext.getEventBus().subscribe(this);
    }

    public void updateUserList(List<User> users) {
        List<UserEntry> newEntries = users.stream().map(UserEntry::new).toList();

        Platform.runLater(() -> {
            usersArea.getChildren().clear();
            usersArea.getChildren().addAll(newEntries);
        });
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_USERS_UPDATED) {
            List<User> users = (List<User>) event.data();
            updateUserList(users);
        }
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
