package com.prismix.server.core;

import com.prismix.common.model.network.NetworkMessage;
import com.prismix.server.data.manager.RoomManager;
import com.prismix.server.data.manager.UserManager;
import com.prismix.server.data.repository.UserRepository;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final int PORT = 8008;
    private final AuthHandler userHandler;
    private final HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers;

    public Server() throws IOException {
        requestHandlers = new HashMap<>();
        new AuthHandler(requestHandlers);
        new RoomHandler(requestHandlers);

        ServerSocket ss = new ServerSocket(PORT);
        while (true) {
            Socket socket = ss.accept();
            System.out.println("Accepted connection from " + socket.getRemoteSocketAddress());
            new Thread(new ClientHandler(socket, this)).start();
        }
    }

    protected void processMessage(NetworkMessage msg, ClientHandler clientHandler) throws IOException {
        if (!requestHandlers.containsKey(msg.getMessageType()))
            return;
        RequestHandler handler = requestHandlers.get(msg.getMessageType());
        handler.handleRequest(msg, clientHandler);
    }

    public static void main(String[] args) throws IOException {
        new Server();
    }
}
