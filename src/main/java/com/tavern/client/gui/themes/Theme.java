package com.tavern.client.gui.themes;

import java.awt.Color;
import java.awt.Font;

public interface Theme {
    Color getPrimaryColor();
    Color getOnPrimaryColor(); // Text/icon color on the primary color

    Color getSecondaryColor();
    Color getOnSecondaryColor(); // Text/icon color on the secondary color

    Color getTertiaryColor();
    Color getOnTertiaryColor(); // Text/icon color on the tertiary color

    Color getErrorColor();
    Color getOnErrorColor(); // Text/icon color on the error color

    Color getBackgroundColor(); // Main background color
    Color getOnBackgroundColor(); // Text color on the main background

    Color getSurfaceColor(); // Color for components like panels, cards
    Color getOnSurfaceColor(); // Text color on surfaces

    Color getSurfaceVariantColor(); // A slightly different surface color
    Color getOnSurfaceVariantColor(); // Text color on surface variants

    Color getOutlineColor(); // Color for borders and outlines

    Font getDefaultFont(); // A default font for components

    Font getLargerFont();

    Font getTitleFont();

    Font getSmallerFont();

    int getCornerRadius();

    int getButtonPadding();
}