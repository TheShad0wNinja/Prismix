package com.tavern.client.components;

import com.tavern.client.utils.AvatarHelper;
import com.tavern.common.model.User;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import java.awt.image.BufferedImage;

public class DirectUserEntry {
    private static final int AVATAR_SIZE = 25;
    @FXML
    public ImageView avatar;

    @FXML
    public Label username;

    public static FXMLLoader load() {
        return new FXMLLoader(DirectUserEntry.class.getResource("/client/components/DirectUserEntry.fxml"));
    }

    public void setUser(User user) {
        username.setText(user.getUsername());
        avatar.setImage(
                SwingFXUtils.toFXImage(
                        (BufferedImage) AvatarHelper.getCircleImageIcon(
                                AvatarHelper.getAvatarImageIcon(user.getAvatar(), AVATAR_SIZE, AVATAR_SIZE),
                                AVATAR_SIZE, AVATAR_SIZE).getImage(),
                        null));
    }
}
