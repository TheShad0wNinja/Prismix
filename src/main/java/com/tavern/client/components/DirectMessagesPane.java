package com.tavern.client.components;

import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DirectMessagesPane extends BorderPane implements Initializable, Cleanable {

    public DirectMessagesPane() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/DirectMessagesPane.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException _) { }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

    }

    @Override
    public void clean() {

    }
}
