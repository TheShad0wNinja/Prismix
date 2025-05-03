package com.prismix.client.core.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventBus;
import com.prismix.client.core.EventListener;
import com.prismix.client.repositories.MessageRepository;
import com.prismix.common.model.Message;
import com.prismix.common.model.Room;
import com.prismix.common.model.network.NetworkMessage;

import java.util.ArrayList;

public class MessageHandler implements ResponseHandler, EventListener {
    private ArrayList<Message> currentMessages;
    private Room currentRoom;
    private EventBus eventBus;

    public MessageHandler(EventBus eventBus) {
        this.eventBus = eventBus;
        currentMessages = new ArrayList<>();
        eventBus.subscribe(this);
    }

    void updateMessages() {
        ArrayList<Message> messages = MessageRepository.getMessagesByRoomId(currentRoom.getId());
        System.out.println("Messages: " + messages);
        for (Message message : messages) {
            System.out.println("PUSHING MESSAGE: " + message);
            eventBus.publish(new ApplicationEvent(ApplicationEvent.Type.ROOM_MESSAGE, message));
        }
    }

    @Override
    public void handleResponse(NetworkMessage message) {
//        if (message.getMessageType() == NetworkMessage.MessageType.SEND_TEXT_MESSAGE) {
//
//        }
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.ROOM_USERS_UPDATED) {
//            currentRoom = (Room) event.data();
            updateMessages();
        } else if (event.type() == ApplicationEvent.Type.ROOM_SELECTED) {
            currentRoom = (Room) event.data();
        }
    }
}
