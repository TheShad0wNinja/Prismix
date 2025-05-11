package com.tavern.client.core;

import com.tavern.client.utils.ConnectionManager;
import com.tavern.common.model.network.NetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class MessageListener implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(MessageListener.class);
    Client client;

    public MessageListener(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        ConnectionManager manager = ConnectionManager.getInstance();
        while (manager.isConnected()) {
            try {
                NetworkMessage msg = manager.receiveMessage();
                logger.debug("Received message: {}", msg);
                new Thread(() -> client.processMessage(msg)).start();
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Error receiving message: {}", e.getMessage(), e);
            }
        }
        manager.close();
    }
}
