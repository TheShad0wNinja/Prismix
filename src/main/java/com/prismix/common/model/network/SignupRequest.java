package com.prismix.common.model.network;

public record SignupRequest(String username, String displayName, byte[] avatar) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.SIGNUP_REQUEST;
    }
}
