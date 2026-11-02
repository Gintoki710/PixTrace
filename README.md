# PixTrace — AI Image Forgery Detection & Authentication System
> **"Pixel trail to the edit"**

### Academic Review Information
* **Course:** PAMCA502 – Java Programming Project Review
* **Guide:** Dr. John Singh K. | Faculty: SCORE
* **Team Member 1:** Abdul Arman Rayeen (Roll No.: 26MCA0238)
* **Team Member 2:** Harsh Gupta (Roll No.: 26MCA0211)

---

## 1. Directory Structure

This repository is organized following standard enterprise software engineering conventions:

```
Java Project/
├── bin/                       # Executable launcher and diagnostic scripts
│   ├── run_all.bat            # One-click start for ALL services & browser
│   ├── run_backend.bat        # Starts Spring Boot Backend on http://localhost:8080
│   ├── run_ai_service.bat     # Starts Python FastAPI on http://127.0.0.1:8000
│   └── check_health.bat       # Tests health status of both services
│
├── src/                       # Application Source Code
│   ├── backend/               # Spring Boot 3 Maven Project (Java 17/21)
│   │   ├── pom.xml            # Dependencies: JPA, Spring Security, OpenPDF, Drew Noakes
│   │   ├── data/              # Embedded H2 Database file
│   │   └── src/main/java/     # Core Java forensic packages
│   │
│   └── ai_service/            # Python FastAPI AI Microservice
│       ├── requirements.txt   # Dependencies: fastapi, uvicorn, opencv, pillow, numpy
│       └── app/               # CASIA-v2 Deep Neural Network & Noise Residual Filter
│
├── web/                       # Standalone Frontend Web Assets
│   ├── index.html             # Pitch Black OLED / Day mode dashboard
│   ├── css/style.css          # Cyber-forensics stylesheet & animations
│   └── js/app.js              # State manager, comparison slider, REST API handlers
│
├── tools/                     # Toolchains & Portable Runtimes
│   └── maven/                 # Portable Apache Maven 3.9.6 (zero setup needed)
│
├── docs/                      # Technical Documentation & Viva Guides
│   ├── ARCHITECTURE.md        # Architecture diagrams, schemas, and API design
│   ├── SYSTEM_WORKFLOW.md     # Mathematical algorithms (ELA, Splicing, Cloning)
│   └── VIVA_PREPARATION.md    # Java concepts & oral examination cheat sheet
│
├── README.md                  # This file
├── run.bat                    # 1-Click root launcher (starts everything)
├── run_backend.bat            # 1-Click root launcher for Spring Boot
└── run_ai_service.bat         # 1-Click root launcher for Python AI
```

---

## 2. Quick Start (How to Run)

### Method 1: One-Click Start (Recommended)
Simply double-click:
```text
run.bat
```
*(or run `bin\run_all.bat`)*

This script automatically:
1. Starts the **Python FastAPI microservice** on `http://127.0.0.1:8000`.
2. Starts the **Spring Boot backend** on `http://localhost:8080`.
3. Opens the **PixTrace dashboard** directly in your default browser.

---

### Method 2: Manual Start

#### Terminal 1 (Python AI Microservice):
```powershell
cd src\ai_service
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload
```
* **Swagger API Documentation:** `http://127.0.0.1:8000/docs`

#### Terminal 2 (Spring Boot Backend):
```powershell
cd src\backend
..\..\tools\maven\apache-maven-3.9.6\bin\mvn.cmd spring-boot:run
```
* **Web Dashboard:** `http://localhost:8080`
* **H2 Database Console:** `http://localhost:8080/h2-console`
  * JDBC URL: `jdbc:h2:file:./data/forgery_db`
  * Username: `sa`
  * Password: `password`

---

## 3. Java Concepts Implemented (Slide 7 Mapping)

| Module | What It Does | Exact Java Concepts Demonstrated |
| :--- | :--- | :--- |
| **Upload & Validation** | Validates file format, size, computes SHA-256 | File I/O (`java.nio.file`), Custom Exception Handling, Cryptographic `MessageDigest` |
| **Detection Engine** | Metadata, ELA, Copy-Move, and AI modules | **Interfaces** (`ForgeryDetector`), **Inheritance** (`AbstractForgeryDetector`), **Multithreading** (`ExecutorService`, `CompletableFuture.allOf`) |
| **Evidence Fusion Engine** | Computes 0–100 Trust Score | **Java Collections**, **Stream API**, **Lambdas**, Strategy Pattern |
| **Authentication & Audit** | Hash-chained tamper-evident audit log | **Cryptography** (`SHA-256`), **Immutability**, JPA Entities & Transactions |
| **Report Generation** | Downloadable PDF Certificate | OpenPDF, Binary Stream I/O, Document Formatting |
| **Persistence & Auth** | User sessions & historical records | Spring Data JPA, Hibernate ORM, BCrypt, JWT |
