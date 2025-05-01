package com.prismix.client.gui.components;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GroupChatListPanel extends ThemedPanel {

    private JPanel roomsPanel; // Panel to hold individual room entries
    private JScrollPane scrollPane;
    private ThemedButton createRoomButton;

    public GroupChatListPanel() {
        super();
        setLayout(new BorderLayout()); // Use BorderLayout
        initComponents();
        updateRoomList(new ArrayList<Room>(java.util.List.of(
                new Room("ligma", null),
                new Room("ligma", null),
                new Room("ligma", null),
                new Room("ligma", null),
                new Room("ligma", null),
                new Room("balls", null)
        )));

        createRoomButton.addActionListener(e -> {
            System.out.println("Create New Room button clicked");
            // TODO: Implement logic to show a dialog for creating a new room
        });
    }

    private void initComponents() {
        roomsPanel = new JPanel();
        roomsPanel.setLayout(new BoxLayout(roomsPanel, BoxLayout.Y_AXIS));
        roomsPanel.setOpaque(false);

        scrollPane = new JScrollPane(roomsPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        createRoomButton = new ThemedButton("Create New Room");

        add(scrollPane, BorderLayout.CENTER);
        add(createRoomButton, BorderLayout.SOUTH);

        applyTheme(ThemeManager.getCurrentTheme());
    }

    public void updateRoomList(ArrayList<Room> rooms) {
        roomsPanel.removeAll();
        for (Room room : rooms) {
            RoomEntryPanel roomEntry = new RoomEntryPanel(room);
            roomsPanel.add(roomEntry);
        }
        roomsPanel.add(Box.createVerticalGlue());

        roomsPanel.revalidate();
        roomsPanel.repaint();
    }

    @Override
    public void applyTheme(Theme theme) {
        super.applyTheme(theme);
        if (scrollPane != null) {
            scrollPane.getViewport().setBackground(theme.getBackgroundColor());
            scrollPane.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, theme.getBackgroundColor().darker()));
        }
    }

    // Getters for components if needed (e.g., to add action listeners to room entries)
    public JPanel getRoomsPanel() {
        return roomsPanel;
    }

    public ThemedButton getCreateRoomButton() {
        return createRoomButton;
    }
}