package com.prismix.client.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.client.core.EventBus;
import com.prismix.client.core.EventListener;
import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.common.model.network.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomHandler implements ResponseHandler, EventListener {
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

        eventBus.subscribe(this);
    }

    public void updateRooms() {
        if (userHandler.getUser() == null)
            return;

        try {
            ConnectionManager.getInstance().sendMessage(new GetRoomsRequest(userHandler.getUser()));
        } catch (IOException e) {
            System.out.println("Error getting users's rooms: " + e.getMessage());
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
            System.out.println("Error getting room's users: " + e.getMessage());
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            currentRoom = (Room) event.data();
            System.out.println("Room selected: " + currentRoom);
//            updateRoomUsers();
        }
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }
}
