package com.prismix.common.model.network;

import java.io.Serializable;

public class FileTransferDownloadResponse implements NetworkMessage, Serializable {
    private static final long serialVersionUID = 1L;

    private final boolean accepted;
    private final String fileName;
    private final String transferId;
    private final long fileSize;

    public FileTransferDownloadResponse(boolean accepted, String fileName, String transferId, long fileSize) {
        this.accepted = accepted;
        this.fileName = fileName;
        this.transferId = transferId;
        this.fileSize = fileSize;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_DOWNLOAD_RESPONSE;
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

    public long getFileSize() {
        return fileSize;
    }
}