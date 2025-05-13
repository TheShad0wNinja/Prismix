package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Room;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainSidebar extends VBox implements Initializable, EventListener {
    @FXML
    private VBox roomsPanel;

    private ObservableList<Room> rooms = FXCollections.observableArrayList();
    private ObservableList<Node> roomsPanelChildren;

    public MainSidebar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/MainSidebar.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            System.out.println("ERROR: " + e.getMessage());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
         roomsPanelChildren = roomsPanel.getChildren();
        roomsPanel.setSpacing(5);
        roomsPanel.setPadding(new Insets(5));

        rooms.addListener((javafx.collections.ListChangeListener<? super Room>) c -> {
            updateRoomList();
        });

        ApplicationContext.getEventBus().subscribe(this);
        ApplicationContext.getRoomHandler().updateRooms();
    }

    private void updateRoomList() {
        Platform.runLater(() -> {
            roomsPanelChildren.clear();
            for (Room room : rooms) {
                RoomEntryPanel roomEntry = new RoomEntryPanel(room);
                roomsPanelChildren.add(roomEntry); // Insert before the last 4 elements
            }
        });
    }


    public static class RoomEntryPanel extends VBox{
        private Label nameLabel;
        private Label idLabel;

        public RoomEntryPanel(Room room){
            nameLabel = new Label("Name: " + room.getName());
            idLabel = new Label("ID: " + room.getId());

            this.getChildren().addAll(nameLabel, idLabel);
            this.setPadding(new Insets(5));
            this.setStyle("-fx-border-color: gray; -fx-border-width: 1;");
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_LIST_UPDATED) {
            List<Room> rooms = (List<Room>) event.data();
            this.rooms.clear();
            this.rooms.setAll(rooms);
        }
    }
}
