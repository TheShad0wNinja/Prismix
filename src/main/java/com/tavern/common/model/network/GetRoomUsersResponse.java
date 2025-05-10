package com.tavern.common.model.network;

import com.tavern.common.model.User;

import java.util.List;

public record GetRoomUsersResponse(List<User> users) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ROOM_USERS_RESPONSE;
    }
}
