package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class ChatSidebar extends ThemedPanel implements EventListener {
    private JPanel roomsPanel; // Panel to hold individual room entries
    private JScrollPane scrollPane;
    private ThemedButton createRoomButton;
    private ThemedButton privateMessageBtn;

    public ChatSidebar() {
        super(Variant.SURFACE);

        initComponents();

        privateMessageBtn.addActionListener(e -> ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.DIRECT_SCREEN_SELECTED)));

        createRoomButton.addActionListener(e -> System.out.println("CReATE"));

        ApplicationContext.getEventBus().subscribe(this);
        ApplicationContext.getRoomHandler().updateRooms();
    }

    private void initComponents() {
        setLayout(new BorderLayout()); // Use BorderLayout

        roomsPanel = new ThemedPanel(Variant.PRIMARY);
        roomsPanel.setLayout(new BoxLayout(roomsPanel, BoxLayout.Y_AXIS));

        scrollPane = new JScrollPane(roomsPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        privateMessageBtn = new ThemedButton("Direct Messages", ThemedButton.Variant.SECONDARY);
        createRoomButton = new ThemedButton("Create new Room", ThemedButton.Variant.SECONDARY);

        add(scrollPane, BorderLayout.CENTER);

        applyTheme(ThemeManager.getCurrentTheme());
    }

    public void updateRoomList(ArrayList<Room> rooms) {
        roomsPanel.removeAll();
        roomsPanel.add(privateMessageBtn);
        roomsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        for (Room room : rooms) {
            RoomEntryPanel roomEntry = new RoomEntryPanel(room);
            roomsPanel.add(roomEntry);
        }
        roomsPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        roomsPanel.add(createRoomButton);
        roomsPanel.add(Box.createVerticalGlue());

        roomsPanel.revalidate();
        roomsPanel.repaint();
        revalidate();
        repaint();
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

//    @Override
//    public Dimension getPreferredSize() {
//        return new Dimension(250, super.getPreferredSize().height);
//    }

    @Override
    public Dimension getMaximumSize() {
        return new Dimension(250, getPreferredSize().height);
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_LIST_UPDATED) {
            SwingUtilities.invokeLater(() -> {
                updateRoomList((ArrayList<Room>) event.data());
            });
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}