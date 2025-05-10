package com.tavern.client.handlers;

import com.tavern.common.model.network.NetworkMessage;

public interface ResponseHandler {
    void handleResponse(NetworkMessage message);
}
