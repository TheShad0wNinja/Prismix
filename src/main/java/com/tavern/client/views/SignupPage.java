package com.tavern.client.views;

import com.tavern.client.components.Cleanable;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext; // Assuming you still use this for application context
import com.tavern.client.gui.screens.MainFrame; // Assuming MainFrame has AppScreen enum

import com.tavern.client.utils.AvatarHelper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.control.Alert; // For displaying messages
import javafx.scene.control.ButtonType; // For Alert dialogs
import javafx.stage.FileChooser; // For selecting files
import javafx.scene.image.Image; // For JavaFX Image
import javafx.scene.image.ImageView; // For the ImageView
import javafx.embed.swing.SwingFXUtils; // To convert BufferedImage to JavaFX Image

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage; // For image manipulation if needed
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.Optional;

// Implement Initializable and your EventListener interface
public class SignupPage implements Initializable, EventListener, Cleanable {

    @FXML
    private TextField usernameField;

    @FXML
    private TextField displayNameField;

    @FXML
    private ImageView avatarPreview; // Link to the ImageView

    @FXML
    private Label errorLabel; // Link to the error label


    private byte[] avatarData; // To store avatar byte array


    public static PageData load() {
        FXMLLoader fxmlLoader = new FXMLLoader(SignupPage.class.getResource("/client/views/SignupPage.fxml"));
        try {
            return new PageData(fxmlLoader.load(), fxmlLoader.getController());
        } catch (IOException e) {
            return null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // You can set initial properties or listeners here

        // Subscribe to event bus (using ApplicationContext)
        ApplicationContext.getEventBus().subscribe(this);
    }

    @FXML
    private void selectAvatar() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        // Set extension filters
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );

        File selectedFile = fileChooser.showOpenDialog(avatarPreview.getScene().getWindow()); // Show dialog relative to the window
        if (selectedFile != null) {
            try {
                // Read image using ImageIO (if complex resizing needed, otherwise use JavaFX Image directly)
                BufferedImage originalImage = ImageIO.read(selectedFile);

                // Resize image (if needed, same logic as your Swing method)
                BufferedImage resizedImage = AvatarHelper.resizeImage(originalImage, 64, 64);

                // Update preview
                avatarPreview.setImage(SwingFXUtils.toFXImage(resizedImage, null)); // Convert BufferedImage to JavaFX Image

                // Convert to byte array for storage
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                // Use the format that matches the original or a standard like png
                String format = getFileExtension(selectedFile);
                if (format == null || (!format.equalsIgnoreCase("png") && !format.equalsIgnoreCase("jpg") && !format.equalsIgnoreCase("gif"))) {
                    format = "png"; // Default to png if unknown or unsupported by ImageIO.write
                }
                ImageIO.write(resizedImage, format, baos);
                avatarData = baos.toByteArray();

            } catch (IOException ex) {
                showErrorAlert("Error loading image: " + ex.getMessage());
                avatarData = null; // Clear avatar data on error
            }
        }
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return null; // No extension
        }
        return name.substring(lastIndexOf + 1).toLowerCase();
    }

    @FXML
    private void handleSignup() {
        String username = usernameField.getText().trim();
        String displayName = displayNameField.getText().trim();

        // Clear previous error message
        hideErrorMessage();

        // Validate inputs (JavaFX Alert for messages)
        if (username.isEmpty() || displayName.isEmpty() ) {
            showErrorMessage("All fields are required.");
            return;
        }

        if (username.length() > 30) {
            showErrorMessage("Username can only be 30 characters long.");
            return;
        }

        if (displayName.length() > 50) {
            showErrorMessage("Display name can only be 50 characters long.");
            return;
        }

        if (username.contains(" ")) {
            showErrorMessage("Username can't have spaces.");
            return;
        }

        // If no avatar selected, create a default one with initials
        if (avatarData == null) {
            avatarData = AvatarHelper.createDefaultAvatar(displayName);
        }


        // Send signup request
        // You'll likely need to pass the password string to your user handler
        ApplicationContext.getUserHandler().signup(username, displayName, avatarData);
    }



    @FXML
    private void goBackToLogin() {
        ApplicationContext.getEventBus().publish(new ApplicationEvent(
                ApplicationEvent.Type.SWITCH_PAGE,
                AppPage.LOGIN
        ));
    }

    // Implement the onEvent method from the EventListener interface
    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.AUTH_ERROR) {
            String msg = (String) event.data();
            // Show error message in the UI Label instead of a JOptionPane
            showErrorMessage(msg);
        }
    }

    // Helper methods to show and hide the error label
    private void showErrorMessage(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            errorLabel.setManaged(true); // Ensure it takes up space in the layout
        } else {
            // Fallback to an Alert if errorLabel is not available (shouldn't happen with @FXML)
            showErrorAlert(message);
        }
    }

    private void hideErrorMessage() {
        if (errorLabel != null) {
            errorLabel.setVisible(false);
            errorLabel.setManaged(false); // Ensure it doesn't take up space
            errorLabel.setText(""); // Clear text
        }
    }

    // Helper method to show a JavaFX Alert for errors (like image loading errors)
    private void showErrorAlert(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null); // No header text
        alert.setContentText(message);
        alert.showAndWait(); // Show the dialog and wait for user to close
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }
}
