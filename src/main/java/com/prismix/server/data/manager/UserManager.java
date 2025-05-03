package com.prismix.server.data.manager;

import com.prismix.common.model.User;
import com.prismix.server.data.repository.UserRepository;

import java.sql.SQLException;

public class UserManager {

    private UserManager() { }

    public static boolean userExists(String username) throws SQLException {
        try {
            User user = UserRepository.getUserByUsername(username);
            return user != null;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static User login(String username) {
        try {
            return UserRepository.getUserByUsername(username);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }

    public static User registerUser(String username, String displayName, byte[] avatar) {
        try {
            if (UserRepository.getUserByUsername(username) != null) {
                System.out.println("User already exists");
                return null;
            }

            User user = new User(username, displayName, avatar);

            System.out.println("User created");
            return UserRepository.createUser(user);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }
    }
}
