package com.prismix.client.core;

import com.prismix.common.model.User;
import com.prismix.common.model.network.LoginRequest;
import com.prismix.common.model.network.LoginResponse;
import com.prismix.common.model.network.SignupRequest;
import com.prismix.common.model.network.SignupResponse;

import java.io.IOException;
import java.util.function.Function;

public class AuthManager {
    private static AuthManager instance;
    private User user;

    private AuthManager() {}

    public static AuthManager getInstance() {
        if (instance == null) {
            instance = new AuthManager();
        }
        return instance;
    }

    public static User getUser() {
        if (instance == null) {
            return null;
        }
        return getInstance().user;
    }

    public void login(String username, Function<User, Void> callback) {
        ConnectionManager manager = ConnectionManager.getInstance();
        try {
            manager.sendMessage(new LoginRequest(username));
            LoginResponse response = (LoginResponse) manager.receiveMessage();
            if (!response.status())
                throw new IllegalArgumentException(response.errorMessage());
            else {
                user = response.user();
                callback.apply(user);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }

    public void signup(String username, String displayName, byte[] avatar, Function<User, Void> callback) {
        ConnectionManager manager = ConnectionManager.getInstance();
        try {
            manager.sendMessage(new SignupRequest(username, displayName, avatar));
            SignupResponse response = (SignupResponse) manager.receiveMessage();
            if (!response.status())
                throw new IllegalArgumentException(response.errorMessage());
            else {
                user = response.user();
                callback.apply(user);
            }
        } catch (IOException | ClassNotFoundException e) {
            System.out.println(e.getMessage());
        }
    }
}
