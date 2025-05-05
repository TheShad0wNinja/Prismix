package com.prismix.client.gui.screens;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;

public class TestCamera extends JFrame {

    private Webcam webcam;
    private volatile boolean running = true;

    public TestCamera() {
        super("Webcam Test");
        initializeUI();
        setupWebcam();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE); // We'll handle closing ourselves
        setLayout(new FlowLayout());

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeApplication();
            }
        });
    }

    private void setupWebcam() {
        try {
            // Set the native driver
            Webcam.setDriver(new NativeDriver());

            // Get available webcams
            List<Webcam> webcams = Webcam.getWebcams();
            if (webcams.isEmpty()) {
                JOptionPane.showMessageDialog(this, "No webcams found", "Error", JOptionPane.ERROR_MESSAGE);
                System.exit(1);
            }

            // Try to use default webcam, fallback to first available if needed
            System.out.println(webcams);
            webcam = Webcam.getDefault();
            if (webcam == null || webcam.getLock().isLocked()) {
                webcam = webcams.get(1);
            }

            System.out.println("Using webcam: " + webcam.getName());
            webcam.setViewSize(WebcamResolution.VGA.getSize());

            // Open with timeout
            if (!webcam.open(true)) {
                throw new Exception("Failed to open webcam");
            }

            // Create and add image panel
            JLabel imageHolder = new JLabel();
            add(imageHolder);
            pack();

            // Start image capture thread
            new Thread(() -> {
                while (running) {
                    try {
                        System.out.println(webcam + ": " + webcam.isOpen());
                        Image image = webcam.getImage();
                        if (image != null) {
                            SwingUtilities.invokeLater(() -> {
                                imageHolder.setIcon(new ImageIcon(image));
                            });
                        }
                        Thread.sleep(33); // ~30 FPS
                    } catch (Exception e) {
                        if (running) { // Only log if we didn't intentionally stop
                            e.printStackTrace();
                        }
                        break;
                    }
                }
                // Ensure webcam is closed when thread ends
                safelyCloseWebcam();
            }).start();

            setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error initializing webcam: " + e.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            closeApplication();
        }
    }

    private void safelyCloseWebcam() {
        if (webcam != null) {
            try {
                // Close asynchronously to prevent deadlocks
                new Thread(() -> {
                    webcam.close();
                    System.out.println("Webcam closed successfully");
                }).start();
            } catch (Exception e) {
                System.err.println("Error closing webcam: " + e.getMessage());
            }
        }
    }

    private void closeApplication() {
        running = false; // Signal threads to stop
        dispose(); // Close the window
        safelyCloseWebcam();
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TestCamera();
        });
    }
}