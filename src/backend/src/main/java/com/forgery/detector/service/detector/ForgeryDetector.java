package com.forgery.detector.service.detector;

import com.forgery.detector.model.DetectorOutput;
import java.awt.image.BufferedImage;

/**
 * Common interface implemented by all forensic detection modules.
 * Demonstrates: Java Interfaces, Polymorphism, Abstraction.
 */
public interface ForgeryDetector {

    /**
     * Unique display name of this detector.
     */
    String getName();

    /**
     * Detailed technical description of what this module analyzes.
     */
    String getDescription();

    /**
     * Weight of this detector in the Evidence Fusion Engine (0.0 to 1.0).
     */
    double getDefaultWeight();

    /**
     * Executes forensic analysis on the provided image and file bytes.
     *
     * @param originalImage The decoded BufferedImage
     * @param rawFileBytes  Raw binary bytes of the uploaded file
     * @return DetectorOutput containing tamper confidence and findings
     */
    DetectorOutput analyze(BufferedImage originalImage, byte[] rawFileBytes);
}

