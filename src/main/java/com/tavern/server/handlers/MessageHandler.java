package com.tavern.server.handlers;

import com.tavern.common.model.network.*;
import com.tavern.common.model.Message;
import com.tavern.common.model.User;
import com.tavern.server.core.ClientHandler;
import com.tavern.server.core.RequestHandler;
import com.tavern.server.data.manager.MessageManager;
import com.tavern.server.data.manager.RoomManager;

import java.util.HashMap;
import java.util.List;

public class MessageHandler implements RequestHandler {
    private final UserHandler userHandler;

    public MessageHandler(UserHandler userHandler, HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.userHandler = userHandler;
        requestHandlers.put(NetworkMessage.MessageType.SEND_TEXT_MESSAGE_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_UNREAD_MESSAGE_REQUEST, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case SEND_TEXT_MESSAGE_REQUEST -> {
                SendTextMessageRequest request = (SendTextMessageRequest) message;
                Message msg = request.message();
                HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
                if (msg.isDirect()) {
                    Message createdMsg = MessageManager.createMessage(msg);
                    if (createdMsg == null) {
                        client.sendMessage(new SendTextMessageResponse(msg, false));
                        return;
                    }
                    
                    // Send response to sender
                    client.sendMessage(new SendTextMessageResponse(msg, true));
                    
                    User receiver = new User();
                    receiver.setId(msg.getReceiverId());
                    
                    // If receiver is active, send the message directly
                    if (activeUsers.containsKey(receiver)) {
                        boolean delivered = false;
                        if (activeUsers.get(receiver).isConnected()) {
                            delivered = activeUsers.get(receiver).sendMessage(new ReceiveTextMessageRequest(msg));
                        }
                        
                        // If delivery failed or user not connected, mark as unread
                        if (!delivered) {
                            activeUsers.remove(receiver);
                            System.out.println("Marking message as unread for receiver: " + msg.getReceiverId());
                            MessageManager.markMessageAsUnread(msg.getReceiverId(), createdMsg);
                        }
                    } else {
                        // Receiver is not active, mark message as unread
                        System.out.println("Receiver not active, marking message as unread: " + msg.getReceiverId());
                        MessageManager.markMessageAsUnread(msg.getReceiverId(), createdMsg);
                    }
                } else {
                    List<User> roomUsers = RoomManager.getMembersOfRoom(msg.getRoomId());
                    if (roomUsers == null) {
                        return;
                    }

                    Message createdMsg = MessageManager.createMessage(msg);
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
                System.out.println("THE MESSAGES: " + messages);
                if (messages == null) {
                    return;
                }

                if (userHandler.getActiveUsers().containsKey(request.user())) {
                    System.out.println(messages);
                    userHandler.getActiveUsers().get(request.user()).sendMessage(new GetUnreadMessagesResponse(messages));
                    for (Message m : messages) {
                        MessageManager.markMessageAsRead(request.user().getId(), m.getId());
                    }
                }
                MessageManager.cleanupMessages();
            }
        }
    }
}
