package com.tavern.common.model.network;

import java.io.Serializable;

public class FileTransferUploadRequest implements NetworkMessage, Serializable {
    private static final long serialVersionUID = 1L;

    private final String fileName;
    private final long fileSize;
    private final int senderId;
    private final int roomId;
    private final boolean isDirect;
    private final int receiverId;

    public FileTransferUploadRequest(String fileName, long fileSize, int senderId, int roomId, boolean isDirect,
            int receiverId) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.senderId = senderId;
        this.roomId = roomId;
        this.isDirect = isDirect;
        this.receiverId = receiverId;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_UPLOAD_REQUEST;
    }

    public String getFileName() {
        return fileName;
    }

    public long getFileSize() {
        return fileSize;
    }

    public int getSenderId() {
        return senderId;
    }

    public int getRoomId() {
        return roomId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public boolean isDirect() {
        return isDirect;
    }
}