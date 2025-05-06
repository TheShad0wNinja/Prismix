package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedIcon;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;

/**
 * Header component for direct chat that displays the user's name and avatar,
 * along with a video call button.
 */
public class DirectChatHeader extends ThemedPanel {
    private final User user;
    private static final int AVATAR_SIZE = 40;

    public DirectChatHeader(User user) {
        super(Variant.SURFACE);
        this.user = user;
        initializeUI();
    }

    private void initializeUI() {
        // Use BorderLayout to place elements
        setLayout(new BorderLayout(10, 0));
        setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        setPreferredSize(new Dimension(getWidth(), 60));

        // Create left panel for user info (avatar + name)
        JPanel userInfoPanel = new ThemedPanel(Variant.SURFACE);
        userInfoPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 0));
        userInfoPanel.setOpaque(false);

        // Add user avatar
        JLabel avatarLabel = new ThemedIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.CIRCLE);
        userInfoPanel.add(avatarLabel);

        // Add user display name
        ThemedLabel usernameLabel = new ThemedLabel(user.getDisplayName(), ThemedLabel.Size.LARGER);
        userInfoPanel.add(usernameLabel);

        // Create right panel for actions (call button)
        JPanel actionsPanel = new ThemedPanel(Variant.SURFACE);
        actionsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionsPanel.setOpaque(false);

        // Create video call button
        ThemedButton callButton = new ThemedButton("Video Call", ThemedButton.Variant.PRIMARY);
        callButton.addActionListener(e -> initiateVideoCall());
        actionsPanel.add(callButton);

        // Add panels to the layout
        add(userInfoPanel, BorderLayout.WEST);
        add(actionsPanel, BorderLayout.EAST);
    }

    private void initiateVideoCall() {
        // Initiate video call using VideoChatHandler
        ApplicationContext.getVideoChatHandler().initiateCall(user);
    }
} 