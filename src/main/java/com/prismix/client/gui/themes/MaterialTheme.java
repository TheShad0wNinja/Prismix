package com.prismix.client.gui.themes;

import java.awt.Color;
import java.awt.Font;

public class MaterialTheme implements Theme {

    private Color primaryColor;
    private Color onPrimaryColor;
    private Color secondaryColor;
    private Color onSecondaryColor;
    private Color tertiaryColor;
    private Color onTertiaryColor;
    private Color errorColor;
    private Color onErrorColor;
    private Color backgroundColor;
    private Color onBackgroundColor;
    private Color surfaceColor;
    private Color onSurfaceColor;
    private Color surfaceVariantColor;
    private Color onSurfaceVariantColor;
    private Color outlineColor;
    private Font defaultFont; // Add a default font
    private Font largerFont;
    private Font titleFont;
    private Font smallerFont;

    // Hardcoded colors from the light scheme for simplicity
    private static final String LIGHT_PRIMARY = "#006480";
    private static final String LIGHT_ON_PRIMARY = "#FFFFFF";
    private static final String LIGHT_SECONDARY = "#426372";
    private static final String LIGHT_ON_SECONDARY = "#FFFFFF";
    private static final String LIGHT_TERTIARY = "#764A8E";
    private static final String LIGHT_ON_TERTIARY = "#FFFFFF";
    private static final String LIGHT_ERROR = "#BA1A1A";
    private static final String LIGHT_ON_ERROR = "#FFFFFF";
    private static final String LIGHT_BACKGROUND = "#F6FAFD";
    private static final String LIGHT_ON_BACKGROUND = "#181C1F";
    private static final String LIGHT_SURFACE = "#DAE4EA";
    private static final String LIGHT_ON_SURFACE = "#3E484D";
    private static final String LIGHT_SURFACE_VARIANT = "#DAE4EA";
    private static final String LIGHT_ON_SURFACE_VARIANT = "#3E484D";
    private static final String LIGHT_OUTLINE = "#6E797E";


    public MaterialTheme() {
        // Load theme from JSON if you want to support different schemes (light/dark)
        // For now, we'll just use the hardcoded light scheme colors
        loadLightScheme();
    }

    private void loadLightScheme() {
        primaryColor = Color.decode(LIGHT_PRIMARY);
        onPrimaryColor = Color.decode(LIGHT_ON_PRIMARY);
        secondaryColor = Color.decode(LIGHT_SECONDARY);
        onSecondaryColor = Color.decode(LIGHT_ON_SECONDARY);
        tertiaryColor = Color.decode(LIGHT_TERTIARY);
        onTertiaryColor = Color.decode(LIGHT_ON_TERTIARY);
        errorColor = Color.decode(LIGHT_ERROR);
        onErrorColor = Color.decode(LIGHT_ON_ERROR);
        backgroundColor = Color.decode(LIGHT_BACKGROUND);
        onBackgroundColor = Color.decode(LIGHT_ON_BACKGROUND);
        surfaceColor = Color.decode(LIGHT_SURFACE);
        onSurfaceColor = Color.decode(LIGHT_ON_SURFACE);
        surfaceVariantColor = Color.decode(LIGHT_SURFACE_VARIANT);
        onSurfaceVariantColor = Color.decode(LIGHT_ON_SURFACE_VARIANT);
        outlineColor = Color.decode(LIGHT_OUTLINE);

        // Set a default font
        defaultFont = new Font("Arial", Font.PLAIN, 18); // Or load from properties
        largerFont = new Font("Arial", Font.PLAIN, 24);
        titleFont = new Font("Arial", Font.BOLD, 32);
        smallerFont = new Font("Arial", Font.PLAIN, 14);
    }

    // Implement the methods from the Theme interface
    @Override
    public Color getPrimaryColor() {
        return primaryColor;
    }

    @Override
    public Color getOnPrimaryColor() {
        return onPrimaryColor;
    }

    @Override
    public Color getSecondaryColor() {
        return secondaryColor;
    }

    @Override
    public Color getOnSecondaryColor() {
        return onSecondaryColor;
    }

    @Override
    public Color getTertiaryColor() {
        return tertiaryColor;
    }

    @Override
    public Color getOnTertiaryColor() {
        return onTertiaryColor;
    }

    @Override
    public Color getErrorColor() {
        return errorColor;
    }

    @Override
    public Color getOnErrorColor() {
        return onErrorColor;
    }

    @Override
    public Color getBackgroundColor() {
        return backgroundColor;
    }

    @Override
    public Color getOnBackgroundColor() {
        return onBackgroundColor;
    }

    @Override
    public Color getSurfaceColor() {
        return surfaceColor;
    }

    @Override
    public Color getOnSurfaceColor() {
        return onSurfaceColor;
    }

    @Override
    public Color getSurfaceVariantColor() {
        return surfaceVariantColor;
    }

    @Override
    public Color getOnSurfaceVariantColor() {
        return onSurfaceVariantColor;
    }

    @Override
    public Color getOutlineColor() {
        return outlineColor;
    }

    @Override
    public Font getDefaultFont() {
        return defaultFont;
    }

    @Override
    public Font getLargerFont() {
        return largerFont;
    }

    @Override
    public Font getTitleFont() {
        return titleFont;
    }

    @Override
    public Font getSmallerFont() {
        return smallerFont;
    }

    @Override
    public int getCornerRadius() {
        return 10;
    }

    @Override
    public int getButtonPadding() {
        return 10;
    }
}