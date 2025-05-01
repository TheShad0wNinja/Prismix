package com.prismix.client.gui.themes;

import java.util.ArrayList;
import java.util.List;

public class ThemeManager {

    private static Theme currentTheme = new DefaultTheme();
    private static final List<ThemeChangeListener> listeners = new ArrayList<>();

    public static Theme getCurrentTheme() {
        return currentTheme;
    }

    public static void setCurrentTheme(Theme theme) {
        if (theme != null && currentTheme != theme) {
            currentTheme = theme;
            notifyThemeChangeListeners();
        }
    }

    public static void addThemeChangeListener(ThemeChangeListener listener) {
        listeners.add(listener);
    }

    public static void removeThemeChangeListener(ThemeChangeListener listener) {
        listeners.remove(listener);
    }

    private static void notifyThemeChangeListeners() {
        for (ThemeChangeListener listener : listeners) {
            listener.themeChanged(currentTheme);
        }
    }
}
