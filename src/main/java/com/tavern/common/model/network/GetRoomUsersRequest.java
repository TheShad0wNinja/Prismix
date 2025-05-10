package com.tavern.common.model.network;

import com.tavern.common.model.Room;

public record GetRoomUsersRequest(Room room) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ROOM_USERS_REQUEST;
    }
}
