package com.prismix.client.gui.screens;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.gui.components.ChatPanel;
import com.prismix.client.gui.components.DirectUserList;
import com.prismix.client.gui.components.themed.ThemedLabel;
import com.prismix.client.gui.components.themed.ThemedPanel;
import com.prismix.client.handlers.ApplicationContext;

import javax.swing.*;
import java.awt.*;

public class DirectMainPanel extends JPanel implements EventListener {
    private JPanel mainPanel;
    public DirectMainPanel() {
        super();
        setLayout(new BorderLayout());
        JPanel directUserList = new DirectUserList();
        add(directUserList, BorderLayout.WEST);
        mainPanel = new JPanel();
        JLabel tmp = new ThemedLabel("Prismix", ThemedLabel.Size.TITLE, ThemedLabel.Variant.BACKGROUND);
        tmp.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(tmp);
        add(mainPanel, BorderLayout.CENTER);

        ApplicationContext.getEventBus().subscribe(this);
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.DIRECT_USER_SELECTED) {
            SwingUtilities.invokeLater(() -> {
                remove(mainPanel);
                mainPanel = new ChatPanel(true);
                add(mainPanel, BorderLayout.CENTER);
                revalidate();
                repaint();
            });
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
