package com.tavern.common.model.network;

import com.tavern.common.model.Room;

public record JoinRoomResponse(boolean status, Room room, String errorMessage) implements NetworkMessage {

    public JoinRoomResponse(Room room) {
        this(true, room, null);
    }

    public JoinRoomResponse(String errorMessage) {
        this(false, null, errorMessage);
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.JOIN_ROOM_RESPONSE;
    }
} 