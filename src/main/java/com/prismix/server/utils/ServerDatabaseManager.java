package com.prismix.server.utils;

import com.prismix.common.utils.DatabaseManager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerDatabaseManager extends DatabaseManager {
    private final static ServerDatabaseManager instance = new ServerDatabaseManager();

    private ServerDatabaseManager() {
        super("serverChat.db");
    }

    public static Connection getConnection() throws SQLException {
        return instance.getDriverConnection();
    }

    @Override
    protected void initDatabase() {
        try (Connection conn = getDriverConnection();
             Statement stmt = conn.createStatement()) {

            // Example: Create users table
            String createUserTableSQL = """
                    CREATE TABLE IF NOT EXISTS user (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        username TEXT NOT NULL UNIQUE,
                        display_name TEXT NOT NULL,
                        avatar BLOB
                    );
                    """;
            stmt.execute(createUserTableSQL);

            String createRoomTableSQL = """
                    CREATE TABLE IF NOT EXISTS room (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        name TEXT NOT NULL UNIQUE,
                        avatar BLOB
                    );
                    """;
            stmt.execute(createRoomTableSQL);

            String createRoomMemberTableSQL = """
                    CREATE TABLE IF NOT EXISTS room_member (
                        room_id INTEGER,
                        user_id INTEGER,
                        PRIMARY KEY (room_id, user_id),
                        FOREIGN KEY (room_id) REFERENCES room(id),
                        FOREIGN KEY (user_id) REFERENCES user(id)
                    );
                    """;
            stmt.execute(createRoomMemberTableSQL);

            String createMessageTableSQL = """
                    CREATE TABLE IF NOT EXISTS message (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        sender_id INTEGER NOT NULL,
                        receiver_id INTEGER,
                        room_id INTEGER,
                        content TEXT NOT NULL,
                        direct INTEGER NOT NULL CHECK ( direct IN (0, 1) ),
                        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (sender_id) REFERENCES user(id),
                        FOREIGN KEY (receiver_id) REFERENCES room(id),
                        FOREIGN KEY (room_id) REFERENCES room(id)
                    );
                    """;
            stmt.execute(createMessageTableSQL);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
