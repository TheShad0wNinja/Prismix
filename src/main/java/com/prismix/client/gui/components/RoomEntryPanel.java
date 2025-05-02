package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationContext;
import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemePainter;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Room;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class RoomEntryPanel extends ThemedPanel {

    private static final int VERTICAL_SPACING = 4;
    private final Room room;
    private JLabel roomAvatarLabel;
    private JLabel roomNameLabel;

    private Color defaultBackgroundColor;
    private Color defaultTextColor;
    private Color hoverBackgroundColor;
    private Color hoverTextColor;
    private boolean isHovered;

    public RoomEntryPanel(Room room) {
        super(Variant.SURFACE_ALT, true);
        this.room = room;

        initComponents();
        displayRoomInfo();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setHover(true);
                roomNameLabel.setForeground(hoverTextColor);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(false);
                roomNameLabel.setForeground(defaultTextColor);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                ApplicationContext.getEventBus().publish(new ApplicationEvent(
                    ApplicationEvent.Type.ROOM_SELECTED,
                    room
                ));
            }
        });
    }

    private void setHover(boolean hover) {
        if (this.isHovered != hover) {
            this.isHovered = hover;
            repaint();
        }
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Simple layout for the entry
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
        return new Dimension(Integer.MAX_VALUE, preferredSize.height + VERTICAL_SPACING);
    }

    @Override
    public void applyTheme(Theme theme) {
        super.applyTheme(theme);

        this.defaultBackgroundColor = theme.getSurfaceVariantColor();
        this.hoverBackgroundColor = theme.getTertiaryColor();
        this.hoverTextColor = theme.getOnTertiaryColor();
        this.defaultTextColor = theme.getOnSurfaceVariantColor();

        setBackground(defaultBackgroundColor);

        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        Color currentBackgroundColor = defaultBackgroundColor;
        if (isHovered) {
            currentBackgroundColor = this.hoverBackgroundColor;
        }

        ThemePainter.paintRoundedBackground(g2d, currentBackgroundColor, 0, 0, width, height, this.cornerRadius);

        g2d.dispose();
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
