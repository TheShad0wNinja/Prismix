package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Room;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.io.ByteArrayInputStream;

public class RoomEntry implements EventListener, Cleanable {

    @FXML
    private HBox root;
    
    @FXML
    private StackPane avatarContainer;
    
    @FXML
    private ImageView roomAvatar;
    
    @FXML
    private Label roomNameLabel;
    
    private final Room room;
    private boolean isSelected;
    
    public RoomEntry(Room room) {
        this.room = room;
        this.isSelected = ApplicationContext.getRoomHandler().getCurrentRoom() != null && 
                         ApplicationContext.getRoomHandler().getCurrentRoom().equals(room);
    }
    
    @FXML
    public void initialize() {
        // Display room information
        roomNameLabel.setText(room.getName());
        
        // Set room avatar if available
        if (room.getAvatar() != null && room.getAvatar().length > 0) {
            try {
                Image avatar = new Image(new ByteArrayInputStream(room.getAvatar()));
                roomAvatar.setImage(avatar);
            } catch (Exception e) {
                System.err.println("Failed to load room avatar: " + e.getMessage());
            }
        }
        
        // Apply selected state if this room is selected
        updateSelectedState();
        
        // Add click event
        root.setOnMouseClicked(event -> {
            ApplicationContext.getEventBus().publish(new ApplicationEvent(
                ApplicationEvent.Type.ROOM_SELECTED,
                room
            ));
        });
        
        // Add hover effect
        root.setOnMouseEntered(event -> {
            if (!isSelected) {
                root.getStyleClass().add("room-entry-hover");
            }
        });
        
        root.setOnMouseExited(event -> {
            root.getStyleClass().remove("room-entry-hover");
        });
        
        // Subscribe to events
        ApplicationContext.getEventBus().subscribe(this);
    }
    
    private void updateSelectedState() {
        if (isSelected) {
            if (!root.getStyleClass().contains("room-entry-selected")) {
                root.getStyleClass().add("room-entry-selected");
            }
        } else {
            root.getStyleClass().remove("room-entry-selected");
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
