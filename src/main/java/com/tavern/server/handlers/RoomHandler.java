package com.tavern.server.handlers;

import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import com.tavern.server.core.ClientHandler;
import com.tavern.server.core.RequestHandler;
import com.tavern.server.data.manager.RoomManager;
import com.tavern.server.data.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class RoomHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(RoomHandler.class);

    public RoomHandler(HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        requestHandlers.put(NetworkMessage.MessageType.GET_ROOMS_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_ROOM_USERS_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.CREATE_ROOM_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.JOIN_ROOM_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_ALL_ROOMS_REQUEST, this);
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
                GetRoomUsersRequest request = (GetRoomUsersRequest) message;
                List<User> users = RoomManager.getMembersOfRoom(request.room().getId());
                GetRoomUsersResponse response = new GetRoomUsersResponse(users);
                client.sendMessage(response);
            }
            case GET_ALL_ROOMS_REQUEST -> {
                try {
                    List<Room> allRooms = RoomRepository.getAllRooms();
                    GetAllRoomsResponse response = new GetAllRoomsResponse(allRooms);
                    client.sendMessage(response);
                } catch (SQLException e) {
                    logger.error("Database error when getting all rooms", e);
                    client.sendMessage(new GetAllRoomsResponse(new ArrayList<>()));
                }
            }
            case CREATE_ROOM_REQUEST -> {
                CreateRoomRequest request = (CreateRoomRequest) message;
                Room newRoom = RoomManager.createRoom(request.roomName(), request.avatar());
                
                // Create response based on success/failure
                if (newRoom != null) {
                    // Automatically join the creator to the room
                    boolean joined = RoomManager.joinRoom(request.creator().getId(), newRoom.getId());
                    if (joined) {
                        client.sendMessage(new CreateRoomResponse(newRoom));
                    } else {
                        client.sendMessage(new CreateRoomResponse("Failed to join the newly created room."));
                    }
                } else {
                    client.sendMessage(new CreateRoomResponse("Failed to create room. The room name may already exist."));
                }
            }
            case JOIN_ROOM_REQUEST -> {
                JoinRoomRequest request = (JoinRoomRequest) message;
                
                // Check if room exists first
                Room room = null;
                try {
                    room = RoomRepository.getRoomById(request.roomId());
                } catch (SQLException e) {
                    logger.error("Database error when retrieving room: {}", request.roomId(), e);
                }
                
                if (room != null) {
                    boolean joined = RoomManager.joinRoom(request.user().getId(), request.roomId());
                    if (joined) {
                        client.sendMessage(new JoinRoomResponse(room));
                    } else {
                        client.sendMessage(new JoinRoomResponse("Failed to join room. You may already be a member."));
                    }
                } else {
                    client.sendMessage(new JoinRoomResponse("Room not found. Please check the room ID."));
                }
            }
        }
    }
}
