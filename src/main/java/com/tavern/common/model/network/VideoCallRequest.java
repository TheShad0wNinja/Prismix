package com.tavern.common.model.network;

import com.tavern.common.model.User;

public record VideoCallRequest(User caller, User callee) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.VIDEO_CALL_REQUEST;
    }
}
