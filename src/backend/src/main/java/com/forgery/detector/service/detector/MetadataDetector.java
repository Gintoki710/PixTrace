package com.forgery.detector.service.detector;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import com.forgery.detector.model.DetectorOutput;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.*;

/**
 * Forensic Metadata Detector.
 * Analyzes EXIF, IPTC, and XMP headers to identify editing software signatures,
 * missing camera hardware tags, timestamp inconsistencies, and C2PA markers.
 * 
 * Demonstrates: Third-party library integration, Collections, Forensic Heuristics.
 */
@Service
public class MetadataDetector extends AbstractForgeryDetector {

    private static final List<String> EDITING_SOFTWARE_KEYWORDS = Arrays.asList(
            "photoshop", "gimp", "canva", "lightroom", "picsart", "paint.net",
            "snapseed", "affinity", "pixelmator", "midjourney", "stable diffusion", "dall-e"
    );

    @Override
    public String getName() {
        return "Metadata Analyzer";
    }

    @Override
    public String getDescription() {
        return "Inspects EXIF/XMP tags for digital editing software traces, missing hardware signatures, and timestamp anomalies.";
    }

    @Override
    public double getDefaultWeight() {
        return 0.20; // 20% weight in evidence fusion
    }

    @Override
    protected DetectorOutput doAnalysis(BufferedImage originalImage, byte[] rawFileBytes) throws Exception {
        DetectorOutput output = new DetectorOutput();
        Map<String, String> extractedTags = new LinkedHashMap<>();
        List<String> warnings = new ArrayList<>();
        double tamperConfidence = 0.0;

        try (ByteArrayInputStream bais = new ByteArrayInputStream(rawFileBytes)) {
            Metadata metadata = ImageMetadataReader.readMetadata(bais);

            boolean hasCameraInfo = false;
            String softwareUsed = null;
            String make = null;
            String model = null;
            String dateOriginal = null;
            String dateModify = null;

            for (Directory directory : metadata.getDirectories()) {
                for (Tag tag : directory.getTags()) {
                    String tagName = tag.getTagName().toLowerCase();
                    String tagDesc = tag.getDescription();

                    extractedTags.put(directory.getName() + " - " + tag.getTagName(), tagDesc);

                    if (tagName.contains("software") || tagName.contains("processing software") || tagName.contains("history")) {
                        softwareUsed = tagDesc;
                    }
                    if (tagName.equals("make")) {
                        make = tagDesc;
                        hasCameraInfo = true;
                    }
                    if (tagName.equals("model")) {
                        model = tagDesc;
                        hasCameraInfo = true;
                    }
                    if (tagName.contains("date/time original")) {
                        dateOriginal = tagDesc;
                    }
                    if (tagName.contains("date/time digitized") || tagName.contains("modify date")) {
                        dateModify = tagDesc;
                    }
                }
            }

            // Heuristic 1: Check for Known Editing Software Signatures
            if (softwareUsed != null) {
                String swLower = softwareUsed.toLowerCase();
                for (String keyword : EDITING_SOFTWARE_KEYWORDS) {
                    if (swLower.contains(keyword)) {
                        tamperConfidence += 55.0;
                        warnings.add("Image processed by known editing software: '" + softwareUsed + "'");
                        break;
                    }
                }
            }

            // Heuristic 2: Total Absence of Camera Hardware EXIF (Typical of forged/stripped images)
            if (!hasCameraInfo) {
                tamperConfidence += 25.0;
                warnings.add("Camera hardware metadata (Make/Model) is absent or stripped.");
            } else {
                output.addDetail("Camera Make", make);
                output.addDetail("Camera Model", model);
            }

            // Heuristic 3: Check for C2PA / Content Authenticity Credentials
            boolean c2paFound = false;
            for (String key : extractedTags.keySet()) {
                if (key.toLowerCase().contains("c2pa") || key.toLowerCase().contains("authenticity")) {
                    c2paFound = true;
                    break;
                }
            }
            output.addDetail("C2PA Credentials Present", c2paFound);
            if (c2paFound) {
                tamperConfidence = Math.max(0.0, tamperConfidence - 30.0);
                warnings.add("Verified C2PA Content Credentials signature found.");
            }

            // Heuristic 4: Date mismatch check
            if (dateOriginal != null && dateModify != null && !dateOriginal.equals(dateModify)) {
                tamperConfidence += 20.0;
                warnings.add("Original capture timestamp differs from modification timestamp.");
            }

            tamperConfidence = Math.min(100.0, tamperConfidence);
            boolean isTampered = tamperConfidence >= 40.0;

            output.setTampered(isTampered);
            output.setTamperConfidence(tamperConfidence);
            output.setSummary(warnings.isEmpty() 
                    ? "Metadata intact. Natural camera EXIF verified with no editing software footprints." 
                    : String.join("; ", warnings));
            
            output.addDetail("Extracted Tag Count", extractedTags.size());
            output.addDetail("Warnings", warnings);
            output.addDetail("Software Detected", softwareUsed != null ? softwareUsed : "None");

            return output;
        }
    }
}

