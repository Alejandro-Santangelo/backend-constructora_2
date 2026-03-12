# Plan de Deployment Railway - 12 de Marzo 2026

## 🔍 PROBLEMAS IDENTIFICADOS

### 1. **Schema de BD desactualizado en Railway**
   - **Problema**: Railway tiene `spring.jpa.hibernate.ddl-auto=validate` pero le faltan columnas nuevas
   - **Síntoma**: El backend no arranca o falla al crear/actualizar presupuestos
   - **Columnas faltantes**:
     - En `descuentos_por_rubro`: 6 columnas (honorarios_*, mayores_costos_*)
     - En `mayores_costos_por_rubro`: 3 columnas (honorarios_*)
     - En `presupuesto_no_cliente`: 12 columnas (totales por rubro, descuentos honorarios, etc.)
     - En `trabajo_extra`: 3 columnas (mayores_costos_honorarios_*)

### 2. **Funcionalidades que fallan en Railway pero funcionan en local**
   - ❌ No se pueden crear tareas leves (PRESUPUESTO_TAREA_LEVE)
   - ❌ No se crean materiales automáticamente al asignar a obra
   
   **Causa raíz**: Validación de Hibernate falla porque faltan columnas en BD

### 3. **Configuración Railway vs Local**
   - Local: `ddl-auto=none` (schema actualizado manualmente con scripts SQL)
   - Railway: `ddl-auto=validate` (exige que schema coincida 100% con entidades Java)
   - Railway tiene Flyway deshabilitado, por eso las migraciones son manuales

## 📋 PLAN DE ACCIÓN (PASO A PASO)

### FASE 1: Migrar Schema de Railway ⚠️ CRÍTICO
**Objetivo**: Agregar columnas faltantes sin borrar datos

```powershell
# Paso 1.1: Ejecutar migración de schema
.\ejecutar-migracion-railway.ps1

# Paso 1.2: Verificar que se aplicó correctamente
# (el script muestra las columnas al final)
```

**IMPORTANTE**: 
- ✅ Esta migración es **idempotente** (se puede ejecutar varias veces sin problemas)
- ✅ **NO borra datos**
- ✅ Solo **agrega columnas** que faltan

### FASE 2: Verificar Backend Local
**Objetivo**: Asegurar que funciona OK antes de deployar

```powershell
# Paso 2.1: Compilar y ejecutar localmente
./mvnw clean install -DskipTests
./mvnw spring-boot:run

# Paso 2.2: Probar funcionalidades críticas
# - Crear tarea leve: POST /presupuestos-no-cliente con tipo TAREA_LEVE
# - Asignar material nuevo a obra
# - Verificar que se guardan descuentos/honorarios por rubro
```

### FASE 3: Deploy Backend a Railway
**Objetivo**: Subir código actualizado

```bash
# Paso 3.1: Verificar rama actual (debe ser main para Railway)
git status
git branch

# Paso 3.2: Asegurar que estamos en main
git checkout main

# Paso 3.3: Commit de cambios (si hay pendientes)
git add .
git commit -m "fix: sincronizar schema BD con entidades Java - agregar columnas honorarios y mayores costos por rubro"

# Paso 3.4: Push a main (Railway auto-deploya)
git push origin main
```

**Monitoreo**:
- Railway Dashboard → Deployments
- Revisar logs de build
- Verificar que el servicio arranca sin errores
- Si falla 2 veces → STOP y rollback

### FASE 4: Probar en Producción
**Objetivo**: Verificar que funciona igual que en local

```bash
# Endpoint Railway: https://backend-constructora2-production.up.railway.app

# Test 1: Crear tarea leve
curl -X POST https://backend-constructora2-production.up.railway.app/presupuestos-no-cliente \
  -H "Content-Type: application/json" \
  -H "X-Empresa-Id: 1" \
  -d '{
    "tipo": "TAREA_LEVE",
    "trabajo_adicional_id": 1,
    "descripcion": "Test tarea leve Railway"
  }'

# Test 2: Asignar material nuevo a obra
# (desde frontend o con curl)

# Test 3: Verificar descuentos por rubro
# GET /presupuestos-no-cliente/{id} → verificar campos nuevos
```

### FASE 5: Deploy Frontend (si es necesario)
**Objetivo**: Sincronizar cambios de UI con backend

```bash
# Paso 5.1: Ir al repositorio del frontend
cd ../frontend-constructora2

# Paso 5.2: Verificar cambios pendientes
git status

# Paso 5.3: Commit y push a main
git add .
git commit -m "feat: sincronizar con cambios backend - honorarios y descuentos por rubro"
git push origin main

# Railway auto-deploya desde main
```

## 🔧 TROUBLESHOOTING

### Si Railway crashea después del deploy:

1. **Revisar logs de Railway**
   ```
   Railway Dashboard → Deployments → View Logs
   Buscar: "ERROR", "Exception", "Column", "Table"
   ```

2. **Verificar que la migración SQL se ejecutó**
   ```powershell
   # Conectarse a Railway
   psql -h caboose.proxy.rlwy.net -p 16821 -U postgres -d construccion_app_v3
   
   # Verificar columnas
   \d descuentos_por_rubro
   \d mayores_costos_por_rubro
   \d presupuesto_no_cliente
   ```

3. **Rollback si es necesario**
   ```bash
   # Volver al último commit estable
   git reset --hard <commit-estable>
   git push origin main --force
   
   # Railway re-deploya automáticamente
   ```

### Si la migración SQL falla:

1. **Error de "column already exists"**
   - ✅ Normal, significa que esa columna ya existe
   - El script sigue con las demás

2. **Error de "relation does not exist"**
   - ❌ La tabla no existe en Railway
   - Verificar que estás conectado a la BD correcta

3. **Timeout de conexión**
   - Verificar credenciales de Railway
   - Revisar firewall/red

## 📊 CHECKLIST FINAL

Antes de considerar el deployment exitoso, verificar:

- [ ] Migración SQL ejecutada sin errores
- [ ] Backend local funciona correctamente
- [ ] Backend desplegado en Railway sin crashes
- [ ] Se pueden crear tareas leves en Railway
- [ ] Se pueden agregar materiales nuevos en Railway
- [ ] Descuentos y honorarios por rubro funcionan
- [ ] Frontend actualizado (si hay cambios)
- [ ] No hay errores en logs de Railway
- [ ] Rollback plan identificado (commit hash del estado estable)

## 🎯 COMMITS ESTABLES DE REFERENCIA

```
Backend actual en Railway: <verificar en Railway Deployments>
Frontend actual en Railway: <verificar en Railway Deployments>
```

Actualizar estos hashes después de un deployment exitoso.

## 📝 NOTAS ADICIONALES

- **Tiempo estimado**: 30-45 minutos total
- **Riesgo**: Bajo (migración solo agrega columnas)
- **Horario recomendado**: En horario de baja actividad de usuarios
- **Backup**: Railway hace backups automáticos cada 24h
- **Recovery time**: < 5 minutos (rollback via git)
