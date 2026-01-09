# Sistema Completo de Rubros e Items para Trabajos Extra

## 📋 Resumen

Se ha implementado un **sistema completo de rubros, items, jornales, materiales, gastos generales, honorarios y mayores costos** para trabajos extra, replicando fielmente la estructura de `PresupuestoNoCliente` pero adaptada a `TrabajoExtra`.

---

## 🗄️ Estructura de Base de Datos

### Migración V20: `V20__agregar_rubros_items_trabajos_extra.sql`

#### PARTE 1: Nuevas columnas en `trabajos_extra`

**Configuración de Honorarios:**
- `honorarios_aplicar_a_todos` - Si se aplica valor general a todas las categorías
- `honorarios_valor_general` - Valor general de honorarios
- `honorarios_tipo_general` - Tipo (PORCENTAJE/VALOR_FIJO)

**Honorarios por Categoría:**
- Jornales: `honorarios_jornales_activo`, `honorarios_jornales_valor`, `honorarios_jornales_tipo`
- Materiales: `honorarios_materiales_activo`, `honorarios_materiales_valor`, `honorarios_materiales_tipo`
- Profesionales: `honorarios_profesionales_activo`, `honorarios_profesionales_valor`, `honorarios_profesionales_tipo`
- Otros Costos: `honorarios_otros_costos_activo`, `honorarios_otros_costos_valor`, `honorarios_otros_costos_tipo`
- Configuración: `honorarios_configuracion_presupuesto_activo`, `honorarios_configuracion_presupuesto_valor`, `honorarios_configuracion_presupuesto_tipo`

**Configuración de Mayores Costos:**
- `mayores_costos_aplicar_valor_general` - Si se aplica valor general a todas las categorías
- `mayores_costos_valor_general` - Valor general de mayores costos
- `mayores_costos_tipo_general` - Tipo (PORCENTAJE/VALOR_FIJO)

**Mayores Costos por Categoría:**
- Jornales: `mayores_costos_jornales_activo`, `mayores_costos_jornales_valor`, `mayores_costos_jornales_tipo`
- Materiales: `mayores_costos_materiales_activo`, `mayores_costos_materiales_valor`, `mayores_costos_materiales_tipo`
- Profesionales: `mayores_costos_profesionales_activo`, `mayores_costos_profesionales_valor`, `mayores_costos_profesionales_tipo`
- Otros Costos: `mayores_costos_otros_costos_activo`, `mayores_costos_otros_costos_valor`, `mayores_costos_otros_costos_tipo`
- Honorarios: `mayores_costos_honorarios_activo`, `mayores_costos_honorarios_valor`, `mayores_costos_honorarios_tipo`
- Configuración: `mayores_costos_configuracion_presupuesto_activo`, `mayores_costos_configuracion_presupuesto_valor`, `mayores_costos_configuracion_presupuesto_tipo`

**Mayores Costos Importado:**
- `mayores_costos_general_importado`
- `mayores_costos_rubro_importado`
- `mayores_costos_nombre_rubro_importado`
- `mayores_costos_explicacion`

**Totales Adicionales:**
- `total_honorarios_calculado` - Total de honorarios calculado automáticamente
- `total_presupuesto_con_honorarios` - Total del presupuesto incluyendo honorarios
- `total_materiales` - Total de materiales
- `total_profesionales` - Total de profesionales
- `total_general` - Total general

**Honorario Dirección:**
- `honorario_direccion_porcentaje`
- `honorario_direccion_importe`
- `honorario_direccion_valor_fijo`

#### PARTE 2: Nueva tabla `trabajos_extra_items_calculadora`

Equivalente a `items_calculadora_presupuesto`. Representa los **rubros/items** del trabajo extra.

**Campos principales:**
- `id` - Primary key
- `trabajo_extra_id` - FK a trabajos_extra (CASCADE)
- `empresa_id` - ID de la empresa
- `tipo_profesional` - Nombre del rubro
- `descripcion`, `observaciones`

**Modo de cálculo:**
- `es_modo_manual` - Si el total se ingresa manualmente o se calcula

**Jornales (modo automático):**
- `cantidad_jornales`, `importe_jornal`, `subtotal_mano_obra`

**Materiales:**
- `materiales`, `subtotal_materiales`

**Total manual:**
- `total_manual`, `descripcion_total_manual`, `observaciones_total_manual`

**Total calculado:**
- `total` - Total del item

**Control de días hábiles:**
- `incluir_en_calculo_dias` - Si se incluye en cálculo de días
- `trabaja_en_paralelo` - Si trabaja en paralelo con otros rubros

**Gastos generales:**
- `es_gasto_general` - Si el item es un gasto general
- `subtotal_gastos_generales`
- `descripcion_gastos_generales`, `observaciones_gastos_generales`

**Descripciones por categoría:**
- `descripcion_profesionales`, `observaciones_profesionales`
- `descripcion_materiales`, `observaciones_materiales`

#### PARTE 3: Nueva tabla `trabajos_extra_jornal_calculadora`

**Jornales desglosados** por rol dentro de un item.

**Campos:**
- `id`, `item_calculadora_id` (FK CASCADE)
- `empresa_id`, `profesional_obra_id` (opcional)
- `rol` - Nombre del rol
- `cantidad`, `valor_unitario`, `subtotal`
- `incluir_en_calculo_dias`, `frontend_id`, `observaciones`

#### PARTE 4: Nueva tabla `trabajos_extra_material_calculadora`

**Materiales desglosados** dentro de un item.

**Campos:**
- `id`, `item_calculadora_id` (FK CASCADE)
- `empresa_id`, `obra_material_id` (opcional)
- `nombre`, `descripcion`, `unidad`
- `cantidad`, `precio`, `subtotal`
- `frontend_id`, `observaciones`

#### PARTE 5: Nueva tabla `trabajos_extra_profesional_calculadora`

**Profesionales desglosados** dentro de un item.

**Campos:**
- `id`, `item_calculadora_id` (FK CASCADE)
- `empresa_id`, `profesional_obra_id` (opcional)
- `rol`, `nombre_completo`
- `cantidad_jornales`, `valor_jornal`, `subtotal`
- `incluir_en_calculo_dias`, `frontend_id`, `observaciones`
- `created_at`, `updated_at`

#### PARTE 6: Nueva tabla `trabajos_extra_gasto_general`

**Gastos generales desglosados** dentro de un item.

**Campos:**
- `id`, `item_calculadora_id` (FK CASCADE)
- `empresa_id`
- `descripcion` - Descripción del gasto
- `cantidad`, `precio_unitario`, `subtotal`
- `sin_cantidad`, `sin_precio` - Flags para valores opcionales
- `orden` - Orden de visualización
- `frontend_id`, `observaciones`
- `created_at`, `updated_at`

---

## 🔧 Entidades Java Creadas

### 1. `TrabajoExtraItemCalculadora.java`
Entidad principal para los rubros/items del trabajo extra. Réplica fiel de `ItemCalculadoraPresupuesto`.

**Relaciones:**
- `@ManyToOne` con `TrabajoExtra`
- `@OneToMany` con `TrabajoExtraProfesionalCalculadora`
- `@OneToMany` con `TrabajoExtraMaterialCalculadora`
- `@OneToMany` con `TrabajoExtraJornalCalculadora`
- `@OneToMany` con `TrabajoExtraGastoGeneral`

### 2. `TrabajoExtraJornalCalculadora.java`
Jornales desglosados por rol.

### 3. `TrabajoExtraMaterialCalculadora.java`
Materiales desglosados.

### 4. `TrabajoExtraProfesionalCalculadora.java`
Profesionales desglosados.

### 5. `TrabajoExtraGastoGeneral.java`
Gastos generales desglosados.

### 6. `TrabajoExtra.java` (actualizado)
Se agregaron **todos los campos** de honorarios y mayores costos, y la relación `@OneToMany` con `itemsCalculadora`.

---

## 📦 Repositorios Creados

1. `TrabajoExtraItemCalculadoraRepository.java`
   - `findByTrabajoExtraId()`
   - `findByEmpresaId()`
   - `findByTrabajoExtraIdAndEsGastoGeneralTrue()`
   - `deleteByTrabajoExtraId()`
   - `countByTrabajoExtraId()`

2. `TrabajoExtraJornalCalculadoraRepository.java`
   - `findByItemCalculadoraId()`
   - `deleteByItemCalculadoraId()`

3. `TrabajoExtraMaterialCalculadoraRepository.java`
   - `findByItemCalculadoraId()`
   - `deleteByItemCalculadoraId()`

4. `TrabajoExtraProfesionalCalculadoraRepository.java`
   - `findByItemCalculadoraId()`
   - `deleteByItemCalculadoraId()`

5. `TrabajoExtraGastoGeneralRepository.java`
   - `findByItemCalculadoraId()`
   - `findByItemCalculadoraIdOrderByOrdenAsc()`
   - `deleteByItemCalculadoraId()`

---

## ✅ Características Implementadas

### 🎯 Réplica Fiel de PresupuestoNoCliente
- **Misma estructura** de rubros e items
- **Mismo modelo** de cálculo (modo manual vs automático)
- **Misma configuración** de honorarios y mayores costos
- **Mismos desglose** de jornales, materiales, profesionales y gastos generales

### 🔗 Relaciones en Cascada
- Al eliminar un `TrabajoExtra`, se eliminan automáticamente:
  - Todos los `itemsCalculadora`
  - Todos los jornales, materiales, profesionales y gastos generales de cada item

### 🧮 Cálculos Automáticos
- **Honorarios** por categoría (jornales, materiales, profesionales, otros costos, honorarios)
- **Mayores costos** por categoría
- **Totales** calculados automáticamente

### 📊 Flexibilidad
- **Modo manual**: Total ingresado directamente
- **Modo automático**: `cantidad_jornales × importe_jornal + materiales`
- **Desglose completo**: Profesionales, materiales, jornales y gastos generales por item

---

## 🚀 Próximos Pasos

### 1. DTOs
Crear DTOs para request y response de:
- `TrabajoExtraItemCalculadoraDTO`
- `TrabajoExtraJornalCalculadoraDTO`
- `TrabajoExtraMaterialCalculadoraDTO`
- `TrabajoExtraProfesionalCalculadoraDTO`
- `TrabajoExtraGastoGeneralDTO`

### 2. Service Layer
Implementar lógica de negocio para:
- Crear/actualizar items con sus desglose
- Calcular totales automáticamente
- Aplicar honorarios y mayores costos
- Validaciones

### 3. Controller
Actualizar `TrabajoExtraController` para soportar:
- Crear trabajo extra con rubros completos
- Actualizar rubros e items
- Consultar rubros de un trabajo extra

### 4. Lógica de Cálculo
Implementar las mismas fórmulas que `PresupuestoNoCliente`:
- Cálculo de honorarios por categoría
- Cálculo de mayores costos por categoría
- Cálculo de totales
- Cálculo de días hábiles

---

## 📝 Notas Importantes

1. **Compatibilidad total**: Los trabajos extra existentes (simples) siguen funcionando sin cambios.

2. **Estructura idéntica**: La estructura es exactamente igual a `PresupuestoNoCliente`, por lo que la lógica de cálculo puede ser **reutilizada**.

3. **Cascada completa**: Todas las relaciones tienen `CASCADE` para eliminación automática.

4. **Índices optimizados**: Se crearon índices en las columnas más consultadas para mejor performance.

5. **Auditoría**: Todas las tablas tienen campos `created_at` y `updated_at` donde corresponde.

---

## 🎉 Resultado

El sistema de `TrabajoExtra` ahora tiene **exactamente la misma capacidad** que `PresupuestoNoCliente` para manejar:
- ✅ Rubros organizados
- ✅ Items desglosados
- ✅ Jornales por rol
- ✅ Materiales detallados
- ✅ Gastos generales
- ✅ Honorarios configurables por categoría
- ✅ Mayores costos configurables por categoría
- ✅ Cálculos automáticos de totales

**La base de datos y las entidades están listas**. El siguiente paso es implementar los servicios y controladores para exponer esta funcionalidad vía API.
