package com.prismix.client.core;

import com.prismix.common.model.network.NetworkMessage;

import java.io.IOException;

public class MessageListener implements Runnable {
    Client client;

    public MessageListener(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        ConnectionManager manager = ConnectionManager.getInstance();
        System.out.println("Listing to messages");

        while (manager.isConnected()) {
            System.out.println(manager);
            try {
                NetworkMessage msg = manager.receiveMessage();
                System.out.println("Received message: " + msg);
                new Thread(() -> client.processMessage(msg)).start();
            } catch (IOException | ClassNotFoundException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
