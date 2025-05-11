package com.tavern.server.handlers;

import com.tavern.common.model.User;
import com.tavern.common.model.network.*;
import com.tavern.server.core.ClientHandler;
import com.tavern.server.core.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

public class VideoChatHandler implements RequestHandler {
    private static final Logger logger = LoggerFactory.getLogger(VideoChatHandler.class);
    private final UserHandler userHandler;

    public VideoChatHandler(UserHandler userHandler, HashMap<NetworkMessage.MessageType, RequestHandler> requestHandlers) {
        this.userHandler = userHandler;
        
        // Register for video chat message types
        requestHandlers.put(NetworkMessage.MessageType.VIDEO_CALL_REQUEST, this);
        requestHandlers.put(NetworkMessage.MessageType.VIDEO_CALL_RESPONSE, this);
        requestHandlers.put(NetworkMessage.MessageType.VIDEO_CALL_OFFER, this);
        requestHandlers.put(NetworkMessage.MessageType.VIDEO_CALL_ANSWER, this);
//        requestHandlers.put(NetworkMessage.MessageType.VIDEO_ICE_CANDIDATE, this);
        requestHandlers.put(NetworkMessage.MessageType.VIDEO_CALL_END, this);
    }

    @Override
    public void handleRequest(NetworkMessage message, ClientHandler client) {
        switch (message.getMessageType()) {
            case VIDEO_CALL_REQUEST -> {
                VideoCallRequest request = (VideoCallRequest) message;
                handleCallRequest(request, client);
            }
            case VIDEO_CALL_RESPONSE -> {
                VideoCallResponse response = (VideoCallResponse) message;
                handleCallResponse(response, client);
            }
            case VIDEO_CALL_OFFER -> {
                VideoCallOffer offer = (VideoCallOffer) message;
                handleCallOffer(offer, client);
            }
            case VIDEO_CALL_ANSWER -> {
                VideoCallAnswer answer = (VideoCallAnswer) message;
                handleCallAnswer(answer, client);
            }
//            case VIDEO_ICE_CANDIDATE -> {
//                VideoIceCandidate candidate = (VideoIceCandidate) message;
//                handleIceCandidate(candidate, client);
//            }
            case VIDEO_CALL_END -> {
                VideoCallEnd end = (VideoCallEnd) message;
                handleCallEnd(end, client);
            }
        }
    }

    private void handleCallRequest(VideoCallRequest request, ClientHandler client) {
        logger.info("Video call request from {} to {}", 
                request.caller().getUsername(), request.callee().getUsername());
        
        // Get the client handler for the callee
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler calleeClient = activeUsers.get(request.callee());
        
        if (calleeClient != null && calleeClient.isConnected()) {
            // Forward the call request to the callee
            calleeClient.sendMessage(request);
        } else {
            // Callee is not online, send a rejection response
            logger.info("Callee {} is not online, automatically rejecting call", request.callee().getUsername());
            client.sendMessage(new VideoCallResponse(
                    request.callee(), 
                    request.caller(), 
                    false));
        }
    }

    private void handleCallResponse(VideoCallResponse response, ClientHandler client) {
        logger.info("Video call response from {} to {}: {}", 
                response.callee().getUsername(), 
                response.caller().getUsername(), 
                (response.accepted() ? "Accepted" : "Rejected"));
        
        // Forward the response to the caller
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler callerClient = activeUsers.get(response.caller());
        
        if (callerClient != null && callerClient.isConnected()) {
            callerClient.sendMessage(response);
        } else {
            logger.warn("Caller {} is no longer online", response.caller().getUsername());
        }
    }

    private void handleCallOffer(VideoCallOffer offer, ClientHandler client) {
        logger.info("Video call offer from {} to {}", 
                offer.caller().getUsername(), offer.callee().getUsername());
        
        // Forward the offer to the callee
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler calleeClient = activeUsers.get(offer.callee());
        
        if (calleeClient != null && calleeClient.isConnected()) {
            calleeClient.sendMessage(offer);
        } else {
            logger.warn("Callee {} is no longer online", offer.callee().getUsername());
        }
    }

    private void handleCallAnswer(VideoCallAnswer answer, ClientHandler client) {
        logger.info("Video call answer from {} to {}", 
                answer.callee().getUsername(), answer.caller().getUsername());
        
        // Forward the answer to the caller
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler callerClient = activeUsers.get(answer.caller());
        
        if (callerClient != null && callerClient.isConnected()) {
            callerClient.sendMessage(answer);
        } else {
            logger.warn("Caller {} is no longer online", answer.caller().getUsername());
        }
    }

    private void handleCallEnd(VideoCallEnd end, ClientHandler client) {
        logger.info("Call end from {} to {}", 
                end.sender().getUsername(), end.receiver().getUsername());
        
        // Forward the call end message to the receiver
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler receiverClient = activeUsers.get(end.receiver());
        
        if (receiverClient != null && receiverClient.isConnected()) {
            receiverClient.sendMessage(end);
        } else {
            logger.debug("Receiver {} is not online to receive call end notification", 
                    end.receiver().getUsername());
        }
    }
}
