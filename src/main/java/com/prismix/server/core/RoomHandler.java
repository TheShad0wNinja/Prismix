package com.prismix.server.core;

import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.common.model.network.*;
import com.prismix.server.data.manager.RoomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomHandler implements RequestHandler {
    public RoomHandler(HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        requestHandlers.put(NetworkMessage.MessageType.GET_ROOMS_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_ROOM_USERS_REQUEST, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case GET_ROOMS_REQUEST -> {
                GetRoomsRequest request = (GetRoomsRequest) message;
                ArrayList<Room> rooms = new ArrayList<>(RoomManager.getRoomsForUser(request.user().getId()));
                GetRoomsResponse response = new GetRoomsResponse(rooms);
                client.sendMessage(response);
            }
            case GET_ROOM_USERS_REQUEST -> {
                GetRoomUsersRequest request = (GetRoomUsersRequest) message;;
                List<User> users = RoomManager.getMembersOfRoom(request.room().getId());
                GetRoomUsersResponse response = new GetRoomUsersResponse(users);
                client.sendMessage(response);
            }
        }
    }
}
