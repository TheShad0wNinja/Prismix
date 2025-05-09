package com.prismix.client.gui.components.themed;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeChangeListener;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemedComponent;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.function.Consumer;

public class ThemedTextArea extends JTextArea implements ThemedComponent, ThemeChangeListener {

    private Color defaultBorderColor;
    private Color focusBorderColor;
    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private Consumer<String> enterAction;

    // Define the border size and padding
    private static final int BORDER_SIZE = 1;
    private static final int PADDING_SIZE = 5; // Inner padding

    public ThemedTextArea() {
        this(null, null);
    }

    public ThemedTextArea(String text) {
        this(text, null);
    }

    public ThemedTextArea(String text, Consumer<String> enterAction) {
        super(text);
        this.enterAction = enterAction;
        initComponents();
    }

    private void initComponents() {
        // Setup text area properties
        setLineWrap(true);
        setWrapStyleWord(true);
        setRows(1);
        
        // Remove default border and make it non-opaque
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false); // We will paint the background ourselves

        // Add focus listener to change border color
        addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                updateBorderColor(focusBorderColor);
            }

            @Override
            public void focusLost(FocusEvent e) {
                updateBorderColor(defaultBorderColor);
            }
        });
        
        // Add key listener for enter key handling (send on Enter)
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && !e.isShiftDown() && enterAction != null) {
                    e.consume(); // Prevent newline
                    String text = getText().trim();
                    if (!text.isEmpty()) {
                        enterAction.accept(text);
                        setText("");
                    }
                }
                // Allow Shift+Enter for new line
            }
        });

        applyTheme(ThemeManager.getCurrentTheme());
        ThemeManager.addThemeChangeListener(this);
    }

    @Override
    public void applyTheme(Theme theme) {
        this.defaultBackgroundColor = theme.getSurfaceColor();
        this.defaultForegroundColor = theme.getOnSurfaceColor();
        this.defaultBorderColor = theme.getOutlineColor();
        this.focusBorderColor = theme.getPrimaryColor();

        // Apply text-related theme properties
        setForeground(defaultForegroundColor);
        setBackground(defaultBackgroundColor);
        setFont(theme.getDefaultFont());
        setCaretColor(defaultForegroundColor); // Set caret color to match text color

        // Update the border color based on current focus state
        if (hasFocus()) {
            updateBorderColor(focusBorderColor);
        } else {
            updateBorderColor(defaultBorderColor);
        }
    }

    @Override
    public void themeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    private void updateBorderColor(Color color) {
        // Create a LineBorder for the border
        Border lineBorder = BorderFactory.createLineBorder(color, BORDER_SIZE);
        // Create an EmptyBorder for inner padding
        Border paddingBorder = new EmptyBorder(PADDING_SIZE, PADDING_SIZE, PADDING_SIZE, PADDING_SIZE);
        // Combine them into a CompoundBorder
        setBorder(new CompoundBorder(lineBorder, paddingBorder));
    }

    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D) g.create();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Paint the background
        g2d.setColor(getBackground());
        g2d.fillRect(0, 0, getWidth(), getHeight());

        g2d.dispose();

        // Paint the text after the background
        super.paintComponent(g);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
    }
} 