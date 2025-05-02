package com.prismix.client.gui.layout;

import com.prismix.client.gui.components.themed.ThemedPanel;

import javax.swing.*;
import java.awt.*;

public abstract class BaseLayout extends ThemedPanel {
    protected final ThemedPanel sidebar;
    protected final ThemedPanel header;
    protected final ThemedPanel content;
    
    public BaseLayout() {
        setLayout(new BorderLayout());
        
        // Shared sidebar containing room list
        sidebar = new ThemedPanel();
        sidebar.setLayout(new BorderLayout());
        add(sidebar, BorderLayout.WEST);

        // Main content area that changes
        content = new ThemedPanel();
        content.setLayout(new BorderLayout());
        add(content, BorderLayout.CENTER);

        header = new ThemedPanel();
        header.setLayout(new BorderLayout());
        add(header, BorderLayout.NORTH);
    }
    
    // Allow subclasses to modify content
    protected void setContent(JComponent component) {
        content.removeAll();
        content.add(component);
        content.revalidate();
        content.repaint();
    }

    protected void setSidebar(JComponent component) {
        sidebar.removeAll();
        sidebar.add(component);
        sidebar.revalidate();
        sidebar.repaint();
    }

    protected void setHeader(JComponent component) {
        header.removeAll();
        header.add(component);
        header.revalidate();
        header.repaint();
    }
} 