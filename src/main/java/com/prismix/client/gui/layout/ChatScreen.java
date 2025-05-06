package com.prismix.client.gui.layout;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.gui.components.ChatHeader;
import com.prismix.client.gui.components.RoomMainPanel;
import com.prismix.client.gui.components.ChatSidebar;
import com.prismix.client.gui.screens.DirectMainPanel;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;

public class ChatScreen extends JPanel implements EventListener {
//    private RoomMainPanel mainPanel;
    private ChatSidebar chatSidebar;
    private ChatHeader chatHeader;
    private JPanel mainPanel;

    public ChatScreen() {
        super();
        setLayout(new BorderLayout());

        chatHeader = new ChatHeader("General");
        chatSidebar = new ChatSidebar();
        mainPanel = new RoomMainPanel();

        add(chatHeader, BorderLayout.NORTH);
        add(chatSidebar, BorderLayout.WEST);
        add(mainPanel, BorderLayout.CENTER);

        ApplicationContext.getEventBus().subscribe(this);
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case DIRECT_SCREEN_SELECTED -> {
                SwingUtilities.invokeLater(() -> {
                    remove(mainPanel);
                    mainPanel = new DirectMainPanel();
                    chatHeader = new ChatHeader("Direct Messages");
                    add(mainPanel, BorderLayout.CENTER);
                    revalidate();
                    repaint();
                });
            }
            case ROOM_SELECTED -> {
                SwingUtilities.invokeLater(() -> {
                    remove(mainPanel);
                    mainPanel = new RoomMainPanel((Room) event.data());
                    add(mainPanel, BorderLayout.CENTER);
                    revalidate();
                    repaint();
                });
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }

    //    public void setRooms(ArrayList<Room> rooms) {
//        setSidebar(new ChatSidebar(rooms));
//    }
//
//    public void setUsers(ArrayList<User> users) {
//        mainPanel.updateUserList(users);
//    }
} 