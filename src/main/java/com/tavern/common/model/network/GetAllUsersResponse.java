package com.tavern.common.model.network;

import com.tavern.common.model.User;

import java.util.List;

public record GetAllUsersResponse(List<User> users) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ALL_USERS_RESPONSE;
    }
} 