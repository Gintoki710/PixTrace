package com.forgery.detector.service.audit;

import com.forgery.detector.entity.AuditLogEntity;
import com.forgery.detector.repository.AuditLogRepository;
import com.forgery.detector.util.CryptoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Hash-Chained Audit Service.
 * Implements an immutable, cryptographic hash chain (mini-blockchain) to guarantee
 * that forensic inspection logs cannot be altered after the fact.
 * 
 * Demonstrates: Cryptography, Database Auditing, Spring Transactions, Immutability.
 */
@Service
public class HashChainAuditService {

    private static final String GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000";

    @Autowired
    private AuditLogRepository auditLogRepository;

    /**
     * Appends a new immutable event to the cryptographic audit chain.
     */
    @Transactional
    public synchronized AuditLogEntity recordEvent(String eventType, String imageSha256, String details) {
        Optional<AuditLogEntity> latestEntry = auditLogRepository.findTopByOrderByIdDesc();
        String previousHash = latestEntry.map(AuditLogEntity::getCurrentHash).orElse(GENESIS_HASH);

        LocalDateTime now = LocalDateTime.now();
        String payload = previousHash + "|" + eventType + "|" + (imageSha256 != null ? imageSha256 : "") + "|" + details + "|" + now;
        String currentHash = CryptoUtils.hashString(payload);

        AuditLogEntity entry = new AuditLogEntity(eventType, imageSha256, details, previousHash, currentHash);
        entry.setTimestamp(now);
        return auditLogRepository.save(entry);
    }

    /**
     * Verifies the cryptographic chain across all historical records.
     * Returns true if the audit log is completely untampered, false if any record was modified.
     */
    public boolean verifyChainIntegrity() {
        List<AuditLogEntity> logs = auditLogRepository.findAllByOrderByIdAsc();
        if (logs.isEmpty()) return true;

        String expectedPrevHash = GENESIS_HASH;
        for (AuditLogEntity entry : logs) {
            if (!entry.getPreviousHash().equals(expectedPrevHash)) {
                return false; // Broken chain link!
            }
            String payload = entry.getPreviousHash() + "|" + entry.getEventType() + "|" + 
                    (entry.getImageSha256() != null ? entry.getImageSha256() : "") + "|" + 
                    entry.getDetails() + "|" + entry.getTimestamp();
            String recomputedHash = CryptoUtils.hashString(payload);

            if (!recomputedHash.equals(entry.getCurrentHash())) {
                return false; // Data inside this record was tampered!
            }
            expectedPrevHash = entry.getCurrentHash();
        }
        return true;
    }

    public List<AuditLogEntity> getAllLogs() {
        return auditLogRepository.findAllByOrderByIdAsc();
    }
}

