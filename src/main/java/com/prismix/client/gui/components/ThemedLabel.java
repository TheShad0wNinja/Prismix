package com.prismix.client.gui.components;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeChangeListener;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemedComponent;

import javax.swing.*;
import java.awt.*;

public class ThemedLabel extends JLabel implements ThemedComponent, ThemeChangeListener {
    private Font defaultFont;
    private Color foregroundColor;
    private boolean isLarge;

    public ThemedLabel() {
        this("", false);
    }

    public ThemedLabel(boolean isLarge) {
        this("", isLarge);
    }

    public ThemedLabel(String text) {
        this(text, false);
    }

    public ThemedLabel(String text, boolean isLarge) {
        super(text);

        this.isLarge = isLarge;
        setOpaque(true);

        applyTheme(ThemeManager.getCurrentTheme());
        ThemeManager.addThemeChangeListener(this);
    }

    @Override
    public void themeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    @Override
    public void applyTheme(Theme theme) {
        setBackground(theme.getBackgroundColor());
        setForeground(theme.getForegroundColor());
        if (isLarge)
            setFont(theme.getLargeFont());
        else
            setFont(theme.getDefaultFont());
    }

    @Override
    public void removeNotify(){
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
    }
}
