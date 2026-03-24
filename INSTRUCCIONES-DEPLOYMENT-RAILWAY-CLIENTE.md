# Instrucciones de Deployment en Railway - Cliente

## Pre-requisitos Completados ✅
- ✅ Base de datos PostgreSQL creada en Railway
- ✅ Schema y datos de tabla `empresas` restaurados (78 tablas, 3 empresas)
- ✅ Código sincronizado en ramas: `main`, `develop`, `cacho`

## Credenciales Base de Datos
```
Host: metro.proxy.rlwy.net
Port: 20275
User: postgres
Password: desconjpIHHFcXvjmyYDPXGJFYVkyCCf
Database: railway
```

---

## PASO 1: Desplegar Backend

### 1.1 Crear Servicio Backend en Railway
1. En tu proyecto Railway, clic en `+ New Service`
2. Seleccionar `GitHub Repo`
3. Conectar con: `https://github.com/Alejandro-Santangelo/backend-constructora_2`
4. Seleccionar rama: `main`
5. Railway detectará automáticamente que es una aplicación Spring Boot

### 1.2 Configurar Variables de Entorno del Backend
En el servicio backend, ir a `Variables` y agregar:

```bash
# Database Connection
SPRING_DATASOURCE_URL=jdbc:postgresql://metro.proxy.rlwy.net:20275/railway?sslmode=require
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=desconjpIHHFcXvjmyYDPXGJFYVkyCCf

# Spring Profile
SPRING_PROFILES_ACTIVE=railway

# JWT Configuration (mantener o cambiar según necesidad)
JWT_SECRET=tu_clave_secreta_jwt_aqui_cambiar_en_produccion
JWT_EXPIRATION=86400000

# Hikari Pool Optimization
SPRING_DATASOURCE_HIKARI_MAXIMUM_POOL_SIZE=5
SPRING_DATASOURCE_HIKARI_MINIMUM_IDLE=2
SPRING_DATASOURCE_HIKARI_CONNECTION_TIMEOUT=20000
SPRING_DATASOURCE_HIKARI_IDLE_TIMEOUT=300000

# JVM Memory Optimization
JAVA_TOOL_OPTIONS=-Xmx512m -Xms256m
```

### 1.3 Optimización de Recursos Backend
En `Settings` del servicio:
- **CPU**: Mantener default (0.5 vCPU)
- **Memory**: 512 MB
- **Healthcheck**: Configurar en `/actuator/health` (si está habilitado)

### 1.4 Verificar Deployment Backend
1. Esperar a que el build termine (2-4 minutos)
2. Railway asignará una URL pública, ejemplo: `https://backend-constructora2-production.up.railway.app`
3. **COPIAR ESTA URL** - la necesitarás para el frontend

---

## PASO 2: Desplegar Frontend

### 2.1 Crear Servicio Frontend en Railway
1. En tu proyecto Railway, clic en `+ New Service`
2. Seleccionar `GitHub Repo`
3. Conectar con: `https://github.com/Alejandro-Santangelo/frontend-constructora_2`
4. Seleccionar rama: `main`
5. Railway detectará automáticamente que es una aplicación Vite/React

### 2.2 Configurar Variables de Entorno del Frontend
En el servicio frontend, ir a `Variables` y agregar:

```bash
# API Backend URL (USAR LA URL DEL PASO 1.4)
VITE_API_URL=https://tu-backend-url-aqui.up.railway.app

# Node Environment
NODE_ENV=production
```

**IMPORTANTE**: Reemplazar `https://tu-backend-url-aqui.up.railway.app` con la URL real de tu backend desplegado.

### 2.3 Optimización de Recursos Frontend
En `Settings` del servicio:
- **CPU**: Mantener default (0.5 vCPU)
- **Memory**: 512 MB
- **Build Command**: Verificar que sea `npm run build` o `vite build`
- **Start Command**: Verificar que sea `npm run preview` o similar

### 2.4 Verificar Deployment Frontend
1. Esperar a que el build termine (2-3 minutos)
2. Railway asignará una URL pública, ejemplo: `https://frontend-constructora2-production.up.railway.app`
3. **COPIAR ESTA URL** - la necesitarás para actualizar CORS

---

## PASO 3: Actualizar Configuración CORS

### 3.1 Informar la URL del Frontend
Una vez que tengas la URL del frontend desplegado, necesitamos actualizar el backend para permitir peticiones CORS desde esa URL.

**ENVIAR A ALEJANDRO**: La URL completa del frontend desplegado en Railway.

Ejemplo: `https://frontend-constructora2-production-abc123.up.railway.app`

Yo actualizaré el archivo `CorsConfig.java` y haré push para que Railway redeploy automáticamente el backend.

---

## PASO 4: Verificación Final

### 4.1 Probar la Aplicación
1. Abrir la URL del frontend en el navegador
2. Intentar hacer login con una empresa existente
3. Verificar que carga los datos correctamente

### 4.2 Verificar Base de Datos
Ejecutar desde tu máquina local:
```powershell
$env:PGPASSWORD="desconjpIHHFcXvjmyYDPXGJFYVkyCCf"
psql -h metro.proxy.rlwy.net -p 20275 -U postgres -d railway -c "SELECT * FROM empresas;"
```

Deberías ver las 3 empresas:
1. CONSTRUCTORA CUENCA Y ASOCIADOS (ID: 5)
2. MACCINO Soluciones & servicios (ID: 8)
3. Empresa Prueba (ID: 9)

---

## PASO 5: Monitoreo de Uso de Crédito

### 5.1 Dashboard de Railway
- Ir a `Usage` en el proyecto
- Monitorear el consumo de los $5 de crédito gratuito
- Railway muestra estimación de cuántos días quedan

### 5.2 Optimizaciones Adicionales (Opcional)
Si el crédito se consume muy rápido:

1. **Reducir recursos**:
   - Backend: 256 MB RAM (mínimo para Java)
   - Frontend: 256 MB RAM

2. **Configurar Sleep Scheduling** (requiere plan Pro):
   - Railway no tiene auto-sleep en plan gratuito
   - Considera actualizar a plan Pro si necesitas esta función

3. **Optimizar queries SQL**:
   - Revisar logs de consultas lentas
   - Agregar índices si es necesario

---

## Resumen de URLs a Configurar

```
Database PostgreSQL:  metro.proxy.rlwy.net:20275
Backend (por desplegar): https://[tu-backend].up.railway.app
Frontend (por desplegar): https://[tu-frontend].up.railway.app
```

## Próximos Pasos Inmediatos

1. ✅ Crear servicio backend en Railway con variables de entorno
2. ✅ Copiar URL del backend desplegado
3. ✅ Crear servicio frontend en Railway con VITE_API_URL
4. ✅ Copiar URL del frontend desplegado
5. ⏳ Enviar URL frontend a Alejandro para actualizar CORS
6. ⏳ Verificar login y funcionalidad completa

---

## Contacto y Soporte
- Código permanece en GitHub de Alejandro (no transferir repos)
- Si hay problemas en deployment, revisar logs en Railway → servicio → Logs
- Verificar que las variables de entorno estén correctamente configuradas
