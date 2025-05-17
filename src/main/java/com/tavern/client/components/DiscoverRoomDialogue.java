package com.tavern.client.components;

import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.handlers.ResponseHandler;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.Room;
import com.tavern.common.model.network.GetAllRoomsResponse;
import com.tavern.common.model.network.NetworkMessage;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class DiscoverRoomDialogue extends BorderPane implements ResponseHandler, Initializable {

    private static final int AVATAR_SIZE = 30;
    private static final int MAX_ROOM_NAME_LENGTH = 30;

    @FXML private BorderPane rootPane;
    @FXML private TextField searchField;
    @FXML private ScrollPane scrollPane;
    @FXML private VBox resultsPanel;
    @FXML private Button cancelButton;

    private final List<Room> allRooms = new ArrayList<>();
    private final List<Room> filteredRooms = new ArrayList<>();
    private boolean isInitialized = false;
    private Stage dialogStage;
    private final Window owner;

    public DiscoverRoomDialogue(Window owner) {
        this.owner = owner;

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/client/components/DiscoverRoomDialog.fxml"));
        loader.setRoot(this);
        loader.setController(this);

        try {
            loader.load();
            this.getStylesheets().add(getClass().getResource("/client/themes/tavern-dark-theme.css").toExternalForm());
        } catch (IOException exception) {
            // Consider a more robust error handling strategy for production
            System.err.println("Failed to load DiscoverRoomDialog.fxml: " + exception.getMessage());
            exception.printStackTrace();
            throw new RuntimeException(exception); // Or handle more gracefully
        }

    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Initialize stage for dialog presentation
        setupDialogStage(owner);

        // Initialize listeners and fetch initial data
        initializeLogic();

    }

    private void setupDialogStage(Window owner) {
        dialogStage = new Stage();
        dialogStage.setTitle("Discover Rooms"); // Changed title
        // The 'this' (DiscoverRoomDialogue component) is the root of the scene
        Scene scene = new Scene(this);
        // Apply global CSS if necessary:
        // scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/path/to/your/styles.css")).toExternalForm());
        dialogStage.setScene(scene);
        dialogStage.setWidth(300);
        dialogStage.setHeight(300);
//        dialogStage.setResizable(false);

        dialogStage.initModality(Modality.WINDOW_MODAL);
        if (owner != null) {
            dialogStage.initOwner(owner);
            // Center on owner when shown
            dialogStage.setOnShown(event -> {
                if (owner.isShowing()) {
                    dialogStage.setX(owner.getX() + (owner.getWidth() - dialogStage.getWidth()) / 2);
                    dialogStage.setY(owner.getY() + (owner.getHeight() - dialogStage.getHeight()) / 2);
                } else {
                    dialogStage.centerOnScreen();
                }
            });
        } else {
            dialogStage.centerOnScreen();
        }

        // Ensure cleanup when the dialog stage is closed
        dialogStage.setOnCloseRequest(event -> cleanUp());
    }

    private void initializeLogic() {
        // Register for room search responses
        ApplicationContext.getResponseHandlers().put(NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE, this);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> filterRooms());

        showLoadingPanel();
        requestAllRooms();
        isInitialized = true;
    }


    public void showAndWait() {
        if (dialogStage != null) {
            dialogStage.showAndWait();
        } else {
            System.err.println("Dialog stage is not initialized. Cannot show.");
        }
    }

    @FXML
    private void handleCancelAction() {
        closeDialog();
    }

    private void closeDialog() {
        if (dialogStage != null) {
            dialogStage.close(); // This will also trigger cleanUp via setOnCloseRequest
        }
    }

    private void requestAllRooms() {
        try {
            ApplicationContext.getRoomHandler().getAllRooms();
        } catch (Exception e) {
            Platform.runLater(() -> {
                showAlert("Error", "Error retrieving room list: " + e.getMessage());
                closeDialog(); // Close dialog on critical error
            });
        }
    }

    private void showLoadingPanel() {
        resultsPanel.getChildren().clear();
        Label loadingLabel = new Label("Loading rooms...");
//        loadingLabel.setTextFill(defaultOnBackgroundColor); // Use themed color
        VBox.setMargin(loadingLabel, new Insets(20));
        resultsPanel.setAlignment(Pos.CENTER);
        resultsPanel.getChildren().add(loadingLabel);
    }

    private void filterRooms() {
        if (!isInitialized) return;

        String searchText = searchField.getText().toLowerCase().trim();
        filteredRooms.clear();

        for (Room room : allRooms) {
            if (room.getName().toLowerCase().contains(searchText)) {
                filteredRooms.add(room);
            }
        }
        updateResultsList();
    }

    private void updateResultsList() {
        resultsPanel.getChildren().clear();
        resultsPanel.setAlignment(Pos.TOP_LEFT);

        if (filteredRooms.isEmpty() && !allRooms.isEmpty() && isInitialized) {
            resultsPanel.getChildren().add(createNoResultsPanel());
        } else if (allRooms.isEmpty() && isInitialized) {
            // If still loading or explicit "no rooms available" message
            if (resultsPanel.getChildren().isEmpty()) { // Avoid overwriting loading if it's still there
                showLoadingPanel(); // Or a "No rooms available from server" message
            }
        } else {
            for (Room room : filteredRooms) {
                resultsPanel.getChildren().add(createRoomEntryNode(room));
            }
        }
    }

    private Node createNoResultsPanel() {
        Label label = new Label("No rooms found matching your search.");
//        label.setTextFill(defaultOnBackgroundColor); // Use themed color
        HBox container = new HBox(label);
        container.setAlignment(Pos.CENTER);
        VBox.setMargin(container, new Insets(20));
        return container;
    }

    private Node createRoomEntryNode(Room room) {
        BorderPane entryPane = new BorderPane();
        entryPane.setPadding(new Insets(10));
//        entryPane.setBackground(new Background(new BackgroundFill(defaultBackgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));

        ImageView avatarView = new ImageView();
        avatarView.setFitWidth(AVATAR_SIZE);
        avatarView.setFitHeight(AVATAR_SIZE);

        avatarView.setImage(SwingFXUtils.toFXImage( (BufferedImage) AvatarHelper.getRoundedImageIcon(AvatarHelper.getAvatarImageIcon(room.getAvatar(), AVATAR_SIZE, AVATAR_SIZE), AVATAR_SIZE, AVATAR_SIZE, 10).getImage(), null));
//        try {
//            String avatarUrl = room.getAvatar();
//            if (avatarUrl != null && !avatarUrl.isEmpty()) {
//                Image avatarImg = new Image(avatarUrl, true); // true for background loading
//                avatarView.setImage(avatarImg);
//            } else {
//                avatarView.setImage(createPlaceholderImage(AVATAR_SIZE, Color.GRAY));
//            }
//        } catch (IllegalArgumentException e) {
//            System.err.println("Invalid avatar URL/Path: " + room.getAvatar() + " - " + e.getMessage());
//            avatarView.setImage(createPlaceholderImage(AVATAR_SIZE, Color.DARKGRAY));
//        } catch (Exception e) {
//            System.err.println("Failed to load avatar: " + room.getAvatar() + " - " + e.getMessage());
//            avatarView.setImage(createPlaceholderImage(AVATAR_SIZE, Color.LIGHTGRAY));
//        }
//        Circle clip = new Circle(AVATAR_SIZE / 2.0, AVATAR_SIZE / 2.0, AVATAR_SIZE / 2.0);
//        avatarView.setClip(clip);
        BorderPane.setMargin(avatarView, new Insets(0, 10, 0, 0));
        entryPane.setLeft(avatarView);

        VBox roomInfoVBox = new VBox(2); // Spacing 2
//        roomInfoVBox.setBackground(Background.EMPTY); // Transparent background for the VBox itself

        String roomNameText = room.getName();
        if (roomNameText.length() > MAX_ROOM_NAME_LENGTH) {
            roomNameText = roomNameText.substring(0, MAX_ROOM_NAME_LENGTH - 3) + "...";
        }
        Label roomNameLabel = new Label(roomNameText);
//        roomNameLabel.setTextFill(defaultOnBackgroundColor);

        Label roomIdLabel = new Label("ID: " + room.getId());
//        roomIdLabel.setTextFill(defaultOnBackgroundColor);

        roomInfoVBox.getChildren().addAll(roomNameLabel, roomIdLabel);
        entryPane.setCenter(roomInfoVBox);

        entryPane.setCursor(Cursor.HAND);
        entryPane.setOnMouseClicked(event -> {
            joinRoom(room);
            closeDialog();
        });

//        entryPane.setOnMouseEntered(event -> {
//            entryPane.setBackground(new Background(new BackgroundFill(hoverBackgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
//            roomNameLabel.setTextFill(hoverOnBackgroundColor);
//            roomIdLabel.setTextFill(hoverOnBackgroundColor);
//        });
//
//        entryPane.setOnMouseExited(event -> {
//            entryPane.setBackground(new Background(new BackgroundFill(defaultBackgroundColor, CornerRadii.EMPTY, Insets.EMPTY)));
//            roomNameLabel.setTextFill(defaultOnBackgroundColor);
//            roomIdLabel.setTextFill(defaultOnBackgroundColor);
//        });

        return entryPane;
    }

//    private Image createPlaceholderImage(int size, Color color) {
//        WritableImage img = new WritableImage(size, size);
//        PixelWriter pw = img.getPixelWriter();
//        for (int x = 0; x < size; x++) {
//            for (int y = 0; y < size; y++) {
//                pw.setColor(x, y, color);
//            }
//        }
//        return img;
//    }

    private void joinRoom(Room room) {
        try {
            ApplicationContext.getRoomHandler().joinRoom(room.getId());
            // Optionally, provide feedback to the user that join was attempted
        } catch (Exception e) {
            Platform.runLater(() -> showAlert("Error", "Error joining room: " + e.getMessage()));
        }
    }

    @Override
    public void handleResponse(NetworkMessage message) {
        if (message.getMessageType() == NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE) {
            GetAllRoomsResponse response = (GetAllRoomsResponse) message;
            Platform.runLater(() -> {
                allRooms.clear();
                if (response.rooms() != null) {
                    allRooms.addAll(response.rooms());
                }
                if (isInitialized && dialogStage != null && dialogStage.isShowing()) {
                    filterRooms();
                }
            });
        }
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);

        // Attempt to set owner for modality if scene and window are available
        Window currentWindow = this.getScene() != null ? this.getScene().getWindow() : null;
        if (currentWindow instanceof Stage) {
            alert.initOwner(currentWindow);
        } else if (dialogStage != null && dialogStage.getOwner() != null) {
            alert.initOwner(dialogStage.getOwner());
        } else if (dialogStage != null) {
            alert.initOwner(dialogStage);
        }
        alert.showAndWait();
    }

    public void cleanUp() {
        ApplicationContext.getResponseHandlers().remove(NetworkMessage.MessageType.GET_ALL_ROOMS_RESPONSE);
    }

}
