package com.tavern.common.model.network;

import com.tavern.common.model.Message;

public record ReceiveTextMessageRequest(Message message) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.RECEIVE_TEXT_MESSAGE_REQUEST;
    }
}
