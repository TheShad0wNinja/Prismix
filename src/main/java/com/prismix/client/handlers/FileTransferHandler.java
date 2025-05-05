package com.prismix.client.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventBus;
import com.prismix.client.data.repository.FileTransferRepository;
import com.prismix.client.gui.components.themed.ThemedProgressBar;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.common.model.network.*;

import javax.swing.*;
import java.io.*;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class FileTransferHandler implements ResponseHandler {
    private final EventBus eventBus;
    private final AuthHandler authHandler;
    private final Map<String, FileOutputStream> fileStreams;
    private final Map<String, ThemedProgressBar> progressBars;
    private final Path downloadDirectory;
    private static final int CHUNK_SIZE = 8192; // 8KB chunks
    private final Map<String, File> pendingFiles;

    public FileTransferHandler(EventBus eventBus, AuthHandler authHandler,
            HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandlers) {
        this.eventBus = eventBus;
        this.authHandler = authHandler;
        this.fileStreams = new HashMap<>();
        this.progressBars = new HashMap<>();
        this.downloadDirectory = Paths.get("downloads");
        this.pendingFiles = new HashMap<>();

        try {
            Files.createDirectories(downloadDirectory);
        } catch (IOException e) {
            System.err.println("Failed to create download directory: " + e.getMessage());
        }

        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_REQUEST, this);
        responseHandlers.put(NetworkMessage.MessageType.FILE_TRANSFER_RESPONSE, this);
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
            FileTransferRequest request = new FileTransferRequest(
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

    @Override
    public void handleResponse(NetworkMessage message) {
        switch (message.getMessageType()) {
            case FILE_TRANSFER_REQUEST -> handleIncomingTransfer((FileTransferRequest) message);
            case FILE_TRANSFER_RESPONSE -> handleTransferResponse((FileTransferResponse) message);
            case FILE_TRANSFER_PROGRESS -> handleTransferProgress((FileTransferProgress) message);
            case FILE_TRANSFER_CHUNK -> handleTransferChunk((FileTransferChunk) message);
            case FILE_TRANSFER_COMPLETE -> handleTransferComplete((FileTransferComplete) message);
            case FILE_TRANSFER_ERROR -> handleTransferError((FileTransferError) message);
        }
    }

    private void handleIncomingTransfer(FileTransferRequest request) {
        try {
            // Create file output stream
            String fileName = request.getFileName();
            Path filePath = downloadDirectory.resolve(fileName);
            FileOutputStream fos = new FileOutputStream(filePath.toFile());
            fileStreams.put(fileName, fos);

            // Create progress bar
            ThemedProgressBar progressBar = new ThemedProgressBar();
            progressBar.setMaximum(100);
            progressBar.setString(fileName);
            progressBars.put(fileName, progressBar);

            // Show progress bar
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null, progressBar, "File Transfer Progress", JOptionPane.PLAIN_MESSAGE);
            });

            // Create transfer record in database
            FileTransferRepository.createFileTransfer(request, filePath.toString());

        } catch (IOException | SQLException e) {
            System.err.println("Error handling incoming transfer: " + e.getMessage());
        }
    }

    private void handleTransferResponse(FileTransferResponse response) {
        if (!response.isAccepted()) {
            System.err.println("File transfer rejected: " + response.getMessage());
            return;
        }

        String transferId = response.getTransferId();
        File file = pendingFiles.remove(response.getMessage()); // message contains original filename
        if (file == null) {
            System.err.println("File not found for transfer: " + response.getMessage());
            return;
        }

        // Create and show progress bar after transfer is accepted
        ThemedProgressBar progressBar = new ThemedProgressBar();
        progressBar.setMaximum(100);
        progressBar.setString(file.getName());
        progressBars.put(transferId, progressBar);

        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(null, progressBar, "File Transfer Progress", JOptionPane.PLAIN_MESSAGE);
        });

        // Start sending file chunks in a background thread
        new Thread(() -> {
            try (FileInputStream fis = new FileInputStream(file)) {
                byte[] buffer = new byte[CHUNK_SIZE];
                int totalChunks = (int) Math.ceil((double) file.length() / CHUNK_SIZE);
                int chunkNumber = 0;
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    byte[] chunk = bytesRead < CHUNK_SIZE ? Arrays.copyOf(buffer, bytesRead) : buffer;
                    FileTransferChunk chunkMessage = new FileTransferChunk(
                            transferId,
                            chunk,
                            chunkNumber,
                            totalChunks);

                    ConnectionManager.getInstance().sendMessage(chunkMessage);

                    // Update progress bar
                    int progress = (int) ((chunkNumber + 1.0) / totalChunks * 100);
                    ThemedProgressBar pb = progressBars.get(transferId);
                    if (pb != null) {
                        SwingUtilities.invokeLater(() -> {
                            pb.setValue(progress);
                            pb.setString(String.format("%s (%d%%)", file.getName(), progress));
                        });
                    }

                    chunkNumber++;
                }
            } catch (IOException e) {
                System.err.println("Error sending file chunks: " + e.getMessage());
                ThemedProgressBar pb = progressBars.remove(transferId);
                if (pb != null) {
                    SwingUtilities.invokeLater(() -> {
                        pb.setString("Error sending file: " + e.getMessage());
                    });
                }
            }
        }).start();
    }

    private void handleTransferProgress(FileTransferProgress progress) {
        ThemedProgressBar progressBar = progressBars.get(progress.getFileName());
        if (progressBar != null) {
            SwingUtilities.invokeLater(() -> {
                progressBar.setValue(progress.getProgress());
                progressBar.setString(String.format("%s (%d%%)", progress.getFileName(), progress.getProgress()));
            });
        }
    }

    private void handleTransferChunk(FileTransferChunk chunk) {
        try {
            FileOutputStream fos = fileStreams.get(chunk.getTransferId());
            if (fos != null) {
                fos.write(chunk.getData());
                fos.flush();
            }
        } catch (IOException e) {
            System.err.println("Error writing file chunk: " + e.getMessage());
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