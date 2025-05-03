package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;

public class MessageEntry extends ThemedPanel {
    public MessageEntry(User user, Message message) {
        super(Variant.BACKGROUND);
        setLayout(new GridLayout(1, 1));

        JPanel wrapperPanel = new JPanel();
        setOpaque(false);
        wrapperPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 20));

        JLabel icon = new JLabel();
        icon.setIcon(AvatarDisplayHelper.getAvatarImageIcon(user.getAvatar(), 35, 35));
        wrapperPanel.add(icon);

        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setOpaque(false);

        contentPanel.add(new ThemedLabel(user.getDisplayName(), ThemedLabel.Size.SMALLER));
        contentPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        contentPanel.add(new ThemedLabel(message.getContent(), ThemedLabel.Size.DEFAULT));

        wrapperPanel.add(contentPanel);

        add(wrapperPanel);
    }
}
