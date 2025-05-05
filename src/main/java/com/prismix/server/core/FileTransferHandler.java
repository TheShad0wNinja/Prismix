package com.prismix.server.core;

import com.prismix.common.model.network.*;
import com.prismix.server.data.repository.UserRepository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class FileTransferHandler implements RequestHandler {
    private final AuthHandler authHandler;
    private final Map<String, FileOutputStream> fileStreams;
    private final Map<String, Long> transferProgress;
    private final String uploadDir;

    public FileTransferHandler(AuthHandler authHandler,
            HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.authHandler = authHandler;
        this.fileStreams = new HashMap<>();
        this.transferProgress = new HashMap<>();
        this.uploadDir = "uploads/";

        // Create upload directory if it doesn't exist
        new File(uploadDir).mkdirs();

        requestHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_REQUEST, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        if (message instanceof FileTransferRequest request) {
            handleTransferRequest(request, client);
        }
    }

    private void handleTransferRequest(FileTransferRequest request, ClientHandler client) {
        try {
            // Create file output stream
            String filePath = uploadDir + request.getFileName();
            FileOutputStream fos = new FileOutputStream(filePath);
            fileStreams.put(request.getFileName(), fos);
            transferProgress.put(request.getFileName(), 0L);

            // Send response to sender
            client.sendMessage(new FileTransferResponse(true, "Transfer accepted", request.getFileName()));

            // Notify recipient
            if (request.isDirect()) {
                try {
                    var recipient = UserRepository.getUserById(request.getReceiverId());
                    if (recipient != null && authHandler.getActiveUsers().containsKey(recipient)) {
                        authHandler.getActiveUsers().get(recipient).sendMessage(request);
                    }
                } catch (Exception e) {
                    System.out.println("Error notifying recipient: " + e.getMessage());
                }
            }

            // Start progress updates
            new Thread(() -> {
                try {
                    long totalBytes = request.getFileSize();
                    long transferredBytes = 0;
                    int lastProgress = 0;

                    while (transferredBytes < totalBytes) {
                        Thread.sleep(1000); // Update every second
                        transferredBytes = transferProgress.get(request.getFileName());
                        int progress = (int) ((transferredBytes * 100) / totalBytes);

                        if (progress != lastProgress) {
                            client.sendMessage(new FileTransferProgress(
                                    request.getFileName(),
                                    progress,
                                    transferredBytes,
                                    totalBytes));
                            lastProgress = progress;
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error updating progress: " + e.getMessage());
                }
            }).start();

        } catch (IOException e) {
            System.out.println("Error handling file transfer: " + e.getMessage());
            try {
                client.sendMessage(new FileTransferResponse(false, "Error: " + e.getMessage(), request.getFileName()));
            } catch (Exception ex) {
                System.out.println("Error sending error response: " + ex.getMessage());
            }
        }
    }
}