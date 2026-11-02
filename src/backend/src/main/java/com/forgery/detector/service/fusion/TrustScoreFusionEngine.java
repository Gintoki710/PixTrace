package com.forgery.detector.service.fusion;

import com.forgery.detector.model.DetectorOutput;
import com.forgery.detector.model.TrustScoreResult;
import com.forgery.detector.service.detector.ForgeryDetector;
import com.forgery.detector.util.ImageUtils;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * Evidence Fusion Engine.
 * Fuses multi-modal forensic signals (Metadata, ELA, Copy-Move, AI) into a
 * unified 0-100 Trust Score and composite visual heatmap.
 * 
 * Demonstrates: Java Stream API, Lambdas, Functional Programming, Strategy Pattern.
 */
@Service
public class TrustScoreFusionEngine {

    /**
     * Fuses detector outputs into an explainable TrustScoreResult using Stream API and Lambdas.
     */
    public TrustScoreResult fuseEvidence(List<DetectorOutput> outputs, List<ForgeryDetector> detectors) {
        TrustScoreResult result = new TrustScoreResult();

        // Map weights using Java 8 Stream API and Lambdas
        Map<String, Double> weightMap = new HashMap<>();
        detectors.forEach(d -> weightMap.put(d.getName(), d.getDefaultWeight()));
        result.setModuleWeights(weightMap);

        // Normalize weights so they sum to 1.0
        double totalWeight = outputs.stream()
                .mapToDouble(o -> weightMap.getOrDefault(o.getDetectorName(), 0.25))
                .sum();

        // Calculate weighted tamper penalty using Java Streams
        double totalTamperPenalty = outputs.stream()
                .mapToDouble(output -> {
                    double weight = weightMap.getOrDefault(output.getDetectorName(), 0.25) / totalWeight;
                    double deduction = output.getTamperConfidence() * weight;
                    result.getModuleDeductions().put(output.getDetectorName(), Math.round(deduction * 100.0) / 100.0);
                    return deduction;
                })
                .sum();

        // Trust Score: 100 is fully authentic, 0 is definitively forged
        double calculatedScore = Math.max(0.0, Math.min(100.0, 100.0 - totalTamperPenalty));
        result.setTrustScore(calculatedScore);

        // Classify Verdict & Risk Level
        if (calculatedScore >= 80.0) {
            result.setVerdict("AUTHENTIC");
            result.setRiskLevel("LOW");
        } else if (calculatedScore >= 55.0) {
            result.setVerdict("SUSPICIOUS");
            result.setRiskLevel("MEDIUM");
        } else {
            result.setVerdict("FORGED");
            result.setRiskLevel("CRITICAL");
        }

        // Generate explainable bullet points via Stream filtering
        outputs.stream()
                .filter(DetectorOutput::isTampered)
                .forEach(o -> result.addExplanation(String.format("[%s]: %s (Tamper confidence: %.1f%%)",
                        o.getDetectorName(), o.getSummary(), o.getTamperConfidence())));

        if (result.getExplanations().isEmpty()) {
            result.addExplanation("All forensic detectors verified image integrity. No structural manipulation detected.");
        }

        return result;
    }

    /**
     * Blends the individual detector heatmaps into a single master composite heatmap.
     */
    public String generateCompositeHeatmap(BufferedImage original, List<DetectorOutput> outputs) {
        int w = original.getWidth();
        int h = original.getHeight();

        BufferedImage composite = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = composite.createGraphics();
        g.drawImage(original, 0, 0, null);

        // Blend heatmaps from each detector that produced one
        for (DetectorOutput output : outputs) {
            if (output.getHeatmapBase64() != null && output.getHeatmapBase64().contains("base64,")) {
                try {
                    String base64Data = output.getHeatmapBase64().substring(output.getHeatmapBase64().indexOf("base64,") + 7);
                    byte[] imgBytes = Base64.getDecoder().decode(base64Data);
                    BufferedImage layer = ImageIO.read(new ByteArrayInputStream(imgBytes));
                    if (layer != null) {
                        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.45f));
                        g.drawImage(layer, 0, 0, w, h, null);
                    }
                } catch (Exception ignored) {
                }
            }
        }
        g.dispose();

        return ImageUtils.toBase64DataUrl(composite, "png");
    }
}

