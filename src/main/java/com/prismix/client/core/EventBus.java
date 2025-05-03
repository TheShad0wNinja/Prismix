package com.prismix.client.core;

import javax.swing.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

public class EventBus {
    private final ConcurrentLinkedDeque<EventListener> listeners = new ConcurrentLinkedDeque<>();

    public void publish(ApplicationEvent event) {
        System.out.println("Event: " + event);
        for (EventListener listener : listeners) {
            listener.onEvent(event);
        }
    }

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }
} 