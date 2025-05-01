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

    public NetworkMessage handleMessage(NetworkMessage message) {
        return switch (message.getMessageType()) {
            case SIGNUP_REQUEST -> {
                SignupRequest msg = (SignupRequest) message;
                User user = userManager.registerUser(msg.username(), msg.username(), null);
                if (user == null) {
                    yield new SignupResponse(false, "Invalid ", null);
                }
                yield new SignupResponse(true, null, user);
            }
            case LOGIN_REQUEST -> {
                LoginRequest msg = (LoginRequest) message;
                User user = userManager.login(msg.username());
                if (user == null) {
                    yield new LoginResponse(false, "Invalid Username", null);
                }
                yield new LoginResponse(true, null, user);
            }
            default -> {
                yield null;
            }
        };
    }
}
