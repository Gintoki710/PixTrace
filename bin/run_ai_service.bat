@echo off
title PixTrace - Python FastAPI AI Microservice
cd /d "%~dp0..\src\ai_service"

echo ========================================================
echo  PixTrace - AI Neural Microservice (FastAPI + OpenCV)
echo  CASIA v2 Deep Neural Splicing & Noise Residual Engine
echo ========================================================
echo.

python --version >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Python is not installed or not in PATH!
    echo Please install Python 3.10+ from python.org
    pause
    exit /b 1
)

echo [1/2] Verifying Python dependencies...
python -m pip install -r requirements.txt >nul 2>&1

echo [2/2] Starting FastAPI microservice on http://127.0.0.1:8000 ...
echo Swagger API documentation available at: http://127.0.0.1:8000/docs
echo.
python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 --reload

pause

