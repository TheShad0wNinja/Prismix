package com.prismix.common.model.network;

import com.prismix.common.model.Message;

import java.util.List;

public record GetUnreadMessagesResponse(List<Message> messages) implements NetworkMessage{
    @Override
    public MessageType getMessageType() {
        return MessageType.GET_UNREAD_MESSAGE_RESPONSE;
    }
}
