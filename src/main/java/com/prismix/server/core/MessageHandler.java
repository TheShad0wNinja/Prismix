package com.prismix.server.core;

import com.prismix.common.model.network.*;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;
import com.prismix.server.data.manager.MessageManager;
import com.prismix.server.data.manager.RoomManager;

import java.util.HashMap;
import java.util.List;

public class MessageHandler implements RequestHandler {
    private final AuthHandler authHandler;

    public MessageHandler(AuthHandler authHandler, HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.authHandler = authHandler;
        requestHandlers.put(NetworkMessage.MessageType.SEND_TEXT_MESSAGE_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_UNREAD_MESSAGE_REQUEST, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case SEND_TEXT_MESSAGE_REQUEST -> {
                SendTextMessageRequest request = (SendTextMessageRequest) message;
                Message msg = request.message();
                HashMap<User, ClientHandler> activeUsers = authHandler.getActiveUsers();
                if (msg.isDirect()) {
                    System.out.println("DMS");
                } else {
                    List<User> roomUsers = RoomManager.getMembersOfRoom(msg.getRoomId());
                    if (roomUsers == null) {
                        return;
                    }

                    Message createdMsg = MessageManager.createMessage(msg);
                    System.out.println(createdMsg);
                    if (createdMsg == null) {
                        client.sendMessage(new SendTextMessageResponse(msg, false));
                        return;
                    }

                    System.out.println("ROOM USERS: " + roomUsers);
                    for (User user : roomUsers) {
                        if (user.getId() == (msg.getSenderId())) {
                            client.sendMessage(new SendTextMessageResponse(msg, true));
                            continue;
                        }

                        if (activeUsers.containsKey(user)) {
                            System.out.println("ACTIVE USER: " + user.getUsername() + " : " + activeUsers.get(user).isConnected());
                            if (activeUsers.get(user).isConnected()) {
                                activeUsers.get(user).sendMessage(new ReceiveTextMessageRequest(msg));
                                continue;
                            }
                            activeUsers.remove(user);
                        }
                        System.out.println("Marking user " + user.getUsername() + " as unread");
                        MessageManager.markMessageAsUnread(user, createdMsg);
                    }
                }
            }
            case GET_UNREAD_MESSAGE_REQUEST -> {
                GetUnreadMessagesRequest request = (GetUnreadMessagesRequest) message;
                List<Message> messages = MessageManager.getUnreadMessages(request.user().getId());
                if (messages == null) {
                    return;
                }

                if (authHandler.getActiveUsers().containsKey(request.user())) {
                    authHandler.getActiveUsers().get(request.user()).sendMessage(new GetUnreadMessagesResponse(messages));
                    for (Message m : messages) {
                        MessageManager.markMessageAsRead(request.user().getId(), m.getId());
                    }
                }
                MessageManager.cleanupMessages();
            }
        }
    }
}
