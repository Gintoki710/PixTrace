package com.forgery.detector.service.detector;

import com.forgery.detector.model.DetectorOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;

/**
 * Base abstract class providing common execution lifecycle, exception safety,
 * and execution time benchmarking.
 * 
 * Demonstrates: Abstract Classes, Inheritance, Exception Handling.
 */
public abstract class AbstractForgeryDetector implements ForgeryDetector {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public DetectorOutput analyze(BufferedImage originalImage, byte[] rawFileBytes) {
        long startTime = System.currentTimeMillis();
        try {
            logger.info("Executing detector: {}", getName());
            DetectorOutput output = doAnalysis(originalImage, rawFileBytes);
            output.setDetectorName(getName());
            output.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return output;
        } catch (Exception e) {
            logger.error("Error executing detector {}: {}", getName(), e.getMessage(), e);
            DetectorOutput fallback = new DetectorOutput(getName(), false, 0.0, "Analysis failed: " + e.getMessage());
            fallback.setExecutionTimeMs(System.currentTimeMillis() - startTime);
            return fallback;
        }
    }

    /**
     * Internal template method implemented by specific detectors.
     */
    protected abstract DetectorOutput doAnalysis(BufferedImage originalImage, byte[] rawFileBytes) throws Exception;
}

