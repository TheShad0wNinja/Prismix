package com.prismix.server.data.repository;

import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.server.utils.ServerDatabaseManager;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomMemberRepository {

    private RoomMemberRepository() {}

    public static void addRoomMember(int roomId, int userId) throws SQLException {
        String sql = "INSERT INTO room_member (room_id, user_id) VALUES (?, ?)";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.setInt(2, userId);

            pstmt.executeUpdate();
            System.out.println("User " + userId + " added to room " + roomId);

        } catch (SQLException e) {
            // Handle the case where the users is already a member (Integrity constraint violation)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                System.out.println("User " + userId + " is already a member of room " + roomId);
            } else {
                System.err.println("Error adding room member: " + e.getMessage());
                throw e;
            }
        }
    }

    public static void removeRoomMember(int roomId, int userId) throws SQLException {
        String sql = "DELETE FROM room_member WHERE room_id = ? AND user_id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
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

    public static List<User> getRoomMembers(int roomId) throws SQLException {
        List<User> members = new ArrayList<>();
        String sql = "SELECT u.id, u.username, u.display_name, u.avatar FROM room_member rm JOIN main.user u on u.id = rm.user_id WHERE room_id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            ResultSet rs = pstmt.executeQuery();

            System.out.println("Room " + roomId + " members found");
            while (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String displayName = rs.getString("display_name");
                byte[] avatar = rs.getBytes("avatar");
                System.out.println("User " + userId + " member: " + username + " displayName: " + displayName + " avatar: " + avatar);
                members.add(new User(userId, username, displayName, avatar));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching room members: " + e.getMessage());
        }
        return members;
    }

    public static List<Room> getUserRooms(int userId) throws SQLException {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT r.id, r.name, r.avatar FROM room_member rm JOIN room r ON rm.room_id = r.id WHERE rm.user_id = ?";

        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int roomId = rs.getInt("id");
                String name = rs.getString("name");
                byte[] avatar = rs.getBytes("avatar");

                Room room = new Room(roomId, name, avatar);
                rooms.add(room);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching users's rooms: " + e.getMessage());
        }
        return rooms;
    }

    public static boolean isUserInRoom(int userId, int roomId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM room_member WHERE user_id = ? AND room_id = ?";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, roomId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking if users is in room: " + e.getMessage());
            throw e;
        }
        return false;
    }
}