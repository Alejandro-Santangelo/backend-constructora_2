# 🔍 GUÍA DE DECISIÓN - QUÉ TABLAS ELIMINAR

## 📝 INSTRUCCIONES

1. Ejecuta el script `auditoria_simple_tablas.sql` en DBeaver
2. Copia los resultados aquí abajo
3. Usa esta guía para decidir qué eliminar

---

## ✅ TABLAS CONFIRMADAS PARA ELIMINAR (Sin importar si tienen datos)

### 1. `cobros_obra` ❌ ELIMINAR
```sql
DROP TABLE IF EXISTS cobros_obra CASCADE;
```
**Motivo:** Reemplazada por `cobros_empresa`. El código ya usa `CobroEmpresaRepository`.

---

### 2. `asignaciones_cobro_obra` ❌ ELIMINAR
```sql
DROP TABLE IF EXISTS asignaciones_cobro_obra CASCADE;
```
**Motivo:** Reemplazada por `asignaciones_cobro_empresa_obra`.

---

## ⚠️ TABLAS A REVISAR (Depende de los datos)

### 3. `asignacion_profesional_dia`
- **Si tiene 0 registros:** ❌ ELIMINAR
- **Si tiene > 0 registros:** ⚡ REVISAR si se sigue usando
  - Buscar en código: `AsignacionProfesionalDia.java`
  - Si existe la entidad → MANTENER
  - Si no existe → Posible tabla huérfana → ELIMINAR

```sql
-- Solo si está vacía O no tiene entidad Java:
DROP TABLE IF EXISTS asignacion_profesional_dia CASCADE;
```

---

### 4. `cliente_empresa`
- **Si existe y tiene 0 registros:** ❌ ELIMINAR (tabla de relación vieja)
- **Si no existe:** ✅ Ya fue eliminada

```sql
DROP TABLE IF EXISTS cliente_empresa CASCADE;
```

---

## 📋 DECISIÓN POR CATEGORÍA

### **A. Tablas de Etapas Diarias** (Sistema nuevo)

Verifica estos resultados:
- `etapas_diarias`: **Registros = ?**
- `tareas_etapa_diaria`: **Registros = ?**
- `profesionales_tarea_etapa`: **Registros = ?**
- `profesional_tarea_dia`: **Registros = ?**

**Decisión:**
- Si **TODAS están vacías** (0 registros): ⚡ Sistema no implementado aún
  - **MANTENER** (para uso futuro)
  - O **ELIMINAR** si no planeas usarlo
  
- Si **alguna tiene datos**: ✅ MANTENER TODAS (sistema activo)

---

### **B. Tablas de Caja Chica**

Verifica:
- `caja_chica_movimientos`: **Registros = ?**
- `caja_chica_obra`: **Registros = ?**
- `gastos_obra_profesional`: **Registros = ?**

**Decisión:**
- Si **TODAS vacías**: ⚡ Sistema no usado
  - **MANTENER** (implementación futura)
  
- Si **alguna con datos**: ✅ MANTENER TODAS

---

### **C. Tablas de Asistencia**

Verifica:
- `asistencia_obra`: **Registros = ?**

**Decisión:**
- **Si vacía**: ⚡ ¿Usas control de asistencia?
  - SÍ → MANTENER (para uso futuro)
  - NO → ELIMINAR
  
- **Si con datos**: ✅ MANTENER

---

### **D. Tablas de Trabajos Extra**

Verifica:
- `trabajos_extra`: **Registros = ?**
- `trabajos_extra_dias`: **Registros = ?**
- `trabajos_extra_profesionales`: **Registros = ?**
- `trabajos_extra_tareas`: **Registros = ?**
- `trabajos_extra_pdf`: **Registros = ?**

**Decisión:**
- ✅ **MANTENER TODAS** (independiente de si tienen datos)
- Motivo: Sistema implementado y funcional

---

### **E. Tablas de Trabajos Adicionales**

Verifica:
- `trabajos_adicionales`: **Registros = ?**
- `trabajos_adicionales_profesionales`: **Registros = ?**

**Decisión:**
- ✅ **MANTENER TODAS**
- Nota: Son diferentes de "trabajos_extra" (dos sistemas distintos)

---

## 🎯 SCRIPT DE LIMPIEZA FINAL

Después de ejecutar la auditoría, rellena esto:

```sql
-- ============================================================================
-- SCRIPT DE LIMPIEZA - A ejecutar después de analizar resultados
-- ============================================================================

-- ⚠️ RESPALDA ANTES:
-- pg_dump -U usuario -d bd_construccion > backup_antes_limpieza_$(date +%Y%m%d).sql

-- 1. TABLAS CONFIRMADAS OBSOLETAS (eliminar siempre)
DROP TABLE IF EXISTS cobros_obra CASCADE;
DROP TABLE IF EXISTS asignaciones_cobro_obra CASCADE;

-- 2. SOLO SI ESTÁN VACÍAS Y NO TIENEN ENTIDAD JAVA:
-- DROP TABLE IF EXISTS asignacion_profesional_dia CASCADE;
-- DROP TABLE IF EXISTS cliente_empresa CASCADE;

-- 3. TABLAS DE SISTEMAS NO USADOS (solo si TODO el sistema está vacío):
-- Sistema de Asistencia (si asistencia_obra = 0)
-- DROP TABLE IF EXISTS asistencia_obra CASCADE;

-- Sistema de Etapas Diarias (solo si TODAS vacías)
-- DROP TABLE IF EXISTS etapas_diarias CASCADE;
-- DROP TABLE IF EXISTS tareas_etapa_diaria CASCADE;
-- DROP TABLE IF EXISTS profesionales_tarea_etapa CASCADE;
-- DROP TABLE IF EXISTS profesional_tarea_dia CASCADE;

-- ============================================================================
-- VERIFICACIÓN POST-LIMPIEZA
-- ============================================================================

-- Listar tablas restantes
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE'
  AND table_name NOT LIKE 'flyway%'
ORDER BY table_name;
```

---

## 📊 PLANTILLA DE REPORTE

Rellena con tus resultados:

```
RESULTADOS DE AUDITORÍA:
========================

TABLAS OBSOLETAS (eliminar siempre):
- cobros_obra: ___ registros → ELIMINAR
- asignaciones_cobro_obra: ___ registros → ELIMINAR

TABLAS SOSPECHOSAS:
- asignacion_profesional_dia: ___ registros → ¿Tiene entidad Java? __
- cliente_empresa: ___ registros → DECISIÓN: __

SISTEMAS COMPLETOS:
- Etapas Diarias: ___ registros totales → DECISIÓN: __
- Caja Chica: ___ registros totales → DECISIÓN: __
- Asistencia: ___ registros → DECISIÓN: __
```

---

## ⚡ RESUMEN RÁPIDO

### ✅ ELIMINAR SEGURO (ya confirmadas obsoletas):
1. `cobros_obra`
2. `asignaciones_cobro_obra`

### ⚠️ REVISAR (ejecuta auditoría primero):
1. `asignacion_profesional_dia`
2. `cliente_empresa`
3. Sistemas completos vacíos (etapas diarias, asistencia)

### ❌ NO ELIMINAR NUNCA:
- `cobros_empresa` ✅
- `asignaciones_cobro_empresa_obra` ✅
- `retiros_personales` ✅
- `pagos_*` (todas las de pagos) ✅
- `obras`, `profesionales`, `presupuesto_*` ✅
- `trabajos_adicionales`, `trabajos_extra` ✅
