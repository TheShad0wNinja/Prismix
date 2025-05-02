package com.prismix.client.core;

import com.prismix.common.model.Room;
import com.prismix.common.model.network.GetRoomsRequest;
import com.prismix.common.model.network.GetRoomsResponse;
import com.prismix.common.model.network.NetworkMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomManager {
    private final EventBus eventBus;
    private final AuthManager authManager;
    private ArrayList<Room> rooms;

    public RoomManager(EventBus eventBus, AuthManager authManager) {
        this.eventBus = eventBus;
        this.authManager = authManager;
        rooms = new ArrayList<>();
    }

    public void updateRooms() {
        if (authManager.getUser() == null)
            return;

        try {
            ConnectionManager.getInstance().sendMessage(new GetRoomsRequest(authManager.getUser()));
        } catch (IOException e) {
            System.out.println("Error getting user's rooms: " + e.getMessage());
        }

    }

    public void handleMessage(NetworkMessage message) {
        switch (message.getMessageType()) {
            case GET_ROOMS_RESPONSE -> {
                GetRoomsResponse response = (GetRoomsResponse) message;
                rooms = response.rooms();
                eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ROOM_LIST_UPDATED, response.rooms()));
            }
        }
    }

    public ArrayList<Room> getRooms() {
        return rooms;
    }
}
