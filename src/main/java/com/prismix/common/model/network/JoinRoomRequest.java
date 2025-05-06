package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record JoinRoomRequest(User user, int roomId) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.JOIN_ROOM_REQUEST;
    }
} 