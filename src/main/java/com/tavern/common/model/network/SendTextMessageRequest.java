package com.tavern.common.model.network;

import com.tavern.common.model.Message;

public record SendTextMessageRequest(Message message) implements NetworkMessage{

    @Override
    public MessageType getMessageType() {
        return MessageType.SEND_TEXT_MESSAGE_REQUEST;
    }
}
