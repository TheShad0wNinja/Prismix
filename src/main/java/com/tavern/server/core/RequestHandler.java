package com.tavern.server.core;

import com.tavern.common.model.network.NetworkMessage;

public interface RequestHandler {
    void handleRequest(NetworkMessage message, ClientHandler client);
}
