package com.forgery.detector.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forgery.detector.entity.ImageRecord;
import com.forgery.detector.model.AnalysisResponse;
import com.forgery.detector.model.DetectorOutput;
import com.forgery.detector.model.TrustScoreResult;
import com.forgery.detector.repository.ImageRecordRepository;
import com.forgery.detector.service.audit.HashChainAuditService;
import com.forgery.detector.service.detector.ForgeryDetector;
import com.forgery.detector.service.fusion.TrustScoreFusionEngine;
import com.forgery.detector.util.CryptoUtils;
import com.forgery.detector.util.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * Core Multithreaded Orchestrator.
 * Coordinates parallel forensic detector execution, evidence fusion,
 * persistence, and hash-chain audit logging.
 * 
 * Demonstrates: Multithreading (ExecutorService, CompletableFuture),
 * Dependency Injection, Spring Transactions, Stream API.
 */
@Service
public class AnalysisOrchestrationService {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private List<ForgeryDetector> detectors; // Automatically injects all 4 detectors (Polymorphism)

    @Autowired
    private TrustScoreFusionEngine fusionEngine;

    @Autowired
    private ImageRecordRepository imageRecordRepository;

    @Autowired
    private HashChainAuditService auditService;

    private final ExecutorService threadPool = Executors.newFixedThreadPool(4);
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Executes parallel image forgery analysis across all registered detectors.
     */
    public AnalysisResponse analyzeImage(MultipartFile file) throws Exception {
        byte[] fileBytes = file.getBytes();
        String originalFilename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "uploaded_image.jpg";

        // Step 1: Cryptographic Validation (SHA-256 Fingerprint)
        String sha256 = CryptoUtils.calculateSha256(fileBytes);
        logger.info("Initiating parallel forensic analysis for: {} (SHA-256: {})", originalFilename, sha256);

        BufferedImage bufferedImage = ImageUtils.bytesToBufferedImage(fileBytes);

        // Step 2: Multithreaded Parallel Execution across all 4 Detectors
        List<CompletableFuture<DetectorOutput>> futures = detectors.stream()
                .map(detector -> CompletableFuture.supplyAsync(
                        () -> detector.analyze(bufferedImage, fileBytes),
                        threadPool
                ))
                .collect(Collectors.toList());

        // Wait for all detector threads to complete execution
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        List<DetectorOutput> outputs = new ArrayList<>();
        for (CompletableFuture<DetectorOutput> future : futures) {
            outputs.add(future.get());
        }

        // Step 3: Evidence Fusion & 0-100 Trust Score Computation
        TrustScoreResult trustScore = fusionEngine.fuseEvidence(outputs, detectors);

        // Step 4: Generate Composite Heatmap Overlay
        String compositeHeatmap = fusionEngine.generateCompositeHeatmap(bufferedImage, outputs);

        // Step 5: Persist Result in Database
        ImageRecord record = new ImageRecord();
        record.setFilename(originalFilename);
        record.setSha256(sha256);
        record.setFileSize(file.getSize());
        record.setTrustScore(trustScore.getTrustScore());
        record.setVerdict(trustScore.getVerdict());
        record.setAnalysisJson(objectMapper.writeValueAsString(trustScore));
        record.setOriginalBase64(ImageUtils.toBase64DataUrl(bufferedImage, "jpg"));
        record.setHeatmapBase64(compositeHeatmap);
        record = imageRecordRepository.save(record);

        // Step 6: Log Tamper-Evident Event into Cryptographic Audit Chain
        var auditEntry = auditService.recordEvent(
                "IMAGE_ANALYZED",
                sha256,
                String.format("File '%s' analyzed. Verdict: %s (Trust Score: %.1f)", originalFilename, trustScore.getVerdict(), trustScore.getTrustScore())
        );

        // Step 7: Construct Response for Web UI
        AnalysisResponse response = new AnalysisResponse();
        response.setId(record.getId());
        response.setFilename(originalFilename);
        response.setFileSize(file.getSize());
        response.setSha256(sha256);
        response.setOriginalImageDataUrl(record.getOriginalBase64());
        response.setCompositeHeatmapDataUrl(compositeHeatmap);
        response.setTrustScore(trustScore);
        response.setDetectorResults(outputs);
        response.setAuditHash(auditEntry.getCurrentHash());
        response.setAnalyzedAt(record.getAnalyzedAt());

        return response;
    }
}

