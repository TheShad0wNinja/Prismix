package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedIcon;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.components.themed.ThemedTextField;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Message;
import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.common.model.network.NetworkMessage;

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
    private final JPanel usersPanel;
    private final ChatPanel chatPanel;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private static final int USERS_PANEL_WIDTH = 200;
    private static final int MAX_USERNAME_LENGTH = 15;
    private static final int AVATAR_SIZE = 30;
    private static final AtomicLong messageSerial = new AtomicLong(0);

    public RoomMainPanel(Room room) {
        super(Variant.BACKGROUND, true);
        this.room = room;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        chatPanel = new ChatPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 1;
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

        // Add message input panel at the bottom
        JPanel messageInputPanel = new ThemedPanel(Variant.PRIMARY);
        messageInputPanel.setLayout(new BorderLayout(5, 5));
        messageInputPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));

        ThemedTextField messageInput = new ThemedTextField("Type a message...");
        messageInput.setPreferredSize(new Dimension(0, 30));
        messageInputPanel.add(messageInput, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));

        JButton downloadButton = new JButton("Download");
        downloadButton.setPreferredSize(new Dimension(90, 30));
        downloadButton.addActionListener(e -> showFileListDialog());
        buttonPanel.add(downloadButton);

        JButton fileButton = new JButton("Upload");
        fileButton.setPreferredSize(new Dimension(80, 30));
        fileButton.addActionListener(e -> {
            ApplicationContext.getFileTransferHandler().selectAndSendFileToRoom(room.getId());
        });
        buttonPanel.add(fileButton);

        JButton sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(80, 30));
        sendButton.addActionListener(e -> sendMessage(messageInput));
        buttonPanel.add(sendButton);

        messageInputPanel.add(buttonPanel, BorderLayout.EAST);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1.0;
        c.weighty = 0.0;
        c.fill = GridBagConstraints.HORIZONTAL;
        add(messageInputPanel, c);

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
                ApplicationContext.getAuthHandler().getUser().getId(),
                -1,
                room.getId(),
                content,
                false,
                Timestamp.valueOf(LocalDateTime.now()));

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
                    itemPanel
                            .setMaximumSize(new Dimension(USERS_PANEL_WIDTH, itemPanel.getPreferredSize().height + 10));
                    itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

                    JLabel icon = new ThemedIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.CIRCLE);
                    itemPanel.add(icon);

                    String username = user.getDisplayName();
                    if (username.length() > MAX_USERNAME_LENGTH) {
                        username = username.substring(0, MAX_USERNAME_LENGTH - 3) + "...";
                    }

                    ThemedLabel usernameLabel = new ThemedLabel(username, ThemedLabel.Size.SMALLER,
                            ThemedLabel.Variant.BACKGROUND);
                    usernameLabel.setToolTipText(user.getDisplayName());
                    itemPanel.add(usernameLabel);

                    itemPanel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                    if (!user.equals(ApplicationContext.getAuthHandler().getUser())) {
                        itemPanel.addMouseListener(new MouseAdapter() {
                            @Override
                            public void mousePressed(MouseEvent e) {
                                super.mousePressed(e);
                                System.out.println("CLICK ON : " + user);
                                ApplicationContext.getVideoChatHandler().initiateCall(user);
                            }

                            @Override
                            public void mouseEntered(MouseEvent e) {
                                super.mouseEntered(e);
                                itemPanel.setBackground(Color.PINK);
                            }

                            @Override
                            public void mouseExited(MouseEvent e) {
                                super.mouseExited(e);
                                itemPanel.setBackground(Color.WHITE);
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

    private void showFileListDialog() {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Available Files", false);
        dialog.setLayout(new BorderLayout());

        FileListPanel fileListPanel = new FileListPanel();
        dialog.add(fileListPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        buttonPanel.add(closeButton);
        dialog.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setSize(400, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
