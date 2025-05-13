package com.tavern.client;

import com.tavern.client.components.Cleanable;
import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.views.AppPage;
import com.tavern.client.views.LoginPage;
import com.tavern.client.views.PageData;
import com.tavern.client.views.SignupPage;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class TavernApplicationFX extends Application implements EventListener {
    private Stage primaryStage;
    private BorderPane root;
    private Parent currentPage;
    private Cleanable currentController;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        root = new BorderPane();
        root.getStylesheets().add(getClass().getResource("/client/themes/material-theme.css").toExternalForm());
        switchPage(AppPage.LOGIN);

        Scene scene = new Scene(root, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Tavern");
        primaryStage.setOnCloseRequest(event -> {
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
        ApplicationContext.getEventBus().subscribe(this);
    }

    private void switchPage(AppPage page) {
        if (currentController != null)
            currentController.clean();

        PageData pd = switch (page) {
            case LOGIN -> LoginPage.load();
            case SIGNUP -> SignupPage.load();
            default -> throw new IllegalStateException("Unexpected value: " + page);
        };

        currentPage = pd.root();
        currentController = pd.controller();

        root.setCenter(currentPage);
    }

    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.SWITCH_PAGE) {
            AppPage screen = (AppPage) event.data();
            Platform.runLater(() -> {
                switchPage(screen);
            });
        }
    }
}
