package com.tavern.client.views;

import com.tavern.client.components.Cleanable;
import com.tavern.client.components.MainSidebar;
import com.tavern.client.handlers.ApplicationContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class MainPage implements Initializable, Cleanable {

    @FXML
    public MainSidebar sidebar;

    public static PageData load() {
        FXMLLoader fxmlLoader = new FXMLLoader(MainPage.class.getResource("/client/views/MainPage.fxml"));

        try {
            return new PageData(fxmlLoader.load(), fxmlLoader.getController());
        } catch (IOException e) {
            System.out.println("Failed: " + e.getMessage());
            return null;
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        System.out.println("MAIN PAGE INTIILIZED");
    }

    @Override
    public void clean() {

    }
}
