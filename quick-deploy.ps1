# Quick Deploy Backend - Script Opcional
# Uso: .\quick-deploy.ps1
# O simplemente ignóralo y haz git push directamente

param(
    [switch]$SkipTest,      # Omitir prueba local
    [switch]$SkipMerge,     # Solo push a cacho, sin merge a main
    [string]$Message = ""   # Mensaje de commit
)

Write-Host "🚀 Quick Deploy Backend" -ForegroundColor Cyan
Write-Host ""

# 1. Estado actual
$currentBranch = git branch --show-current
Write-Host "📍 Rama actual: $currentBranch" -ForegroundColor Yellow

# 2. Probar localmente (OPCIONAL)
if (-not $SkipTest) {
    Write-Host ""
    Write-Host "🧪 Compilando... (usa -SkipTest para omitir)" -ForegroundColor Cyan
    ./mvnw clean compile -DskipTests
    
    if ($LASTEXITCODE -ne 0) {
        Write-Host "❌ Compilación falló" -ForegroundColor Red
        exit 1
    }
    Write-Host "✅ Compilación OK" -ForegroundColor Green
}

# 3. Commit
Write-Host ""
if ($Message -eq "") {
    $Message = Read-Host "💬 Mensaje de commit"
}

git add -A
git commit -m $Message

if ($LASTEXITCODE -ne 0) {
    Write-Host "⚠️  No hay cambios para commitear o commit falló" -ForegroundColor Yellow
    exit 0
}

# 4. Push
Write-Host ""
Write-Host "📤 Pusheando a $currentBranch..." -ForegroundColor Cyan
git push origin $currentBranch

# 5. Merge a main (OPCIONAL)
if (-not $SkipMerge -and $currentBranch -ne "main") {
    Write-Host ""
    $doMerge = Read-Host "¿Mergear a main y deployar? (s/N)"
    
    if ($doMerge -eq "s" -or $doMerge -eq "S") {
        git checkout develop
        git merge $currentBranch --no-ff -m "Merge $currentBranch`: $Message"
        git push origin develop
        
        git checkout main
        git merge develop --no-ff -m "Merge develop`: $Message"
        git push origin main
        
        Write-Host ""
        Write-Host "🎉 Deploy a Railway iniciado desde main" -ForegroundColor Green
        Write-Host "🌐 Verifica: https://railway.app" -ForegroundColor Cyan
        
        git checkout $currentBranch
    } else {
        Write-Host "⏭️  Solo pusheado a $currentBranch" -ForegroundColor Yellow
    }
} else {
    Write-Host "✅ Pusheado a $currentBranch" -ForegroundColor Green
}

Write-Host ""
Write-Host "✨ Listo!" -ForegroundColor Green
