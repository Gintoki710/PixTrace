/**
 * PixTrace — Pixel trail to the edit
 * AI-Based Image Forgery Detection & Authentication
 * Frontend Controller (Vanilla JavaScript)
 */

let selectedFile = null;
let currentAnalysisId = null;

document.addEventListener("DOMContentLoaded", () => {
    initThemeToggle();
    initNavigation();
    initUploadZone();
    initSlider();
    initAuditVerification();
});

// Interactive Day / Night Mode Toggle
function initThemeToggle() {
    const themeBtn = document.getElementById("theme-toggle");
    const savedTheme = localStorage.getItem("pixtrace-theme") || "dark";
    
    // Apply initial theme
    document.documentElement.setAttribute("data-theme", savedTheme);

    if (themeBtn) {
        themeBtn.addEventListener("click", () => {
            const currentTheme = document.documentElement.getAttribute("data-theme") || "dark";
            const newTheme = currentTheme === "dark" ? "light" : "dark";
            
            document.documentElement.setAttribute("data-theme", newTheme);
            localStorage.setItem("pixtrace-theme", newTheme);
        });
    }
}

// Interactive Tab Navigation
function initNavigation() {
    const navButtons = document.querySelectorAll(".interactive-nav-btn");
    navButtons.forEach(btn => {
        btn.addEventListener("click", () => {
            navButtons.forEach(b => b.classList.remove("active"));
            document.querySelectorAll(".tab-pane").forEach(p => p.classList.remove("active"));

            btn.classList.add("active");
            const target = btn.getAttribute("data-tab");
            const targetPane = document.getElementById(target);
            if (targetPane) {
                targetPane.classList.add("active");
            }

            if (target === "history-tab") loadHistory();
            if (target === "audit-tab") loadAuditChain();
        });
    });
}

// Drag & Drop / File Selection
function initUploadZone() {
    const dropZone = document.getElementById("drop-zone");
    const fileInput = document.getElementById("file-input");
    const previewBar = document.getElementById("file-preview-bar");
    const btnAnalyze = document.getElementById("btn-start-analysis");

    ["dragenter", "dragover"].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropZone.classList.add("dragover");
        });
    });

    ["dragleave", "drop"].forEach(eventName => {
        dropZone.addEventListener(eventName, (e) => {
            e.preventDefault();
            dropZone.classList.remove("dragover");
        });
    });

    dropZone.addEventListener("drop", (e) => {
        if (e.dataTransfer.files && e.dataTransfer.files[0]) {
            handleFileSelect(e.dataTransfer.files[0]);
        }
    });

    fileInput.addEventListener("change", (e) => {
        if (e.target.files && e.target.files[0]) {
            handleFileSelect(e.target.files[0]);
        }
    });

    btnAnalyze.addEventListener("click", () => {
        if (selectedFile) {
            executeAnalysis(selectedFile);
        }
    });
}

function handleFileSelect(file) {
    if (!file.type.startsWith("image/")) {
        alert("Please select a valid image file (JPEG, PNG, WebP).");
        return;
    }
    selectedFile = file;
    document.getElementById("preview-filename").textContent = file.name;
    document.getElementById("preview-filesize").textContent = formatBytes(file.size);
    document.getElementById("file-preview-bar").classList.remove("hidden");
}

function formatBytes(bytes) {
    if (bytes === 0) return '0 Bytes';
    const k = 1024;
    const sizes = ['Bytes', 'KB', 'MB', 'GB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
}

// Heatmap Opacity Slider Control
function initSlider() {
    const slider = document.getElementById("opacity-slider");
    const sliderVal = document.getElementById("slider-val");
    const heatmapImg = document.getElementById("img-heatmap");

    slider.addEventListener("input", (e) => {
        const val = e.target.value;
        sliderVal.textContent = val + "%";
        heatmapImg.style.opacity = val / 100.0;
    });
}

// API: Execute Multi-Modal Analysis
async function executeAnalysis(file) {
    const loader = document.getElementById("scan-loader");
    const resultsDash = document.getElementById("results-dashboard");

    loader.classList.remove("hidden");
    resultsDash.classList.add("hidden");

    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await fetch("/api/analysis/detect", {
            method: "POST",
            body: formData
        });

        if (!response.ok) {
            const err = await response.text();
            throw new Error(err || "Failed to analyze image");
        }

        const data = await response.json();
        renderAnalysisResults(data);
    } catch (err) {
        alert("Analysis Error: " + err.message);
    } finally {
        loader.classList.add("hidden");
    }
}

// Render Results in Dashboard
function renderAnalysisResults(data) {
    currentAnalysisId = data.id;
    const resultsDash = document.getElementById("results-dashboard");
    resultsDash.classList.remove("hidden");

    // Summary metadata
    document.getElementById("res-filename").textContent = data.filename;
    document.getElementById("res-sha256").textContent = data.sha256;
    document.getElementById("res-audithash").textContent = data.auditHash || "Sealed in Genesis Chain";

    // Set Images in Viewer
    document.getElementById("img-original").src = data.originalImageDataUrl;
    document.getElementById("img-heatmap").src = data.compositeHeatmapDataUrl;

    // Reset slider to 50%
    document.getElementById("opacity-slider").value = 50;
    document.getElementById("slider-val").textContent = "50%";
    document.getElementById("img-heatmap").style.opacity = 0.5;

    // Render Trust Score Gauge
    const score = data.trustScore.trustScore;
    document.getElementById("score-number").textContent = Math.round(score);

    const verdictPill = document.getElementById("verdict-pill");
    const gauge = document.getElementById("score-gauge");
    verdictPill.className = "verdict-pill";

    let color = "#00e676";
    if (data.trustScore.verdict === "AUTHENTIC") {
        verdictPill.classList.add("verdict-authentic");
        verdictPill.textContent = "AUTHENTIC (VERIFIED)";
        color = "#00e676";
    } else if (data.trustScore.verdict === "SUSPICIOUS") {
        verdictPill.classList.add("verdict-suspicious");
        verdictPill.textContent = "SUSPICIOUS (ANOMALIES)";
        color = "#ffb300";
    } else {
        verdictPill.classList.add("verdict-forged");
        verdictPill.textContent = "FORGED (TAMPERED)";
        color = "#ff3d57";
    }

    const deg = (score / 100) * 360;
    gauge.style.background = `conic-gradient(${color} ${deg}deg, #232d42 ${deg}deg)`;

    // Render Explanations
    const expList = document.getElementById("explanations-list");
    expList.innerHTML = "";
    data.trustScore.explanations.forEach(exp => {
        const li = document.createElement("li");
        li.textContent = "• " + exp;
        expList.appendChild(li);
    });

    // Render 4 Module Cards
    const modContainer = document.getElementById("modules-container");
    modContainer.innerHTML = "";
    data.detectorResults.forEach(mod => {
        const card = document.createElement("div");
        card.className = "module-card";

        const badgeClass = mod.tampered ? "badge-danger" : "badge-info";
        const badgeText = mod.tampered ? `Tampered (${Math.round(mod.tamperConfidence)}%)` : `Clean (${Math.round(mod.tamperConfidence)}%)`;

        card.innerHTML = `
            <div>
                <div class="module-top">
                    <span class="module-title">${escapeHtml(mod.detectorName)}</span>
                    <span class="badge ${badgeClass}">${badgeText}</span>
                </div>
                <p class="module-desc">${escapeHtml(mod.summary)}</p>
            </div>
            <div class="module-stat">
                Execution Time: ${mod.executionTimeMs} ms
            </div>
        `;
        modContainer.appendChild(card);
    });

    // Download PDF Button
    document.getElementById("btn-download-pdf").onclick = () => {
        window.open(`/api/analysis/${data.id}/report`, "_blank");
    };

    // Smooth scroll down to results
    resultsDash.scrollIntoView({ behavior: 'smooth' });
}

// API: Load Historical Audits
async function loadHistory() {
    const tbody = document.getElementById("history-table-body");
    tbody.innerHTML = `<tr><td colspan="7" class="text-center">Loading audit history...</td></tr>`;

    try {
        const res = await fetch("/api/analysis/history");
        const records = await res.json();

        if (records.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center text-muted">No historical scans recorded yet.</td></tr>`;
            return;
        }

        tbody.innerHTML = "";
        records.forEach(r => {
            const tr = document.createElement("tr");
            const dateStr = new Date(r.analyzedAt).toLocaleString();
            tr.innerHTML = `
                <td>#${r.id}</td>
                <td><strong>${escapeHtml(r.filename)}</strong></td>
                <td class="font-mono text-truncate" title="${r.sha256}">${r.sha256.substring(0, 16)}...</td>
                <td><strong>${r.trustScore.toFixed(1)}</strong> / 100</td>
                <td><span class="badge ${r.verdict === 'AUTHENTIC' ? 'badge-info' : 'badge-danger'}">${r.verdict}</span></td>
                <td>${dateStr}</td>
                <td>
                    <a href="/api/analysis/${r.id}/report" target="_blank" class="btn btn-sm btn-outline">PDF Report</a>
                </td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center text-danger">Failed to load history: ${e.message}</td></tr>`;
    }
}

// API: Load Cryptographic Hash Chain (Mini-Blockchain)
async function loadAuditChain() {
    const tbody = document.getElementById("audit-table-body");
    tbody.innerHTML = `<tr><td colspan="6" class="text-center font-sans">Loading blockchain blocks...</td></tr>`;

    try {
        const res = await fetch("/api/audit/logs");
        const logs = await res.json();

        if (logs.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center text-muted font-sans">Blockchain is currently empty.</td></tr>`;
            return;
        }

        tbody.innerHTML = "";
        logs.forEach(log => {
            const tr = document.createElement("tr");
            const dateStr = new Date(log.timestamp).toLocaleString();
            tr.innerHTML = `
                <td><strong>Block #${log.id}</strong></td>
                <td><span class="badge badge-info">${log.eventType}</span></td>
                <td class="text-truncate" title="${log.previousHash}">${log.previousHash.substring(0, 16)}...</td>
                <td class="text-truncate text-highlight" title="${log.currentHash}">${log.currentHash.substring(0, 16)}...</td>
                <td class="font-sans">${escapeHtml(log.details || '')}</td>
                <td class="font-sans">${dateStr}</td>
            `;
            tbody.appendChild(tr);
        });
    } catch (e) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center text-danger font-sans">Failed to load audit blocks: ${e.message}</td></tr>`;
    }
}

// API: Verify Cryptographic Chain Integrity
function initAuditVerification() {
    const btn = document.getElementById("btn-verify-chain");
    const banner = document.getElementById("chain-status-banner");

    btn.addEventListener("click", async () => {
        btn.textContent = "Verifying Hashes...";
        try {
            const res = await fetch("/api/audit/verify-chain");
            const data = await res.json();

            banner.classList.remove("hidden", "success", "danger");
            if (data.chainIntact) {
                banner.classList.add("success");
                banner.textContent = "✔ " + data.message;
            } else {
                banner.classList.add("danger");
                banner.textContent = "✖ " + data.message;
            }
        } catch (e) {
            alert("Verification error: " + e.message);
        } finally {
            btn.textContent = "Verify Cryptographic Integrity";
        }
    });
}

function escapeHtml(text) {
    if (!text) return "";
    return text
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}

