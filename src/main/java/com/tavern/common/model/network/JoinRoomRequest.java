package com.tavern.common.model.network;

import com.tavern.common.model.User;

public record JoinRoomRequest(User user, int roomId) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.JOIN_ROOM_REQUEST;
    }
} 