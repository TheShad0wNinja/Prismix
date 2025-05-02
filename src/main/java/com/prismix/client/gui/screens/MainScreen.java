package com.prismix.client.gui.screens;

import com.prismix.client.core.AuthManager;
import com.prismix.client.gui.components.GroupChatListPanel;
import com.prismix.client.gui.components.ThemedLabel;
import com.prismix.client.gui.components.ThemedPanel;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;

public class MainScreen extends ThemedPanel {
    private JPanel mainPanel;
    private static MainScreen instance;
    private MainScreen() {
        super();
        initComponents();
    }

    public static MainScreen getInstance() {
        if (instance == null) {
            instance = new MainScreen();
        }
        return instance;
    }

    public static void switchRoom(Room room) {
        System.out.println("Switching room: " + room.getName());

        instance.mainPanel.removeAll();

        JLabel roomLabel = new JLabel("Room: " + room.getName());
        instance.mainPanel.add(roomLabel, BorderLayout.NORTH);

        instance.mainPanel.revalidate();
        instance.mainPanel.repaint();
    }

    private void initComponents() {
        setLayout(new BorderLayout());

        ThemedLabel label = new ThemedLabel("Welcome to Prismix", true);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label, BorderLayout.NORTH);

        GroupChatListPanel groupChatListPanel = new GroupChatListPanel();
        add(groupChatListPanel, BorderLayout.WEST);

        mainPanel = new ThemedPanel();
        add(mainPanel, BorderLayout.CENTER);
    }
}
