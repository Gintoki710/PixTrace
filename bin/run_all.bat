@echo off
title PixTrace - Starting All Services
cd /d "%~dp0"

echo ========================================================
echo  PixTrace - Launching All Services
echo ========================================================
echo.
echo [1/3] Starting Python FastAPI AI Microservice (Port 8000)...
start "PixTrace - AI Microservice (Port 8000)" cmd /k "call "%~dp0run_ai_service.bat""

timeout /t 3 /nobreak >nul

echo [2/3] Starting Spring Boot Backend (Port 8080)...
start "PixTrace - Spring Boot Backend (Port 8080)" cmd /k "call "%~dp0run_backend.bat""

echo [3/3] Waiting for servers to initialize...
timeout /t 8 /nobreak >nul

echo.
echo ========================================================
echo  PixTrace System is launching!
echo  Opening browser at: http://localhost:8080
echo  AI Docs at:         http://127.0.0.1:8000/docs
echo ========================================================
start http://localhost:8080
