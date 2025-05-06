package com.prismix.client.gui.screens;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.components.themed.ThemedTextField;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class LoginScreen extends ThemedPanel implements EventListener {
//    private final ApplicationContext context;
    private JButton loginButton;
    private JButton signupButton;

    public LoginScreen() {
        ApplicationContext.getEventBus().subscribe(this);
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
        usernameField.setText("zeyad");
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
                new Thread(() -> ApplicationContext.getUserHandler().login(usernameField.getText())).start();
            }
        });

        signupBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.SWITCH_SCREEN, MainFrame.AppScreen.SIGNUP_SCREEN));
            }
        });
    }

    public JButton getLoginButton() {
        return loginButton;
    }

    public JButton getSignupButton() {
        return signupButton;
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.AUTH_ERROR) {
            String msg = (String) event.data();
            JOptionPane.showMessageDialog(this,
                    msg,
                    "Login Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
