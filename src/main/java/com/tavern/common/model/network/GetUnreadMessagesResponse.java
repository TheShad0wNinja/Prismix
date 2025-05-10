package com.tavern.common.model.network;

import com.tavern.common.model.Message;

import java.util.List;

public record GetUnreadMessagesResponse(List<Message> messages) implements NetworkMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_UNREAD_MESSAGE_RESPONSE;
    }
}
