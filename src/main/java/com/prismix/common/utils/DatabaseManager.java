package com.prismix.common.utils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.File;
import java.net.URL;

public abstract class DatabaseManager {
    protected static DatabaseManager instance;
    protected final String dbName;

    protected DatabaseManager(String dbName) {
        this.dbName = dbName;
        initDatabase();
    }

    public Connection getDriverConnection() throws SQLException {
        try {
            String url = getDatabaseUrl(dbName);
            return DriverManager.getConnection(url);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    protected abstract void initDatabase();

    public void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }

    public boolean executeUpdate(String sqlStatement) {
        try (Connection conn = getDriverConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlStatement);
            return true;
        } catch (SQLException e) {
            System.out.println("SQL Exception: " + e.getMessage());
            return false;
        }
    }

    /**
     * Constructs the JDBC URL for the SQLite database using the file system path.
     * @param databaseName The name of the database file (e.g., "client_data.db").
     * @return The JDBC URL string.
     * @throws IOException If the database file path cannot be determined.
     */
    public static String getDatabaseUrl(String databaseName) throws IOException {
        Path dbFilePath = getDatabaseFilePath(databaseName);
        return "jdbc:sqlite:" + dbFilePath.toAbsolutePath().toString();
    }

    /**
     * Constructs the full file system path for the SQLite database file
     * within the application data directory.
     * @param databaseName The name of the database file (e.g., "client_data.db").
     * @return The full file system Path to the database file.
     * @throws IOException If the application data directory cannot be determined or created.
     */
    public static Path getDatabaseFilePath(String databaseName) throws IOException {
        Path appDataDir = getAppDataDirectory();
        return appDataDir.resolve(databaseName);
    }

    /**
     * Determines the base directory for application data based on the operating system.
     * Attempts to use platform-appropriate application data directories.
     * @return The Path to the application data directory.
     * @throws IOException If the directory cannot be created.
     */
    public static Path getAppDataDirectory() throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        Path baseDir;
        final String APP_FOLDER_NAME = "prismix";

        if (os.contains("win")) {
            // Windows: Use AppData\Roaming
            String appData = System.getenv("APPDATA");
            if (appData != null) {
                baseDir = Paths.get(appData, APP_FOLDER_NAME);
            } else {
                // Fallback if APPDATA is not set
                baseDir = Paths.get(System.getProperty("user.home"), "AppData", "Roaming", APP_FOLDER_NAME);
            }
        } else if (os.contains("mac")) {
            // macOS: Use Library/Application Support
            baseDir = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_FOLDER_NAME);
        } else {
            // Linux/Unix: Use a hidden directory in the user's home
            baseDir = Paths.get(System.getProperty("user.home"), "." + APP_FOLDER_NAME.toLowerCase());
        }

        // Ensure the directory exists
        if (!Files.exists(baseDir)) {
            Files.createDirectories(baseDir);
        }
//            System.out.println("Created application data directory: " + baseDir.toAbsolutePath());
//        } else {
//            System.out.println("Using existing application data directory: " + baseDir.toAbsolutePath());
//        }

        return baseDir;
    }}