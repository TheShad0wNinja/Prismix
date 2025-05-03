package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record GetUnreadMessagesRequest(User user) implements NetworkMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_UNREAD_MESSAGE_REQUEST;
    }
}
