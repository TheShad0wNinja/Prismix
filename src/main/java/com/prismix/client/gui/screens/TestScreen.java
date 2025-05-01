package com.prismix.client.gui.screens;

import com.prismix.client.gui.components.*;
import com.prismix.client.utils.AvatarDisplayHelper;

import javax.swing.*;
import java.awt.*;

public class TestScreen extends JFrame {
    public TestScreen() {
        setTitle("Test");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLocationRelativeTo(null);

        setLayout(new BorderLayout());

        JPanel mainPanel = new ThemedPanel();
//        mainPanel.setLayout(new GridLayout(0, 1));
        mainPanel.add(new ThemedButton("LIGMA BOI"), BorderLayout.EAST);
        GroupChatListPanel groupChatListPanel = new GroupChatListPanel();

// Assuming your main screen uses BorderLayout
        add(groupChatListPanel, BorderLayout.WEST);

//        JLabel test = new JLabel();
//        test.setIcon(AvatarDisplayHelper.getAvatarImageIcon(null, 50, 50));
//        mainPanel.add(test);

        add(mainPanel);

        setResizable(false);
        setVisible(true);
    }

    public static void main(String[] args) {
        new TestScreen();
    }
}
