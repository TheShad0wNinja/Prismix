package com.tavern.common.model.network;

import java.io.Serializable;

public class FileTransferError implements NetworkMessage, Serializable {
    private final String transferId;
    private final String errorMessage;

    public FileTransferError(String transferId, String errorMessage) {
        this.transferId = transferId;
        this.errorMessage = errorMessage;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_ERROR;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }
}