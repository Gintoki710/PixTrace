package com.forgery.detector.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity storing historical image forgery analysis results.
 * Demonstrates: Hibernate ORM, Large Objects (LOB), Relations.
 */
@Entity
@Table(name = "image_records")
public class ImageRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String filename;

    @Column(nullable = false, length = 64)
    private String sha256;

    private Long fileSize;

    private double trustScore;

    private String verdict; // AUTHENTIC, SUSPICIOUS, FORGED

    @Lob
    @Column(columnDefinition = "CLOB")
    private String analysisJson;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String originalBase64;

    @Lob
    @Column(columnDefinition = "CLOB")
    private String heatmapBase64;

    private LocalDateTime analyzedAt;

    public ImageRecord() {
        this.analyzedAt = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public double getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(double trustScore) {
        this.trustScore = trustScore;
    }

    public String getVerdict() {
        return verdict;
    }

    public void setVerdict(String verdict) {
        this.verdict = verdict;
    }

    public String getAnalysisJson() {
        return analysisJson;
    }

    public void setAnalysisJson(String analysisJson) {
        this.analysisJson = analysisJson;
    }

    public String getOriginalBase64() {
        return originalBase64;
    }

    public void setOriginalBase64(String originalBase64) {
        this.originalBase64 = originalBase64;
    }

    public String getHeatmapBase64() {
        return heatmapBase64;
    }

    public void setHeatmapBase64(String heatmapBase64) {
        this.heatmapBase64 = heatmapBase64;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}

