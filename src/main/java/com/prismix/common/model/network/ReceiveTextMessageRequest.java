package com.prismix.common.model.network;

import com.prismix.common.model.Message;

public record ReceiveTextMessageRequest(Message message) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.RECEIVE_TEXT_MESSAGE_REQUEST;
    }
}
