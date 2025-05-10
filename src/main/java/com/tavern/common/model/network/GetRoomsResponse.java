package com.tavern.common.model.network;

import com.tavern.common.model.Room;

import java.util.ArrayList;

public record GetRoomsResponse(ArrayList<Room> rooms) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_ROOMS_RESPONSE;
    }
}
