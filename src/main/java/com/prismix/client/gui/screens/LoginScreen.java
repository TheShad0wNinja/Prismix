package com.prismix.client.gui.screens;

import com.prismix.client.core.AuthManager;
import com.prismix.client.core.ConnectionManager;
import com.prismix.client.gui.components.ThemedButton;
import com.prismix.client.gui.components.ThemedLabel;
import com.prismix.client.gui.components.ThemedPanel;
import com.prismix.client.gui.components.ThemedTextField;
import com.prismix.common.model.network.LoginRequest;
import com.prismix.common.model.network.SignupRequest;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

public class LoginScreen extends ThemedPanel {
    private JButton loginButton;
    private JButton signupButton;

    public LoginScreen() {
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);

        JLabel loginLabel = new ThemedLabel("Login", true);
        add(loginLabel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);

        JTextField usernameField = new ThemedTextField();
        add(usernameField, c);

        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 1;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);

        JButton loginButton = new ThemedButton("Login");
        add(loginButton, c);

        c.gridx = 1;
        c.gridy = 2;
        c.weightx = 0.5;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);

        JButton signupBtn = new ThemedButton("Signup");
        add(signupBtn, c);

        loginButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> AuthManager.getInstance().login(usernameField.getText())).start();
                System.out.println(usernameField.getText());
            }
        });

        signupBtn.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> AuthManager.getInstance().signup(usernameField.getText(), usernameField.getText(), null)).start();
                System.out.println(usernameField.getText());
            }
        });
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getSignupButton() {
        return signupButton;
    }
}
