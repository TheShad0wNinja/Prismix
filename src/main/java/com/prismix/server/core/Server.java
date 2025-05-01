package com.prismix.server.core;

import com.prismix.common.model.User;
import com.prismix.server.data.repository.UserRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final int PORT = 8008;

    public Server() throws IOException {
        UserHandler userHandler = new UserHandler(new UserRepository());
        ServerSocket ss = new ServerSocket(PORT);
        while (true) {
            Socket socket = ss.accept();
            System.out.println("Accepted connection from " + socket.getRemoteSocketAddress());
            new Thread(new ClientHandler(socket, userHandler)).start();
        }
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
