package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatPanel extends ThemedPanel implements EventListener {
    private final JScrollPane chatScrollPane;
    private final JPanel mainPanel;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private final ConcurrentLinkedQueue<Message> messages = new ConcurrentLinkedQueue<>();
    private int messageCount = 0;
    private final Component verticalGlue;
//    private static final int MAX_MESSAGES = 100; // Limit number of messages to prevent memory issues
    
    public ChatPanel() {
        super(Variant.PRIMARY);
        setLayout(new GridLayout(1, 1));

        mainPanel = new ThemedPanel(Variant.PRIMARY);
        mainPanel.setLayout(new GridBagLayout());
        verticalGlue = Box.createVerticalGlue();

        chatScrollPane = new JScrollPane(mainPanel);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(chatScrollPane);

        ApplicationContext.getEventBus().subscribe(this);
    }

    private void processMessage() {
        if (isUpdating.getAndSet(true)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                Message msg = messages.poll();
                if (msg == null) {
                    isUpdating.set(false);
                    return;
                }
                User user = ApplicationContext.getRoomHandler().getRoomUser(msg.getSenderId());
                if (user == null) {
                    return;
                }

                messageCount++;
                JPanel messageEntry = new MessageEntry(user, msg);
                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 1.0;
                c.gridx = 0;
                c.fill = GridBagConstraints.HORIZONTAL;

                mainPanel.add(messageEntry, c);

                // Add vertical glue at the end
                c.weighty = 1.0;
                c.gridy = messageCount;
                c.fill = GridBagConstraints.BOTH;
                mainPanel.remove(verticalGlue);
                mainPanel.add(verticalGlue, c);

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
                processMessage();
            }
        });
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.MESSAGE) {
            Message msg = (Message) event.data();
            if (msg.getRoomId() == ApplicationContext.getRoomHandler().getCurrentRoom().getId()) {
                System.out.println("GOT MESSAGE: " + msg);
                messages.offer(msg);
                processMessage();
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
