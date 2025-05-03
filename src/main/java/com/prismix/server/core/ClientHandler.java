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
    Server server;

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

    private void initSession() throws IOException {
        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());
    }

    private void startSession() {
        System.out.println("Starting session with: " + socket.getRemoteSocketAddress());
        while (socket.isConnected() && !socket.isClosed()) {
            try {
                NetworkMessage message = (NetworkMessage) in.readObject();
                System.out.println("Received message: " + message);
                server.processMessage(message, this);
            } catch (IOException | ClassNotFoundException e) {
                System.out.println("Error: " + e.getMessage());
                break;
            }
        }
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
