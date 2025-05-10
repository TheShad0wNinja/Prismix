package com.tavern.server.utils;

import com.tavern.common.utils.DatabaseManager;
import com.tavern.server.core.Server;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ServerDatabaseManager extends DatabaseManager {
    private final static ServerDatabaseManager instance = new ServerDatabaseManager();

    private ServerDatabaseManager() {
        super(Server.properties.getProperty("db.name"));
    }

    public static Connection getConnection() throws SQLException {
        return instance.getDriverConnection();
    }

    @Override
    protected void initDatabase() {
        try (Connection conn = getDriverConnection();
                Statement stmt = conn.createStatement()) {

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

            String createUserUnreadMessageTable = """
                    CREATE TABLE IF NOT EXISTS user_unread_message (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        user_id INTEGER NOT NULL,
                        message_id INTEGER NOT NULL,
                        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (user_id) REFERENCES user(id),
                        FOREIGN KEY (message_id) REFERENCES message(id)
                    );
                    """;
            stmt.execute(createUserUnreadMessageTable);

            String createFileTransferTable = """
                    CREATE TABLE IF NOT EXISTS file_transfer (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transfer_id TEXT NOT NULL UNIQUE,
                        file_name TEXT NOT NULL,
                        file_path TEXT NOT NULL,
                        file_size INTEGER NOT NULL,
                        sender_id INTEGER NOT NULL,
                        room_id INTEGER,
                        receiver_id INTEGER,
                        is_direct INTEGER NOT NULL CHECK (is_direct IN (0, 1)),
                        status TEXT NOT NULL CHECK (status IN ('PENDING', 'IN_PROGRESS', 'COMPLETED', 'FAILED')),
                        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (sender_id) REFERENCES user(id),
                        FOREIGN KEY (room_id) REFERENCES room(id),
                        FOREIGN KEY (receiver_id) REFERENCES user(id)
                    );
                    """;
            stmt.execute(createFileTransferTable);

        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
}
