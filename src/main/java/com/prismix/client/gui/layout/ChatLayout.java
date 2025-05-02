package com.prismix.client.gui.layout;

import com.prismix.client.gui.components.ChatHeader;
import com.prismix.client.gui.components.ChatPanel;
import com.prismix.client.gui.components.ChatSidebar;
import com.prismix.common.model.Room;

import java.util.ArrayList;

public class ChatLayout extends BaseLayout {
    public ChatLayout(Room room, ArrayList<Room> rooms) {
        super();
        setHeader(new ChatHeader(room));
        setSidebar(new ChatSidebar(rooms));
        setContent(new ChatPanel(room));
    }
    
    public void setRoom(Room room) {
        setHeader(new ChatHeader(room));
        setContent(new ChatPanel(room));
    }

    public void setRooms(ArrayList<Room> rooms) {
        setSidebar(new ChatSidebar(rooms));
    }
} 