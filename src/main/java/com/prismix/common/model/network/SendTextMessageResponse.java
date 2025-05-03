package com.prismix.common.model.network;

import com.prismix.common.model.Message;

public record SendTextMessageResponse(Message message, boolean status) implements NetworkMessage{

    @Override
    public MessageType getMessageType() {
        return MessageType.SEND_TEXT_MESSAGE_RESPONSE;
    }
}
