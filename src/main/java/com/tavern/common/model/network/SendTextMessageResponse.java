package com.tavern.common.model.network;

import com.tavern.common.model.Message;

public record SendTextMessageResponse(Message message, boolean status) implements NetworkMessage{

    @Override
    public MessageType getMessageType() {
        return MessageType.SEND_TEXT_MESSAGE_RESPONSE;
    }
}
