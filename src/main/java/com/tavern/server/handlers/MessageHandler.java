package com.tavern.server.handlers;

import com.tavern.common.model.network.*;
import com.tavern.common.model.Message;
import com.tavern.common.model.User;
import com.tavern.server.core.ClientHandler;
import com.tavern.server.core.RequestHandler;
import com.tavern.server.data.manager.MessageManager;
import com.tavern.server.data.manager.RoomManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;

public class MessageHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(MessageHandler.class);
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
                            logger.debug("Marking message as unread for receiver: {}", msg.getReceiverId());
                            MessageManager.markMessageAsUnread(msg.getReceiverId(), createdMsg);
                        }
                    } else {
                        // Receiver is not active, mark message as unread
                        logger.debug("Receiver not active, marking message as unread: {}", msg.getReceiverId());
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

                    logger.debug("Room users: {}", roomUsers);
                    for (User user : roomUsers) {
                        if (user.getId() == (msg.getSenderId())) {
                            client.sendMessage(new SendTextMessageResponse(msg, true));
                            continue;
                        }

                        if (activeUsers.containsKey(user)) {
                            logger.debug("Active user: {}, connected: {}", 
                                    user.getUsername(), activeUsers.get(user).isConnected());
                            if (activeUsers.get(user).isConnected()) {
                                activeUsers.get(user).sendMessage(new ReceiveTextMessageRequest(msg));
                                continue;
                            }
                            activeUsers.remove(user);
                        }
                        logger.debug("Marking user {} as unread", user.getUsername());
                        MessageManager.markMessageAsUnread(user, createdMsg);
                    }
                }
            }
            case GET_UNREAD_MESSAGE_REQUEST -> {
                GetUnreadMessagesRequest request = (GetUnreadMessagesRequest) message;
                List<Message> messages = MessageManager.getUnreadMessages(request.user().getId());
                logger.debug("Retrieved unread messages for user {}: count={}", 
                        request.user().getUsername(), messages != null ? messages.size() : 0);
                if (messages == null) {
                    return;
                }

                if (userHandler.getActiveUsers().containsKey(request.user())) {
                    logger.debug("Sending {} unread messages to user {}", 
                            messages.size(), request.user().getUsername());
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
