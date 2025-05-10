package com.tavern.server.data.model;

public class RoomMember {
    private int roomId;
    private int userId;

    public RoomMember(int roomId, int userId) {
        this.roomId = roomId;
        this.userId = userId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
