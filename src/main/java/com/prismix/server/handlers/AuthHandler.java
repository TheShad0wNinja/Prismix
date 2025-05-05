package com.prismix.server.handlers;

import com.prismix.common.model.User;
import com.prismix.common.model.network.*;
import com.prismix.server.core.ClientHandler;
import com.prismix.server.core.RequestHandler;
import com.prismix.server.data.manager.UserManager;

import java.util.HashMap;

public class AuthHandler implements RequestHandler {
    private final HashMap<User, ClientHandler> activeUsers;

    public AuthHandler(HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.activeUsers = new HashMap<>();
        requestHandlers.put(NetworkMessage.MessageType.LOGIN_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.SIGNUP_REQUEST, this);
    }

    public HashMap<User, ClientHandler> getActiveUsers() {
        return activeUsers;
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler clientHandler) {
        switch (message.getMessageType()) {
            case SIGNUP_REQUEST -> {
                SignupRequest msg = (SignupRequest) message;
                User user = UserManager.registerUser(msg.username(), msg.username(), null);
                SignupResponse response;
                if (user == null) {
                    response = new SignupResponse(false, "Invalid ", null);
                } else {
                    response = new SignupResponse(true, null, user);
                    activeUsers.put(user, clientHandler);
                    clientHandler.setUser(user);
                }
                clientHandler.sendMessage(response);
            }
            case LOGIN_REQUEST -> {
                LoginRequest msg = (LoginRequest) message;
                User user = UserManager.login(msg.username());
                LoginResponse response;
                if (user == null)
                    response = new LoginResponse(false, "Invalid Username", null);
                else {
                    response = new LoginResponse(true, null, user);
                    activeUsers.put(user, clientHandler);
                    clientHandler.setUser(user);
                }
                clientHandler.sendMessage(response);
            }
        }
    }
}
