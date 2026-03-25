# 🚀 Guía de Deployment Railway - Constructora App

> **Última actualización:** 25 de marzo de 2026  
> **Estado:** Railway GitHub sync ROTO - usar Railway CLI únicamente

---

## 📋 URLs de Producción

- **Backend:** https://backend-constructora2-production-8bc9.up.railway.app
- **Frontend:** https://zonal-curiosity-production-3041.up.railway.app
- **PostgreSQL:** metro.proxy.rlwy.net:20275/railway

---

## ⚠️ PROBLEMA CRÍTICO CONOCIDO

**Railway NO detecta commits de GitHub automáticamente**

- ❌ El botón "Redeploy" de Railway → Solo reconstruye código viejo
- ❌ "Apply changes" después de agregar variables → Usa commit obsoleto
- ❌ Reconnectar GitHub App → No funciona (probado 5+ veces)
- ✅ **SOLUCIÓN: Railway CLI** (`railway up`)

---

## 🔧 Setup Inicial (una sola vez)

### 1. Instalar Railway CLI
```powershell
# Si no está instalado:
# Descargar de: https://docs.railway.app/develop/cli
railway --version  # Verificar: v4.33.0 o superior
```

### 2. Autenticar
```powershell
railway login --browserless
# Copiar el código de emparejamiento y pegarlo en: railway.app/cli-login
# Cuenta: tntconstrucciones.app@gmail.com
```

### 3. Verificar autenticación
```powershell
railway whoami
# Debe mostrar: tntconstrucciones.app@gmail.com
```

---

## 🚀 Workflow de Deployment

### **BACKEND - Spring Boot**

```powershell
cd C:\Users\Usuario\Desktop\AppConstructoras\backend-constructora_2

# 1. PROBAR LOCALMENTE PRIMERO (OBLIGATORIO)
./mvnw clean spring-boot:run -DskipTests

# Verificar: http://localhost:8080
# Solo si funciona → continuar

# 2. Commitear cambios
git add .
git commit -m "fix: descripción clara del cambio"
git push origin main

# 3. Desplegar a Railway
railway link
# Seleccionar:
#   - Workspace: tntconstruccionesapp's Projects
#   - Project: alluring-kindness
#   - Environment: production
#   - Service: backend-constructora_2

railway up

# Tiempo estimado: 3-5 minutos
# Logs en tiempo real durante build
```

**Verificar deployment:**
```powershell
railway logs
# Buscar: "Started ConstruccionBackendApplication in X seconds"
# Buscar: "✅ CORS: Usando orígenes desde variable de entorno"
```

---

### **FRONTEND - React + Vite**

```powershell
cd C:\Users\Usuario\Desktop\AppConstructoras\frontend-constructora_2

# 1. Probar localmente
npm run dev  # http://localhost:5173

# 2. Commitear
git add .
git commit -m "feat: descripción del cambio"
git push origin main

# 3. Desplegar
railway link
# Seleccionar:
#   - Workspace: tntconstruccionesapp's Projects
#   - Project: alluring-kindness
#   - Environment: production
#   - Service: [NOMBRE_SERVICIO_FRONTEND]  # Ver en Railway dashboard

railway up

# Tiempo estimado: 2-3 minutos
```

**Verificar deployment:**
- Abrir: https://zonal-curiosity-production-3041.up.railway.app
- Probar login con PIN 3333
- Verificar DevTools Console (no debe haber errores CORS)

---

### **BASE DE DATOS - PostgreSQL**

#### Opción 1: Migraciones Flyway (RECOMENDADO)

```powershell
# 1. Crear archivo SQL en:
# backend-constructora_2/src/main/resources/db/migration/

# Formato nombre: VXX__descripcion.sql
# Ejemplo: V9__agregar_columna_estado_obra.sql

# 2. Contenido SQL:
ALTER TABLE obras ADD COLUMN estado VARCHAR(50) DEFAULT 'EN_PROGRESO';

# 3. Desplegar backend (Flyway ejecuta automático)
railway up

# Flyway ejecuta migraciones automáticamente al iniciar
```

#### Opción 2: SQL Directo (urgencias/hotfixes)

```powershell
# Conectar a Railway PostgreSQL
psql "postgresql://postgres:desconjpIHHFcXvjmyYDPXGJFYVkyCCf@metro.proxy.rlwy.net:20275/railway"

# O con variables separadas:
psql -h metro.proxy.rlwy.net -p 20275 -U postgres -d railway

# Ejecutar SQL
\i mi-script.sql

# O inline:
INSERT INTO empresas (nombre, pin) VALUES ('Nueva Empresa', '4444');
```

**Credenciales PostgreSQL:**
```
PGHOST=metro.proxy.rlwy.net
PGPORT=20275
PGDATABASE=railway
PGUSER=postgres
PGPASSWORD=desconjpIHHFcXvjmyYDPXGJFYVkyCCf
```

---

## 🔐 Variables de Entorno

### Agregar nueva variable:

1. **Railway Dashboard** → backend-constructora_2 → Variables
2. Agregar variable
3. **NO usar "Apply changes"** (usa código viejo)
4. Desplegar manualmente:
   ```powershell
   cd backend-constructora_2
   railway up
   ```

### Variables actuales (Backend):

```env
# PostgreSQL
PGHOST=postgres.railway.internal
PGPORT=5432
PGDATABASE=railway
PGUSER=postgres
PGPASSWORD=desconjpIHHFcXvjmyYDPXGJFYVkyCCf

# CORS (crítico)
CORS_ALLOWED_ORIGINS=http://localhost:5173,https://zonal-curiosity-production-3041.up.railway.app,https://frontend-constructora2-production.up.railway.app

# Spring Boot
SPRING_PROFILES_ACTIVE=prod
```

### Variables actuales (Frontend):

```env
VITE_API_URL=https://backend-constructora2-production-8bc9.up.railway.app
```

---

## 🔄 Rollback (deshacer deployment)

```powershell
# 1. Ver historial de commits
git log --oneline -5

# 2. Identificar commit estable
# Ejemplo: 621c41b

# 3. Resetear a ese commit
git reset --hard 621c41b

# 4. Redesplegar
railway up

# 5. Restaurar main (si querés conservar commits nuevos en branch)
git checkout -b fix-attempt
git checkout main
git pull
```

---

## 📊 Datos de Producción

### Usuarios activos (4 PINs):

```sql
-- PIN 1111: Usuario Gisel (CONTRATISTA, empresa 1)
-- PIN 2222: Usuario Construcciones SRL (CONTRATISTA, empresa 2)
-- PIN 3333: Super Admin TNT (SUPER_ADMIN, empresa 3) ← DUEÑO APP
-- PIN 9999: Cacho (CONTRATISTA, empresa 2)
```

### Empresas (3):

```sql
-- ID 1: Gisel
-- ID 2: Construcciones SRL
-- ID 3: TNT (empresa dueña - multi-tenant)
```

---

## 🧪 Testing Pre-Deployment

### Backend:
```powershell
# Test completo local
./mvnw clean spring-boot:run -DskipTests

# URLs a verificar:
# - http://localhost:8080/actuator/health → {"status":"UP"}
# - http://localhost:8080/api/rubros → Lista de rubros
```

### Frontend:
```powershell
npm run dev

# Verificar:
# - Login con PIN 3333
# - Navegación entre módulos
# - No errores en Console
```

### PostgreSQL:
```powershell
# Verificar conexión
psql "postgresql://postgres:desconjpIHHFcXvjmyYDPXGJFYVkyCCf@metro.proxy.rlwy.net:20275/railway" -c "SELECT COUNT(*) FROM usuarios;"

# Debe retornar: 4
```

---

## 🚨 Troubleshooting

### Problema: "No linked project found"
```powershell
railway unlink
railway link  # Volver a seleccionar proyecto
```

### Problema: "Build failed"
```powershell
# Ver logs completos
railway logs

# Verificar localmente primero
./mvnw clean install
```

### Problema: CORS errors en frontend
```powershell
# 1. Verificar variable CORS_ALLOWED_ORIGINS en Railway
# 2. Verificar logs backend:
railway logs | Select-String "CORS"

# Debe mostrar:
# "✅ CORS: Usando orígenes desde variable de entorno"
```

### Problema: 502 Bad Gateway
```powershell
# Backend no inició correctamente
railway logs

# Buscar error en logs (conexión DB, puerto, etc.)
```

### Problema: Database connection failed
```powershell
# Verificar variables PostgreSQL
railway variables

# Verificar que contengan:
# PGHOST, PGPORT, PGDATABASE, PGUSER, PGPASSWORD
```

---

## 📝 Commits estables conocidos

```
Backend:
- b86c151 ← ACTUAL (CORS env var + funcionando)
- 621c41b (CORS preflight fix)
- 5983378 (backup desactivado)

Frontend:
- ce3901c ← ACTUAL (backend URL con -8bc9)
- 4f0aae8 (encoding UTF-8 fix)
```

---

## ⏱️ Tiempos estimados

- **Backend deployment:** 3-5 minutos
- **Frontend deployment:** 2-3 minutos
- **Migración Flyway:** +30 segundos al inicio backend
- **SQL directo:** Instantáneo

---

## 🎯 Checklist Deployment

```
Backend:
□ Probar localmente (./mvnw spring-boot:run)
□ Commit descriptivo
□ Push a main
□ railway link (seleccionar backend-constructora_2)
□ railway up
□ Verificar logs: "Started ConstruccionBackendApplication"
□ Verificar logs: "✅ CORS: Usando orígenes"
□ Probar /actuator/health

Frontend:
□ Probar localmente (npm run dev)
□ Commit descriptivo
□ Push a main
□ railway link (seleccionar servicio frontend)
□ railway up
□ Verificar URL en navegador
□ Probar login PIN 3333

Base de datos:
□ Crear migración Flyway VXX__nombre.sql
□ Desplegar backend (ejecuta migración automática)
□ Verificar con psql que cambio aplicó
```

---

## 📞 Información de Soporte

**Proyecto Railway:** alluring-kindness  
**Cuenta:** tntconstrucciones.app@gmail.com  
**Tier:** Free (1GB RAM, sleep after 5h inactividad)

**Documentación Railway CLI:**  
https://docs.railway.app/develop/cli

**Logs en tiempo real:**
```powershell
railway logs --tail
```

---

## 🔥 Reglas de Oro

1. **SIEMPRE probar local antes de railway up**
2. **NUNCA usar botón "Redeploy" de Railway UI**
3. **NUNCA intentar reconectar GitHub** (no funciona)
4. **UN cambio a la vez** (backend O frontend, no ambos)
5. **Rollback inmediato si falla 2 veces**
6. **Commits descriptivos** (Railway muestra el mensaje)
7. **Variables primero, código después**

---

_Última deployment exitoso: 25 de marzo de 2026, 19:47 UTC_  
_Método usado: Railway CLI `railway up`_  
_Estado: ✅ Backend + Frontend funcionando, login OK_
