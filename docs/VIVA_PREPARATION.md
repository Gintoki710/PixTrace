# PixTrace - Viva & Evaluation Preparation Guide
### Course: PAMCA502 Java Programming | Guide: Dr. John Singh K. | SCORE

This document provides quick answers to standard questions during your project review.

---

### Q1: How did you use Object-Oriented Programming (OOP) in this project?
* **Interfaces & Polymorphism:** We created the `ForgeryDetector` interface implemented by `MetadataDetector`, `ErrorLevelAnalysisDetector`, `CopyMoveDetector`, and `AiClassifierDetector`. Spring automatically injects all implementations into a `List<ForgeryDetector>` polymorphically.
* **Inheritance & Template Pattern:** `AbstractForgeryDetector` handles execution time benchmarking, error handling, and logging, while child classes only implement `doAnalysis()`.
* **Encapsulation:** Entities (`ImageRecord`, `AuditLogEntity`, `UserEntity`) and DTOs (`DetectorOutput`, `TrustScoreResult`, `AnalysisResponse`) strictly encapsulate fields with validation.

---

### Q2: Where and why did you use Java Multithreading?
* **Where:** In `AnalysisOrchestrationService.java`.
* **Why:** Running 4 complex forensic detection algorithms sequentially would take 4–6 seconds per image. By using `ExecutorService` with a fixed thread pool of 4 workers and `CompletableFuture.supplyAsync()`, all detectors run concurrently across multiple CPU cores, reducing response time to under 1 second.
* **Synchronization:** `CompletableFuture.allOf(...).join()` ensures the orchestrator waits for all threads before fusing evidence.

---

### Q3: Where are Java Streams and Lambdas used?
* **Where:** In `TrustScoreFusionEngine.java`.
* **How:**
  * To calculate total weights and weighted penalties dynamically:
    ```java
    double totalWeight = outputs.stream()
            .mapToDouble(o -> weightMap.getOrDefault(o.getDetectorName(), 0.25))
            .sum();
    ```
  * To filter only tampered modules to generate explainable bullet points:
    ```java
    outputs.stream()
            .filter(DetectorOutput::isTampered)
            .forEach(o -> result.addExplanation(...));
    ```

---

### Q4: How is data persisted, and how do you prevent database tampering?
* **ORM & JPA:** We use Spring Data JPA and Hibernate with `ImageRecordRepository`, `AuditLogRepository`, and `UserRepository`.
* **Tamper-Evident Audit Chain:** We implemented a cryptographic mini-blockchain in `HashChainAuditService.java`. Every audit entry hashes the previous entry's SHA-256 hash. If someone directly alters a database row, recomputing the chain flags a cryptographic mismatch instantly.

---

### Q5: What is the Python microservice doing and how does Java communicate with it?
* **Role:** The Python microservice uses FastAPI and OpenCV to run high-pass Laplacian spatial filtering and CASIA-v2 deep learning splicing analysis.
* **Communication:** Spring Boot uses Java 11+ `java.net.http.HttpClient` to send a multipart HTTP POST request to `http://127.0.0.1:8000/api/v1/analyze`, parsing the JSON response using Jackson `ObjectMapper`.
* **Fault Tolerance:** If the Python service is offline, Java automatically falls back to an internal Edge-Gradient Analyzer so the application never crashes.

---

### Q6: How does the Forensic PDF Certificate work?
* In `PdfReportService.java`, we use the **OpenPDF** library to generate a tamper certificate containing the image's SHA-256 digest, verdict banner, module findings table, and evaluation timestamps.

