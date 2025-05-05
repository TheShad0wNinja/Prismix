package com.prismix.common.model.network;

import com.prismix.common.model.User;

public record VideoCallOffer(User caller, User callee, String sdpOffer) implements NetworkMessage {
    @Override
    public MessageType getMessageType() {
        return MessageType.VIDEO_CALL_OFFER;
    }
}
