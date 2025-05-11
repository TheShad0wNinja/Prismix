package com.tavern.server.data.repository;

import com.tavern.common.model.Room;
import com.tavern.server.utils.ServerDatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository {
    private static final Logger logger = LoggerFactory.getLogger(RoomRepository.class);

    public static Room createRoom(Room room) throws SQLException {
        String sql = "INSERT INTO room (name, avatar) VALUES (?, ?)";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) { // No RETURN_GENERATED_KEYS needed with String ID

            stmt.setString(1, room.getName());
            stmt.setBytes(2, room.getAvatar());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating room failed, no rows affected.");
            }

            ResultSet rs = stmt.getGeneratedKeys();
            if (rs.next()) {
                int id = rs.getInt(1);
                room.setId(id);
                logger.info("Room created successfully with ID: {}", room.getId());
                return room;
            }
        } catch (SQLException e) {
            logger.error("Error creating room", e);
            throw e;
        }

        return null;
    }

    public static Room getRoomById(int roomId) throws SQLException {
        logger.debug("Fetching room with ID: {}", roomId);
        String sql = "SELECT * FROM room WHERE id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String name = rs.getString("name");
                byte[] avatar = rs.getBytes("avatar");
                return new Room(id, name, avatar);
            }
        } catch (SQLException e) {
            logger.error("Error fetching room by ID: {}", roomId, e);
        }
        return null;
    }

    public static Room getRoomByName(String roomName) throws SQLException {
        String sql = "SELECT id, name, avatar FROM room WHERE name = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, roomName);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString("name");
                byte[] avatar = rs.getBytes("avatar");
                return new Room(id, name, avatar);
            }
        } catch (SQLException e) {
            logger.error("Error fetching room by name: {}", roomName, e);
            throw e;
        }
        return null;
    }


    // Method to get all rooms
    public static List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, name, avatar FROM room";
        try (Connection conn = ServerDatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString("name");
                byte[] avatar = rs.getBytes("avatar");
                rooms.add(new Room(id, name, avatar));
            }
        } catch (SQLException e) {
            logger.error("Error fetching all rooms", e);
            throw e;
        }
        return rooms;
    }

    public void updateRoomName(int roomId, String newName) throws SQLException {
        String sql = "UPDATE room SET name = ? WHERE id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setInt(2, roomId);

            pstmt.executeUpdate();
            logger.info("Updated room name to '{}' for room ID: {}", newName, roomId);

        } catch (SQLException e) {
            logger.error("Error updating room name for room ID: {}", roomId, e);
            throw e;
        }
    }

    public void updateRoomAvatar(int roomId, byte[] avatarData) throws SQLException {
        String sql = "UPDATE room SET avatar = ? WHERE id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBytes(1, avatarData);
            pstmt.setInt(2, roomId);

            pstmt.executeUpdate();
            logger.info("Updated avatar for room ID: {}", roomId);

        } catch (SQLException e) {
            logger.error("Error updating room avatar for room ID: {}", roomId, e);
            throw e;
        }
    }

    public void deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM room WHERE id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.executeUpdate();
            logger.info("Deleted room with ID: {}", roomId);

        } catch (SQLException e) {
            logger.error("Error deleting room with ID: {}", roomId, e);
            throw e;
        }
    }
}