package com.forgery.detector.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

/**
 * Image processing utilities for Java ImageIO, buffer conversions,
 * and base64 transmission to the web client.
 */
public class ImageUtils {

    private ImageUtils() {}

    /**
     * Converts a raw byte array to a Java BufferedImage.
     */
    public static BufferedImage bytesToBufferedImage(byte[] data) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(data)) {
            BufferedImage image = ImageIO.read(bais);
            if (image == null) {
                throw new IOException("Unsupported image format or corrupt image file");
            }
            return image;
        }
    }

    /**
     * Converts a BufferedImage to a PNG byte array.
     */
    public static byte[] bufferedImageToBytes(BufferedImage image, String format) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, format != null ? format : "png", baos);
            return baos.toByteArray();
        }
    }

    /**
     * Converts a BufferedImage to a Base64 data URL for direct rendering in HTML5 img tag.
     */
    public static String toBase64DataUrl(BufferedImage image, String format) {
        try {
            byte[] bytes = bufferedImageToBytes(image, format);
            return "data:image/" + (format != null ? format.toLowerCase() : "png") + ";base64," + Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            return "";
        }
    }

    /**
     * Converts raw bytes directly to Base64 data URL.
     */
    public static String bytesToBase64DataUrl(byte[] bytes, String mimeType) {
        if (bytes == null || bytes.length == 0) return "";
        return "data:" + mimeType + ";base64," + Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Creates an overlay blending the original image with a colored tamper heatmap.
     */
    public static BufferedImage createOverlay(BufferedImage original, BufferedImage heatmap, float alpha) {
        int width = original.getWidth();
        int height = original.getHeight();

        BufferedImage combined = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = combined.createGraphics();

        g.drawImage(original, 0, 0, width, height, null);
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g.drawImage(heatmap, 0, 0, width, height, null);
        g.dispose();

        return combined;
    }
}

