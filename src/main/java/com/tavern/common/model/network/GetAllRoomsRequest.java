package com.tavern.common.model.network;

public record GetAllRoomsRequest() implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ALL_ROOMS_REQUEST;
    }
} 