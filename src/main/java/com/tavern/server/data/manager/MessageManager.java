package com.tavern.server.data.manager;

import com.tavern.common.model.Message;
import com.tavern.common.model.User;
import com.tavern.server.data.repository.MessageRepository;
import com.tavern.server.data.repository.RoomMemberRepository;
import com.tavern.server.data.repository.UserRepository;
import com.tavern.server.data.repository.UserUnreadMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;

public class MessageManager {
    private static final Logger logger = LoggerFactory.getLogger(MessageManager.class);

    private MessageManager() {}

    public static Message sendMessage(Message message) {
        try {
            // Validate sender exists
            if (UserRepository.getUserById(message.getSenderId()) == null) {
                logger.warn("Error sending message: Sender ID {} does not exist", message.getSenderId());
                return null;
            }

            if (message.isDirect()) {
                // Validate receiver exists for direct messages
                if (UserRepository.getUserById(message.getReceiverId()) == null) {
                    logger.warn("Error sending message: Direct message receiver ID {} does not exist", message.getReceiverId());
                    return null;
                }
            } else {
                // Validate room exists and user is a member for room messages
                if (!RoomMemberRepository.isUserInRoom(message.getSenderId(), message.getRoomId())) {
                    logger.warn("Error sending message: Sender ID {} is not a member of room ID {}", 
                            message.getSenderId(), message.getRoomId());
                    return null;
                }
            }

            // Create the message
            Message createdMessage = MessageRepository.createMessage(message);
            
            if (createdMessage != null) {
                // For direct messages, mark as unread for the receiver
                if (message.isDirect()) {
                    UserUnreadMessageRepository.markMessageAsUnread(message.getReceiverId(), createdMessage.getId());
                } else {
                    // For room messages, mark as unread for all room members except the sender
                    try {
                        List<com.tavern.common.model.User> roomMembers = RoomMemberRepository.getRoomMembers(message.getRoomId());
                        for (com.tavern.common.model.User member : roomMembers) {
                            if (member.getId() != message.getSenderId()) {
                                UserUnreadMessageRepository.markMessageAsUnread(member.getId(), createdMessage.getId());
                            }
                        }
                    } catch (SQLException e) {
                        logger.error("Error marking message as unread for room members: {}", e.getMessage(), e);
                    }
                }
            }
            
            return createdMessage;
        } catch (SQLException e) {
            logger.error("Error sending message: {}", e.getMessage(), e);
            return null;
        }
    }

    public static Message createMessage(Message message) {
        try {
            return MessageRepository.createMessage(message);
        } catch (SQLException e) {
            logger.error("Error creating message: {}", e.getMessage(), e);
            return null;
        }
    }

    public static boolean markMessageAsUnread(int userId, Message message) {
        try {
            return UserUnreadMessageRepository.markMessageAsUnread(userId, message.getId());
        } catch (SQLException e) {
            logger.error("Error marking message as unread for user {}: {}", userId, e.getMessage(), e);
            return false;
        }
    }

    public static boolean markMessageAsUnread(User user, Message message) {
        try {
            return UserUnreadMessageRepository.markMessageAsUnread(user.getId(), message.getId());
        } catch (SQLException e) {
            logger.error("Error marking message as unread for user {}: {}", user.getUsername(), e.getMessage(), e);
            return false;
        }
    }

    public static List<Message> getMessagesForRoom(int roomId, int limit, int offset) {
        try {
            return MessageRepository.getMessagesForRoom(roomId, limit, offset);
        } catch (SQLException e) {
            logger.error("Error getting messages for room {}: {}", roomId, e.getMessage(), e);
            return null;
        }
    }

    public static List<Message> getDirectMessages(int user1Id, int user2Id, int limit, int offset) {
        try {
            return MessageRepository.getDirectMessages(user1Id, user2Id, limit, offset);
        } catch (SQLException e) {
            logger.error("Error getting direct messages between users {} and {}: {}", 
                    user1Id, user2Id, e.getMessage(), e);
            return null;
        }
    }

    public static boolean deleteMessage(int messageId) {
        try {
            // First, remove all unread message records for this message
            try (java.sql.Connection conn = com.tavern.server.utils.ServerDatabaseManager.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(
                         "DELETE FROM user_unread_message WHERE message_id = ?")) {
                pstmt.setInt(1, messageId);
                pstmt.executeUpdate();
            }
            
            // Then delete the message itself
            return MessageRepository.deleteMessage(messageId);
        } catch (SQLException e) {
            logger.error("Error deleting message {}: {}", messageId, e.getMessage(), e);
            return false;
        }
    }

    public static List<Message> getUnreadMessages(int userId) {
        try {
            return UserUnreadMessageRepository.getUnreadMessages(userId);
        } catch (SQLException e) {
            logger.error("Error getting unread messages for user {}: {}", userId, e.getMessage(), e);
            return null;
        }
    }

    public static boolean markMessageAsRead(int userId, int messageId) {
        try {
            return UserUnreadMessageRepository.markMessageAsRead(userId, messageId);
        } catch (SQLException e) {
            logger.error("Error marking message {} as read for user {}: {}", 
                    messageId, userId, e.getMessage(), e);
            return false;
        }
    }

    public static boolean markAllRoomMessagesAsRead(int userId, int roomId) {
        try {
            return UserUnreadMessageRepository.markAllMessagesAsRead(userId, roomId);
        } catch (SQLException e) {
            logger.error("Error marking all messages as read for user {} in room {}: {}", 
                    userId, roomId, e.getMessage(), e);
            return false;
        }
    }

    public static boolean markAllDirectMessagesAsRead(int userId, int otherUserId) {
        try {
            return UserUnreadMessageRepository.markAllDirectMessagesAsRead(userId, otherUserId);
        } catch (SQLException e) {
            logger.error("Error marking all direct messages as read between users {} and {}: {}", 
                    userId, otherUserId, e.getMessage(), e);
            return false;
        }
    }

    public static int getUnreadMessageCount(int userId) {
        try {
            return UserUnreadMessageRepository.getUnreadMessageCount(userId);
        } catch (SQLException e) {
            logger.error("Error getting unread message count for user {}: {}", userId, e.getMessage(), e);
            return 0;
        }
    }

    public static int getUnreadMessageCountForRoom(int userId, int roomId) {
        try {
            return UserUnreadMessageRepository.getUnreadMessageCountForRoom(userId, roomId);
        } catch (SQLException e) {
            logger.error("Error getting unread message count for user {} in room {}: {}", 
                    userId, roomId, e.getMessage(), e);
            return 0;
        }
    }

    public static int getUnreadDirectMessageCount(int userId, int otherUserId) {
        try {
            return UserUnreadMessageRepository.getUnreadDirectMessageCount(userId, otherUserId);
        } catch (SQLException e) {
            logger.error("Error getting unread direct message count between users {} and {}: {}", 
                    userId, otherUserId, e.getMessage(), e);
            return 0;
        }
    }

    public static void cleanupMessages() {
        UserUnreadMessageRepository.cleanupMessages();
    }
}
