package com.prismix.client.gui.components;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeChangeListener;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemedComponent;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class ThemedButton extends JButton implements ThemedComponent, ThemeChangeListener {
    private Color defaultBackgroundColor;
    private Color defaultForegroundColor;
    private Color hoverColor;
    private Color focusColor;

    public ThemedButton(String text) {
        super(text);
        setFocusPainted(false);
        setBorderPainted(false);
        setOpaque(true);

        setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        applyTheme(ThemeManager.getCurrentTheme());

        ThemeManager.addThemeChangeListener(this);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                setBackground(hoverColor);
        }

            @Override
            public void mouseExited(MouseEvent e) {
                setBackground(defaultBackgroundColor);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                setBackground(focusColor);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                setBackground(hoverColor);
            }
        });
    }

    @Override
    public void applyTheme(Theme theme) {
        this.defaultBackgroundColor = theme.getButtonBackgroundColor();
        this.defaultForegroundColor = theme.getButtonForegroundColor();
        this.hoverColor = theme.getHoverColor();
        this.focusColor = theme.getFocusColor();

        setBackground(defaultBackgroundColor);
        setForeground(defaultForegroundColor);
        setFont(theme.getDefaultFont());
    }

    @Override
    public void themeChanged(Theme newTheme) {
        applyTheme(newTheme);
    }

    @Override
    public void removeNotify(){
        super.removeNotify();
        ThemeManager.removeThemeChangeListener(this);
    }
}
