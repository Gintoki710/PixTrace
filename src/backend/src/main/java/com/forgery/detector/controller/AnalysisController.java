package com.forgery.detector.controller;

import com.forgery.detector.entity.ImageRecord;
import com.forgery.detector.model.AnalysisResponse;
import com.forgery.detector.repository.ImageRecordRepository;
import com.forgery.detector.service.AnalysisOrchestrationService;
import com.forgery.detector.service.report.PdfReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * REST Controller for image analysis, historical record retrieval, and PDF report downloads.
 * Demonstrates: Spring Web MVC, REST Endpoints, Multipart file processing, Streaming Binary I/O.
 */
@RestController
@RequestMapping("/api/analysis")
@CrossOrigin(origins = "*")
public class AnalysisController {

    @Autowired
    private AnalysisOrchestrationService orchestrationService;

    @Autowired
    private ImageRecordRepository imageRecordRepository;

    @Autowired
    private PdfReportService pdfReportService;

    /**
     * Upload an image for full parallel multi-modal forgery analysis.
     */
    @PostMapping(value = "/detect", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> analyzeImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("Uploaded file cannot be empty");
        }
        try {
            AnalysisResponse response = orchestrationService.analyzeImage(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Analysis failure: " + e.getMessage());
        }
    }

    /**
     * Retrieves previous image analyses.
     */
    @GetMapping("/history")
    public ResponseEntity<List<ImageRecord>> getHistory() {
        return ResponseEntity.ok(imageRecordRepository.findAllByOrderByAnalyzedAtDesc());
    }

    /**
     * Generates and streams a professional Forensic PDF report.
     */
    @GetMapping("/{id}/report")
    public ResponseEntity<byte[]> downloadPdfReport(@PathVariable("id") Long id) {
        try {
            byte[] pdfBytes = pdfReportService.generatePdfReport(id);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "Forensic_Report_" + id + ".pdf");
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}

