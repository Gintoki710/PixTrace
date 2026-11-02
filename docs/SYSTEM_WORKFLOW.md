# PixTrace Forensic Workflow & Mathematical Models

## 1. Step-by-Step Analysis Lifecycle

When an image is submitted for analysis:

### Stage 1: Cryptographic Ingestion
1. The uploaded file stream is intercepted in memory via `MultipartFile`.
2. A cryptographic **SHA-256 digest** is calculated:
   $$\text{SHA256}(B) = \text{Hex}(\text{MessageDigest}(\text{"SHA-256"}).\text{digest}(B))$$
3. The image is parsed into an in-memory `BufferedImage` without writing temporary unencrypted files to disk.

---

### Stage 2: Parallel Multi-Modal Detection (4 Engines)

Java's `ExecutorService` executes 4 forensic engines simultaneously across available CPU cores:

#### A. Metadata Inspection (`MetadataDetector.java`)
* Extracts EXIF, IPTC, and XMP headers using `metadata-extractor`.
* **Heuristics:**
  * Checks for digital editing signatures: `Photoshop`, `Canva`, `GIMP`, `Lightroom`, `PicsArt`, `Midjourney`.
  * Detects missing camera hardware tags (`Make`, `Model`).
  * Validates creation timestamp against modification timestamp.
  * Checks for signed **C2PA Content Credentials**.

#### B. Error Level Analysis / ELA (`ErrorLevelAnalysisDetector.java`)
* Resaves the image in memory at a fixed 90% JPEG quality.
* Computes the absolute delta for each pixel:
  $$\Delta_{(x,y)} = \min\left(255, \frac{|\text{Orig}_R - \text{Comp}_R| + |\text{Orig}_G - \text{Comp}_G| + |\text{Orig}_B - \text{Comp}_B|}{3} \times \text{Scale}\right)$$
* Generates a thermal visual difference map showing non-uniform recompression errors.

#### C. Copy-Move (Cloning) Detector (`CopyMoveDetector.java`)
* Divides the image into overlapping $16 \times 16$ pixel blocks with step size 8.
* Extracts luminance mean ($\mu$) and variance ($\sigma^2$) for each block:
  $$\mu = \frac{1}{N} \sum Y_i, \quad \sigma^2 = \frac{1}{N} \sum (Y_i - \mu)^2$$
* Sorts blocks lexicographically by luminance to achieve $O(N \log N)$ neighbor search efficiency.
* Clusters pairs with identical spatial displacement vectors:
  $$\vec{v} = (\Delta x, \Delta y) = (x_2 - x_1, y_2 - y_1)$$
* Clustered vectors $\ge 3$ contiguous blocks trigger verified copy-move cloning flags.

#### D. AI Neural Splicing Classifier (`AiClassifierDetector.java` & Python `neural_service.py`)
* Dispatches image bytes over HTTP to Python FastAPI on port 8000.
* Applies a 3x3 high-pass Laplacian spatial convolution kernel to isolate sensor pattern noise:
  $$K = \begin{bmatrix} -1 & -1 & -1 \\ -1 & 8 & -1 \\ -1 & -1 & -1 \end{bmatrix}$$
* Computes local standard deviation across $15 \times 15$ blur windows to spot foreign boundary noise divergence typical of spliced CASIA-v2 benchmarks.
* Outputs a thermal JET colormap highlighting synthetic or inserted elements.

---

### Stage 3: Evidence Fusion & Trust Score Calculation

The **Evidence Fusion Engine** combines detector findings using normalized weights:

$$\text{Weight}_{\text{normalized}}(i) = \frac{W_i}{\sum W_k}$$
$$\text{Tamper Penalty} = \sum_{i=1}^{4} \left( \text{Confidence}_i \times \text{Weight}_{\text{normalized}}(i) \right)$$
$$\text{Trust Score} = \max\left(0.0, \min\left(100.0, 100.0 - \text{Tamper Penalty}\right)\right)$$

#### Classification Thresholds:
* 🟢 **80.0 – 100.0**: **AUTHENTIC** (Clean, natural image)
* 🟡 **55.0 – 79.9**: **SUSPICIOUS** (Recompression anomalies or stripped EXIF)
* 🔴 **0.0 – 54.9**: **FORGED** (Confirmed splicing, cloning, or editing traces)

---

### Stage 4: Cryptographic Blockchain Sealing

The analysis is appended to an immutable database audit log:
$$\text{Block}_n.\text{PreviousHash} = \text{Block}_{n-1}.\text{CurrentHash}$$
$$\text{Block}_n.\text{CurrentHash} = \text{SHA256}(\text{PrevHash} \parallel \text{EventType} \parallel \text{ImageSHA} \parallel \text{Details} \parallel \text{Timestamp})$$

Clicking **"Verify Cryptographic Integrity"** walks the entire chain from Block #1 to verify that no historical records have been modified in the database.

