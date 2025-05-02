package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record GetRoomsRequest(User user) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ROOMS_REQUEST;
    }
}
