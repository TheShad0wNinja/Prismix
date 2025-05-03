package com.prismix.client.utils;

import com.prismix.common.model.network.NetworkMessage;

import java.io.*;
import java.net.Socket;

public class ConnectionManager {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Socket socket;
    private static ConnectionManager instance;

    private ConnectionManager() {
        startConnection();
    }

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
       return instance;
    }

    public boolean isConnected() {
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    private void startConnection() {
        try {
            System.out.println("Starting connection...");
            socket = new Socket("localhost", 8008);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Connection Established");
        } catch (IOException e) {
            System.out.println("Unable to connect to server");
        }
    }

    public void sendMessage(NetworkMessage message) throws IOException {
        out.writeObject(message);
        out.flush();
    }

    public NetworkMessage receiveMessage() throws IOException, ClassNotFoundException {
        return (NetworkMessage) in.readObject();
    }
}
