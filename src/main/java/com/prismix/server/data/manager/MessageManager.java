package com.prismix.server.data.manager;

import com.prismix.common.model.Message;
import com.prismix.common.model.User;
import com.prismix.server.data.repository.MessageRepository;
import com.prismix.server.data.repository.RoomMemberRepository;
import com.prismix.server.data.repository.UserRepository;
import com.prismix.server.data.repository.UserUnreadMessageRepository;

import java.sql.SQLException;
import java.util.List;

public class MessageManager {

    private MessageManager() {}

    public static Message sendMessage(Message message) {
        try {
            // Validate sender exists
            if (UserRepository.getUserById(message.getSenderId()) == null) {
                System.out.println("Error sending message: Sender does not exist");
                return null;
            }

            if (message.isDirect()) {
                // Validate receiver exists for direct messages
                if (UserRepository.getUserById(message.getReceiverId()) == null) {
                    System.out.println("Error sending message: Direct message receiver does not exist");
                    return null;
                }
            } else {
                // Validate room exists and user is a member for room messages
                if (!RoomMemberRepository.isUserInRoom(message.getSenderId(), message.getRoomId())) {
                    System.out.println("Error sending message: Sender is not a member of the room");
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
                        List<com.prismix.common.model.User> roomMembers = RoomMemberRepository.getRoomMembers(message.getRoomId());
                        for (com.prismix.common.model.User member : roomMembers) {
                            if (member.getId() != message.getSenderId()) {
                                UserUnreadMessageRepository.markMessageAsUnread(member.getId(), createdMessage.getId());
                            }
                        }
                    } catch (SQLException e) {
                        System.err.println("Error marking message as unread for room members: " + e.getMessage());
                    }
                }
            }
            
            return createdMessage;
        } catch (SQLException e) {
            System.err.println("Error sending message: " + e.getMessage());
            return null;
        }
    }

    public static Message createMessage(Message message) {
        try {
            return MessageRepository.createMessage(message);
        } catch (SQLException e) {
            System.err.println("Error creating messages: " + e.getMessage());
            return null;
        }
    }

    public static boolean markMessageAsUnread(int userId, Message message) {
        try {
            return UserUnreadMessageRepository.markMessageAsUnread(userId, message.getId());
        } catch (SQLException e) {
            System.out.println("Error marking message as unread for user: " + e.getMessage());
            return false;
        }
    }

    public static boolean markMessageAsUnread(User user, Message message) {
        try {
            return UserUnreadMessageRepository.markMessageAsUnread(user.getId(), message.getId());
        } catch (SQLException e) {
            System.out.println("Error marking message as unread for user: " + e.getMessage());
            return false;
        }
    }

    public static List<Message> getMessagesForRoom(int roomId, int limit, int offset) {
        try {
            return MessageRepository.getMessagesForRoom(roomId, limit, offset);
        } catch (SQLException e) {
            System.err.println("Error getting messages for room: " + e.getMessage());
            return null;
        }
    }

    public static List<Message> getDirectMessages(int user1Id, int user2Id, int limit, int offset) {
        try {
            return MessageRepository.getDirectMessages(user1Id, user2Id, limit, offset);
        } catch (SQLException e) {
            System.err.println("Error getting direct messages: " + e.getMessage());
            return null;
        }
    }

    public static boolean deleteMessage(int messageId) {
        try {
            // First, remove all unread message records for this message
            try (java.sql.Connection conn = com.prismix.server.utils.ServerDatabaseManager.getConnection();
                 java.sql.PreparedStatement pstmt = conn.prepareStatement(
                         "DELETE FROM user_unread_message WHERE message_id = ?")) {
                pstmt.setInt(1, messageId);
                pstmt.executeUpdate();
            }
            
            // Then delete the message itself
            return MessageRepository.deleteMessage(messageId);
        } catch (SQLException e) {
            System.err.println("Error deleting message: " + e.getMessage());
            return false;
        }
    }

    public static List<Message> getUnreadMessages(int userId) {
        try {
            return UserUnreadMessageRepository.getUnreadMessages(userId);
        } catch (SQLException e) {
            System.err.println("Error getting unread messages: " + e.getMessage());
            return null;
        }
    }

    public static boolean markMessageAsRead(int userId, int messageId) {
        try {
            return UserUnreadMessageRepository.markMessageAsRead(userId, messageId);
        } catch (SQLException e) {
            System.err.println("Error marking message as read: " + e.getMessage());
            return false;
        }
    }

    public static boolean markAllRoomMessagesAsRead(int userId, int roomId) {
        try {
            return UserUnreadMessageRepository.markAllMessagesAsRead(userId, roomId);
        } catch (SQLException e) {
            System.err.println("Error marking all room messages as read: " + e.getMessage());
            return false;
        }
    }

    public static boolean markAllDirectMessagesAsRead(int userId, int otherUserId) {
        try {
            return UserUnreadMessageRepository.markAllDirectMessagesAsRead(userId, otherUserId);
        } catch (SQLException e) {
            System.err.println("Error marking all direct messages as read: " + e.getMessage());
            return false;
        }
    }

    public static int getUnreadMessageCount(int userId) {
        try {
            return UserUnreadMessageRepository.getUnreadMessageCount(userId);
        } catch (SQLException e) {
            System.err.println("Error getting unread message count: " + e.getMessage());
            return 0;
        }
    }

    public static int getUnreadMessageCountForRoom(int userId, int roomId) {
        try {
            return UserUnreadMessageRepository.getUnreadMessageCountForRoom(userId, roomId);
        } catch (SQLException e) {
            System.err.println("Error getting unread message count for room: " + e.getMessage());
            return 0;
        }
    }

    public static int getUnreadDirectMessageCount(int userId, int otherUserId) {
        try {
            return UserUnreadMessageRepository.getUnreadDirectMessageCount(userId, otherUserId);
        } catch (SQLException e) {
            System.err.println("Error getting unread direct message count: " + e.getMessage());
            return 0;
        }
    }

    public static void cleanupMessages() {
        UserUnreadMessageRepository.cleanupMessages();
    }
}
