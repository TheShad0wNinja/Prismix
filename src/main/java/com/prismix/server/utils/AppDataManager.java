package com.prismix.server.utils;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AppDataManager {
    private static final String APP_NAME = "Prismix";

    public static Path getAppDataPath() {
        String os = System.getProperty("os.name").toLowerCase();
        Path appDataPath;

        if (os.contains("win")) {
            // Windows: %APPDATA%\Prismix
            appDataPath = Paths.get(System.getenv("APPDATA"), APP_NAME);
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Prismix
            appDataPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
        } else {
            // Linux and others: ~/.prismix
            appDataPath = Paths.get(System.getProperty("user.home"), "." + APP_NAME.toLowerCase());
        }

        return appDataPath;
    }

    public static Path getUploadsPath() {
        return getAppDataPath().resolve("uploads");
    }
}