package com.prismix.common.model.network;

import com.prismix.common.model.Room;

public record GetRoomUsersRequest(Room room) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ROOM_USERS_REQUEST;
    }
}
