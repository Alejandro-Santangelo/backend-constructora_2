# TABLAS RELACIONADAS CON TRABAJOS EXTRA

## 1. TABLA PRINCIPAL: `trabajos_extra`

**Descripción**: Almacena la información principal de cada trabajo extra de una obra.

### Columnas Principales:
- `id` (PK)
- `id_trabajo_extra` 
- `obra_id` (FK → obras)
- `cliente_id` (FK → clientes)
- `empresa_id` (FK → empresas)
- `nombre` - Nombre del trabajo extra
- `descripcion` - Descripción detallada
- `estado` - Estado del trabajo extra
- `estado_pago_general` - Estado de pago (default: 'PENDIENTE')
- `numero_presupuesto`
- `version`
- `es_trabajo_extra` - Flag booleano

### Fechas:
- `fecha_emision`
- `fecha_creacion`
- `fecha_probable_inicio`
- `vencimiento`
- `created_at`
- `updated_at`

### Totales Calculados:
- `monto_total`
- `total_presupuesto`
- `total_honorarios`
- `total_honorarios_calculado`
- `total_mayores_costos`
- `total_presupuesto_con_honorarios`
- `total_final`
- `total_general`
- `total_profesionales`
- `total_materiales`

### Configuración de Honorarios:
- `honorarios_aplicar_a_todos` (boolean)
- `honorarios_valor_general` (numeric)
- `honorarios_tipo_general` (varchar)
- `honorarios_profesionales_activo` (boolean)
- `honorarios_profesionales_tipo` (varchar)
- `honorarios_profesionales_valor` (numeric)
- `honorarios_materiales_activo` (boolean)
- `honorarios_materiales_tipo` (varchar)
- `honorarios_materiales_valor` (numeric)
- `honorarios_otros_costos_activo` (boolean)
- `honorarios_otros_costos_tipo` (varchar)
- `honorarios_otros_costos_valor` (numeric)
- `honorarios_jornales_activo` (boolean)
- `honorarios_jornales_tipo` (varchar)
- `honorarios_jornales_valor` (numeric)
- `honorarios_configuracion_presupuesto_activo` (boolean)
- `honorarios_configuracion_presupuesto_tipo` (varchar)
- `honorarios_configuracion_presupuesto_valor` (numeric)
- `honorario_direccion_valor_fijo` (numeric)
- `honorario_direccion_porcentaje` (numeric)
- `honorario_direccion_importe` (numeric)

### Configuración de Mayores Costos:
- `mayores_costos_aplicar_valor_general` (boolean)
- `mayores_costos_valor_general` (numeric)
- `mayores_costos_tipo_general` (varchar)
- `mayores_costos_general_importado` (boolean)
- `mayores_costos_rubro_importado` (varchar)
- `mayores_costos_nombre_rubro_importado` (varchar)
- `mayores_costos_explicacion` (text)
- `mayores_costos_profesionales_activo` (boolean)
- `mayores_costos_profesionales_tipo` (varchar)
- `mayores_costos_profesionales_valor` (numeric)
- `mayores_costos_materiales_activo` (boolean)
- `mayores_costos_materiales_tipo` (varchar)
- `mayores_costos_materiales_valor` (numeric)
- `mayores_costos_otros_costos_activo` (boolean)
- `mayores_costos_otros_costos_tipo` (varchar)
- `mayores_costos_otros_costos_valor` (numeric)
- `mayores_costos_jornales_activo` (boolean)
- `mayores_costos_jornales_tipo` (varchar)
- `mayores_costos_jornales_valor` (numeric)
- `mayores_costos_configuracion_presupuesto_activo` (boolean)
- `mayores_costos_configuracion_presupuesto_tipo` (varchar)
- `mayores_costos_configuracion_presupuesto_valor` (numeric)
- `mayores_costos_honorarios_activo` (boolean)
- `mayores_costos_honorarios_tipo` (varchar)
- `mayores_costos_honorarios_valor` (numeric)

### Dirección de Obra:
- `direccion_obra_calle`
- `direccion_obra_altura`
- `direccion_obra_piso`
- `direccion_obra_departamento`
- `direccion_obra_torre`
- `direccion_obra_barrio`
- `direccion_obra_localidad`
- `direccion_obra_provincia`
- `direccion_obra_codigo_postal`
- `direccion_particular`

### Otros Campos:
- `nombre_obra`
- `nombre_solicitante`
- `nombre_empresa`
- `telefono`
- `mail`
- `tiempo_estimado_terminacion`
- `calculo_automatico_dias_habiles` (boolean)
- `observaciones` (text)

### Índices:
- PK: `trabajos_extra_pkey` en `id`
- `idx_trabajos_extra_obra` en `obra_id`
- `idx_trabajos_extra_cliente` en `cliente_id`
- `idx_trabajos_extra_empresa` en `empresa_id`
- `idx_trabajos_extra_obra_empresa` en `(obra_id, empresa_id)`

---

## 2. `trabajos_extra_items_calculadora`

**Descripción**: Items de la calculadora del trabajo extra (profesionales, materiales, gastos generales).

### Columnas:
- `id` (PK)
- `trabajo_extra_id` (FK → trabajos_extra) **OBLIGATORIO**
- `empresa_id` **OBLIGATORIO**
- `tipo_profesional` (varchar 255) **OBLIGATORIO** - Ej: "Albanil", "Gastos Generales"
- `descripcion` (varchar 500)
- `observaciones` (text)
- `es_modo_manual` (boolean) **OBLIGATORIO**
- `es_rubro_vacio` (boolean)
- `es_gasto_general` (boolean) **OBLIGATORIO**
- `trabaja_en_paralelo` (boolean) **OBLIGATORIO**
- `incluir_en_calculo_dias` (boolean)

### Subtotales por Tipo:
- `subtotal_mano_obra` (numeric 15,2)
- `subtotal_materiales` (numeric 15,2)
- `subtotal_gastos_generales` (numeric 15,2)
- `materiales` (numeric 15,2)
- `importe_jornal` (numeric 15,2)
- `cantidad_jornales` (numeric 10,2)
- `total` (numeric 15,2)
- `total_manual` (numeric 15,2)

### Descripciones y Observaciones por Tipo:
- `descripcion_profesionales` (varchar 500)
- `observaciones_profesionales` (text)
- `descripcion_materiales` (varchar 500)
- `observaciones_materiales` (text)
- `descripcion_gastos_generales` (varchar 500)
- `observaciones_gastos_generales` (text)
- `descripcion_total_manual` (varchar 500)
- `observaciones_total_manual` (text)

### Auditoría:
- `created_at`
- `updated_at`

### Índices:
- PK: `trabajos_extra_items_calculadora_pkey`
- `idx_te_items_trabajo_extra_id` en `trabajo_extra_id`
- `idx_te_items_empresa_id` en `empresa_id`
- `idx_te_items_tipo_profesional` en `tipo_profesional`
- `idx_te_items_es_gasto_general` en `es_gasto_general`

---

## 3. `trabajos_extra_profesional_calculadora`

**Descripción**: Profesionales asignados a items de calculadora del trabajo extra.

### Columnas Principales:
- `id` (PK)
- `item_calculadora_id` (FK → trabajos_extra_items_calculadora) **OBLIGATORIO**
- `empresa_id` **OBLIGATORIO**
- `nombre_profesional` (varchar 255)
- `descripcion` (varchar 500)
- `cantidad_dias` (numeric 10,2)
- `importe_unitario` (numeric 15,2)
- `subtotal` (numeric 15,2)
- `orden` (integer)
- `observaciones` (text)
- `created_at`
- `updated_at`

---

## 4. `trabajos_extra_material_calculadora`

**Descripción**: Materiales asignados a items de calculadora del trabajo extra.

### Columnas Principales:
- `id` (PK)
- `item_calculadora_id` (FK → trabajos_extra_items_calculadora) **OBLIGATORIO**
- `empresa_id` **OBLIGATORIO**
- `nombre_material` (varchar 255)
- `descripcion` (varchar 500)
- `cantidad` (numeric 10,2)
- `unidad_medida` (varchar 50)
- `precio_unitario` (numeric 15,2)
- `subtotal` (numeric 15,2)
- `orden` (integer)
- `observaciones` (text)
- `sin_cantidad` (boolean)
- `sin_precio` (boolean)
- `created_at`
- `updated_at`

---

## 5. `trabajos_extra_jornal_calculadora`

**Descripción**: Jornales asignados a items de calculadora del trabajo extra.

### Columnas Principales:
- `id` (PK)
- `item_calculadora_id` (FK → trabajos_extra_items_calculadora) **OBLIGATORIO**
- `empresa_id` **OBLIGATORIO**
- `nombre_jornal` (varchar 255)
- `descripcion` (varchar 500)
- `cantidad` (numeric 10,2)
- `precio_unitario` (numeric 15,2)
- `subtotal` (numeric 15,2)
- `orden` (integer)
- `observaciones` (text)
- `created_at`
- `updated_at`

---

## 6. `trabajos_extra_gasto_general`

**Descripción**: Gastos generales asignados a items de calculadora del trabajo extra.

### Columnas Principales:
- `id` (PK)
- `item_calculadora_id` (FK → trabajos_extra_items_calculadora) **OBLIGATORIO**
- `empresa_id` **OBLIGATORIO**
- `descripcion` (varchar 500) **OBLIGATORIO**
- `cantidad` (numeric 10,2)
- `precio_unitario` (numeric 15,2)
- `subtotal` (numeric 15,2)
- `orden` (integer)
- `observaciones` (text)
- `sin_cantidad` (boolean)
- `sin_precio` (boolean)
- `es_global` (boolean)
- `frontend_id` (bigint) - ID de vinculación con catálogo
- `created_at`
- `updated_at`

---

## 7. `trabajos_extra_profesionales`

**Descripción**: Profesionales asignados directamente al trabajo extra (legacy o asignaciones generales).

### Columnas Principales:
- `id` (PK)
- `trabajo_extra_id` (FK → trabajos_extra)
- `profesional_id` (FK → profesionales)
- `empresa_id`
- `nombre`
- `importe_base`
- `dias`
- `subtotal`
- `total`
- `estado_pago`
- `observaciones`
- `created_at`
- `updated_at`

---

## 8. `trabajos_extra_tareas`

**Descripción**: Tareas específicas del trabajo extra.

### Columnas Principales:
- `id` (PK)
- `trabajo_extra_id` (FK → trabajos_extra)
- `empresa_id`
- `descripcion` (text)
- `estado` (varchar)
- `prioridad` (varchar)
- `fecha_inicio`
- `fecha_fin`
- `observaciones` (text)
- `created_at`
- `updated_at`

---

## 9. `trabajos_extra_tareas_profesionales`

**Descripción**: Relación muchos-a-muchos entre tareas y profesionales.

### Columnas Principales:
- `id` (PK)
- `tarea_id` (FK → trabajos_extra_tareas)
- `profesional_id` (FK → profesionales o trabajos_extra_profesionales)
- `empresa_id`
- `horas_asignadas` (numeric)
- `horas_trabajadas` (numeric)
- `observaciones` (text)
- `created_at`
- `updated_at`

---

## 10. `trabajos_extra_dias`

**Descripción**: Control de días trabajados en el trabajo extra.

### Columnas Principales:
- `id` (PK)
- `trabajo_extra_id` (FK → trabajos_extra)
- `empresa_id`
- `fecha` (date)
- `descripcion` (text)
- `horas` (numeric)
- `estado` (varchar) - Ej: "trabajado", "lluvia", "feriado"
- `observaciones` (text)
- `created_at`
- `updated_at`

---

## 11. `pagos_trabajos_extra_obra`

**Descripción**: Pagos realizados para trabajos extra.

### Columnas Principales:
- `id` (PK)
- `trabajo_extra_id` (FK → trabajos_extra) ON DELETE CASCADE
- `obra_id` (FK → obras)
- `empresa_id`
- `monto` (numeric 15,2)
- `fecha_pago` (date)
- `metodo_pago` (varchar) - Ej: "efectivo", "transferencia", "cheque"
- `numero_comprobante` (varchar)
- `observaciones` (text)
- `estado` (varchar) - Ej: "pendiente", "aprobado", "rechazado"
- `created_at`
- `updated_at`

---

## 12. `trabajos_extra_pdf`

**Descripción**: PDFs generados para trabajos extra.

### Columnas Principales:
- `id` (PK)
- `trabajo_extra_id` (FK → trabajos_extra)
- `empresa_id`
- `tipo_pdf` (varchar) - Ej: "presupuesto", "factura", "recibo"
- `ruta_archivo` (varchar)
- `nombre_archivo` (varchar)
- `fecha_generacion` (timestamp)
- `version` (integer)
- `created_at`
- `updated_at`

---

## RELACIONES PRINCIPALES

```
trabajos_extra (1) ──→ (N) trabajos_extra_items_calculadora
                 │
                 ├──→ (N) trabajos_extra_profesionales
                 │
                 ├──→ (N) trabajos_extra_tareas
                 │
                 ├──→ (N) trabajos_extra_dias
                 │
                 ├──→ (N) pagos_trabajos_extra_obra
                 │
                 └──→ (N) trabajos_extra_pdf

trabajos_extra_items_calculadora (1) ──→ (N) trabajos_extra_profesional_calculadora
                                    │
                                    ├──→ (N) trabajos_extra_material_calculadora
                                    │
                                    ├──→ (N) trabajos_extra_jornal_calculadora
                                    │
                                    └──→ (N) trabajos_extra_gasto_general

trabajos_extra_tareas (1) ──→ (N) trabajos_extra_tareas_profesionales
```

---

## NOTAS IMPORTANTES

1. **Estructura Similar a Presupuestos**: Los trabajos extra tienen una estructura muy similar a `presupuesto_no_cliente` con las mismas configuraciones de honorarios y mayores costos.

2. **Campo `subtotal` en Gastos**: Al igual que en presupuestos, el campo `subtotal` en `trabajos_extra_gasto_general` guarda **SOLO EL VALOR BASE** (cantidad × precio_unitario), sin honorarios ni mayores costos aplicados.

3. **Configuración de Honorarios**: Se almacena en la tabla principal `trabajos_extra` y se aplica dinámicamente en el frontend al calcular totales.

4. **Multi-Tenant**: Todas las tablas incluyen `empresa_id` para filtrado multi-tenant.

5. **Índices Compuestos**: La tabla principal tiene índices compuestos `(obra_id, empresa_id)` para búsquedas eficientes.

---

## FLUJO DE TRABAJO COMPLETO

### 📋 FASE 1: CREACIÓN DEL TRABAJO EXTRA

**Paso 1.1: Crear el Trabajo Extra**
```
Tabla: trabajos_extra
Acción: INSERT

Datos básicos:
- obra_id (FK → obras) ✅
- cliente_id (FK → clientes) ✅
- empresa_id (FK → empresas) ✅
- nombre: "Ampliación de quincho"
- descripcion: "Trabajos adicionales solicitados por el cliente"
- fecha_emision: 2026-01-12
- estado: "PENDIENTE"
- estado_pago_general: "PENDIENTE"
- numero_presupuesto: "TE-2026-001"
- version: 1
- es_trabajo_extra: true

Configuración de honorarios (valores por defecto o personalizados):
- honorarios_otros_costos_activo: true
- honorarios_otros_costos_valor: 10.00 (10%)
- mayores_costos_otros_costos_activo: true
- mayores_costos_otros_costos_valor: 5.00 (5%)
- mayores_costos_honorarios_activo: true
- mayores_costos_honorarios_valor: 5.00 (5%)

Resultado: Se obtiene trabajo_extra.id = 123
```

---

### 🧮 FASE 2: CONSTRUCCIÓN DE LA CALCULADORA

**Paso 2.1: Crear Items de Calculadora (Rubros)**
```
Tabla: trabajos_extra_items_calculadora
Acción: INSERT (uno por cada rubro/categoría)

Item 1 - Rubro de Albanilería:
- trabajo_extra_id: 123 ✅
- empresa_id: 1 ✅
- tipo_profesional: "Albanil"
- descripcion: "Trabajos de mampostería"
- es_modo_manual: false
- es_rubro_vacio: false
- es_gasto_general: false
- trabaja_en_paralelo: false
- incluir_en_calculo_dias: true

Resultado: item_calculadora.id = 456

Item 2 - Gastos Generales:
- trabajo_extra_id: 123 ✅
- tipo_profesional: "Gastos Generales"
- es_gasto_general: true ✅

Resultado: item_calculadora.id = 457
```

**Paso 2.2: Agregar Profesionales al Item**
```
Tabla: trabajos_extra_profesional_calculadora
Acción: INSERT (por cada profesional en el item)

Para item_calculadora_id = 456:
- nombre_profesional: "Oficial Albanil"
- cantidad_dias: 5.00
- importe_unitario: 15000.00
- subtotal: 75000.00 (5 × 15000)
- orden: 1

Profesional 2:
- nombre_profesional: "Ayudante"
- cantidad_dias: 5.00
- importe_unitario: 10000.00
- subtotal: 50000.00
- orden: 2

Subtotal Mano de Obra del Item 456: $125.000
```

**Paso 2.3: Agregar Materiales al Item**
```
Tabla: trabajos_extra_material_calculadora
Acción: INSERT (por cada material en el item)

Para item_calculadora_id = 456:
- nombre_material: "Cemento"
- cantidad: 20.00
- unidad_medida: "Bolsa"
- precio_unitario: 8000.00
- subtotal: 160000.00
- orden: 1
- sin_cantidad: false
- sin_precio: false

Material 2:
- nombre_material: "Arena"
- cantidad: 5.00
- unidad_medida: "m³"
- precio_unitario: 12000.00
- subtotal: 60000.00
- orden: 2

Subtotal Materiales del Item 456: $220.000
```

**Paso 2.4: Agregar Jornales al Item**
```
Tabla: trabajos_extra_jornal_calculadora
Acción: INSERT (por cada jornal en el item)

Para item_calculadora_id = 456:
- nombre_jornal: "Jornal oficial"
- cantidad: 5.00
- precio_unitario: 15000.00
- subtotal: 75000.00
- orden: 1
```

**Paso 2.5: Agregar Gastos Generales**
```
Tabla: trabajos_extra_gasto_general
Acción: INSERT (por cada gasto en el item de gastos)

Para item_calculadora_id = 457 (Item de Gastos Generales):
- descripcion: "Alquiler de andamios"
- cantidad: 1.00
- precio_unitario: 50000.00
- subtotal: 50000.00 ← SOLO BASE, SIN HONORARIOS
- orden: 1
- es_global: false
- sin_cantidad: false
- sin_precio: false

Gasto 2:
- descripcion: "Flete de materiales"
- cantidad: 1.00
- precio_unitario: 30000.00
- subtotal: 30000.00 ← SOLO BASE
- orden: 2

Subtotal Gastos Generales: $80.000 (BASE)
```

**Paso 2.6: Calcular Totales del Item**
```
Tabla: trabajos_extra_items_calculadora
Acción: UPDATE (actualizar subtotales del item 456)

UPDATE trabajos_extra_items_calculadora
SET 
  subtotal_mano_obra = 125000.00,
  subtotal_materiales = 220000.00,
  total = 345000.00  ← Suma de mano obra + materiales
WHERE id = 456;

Para item 457 (Gastos):
UPDATE trabajos_extra_items_calculadora
SET 
  subtotal_gastos_generales = 80000.00,
  total = 80000.00
WHERE id = 457;
```

**Paso 2.7: Calcular Totales del Trabajo Extra**
```
Tabla: trabajos_extra
Acción: UPDATE (calcular totales finales)

Cálculo:
Base = 345000 + 80000 = 425000

Honorarios sobre otros costos (10% sobre gastos):
- Sobre base gastos: 80000 × 10% = 8000

Mayores costos sobre otros costos (5% sobre gastos):
- Sobre base gastos: 80000 × 5% = 4000

Mayores costos sobre honorarios (5%):
- Sobre honorarios: 8000 × 5% = 400

Total con honorarios y mayores costos:
425000 + 8000 + 4000 + 400 = 437400

UPDATE trabajos_extra
SET
  total_presupuesto = 425000.00,
  total_honorarios = 8000.00,
  total_mayores_costos = 4400.00,
  total_presupuesto_con_honorarios = 437400.00,
  total_final = 437400.00,
  monto_total = 437400.00
WHERE id = 123;
```

---

### ✅ FASE 3: APROBACIÓN Y ACTIVACIÓN

**Paso 3.1: Cambiar Estado del Trabajo Extra**
```
Tabla: trabajos_extra
Acción: UPDATE

UPDATE trabajos_extra
SET 
  estado = 'APROBADO',
  fecha_probable_inicio = '2026-01-15'
WHERE id = 123;

Nota: Al aprobar, el trabajo extra queda disponible para asignaciones.
```

**Paso 3.2: Generar PDF (Opcional)**
```
Tabla: trabajos_extra_pdf
Acción: INSERT

Datos:
- trabajo_extra_id: 123 ✅
- empresa_id: 1
- tipo_pdf: "presupuesto"
- ruta_archivo: "/pdfs/trabajos_extra/2026/01/"
- nombre_archivo: "TE-2026-001-v1.pdf"
- fecha_generacion: 2026-01-12 14:30:00
- version: 1
```

---

### 👷 FASE 4: ASIGNACIÓN DE PROFESIONALES A TAREAS

**Paso 4.1: Crear Tareas del Trabajo Extra**
```
Tabla: trabajos_extra_tareas
Acción: INSERT (por cada tarea específica)

Tarea 1:
- trabajo_extra_id: 123 ✅
- empresa_id: 1
- descripcion: "Construcción de base de quincho"
- estado: "PENDIENTE"
- prioridad: "ALTA"
- fecha_inicio: 2026-01-15
- fecha_fin: 2026-01-20

Resultado: tarea.id = 789

Tarea 2:
- descripcion: "Instalación de techo"
- estado: "PENDIENTE"
- prioridad: "MEDIA"

Resultado: tarea.id = 790
```

**Paso 4.2: Asignar Profesionales a Tareas**
```
Tabla: trabajos_extra_tareas_profesionales
Acción: INSERT (relación muchos-a-muchos)

Para tarea_id = 789:
- profesional_id: 25 (FK → profesionales tabla general)
- empresa_id: 1
- horas_asignadas: 40.00
- horas_trabajadas: 0.00
- observaciones: "Responsable de albanilería"

Para tarea_id = 790:
- profesional_id: 26
- horas_asignadas: 20.00
```

---

### 📅 FASE 5: CONTROL DE AVANCE DIARIO

**Paso 5.1: Registrar Días Trabajados**
```
Tabla: trabajos_extra_dias
Acción: INSERT (por cada día de trabajo)

Día 1:
- trabajo_extra_id: 123 ✅
- empresa_id: 1
- fecha: 2026-01-15
- descripcion: "Inicio de trabajos de base"
- horas: 8.00
- estado: "trabajado"
- observaciones: "Día normal de trabajo"

Día 2:
- fecha: 2026-01-16
- horas: 0.00
- estado: "lluvia"
- observaciones: "Suspendido por lluvia"

Día 3:
- fecha: 2026-01-17
- horas: 8.00
- estado: "trabajado"
```

**Paso 5.2: Actualizar Horas Trabajadas en Tareas**
```
Tabla: trabajos_extra_tareas_profesionales
Acción: UPDATE (actualizar progreso)

UPDATE trabajos_extra_tareas_profesionales
SET 
  horas_trabajadas = 16.00,  ← Acumulado 2 días × 8 horas
  observaciones = "Progreso normal"
WHERE tarea_id = 789 AND profesional_id = 25;
```

---

### 💰 FASE 6: GESTIÓN DE PAGOS

**Paso 6.1: Registrar Pago Parcial**
```
Tabla: pagos_trabajos_extra_obra
Acción: INSERT

Pago 1 (50% adelanto):
- trabajo_extra_id: 123 ✅ ON DELETE CASCADE
- obra_id: 10 (FK → obras)
- empresa_id: 1
- monto: 218700.00  ← 50% de 437400
- fecha_pago: 2026-01-15
- metodo_pago: "transferencia"
- numero_comprobante: "TRANS-2026-001"
- estado: "aprobado"
- observaciones: "Pago adelantado 50%"

Resultado: pago.id = 555
```

**Paso 6.2: Actualizar Estado de Pago del Trabajo Extra**
```
Tabla: trabajos_extra
Acción: UPDATE

UPDATE trabajos_extra
SET estado_pago_general = 'PARCIAL'
WHERE id = 123;
```

**Paso 6.3: Registrar Pago Final**
```
Tabla: pagos_trabajos_extra_obra
Acción: INSERT

Pago 2 (50% restante):
- trabajo_extra_id: 123
- monto: 218700.00
- fecha_pago: 2026-01-22
- metodo_pago: "efectivo"
- estado: "aprobado"
- observaciones: "Pago final al completar trabajos"
```

**Paso 6.4: Marcar como Pagado Completamente**
```
Tabla: trabajos_extra
Acción: UPDATE

UPDATE trabajos_extra
SET 
  estado_pago_general = 'PAGADO',
  estado = 'FINALIZADO'
WHERE id = 123;
```

---

### 🔄 FASE 7: ASIGNACIONES A OBRA (STOCK)

**Paso 7.1: Asignar Gastos Generales a Obra**
```
Flujo:
1. El frontend consulta los gastos del trabajo extra:
   GET /api/presupuestos-no-cliente/123/gastos-generales
   (Los trabajos extra usan el mismo endpoint)

2. El backend extrae de trabajos_extra_gasto_general:
   - Para cada gasto con subtotal (valor base)
   - Devuelve: { id, descripcion, importe: 50000 }  ← SOLO BASE

3. El frontend calcula el total disponible:
   - Obtiene configuración de honorarios/mayores costos desde trabajos_extra
   - Calcula: base + honorarios + mayores costos
   - Total disponible del gasto "Alquiler de andamios": $57750

4. Usuario asigna a la obra:
   - Frontend envía: { gastoId: X, montoAsignado: 30000 }

Tablas involucradas:
- Lectura: trabajos_extra (configuración)
- Lectura: trabajos_extra_items_calculadora
- Lectura: trabajos_extra_gasto_general
- Escritura: asignaciones_otro_costo_obra (o tabla similar)
```

**Paso 7.2: Asignar Materiales a Obra**
```
Flujo similar:
1. Consultar materiales del trabajo extra
   Tabla: trabajos_extra_material_calculadora

2. Frontend calcula disponible con honorarios

3. Asignar a obra:
   Tabla destino: asignaciones_material_obra
   (Vincula material del trabajo extra con la obra)
```

**Paso 7.3: Asignar Profesionales a Obra**
```
Tablas origen:
- trabajos_extra_profesional_calculadora
- trabajos_extra_profesionales

Tabla destino:
- asignaciones_profesional_obra o similar
- profesional_obra (asignación general)
```

---

### 📊 CONSULTAS FRECUENTES

**Obtener trabajo extra completo con todos sus items:**
```sql
-- Trabajo extra principal
SELECT * FROM trabajos_extra WHERE id = 123;

-- Items de calculadora
SELECT * FROM trabajos_extra_items_calculadora 
WHERE trabajo_extra_id = 123;

-- Profesionales de cada item
SELECT p.* 
FROM trabajos_extra_profesional_calculadora p
JOIN trabajos_extra_items_calculadora i ON p.item_calculadora_id = i.id
WHERE i.trabajo_extra_id = 123;

-- Materiales
SELECT m.* 
FROM trabajos_extra_material_calculadora m
JOIN trabajos_extra_items_calculadora i ON m.item_calculadora_id = i.id
WHERE i.trabajo_extra_id = 123;

-- Gastos generales
SELECT g.* 
FROM trabajos_extra_gasto_general g
JOIN trabajos_extra_items_calculadora i ON g.item_calculadora_id = i.id
WHERE i.trabajo_extra_id = 123;
```

**Obtener total de pagos realizados:**
```sql
SELECT 
  te.id,
  te.nombre,
  te.total_final,
  COALESCE(SUM(p.monto), 0) as total_pagado,
  te.total_final - COALESCE(SUM(p.monto), 0) as saldo_pendiente
FROM trabajos_extra te
LEFT JOIN pagos_trabajos_extra_obra p ON te.id = p.trabajo_extra_id 
  AND p.estado = 'aprobado'
WHERE te.id = 123
GROUP BY te.id;
```

**Obtener avance de días trabajados:**
```sql
SELECT 
  fecha,
  estado,
  horas,
  descripcion
FROM trabajos_extra_dias
WHERE trabajo_extra_id = 123
ORDER BY fecha;
```

---

### 🔑 PUNTOS CLAVE DEL FLUJO

1. **Creación Jerárquica**: 
   - trabajos_extra → items_calculadora → (profesionales/materiales/jornales/gastos)

2. **Cálculo de Totales**: 
   - Se guardan valores BASE en subtotales
   - Honorarios y mayores costos se aplican dinámicamente en frontend
   - Solo el total final se persiste en trabajos_extra

3. **Asignaciones**: 
   - Se leen gastos/materiales/profesionales del trabajo extra
   - Se aplican honorarios/mayores costos en frontend
   - Se crean asignaciones a obra con montos calculados

4. **Control de Pagos**: 
   - Pagos se registran en tabla separada
   - Estado de pago se actualiza en tabla principal
   - Soporte para pagos parciales y múltiples métodos

5. **Seguimiento**: 
   - Días trabajados por fecha
   - Tareas con profesionales asignados
   - Horas trabajadas vs. asignadas

6. **Eliminación en Cascada**: 
   - Al eliminar trabajo extra, se eliminan automáticamente:
     - Items de calculadora y sus detalles
     - Pagos (ON DELETE CASCADE)
     - Días trabajados
     - Tareas y asignaciones

---

## 🎯 GUÍA PARA FRONTEND

### ENDPOINTS DE LA API

Los trabajos extra comparten los mismos endpoints que los presupuestos normales, diferenciándose por el campo `tipo_presupuesto = 'TRABAJOS_SEMANALES'` o `es_trabajo_extra = true`.

#### 📋 **Endpoints Principales**

```
BASE_URL: /api/presupuestos-no-cliente
ALTERNATIVO: /api/trabajos-extra (si existe router específico)
```

**1. Crear Trabajo Extra**
```http
POST /api/presupuestos-no-cliente
Headers: 
  empresaId: 1
  Content-Type: application/json

Body: {
  "idEmpresa": 1,
  "idObra": 10,
  "idCliente": 5,
  "nombre": "Ampliación de quincho",
  "descripcion": "Trabajos adicionales solicitados",
  "tipoPresupuesto": "TRABAJOS_SEMANALES",
  "estado": "PENDIENTE",
  
  // Configuración de honorarios
  "honorariosOtrosCostosActivo": true,
  "honorariosOtrosCostosTipo": "porcentaje",
  "honorariosOtrosCostosValor": 10.00,
  
  // Configuración de mayores costos
  "mayoresCostosOtrosCostosActivo": true,
  "mayoresCostosOtrosCostosTipo": "porcentaje",
  "mayoresCostosOtrosCostosValor": 5.00,
  
  "mayoresCostosHonorariosActivo": true,
  "mayoresCostosHonorariosTipo": "porcentaje",
  "mayoresCostosHonorariosValor": 5.00,
  
  // Items de calculadora
  "itemsCalculadora": [
    {
      "tipoProfesional": "Albanil",
      "descripcion": "Trabajos de mampostería",
      "esModoManual": false,
      "esGastoGeneral": false,
      "trabajaEnParalelo": false,
      "incluirEnCalculoDias": true,
      
      "profesionales": [
        {
          "nombreProfesional": "Oficial Albanil",
          "cantidadDias": 5.00,
          "importeUnitario": 15000.00,
          "subtotal": 75000.00,
          "orden": 1
        }
      ],
      
      "materiales": [
        {
          "nombreMaterial": "Cemento",
          "cantidad": 20.00,
          "unidadMedida": "Bolsa",
          "precioUnitario": 8000.00,
          "subtotal": 160000.00,
          "sinCantidad": false,
          "sinPrecio": false,
          "orden": 1
        }
      ],
      
      "jornales": [
        {
          "nombreJornal": "Jornal oficial",
          "cantidad": 5.00,
          "precioUnitario": 15000.00,
          "subtotal": 75000.00,
          "orden": 1
        }
      ]
    },
    {
      "tipoProfesional": "Gastos Generales",
      "esGastoGeneral": true,
      
      "gastosGenerales": [
        {
          "descripcion": "Alquiler de andamios",
          "cantidad": 1.00,
          "precioUnitario": 50000.00,
          "subtotal": 50000.00,
          "orden": 1,
          "esGlobal": false
        }
      ]
    }
  ]
}

Response 201 Created:
{
  "id": 123,
  "nombre": "Ampliación de quincho",
  "estado": "PENDIENTE",
  "totalPresupuesto": 425000.00,
  "totalHonorarios": 8000.00,
  "totalMayoresCostos": 4400.00,
  "totalPresupuestoConHonorarios": 437400.00,
  "itemsCalculadora": [...],
  ...
}
```

**2. Obtener Trabajo Extra por ID**
```http
GET /api/presupuestos-no-cliente/123?empresaId=1

Response 200:
{
  "id": 123,
  "obraId": 10,
  "clienteId": 5,
  "empresaId": 1,
  "nombre": "Ampliación de quincho",
  "estado": "PENDIENTE",
  
  // ✅ CONFIGURACIÓN DE HONORARIOS (incluida en respuesta)
  "honorariosOtrosCostosActivo": true,
  "honorariosOtrosCostosValor": 10.00,
  "honorariosOtrosCostosTipo": "porcentaje",
  
  // ✅ CONFIGURACIÓN DE MAYORES COSTOS (incluida en respuesta)
  "mayoresCostosOtrosCostosActivo": true,
  "mayoresCostosOtrosCostosValor": 5.00,
  "mayoresCostosHonorariosActivo": true,
  "mayoresCostosHonorariosValor": 5.00,
  
  // ✅ ITEMS CON GASTOS (subtotales SIN honorarios)
  "itemsCalculadora": [
    {
      "id": 456,
      "tipoProfesional": "Albanil",
      "subtotalManoObra": 125000.00,
      "subtotalMateriales": 220000.00,
      "total": 345000.00,
      "profesionales": [...],
      "materiales": [...],
      "jornales": [...]
    },
    {
      "id": 457,
      "tipoProfesional": "Gastos Generales",
      "esGastoGeneral": true,
      "gastosGenerales": [
        {
          "id": 101,
          "descripcion": "Alquiler de andamios",
          "cantidad": 1.00,
          "precioUnitario": 50000.00,
          "subtotal": 50000.00  // ← VALOR BASE SIN HONORARIOS
        }
      ]
    }
  ],
  
  "totalPresupuesto": 425000.00,
  "totalPresupuestoConHonorarios": 437400.00
}
```

**3. Obtener Gastos Generales del Trabajo Extra**
```http
GET /api/presupuestos-no-cliente/123/gastos-generales
Headers:
  empresaId: 1

Response 200:
[
  {
    "id": 101,
    "categoria": "Gastos Generales",
    "descripcion": "Alquiler de andamios",
    "importe": 50000.00,  // ← SOLO VALOR BASE
    "observaciones": ""
  },
  {
    "id": 102,
    "categoria": "Gastos Generales",
    "descripcion": "Flete de materiales",
    "importe": 30000.00,
    "observaciones": ""
  }
]

⚠️ IMPORTANTE: Este endpoint NO devuelve configuración de honorarios.
   Debes hacer GET /presupuestos-no-cliente/123 para obtener esa info.
```

**4. Listar Trabajos Extra de una Obra**
```http
GET /api/presupuestos-no-cliente?empresaId=1&obraId=10

Response 200:
[
  {
    "id": 123,
    "nombre": "Ampliación de quincho",
    "tipoPresupuesto": "TRABAJOS_SEMANALES",
    "estado": "APROBADO",
    "totalPresupuestoConHonorarios": 437400.00,
    ...
  },
  {
    "id": 124,
    "nombre": "Reparación de techo",
    "estado": "PENDIENTE",
    ...
  }
]
```

**5. Actualizar Trabajo Extra**
```http
PUT /api/presupuestos-no-cliente/123
Headers:
  empresaId: 1
  Content-Type: application/json

Body: {
  "estado": "APROBADO",
  "fechaProbableInicio": "2026-01-15",
  ... (campos a actualizar)
}
```

**6. Eliminar Trabajo Extra**
```http
DELETE /api/presupuestos-no-cliente/123?empresaId=1

Response 204 No Content
```

---

### 🧮 CÁLCULO DE TOTALES EN EL FRONTEND

**REGLA DE ORO**: El backend devuelve **VALORES BASE** en `subtotal`. El frontend debe calcular honorarios y mayores costos dinámicamente.

#### **Función de Cálculo (JavaScript/TypeScript)**

```javascript
/**
 * Calcula el total de un gasto aplicando honorarios y mayores costos
 * 
 * @param {number} valorBase - Valor base del gasto (subtotal)
 * @param {object} configuracionHonorarios - Config de honorarios del presupuesto
 * @param {object} configuracionMayoresCostos - Config de mayores costos
 * @returns {object} - Desglose completo del cálculo
 */
function calcularTotalGasto(valorBase, configuracionHonorarios, configuracionMayoresCostos) {
  let totalHonorarios = 0;
  let totalMayoresCostos = 0;
  
  // 1. Calcular honorarios sobre la base
  if (configuracionHonorarios?.otrosCostos?.activo) {
    const valor = configuracionHonorarios.otrosCostos.valor || 0;
    const tipo = configuracionHonorarios.otrosCostos.tipo;
    
    if (tipo === 'porcentaje') {
      totalHonorarios = valorBase * (valor / 100);
    } else if (tipo === 'fijo') {
      totalHonorarios = valor;
    }
  }
  
  // 2. Calcular mayores costos sobre la base
  let mayoresCostosBase = 0;
  if (configuracionMayoresCostos?.otrosCostos?.activo) {
    const valor = configuracionMayoresCostos.otrosCostos.valor || 0;
    const tipo = configuracionMayoresCostos.otrosCostos.tipo;
    
    if (tipo === 'porcentaje') {
      mayoresCostosBase = valorBase * (valor / 100);
    } else if (tipo === 'fijo') {
      mayoresCostosBase = valor;
    }
  }
  
  // 3. Calcular mayores costos sobre honorarios
  let mayoresCostosHonorarios = 0;
  if (configuracionMayoresCostos?.honorarios?.activo) {
    const valor = configuracionMayoresCostos.honorarios.valor || 0;
    const tipo = configuracionMayoresCostos.honorarios.tipo;
    
    if (tipo === 'porcentaje') {
      mayoresCostosHonorarios = totalHonorarios * (valor / 100);
    } else if (tipo === 'fijo') {
      mayoresCostosHonorarios = valor;
    }
  }
  
  totalMayoresCostos = mayoresCostosBase + mayoresCostosHonorarios;
  
  // 4. Total final
  const totalFinal = valorBase + totalHonorarios + totalMayoresCostos;
  
  return {
    valorBase,
    honorarios: totalHonorarios,
    mayoresCostosBase,
    mayoresCostosHonorarios,
    totalMayoresCostos,
    totalFinal
  };
}

// EJEMPLO DE USO:
const presupuesto = {
  honorariosOtrosCostosActivo: true,
  honorariosOtrosCostosValor: 10.00,
  honorariosOtrosCostosTipo: "porcentaje",
  mayoresCostosOtrosCostosActivo: true,
  mayoresCostosOtrosCostosValor: 5.00,
  mayoresCostosHonorariosActivo: true,
  mayoresCostosHonorariosValor: 5.00
};

const gastoBase = 50000; // Valor del backend (subtotal)

const resultado = calcularTotalGasto(
  gastoBase,
  {
    otrosCostos: {
      activo: presupuesto.honorariosOtrosCostosActivo,
      valor: presupuesto.honorariosOtrosCostosValor,
      tipo: presupuesto.honorariosOtrosCostosTipo
    }
  },
  {
    otrosCostos: {
      activo: presupuesto.mayoresCostosOtrosCostosActivo,
      valor: presupuesto.mayoresCostosOtrosCostosValor,
      tipo: "porcentaje"
    },
    honorarios: {
      activo: presupuesto.mayoresCostosHonorariosActivo,
      valor: presupuesto.mayoresCostosHonorariosValor,
      tipo: "porcentaje"
    }
  }
);

console.log(resultado);
// {
//   valorBase: 50000,
//   honorarios: 5000,           // 10% de 50000
//   mayoresCostosBase: 2500,    // 5% de 50000
//   mayoresCostosHonorarios: 250, // 5% de 5000
//   totalMayoresCostos: 2750,
//   totalFinal: 57750
// }
```

---

### 📱 FLUJO DESDE LA PERSPECTIVA DEL FRONTEND

#### **PANTALLA 1: Lista de Trabajos Extra de la Obra**

```javascript
// Componente: ListaTrabajosExtra.jsx
import { useEffect, useState } from 'react';

function ListaTrabajosExtra({ obraId, empresaId }) {
  const [trabajosExtra, setTrabajosExtra] = useState([]);
  
  useEffect(() => {
    async function cargarTrabajosExtra() {
      const response = await fetch(
        `/api/presupuestos-no-cliente?empresaId=${empresaId}&obraId=${obraId}`
      );
      const data = await response.json();
      
      // Filtrar solo trabajos extra (por si hay presupuestos normales)
      const soloTrabajosExtra = data.filter(p => 
        p.tipoPresupuesto === 'TRABAJOS_SEMANALES' || p.esTrabajoExtra
      );
      
      setTrabajosExtra(soloTrabajosExtra);
    }
    
    cargarTrabajosExtra();
  }, [obraId, empresaId]);
  
  return (
    <div>
      <h2>Trabajos Extra de la Obra</h2>
      <button onClick={() => navigate('/crear-trabajo-extra')}>
        + Nuevo Trabajo Extra
      </button>
      
      {trabajosExtra.map(te => (
        <div key={te.id} className="trabajo-extra-card">
          <h3>{te.nombre}</h3>
          <p>Estado: {te.estado}</p>
          <p>Total: ${te.totalPresupuestoConHonorarios?.toLocaleString()}</p>
          <button onClick={() => navigate(`/trabajo-extra/${te.id}`)}>
            Ver Detalle
          </button>
        </div>
      ))}
    </div>
  );
}
```

#### **PANTALLA 2: Crear Trabajo Extra**

```javascript
// Componente: CrearTrabajoExtra.jsx

function CrearTrabajoExtra({ obraId, empresaId }) {
  const [formData, setFormData] = useState({
    nombre: '',
    descripcion: '',
    // Configuración por defecto
    honorariosOtrosCostosActivo: true,
    honorariosOtrosCostosValor: 10.00,
    honorariosOtrosCostosTipo: 'porcentaje',
    mayoresCostosOtrosCostosActivo: true,
    mayoresCostosOtrosCostosValor: 5.00,
    mayoresCostosHonorariosActivo: true,
    mayoresCostosHonorariosValor: 5.00,
    itemsCalculadora: []
  });
  
  const agregarItem = () => {
    setFormData({
      ...formData,
      itemsCalculadora: [
        ...formData.itemsCalculadora,
        {
          tipoProfesional: 'Albanil',
          profesionales: [],
          materiales: [],
          jornales: [],
          gastosGenerales: []
        }
      ]
    });
  };
  
  const guardarTrabajoExtra = async () => {
    const response = await fetch('/api/presupuestos-no-cliente', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'empresaId': empresaId
      },
      body: JSON.stringify({
        ...formData,
        idEmpresa: empresaId,
        idObra: obraId,
        tipoPresupuesto: 'TRABAJOS_SEMANALES',
        estado: 'PENDIENTE'
      })
    });
    
    if (response.ok) {
      const trabajoExtra = await response.json();
      navigate(`/trabajo-extra/${trabajoExtra.id}`);
    }
  };
  
  return (
    <form>
      <input 
        placeholder="Nombre del trabajo extra"
        value={formData.nombre}
        onChange={e => setFormData({...formData, nombre: e.target.value})}
      />
      
      <textarea
        placeholder="Descripción"
        value={formData.descripcion}
        onChange={e => setFormData({...formData, descripcion: e.target.value})}
      />
      
      {/* Configuración de honorarios */}
      <ConfiguracionHonorarios 
        config={formData}
        onChange={setFormData}
      />
      
      {/* Items de calculadora */}
      <div>
        <h3>Items</h3>
        <button onClick={agregarItem}>+ Agregar Item</button>
        
        {formData.itemsCalculadora.map((item, index) => (
          <ItemCalculadora
            key={index}
            item={item}
            onChange={(updatedItem) => {
              const nuevosItems = [...formData.itemsCalculadora];
              nuevosItems[index] = updatedItem;
              setFormData({...formData, itemsCalculadora: nuevosItems});
            }}
          />
        ))}
      </div>
      
      <button onClick={guardarTrabajoExtra}>Guardar Trabajo Extra</button>
    </form>
  );
}
```

#### **PANTALLA 3: Asignar Gastos a Obra**

```javascript
// Componente: AsignarGastosObraModal.jsx

function AsignarGastosObraModal({ trabajoExtraId, empresaId }) {
  const [presupuesto, setPresupuesto] = useState(null);
  const [gastos, setGastos] = useState([]);
  const [asignaciones, setAsignaciones] = useState({});
  
  useEffect(() => {
    async function cargarDatos() {
      // 1. Obtener configuración de honorarios del trabajo extra
      const respPresupuesto = await fetch(
        `/api/presupuestos-no-cliente/${trabajoExtraId}?empresaId=${empresaId}`
      );
      const dataPresupuesto = await respPresupuesto.json();
      setPresupuesto(dataPresupuesto);
      
      // 2. Obtener gastos generales
      const respGastos = await fetch(
        `/api/presupuestos-no-cliente/${trabajoExtraId}/gastos-generales`,
        { headers: { 'empresaId': empresaId } }
      );
      const dataGastos = await respGastos.json();
      setGastos(dataGastos);
    }
    
    cargarDatos();
  }, [trabajoExtraId, empresaId]);
  
  // 3. Calcular total disponible de cada gasto
  const calcularDisponible = (gasto) => {
    const valorBase = gasto.importe; // Del backend
    
    const config = {
      honorarios: {
        otrosCostos: {
          activo: presupuesto?.honorariosOtrosCostosActivo,
          valor: presupuesto?.honorariosOtrosCostosValor,
          tipo: presupuesto?.honorariosOtrosCostosTipo
        }
      },
      mayoresCostos: {
        otrosCostos: {
          activo: presupuesto?.mayoresCostosOtrosCostosActivo,
          valor: presupuesto?.mayoresCostosOtrosCostosValor,
          tipo: 'porcentaje'
        },
        honorarios: {
          activo: presupuesto?.mayoresCostosHonorariosActivo,
          valor: presupuesto?.mayoresCostosHonorariosValor,
          tipo: 'porcentaje'
        }
      }
    };
    
    const resultado = calcularTotalGasto(
      valorBase,
      config.honorarios,
      config.mayoresCostos
    );
    
    return resultado.totalFinal;
  };
  
  const asignarGasto = async (gastoId, montoAsignado) => {
    await fetch('/api/obras/asignar-gasto', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'empresaId': empresaId
      },
      body: JSON.stringify({
        obraId: presupuesto.obraId,
        gastoId,
        montoAsignado,
        trabajoExtraId
      })
    });
  };
  
  return (
    <div>
      <h2>Asignar Gastos a la Obra</h2>
      
      {!presupuesto && <p>Cargando configuración...</p>}
      
      {gastos.map(gasto => {
        const disponible = presupuesto ? calcularDisponible(gasto) : 0;
        
        return (
          <div key={gasto.id} className="gasto-item">
            <h4>{gasto.descripcion}</h4>
            <p>Valor base: ${gasto.importe.toLocaleString()}</p>
            <p className="total-disponible">
              Total disponible: ${disponible.toLocaleString()}
              <span className="info-icon" title="Incluye honorarios y mayores costos">ℹ️</span>
            </p>
            
            <input
              type="number"
              placeholder="Monto a asignar"
              max={disponible}
              onChange={e => setAsignaciones({
                ...asignaciones,
                [gasto.id]: parseFloat(e.target.value)
              })}
            />
            
            <button onClick={() => asignarGasto(gasto.id, asignaciones[gasto.id])}>
              Asignar
            </button>
          </div>
        );
      })}
    </div>
  );
}
```

---

### 🔄 SECUENCIA DE LLAMADAS HTTP COMPLETA

#### **Escenario: Crear un Trabajo Extra con Gastos y Asignarlos**

```
1. Usuario abre "Crear Trabajo Extra"
   └─> GET /api/obras/10?empresaId=1  (obtener datos de la obra)

2. Usuario completa formulario y guarda
   └─> POST /api/presupuestos-no-cliente
       Headers: { empresaId: 1 }
       Body: { nombre, descripcion, itemsCalculadora: [...] }
       ← Response: { id: 123, totalPresupuestoConHonorarios: 437400 }

3. Usuario ve el trabajo extra creado
   └─> GET /api/presupuestos-no-cliente/123?empresaId=1
       ← Response: { ...configuración completa con honorarios... }

4. Usuario quiere asignar gastos a la obra
   ├─> GET /api/presupuestos-no-cliente/123?empresaId=1
   │   (para obtener configuración de honorarios)
   │
   └─> GET /api/presupuestos-no-cliente/123/gastos-generales
       Headers: { empresaId: 1 }
       ← Response: [{ id: 101, importe: 50000 }, ...]
       
   Frontend calcula: 50000 base → 57750 total disponible

5. Usuario asigna $30.000 del gasto a la obra
   └─> POST /api/obras/asignar-gasto
       Headers: { empresaId: 1 }
       Body: { 
         obraId: 10,
         gastoId: 101,
         montoAsignado: 30000,
         trabajoExtraId: 123
       }

6. Usuario registra un pago
   └─> POST /api/trabajos-extra/123/pagos
       Headers: { empresaId: 1 }
       Body: {
         monto: 218700,
         metodoPago: "transferencia",
         fechaPago: "2026-01-15"
       }

7. Usuario consulta estado de pagos
   └─> GET /api/trabajos-extra/123/pagos?empresaId=1
       ← Response: [{ monto: 218700, estado: "aprobado", ... }]
```

---

### ⚠️ PUNTOS CRÍTICOS PARA EL FRONTEND

1. **SIEMPRE obtener configuración de honorarios antes de mostrar totales**
   ```javascript
   // ❌ MAL - Solo consultar gastos
   const gastos = await fetch('/gastos-generales');
   // Total incorrecto: solo muestra base
   
   // ✅ BIEN - Primero obtener presupuesto completo
   const presupuesto = await fetch('/presupuestos-no-cliente/123');
   const gastos = await fetch('/gastos-generales');
   // Ahora puedes calcular totales correctos
   ```

2. **Incluir header `empresaId` en TODAS las peticiones**
   ```javascript
   fetch(url, {
     headers: {
       'empresaId': empresaId.toString(), // ← OBLIGATORIO
       'Content-Type': 'application/json'
     }
   });
   ```

3. **Validar disponible antes de asignar**
   ```javascript
   const disponible = calcularTotalGasto(base, config);
   if (montoAsignado > disponible) {
     alert('El monto supera el disponible');
     return;
   }
   ```

4. **Manejar valores null/undefined en configuración**
   ```javascript
   const valor = configuracion?.honorarios?.otrosCostos?.valor || 0;
   const activo = configuracion?.honorarios?.otrosCostos?.activo ?? false;
   ```

5. **Diferenciar trabajos extra de presupuestos normales**
   ```javascript
   const esTrabajoExtra = presupuesto.tipoPresupuesto === 'TRABAJOS_SEMANALES' 
                       || presupuesto.esTrabajoExtra === true;
   ```

---

### 📊 RESUMEN VISUAL DEL FLUJO

```
┌─────────────────────────────────────────────────────┐
│ FRONTEND: Crear Trabajo Extra                      │
└─────────────────────────────────────────────────────┘
                      │
                      ▼
         POST /api/presupuestos-no-cliente
         { nombre, itemsCalculadora, honorarios... }
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│ BACKEND: Guardar en Base de Datos                  │
│  ├─ trabajos_extra (principal)                     │
│  ├─ trabajos_extra_items_calculadora               │
│  ├─ trabajos_extra_gasto_general (subtotal=BASE)   │
│  └─ ...otras tablas relacionadas                   │
└─────────────────────────────────────────────────────┘
                      │
                      ▼
         Response: { id: 123, total: 437400 }
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│ FRONTEND: Ver Trabajo Extra                        │
│  GET /presupuestos-no-cliente/123                  │
│  ↓ Recibe configuración completa                   │
│  ↓ Muestra totales calculados                      │
└─────────────────────────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────┐
│ FRONTEND: Asignar Gastos a Obra                    │
│  1. GET /presupuestos-no-cliente/123               │
│     → Obtener config honorarios                    │
│  2. GET /gastos-generales                          │
│     → Obtener gastos (base)                        │
│  3. Calcular disponible (base + honorarios + MC)   │
│  4. POST /asignar-gasto                            │
│     → Crear asignación con monto                   │
└─────────────────────────────────────────────────────┘
```

---

Con esta guía, la IA del frontend debería entender perfectamente:
- ✅ Qué endpoints llamar y en qué orden
- ✅ Qué datos enviar y qué esperar recibir
- ✅ Cómo calcular los totales con honorarios
- ✅ Cómo manejar las asignaciones correctamente
- ✅ Qué headers y validaciones son necesarias
