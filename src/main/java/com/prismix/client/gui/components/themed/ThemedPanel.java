package com.prismix.client.gui.components.themed;

import com.prismix.client.gui.themes.*;

import javax.swing.*;
import java.awt.*;

public class ThemedPanel extends JPanel implements ThemedComponent, ThemeChangeListener {
    private Variant variant;
    protected int cornerRadius;
    protected final boolean rounded;

    public enum Variant {
        PRIMARY, SECONDARY, BACKGROUND, SURFACE, SURFACE_ALT, TERTIARY, ERROR, OUTLINE
    }

    public ThemedPanel() {
        this(false);
    }

    public ThemedPanel(Variant variant) {
        this(variant, false);
    }

    public ThemedPanel(boolean rounded) {
        this(Variant.BACKGROUND, rounded);
    }

    public ThemedPanel(Variant variant, boolean rounded) {
        super();

        this.variant = variant;
        this.rounded = rounded;
        applyTheme(ThemeManager.getCurrentTheme());

        ThemeManager.addThemeChangeListener(this);
    }

    public void setVariant(Variant variant) {
        this.variant = variant;
    }

    @Override
    public void applyTheme(Theme theme) {
        Color backgroundColor = switch (variant) {
            case PRIMARY -> theme.getPrimaryColor();
            case SECONDARY -> theme.getSecondaryColor();
            case BACKGROUND -> theme.getBackgroundColor();
            case SURFACE -> theme.getSurfaceColor();
            case SURFACE_ALT -> theme.getSurfaceVariantColor();
            case TERTIARY -> theme.getTertiaryColor();
            case ERROR -> theme.getErrorColor();
            case OUTLINE -> theme.getOutlineColor();
        };
        setBackground(backgroundColor);

        if (rounded) {
            this.cornerRadius = theme.getCornerRadius();
        } else {
            this.cornerRadius = 0;
        }

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        ThemePainter.paintRoundedBackground(g2d, getBackground(), 0, 0, width, height, cornerRadius);

        g2d.dispose();
    }

    @Override
    public void themeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
    }
}
