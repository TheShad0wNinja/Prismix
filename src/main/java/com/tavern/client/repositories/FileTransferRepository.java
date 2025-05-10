package com.tavern.client.repositories;

import com.tavern.common.model.network.FileTransferRequest;
import com.tavern.common.model.network.FileTransferUploadRequest;
import com.tavern.client.utils.ClientDatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileTransferRepository {
    private FileTransferRepository() {
    }

    public static int createFileTransfer(FileTransferRequest request, String filePath) throws SQLException {
        return createFileTransferInternal(
                request.getFileName(),
                filePath,
                request.getFileSize(),
                request.getSenderId(),
                request.getRoomId(),
                request.getReceiverId(),
                request.isDirect(),
                request.getFileName());
    }

    public static int createFileTransfer(FileTransferUploadRequest request, String filePath) throws SQLException {
        return createFileTransferInternal(
                request.getFileName(),
                filePath,
                request.getFileSize(),
                request.getSenderId(),
                request.getRoomId(),
                request.getReceiverId(),
                request.isDirect(),
                request.getFileName());
    }

    public static int createFileTransfer(FileTransferRequest request, String filePath, String transferId)
            throws SQLException {
        return createFileTransferInternal(
                request.getFileName(),
                filePath,
                request.getFileSize(),
                request.getSenderId(),
                request.getRoomId(),
                request.getReceiverId(),
                request.isDirect(),
                transferId);
    }

    public static int createFileTransfer(FileTransferUploadRequest request, String filePath, String transferId)
            throws SQLException {
        return createFileTransferInternal(
                request.getFileName(),
                filePath,
                request.getFileSize(),
                request.getSenderId(),
                request.getRoomId(),
                request.getReceiverId(),
                request.isDirect(),
                transferId);
    }

    private static int createFileTransferInternal(String fileName, String filePath, long fileSize,
            int senderId, int roomId, int receiverId, boolean isDirect, String transferId) throws SQLException {
        String sql = """
                INSERT INTO file_transfer (
                    file_name, file_path, file_size, sender_id, room_id, receiver_id, is_direct, status, transfer_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)
                """;

        try (Connection conn = ClientDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, fileName);
            pstmt.setString(2, filePath);
            pstmt.setLong(3, fileSize);
            pstmt.setInt(4, senderId);
            pstmt.setInt(5, roomId);
            pstmt.setInt(6, receiverId);
            pstmt.setBoolean(7, isDirect);
            pstmt.setString(8, transferId);

            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1);
            }
            return -1;
        }
    }

    public static void updateFileTransferStatus(String transferId, String status) throws SQLException {
        String sql = "UPDATE file_transfer SET status = ? WHERE transfer_id = ?";

        try (Connection conn = ClientDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, transferId);
            pstmt.executeUpdate();
        }
    }

    public static List<FileTransferInfo> getPendingTransfers() throws SQLException {
        String sql = "SELECT * FROM file_transfer WHERE status = 'PENDING'";
        List<FileTransferInfo> transfers = new ArrayList<>();

        try (Connection conn = ClientDatabaseManager.getConnection();
                Statement stmt = conn.createStatement();
                ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                transfers.add(new FileTransferInfo(
                        rs.getInt("id"),
                        rs.getString("file_name"),
                        rs.getString("file_path"),
                        rs.getLong("file_size"),
                        rs.getInt("sender_id"),
                        rs.getInt("room_id"),
                        rs.getInt("receiver_id"),
                        rs.getBoolean("is_direct"),
                        rs.getString("status"),
                        rs.getString("transfer_id")));
            }
        }
        return transfers;
    }

    public static String getFilePath(String transferId) throws SQLException {
        String sql = "SELECT file_path FROM file_transfer WHERE transfer_id = ?";

        try (Connection conn = ClientDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transferId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("file_path");
            }
            return null;
        }
    }

    public static class FileTransferInfo {
        private final int id;
        private final String fileName;
        private final String filePath;
        private final long fileSize;
        private final int senderId;
        private final int roomId;
        private final int receiverId;
        private final boolean isDirect;
        private final String status;
        private final String transferId;

        public FileTransferInfo(int id, String fileName, String filePath, long fileSize,
                int senderId, int roomId, int receiverId, boolean isDirect, String status, String transferId) {
            this.id = id;
            this.fileName = fileName;
            this.filePath = filePath;
            this.fileSize = fileSize;
            this.senderId = senderId;
            this.roomId = roomId;
            this.receiverId = receiverId;
            this.isDirect = isDirect;
            this.status = status;
            this.transferId = transferId;
        }

        public int getId() {
            return id;
        }

        public String getFileName() {
            return fileName;
        }

        public String getFilePath() {
            return filePath;
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

        public String getStatus() {
            return status;
        }

        public String getTransferId() {
            return transferId;
        }
    }
}
