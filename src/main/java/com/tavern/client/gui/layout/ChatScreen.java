package com.tavern.client.gui.layout;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.components.ChatHeader;
import com.tavern.client.gui.components.RoomMainPanel;
import com.tavern.client.gui.components.ChatSidebar;
import com.tavern.client.gui.screens.DirectMainPanel;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Room;

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
                    remove(chatHeader);
                    mainPanel = new DirectMainPanel();
                    chatHeader = new ChatHeader("Direct Messages");
                    add(chatHeader, BorderLayout.NORTH);
                    add(mainPanel, BorderLayout.CENTER);
                    revalidate();
                    repaint();
                });
            }
            case ROOM_SELECTED -> {
                SwingUtilities.invokeLater(() -> {
                    remove(mainPanel);
                    remove(chatHeader);
                    Room room = (Room) event.data();
                    mainPanel = new RoomMainPanel(room);
                    chatHeader = new ChatHeader(room.getName(), room.getAvatar());
                    add(chatHeader, BorderLayout.NORTH);
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
}