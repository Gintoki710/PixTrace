package com.forgery.detector.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Complete analysis payload returned to the Web Client.
 */
public class AnalysisResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String filename;
    private long fileSize;
    private String sha256;
    private String originalImageDataUrl;
    private String compositeHeatmapDataUrl;
    private TrustScoreResult trustScore;
    private List<DetectorOutput> detectorResults = new ArrayList<>();
    private String auditHash;
    private LocalDateTime analyzedAt;

    public AnalysisResponse() {
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

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getSha256() {
        return sha256;
    }

    public void setSha256(String sha256) {
        this.sha256 = sha256;
    }

    public String getOriginalImageDataUrl() {
        return originalImageDataUrl;
    }

    public void setOriginalImageDataUrl(String originalImageDataUrl) {
        this.originalImageDataUrl = originalImageDataUrl;
    }

    public String getCompositeHeatmapDataUrl() {
        return compositeHeatmapDataUrl;
    }

    public void setCompositeHeatmapDataUrl(String compositeHeatmapDataUrl) {
        this.compositeHeatmapDataUrl = compositeHeatmapDataUrl;
    }

    public TrustScoreResult getTrustScore() {
        return trustScore;
    }

    public void setTrustScore(TrustScoreResult trustScore) {
        this.trustScore = trustScore;
    }

    public List<DetectorOutput> getDetectorResults() {
        return detectorResults;
    }

    public void setDetectorResults(List<DetectorOutput> detectorResults) {
        this.detectorResults = detectorResults;
    }

    public String getAuditHash() {
        return auditHash;
    }

    public void setAuditHash(String auditHash) {
        this.auditHash = auditHash;
    }

    public LocalDateTime getAnalyzedAt() {
        return analyzedAt;
    }

    public void setAnalyzedAt(LocalDateTime analyzedAt) {
        this.analyzedAt = analyzedAt;
    }
}

