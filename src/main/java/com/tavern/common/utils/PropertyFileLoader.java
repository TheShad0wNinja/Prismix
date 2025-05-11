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
import java.util.Properties;

/**
 * Utility class for loading properties files.
 * This class prioritizes loading from external file system first, then
 * falls back to classpath (resources folder) for any missing properties.
 */
public class PropertyFileLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertyFileLoader.class);
    private static final String DEFAULT_PROPERTIES_FILENAME = "application.properties";
    
    private final String propertiesFilename;
    private final Properties properties;
    
    /**
     * Creates a PropertyFileLoader with the default properties filename.
     */
    public PropertyFileLoader() {
        this(DEFAULT_PROPERTIES_FILENAME);
    }
    
    /**
     * Creates a PropertyFileLoader with a custom properties filename.
     * 
     * @param propertiesFilename the name of the properties file to load
     */
    public PropertyFileLoader(String propertiesFilename) {
        this.propertiesFilename = propertiesFilename;
        this.properties = new Properties();
        loadProperties();
    }
    
    /**
     * Attempts to load properties in the following order:
     * 1. From file system next to the JAR or application directory (external)
     * 2. From classpath (resources folder) for any missing properties (internal)
     */
    private void loadProperties() {
        // First load default properties from classpath (used as fallback values)
        Properties defaultProps = new Properties();
        boolean defaultsLoaded = loadDefaultsFromClasspath(defaultProps);
        
        // Then try to load from external file system (these override defaults)
        boolean externalLoaded = loadFromFileSystem();
        
        // If external file wasn't loaded and defaults were loaded, copy defaults to main properties
        if (!externalLoaded && defaultsLoaded) {
            properties.putAll(defaultProps);
            logger.info("Using only default properties from classpath: {}", propertiesFilename);
        }
        
        // If external was loaded but some properties might be missing, add defaults as fallback
        if (externalLoaded && defaultsLoaded) {
            // For each default property
            for (String key : defaultProps.stringPropertyNames()) {
                // If the key doesn't exist in the main properties, add it
                if (!properties.containsKey(key)) {
                    properties.setProperty(key, defaultProps.getProperty(key));
                    logger.info("Using default value for property: {}", key);
                }
            }
            logger.info("Merged external properties with defaults from classpath");
        }
        
        if (properties.isEmpty()) {
            logger.warn("Could not load properties from either file system or classpath: {}", propertiesFilename);
        }
    }
    
    /**
     * Attempts to load default properties from the classpath (resources folder).
     * 
     * @param defaultProps the Properties object to load into
     * @return true if properties were successfully loaded, false otherwise
     */
    private boolean loadDefaultsFromClasspath(Properties defaultProps) {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(propertiesFilename)) {
            if (input != null) {
                defaultProps.load(input);
                logger.info("Loaded default properties from classpath: {}", propertiesFilename);
                return true;
            }
        } catch (IOException e) {
            logger.warn("Failed to load default properties from classpath: {}", propertiesFilename, e);
        }
        return false;
    }
    
    /**
     * Attempts to load properties from the file system next to the JAR file or application directory.
     * 
     * @return true if properties were successfully loaded, false otherwise
     */
    private boolean loadFromFileSystem() {
        // Try to locate the directory containing the JAR file
        File externalPropertiesFile = null;
        
        try {
            URL location = getClass().getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                Path jarPath = Paths.get(location.toURI());
                // If in JAR, get parent directory; otherwise use current directory
                Path parentDir = jarPath.getFileName().toString().endsWith(".jar") 
                        ? jarPath.getParent() : jarPath;
                
                if (parentDir != null) {
                    externalPropertiesFile = parentDir.resolve(propertiesFilename).toFile();
                }
            }
        } catch (URISyntaxException e) {
            logger.warn("Failed to determine application location", e);
        }
        
        // If couldn't determine JAR location, try current working directory
        if (externalPropertiesFile == null) {
            externalPropertiesFile = new File(propertiesFilename);
        }
        
        // Load properties if file exists
        if (externalPropertiesFile.exists() && externalPropertiesFile.canRead()) {
            try (FileInputStream fis = new FileInputStream(externalPropertiesFile)) {
                properties.load(fis);
                logger.info("Loaded properties from file system: {}", externalPropertiesFile.getAbsolutePath());
                return true;
            } catch (IOException e) {
                logger.warn("Failed to load properties from file system: {}", externalPropertiesFile.getAbsolutePath(), e);
            }
        }
        
        return false;
    }
    
    /**
     * Gets a property value.
     * 
     * @param key the property key
     * @return the property value, or null if not found
     */
    public String getProperty(String key) {
        return properties.getProperty(key);
    }
    
    /**
     * Gets a property value with a default value if the key is not found.
     * 
     * @param key the property key
     * @param defaultValue the default value to return if the key is not found
     * @return the property value, or the default value if not found
     */
    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }
    
    /**
     * Returns all loaded properties.
     * 
     * @return the properties
     */
    public Properties getProperties() {
        return properties;
    }
} 