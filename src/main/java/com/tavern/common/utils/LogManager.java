package com.tavern.common.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Manages logging configuration for the application.
 * This class configures logging based on properties loaded from the PropertyFileLoader.
 */
public class LogManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LogManager.class);

    /**
     * Configures logging based on the provided properties.
     *
     * @param properties the properties containing logging configuration
     */
    public static void configureLogging(PropertyFileLoader properties) {
        // Get log level from properties or default to INFO
        String logLevel = properties.getProperty("logging.level", "INFO");
        
        // Get log file from properties or default
        String logFile = properties.getProperty("logging.file", "logs/tavern.log");
        
        // Set system properties for logback.xml
        System.setProperty("logging.level", logLevel);
        System.setProperty("logging.file", logFile);
        
        LOGGER.info("Logging configured: level={}, file={}", logLevel, logFile);
    }
} 