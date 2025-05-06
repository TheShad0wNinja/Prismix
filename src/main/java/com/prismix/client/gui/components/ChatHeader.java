package com.prismix.client.gui.components;

import com.prismix.client.gui.components.themed.ThemedIcon;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;

import javax.swing.*;
import java.awt.*;

public class ChatHeader extends ThemedPanel {
    private static final int AVATAR_SIZE = 20;

    public ChatHeader(String title) {
        this(title, null);
    }

    public ChatHeader(String title, byte[] avatar) {
        super(Variant.SURFACE_ALT);

        setLayout(new FlowLayout(FlowLayout.CENTER, 5, 15));

        if (avatar != null) {
            JLabel icon = new ThemedIcon(avatar, AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.ROUNDED);
            add(icon);
        }
        JLabel name = new ThemedLabel(title, ThemedLabel.Size.SMALLER, ThemedLabel.Variant.SURFACE_ALT);
        name.setFont(name.getFont().deriveFont(Font.BOLD));
        add(name);

        setAlignmentX(CENTER_ALIGNMENT);
    }
}
