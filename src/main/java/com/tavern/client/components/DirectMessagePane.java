package com.tavern.client.components;

import com.tavern.client.core.ApplicationEvent;
import com.tavern.client.core.EventListener;
import com.tavern.client.handlers.ApplicationContext;
import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.User;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class DirectMessagePane extends BorderPane implements EventListener, Initializable, Cleanable {
    private final static int AVATAR_SIZE = 40;
    @FXML
    private ImageView avatar;
    @FXML
    private Label displayName;
    @FXML
    private Button callButton;

    private ChatPane chatPane;

    public DirectMessagePane() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/DirectMessagePane.fxml"));
        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void onEvent(ApplicationEvent event) {
        if (event.type() == ApplicationEvent.Type.DIRECT_USER_SELECTED)
        {
            User user = (User) event.data();
            Platform.runLater(() -> {
                avatar.setImage(SwingFXUtils.toFXImage((BufferedImage) AvatarHelper.getRoundedImageIcon( AvatarHelper.getAvatarImageIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE) , AVATAR_SIZE, AVATAR_SIZE, 5).getImage(), null));
                displayName.setText(user.getDisplayName());

                if (chatPane != null)
                    chatPane.clean();

                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/ChatPane.fxml"));
                try {
                    Node n = fxmlLoader.load();
                    chatPane = fxmlLoader.getController();
                    setCenter(n);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        ApplicationContext.getEventBus().subscribe(this);
    }

    @Override
    public void clean() {
        ApplicationContext.getEventBus().unsubscribe(this);
        chatPane.clean();
    }
}
