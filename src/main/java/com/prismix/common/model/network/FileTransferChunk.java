package com.prismix.common.model.network;

import java.io.Serializable;

public class FileTransferChunk implements NetworkMessage, Serializable {
    private final String transferId;
    private final byte[] data;
    private final int chunkNumber;
    private final int totalChunks;

    public FileTransferChunk(String transferId, byte[] data, int chunkNumber, int totalChunks) {
        this.transferId = transferId;
        this.data = data;
        this.chunkNumber = chunkNumber;
        this.totalChunks = totalChunks;
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_CHUNK;
    }

    public String getTransferId() {
        return transferId;
    }

    public byte[] getData() {
        return data;
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public int getTotalChunks() {
        return totalChunks;
    }
}