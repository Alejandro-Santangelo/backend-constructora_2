# ============================================
# Script Simple: Solo Crear Backup
# Para ejecutar ANTES de hacer cambios manuales

#   .\backup-bd-construccion-app.ps1
#     explorer .\db-backups


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

# Crear backup
Write-Info "Creando backup..."
& pg_dump -h $DbHost -p $DbPort -U $DbUser -d $DbName -F p -f $backupFile

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