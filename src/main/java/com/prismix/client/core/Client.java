package com.prismix.client.core;

import com.prismix.client.core.handlers.ResponseHandler;
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
//        switch (msg.getMessageType()) {

//            case LOGIN_RESPONSE, SIGNUP_RESPONSE ->
//                ApplicationContext.getAuthHandler().handleResponse(msg);
//            case GET_ROOMS_RESPONSE ->
//                ApplicationContext.getRoomHandler().handleResponse(msg);
//        }
    }
}
