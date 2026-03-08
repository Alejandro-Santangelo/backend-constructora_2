# Protocolo de Deployment - Railway

## ⚠️ REGLAS OBLIGATORIAS

### 1. Testing Local ANTES de Deploy
```bash
# Backend - SIEMPRE probar localmente primero
cd backend-constructora_2
./mvnw clean spring-boot:run

# Verificar endpoints funcionan:
# http://localhost:8080/api/obras/empresa/1?empresaId=1
# http://localhost:8080/api/profesionales?empresaId=1

# Solo si funciona OK → git add, commit, push
```

### 2. Workflow Git Estricto
```bash
# Paso 1: Trabajar en rama cacho
git checkout cacho
# ... hacer cambios ...
./mvnw spring-boot:run  # PROBAR LOCAL

# Paso 2: Solo si funciona → commit
git add -A
git commit -m "fix: descripción clara del cambio"

# Paso 3: Merge a develop para testing
git checkout develop
git merge cacho --no-ff
git push origin develop

# Paso 4: Si develop OK en Railway → merge a main
git checkout main
git merge develop --no-ff
git push origin main  # ← Railway auto-deploya desde main

# Paso 5: Volver a cacho
git checkout cacho
```

### 3. Verificación Post-Deploy
```bash
# Después de push a main, verificar en Railway:
# 1. Ir a Deployments → ver último deployment
# 2. Estado debe ser: Building → Deploying → Completed ✅
# 3. Verificar commit message coincide con tu commit
# 4. Revisar Deploy Logs que muestre "Started Application"
# 5. Probar frontend → verificar sin errores CORS
```

### 4. Si Algo Falla - Rollback Inmediato
```bash
# Identificar último commit estable (ej: 621c41b)
git log --oneline -5

# Rollback todas las ramas
git checkout cacho
git reset --hard 621c41b
git push -f origin cacho

git checkout develop  
git reset --hard 621c41b
git push -f origin develop

git checkout main
git reset --hard 621c41b
git push -f origin main

git checkout cacho
```

### 5. Railway Redeploy (solo si necesario)
```
Si Railway muestra "Crashed" pero código es correcto:
1. Railway Dashboard → Deployments
2. Click en el deployment más reciente
3. Menú "..." → Redeploy
4. Esperar completación limpia
```

## 🚫 PROHIBIDO

❌ **NUNCA** modificar código de backup (`detectRailway()` en BackupService)
❌ **NUNCA** hacer 4+ intentos de deploy si algo crashea
❌ **NUNCA** deployar backend + frontend simultáneamente
❌ **NUNCA** commitear sin probar localmente primero
❌ **NUNCA** usar `git push -f` a main si no es rollback de emergencia

## ✅ Commits Estables Registrados

| Commit | Rama | Descripción | Estado |
|--------|------|-------------|--------|
| 621c41b | main | CORS preflight fix | ⚠️ CRASHEÓ 7/3/2026 (OOM) |
| 5983378 | todas | Backup desactivado Railway | ✅ ESTABLE |
| 4f0aae8 | frontend | Encoding UTF-8 fix | ✅ ESTABLE |

## 🐛 Incidentes Registrados

### Crash 7/3/2026 17:47 GMT-3 (Deployment 7151e56f)
- **Causa**: OutOfMemoryError - Dockerfile sin límites JVM
- **Síntomas**: Crash silencioso sin exception logs, memoria ~500MB
- **Solución**: Agregar -Xmx512m -Xms256m al Dockerfile ENTRYPOINT
- **Commit fix**: [pendiente merge]
## 📊 Verificación de Estado
```bash
# Backend - verificar commit actual
cd backend-constructora_2
git log --oneline -1

# Frontend - verificar commit actual  
cd ../frontend-constructora_2
git log --oneline -1

# Verificar Railway está en sync
# Main local debe coincidir con Railway main
```

## 🆘 Troubleshooting

### CORS Errors en Producción
- Verificar `CorsConfig.java` incluye frontend Railway URL
- Verificar `TenantFilter.java` excluye OPTIONS requests
- Reiniciar deployment si es necesario

### Backend Crashed en Railway
- **NO hacer más pushes**
- Revisar Deploy Logs para encontrar error
- Si es OOM: problema de código, no de Railway
- Si es startup timeout: revisar @PostConstruct/@Configuration
- Rollback a commit estable conocido

### Frontend sin cambios visibles
- Hard refresh: Ctrl+Shift+R
- Cache del navegador puede tardar 5-10 min
- Verificar en Railway que deploy de frontend completó
- Probar en modo incógnito

## 📝 Documentar Cambios

Después de cada deploy exitoso, actualizar:
```bash
# En este archivo, sección "Commits Estables Registrados"
# Anotar commit hash, fecha, qué se cambió
```
