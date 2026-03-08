# Script para iniciar backend sin DevTools (modo produccion)
Write-Host "Iniciando backend en modo produccion (sin DevTools)..." -ForegroundColor Green

# Matar proceso en puerto 8080 si existe
$process = Get-NetTCPConnection -LocalPort 8080 -ErrorAction SilentlyContinue | 
           Select-Object -ExpandProperty OwningProcess -Unique

if ($process) {
    Write-Host "Deteniendo proceso en puerto 8080 (PID: $process)..." -ForegroundColor Yellow
    Stop-Process -Id $process -Force
    Start-Sleep -Seconds 2
}

# Compilar sin tests
Write-Host "Compilando proyecto..." -ForegroundColor Cyan
./mvnw clean package -DskipTests

if ($LASTEXITCODE -ne 0) {
    Write-Host "Error en compilacion" -ForegroundColor Red
    exit 1
}

# Ejecutar JAR directamente (sin DevTools)
Write-Host "Ejecutando backend..." -ForegroundColor Green
Write-Host ""
java -jar target/construccion-backend-1.0.0.jar --spring.devtools.restart.enabled=false
