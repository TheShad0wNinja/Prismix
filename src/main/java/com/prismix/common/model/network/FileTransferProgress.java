package com.prismix.common.model.network;

import java.io.Serializable;

public class FileTransferProgress implements NetworkMessage, Serializable {
    private final String transferId;
    private final String fileName;
    private final int progress;
    private final long transferredBytes;
    private final long totalBytes;

    public FileTransferProgress(String transferId, String fileName, int progress, long transferredBytes,
            long totalBytes) {
        this.transferId = transferId;
        this.fileName = fileName;
        this.progress = progress;
        this.transferredBytes = transferredBytes;
        this.totalBytes = totalBytes;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_PROGRESS;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getFileName() {
        return fileName;
    }

    public int getProgress() {
        return progress;
    }

    public long getTransferredBytes() {
        return transferredBytes;
    }

    public long getTotalBytes() {
        return totalBytes;
    }
}