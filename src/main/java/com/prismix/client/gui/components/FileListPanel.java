package com.prismix.client.gui.components;

import com.prismix.client.core.ApplicationEvent;
import com.prismix.client.core.EventListener;
import com.prismix.client.handlers.ApplicationContext;
import com.prismix.client.gui.components.themed.ThemedButton;
import com.prismix.client.gui.components.themed.ThemedPanel;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class FileListPanel extends ThemedPanel implements EventListener {
    private final JPanel fileListPanel;
    private final JScrollPane scrollPane;
    private final List<File> availableFiles;
    private static final DecimalFormat sizeFormat = new DecimalFormat("#,##0.#");

    public FileListPanel() {
        super(Variant.BACKGROUND);
        setLayout(new BorderLayout());
        availableFiles = new ArrayList<>();

        // Create header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        ThemedButton refreshButton = new ThemedButton("Refresh");
        refreshButton.addActionListener(e -> refreshFileList());
        headerPanel.add(refreshButton, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // Create file list panel
        fileListPanel = new JPanel();
        fileListPanel.setLayout(new BoxLayout(fileListPanel, BoxLayout.Y_AXIS));
        scrollPane = new JScrollPane(fileListPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // Subscribe to events
        ApplicationContext.getEventBus().subscribe(this);

        // Initial file list refresh
        refreshFileList();
    }

    private void refreshFileList() {
        fileListPanel.removeAll();
        availableFiles.clear();

        // Get files from uploads directory
        Path uploadsDir = Paths.get("uploads");
        if (Files.exists(uploadsDir)) {
            File[] files = uploadsDir.toFile().listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        availableFiles.add(file);
                        addFileToList(file);
                    }
                }
            }
        }

        if (availableFiles.isEmpty()) {
            JLabel noFilesLabel = new JLabel("No files available");
            noFilesLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            fileListPanel.add(noFilesLabel);
        }

        fileListPanel.revalidate();
        fileListPanel.repaint();
    }

    private String formatFileSize(long bytes) {
        if (bytes < 1024)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return sizeFormat.format(bytes / Math.pow(1024, exp)) + " " + pre + "B";
    }

    private void addFileToList(File file) {
        JPanel filePanel = new JPanel(new BorderLayout(10, 0));
        filePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // File info panel
        JPanel infoPanel = new JPanel(new BorderLayout());

        // Get original file name (remove UUID prefix if present)
        final String displayName = getDisplayName(file.getName());

        // File name label
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
        infoPanel.add(nameLabel, BorderLayout.NORTH);

        // File size label
        JLabel sizeLabel = new JLabel(formatFileSize(file.length()));
        sizeLabel.setForeground(Color.GRAY);
        infoPanel.add(sizeLabel, BorderLayout.SOUTH);

        filePanel.add(infoPanel, BorderLayout.CENTER);

        // Download button
        ThemedButton downloadButton = new ThemedButton("Download");
        downloadButton.addActionListener(e -> downloadFile(file, displayName));
        filePanel.add(downloadButton, BorderLayout.EAST);

        fileListPanel.add(filePanel);
        fileListPanel.add(Box.createVerticalStrut(5));
    }

    private String getDisplayName(String fileName) {
        int underscoreIndex = fileName.indexOf('_');
        if (underscoreIndex > 0 && underscoreIndex < fileName.length() - 1) {
            // Check if the part before underscore looks like a UUID
            String possibleUuid = fileName.substring(0, underscoreIndex);
            if (possibleUuid.length() >= 32) { // Simple UUID check
                return fileName.substring(underscoreIndex + 1);
            }
        }
        return fileName;
    }

    private void downloadFile(File file, String displayName) {
        // Create download directory if it doesn't exist
        Path downloadDir = Paths.get(System.getProperty("user.home"), "Prismix", "Downloads");
        try {
            Files.createDirectories(downloadDir);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Failed to create download directory: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Use the new download method with the display name (without UUID prefix)
        ApplicationContext.getFileTransferHandler().downloadFile(
                displayName,
                ApplicationContext.getRoomHandler().getCurrentRoom().getId());
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        switch (event.type()) {
            case FILE_TRANSFER_COMPLETE -> refreshFileList();
            case FILE_TRANSFER_ERROR -> refreshFileList();
        }
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}