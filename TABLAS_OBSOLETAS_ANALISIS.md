# 🗑️ ANÁLISIS DE TABLAS OBSOLETAS - BASE DE DATOS

## ⚠️ PROBLEMA IDENTIFICADO

Tu base de datos tiene **tablas duplicadas y obsoletas** de implementaciones anteriores.

---

## 📊 TABLAS ACTUALES EN USO (✅ Mantener)

### **Sistema de Cobros**
- ✅ `cobros_empresa` - **TABLA ACTIVA**
  - Almacena los cobros (ej: $60M que viste)
  - Tiene entidad: `CobroEmpresa.java`
  - Repositorio: `CobroEmpresaRepository.java`

- ✅ `asignaciones_cobro_empresa_obra` - **TABLA ACTIVA**
  - Asigna cobros a obras específicas
  - Entidad: `AsignacionCobroEmpresaObra.java`

### **Sistema de Pagos**
- ✅ `pagos_profesional_obra` - Pagos a profesionales
- ✅ `pagos_material_obra` - Pagos de materiales (PagoConsolidado)
- ✅ `pagos_gastos_generales_obra` - Pagos de gastos generales
- ✅ `pagos_trabajos_extra_obra` - Pagos de trabajos extra

### **Sistema de Retiros Personales**
- ✅ `retiros_personales` - **TABLA ACTIVA**

### **Sistema de Obras y Profesionales**
- ✅ `obras`
- ✅ `asignaciones_profesional_obra` - (ProfesionalObra)
- ✅ `profesionales`
- ✅ `jornales`

### **Sistema de Presupuestos**
- ✅ `presupuesto_no_cliente`
- ✅ `presupuesto_material`
- ✅ `presupuesto_gasto_general`
- ✅ `presupuesto_pdf`
- ✅ `presupuesto_costo_inicial`
- ✅ `presupuestos_auditoria`

### **Sistema de Trabajos Extra/Adicionales**
- ✅ `trabajos_adicionales`
- ✅ `trabajos_adicionales_profesionales`
- ✅ `trabajos_extra`
- ✅ `trabajos_extra_dias`
- ✅ `trabajos_extra_profesionales`
- ✅ `trabajos_extra_tareas`
- ✅ `trabajos_extra_pdf`

### **Sistema de Calculadoras**
- ✅ `items_calculadora_presupuesto`
- ✅ `profesional_calculadora`
- ✅ `material_calculadora`
- ✅ `jornal_calculadora`
- ✅ `trabajos_extra_items_calculadora`
- ✅ `trabajos_extra_profesional_calculadora`
- ✅ `trabajos_extra_material_calculadora`
- ✅ `trabajos_extra_jornal_calculadora`
- ✅ `trabajos_extra_gasto_general`

### **Sistema de Etapas Diarias**
- ✅ `etapas_diarias`
- ✅ `tareas_etapa_diaria`
- ✅ `profesionales_tarea_etapa`
- ✅ `profesional_tarea_dia`

### **Sistema Core**
- ✅ `empresas`
- ✅ `clientes`
- ✅ `usuarios`
- ✅ `materiales`
- ✅ `gastos_generales`
- ✅ `proveedor`
- ✅ `honorarios`
- ✅ `costos`

### **Sistema de Stock**
- ✅ `stock_material`
- ✅ `stock_gastos_generales`
- ✅ `movimiento_material`

### **Sistema de Caja Chica**
- ✅ `caja_chica_obra`
- ✅ `caja_chica_movimientos`
- ✅ `gastos_obra_profesional`

### **Sistema de Entidades Financieras**
- ✅ `entidades_financieras`
- ✅ `cobros_entidad`

### **Otras Activas**
- ✅ `asistencia_obra`
- ✅ `asignaciones_material_obra`
- ✅ `asignaciones_otro_costo_obra`
- ✅ `pago_adelantos_aplicados`
- ✅ `pedido_pago`

---

## ⛔ TABLAS OBSOLETAS (❌ Eliminar)

### **1. Sistema de Cobros VIEJO**
```sql
-- ❌ OBSOLETA - Reemplazada por cobros_empresa
DROP TABLE IF EXISTS cobros_obra CASCADE;

-- ❌ OBSOLETA - Reemplazada por asignaciones_cobro_empresa_obra  
DROP TABLE IF EXISTS asignaciones_cobro_obra CASCADE;
```

**Motivo:** El sistema original usaba `cobros_obra` vinculados directamente a obras. 
El nuevo sistema usa `cobros_empresa` que permite asignar cobros a múltiples obras.

---

### **2. Tablas Duplicadas de Asignaciones**
```sql
-- ❌ DUPLICADA - Ya existe como ProfesionalObra
-- Ambas apuntan a la misma tabla: asignaciones_profesional_obra
-- Verifica si existen dos entidades para la misma tabla
```

**Revisar:** Hay dos entidades Java que pueden apuntar a la misma tabla:
- `ProfesionalObra.java` → `asignaciones_profesional_obra`
- `AsignacionProfesionalObra.java` → `asignaciones_profesional_obra`

**Acción:** Mantener **solo una** y eliminar la entidad duplicada.

---

### **3. Tablas de Sistema Viejo de Asignaciones**
```sql
-- ❌ Si existe y ya no se usa
DROP TABLE IF EXISTS asignacion_profesional_dia CASCADE;
```

**Verificar:** Si esta tabla aún se usa o fue reemplazada por el nuevo sistema de etapas diarias.

---

### **4. Tablas de Frontend Antiguo (si existen)**
```sql
-- Verifica si existen estas tablas que podrían ser del sistema viejo:
DROP TABLE IF EXISTS cliente_empresa CASCADE; -- Reemplazada por relación en clientes
```

---

## 🔍 CÓMO IDENTIFICAR MÁS TABLAS OBSOLETAS

### **Paso 1: Listar todas las tablas de la BD**
```sql
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public' 
  AND table_type = 'BASE TABLE'
ORDER BY table_name;
```

### **Paso 2: Comparar con entidades Java actuales**
Ejecuta esta búsqueda en el código:
```bash
grep -r "@Table(name =" src/main/java/
```

### **Paso 3: Buscar tablas sin entidad**
Las tablas que aparezcan en la BD pero NO tengan una entidad `@Table(name = "tabla")` son candidatas a eliminación.

---

## 📝 SCRIPT DE LIMPIEZA SUGERIDO

```sql
-- ⚠️ RESPALDA LA BD ANTES DE EJECUTAR
-- pg_dump -U usuario -d nombre_bd > backup_antes_limpieza.sql

-- 1. ELIMINAR TABLA OBSOLETA: cobros_obra
DROP TABLE IF EXISTS cobros_obra CASCADE;

-- 2. ELIMINAR TABLA OBSOLETA: asignaciones_cobro_obra (vieja)
DROP TABLE IF EXISTS asignaciones_cobro_obra CASCADE;

-- 3. VERIFICAR Y ELIMINAR (si no se usan):
-- DROP TABLE IF EXISTS asignacion_profesional_dia CASCADE;
-- DROP TABLE IF EXISTS cliente_empresa CASCADE;

-- 4. LIMPIAR MIGRACIONES FLYWAY OBSOLETAS (opcional)
-- DELETE FROM flyway_schema_history 
-- WHERE script LIKE 'V2025%' 
--   AND version < '20251115';  -- Ajusta según tu caso
```

---

## ⚙️ RECOMENDACIONES

### **1. ANTES de eliminar tablas:**
```sql
-- Verifica si tienen datos
SELECT 'cobros_obra' as tabla, COUNT(*) as registros FROM cobros_obra
UNION ALL
SELECT 'asignaciones_cobro_obra', COUNT(*) FROM asignaciones_cobro_obra;
```

### **2. Respalda todo:**
```bash
pg_dump -U tu_usuario -d construccion_db > backup_completo_$(date +%Y%m%d).sql
```

### **3. Elimina gradualmente:**
- Primero las tablas vacías
- Luego las que tienen pocos registros
- Verifica que el backend siga funcionando después de cada eliminación

### **4. Limpiar carpeta migration_old:**
La carpeta `migration_old/` contiene **70+ migraciones obsoletas** que ya no se usan.

**Acción recomendada:**
- ✅ Mantener solo `migration/` (las activas)
- ❌ Eliminar `migration_old/` (son históricas, ya aplicadas)
- ✅ Si necesitas referencia, muévelas a un archivo ZIP fuera del proyecto

---

## 🎯 RESULTADO ESPERADO

Después de la limpieza tendrás:
- ✅ ~40-50 tablas activas (las necesarias)
- ❌ 0 tablas duplicadas
- ❌ 0 tablas obsoletas
- ✅ Base de datos optimizada y clara

---

## 📋 TABLAS ESPECÍFICAS A REVISAR (consulta manual)

Ejecuta esto en tu BD para ver todas las tablas:
```sql
SELECT 
    t.table_name,
    pg_size_pretty(pg_total_relation_size(quote_ident(t.table_name))) as tamaño,
    (SELECT COUNT(*) FROM information_schema.columns c 
     WHERE c.table_name = t.table_name) as num_columnas
FROM information_schema.tables t
WHERE t.table_schema = 'public' 
  AND t.table_type = 'BASE TABLE'
ORDER BY pg_total_relation_size(quote_ident(t.table_name)) DESC;
```

Esto te mostrará:
- Todas las tablas
- Su tamaño en disco
- Número de columnas

Las tablas grandes con pocas columnas o vacías son candidatas a revisión.

---

## ⚠️ IMPORTANTE

🔴 **NO ELIMINES** sin verificar primero:
1. Que la tabla no tenga datos importantes
2. Que ningún código Java la referencie
3. Que tengas un backup completo

🟢 **SÍ ELIMINA** si:
1. No tiene entidad Java asociada
2. Está vacía o tiene datos de prueba
3. Está duplicada con otra tabla activa
