package com.prismix.common.model.network;

import com.prismix.common.model.User;

import java.util.List;

public record GetAllUsersResponse(List<User> users) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ALL_USERS_RESPONSE;
    }
} 