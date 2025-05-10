package com.tavern.client.gui.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.themes.ThemeManager;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.gui.components.themed.ThemedIcon;
import com.tavern.client.gui.components.themed.ThemedLabel;
import com.tavern.client.gui.components.themed.ThemedPanel;
import com.tavern.client.gui.components.themed.ThemedTextField;
import com.tavern.common.model.Message;
import com.tavern.common.model.Room;
import com.tavern.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class RoomMainPanel extends ThemedPanel implements EventListener {
    private final Room room;
    private JPanel usersPanel;
    private ChatPanel chatPanel;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private static final int USERS_PANEL_WIDTH = 200;
    private static final int MAX_USERNAME_LENGTH = 15;
    private static final int AVATAR_SIZE = 30;
    private static final AtomicLong messageSerial = new AtomicLong(0);

    public RoomMainPanel() {
        this(null);
    }

    public RoomMainPanel(Room room) {
        super(Variant.BACKGROUND, true);
        this.room = room;
        if (room == null) {
            setLayout(new BorderLayout());
            JLabel titleLabel = new ThemedLabel("Tavern", ThemedLabel.Size.LARGER, ThemedLabel.Variant.BACKGROUND);
            titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
            add(titleLabel, BorderLayout.CENTER);
            return;
        }

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        chatPanel = new ChatPanel(false);
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1.0;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        add(chatPanel, c);

        usersPanel = new ThemedPanel(Variant.BACKGROUND, Border.LEFT);
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

    private void sendMessage(ThemedTextField messageInput) {
        String content = messageInput.getText().trim();
        if (content.isEmpty()) {
            return;
        }

        // Create message with unique serial number
        long messageId = messageSerial.incrementAndGet();
        Message message = new Message(
                (int) messageId,
                ApplicationContext.getUserHandler().getUser().getId(),
                -1,
                room.getId(),
                content,
                false,
                Timestamp.valueOf(LocalDateTime.now())
        );

        try {
            // Send the message using the client's message handler
            ApplicationContext.getMessageHandler().sendTextMessage(message);

            // Clear input
            messageInput.setText("");
        } catch (Exception e) {
            System.err.println("Error sending message: " + e.getMessage());
        }
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

                    itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    if (!user.equals(ApplicationContext.getUserHandler().getUser())) {
                        itemPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                super.mousePressed(e);
                                System.out.println("CLICK ON : " + user);

                                // Check if user has existing direct messages
                                List<Message> directMessages = ApplicationContext.getMessageHandler().getDirectMessageHistory(user);

                                if (directMessages == null || directMessages.isEmpty()) {
                                    // First time: Send an invisible message to initiate the direct chat
                                    long messageId = messageSerial.incrementAndGet();
                                    Message initMessage = new Message(
                                            (int) messageId,
                                            ApplicationContext.getUserHandler().getUser().getId(),
                                            user.getId(),
                                            -1,
                                            "👋", // Invisible / greeting emoji message to initiate
                                            true,
                                            Timestamp.valueOf(LocalDateTime.now())
                                    );

                                    try {
                                        ApplicationContext.getMessageHandler().sendTextMessage(initMessage);
                                        System.out.println("Sent initial direct message to: " + user.getDisplayName());
                                    } catch (Exception ex) {
                                        System.err.println("Error sending initial direct message: " + ex.getMessage());
                                    }
                                }

                                // Navigate to direct message screen with this user
                                ApplicationContext.getEventBus().publish(new ApplicationEvent(
                                        ApplicationEvent.Type.DIRECT_SCREEN_SELECTED
                                ));

                                // Add a delay to ensure the direct message screen is loaded before selecting the user
                                SwingUtilities.invokeLater(() -> {
                                    // Give the UI time to update and mount components
                                    SwingUtilities.invokeLater(() -> {
                                        ApplicationContext.getEventBus().publish(new ApplicationEvent(
                                                ApplicationEvent.Type.DIRECT_USER_SELECTED,
                                                user
                                        ));
                                    });
                                });
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                super.mouseEntered(e);
                                itemPanel.setBackground(ThemeManager.getCurrentTheme().getTertiaryColor());
                                usernameLabel.setForeground(ThemeManager.getCurrentTheme().getOnTertiaryColor());
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                super.mouseExited(e);
                                itemPanel.setBackground(ThemeManager.getCurrentTheme().getBackgroundColor());
                                usernameLabel.setForeground(ThemeManager.getCurrentTheme().getOnBackgroundColor());
                            }
                        });
                    }

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
