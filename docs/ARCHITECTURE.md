# PixTrace System Architecture & Technical Specification

## 1. High-Level Architecture Overview

PixTrace implements a distributed, multi-modal microservices architecture combining enterprise Java backend engineering with deep learning computer vision.

```
                           ┌───────────────────────────────────────────┐
                           │               PRESENTATION LAYER          │
                           │   PixTrace Modern Responsive Web Client   │
                           │     (HTML5 / CSS3 OLED / Vanilla JS)      │
                           └─────────────────────┬─────────────────────┘
                                                 │ HTTP / REST (Multipart)
                                                 ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                            ENTERPRISE CORE: SPRING BOOT 3 (PORT 8080)                         │
│                                                                                              │
│  [Security & Ingestion Layer]                                                                │
│   • Spring Security 6 (Stateless JWT / BCrypt Password Hashing)                              │
│   • Input Validation & Cryptographic SHA-256 Digest (java.security.MessageDigest)           │
│                                                                                              │
│  [Multithreaded Parallel Forensic Orchestrator] (ExecutorService & CompletableFuture)        │
│   ┌───────────────────────────────────────────────────────────────────────────────────────┐  │
│   │ 1. Metadata Analyzer        (Drew Noakes Library - EXIF/IPTC/XMP/C2PA Credentials)   │  │
│   │ 2. Error Level Analysis     (Java ImageIO - JPEG 90% recompression delta map)         │  │
│   │ 3. Copy-Move Clone Detector (Block-based feature vector extraction & spatial cluster) │  │
│   │ 4. AI Deep Learning Bridge  (HTTP REST client to Python FastAPI microservice)         │  │
│   └───────────────────────────────────────────┬───────────────────────────────────────────┘  │
│                                               ▼                                              │
│  [Evidence Fusion Engine] (Strategy Pattern, Stream API & Lambdas)                           │
│   • Normalized weighted deduction algorithm computing final 0-100 Trust Score                │
│   • Composite thermal Heatmap Generator (AlphaComposite blending)                            │
│                                                                                              │
│  [Persistence & Audit Layer]                                                                 │
│   • Spring Data JPA / Hibernate ORM with H2 / MySQL                                          │
│   • Cryptographic Hash-Chained Audit Trail (Mini-Blockchain)                                 │
│   • Forensic PDF Certificate Generator (OpenPDF)                                             │
└───────────────────────────────────────────────┬──────────────────────────────────────────────┘
                                                │ JSON / HTTP REST
                                                ▼
┌──────────────────────────────────────────────────────────────────────────────────────────────┐
│                    AI COMPUTER VISION MICROSERVICE: FASTAPI (PORT 8000)                      │
│   • Framework: FastAPI + Uvicorn (Asynchronous Python 3.12)                                  │
│   • Architecture: CASIA-v2 Deep Residual CNN & Noise Residual Spatial Filter                 │
│   • High-pass 3x3 Laplacian Convolution Kernel extracting sensor pattern noise (PRNU)       │
│   • OpenCV Thermal JET Colormap generator for manipulated boundary detection                 │
└──────────────────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Directory Structure Conventions

```
Java Project/
├── bin/                       # Executable launcher and diagnostic scripts
│   ├── run_all.bat            # One-click start for all services & auto-open browser
│   ├── run_backend.bat        # Spring Boot backend launcher
│   ├── run_ai_service.bat     # Python FastAPI microservice launcher
│   └── check_health.bat       # Diagnostic health check script
│
├── src/                       # Complete Application Source Code
│   ├── backend/               # Spring Boot 3 Maven Project
│   │   ├── pom.xml            # Dependencies: JPA, Security, OpenPDF, Drew Noakes
│   │   └── src/main/java/     # Core Java source code
│   └── ai_service/            # Python FastAPI Project
│       ├── requirements.txt   # Dependencies: fastapi, uvicorn, opencv, pillow, numpy
│       └── app/               # Microservice code
│
├── web/                       # Standalone Frontend Assets (HTML5/CSS3/Vanilla JS)
│   ├── index.html             # PixTrace Dark OLED & Day mode interface
│   ├── css/style.css          # Theme styles, animations, responsive grid
│   └── js/app.js              # State manager, comparison slider, REST API handlers
│
├── tools/                     # Local toolchains
│   └── maven/                 # Portable Apache Maven 3.9.6 distribution
│
├── docs/                      # Technical documentation
│   ├── ARCHITECTURE.md        # This file
│   ├── SYSTEM_WORKFLOW.md     # Step-by-step mathematical detection workflow
│   └── VIVA_PREPARATION.md    # Java concepts & oral examination cheat sheet
│
├── README.md                  # Quick start guide & team information
├── run.bat                    # Root convenient 1-click launcher
└── run_ai_service.bat         # Root shortcut for AI service
```

---

## 3. Database Schema (Entities & Relationships)

### 1. `ImageRecord` (`image_records`)
* `id` (BIGINT, Primary Key, Auto-increment)
* `filename` (VARCHAR)
* `sha256` (VARCHAR(64), Cryptographic File Digest)
* `file_size` (BIGINT)
* `trust_score` (DOUBLE, 0.0 - 100.0)
* `verdict` (VARCHAR: `AUTHENTIC`, `SUSPICIOUS`, `FORGED`)
* `analysis_json` (CLOB, Serialized detailed findings)
* `original_base64` (CLOB, Data URL)
* `heatmap_base64` (CLOB, Composite thermal overlay)
* `analyzed_at` (TIMESTAMP)

### 2. `AuditLogEntity` (`audit_log`)
* `id` (BIGINT, Primary Key, Block Number)
* `event_type` (VARCHAR: `IMAGE_ANALYZED`, `USER_LOGIN`, `SYSTEM_INIT`)
* `image_sha256` (VARCHAR(64))
* `details` (VARCHAR(1000))
* `previous_hash` (VARCHAR(64), Cryptographic pointer to block `id - 1`)
* `current_hash` (VARCHAR(64), `SHA256(previous_hash + event_type + image_sha256 + details + timestamp)`)
* `timestamp` (TIMESTAMP)

### 3. `UserEntity` (`users`)
* `id` (BIGINT, Primary Key)
* `username` (VARCHAR, Unique)
* `password` (VARCHAR, BCrypt Salted Hash)
* `role` (VARCHAR: `ROLE_ADMIN`, `ROLE_ANALYST`)
* `created_at` (TIMESTAMP)

