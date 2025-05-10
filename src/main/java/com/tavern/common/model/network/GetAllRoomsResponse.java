package com.tavern.common.model.network;

import com.tavern.common.model.Room;
import java.util.List;

public record GetAllRoomsResponse(List<Room> rooms) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ALL_ROOMS_RESPONSE;
    }
} 