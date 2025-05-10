package com.tavern.client.core;

import com.tavern.client.handlers.ResponseHandler;
import com.tavern.common.model.network.NetworkMessage;

import java.util.HashMap;

public class Client {
    private HashMap<NetworkMessage.MessageType, ResponseHandler> handlers;

    public Client(HashMap<NetworkMessage.MessageType, ResponseHandler> handlers) {
        this.handlers = handlers;
        new Thread(new MessageListener(this)).start();
    }

    public void processMessage(NetworkMessage msg) {
        if (handlers.containsKey(msg.getMessageType())) {
            handlers.get(msg.getMessageType()).handleResponse(msg);
        }
    }
}
