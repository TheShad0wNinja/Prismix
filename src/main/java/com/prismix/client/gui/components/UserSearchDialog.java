package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedIcon;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.gui.components.themed.ThemedTextField;
import com.prismix.client.gui.themes.ThemeManager;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.handlers.ResponseHandler;
import com.prismix.client.utils.ConnectionManager;
import com.prismix.common.model.Message;
import com.prismix.common.model.User;
import com.prismix.common.model.network.GetAllUsersRequest;
import com.prismix.common.model.network.GetAllUsersResponse;
import com.prismix.common.model.network.NetworkMessage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class UserSearchDialog extends JDialog implements ResponseHandler {
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 500;
    private static final int AVATAR_SIZE = 30;
    private static final int MAX_USERNAME_LENGTH = 30;
    private static final AtomicLong messageSerial = new AtomicLong(0);
    
    private final ThemedTextField searchField;
    private final JPanel resultsPanel;
    private final List<User> allUsers = new ArrayList<>();
    private final List<User> filteredUsers = new ArrayList<>();
    
    public UserSearchDialog() {
        super((Frame) null, "Search Users", true);
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Register for user search responses
        ApplicationContext.getResponseHandlers().put(NetworkMessage.MessageType.GET_ALL_USERS_RESPONSE, this);
        
        // Search field at the top
        JPanel searchPanel = new ThemedPanel(ThemedPanel.Variant.SURFACE);
        searchPanel.setLayout(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        ThemedLabel searchLabel = new ThemedLabel("Search by Username:", ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.SURFACE);
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        
        searchField = new ThemedTextField("");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterUsers();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterUsers();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterUsers();
            }
        });
        searchPanel.add(searchField, BorderLayout.CENTER);
        
        add(searchPanel, BorderLayout.NORTH);
        
        // Results panel in the center
        resultsPanel = new ThemedPanel(ThemedPanel.Variant.BACKGROUND);
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(resultsPanel);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Button panel at the bottom
        JPanel buttonPanel = new ThemedPanel(ThemedPanel.Variant.SURFACE);
        buttonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        ThemedButton cancelButton = new ThemedButton("Cancel", ThemedButton.Variant.SECONDARY);
        cancelButton.addActionListener(e -> dispose());
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize by getting all users
        requestAllUsers();
    }
    
    private void requestAllUsers() {
        try {
            ConnectionManager.getInstance().sendMessage(new GetAllUsersRequest());
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error retrieving user list: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void filterUsers() {
        String searchText = searchField.getText().toLowerCase();
        
        filteredUsers.clear();
        
        // Don't include current user in results
        User currentUser = ApplicationContext.getUserHandler().getUser();
        
        for (User user : allUsers) {
            if (user.getId() == currentUser.getId()) {
                continue; // Skip current user
            }
            
            if (user.getUsername().toLowerCase().contains(searchText) ||
                user.getDisplayName().toLowerCase().contains(searchText)) {
                filteredUsers.add(user);
            }
        }
        
        updateResultsList();
    }
    
    private void updateResultsList() {
        resultsPanel.removeAll();
        
        if (filteredUsers.isEmpty()) {
            resultsPanel.add(createNoResultsPanel());
        } else {
            for (User user : filteredUsers) {
                JPanel userPanel = createUserEntryPanel(user);
                resultsPanel.add(userPanel);
                
                // Add a small rigid area (spacing) between entries
                resultsPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        
        // Add glue at the end to push everything to the top
        resultsPanel.add(Box.createVerticalGlue());
        
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    private JPanel createNoResultsPanel() {
        JPanel panel = new ThemedPanel(ThemedPanel.Variant.BACKGROUND);
        panel.setLayout(new BorderLayout());
        
        ThemedLabel label = new ThemedLabel("No users found", ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createUserEntryPanel(User user) {
        JPanel panel = new ThemedPanel(ThemedPanel.Variant.BACKGROUND);
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Avatar on the left
        JLabel avatar = new ThemedIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.CIRCLE);
        panel.add(avatar, BorderLayout.WEST);
        
        // Username and display name in the center - make this panel transparent
        JPanel userInfoPanel = new JPanel();
        userInfoPanel.setLayout(new BoxLayout(userInfoPanel, BoxLayout.Y_AXIS));
        userInfoPanel.setOpaque(false); // Make panel transparent so hover effects show through
        
        String username = user.getUsername();
        if (username.length() > MAX_USERNAME_LENGTH) {
            username = username.substring(0, MAX_USERNAME_LENGTH - 3) + "...";
        }
        
        ThemedLabel usernameLabel = new ThemedLabel(username, ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND);
        usernameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        userInfoPanel.add(usernameLabel);
        usernameLabel.setToolTipText(user.getUsername());
        
        String displayName = user.getDisplayName();
        if (displayName != null && !displayName.isEmpty() && !displayName.equals(username)) {
            if (displayName.length() > MAX_USERNAME_LENGTH) {
                displayName = displayName.substring(0, MAX_USERNAME_LENGTH - 3) + "...";
            }
            
            ThemedLabel displayNameLabel = new ThemedLabel(displayName, ThemedLabel.Size.SMALLER, ThemedLabel.Variant.BACKGROUND);
            displayNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            userInfoPanel.add(displayNameLabel);
        }
        
        panel.add(userInfoPanel, BorderLayout.CENTER);
        
        // Calculate the preferred size after all components are added
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 
                Math.max(userInfoPanel.getPreferredSize().height, AVATAR_SIZE) + 20));
        
        // Ensure the panel only takes the height it needs
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        
        // Add click handler to start chat
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                initiateDirectChat(user);
                dispose(); // Close dialog after selection
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = ThemeManager.getCurrentTheme().getTertiaryColor();
                panel.setBackground(hoverColor);
                
                // Update the labels to ensure good contrast on hover
                for (Component component : userInfoPanel.getComponents()) {
                    if (component instanceof ThemedLabel) {
                        component.setForeground(ThemeManager.getCurrentTheme().getOnTertiaryColor());
                    }
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                Color normalColor = ThemeManager.getCurrentTheme().getBackgroundColor();
                panel.setBackground(normalColor);
                
                // Reset the label colors
                for (Component component : userInfoPanel.getComponents()) {
                    if (component instanceof ThemedLabel) {
                        ThemedLabel label = (ThemedLabel) component;
                        label.setForeground(ThemeManager.getCurrentTheme().getOnBackgroundColor());
                    }
                }
            }
        });
        
        return panel;
    }
    
    private void initiateDirectChat(User user) {
        // Check if user has existing direct messages
        List<Message> directMessages = ApplicationContext.getMessageHandler().getDirectMessageHistory(user);
        
        if (directMessages == null || directMessages.isEmpty()) {
            // First time: Send an invisible message to initiate the direct chat
            long messageId = messageSerial.incrementAndGet();
            Message initMessage = new Message(
                    (int) messageId,
                    ApplicationContext.getUserHandler().getUser().getId(),
                    user.getId(),
                    -1,
                    "👋", // Greeting emoji message to initiate
                    true,
                    Timestamp.valueOf(LocalDateTime.now())
            );
            
            try {
                ApplicationContext.getMessageHandler().sendTextMessage(initMessage);
                System.out.println("Sent initial direct message to: " + user.getDisplayName());
            } catch (Exception ex) {
                System.err.println("Error sending initial direct message: " + ex.getMessage());
            }
        }
        
        // Navigate to direct message screen with this user
        ApplicationContext.getEventBus().publish(new ApplicationEvent(
                ApplicationEvent.Type.DIRECT_SCREEN_SELECTED
        ));
        
        // Add a delay to ensure the direct message screen is loaded before selecting the user
        SwingUtilities.invokeLater(() -> {
            // Give the UI time to update and mount components
            SwingUtilities.invokeLater(() -> {
                ApplicationContext.getEventBus().publish(new ApplicationEvent(
                        ApplicationEvent.Type.DIRECT_USER_SELECTED,
                        user
                ));
            });
        });
    }
    
    @Override
    public void handleResponse(NetworkMessage message) {
        if (message.getMessageType() == NetworkMessage.MessageType.GET_ALL_USERS_RESPONSE) {
            allUsers.clear();
            allUsers.addAll(((GetAllUsersResponse) message).users());
            filterUsers(); // Apply initial filtering
        }
    }
    
    @Override
    public void dispose() {
        // Unregister from response handler
        ApplicationContext.getResponseHandlers().remove(NetworkMessage.MessageType.GET_ALL_USERS_RESPONSE);
        super.dispose();
    }
} 