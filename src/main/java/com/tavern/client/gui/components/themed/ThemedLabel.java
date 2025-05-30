package com.tavern.client.gui.components.themed;

import com.tavern.client.gui.themes.Theme;
import com.tavern.client.gui.themes.ThemeChangeListener;
import com.tavern.client.gui.themes.ThemeManager;
import com.tavern.client.gui.themes.ThemedComponent;

import javax.swing.*;
import java.awt.*;

public class ThemedLabel extends JLabel implements ThemedComponent, ThemeChangeListener {
    private Font defaultFont;
    private Color foregroundColor;
    private Size size;
    private Variant variant;
    private int fontSize;

    public enum Size {
        DEFAULT, LARGER, TITLE, SMALLER, CUSTOM
    }

    public enum Variant {
        PRIMARY, SECONDARY, TERTIARY, ERROR, BACKGROUND, SURFACE, SURFACE_ALT
    }

    public ThemedLabel() {
        this(null);
    }

    public ThemedLabel(String text) {
        this(text, Size.DEFAULT, Variant.BACKGROUND, 0);
    }

    public ThemedLabel(String text, Size size) {
        this(text, size, Variant.BACKGROUND, 0);
    }

    public ThemedLabel(String text, int fontSize) {
        this(text, Size.CUSTOM, Variant.BACKGROUND, fontSize);
    }

    public ThemedLabel(String text, Size size, Variant variant) {
        this(text, size, variant, 0);
    }

    public ThemedLabel(String text, Size size, Variant variant, int fontSize) {
        super(text);
        this.size = size;
        this.variant = variant;
        this.fontSize = fontSize;
        setOpaque(false);

        applyTheme(ThemeManager.getCurrentTheme());
        ThemeManager.addThemeChangeListener(this);
    }

    @Override
    public void themeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    @Override
    public void applyTheme(Theme theme) {
        foregroundColor = switch (variant) {
            case PRIMARY -> theme.getOnPrimaryColor();
            case SECONDARY -> theme.getOnSecondaryColor();
            case TERTIARY -> theme.getOnTertiaryColor();
            case ERROR -> theme.getOnErrorColor();
            case BACKGROUND -> theme.getOnBackgroundColor();
            case SURFACE -> theme.getOnSurfaceColor();
            case SURFACE_ALT -> theme.getOnSurfaceVariantColor();
        };

        defaultFont = switch (size) {
            case DEFAULT -> theme.getDefaultFont();
            case LARGER -> theme.getLargerFont();
            case TITLE -> theme.getTitleFont();
            case SMALLER -> theme.getSmallerFont();
            case CUSTOM -> theme.getDefaultFont().deriveFont(Font.PLAIN, fontSize);
        };

        setForeground(foregroundColor);
        setFont(defaultFont);
    }

    @Override
    public void removeNotify(){
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
    }
}
