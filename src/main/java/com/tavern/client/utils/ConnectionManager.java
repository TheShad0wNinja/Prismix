package com.tavern.client.utils;

import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.network.NetworkMessage;
import com.tavern.common.utils.AppDataManager;
import com.tavern.common.utils.LogManager;
import com.tavern.common.utils.PropertyFileLoader;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.*;
import java.security.KeyStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(ConnectionManager.class);
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
        
        // Configure logging from properties
        LogManager.configureLogging(props);
        
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
            logger.info("Starting secure connection to {}:{}", serverHost, serverPort);
            
            // Load truststore using AppDataManager
            KeyStore trustStore = KeyStore.getInstance("JKS");
            try (InputStream trustStoreStream = AppDataManager.loadFile(trustStorePath, getClass())) {
                trustStore.load(trustStoreStream, trustStorePassword.toCharArray());
                logger.info("Successfully loaded truststore from: {}", trustStorePath);
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
            logger.info("Secure connection established");
        } catch (Exception e) {
            logger.error("Unable to connect to server: {}", e.getMessage(), e);
        }
    }

    public void sendMessage(NetworkMessage message) throws IOException {
        logger.debug("Sending message: {}", message);
        out.writeObject(message);
        out.flush();
    }

    public NetworkMessage receiveMessage() throws IOException, ClassNotFoundException {
        NetworkMessage message = (NetworkMessage) in.readObject();
        logger.debug("Received message: {}", message);
        return message;
    }

    public void close() {
        logger.info("Closing connection...");
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (socket != null) socket.close();
        } catch (IOException e) {
            logger.warn("Error closing connection", e);
        }
    }
}
