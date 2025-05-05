package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record VideoCallAnswer(User caller, User callee, String sdpAnswer) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.VIDEO_CALL_ANSWER;
    }
}
