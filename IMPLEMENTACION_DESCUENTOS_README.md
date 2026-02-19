# Implementación de Configuración de Descuentos - Backend

## Resumen de la Implementación

Se ha implementado exitosamente la funcionalidad de **Configuración de Descuentos** para el módulo de presupuestos no cliente, siguiendo las especificaciones proporcionadas por el frontend.

---

## Archivos Creados

### 1. DTOs (Data Transfer Objects)

#### DescuentoCategoriaDTO.java
- **Ubicación**: `src/main/java/com/rodrigo/construccion/dto/request/DescuentoCategoriaDTO.java`
- **Propósito**: Representa la configuración de descuento para una categoría individual (jornales, materiales, honorarios, mayores costos)
- **Propiedades**:
  - `activo` (Boolean): Indica si el descuento está activo
  - `tipo` (String): "porcentaje" o "fijo"
  - `valor` (BigDecimal): Valor del descuento

#### DescuentosConfigDTO.java  
- **Ubicación**: `src/main/java/com/rodrigo/construccion/dto/request/DescuentosConfigDTO.java`
- **Propósito**: Contiene la configuración completa de descuentos
- **Propiedades**:
  - `explicacion` (String): Texto justificativo visible para el cliente
  - `jornales` (DescuentoCategoriaDTO): Configuración de descuento para jornales
  - `materiales` (DescuentoCategoriaDTO): Configuración de descuento para materiales
  - `honorarios` (DescuentoCategoriaDTO): Configuración de descuento para honorarios
  - `mayoresCostos` (DescuentoCategoriaDTO): Configuración de descuento para mayores costos

### 2. Script SQL

#### agregar_descuentos_config_presupuestos.sql
- **Ubicación**: `migrations/agregar_descuentos_config_presupuestos.sql`
- **Propósito**: Script SQL manual para agregar la columna `descuentos_config` a la tabla `presupuesto_no_cliente`
- **Contenido**:
  - Verificación de existencia de la tabla
  - Creación de columna JSONB para descuentos
  - Comentarios de documentación
  - Índice GIN para búsquedas eficientes
  - Ejemplos de uso y consultas

---

## Archivos Modificados

### 1. Entidad PresupuestoNoCliente.java

**Cambios realizados**:

1. **Imports agregados**:
   ```java
   import com.fasterxml.jackson.core.JsonProcessingException;
   import com.fasterxml.jackson.databind.ObjectMapper;
   import com.rodrigo.construccion.dto.request.DescuentosConfigDTO;
   import com.rodrigo.construccion.dto.request.DescuentoCategoriaDTO;
   ```

2. **Nuevo campo JSON**:
   ```java
   @Column(name = "descuentos_config", columnDefinition = "jsonb")
   private String descuentosConfig;
   ```

3. **Campos transient para cálculos**:
   ```java
   @Transient
   private BigDecimal totalDescuentos;
   
   @Transient
   private BigDecimal totalSinDescuentos;
   ```

4. **Nuevos métodos**:
   - `parsearDescuentosConfig()`: Parsea el JSON de descuentos a DTO
   - `calcularDescuentoCategoria()`: Calcula el descuento de una categoría individual
   - `calcularTotalDescuentos()`: Calcula el total de todos los descuentos
   - `validarDescuentos()`: Valida que la configuración de descuentos sea correcta
   - `validarCategoria()`: Valida una categoría individual de descuento

5. **Método modificado**:
   - `calcularCamposCalculados()`: Ahora incluye el cálculo de descuentos en el flujo de cálculo de totales

### 2. PresupuestoNoClienteRequestDTO.java

**Cambios realizados**:

Agregado campo para recibir descuentos desde el frontend:
```java
@Schema(description = "Configuración de descuentos en formato JSON...")
private String descuentosConfig;
```

### 3. PresupuestoNoClienteService.java

**Cambios realizados**:

1. **En método `crear()`**:
   - Agregado manejo y validación de `descuentosConfig`
   - Validación automática antes de guardar

2. **En método `mapearDtoAPresupuesto()`** (3 ubicaciones):
   - Agregado mapeo de `descuentosConfig` desde DTO a entidad
   - Validación de descuentos antes de guardar
   - Limpieza del campo si no viene en el DTO

3. **En método `crearNuevaVersion()`**:
   - Agregado copia del campo `descuentosConfig` a la nueva versión

---

## Comportamiento de Cálculo

### Orden de Cálculo

1. **Base**: Se calcula el total base del presupuesto (jornales + profesionales + materiales + otros costos)
2. **Honorarios**: Se calculan sobre la BASE de cada categoría
3. **Mayores Costos**: Se calculan sobre la BASE de cada categoría (NO sobre base + honorarios)
4. **Subtotal sin Descuentos**: BASE + HONORARIOS + MAYORES COSTOS
5. **Descuentos**: Se calculan sobre diferentes bases según la categoría:
   - Descuento Jornales → sobre subtotal de jornales SIN honorarios
   - Descuento Materiales → sobre subtotal de materiales SIN honorarios
   - Descuento Honorarios → sobre total de honorarios ya calculados
   - Descuento Mayores Costos → sobre total de mayores costos ya calculados
6. **Total Final**: SUBTOTAL SIN DESCUENTOS - TOTAL DESCUENTOS

### Bases de Cálculo

| Categoría | Base sobre la cual se aplica el descuento |
|-----------|-------------------------------------------|
| Jornales | Subtotal de jornales SIN honorarios |
| Materiales | Subtotal de materiales SIN honorarios |
| Honorarios | Total de honorarios calculados del presupuesto |
| Mayores Costos | Total de mayores costos calculados del presupuesto |

---

## Validaciones Implementadas

### Validaciones Automáticas

1. **Valor positivo**: Los valores de descuento no pueden ser negativos
2. **Porcentaje válido**: Los porcentajes deben estar entre 0 y 100
3. **Valor fijo válido**: Los valores fijos no deben exceder la base correspondiente
4. **Total no negativo**: El total de descuentos no puede exceder el subtotal sin descuentos (el total final no puede ser negativo)

### Manejo de Errores

Si alguna validación falla, se lanza una `IllegalArgumentException` con un mensaje descriptivo antes de guardar el presupuesto.

---

## Persistencia

### Campo JSON

El campo `descuentos_config` se almacena como **JSONB** en PostgreSQL, lo que permite:
- Almacenamiento eficiente
- Indexación con GIN para búsquedas rápidas
- Consultas directas sobre el JSON
- Flexibilidad para extender la estructura sin cambios de esquema

### Ejemplo de JSON almacenado:

```json
{
  "explicacion": "Descuento por cliente frecuente",
  "jornales": {
    "activo": true,
    "tipo": "porcentaje",
    "valor": 10.0
  },
  "materiales": {
    "activo": true,
    "tipo": "fijo",
    "valor": 5000.00
  },
  "honorarios": {
    "activo": true,
    "tipo": "porcentaje",
    "valor": 5.0
  },
  "mayoresCostos": {
    "activo": false,
    "tipo": "porcentaje",
    "valor": 0.0
  }
}
```

---

## Versionamiento

Los cambios en la configuración de descuentos **generan una nueva versión del presupuesto**, igual que ocurre con honorarios y mayores costos, ya que modifican el monto total del presupuesto.

---

## Instrucciones de Despliegue

### 1. Backup de la Base de Datos

**IMPORTANTE**: Ejecutar un backup ANTES de aplicar cualquier cambio:

```powershell
.\backup-bd-construccion-app.ps1
```

### 2. Ejecutar Script SQL

Conectarse a la base de datos y ejecutar:

```bash
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f "migrations/agregar_descuentos_config_presupuestos.sql"
```

O ejecutar manualmente las sentencias SQL desde un cliente de PostgreSQL.

### 3. Compilar y Ejecutar Backend

```bash
./mvnw clean install
./mvnw spring-boot:run
```

### 4. Verificación

Verificar que el campo fue creado correctamente:

```sql
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'presupuesto_no_cliente'
  AND column_name = 'descuentos_config';
```

Debe retornar:
```
descuentos_config | jsonb | YES
```

---

## Rollback

Si algo sale mal, restaurar desde el backup:

```bash
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f ".\db-backups\backup_construccion_app_v3_FECHA.sql"
```

---

## Compatibilidad

- La funcionalidad es **totalmente retrocompatible**
- Presupuestos existentes funcionarán normalmente (el campo `descuentos_config` es nullable)
- Si no hay descuentos configurados, no se aplican descuentos al total

---

## Testing

### Probar Funcionalidad

1. **Crear presupuesto con descuentos**:
   - Enviar `descuentosConfig` en el request
   - Verificar que se guarda en la BD
   - Verificar que se calculan correctamente los totales

2. **Actualizar presupuesto con descuentos**:
   - Modificar `descuentosConfig`
   - Verificar que se genera nueva versión (si está aprobado)
   - Verificar que se recalculan los totales

3. **Validaciones**:
   - Intentar crear descuento con porcentaje > 100 (debe fallar)
   - Intentar crear descuento con valor fijo > base (debe fallar)
   - Intentar crear descuentos que excedan el total (debe fallar)

4. **Casos edge**:
   - Presupuesto sin descuentos (debe funcionar normal)
   - Descuentos con todas las categorías desactivadas (no debe aplicar descuentos)
   - Descuentos con valores 0 (no debe aplicar descuentos)

---

## Documentación API para Frontend

### IMPORTANTE: Formato del Campo `descuentosConfig`

El campo `descuentosConfig` debe enviarse como **STRING JSON escapado**, NO como objeto.

### Endpoints

#### POST /api/v1/presupuestos-no-cliente
**Crear nuevo presupuesto con descuentos**

#### PUT /api/v1/presupuestos-no-cliente/{id}
**Actualizar presupuesto existente con descuentos**

---

### Request Structure

**Nombre del campo**: `descuentosConfig` (camelCase)

**Tipo**: `String` (JSON serializado)

**Ubicación**: Dentro del objeto principal del request body

**Ejemplo de Request Completo**:

```json
{
  "idEmpresa": 1,
  "nombreObra": "Casa Moderna",
  "idProyecto": 5,
  "descuentosConfig": "{\"explicacion\":\"Descuento por cliente frecuente\",\"jornales\":{\"activo\":true,\"tipo\":\"porcentaje\",\"valor\":10.0},\"materiales\":{\"activo\":true,\"tipo\":\"fijo\",\"valor\":5000.00},\"honorarios\":{\"activo\":true,\"tipo\":\"porcentaje\",\"valor\":5.0},\"mayoresCostos\":{\"activo\":false,\"tipo\":\"porcentaje\",\"valor\":0.0}}"
}
```

**Estructura del JSON dentro de `descuentosConfig`** (antes de serializar):

```json
{
  "explicacion": "Texto justificativo del descuento",
  "jornales": {
    "activo": true,
    "tipo": "porcentaje",
    "valor": 10.0
  },
  "materiales": {
    "activo": false,
    "tipo": "fijo",
    "valor": 0.0
  },
  "honorarios": {
    "activo": true,
    "tipo": "porcentaje",
    "valor": 5.0
  },
  "mayoresCostos": {
    "activo": false,
    "tipo": "porcentaje",
    "valor": 0.0
  }
}
```

---

### Propiedades de Configuración de Descuento

#### Objeto Principal: `DescuentosConfigDTO`

| Campo | Tipo | Requerido | Descripción |
|-------|------|-----------|-------------|
| `explicacion` | String | Sí | Texto justificativo del descuento (visible en PDF) |
| `jornales` | DescuentoCategoriaDTO | Sí | Configuración de descuento para jornales |
| `materiales` | DescuentoCategoriaDTO | Sí | Configuración de descuento para materiales |
| `honorarios` | DescuentoCategoriaDTO | Sí | Configuración de descuento para honorarios |
| `mayoresCostos` | DescuentoCategoriaDTO | Sí | Configuración de descuento para mayores costos |

#### Objeto Categoría: `DescuentoCategoriaDTO`

| Campo | Tipo | Requerido | Descripción | Validación |
|-------|------|-----------|-------------|------------|
| `activo` | Boolean | Sí | Si el descuento está activo | true/false |
| `tipo` | String | Sí | Tipo de descuento | "porcentaje" o "fijo" |
| `valor` | Number | Sí | Valor del descuento | ≥ 0 |

**Validaciones de `valor`**:
- Si `tipo` es "porcentaje": debe estar entre 0 y 100
- Si `tipo` es "fijo": debe ser ≥ 0 y no exceder la base correspondiente

---

### Response Structure

El backend retorna el presupuesto completo con **campos calculados adicionales**:

**Campos Calculados Relacionados con Descuentos**:

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `totalDescuentos` | Number | Total de todos los descuentos aplicados |
| `totalSinDescuentos` | Number | Subtotal antes de aplicar descuentos (base + honorarios + mayores costos) |
| `total` | Number | Total final DESPUÉS de aplicar descuentos |
| `descuentosConfig` | String | Configuración de descuentos (JSON serializado) |

**Ejemplo de Response**:

```json
{
  "id": 123,
  "idEmpresa": 1,
  "nombreObra": "Casa Moderna",
  "estado": "BORRADOR",
  "version": 1,
  
  "totalJornales": 100000.00,
  "totalMateriales": 50000.00,
  "totalProfesionales": 30000.00,
  "totalOtrosCostos": 20000.00,
  
  "totalHonorarios": 20000.00,
  "totalMayoresCostos": 10000.00,
  
  "totalSinDescuentos": 230000.00,
  "totalDescuentos": 15000.00,
  "total": 215000.00,
  
  "descuentosConfig": "{\"explicacion\":\"Descuento por cliente frecuente\",\"jornales\":{\"activo\":true,\"tipo\":\"porcentaje\",\"valor\":10.0},\"materiales\":{\"activo\":true,\"tipo\":\"fijo\",\"valor\":5000.00},\"honorarios\":{\"activo\":true,\"tipo\":\"porcentaje\",\"valor\":5.0},\"mayoresCostos\":{\"activo\":false,\"tipo\":\"porcentaje\",\"valor\":0.0}}",
  
  "fechaCreacion": "2026-02-19T14:30:00",
  "fechaModificacion": "2026-02-19T14:30:00"
}
```

---

### Cálculo de Totales

El backend calcula automáticamente:

1. **totalSinDescuentos** = totalJornales + totalMateriales + totalProfesionales + totalOtrosCostos + totalHonorarios + totalMayoresCostos

2. **totalDescuentos** = suma de todos los descuentos activos, donde cada descuento se calcula según su categoría:
   - Descuento Jornales → aplica sobre `totalJornales` (SIN honorarios)
   - Descuento Materiales → aplica sobre `totalMateriales` (SIN honorarios)
   - Descuento Honorarios → aplica sobre `totalHonorarios`
   - Descuento Mayores Costos → aplica sobre `totalMayoresCostos`

3. **total** = totalSinDescuentos - totalDescuentos

---

### Ejemplos de Uso desde Frontend

#### Ejemplo 1: Enviar Descuento por Porcentaje en Jornales

```javascript
const descuentosConfig = {
  explicacion: "Descuento 10% por volumen de obra",
  jornales: {
    activo: true,
    tipo: "porcentaje",
    valor: 10.0
  },
  materiales: {
    activo: false,
    tipo: "porcentaje",
    valor: 0.0
  },
  honorarios: {
    activo: false,
    tipo: "porcentaje",
    valor: 0.0
  },
  mayoresCostos: {
    activo: false,
    tipo: "porcentaje",
    valor: 0.0
  }
};

const request = {
  idEmpresa: 1,
  nombreObra: "Obra Ejemplo",
  // ... otros campos del presupuesto
  descuentosConfig: JSON.stringify(descuentosConfig)
};

// Enviar con fetch/axios
await fetch('/api/v1/presupuestos-no-cliente', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(request)
});
```

#### Ejemplo 2: Enviar Múltiples Descuentos

```javascript
const descuentosConfig = {
  explicacion: "Descuentos especiales - Cliente VIP",
  jornales: {
    activo: true,
    tipo: "porcentaje",
    valor: 15.0
  },
  materiales: {
    activo: true,
    tipo: "fijo",
    valor: 8000.00
  },
  honorarios: {
    activo: true,
    tipo: "porcentaje",
    valor: 5.0
  },
  mayoresCostos: {
    activo: false,
    tipo: "porcentaje",
    valor: 0.0
  }
};

const request = {
  idEmpresa: 1,
  nombreObra: "Obra VIP",
  descuentosConfig: JSON.stringify(descuentosConfig)
};
```

#### Ejemplo 3: Sin Descuentos (Opcional)

Si NO hay descuentos, puedes:
- **Opción 1**: No enviar el campo `descuentosConfig`
- **Opción 2**: Enviar `null`
- **Opción 3**: Enviar todas las categorías con `activo: false`

```javascript
const request = {
  idEmpresa: 1,
  nombreObra: "Obra sin descuentos"
  // descuentosConfig no se incluye
};
```

---

### Manejo de Errores

El backend retorna errores de validación con status **400 Bad Request** y mensaje descriptivo:

**Errores Comunes**:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El porcentaje de descuento no puede ser mayor a 100"
}
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El valor fijo del descuento no puede exceder la base correspondiente"
}
```

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "El total de descuentos no puede exceder el subtotal del presupuesto"
}
```

**Validaciones que Debes Manejar en Frontend**:

1. ✅ Si `tipo` es "porcentaje", `valor` debe estar entre 0 y 100
2. ✅ Si `tipo` es "fijo", `valor` debe ser ≥ 0
3. ✅ `activo` debe ser booleano (true/false)
4. ✅ `tipo` debe ser exactamente "porcentaje" o "fijo"
5. ✅ Todas las categorías deben estar presentes (jornales, materiales, honorarios, mayoresCostos)

---

### Resumen para IA del Frontend

**Campo a enviar**: `descuentosConfig`  
**Tipo**: String (JSON serializado)  
**Ubicación**: Raíz del request body  
**Requerido**: No (nullable)

**Pasos para construir el request**:
1. Crear objeto JavaScript con la estructura de descuentos
2. Serializar con `JSON.stringify()`
3. Incluir en el request body como campo `descuentosConfig`

**Pasos para procesar el response**:
1. Recibir el presupuesto completo
2. Parsear `descuentosConfig` con `JSON.parse()` si necesitas manipularlo
3. Usar `totalDescuentos`, `totalSinDescuentos`, y `total` para mostrar en UI
4. Mostrar `explicacion` en el PDF del presupuesto

---

## Notas Finales

✅ **Implementación Completa**: Todos los componentes están implementados según las especificaciones
✅ **Validaciones**: Se incluyen todas las validaciones requeridas
✅ **Retrocompatibilidad**: No afecta presupuestos existentes
✅ **Script SQL Listo**: Incluye ejemplos y documentación completa
✅ **Sin Migraciones Automáticas**: Se proporciona script SQL manual como solicitado

---

## Soporte

Para cualquier duda o problema con la implementación:
1. Revisar los logs de Spring Boot para mensajes de error
2. Verificar que el campo JSON se creó correctamente en la BD
3. Verificar que el frontend está enviando el JSON en el formato correcto
4. Consultar el script SQL para ejemplos de uso y consultas de diagnóstico
