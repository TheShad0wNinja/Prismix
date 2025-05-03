package com.prismix.server.data.repository;

import com.prismix.common.model.Message;
import com.prismix.server.utils.ServerDatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MessageRepository {

    private MessageRepository() {}

    public static Message createMessage(Message message) throws SQLException {
        String sql = message.isDirect()
                ? "INSERT INTO message (sender_id, receiver_id, content, direct) VALUES (?, ?, ?, ?)"
                : "INSERT INTO message (sender_id, room_id, content, direct) VALUES (?, ?, ?, ?)";

        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, message.getSenderId());
            if (message.isDirect()) {
                pstmt.setInt(2, message.getReceiverId());
            } else {
                pstmt.setInt(2, message.getRoomId());
            }
            pstmt.setString(3, message.getContent());
            pstmt.setBoolean(4, message.isDirect());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating message failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int id = generatedKeys.getInt(1);
                    
                    // Get the timestamp from the database
                    String getTimestampSql = "SELECT timestamp FROM message WHERE id = ?";
                    Timestamp timestamp = null;
                    try (PreparedStatement tsStmt = conn.prepareStatement(getTimestampSql)) {
                        tsStmt.setInt(1, id);
                        ResultSet rs = tsStmt.executeQuery();
                        if (rs.next()) {
                            timestamp = rs.getTimestamp("timestamp");
                        }
                    }
                    
                    return new Message(id, message.getSenderId(), message.getReceiverId(), message.getRoomId(), message.getContent(), message.isDirect(), timestamp);
                } else {
                    throw new SQLException("Creating message failed, no ID obtained.");
                }
            }
        } catch (SQLException e) {
            System.err.println("Error creating message: " + e.getMessage());
            throw e;
        }
    }

    public static Message getMessageById(int messageId) throws SQLException {
        String sql = "SELECT * FROM message WHERE id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, messageId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return extractMessageFromResultSet(rs);
            }
        } catch (SQLException e) {
            System.err.println("Error getting message by ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public static List<Message> getMessagesForRoom(int roomId, int limit, int offset) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE room_id = ? AND direct = 0 ORDER BY timestamp ASC LIMIT ? OFFSET ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.setInt(2, limit);
            pstmt.setInt(3, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting messages for room: " + e.getMessage());
            throw e;
        }
        return messages;
    }

    public static List<Message> getDirectMessages(int user1Id, int user2Id, int limit, int offset) throws SQLException {
        List<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE direct = 1 AND " +
                     "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)) " +
                     "ORDER BY timestamp ASC LIMIT ? OFFSET ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, user1Id);
            pstmt.setInt(2, user2Id);
            pstmt.setInt(3, user2Id);
            pstmt.setInt(4, user1Id);
            pstmt.setInt(5, limit);
            pstmt.setInt(6, offset);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                messages.add(extractMessageFromResultSet(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting direct messages: " + e.getMessage());
            throw e;
        }
        return messages;
    }
    
    public static boolean deleteMessage(int messageId) throws SQLException {
        String sql = "DELETE FROM message WHERE id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, messageId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            throw e;
        }
    }

    private static Message extractMessageFromResultSet(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        int senderId = rs.getInt("sender_id");
        int receiverId = rs.getInt("receiver_id");
        int roomId = rs.getInt("room_id");
        String content = rs.getString("content");
        boolean direct = rs.getBoolean("direct");
        Timestamp timestamp = rs.getTimestamp("timestamp");

        return new Message(id, senderId, receiverId, roomId, content, direct, timestamp);
    }
}
