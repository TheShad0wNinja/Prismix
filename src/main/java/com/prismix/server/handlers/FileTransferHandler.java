package com.prismix.server.handlers;

import com.prismix.common.model.network.*;
import com.prismix.server.core.ClientHandler;
import com.prismix.server.core.RequestHandler;
import com.prismix.server.data.repository.FileTransferRepository;
import com.prismix.server.data.repository.UserRepository;
import com.prismix.server.utils.AppDataManager;

import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Arrays;
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
        this.uploadDirectory = AppDataManager.getUploadsPath();
        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_UPLOAD_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_DOWNLOAD_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_CHUNK, this);

        try {
            Files.createDirectories(uploadDirectory);
            System.out.println("Upload directory created at: " + uploadDirectory.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create upload directory: " + e.getMessage());
        }
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case FILE_TRANSFER_REQUEST -> handleTransferRequest((FileTransferRequest) message, client);
            case FILE_TRANSFER_UPLOAD_REQUEST -> handleUploadRequest((FileTransferUploadRequest) message, client);
            case FILE_TRANSFER_DOWNLOAD_REQUEST -> handleDownloadRequest((FileTransferDownloadRequest) message, client);
            case FILE_TRANSFER_CHUNK -> handleTransferChunk((FileTransferChunk) message, client);
            default -> {
            }
        }
    }

    // Legacy method for backward compatibility
    private void handleTransferRequest(FileTransferRequest request, ClientHandler client) {
        handleUploadRequest(new FileTransferUploadRequest(
                request.getFileName(),
                request.getFileSize(),
                request.getSenderId(),
                request.getRoomId(),
                request.isDirect(),
                request.getReceiverId()), client);
    }

    private void handleUploadRequest(FileTransferUploadRequest request, ClientHandler client) {
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

            // Send response to uploader
            client.sendMessage(new FileTransferUploadResponse(true, request.getFileName(), transferId));

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

    private void handleDownloadRequest(FileTransferDownloadRequest request, ClientHandler client) {
        try {
            System.out.println("a7a");
            String fileName = request.getFileName();

            // Check if the file exists in uploads
            File[] files = uploadDirectory.toFile().listFiles();
            if (files == null) {
                client.sendMessage(new FileTransferError(null, "File not found: " + fileName));
                return;
            }
            System.out.println("a7a2");
            // Look for a file with the requested name
            File requestedFile = null;
            for (File file : files) {
                // Check if the file name contains the requested name after the UUID_
                if (file.isFile() && file.getName().contains("_" + fileName)) {
                    requestedFile = file;
                    break;
                }
            }

            if (requestedFile == null) {
                client.sendMessage(new FileTransferError(null, "File not found: " + fileName));
                return;
            }
            System.out.println("a7a3");
            String transferId = UUID.randomUUID().toString();

            // Create record in database
            int dbTransferId = FileTransferRepository.createDownloadTransfer(
                    request.getRequesterId(), request.getRoomId(), requestedFile.getPath(), fileName, transferId);

            if (dbTransferId == -1) {
                client.sendMessage(new FileTransferError(null, "Failed to create transfer record"));
                return;
            }
            System.out.println("a7a4");
            // Set up transfer data
            transferPaths.put(transferId, requestedFile.getPath());
            transferSizes.put(transferId, requestedFile.length());
            transferredBytes.put(transferId, 0L);
            recipients.put(transferId, client);

            // Send download response to client
            client.sendMessage(new FileTransferDownloadResponse(
                    true, fileName, transferId, requestedFile.length()));

            // Start sending file chunks in a background thread
            File finalRequestedFile = requestedFile;
            new Thread(() -> {
                try (FileInputStream fis = new FileInputStream(finalRequestedFile)) {
                    byte[] buffer = new byte[8192]; // 8KB chunks
                    int totalChunks = (int) Math.ceil((double) finalRequestedFile.length() / 8192);
                    int chunkNumber = 0;
                    int bytesRead;

                    while ((bytesRead = fis.read(buffer)) != -1) {
                        byte[] chunk = bytesRead < 8192 ? Arrays.copyOf(buffer, bytesRead) : buffer;
                        FileTransferChunk chunkMessage = new FileTransferChunk(
                                transferId,
                                chunk,
                                chunkNumber,
                                totalChunks);

                        client.sendMessage(chunkMessage);

                        // Update progress
                        int progress = (int) ((chunkNumber + 1.0) / totalChunks * 100);
                        client.sendMessage(new FileTransferProgress(
                                transferId,
                                fileName,
                                progress,
                                (chunkNumber + 1) * bytesRead,
                                finalRequestedFile.length()));

                        chunkNumber++;
                    }

                    // Send completion message
                    client.sendMessage(new FileTransferComplete(
                            transferId, fileName, finalRequestedFile.length(), 0, request.getRoomId(), false,
                            request.getRequesterId()));

                    // Update database
                    FileTransferRepository.updateFileTransferStatus(transferId, "COMPLETED");
                } catch (Exception e) {
                    System.err.println("Error sending file: " + e.getMessage());
                    try {
                        client.sendMessage(new FileTransferError(transferId, "Error sending file: " + e.getMessage()));
                        FileTransferRepository.updateFileTransferStatus(transferId, "FAILED");
                    } catch (Exception ex) {
                        System.err.println("Error sending error message: " + ex.getMessage());
                    }
                }
            }).start();

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

        // Verify checksum before writing
        if (!chunk.verifyChecksum()) {
            System.err.println("Checksum verification failed for chunk " + chunk.getChunkNumber());
            // Request retry of the chunk
            client.sendMessage(new FileTransferError(chunk.getTransferId(),
                    "Data corruption detected in chunk " + chunk.getChunkNumber() + ". Please retry."));
            return;
        }

        try {
            // Create parent directories if they don't exist
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());

            // Write chunk with atomic operation
            Files.write(path, chunk.getData(),
                    chunk.getChunkNumber() == 0
                            ? new StandardOpenOption[] { StandardOpenOption.CREATE,
                                    StandardOpenOption.TRUNCATE_EXISTING }
                            : new StandardOpenOption[] { StandardOpenOption.APPEND });

            // Update progress
            long totalBytes = transferSizes.getOrDefault(chunk.getTransferId(), 0L);
            long transferred = transferredBytes.compute(chunk.getTransferId(),
                    (id, bytes) -> (bytes == null ? 0L : bytes) + chunk.getData().length);
            int progress = totalBytes > 0 ? (int) ((transferred * 100) / totalBytes) : 0;

            // Send progress update to recipient
            ClientHandler recipient = recipients.get(chunk.getTransferId());
            if (recipient != null && recipient.isConnected()) {
                try {
                    String fileName = FileTransferRepository.getFileName(chunk.getTransferId());
                    if (fileName != null) {
                        recipient.sendMessage(new FileTransferProgress(
                                chunk.getTransferId(),
                                fileName,
                                progress,
                                transferred,
                                totalBytes));
                    }
                } catch (SQLException e) {
                    System.err.println("Error getting file name: " + e.getMessage());
                }
            }

            if (chunk.getChunkNumber() == chunk.getTotalChunks() - 1) {
                // Verify final file size
                long actualSize = Files.size(path);
                if (actualSize != totalBytes) {
                    System.err.println("File size mismatch: expected " + totalBytes + ", got " + actualSize);
                    // Delete the incomplete file
                    try {
                        Files.deleteIfExists(path);
                    } catch (IOException ex) {
                        System.err.println("Failed to delete incomplete file: " + ex.getMessage());
                    }
                    client.sendMessage(new FileTransferError(chunk.getTransferId(),
                            "File size verification failed. Transfer incomplete. Please retry."));
                    return;
                }

                try {
                    FileTransferRepository.updateFileTransferStatus(chunk.getTransferId(), "COMPLETED");
                } catch (SQLException e) {
                    System.err.println("Failed to update transfer status: " + e.getMessage());
                    // Continue with cleanup even if database update fails
                }

                // Clean up
                transferPaths.remove(chunk.getTransferId());
                transferSizes.remove(chunk.getTransferId());
                transferredBytes.remove(chunk.getTransferId());
                recipients.remove(chunk.getTransferId());

                client.sendMessage(new FileTransferComplete(chunk.getTransferId(), null, 0, 0, 0, false, 0));
            }
        } catch (IOException e) {
            System.err.println("Error writing file chunk: " + e.getMessage());
            client.sendMessage(new FileTransferError(chunk.getTransferId(),
                    "Error writing file: " + e.getMessage() + ". Please retry."));
            try {
                FileTransferRepository.updateFileTransferStatus(chunk.getTransferId(), "FAILED");
            } catch (SQLException ex) {
                System.err.println("Failed to update transfer status: " + ex.getMessage());
            }
        }
    }
}
