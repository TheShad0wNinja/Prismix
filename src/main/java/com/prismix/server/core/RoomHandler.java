package com.prismix.server.core;

import com.prismix.common.model.Room;
import com.prismix.common.model.network.GetRoomsRequest;
import com.prismix.common.model.network.GetRoomsResponse;
import com.prismix.common.model.network.NetworkMessage;
import com.prismix.server.data.manager.RoomManager;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomHandler implements RequestHandler {
    private final RoomManager roomManager;

    public RoomHandler(HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.roomManager = new RoomManager();
        requestHandlers.put(NetworkMessage.MessageType.GET_ROOMS_REQUEST, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case GET_ROOMS_REQUEST -> {
                GetRoomsRequest request = (GetRoomsRequest) message;
                ArrayList<Room> rooms = new ArrayList<>(roomManager.getRoomsForUser(request.user().getId()));
                GetRoomsResponse response = new GetRoomsResponse(rooms);
                client.sendMessage(response);
            }
        }
    }
}
