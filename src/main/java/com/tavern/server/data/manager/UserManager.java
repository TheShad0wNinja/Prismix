package com.tavern.server.data.manager;

import com.tavern.common.model.User;
import com.tavern.server.data.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

public class UserManager {
    private static final Logger logger = LoggerFactory.getLogger(UserManager.class);

    private UserManager() { }

    public static boolean userExists(String username) throws SQLException {
        try {
            User user = UserRepository.getUserByUsername(username);
            return user != null;
        } catch (SQLException e) {
            logger.error("Error checking if user exists: {}", e.getMessage(), e);
            return false;
        }
    }

    public static User login(String username) {
        try {
            return UserRepository.getUserByUsername(username);
        } catch (SQLException e) {
            logger.error("Error during login attempt for user {}: {}", username, e.getMessage(), e);
            return null;
        }
    }

    public static User registerUser(String username, String displayName, byte[] avatar) {
        try {
            if (UserRepository.getUserByUsername(username) != null) {
                logger.info("Registration failed: User {} already exists", username);
                return null;
            }

            User user = new User(username, displayName, avatar);

            logger.info("New user created: {}", username);
            return UserRepository.createUser(user);
        } catch (SQLException e) {
            logger.error("Error registering user {}: {}", username, e.getMessage(), e);
            return null;
        }
    }
}
