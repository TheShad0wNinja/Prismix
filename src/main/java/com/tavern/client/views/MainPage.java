package com.tavern.client.views;

import com.tavern.client.components.Cleanable;
import com.tavern.client.components.MainHeader;
import com.tavern.client.components.MainSidebar;
import com.tavern.client.components.RoomPane;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Room;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPage implements Initializable, Cleanable, EventListener {

    @FXML
    public MainSidebar sidebar;

    @FXML
    public MainHeader header;

    @FXML
    public BorderPane root;

    private Cleanable currentMainContent;

    public static PageData load() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainPage.class.getResource("/client/views/MainPage.fxml"));

        try {
            return new PageData(fxmlLoader.load(), fxmlLoader.getController());
        } catch (IOException e) {
            System.out.println("Failed: " + e.getMessage());
            return null;
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext.getEventBus().subscribe(this);
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }

    private void showRoomPane(Room room) {
        if (currentMainContent != null)
            currentMainContent.clean();

        currentMainContent = new RoomPane();
        Platform.runLater(() -> {
            root.setCenter((Node) currentMainContent);
        });
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case ROOM_SELECTED -> {
                showRoomPane((Room) event.data());
            }
        }
    }
}
