package com.tavern.common.model;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

public final class Message implements Serializable {
    private int id;
    private int senderId;
    private int receiverId;
    private int roomId;
    private String content;
    private boolean direct;
    private Timestamp timestamp;

    public Message(int id, int sender_id, int receiver_id, int room_id, String content, boolean direct) {
        this.id = id;
        this.senderId = sender_id;
        this.receiverId = receiver_id;
        this.roomId = room_id;
        this.content = content;
        this.direct = direct;
        this.timestamp = null;
    }

    public Message(int id, int sender_id, int receiver_id, int room_id, String content, boolean direct,
                   Timestamp timestamp) {
        this.id = id;
        this.senderId = sender_id;
        this.receiverId = receiver_id;
        this.roomId = room_id;
        this.content = content;
        this.direct = direct;
        this.timestamp = timestamp;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public int getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(int receiverId) {
        this.receiverId = receiverId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public boolean isDirect() {
        return direct;
    }

    public void setDirect(boolean direct) {
        this.direct = direct;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Message) obj;
        return this.id == that.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Message[" +
                "id=" + id + ", " +
                "sender_id=" + senderId + ", " +
                "receiver_id=" + receiverId + ", " +
                "room_id=" + roomId + ", " +
                "content=" + content + ", " +
                "direct=" + direct + ", " +
                "timestamp=" + timestamp + ']';
    }

}
