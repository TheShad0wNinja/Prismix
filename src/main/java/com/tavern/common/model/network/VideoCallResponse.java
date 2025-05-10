package com.tavern.common.model.network;

import com.tavern.common.model.User;

public record VideoCallResponse(User caller, User callee, boolean accepted) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.VIDEO_CALL_RESPONSE;
    }
}
