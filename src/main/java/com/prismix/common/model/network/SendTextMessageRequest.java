package com.prismix.common.model.network;

import com.prismix.common.model.Message;

public record SendTextMessageRequest(Message message) implements NetworkMessage{

    @Override
    public MessageType getMessageType() {
        return MessageType.SEND_TEXT_MESSAGE;
    }
}
