package com.prismix.common.model.network;

import java.io.Serializable;

public class FileTransferComplete implements NetworkMessage, Serializable {
    private final String transferId;
    private final String fileName;
    private final long fileSize;
    private final int senderId;
    private final int roomId;
    private final boolean isDirect;
    private final int receiverId;

    public FileTransferComplete(String transferId, String fileName, long fileSize, int senderId, int roomId,
            boolean isDirect, int receiverId) {
        this.transferId = transferId;
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.senderId = senderId;
        this.roomId = roomId;
        this.isDirect = isDirect;
        this.receiverId = receiverId;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_COMPLETE;
    }

    public String getTransferId() {
        return transferId;
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

    public boolean isDirect() {
        return isDirect;
    }

    public int getReceiverId() {
        return receiverId;
    }
}