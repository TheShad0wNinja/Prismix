package com.tavern.common.utils;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

    public static String getDatabaseUrl(String databaseName) throws IOException {
        Path dbFilePath = AppDataManager.getAppDataPath().resolve(databaseName);
        return "jdbc:sqlite:" + dbFilePath.toAbsolutePath();
    }

}