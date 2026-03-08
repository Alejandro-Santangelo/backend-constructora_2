# Script para ejecutar el backend Spring Boot en modo desarrollo
# Compila y ejecuta sin hacer clean (más rápido)

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  EJECUTANDO BACKEND SPRING BOOT" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

.\mvnw spring-boot:run
