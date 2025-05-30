package com.tavern.client.handlers;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventBus;
import com.tavern.client.core.EventListener;
import com.tavern.client.repositories.MessageRepository;
import com.tavern.client.utils.ConnectionManager;
import com.tavern.common.model.Message;
import com.tavern.common.model.Room;
import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandler implements ResponseHandler, EventListener {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
    private final ConcurrentLinkedQueue<Message> pendingMessages;
    private final ConcurrentHashMap<Message, Boolean> pendingMessageStatus;
    private Room currentRoom;
    private User currentDirectUser;
    private final EventBus eventBus;
    private final UserHandler userHandler;

    public MessageHandler(EventBus eventBus, UserHandler userHandler, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandler) {
        this.eventBus = eventBus;
        this.userHandler = userHandler;
        pendingMessages = new ConcurrentLinkedQueue<>();
        pendingMessageStatus = new ConcurrentHashMap<>();
        responseHandler.put(NetworkMessage.MessageType.RECEIVE_TEXT_MESSAGE_REQUEST, this);
        responseHandler.put(NetworkMessage.MessageType.SEND_TEXT_MESSAGE_RESPONSE, this);
        responseHandler.put(NetworkMessage.MessageType.GET_UNREAD_MESSAGE_RESPONSE, this);
        eventBus.subscribe(this);
    }

    void updateRoomMessages() {
        ArrayList<Message> messages = MessageRepository.getMessagesByRoomId(currentRoom.getId());
        logger.debug("Room messages for room {}: count={}", currentRoom.getName(), messages.size());
        eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.MESSAGES, messages));
    }

    void updateDirectUserMessages() {
        logger.debug("Loading direct messages with user: {}", currentDirectUser.getUsername());
        List<Message> messages = MessageRepository.getDirectMessageWithUser(currentDirectUser.getId());
        logger.debug("Direct messages with {}: count={}", currentDirectUser.getUsername(), messages.size());
        eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.MESSAGES, messages));
    }

    private synchronized void processMessage() {
        Message currMsg = pendingMessages.peek();
        if (currMsg != null) {
            logger.debug("Processing pending message: {}, status={}", 
                    currMsg.getContent().substring(0, Math.min(50, currMsg.getContent().length())), 
                    pendingMessageStatus.get(currMsg));
        }
        
        while (currMsg != null && Boolean.TRUE.equals(pendingMessageStatus.get(currMsg))) {
            logger.debug("Confirming message delivery: {}", 
                    currMsg.getContent().substring(0, Math.min(50, currMsg.getContent().length())));
            pendingMessages.poll(); // Remove from queue after confirmation
            MessageRepository.createMessage(currMsg);
            pendingMessageStatus.remove(currMsg);
            eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.MESSAGE, currMsg));
            currMsg = pendingMessages.peek();
        }
    }

    public User getCurrentDirectUser() {
        return currentDirectUser;
    }

    @Override
    public void handleResponse(NetworkMessage message) {
        switch (message.getMessageType()) {
            case RECEIVE_TEXT_MESSAGE_REQUEST -> {
                ReceiveTextMessageRequest response = (ReceiveTextMessageRequest) message;
                MessageRepository.createMessage(response.message());
                eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.MESSAGE, response.message()));
            }
            case SEND_TEXT_MESSAGE_RESPONSE -> {
                SendTextMessageResponse response = (SendTextMessageResponse) message;
                if (response.status()) {
                    pendingMessageStatus.put(response.message(), true);
                    processMessage();
                } else {
                    pendingMessageStatus.remove(response.message());
                    pendingMessages.remove(response.message());
                }
            }
            case GET_UNREAD_MESSAGE_RESPONSE -> {
                GetUnreadMessagesResponse response = (GetUnreadMessagesResponse) message;
                for (Message msg : response.messages()) {
                    MessageRepository.createMessage(msg);
                }
            }
        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case USER_LOGGED_IN -> {
                try {
                    ConnectionManager.getInstance().sendMessage(new GetUnreadMessagesRequest(userHandler.getUser()));
                } catch (IOException e) {
                    logger.error("Unable to get unread messages: {}", e.getMessage(), e);
                }
            }
            case ROOM_USERS_UPDATED -> {
                updateRoomMessages();
            }
            case ROOM_SELECTED -> {
                currentRoom = (Room) event.data();
                currentDirectUser = null;
            }
            case DIRECT_USER_SELECTED -> {
                currentDirectUser = (User) event.data();
                currentRoom = null;
                updateDirectUserMessages();
            }
        }
    }

    public void sendTextMessage(Message message) {
        try {
            pendingMessages.offer(message);
            pendingMessageStatus.put(message, false);
            ConnectionManager.getInstance().sendMessage(new SendTextMessageRequest(message));
        } catch (IOException e) {
            logger.error("Failed to send text message: {}", e.getMessage(), e);
        }
    }
    
    /**
     * Get direct message history with a specific user
     * @param user The user to get direct message history with
     * @return List of direct messages with the user
     */
    public List<Message> getDirectMessageHistory(User user) {
        if (user == null) return new ArrayList<>();
        return MessageRepository.getDirectMessageWithUser(user.getId());
    }
}
