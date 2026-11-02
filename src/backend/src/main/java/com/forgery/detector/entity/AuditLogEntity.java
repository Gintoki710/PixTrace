package com.forgery.detector.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * JPA Entity implementing a Tamper-Evident Hash-Chained Audit Trail.
 * Each entry stores the hash of the preceding entry, forming a cryptographic chain.
 * 
 * Demonstrates: Cryptography, Database Auditing, Immutability.
 */
@Entity
@Table(name = "audit_log")
public class AuditLogEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String eventType; // e.g., IMAGE_ANALYZED, USER_LOGIN, VERDICT_GENERATED

    private String imageSha256;

    @Column(length = 1000)
    private String details;

    @Column(length = 64, nullable = false)
    private String previousHash; // Hash of record (id - 1)

    @Column(length = 64, nullable = false)
    private String currentHash; // SHA-256(previousHash + eventType + imageSha256 + details + timestamp)

    private LocalDateTime timestamp;

    public AuditLogEntity() {
        this.timestamp = LocalDateTime.now();
    }

    public AuditLogEntity(String eventType, String imageSha256, String details, String previousHash, String currentHash) {
        this.eventType = eventType;
        this.imageSha256 = imageSha256;
        this.details = details;
        this.previousHash = previousHash;
        this.currentHash = currentHash;
        this.timestamp = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getImageSha256() {
        return imageSha256;
    }

    public void setImageSha256(String imageSha256) {
        this.imageSha256 = imageSha256;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getPreviousHash() {
        return previousHash;
    }

    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }

    public String getCurrentHash() {
        return currentHash;
    }

    public void setCurrentHash(String currentHash) {
        this.currentHash = currentHash;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}

