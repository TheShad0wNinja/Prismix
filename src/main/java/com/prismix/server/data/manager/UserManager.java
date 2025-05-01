package com.prismix.server.data.manager;

import com.prismix.common.model.User;
import com.prismix.server.data.repository.UserRepository;

import java.sql.SQLException;

public class UserManager {
    private final UserRepository userRepository;

    public UserManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean userExists(String username) throws SQLException {
        try {
            User user = userRepository.getUserByUsername(username);
            return user != null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public User login(String username) {
        try {
            return userRepository.getUserByUsername(username);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public User registerUser(String username, String displayName, byte[] avatar) {
        try {
            if (userRepository.getUserByUsername(username) != null) {
                System.out.println("User already exists");
                return null;
            }

            User user = new User(username, displayName, avatar);

            System.out.println("User created");
            return userRepository.createUser(user);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
