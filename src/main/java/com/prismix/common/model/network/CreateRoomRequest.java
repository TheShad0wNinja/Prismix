package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record CreateRoomRequest(User creator, String roomName, byte[] avatar) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.CREATE_ROOM_REQUEST;
    }
} 