package com.forgery.detector.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Output data contract produced by each individual Forgery Detector.
 * 
 * Demonstrates: Encapsulation, Serialization, Collections.
 */
public class DetectorOutput implements Serializable {
    private static final long serialVersionUID = 1L;

    private String detectorName;
    private boolean tampered;
    private double tamperConfidence; // 0.0 to 100.0%
    private String summary;
    private Map<String, Object> details = new HashMap<>();
    private String heatmapBase64; // Data URL for display
    private long executionTimeMs;

    public DetectorOutput() {}

    public DetectorOutput(String detectorName, boolean tampered, double tamperConfidence, String summary) {
        this.detectorName = detectorName;
        this.tampered = tampered;
        this.tamperConfidence = Math.max(0.0, Math.min(100.0, tamperConfidence));
        this.summary = summary;
    }

    public String getDetectorName() {
        return detectorName;
    }

    public void setDetectorName(String detectorName) {
        this.detectorName = detectorName;
    }

    public boolean isTampered() {
        return tampered;
    }

    public void setTampered(boolean tampered) {
        this.tampered = tampered;
    }

    public double getTamperConfidence() {
        return tamperConfidence;
    }

    public void setTamperConfidence(double tamperConfidence) {
        this.tamperConfidence = Math.max(0.0, Math.min(100.0, tamperConfidence));
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

    public void setDetails(Map<String, Object> details) {
        this.details = details;
    }

    public void addDetail(String key, Object value) {
        this.details.put(key, value);
    }

    public String getHeatmapBase64() {
        return heatmapBase64;
    }

    public void setHeatmapBase64(String heatmapBase64) {
        this.heatmapBase64 = heatmapBase64;
    }

    public long getExecutionTimeMs() {
        return executionTimeMs;
    }

    public void setExecutionTimeMs(long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
    }
}

