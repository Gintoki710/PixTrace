package com.forgery.detector.service.detector;

import com.forgery.detector.model.DetectorOutput;
import com.forgery.detector.util.ImageUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;

/**
 * Copy-Move (Cloning) Forgery Detector.
 * Detects duplicated regions copied from one area of the image and pasted
 * onto another to obscure evidence or duplicate objects.
 * 
 * Uses Block-Based Feature Extraction, Lexicographical Sorting,
 * and Spatial Vector Clustering.
 * 
 * Demonstrates: Java Collections (Sorting, Custom Comparators), Coordinate Geometry,
 * Multidimensional Feature Vectors.
 */
@Service
public class CopyMoveDetector extends AbstractForgeryDetector {

    private static final int BLOCK_SIZE = 16;
    private static final int STEP_SIZE = 8;
    private static final double MIN_VARIANCE_THRESHOLD = 300.0; // Discard flat/uniform areas like plain sky
    private static final double SIMILARITY_THRESHOLD = 18.0;   // Feature vector distance
    private static final double MIN_SPATIAL_DISTANCE = 35.0;   // Blocks must be at least this far apart

    @Override
    public String getName() {
        return "Copy-Move Detector";
    }

    @Override
    public String getDescription() {
        return "Extracts localized block feature vectors to discover duplicated and cloned regions within the image.";
    }

    @Override
    public double getDefaultWeight() {
        return 0.25; // 25% weight in evidence fusion
    }

    @Override
    protected DetectorOutput doAnalysis(BufferedImage originalImage, byte[] rawFileBytes) throws Exception {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Downsample very high-res images for fast interactive analysis
        BufferedImage workingImage = originalImage;
        double scale = 1.0;
        if (width > 800 || height > 800) {
            scale = 800.0 / Math.max(width, height);
            int newW = (int) (width * scale);
            int newH = (int) (height * scale);
            workingImage = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_RGB);
            Graphics2D g = workingImage.createGraphics();
            g.drawImage(originalImage, 0, 0, newW, newH, null);
            g.dispose();
            width = newW;
            height = newH;
        }

        List<ImageBlock> blocks = new ArrayList<>();

        for (int y = 0; y <= height - BLOCK_SIZE; y += STEP_SIZE) {
            for (int x = 0; x <= width - BLOCK_SIZE; x += STEP_SIZE) {
                ImageBlock block = extractBlock(workingImage, x, y, BLOCK_SIZE);
                if (block.variance > MIN_VARIANCE_THRESHOLD) {
                    blocks.add(block);
                }
            }
        }

        // Sort blocks by their primary luminance feature for fast candidate matching
        blocks.sort(Comparator.comparingDouble(b -> b.meanLuminance));

        List<MatchPair> matchPairs = new ArrayList<>();
        int searchWindow = Math.min(25, blocks.size());

        for (int i = 0; i < blocks.size(); i++) {
            ImageBlock b1 = blocks.get(i);
            for (int j = i + 1; j < Math.min(blocks.size(), i + searchWindow); j++) {
                ImageBlock b2 = blocks.get(j);

                if (Math.abs(b1.meanLuminance - b2.meanLuminance) > 5.0) {
                    break; // Exceeded luminance boundary
                }

                double spatialDist = Math.hypot(b1.x - b2.x, b1.y - b2.y);
                if (spatialDist >= MIN_SPATIAL_DISTANCE) {
                    double featDist = b1.featureDistance(b2);
                    if (featDist < SIMILARITY_THRESHOLD) {
                        matchPairs.add(new MatchPair(b1, b2, spatialDist));
                    }
                }
            }
        }

        // Cluster matching pairs with consistent displacement vectors
        Map<String, List<MatchPair>> vectorClusters = new HashMap<>();
        for (MatchPair pair : matchPairs) {
            int dx = (int) Math.round((pair.b2.x - pair.b1.x) / 10.0) * 10;
            int dy = (int) Math.round((pair.b2.y - pair.b1.y) / 10.0) * 10;
            String key = dx + ":" + dy;
            vectorClusters.computeIfAbsent(key, k -> new ArrayList<>()).add(pair);
        }

        int maxClusterSize = 0;
        List<MatchPair> verifiedMatches = new ArrayList<>();
        for (List<MatchPair> cluster : vectorClusters.values()) {
            if (cluster.size() > maxClusterSize) {
                maxClusterSize = cluster.size();
            }
            if (cluster.size() >= 3) { // At least 3 contiguous blocks sharing the identical clone vector
                verifiedMatches.addAll(cluster);
            }
        }

        // Generate Copy-Move Visual Overlay
        BufferedImage visualMap = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2 = visualMap.createGraphics();
        g2.drawImage(workingImage, 0, 0, null);
        g2.setStroke(new BasicStroke(2.0f));

        for (MatchPair pair : verifiedMatches) {
            // Draw Source Block in Blue
            g2.setColor(new Color(0, 150, 255, 180));
            g2.drawRect(pair.b1.x, pair.b1.y, BLOCK_SIZE, BLOCK_SIZE);
            g2.fillRect(pair.b1.x, pair.b1.y, BLOCK_SIZE, BLOCK_SIZE);

            // Draw Cloned Target Block in Red
            g2.setColor(new Color(255, 50, 50, 180));
            g2.drawRect(pair.b2.x, pair.b2.y, BLOCK_SIZE, BLOCK_SIZE);
            g2.fillRect(pair.b2.x, pair.b2.y, BLOCK_SIZE, BLOCK_SIZE);

            // Draw Connection Line in Yellow
            g2.setColor(new Color(255, 255, 0, 160));
            g2.drawLine(pair.b1.x + BLOCK_SIZE / 2, pair.b1.y + BLOCK_SIZE / 2,
                    pair.b2.x + BLOCK_SIZE / 2, pair.b2.y + BLOCK_SIZE / 2);
        }
        g2.dispose();

        double tamperConfidence = 0.0;
        if (maxClusterSize >= 6) {
            tamperConfidence = Math.min(98.0, 50.0 + (maxClusterSize * 4.0));
        } else if (maxClusterSize >= 3) {
            tamperConfidence = 45.0 + (maxClusterSize * 3.0);
        } else if (!matchPairs.isEmpty()) {
            tamperConfidence = Math.min(25.0, matchPairs.size() * 1.5);
        }

        boolean tampered = tamperConfidence >= 45.0;

        DetectorOutput output = new DetectorOutput(
                getName(),
                tampered,
                tamperConfidence,
                tampered
                        ? String.format("Duplicated structures detected: Found %d coherent cloned block pairs with identical shift vectors.", verifiedMatches.size())
                        : "No coherent copy-move or cloned pixel regions detected."
        );

        output.addDetail("Candidate Block Matches", matchPairs.size());
        output.addDetail("Verified Cloned Pairs", verifiedMatches.size());
        output.addDetail("Max Cluster Size", maxClusterSize);
        output.setHeatmapBase64(ImageUtils.toBase64DataUrl(visualMap, "png"));

        return output;
    }

    private ImageBlock extractBlock(BufferedImage img, int x, int y, int size) {
        double sumR = 0, sumG = 0, sumB = 0, sumLum = 0;
        int count = size * size;

        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                int rgb = img.getRGB(x + dx, y + dy);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                double lum = 0.299 * r + 0.587 * g + 0.114 * b;

                sumR += r;
                sumG += g;
                sumB += b;
                sumLum += lum;
            }
        }

        double meanLum = sumLum / count;
        double varSum = 0;

        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                int rgb = img.getRGB(x + dx, y + dy);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                double lum = 0.299 * r + 0.587 * g + 0.114 * b;
                varSum += Math.pow(lum - meanLum, 2);
            }
        }

        return new ImageBlock(x, y, meanLum, varSum / count, sumR / count, sumG / count, sumB / count);
    }

    private static class ImageBlock {
        int x, y;
        double meanLuminance;
        double variance;
        double meanR, meanG, meanB;

        ImageBlock(int x, int y, double meanLuminance, double variance, double meanR, double meanG, double meanB) {
            this.x = x;
            this.y = y;
            this.meanLuminance = meanLuminance;
            this.variance = variance;
            this.meanR = meanR;
            this.meanG = meanG;
            this.meanB = meanB;
        }

        double featureDistance(ImageBlock o) {
            return Math.sqrt(
                    Math.pow(meanR - o.meanR, 2) +
                    Math.pow(meanG - o.meanG, 2) +
                    Math.pow(meanB - o.meanB, 2) +
                    Math.pow(variance - o.variance, 2) * 0.05
            );
        }
    }

    private static class MatchPair {
        ImageBlock b1, b2;
        double distance;

        MatchPair(ImageBlock b1, ImageBlock b2, double distance) {
            this.b1 = b1;
            this.b2 = b2;
            this.distance = distance;
        }
    }
}

