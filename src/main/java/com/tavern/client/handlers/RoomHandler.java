package com.tavern.client.handlers;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.utils.ConnectionManager;
import com.tavern.client.core.EventBus;
import com.tavern.client.core.EventListener;
import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomHandler implements ResponseHandler, EventListener {
    private static final Logger logger = LoggerFactory.getLogger(RoomHandler.class);
    private final EventBus eventBus;
    private final UserHandler userHandler;
    private ArrayList<Room> rooms;
    private ArrayList<User> currentRoomUsers;
    private Room currentRoom;

    public record RoomUsersInfo(List<User> users, Room room) {}

    public RoomHandler(EventBus eventBus, UserHandler userHandler, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandlers) {
        this.eventBus = eventBus;
        this.userHandler = userHandler;
        rooms = new ArrayList<>();
        responseHandlers.put(NetworkMessage.MessageType.GET_ROOMS_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.GET_ROOM_USERS_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.CREATE_ROOM_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.JOIN_ROOM_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE, this);

        eventBus.subscribe(this);
    }

    public void updateRooms() {
        if (userHandler.getUser() == null)
            return;

        try {
            ConnectionManager.getInstance().sendMessage(new GetRoomsRequest(userHandler.getUser()));
        } catch (IOException e) {
            logger.error("Error getting user's rooms: {}", e.getMessage(), e);
        }
    }

    public User getRoomUser(int roomId) {
        return currentRoomUsers.stream().filter(u -> u.equals(roomId)).findFirst().orElse(null);
    }

    public void handleResponse(NetworkMessage message) {
        switch (message.getMessageType()) {
            case GET_ROOMS_RESPONSE -> {
                GetRoomsResponse response = (GetRoomsResponse) message;
                rooms = response.rooms();
                eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ROOM_LIST_UPDATED, response.rooms()));
            }
            case GET_ROOM_USERS_RESPONSE -> {
                GetRoomUsersResponse response = (GetRoomUsersResponse) message;
                currentRoomUsers = new ArrayList<>(response.users());
                eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ROOM_USERS_UPDATED, currentRoomUsers));
            }
            case CREATE_ROOM_RESPONSE -> {
                CreateRoomResponse response = (CreateRoomResponse) message;
                if (response.status()) {
                    // Room creation successful, update rooms list
                    updateRooms();
                } else {
                    // Room creation failed, notify user
                    eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ERROR, 
                            "Failed to create room: " + response.errorMessage()));
                }
            }
            case JOIN_ROOM_RESPONSE -> {
                JoinRoomResponse response = (JoinRoomResponse) message;
                if (response.status()) {
                    // Room join successful, update rooms list
                    updateRooms();
                } else {
                    // Room join failed, notify user
                    eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ERROR, 
                            "Failed to join room: " + response.errorMessage()));
                }
            }
            case GET_ALL_ROOMS_RESPONSE -> {
                GetAllRoomsResponse response = (GetAllRoomsResponse) message;
                eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ALL_ROOMS_UPDATED, response.rooms()));
            }
        }
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }

    public void updateRoomUsers() {
        if (currentRoom == null)
            return;

        try {
            ConnectionManager.getInstance().sendMessage(new GetRoomUsersRequest(currentRoom));
        } catch (IOException e) {
            logger.error("Error getting room's users: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            currentRoom = (Room) event.data();
            logger.debug("Room selected: {}", currentRoom);
//            updateRoomUsers();
        }
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void createRoom(String roomName, byte[] avatar) {
        try {
            ConnectionManager.getInstance().sendMessage(
                    new CreateRoomRequest(userHandler.getUser(), roomName, avatar));
        } catch (IOException e) {
            logger.error("Error creating room: {}", e.getMessage(), e);
        }
    }

    public void joinRoom(int roomId) {
        try {
            ConnectionManager.getInstance().sendMessage(
                    new JoinRoomRequest(userHandler.getUser(), roomId));
        } catch (IOException e) {
            logger.error("Error joining room: {}", e.getMessage(), e);
        }
    }

    public void getAllRooms() {
        try {
            ConnectionManager.getInstance().sendMessage(new GetAllRoomsRequest());
        } catch (IOException e) {
            logger.error("Error getting all rooms: {}", e.getMessage(), e);
        }
    }
}
