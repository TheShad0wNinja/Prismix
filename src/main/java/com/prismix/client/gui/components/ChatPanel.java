package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class ChatPanel extends ThemedPanel implements EventListener {
    private final JScrollPane chatScrollPane;
    private final JPanel mainPanel;
    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private final PriorityBlockingQueue<Message> messages = new PriorityBlockingQueue<>(
            11,
            Comparator.comparing(Message::getTimestamp)
    );
    private int messageCount = 0;
    private final Component verticalGlue;
    private final boolean isDirect;

    public ChatPanel(boolean isDirect) {
        super(Variant.BACKGROUND);
        setLayout(new BorderLayout());
        this.isDirect = isDirect;

        mainPanel = new ThemedPanel(Variant.BACKGROUND);
        mainPanel.setLayout(new GridBagLayout());
        verticalGlue = Box.createVerticalGlue();

        chatScrollPane = new JScrollPane(mainPanel);
        chatScrollPane.setBorder(null);
        chatScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        chatScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        add(chatScrollPane, BorderLayout.CENTER);

        add(new InputBar(isDirect), BorderLayout.SOUTH);

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
                User user;
                if (isDirect) {
                    User myUser = ApplicationContext.getUserHandler().getUser();
                    User otherUser = ApplicationContext.getMessageHandler().getCurrentDirectUser();
                    user = myUser.getId() == msg.getSenderId() ? myUser : otherUser;
                } else {
                    user =  ApplicationContext.getRoomHandler().getRoomUser(msg.getSenderId());
                }
                if (user == null) {
                    return;
                }

                messageCount++;
                JPanel messageEntry = new MessageEntry(user, msg);
                GridBagConstraints c = new GridBagConstraints();
                c.weightx = 1.0;
                c.gridx = 0;
                c.insets = new Insets(10, 10, 10, 10);
                c.fill = GridBagConstraints.HORIZONTAL;

                mainPanel.add(messageEntry, c);

                c.weighty = 1.0;
                c.gridy = messageCount;
                c.fill = GridBagConstraints.BOTH;
                c.insets = new Insets(0, 0, 0, 0);
                mainPanel.remove(verticalGlue);
                mainPanel.add(verticalGlue, c);

                mainPanel.revalidate();
                mainPanel.repaint();

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
        switch (event.type()) {
            case MESSAGE -> {
                Message msg = (Message) event.data();
                if (isDirect) {
                    int currentDirectUserId = ApplicationContext.getMessageHandler().getCurrentDirectUser().getId();
                    if (msg.getReceiverId() == currentDirectUserId || msg.getSenderId() == currentDirectUserId) {
                        System.out.println("GOT MESSAGE: " + msg);
                        messages.offer(msg);
                        processMessage();
                    }
                } else {
                    if (msg.getRoomId() == ApplicationContext.getRoomHandler().getCurrentRoom().getId()) {
                        System.out.println("GOT MESSAGE: " + msg);
                        messages.offer(msg);
                        processMessage();
                    }
                }
            }
            case MESSAGES -> {
                List<Message> msg = (List<Message>) event.data();
                if (msg != null) {
                    messages.addAll(msg);
                    processMessage();
                }
            }
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
