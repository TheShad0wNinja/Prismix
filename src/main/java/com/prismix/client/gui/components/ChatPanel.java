package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;

public class ChatPanel extends ThemedPanel implements EventListener {
    JScrollPane chatScrollPane;
    JPanel mainPanel;
    public ChatPanel() {
        super(Variant.PRIMARY);
        setLayout(new GridLayout(1, 1));

        mainPanel = new ThemedPanel(Variant.PRIMARY);
        mainPanel.setLayout(new GridBagLayout());

        chatScrollPane = new JScrollPane(mainPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(chatScrollPane);

        ApplicationContext.getEventBus().subscribe(this);

    }

    private void appendMessage(Message msg) {
        User user =  ApplicationContext.getRoomHandler().getRoomUser(msg.getSenderId());
        JPanel messageEntry = new MessageEntry(user, msg);
        GridBagConstraints c = new GridBagConstraints();
        c.weightx = 1.0;
        c.gridx = 0;
        c.fill = GridBagConstraints.HORIZONTAL;

        mainPanel.add(messageEntry, c);
        JPanel messageEntry1 = new MessageEntry(user, msg);

        mainPanel.add(messageEntry1, c);

        JPanel messageEntry2 = new MessageEntry(user, msg);
        mainPanel.add(messageEntry2, c);

        c.weighty = 1.0;
        c.fill = GridBagConstraints.BOTH;
        mainPanel.add(Box.createVerticalGlue(), c);

        mainPanel.revalidate();
        mainPanel.repaint();
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_MESSAGE) {
            Message msg = (Message) event.data();
            appendMessage(msg);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
