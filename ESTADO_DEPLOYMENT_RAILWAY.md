# Estado del Deployment en Railway - 5 de Marzo 2026

## ✅ COMPLETADO

### 1. Infraestructura Railway
- ✅ Cuenta Railway creada (usuario: Alejandro-Santangelo)
- ✅ Proyecto: "powerful-encouragement" / Entorno: "production"
- ✅ PostgreSQL deployado en Railway
- ✅ Base de datos `construccion_app_v3` creada e importada con datos completos

### 2. Base de Datos Railway PostgreSQL
**Credenciales de Conexión:**
```
Host: caboose.proxy.rlwy.net
Port: 16821
Database: construccion_app_v3
User: postgres
Password: HYOJMbsvtyqHmrszxCBiqPSIOEkDIBdi
SSL: Requerido (sslmode=require)
```

**Datos Importados:**
- 6 obras
- 8 presupuestos
- 28 clientes
- 33+ tablas con estructura completa

### 3. Configuración del Backend
- ✅ Archivo `application-prod.properties` creado con configuración de Railway
- ✅ Configuración committeada y pusheada a GitHub (rama: `cacho`)
- ✅ Archivo `nixpacks.toml` creado y corregido para Java 17

**Commits realizados:**
1. `22d28f8` - Add Railway production config
2. `26a8191` - Add Railway nixpacks config for Java 17
3. `6f767e7` - Fix nixpacks.toml for Java 17

### 4. Variables de Entorno Configuradas
- ✅ `NIXPACKS_JDK_VERSION = 17`
- ✅ `SPRING_PROFILES_ACTIVE = prod`

### 5. Repositorios GitHub
- Backend: https://github.com/Alejandro-Santangelo/backend-constructora_2
- Frontend: https://github.com/Alejandro-Santangelo/frontend-constructora_2
- **Rama activa:** `cacho` (NO main)

---

## ⚠️ PROBLEMA ACTUAL

### Error de Build en Railway
**Estado:** Build falló - JAVA_HOME no definido correctamente

**Último error:**
```
Error: JAVA_HOME is not defined correctly.
We cannot execute /usr/local/bin/java
```

**Causa:** Railway no está detectando automáticamente el último commit con la corrección del `nixpacks.toml`

---

## 🔧 ARCHIVOS CREADOS/MODIFICADOS

### 1. `src/main/resources/application-prod.properties`
```properties
# Railway Production Configuration
server.port=${PORT:8080}

# PostgreSQL Railway Database
spring.datasource.url=jdbc:postgresql://caboose.proxy.rlwy.net:16821/construccion_app_v3?sslmode=require
spring.datasource.username=postgres
spring.datasource.password=HYOJMbsvtyqHmrszxCBiqPSIOEkDIBdi
spring.datasource.driver-class-name=org.postgresql.Driver

# JPA/Hibernate
spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=none
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.format_sql=false
spring.jpa.properties.hibernate.jdbc.lob.non_contextual_creation=true

# CORS - Actualizar con URL real del frontend de Railway
app.cors.allowed-origins=https://tu-frontend-production.up.railway.app

# Logging
logging.level.root=INFO
logging.level.com.rodrigo=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=ERROR

# Encoding
spring.http.encoding.charset=UTF-8
spring.http.encoding.enabled=true
spring.http.encoding.force=true

# Jackson JSON
spring.jackson.serialization.write-dates-as-timestamps=false
spring.jackson.time-zone=America/Argentina/Buenos_Aires
```

### 2. `nixpacks.toml`
```toml
[phases.setup]
nixPkgs = ["jdk17"]

[phases.build]
cmds = ["./mvnw clean install -DskipTests"]

[start]
cmd = "./mvnw spring-boot:run"
```

---

## 📋 PRÓXIMOS PASOS (PARA MAÑANA)

### Paso 1: Forzar Redeploy en Railway
1. Entrar a Railway → proyecto "powerful-encouragement"
2. Click en servicio "backend-constructora_2"
3. Ir a pestaña "Deployments"
4. Click en los **tres puntos (⋮)** del deployment fallido
5. Seleccionar **"Redeploy"** o **"Trigger deploy"**

**Alternativa:**
- Ir a pestaña "Settings"
- Buscar sección "Trigger Deployment" o similar
- Forzar deployment desde el último commit de la rama `cacho`

### Paso 2: Verificar Build Exitoso
Una vez que inicie el redeploy:
1. Monitorear logs en tiempo real
2. Verificar que use Java 17 correctamente
3. Confirmar que Maven compile sin errores
4. Esperar a que Spring Boot inicie

**Tiempo estimado:** 5-10 minutos

### Paso 3: Obtener URL del Backend
Cuando el deployment sea exitoso:
1. Railway asignará una URL pública (ej: `https://backend-constructora-2-production.up.railway.app`)
2. Copiar esa URL
3. Probar acceso: `https://[URL]/actuator/health` (si está habilitado)

### Paso 4: Actualizar CORS
1. Editar `application-prod.properties`
2. Reemplazar línea:
   ```properties
   app.cors.allowed-origins=https://tu-frontend-production.up.railway.app
   ```
   Con la URL real del frontend (cuando lo deploys)
3. Commit y push a rama `cacho`
4. Railway auto-redeployará

### Paso 5: Deploy Frontend
1. En Railway, click "+ Add" → "GitHub Repo"
2. Seleccionar `frontend-constructora_2`
3. **IMPORTANTE:** Cambiar rama de "main" a **"cacho"**
4. Agregar variable de entorno:
   - `VITE_API_URL = [URL del backend de Railway]`
5. Railway auto-detectará Vite/React y lo deployará

### Paso 6: Pruebas Finales
1. Abrir URL del frontend
2. Probar login
3. Verificar que las APIs funcionen
4. Confirmar que los datos se muestren correctamente

---

## 🐛 PROBLEMAS CONOCIDOS Y SOLUCIONES

### Problema 1: Railway no detecta cambios automáticamente
**Solución:** Forzar redeploy manualmente desde la UI

### Problema 2: Error JAVA_HOME
**Causa:** `nixpacks.toml` tenía sintaxis incorrecta (`["...", "jdk17"]`)
**Solución:** Corregido a `["jdk17"]` en commit `6f767e7`

### Problema 3: Variables en español
**Causa:** Railway tradujo automáticamente los nombres de variables
**Solución:** Creadas manualmente con nombres en inglés exactos

### Problema 4: Rama incorrecta
**Precaución:** Todo el código está en rama `cacho`, NO en `main`
**Solución:** Siempre verificar que Railway use rama `cacho`

---

## 📝 FLUJO DE TRABAJO RECOMENDADO (Git + Railway)

### Desarrollo diario:

**1. Trabajar en rama `cacho` (o crear features):**
```powershell
git checkout cacho
# ... hacer cambios, commits normales ...
git add .
git commit -m "Nueva funcionalidad X"
git push origin cacho  # ← NO despliega a Railway
```

**2. Cuando quieras desplegar a producción:**
```powershell
# Integrar en develop
git checkout develop
git pull origin develop
git merge cacho
git push origin develop

# Promover a producción (main)
git checkout main
git pull origin main
git merge develop
git push origin main  # ← ESTO despliega a Railway automáticamente

# Volver a desarrollo
git checkout cacho
```

### Comandos rápidos:

**Deploy rápido (si cacho está listo para producción):**
```powershell
git checkout develop; git merge cacho; git push origin develop; git checkout main; git merge develop; git push origin main; git checkout cacho
```

**Rollback (si el deploy falla):**
```powershell
git checkout main
git reset --hard HEAD~1  # Volver al commit anterior
git push origin main --force  # Railway redeployará versión anterior
```

---

## 📝 NOTAS IMPORTANTES

1. **Costo estimado:** $5-10 USD/mes según uso
2. **Trial:** 30 días o $5.00 de crédito gratuito
3. **SSL:** Railway provee HTTPS automático
4. **Dominio custom:** Se puede configurar después
5. **Logs:** Accesibles en tiempo real desde Railway UI
6. **Escalado:** Railway escala automáticamente según demanda

---

## 🔗 RECURSOS ÚTILES

- Railway Dashboard: https://railway.com/dashboard
- Documentación Railway: https://docs.railway.app
- Nixpacks Docs: https://nixpacks.com/docs

---

## ✍️ COMANDOS ÚTILES

### Ver logs de Railway (desde terminal local - si instalás Railway CLI):
```bash
railway login
railway link
railway logs
```

### Conectar a PostgreSQL de Railway:
```bash
psql -h caboose.proxy.rlwy.net -p 16821 -U postgres -d construccion_app_v3
# Password: HYOJMbsvtyqHmrszxCBiqPSIOEkDIBdi
```

### Verificar status del servicio:
```bash
curl https://[tu-backend-url]/actuator/health
```

---

---

## ✅ BD RAILWAY ACTUALIZADA (6 de Marzo 17:50hs)

### Sincronización completada:
- ✅ Backup local exportado: `backup_local_20260306_175042.dump` (19.4 MB)
- ✅ BD Railway limpiada y restaurada con datos locales
- ✅ **6 obras** sincronizadas
- ✅ **Nuevas columnas validadas**:
  - `tipo_origen` ✓
  - `tipo_presupuesto` ✓
  - `honorario_jornales_obra` ✓
  - `descuento_materiales_obra` ✓
  - (+ 20 columnas más de honorarios/descuentos)

### Estados actualizados:
- **Obras:** APROBADO, TERMINADO
- **Presupuestos:** APROBADO, BORRADOR, TERMINADO

### Script de sincronización:
Archivo: [actualizar-bd-railway.ps1](actualizar-bd-railway.ps1)

**Futuras sincronizaciones:**
```powershell
.\actualizar-bd-railway.ps1
```

---

**Última actualización:** 6 de Marzo 2026, 18:00hs
**Estado general:** ⚠️ Cambiar configuración Railway a rama main | ✅ Database SINCRONIZADA | ✅ Ramas Git sincronizadas

---

## ✅ RAMAS GIT SINCRONIZADAS (6 de Marzo 18:00hs)

### Merges completados:
- ✅ `cacho` → `develop` (commit 05a59bf)
- ✅ `develop` → `main` (commit 05a59bf) 
- ✅ 65 commits integrados en main
- ✅ Todas las ramas en GitHub actualizadas

### Estructura de ramas establecida:
```
cacho (desarrollo activo) 
  ↓
develop (integración)
  ↓  
main (PRODUCCIÓN - Railway)
```

---

## ⚠️ ACCIÓN REQUERIDA: Cambiar Railway a rama MAIN

**IMPORTANTE:** Railway todavía está configurado para desplegar desde `cacho`.  
Necesitas cambiar la configuración en Railway Dashboard.

### Pasos en Railway:

1. **Ir a Railway Dashboard:** https://railway.com/dashboard
2. **Navegar:** proyecto "powerful-encouragement" → servicio "backend-constructora_2"
3. **Settings** → **Source** 
4. **Cambiar rama:**
   - De: `cacho` 
   - A: `main` ✓
5. **Guardar cambios**

Esto forzará un **redeploy automático desde main** con:
- ✅ Código estable (todos los commits integrados)
- ✅ Configuración nixpacks.toml corregida
- ✅ application-prod.properties configurado

**Tiempo de deploy:** 5-10 minutos

---

## 🔄 PRÓXIMO PASO CRÍTICO: Redeploy Backend
