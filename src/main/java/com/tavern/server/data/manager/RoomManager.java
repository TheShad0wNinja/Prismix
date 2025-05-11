package com.tavern.server.data.manager;

import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import com.tavern.server.data.repository.RoomMemberRepository;
import com.tavern.server.data.repository.RoomRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.List;

public class RoomManager {
    private static final Logger logger = LoggerFactory.getLogger(RoomManager.class);

    private RoomManager() {}

    public static Room createRoom(String roomName, byte[] avatarData) {
        try {
            // Check if room name already exists
            if (RoomRepository.getRoomByName(roomName) != null) {
                logger.info("Room creation failed: Room name '{}' already exists", roomName);
                return null;
            }

            Room newRoom = new Room(roomName, avatarData);
            return RoomRepository.createRoom(newRoom);

        } catch (SQLException e) {
            logger.error("Error creating room '{}': {}", roomName, e.getMessage(), e);
            return null;
        }
    }

    public static boolean joinRoom(int userId, int roomId) {
        try {
            if (RoomRepository.getRoomById(roomId) == null) {
                logger.info("Joining room failed: Room with ID {} does not exist", roomId);
                return false;
            }

            RoomMemberRepository.addRoomMember(roomId, userId);
            return true;
        } catch (SQLException e) {
            logger.error("Error joining room {}: {}", roomId, e.getMessage(), e);
            return false;
        }
    }

    public static boolean leaveRoom(int userId, int roomId) {
        try {
            RoomMemberRepository.removeRoomMember(roomId, userId);
            return true;
        } catch (SQLException e) {
            logger.error("Error leaving room {}: {}", roomId, e.getMessage(), e);
            return false;
        }
    }

    public static List<User> getMembersOfRoom(int roomId) {
        try {
            return RoomMemberRepository.getRoomMembers(roomId);
        } catch (SQLException e) {
            logger.error("Error getting members of room {}: {}", roomId, e.getMessage(), e);
            return null;
        }
    }

    public static List<Room> getRoomsForUser(int userId) {
        try {
            return RoomMemberRepository.getUserRooms(userId);
        } catch (SQLException e) {
            logger.error("Error getting rooms for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }
}