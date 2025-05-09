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
import com.prismix.common.model.Room;
import com.prismix.common.model.User;
import com.prismix.common.model.network.GetAllRoomsRequest;
import com.prismix.common.model.network.GetAllRoomsResponse;
import com.prismix.common.model.network.NetworkMessage;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RoomSearchDialog extends JDialog implements ResponseHandler {
    private static final int DIALOG_WIDTH = 400;
    private static final int DIALOG_HEIGHT = 500;
    private static final int AVATAR_SIZE = 30;
    private static final int MAX_ROOM_NAME_LENGTH = 30;
    
    private final ThemedTextField searchField;
    private final JPanel resultsPanel;
    private final List<Room> allRooms = new ArrayList<>();
    private final List<Room> filteredRooms = new ArrayList<>();
    private boolean dialogReady = false;
    
    public RoomSearchDialog() {
        super((Frame) null, "Search Rooms", true);
        setSize(DIALOG_WIDTH, DIALOG_HEIGHT);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));
        
        // Register for room search responses
        ApplicationContext.getResponseHandlers().put(NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE, this);
        
        // Search field at the top
        JPanel searchPanel = new ThemedPanel(ThemedPanel.Variant.SURFACE);
        searchPanel.setLayout(new BorderLayout(5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        ThemedLabel searchLabel = new ThemedLabel("Search by Room Name:", ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.SURFACE);
        searchPanel.add(searchLabel, BorderLayout.NORTH);
        
        searchField = new ThemedTextField("");
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                filterRooms();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                filterRooms();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                filterRooms();
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
        
        // Initialize by getting all rooms
        requestAllRooms();
        
        // Show loading indicator
        showLoadingPanel();
        
        // Set flag after initialization
        dialogReady = true;
    }
    
    private void requestAllRooms() {
        try {
            ApplicationContext.getRoomHandler().getAllRooms();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                    "Error retrieving room list: " + e.getMessage(), 
                    "Error", 
                    JOptionPane.ERROR_MESSAGE);
            dispose();
        }
    }
    
    private void showLoadingPanel() {
        resultsPanel.removeAll();
        
        JPanel loadingPanel = new ThemedPanel(ThemedPanel.Variant.BACKGROUND);
        loadingPanel.setLayout(new BorderLayout());
        
        ThemedLabel loadingLabel = new ThemedLabel("Loading rooms...", ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND);
        loadingLabel.setHorizontalAlignment(SwingConstants.CENTER);
        loadingPanel.add(loadingLabel, BorderLayout.CENTER);
        
        resultsPanel.add(loadingPanel);
        resultsPanel.revalidate();
        resultsPanel.repaint();
    }
    
    private void filterRooms() {
        String searchText = searchField.getText().toLowerCase();
        
        filteredRooms.clear();
        
        for (Room room : allRooms) {
            if (room.getName().toLowerCase().contains(searchText)) {
                filteredRooms.add(room);
            }
        }
        
        updateResultsList();
    }
    
    private void updateResultsList() {
        resultsPanel.removeAll();
        
        if (filteredRooms.isEmpty()) {
            resultsPanel.add(createNoResultsPanel());
        } else {
            for (Room room : filteredRooms) {
                JPanel roomPanel = createRoomEntryPanel(room);
                resultsPanel.add(roomPanel);
                
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
        
        ThemedLabel label = new ThemedLabel("No rooms found", ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND);
        label.setHorizontalAlignment(SwingConstants.CENTER);
        panel.add(label, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createRoomEntryPanel(Room room) {
        JPanel panel = new ThemedPanel(ThemedPanel.Variant.BACKGROUND);
        panel.setLayout(new BorderLayout(10, 5));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        // Avatar on the left
        JLabel avatar = new ThemedIcon(room.getAvatar(), AVATAR_SIZE, AVATAR_SIZE, ThemedIcon.Variant.ROUNDED);
        panel.add(avatar, BorderLayout.WEST);
        
        // Room info in the center - make this panel transparent
        JPanel roomInfoPanel = new JPanel();
        roomInfoPanel.setLayout(new BoxLayout(roomInfoPanel, BoxLayout.Y_AXIS));
        roomInfoPanel.setOpaque(false); // Make panel transparent so hover effects show through
        
        String roomName = room.getName();
        if (roomName.length() > MAX_ROOM_NAME_LENGTH) {
            roomName = roomName.substring(0, MAX_ROOM_NAME_LENGTH - 3) + "...";
        }
        
        ThemedLabel roomNameLabel = new ThemedLabel(roomName, ThemedLabel.Size.DEFAULT, ThemedLabel.Variant.BACKGROUND);
        roomNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomInfoPanel.add(roomNameLabel);
        
        // Add room ID as a smaller label
        ThemedLabel roomIdLabel = new ThemedLabel("ID: " + room.getId(), ThemedLabel.Size.SMALLER, ThemedLabel.Variant.BACKGROUND);
        roomIdLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        roomInfoPanel.add(roomIdLabel);
        
        panel.add(roomInfoPanel, BorderLayout.CENTER);
        
        // Calculate the preferred size after all components are added
        panel.setPreferredSize(new Dimension(panel.getPreferredSize().width, 
                Math.max(roomInfoPanel.getPreferredSize().height, AVATAR_SIZE) + 20));
        
        // Ensure the panel only takes the height it needs
        panel.setMaximumSize(new Dimension(Integer.MAX_VALUE, panel.getPreferredSize().height));
        
        // Add click handler to join room
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                joinRoom(room);
                dispose(); // Close dialog after selection
            }
            
            @Override
            public void mouseEntered(MouseEvent e) {
                Color hoverColor = ThemeManager.getCurrentTheme().getTertiaryColor();
                panel.setBackground(hoverColor);
                
                // Update the labels to ensure good contrast on hover
                for (Component component : roomInfoPanel.getComponents()) {
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
                for (Component component : roomInfoPanel.getComponents()) {
                    if (component instanceof ThemedLabel) {
                        ThemedLabel label = (ThemedLabel) component;
                        label.setForeground(ThemeManager.getCurrentTheme().getOnBackgroundColor());
                    }
                }
            }
        });
        
        return panel;
    }
    
    private void joinRoom(Room room) {
        ApplicationContext.getRoomHandler().joinRoom(room.getId());
    }
    
    @Override
    public void handleResponse(NetworkMessage message) {
        if (message.getMessageType() == NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE) {
            GetAllRoomsResponse response = (GetAllRoomsResponse) message;
            allRooms.clear();
            allRooms.addAll(response.rooms());
            
            // Only update UI if the dialog is ready
            if (dialogReady) {
                filterRooms(); // Apply initial filtering
            }
        }
    }
    
    @Override
    public void dispose() {
        // Unregister from response handler
        ApplicationContext.getResponseHandlers().remove(NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE);
        super.dispose();
    }
} 