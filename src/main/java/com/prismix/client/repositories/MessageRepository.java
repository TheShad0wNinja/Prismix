package com.prismix.client.repositories;

import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.utils.ClientDatabaseManager;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;

import java.sql.*;
import java.util.ArrayList;

public class MessageRepository {
    public static void addMessage(Message message) {
        String sql;
        if (message.isDirect()) {
            sql = "INSERT INTO message (sender_id, receiver_id, content, direct, owner_id) VALUES (?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO message (sender_id, room_id, content, direct, owner_id) VALUES (?, ?, ?, ?, ?)";
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
            stmt.setInt(5, ApplicationContext.getAuthHandler().getUser().getId());

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
            stmt.setInt(2, ApplicationContext.getAuthHandler().getUser().getId());

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
}
