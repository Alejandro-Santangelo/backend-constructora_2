# TABLAS QUE INTERACTÚAN CON PRESUPUESTO_NO_CLIENTE

**Fecha:** 11 de marzo de 2026  
**Sistema:** Backend Constructora v3

---

## 📋 RESUMEN EJECUTIVO

Cuando creás un **presupuesto_no_cliente** (cualquiera de las 4 posibilidades) y configurás:
- **Honorarios**
- **Mayores Costos** 
- **Descuentos por Rubros**

Estas son las tablas que interactúan:

---

## 🔗 RELACIONES DE TABLAS

### 1️⃣ TABLA PRINCIPAL: `presupuesto_no_cliente`

**Columnas internas para configuración:**

#### Honorarios (columnas dentro de presupuesto_no_cliente):
- `honorarios_configuracion_presupuesto_activo`
- `honorarios_configuracion_presupuesto_valor`
- `honorarios_configuracion_presupuesto_tipo` ('porcentaje' o 'fijo')
- `honorarios_profesionales_activo`
- `honorarios_profesionales_valor`
- `honorarios_profesionales_tipo`
- `honorarios_materiales_activo`
- `honorarios_materiales_valor`
- `honorarios_materiales_tipo`
- `honorarios_jornales_activo`
- `honorarios_jornales_valor`
- `honorarios_jornales_tipo`
- `honorarios_otros_costos_activo`
- `honorarios_otros_costos_valor`
- `honorarios_otros_costos_tipo`
- `honorarios_aplicar_a_todos` (boolean)
- `honorarios_valor_general`
- `honorarios_tipo_general`
- `total_honorarios_calculado`

#### Mayores Costos (columnas dentro de presupuesto_no_cliente):
- `mayores_costos_configuracion_presupuesto_activo`
- `mayores_costos_configuracion_presupuesto_valor`
- `mayores_costos_configuracion_presupuesto_tipo`
- `mayores_costos_profesionales_activo`
- `mayores_costos_profesionales_valor`
- `mayores_costos_profesionales_tipo`
- `mayores_costos_materiales_activo`
- `mayores_costos_materiales_valor`
- `mayores_costos_materiales_tipo`
- `mayores_costos_jornales_activo`
- `mayores_costos_jornales_valor`
- `mayores_costos_jornales_tipo`
- `mayores_costos_otros_costos_activo`
- `mayores_costos_otros_costos_valor`
- `mayores_costos_otros_costos_tipo`
- `mayores_costos_honorarios_activo`
- `mayores_costos_honorarios_valor`
- `mayores_costos_honorarios_tipo`
- `mayores_costos_valor_general`
- `mayores_costos_tipo_general`
- `mayores_costos_aplicar_valor_general` (boolean)
- `mayores_costos_general_importado` (boolean)

#### Descuentos por Rubro (columnas agregadas recientemente):
- `total_descuentos_por_rubro` (DECIMAL(15,2)) - Total calculado de descuentos
- **Nota:** Los descuentos se configuran en tabla relacional (ver más abajo)

#### Otros Totales:
- `total_general`
- `total_presupuesto`
- `total_presupuesto_con_honorarios`
- `total_materiales`
- `total_profesionales`

---

### 2️⃣ TABLA RELACIONAL: `items_calculadora_presupuesto`

**Relación:** `presupuesto_no_cliente_id` → `presupuesto_no_cliente.id`

**Qué contiene:**
- Los **ítems/rubros** del presupuesto
- Cada ítem tiene un `tipo_profesional` (Albañilería, Pintura, Herrería, etc.)
- Totales por ítem: `total`, `subtotal_profesionales`, `subtotal_materiales`, `subtotal_gastos_generales`

**Foreign Key:**
```sql
CONSTRAINT fk_items_presupuesto 
    FOREIGN KEY (presupuesto_no_cliente_id) 
    REFERENCES presupuesto_no_cliente(id) 
    ON DELETE CASCADE
```

**Ejemplo:**
```
presupuesto_no_cliente (id=108)
  ├─ item_1 (tipo_profesional='Albañilería', total=50000)
  ├─ item_2 (tipo_profesional='Pintura', total=30000)
  └─ item_3 (tipo_profesional='Herrería', total=20000)
```

---

### 3️⃣ TABLA RELACIONAL: `honorarios_por_rubro` ⚠️ (Opcional - Si existe)

**Estado:** Esta tabla existe **solo si ejecutaste** `crear_tablas_mayores_costos_descuentos_por_rubro.sql`

**Relación:** `presupuesto_no_cliente_id` → `presupuesto_no_cliente.id`

**Qué contiene:**
- Configuración de **honorarios específicos por cada rubro**
- Permite sobrescribir los honorarios generales del presupuesto
- Un registro por cada rubro (Albañilería, Pintura, etc.)

**Estructura:**
```sql
CREATE TABLE honorarios_por_rubro (
    id BIGSERIAL PRIMARY KEY,
    presupuesto_no_cliente_id BIGINT REFERENCES presupuesto_no_cliente(id) ON DELETE CASCADE,
    nombre_rubro VARCHAR(255), -- Debe coincidir con items_calculadora.tipo_profesional
    
    -- Configuración general
    activo BOOLEAN,
    tipo VARCHAR(50), -- 'porcentaje' o 'fijo'
    valor NUMERIC(15,2),
    
    -- Configuración específica por concepto
    profesionales_activo BOOLEAN,
    profesionales_tipo VARCHAR(50),
    profesionales_valor NUMERIC(15,2),
    
    materiales_activo BOOLEAN,
    materiales_tipo VARCHAR(50),
    materiales_valor NUMERIC(15,2),
    
    otros_costos_activo BOOLEAN,
    otros_costos_tipo VARCHAR(50),
    otros_costos_valor NUMERIC(15,2),
    
    UNIQUE (presupuesto_no_cliente_id, nombre_rubro)
);
```

**Ejemplo:**
```
presupuesto_no_cliente (id=108)
  ├─ honorarios_por_rubro (nombre_rubro='Albañilería', profesionales_valor=15%)
  ├─ honorarios_por_rubro (nombre_rubro='Pintura', profesionales_valor=10%)
  └─ honorarios_por_rubro (nombre_rubro='Herrería', profesionales_valor=12%)
```

---

### 4️⃣ TABLA RELACIONAL: `mayores_costos_por_rubro` ⚠️ (Opcional - Si existe)

**Estado:** Esta tabla existe **solo si ejecutaste** `crear_tablas_mayores_costos_descuentos_por_rubro.sql`

**Relación:** `presupuesto_no_cliente_id` → `presupuesto_no_cliente.id`

**Qué contiene:**
- Configuración de **mayores costos específicos por cada rubro**
- Permite sobrescribir los mayores costos generales del presupuesto
- Un registro por cada rubro (Albañilería, Pintura, etc.)

**Estructura:**
```sql
CREATE TABLE mayores_costos_por_rubro (
    id BIGSERIAL PRIMARY KEY,
    presupuesto_no_cliente_id BIGINT REFERENCES presupuesto_no_cliente(id) ON DELETE CASCADE,
    nombre_rubro VARCHAR(255),
    
    -- Igual estructura que honorarios_por_rubro
    activo BOOLEAN,
    tipo VARCHAR(50),
    valor NUMERIC(15,2),
    profesionales_activo BOOLEAN,
    profesionales_tipo VARCHAR(50),
    profesionales_valor NUMERIC(15,2),
    materiales_activo BOOLEAN,
    materiales_tipo VARCHAR(50),
    materiales_valor NUMERIC(15,2),
    otros_costos_activo BOOLEAN,
    otros_costos_tipo VARCHAR(50),
    otros_costos_valor NUMERIC(15,2),
    
    UNIQUE (presupuesto_no_cliente_id, nombre_rubro)
);
```

---

### 5️⃣ TABLA RELACIONAL: `descuentos_por_rubro` ⚠️ (Opcional - Si existe)

**Estado:** Esta tabla existe **solo si ejecutaste** `crear_tablas_mayores_costos_descuentos_por_rubro.sql`

**Relación:** `presupuesto_no_cliente_id` → `presupuesto_no_cliente.id`

**Qué contiene:**
- Configuración de **descuentos específicos por cada rubro**
- Permite aplicar descuentos diferenciados
- Un registro por cada rubro (Albañilería, Pintura, etc.)

**Estructura:**
```sql
CREATE TABLE descuentos_por_rubro (
    id BIGSERIAL PRIMARY KEY,
    presupuesto_no_cliente_id BIGINT REFERENCES presupuesto_no_cliente(id) ON DELETE CASCADE,
    nombre_rubro VARCHAR(255),
    
    -- Igual estructura que honorarios_por_rubro y mayores_costos_por_rubro
    activo BOOLEAN,
    tipo VARCHAR(50),
    valor NUMERIC(15,2),
    profesionales_activo BOOLEAN,
    profesionales_tipo VARCHAR(50),
    profesionales_valor NUMERIC(15,2),
    materiales_activo BOOLEAN,
    materiales_tipo VARCHAR(50),
    materiales_valor NUMERIC(15,2),
    otros_costos_activo BOOLEAN,
    otros_costos_tipo VARCHAR(50),
    otros_costos_valor NUMERIC(15,2),
    
    UNIQUE (presupuesto_no_cliente_id, nombre_rubro)
);
```

---

### 6️⃣ SUB-TABLAS DE ITEMS (DETALLE)

Estas tablas se relacionan con `items_calculadora_presupuesto`, no directamente con `presupuesto_no_cliente`:

#### `jornal_calculadora`
**Relación:** `item_calculadora_id` → `items_calculadora_presupuesto.id`

```sql
CONSTRAINT fk_jornal_item 
    FOREIGN KEY (item_calculadora_id) 
    REFERENCES items_calculadora_presupuesto(id) 
    ON DELETE CASCADE
```

#### `material_calculadora`
**Relación:** `item_calculadora_id` → `items_calculadora_presupuesto.id`

```sql
CONSTRAINT fk_material_item 
    FOREIGN KEY (item_calculadora_id) 
    REFERENCES items_calculadora_presupuesto(id) 
    ON DELETE CASCADE
```

#### `gasto_general_calculadora` (si existe)
**Relación:** `item_calculadora_id` → `items_calculadora_presupuesto.id`

---

## 📊 DIAGRAMA DE RELACIONES

```
presupuesto_no_cliente (id=108)
│
├─── [1:N] items_calculadora_presupuesto
│    │
│    ├─── [1:N] jornal_calculadora
│    ├─── [1:N] material_calculadora
│    └─── [1:N] gasto_general_calculadora
│
├─── [1:N] honorarios_por_rubro (OPCIONAL - si tabla existe)
│    └─── [Unique] nombre_rubro
│
├─── [1:N] mayores_costos_por_rubro (OPCIONAL - si tabla existe)
│    └─── [Unique] nombre_rubro
│
└─── [1:N] descuentos_por_rubro (OPCIONAL - si tabla existe)
     └─── [Unique] nombre_rubro
```

---

## 🔍 FLUJO DE CONFIGURACIÓN

### Cuando creás un presupuesto_no_cliente:

1. **Se crea el registro en `presupuesto_no_cliente`**
   - Con configuración global de honorarios (columnas internas)
   - Con configuración global de mayores costos (columnas internas)
   - Con totales en 0

2. **Se crean ítems en `items_calculadora_presupuesto`**
   - Cada ítem tiene un `tipo_profesional` (rubro)
   - Cada ítem tiene subtotales

3. **Opcionalmente, se configuran honorarios/mayores costos/descuentos POR RUBRO**
   - Si las tablas `*_por_rubro` existen en tu BD
   - Se crea un registro por cada rubro que quieras configurar específicamente
   - Estos valores sobrescriben los valores generales del presupuesto

4. **Se calculan totales**
   - Los totales se actualizan en `presupuesto_no_cliente`
   - `total_presupuesto`, `total_presupuesto_con_honorarios`, etc.

---

## ⚠️ IMPORTANTE: COLUMNAS AGREGADAS RECIENTEMENTE

Estas columnas se agregaron con el script `agregar_columnas_totales_por_rubro.sql`:

```sql
-- En presupuesto_no_cliente:
total_mayores_costos_por_rubro DECIMAL(15,2) DEFAULT 0.00
total_descuentos_por_rubro DECIMAL(15,2) DEFAULT 0.00
```

**Propósito:**
- Almacenar el total calculado de mayores costos aplicados vía configuración por rubro
- Almacenar el total calculado de descuentos aplicados vía configuración por rubro

---

## ✅ PARA AUDITAR PRESUPUESTO 108

Ejecutá el script que creé:

```bash
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f auditar_presupuesto_108.sql
```

Este script te mostrará:
1. ✓ Si el presupuesto existe
2. ✓ Datos básicos
3. ✓ Qué tablas relacionadas existen en tu BD
4. ✓ Cantidad de registros en cada tabla
5. ✓ Detección de datos huérfanos
6. ✓ Resumen completo

---

## 🔧 VERIFICAR QUÉ TABLAS EXISTEN EN TU BD

```sql
-- Verificar si existen las tablas opcionales
SELECT 
    tablename,
    CASE 
        WHEN tablename IN ('honorarios_por_rubro', 'mayores_costos_por_rubro', 'descuentos_por_rubro')
        THEN '✓ Existe'
        ELSE '⚠ No existe'
    END AS estado
FROM pg_tables 
WHERE schemaname = 'public' 
  AND tablename IN (
      'presupuesto_no_cliente',
      'items_calculadora_presupuesto',
      'honorarios_por_rubro',
      'mayores_costos_por_rubro',
      'descuentos_por_rubro',
      'jornal_calculadora',
      'material_calculadora'
  )
ORDER BY tablename;
```

---

## 📝 RESUMEN

**Tablas que SIEMPRE interactúan:**
1. `presupuesto_no_cliente` (tabla principal)
2. `items_calculadora_presupuesto` (ítems/rubros)
3. `jornal_calculadora` (detalle jornales por ítem)
4. `material_calculadora` (detalle materiales por ítem)

**Tablas que OPCIONALMENTE interactúan (si las creaste):**
5. `honorarios_por_rubro` (configuración honorarios por rubro)
6. `mayores_costos_por_rubro` (configuración mayores costos por rubro)
7. `descuentos_por_rubro` (configuración descuentos por rubro)

**Configuración almacenada DENTRO de presupuesto_no_cliente:**
- Honorarios globales (columnas `honorarios_*`)
- Mayores costos globales (columnas `mayores_costos_*`)
- Totales calculados (columnas `total_*`)
