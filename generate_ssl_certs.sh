#!/bin/bash

# This script generates SSL keys and certificates for Tavern secure communication

echo "Generating SSL certificates for Tavern..."

# Set variables
SERVER_KEYSTORE="server.keystore"
CLIENT_TRUSTSTORE="client.truststore"
PASSWORD="tavern"
VALIDITY_DAYS=3650  # 10 years
SERVER_ALIAS="tavernserver"

# Create server keystore and certificate
echo "Generating server keystore and certificate..."
keytool -genkeypair \
  -alias $SERVER_ALIAS \
  -keyalg RSA \
  -keysize 2048 \
  -validity $VALIDITY_DAYS \
  -keystore $SERVER_KEYSTORE \
  -storepass $PASSWORD \
  -keypass $PASSWORD \
  -dname "CN=Tavern Server, OU=Tavern, O=Tavern, L=Unknown, ST=Unknown, C=US"

# Export server certificate 
echo "Exporting server certificate..."
keytool -exportcert \
  -alias $SERVER_ALIAS \
  -file server.cer \
  -keystore $SERVER_KEYSTORE \
  -storepass $PASSWORD

# Create client truststore and import server certificate
echo "Creating client truststore with server certificate..."
keytool -importcert \
  -alias $SERVER_ALIAS \
  -file server.cer \
  -keystore $CLIENT_TRUSTSTORE \
  -storepass $PASSWORD \
  -noprompt

# Clean up 
rm server.cer

echo ""
echo "SSL certificate generation complete!"
echo "Created: $SERVER_KEYSTORE (for server)"
echo "Created: $CLIENT_TRUSTSTORE (for client)"
echo "Password for both keystores: $PASSWORD"
echo ""
echo "Place $SERVER_KEYSTORE in the server's working directory"
echo "Place $CLIENT_TRUSTSTORE in the client's working directory" 