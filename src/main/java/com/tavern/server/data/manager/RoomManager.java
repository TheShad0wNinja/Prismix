package com.tavern.server.data.manager;

import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import com.tavern.server.data.repository.RoomMemberRepository;
import com.tavern.server.data.repository.RoomRepository;
import java.sql.*;
import java.util.List;

public class RoomManager {

    private RoomManager() {}

    public static Room createRoom(String roomName, byte[] avatarData) {
        try {
            // Check if room name already exists
            if (RoomRepository.getRoomByName(roomName) != null) {
                System.out.println("Room creation failed: Room name already exists.");
                return null;
            }

            Room newRoom = new Room(roomName, avatarData);
            return RoomRepository.createRoom(newRoom);

        } catch (SQLException e) {
            System.err.println("Error creating room: " + e.getMessage());
            return null;
        }
    }

    public static boolean joinRoom(int userId, int roomId) {
        try {
            if (RoomRepository.getRoomById(roomId) == null) {
                System.out.println("Joining room failed: Room with ID " + roomId + " does not exist.");
                return false;
            }

            RoomMemberRepository.addRoomMember(roomId, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error joining room: " + e.getMessage());
            return false;
        }
    }

    public static boolean leaveRoom(int userId, int roomId) {
        try {
            RoomMemberRepository.removeRoomMember(roomId, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error leaving room: " + e.getMessage());
            return false;
        }
    }

    public static List<User> getMembersOfRoom(int roomId) {
        try {
            return RoomMemberRepository.getRoomMembers(roomId);
        } catch (SQLException e) {
            System.err.println("Error getting room members: " + e.getMessage());
            return null;
        }
    }

    public static List<Room> getRoomsForUser(int userId) {
        try {
            return RoomMemberRepository.getUserRooms(userId);
        } catch (SQLException e) {
            System.err.println("Error getting users's rooms: " + e.getMessage());
            return null;
        }
    }
}