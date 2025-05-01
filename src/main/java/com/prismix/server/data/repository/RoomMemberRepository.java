package com.prismix.server.data.repository;

import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.server.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomMemberRepository {

    private final UserRepository userRepository;
    private final RoomRepository roomRepository;

    public RoomMemberRepository() {
        this.userRepository = new UserRepository();
        this.roomRepository = new RoomRepository(); // Initialize RoomRepository
    }

    public void addRoomMember(int roomId, int userId) throws SQLException {
        String sql = "INSERT INTO room_member (room_id, user_id) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.setInt(2, userId);

            pstmt.executeUpdate();
            System.out.println("User " + userId + " added to room " + roomId);

        } catch (SQLException e) {
            // Handle the case where the user is already a member (Integrity constraint violation)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                System.out.println("User " + userId + " is already a member of room " + roomId);
            } else {
                System.err.println("Error adding room member: " + e.getMessage());
                throw e;
            }
        }
    }

    public void removeRoomMember(int roomId, int userId) throws SQLException {
        String sql = "DELETE FROM room_member WHERE room_id = ? AND user_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.setInt(2, userId);

            pstmt.executeUpdate();
            System.out.println("User " + userId + " removed from room " + roomId);

        } catch (SQLException e) {
            System.err.println("Error removing room member: " + e.getMessage());
            throw e;
        }
    }

    public List<User> getRoomMembers(int roomId) throws SQLException {
        List<User> members = new ArrayList<>();
        String sql = "SELECT user_id FROM room_member WHERE room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("user_id");

                User user = userRepository.getUserById(userId);
                if (user != null) {
                    members.add(user);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room members: " + e.getMessage());
            throw e;
        }
        return members;
    }

    public List<Room> getUserRooms(int userId) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_id FROM room_member WHERE user_id = ?";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int roomId = rs.getInt("room_id");
                Room room = roomRepository.getRoomById(roomId);
                if (room != null) {
                    rooms.add(room);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user's rooms: " + e.getMessage());
            throw e;
        }
        return rooms;
    }

    public boolean isUserInRoom(int userId, int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM room_member WHERE user_id = ? AND room_id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user is in room: " + e.getMessage());
            throw e;
        }
        return false;
    }
}