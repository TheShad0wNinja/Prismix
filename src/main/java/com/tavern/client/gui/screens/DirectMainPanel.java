package com.tavern.client.gui.screens;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.components.ChatPanel;
import com.tavern.client.gui.components.DirectChatHeader;
import com.tavern.client.gui.components.DirectUserList;
import com.tavern.client.gui.components.themed.ThemedLabel;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.User;

import javax.swing.*;
import java.awt.*;

public class DirectMainPanel extends JPanel implements EventListener {
    private JPanel mainPanel;
    private DirectChatHeader chatHeader;
    private JPanel contentPanel;
    private final DirectUserList userListPanel;
    
    public DirectMainPanel() {
        super();
        setLayout(new BorderLayout());
        
        // Create user list panel (left sidebar)
        userListPanel = new DirectUserList();
        add(userListPanel, BorderLayout.WEST);
        
        // Create content panel for chat (will hold header and chat panel)
        contentPanel = new JPanel(new BorderLayout());
        
        // Initial welcome panel
        mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setOpaque(false);
        JLabel welcomeLabel = new ThemedLabel("Tavern", ThemedLabel.Size.TITLE, ThemedLabel.Variant.BACKGROUND);
        welcomeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(welcomeLabel);
        
        // Add main panel to content area
        contentPanel.add(mainPanel, BorderLayout.CENTER);
        add(contentPanel, BorderLayout.CENTER);

        ApplicationContext.getEventBus().subscribe(this);
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.DIRECT_USER_SELECTED) {
            User selectedUser = (User) event.data();
            
            SwingUtilities.invokeLater(() -> {
                // Clear just the content panel (keeping user list intact)
                contentPanel.removeAll();
                
                // Create chat header with the selected user
                chatHeader = new DirectChatHeader(selectedUser);
                contentPanel.add(chatHeader, BorderLayout.NORTH);
                
                // Create chat panel for direct messages
                mainPanel = new ChatPanel(true);
                contentPanel.add(mainPanel, BorderLayout.CENTER);
                
                // Update UI
                contentPanel.revalidate();
                contentPanel.repaint();
            });
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
