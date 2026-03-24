# 🚀 Guía Migración a Railway del Cliente

**Objetivo:** Migrar backend, frontend y BD PostgreSQL a cuenta Railway del cliente manteniendo código en tu GitHub.

**Tiempo estimado:** 1-2 horas  
**Beneficio:** Cliente usa su trial gratuito ($5)

---

## 📋 CHECKLIST PRE-MIGRACIÓN

- [ ] Cliente tiene cuenta Railway creada
- [ ] Email de la cuenta Railway del cliente
- [ ] Repos GitHub listos (cliente necesitará acceso read-only)
- [ ] Backup actual de BD descargado

---

## 🔧 PASO 1: BACKUP BASE DE DATOS ACTUAL

### 1.1 Backup con pg_dump (Recomendado)

```powershell
# Configurar variables de entorno para la BD actual
$env:PGPASSWORD = "HYOJMbsvtyqHmrszxCBiqPSIOEkDIBdi"
$BACKUP_FILE = "backup-railway-produccion-$(Get-Date -Format 'yyyy-MM-dd-HHmm').sql"

# Hacer dump completo
pg_dump -h caboose.proxy.rlwy.net `
        -p 16821 `
        -U postgres `
        -d construccion_app_v3 `
        --no-owner `
        --no-acl `
        -F p `
        -f $BACKUP_FILE

Write-Host "✅ Backup guardado en: $BACKUP_FILE"
```

### 1.2 Verificar Backup

```powershell
# Ver tamaño del archivo
Get-Item $BACKUP_FILE | Select-Object Name, Length

# Ver primeras líneas
Get-Content $BACKUP_FILE -Head 20
```

---

## 🏗️ PASO 2: CLIENTE CREA BD POSTGRESQL EN RAILWAY

**El cliente hace esto:**

1. **Login en Railway:** https://railway.app
2. **New Project** → **Provision PostgreSQL**
3. **Copiar credenciales:** Settings → Connect → Connection URL
   ```
   Formato: postgresql://postgres:password@host:port/dbname
   ```
4. **Enviarte las credenciales** (de forma segura)

**Variables que necesitas del cliente:**
- `POSTGRES_HOST`
- `POSTGRES_PORT`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_DB`

---

## 📥 PASO 3: RESTAURAR DATOS EN BD DEL CLIENTE

```powershell
# Usar credenciales del cliente
$env:PGPASSWORD = "CLIENTE_PASSWORD_AQUI"

# Restaurar backup
psql -h CLIENTE_HOST `
     -p CLIENTE_PORT `
     -U CLIENTE_USER `
     -d CLIENTE_DATABASE `
     -f $BACKUP_FILE

Write-Host "✅ Datos restaurados en BD del cliente"
```

**Verificar restauración:**
```powershell
# Contar tablas
psql -h CLIENTE_HOST -p CLIENTE_PORT -U CLIENTE_USER -d CLIENTE_DATABASE `
     -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';"

# Ver algunas tablas
psql -h CLIENTE_HOST -p CLIENTE_PORT -U CLIENTE_USER -d CLIENTE_DATABASE `
     -c "\dt"
```

---

## 🔗 PASO 4: DAR ACCESO A TUS REPOS EN GITHUB

### Opción A: GitHub Colaborador Read-Only (Recomendado)

**Para cada repo (backend y frontend):**

1. **Tu GitHub** → Repo → **Settings** → **Collaborators**
2. **Add people** → Email del cliente
3. **Change role** → **Read** (solo lectura)

Cliente acepta invitación en su email.

### Opción B: Railway se conecta directamente

El cliente puede conectar su Railway con **tu GitHub** si le das permiso:

1. Cliente en Railway → **Settings** → **Connect GitHub**
2. Autoriza acceso OAuth
3. Selecciona **solo los repos** que le permites

---

## 🚀 PASO 5: CLIENTE DEPLOYA BACKEND

**El cliente hace esto en su Railway:**

1. **New Service** → **GitHub Repo** → Selecciona tu repo backend
2. **Settings** → **Environment Variables** → Agregar:

```env
# Spring Profile
SPRING_PROFILES_ACTIVE=railway

# Base de datos (usar las de SU PostgreSQL en Railway)
SPRING_DATASOURCE_URL=jdbc:postgresql://[SU_HOST]:[SU_PORT]/[SU_DB]?sslmode=require&connectTimeout=10&socketTimeout=30
SPRING_DATASOURCE_USERNAME=[SU_USER_POSTGRES]
SPRING_DATASOURCE_PASSWORD=[SU_PASSWORD_POSTGRES]

# Puerto (Railway lo asigna automáticamente)
PORT=8080

# Multi-tenancy
APP_TENANT_HEADER_NAME=X-Tenant-ID
APP_TENANT_DEFAULT_SCHEMA=public
APP_HIBERNATE_FILTER_INTERCEPTOR_ENABLED=true

# JWT (mantener los mismos valores)
JWT_SECRET=[TU_JWT_SECRET_ACTUAL]
JWT_EXPIRATION=[TU_JWT_EXPIRATION_ACTUAL]
```

3. **Deploy** → Esperar build
4. **Copiar URL pública** del backend (ej: `https://backend-xyz.up.railway.app`)

---

## 🎨 PASO 6: CLIENTE DEPLOYA FRONTEND

**El cliente hace esto:**

1. **New Service** → **GitHub Repo** → Selecciona tu repo frontend
2. **Settings** → **Environment Variables** → Agregar:

```env
# URL del backend que deployó en el paso anterior
VITE_API_URL=https://backend-xyz.up.railway.app/api
VITE_API_BASE_URL=https://backend-xyz.up.railway.app
```

3. **Deploy** → Esperar build
4. **Copiar URL pública** del frontend (ej: `https://frontend-abc.up.railway.app`)

---

## 🔧 PASO 7: ACTUALIZAR CORS EN BACKEND

**TÚ haces esto** (porque el código sigue siendo tuyo):

1. Editar [CorsConfig.java](src/main/java/com/rodrigo/construccion/config/CorsConfig.java)
2. Agregar la **nueva URL del frontend del cliente**:

```java
config.setAllowedOrigins(Arrays.asList(
    "http://localhost:3000",
    "http://localhost:5173",
    "https://frontend-constructora2-production.up.railway.app", // Tu viejo
    "https://frontend-abc.up.railway.app" // ← NUEVO del cliente
));
```

3. Commit y push a `main`:
```bash
git add src/main/java/com/rodrigo/construccion/config/CorsConfig.java
git commit -m "feat: agregar CORS para frontend Railway del cliente"
git push origin main
```

4. **Railway del cliente auto-deploya** desde tu repo (porque está conectado)

---

## ✅ PASO 8: VERIFICACIÓN FINAL

### 8.1 Verificar Backend

```powershell
# Health check
Invoke-WebRequest -Uri "https://backend-xyz.up.railway.app/actuator/health"
```

**Debe responder:** `{"status":"UP"}`

### 8.2 Verificar Frontend

1. Abrir: `https://frontend-abc.up.railway.app`
2. Intentar login
3. Verificar que carga datos

### 8.3 Verificar BD

```powershell
# Contar registros en tabla crítica
psql -h CLIENTE_HOST -p CLIENTE_PORT -U CLIENTE_USER -d CLIENTE_DATABASE `
     -c "SELECT COUNT(*) FROM empresas;"
```

---

## 🎯 PASO 9: LIMPIAR (OPCIONAL)

### Una vez que TODO funciona en Railway del cliente:

1. **Pausar tus servicios en tu Railway** (para no seguir gastando)
   - Tu Railway → Project → Settings → Pause Project

2. **Mantener tu BD activa** algunos días más por seguridad

3. **Eliminar credenciales hardcodeadas:**
   - Editar `application-railway.properties`
   - Usar variables de entorno en lugar de valores hardcodeados

```properties
# ANTES (inseguro):
spring.datasource.url=jdbc:postgresql://caboose.proxy.rlwy.net:16821/construccion_app_v3?sslmode=require
spring.datasource.username=postgres
spring.datasource.password=HYOJMbsvtyqHmrszxCBiqPSIOEkDIBdi

# DESPUÉS (seguro):
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}
```

---

## 🆘 TROUBLESHOOTING

### Error: "Connection refused" en BD

**Causa:** Credenciales incorrectas o IP bloqueada

**Solución:**
- Verificar credenciales en Railway del cliente
- Railway → PostgreSQL → Settings → Connect
- Copiar URL completa y usarla

### Error: "CORS policy blocked"

**Causa:** URL del frontend no está en CorsConfig.java

**Solución:**
- Agregar URL en CorsConfig.java (paso 7)
- Commit y push

### Error: "Application failed to start"

**Causa:** Variables de entorno faltantes

**Solución:**
```bash
# Ver logs en Railway:
Railway → Service → Deployments → View Logs

# Buscar: "Required property ... not set"
```

### Backend crashea con OOM

**Causa:** Memoria insuficiente

**Solución:** Cliente aumenta memoria en Railway
- Railway → Service → Settings → Memory → 2GB

---

## 📊 COSTOS ESTIMADOS RAILWAY (Cliente)

**Plan Hobby (Gratuito):**
- $5 USD de crédito mensual
- 500 horas de ejecución/mes
- **Suficiente para:**
  - Backend (24/7) + Frontend estático + BD pequeña

**Después del trial:**
- ~$10-15 USD/mes (uso típico)
- Escalable según necesidad

---

## 📝 RESUMEN CONFIGURACIÓN

### Backend:
- **Repo:** Tu GitHub (propiedad tuya)
- **Deploy:** Railway cliente (auto desde tu repo)
- **Variables:** BD del cliente
- **URL:** Asignada por Railway cliente

### Frontend:
- **Repo:** Tu GitHub (propiedad tuya)
- **Deploy:** Railway cliente (auto desde tu repo)
- **Variables:** URL backend del cliente
- **URL:** Asignada por Railway cliente

### Base de Datos:
- **Datos:** Migrados de tu Railway
- **Hosting:** PostgreSQL Railway cliente
- **Propiedad:** Cliente

### Control:
- **Código:** TÚ (commits, branches, releases)
- **Infraestructura:** CLIENTE (deploys, variables, facturación)
- **Datos:** CLIENTE (ownership, backups en su cuenta)

---

## ✅ CHECKLIST FINAL

- [ ] Backup BD descargado y verificado
- [ ] BD PostgreSQL creada en Railway cliente
- [ ] Datos restaurados en nueva BD
- [ ] Backend deployado en Railway cliente
- [ ] Frontend deployado en Railway cliente
- [ ] CORS actualizado con nueva URL
- [ ] Health check backend OK
- [ ] Login y datos funcionando en frontend
- [ ] Cliente tiene acceso a Railway dashboard
- [ ] Tus servicios pausados (opcional)
- [ ] Documentación entregada al cliente

---

## 🎉 ¡MIGRACIÓN COMPLETADA!

**Resultado:**
- ✅ Código sigue en tu GitHub
- ✅ Cliente controla infraestructura y facturación
- ✅ Cliente aprovecha su trial de Railway
- ✅ Tú mantienes propiedad intelectual del código
- ✅ Deployments automáticos desde tus commits

**Próximos pasos:**
- Monitorear métricas en Railway del cliente
- Configurar alertas de errores (opcional)
- Documentar proceso de deploy para futuros cambios
