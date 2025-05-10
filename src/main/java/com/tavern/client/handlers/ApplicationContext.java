package com.tavern.client.handlers;

import com.tavern.client.core.Client;
import com.tavern.client.core.EventBus;
import com.tavern.common.model.network.NetworkMessage;

import java.util.HashMap;

public class ApplicationContext {
    private static ApplicationContext instance;

    private final Client client;
    private final UserHandler userHandler;
    private final RoomHandler roomHandler;
    private final MessageHandler messageHandler;
    private final VideoChatHandler videoChatHandler;
    private final FileTransferHandler fileTransferHandler;
    private final EventBus eventBus;
    private final HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandlers;

    private ApplicationContext() {
        responseHandlers = new HashMap<>();
        this.client = new Client(responseHandlers);
        this.eventBus = new EventBus();
        this.userHandler = new UserHandler(eventBus, responseHandlers);
        this.roomHandler = new RoomHandler(eventBus, userHandler, responseHandlers);
        this.messageHandler = new MessageHandler(eventBus, userHandler, responseHandlers);
        this.videoChatHandler = new VideoChatHandler(eventBus, userHandler, responseHandlers);
        this.fileTransferHandler = new FileTransferHandler(eventBus, userHandler, responseHandlers);
    }

    private static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public static HashMap<NetworkMessage.MessageType, ResponseHandler> getResponseHandlers() {
        return getInstance().responseHandlers;
    }

    public static Client getClient() {
        return getInstance().client;
    }

    public static EventBus getEventBus() {
        return getInstance().eventBus;
    }

    public static UserHandler getUserHandler() {
        return getInstance().userHandler;
    }

    public static MessageHandler getMessageHandler() {
        return getInstance().messageHandler;
    }

    public static RoomHandler getRoomHandler() {
        return getInstance().roomHandler;
    }

    public static VideoChatHandler getVideoChatHandler() {
        return getInstance().videoChatHandler;
    }

    public static FileTransferHandler getFileTransferHandler() {
        return getInstance().fileTransferHandler;
    }
}
