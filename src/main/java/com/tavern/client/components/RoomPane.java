package com.tavern.client.components;

import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Room;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoomPane extends BorderPane implements Initializable, Cleanable {
    @FXML
    private BorderPane chatPane;

    @FXML
    private ChatPane chatPaneController;

    @FXML
    private VBox userSidebar;

    @FXML
    private RoomUsersSidebar userSidebarController;

    public RoomPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/RoomPane.fxml"));

        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException _) {}
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext.getRoomHandler().updateRoomUsers();
    }

    @Override
    public void clean() {
        chatPaneController.clean();
        userSidebarController.clean();
    }
}
