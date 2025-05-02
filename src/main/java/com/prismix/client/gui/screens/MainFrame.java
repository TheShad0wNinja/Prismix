package com.prismix.client.gui.screens;

import com.prismix.client.core.Client;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {
    JPanel mainPanel;
    Client client;

    private static MainFrame instance;

    public MainFrame() {
        super();
        if (instance == null) {
            instance = this;
            client = new Client();
        }

        setTitle("Prismix");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(new BorderLayout());

        mainPanel = new JPanel(new BorderLayout());
        getContentPane().add(mainPanel, BorderLayout.CENTER);
        switchToLoginScreen();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void clearPanel() {
        if (mainPanel != null) {
            mainPanel.removeAll();
        }
    }

    private void updateUI() {
        mainPanel.revalidate();
        mainPanel.repaint();
    }

    private void switchToLoginScreen() {
        clearPanel();
        LoginScreen newPanel = new LoginScreen();
        mainPanel.add(newPanel, BorderLayout.CENTER);
        updateUI();
    }

    private void switchToMainScreen() {
        clearPanel();
        MainScreen newPanel = MainScreen.getInstance();
        mainPanel.add(newPanel, BorderLayout.CENTER);
        updateUI();
    }

    public static void switchPage(String page) {
        // Ensure we are on the Event Dispatch Thread
        if (!SwingUtilities.isEventDispatchThread()) {
            SwingUtilities.invokeLater(() -> switchPage(page)); // Re-invoke on EDT
            return;
        }

        if (instance == null)
            return;

        System.out.println("Switching to page: " + page);

        SwingUtilities.invokeLater(() -> {
            switch (page) {
                case "login":
                    instance.switchToLoginScreen();
                    break;
                case "main":
                    instance.switchToMainScreen();
                    break;
            }
        });
    }

    public static void main(String[] args) {
        MainFrame frame = new MainFrame();
    }
}
