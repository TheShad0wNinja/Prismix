package com.tavern.common.model.network;

import com.tavern.common.model.User;

public record GetUnreadMessagesRequest(User user) implements NetworkMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_UNREAD_MESSAGE_REQUEST;
    }
}
