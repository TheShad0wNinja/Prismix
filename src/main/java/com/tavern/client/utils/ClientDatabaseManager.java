package com.tavern.client.utils;

import com.tavern.common.utils.DatabaseManager;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class ClientDatabaseManager extends DatabaseManager {
    public final static ClientDatabaseManager instance = new ClientDatabaseManager();

    private ClientDatabaseManager() {
        super("clientChat.db");
    }

    public static Connection getConnection() throws SQLException {
        return instance.getDriverConnection();
    }

    @Override
    protected void initDatabase() {
        System.out.println("Initializing database");
        try (Connection conn = getDriverConnection();
                Statement stmt = conn.createStatement()) {

            String messageTableSql = """
                    CREATE TABLE IF NOT EXISTS message (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        owner_id INTEGER NOT NULL,
                        sender_id INTEGER NOT NULL,
                        direct INTEGER NOT NULL CHECK ( direct IN (0, 1) ),
                        receiver_id INTEGER,
                        room_id INTEGER,
                        content TEXT NOT NULL,
                        timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        FOREIGN KEY (sender_id) REFERENCES user(id),
                        FOREIGN KEY (receiver_id) REFERENCES room(id),
                        FOREIGN KEY (room_id) REFERENCES user(id)
                    );
                    """;
            stmt.executeUpdate(messageTableSql);

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
            stmt.executeUpdate(createFileTransferTable);

        } catch (SQLException e) {
            System.out.println("Unable to connect to database:" + e.getMessage());
        }
    }
}
