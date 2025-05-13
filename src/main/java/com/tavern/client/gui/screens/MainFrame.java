package com.tavern.client.gui.screens;

import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.layout.BaseLayout;
import com.tavern.client.gui.layout.ChatScreen;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.Queue;

public class MainFrame extends JFrame implements EventListener {
    private final Queue<Runnable> screenSwitchQueue;
    private final JPanel mainPanel;
    private BaseLayout currentLayout;
    private boolean isSwitching;

    public enum AppScreen {
        LOGIN_SCREEN,
        SIGNUP_SCREEN,
        CHAT_SCREEN,
        SETTING_SCREEN,
    }

    public MainFrame() {
        screenSwitchQueue = new LinkedList<>();
        isSwitching = false;

        setTitle("Tavern");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 800);
        setLayout(new BorderLayout());
        
        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        
        ApplicationContext.getEventBus().subscribe(this);
        
//        switchToLoginScreen();
        switchScreen(AppScreen.LOGIN_SCREEN);
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
    
//    private void switchToLoginScreen() {
//        queueScreenSwitch(() -> {
//            clearPanel();
//            LoginScreen newPanel = new LoginScreen();
//            mainPanel.add(newPanel, BorderLayout.CENTER);
//            updateUI();
//        });
//    }
//
//    private void switchToChatLayout(Room room, ArrayList<Room> rooms) {
//        queueScreenSwitch(() -> {
//            clearPanel();
//            currentLayout = new ChatScreen(room, rooms);
//            mainPanel.add(currentLayout, BorderLayout.CENTER);
//            updateUI();
//        });
//    }
//
//    private void queueScreenSwitch(Runnable switchAction) {
//        synchronized (screenSwitchQueue) {
//            screenSwitchQueue.add(switchAction);
//            if (!isSwitching)
//                processNextSwitch();
//        }
//    }
//
//    private void processNextSwitch() {
//        Runnable nextSwitchAction;
//        synchronized (screenSwitchQueue) {
//            nextSwitchAction = screenSwitchQueue.poll();
//            if (nextSwitchAction == null) {
//                isSwitching = false;
//                return;
//            }
//            isSwitching = true;
//        }
//
//        SwingUtilities.invokeLater(() -> {
//            try {
//                nextSwitchAction.run();
//            } finally {
//                processNextSwitch();
//            }
//        });
//    }

    private void switchScreen(AppScreen screen) {
        JPanel newScreen = switch (screen)  {
            case LOGIN_SCREEN -> new LoginScreen();
            case SIGNUP_SCREEN -> new SignupScreen();
            case CHAT_SCREEN -> new ChatScreen();
            case SETTING_SCREEN -> null;
        };

        if (newScreen == null || mainPanel == null)
            return;

        SwingUtilities.invokeLater(() -> {
            clearPanel();
            mainPanel.add(newScreen, BorderLayout.CENTER);
            mainPanel.revalidate();
            mainPanel.repaint();

            revalidate();
        });
    }

    @Override
    public void onEvent(ApplicationEvent event) {
//        if (event.type() == ApplicationEvent.Type.SWITCH_SCREEN) {
//            switchScreen((AppScreen) event.data());
//        }
//        switch (event.type()) {
//            case USER_LOGGED_IN -> {
//                Room defaultRoom = new Room(-1, "General", null);
//                ApplicationContext.getRoomHandler().updateRooms();
//                switchToChatLayout(defaultRoom, new ArrayList<>());
//            }
//            case ROOM_SELECTED -> {
//                Room selectedRoom = (Room) event.data();
//                if (selectedRoom == null)
//                    return;
//
//                if (currentLayout instanceof ChatLayout layout) {
//                    queueScreenSwitch(() -> {
//                        layout.setRoom(selectedRoom);
//                    });
//                } else {
//                    switchToChatLayout(selectedRoom, ApplicationContext.getRoomHandler().getRooms());
//                }
//            }
//            case ROOM_LIST_UPDATED -> {
//                ArrayList<Room> rooms = (ArrayList<Room>) event.data();
//                if (rooms == null)
//                    return;
//
//                if (currentLayout instanceof ChatLayout layout) {
//                    queueScreenSwitch(() -> {
//                        layout.setRooms(rooms);
//                    });
//                } else {
//                    Room defaultRoom = new Room(-1, "General", null);
//                    switchToChatLayout(defaultRoom, rooms);
//                }
//            }
//        }
    }
}
