package com.prismix.common.model.network;

public record LoginRequest(String username) implements NetworkMessage {

    @Override
    public MessageType getMessageType() {
        return MessageType.LOGIN_REQUEST;
    }
}
