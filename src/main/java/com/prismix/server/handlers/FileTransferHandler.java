package com.prismix.server.handlers;

import com.prismix.common.model.network.*;
import com.prismix.server.core.ClientHandler;
import com.prismix.server.core.RequestHandler;
import com.prismix.server.data.repository.FileTransferRepository;
import com.prismix.server.data.repository.UserRepository;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FileTransferHandler implements RequestHandler {
    private final Map<String, String> transferPaths;
    private final Map<String, Long> transferSizes;
    private final Map<String, Long> transferredBytes;
    private final Map<String, ClientHandler> recipients;
    private final Path uploadDirectory;

    public FileTransferHandler(Map<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.transferPaths = new HashMap<>();
        this.transferSizes = new HashMap<>();
        this.transferredBytes = new HashMap<>();
        this.recipients = new HashMap<>();
        this.uploadDirectory = Paths.get("uploads");
        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_CHUNK, this);

        try {
            Files.createDirectories(uploadDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case FILE_TRANSFER_REQUEST -> handleTransferRequest((FileTransferRequest) message, client);
            case FILE_TRANSFER_CHUNK -> handleTransferChunk((FileTransferChunk) message, client);
            default -> {
            }
        }
    }

    private void handleTransferRequest(FileTransferRequest request, ClientHandler client) {
        try {
            // Only check recipient for direct transfers
            if (request.isDirect()) {
                var recipient = UserRepository.getUserById(request.getReceiverId());
                if (recipient == null) {
                    client.sendMessage(new FileTransferError(null, "Recipient not found"));
                    return;
                }
            }

            String transferId = UUID.randomUUID().toString();
            String fileName = transferId + "_" + request.getFileName();
            Path filePath = uploadDirectory.resolve(fileName);

            int dbTransferId = FileTransferRepository.createFileTransfer(request, filePath.toString(), transferId);
            if (dbTransferId == -1) {
                client.sendMessage(new FileTransferError(transferId, "Failed to create transfer record"));
                return;
            }

            transferPaths.put(transferId, filePath.toString());
            transferSizes.put(transferId, request.getFileSize());
            transferredBytes.put(transferId, 0L);
            recipients.put(transferId, client);

            client.sendMessage(new FileTransferResponse(true, request.getFileName(), transferId));

            // Forward request to recipient if direct transfer
            if (request.isDirect()) {
                try {
                    var recipientClient = recipients.get(transferId);
                    if (recipientClient != null && recipientClient.isConnected()) {
                        recipientClient.sendMessage(request);
                    }
                } catch (Exception e) {
                    System.err.println("Error notifying recipient: " + e.getMessage());
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error: " + e.getMessage());
            client.sendMessage(new FileTransferError(null, "Internal server error"));
        }
    }

    private void handleTransferChunk(FileTransferChunk chunk, ClientHandler client) {
        String filePath = transferPaths.get(chunk.getTransferId());
        if (filePath == null) {
            try {
                filePath = FileTransferRepository.getFilePath(chunk.getTransferId());
                if (filePath == null) {
                    client.sendMessage(new FileTransferError(chunk.getTransferId(), "Transfer not found"));
                    return;
                }
                transferPaths.put(chunk.getTransferId(), filePath);
            } catch (SQLException e) {
                System.err.println("Database error: " + e.getMessage());
                client.sendMessage(new FileTransferError(chunk.getTransferId(), "Internal server error"));
                return;
            }
        }

        try {
            Files.write(Paths.get(filePath), chunk.getData(),
                    chunk.getChunkNumber() == 0 ? StandardOpenOption.CREATE : StandardOpenOption.APPEND);

            // Update progress
            long totalBytes = transferSizes.getOrDefault(chunk.getTransferId(), 0L);
            long transferred = transferredBytes.compute(chunk.getTransferId(),
                    (id, bytes) -> (bytes == null ? 0L : bytes) + chunk.getData().length);
            int progress = totalBytes > 0 ? (int) ((transferred * 100) / totalBytes) : 0;

            // Send progress update to recipient
            ClientHandler recipient = recipients.get(chunk.getTransferId());
            if (recipient != null && recipient.isConnected()) {
                recipient.sendMessage(new FileTransferProgress(
                        chunk.getTransferId(),
                        progress,
                        transferred,
                        totalBytes));
            }

            if (chunk.getChunkNumber() == chunk.getTotalChunks() - 1) {
                FileTransferRepository.updateFileTransferStatus(chunk.getTransferId(), "COMPLETED");

                // Clean up
                transferPaths.remove(chunk.getTransferId());
                transferSizes.remove(chunk.getTransferId());
                transferredBytes.remove(chunk.getTransferId());
                recipients.remove(chunk.getTransferId());

                client.sendMessage(new FileTransferComplete(chunk.getTransferId(), null, 0, 0, 0, false, 0));
            }
        } catch (IOException | SQLException e) {
            System.err.println("Error handling file chunk: " + e.getMessage());
            try {
                FileTransferRepository.updateFileTransferStatus(chunk.getTransferId(), "FAILED");
            } catch (SQLException ex) {
                System.err.println("Failed to update transfer status: " + ex.getMessage());
            }
            client.sendMessage(new FileTransferError(chunk.getTransferId(), "Failed to save file chunk"));
        }
    }
}