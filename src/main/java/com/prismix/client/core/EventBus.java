package com.prismix.client.core;

import javax.swing.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class EventBus {
    private final ConcurrentLinkedDeque<EventListener> listeners = new ConcurrentLinkedDeque<>();
    private final PriorityBlockingQueue<ApplicationEvent> eventQueue = new PriorityBlockingQueue<>(11, 
        Comparator.comparingInt(e -> e.type().ordinal()));
    private final ExecutorService eventProcessor = Executors.newSingleThreadExecutor();
    private final AtomicBoolean isProcessing = new AtomicBoolean(false);
    private final javax.swing.Timer batchTimer = new javax.swing.Timer(16, e -> processEvents()); // ~60fps

    public EventBus() {
        batchTimer.start();
    }

    public void publish(ApplicationEvent event) {
        eventQueue.offer(event);
        if (!isProcessing.get()) {
            processEvents();
        }
    }

    private void processEvents() {
        if (isProcessing.getAndSet(true)) {
            return;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                List<ApplicationEvent> batch = new ArrayList<>();
                eventQueue.drainTo(batch);
                
                for (ApplicationEvent event : batch) {
                    for (EventListener listener : listeners) {
                        try {
                            listener.onEvent(event);
                        } catch (Exception e) {
                            System.err.println("Error processing event: " + e.getMessage());
                        }
                    }
                }
            } finally {
                isProcessing.set(false);
            }
        });
    }

    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }

    public void unsubscribe(EventListener listener) {
        listeners.remove(listener);
    }

    public void shutdown() {
        batchTimer.stop();
        eventProcessor.shutdown();
        try {
            eventProcessor.awaitTermination(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
} 