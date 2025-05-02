package com.prismix.server.core;

import com.prismix.common.model.User;
import com.prismix.common.model.network.*;
import com.prismix.server.data.manager.UserManager;
import com.prismix.server.data.repository.UserRepository;

public class UserHandler {
    private final UserManager userManager;

    public UserHandler(UserRepository userRepository) {
        userManager = new UserManager(userRepository);
    }

    public void handleMessage(NetworkMessage message, ClientHandler clientHandler) {
        switch (message.getMessageType()) {
            case SIGNUP_REQUEST -> {
                SignupRequest msg = (SignupRequest) message;
                User user = userManager.registerUser(msg.username(), msg.username(), null);
                if (user == null) {
                    new SignupResponse(false, "Invalid ", null);
                    break;
                }
                new SignupResponse(true, null, user);
            }
            case LOGIN_REQUEST -> {
                LoginRequest msg = (LoginRequest) message;
                User user = userManager.login(msg.username());
                LoginResponse response;
                if (user == null)
                    response = new LoginResponse(false, "Invalid Username", null);
                else
                    response = new LoginResponse(true, null, user);
                clientHandler.sendMessage(response);
            }
        }
    }
}
