package com.prismix.server.data.repository;

import com.prismix.common.model.Room;
import com.prismix.server.utils.DatabaseManager;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class RoomRepository {

    public Room createRoom(Room room) throws SQLException {
        String sql = "INSERT INTO room (name, avatar) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
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
                System.out.println("Room created successfully with ID: " + room.getId());
                return room;
            }
        } catch (SQLException e) {
            System.err.println("Error creating room: " + e.getMessage());
            throw e;
        }

        return null;
    }

    public Room getRoomById(int roomId) throws SQLException {
        String sql = "SELECT * FROM room WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString("name");
                byte[] avatar = rs.getBytes("avatar");
                return new Room(id, name, avatar);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room by ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public Room getRoomByName(String roomName) throws SQLException {
        String sql = "SELECT id, name, avatar FROM room WHERE name = ?";
        try (Connection conn = DatabaseManager.getConnection();
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
            System.err.println("Error fetching room by name: " + e.getMessage());
            throw e;
        }
        return null;
    }


    // Method to get all rooms
    public List<Room> getAllRooms() throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT id, name, avatar FROM room";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt(1);
                String name = rs.getString("name");
                byte[] avatar = rs.getBytes("avatar");
                rooms.add(new Room(id, name, avatar));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all rooms: " + e.getMessage());
            throw e;
        }
        return rooms;
    }

    public void updateRoomName(int roomId, String newName) throws SQLException {
        String sql = "UPDATE room SET name = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, newName);
            pstmt.setInt(2, roomId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating room name: " + e.getMessage());
            throw e;
        }
    }

    public void updateRoomAvatar(int roomId, byte[] avatarData) throws SQLException {
        String sql = "UPDATE room SET avatar = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setBytes(1, avatarData);
            pstmt.setInt(2, roomId);

            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error updating room avatar: " + e.getMessage());
            throw e;
        }
    }

    public void deleteRoom(int roomId) throws SQLException {
        String sql = "DELETE FROM room WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
            throw e;
        }
    }
}