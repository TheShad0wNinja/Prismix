package com.tavern.client.utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.awt.geom.Ellipse2D;

public class AvatarDisplayHelper {
    // Sharpen kernel for small images - reduced intensity for more subtle effect
    private static final float[] SHARPEN_KERNEL = {
            0.0f, -0.1f, 0.0f,
            -0.1f, 1.4f, -0.1f,
            0.0f, -0.1f, 0.0f
    };

    public static ImageIcon getAvatarImageIcon(byte[] avatarData, int width, int height) {
        if (avatarData == null || avatarData.length == 0) {
            return getDefaultAvatarIcon(width, height);
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(avatarData)) {
            BufferedImage bImage = ImageIO.read(bis);
            if (bImage != null) {
                return new ImageIcon(getHighQualityScaledImage(bImage, width, height));
            }
        } catch (IOException e) {
            System.err.println("Error creating image from avatar data: " + e.getMessage());
        }
        return getDefaultAvatarIcon(width, height);
    }

    private static BufferedImage getHighQualityScaledImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        // Step 1: Scale down in multiple steps for higher quality if the image is significantly larger
        BufferedImage scaledImage;
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();
        
        if (originalWidth > targetWidth * 2 || originalHeight > targetHeight * 2) {
            // Multi-step scaling for large reductions
            int currentWidth = originalWidth;
            int currentHeight = originalHeight;
            scaledImage = originalImage;
            
            // Step down incrementally (70% each step) until close to target size
            while (currentWidth > targetWidth * 1.25 || currentHeight > targetHeight * 1.25) {
                currentWidth = Math.max(targetWidth, (int)(currentWidth * 0.7));
                currentHeight = Math.max(targetHeight, (int)(currentHeight * 0.7));
                
                BufferedImage tempImage = new BufferedImage(currentWidth, currentHeight, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g2 = tempImage.createGraphics();
                setupHighQualityGraphics(g2);
                g2.drawImage(scaledImage, 0, 0, currentWidth, currentHeight, null);
                g2.dispose();
                
                scaledImage = tempImage;
            }
        } else {
            // For smaller reductions, initialize with original
            scaledImage = originalImage;
        }
        
        // Step 2: Final scaling to exact target size
        BufferedImage resultImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = resultImage.createGraphics();
        setupHighQualityGraphics(g2);
        g2.drawImage(scaledImage, 0, 0, targetWidth, targetHeight, null);
        g2.dispose();
        
        // Step A: Apply sharpening if the image is small
        if (targetWidth < 100 || targetHeight < 100) {
            return sharpenImage(resultImage);
        }
        
        return resultImage;
    }
    
    private static void setupHighQualityGraphics(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
    }
    
    private static BufferedImage sharpenImage(BufferedImage image) {
        Kernel kernel = new Kernel(3, 3, SHARPEN_KERNEL);
        ConvolveOp op = new ConvolveOp(kernel, ConvolveOp.EDGE_NO_OP, null);
        return op.filter(image, null);
    }

    private static ImageIcon getDefaultAvatarIcon(int width, int height) {
        try {
            ImageIcon defaultIcon = new ImageIcon(
                    AvatarDisplayHelper.class.getClassLoader().getResource("client/images/default_avatar.jpeg")
            );
            return new ImageIcon(getHighQualityScaledImage(
                    imageIconToBufferedImage(defaultIcon), width, height));
        } catch (Exception e) {
            System.err.println("Error loading default avatar: " + e.getMessage());
            BufferedImage placeholder = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            return new ImageIcon(placeholder);
        }
    }
    
    private static BufferedImage imageIconToBufferedImage(ImageIcon icon) {
        Image image = icon.getImage();
        if (image instanceof BufferedImage) {
            return (BufferedImage)image;
        }
        
        // Convert to BufferedImage
        BufferedImage bImage = new BufferedImage(
            icon.getIconWidth(), 
            icon.getIconHeight(), 
            BufferedImage.TYPE_INT_ARGB
        );
        Graphics g = bImage.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return bImage;
    }

    private static BufferedImage createMask(int width, int height, Shape shape) {
        BufferedImage mask = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = mask.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setColor(Color.WHITE);
        g2.fill(shape);
        g2.dispose();
        return mask;
    }

    public static ImageIcon getRoundedImageIcon(ImageIcon icon, int width, int height, int cornerRadius) {
        if (icon == null) {
            return null;
        }
        
        // First ensure the source image is high quality
        BufferedImage sourceImage = getHighQualityScaledImage(
            imageIconToBufferedImage(icon), width, height);

        // Create the mask
        RoundRectangle2D.Float shape = new RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius);
        BufferedImage mask = createMask(width, height, shape);

        // Create the result image
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw the original image
        g2.drawImage(sourceImage, 0, 0, null);

        // Apply the mask
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
        g2.drawImage(mask, 0, 0, null);
        g2.dispose();

        return new ImageIcon(result);
    }

    public static ImageIcon getCircleImageIcon(ImageIcon icon, int width, int height) {
        if (icon == null) {
            return null;
        }
        
        // First ensure the source image is high quality
        BufferedImage sourceImage = getHighQualityScaledImage(
            imageIconToBufferedImage(icon), width, height);

        // Create the mask
        Ellipse2D.Double shape = new Ellipse2D.Double(0, 0, width, height);
        BufferedImage mask = createMask(width, height, shape);

        // Create the result image
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = result.createGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

        // Draw the original image
        g2.drawImage(sourceImage, 0, 0, null);

        // Apply the mask
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.DST_IN));
        g2.drawImage(mask, 0, 0, null);
        g2.dispose();

        return new ImageIcon(result);
    }

    public static void setRoundedImageIcon(JLabel label, ImageIcon icon, int width, int height) {
        ImageIcon roundedIcon = getCircleImageIcon(icon, width, height);
        if (roundedIcon != null) {
            label.setIcon(roundedIcon);
        }
    }
}