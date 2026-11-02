package com.forgery.detector.controller;

import com.forgery.detector.entity.AuditLogEntity;
import com.forgery.detector.service.audit.HashChainAuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * REST Controller for the Hash-Chained Audit Trail and integrity verification.
 */
@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    @Autowired
    private HashChainAuditService auditService;

    /**
     * Lists all hash-chained audit records.
     */
    @GetMapping("/logs")
    public ResponseEntity<List<AuditLogEntity>> getAuditLogs() {
        return ResponseEntity.ok(auditService.getAllLogs());
    }

    /**
     * Verifies the cryptographic integrity of the entire database audit chain.
     */
    @GetMapping("/verify-chain")
    public ResponseEntity<Map<String, Object>> verifyIntegrity() {
        boolean intact = auditService.verifyChainIntegrity();
        Map<String, Object> response = new HashMap<>();
        response.put("chainIntact", intact);
        response.put("message", intact 
                ? "Cryptographic hash chain verified. Zero database modifications detected." 
                : "WARNING: Hash chain breach detected! Database tampering identified.");
        return ResponseEntity.ok(response);
    }
}

