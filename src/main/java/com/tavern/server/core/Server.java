package com.tavern.server.core;

import com.tavern.common.model.network.NetworkMessage;
import com.tavern.common.utils.AppDataManager;
import com.tavern.common.utils.PropertyFileLoader;
import com.tavern.server.handlers.UserHandler;
import com.tavern.server.handlers.MessageHandler;
import com.tavern.server.handlers.RoomHandler;
import com.tavern.server.handlers.VideoChatHandler;
import com.tavern.server.handlers.FileTransferHandler;
import com.tavern.server.utils.ServerDatabaseManager;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.security.KeyStore;
import java.sql.Connection;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server {
    private static final Logger logger = Logger.getLogger(Server.class.getName());
    private final int port;
    private final UserHandler userHandler;
    private final HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers;
    private final AtomicBoolean isRunning = new AtomicBoolean(true);
    public static PropertyFileLoader properties = new PropertyFileLoader("server/server.properties");
    private SSLServerSocket serverSocket;

    // SSL configuration
    private final String keystorePath;
    private final String keystorePassword;

    public Server() throws IOException {
        keystorePath = properties.getProperty("ssl.path");
        keystorePassword = properties.getProperty("ssl.password");
        port = Integer.parseInt(properties.getProperty("server.port", "0"));

        requestHandlers = new HashMap<>();
        this.userHandler = new UserHandler(requestHandlers);
        new RoomHandler(requestHandlers);
        new MessageHandler(userHandler, requestHandlers);
        new VideoChatHandler(userHandler, requestHandlers);
        new FileTransferHandler(requestHandlers);

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        initializeServer();
    }

    private void initializeServer() throws IOException {
        try {
            // Load keystore using AppDataManager
            KeyStore keyStore = KeyStore.getInstance("JKS");
            try (InputStream keystoreStream = AppDataManager.loadFile(keystorePath, getClass())) {
                keyStore.load(keystoreStream, keystorePassword.toCharArray());
                logger.info("Successfully loaded keystore from: " + keystorePath);
            }

            // Create key manager factory
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, keystorePassword.toCharArray());

            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(kmf.getKeyManagers(), null, null);

            // Create server socket factory
            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            serverSocket = (SSLServerSocket) ssf.createServerSocket(port);

            // Require client authentication (optional, set to false if not required)
            serverSocket.setNeedClientAuth(false);

            logger.info("Secure server started on port " + port);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error initializing SSL", e);
            throw new IOException("Error initializing SSL", e);
        }

        try {
            while (isRunning.get() && !serverSocket.isClosed()) {
                Socket socket = serverSocket.accept();
                logger.info("Accepted secure connection from " + socket.getRemoteSocketAddress());
                new Thread(new ClientHandler(socket, this)).start();
            }
        } catch (IOException e) {
            if (isRunning.get()) { // Only log if not shutting down
                logger.log(Level.SEVERE, "Error accepting connections", e);
            }
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        if (!isRunning.getAndSet(false)) {
            return; // Already shutting down
        }

        logger.info("Server shutting down...");
        
        // Close all active client connections
        for (ClientHandler handler : userHandler.getActiveUsers().values()) {
            try {
                handler.close();
            } catch (Exception e) {
                logger.log(Level.WARNING, "Error closing client handler", e);
            }
        }

        // Close server socket
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                logger.log(Level.WARNING, "Error closing server socket", e);
            }
        }

        // Close any open database connections
        try {
            Connection conn = ServerDatabaseManager.getConnection();
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Error closing database connection", e);
        }

        logger.info("Server shutdown complete");
    }

    protected void processMessage(NetworkMessage msg, ClientHandler clientHandler) throws IOException {
        if (!requestHandlers.containsKey(msg.getMessageType()))
            return;
        RequestHandler handler = requestHandlers.get(msg.getMessageType());
        handler.handleRequest(msg, clientHandler);
    }

    public static void main(String[] args) {
        try {
            new Server();
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to start server", e);
            System.exit(1);
        }
    }
}
