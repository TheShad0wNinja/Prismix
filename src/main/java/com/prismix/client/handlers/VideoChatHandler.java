package com.prismix.client.handlers;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventBus;
import com.prismix.client.core.EventListener;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.client.utils.SdpUtils;
import com.prismix.common.model.User;
import com.prismix.common.model.network.*;
import org.ice4j.Transport;
import org.ice4j.TransportAddress;
import org.ice4j.ice.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.github.sarxos.webcam.Webcam;
import org.ice4j.ice.Component;
import org.ice4j.ice.harvest.StunCandidateHarvester;
import org.ice4j.socket.IceSocketWrapper;
import org.ice4j.socket.SocketClosedException;

public class VideoChatHandler implements ResponseHandler, EventListener {
    private final EventBus eventBus;
    private final AuthHandler authHandler;
    private IceMediaStream videoStream;
    private Agent iceAgent;
    private DatagramSocket udpSocket;
    private static final String STUN_SERVER = "stun.l.google.com";
    private static final int STUN_PORT = 19302;
    private static final Logger logger = Logger.getLogger(VideoChatHandler.class.getName());

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

    private static final int MAX_PACKET_SIZE = 1400; // Slightly less than typical MTU
    private static final byte[] HEADER = "IMG".getBytes();
    private static final byte[] TRAILER = "END".getBytes();
    private final ByteArrayOutputStream receivedDataBuffer = new ByteArrayOutputStream();
    private boolean receivingImage = false;

    public VideoChatHandler(EventBus eventBus, AuthHandler authHandler, HashMap<NetworkMessage.MessageType, ResponseHandler> responseHandler) {
        this.eventBus = eventBus;
        this.authHandler = authHandler;

        // Register for video chat message types
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_REQUEST, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_RESPONSE, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_OFFER, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_ANSWER, this);
//        responseHandler.put(NetworkMessage.MessageType.VIDEO_ICE_CANDIDATE, this);
        responseHandler.put(NetworkMessage.MessageType.VIDEO_CALL_END, this);

        eventBus.subscribe(this);
    }

    @Override
    public void handleResponse(NetworkMessage message) {
        System.out.println("DEBUG: Handling response type: " + message.getMessageType());
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
                System.out.println("DEBUG: Received VIDEO_CALL_END message");
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
                    System.out.println("CURRENT CALL PARTNER: " + currentCallPartner);
                    initializeCallWindow();
                }
            } catch (IOException e) {
                System.err.println("Error responding to call request: " + e.getMessage());
            }
        });
    }

    private void initializeCallWindow() {
        SwingUtilities.invokeLater(() -> {
            videoFrame = new JFrame("Video Call with " + currentCallPartner.getUsername());
            videoFrame.setSize(800, 600);
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

            // Set up video panels
            localVideoPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    System.out.println("DEBUG: Painting local panel. Image null? " + (lastCapturedImage == null));
                    if (lastCapturedImage != null) {
                        g.drawImage(lastCapturedImage, 0, 0, getWidth(), getHeight(), this);
                    }
                }
            };
            localVideoPanel.setBorder(BorderFactory.createTitledBorder("Local Video"));

            remoteVideoPanel = new JPanel() {
                @Override
                protected void paintComponent(Graphics g) {
                    super.paintComponent(g);
                    System.out.println("DEBUG: Painting remote panel. Image null? " + (lastReceivedImage == null));
                    if (lastReceivedImage != null) {
                        g.drawImage(lastReceivedImage, 0, 0, getWidth(), getHeight(), this);
                    }
                }
            };
            remoteVideoPanel.setBorder(BorderFactory.createTitledBorder("Remote Video"));

            JPanel videoPanel = new JPanel(new GridLayout(1, 2));
            videoPanel.add(localVideoPanel);
            videoPanel.add(remoteVideoPanel);

            // Set up control panel
            JPanel controlPanel = new JPanel();
            hangupButton = new JButton("Hang Up");
            hangupButton.addActionListener(e -> hangupCall());
            controlPanel.add(hangupButton);

            videoFrame.add(videoPanel, BorderLayout.CENTER);
            videoFrame.add(controlPanel, BorderLayout.SOUTH);
            videoFrame.setVisible(true);

            // Start timestamp generation thread
            startCapture();
        });
    }

    private void handleCallResponse(VideoCallResponse response) {
        if (response.accepted()) {
            currentCallPartner = response.callee();
            inCall.set(true);
            initializeCallWindow();

            try {
                String sdpOffer = initializeIceAgent();
                ConnectionManager.getInstance().sendMessage(
                        new VideoCallOffer(response.caller(), response.callee(), sdpOffer));
            } catch (Throwable e) {
                System.err.println("Error sending call offer: " + e.getMessage());
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

        // Add STUN server
        iceAgent.addCandidateHarvester(new StunCandidateHarvester(
                new TransportAddress(STUN_SERVER, STUN_PORT, Transport.UDP)));

        // Create media stream
        videoStream = iceAgent.createMediaStream("video");

        // Create component for video
        iceAgent.createComponent(videoStream, 13000, 10000, 15000);

        iceAgent.addStateChangeListener(new StateListener());
        return SdpUtils.createSDPDescription(iceAgent);

//        org.ice4j.ice.Component videoComponent = iceAgent.createComponent(videoStream, Transport.UDP, 1, 1, 1);
//        Component videoComponent = videoStream.getComponent(Component.RTP);

        // Set up UDP socket for direct communication
//        udpSocket = new DatagramSocket();
//        udpSocket.setReuseAddress(true);

        // Add local host candidate
//        InetAddress localAddress = InetAddress.getLocalHost();

//        TransportAddress localTransportAddress = new TransportAddress(
//            localAddress, udpSocket.getLocalPort(), Transport.UDP);



//        LocalCandidate localCandidate = new HostCandidate(
//            localTransportAddress, videoComponent);
//        videoComponent.addLocalCandidate(localCandidate);

        // Start receiving video data
//        startVideoReceiver();
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
            System.out.println("DEBUG: Sent message: " + msg);
        } catch (IOException e) {
            System.out.println("Unable to send message: " + msg + ": " + e.getMessage());
        }
    }

    private void handleCallOffer(VideoCallOffer offer) {
        try {
            String sdp = initializeIceAgent();
            SdpUtils.parseSDP(iceAgent, offer.sdpOffer());
            iceAgent.startConnectivityEstablishment();
            ConnectionManager.getInstance().sendMessage(new VideoCallAnswer(offer.caller(), offer.callee(), sdp));
        } catch (Throwable e) {
            System.out.println("Error sending call offer: " + e.getMessage());
        }
    }


    private void handleCallAnswer(VideoCallAnswer answer) {
        try {
            SdpUtils.parseSDP(iceAgent, answer.sdpAnswer());
            iceAgent.startConnectivityEstablishment();
        } catch (Exception e) {
            System.out.println("Unable to handle call answer: " + e.getMessage());
        }
        // In a real WebRTC implementation, we would process the SDP answer
//        System.out.println("Received call answer from " + answer.callee().getUsername());

    }
//
//    private void handleIceCandidate(VideoIceCandidate candidate) {
////        try {
////            // Parse and add remote candidates
////            String[] candidatesArray = candidate.candidate().split("\\|");
////            for (String candidateStr : candidatesArray) {
////                if (candidateStr.isEmpty()) continue;
////
////                String[] parts = candidateStr.split(";");
////                if (parts.length >= 4) {
////                    String type = parts[0];
////                    String ip = parts[1];
////                    int port = Integer.parseInt(parts[2]);
////                    Transport transport = Transport.parse(parts[3]);
////
////                    TransportAddress transportAddress = new TransportAddress(ip, port, transport);
////                    org.ice4j.ice.Component component = videoStream.getComponent(1);
////
////                    // Create remote candidate using the component's factory method
////                    RemoteCandidate remoteCandidate = new RemoteCandidate(
////                        transportAddress,
////                        component,
////                        CandidateType.parse(type),
////                        "foundation",
////                        1,
////                        1
////                    );
////
////                    component.addRemoteCandidate(remoteCandidate);
////                }
////            }
////
////            // Start ICE connectivity checks
////            iceAgent.startConnectivityEstablishment();
////        } catch (Exception e) {
////            logger.log(Level.SEVERE, "Error handling ICE candidate", e);
////        }
//    }
//

    private class StateListener implements PropertyChangeListener{
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if(evt.getSource() instanceof Agent){
                Agent agent = (Agent) evt.getSource();
                if(agent.getState().equals(IceProcessingState.TERMINATED)) {
                    System.out.println("Agent terminated");
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
                    System.out.println("CalleeAddress: " + calleeAddress.getHostAddress());
                    System.out.println("CalleePort: " + calleePort);
                    startCapture();
                    startReceiver();
                }
            }
        }
    }
    private void handleCallEnd(VideoCallEnd end) {
        System.out.println("DEBUG: handleCallEnd entered. Sender: " + end.sender().getUsername() + ", InCall: " + inCall.get());
        if (inCall.get() && currentCallPartner != null &&
                (currentCallPartner.getId() == end.sender().getId() || currentCallPartner.getId() == end.receiver().getId())) {
            // Check if we are either the sender or receiver mentioned in the end message
            SwingUtilities.invokeLater(() -> {
                if (videoFrame != null) { // Ensure frame exists before showing message
                    System.out.println("DEBUG: Showing call ended dialog.");
                    JOptionPane.showMessageDialog(videoFrame, // Show relative to video frame
                            end.sender().getUsername() + " ended the call.",
                            "Call Ended",
                            JOptionPane.INFORMATION_MESSAGE);
                }
                System.out.println("DEBUG: Calling cleanupCall from handleCallEnd.");
                cleanupCall(); // Clean up local resources immediately
            });
        } else {
            System.out.println("DEBUG: handleCallEnd ignored - conditions not met (inCall=" + inCall.get() + ", partner=" + (currentCallPartner != null ? currentCallPartner.getUsername() : "null") + ")");
        }
    }

    public void initiateCall(User callee) {
        try {
            ConnectionManager.getInstance().sendMessage(
                    new VideoCallRequest(authHandler.getUser(), callee));
            pendingCalls.put(callee, true);
        } catch (IOException e) {
            System.err.println("Error initiating call: " + e.getMessage());
        }
    }

//    private void initializeVideoCall() {
//        inCall.set(true);
//
//        try {
//            // Initialize ICE Agent
//            initializeIceAgent();
//
//            // Start gathering ICE candidates
//            iceAgent.startConnectivityEstablishment();
//
//            // Send our local candidates to the peer
//            sendLocalCandidates();
//
//            SwingUtilities.invokeLater(() -> {
//                videoFrame = new JFrame("Video Call with " + currentCallPartner.getUsername());
//                videoFrame.setSize(800, 600);
//                videoFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
//                videoFrame.addWindowListener(new WindowAdapter() {
//                    @Override
//                    public void windowClosing(WindowEvent e) {
//                        // Send hangup message *if* still in call and initiated by this user closing window
//                        if (inCall.get()) {
//                             sendHangupMessage();
//                        }
//                        cleanupCall(); // Clean up local resources
//                        // Frame disposal is handled in cleanupCall
//                    }
//                });
//
//                // Set up video panels
//                localVideoPanel = new JPanel() {
//                    @Override
//                    protected void paintComponent(Graphics g) {
//                        super.paintComponent(g);
//                        System.out.println("DEBUG: Painting local panel. Image null? " + (lastCapturedImage == null));
//                        if (lastCapturedImage != null) {
//                            g.drawImage(lastCapturedImage, 0, 0, getWidth(), getHeight(), this);
//                        }
//                    }
//                };
//                localVideoPanel.setBorder(BorderFactory.createTitledBorder("Local Video"));
//
//                remoteVideoPanel = new JPanel() {
//                    @Override
//                    protected void paintComponent(Graphics g) {
//                        super.paintComponent(g);
//                        System.out.println("DEBUG: Painting remote panel. Image null? " + (lastReceivedImage == null));
//                        if (lastReceivedImage != null) {
//                            g.drawImage(lastReceivedImage, 0, 0, getWidth(), getHeight(), this);
//                        }
//                    }
//                };
//                remoteVideoPanel.setBorder(BorderFactory.createTitledBorder("Remote Video"));
//
//                JPanel videoPanel = new JPanel(new GridLayout(1, 2));
//                videoPanel.add(localVideoPanel);
//                videoPanel.add(remoteVideoPanel);
//
//                // Set up control panel
//                JPanel controlPanel = new JPanel();
//                hangupButton = new JButton("Hang Up");
//                hangupButton.addActionListener(e -> hangupCall());
//                controlPanel.add(hangupButton);
//
//                videoFrame.add(videoPanel, BorderLayout.CENTER);
//                videoFrame.add(controlPanel, BorderLayout.SOUTH);
//                videoFrame.setVisible(true);
//
//                // Start timestamp generation thread
//                startTimestampGeneration();
//            });
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Failed to initialize video call", e);
//            cleanupCall();
//        }
//    }
//

//
//    private void sendLocalCandidates() {
//        try {
//            StringBuilder candidatesBuilder = new StringBuilder();
//            for (LocalCandidate candidate : videoStream.getComponent(1).getLocalCandidates()) {
//                candidatesBuilder.append(serializeCandidate(candidate)).append("|");
//            }
//
//            String candidatesStr = candidatesBuilder.toString();
//            ConnectionManager.getInstance().sendMessage(
//                new VideoIceCandidate(authHandler.getUser(), currentCallPartner, candidatesStr, "video", 0));
//        } catch (Exception e) {
//            logger.log(Level.SEVERE, "Error sending local candidates", e);
//        }
//    }
//
//    private String serializeCandidate(LocalCandidate candidate) {
//        TransportAddress ta = candidate.getTransportAddress();
//        return candidate.getType() + ";" +
//               ta.getHostAddress() + ";" +
//               ta.getPort() + ";" +
//               candidate.getTransport();
//    }
//
    private void startCapture() {
        capturing.set(true);
        System.out.println("DEBUG: Starting timestamp generation thread.");
        captureThread = new Thread(() -> {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
            while (capturing.get()) {
                try {
                    // Generate timestamp image
                    lastCapturedImage = createTimestampImage(sdf.format(new Date()));

                    // Update local video panel
                    if (localVideoPanel != null) {
                        localVideoPanel.repaint();
                    }

                    // Send frame to partner
//                    sendMessage("I WORK NOW you goofy I AM " + authHandler.getUser().getUsername());
                    sendVideoFrame(lastCapturedImage);

                    Thread.sleep(1000); // Capture frame every second
                } catch (InterruptedException e) {
                    System.out.println("DEBUG: Timestamp generation thread interrupted.");
                    Thread.currentThread().interrupt(); // Preserve interrupt status
                    capturing.set(false); // Ensure loop termination
                } catch (Exception e) {
                    System.err.println("Error in video capture: " + e.getMessage());
                    // Exit loop if interrupted
                    if (Thread.currentThread().isInterrupted()) {
                        capturing.set(false);
                    }
                }
            }
            System.out.println("Capturing finished.");
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

        String userText = "User: " + (authHandler.getUser() != null ? authHandler.getUser().getUsername() : "Unknown");
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

        try {
            // Convert image to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(image, "jpg", baos);
            byte[] imageData = baos.toByteArray();
            
            System.out.println("DEBUG: Sending image of size: " + imageData.length + " bytes");
            
            // Send start header
            DatagramPacket headerPacket = new DatagramPacket(HEADER, HEADER.length);
            headerPacket.setPort(calleePort);
            headerPacket.setAddress(calleeAddress);
            iceSocket.send(headerPacket);
            
            // Send image in chunks
            int offset = 0;
            while (offset < imageData.length) {
                int chunkSize = Math.min(MAX_PACKET_SIZE, imageData.length - offset);
                byte[] chunk = new byte[chunkSize];
                System.arraycopy(imageData, offset, chunk, 0, chunkSize);
                
                DatagramPacket packet = new DatagramPacket(chunk, chunkSize);
                packet.setPort(calleePort);
                packet.setAddress(calleeAddress);
                iceSocket.send(packet);
                
                offset += chunkSize;
                // Small delay to avoid overwhelming the network
                Thread.sleep(5);
            }
            
            // Send end trailer
            DatagramPacket trailerPacket = new DatagramPacket(TRAILER, TRAILER.length);
            trailerPacket.setPort(calleePort);
            trailerPacket.setAddress(calleeAddress);
            iceSocket.send(trailerPacket);
            
            System.out.println("DEBUG: Image sent in " + (offset / MAX_PACKET_SIZE + 1) + " chunks");
            
        } catch (IOException | InterruptedException e) {
            logger.log(Level.WARNING, "Error sending video frame", e);
        }
    }

    private void receiveVideoFrame(byte[] imageData) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
            lastReceivedImage = ImageIO.read(bais);

            System.out.println("DEBUG: Received image successfully decoded. Image null? " + (lastReceivedImage == null));

            if (remoteVideoPanel != null) {
                //System.out.println("DEBUG: Repainting remote panel.");
                remoteVideoPanel.repaint();
            }
        } catch (IOException e) {
            System.err.println("Error receiving video frame: " + e.getMessage());
        }
    }

    private void startReceiver() {
        new Thread(() -> {
            byte[] buffer = new byte[MAX_PACKET_SIZE];
            while (inCall.get()) {
                try {
                    if (iceSocket != null) {
                        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                        iceSocket.receive(packet);
                        
                        // Check if it's a header or trailer packet
                        if (packet.getLength() == HEADER.length && 
                            new String(packet.getData(), 0, packet.getLength()).equals(new String(HEADER))) {
                            // Start of a new image
                            receivedDataBuffer.reset();
                            receivingImage = true;
                            System.out.println("DEBUG: Started receiving new image");
                        } else if (packet.getLength() == TRAILER.length && 
                                  new String(packet.getData(), 0, packet.getLength()).equals(new String(TRAILER))) {
                            // End of the image
                            if (receivingImage) {
                                byte[] completeImageData = receivedDataBuffer.toByteArray();
                                System.out.println("DEBUG: Finished receiving image, size: " + completeImageData.length + " bytes");
                                receiveVideoFrame(completeImageData);
                                receivingImage = false;
                            }
                        } else if (receivingImage) {
                            // It's a chunk of the image
                            receivedDataBuffer.write(packet.getData(), 0, packet.getLength());
                        }
                    }
                } catch (SocketClosedException e) {
                    System.out.println("DEBUG: Socket closed");
                } catch (IOException e) {
                    if (iceSocket != null) {
                        logger.log(Level.WARNING, "Error receiving data", e);
                    }
                }
            }
        }).start();
    }
//
//    // Renamed endCall to sendHangupMessage to clarify its purpose

    // Public method to trigger hangup (e.g., from button)
    public void hangupCall() {
         System.out.println("DEBUG: hangupCall entered. InCall: " + inCall.get());
         if (inCall.get()) {
            sendHangupMessage();
            System.out.println("DEBUG: Calling cleanupCall from hangupCall.");
            cleanupCall();
        }
    }

    private void sendHangupMessage() {
        System.out.println("DEBUG: sendHangupMessage entered. Partner: " + (currentCallPartner != null ? currentCallPartner.getUsername() : "null"));
        if (currentCallPartner != null) {
            try {
                ConnectionManager.getInstance().sendMessage(
                        new VideoCallEnd(authHandler.getUser(), currentCallPartner));
                System.out.println("Sent hangup message to " + currentCallPartner.getUsername());
            } catch (IOException e) {
                System.err.println("Error sending call end message: " + e.getMessage());
            }
        } else {
            System.out.println("DEBUG: sendHangupMessage skipped - no current partner.");
        }
    }

    private void cleanupCall() {
        System.out.println("DEBUG: cleanupCall entered. Current inCall state: " + inCall.get());
        if (!inCall.getAndSet(false)) { // Prevent double cleanup
            System.out.println("Cleanup already in progress or call not active.");
            return;
        }
        System.out.println("Starting call cleanup...");

        capturing.set(false);
        if (captureThread != null) {
            System.out.println("Interrupting capture thread...");
            captureThread.interrupt();
            try {
                System.out.println("DEBUG: Joining capture thread...");
                captureThread.join(500); // Wait briefly for thread to finish
                System.out.println("DEBUG: Capture thread join completed. Is alive? " + captureThread.isAlive());
            } catch (InterruptedException e) {
                System.err.println("DEBUG: Interrupted while joining capture thread.");
                Thread.currentThread().interrupt(); // Re-interrupt if needed
            }
            if (captureThread.isAlive()) {
                System.err.println("Capture thread did not terminate cleanly.");
            }
            captureThread = null;
            System.out.println("Capture thread stopped.");
        }

        // Use SwingUtilities.invokeLater for UI updates
        SwingUtilities.invokeLater(() -> {
            System.out.println("DEBUG: Executing cleanupCall SwingUtilities.invokeLater.");
            if (videoFrame != null) {
                System.out.println("Disposing video frame...");
                videoFrame.dispose();
                videoFrame = null;
                System.out.println("Video frame disposed.");
            } else {
                System.out.println("DEBUG: Video frame was already null during cleanup.");
            }
        });

        System.out.println("DEBUG: Resetting partner and images in cleanupCall.");
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
        System.out.println("DEBUG: cleanupCall finished.");
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        // Handle any application events if needed
    }
}
