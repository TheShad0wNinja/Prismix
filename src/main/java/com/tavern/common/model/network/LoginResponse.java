package com.tavern.common.model.network;

import com.tavern.common.model.User;

public record LoginResponse(boolean status, String errorMessage, User user) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.LOGIN_RESPONSE;
    }
}
