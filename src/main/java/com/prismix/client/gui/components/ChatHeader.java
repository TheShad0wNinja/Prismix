package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;

public class ChatHeader extends ThemedPanel {
    Room room;
    public ChatHeader(Room room) {
        super(Variant.SURFACE_ALT);
        this.room = room;

        initComponents();
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.CENTER, 20, 0));
        JLabel icon =  new JLabel();
        icon.setIcon(AvatarDisplayHelper.getAvatarImageIcon(room.getAvatar(), 50, 50));
        add(icon);
        add(new ThemedLabel(room.getName()));
        setAlignmentX(CENTER_ALIGNMENT);
    }
}
