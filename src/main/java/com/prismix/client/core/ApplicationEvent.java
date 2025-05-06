package com.prismix.client.core;

public record ApplicationEvent(Type type, Object data) {
    public ApplicationEvent(Type type) {
        this(type, null);
    }
    public enum Type {
        SWITCH_SCREEN,
        DIRECT_SCREEN_SELECTED,
        USER_LOGGED_IN,
        DIRECT_USER_LIST_UPDATED,
        DIRECT_USER_SELECTED,
        ROOM_LIST_UPDATED,
        ROOM_SELECTED,
        ROOM_USERS_UPDATED,
        START_CALL,
        END_CALL,
        MESSAGE,
        MESSAGES,
        THEME_CHANGED
    }
}