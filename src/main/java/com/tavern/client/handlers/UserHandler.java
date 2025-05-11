package com.tavern.client.handlers;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.gui.screens.MainFrame;
import com.tavern.client.repositories.MessageRepository;
import com.tavern.client.utils.ConnectionManager;
import com.tavern.client.core.EventBus;
import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class UserHandler implements ResponseHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
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
            logger.error("Unable to send GetUsersInfoRequest: {}", e.getMessage(), e);
        }
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void login(String username) {
        ConnectionManager manager = ConnectionManager.getInstance();
        try {
            manager.sendMessage(new LoginRequest(username));
        } catch (IOException e) {
            logger.error("Failed to send login request: {}", e.getMessage(), e);
        }
    }

    public void signup(String username, String displayName, byte[] avatar) {
        ConnectionManager manager = ConnectionManager.getInstance();
        try {
            manager.sendMessage(new SignupRequest(username, displayName, avatar));
        } catch (IOException e) {
            logger.error("Failed to send signup request: {}", e.getMessage(), e);
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
                } else {
                    eventBus.publish(new ApplicationEvent(
                            ApplicationEvent.Type.AUTH_ERROR,
                            res.errorMessage()
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
                } else {
                    eventBus.publish(new ApplicationEvent(
                            ApplicationEvent.Type.AUTH_ERROR,
                            res.errorMessage()
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
