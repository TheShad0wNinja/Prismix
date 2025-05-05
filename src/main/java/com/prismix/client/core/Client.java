package com.prismix.client.core;

import com.prismix.client.handlers.ResponseHandler;
import com.prismix.common.model.network.NetworkMessage;

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
