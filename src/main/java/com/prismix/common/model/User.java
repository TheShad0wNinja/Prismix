package com.prismix.common.model;

import java.io.Serializable;
import java.util.Objects;

public class User implements Serializable {
    private int id;
    private String username;
    private String displayName;
    private byte[] avatar;
//    private String password;

    public User() {
        this.id = -1;
        this.username = null;
        this.displayName = null;
        this.avatar = null;
    }

    public User(String username, String displayName, byte[] avatar) {
        this(-1, username, displayName, avatar);
    }

    public User(int id, String username, String displayName, byte[] avatar) {
        this.id = id;
        this.username = username;
        this.displayName = displayName;
        this.avatar = avatar;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public byte[] getAvatar() {
        return avatar;
    }

    public void setAvatar(byte[] avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", avatar=" + (avatar != null ? "present" : "absent")  +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof User u) {
            return id == u.id;
        } else if (o instanceof Integer i) {
            return id == i;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
