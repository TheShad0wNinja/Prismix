package com.prismix.client.gui.themes;

import java.awt.Color;
import java.awt.Font;

public interface Theme {
    Color getBackgroundColor();
    Color getForegroundColor();
    Color getAccentColor();
    Color getButtonBackgroundColor();
    Color getButtonForegroundColor();
    Color getHoverColor();
    Color getFocusColor();
    Font getDefaultFont();
    Font getLargeFont();
}
