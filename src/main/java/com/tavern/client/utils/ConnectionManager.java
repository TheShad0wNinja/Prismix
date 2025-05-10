package com.tavern.client.utils;

import com.tavern.common.model.network.NetworkMessage;

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
    private static final String TRUSTSTORE_PATH = "client.truststore";
    private static final String TRUSTSTORE_PASSWORD = "tavern";
    private static final String SERVER_HOST = "prismix.zapto.org";
    private static final int SERVER_PORT = 9001;

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
            System.out.println("Starting secure connection...");
            
            // Load truststore
            KeyStore trustStore = KeyStore.getInstance("JKS");
            trustStore.load(new FileInputStream(TRUSTSTORE_PATH), TRUSTSTORE_PASSWORD.toCharArray());
            
            // Create trust manager factory
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(trustStore);
            
            // Create SSL context
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            
            // Create socket factory
            SSLSocketFactory sf = sslContext.getSocketFactory();
            socket = (SSLSocket) sf.createSocket(SERVER_HOST, SERVER_PORT);
            
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
