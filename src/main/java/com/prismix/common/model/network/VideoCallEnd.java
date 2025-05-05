package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record VideoCallEnd(User sender, User receiver) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.VIDEO_CALL_END;
    }
}
