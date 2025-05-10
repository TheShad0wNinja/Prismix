package com.tavern.common.model.network;

import com.tavern.common.model.Room;

public record CreateRoomResponse(boolean status, Room room, String errorMessage) implements NetworkMessage {

    public CreateRoomResponse(Room room) {
        this(true, room, null);
    }

    public CreateRoomResponse(String errorMessage) {
        this(false, null, errorMessage);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_ROOM_RESPONSE;
    }
} 