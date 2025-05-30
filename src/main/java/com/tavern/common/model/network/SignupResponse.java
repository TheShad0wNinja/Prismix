package com.tavern.common.model.network;

import com.tavern.common.model.User;

public record SignupResponse(boolean status, String errorMessage, User user) implements NetworkMessage{

    @Override
    public MessageType getMessageType() {
        return MessageType.SIGNUP_RESPONSE;
    }
}
