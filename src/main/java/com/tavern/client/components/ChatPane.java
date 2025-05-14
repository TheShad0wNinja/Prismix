package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.components.ChatPanel;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.Message;
import com.tavern.common.model.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class ChatPane implements Initializable, EventListener, Cleanable {
    private static final Logger logger = LoggerFactory.getLogger(ChatPane.class);

    @FXML
    public ScrollPane chatScrollPane;
    @FXML
    public VBox chatArea;
    @FXML
    public HBox inputBar;

    private final AtomicBoolean isUpdating = new AtomicBoolean(false);
    private boolean isDirect;
    private final PriorityBlockingQueue<Message> messages = new PriorityBlockingQueue<>(
            11,
            Comparator.comparing(Message::getTimestamp));

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        isDirect = ApplicationContext.getMessageHandler().getCurrentDirectUser() != null;
        ApplicationContext.getEventBus().subscribe(this);
    }

    private void processMessage() {
        if (isUpdating.getAndSet(true)) {
            return;
        }

        Platform.runLater(() -> {
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
                    user = ApplicationContext.getRoomHandler().getRoomUser(msg.getSenderId());
                }

                if (user == null) {
                    return;
                }

                MessageEntry messageEntry = new MessageEntry(user, msg);
                chatArea.getChildren().add(messageEntry);
            } finally {
                isUpdating.set(false);
                if (!messages.isEmpty()) {
                    processMessage();
                } else {
                    Platform.runLater(() -> {
                        chatScrollPane.setVvalue(1.0);
                    });
                }
            }
        });
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }

    private boolean shouldProcessMessages(Message message) {
        if (isDirect) {
            int currentDirectUserId = ApplicationContext.getMessageHandler().getCurrentDirectUser().getId();
            return message.getReceiverId() == currentDirectUserId || message.getSenderId() == currentDirectUserId;
        }
        return message.getRoomId() == ApplicationContext.getRoomHandler().getCurrentRoom().getId();
    }


    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case MESSAGE -> {
                Message msg = (Message) event.data();
                if (shouldProcessMessages(msg)) {
                    logger.debug("Got message: {}", msg);
                    messages.offer(msg);
                    processMessage();
                }
            }
            case MESSAGES -> {
                List<Message> msg = (List<Message>) event.data();
                if (msg != null) {
                    messages.addAll(msg.stream().filter(this::shouldProcessMessages).toList());
                    processMessage();
                }
            }
        }
    }
}
