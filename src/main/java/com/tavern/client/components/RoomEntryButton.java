package com.tavern.client.components;

public class RoomEntryButton extends RoomEntry {
    private final Runnable action;

    public RoomEntryButton(String name, byte[] icon, Runnable action) {
        super(name, icon);
        this.action = action;
    }

    public void performAction() {
        if (action != null) {
            action.run();
        }
    }

}
