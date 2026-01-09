# Trabajos Extra - Presupuesto Completo

## ✅ Cambios Implementados

### Resumen
Se ha ajustado el endpoint de trabajos extra para soportar datos de presupuesto completo, incluyendo profesionales, tareas y días asociados. El sistema es **completamente compatible** con trabajos extra simples existentes.

---

## 📋 Estructura de Datos

### Endpoint Principal
**POST** `/api/trabajos-extra`
- Acepta `empresaId` tanto en **header** como en **query parameter** (`?empresaId=X`)

### Request JSON
```json
{
  "obraId": 8,
  "nombre": "Casa de Cacho Pileta",
  "observaciones": "Descripción del trabajo extra",
  "dias": ["2025-12-03", "2025-12-04"],
  "profesionales": [
    {
      "tipo": "MANUAL",
      "nombre": "Nombre del profesional",
      "especialidad": "Albañil",
      "importe": 50000
    }
  ],
  "tareas": [
    {
      "descripcion": "Descripción de la tarea",
      "estado": "A_TERMINAR",
      "importe": 100000,
      "profesionalesAsignados": []
    }
  ]
}
```

### Response JSON
```json
{
  "id": 123,
  "obraId": 8,
  "clienteId": 45,
  "empresaId": 1,
  "nombre": "Casa de Cacho Pileta",
  "observaciones": "Descripción del trabajo extra",
  "dias": ["2025-12-03", "2025-12-04"],
  "profesionales": [
    {
      "id": 456,
      "profesionalId": null,
      "tipo": "MANUAL",
      "nombre": "Nombre del profesional",
      "especialidad": "Albañil",
      "importe": 50000.00
    }
  ],
  "tareas": [
    {
      "id": 789,
      "descripcion": "Descripción de la tarea",
      "estado": "A_TERMINAR",
      "importe": 100000.00,
      "profesionalesAsignados": []
    }
  ],
  "fechaCreacion": "2026-01-08T03:45:00",
  "fechaModificacion": "2026-01-08T03:45:00"
}
```

---

## 🗄️ Tablas de Base de Datos

### 1. `trabajos_extra` (Principal)
```sql
- id
- cliente_id
- empresa_id
- obra_id
- nombre
- observaciones
- estado_pago_general
- created_at
- updated_at
```

### 2. `trabajos_extra_profesionales`
```sql
- id
- trabajo_extra_id (FK a trabajos_extra con CASCADE)
- profesional_id (nullable)
- nombre
- especialidad
- tipo (ASIGNADO_OBRA | LISTADO_GENERAL | MANUAL)
- importe
- estado_pago
```

### 3. `trabajos_extra_tareas`
```sql
- id
- trabajo_extra_id (FK a trabajos_extra con CASCADE)
- descripcion
- estado (TERMINADA | A_TERMINAR | POSTERGADA | SUSPENDIDA)
- importe
- estado_pago
```

### 4. `trabajos_extra_dias`
```sql
- id
- trabajo_extra_id (FK a trabajos_extra con CASCADE)
- fecha
```

### 5. `trabajos_extra_tareas_profesionales`
```sql
- tarea_id (FK a trabajos_extra_tareas)
- profesional_index
```

---

## 🔧 Archivos Modificados

### 1. DTOs de Request
- ✅ `TrabajoExtraRequestDTO.java` - Ya existente
- ✅ `TrabajoExtraProfesionalDTO.java` - Ya existente  
- ✅ `TrabajoExtraTareaDTO.java` - **Actualizado** con `@JsonAlias` para aceptar `profesionalesAsignados` o `profesionalesIndices`

### 2. DTOs de Response (Nuevos)
- ✅ `TrabajoExtraResponseDTO.java` - **Actualizado** para usar DTOs específicos de respuesta
- ✅ `TrabajoExtraProfesionalResponseDTO.java` - **NUEVO** - Incluye `id` generado
- ✅ `TrabajoExtraTareaResponseDTO.java` - **NUEVO** - Incluye `id` generado y `profesionalesAsignados`

### 3. Controller
- ✅ `TrabajoExtraController.java` - **Actualizado** para aceptar `empresaId` en header o query param

### 4. Service
- ✅ `TrabajoExtraService.java` - **Actualizado** para mapear a DTOs de respuesta con IDs

### 5. Entities (Ya existentes)
- ✅ `TrabajoExtra.java`
- ✅ `TrabajoExtroProfesional.java`
- ✅ `TrabajoExtraTarea.java`
- ✅ `TrabajoExtraDia.java`

### 6. Repositories (Ya existentes)
- ✅ `TrabajoExtraRepository.java`
- ✅ `TrabajoExtroProfesionalRepository.java`
- ✅ `TrabajoExtraTareaRepository.java`
- ✅ `TrabajoExtraDiaRepository.java`

---

## ⚙️ Características Clave

### ✅ Compatibilidad Total
- Los campos `dias`, `profesionales` y `tareas` son **opcionales**
- Trabajos extra simples (solo nombre y observaciones) siguen funcionando
- No hay cambios en la base de datos (ya estaban todas las tablas)

### ✅ Cascada de Eliminación
- Al eliminar un trabajo extra, se eliminan automáticamente:
  - Todos los días asociados
  - Todos los profesionales asociados
  - Todas las tareas asociadas
  - Todas las asignaciones de profesionales a tareas

### ✅ Flexibilidad en empresaId
- Se puede enviar en el **header**: `empresaId: 1`
- O como **query param**: `?empresaId=1`

### ✅ Response Completo
- La respuesta incluye los **IDs generados** para cada entidad
- El campo `profesionalesAsignados` en tareas se devuelve correctamente
- Incluye fechas de creación y modificación

---

## 📝 Ejemplo de Uso

### Crear Trabajo Extra Completo
```bash
POST /api/trabajos-extra?empresaId=1
Content-Type: application/json

{
  "obraId": 8,
  "nombre": "Instalación Eléctrica Extra",
  "observaciones": "Trabajo urgente",
  "dias": ["2026-01-10", "2026-01-11"],
  "profesionales": [
    {
      "tipo": "MANUAL",
      "nombre": "Juan Electricista",
      "especialidad": "Electricista",
      "importe": 75000
    }
  ],
  "tareas": [
    {
      "descripcion": "Instalar cableado planta baja",
      "estado": "A_TERMINAR",
      "importe": 150000,
      "profesionalesAsignados": [0]
    }
  ]
}
```

### Crear Trabajo Extra Simple
```bash
POST /api/trabajos-extra?empresaId=1
Content-Type: application/json

{
  "obraId": 8,
  "nombre": "Reparación menor",
  "observaciones": "Arreglo de puerta"
}
```

---

## ✅ Validaciones

- ✅ `obraId` es obligatorio
- ✅ `nombre` es obligatorio
- ✅ `empresaId` es obligatorio (header o param)
- ✅ El `clienteId` se obtiene automáticamente de la obra
- ✅ La obra debe pertenecer a la empresa
- ✅ Los estados de tarea válidos: `TERMINADA`, `A_TERMINAR`, `POSTERGADA`, `SUSPENDIDA`
- ✅ Los tipos de profesional válidos: `ASIGNADO_OBRA`, `LISTADO_GENERAL`, `MANUAL`

---

## 🎯 Resultado

El sistema ahora soporta trabajos extra con presupuesto completo, manteniendo 100% de compatibilidad con trabajos extra simples existentes. Las respuestas incluyen todos los IDs generados y el formato es exactamente el que espera el frontend.
