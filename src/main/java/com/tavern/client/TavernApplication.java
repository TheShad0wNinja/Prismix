package com.tavern.client;

import com.tavern.client.gui.screens.MainFrame;
import com.tavern.client.handlers.ApplicationContext;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.swing.*;

public class TavernApplication{
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ApplicationContext.getClient();
            new MainFrame();
        });
    }
}
