package com.prismix.common.model.network;

import com.prismix.common.model.User;

import java.util.List;

public record GetRoomUsersResponse(List<User> users) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ROOM_USERS_RESPONSE;
    }
}
