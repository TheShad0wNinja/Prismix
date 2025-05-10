package com.tavern.client.gui.components.themed;

import com.tavern.client.gui.themes.Theme;
import com.tavern.client.gui.themes.ThemeChangeListener;
import com.tavern.client.gui.themes.ThemeManager;
import com.tavern.client.gui.themes.ThemedComponent;
import com.tavern.client.utils.AvatarDisplayHelper;

import javax.swing.*;

public class ThemedIcon extends JLabel implements ThemedComponent, ThemeChangeListener {
    private final Variant variant;
    private int cornerRadius;
    private byte[] image;
    private int w;
    private int h;

    public enum Variant {
        SQUARE,
        ROUNDED,
        CIRCLE
    }

    public ThemedIcon(byte[] image, int w, int h, Variant variant) {
        this.image = image;
        this.w = w;
        this.h = h;
        this.variant = variant;

        applyTheme(ThemeManager.getCurrentTheme());
    }

    private void setIconImage() {
        ImageIcon icon = AvatarDisplayHelper.getAvatarImageIcon(image, w, h);
        if (variant == Variant.SQUARE) {
            setIcon(icon);
        } else if (variant == Variant.ROUNDED) {
            setIcon(AvatarDisplayHelper.getRoundedImageIcon(icon, w, h, cornerRadius));
        } else {
            setIcon(AvatarDisplayHelper.getCircleImageIcon(icon, w, h));
        }
    }


    @Override
    public void applyTheme(Theme theme) {
        cornerRadius = theme.getCornerRadius();
        setIconImage();
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
