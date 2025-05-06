package com.prismix.server.data.repository;

import com.prismix.common.model.Message;
import com.prismix.server.utils.ServerDatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserUnreadMessageRepository {

    private UserUnreadMessageRepository() {}
    
    public static boolean markMessageAsUnread(int userId, int messageId) throws SQLException {
        String sql = "INSERT INTO user_unread_message (user_id, message_id) VALUES (?, ?)";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, messageId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            // Handle the case where this combination already exists
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                System.out.println("Message " + messageId + " is already marked as unread for user " + userId);
                return false;
            } else {
                System.err.println("Error marking message as unread: " + e.getMessage());
                throw e;
            }
        }
    }
    
    public static boolean markMessageAsRead(int userId, int messageId) throws SQLException {
        String sql = "DELETE FROM user_unread_message WHERE user_id = ? AND message_id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, messageId);
            
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            throw e;
        }
    }
    
    public static boolean markAllMessagesAsRead(int userId, int roomId) throws SQLException {
        String sql = "DELETE FROM user_unread_message WHERE user_id = ? AND message_id IN " +
                     "(SELECT id FROM message WHERE room_id = ?)";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, roomId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error marking all messages as read: " + e.getMessage());
            throw e;
        }
    }
    
    public static boolean markAllDirectMessagesAsRead(int userId, int otherUserId) throws SQLException {
        String sql = "DELETE FROM user_unread_message WHERE user_id = ? AND message_id IN " +
                     "(SELECT id FROM message WHERE direct = 1 AND " +
                     "((sender_id = ? AND receiver_id = ?) OR (sender_id = ? AND receiver_id = ?)))";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, otherUserId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, otherUserId);
            
            pstmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error marking all direct messages as read: " + e.getMessage());
            throw e;
        }
    }
    
    public static List<Message> getUnreadMessages(int userId) throws SQLException {
        List<Message> unreadMessages = new ArrayList<>();
        String sql = """
                     SELECT m.*
                     FROM user_unread_message um JOIN message m 
                     ON um.message_id = m.id
                     WHERE um.user_id = ?
                     ORDER BY m.timestamp ASC;
                     """;
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("id");
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                int roomId = rs.getInt("room_id");
                String content = rs.getString("content");
                boolean direct = rs.getBoolean("direct");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                unreadMessages.add(new Message(id, senderId, receiverId, roomId, content, direct, timestamp));
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread messages: " + e.getMessage());
            throw e;
        }
        return unreadMessages;
    }
    
    public static int getUnreadMessageCount(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_unread_message WHERE user_id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread message count: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    public static int getUnreadMessageCountForRoom(int userId, int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_unread_message um " +
                     "JOIN message m ON um.message_id = m.id " +
                     "WHERE um.user_id = ? AND m.room_id = ? AND m.direct = 0";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread message count for room: " + e.getMessage());
            throw e;
        }
        return 0;
    }
    
    public static int getUnreadDirectMessageCount(int userId, int otherUserId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM user_unread_message um " +
                     "JOIN message m ON um.message_id = m.id " +
                     "WHERE um.user_id = ? AND m.direct = 1 AND " +
                     "((m.sender_id = ? AND m.receiver_id = ?) OR (m.sender_id = ? AND m.receiver_id = ?))";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, otherUserId);
            pstmt.setInt(3, userId);
            pstmt.setInt(4, userId);
            pstmt.setInt(5, otherUserId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Error getting unread direct message count: " + e.getMessage());
            throw e;
        }
        return 0;
    }

    public static void cleanupMessages() {
        String sql = """
            DELETE FROM message
            WHERE id NOT IN (SELECT DISTINCT message_id FROM user_unread_message);
            """;

        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Unable to cleanup messages: " + e.getMessage());
        }
    }
}
