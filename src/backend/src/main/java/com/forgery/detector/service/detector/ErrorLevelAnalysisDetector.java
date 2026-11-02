package com.forgery.detector.service.detector;

import com.forgery.detector.model.DetectorOutput;
import com.forgery.detector.util.ImageUtils;
import org.springframework.stereotype.Service;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Iterator;

/**
 * Error Level Analysis (ELA) Detector.
 * Identifies manipulated regions by recompressing the image at a known JPEG quality
 * (e.g. 90%) and analyzing the pixel compression error differential across surfaces.
 * 
 * Spliced or edited elements have different compression error rates compared to
 * the authentic background.
 * 
 * Demonstrates: Java 2D Graphics, ImageIO, Bitwise Pixel Operations, Mathematical Variance.
 */
@Service
public class ErrorLevelAnalysisDetector extends AbstractForgeryDetector {

    private static final float JPEG_QUALITY = 0.90f;
    private static final int SCALE_FACTOR = 18; // Scales subtle compression differences for human/algorithm detection

    @Override
    public String getName() {
        return "Error Level Analysis (ELA)";
    }

    @Override
    public String getDescription() {
        return "Measures compression error variations across surfaces. Spliced elements exhibit divergent compression rates.";
    }

    @Override
    public double getDefaultWeight() {
        return 0.30; // 30% weight in evidence fusion
    }

    @Override
    protected DetectorOutput doAnalysis(BufferedImage originalImage, byte[] rawFileBytes) throws Exception {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Step 1: Compress and resave image at 90% JPEG quality into memory
        byte[] recompressedBytes = recompressJpeg(originalImage, JPEG_QUALITY);
        BufferedImage recompressedImage = ImageIO.read(new ByteArrayInputStream(recompressedBytes));

        // Step 2: Compute absolute difference and build ELA Heatmap
        BufferedImage elaImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        long totalDiff = 0;
        int maxDiff = 0;
        long highAnomalyPixelCount = 0;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgbOrig = originalImage.getRGB(x, y);
                int rgbRecomp = recompressedImage.getRGB(x, y);

                int r1 = (rgbOrig >> 16) & 0xFF;
                int g1 = (rgbOrig >> 8) & 0xFF;
                int b1 = rgbOrig & 0xFF;

                int r2 = (rgbRecomp >> 16) & 0xFF;
                int g2 = (rgbRecomp >> 8) & 0xFF;
                int b2 = rgbRecomp & 0xFF;

                int diffR = Math.min(255, Math.abs(r1 - r2) * SCALE_FACTOR);
                int diffG = Math.min(255, Math.abs(g1 - g2) * SCALE_FACTOR);
                int diffB = Math.min(255, Math.abs(b1 - b2) * SCALE_FACTOR);

                int avgDiff = (diffR + diffG + diffB) / 3;
                totalDiff += avgDiff;
                if (avgDiff > maxDiff) maxDiff = avgDiff;

                // Threshold for significant anomaly
                if (avgDiff > 140) {
                    highAnomalyPixelCount++;
                }

                // Colorize: High error areas highlighted in vibrant thermal gradient
                int heatColor = computeHeatColor(avgDiff);
                elaImage.setRGB(x, y, heatColor);
            }
        }

        long totalPixels = (long) width * height;
        double averageDiff = (double) totalDiff / totalPixels;
        double anomalyRatio = (double) highAnomalyPixelCount / totalPixels;

        // Forensic evaluation heuristic
        double tamperConfidence = 0.0;
        if (anomalyRatio > 0.015 && averageDiff > 12.0) {
            tamperConfidence = Math.min(95.0, 40.0 + (anomalyRatio * 1500.0) + (averageDiff * 1.5));
        } else if (anomalyRatio > 0.005) {
            tamperConfidence = Math.min(65.0, 20.0 + (anomalyRatio * 800.0));
        } else {
            tamperConfidence = Math.min(25.0, averageDiff * 1.2);
        }

        boolean tampered = tamperConfidence >= 45.0;

        DetectorOutput output = new DetectorOutput(
                getName(),
                tampered,
                tamperConfidence,
                tampered 
                        ? String.format("Significant compression inconsistency detected (Anomaly Ratio: %.2f%%). High probability of splicing.", anomalyRatio * 100)
                        : "Uniform compression levels detected. Image surfaces display consistent error distribution."
        );

        output.addDetail("Recompression Quality", (int)(JPEG_QUALITY * 100) + "%");
        output.addDetail("Average Pixel Difference", Math.round(averageDiff * 100.0) / 100.0);
        output.addDetail("High Error Anomaly Ratio", String.format("%.3f%%", anomalyRatio * 100));
        output.addDetail("Max Pixel Delta", maxDiff);

        // Convert ELA visualization to Base64 data URL
        output.setHeatmapBase64(ImageUtils.toBase64DataUrl(elaImage, "png"));

        return output;
    }

    private byte[] recompressJpeg(BufferedImage image, float quality) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("No JPEG ImageWriter found");
        }
        ImageWriter writer = writers.next();
        try (ImageOutputStream ios = ImageIO.createImageOutputStream(baos)) {
            writer.setOutput(ios);
            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(quality);
            }

            // Convert ARGB to RGB if needed to prevent JPEG inversion artifacts
            BufferedImage rgbImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = rgbImage.createGraphics();
            g.drawImage(image, 0, 0, Color.WHITE, null);
            g.dispose();

            writer.write(null, new IIOImage(rgbImage, null, null), param);
        } finally {
            writer.dispose();
        }
        return baos.toByteArray();
    }

    private int computeHeatColor(int intensity) {
        // High intensity -> Red/Yellow, Medium -> Purple/Cyan, Low -> Dark Blue/Black
        if (intensity < 40) {
            return (0 << 16) | (0 << 8) | (intensity * 3);
        } else if (intensity < 120) {
            return (intensity << 16) | (0 << 8) | (200);
        } else if (intensity < 200) {
            return (240 << 16) | ((intensity - 40) << 8) | 0;
        } else {
            return (255 << 16) | (255 << 8) | 80;
        }
    }
}

