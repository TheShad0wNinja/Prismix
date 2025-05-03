package com.prismix.server.core;

import com.prismix.server.core.AuthHandler;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;
import com.prismix.common.model.network.NetworkMessage;
import com.prismix.common.model.network.SendTextMessageRequest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class MessageHandler implements RequestHandler {
    private final AuthHandler authHandler;

    public MessageHandler(AuthHandler authHandler, HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.authHandler = authHandler;
        requestHandlers.put(NetworkMessage.MessageType.SEND_TEXT_MESSAGE, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        if (message.getMessageType() == NetworkMessage.MessageType.SEND_TEXT_MESSAGE) {
            SendTextMessageRequest request = (SendTextMessageRequest) message;
            Message msg = request.message();
            HashMap<User, ClientHandler> activeUsers = authHandler.getActiveUsers();
            if (msg.isDirect() && activeUsers.containsKey(msg.getReceiverId())) {
                ClientHandler receiver = activeUsers.get(msg.getReceiverId());
                boolean status = receiver.sendMessage(request);
                if (!status)
                    activeUsers.remove(msg.getReceiverId());
            }
        }
    }
}
