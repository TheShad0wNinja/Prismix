package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.core.handlers.RoomHandler;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.utils.AvatarDisplayHelper;
import com.prismix.common.model.Room;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class RoomMainPanel extends ThemedPanel implements EventListener {
    private Room room;
    private final JPanel usersPanel;

    public RoomMainPanel(Room room) {
        super(Variant.BACKGROUND, true);
        this.room = room;

        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        JPanel chatsPanel = new ChatPanel();
        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 0.7;
        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        add(chatsPanel, c);

        usersPanel = new ThemedPanel(Variant.BACKGROUND);
        usersPanel.setLayout(new BoxLayout(usersPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(usersPanel);
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.3;
        add(scrollPane, c);

        ApplicationContext.getEventBus().subscribe(this);
    }

    public void updateUserList(List<User> users) {
        usersPanel.removeAll();

        System.out.println("UPDATING USERS: " + users);

        for (User user : users) {
            JPanel itemPanel = new ThemedPanel(Variant.BACKGROUND);
            itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, itemPanel.getPreferredSize().height + 10));
            itemPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));

            JLabel icon = new JLabel();
            icon.setIcon(AvatarDisplayHelper.getAvatarImageIcon(user.getAvatar(), 30, 30));
            itemPanel.add(icon);
            itemPanel.add(new ThemedLabel(user.getUsername(), ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND));

            usersPanel.add(itemPanel);
            usersPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            System.out.println("ADDING USER: " + user);
        }
        usersPanel.add(Box.createVerticalGlue());

        usersPanel.revalidate();
        usersPanel.repaint();

        revalidate();
        repaint();
    }

    public void setRoom(Room room) {
        this.room = room;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_USERS_UPDATED) {
            RoomHandler.RoomUsersInfo info = (RoomHandler.RoomUsersInfo) event.data();
            System.out.println(info.room() + " vs " + this.room);
            if (info.room() != null && info.room().equals(room)) {
                System.out.println(" AJKLAJKLFJKLDFJKL " + info.users() );
                updateUserList(info.users());
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}