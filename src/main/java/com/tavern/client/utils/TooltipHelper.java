package com.tavern.client.utils;

import javafx.scene.control.Tooltip;
import javafx.stage.PopupWindow;
import javafx.util.Duration;

public class TooltipHelper {
    public static Tooltip createTooltip(String text) {
        Tooltip tooltip = new Tooltip(text);
        tooltip.setShowDelay(Duration.millis(50));
        tooltip.setStyle("""
                    -fx-font-size: 15px;
                    -fx-padding: 4 10 4 10;
                    -fx-background-color: -fx-surface-variant-color;
                    -fx-border-radius: 8px;
                    -fx-background-radius: 8px;
                    -fx-text-fill: -fx-on-surface-variant-color;
                    -fx-border-color: -fx-outline-color;
                """);
        tooltip.setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
        return tooltip;
    }
}
