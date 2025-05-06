package com.prismix.client.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.gui.screens.MainFrame;
import com.prismix.client.repositories.MessageRepository;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.client.core.EventBus;
import com.prismix.common.model.User;
import com.prismix.common.model.network.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UserHandler implements ResponseHandler {
    private final EventBus eventBus;
    private User user;

    public UserHandler(EventBus eventBus, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandlers) {
        this.eventBus = eventBus;
        responseHandlers.put(NetworkMessage.MessageType.LOGIN_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.SIGNUP_RESPONSE, this);
        responseHandlers.put(NetworkMessage.MessageType.GET_USERS_INFO_RESPONSE, this);
    }

    public void updateDirectUsers() {
        Set<Integer> userIds = MessageRepository.getUserDirectContacts(ApplicationContext.getUserHandler().getUser().getId());
        try {
            ConnectionManager.getInstance().sendMessage(new GetUsersInfoRequest(new ArrayList<>(userIds)));
        } catch (IOException e) {
            System.out.println("Unable to send GetUsersInfoRequest: " + e.getMessage());
        }
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

    @Override
    public void handleResponse(NetworkMessage message) {
        switch (message.getMessageType()) {
            case LOGIN_RESPONSE -> {
                LoginResponse res = (LoginResponse) message;
                if (res.status()) {
                    user = res.user();
                    eventBus.publish(new ApplicationEvent(
                            ApplicationEvent.Type.USER_LOGGED_IN,
                            user
                    ));
                    eventBus.publish(new ApplicationEvent(
                            ApplicationEvent.Type.SWITCH_SCREEN,
                            MainFrame.AppScreen.CHAT_SCREEN
                    ));
                }
            }
            case SIGNUP_RESPONSE -> {
                SignupResponse res = (SignupResponse) message;
                if (res.status()) {
                    user = res.user();
                    eventBus.publish(new ApplicationEvent(
                            ApplicationEvent.Type.USER_LOGGED_IN,
                            user
                    ));
                    eventBus.publish(new ApplicationEvent(
                            ApplicationEvent.Type.SWITCH_SCREEN,
                            MainFrame.AppScreen.CHAT_SCREEN
                    ));
                }
            }
            case GET_USERS_INFO_RESPONSE -> {
                GetUsersInfoResponse response = (GetUsersInfoResponse) message;
                ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.DIRECT_USER_LIST_UPDATED, response.users()));
            }
        }
    }
}
