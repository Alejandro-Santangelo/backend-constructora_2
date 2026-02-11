# ============================================
# Script Simple: Solo Crear Backup
# Para ejecutar ANTES de hacer cambios manuales

#   .\backup-bd-construccion-app.ps1
#     explorer .\db-backups
# Si algo sale mal, restaura con:
# psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f ".\db-backups\backup_construccion_app_v3_2026-02-11_12-58-23.sql"

# detener proceso puerto 8080
#  netstat -ano | findstr :8080
#  taskkill /F /PID 5656
# ============================================

param(
    [Parameter(Mandatory=$false)]
    [string]$DbHost = "localhost",
    
    [Parameter(Mandatory=$false)]
    [string]$DbPort = "5432",
    
    [Parameter(Mandatory=$false)]
    [string]$DbName = "construccion_app_v3",
    
    [Parameter(Mandatory=$false)]
    [string]$DbUser = "postgres",
    
    [Parameter(Mandatory=$false)]
    [string]$BackupDir = ".\db-backups"
)

# Colores
function Write-Success { Write-Host "OK $args" -ForegroundColor Green }
function Write-Info { Write-Host "INFO $args" -ForegroundColor Cyan }
function Write-Error { Write-Host "ERROR $args" -ForegroundColor Red }

Write-Host ""
Write-Host "CREAR BACKUP ANTES DE MODIFICAR BD" -ForegroundColor Magenta
Write-Host ""

# Verificar pg_dump
if (-not (Get-Command pg_dump -ErrorAction SilentlyContinue)) {
    Write-Error "pg_dump no encontrado. Instala PostgreSQL client tools."
    exit 1
}

# Crear directorio
if (-not (Test-Path $BackupDir)) {
    New-Item -ItemType Directory -Path $BackupDir | Out-Null
}

# Nombre con timestamp
$timestamp = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$backupFile = Join-Path $BackupDir "backup_${DbName}_${timestamp}.sql"

Write-Host "Base de datos: $DbName"
Write-Host "Backup: $backupFile"
Write-Host ""

# Pedir password
$securePassword = Read-Host "Contrasena PostgreSQL" -AsSecureString
$env:PGPASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringAuto([Runtime.InteropServices.Marshal]::SecureStringToBSTR($securePassword))

# Crear backup COMPLETO con todas las opciones necesarias
Write-Info "Creando backup completo de la base de datos..."
Write-Info "Incluyendo: estructura, datos, secuencias, indices, constraints, comentarios..."

# Opciones explicadas:
# -h: host
# -p: puerto
# -U: usuario
# -d: nombre de la base de datos
# -F p: formato plain (SQL text)
# -f: archivo de salida
# --data-only: NO, queremos estructura también
# --schema-only: NO, queremos datos también
# -a: incluir SOLO datos (deshabilitado, queremos todo)
# -c: incluir DROP antes de CREATE (limpia antes de restaurar)
# -C: incluir CREATE DATABASE
# --column-inserts: usar INSERT con nombres de columnas (más legible y robusto)
# --disable-dollar-quoting: usar comillas estándar
# --disable-triggers: desactivar triggers durante restauración
# -E UTF8: encoding UTF-8
# -v: verbose (mostrar progreso)
# --inserts: usar comandos INSERT en lugar de COPY (más compatible)
# --no-owner: no incluir comandos SET OWNER
# --no-privileges: no incluir comandos GRANT/REVOKE
# -O: igual que --no-owner
# -x: igual que --no-privileges

& pg_dump `
    -h $DbHost `
    -p $DbPort `
    -U $DbUser `
    -d $DbName `
    -F p `
    -f $backupFile `
    --column-inserts `
    --create `
    --clean `
    -E UTF8 `
    -v `
    --no-owner `
    --no-privileges

if ($LASTEXITCODE -eq 0) {
    $size = (Get-Item $backupFile).Length / 1KB
    $sizeRounded = [math]::Round($size, 2)
    Write-Success "Backup creado: $backupFile ($sizeRounded KB)"
    Write-Host ""
    Write-Host "Ahora podes ejecutar tus scripts SQL manualmente" -ForegroundColor Yellow
    Write-Host "Si algo sale mal, restaura con:" -ForegroundColor Cyan
    Write-Host "   psql -h $DbHost -p $DbPort -U $DbUser -d $DbName -f `"$backupFile`"" -ForegroundColor Gray
    Write-Host ""
} else {
    Write-Error "Error al crear backup"
}

$env:PGPASSWORD = $null