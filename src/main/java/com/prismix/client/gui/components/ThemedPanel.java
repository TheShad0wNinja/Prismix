package com.prismix.client.gui.components;

import com.prismix.client.gui.themes.Theme;
import com.prismix.client.gui.themes.ThemeChangeListener;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.gui.themes.ThemedComponent;

import javax.swing.*;
import java.awt.*;

public class ThemedPanel extends JPanel implements ThemedComponent, ThemeChangeListener {

    public ThemedPanel() {
        applyTheme(ThemeManager.getCurrentTheme());

        ThemeManager.addThemeChangeListener(this);
    }

    @Override
    public void applyTheme(Theme theme) {
        setBackground(theme.getBackgroundColor());
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
