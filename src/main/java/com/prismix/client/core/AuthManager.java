package com.prismix.client.core;

import com.prismix.client.gui.screens.MainFrame;
import com.prismix.common.model.User;
import com.prismix.common.model.network.*;

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

    public void handleMessage(NetworkMessage msg) {
        if (msg.getMessageType() == NetworkMessage.MessageType.LOGIN_RESPONSE) {
            LoginResponse res = (LoginResponse) msg;
            if (res.status()) {
                user = res.user();
                System.out.println("Logged in: " + user);
                MainFrame.switchPage("main");
            }
        } else if (msg.getMessageType() == NetworkMessage.MessageType.SIGNUP_RESPONSE) {
            SignupResponse res = (SignupResponse) msg;
            if (res.status()) {
                user = res.user();
                System.out.println("Logged in: " + user);
                MainFrame.switchPage("main");
            }
        } else {
            user = null;
        }
    }
}
