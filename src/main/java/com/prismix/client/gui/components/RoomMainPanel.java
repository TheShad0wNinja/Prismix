package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedIcon;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Room;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class RoomMainPanel extends ThemedPanel implements EventListener {
    private final Room room;
    private final JPanel usersPanel;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private static final int USERS_PANEL_WIDTH = 200;
    private static final int MAX_USERNAME_LENGTH = 15;
    private static final int AVATAR_SIZE = 30;
    
    public RoomMainPanel(Room room) {
        super(Variant.BACKGROUND, true);
        this.room = room;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel chatsPanel = new ChatPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        add(chatsPanel, c);

        usersPanel = new ThemedPanel(Variant.BACKGROUND);
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));
        usersPanel.setPreferredSize(new Dimension(USERS_PANEL_WIDTH, 0));
        usersPanel.setMinimumSize(new Dimension(USERS_PANEL_WIDTH, 0));
        usersPanel.setMaximumSize(new Dimension(USERS_PANEL_WIDTH, Integer.MAX_VALUE));

        JScrollPane scrollPane = new JScrollPane(usersPanel);
        scrollPane.setBorder(null);
        scrollPane.setPreferredSize(new Dimension(USERS_PANEL_WIDTH, 0));
        scrollPane.setMinimumSize(new Dimension(USERS_PANEL_WIDTH, 0));
        scrollPane.setMaximumSize(new Dimension(USERS_PANEL_WIDTH, Integer.MAX_VALUE));
        
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.0;
        c.fill = GridBagConstraints.VERTICAL;
        add(scrollPane, c);

        ApplicationContext.getEventBus().subscribe(this);

        SwingUtilities.invokeLater(() -> ApplicationContext.getRoomHandler().updateRoomUsers());
    }

    public void updateUserList(List<User> users) {
        if (isUpdating.getAndSet(true)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                usersPanel.removeAll();

                for (User user : users) {
                    JPanel itemPanel = new ThemedPanel(Variant.BACKGROUND);
                    itemPanel.setMaximumSize(new Dimension(USERS_PANEL_WIDTH, itemPanel.getPreferredSize().height + 10));
                    itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

                    JLabel icon = new ThemedIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.CIRCLE);
                    itemPanel.add(icon);

                    String username = user.getDisplayName();
                    if (username.length() > MAX_USERNAME_LENGTH) {
                        username = username.substring(0, MAX_USERNAME_LENGTH - 3) + "...";
                    }
                    
                    ThemedLabel usernameLabel = new ThemedLabel(username, ThemedLabel.Size.SMALLER, ThemedLabel.Variant.BACKGROUND);
                    usernameLabel.setToolTipText(user.getDisplayName());
                    itemPanel.add(usernameLabel);

                    usersPanel.add(itemPanel);
                }
                usersPanel.add(Box.createVerticalGlue());

                usersPanel.revalidate();
                usersPanel.repaint();
                revalidate();
                repaint();
            } finally {
                isUpdating.set(false);
            }
        });
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_USERS_UPDATED) {
            List<User> users = (List<User>) event.data();
            Room currentRoom = ApplicationContext.getRoomHandler().getCurrentRoom();

            if (currentRoom != null && currentRoom.equals(this.room)) {
                updateUserList(users);
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}