package com.prismix.common.model.network;

import java.util.List;

public record GetUsersInfoRequest(List<Integer> userIds) implements NetworkMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_USERS_INFO_REQUEST;
    }
}
