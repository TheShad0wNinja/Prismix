package com.prismix.client.gui.components.themed;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeChangeListener;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemedComponent;

import javax.swing.*;
import java.awt.*;

public class ThemedProgressBar extends JProgressBar implements ThemedComponent, ThemeChangeListener {
    private Color progressColor;
    private Color backgroundColor;
    private Color borderColor;
    private int cornerRadius;

    public ThemedProgressBar() {
        super();
        setBorderPainted(false);
        setOpaque(false);
        setStringPainted(true);

        applyTheme(ThemeManager.getCurrentTheme());
        ThemeManager.addThemeChangeListener(this);
    }

    @Override
    public void applyTheme(Theme theme) {
        this.progressColor = theme.getPrimaryColor();
        this.backgroundColor = theme.getSurfaceVariantColor();
        this.borderColor = theme.getOutlineColor();
        this.cornerRadius = theme.getCornerRadius();

        setForeground(theme.getOnPrimaryColor());
        setBackground(backgroundColor);
        setFont(theme.getSmallerFont());

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint background
        g2d.setColor(backgroundColor);
        g2d.fillRoundRect(0, 0, getWidth(), getHeight(), cornerRadius, cornerRadius);

        // Paint progress
        if (getValue() > 0) {
            int progressWidth = (int) (getWidth() * ((double) getValue() / getMaximum()));
            g2d.setColor(progressColor);
            g2d.fillRoundRect(0, 0, progressWidth, getHeight(), cornerRadius, cornerRadius);
        }

        // Paint border
        g2d.setColor(borderColor);
        g2d.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, cornerRadius, cornerRadius);

        // Paint text
        if (isStringPainted()) {
            g2d.setColor(getForeground());
            FontMetrics fm = g2d.getFontMetrics();
            String text = getString();
            int textWidth = fm.stringWidth(text);
            int textHeight = fm.getHeight();
            int x = (getWidth() - textWidth) / 2;
            int y = (getHeight() + textHeight) / 2 - fm.getDescent();
            g2d.drawString(text, x, y);
        }

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