package com.forgery.detector.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Encapsulates the aggregated 0-100 Trust Score and forensic explanation.
 * 
 * Demonstrates: Encapsulation, Collections, Serialization.
 */
public class TrustScoreResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private double trustScore; // 0.0 (Completely Forged) to 100.0 (Authentic)
    private String verdict;    // AUTHENTIC, SUSPICIOUS, FORGED
    private String riskLevel;  // LOW, MEDIUM, CRITICAL
    private Map<String, Double> moduleWeights = new HashMap<>();
    private Map<String, Double> moduleDeductions = new HashMap<>();
    private List<String> explanations = new ArrayList<>();

    public TrustScoreResult() {}

    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = Math.round(trustScore * 100.0) / 100.0;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getRiskLevel() {
        return riskLevel;
    }

    public void setRiskLevel(String riskLevel) {
        this.riskLevel = riskLevel;
    }

    public Map<String, Double> getModuleWeights() {
        return moduleWeights;
    }

    public void setModuleWeights(Map<String, Double> moduleWeights) {
        this.moduleWeights = moduleWeights;
    }

    public Map<String, Double> getModuleDeductions() {
        return moduleDeductions;
    }

    public void setModuleDeductions(Map<String, Double> moduleDeductions) {
        this.moduleDeductions = moduleDeductions;
    }

    public List<String> getExplanations() {
        return explanations;
    }

    public void setExplanations(List<String> explanations) {
        this.explanations = explanations;
    }

    public void addExplanation(String explanation) {
        this.explanations.add(explanation);
    }
}

