package com.prismix.client.core.handlers;

import com.prismix.common.model.network.NetworkMessage;

public interface ResponseHandler {
    void handleResponse(NetworkMessage message);
}
