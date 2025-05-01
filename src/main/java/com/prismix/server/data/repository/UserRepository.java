package com.prismix.server.data.repository;

import com.prismix.common.model.User;
import com.prismix.server.utils.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {

    public User createUser(User user) throws SQLException {
        String sql = "INSERT INTO user (username, display_name) VALUES (?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getDisplayName());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Failed to insert user");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }

            System.out.println("User inserted");
            return user;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw e;
        }
    }

    public User createUserWithAvatar(User user) throws SQLException {
        String sql = "INSERT INTO user (username, display_name, avatar) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getDisplayName());
            stmt.setBytes(2, user.getAvatar());

            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Failed to insert user");
            }

            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) {
                    user.setId(rs.getInt(1));
                }
            }
            return user;
        } catch (SQLException e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw e;
        }
    }

    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM user WHERE username = ?";

        try (Connection conn = DatabaseManager.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    int id = rs.getInt("id");
                    String displayName = rs.getString("display_name");
                    byte[] avatar = rs.getBytes("avatar");

                    return new User(id, username, displayName, avatar);
                }
            }
        } catch (SQLException ex) {
            System.out.println("Unable to get user: " + ex.getMessage());
            throw ex;
        };

        return null;
    }

    public User getUserById(int id) throws SQLException {
        String sql = "SELECT * FROM user WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    String username = rs.getString("username");
                    String displayName = rs.getString("display_name");
                    byte[] avatar = rs.getBytes("avatar");

                    return new User(id, username, displayName, avatar);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user by ID: " + e.getMessage());
            throw e;
        }
        return null;
    }

    public void updateUserAvatar(int userId, byte[] avatarData) throws SQLException {
        String sql = "UPDATE user SET avatar = ? WHERE id = ?";
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBytes(1, avatarData);
            stmt.setInt(2, userId);

            stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error updating user avatar: " + e.getMessage());
            throw e;
        }
    }

    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try (Connection conn = DatabaseManager.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                int id = rs.getInt("id");
                String username = rs.getString("username");
                String displayName = rs.getString("display_name");
                byte[] avatar = rs.getBytes("avatar");
                users.add(new User(id, username, displayName, avatar));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            throw e;
        }
        return users;
    }
}
