package com.tavern.common.model.network;

import java.io.Serializable;

public class FileTransferDownloadRequest implements NetworkMessage, Serializable {
    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final int requesterId;
    private final int roomId;

    public FileTransferDownloadRequest(String fileName, int requesterId, int roomId) {
        this.fileName = fileName;
        this.requesterId = requesterId;
        this.roomId = roomId;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_DOWNLOAD_REQUEST;
    }

    public String getFileName() {
        return fileName;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public int getRoomId() {
        return roomId;
    }
}