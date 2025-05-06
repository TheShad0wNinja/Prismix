package com.prismix.client.repositories;

import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.utils.ClientDatabaseManager;
import com.prismix.common.model.Message;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class MessageRepository {
    public static void createMessage(Message message) {
        String sql;
        if (message.isDirect()) {
            sql = "INSERT INTO message (sender_id, receiver_id, content, direct, owner_id, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO message (sender_id, room_id, content, direct, owner_id, timestamp) VALUES (?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = ClientDatabaseManager.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, message.getSenderId());
            if (message.isDirect()) {
                stmt.setInt(2, message.getReceiverId());
            } else {
                stmt.setInt(2, message.getRoomId());
            }
            stmt.setString(3, message.getContent());
            stmt.setBoolean(4, message.isDirect());
            stmt.setInt(5, ApplicationContext.getUserHandler().getUser().getId());

            if (message.getTimestamp() == null)
                stmt.setTimestamp(6, Timestamp.valueOf(LocalDateTime.now()));
            else
                stmt.setTimestamp(6, message.getTimestamp());

            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                message.setId(rs.getInt(1));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static ArrayList<Message> getMessagesByRoomId(int roomId) {
        ArrayList<Message> messages = new ArrayList<>();
        String sql = "SELECT * FROM message WHERE room_id = ? AND owner_id = ? ORDER BY timestamp ASC";
        try(Connection conn = ClientDatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            stmt.setInt(2, ApplicationContext.getUserHandler().getUser().getId());

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                String content = rs.getString("content");
                boolean direct = rs.getBoolean("direct");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                messages.add(new Message(id, senderId, receiverId, roomId, content, direct, timestamp));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return messages;
    }

    public static List<Message> getDirectMessageWithUser(int userId) {
        ArrayList<Message> messages = new ArrayList<>();
        String sql  = """
            SELECT *
            FROM message
            WHERE direct = 1
            AND owner_id = ?
            AND (sender_id = ? OR receiver_id = ?)
            ORDER BY timestamp ASC
            """;
        try(Connection conn = ClientDatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ApplicationContext.getUserHandler().getUser().getId());
            stmt.setInt(2, userId);
            stmt.setInt(3, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("id");
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                String content = rs.getString("content");
                boolean direct = rs.getBoolean("direct");
                Timestamp timestamp = rs.getTimestamp("timestamp");

                messages.add(new Message(id, senderId, receiverId, userId, content, direct, timestamp));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return messages;
    }


    public static Set<Integer> getUserDirectContacts(int userId) {
        System.out.println(userId);
        Set<Integer> contacts = new LinkedHashSet<>();
        String sql  = """
            SELECT sender_id, receiver_id
            FROM message
            WHERE direct = 1
            AND (sender_id = ? OR receiver_id = ?)
            ORDER BY timestamp DESC
            """;

        try(Connection conn = ClientDatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userId);
            stmt.setInt(2, userId);

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int senderId = rs.getInt("sender_id");
                int receiverId = rs.getInt("receiver_id");
                if (senderId == userId) {
                    contacts.add(receiverId);
                } else {
                    contacts.add(senderId);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return contacts;
    }
}
