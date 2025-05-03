package com.prismix.client.gui.layout;

import com.prismix.client.gui.components.ChatHeader;
import com.prismix.client.gui.components.RoomMainPanel;
import com.prismix.client.gui.components.ChatSidebar;
import com.prismix.common.model.Room;
import com.prismix.common.model.User;

import java.util.ArrayList;

public class ChatLayout extends BaseLayout {
    private RoomMainPanel mainPanel;

    public ChatLayout(Room room, ArrayList<Room> rooms) {
        super();
        setHeader(new ChatHeader(room));
        setSidebar(new ChatSidebar(rooms));
        mainPanel = new RoomMainPanel(room);
        setContent(mainPanel);
    }
    
    public void setRoom(Room room) {
        setHeader(new ChatHeader(room));
//        mainPanel = new RoomMainPanel(room);
        mainPanel.setRoom(room);
//        setContent(mainPanel);
    }

    public void setRooms(ArrayList<Room> rooms) {
        setSidebar(new ChatSidebar(rooms));
    }

    public void setUsers(ArrayList<User> users) {
        mainPanel.updateUserList(users);
    }
} 