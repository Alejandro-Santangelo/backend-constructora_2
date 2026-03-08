# ============================================
# BACKUP SOLO SCHEMA DE BD RAILWAY
# ============================================
# Hace backup SOLO de ESTRUCTURA (sin datos del cliente)
# Útil para aplicar cambios de schema a BD local

$ErrorActionPreference = "Stop"

# Configuración
$FECHA = Get-Date -Format "yyyyMMdd_HHmmss"
$BACKUP_DIR = ".\backups"
$BACKUP_FILE = "$BACKUP_DIR\railway_schema_only_$FECHA.sql"

Write-Host "🔄 Iniciando backup SOLO SCHEMA de Railway..." -ForegroundColor Cyan
Write-Host ""

# Crear carpeta de backups si no existe
if (!(Test-Path $BACKUP_DIR)) {
    New-Item -ItemType Directory -Path $BACKUP_DIR | Out-Null
    Write-Host "✅ Carpeta de backups creada: $BACKUP_DIR" -ForegroundColor Green
}

# IMPORTANTE: Obtener DATABASE_URL desde Railway
Write-Host "📋 PASO 1: Obtener DATABASE_URL desde Railway Dashboard" -ForegroundColor Yellow
Write-Host "   1. Ir a: https://railway.app/dashboard" -ForegroundColor Gray
Write-Host "   2. Seleccionar proyecto: backend-constructora_2" -ForegroundColor Gray
Write-Host "   3. Click en PostgreSQL" -ForegroundColor Gray
Write-Host "   4. Tab 'Variables' → Copiar 'DATABASE_URL'" -ForegroundColor Gray
Write-Host ""

$DATABASE_URL = Read-Host "Pegar DATABASE_URL aquí"

if ([string]::IsNullOrWhiteSpace($DATABASE_URL)) {
    Write-Host "❌ DATABASE_URL vacío. Abortando." -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "📥 PASO 2: Ejecutando pg_dump (solo schema)..." -ForegroundColor Yellow

try {
    # Ejecutar pg_dump con --schema-only (sin datos)
    $env:PGPASSWORD = ""  # Se obtiene del DATABASE_URL
    pg_dump $DATABASE_URL --schema-only -f $BACKUP_FILE -v
    
    if (Test-Path $BACKUP_FILE) {
        $size = (Get-Item $BACKUP_FILE).Length / 1KB
        Write-Host ""
        Write-Host "✅ BACKUP DE SCHEMA COMPLETADO!" -ForegroundColor Green
        Write-Host "📁 Archivo: $BACKUP_FILE" -ForegroundColor Cyan
        Write-Host "💾 Tamaño: $([math]::Round($size, 2)) KB" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "📌 Este archivo contiene:" -ForegroundColor Yellow
        Write-Host "   ✓ CREATE TABLE de todas las tablas" -ForegroundColor Gray
        Write-Host "   ✓ Constraints (PRIMARY KEY, FOREIGN KEY)" -ForegroundColor Gray
        Write-Host "   ✓ Índices y secuencias" -ForegroundColor Gray
        Write-Host "   ✗ NO contiene datos del cliente" -ForegroundColor Red
        Write-Host ""
        Write-Host "💡 USO:" -ForegroundColor Cyan
        Write-Host "   Para aplicar este schema a BD local:" -ForegroundColor Gray
        Write-Host "   psql -U postgres -d construccion_local -f $BACKUP_FILE" -ForegroundColor Yellow
    } else {
        Write-Host "❌ Error: archivo de backup no creado" -ForegroundColor Red
        exit 1
    }
} catch {
    Write-Host "❌ Error ejecutando pg_dump: $_" -ForegroundColor Red
    exit 1
}
