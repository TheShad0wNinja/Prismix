package com.prismix.client.gui.components;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoomEntryPanel extends ThemedPanel {

    private static final int HORIZONTAL_SPACING = 240;
    private static final int VERTICAL_SPACING = 4;
    private final Room room;
    private JLabel roomAvatarLabel;
    private JLabel roomNameLabel;

    private Color defaultBackgroundColor;
    private Color defaultTextColor;
    private Color hoverBackgroundColor;
    private Color hoverTextColor;

    public RoomEntryPanel(Room room) {
        super();
        this.room = room;

        initComponents();
        displayRoomInfo();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverBackgroundColor);
                roomNameLabel.setForeground(hoverTextColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultBackgroundColor);
                roomNameLabel.setForeground(defaultTextColor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO: Handle room selection/opening the chat for this room
                System.out.println("Clicked on room: " + room.getName());
            }
        });
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5)); // Simple layout for the entry
        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Indicate clickable

        roomAvatarLabel = new ThemedLabel();
        roomNameLabel = new ThemedLabel();

        add(roomAvatarLabel);
        add(roomNameLabel);

        setAlignmentX(Component.LEFT_ALIGNMENT);

    }

    private void displayRoomInfo() {
        if (room != null) {
            ImageIcon avatarIcon = AvatarDisplayHelper.getAvatarImageIcon(room.getAvatar(), 40, 40);
            roomAvatarLabel.setIcon(avatarIcon);

            roomNameLabel.setText(room.getName());
        } else {
            roomAvatarLabel.setIcon(AvatarDisplayHelper.getAvatarImageIcon(null, 40, 40));
            roomNameLabel.setText("Unknown Room");
        }
    }

    @Override
    public Dimension getMaximumSize() {
        // Get the preferred size of THIS panel
        Dimension preferredSize = getPreferredSize();
        // Return a dimension with max height equal to preferred height (plus spacing)
        // Allow stretching horizontally (Integer.MAX_VALUE)
        return new Dimension(HORIZONTAL_SPACING, preferredSize.height + VERTICAL_SPACING);
    }

    @Override
    public void applyTheme(Theme theme) {
        this.defaultBackgroundColor = theme.getBackgroundColor();
        this.hoverBackgroundColor = theme.getHoverColor().brighter();
        this.hoverTextColor = theme.getBackgroundColor();
        this.defaultTextColor = theme.getForegroundColor();

        setBackground(defaultBackgroundColor);
    }

    @Override
    public void themeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
    }

    // Getter to access the Room object if needed by a listener
    public Room getRoom() {
        return room;
    }
}
