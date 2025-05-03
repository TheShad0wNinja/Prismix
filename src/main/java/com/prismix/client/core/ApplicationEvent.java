package com.prismix.client.core;

public record ApplicationEvent(Type type, Object data) {
    public enum Type {
        ROOM_SELECTED,
        ROOM_LIST_UPDATED,
        USER_LOGGED_IN,
        ROOM_MESSAGE,
        ROOM_USERS_UPDATED,
        THEME_CHANGED
    }
}