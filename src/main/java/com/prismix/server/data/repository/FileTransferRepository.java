package com.prismix.server.data.repository;

import com.prismix.common.model.network.FileTransferRequest;
import com.prismix.common.model.network.FileTransferUploadRequest;
import com.prismix.server.utils.ServerDatabaseManager;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;

public class FileTransferRepository {
    private FileTransferRepository() {
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

    public static int createDownloadTransfer(int requesterId, int roomId, String filePath, String fileName,
            String transferId) throws SQLException {
        String sql = """
                INSERT INTO file_transfer (
                    file_name, file_path, transfer_id, file_size, sender_id, room_id, receiver_id, is_direct, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
                """;

        try (Connection conn = ServerDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, fileName);
            pstmt.setString(2, filePath);
            pstmt.setString(3, transferId);
            pstmt.setLong(4, new File(filePath).length());
            pstmt.setInt(5, 0); // No sender for download
            pstmt.setInt(6, roomId);
            pstmt.setInt(7, requesterId);
            pstmt.setBoolean(8, false);

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        }
    }

    private static int createFileTransferInternal(String fileName, String filePath, long fileSize,
            int senderId, int roomId, int receiverId, boolean isDirect,
            String transferId) throws SQLException {
        String sql = """
                INSERT INTO file_transfer (
                    file_name, file_path, transfer_id, file_size, sender_id, room_id, receiver_id, is_direct, status
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')
                """;

        try (Connection conn = ServerDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, fileName);
            pstmt.setString(2, filePath);
            pstmt.setString(3, transferId);
            pstmt.setLong(4, fileSize);
            pstmt.setInt(5, senderId);
            pstmt.setInt(6, roomId);
            pstmt.setInt(7, receiverId);
            pstmt.setBoolean(8, isDirect);

            pstmt.executeUpdate();
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return -1;
        }
    }

    public static void updateFileTransferStatus(String transferId, String status) throws SQLException {
        String sql = "UPDATE file_transfer SET status = ? WHERE transfer_id = ?";

        try (Connection conn = ServerDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, status);
            pstmt.setString(2, transferId);
            pstmt.executeUpdate();
        }
    }

    public static String getFilePath(String transferId) throws SQLException {
        String sql = "SELECT file_path FROM file_transfer WHERE transfer_id = ?";

        try (Connection conn = ServerDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transferId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("file_path");
                }
            }
        }
        return null;
    }

    public static String getFileName(String transferId) throws SQLException {
        String sql = "SELECT file_name FROM file_transfer WHERE transfer_id = ?";

        try (Connection conn = ServerDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, transferId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString("file_name");
            }
            return null;
        }
    }
}
