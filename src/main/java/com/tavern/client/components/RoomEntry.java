package com.tavern.client.components;

import com.tavern.client.utils.AvatarHelper;
import com.tavern.client.utils.TooltipHelper;
import com.tavern.common.model.Room;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javax.swing.ImageIcon;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class RoomEntry extends StackPane implements  Initializable {
    @FXML
    private ImageView roomAvatar;
    private Room room;
    private String name;
    private byte[] avatar;

    public RoomEntry() {
        loadFXML();
    }

    protected RoomEntry(String name, byte[] avatar) {
        this.name = name;
        this.avatar = avatar;
        loadFXML();
    }

    private void loadFXML() {
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/client/components/RoomEntry.fxml"));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);

        try {
            fxmlLoader.load();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    public void setRoom(Room room) {
        this.name = room.getName();
        this.avatar = room.getAvatar();
        prepareComponent();
    }

    protected void prepareComponent() {
        Tooltip.install(this, TooltipHelper.createTooltip(this.name));
        setAvatar(this.avatar);
    }

    protected void setAvatar(byte[] avatar) {
        ImageIcon icon = AvatarHelper.getAvatarImageIcon(avatar, 40, 40);
        BufferedImage i = (BufferedImage) AvatarHelper.getRoundedImageIcon(icon, 40, 40, 10).getImage();
        Image img = SwingFXUtils.toFXImage(i, null);

        roomAvatar.setImage(img);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        if (name != null) {
            prepareComponent();
        }
    }
}
