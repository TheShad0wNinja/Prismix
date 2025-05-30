package com.tavern.client.gui.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.components.themed.ThemedButton;
import com.tavern.client.gui.components.themed.ThemedPanel;
import com.tavern.client.gui.themes.Theme;
import com.tavern.client.gui.themes.ThemeManager;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.User;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class DirectUserList extends ThemedPanel implements EventListener {
        private JPanel roomsPanel; // Panel to hold individual room entries
        private JScrollPane scrollPane;
        private ThemedButton lookForUserButton;

        public DirectUserList() {
            super(Variant.SURFACE_ALT);

            initComponents();

            ApplicationContext.getEventBus().subscribe(this);
            ApplicationContext.getUserHandler().updateDirectUsers();
        }

        private void initComponents() {
            setLayout(new BorderLayout());

            roomsPanel = new ThemedPanel(Variant.PRIMARY);
            roomsPanel.setLayout(new BoxLayout(roomsPanel, BoxLayout.Y_AXIS));

            scrollPane = new JScrollPane(roomsPanel);
            scrollPane.setBorder(null);
            scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
            scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

            add(scrollPane, BorderLayout.CENTER);
            
            // Add Look For User button at the bottom
            lookForUserButton = new ThemedButton("Look For User", ThemedButton.Variant.TERTIARY);
            lookForUserButton.addActionListener(e -> showUserSearchDialog());
            
            JPanel buttonPanel = new ThemedPanel(Variant.SURFACE_ALT);
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(lookForUserButton);
            
            add(buttonPanel, BorderLayout.SOUTH);

            applyTheme(ThemeManager.getCurrentTheme());
        }
        
        private void showUserSearchDialog() {
            UserSearchDialog dialog = new UserSearchDialog();
            dialog.setVisible(true);
        }

        public void updateUserList(List<User> users) {
            User currentUser = ApplicationContext.getMessageHandler().getCurrentDirectUser();
            
            roomsPanel.removeAll();
            for (User user : users) {
                DirectUserEntry entry = new DirectUserEntry(user);
                roomsPanel.add(entry);
            }
            roomsPanel.add(Box.createVerticalGlue());

            roomsPanel.revalidate();
            roomsPanel.repaint();
            revalidate();
            repaint();
        }

        @Override
        public void applyTheme(Theme theme) {
            super.applyTheme(theme);
            if (roomsPanel != null)
                roomsPanel.setBackground(theme.getSurfaceVariantColor());

            if (scrollPane != null) {
                scrollPane.getViewport().setBackground(theme.getSurfaceVariantColor());
                scrollPane.getViewport().setOpaque(true);
            }
        }

    @Override
    public Dimension getPreferredSize() {
        // Set the preferred width to 250 while maintaining dynamic height
        return new Dimension(250, super.getPreferredSize().height);
    }

        @Override
        public Dimension getMaximumSize() {
            return new Dimension(250, getPreferredSize().height);
        }

        @Override
        public void onEvent(ApplicationEvent event) {
            if (event.type() == ApplicationEvent.Type.DIRECT_USER_LIST_UPDATED) {
                SwingUtilities.invokeLater(() -> {
                    updateUserList((List<User>) event.data());
                });
            }
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            ApplicationContext.getEventBus().unsubscribe(this);
        }
}
