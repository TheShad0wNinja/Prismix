package com.prismix.common.model.network;

import java.io.Serializable;

public class FileTransferResponse implements NetworkMessage, Serializable {
    private final boolean accepted;
    private final String message;
    private final String transferId;

    public FileTransferResponse(boolean accepted, String message, String transferId) {
        this.accepted = accepted;
        this.message = message;
        this.transferId = transferId;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_RESPONSE;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getMessage() {
        return message;
    }

    public String getTransferId() {
        return transferId;
    }
}