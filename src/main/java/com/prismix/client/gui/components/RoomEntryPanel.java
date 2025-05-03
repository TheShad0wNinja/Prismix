package com.prismix.client.gui.components;

import com.prismix.client.core.EventListener;
import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.core.ApplicationEvent;
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

public class RoomEntryPanel extends ThemedPanel implements EventListener {

    private static final int VERTICAL_SPACING = 4;
    private final Room room;
    private boolean isSelected;
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

        isSelected = ApplicationContext.getRoomHandler().getCurrentRoom() != null && ApplicationContext.getRoomHandler().getCurrentRoom().equals(room);

        initComponents();
        displayRoomInfo();

        if (!isSelected) {
            addListeners();
        }

        ApplicationContext.getEventBus().subscribe(this);
    }

    private void addListeners() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(false);
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

    private void removeListeners() {
        if (getMouseListeners()[0] != null)
            removeMouseListener(getMouseListeners()[0]);
    }


    private void setHover(boolean hover) {
        if (this.isHovered != hover) {
            this.isHovered = hover;
            repaint();
        }
    }

    private void initComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10)); // Simple layout for the entry
        if (!isSelected) {
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

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
            roomAvatarLabel.setIcon(null);
            roomNameLabel.setText("Unknown Room");
        }
    }

    @Override
    public Dimension getMaximumSize() {
        Dimension preferredSize = getPreferredSize();
        return new Dimension(Integer.MAX_VALUE, preferredSize.height + VERTICAL_SPACING);
    }

    @Override
    public void applyTheme(Theme theme) {
        super.applyTheme(theme);

        if (isSelected) {
            this.defaultBackgroundColor = theme.getTertiaryColor();
            this.defaultTextColor = theme.getOnTertiaryColor();
        } else {
            this.defaultBackgroundColor = theme.getSurfaceVariantColor();
            this.hoverBackgroundColor = theme.getTertiaryColor();
            this.hoverTextColor = theme.getOnTertiaryColor();
            this.defaultTextColor = theme.getOnSurfaceVariantColor();
        }

        setBackground(defaultBackgroundColor);

        repaint();
    }


    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        Color currentBackgroundColor = defaultBackgroundColor;
        Color currentTextColor = defaultTextColor;
        if (isHovered || isSelected) {
            currentBackgroundColor = this.hoverBackgroundColor;
            currentTextColor = this.hoverTextColor;
        }

        ThemePainter.paintRoundedBackground(g2d, currentBackgroundColor, 0, 0, width, height, this.cornerRadius);
        roomNameLabel.setForeground(currentTextColor);


        g2d.dispose();
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            Room room = (Room) event.data();

            boolean isNewSelectedRoom = room != null && room.equals(this.room);
            if (isNewSelectedRoom) {
                this.isSelected = true;
                removeListeners();
                repaint();
            } else if (this.isSelected) {
                this.isSelected = false;
                this.isHovered = false;
                addListeners();
                repaint();
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
