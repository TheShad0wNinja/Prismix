package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.common.model.User;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DirectUserSidebar extends VBox implements Initializable, EventListener, Cleanable {
    @FXML
    public ListView<User> userList;

    private final ObservableList<User> users = FXCollections.observableArrayList();
    private User previouslySelectedUser;

    public DirectUserSidebar() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/DirectUserSidebar.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
        } catch (IOException _) { }
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        userList.setCellFactory(u -> new UserEntryCell());
        userList.setItems(users);

        userList.setOnMouseClicked(event -> {
            User selectedUser = (User) userList.getSelectionModel().getSelectedItem();
            if (selectedUser != null && !selectedUser.equals(previouslySelectedUser)) {
                // Only publish the event if this is a different user than before
                ApplicationContext.getEventBus().publish(new ApplicationEvent(
                        ApplicationEvent.Type.DIRECT_USER_SELECTED,
                        selectedUser
                ));
                // Update the reference to the currently selected user
                previouslySelectedUser = selectedUser;
            }
        });

        ApplicationContext.getEventBus().subscribe(this);
        ApplicationContext.getUserHandler().updateDirectUsers();
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.DIRECT_USER_LIST_UPDATED) {
            Platform.runLater(() -> {
                users.setAll((List<User>) event.data());
            });
        }

    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
    }

    public static class UserEntryCell extends ListCell<User> {

        private DirectUserEntry userEntryController;
        private Node userEntry;

        public UserEntryCell() {
            FXMLLoader loader = DirectUserEntry.load();
            try {
                userEntry = loader.load();
                userEntryController = loader.getController();
            } catch (IOException _) { }
        }

        @Override
        protected void updateItem(User item, boolean empty) {
            super.updateItem(item, empty);

            this.getStyleClass().remove("exists");
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                userEntryController.setUser(item);
                this.getStyleClass().add("exists");
                setGraphic(userEntry);
            }
        }
    }
}
