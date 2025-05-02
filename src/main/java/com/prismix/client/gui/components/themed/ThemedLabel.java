package com.prismix.client.gui.components.themed;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeChangeListener;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemedComponent;

import javax.swing.*;
import java.awt.*;

public class ThemedLabel extends JLabel implements ThemedComponent, ThemeChangeListener {
    private Font defaultFont;
    private Color foregroundColor;
    private Size size;
    private Variant variant;

    public enum Size {
        DEFAULT, LARGER, TITLE, SMALLER
    }

    public enum Variant {
        PRIMARY, SECONDARY, TERTIARY, ERROR, BACKGROUND, SURFACE, SURFACE_ALT
    }

    public ThemedLabel() {
        this(null);
    }

    public ThemedLabel(String text) {
        this(text, Size.DEFAULT, Variant.BACKGROUND);
    }

    public ThemedLabel(String text, Size size) {
        this(text, size, Variant.BACKGROUND);
    }

    public ThemedLabel(String text, Size size, Variant variant) {
        super(text);
        this.size = size;
        this.variant = variant;

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
