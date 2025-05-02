package com.prismix.client.gui.components.themed;

import com.prismix.client.gui.themes.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ThemedButton extends JButton implements ThemedComponent, ThemeChangeListener {
    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private Color hoverColor;
    private Color focusColor;
    private int padding;
    private int cornerRadius;
    private Font defaultFont;

    private final Variant variant;

    private boolean isPressed;
    private boolean isHovered;

    public enum Variant {
        PRIMARY, SECONDARY, TERTIARY, ERROR
    }

    public ThemedButton(String text) {
        this(text, Variant.PRIMARY);
    }

    public ThemedButton(String text, Variant variant) {
        super(text);
        this.variant = variant;
        isHovered = false;
        isPressed = false;

        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        applyTheme(ThemeManager.getCurrentTheme());

        ThemeManager.addThemeChangeListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setHover(true);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                setHover(false);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setPressed(true);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setPressed(false);

                if (contains(e.getPoint())) {
                    setHover(true);
                }
            }
        });
    }

    private void setHover(boolean hover) {
        if (this.isHovered != hover) {
            this.isHovered = hover;
            repaint();
        }
    }

    private void setPressed(boolean pressed) {
        if (this.isPressed != pressed) {
            this.isPressed = pressed;
            repaint();
        }
    }

    public void setPadding(int padding) {
        this.padding = padding;
        revalidate();
        repaint();
    }

    public void setCornerRadius(int cornerRadius) {
        this.cornerRadius = cornerRadius;
        repaint();
    }

    @Override
    public void applyTheme(Theme theme) {
        this.defaultBackgroundColor = switch (variant) {
            case PRIMARY -> theme.getPrimaryColor();
            case SECONDARY -> theme.getSecondaryColor();
            case TERTIARY -> theme.getTertiaryColor();
            case ERROR -> theme.getErrorColor();
        };

        this.defaultForegroundColor = switch (variant) {
            case PRIMARY -> theme.getOnPrimaryColor();
            case SECONDARY -> theme.getOnSecondaryColor();
            case TERTIARY -> theme.getOnTertiaryColor();
            case ERROR -> theme.getOnErrorColor();
        };

        this.hoverColor = defaultBackgroundColor.darker();

        this.focusColor = defaultBackgroundColor.darker().darker();

        this.defaultFont = theme.getDefaultFont().deriveFont(Font.BOLD);

        setCornerRadius(theme.getCornerRadius());
        setPadding(theme.getButtonPadding());

        setForeground(defaultForegroundColor);
        setFont(defaultFont);

        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();

        int width = getWidth();
        int height = getHeight();

        Color currentBackgroundColor = defaultBackgroundColor;
        if (isPressed) {
            currentBackgroundColor = focusColor;
        } else if (isHovered) {
            currentBackgroundColor = hoverColor;
        }

        ThemePainter.paintRoundedBackground(g2d, currentBackgroundColor, 0, 0, width, height, cornerRadius);

        // Use the ThemePainter for padded text
        Rectangle textBounds = new Rectangle(0, 0, width, height); // Paint text within the button bounds

        ThemePainter.paintPaddedText(g2d, defaultForegroundColor, defaultFont, getText(), textBounds, padding);

        g2d.dispose();
    }

    @Override
    public Dimension getPreferredSize() {
        Dimension size = super.getPreferredSize(); // Get preferred size based on text initially

        // Get FontMetrics to calculate text size accurately
        FontMetrics fm = getFontMetrics(getFont());
        int textWidth = fm.stringWidth(getText());
        int textHeight = fm.getHeight();

        // Calculate preferred size considering the actual text size and padding
        int preferredWidth = textWidth + 2 * padding;
        int preferredHeight = textHeight + 2 * padding;

        // Ensure the preferred size is not smaller than the default JComponent preferred size
        // (although with padding, this is less likely to be an issue)
        return new Dimension(Math.max(size.width, preferredWidth), Math.max(size.height, preferredHeight));
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
