package com.prismix.client.core;

import java.util.ArrayList;
import java.util.List;

public class EventBus {
    private final List<EventListener> listeners = new ArrayList<>();
    
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