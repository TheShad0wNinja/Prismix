package com.tavern.common.model.network;

public record GetAllUsersRequest() implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ALL_USERS_REQUEST;
    }
} 