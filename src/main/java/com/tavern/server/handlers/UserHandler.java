package com.tavern.server.handlers;

import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import com.tavern.server.core.ClientHandler;
import com.tavern.server.core.RequestHandler;
import com.tavern.server.data.manager.UserManager;
import com.tavern.server.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class UserHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(UserHandler.class);
    private final HashMap<User, ClientHandler> activeUsers;

    public UserHandler(HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.activeUsers = new HashMap<>();
        requestHandlers.put(NetworkMessage.MessageType.LOGIN_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.SIGNUP_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_ROOM_USERS_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_USERS_INFO_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.GET_ALL_USERS_REQUEST, this);
    }

    public HashMap<User, ClientHandler> getActiveUsers() {
        return activeUsers;
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler clientHandler) {
        switch (message.getMessageType()) {
            case SIGNUP_REQUEST -> {
                SignupRequest msg = (SignupRequest) message;
                User user = UserManager.registerUser(msg.username(), msg.displayName(), msg.avatar());
                SignupResponse response;
                if (user == null) {
                    response = new SignupResponse(false, "Invalid", null);
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
            case GET_USERS_INFO_REQUEST -> {
                GetUsersInfoRequest msg = (GetUsersInfoRequest) message;
                List<User> users = UserRepository.getUsersById(msg.userIds());
                clientHandler.sendMessage(new GetUsersInfoResponse(users));
            }
            case GET_ALL_USERS_REQUEST -> {
                try {
                    List<User> allUsers = UserRepository.getAllUsers();
                    clientHandler.sendMessage(new GetAllUsersResponse(allUsers));
                } catch (Exception e) {
                    logger.error("Error getting all users", e);
                    clientHandler.sendMessage(new GetAllUsersResponse(new ArrayList<>()));
                }
            }
        }
    }
}
