package com.tavern.common.model;

import java.io.Serializable;
import java.util.Objects;

public class Room implements Serializable {
    private int id;
    private String name;
    private byte[] avatar;

    public Room() {
        this.id = -1;
        this.name = null;
        this.avatar = null;
    }

    public Room(String name, byte[] avatar) {
        this(-1, name, avatar);
    }

    public Room(int id, String name, byte[] avatar) {
        this.id = id;
        this.name = name;
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "Room{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", avatar=" + (avatar != null ? "present" : "absent")  +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
