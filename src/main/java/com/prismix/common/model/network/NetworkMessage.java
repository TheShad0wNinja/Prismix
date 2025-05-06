package com.prismix.common.model.network;

import java.io.Serializable;

public interface NetworkMessage extends Serializable {
    public enum MessageType {
        // Auth Messages
        LOGIN_REQUEST, LOGIN_RESPONSE,
        SIGNUP_REQUEST, SIGNUP_RESPONSE,

        // User messages
        GET_USERS_INFO_REQUEST, GET_USERS_INFO_RESPONSE,
        GET_ALL_USERS_REQUEST, GET_ALL_USERS_RESPONSE,

        // Room Messages
        GET_ROOMS_REQUEST, GET_ROOMS_RESPONSE, // Get List of users rooms
        GET_ROOM_USERS_REQUEST, GET_ROOM_USERS_RESPONSE, // GEt list of users in the room

        SEND_TEXT_MESSAGE_REQUEST, SEND_TEXT_MESSAGE_RESPONSE,
        GET_UNREAD_MESSAGE_REQUEST, GET_UNREAD_MESSAGE_RESPONSE,
                                    RECEIVE_TEXT_MESSAGE_REQUEST,

        // Video Chat Messages
        VIDEO_CALL_REQUEST, VIDEO_CALL_RESPONSE,
        VIDEO_CALL_OFFER, VIDEO_CALL_ANSWER,
        VIDEO_CALL_END,



//        SEND_FILE_MESSAGE,
//        CREATE_ROOM_REQUEST,
//        JOIN_ROOM_REQUEST,
//        LEAVE_ROOM_REQUEST,
//        GET_USERS_REQUEST, // Request list of online users
//        GET_ROOM_MEMBERS_REQUEST,
//        GET_MESSAGES_REQUEST, // Request message history
//
//        // Server to Client Messages
//        NEW_MESSAGE, // Sent when a new message arrives (text or file)
//        USER_ONLINE_STATUS_UPDATE, // Inform clients about users online status changes
//        ROOM_CREATED_NOTIFICATION,
//        USER_JOINED_ROOM_NOTIFICATION,
//        USER_LEFT_ROOM_NOTIFICATION,
//        ROOM_LIST_RESPONSE,
//        USER_LIST_RESPONSE,
//        ROOM_MEMBERS_RESPONSE,
//        MESSAGE_HISTORY_RESPONSE,
//        FILE_TRANSFER_INITIATION, // Server informing recipient of incoming file
//        FILE_TRANSFER_PROGRESS,
//        FILE_TRANSFER_COMPLETE,
//        ERROR_RESPONSE, // For general errors
//
//        // WebRTC Signaling Messages (Examples)
//        CALL_OFFER,
//        CALL_ANSWER,
//        ICE_CANDIDATE,
//        CALL_END,
//        CALL_REQUEST, // Client requesting a call

        // Add other message types as needed for your protocol
    }

    MessageType getMessageType();
}
