package com.prismix.client.core.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.client.core.EventBus;
import com.prismix.common.model.User;
import com.prismix.common.model.network.*;

import java.io.IOException;
import java.util.HashMap;

public class AuthHandler implements ResponseHandler {
    private final EventBus eventBus;
    private User user;

    public AuthHandler(EventBus eventBus, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandlers) {
        this.eventBus = eventBus;
        responseHandlers.put(NetworkMessage.MessageType.LOGIN_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.SIGNUP_RESPONSE, this);
    }

    public User getUser() {
        return user;
    }

    public void login(String username) {
        ConnectionManager manager = ConnectionManager.getInstance();
        try {
            manager.sendMessage(new LoginRequest(username));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void signup(String username, String displayName, byte[] avatar) {
        ConnectionManager manager = ConnectionManager.getInstance();
        try {
            manager.sendMessage(new SignupRequest(username, displayName, avatar));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    public void handleResponse(NetworkMessage msg) {
        if (msg.getMessageType() == NetworkMessage.MessageType.LOGIN_RESPONSE) {
            LoginResponse res = (LoginResponse) msg;
            if (res.status()) {
                user = res.user();
                eventBus.publish(new ApplicationEvent(
                    ApplicationEvent.Type.USER_LOGGED_IN,
                    user
                ));
            }
        } else if (msg.getMessageType() == NetworkMessage.MessageType.SIGNUP_RESPONSE) {
            SignupResponse res = (SignupResponse) msg;
            if (res.status()) {
                user = res.user();
                eventBus.publish(new ApplicationEvent(
                    ApplicationEvent.Type.USER_LOGGED_IN,
                    user
                ));
            }
        } else {
            user = null;
        }
    }
}
