package com.tavern.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Utility class for managing application data files.
 * This class provides methods to load files from various locations:
 * 1. External file system (absolute path)
 * 2. Next to the JAR file
 * 3. From classpath resources
 */
public class AppDataManager {
    private static final Logger logger = LoggerFactory.getLogger(AppDataManager.class.getName());
    private static final String APP_NAME = "Tavern";

    private AppDataManager() { }
    
    /**
     * Attempts to load a file from both the file system and classpath.
     * First checks if the file exists at the absolute path or next to the JAR,
     * then falls back to loading from classpath if not found.
     *
     * @param filePath the path to the file
     * @return an InputStream for the file
     * @throws IOException if the file could not be found in any location
     */
    public static InputStream loadFile(String filePath) throws IOException {
        return loadFile(filePath, AppDataManager.class);
    }
    
    /**
     * Attempts to load a file from both the file system and classpath.
     * First checks if the file exists at the absolute path or next to the JAR,
     * then falls back to loading from classpath if not found.
     *
     * @param filePath the path to the file
     * @param contextClass the class to use for classpath resource loading
     * @return an InputStream for the file
     * @throws IOException if the file could not be found in any location
     */
    public static InputStream loadFile(String filePath, Class<?> contextClass) throws IOException {
        // First try to load as an absolute path
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            logger.info("Loading file from absolute path: {}", file.getAbsolutePath());
            return new FileInputStream(file);
        }
        
        // Try to load relative to the JAR location
        try {
            URL location = contextClass.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                Path jarPath = Paths.get(location.toURI());
                // If in JAR, get parent directory; otherwise use current directory
                Path parentDir = jarPath.getFileName().toString().endsWith(".jar") 
                        ? jarPath.getParent() : jarPath;
                
                if (parentDir != null) {
                    File externalFile = parentDir.resolve(filePath).toFile();
                    if (externalFile.exists() && externalFile.isFile()) {
                        logger.info("Loading file from JAR directory: {}", externalFile.getAbsolutePath());
                        return new FileInputStream(externalFile);
                    }
                }
            }
        } catch (URISyntaxException e) {
            logger.warn("Failed to determine application location", e);
        }
        
        // Finally, try to load from classpath
        InputStream classpathStream = contextClass.getClassLoader().getResourceAsStream(filePath);
        if (classpathStream != null) {
            logger.info("Loading file from classpath: {}", filePath);
            return classpathStream;
        }
        
        throw new IOException("Could not find file: " + filePath);
    }


    public static Path getAppDataPath() {
        String os = System.getProperty("os.name").toLowerCase();
        Path appDataPath;

        if (os.contains("win")) {
            // Windows: %APPDATA%\Tavern
            appDataPath = Paths.get(System.getenv("APPDATA"), APP_NAME);
        } else if (os.contains("mac")) {
            // macOS: ~/Library/Application Support/Tavern
            appDataPath = Paths.get(System.getProperty("user.home"), "Library", "Application Support", APP_NAME);
        } else {
            // Linux and others: ~/.tavern
            appDataPath = Paths.get(System.getProperty("user.home"), "." + APP_NAME.toLowerCase());
        }

        return appDataPath;
    }
}