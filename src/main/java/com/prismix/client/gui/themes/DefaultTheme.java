package com.prismix.client.gui.themes;

import java.awt.Color;
import java.awt.Font;

public class DefaultTheme implements Theme {

    @Override
    public Color getBackgroundColor() {
        return new Color(240, 242, 245);
    }

    @Override
    public Color getForegroundColor() {
        return new Color(51, 51, 51);
    }

    @Override
    public Color getFocusColor() {
        return new Color(2, 78, 159);
    }

    @Override
    public Color getAccentColor() {
        return new Color(0, 123, 255);
    }

    @Override
    public Color getButtonBackgroundColor() {
        return getAccentColor();
    }

    @Override
    public Color getButtonForegroundColor() {
        return Color.WHITE;
    }

    @Override
    public Color getHoverColor() {
        return new Color(0, 100, 200);
    }

    @Override
    public Font getDefaultFont() {
        return new Font("Segoe UI", Font.PLAIN, 14);
    }

    @Override
    public Font getLargeFont() {
        return new Font("Segoe UI", Font.BOLD, 28);
    }
}