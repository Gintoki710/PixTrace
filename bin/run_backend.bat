@echo off
title PixTrace - Spring Boot Backend
setlocal enabledelayedexpansion

cd /d "%~dp0..\src\backend"

echo ========================================================
echo  PixTrace - Image Forgery Detection & Authentication
echo  PAMCA502 Java Programming Project Review
echo  Backend Service: Spring Boot 3 + Hibernate JPA
echo ========================================================
echo.

set "MVN_CMD=mvn"
where mvn >nul 2>&1
if %errorlevel% neq 0 (
    if exist "%~dp0..\tools\maven\apache-maven-3.9.6\bin\mvn.cmd" (
        set "MVN_CMD=%~dp0..\tools\maven\apache-maven-3.9.6\bin\mvn.cmd"
        echo [INFO] Using included portable Maven from tools\maven\apache-maven-3.9.6
    ) else (
        echo [ERROR] Maven was not found in PATH or tools\maven folder.
        pause
        exit /b 1
    )
)

echo [1/2] Compiling and starting Spring Boot backend on port 8080...
echo Web UI will be accessible at: http://localhost:8080
echo H2 Console accessible at:      http://localhost:8080/h2-console
echo.

call "%MVN_CMD%" spring-boot:run

pause

