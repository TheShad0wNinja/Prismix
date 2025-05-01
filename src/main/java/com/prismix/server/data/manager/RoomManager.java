package com.prismix.server.data.manager;

import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.server.data.repository.RoomMemberRepository;
import com.prismix.server.data.repository.RoomRepository;
import java.sql.*;
import java.util.List;

public class RoomManager {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;

    public RoomManager() {
        this.roomRepository = new RoomRepository();
        this.roomMemberRepository = new RoomMemberRepository();
    }

    public Room createRoom(String roomName, byte[] avatarData) {
        try {
            // Check if room name already exists
            if (roomRepository.getRoomByName(roomName) != null) {
                System.out.println("Room creation failed: Room name already exists.");
                return null;
            }

            Room newRoom = new Room(roomName, avatarData);
            return roomRepository.createRoom(newRoom);

        } catch (SQLException e) {
            System.err.println("Error creating room: " + e.getMessage());
            return null;
        }
    }

    public boolean joinRoom(int userId, int roomId) {
        try {
            if (roomRepository.getRoomById(roomId) == null) {
                System.out.println("Joining room failed: Room with ID " + roomId + " does not exist.");
                return false;
            }

            roomMemberRepository.addRoomMember(roomId, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error joining room: " + e.getMessage());
            return false;
        }
    }

    public boolean leaveRoom(int userId, int roomId) {
        try {
            roomMemberRepository.removeRoomMember(roomId, userId);
            return true;
        } catch (SQLException e) {
            System.err.println("Error leaving room: " + e.getMessage());
            return false;
        }
    }

    public List<User> getMembersOfRoom(int roomId) {
        try {
            return roomMemberRepository.getRoomMembers(roomId);
        } catch (SQLException e) {
            System.err.println("Error getting room members: " + e.getMessage());
            return null;
        }
    }

    public List<Room> getRoomsForUser(int userId) {
        try {
            return roomMemberRepository.getUserRooms(userId);
        } catch (SQLException e) {
            System.err.println("Error getting user's rooms: " + e.getMessage());
            return null;
        }
    }
}