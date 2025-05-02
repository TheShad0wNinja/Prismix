package com.prismix.client.core;

public class ApplicationContext {
    private static ApplicationContext instance;

    private final Client client;
    private final AuthManager authManager;
    private final RoomManager roomManager;
    private final EventBus eventBus;
    //    private final ThemeManager themeManager;

    private ApplicationContext() {
        this.client = new Client();
        this.eventBus = new EventBus();
        this.authManager = new AuthManager(eventBus);
        this.roomManager = new RoomManager(eventBus, authManager);
//        this.themeManager = new ThemeManager();
    }

    private static ApplicationContext getInstance() {
        if (instance == null) {
            instance = new ApplicationContext();
        }
        return instance;
    }

    public static Client getClient() {
        return getInstance().client;
    }

    public static AuthManager getAuthManager() {
        return getInstance().authManager;
    }

    public static EventBus getEventBus() {
        return getInstance().eventBus;
    }

    public static RoomManager getRoomManager() {
        return getInstance().roomManager;
    }
//    public ThemeManager getThemeManager() { return themeManager; }
}