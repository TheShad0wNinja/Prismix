# Tavern

A secure chat and collaboration application with client-server architecture.

## Configuration

### Property Files

The application uses property files for configuration. Template files are provided in the repository:

- `src/main/resources/common/application.properties.template` - Common settings for both client and server
- `src/main/resources/client/client.properties.template` - Client-specific settings
- `src/main/resources/server/server.properties.template` - Server-specific settings

To configure the application:

1. Copy each `.properties.template` file to a new file with the same name but without the `.template` extension
2. Edit the properties in these files according to your environment

Example:
```bash
cp src/main/resources/server/server.properties.template src/main/resources/server/server.properties
cp src/main/resources/client/client.properties.template src/main/resources/client/client.properties
cp src/main/resources/common/application.properties.template src/main/resources/common/application.properties
```

### External Configuration

The application will first look for property files in the following locations:

1. Next to the JAR file when deployed
2. In the classpath resources (inside the JAR)

This allows you to override the default configuration by placing a properties file next to the JAR file.

### SSL Configuration

For secure communication, the application requires SSL certificates:

- Server needs a keystore file (JKS format)
- Client needs a truststore file (JKS format)

You can generate self-signed certificates for development using the provided script:
```bash
./generate_ssl_certs.sh
```

## Building and Running

### Prerequisites
- Java 11 or higher
- Maven 3.6 or higher

### Build
```bash
mvn clean package
```

### Run Server
```bash
java -jar server/target/tavern-server.jar
```

### Run Client
```bash
java -jar client/target/tavern-client.jar
``` 