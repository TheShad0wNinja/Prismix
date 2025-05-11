# Tavern Logging Guide

This document describes how to use the logging system in the Tavern application.

## Configuration

Logging is configured through property files:

- `server/server.properties` for server-side logging
- `client/client.properties` for client-side logging

Each properties file can contain these logging settings:

```properties
# Valid levels: TRACE, DEBUG, INFO, WARN, ERROR
logging.level=INFO
logging.file=logs/app.log
```

The configuration is loaded when the application starts.

## Log Levels

Logging levels in order of verbosity (most to least):

1. **TRACE** - Very detailed debug info, including method entry/exit
2. **DEBUG** - Detailed information useful for debugging
3. **INFO** - General information about application progress (default)
4. **WARN** - Warning situations that might cause problems
5. **ERROR** - Errors that prevent normal operation

Production systems should typically use INFO, WARN, or ERROR levels. 
Debug and trace are useful during development but generate large log files.

## Using Logging in Code

### Adding Logging to a Class

```java
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyClass {
    private static final Logger logger = LoggerFactory.getLogger(MyClass.class);
    
    public void myMethod() {
        // Simple message
        logger.info("Processing started");
        
        // With parameters (preferred method)
        logger.info("User {} logged in from {}", username, ipAddress);
        
        try {
            // Do something
        } catch (Exception e) {
            // Log exceptions with stack trace
            logger.error("Failed to process request", e);
        }
    }
}
```

### Choosing the Right Log Level

- **ERROR** - Use for serious failures that prevent operation
- **WARN** - Use for situations that aren't errors but might cause problems
- **INFO** - Use for significant events in normal operation
- **DEBUG** - Use for detailed information useful during development
- **TRACE** - Use for very verbose diagnostic information

### Performance Best Practices

- Use parameterized logging with `{}` placeholders instead of string concatenation
- Check if a log level is enabled before executing expensive operations:

```java
if (logger.isDebugEnabled()) {
    logger.debug("Complex calculation result: {}", performExpensiveCalculation());
}
```

## Log File Management

Log files are automatically rotated daily and compressed. Old log files are deleted after 30 days, and the total log size is capped at 100MB to prevent disk space issues.

## Viewing Logs

- Console logs are displayed in the terminal where the application is running
- File logs are stored in the location specified by `logging.file` property
- You can use tools like `less`, `grep`, or `tail -f` to view log files 