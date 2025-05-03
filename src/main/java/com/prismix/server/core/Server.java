package com.prismix.server.core;

import com.prismix.common.model.network.NetworkMessage;
import com.prismix.server.utils.ServerDatabaseManager;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
    private final int PORT = 6969;
    private final AuthHandler userHandler;
    private final HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers;

    public Server() throws IOException {
        requestHandlers = new HashMap<>();
        this.userHandler = new AuthHandler(requestHandlers);
        new RoomHandler(requestHandlers);
        new MessageHandler(userHandler, requestHandlers);

        ServerSocket ss = new ServerSocket(PORT);
        while (!ss.isClosed()) {
            Socket socket = ss.accept();
            System.out.println("Accepted connection from " + socket.getRemoteSocketAddress());
            new Thread(new ClientHandler(socket, this)).start();
        }

        ss.close();
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
