package com.tavern.server.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertiesLoader {
    private static Properties properties = null;
    private static final String CONFIG_FILE = "server/config.properties"; // Path within resources

    private PropertiesLoader() { }

    public static Properties getProperties() {
        if (properties == null) {
            properties = new Properties();
            try (InputStream input = PropertiesLoader.class.getClassLoader().getResourceAsStream(CONFIG_FILE)) {
                if (input == null) {
                    System.err.println("Sorry, unable to find " + CONFIG_FILE);
                    System.exit(1);
                } else {
                    properties.load(input);
                    System.out.println("Configuration properties loaded from: " + CONFIG_FILE);
                }
            } catch (IOException ex) {
                System.err.println("Error loading configuration properties: " + ex.getMessage());
            }
        }
        return properties;
    }

    public static String getProperty(String key) {
        Properties props = getProperties();
        return props.getProperty(key);
    }

    public static String getProperty(String key, String defaultValue) {
        Properties props = getProperties();
        return props.getProperty(key, defaultValue);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            try {
                return Integer.parseInt(value);
            } catch (NumberFormatException e) {
                System.err.println("Warning: Invalid integer format for property '" + key + "'. Using default value: " + defaultValue);
            }
        }
        return defaultValue;
    }

    public static boolean getBooleanProperty(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value != null) {
            return Boolean.parseBoolean(value);
        }
        return defaultValue;
    }
}
