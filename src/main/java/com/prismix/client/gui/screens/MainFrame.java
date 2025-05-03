package com.prismix.client.gui.screens;

import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.gui.layout.BaseLayout;
import com.prismix.client.gui.layout.ChatLayout;
import com.prismix.common.model.Room;
import com.prismix.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

public class MainFrame extends JFrame implements EventListener {
    private final Queue<Runnable> pageSwitchQueue;
    private final JPanel mainPanel;
    private BaseLayout currentLayout;
    private boolean isSwitching;

    public MainFrame() {
        pageSwitchQueue = new LinkedList<>();
        isSwitching = false;

        setTitle("Prismix");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());
        
        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        ApplicationContext.getEventBus().subscribe(this);
        
        switchToLoginScreen();
        
        setLocationRelativeTo(null);
        setVisible(true);
    }
    
    private void clearPanel() {
        if (mainPanel != null) {
            mainPanel.removeAll();
        }
    }
    
    private void updateUI() {
        mainPanel.revalidate();
        mainPanel.repaint();
    }
    
    private void switchToLoginScreen() {
        queuePageSwitch(() -> {
            clearPanel();
            LoginScreen newPanel = new LoginScreen();
            mainPanel.add(newPanel, BorderLayout.CENTER);
            updateUI();
        });
    }
    
    private void switchToChatLayout(Room room, ArrayList<Room> rooms) {
        queuePageSwitch(() -> {
            clearPanel();
            currentLayout = new ChatLayout(room, rooms);
            mainPanel.add(currentLayout, BorderLayout.CENTER);
            updateUI();
        });
    }

    private void queuePageSwitch(Runnable switchAction) {
        synchronized (pageSwitchQueue) {
            pageSwitchQueue.add(switchAction);
            if (!isSwitching)
                processNextSwitch();
        }
    }

    private void processNextSwitch() {
        Runnable nextSwitchAction;
        synchronized (pageSwitchQueue) {
            nextSwitchAction = pageSwitchQueue.poll();
            if (nextSwitchAction == null) {
                isSwitching = false;
                return;
            }
            isSwitching = true;
        }

        SwingUtilities.invokeLater(() -> {
            try {
                nextSwitchAction.run();
            } finally {
                processNextSwitch();
            }
        });
    }
    
    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case USER_LOGGED_IN -> {
                Room defaultRoom = new Room(-1, "General", null);
                ApplicationContext.getRoomHandler().updateRooms();
                switchToChatLayout(defaultRoom, new ArrayList<>());
            }
            case ROOM_SELECTED -> {
                Room selectedRoom = (Room) event.data();
                if (selectedRoom == null)
                    return;

                if (currentLayout instanceof ChatLayout layout) {
                    queuePageSwitch(() -> {
                        layout.setRoom(selectedRoom);
                    });
                } else {
                    switchToChatLayout(selectedRoom, ApplicationContext.getRoomHandler().getRooms());
                }
            }
            case ROOM_LIST_UPDATED -> {
                ArrayList<Room> rooms = (ArrayList<Room>) event.data();
                if (rooms == null)
                    return;

                if (currentLayout instanceof ChatLayout layout) {
                    queuePageSwitch(() -> {
                        layout.setRooms(rooms);
                    });
                } else {
                    Room defaultRoom = new Room(-1, "General", null);
                    switchToChatLayout(defaultRoom, rooms);
                }
            }
        }
    }
}
