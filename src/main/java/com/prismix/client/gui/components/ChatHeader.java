package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedIcon;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;

public class ChatHeader extends ThemedPanel {
    private static final int AVATAR_SIZE = 20;
    Room room;

    public ChatHeader(Room room) {
        super(Variant.SURFACE_ALT);
        this.room = room;

        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 10));
        // JLabel icon = new JLabel();
        // icon.setIcon(AvatarDisplayHelper.getAvatarImageIcon(room.getAvatar(), 50,
        // 50));
        if (room.getId() != -1) {
            JLabel icon = new ThemedIcon(room.getAvatar(), AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.ROUNDED);
            add(icon);
        }
        JLabel name = new ThemedLabel(room.getName(), ThemedLabel.Size.SMALLER, ThemedLabel.Variant.SURFACE_ALT);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        add(name);

        setAlignmentX(CENTER_ALIGNMENT);
    }
}
