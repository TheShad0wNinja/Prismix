package com.prismix.client.core;

public record ApplicationEvent(Type type, Object data) {
    public enum Type {
        USER_LOGGED_IN,
        ROOM_LIST_UPDATED,
        ROOM_SELECTED,
        ROOM_USERS_UPDATED,
        ROOM_MESSAGE,
        THEME_CHANGED
    }
}