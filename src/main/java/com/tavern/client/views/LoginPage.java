package com.tavern.client.views;

import com.tavern.client.components.Cleanable;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.gui.screens.MainFrame;
import com.tavern.client.handlers.ApplicationContext;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class LoginPage implements Cleanable, EventListener, Initializable {

    @FXML
    public TextField input;

    public static PageData load() {
        FXMLLoader fxmlLoader = new FXMLLoader(LoginPage.class.getResource("/client/views/LoginPage.fxml"));
        try {
            return new PageData(fxmlLoader.load(), fxmlLoader.getController());
        } catch (IOException e) {
            return null;
        }
    }

    public void handleLogin(ActionEvent actionEvent) {
        String username = input.getText().trim();
        if (username.isEmpty()) {
            showErrorAlert("Please enter a valid username");
            return;
        }

        if (!username.equals(username.toLowerCase()) || username.contains(" ")) {
            showErrorAlert("Username must be lowercase & have no spaces");
            return;
        }

        ApplicationContext.getUserHandler().login(username);
    }

    public void handleSignupSwitch(ActionEvent actionEvent) {
        ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.SWITCH_PAGE, AppPage.SIGNUP));
    }

    private void showErrorAlert(String message) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText(null); // No header text
            alert.setContentText(message);
            alert.showAndWait(); // Show the dialog and wait for user to close
        });
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.AUTH_ERROR)  {
            showErrorAlert((String) event.data());
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext.getEventBus().subscribe(this);
    }
}
