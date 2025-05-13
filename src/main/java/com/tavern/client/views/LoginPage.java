package com.tavern.client.views;

import com.tavern.client.components.Cleanable;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.gui.screens.MainFrame;
import com.tavern.client.handlers.ApplicationContext;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TextField;

import java.io.IOException;

public class LoginPage implements Cleanable {

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
    }

    public void handleSignupSwitch(ActionEvent actionEvent) {
        ApplicationContext.getEventBus().publish(new ApplicationEvent(ApplicationEvent.Type.SWITCH_PAGE, AppPage.SIGNUP));
    }

    @Override
    public void clean() {

    }
}
