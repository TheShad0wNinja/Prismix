package com.prismix.common.model.network;

import java.io.Serializable;

public interface NetworkMessage extends Serializable {
    public enum MessageType {
        // Client to Server Messages
        LOGIN_REQUEST,
        SIGNUP_REQUEST,
        SEND_TEXT_MESSAGE,
        SEND_FILE_MESSAGE,
        CREATE_ROOM_REQUEST,
        JOIN_ROOM_REQUEST,
        LEAVE_ROOM_REQUEST,
        GET_ROOMS_REQUEST, // Request list of rooms the user is in
        GET_USERS_REQUEST, // Request list of online users
        GET_ROOM_MEMBERS_REQUEST,
        GET_MESSAGES_REQUEST, // Request message history

        // Server to Client Messages
        LOGIN_RESPONSE,
        SIGNUP_RESPONSE,
        NEW_MESSAGE, // Sent when a new message arrives (text or file)
        USER_ONLINE_STATUS_UPDATE, // Inform clients about user online status changes
        ROOM_CREATED_NOTIFICATION,
        USER_JOINED_ROOM_NOTIFICATION,
        USER_LEFT_ROOM_NOTIFICATION,
        ROOM_LIST_RESPONSE,
        USER_LIST_RESPONSE,
        ROOM_MEMBERS_RESPONSE,
        MESSAGE_HISTORY_RESPONSE,
        FILE_TRANSFER_INITIATION, // Server informing recipient of incoming file
        FILE_TRANSFER_PROGRESS,
        FILE_TRANSFER_COMPLETE,
        ERROR_RESPONSE, // For general errors

        // WebRTC Signaling Messages (Examples)
        CALL_OFFER,
        CALL_ANSWER,
        ICE_CANDIDATE,
        CALL_END,
        CALL_REQUEST, // Client requesting a call

        // Add other message types as needed for your protocol
    }

    MessageType getMessageType();
}
