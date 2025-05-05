package com.prismix.server.data.repository;

import com.prismix.common.model.network.FileTransferRequest;
import com.prismix.server.utils.ServerDatabaseManager;

import java.sql.*;
import java.time.LocalDateTime;

public class FileTransferRepository {
    private FileTransferRepository() {
    }

    public static int createFileTransfer(FileTransferRequest request, String filePath, String transfer_id)
            throws SQLException {
        String sql = """
                INSERT INTO file_transfer (
                    file_name, file_path, file_size, sender_id, room_id, receiver_id, is_direct, status, transfer_id
                ) VALUES (?, ?, ?, ?, ?, ?, ?, 'PENDING', ?)
                """;

        try (Connection conn = ServerDatabaseManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, request.getFileName());
            pstmt.setString(2, filePath);
            pstmt.setLong(3, request.getFileSize());
            pstmt.setInt(4, request.getSenderId());
            pstmt.setInt(5, request.getRoomId());
            pstmt.setInt(6, request.getReceiverId());
            pstmt.setBoolean(7, request.isDirect());
            pstmt.setString(8, transfer_id);

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
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("file_path");
            }
            return null;
        }
    }
}