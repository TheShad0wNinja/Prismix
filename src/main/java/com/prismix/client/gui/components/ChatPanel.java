package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatPanel extends ThemedPanel implements EventListener {
    private final JScrollPane chatScrollPane;
    private final JPanel mainPanel;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private static final int MAX_MESSAGES = 100; // Limit number of messages to prevent memory issues
    
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
        if (isUpdating.getAndSet(true)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                User user = ApplicationContext.getRoomHandler().getRoomUser(msg.getSenderId());
                if (user == null) {
                    return;
                }

                JPanel messageEntry = new MessageEntry(user, msg);
                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 1.0;
                c.gridx = 0;
                c.fill = GridBagConstraints.HORIZONTAL;

                // Remove oldest message if we've reached the limit
                if (mainPanel.getComponentCount() >= MAX_MESSAGES) {
                    mainPanel.remove(0);
                }

                mainPanel.add(messageEntry, c);

                // Add vertical glue at the end
                c.weighty = 1.0;
                c.fill = GridBagConstraints.BOTH;
                mainPanel.add(Box.createVerticalGlue(), c);

                // Batch UI updates
                mainPanel.revalidate();
                mainPanel.repaint();

                // Scroll to bottom
                SwingUtilities.invokeLater(() -> {
                    JScrollBar vertical = chatScrollPane.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                });
            } finally {
                isUpdating.set(false);
            }
        });
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
