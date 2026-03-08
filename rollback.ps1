# Rollback Rápido - Volver al último commit estable
# Uso: .\rollback.ps1 <commit-hash>
# Ejemplo: .\rollback.ps1 621c41b

param(
    [Parameter(Mandatory=$true)]
    [string]$CommitHash
)

Write-Host "🔙 ROLLBACK a commit $CommitHash" -ForegroundColor Yellow
Write-Host ""

$confirm = Read-Host "⚠️  Esto revertirá TODAS las ramas (cacho, develop, main). ¿Continuar? (s/N)"

if ($confirm -ne "s" -and $confirm -ne "S") {
    Write-Host "❌ Rollback cancelado" -ForegroundColor Red
    exit 0
}

Write-Host ""
Write-Host "⏪ Rollback en progreso..." -ForegroundColor Cyan

# Guardar rama actual
$currentBranch = git branch --show-current

# Rollback cacho
git checkout cacho
git reset --hard $CommitHash
git push -f origin cacho

# Rollback develop
git checkout develop
git reset --hard $CommitHash
git push -f origin develop

# Rollback main
git checkout main
git reset --hard $CommitHash
git push -f origin main

# Volver a rama original
git checkout $currentBranch

Write-Host ""
Write-Host "✅ Rollback completado" -ForegroundColor Green
Write-Host "🔄 Railway re-deploying automáticamente desde main" -ForegroundColor Cyan
Write-Host ""
Write-Host "📊 Commits estables conocidos:" -ForegroundColor Yellow
Write-Host "  621c41b - CORS preflight fix (actual antes de este rollback)" -ForegroundColor Gray
Write-Host "  5983378 - Backup desactivado Railway" -ForegroundColor Gray
