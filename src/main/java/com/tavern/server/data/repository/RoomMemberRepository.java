package com.tavern.server.data.repository;

import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import com.tavern.server.utils.ServerDatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RoomMemberRepository {
    private static final Logger logger = LoggerFactory.getLogger(RoomMemberRepository.class);

    private RoomMemberRepository() {}

    public static void addRoomMember(int roomId, int userId) throws SQLException {
        String sql = "INSERT INTO room_member (room_id, user_id) VALUES (?, ?)";
        try (Connection conn = ServerDatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, roomId);
            pstmt.setInt(2, userId);

            pstmt.executeUpdate();
            logger.info("User {} added to room {}", userId, roomId);

        } catch (SQLException e) {
            // Handle the case where the users is already a member (Integrity constraint violation)
            if (e.getSQLState() != null && e.getSQLState().startsWith("23")) {
                logger.info("User {} is already a member of room {}", userId, roomId);
            } else {
                logger.error("Error adding room member: {}", e.getMessage(), e);
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
            logger.info("User {} removed from room {}", userId, roomId);

        } catch (SQLException e) {
            logger.error("Error removing room member: {}", e.getMessage(), e);
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

            logger.debug("Retrieving members for room {}", roomId);
            while (rs.next()) {
                int userId = rs.getInt("id");
                String username = rs.getString("username");
                String displayName = rs.getString("display_name");
                byte[] avatar = rs.getBytes("avatar");
                logger.debug("Found member: userId={}, username={}, displayName={}", 
                        userId, username, displayName);
                members.add(new User(userId, username, displayName, avatar));
            }
        } catch (SQLException e) {
            logger.error("Error fetching room members: {}", e.getMessage(), e);
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
            logger.error("Error fetching user's rooms: {}", e.getMessage(), e);
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
            logger.error("Error checking if user is in room: {}", e.getMessage(), e);
            throw e;
        }
        return false;
    }
}