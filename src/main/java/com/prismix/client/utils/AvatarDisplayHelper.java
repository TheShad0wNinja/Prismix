package com.prismix.client.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;

public class AvatarDisplayHelper {
    public static ImageIcon getAvatarImageIcon(byte[] avatarData, int width, int height) {
        if (avatarData == null || avatarData.length == 0) {
            // Return a default avatar image if no avatar is present
            // You'll need to have a default image resource
            return getDefaultAvatarIcon(width, height);
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(avatarData)) {
            BufferedImage bImage = ImageIO.read(bis);
            if (bImage != null) {
                // Scale the image to the desired size
                Image scaledImage = bImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaledImage);
            }
        } catch (IOException e) {
            System.err.println("Error creating image from avatar data: " + e.getMessage());
        }
        return getDefaultAvatarIcon(width, height);
    }

    private static ImageIcon getDefaultAvatarIcon(int width, int height) {
        try {
            ImageIcon defaultIcon = new ImageIcon(
                    AvatarDisplayHelper.class.getClassLoader().getResource("client/images/default_avatar.jpeg")
            );
            Image scaledDefaultImage = defaultIcon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaledDefaultImage);
        } catch (Exception e) {
            System.err.println("Error loading default avatar: " + e.getMessage());
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            return new ImageIcon(placeholder);
        }
    }
}