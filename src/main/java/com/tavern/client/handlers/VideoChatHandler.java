package com.tavern.client.handlers;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventBus;
import com.tavern.client.core.EventListener;
import com.tavern.client.utils.ConnectionManager;
import com.tavern.client.utils.SdpUtils;
import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.*;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.ice.harvest.TurnCandidateHarvester;
import org.ice4j.security.LongTermCredential;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import com.github.sarxos.webcam.Webcam;
import org.ice4j.ice.Component;
import org.ice4j.socket.IceSocketWrapper;
import org.ice4j.socket.SocketClosedException;

public class VideoChatHandler implements ResponseHandler, EventListener {
    private static final Logger logger = LoggerFactory.getLogger(VideoChatHandler.class);
    private static final int NUMBER_OF_STUN_SERVERS_TO_USE = 30;
    private final EventBus eventBus;
    private final UserHandler userHandler;
    private IceMediaStream videoStream;
    private Agent iceAgent;

    private JFrame videoFrame;
    private JPanel localVideoPanel;
    private JPanel remoteVideoPanel;
    private JButton hangupButton;

    private Webcam webcam;
    private Thread captureThread;
    private final AtomicBoolean capturing = new AtomicBoolean(false);
    private final AtomicBoolean inCall = new AtomicBoolean(false);

    private BufferedImage lastCapturedImage;
    private BufferedImage lastReceivedImage;

    private User currentCallPartner;
    private final ConcurrentHashMap<User, Boolean> pendingCalls = new ConcurrentHashMap<>();
    private IceSocketWrapper iceSocket;
    private int calleePort;
    private InetAddress calleeAddress;
    private long startTime = System.currentTimeMillis();

    private static final int MAX_PACKET_SIZE = 1200; // Lower to avoid fragmentation
    private static final String HEADER = "IMG";
    private static final String TRAILER = "END";
    private final ByteArrayOutputStream receivedDataBuffer = new ByteArrayOutputStream();
    private boolean receivingImage = false;
    private int frameCounter = 0;
    private long lastSuccessfulTransmission = 0;
    private float compressionQuality = 0.5f; // Start with medium quality (0.0-1.0)
    private static final long ADAPTIVE_QUALITY_INTERVAL = 5000; // Check every 5 seconds
    private static final int TARGET_FRAME_RATE = 5; // Frames per second
    private static final long FRAME_INTERVAL = 1000 / TARGET_FRAME_RATE; // Milliseconds between frames

    static {
        Webcam.setDriver(new NativeDriver());
    }

    // Flag to track if we're currently initializing the call window
    private final AtomicBoolean initializingCallWindow = new AtomicBoolean(false);

    public VideoChatHandler(EventBus eventBus, UserHandler userHandler, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandler) {
        this.eventBus = eventBus;
        this.userHandler = userHandler;

        // Register for video chat message types
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_REQUEST, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_RESPONSE, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_OFFER, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_ANSWER, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_END, this);

        eventBus.subscribe(this);
    }

    @Override
    public void handleResponse(NetworkMessage message) {
        logger.debug("Handling response type: {}", message.getMessageType());
        switch (message.getMessageType()) {
            case VIDEO_CALL_REQUEST -> {
                VideoCallRequest request = (VideoCallRequest) message;
                handleIncomingCallRequest(request);
            }
            case VIDEO_CALL_RESPONSE -> {
                VideoCallResponse response = (VideoCallResponse) message;
                handleCallResponse(response);
            }
            case VIDEO_CALL_OFFER -> {
                VideoCallOffer offer = (VideoCallOffer) message;
                handleCallOffer(offer);
            }
            case VIDEO_CALL_ANSWER -> {
                VideoCallAnswer answer = (VideoCallAnswer) message;
                handleCallAnswer(answer);
            }
//            case VIDEO_ICE_CANDIDATE -> {
//                VideoIceCandidate candidate = (VideoIceCandidate) message;
//                handleIceCandidate(candidate);
//            }
            case VIDEO_CALL_END -> {
                VideoCallEnd end = (VideoCallEnd) message;
                logger.debug("Received VIDEO_CALL_END message");
                handleCallEnd(end);
            }
        }
    }

    private void handleIncomingCallRequest(VideoCallRequest request) {
        SwingUtilities.invokeLater(() -> {
            int choice = JOptionPane.showConfirmDialog(null,
                    "Incoming video call from " + request.caller().getUsername() + ". Accept?",
                    "Incoming Call",
                    JOptionPane.YES_NO_OPTION);

            boolean accepted = (choice == JOptionPane.YES_OPTION);
            try {
                ConnectionManager.getInstance().sendMessage(
                        new VideoCallResponse(request.caller(), request.callee(), accepted));

                if (accepted) {
                    currentCallPartner = request.caller();
                    inCall.set(true);
                    logger.debug("Current call partner: {}", currentCallPartner);
                    
                    // Initialize UI - happens first
                    initializeCallWindow();
                }
            } catch (IOException e) {
                logger.error("Error responding to call request: {}", e.getMessage(), e);
            }
        });
    }

    private void initializeCallWindow() {
        // Prevent multiple simultaneous initialization attempts
        if (!initializingCallWindow.compareAndSet(false, true)) {
            logger.debug("Call window initialization already in progress");
            return;
        }
        
        if (videoFrame != null && videoFrame.isVisible()) {
            logger.debug("Call window already initialized and visible");
            initializingCallWindow.set(false);
            return;
        }
        
        // Dispose any existing frame before creating a new one
        if (videoFrame != null) {
            SwingUtilities.invokeLater(() -> {
                videoFrame.dispose();
                videoFrame = null;
                createCallWindow();
            });
        } else {
            SwingUtilities.invokeLater(this::createCallWindow);
        }
    }
    
    private void createCallWindow() {
        try {
            logger.debug("Creating call window for partner: {}", 
                (currentCallPartner != null ? currentCallPartner.getUsername() : "unknown"));
                
            videoFrame = new JFrame("Video Call with " + currentCallPartner.getUsername());
            videoFrame.setSize(800, 600);
            videoFrame.setLocationRelativeTo(null); // Center on screen
            videoFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
            videoFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    if (inCall.get()) {
                        sendHangupMessage();
                    }
                    cleanupCall();
                }
            });

            // Set up video panels with a custom repaint manager
            localVideoPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (lastCapturedImage != null) {
                        g.drawImage(lastCapturedImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // Draw placeholder if no image
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Color.WHITE);
                        g.drawString("Local video initializing...", 20, getHeight()/2);
                    }
                }
                
                @Override
                public void repaint() {
                    super.repaint();
                    // Force immediate repaint for smoother video
                    paintImmediately(0, 0, getWidth(), getHeight());
                }
            };
            localVideoPanel.setBorder(BorderFactory.createTitledBorder("Local Video"));
            
            remoteVideoPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    if (lastReceivedImage != null) {
                        g.drawImage(lastReceivedImage, 0, 0, getWidth(), getHeight(), this);
                    } else {
                        // Draw placeholder if no image
                        g.setColor(Color.DARK_GRAY);
                        g.fillRect(0, 0, getWidth(), getHeight());
                        g.setColor(Color.WHITE);
                        g.drawString("Waiting for remote video...", 20, getHeight()/2);
                    }
                }
                
                @Override
                public void repaint() {
                    super.repaint();
                    // Force immediate repaint for smoother video
                    paintImmediately(0, 0, getWidth(), getHeight());
                }
            };
            remoteVideoPanel.setBorder(BorderFactory.createTitledBorder("Remote Video"));

            // Create UI with proper layout
            JPanel videoPanel = new JPanel(new GridLayout(1, 2, 5, 0));
            videoPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            videoPanel.add(localVideoPanel);
            videoPanel.add(remoteVideoPanel);

            // Set up control panel
            JPanel controlPanel = new JPanel();
            hangupButton = new JButton("Hang Up");
            hangupButton.addActionListener(e -> hangupCall());
            controlPanel.add(hangupButton);

            videoFrame.add(videoPanel, BorderLayout.CENTER);
            videoFrame.add(controlPanel, BorderLayout.SOUTH);
            
            // Make sure we're visible with focus - this is key to prevent flickering
            videoFrame.setVisible(true);
            videoFrame.toFront();
            videoFrame.requestFocus();
            
            // Create a robust timer to ensure the window stays visible and refreshes properly
            Timer uiRefreshTimer = new Timer(100, e -> {
                // Revalidate and repaint the window if not visible
                if (videoFrame != null && !videoFrame.isVisible()) {
                    logger.warn("Video frame not visible, making visible again");
                    videoFrame.setVisible(true);
                    videoFrame.toFront();
                }
                
                // Refresh the video panels
                if (localVideoPanel != null) localVideoPanel.repaint();
                if (remoteVideoPanel != null) remoteVideoPanel.repaint();
            });
            uiRefreshTimer.setInitialDelay(500); // Wait before starting
            uiRefreshTimer.start();

            // Start video capture
            startCapture();
            
            // Log success
            logger.debug("Call window creation successful");
        } catch (Exception e) {
            logger.error("ERROR creating call window: {}", e.getMessage(), e);
        } finally {
            initializingCallWindow.set(false);
        }
    }

    private void handleCallResponse(VideoCallResponse response) {
        if (response.accepted()) {
            currentCallPartner = response.callee();
            inCall.set(true);
            
            // First initialize UI
            initializeCallWindow();

            // Then start ICE negotiation
            try {
                String sdpOffer = initializeIceAgent();
                ConnectionManager.getInstance().sendMessage(
                        new VideoCallOffer(response.caller(), response.callee(), sdpOffer));
            } catch (Throwable e) {
                logger.error("Error sending call offer: {}", e.getMessage(), e);
            }
        } else {
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(null,
                        response.callee().getUsername() + " declined your call.",
                        "Call Declined",
                        JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    private String initializeIceAgent() throws Throwable {
        // Create ICE Agent
        iceAgent = new Agent();

        configureStunServers(iceAgent);

        // Add STUN server
//        iceAgent.addCandidateHarvester(new StunCandidateHarvester(
//                new TransportAddress(STUN_SERVER, STUN_PORT, Transport.UDP)));
//        System.out.println("Added STUN harvester: " + STUN_SERVER + ":" + STUN_PORT);
                
        // Add TURN servers
        List<TurnServer> turnServers = fetchTurnServers();
        for (TurnServer turnServer : turnServers) {
            TransportAddress turnAddress = new TransportAddress(
                turnServer.host,
                turnServer.port,
                Transport.UDP);

            LongTermCredential credential = new LongTermCredential(
                turnServer.username,
                turnServer.credential);

            TurnCandidateHarvester turnHarvester = new TurnCandidateHarvester(turnAddress, credential);
            iceAgent.addCandidateHarvester(turnHarvester);
            logger.info("Added TURN harvester: {}", turnServer.host + ":" + turnServer.port);
        }

        // Create media stream
        videoStream = iceAgent.createMediaStream("video");

        // Create component for video
        iceAgent.createComponent(videoStream, 13000, 10000, 15000);

        iceAgent.addStateChangeListener(new StateListener());
        return SdpUtils.createSDPDescription(iceAgent);
    }

    private void sendMessage(String msg) {
        if (iceSocket == null) {
            return;
        }

        byte[] messageBytes = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(messageBytes, messageBytes.length);
        packet.setAddress(calleeAddress);
        packet.setPort(calleePort);
        try {
            iceSocket.send(packet);
            logger.debug("Sent message: {}", msg);
        } catch (IOException e) {
            logger.warn("Unable to send message: {} : {}", msg, e.getMessage());
        }
    }

    private void handleCallOffer(VideoCallOffer offer) {
        try {
            String sdp = initializeIceAgent();
            SdpUtils.parseSDP(iceAgent, offer.sdpOffer());
            iceAgent.startConnectivityEstablishment();
            ConnectionManager.getInstance().sendMessage(new VideoCallAnswer(offer.caller(), offer.callee(), sdp));
        } catch (Throwable e) {
            logger.warn("Error sending call offer: {}", e.getMessage());
        }
    }


    private void handleCallAnswer(VideoCallAnswer answer) {
        try {
            SdpUtils.parseSDP(iceAgent, answer.sdpAnswer());
            iceAgent.startConnectivityEstablishment();
        } catch (Exception e) {
            logger.warn("Unable to handle call answer: {}", e.getMessage());
        }
    }

    private class StateListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() instanceof Agent){
                Agent agent = (Agent) evt.getSource();
                if(agent.getState().equals(IceProcessingState.TERMINATED)) {
                    logger.info("Agent terminated");
                    // Your agent is connected. Terminated means ready to communicate
                    for (IceMediaStream stream: agent.getStreams()) {
                        if (stream.getName().contains("video")) {
                            Component rtpComponent = stream.getComponent(Component.RTP);
                            CandidatePair rtpPair = rtpComponent.getSelectedPair();
                            // We use IceSocketWrapper, but you can just use the UDP socket
                            // The advantage is that you can change the protocol from UDP to TCP easily
                            // Currently only UDP exists so you might not need to use the wrapper.
                            iceSocket  = rtpPair.getIceSocketWrapper();
                            // Get information about remote address for packet settings
                            TransportAddress ta = rtpPair.getRemoteCandidate().getTransportAddress();
                            calleeAddress = ta.getAddress();
                            calleePort = ta.getPort();
                        }
                    }
                    logger.info("CalleeAddress: {}", calleeAddress.getHostAddress());
                    logger.info("CalleePort: {}", calleePort);
                    startCapture();
                    startReceiver();
                }
            }
        }
    }
    private void handleCallEnd(VideoCallEnd end) {
        logger.debug("handleCallEnd entered. Sender: {}, InCall: {}", end.sender().getUsername(), inCall.get());
        if (inCall.get() && currentCallPartner != null &&
                (currentCallPartner.getId() == end.sender().getId() || currentCallPartner.getId() == end.receiver().getId())) {
            // Check if we are either the sender or receiver mentioned in the end message
            SwingUtilities.invokeLater(() -> {
                if (videoFrame != null) { // Ensure frame exists before showing message
//                    System.out.println("DEBUG: Showing call ended dialog.");
                    JOptionPane.showMessageDialog(videoFrame, // Show relative to video frame
                            end.sender().getUsername() + " ended the call.",
                            "Call Ended",
                            JOptionPane.INFORMATION_MESSAGE);
                }
//                System.out.println("DEBUG: Calling cleanupCall from handleCallEnd.");
                cleanupCall(); // Clean up local resources immediately
            });
        } else {
//            System.out.println("DEBUG: handleCallEnd ignored - conditions not met (inCall=" + inCall.get() + ", partner=" + (currentCallPartner != null ? currentCallPartner.getUsername() : "null") + ")");
        }
    }

    public void initiateCall(User callee) {
        try {
            ConnectionManager.getInstance().sendMessage(
                    new VideoCallRequest(userHandler.getUser(), callee));
            pendingCalls.put(callee, true);
        } catch (IOException e) {
            logger.error("Error initiating call: {}", e.getMessage(), e);
        }
    }


    private void startCapture() {
        capturing.set(true);
        logger.debug("Starting video capture thread.");
        
        // Initialize webcam
        try {
            webcam = Webcam.getDefault();
            if (webcam == null) {
                logger.error("No webcam detected!");
                lastCapturedImage = createTimestampImage("No webcam available");
                return;
            }
            
            // Get supported resolutions
            Dimension[] supportedSizes = webcam.getViewSizes();
            logger.info("Supported webcam resolutions:");
            Dimension selectedSize = null;
            
            // Look for 640x480 as preferred resolution
            for (Dimension size : supportedSizes) {
                logger.info("- {}x{}", size.width, size.height);
                if (size.width == 640 && size.height == 480) {
                    selectedSize = size;
                }
            }
            
            // If 640x480 not found, use the smallest available resolution
            if (selectedSize == null) {
                selectedSize = supportedSizes[0]; // Start with first size
                for (Dimension size : supportedSizes) {
                    if (size.width * size.height < selectedSize.width * selectedSize.height) {
                        selectedSize = size;
                    }
                }
            }
            
            logger.info("Selected webcam resolution: {}x{}", selectedSize.width, selectedSize.height);
            webcam.setViewSize(selectedSize);
            webcam.open();
            logger.info("Webcam opened: {}", webcam.getName());
        } catch (Exception e) {
            logger.error("Failed to initialize webcam: {}", e.getMessage(), e);
            e.printStackTrace();
            lastCapturedImage = createTimestampImage("Webcam error: " + e.getMessage());
            return;
        }

        captureThread = new Thread(() -> {
            long lastFrameTime = 0;
            
            while (capturing.get() && webcam != null) {
                try {
                    long now = System.currentTimeMillis();
                    long elapsed = now - lastFrameTime;
                    
                    // Maintain target frame rate
                    if (elapsed >= FRAME_INTERVAL) {
                        // Capture from webcam
                        if (webcam.isOpen()) {
                            lastCapturedImage = webcam.getImage();
                            
                            // If capture failed, create a fallback image
                            if (lastCapturedImage == null) {
                                lastCapturedImage = createTimestampImage("Camera capture failed");
                            }
                        }

                        // Update local video panel
                        if (localVideoPanel != null) {
                            localVideoPanel.repaint();
                        }

                        // Send frame to partner
                        sendVideoFrame(lastCapturedImage);
                        
                        lastFrameTime = now;
                    } else {
                        // Sleep until next frame is due
                        Thread.sleep(Math.max(1, FRAME_INTERVAL - elapsed));
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    capturing.set(false); // Ensure loop termination
                } catch (Exception e) {
                    logger.error("Error in video capture: {}", e.getMessage(), e);
                    // Exit loop if interrupted
                    if (Thread.currentThread().isInterrupted()) {
                        capturing.set(false);
                    }
                }
            }
            
            // Close webcam when done
            if (webcam != null && webcam.isOpen()) {
                webcam.close();
                logger.info("Webcam closed");
            }
            logger.info("Capturing finished.");
        });
        captureThread.start();
    }

    // Helper method to create the timestamp image
    private BufferedImage createTimestampImage(String timestamp) {
        //System.out.println("DEBUG: Creating timestamp image with time: " + timestamp);
        int width = 320;
        int height = 240;
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();

        // Black background
        g2d.setColor(Color.BLACK);
        g2d.fillRect(0, 0, width, height);

        // White text
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));

        String userText = "User: " + (userHandler.getUser() != null ? userHandler.getUser().getUsername() : "Unknown");
        String timeText = "Time: " + timestamp;

        // Center text
        FontMetrics fm = g2d.getFontMetrics();
        int userTextWidth = fm.stringWidth(userText);
        int timeTextWidth = fm.stringWidth(timeText);

        g2d.drawString(userText, (width - userTextWidth) / 2, height / 2 - 15);
        g2d.drawString(timeText, (width - timeTextWidth) / 2, height / 2 + 15);

        g2d.dispose();
        return image;
    }

    private void sendVideoFrame(BufferedImage image) {
        if (!inCall.get() || iceSocket == null) {
            return;
        }

        // Apply adaptive compression based on network conditions
        adaptCompressionQuality();

        try {
            // Scale down the image for better performance if needed
            BufferedImage scaledImage = scaleImage(image, 320, 240);
            
            // Convert image to byte array with compression
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            // Use compression quality settings with JPEG
            javax.imageio.ImageWriter jpgWriter = javax.imageio.ImageIO.getImageWritersByFormatName("jpg").next();
            javax.imageio.ImageWriteParam writeParam = jpgWriter.getDefaultWriteParam();
            writeParam.setCompressionMode(javax.imageio.ImageWriteParam.MODE_EXPLICIT);
            writeParam.setCompressionQuality(compressionQuality);
            
            javax.imageio.stream.MemoryCacheImageOutputStream outputStream = 
                new javax.imageio.stream.MemoryCacheImageOutputStream(baos);
            jpgWriter.setOutput(outputStream);
            jpgWriter.write(null, new javax.imageio.IIOImage(scaledImage, null, null), writeParam);
            jpgWriter.dispose();
            byte[] imageData = baos.toByteArray();
            
//            System.out.println("DEBUG: Sending image " + (++frameCounter) + " of size: " + imageData.length +
//                " bytes, quality: " + compressionQuality);
            
            // Create a frame ID to detect missing frames
            String frameId = String.format("%04d", frameCounter++ % 10000);
            byte[] frameHeader = (HEADER + frameId).getBytes();

            // Send start header with frame ID
            DatagramPacket headerPacket = new DatagramPacket(frameHeader, frameHeader.length);
            headerPacket.setPort(calleePort);
            headerPacket.setAddress(calleeAddress);
            iceSocket.send(headerPacket);
            
            // Send image in chunks
            int offset = 0;
            int chunkId = 0;
            int totalChunks = (int) Math.ceil(imageData.length / (double) MAX_PACKET_SIZE);
            
            while (offset < imageData.length) {
                int chunkSize = Math.min(MAX_PACKET_SIZE - 8, imageData.length - offset);
                byte[] chunk = new byte[chunkSize + 8]; // Add 8 bytes for header
                
                // Add chunk header (frame ID + chunk ID + total chunks)
                byte[] chunkHeader = String.format("%4s%02d%02d", frameId, chunkId, totalChunks).getBytes();
                System.arraycopy(chunkHeader, 0, chunk, 0, 8);
                System.arraycopy(imageData, offset, chunk, 8, chunkSize);
                
                DatagramPacket packet = new DatagramPacket(chunk, chunk.length);
                packet.setPort(calleePort);
                packet.setAddress(calleeAddress);
                iceSocket.send(packet);
                
                offset += chunkSize;
                chunkId++;
            }
            
            // Send end trailer with frame ID
            byte[] frameTrailer = (TRAILER + frameId).getBytes();
            DatagramPacket trailerPacket = new DatagramPacket(frameTrailer, frameTrailer.length);
            trailerPacket.setPort(calleePort);
            trailerPacket.setAddress(calleeAddress);
            iceSocket.send(trailerPacket);
            
            lastSuccessfulTransmission = System.currentTimeMillis();
            
        } catch (java.net.NoRouteToHostException e) {
            logger.warn("ERROR: No route to host: {} : {}", calleeAddress.getHostAddress(), calleePort);
            logger.warn("This usually means:");
            logger.warn("1. The destination IP address is unreachable from your network");
            logger.warn("2. A firewall is blocking outgoing UDP packets");
            logger.warn("3. The selected ICE candidate may be using a local/private address that's not routable outside your network");
            
            // Try to reconnect or restart ICE negotiation
            restartIceNegotiation();
            
            logger.error("No route to host", e);
        } catch (IOException e) {
            logger.warn("Error sending video frame", e);
            // Decrease quality on errors to reduce packet size
            compressionQuality = Math.max(0.1f, compressionQuality - 0.05f);
        }
    }
    
    private BufferedImage scaleImage(BufferedImage src, int targetWidth, int targetHeight) {
        // Don't scale if already at target size
        if (src.getWidth() == targetWidth && src.getHeight() == targetHeight) {
            return src;
        }
        
        // Scale the image
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, src.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(src, 0, 0, targetWidth, targetHeight, null);
        g.dispose();
        return resized;
    }
    
    private void adaptCompressionQuality() {
        long now = System.currentTimeMillis();
        if (now - lastSuccessfulTransmission > ADAPTIVE_QUALITY_INTERVAL) {
            // Increase quality slightly if we've been sending successfully
            compressionQuality = Math.min(0.8f, compressionQuality + 0.05f);
            logger.debug("Adjusted compression quality to: {}", compressionQuality);
            lastSuccessfulTransmission = now;
        }
    }

    private void receiveVideoFrame(byte[] imageData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            BufferedImage receivedImage = ImageIO.read(bais);
            
            if (receivedImage == null) {
                logger.warn("ERROR: Failed to decode received image (null)");
                return;
            }
            
            logger.debug("Received image successfully decoded. Size: {}x{}", 
                receivedImage.getWidth(), receivedImage.getHeight());
            
            // Update the image in the EDT
            final BufferedImage finalImage = receivedImage;
            SwingUtilities.invokeLater(() -> {
                lastReceivedImage = finalImage;
                
                if (remoteVideoPanel != null) {
                    remoteVideoPanel.repaint();
//                    System.out.println("DEBUG: Remote panel repainted");
                } else {
                    logger.warn("WARNING: Remote video panel is null, can't update UI");
                }
            });
        } catch (IOException e) {
            logger.error("Error receiving video frame: {}", e.getMessage(), e);
        }
    }

    private void startReceiver() {
        new Thread(() -> {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            String currentFrameId = null;
            Map<Integer, byte[]> chunks = new HashMap<>();
            int expectedChunks = 0;
            boolean hasReceivedValidFrame = false;
            
            while (inCall.get()) {
                try {
                    if (iceSocket != null) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        iceSocket.receive(packet);

                        byte[] data = Arrays.copyOf(packet.getData(), packet.getLength()); // Create a copy of the data
                        int length = data.length;
                        
                        if (length < 7) continue; // Skip packets smaller than smallest possble item
                        
                        String packetStart = new String(data, 0, 7);
                        logger.debug("PacketStart: {}", packetStart);
                        
                        // Check if it's a header packet (IMG + frame ID)
                        if (packetStart.startsWith(HEADER)) {
                            // Extract frame ID
                            currentFrameId = new String(data, 3, 4);
                            chunks.clear();
                            expectedChunks = 0;
                            logger.debug("Started receiving frame: {}", currentFrameId);
                        }

                        // Check if it's a data chunk (frame ID + chunk ID + total chunks + data)
                        else if (currentFrameId != null && length >= 8) {
                            String frameId = new String(data, 0, 4);
                            if (frameId.equals(currentFrameId)) {
                                try {
                                    int chunkId = Integer.parseInt(new String(data, 4, 2));
                                    expectedChunks = Integer.parseInt(new String(data, 6, 2));
                                    
                                    // Store the chunk
                                    byte[] chunkData = new byte[length - 8];
                                    System.arraycopy(data, 8, chunkData, 0, length - 8);
                                    chunks.put(chunkId, chunkData);
                                    
                                    // Check if we have all chunks
                                    if (chunks.size() == expectedChunks) {
                                        processCompleteFrame(chunks, expectedChunks);
                                        chunks.clear();
                                        hasReceivedValidFrame = true;
                                    }
                                } catch (NumberFormatException e) {
                                    // Invalid chunk header, ignore
                                    logger.warn("WARNING: Invalid chunk header: {}", e.getMessage());
                                }
                            }
                        }
                        // Check if it's a trailer packet (END + frame ID)
                        else if (packetStart.startsWith(TRAILER)) {
                            String frameId = new String(data, 3, 4);
                            if (frameId.equals(currentFrameId)) {
                                // Process any chunks we have even if incomplete
                                if (!chunks.isEmpty() && expectedChunks > 0) {
                                    logger.debug("Processing incomplete frame: {} / {} chunks", chunks.size(), expectedChunks);
                                    processCompleteFrame(chunks, expectedChunks);
                                    chunks.clear();
                                }
                            }
                        }
                        
                        // If we haven't received any valid frames after a while, try sending a test image
                        if (!hasReceivedValidFrame && remoteVideoPanel != null && remoteVideoPanel.isVisible()) {
                            long uptime = System.currentTimeMillis() - startTime;
                            if (uptime > 5000 && uptime % 5000 < 100) { // Check every 5 seconds
                                logger.warn("WARNING: No valid frames received yet, UI may not be updating");
                            }
                        }
                    }
                } catch (SocketClosedException e) {
                    logger.debug("DEBUG: Socket closed");
                } catch (IOException e) {
                    if (iceSocket != null) {
                        logger.warn("Error receiving data", e);
                    }
                }
            }
        }).start();
    }
    
    private void processCompleteFrame(Map<Integer, byte[]> chunks, int totalChunks) {
        try {
            // Calculate total size
            int totalSize = 0;
            for (byte[] chunk : chunks.values()) {
                totalSize += chunk.length;
            }
            
            // Make sure we have a valid frame
            if (totalSize <= 0) {
                logger.warn("WARNING: Received an empty frame, ignoring");
                return;
            }
            
            // Combine chunks
            ByteArrayOutputStream imageData = new ByteArrayOutputStream(totalSize);
            boolean missingChunks = false;
            
            for (int i = 0; i < totalChunks; i++) {
                byte[] chunk = chunks.get(i);
                if (chunk != null) {
                    imageData.write(chunk);
                } else {
                    missingChunks = true;
                    logger.warn("WARNING: Missing chunk {} of {}", i, totalChunks);
                }
            }
            
            byte[] finalImageData = imageData.toByteArray();
            logger.debug("Received {} frame: {} bytes", (missingChunks ? "incomplete" : "complete"), finalImageData.length);
            
            // Log first few bytes to help debug format issues
//            debugImageData(finalImageData);
            
            // Try to decode the image
            receiveVideoFrame(finalImageData);
            
        } catch (IOException e) {
            logger.warn("Error processing frame chunks", e);
        }
    }
    
    private void debugImageData(byte[] imageData) {
        if (imageData == null || imageData.length == 0) {
            logger.debug("DEBUG: Image data is null or empty");
            return;
        }
        
        // Check if data has valid JPEG header (0xFF 0xD8)
        boolean validJpegHeader = (imageData.length >= 2 && 
                                  (imageData[0] & 0xFF) == 0xFF && 
                                  (imageData[1] & 0xFF) == 0xD8);
        
        logger.debug("DEBUG: Image data length: {}, Valid JPEG header: {}", imageData.length, validJpegHeader);
        
        // Print first 20 bytes for debugging (as hex)
        StringBuilder hexDump = new StringBuilder("First bytes: ");
        for (int i = 0; i < Math.min(20, imageData.length); i++) {
            hexDump.append(String.format("%02X ", imageData[i] & 0xFF));
        }
        logger.debug(hexDump.toString());
    }

    // Public method to trigger hangup (e.g., from button)
    public void hangupCall() {
         logger.debug("hangupCall entered. InCall: {}", inCall.get());
         if (inCall.get()) {
            sendHangupMessage();
            logger.debug("Calling cleanupCall from hangupCall.");
            cleanupCall();
        }
    }

    private void sendHangupMessage() {
        logger.debug("sendHangupMessage entered. Partner: {}", 
            (currentCallPartner != null ? currentCallPartner.getUsername() : "null"));
        if (currentCallPartner != null) {
            try {
                ConnectionManager.getInstance().sendMessage(
                        new VideoCallEnd(userHandler.getUser(), currentCallPartner));
                logger.info("Sent hangup message to {}", currentCallPartner.getUsername());
            } catch (IOException e) {
                logger.error("Error sending call end message: {}", e.getMessage(), e);
            }
        } else {
            logger.debug("sendHangupMessage skipped - no current partner.");
        }
    }

    private void cleanupCall() {
        logger.debug("cleanupCall entered. Current inCall state: {}", inCall.get());
        if (!inCall.getAndSet(false)) { // Prevent double cleanup
            logger.debug("Cleanup already in progress or call not active.");
            return;
        }
        logger.debug("Starting call cleanup...");

        capturing.set(false);
        
        // Close webcam if open
        if (webcam != null && webcam.isOpen()) {
            try {
                webcam.close();
                logger.info("Webcam closed.");
            } catch (Exception e) {
                logger.error("Error closing webcam: {}", e.getMessage(), e);
            }
            webcam = null;
        }
        
        if (captureThread != null) {
            logger.debug("Interrupting capture thread...");
            captureThread.interrupt();
            try {
                logger.debug("DEBUG: Joining capture thread...");
                captureThread.join(500); // Wait briefly for thread to finish
                logger.debug("DEBUG: Capture thread join completed. Is alive? {}", captureThread.isAlive());
            } catch (InterruptedException e) {
                logger.error("DEBUG: Interrupted while joining capture thread.");
                Thread.currentThread().interrupt(); // Re-interrupt if needed
            }
            if (captureThread.isAlive()) {
                logger.error("Capture thread did not terminate cleanly.");
            }
            captureThread = null;
            logger.info("Capture thread stopped.");
        }

        // Clean up the UI on the EDT
        final JFrame frameToDispose = videoFrame;
        videoFrame = null; // Clear reference first to prevent race conditions
        
        SwingUtilities.invokeLater(() -> {
            logger.debug("DEBUG: Executing cleanupCall SwingUtilities.invokeLater.");
            if (frameToDispose != null) {
                logger.debug("Disposing video frame...");
                frameToDispose.setVisible(false);
                frameToDispose.dispose();
                logger.info("Video frame disposed.");
            } else {
                logger.debug("DEBUG: Video frame was already null during cleanup.");
            }
            
            // Also clear panel references
            localVideoPanel = null;
            remoteVideoPanel = null;
        });

        logger.debug("Resetting partner and images in cleanupCall.");
        currentCallPartner = null;
        lastCapturedImage = null;
        lastReceivedImage = null;
        if (iceSocket != null) {
            iceSocket.close();
            iceSocket = null;
        }
        calleePort = 0;
        calleeAddress = null;
        receivingImage = false;
        receivedDataBuffer.reset();
        logger.debug("DEBUG: cleanupCall finished.");
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        // Handle any application events if needed
    }

    // Add Metered TURN credentials
    private List<TurnServer> fetchTurnServers() {
        List<TurnServer> turnServers = new ArrayList<>();
        String METERED_API_URL = ApplicationContext.getProperties().getProperty("metered.url");
        if (METERED_API_URL.isBlank())
            return turnServers;
        try {
            // Create URL and open connection
            URL url = new URL(METERED_API_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            
            // Check response code
            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                // Read the response
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();
                
                // Parse JSON response
                String jsonResponse = response.toString();
                logger.info("Metered API response: {}", jsonResponse);
                
                // Simple JSON parsing (could use a proper JSON library)
                // Format is like: [{"urls":"turn:server.com:port","username":"user","credential":"pass"}]
                String[] servers = jsonResponse.split("\\{");
                for (String server : servers) {
                    if (server.contains("urls") && server.contains("turn:")) {
                        String urlPart = server.substring(server.indexOf("turn:"), server.indexOf("\"", server.indexOf("turn:")));
                        String username = server.substring(server.indexOf("username") + 11, server.indexOf("\"", server.indexOf("username") + 11));
                        String credential = server.substring(server.indexOf("credential") + 13, server.indexOf("\"", server.indexOf("credential") + 13));
                        
                        // Extract host and port from URL
                        String host = urlPart.substring(5, urlPart.lastIndexOf(":"));
                        int port = Integer.parseInt(urlPart.substring(urlPart.lastIndexOf(":") + 1));
                        
                        logger.info("Adding TURN server: {} : {}", host, port);
                        turnServers.add(new TurnServer(host, port, username, credential));
                    }
                }
            } else {
                logger.info("Failed to fetch TURN servers: HTTP error code: {}", responseCode);
            }
        } catch (Exception e) {
            logger.error("Error fetching TURN servers: {}", e.getMessage());
            e.printStackTrace();
        }
        
        // Add fallback TURN servers if the API fails
        if (turnServers.isEmpty()) {
            logger.info("Using fallback TURN servers");
            turnServers.add(new TurnServer("turn.voipgate.com", 3478, "webrtc", "webrtc"));
        }
        
        return turnServers;
    }
    
    // TURN server info class
    private static class TurnServer {
        private final String host;
        private final int port;
        private final String username;
        private final String credential;
        
        public TurnServer(String host, int port, String username, String credential) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.credential = credential;
        }
    }

    private void restartIceNegotiation() {
        logger.debug("Attempting to restart ICE negotiation");
        try {
            // Get a selected pair that's more likely to work (prefer public addresses)
            CandidatePair currentPair = null;
            
            for (IceMediaStream stream : iceAgent.getStreams()) {
                if (stream.getName().contains("video")) {
                    Component rtpComponent = stream.getComponent(Component.RTP);
                    // First try to find a candidate with a public address
                    for (RemoteCandidate candidate : rtpComponent.getRemoteCandidates()) {
                        TransportAddress ta = candidate.getTransportAddress();
                        if (!ta.getAddress().isSiteLocalAddress() && !ta.getAddress().isLinkLocalAddress()) {
                            // Found a public address
                            calleeAddress = ta.getAddress();
                            calleePort = ta.getPort();
                            logger.debug("DEBUG: Switched to public candidate: {}", calleeAddress.getHostAddress() + ":" + calleePort);
                            return;
                        }
                    }
                    
                    // If no public address found, just use the selected pair
                    currentPair = rtpComponent.getSelectedPair();
                    
                    // Update connection details if we found a pair
                    if (currentPair != null) {
                        calleeAddress = currentPair.getRemoteCandidate().getTransportAddress().getAddress();
                        calleePort = currentPair.getRemoteCandidate().getTransportAddress().getPort();
                        logger.debug("DEBUG: Using fallback candidate: {}", calleeAddress.getHostAddress() + ":" + calleePort);
                    }
                }
            }
        } catch (Exception e) {
            logger.warn("Failed to restart ICE negotiation", e);
        }
    }

    public static void configureStunServers(Agent iceAgent) {
        if (iceAgent == null) {
            logger.error("IceAgent is null. Cannot configure STUN servers.");
            return;
        }

        logger.info("Configuring STUN servers for IceAgent...");

        List<Integer> serverIndices = new ArrayList<>(STUN_SERVERS.length);
        for (int i = 0; i < STUN_SERVERS.length; i++) {
            serverIndices.add(i);
        }

        Collections.shuffle(serverIndices);

        int serversToPick = Math.min(NUMBER_OF_STUN_SERVERS_TO_USE, STUN_SERVERS.length);

        List<String> randomStunServers = new ArrayList<>(serversToPick);
        for (int i = 0; i < serversToPick; i++) {
            randomStunServers.add(STUN_SERVERS[serverIndices.get(i)]);
        }


        for (String serverAddress : randomStunServers) {
            try {
                String[] parts = serverAddress.split(":");
                if (parts.length != 2) {
                    logger.error("Invalid STUN server address format: {}", serverAddress);
                    continue;
                }
                String host = parts[0];
                int port = Integer.parseInt(parts[1]);

                InetAddress stunAddress = InetAddress.getByName(host);

                TransportAddress transportAddress = new TransportAddress(stunAddress, port, Transport.UDP);

                iceAgent.addCandidateHarvester(new StunCandidateHarvester(transportAddress));

                logger.info("Added STUN server: {}", serverAddress);

            } catch (UnknownHostException e) {
                logger.error("Could not resolve STUN server host: {} - {}", serverAddress, e.getMessage());
            } catch (NumberFormatException e) {
                logger.error("Invalid port number for STUN server: {} - {}", serverAddress, e.getMessage());
            } catch (Exception e) {
                logger.error("Error adding STUN server {}", serverAddress, e);
            }
        }
        logger.info("Finished configuring STUN servers.");
    }
    private static final String[] STUN_SERVERS = {
            "23.21.150.121:3478",
            "iphone-stun.strato-iphone.de:3478",
            "numb.viagenie.ca:3478",
            "s1.taraba.net:3478",
            "s2.taraba.net:3478",
            "stun.12connect.com:3478",
            "stun.12voip.com:3478",
            "stun.1und1.de:3478",
            "stun.2talk.co.nz:3478",
            "stun.2talk.com:3478",
            "stun.3clogic.com:3478",
            "stun.3cx.com:3478",
            "stun.a-mm.tv:3478",
            "stun.aa.net.uk:3478",
            "stun.acrobits.cz:3478",
            "stun.actionvoip.com:3478",
            "stun.advfn.com:3478",
            "stun.aeta-audio.com:3478",
            "stun.aeta.com:3478",
            "stun.alltel.com.au:3478",
            "stun.altar.com.pl:3478",
            "stun.annatel.net:3478",
            "stun.antisip.com:3478",
            "stun.arbuz.ru:3478",
            "stun.avigora.com:3478",
            "stun.avigora.fr:3478",
            "stun.awa-shima.com:3478",
            "stun.awt.be:3478",
            "stun.b2b2c.ca:3478",
            "stun.bahnhof.net:3478",
            "stun.barracuda.com:3478",
            "stun.bluesip.net:3478",
            "stun.bmwgs.cz:3478",
            "stun.botonakis.com:3478",
            "stun.budgetphone.nl:3478",
            "stun.budgetsip.com:3478",
            "stun.cablenet-as.net:3478",
            "stun.callromania.ro:3478",
            "stun.callwithus.com:3478",
            "stun.cbsys.net:3478",
            "stun.chathelp.ru:3478",
            "stun.cheapvoip.com:3478",
            "stun.ciktel.com:3478",
            "stun.cloopen.com:3478",
            "stun.colouredlines.com.au:3478",
            "stun.comfi.com:3478",
            "stun.commpeak.com:3478",
            "stun.comtube.com:3478",
            "stun.comtube.ru:3478",
            "stun.cope.es:3478",
            "stun.counterpath.com:3478",
            "stun.counterpath.net:3478",
            "stun.cryptonit.net:3478",
            "stun.darioflaccovio.it:3478",
            "stun.datamanagement.it:3478",
            "stun.dcalling.de:3478",
            "stun.decanet.fr:3478",
            "stun.demos.ru:3478",
            "stun.develz.org:3478",
            "stun.dingaling.ca:3478",
            "stun.doublerobotics.com:3478",
            "stun.drogon.net:3478",
            "stun.duocom.es:3478",
            "stun.dus.net:3478",
            "stun.e-fon.ch:3478",
            "stun.easybell.de:3478",
            "stun.easycall.pl:3478",
            "stun.easyvoip.com:3478",
            "stun.efficace-factory.com:3478",
            "stun.einsundeins.com:3478",
            "stun.einsundeins.de:3478",
            "stun.ekiga.net:3478",
            "stun.epygi.com:3478",
            "stun.etoilediese.fr:3478",
            "stun.eyeball.com:3478",
            "stun.faktortel.com.au:3478",
            "stun.freecall.com:3478",
            "stun.freeswitch.org:3478",
            "stun.freevoipdeal.com:3478",
            "stun.fuzemeeting.com:3478",
            "stun.gmx.de:3478",
            "stun.gmx.net:3478",
            "stun.gradwell.com:3478",
            "stun.halonet.pl:3478",
            "stun.hellonanu.com:3478",
            "stun.hoiio.com:3478",
            "stun.hosteurope.de:3478",
            "stun.ideasip.com:3478",
            "stun.imesh.com:3478",
            "stun.infra.net:3478",
            "stun.internetcalls.com:3478",
            "stun.intervoip.com:3478",
            "stun.ipcomms.net:3478",
            "stun.ipfire.org:3478",
            "stun.ippi.fr:3478",
            "stun.ipshka.com:3478",
            "stun.iptel.org:3478",
            "stun.irian.at:3478",
            "stun.it1.hr:3478",
            "stun.ivao.aero:3478",
            "stun.jappix.com:3478",
            "stun.jumblo.com:3478",
            "stun.justvoip.com:3478",
            "stun.kanet.ru:3478",
            "stun.kiwilink.co.nz:3478",
            "stun.kundenserver.de:3478",
            "stun.l.google.com:19302",
            "stun.linea7.net:3478",
            "stun.linphone.org:3478",
            "stun.liveo.fr:3478",
            "stun.lowratevoip.com:3478",
            "stun.lugosoft.com:3478",
            "stun.lundimatin.fr:3478",
            "stun.magnet.ie:3478",
            "stun.manle.com:3478",
            "stun.mgn.ru:3478",
            "stun.mit.de:3478",
            "stun.mitake.com.tw:3478",
            "stun.miwifi.com:3478",
            "stun.modulus.gr:3478",
            "stun.mozcom.com:3478",
            "stun.myvoiptraffic.com:3478",
            "stun.mywatson.it:3478",
            "stun.nas.net:3478",
            "stun.neotel.co.za:3478",
            "stun.netappel.com:3478",
            "stun.netappel.fr:3478",
            "stun.netgsm.com.tr:3478",
            "stun.nfon.net:3478",
            "stun.noblogs.org:3478",
            "stun.noc.ams-ix.net:3478",
            "stun.node4.co.uk:3478",
            "stun.nonoh.net:3478",
            "stun.nottingham.ac.uk:3478",
            "stun.nova.is:3478",
            "stun.nventure.com:3478",
            "stun.on.net.mk:3478",
            "stun.ooma.com:3478",
            "stun.ooonet.ru:3478",
            "stun.oriontelekom.rs:3478",
            "stun.outland-net.de:3478",
            "stun.ozekiphone.com:3478",
            "stun.patlive.com:3478",
            "stun.personal-voip.de:3478",
            "stun.petcube.com:3478",
            "stun.phone.com:3478",
            "stun.phoneserve.com:3478",
            "stun.pjsip.org:3478",
            "stun.poivy.com:3478",
            "stun.powerpbx.org:3478",
            "stun.powervoip.com:3478",
            "stun.ppdi.com:3478",
            "stun.prizee.com:3478",
            "stun.qq.com:3478",
            "stun.qvod.com:3478",
            "stun.rackco.com:3478",
            "stun.rapidnet.de:3478",
            "stun.rb-net.com:3478",
            "stun.refint.net:3478",
            "stun.remote-learner.net:3478",
            "stun.rixtelecom.se:3478",
            "stun.rockenstein.de:3478",
            "stun.rolmail.net:3478",
            "stun.rounds.com:3478",
            "stun.rynga.com:3478",
            "stun.samsungsmartcam.com:3478",
            "stun.schlund.de:3478",
            "stun.services.mozilla.com:3478",
            "stun.sigmavoip.com:3478",
            "stun.sip.us:3478",
            "stun.sipdiscount.com:3478",
            "stun.siplogin.de:3478",
            "stun.sipnet.net:3478",
            "stun.sipnet.ru:3478",
            "stun.siportal.it:3478",
            "stun.sippeer.dk:3478",
            "stun.siptraffic.com:3478",
            "stun.skylink.ru:3478",
            "stun.sma.de:3478",
            "stun.smartvoip.com:3478",
            "stun.smsdiscount.com:3478",
            "stun.snafu.de:3478",
            "stun.softjoys.com:3478",
            "stun.solcon.nl:3478",
            "stun.solnet.ch:3478",
            "stun.sonetel.com:3478",
            "stun.sonetel.net:3478",
            "stun.sovtest.ru:3478",
            "stun.speedy.com.ar:3478",
            "stun.spokn.com:3478",
            "stun.srce.hr:3478",
            "stun.ssl7.net:3478",
            "stun.stunprotocol.org:3478",
            "stun.symform.com:3478",
            "stun.symplicity.com:3478",
            "stun.sysadminman.net:3478",
            "stun.t-online.de:3478",
            "stun.tagan.ru:3478",
            "stun.tatneft.ru:3478",
            "stun.teachercreated.com:3478",
            "stun.tel.lu:3478",
            "stun.telbo.com:3478",
            "stun.telefacil.com:3478",
            "stun.tis-dialog.ru:3478",
            "stun.tng.de:3478",
            "stun.twt.it:3478",
            "stun.u-blox.com:3478",
            "stun.ucallweconn.net:3478",
            "stun.ucsb.edu:3478",
            "stun.ucw.cz:3478",
            "stun.uls.co.za:3478",
            "stun.unseen.is:3478",
            "stun.usfamily.net:3478",
            "stun.veoh.com:3478",
            "stun.vidyo.com:3478",
            "stun.vipgroup.net:3478",
            "stun.virtual-call.com:3478",
            "stun.viva.gr:3478",
            "stun.vivox.com:3478",
            "stun.vline.com:3478",
            "stun.vo.lu:3478",
            "stun.vodafone.ro:3478",
            "stun.voicetrading.com:3478",
            "stun.voip.aebc.com:3478",
            "stun.voip.blackberry.com:3478",
            "stun.voip.eutelia.it:3478",
            "stun.voiparound.com:3478",
            "stun.voipblast.com:3478",
            "stun.voipbuster.com:3478",
            "stun.voipbusterpro.com:3478",
            "stun.voipcheap.co.uk:3478",
            "stun.voipcheap.com:3478",
            "stun.voipfibre.com:3478",
            "stun.voipgain.com:3478",
            "stun.voipgate.com:3478",
            "stun.voipinfocenter.com:3478",
            "stun.voipplanet.nl:3478",
            "stun.voippro.com:3478",
            "stun.voipraider.com:3478",
            "stun.voipstunt.com:3478",
            "stun.voipwise.com:3478",
            "stun.voipzoom.com:3478",
            "stun.vopium.com:3478",
            "stun.voxgratia.org:3478",
            "stun.voxox.com:3478",
            "stun.voys.nl:3478",
            "stun.voztele.com:3478",
            "stun.vyke.com:3478",
            "stun.webcalldirect.com:3478",
            "stun.whoi.edu:3478",
            "stun.wifirst.net:3478",
            "stun.wwdl.net:3478",
            "stun.xs4all.nl:3478",
            "stun.xtratelecom.es:3478",
            "stun.yesss.at:3478",
            "stun.zadarma.com:3478",
            "stun.zadv.com:3478",
            "stun.zoiper.com:3478",
            "stun1.faktortel.com.au:3478",
            "stun1.l.google.com:19302",
            "stun1.voiceeclipse.net:3478",
            "stun2.l.google.com:19302",
            "stun3.l.google.com:19302",
            "stun4.l.google.com:19302",
            "stunserver.org:3478",
            "stun.sipnet.net:3478",
            "stun.sipnet.ru:3478",
            "stun.stunprotocol.org:3478",
            "124.64.206.224:8800",
            "stun.nextcloud.com:443",
    };
}
