package com.prismix.server.core;

import com.prismix.common.model.network.NetworkMessage;

public interface RequestHandler {
    void handleRequest(NetworkMessage message, ClientHandler client);
}
