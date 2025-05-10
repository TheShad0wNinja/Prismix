package com.tavern.server.core;

import com.tavern.common.model.User;
import com.tavern.common.model.network.NetworkMessage;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    Socket socket;
    ObjectOutputStream out;
    ObjectInputStream in;
    Server server;
    User user;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
    }

    public boolean sendMessage(NetworkMessage msg) {
        System.out.println("Sending message: " + msg);
        try {
            out.writeObject(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            System.out.println("Error sending message: " + e.getMessage());
            return false;
        }
    }

    public boolean isConnected() {
        return socket.isConnected() && !socket.isClosed();
    }

    private void initSession() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void startSession() {
        System.out.println("Starting session with: " + socket.getRemoteSocketAddress());
        while (socket.isConnected() && !socket.isClosed()) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                System.out.printf("Received message: %s %s\n", message, (user == null ? "" : "from: " + user.getUsername()));
                server.processMessage(message, this);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
                break;
            }
        }
        close();
    }

    public void setUser(User user) {
        this.user = user;
    }

    public User getUser() {
        return this.user;
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

    public void close() {
        System.out.println("Session closed");
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing socket: " + e.getMessage());
        }
    }
}
