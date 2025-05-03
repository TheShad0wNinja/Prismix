package com.prismix.client;

import com.prismix.client.core.handlers.ApplicationContext;
import com.prismix.client.gui.screens.MainFrame;

import javax.swing.*;

public class PrismixApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationContext.getClient();
            new MainFrame();
        });
    }
} 