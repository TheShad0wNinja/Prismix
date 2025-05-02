package com.prismix.server.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.net.URL;

public class DatabaseManager {

    private static DatabaseManager instance;
    private Connection connection = null;

    private DatabaseManager() {}

    public static synchronized DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public static Connection getConnection() throws SQLException {
        return getInstance().getConnectionReal();
    }

    private Connection getConnectionReal() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");

                final String DATABASE_NAME = PropertiesLoader.getProperty("db.name");
                final String DATABASE_DIR = PropertiesLoader.getProperty("db.dir");

                if (DATABASE_NAME == null || DATABASE_DIR == null) {
                    System.out.println("Database name or DB directory not set");
                    return null;
                }

                // Get the path to the database file within the resources directory
//                System.out.println("Connecting to database: " + DATABASE_DIR + DATABASE_NAME);
                URL resourceUrl = DatabaseManager.class.getClassLoader().getResource(DATABASE_DIR + DATABASE_NAME);

                String dbPath;
                if (resourceUrl != null) {
                    dbPath = resourceUrl.getFile();
                    // On Windows, file paths from getFile() might have a leading '/' that needs removing
                    if (System.getProperty("os.name").toLowerCase().contains("win") && dbPath.startsWith("/")) {
                        dbPath = dbPath.substring(1);
                    }
                } else {
                    // If running outside a JAR (e.g., in an IDE), construct the path
                    dbPath = "src/main/resources/" + DATABASE_DIR + DATABASE_NAME;
                }

                String dbUrl = "jdbc:sqlite:" + dbPath;
                this.connection = DriverManager.getConnection(dbUrl);

//                System.out.println("Database connection established to: " + dbUrl);

//                initializeDatabase();

            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC driver not found.");
                throw new SQLException("Failed to load SQLite JDBC driver.", e);
            }
            catch (SQLException e) {
                System.err.println("Failed to connect to the database.");
                throw e;
            }
        }
        return connection;
    }

    private void initializeDatabase() {
        // SQL statements to create tables

        String createUserTableSQL = """
                CREATE TABLE IF NOT EXISTS user (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    display_name TEXT NOT NULL,
                    avatar BLOB
                );
                """;

        String createRoomTableSQL = """
                CREATE TABLE IF NOT EXISTS room (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    name TEXT NOT NULL UNIQUE,
                    avatar BLOB
                );
                """;

        String createRoomMemberTableSQL = """
                CREATE TABLE IF NOT EXISTS room_member (
                    room_id INTEGER,
                    user_id INTEGER,
                    PRIMARY KEY (room_id, user_id),
                    FOREIGN KEY (room_id) REFERENCES room(id),
                    FOREIGN KEY (user_id) REFERENCES user(id)
                );""";
//
//        String createMessagesTableSQL = "CREATE TABLE IF NOT EXISTS messages ("
//                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + "sender_id INTEGER,"
//                + "receiver_id INTEGER," // For one-to-one
//                + "room_id INTEGER,"     // For one-to-many/many-to-many
//                + "content TEXT NOT NULL,"
//                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
//                + "FOREIGN KEY (sender_id) REFERENCES users(id),"
//                + "FOREIGN KEY (receiver_id) REFERENCES users(id),"
//                + "FOREIGN KEY (room_id) REFERENCES rooms(id)"
//                + ");";
//
//        // Add tables for file transfers if you're storing metadata
//        String createFileTransfersTableSQL = "CREATE TABLE IF NOT EXISTS file_transfers ("
//                + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
//                + "sender_id INTEGER,"
//                + "receiver_id INTEGER," // For user-to-user
//                + "room_id INTEGER,"     // For user-to-room
//                + "filename TEXT NOT NULL,"
//                + "filepath TEXT NOT NULL," // Path on the server
//                + "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP,"
//                + "FOREIGN KEY (sender_id) REFERENCES users(id),"
//                + "FOREIGN KEY (receiver_id) REFERENCES users(id),"
//                + "FOREIGN KEY (room_id) REFERENCES rooms(id)"
//                + ");";
//
//
        try (Statement statement = connection.createStatement()) {
            statement.execute(createUserTableSQL);
            statement.execute(createRoomTableSQL);
            statement.execute(createRoomMemberTableSQL);
//            statement.execute(createMessagesTableSQL);
//            statement.execute(createFileTransfersTableSQL);
            System.out.println("Database tables checked/created successfully.");
        } catch (SQLException e) {
            System.err.println("Error initializing database tables: " + e.getMessage());
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Database connection closed.");
            }
        } catch (SQLException e) {
            System.err.println("Error closing database connection: " + e.getMessage());
        }
    }

    // Add a method to handle graceful shutdown
//    public void registerShutdownHook() {
//        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
//            System.out.println("Shutting down database connection...");
//            closeConnection();
//        }));
//    }
}