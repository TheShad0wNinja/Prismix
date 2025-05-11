package com.tavern.server.core;

import com.tavern.common.model.User;
import com.tavern.common.model.network.NetworkMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    
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
        logger.debug("Sending message: {}", msg);
        try {
            out.writeObject(msg);
            out.flush();
            return true;
        } catch (IOException e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
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
        logger.info("Starting session with: {}", socket.getRemoteSocketAddress());
        while (socket.isConnected() && !socket.isClosed()) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                logger.debug("Received message: {} {}", message, (user == null ? "" : "from: " + user.getUsername()));
                server.processMessage(message, this);
            } catch (IOException | ClassNotFoundException e) {
                logger.error("Error in session: {}", e.getMessage(), e);
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
            logger.error("Error initializing session for {}: {}", socket, e.getMessage(), e);
        }
    }

    public void close() {
        logger.info("Session closed for {}", user != null ? user.getUsername() : "unknown user");
        try {
            out.close();
            in.close();
            socket.close();
        } catch (IOException e) {
            logger.error("Error closing socket: {}", e.getMessage(), e);
        }
    }
}
