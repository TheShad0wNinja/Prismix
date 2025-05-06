package com.prismix.common.model.network;

import java.io.Serializable;
import java.util.Arrays;
import java.util.zip.CRC32;

public class FileTransferChunk implements NetworkMessage, Serializable {
    private static final long serialVersionUID = 1L;

    private final String transferId;
    private final byte[] data;
    private final int chunkNumber;
    private final int totalChunks;
    private final long checksum;

    public FileTransferChunk(String transferId, byte[] data, int chunkNumber, int totalChunks) {
        this.transferId = transferId;
        // Create a defensive copy of the data to prevent external modification
        this.data = Arrays.copyOf(data, data.length);
        this.chunkNumber = chunkNumber;
        this.totalChunks = totalChunks;

        // Calculate checksum for data integrity
        CRC32 crc = new CRC32();
        crc.update(this.data, 0, this.data.length);
        this.checksum = crc.getValue();

        // Debug logging
        System.out.println("Chunk " + chunkNumber + " created with size " + this.data.length +
                " bytes, checksum: " + this.checksum +
                ", first few bytes: " + bytesToHex(this.data, Math.min(16, this.data.length)));
    }

    @Override
    public MessageType getMessageType() {
        return MessageType.FILE_TRANSFER_CHUNK;
    }

    public String getTransferId() {
        return transferId;
    }

    public byte[] getData() {
        // Return a defensive copy to prevent external modification
        return Arrays.copyOf(data, data.length);
    }

    public int getChunkNumber() {
        return chunkNumber;
    }

    public int getTotalChunks() {
        return totalChunks;
    }

    public long getChecksum() {
        return checksum;
    }

    public boolean verifyChecksum() {
        CRC32 crc = new CRC32();
        crc.update(data, 0, data.length);
        long calculatedChecksum = crc.getValue();

        // Debug logging
        if (calculatedChecksum != checksum) {
            System.out.println("Checksum mismatch for chunk " + chunkNumber +
                    ": expected " + checksum +
                    ", got " + calculatedChecksum +
                    ", data length: " + data.length +
                    ", first few bytes: " + bytesToHex(data, Math.min(16, data.length)));
        }

        return calculatedChecksum == checksum;
    }

    // Helper method to convert bytes to hex for debugging
    private static String bytesToHex(byte[] bytes, int length) {
        StringBuilder hex = new StringBuilder();
        for (int i = 0; i < length; i++) {
            hex.append(String.format("%02x", bytes[i]));
            if (i < length - 1)
                hex.append(" ");
        }
        return hex.toString();
    }
}