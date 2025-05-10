package com.tavern.common.model.network;

import com.tavern.common.model.User;

import java.util.List;

public record GetUsersInfoResponse(List<User> users) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.GET_USERS_INFO_RESPONSE;
    }
}
