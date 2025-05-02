package com.prismix.server.core;

import com.prismix.common.model.User;
import com.prismix.common.model.network.NetworkMessage;
import com.prismix.server.data.repository.UserRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final int PORT = 8008;
    private final UserHandler userHandler;

    public Server() throws IOException {
        userHandler = new UserHandler(new UserRepository());
        ServerSocket ss = new ServerSocket(PORT);
        while (true) {
            Socket socket = ss.accept();
            System.out.println("Accepted connection from " + socket.getRemoteSocketAddress());
            new Thread(new ClientHandler(socket, this)).start();
        }
    }

    protected void processMessage(NetworkMessage msg, ClientHandler clientHandler) throws IOException {
        switch (msg.getMessageType()) {
            case LOGIN_REQUEST, LOGIN_RESPONSE, SIGNUP_REQUEST, SIGNUP_RESPONSE ->
                    userHandler.handleMessage(msg, clientHandler);
        };
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
