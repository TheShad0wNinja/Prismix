package com.prismix.client.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventBus;
import com.prismix.client.data.repository.FileTransferRepository;
import com.prismix.client.gui.components.themed.ThemedProgressBar;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.common.model.Message;
import com.prismix.common.model.network.*;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileTransferHandler implements ResponseHandler {
    private final EventBus eventBus;
    private final UserHandler authHandler;
    private final Map<String, FileOutputStream> fileStreams;
    private final Map<String, ThemedProgressBar> progressBars;
    private final Path downloadDirectory;
    private static final int CHUNK_SIZE = 8192; // 8KB chunks
    private final Map<String, File> pendingFiles;
    private final Map<String, Integer> chunkRetries = new HashMap<>();
    private static final int MAX_RETRIES = 3;

    public FileTransferHandler(EventBus eventBus, UserHandler authHandler,
            HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandlers) {
        this.eventBus = eventBus;
        this.authHandler = authHandler;
        this.fileStreams = new HashMap<>();
        this.progressBars = new HashMap<>();
        this.downloadDirectory = Paths.get(System.getProperty("user.home"), "Prismix", "Downloads");
        this.pendingFiles = new HashMap<>();

        try {
            Files.createDirectories(downloadDirectory);
            System.out.println("Download directory created at: " + downloadDirectory.toAbsolutePath());
        } catch (IOException e) {
            System.err.println("Failed to create download directory: " + e.getMessage());
        }

        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_REQUEST, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_UPLOAD_REQUEST, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_UPLOAD_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_DOWNLOAD_REQUEST, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_DOWNLOAD_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_PROGRESS, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_CHUNK, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_COMPLETE, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_ERROR, this);
    }

    public void sendFile(File file, int roomId, boolean isDirect, int receiverId) {
        if (authHandler.getUser() == null) {
            System.err.println("Cannot send file: User not logged in");
            return;
        }

        try {
            // Create a message with the file name
            Message fileMessage = new Message(
                    0,
                    authHandler.getUser().getId(),
                    receiverId,
                    roomId,
                    "FILE:" + file.getName(),
                    isDirect,
                    new java.sql.Timestamp(System.currentTimeMillis()));

            // Send the file message
            ApplicationContext.getMessageHandler().sendTextMessage(fileMessage);

            // Send the actual file
            FileTransferUploadRequest request = new FileTransferUploadRequest(
                    file.getName(),
                    file.length(),
                    authHandler.getUser().getId(),
                    roomId,
                    isDirect,
                    receiverId);

            // Store file for later use
            pendingFiles.put(file.getName(), file);

            ConnectionManager.getInstance().sendMessage(request);
        } catch (IOException e) {
            System.err.println("Error sending file transfer request: " + e.getMessage());
        }
    }

    public void downloadFile(String fileName, int roomId) {
        if (authHandler.getUser() == null) {
            System.err.println("Cannot download file: User not logged in");
            return;
        }

        try {
            // Clean up any stale transfers for this file
            cleanupStaleTransfers(fileName);

            FileTransferDownloadRequest request = new FileTransferDownloadRequest(
                    fileName,
                    authHandler.getUser().getId(),
                    roomId);

            ConnectionManager.getInstance().sendMessage(request);
        } catch (IOException e) {
            System.err.println("Error sending file download request: " + e.getMessage());
        }
    }

    private void cleanupStaleTransfers(String fileName) {
        try {
            // Get all transfer records for this file
            List<String> transferIds = FileTransferRepository.getTransferIdsForFile(fileName);
            for (String transferId : transferIds) {
                // Clean up file streams
                FileOutputStream stream = fileStreams.remove(transferId);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        System.err.println("Error closing stale file stream: " + e.getMessage());
                    }
                }

                // Clean up progress bars
                ThemedProgressBar progressBar = progressBars.remove(transferId);
                if (progressBar != null) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setString("Transfer cancelled");
                    });
                }

                // Clean up retry counters
                chunkRetries.remove(transferId);

                // Delete any incomplete files
                try {
                    String filePath = FileTransferRepository.getFilePath(transferId);
                    if (filePath != null) {
                        Files.deleteIfExists(Paths.get(filePath));
                    }
                } catch (Exception e) {
                    System.err.println("Error deleting stale file: " + e.getMessage());
                }

                // Update database record
                FileTransferRepository.updateFileTransferStatus(transferId, "CANCELLED");
            }
        } catch (SQLException e) {
            System.err.println("Error cleaning up stale transfers: " + e.getMessage());
        }
    }

    @Override
    public void handleResponse(NetworkMessage message) {
        switch (message.getMessageType()) {
            case FILE_TRANSFER_REQUEST -> handleTransferRequest((FileTransferRequest) message);
            case FILE_TRANSFER_RESPONSE -> handleTransferResponse((FileTransferResponse) message);
            case FILE_TRANSFER_UPLOAD_REQUEST -> handleUploadRequest((FileTransferUploadRequest) message);
            case FILE_TRANSFER_UPLOAD_RESPONSE -> handleUploadResponse((FileTransferUploadResponse) message);
            case FILE_TRANSFER_DOWNLOAD_REQUEST -> handleDownloadRequest((FileTransferDownloadRequest) message);
            case FILE_TRANSFER_DOWNLOAD_RESPONSE -> handleDownloadResponse((FileTransferDownloadResponse) message);
            case FILE_TRANSFER_PROGRESS -> handleTransferProgress((FileTransferProgress) message);
            case FILE_TRANSFER_CHUNK -> handleTransferChunk((FileTransferChunk) message);
            case FILE_TRANSFER_COMPLETE -> handleTransferComplete((FileTransferComplete) message);
            case FILE_TRANSFER_ERROR -> handleTransferError((FileTransferError) message);
        }
    }

    // Legacy methods for backward compatibility
    private void handleTransferRequest(FileTransferRequest request) {
        handleUploadRequest(new FileTransferUploadRequest(
                request.getFileName(),
                request.getFileSize(),
                request.getSenderId(),
                request.getRoomId(),
                request.isDirect(),
                request.getReceiverId()));
    }

    private void handleTransferResponse(FileTransferResponse response) {
        handleUploadResponse(new FileTransferUploadResponse(
                response.isAccepted(),
                response.getMessage(),
                response.getTransferId()));
    }

    // New methods for handling uploads
    private void handleUploadRequest(FileTransferUploadRequest request) {
        try {
            // Create file output stream
            String fileName = request.getFileName();
            Path filePath = this.downloadDirectory.resolve(fileName);

            // Check if file already exists and add a number if it does
            int counter = 1;
            String baseName = fileName;
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }

            while (Files.exists(filePath)) {
                fileName = baseName + " (" + counter + ")" + extension;
                filePath = this.downloadDirectory.resolve(fileName);
                counter++;
            }

            System.out.println("Creating file at: " + filePath.toAbsolutePath());
            FileOutputStream fos = new FileOutputStream(filePath.toFile());

            // Generate a transfer ID
            String transferId = UUID.randomUUID().toString();
            fileStreams.put(transferId, fos);

            // Create progress bar
            ThemedProgressBar progressBar = new ThemedProgressBar();
            progressBar.setMaximum(100);
            progressBar.setString(fileName);
            progressBars.put(transferId, progressBar);

            // Show progress bar in a dialog
            SwingUtilities.invokeLater(() -> {
                JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(null), "Download Progress",
                        false);
                dialog.setLayout(new BorderLayout());
                dialog.add(progressBar, BorderLayout.CENTER);
                dialog.pack();
                dialog.setLocationRelativeTo(null);
                dialog.setVisible(true);
            });

            System.out.println("send help pls");
            // Create transfer record in database
            FileTransferRepository.createFileTransfer(request, filePath.toString(), transferId);

            // Accept the transfer
            ConnectionManager.getInstance()
                    .sendMessage(new FileTransferUploadResponse(true, request.getFileName(), transferId));

        } catch (IOException | SQLException e) {
            System.err.println("Error handling incoming transfer: " + e.getMessage());
            try {
                ConnectionManager.getInstance()
                        .sendMessage(new FileTransferUploadResponse(false, request.getFileName(), null));
            } catch (Exception ex) {
                System.err.println("Error sending error response: " + ex.getMessage());
            }
        }
    }

    private void handleUploadResponse(FileTransferUploadResponse response) {
        if (!response.isAccepted()) {
            System.err.println("File transfer rejected: " + response.getFileName());
            return;
        }

        String transferId = response.getTransferId();
        File file = pendingFiles.remove(response.getFileName()); // filename contains original filename
        if (file == null) {
            System.err.println("File not found for transfer: " + response.getFileName());
            return;
        }

        // Create and show progress bar after transfer is accepted
        ThemedProgressBar progressBar = new ThemedProgressBar();
        progressBar.setMaximum(100);
        progressBar.setString(file.getName());
        progressBars.put(transferId, progressBar);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, progressBar, "File Upload Progress", JOptionPane.PLAIN_MESSAGE);
        });

        // Start sending file chunks in a background thread
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int totalChunks = (int) Math.ceil((double) file.length() / CHUNK_SIZE);
                int chunkNumber = 0;
                int bytesRead;
                long totalBytesRead = 0;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    // Create a new buffer with exact size to avoid any buffer issues
                    byte[] chunk = new byte[bytesRead];
                    System.arraycopy(buffer, 0, chunk, 0, bytesRead);

                    // Verify the chunk data before sending
                    if (chunk.length != bytesRead) {
                        throw new IOException("Chunk size mismatch: expected " + bytesRead + ", got " + chunk.length);
                    }

                    FileTransferChunk chunkMessage = new FileTransferChunk(
                            transferId,
                            chunk,
                            chunkNumber,
                            totalChunks);

                    // Send chunk and wait for acknowledgment
                    ConnectionManager.getInstance().sendMessage(chunkMessage);
                    totalBytesRead += bytesRead;

                    // Update progress bar
                    int progress = (int) ((chunkNumber + 1.0) / totalChunks * 100);
                    ThemedProgressBar pb = progressBars.get(transferId);
                    if (pb != null) {
                        SwingUtilities.invokeLater(() -> {
                            pb.setValue(progress);
                            pb.setString(String.format("%s (%d%%)", file.getName(), progress));
                        });
                    }

                    // Add a small delay between chunks to prevent overwhelming the network
                    Thread.sleep(10);

                    chunkNumber++;
                }

                // Verify total bytes read matches file size
                if (totalBytesRead != file.length()) {
                    throw new IOException("File size mismatch: expected " + file.length() + ", read " + totalBytesRead);
                }

            } catch (IOException | InterruptedException e) {
                System.err.println("Error sending file chunks: " + e.getMessage());
                ThemedProgressBar pb = progressBars.remove(transferId);
                if (pb != null) {
                    SwingUtilities.invokeLater(() -> {
                        pb.setString("Error sending file: " + e.getMessage());
                    });
                }
                try {
                    ConnectionManager.getInstance()
                            .sendMessage(new FileTransferError(transferId, "Error sending file: " + e.getMessage()));
                } catch (Exception ex) {
                    System.err.println("Error sending error message: " + ex.getMessage());
                }
            }
        }).start();
    }

    // New methods for handling downloads
    private void handleDownloadRequest(FileTransferDownloadRequest request) {
        // Client shouldn't receive download requests
        System.err.println("Received unexpected download request: " + request.getFileName());
    }

    private void handleDownloadResponse(FileTransferDownloadResponse response) {
        if (!response.isAccepted()) {
            System.err.println("File download rejected: " + response.getFileName());
            return;
        }

        String transferId = response.getTransferId();
        String fileName = response.getFileName();

        // Create file in download directory
        try {
            Path filePath = this.downloadDirectory.resolve(fileName);

            // Check if file already exists and add a number if it does
            int counter = 1;
            String baseName = fileName;
            String extension = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                baseName = fileName.substring(0, dotIndex);
                extension = fileName.substring(dotIndex);
            }

            while (Files.exists(filePath)) {
                fileName = baseName + " (" + counter + ")" + extension;
                filePath = this.downloadDirectory.resolve(fileName);
                counter++;
            }

            // Create parent directories if they don't exist
            Files.createDirectories(filePath.getParent());

            System.out.println("Creating download file at: " + filePath.toAbsolutePath());
            FileOutputStream fos = new FileOutputStream(filePath.toFile());
            fileStreams.put(transferId, fos);

            // Create progress bar
            ThemedProgressBar progressBar = new ThemedProgressBar();
            progressBar.setMaximum(100);
            progressBar.setString(fileName);
            progressBars.put(transferId, progressBar);

            // Show progress bar in a dialog
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, progressBar, "File Download Progress", JOptionPane.PLAIN_MESSAGE);
            });

            // Create transfer record in database
            FileTransferRequest dummyRequest = new FileTransferRequest(
                    fileName,
                    response.getFileSize(),
                    0, // No sender for download
                    0, // No room for download
                    false,
                    authHandler.getUser().getId());

            FileTransferRepository.createFileTransfer(dummyRequest, filePath.toString(), transferId);

        } catch (IOException | SQLException e) {
            System.err.println("Error preparing for download: " + e.getMessage());
            try {
                ConnectionManager.getInstance()
                        .sendMessage(new FileTransferError(transferId, "Error preparing download: " + e.getMessage()));
            } catch (Exception ex) {
                System.err.println("Error sending error message: " + ex.getMessage());
            }
        }
    }

    private void handleTransferProgress(FileTransferProgress progress) {
        ThemedProgressBar progressBar = progressBars.get(progress.getTransferId());
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress.getProgress());
                progressBar.setString(String.format("%s (%d%%)", progress.getFileName(), progress.getProgress()));
            });
        }
    }

    private void handleTransferChunk(FileTransferChunk chunk) {
        try {
            // Verify checksum before writing
            if (!chunk.verifyChecksum()) {
                System.err.println("Checksum verification failed for chunk " + chunk.getChunkNumber());

                // Implement retry logic
                int retryCount = chunkRetries.getOrDefault(chunk.getTransferId(), 0);
                if (retryCount < MAX_RETRIES) {
                    chunkRetries.put(chunk.getTransferId(), retryCount + 1);
                    System.out.println(
                            "Retrying chunk " + chunk.getChunkNumber() + " (attempt " + (retryCount + 1) + ")");
                    // Request retry from server
                    ConnectionManager.getInstance()
                            .sendMessage(new FileTransferError(chunk.getTransferId(),
                                    "RETRY_CHUNK:" + chunk.getChunkNumber()));
                    return;
                } else {
                    System.err.println("Max retries reached for chunk " + chunk.getChunkNumber());
                    ConnectionManager.getInstance()
                            .sendMessage(new FileTransferError(chunk.getTransferId(),
                                    "Data corruption detected in chunk " + chunk.getChunkNumber() + " after "
                                            + MAX_RETRIES + " retries"));
                    return;
                }
            }

            // Reset retry count on successful chunk
            chunkRetries.remove(chunk.getTransferId());

            FileOutputStream fos = fileStreams.get(chunk.getTransferId());
            if (fos != null) {
                // Get a defensive copy of the data
                byte[] data = chunk.getData();
                fos.write(data);
                fos.flush();
            } else {
                System.err.println("No file stream found for transfer: " + chunk.getTransferId());
                // Try to get the file path from the database
                try {
                    String filePath = FileTransferRepository.getFilePath(chunk.getTransferId());
                    if (filePath != null) {
                        Path path = Paths.get(filePath);
                        if (Files.exists(path)) {
                            // Create a new file stream
                            FileOutputStream newFos = new FileOutputStream(path.toFile(), true);
                            fileStreams.put(chunk.getTransferId(), newFos);
                            byte[] data = chunk.getData();
                            newFos.write(data);
                            newFos.flush();
                        } else {
                            throw new IOException("File path exists in database but file not found: " + filePath);
                        }
                    } else {
                        throw new IOException("No file path found in database for transfer: " + chunk.getTransferId());
                    }
                } catch (SQLException e) {
                    System.err.println("Error getting file path from database: " + e.getMessage());
                    throw new IOException("Database error while retrieving file path", e);
                }
            }

            // If this is the last chunk, verify file size and close stream
            if (chunk.getChunkNumber() == chunk.getTotalChunks() - 1) {
                FileOutputStream stream = fileStreams.get(chunk.getTransferId());
                if (stream != null) {
                    stream.close();
                    fileStreams.remove(chunk.getTransferId());

                    // Verify final file size
                    try {
                        String filePath = FileTransferRepository.getFilePath(chunk.getTransferId());
                        if (filePath != null) {
                            long actualSize = Files.size(Paths.get(filePath));
                            long expectedSize = FileTransferRepository.getFileSize(chunk.getTransferId());
                            if (actualSize != expectedSize) {
                                throw new IOException(
                                        "File size mismatch: expected " + expectedSize + ", got " + actualSize);
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error verifying file size: " + e.getMessage());
                    }
                }
                // Clean up retry counter
                chunkRetries.remove(chunk.getTransferId());
            }
        } catch (IOException e) {
            System.err.println("Error writing file chunk: " + e.getMessage());
            try {
                // Close and remove the file stream
                FileOutputStream stream = fileStreams.remove(chunk.getTransferId());
                if (stream != null) {
                    stream.close();
                }

                // Delete incomplete file
                try {
                    String filePath = FileTransferRepository.getFilePath(chunk.getTransferId());
                    if (filePath != null) {
                        Files.deleteIfExists(Paths.get(filePath));
                    }
                } catch (Exception ex) {
                    System.err.println("Error deleting incomplete file: " + ex.getMessage());
                }

                // Send error message to server
                ConnectionManager.getInstance()
                        .sendMessage(new FileTransferError(chunk.getTransferId(),
                                "Error writing file: " + e.getMessage()));

                // Update progress bar
                ThemedProgressBar progressBar = progressBars.remove(chunk.getTransferId());
                if (progressBar != null) {
                    SwingUtilities.invokeLater(() -> {
                        progressBar.setString("Transfer failed: " + e.getMessage());
                    });
                }

                // Clean up retry counter
                chunkRetries.remove(chunk.getTransferId());
            } catch (Exception ex) {
                System.err.println("Error handling transfer error: " + ex.getMessage());
            }
        }
    }

    private void handleTransferComplete(FileTransferComplete complete) {
        try {
            FileOutputStream fos = fileStreams.remove(complete.getTransferId());
            if (fos != null) {
                fos.close();
            }

            ThemedProgressBar progressBar = progressBars.remove(complete.getTransferId());
            if (progressBar != null) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setValue(100);
                    progressBar.setString("Transfer complete");

                    // Show completion dialog with file location
                    JOptionPane.showMessageDialog(null,
                            "File download complete!\nSaved to: " + downloadDirectory,
                            "Download Complete",
                            JOptionPane.INFORMATION_MESSAGE);
                });
            }

            FileTransferRepository.updateFileTransferStatus(complete.getTransferId(), "COMPLETED");
            eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.FILE_TRANSFER_COMPLETE, complete));

        } catch (IOException | SQLException e) {
            System.err.println("Error completing transfer: " + e.getMessage());
        }
    }

    private void handleTransferError(FileTransferError error) {
        try {
            FileOutputStream fos = fileStreams.remove(error.getTransferId());
            if (fos != null) {
                fos.close();
            }

            ThemedProgressBar progressBar = progressBars.remove(error.getTransferId());
            if (progressBar != null) {
                SwingUtilities.invokeLater(() -> {
                    progressBar.setString("Transfer failed: " + error.getErrorMessage());
                });
            }

            if (error.getTransferId() != null) {
                FileTransferRepository.updateFileTransferStatus(error.getTransferId(), "FAILED");
            }
            eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.FILE_TRANSFER_ERROR, error));

        } catch (IOException | SQLException e) {
            System.err.println("Error handling transfer error: " + e.getMessage());
        }
    }

    public void selectAndSendFile(int roomId, boolean isDirect, int receiverId) {
        if (authHandler.getUser() == null) {
            System.err.println("Cannot send file: User not logged in");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Select a file to send");
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(false);

        if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            if (selectedFile != null && selectedFile.exists() && selectedFile.isFile()) {
                sendFile(selectedFile, roomId, isDirect, receiverId);
            }
        }
    }

    public void selectAndSendFileToRoom(int roomId) {
        selectAndSendFile(roomId, false, 0);
    }

    public void selectAndSendFileToUser(int userId) {
        selectAndSendFile(0, true, userId);
    }
}
