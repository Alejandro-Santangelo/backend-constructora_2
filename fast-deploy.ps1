# Fast Deploy - Deploy directo a main SIN validaciones
# Uso: .\fast-deploy.ps1 "mensaje del commit"
# ⚠️ CUIDADO: No valida nada, usa bajo tu responsabilidad

param(
    [Parameter(Mandatory=$true)]
    [string]$Message
)

Write-Host "⚡ FAST DEPLOY - SIN VALIDACIONES" -ForegroundColor Red
Write-Host ""

$currentBranch = git branch --show-current

# Commit
git add -A
git commit -m $Message

# Merge directo a main
if ($currentBranch -ne "main") {
    git checkout develop
    git merge $currentBranch --no-ff -m "Fast merge: $Message"
    git push origin develop
    
    git checkout main  
    git merge develop --no-ff -m "Fast deploy: $Message"
    git push origin main
    
    git checkout $currentBranch
} else {
    git push origin main
}

Write-Host ""
Write-Host "🚀 Deploy iniciado en Railway" -ForegroundColor Green
Write-Host "🌐 https://railway.app" -ForegroundColor Cyan
