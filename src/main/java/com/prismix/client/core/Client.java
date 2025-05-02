package com.prismix.client.core;

import com.prismix.common.model.network.NetworkMessage;

public class Client {
    public Client() {
        new Thread(new MessageListener(this)).start();
    }
    public void processMessage(NetworkMessage msg) {
        switch (msg.getMessageType()) {
            case LOGIN_RESPONSE, SIGNUP_RESPONSE ->
                AuthManager.getInstance().handleMessage(msg);
        }
    }
}
