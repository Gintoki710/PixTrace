@echo off
title PixTrace - System Health Check
echo ========================================================
echo  PixTrace - Checking Service Health
echo ========================================================
echo.

powershell -Command "
Write-Host 'Testing Python AI Service (http://127.0.0.1:8000/health)...' -NoNewline
try {
    $res = Invoke-RestMethod -Uri 'http://127.0.0.1:8000/health' -TimeoutSec 3 -ErrorAction Stop
    Write-Host ' [ONLINE]' -ForegroundColor Green
    Write-Host ('   Service: ' + $res.service + ' (Status: ' + $res.status + ')')
} catch {
    Write-Host ' [OFFLINE]' -ForegroundColor Red
}

Write-Host ''
Write-Host 'Testing Spring Boot Backend (http://localhost:8080/api/audit/verify-chain)...' -NoNewline
try {
    $res = Invoke-RestMethod -Uri 'http://localhost:8080/api/audit/verify-chain' -TimeoutSec 3 -ErrorAction Stop
    Write-Host ' [ONLINE]' -ForegroundColor Green
    Write-Host ('   Blockchain Integrity: ' + $res.message)
} catch {
    Write-Host ' [OFFLINE]' -ForegroundColor Red
}
"

echo.
pause

