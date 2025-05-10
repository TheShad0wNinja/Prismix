package com.tavern.client.core;

import com.tavern.client.utils.ConnectionManager;
import com.tavern.common.model.network.NetworkMessage;

import java.io.IOException;

public class MessageListener implements Runnable {
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
                System.out.println("Received message: " + msg);
                new Thread(() -> client.processMessage(msg)).start();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
        manager.close();
    }
}
