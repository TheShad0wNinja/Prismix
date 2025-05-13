package com.tavern.client.handlers;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.gui.screens.MainFrame;
import com.tavern.client.repositories.MessageRepository;
import com.tavern.client.utils.ConnectionManager;
import com.tavern.client.core.EventBus;
import com.tavern.client.views.AppPage;
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

    private void handleAuthResponse(NetworkMessage message) {
        boolean status = switch (message.getMessageType()) {
            case LOGIN_RESPONSE -> ((LoginResponse) message).status();
            case SIGNUP_RESPONSE -> ((SignupResponse) message).status();
            default -> throw new IllegalStateException("Unexpected value: " + message.getMessageType());
        };
        this.user = switch (message.getMessageType()) {
            case LOGIN_RESPONSE ->  ((LoginResponse) message).user();
            case SIGNUP_RESPONSE ->  ((SignupResponse) message).user();
            default -> throw new IllegalStateException("Unexpected value: " + message.getMessageType());
        };

        if (status) {
            eventBus.publish(new ApplicationEvent(
                    ApplicationEvent.Type.USER_LOGGED_IN,
                    this.user
            ));
            eventBus.publish(new ApplicationEvent(
                    ApplicationEvent.Type.SWITCH_PAGE,
                    AppPage.MAIN
            ));
            return;
        }

        String errorMessage = switch (message.getMessageType()) {
            case LOGIN_RESPONSE -> ((LoginResponse) message).errorMessage();
            case SIGNUP_RESPONSE -> ((SignupResponse) message).errorMessage();
            default -> throw new IllegalStateException("Unexpected value: " + message.getMessageType());
        };

        eventBus.publish(new ApplicationEvent(
                ApplicationEvent.Type.AUTH_ERROR,
                errorMessage
        ));
    }

    @Override
    public void handleResponse(NetworkMessage message) {
        switch (message.getMessageType()) {
            case LOGIN_RESPONSE, SIGNUP_RESPONSE -> {
                handleAuthResponse(message);
            }

            case GET_USERS_INFO_RESPONSE -> {
                GetUsersInfoResponse response = (GetUsersInfoResponse) message;
                ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.DIRECT_USER_LIST_UPDATED, response.users()));
            }
        }
    }
}
