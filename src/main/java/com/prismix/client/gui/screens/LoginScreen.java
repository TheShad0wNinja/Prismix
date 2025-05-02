package com.prismix.client.gui.screens;

import com.prismix.client.core.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.components.themed.ThemedTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends ThemedPanel {
//    private final ApplicationContext context;
    private JButton loginButton;
    private JButton signupButton;

    public LoginScreen() {
//        this.context = context;
        setLayout(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);

        JLabel loginLabel = new ThemedLabel("Login", ThemedLabel.Size.TITLE);
        add(loginLabel, c);

        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 2;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.insets = new Insets(10, 15, 10, 15);

        JTextField usernameField = new ThemedTextField();
        usernameField.setText("khalid");
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
                new Thread(() -> ApplicationContext.getAuthManager().login(usernameField.getText())).start();
            }
        });

        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                new Thread(() -> ApplicationContext.getAuthManager().signup(
                    usernameField.getText(), 
                    usernameField.getText(), 
                    null
                )).start();
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
