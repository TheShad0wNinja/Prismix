package com.prismix.server.core;

import com.prismix.common.model.User;
import com.prismix.common.model.network.NetworkMessage;

import java.io.*;
import java.net.Socket;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ClientHandler implements Runnable {
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    UserHandler userHandler;

    public ClientHandler(Socket socket, UserHandler userHandler) {
        this.socket = socket;
        this.userHandler = userHandler;
    }

    private void initSession() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void startSession() {
        System.out.println("Starting session with: " + socket.getRemoteSocketAddress());
        while (true) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                System.out.println("Received message: " + message);
                handleMessage(message);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
                throw new RuntimeException(e);
            }
        }
    }

    private void handleMessage(NetworkMessage message) throws IOException {
        NetworkMessage response = switch (message.getMessageType()) {
            case LOGIN_REQUEST, LOGIN_RESPONSE, SIGNUP_REQUEST, SIGNUP_RESPONSE ->
                    userHandler.handleMessage(message);
            default -> null;
        };
        System.out.println("Response: " + response);
        out.writeObject(response);
    }

    @Override
    public void run() {
        try {
            initSession();
            startSession();
        } catch (IOException e) {
            System.out.println("Error initializing session for " + socket);
        }
    }
}
