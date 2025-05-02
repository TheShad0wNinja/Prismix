package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChatSidebar extends ThemedPanel {
    private JPanel roomsPanel; // Panel to hold individual room entries
    private JScrollPane scrollPane;
    private ThemedButton createRoomButton;

    public ChatSidebar(ArrayList<Room> rooms) {
        super(Variant.PRIMARY );
        initComponents();

        updateRoomList(rooms);

        createRoomButton.addActionListener(e -> {
            System.out.println("Create New Room button clicked");
            // TODO: Implement logic to show a dialog for creating a new room
        });
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // Use BorderLayout

        roomsPanel = new ThemedPanel(Variant.PRIMARY);
        roomsPanel.setLayout(new BoxLayout(roomsPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(roomsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        createRoomButton = new ThemedButton("Create new Room");

        add(scrollPane, BorderLayout.CENTER);

        applyTheme(ThemeManager.getCurrentTheme());
    }

    public void updateRoomList(ArrayList<Room> rooms) {
        roomsPanel.removeAll();
        for (Room room : rooms) {
            RoomEntryPanel roomEntry = new RoomEntryPanel(room);
            roomsPanel.add(roomEntry);
        }
        roomsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        roomsPanel.add(createRoomButton);
        roomsPanel.add(Box.createVerticalGlue());

        roomsPanel.revalidate();
        roomsPanel.repaint();
    }

    @Override
    public void applyTheme(Theme theme) {
        super.applyTheme(theme);
        if (roomsPanel != null)
            roomsPanel.setBackground(theme.getSurfaceVariantColor());

        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(theme.getSurfaceVariantColor());
            scrollPane.getViewport().setOpaque(true);
        }
    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(250, getPreferredSize().height);
    }
}