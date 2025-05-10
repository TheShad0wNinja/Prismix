package com.tavern.common.model.network;

import java.io.Serializable;

public class FileTransferUploadResponse implements NetworkMessage, Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean accepted;
    private final String fileName;
    private final String transferId;

    public FileTransferUploadResponse(boolean accepted, String fileName, String transferId) {
        this.accepted = accepted;
        this.fileName = fileName;
        this.transferId = transferId;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_UPLOAD_RESPONSE;
    }

    public boolean isAccepted() {
        return accepted;
    }

    public String getFileName() {
        return fileName;
    }

    public String getTransferId() {
        return transferId;
    }
}