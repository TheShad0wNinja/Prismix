package com.tavern.client.utils;

import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.network.NetworkMessage;
import com.tavern.common.utils.AppDataManager;
import com.tavern.common.utils.PropertyFileLoader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConnectionManager {
    private static final Logger logger = Logger.getLogger(ConnectionManager.class.getName());
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SSLSocket socket;
    private static ConnectionManager instance;
    
    // SSL configuration
    private final String trustStorePath;
    private final String trustStorePassword;
    private final String serverHost;
    private final int serverPort;

    private ConnectionManager() {
        PropertyFileLoader props = ApplicationContext.getProperties();
        serverHost = props.getProperty("server.host", "");
        serverPort = Integer.parseInt(props.getProperty("server.port",  "0"));
        trustStorePath = props.getProperty("ssl.path");
        trustStorePassword = props.getProperty("ssl.password");

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
            System.out.println("Starting secure connection...");
            
            // Load truststore using AppDataManager
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (InputStream trustStoreStream = AppDataManager.loadFile(trustStorePath, getClass())) {
                trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
                logger.info("Successfully loaded truststore from: " + trustStorePath);
            }
            
            // Create trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            
            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            
            // Create socket factory
            SSLSocketFactory sf = sslContext.getSocketFactory();
            socket = (SSLSocket) sf.createSocket(serverHost, serverPort);
            
            // Start handshake
            socket.startHandshake();
            
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            System.out.println("Secure connection established");
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unable to connect to server", e);
            System.out.println("Unable to connect to server: " + e.getMessage());
        }
    }

    public void sendMessage(NetworkMessage message) throws IOException {
        System.out.println("Sending message: " + message);
        out.writeObject(message);
        out.flush();
    }

    public NetworkMessage receiveMessage() throws IOException, ClassNotFoundException {
        return (NetworkMessage) in.readObject();
    }

    public void close() {
        System.out.println("Closing connection...");
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.log(Level.WARNING, "Error closing connection", e);
        }
    }
}
