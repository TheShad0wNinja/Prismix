package com.prismix.server.handlers;

import com.prismix.common.model.User;
import com.prismix.common.model.network.*;
import com.prismix.server.core.ClientHandler;
import com.prismix.server.core.RequestHandler;

import java.util.HashMap;

public class VideoChatHandler implements RequestHandler {
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
        System.out.println("Video call request from " + request.caller().getUsername() + 
                " to " + request.callee().getUsername());
        
        // Get the client handler for the callee
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler calleeClient = activeUsers.get(request.callee());
        
        if (calleeClient != null && calleeClient.isConnected()) {
            // Forward the call request to the callee
            calleeClient.sendMessage(request);
        } else {
            // Callee is not online, send a rejection response
            client.sendMessage(new VideoCallResponse(
                    request.callee(), 
                    request.caller(), 
                    false));
        }
    }

    private void handleCallResponse(VideoCallResponse response, ClientHandler client) {
        System.out.println("Video call response from " + response.callee().getUsername() + 
                " to " + response.caller().getUsername() + ": " + 
                (response.accepted() ? "Accepted" : "Rejected"));
        
        // Forward the response to the caller
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler callerClient = activeUsers.get(response.caller());
        
        if (callerClient != null && callerClient.isConnected()) {
            callerClient.sendMessage(response);
        }
    }

    private void handleCallOffer(VideoCallOffer offer, ClientHandler client) {
        System.out.println("Video call offer from " + offer.caller().getUsername() + 
                " to " + offer.callee().getUsername());
        
        // Forward the offer to the callee
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler calleeClient = activeUsers.get(offer.callee());
        
        if (calleeClient != null && calleeClient.isConnected()) {
            calleeClient.sendMessage(offer);
        }
    }

    private void handleCallAnswer(VideoCallAnswer answer, ClientHandler client) {
        System.out.println("Video call answer from " + answer.callee().getUsername() + 
                " to " + answer.caller().getUsername());
        
        // Forward the answer to the caller
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler callerClient = activeUsers.get(answer.caller());
        
        if (callerClient != null && callerClient.isConnected()) {
            callerClient.sendMessage(answer);
        }
    }

//    private void handleIceCandidate(VideoIceCandidate candidate, ClientHandler client) {
//        System.out.println("ICE candidate from " + candidate.sender().getUsername() +
//                " to " + candidate.receiver().getUsername());
//
//        // Forward the ICE candidate to the receiver
//        HashMap<User, ClientHandler> activeUsers = authHandler.getActiveUsers();
//        ClientHandler receiverClient = activeUsers.get(candidate.receiver());
//
//        if (receiverClient != null && receiverClient.isConnected()) {
//            receiverClient.sendMessage(candidate);
//        }
//    }

    private void handleCallEnd(VideoCallEnd end, ClientHandler client) {
        System.out.println("Call end from " + end.sender().getUsername() + 
                " to " + end.receiver().getUsername());
        
        // Forward the call end message to the receiver
        HashMap<User, ClientHandler> activeUsers = userHandler.getActiveUsers();
        ClientHandler receiverClient = activeUsers.get(end.receiver());
        
        if (receiverClient != null && receiverClient.isConnected()) {
            receiverClient.sendMessage(end);
        }
    }
}
