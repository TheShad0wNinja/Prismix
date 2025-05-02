package com.prismix.client.gui.themes;

import java.awt.*;
import java.awt.geom.RoundRectangle2D;

public class ThemePainter {
    private ThemePainter() {}

    /**
     * Paints a rounded rectangle background for a component.
     * @param g2d The Graphics2D context.
     * @param backgroundColor The color fo the background
     * @param x The x-coordinate.
     * @param y The y-coordinate.
     * @param width The width.
     * @param height The height.
     * @param cornerRadius The corner radius.
     */
    public static void paintRoundedBackground(Graphics2D g2d, Color backgroundColor, int x, int y, int width, int height, int cornerRadius) {
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(backgroundColor);

        RoundRectangle2D roundedRect = new RoundRectangle2D.Float(x, y, width, height, cornerRadius, cornerRadius);
        g2d.fill(roundedRect);
    }

    /**
     * Paints text centered within specified bounds, with padding.
     * @param g2d The Graphics2D context.
     * @param textColor The current theme.
     * @param text The text to paint.
     * @param font The font
     * @param bounds The bounds within which to paint the text.
     * @param padding The padding around the text.
     */
    public static void paintPaddedText(Graphics2D g2d, Color textColor, Font font, String text, Rectangle bounds, int padding) {
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        g2d.setColor(textColor);
        g2d.setFont(font);

        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(text);
        int textHeight = fm.getHeight();

        // Calculate text position to center it within the padded area of the bounds
        int contentWidth = bounds.width - 2 * padding;
        int contentHeight = bounds.height - 2 * padding;

        int textX = bounds.x + padding + (contentWidth - textWidth) / 2;
        int textY = bounds.y + padding + (contentHeight - textHeight) / 2 + fm.getAscent();

        // Ensure text stays within bounds, considering padding
        textX = Math.max(bounds.x + padding, textX);
        textY = Math.max(bounds.y + padding + fm.getAscent(), textY);


        g2d.drawString(text, textX, textY);
    }
}
