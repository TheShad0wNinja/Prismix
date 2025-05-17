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
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class MainSidebar extends VBox implements Initializable, EventListener {
    @FXML
    private ListView<Object> roomsList;

    private RoomEntryButton directMessagesBtn;
    private RoomEntryButton createRoomBtn;
    private RoomEntryButton discoverRoomBtn;

    private final ObservableList<Object> displayedItems = FXCollections.observableArrayList();
    private final List<Room> rooms = new ArrayList<>();

    public MainSidebar() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/MainSidebar.fxml"));
        fxmlLoader.setController(this);
        fxmlLoader.setRoot(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            System.out.println("ERROR: ");
            e.printStackTrace();
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        roomsList.setCellFactory(_ -> new RoomEntryCell());
        roomsList.setItems(displayedItems);

        directMessagesBtn = new RoomEntryButton(
                "Direct Messages",
                null,
                () -> {
                    ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.DIRECT_SCREEN_SELECTED, null));
                }
        );
        discoverRoomBtn = new RoomEntryButton(
                "Discover Room",
                null,
                () -> {
                    DiscoverRoomDialogue dialogue = new DiscoverRoomDialogue(this.getScene().getWindow());
                    dialogue.showAndWait();
                }
        );

        roomsList.setOnMouseClicked(this::handleItemClick);

        ApplicationContext.getEventBus().subscribe(this);
        ApplicationContext.getRoomHandler().updateRooms();
    }

    private void handleItemClick(MouseEvent mouseEvent) {
        if (roomsList.getSelectionModel().getSelectedItem() == null)
        return;

        Object item = roomsList.getSelectionModel().getSelectedItem();

        if (item instanceof Room r) {
            ApplicationContext.getEventBus().publish(new ApplicationEvent(
                    ApplicationEvent.Type.ROOM_SELECTED,
                    r
            ));
        } else if (item instanceof RoomEntryButton r) {
            r.performAction();
        }
    }

    private void rebuildDisplayedItems() {
        Platform.runLater(() -> {
            displayedItems.clear();

            if (directMessagesBtn != null)
                displayedItems.add(directMessagesBtn);

            displayedItems.addAll(rooms);

            if (discoverRoomBtn != null)
                displayedItems.add(discoverRoomBtn);
        });
    }

    public static class RoomEntryCell extends ListCell<Object> {

        private final RoomEntry roomEntry;

        public RoomEntryCell() {
            roomEntry = new RoomEntry();
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);

            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else if (item instanceof Room r) {
                roomEntry.setRoom(r);
                setGraphic(roomEntry);
            } else if (item instanceof RoomEntryButton r) {
                setGraphic(r);
            } else {
                setText(item.toString());
            }
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_LIST_UPDATED) {
            List<Room> rooms = (List<Room>) event.data();
            if(rooms != null) {
                this.rooms.clear();
                this.rooms.addAll(rooms);
                rebuildDisplayedItems();
            }
        }
    }
}
