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
    private ThemedButton joinRoomButton;
    private ThemedButton privateMessageBtn;

    public ChatSidebar() {
        super(Variant.SURFACE);

        initComponents();

        privateMessageBtn.addActionListener(e -> ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.DIRECT_SCREEN_SELECTED)));

        createRoomButton.addActionListener(e -> showCreateRoomDialog());
        
        joinRoomButton.addActionListener(e -> showJoinRoomDialog());

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
        createRoomButton = new ThemedButton("Create Room", ThemedButton.Variant.SECONDARY);
        joinRoomButton = new ThemedButton("Join Room", ThemedButton.Variant.SECONDARY);

        add(scrollPane, BorderLayout.CENTER);

        applyTheme(ThemeManager.getCurrentTheme());
    }
    
    private void showCreateRoomDialog() {
        Window window = SwingUtilities.getWindowAncestor(this);
        Frame parent = window instanceof Frame ? (Frame) window : null;
        
        CreateRoomDialog dialog = new CreateRoomDialog(parent);
        dialog.setVisible(true);
        
        if (dialog.isConfirmed()) {
            String roomName = dialog.getRoomName();
            byte[] avatarData = dialog.getAvatarData();
            
            // Call the RoomHandler to create the room
            ApplicationContext.getRoomHandler().createRoom(roomName, avatarData);
        }
    }
    
    private void showJoinRoomDialog() {
        RoomSearchDialog dialog = new RoomSearchDialog();
        dialog.setVisible(true);
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
        roomsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        roomsPanel.add(joinRoomButton);
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