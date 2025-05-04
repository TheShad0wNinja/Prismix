package com.prismix.client.core.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventBus;
import com.prismix.client.core.EventListener;
import com.prismix.client.repositories.MessageRepository;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.common.model.Message;
import com.prismix.common.model.Room;
import com.prismix.common.model.network.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MessageHandler implements ResponseHandler, EventListener {
    private final ConcurrentLinkedQueue<Message> pendingMessages;
    private final ConcurrentHashMap<Message, Boolean> pendingMessageStatus;
    private Room currentRoom;
    private final EventBus eventBus;
    private final AuthHandler authHandler;

    public MessageHandler(EventBus eventBus, AuthHandler authHandler, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandler) {
        this.eventBus = eventBus;
        this.authHandler = authHandler;
        pendingMessages = new ConcurrentLinkedQueue<>();
        pendingMessageStatus = new ConcurrentHashMap<>();
        responseHandler.put(NetworkMessage.MessageType.RECEIVE_TEXT_MESSAGE_REQUEST, this);
        responseHandler.put(NetworkMessage.MessageType.SEND_TEXT_MESSAGE_RESPONSE, this);
        responseHandler.put(NetworkMessage.MessageType.GET_UNREAD_MESSAGE_RESPONSE, this);
        eventBus.subscribe(this);
    }

    void updateMessages() {
        ArrayList<Message> messages = MessageRepository.getMessagesByRoomId(currentRoom.getId());
        System.out.println("ROOM MESSAGES: " + messages);
//        for (Message message : messages) {
        eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.MESSAGES, messages));
//        }
    }

    private synchronized void processMessage() {
        Message currMsg = pendingMessages.peek();
        System.out.println("PENDING MESSAGE: " + currMsg);
        if (currMsg != null) {
            System.out.println("PENDING MESSAGE STATUS: " + pendingMessageStatus.get(currMsg));
        }
        
        while (currMsg != null && Boolean.TRUE.equals(pendingMessageStatus.get(currMsg))) {
            System.out.println("ADDING NEW MESSAGE: " + currMsg);
            pendingMessages.poll(); // Remove from queue after confirmation
            MessageRepository.createMessage(currMsg);
            pendingMessageStatus.remove(currMsg);
            eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.MESSAGE, currMsg));
            currMsg = pendingMessages.peek();
        }
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
                    System.out.println("ERRRRORRR");
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
        if (event.type() == ApplicationEvent.Type.USER_LOGGED_IN) {
            try {
                ConnectionManager.getInstance().sendMessage(new GetUnreadMessagesRequest(authHandler.getUser()));
            } catch (IOException e) {
                System.out.println("Unable to get unread messages");
            }
        }
        if (event.type() == ApplicationEvent.Type.ROOM_USERS_UPDATED) {
            updateMessages();
        } else if (event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            currentRoom = (Room) event.data();
        }
    }

    public void sendTextMessage(Message message) {
        try {
            pendingMessages.offer(message);
            pendingMessageStatus.put(message, false);
            ConnectionManager.getInstance().sendMessage(new SendTextMessageRequest(message));
        } catch (IOException e) {
            System.out.println("OMAK");
        }
    }
}
