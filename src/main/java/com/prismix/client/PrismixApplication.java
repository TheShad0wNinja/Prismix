package com.prismix.client;

import com.prismix.client.gui.screens.MainFrame;
import com.prismix.client.handlers.ApplicationContext;

import javax.swing.*;

public class PrismixApplication {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationContext.getClient();
            new MainFrame();
        });
    }
} 
