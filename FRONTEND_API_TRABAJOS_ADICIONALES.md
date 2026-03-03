# 📋 API TRABAJOS ADICIONALES - DOCUMENTACIÓN PARA FRONTEND

## 🎯 Información General

**Base URL**: `http://localhost:8080/api/trabajos-adicionales`  
**Formato**: JSON  
**Charset**: UTF-8  
**Autenticación**: (Se asume según sistema actual de la app)

---

## 📍 Endpoints Disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/api/trabajos-adicionales` | Crear trabajo adicional |
| `GET` | `/api/trabajos-adicionales` | Listar todos los trabajos adicionales |
| `GET` | `/api/trabajos-adicionales/{id}` | Obtener trabajo adicional por ID |
| `PUT` | `/api/trabajos-adicionales/{id}` | Actualizar trabajo adicional |
| `DELETE` | `/api/trabajos-adicionales/{id}` | Eliminar trabajo adicional |
| `PATCH` | `/api/trabajos-adicionales/{id}/estado` | Actualizar solo el estado |

---

## 📤 1. CREAR TRABAJO ADICIONAL

### Request

**Endpoint**: `POST /api/trabajos-adicionales`  
**Content-Type**: `application/json`

#### Estructura JSON (TrabajoAdicionalRequestDTO)

```json
{
  "nombre": "string (obligatorio)",
  "importe": number (obligatorio, mayor a 0),
  "diasNecesarios": number (obligatorio, mínimo 1),
  "fechaInicio": "string (formato: YYYY-MM-DD, obligatorio)",
  "descripcion": "string (opcional)",
  "observaciones": "string (opcional)",
  "profesionales": [
    {
      "profesionalId": number (obligatorio si esRegistrado=true, null si esRegistrado=false),
      "nombre": "string (obligatorio)",
      "tipoProfesional": "string (obligatorio)",
      "honorarioDia": number (opcional)",
      "telefono": "string (opcional)",
      "email": "string (opcional)",
      "esRegistrado": boolean (obligatorio)
    }
  ],
  "obraId": number (SIEMPRE obligatorio - obra padre),
  "trabajoExtraId": number/null (opcional - null: directo de obra, valor: desde trabajo extra),
  "trabajoAdicionalPadreId": number/null (opcional - NUEVA FUNCIONALIDAD: anidación recursiva),
  "empresaId": number (obligatorio)
}
```

#### ⚠️ REGLA CRÍTICA: Constraint de Vinculación

**obraId: SIEMPRE OBLIGATORIO (obra padre)**  
**trabajoExtraId: OPCIONAL**  
**trabajoAdicionalPadreId: OPCIONAL (NUEVO)**

- `obraId`: Siempre tiene valor - indica la obra padre a la que pertenece el trabajo adicional
- `trabajoExtraId`: Puede ser:
  - `null`: Trabajo adicional creado **DIRECTAMENTE desde una obra**
  - `número`: Trabajo adicional creado **desde un TRABAJO EXTRA** de esa obra
- `trabajoAdicionalPadreId`: Puede ser: ⭐ **NUEVA FUNCIONALIDAD**
  - `null`: Trabajo adicional raíz (sin padre adicional)
  - `número`: Trabajo adicional **HIJO de otro TRABAJO ADICIONAL**

**⚠️ IMPORTANTE:** `trabajoExtraId` y `trabajoAdicionalPadreId` son **mutuamente excluyentes**.
No se puede tener valores en ambos campos simultáneamente.

**Jerarquía de Trazabilidad:**
```
Obra Padre (#123) ← obraId SIEMPRE presente
├── Trabajo Adicional #1 (obraId: 123, trabajoExtraId: null, trabajoAdicionalPadreId: null) ← DIRECTO
├── Trabajo Adicional #2 (obraId: 123, trabajoExtraId: null, trabajoAdicionalPadreId: null) ← DIRECTO
│   ├── Trabajo Adicional #5 (obraId: 123, trabajoExtraId: null, trabajoAdicionalPadreId: 2) ← HIJO del #2 ⭐ NUEVO
│   └── Trabajo Adicional #6 (obraId: 123, trabajoExtraId: null, trabajoAdicionalPadreId: 2) ← HIJO del #2 ⭐ NUEVO
└── Trabajo Extra (#456)
    ├── Trabajo Adicional #3 (obraId: 123, trabajoExtraId: 456, trabajoAdicionalPadreId: null) ← DESDE TRABAJO EXTRA
    │   └── Trabajo Adicional #7 (obraId: 123, trabajoExtraId: null, trabajoAdicionalPadreId: 3) ← HIJO del #3 ⭐ NUEVO
    └── Trabajo Adicional #4 (obraId: 123, trabajoExtraId: 456, trabajoAdicionalPadreId: null) ← DESDE TRABAJO EXTRA
```

**✅ VÁLIDO:**
- `{ "obraId": 123, "trabajoExtraId": null, "trabajoAdicionalPadreId": null }` → Directo de obra
- `{ "obraId": 123, "trabajoExtraId": 456, "trabajoAdicionalPadreId": null }` → Desde trabajo extra
- `{ "obraId": 123, "trabajoExtraId": null, "trabajoAdicionalPadreId": 100 }` → Hijo de trabajo adicional ⭐ NUEVO

**❌ INVÁLIDO:**
- `{ "obraId": null, "trabajoExtraId": 456, "trabajoAdicionalPadreId": null }` → obraId es obligatorio
- `{ "obraId": null, "trabajoExtraId": null, "trabajoAdicionalPadreId": null }` → obraId es obligatorio
- `{ "obraId": 123, "trabajoExtraId": 456, "trabajoAdicionalPadreId": 100 }` → No puede tener ambos ❌ NUEVO

#### Ejemplo 1: Trabajo adicional para OBRA

```json
{
  "nombre": "Instalación de sistema de seguridad",
  "importe": 15000.50,
  "diasNecesarios": 5,
  "fechaInicio": "2026-02-20",
  "descripcion": "Instalación completa de cámaras de seguridad y alarmas",
  "observaciones": "Requiere acceso 24/7 durante la instalación",
  "profesionales": [
    {
      "profesionalId": 123,
      "nombre": "Juan Pérez",
      "tipoProfesional": "Electricista",
      "honorarioDia": null,
      "telefono": null,
      "email": null,
      "esRegistrado": true
    },
    {
      "profesionalId": null,
      "nombre": "Carlos Rodríguez",
      "tipoProfesional": "Técnico en Seguridad",
      "honorarioDia": 350.00,
      "telefono": "+54 9 11 1234-5678",
      "email": "carlos.rodriguez@email.com",
      "esRegistrado": false
    }
  ],
  "obraId": 45,
  "trabajoExtraId": null,
  "empresaId": 10
}
```

#### Ejemplo 2: Trabajo adicional para TRABAJO EXTRA

```json
{
  "nombre": "Reparación de impermeabilización",
  "importe": 8500.00,
  "diasNecesarios": 3,
  "fechaInicio": "2026-03-01",
  "descripcion": "Reparación urgente de impermeabilización en terraza",
  "observaciones": null,
  "profesionales": [
    {
      "profesionalId": 67,
      "nombre": "María González",
      "tipoProfesional": "Impermeabilizadora",
      "honorarioDia": null,
      "telefono": null,
      "email": null,
      "esRegistrado": true
    }
  ],
  "obraId": 45,
  "trabajoExtraId": 78,
  "empresaId": 10
}
```

**Interpretación**: Trabajo adicional creado desde el Trabajo Extra #78, que pertenece a la Obra #45. La trazabilidad es: Obra #45 → Trabajo Extra #78 → Trabajo Adicional.

### Response

**Status Code**: `201 Created`  
**Content-Type**: `application/json`

#### Estructura JSON (TrabajoAdicionalResponseDTO)

```json
{
  "id": 1,
  "nombre": "Instalación de sistema de seguridad",
  "importe": 15000.50,
  "diasNecesarios": 5,
  "fechaInicio": "2026-02-20",
  "descripcion": "Instalación completa de cámaras de seguridad y alarmas",
  "observaciones": "Requiere acceso 24/7 durante la instalación",
  "obraId": 45,
  "trabajoExtraId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "fechaCreacion": "2026-02-14T10:30:00",
  "fechaActualizacion": "2026-02-14T10:30:00",
  "profesionales": [
    {
      "id": 1,
      "profesionalId": 123,
      "nombre": "Juan Pérez",
      "tipoProfesional": "Electricista",
      "honorarioDia": null,
      "telefono": null,
      "email": null,
      "esRegistrado": true,
      "fechaAsignacion": "2026-02-14T10:30:00"
    },
    {
      "id": 2,
      "profesionalId": null,
      "nombre": "Carlos Rodríguez",
      "tipoProfesional": "Técnico en Seguridad",
      "honorarioDia": 350.00,
      "telefono": "+54 9 11 1234-5678",
      "email": "carlos.rodriguez@email.com",
      "esRegistrado": false,
      "fechaAsignacion": "2026-02-14T10:30:00"
    }
  ]
}
```

---

## 📥 2. LISTAR TRABAJOS ADICIONALES

### Request

**Endpoint**: `GET /api/trabajos-adicionales`  
**Parámetros**: Ninguno (devuelve todos)

### Response

**Status Code**: `200 OK`  
**Content-Type**: `application/json`

```json
[
  {
    "id": 1,
    "nombre": "Instalación de sistema de seguridad",
    "importe": 15000.50,
    "diasNecesarios": 5,
    "fechaInicio": "2026-02-20",
    "descripcion": "Instalación completa de cámaras de seguridad y alarmas",
    "observaciones": "Requiere acceso 24/7 durante la instalación",
    "obraId": 45,
    "trabajoExtraId": null,
    "empresaId": 10,
    "estado": "PENDIENTE",
    "fechaCreacion": "2026-02-14T10:30:00",
    "fechaActualizacion": "2026-02-14T10:30:00",
    "profesionales": [...]
  },
  {
    "id": 2,
    "nombre": "Reparación de impermeabilización",
    "importe": 8500.00,
    "diasNecesarios": 3,
    "fechaInicio": "2026-03-01",
    "descripcion": "Reparación urgente de impermeabilización en terraza",
    "observaciones": null,
    "obraId": null,
    "trabajoExtraId": 78,
    "empresaId": 10,
    "estado": "EN_PROGRESO",
    "fechaCreacion": "2026-02-13T14:20:00",
    "fechaActualizacion": "2026-02-14T09:15:00",
    "profesionales": [...]
  }
]
```

---

## 📥 3. OBTENER TRABAJO ADICIONAL POR ID

### Request

**Endpoint**: `GET /api/trabajos-adicionales/{id}`  
**Ejemplo**: `GET /api/trabajos-adicionales/1`

### Response

**Status Code**: `200 OK`  
**Content-Type**: `application/json`

```json
{
  "id": 1,
  "nombre": "Instalación de sistema de seguridad",
  "importe": 15000.50,
  "diasNecesarios": 5,
  "fechaInicio": "2026-02-20",
  "descripcion": "Instalación completa de cámaras de seguridad y alarmas",
  "observaciones": "Requiere acceso 24/7 durante la instalación",
  "obraId": 45,
  "trabajoExtraId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "fechaCreacion": "2026-02-14T10:30:00",
  "fechaActualizacion": "2026-02-14T10:30:00",
  "profesionales": [
    {
      "id": 1,
      "profesionalId": 123,
      "nombre": "Juan Pérez",
      "tipoProfesional": "Electricista",
      "honorarioDia": null,
      "telefono": null,
      "email": null,
      "esRegistrado": true,
      "fechaAsignacion": "2026-02-14T10:30:00"
    }
  ]
}
```

**Error Response** (si no existe):
```json
{
  "timestamp": "2026-02-14T11:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Trabajo adicional no encontrado con id: 999"
}
```

---

## 🔄 4. ACTUALIZAR TRABAJO ADICIONAL

### Request

**Endpoint**: `PUT /api/trabajos-adicionales/{id}`  
**Content-Type**: `application/json`  
**Ejemplo**: `PUT /api/trabajos-adicionales/1`

#### Body (mismo formato que POST)

```json
{
  "nombre": "Instalación de sistema de seguridad - ACTUALIZADO",
  "importe": 18000.00,
  "diasNecesarios": 6,
  "fechaInicio": "2026-02-21",
  "descripcion": "Instalación completa con módulo de monitoreo remoto",
  "observaciones": "Se agregó sistema de monitoreo 24/7",
  "profesionales": [
    {
      "profesionalId": 123,
      "nombre": "Juan Pérez",
      "tipoProfesional": "Electricista",
      "honorarioDia": null,
      "telefono": null,
      "email": null,
      "esRegistrado": true
    },
    {
      "profesionalId": null,
      "nombre": "Ana Martínez",
      "tipoProfesional": "Técnico en Redes",
      "honorarioDia": 400.00,
      "telefono": "+54 9 11 9876-5432",
      "email": "ana.martinez@email.com",
      "esRegistrado": false
    }
  ],
  "obraId": 45,
  "trabajoExtraId": null,
  "empresaId": 10
}
```

### Response

**Status Code**: `200 OK`  
**Content-Type**: `application/json`

```json
{
  "id": 1,
  "nombre": "Instalación de sistema de seguridad - ACTUALIZADO",
  "importe": 18000.00,
  "diasNecesarios": 6,
  "fechaInicio": "2026-02-21",
  "descripcion": "Instalación completa con módulo de monitoreo remoto",
  "observaciones": "Se agregó sistema de monitoreo 24/7",
  "obraId": 45,
  "trabajoExtraId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "fechaCreacion": "2026-02-14T10:30:00",
  "fechaActualizacion": "2026-02-14T15:45:00",
  "profesionales": [...]
}
```

---

## ❌ 5. ELIMINAR TRABAJO ADICIONAL

### Request

**Endpoint**: `DELETE /api/trabajos-adicionales/{id}`  
**Ejemplo**: `DELETE /api/trabajos-adicionales/1`

### Response

**Status Code**: `204 No Content`  
**Body**: Sin contenido

---

## 🔄 6. ACTUALIZAR ESTADO (PATCH)

### Request

**Endpoint**: `PATCH /api/trabajos-adicionales/{id}/estado`  
**Content-Type**: `application/json`  
**Ejemplo**: `PATCH /api/trabajos-adicionales/1/estado`

#### Estructura JSON (ActualizarEstadoTrabajoAdicionalDTO)

```json
{
  "estado": "EN_PROGRESO"
}
```

#### Estados Permitidos

| Estado | Descripción |
|--------|-------------|
| `PENDIENTE` | Trabajo no iniciado (estado por defecto) |
| `EN_PROGRESO` | Trabajo en ejecución |
| `COMPLETADO` | Trabajo finalizado exitosamente |
| `CANCELADO` | Trabajo cancelado |

### Response

**Status Code**: `200 OK`  
**Content-Type**: `application/json`

```json
{
  "id": 1,
  "nombre": "Instalación de sistema de seguridad",
  "importe": 15000.50,
  "diasNecesarios": 5,
  "fechaInicio": "2026-02-20",
  "descripcion": "Instalación completa de cámaras de seguridad y alarmas",
  "observaciones": "Requiere acceso 24/7 durante la instalación",
  "obraId": 45,
  "trabajoExtraId": null,
  "empresaId": 10,
  "estado": "EN_PROGRESO",
  "fechaCreacion": "2026-02-14T10:30:00",
  "fechaActualizacion": "2026-02-14T16:00:00",
  "profesionales": [...]
}
```

---

## ⚠️ Validaciones y Reglas de Negocio

### Validaciones de Campos

| Campo | Regla | Mensaje de Error |
|-------|-------|------------------|
| `nombre` | No puede ser vacío | "El nombre del trabajo adicional es obligatorio" |
| `importe` | Debe ser > 0 | "El importe debe ser mayor a cero" |
| `diasNecesarios` | Debe ser >= 1 | "Los días necesarios deben ser al menos 1" |
| `fechaInicio` | Formato: YYYY-MM-DD | "La fecha de inicio es obligatoria" |
| `empresaId` | No puede ser null | "El ID de la empresa es obligatorio" |
| `obraId` | **SIEMPRE obligatorio** | "El ID de la obra es obligatorio" |
| `trabajoExtraId` | Opcional (puede ser null) | Si tiene valor, debe existir y pertenecer a la obra |
| `profesionales[].nombre` | No puede ser vacío | "El nombre del profesional es obligatorio" |
| `profesionales[].tipoProfesional` | No puede ser vacío | "El tipo de profesional es obligatorio" |
| `profesionales[].esRegistrado` | No puede ser null | "Debe indicar si es profesional registrado" |
| `estado` | Debe ser uno de los 4 valores permitidos | Ver tabla de estados |

### Validaciones de Integridad

1. **Obra Padre (obraId)**:
   - ✅ **SIEMPRE obligatorio**: Todo trabajo adicional debe tener una obra padre
   - Debe existir en la tabla `obras`

2. **Trabajo Extra (trabajoExtraId)**:
   - ✅ **Opcional**: Puede ser `null` o tener un valor
   - Si tiene valor:
     - Debe existir en la tabla `trabajos_extra`
     - **DEBE pertenecer a la obra indicada en `obraId`**
     - Si el trabajo extra pertenece a otra obra, la validación fallará

3. **Profesionales Registrados vs Ad-hoc**:
   - Si `esRegistrado = true`: `profesionalId` DEBE tener valor, se cargan datos desde tabla `profesionales`
   - Si `esRegistrado = false`: `profesionalId` debe ser `null`, datos ingresados manualmente

4. **Foreign Keys**:
   - `obraId` debe existir en tabla `obras` (✅ OBLIGATORIO)
   - `trabajoExtraId` debe existir en tabla `trabajos_extra` (si no es null)
   - `empresaId` debe existir en tabla `empresas`
   - `profesionalId` debe existir en tabla `profesionales` (si `esRegistrado = true`)

---

## 🚨 Códigos de Estado HTTP y Errores

### Códigos de Éxito

| Código | Situación |
|--------|-----------|
| `200 OK` | GET, PUT, PATCH exitosos |
| `201 Created` | POST exitoso |
| `204 No Content` | DELETE exitoso |

### Códigos de Error

| Código | Situación | Ejemplo |
|--------|-----------|---------|
| `400 Bad Request` | Validación fallida | Falta campo obligatorio, formato incorrecto |
| `404 Not Found` | Recurso no existe | ID no encontrado |
| `409 Conflict` | Violación de constraint | Obra y TrabajoExtra ambos con valores |
| `500 Internal Server Error` | Error del servidor | Error de base de datos |

### Ejemplos de Respuestas de Error

#### Error 400 - Validación

```json
{
  "timestamp": "2026-02-14T11:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación",
  "errors": {
    "nombre": "El nombre del trabajo adicional es obligatorio",
    "importe": "El importe debe ser mayor a cero",
    "diasNecesarios": "Los días necesarios deben ser al menos 1"
  }
}
```

#### Error 404 - No Encontrado

```json
{
  "timestamp": "2026-02-14T11:00:00",
  "status": 404,
  "error": "Not Found",
  "message": "Trabajo adicional no encontrado con id: 999"
}
```

#### Error 409 - Constraint Violado

```json
{
  "timestamp": "2026-02-14T11:00:00",
  "status": 409,
  "error": "Conflict",
  "message": "El trabajo extra (ID: 456) no pertenece a la obra (ID: 123). El trabajo extra pertenece a la obra ID: 789"
}
```

---

## 🔍 Casos de Uso Comunes

### Caso 1: Crear trabajo adicional para obra con profesional registrado

```javascript
// Frontend Request
const requestData = {
  nombre: "Instalación eléctrica adicional",
  importe: 12000.00,
  diasNecesarios: 4,
  fechaInicio: "2026-02-25",
  descripcion: "Cableado adicional para nueva oficina",
  observaciones: null,
  profesionales: [
    {
      profesionalId: 55,        // ID del catálogo
      nombre: "Pedro López",    // Se puede pre-cargar del catálogo
      tipoProfesional: "Electricista",
      honorarioDia: null,       // Se usa el del catálogo
      telefono: null,
      email: null,
      esRegistrado: true
    }
  ],
  obraId: 30,
  trabajoExtraId: null,
  empresaId: 10
};

fetch('http://localhost:8080/api/trabajos-adicionales', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(requestData)
})
.then(response => response.json())
.then(data => console.log('Trabajo creado:', data))
.catch(error => console.error('Error:', error));
```

### Caso 2: Crear trabajo adicional con profesional ad-hoc

```javascript
const requestData = {
  nombre: "Reparación urgente de techo",
  importe: 5000.00,
  diasNecesarios: 2,
  fechaInicio: "2026-02-18",
  descripcion: "Reparación de goteras en techo",
  observaciones: "Prioridad alta",
  profesionales: [
    {
      profesionalId: null,      // No tiene ID del catálogo
      nombre: "Roberto Silva",
      tipoProfesional: "Techista",
      honorarioDia: 450.00,    // Honorario personalizado
      telefono: "+54 9 11 5555-1234",
      email: "roberto.techista@gmail.com",
      esRegistrado: false      // Es ad-hoc
    }
  ],
  obraId: 30,                  // Obra padre (siempre presente)
  trabajoExtraId: 90,          // Creado desde trabajo extra #90
  empresaId: 10
};
```

### Caso 3: Actualizar estado a EN_PROGRESO

```javascript
const estadoUpdate = {
  estado: "EN_PROGRESO"
};

fetch('http://localhost:8080/api/trabajos-adicionales/1/estado', {
  method: 'PATCH',
  headers: {
    'Content-Type': 'application/json'
  },
  body: JSON.stringify(estadoUpdate)
})
.then(response => response.json())
.then(data => console.log('Estado actualizado:', data))
.catch(error => console.error('Error:', error));
```

---

## 📊 Mapeo de Campos: Frontend ↔ Backend ↔ Base de Datos

| Campo Frontend | Campo Backend (DTO) | Campo Base de Datos | Tipo | Obligatorio |
|----------------|---------------------|---------------------|------|-------------|
| `nombre` | `nombre` | `nombre` | VARCHAR(255) | Sí |
| `importe` | `importe` | `importe` | DECIMAL(15,2) | Sí |
| `diasNecesarios` | `diasNecesarios` | `dias_necesarios` | INTEGER | Sí |
| `fechaInicio` | `fechaInicio` | `fecha_inicio` | DATE | Sí |
| `descripcion` | `descripcion` | `descripcion` | TEXT | No |
| `observaciones` | `observaciones` | `observaciones` | TEXT | No |
| `obraId` | `obraId` | `obra_id` | BIGINT (FK) | **Sí - SIEMPRE** |
| `trabajoExtraId` | `trabajoExtraId` | `trabajo_extra_id` | BIGINT (FK) | No (opcional) |
| `empresaId` | `empresaId` | `empresa_id` | BIGINT (FK) | Sí |
| `estado` | `estado` | `estado` | VARCHAR(50) | Sí (default: PENDIENTE) |
| N/A (auto) | `fechaCreacion` | `fecha_creacion` | TIMESTAMP | Automático |
| N/A (auto) | `fechaActualizacion` | `fecha_actualizacion` | TIMESTAMP | Automático |

### Profesionales

| Campo Frontend | Campo Backend (DTO) | Campo Base de Datos | Tipo | Obligatorio |
|----------------|---------------------|---------------------|------|-------------|
| `profesionalId` | `profesionalId` | `profesional_id` | BIGINT (FK) | Si esRegistrado=true |
| `nombre` | `nombre` | `nombre` | VARCHAR(255) | Sí |
| `tipoProfesional` | `tipoProfesional` | `tipo_profesional` | VARCHAR(100) | Sí |
| `honorarioDia` | `honorarioDia` | `honorario_dia` | DECIMAL(10,2) | No |
| `telefono` | `telefono` | `telefono` | VARCHAR(50) | No |
| `email` | `email` | `email` | VARCHAR(255) | No |
| `esRegistrado` | `esRegistrado` | `es_registrado` | BOOLEAN | Sí |

---

## 🎨 Sugerencias para UI/UX Frontend

### Formulario de Creación

1. **Sección Obra Padre** (Obligatorio):
   - Dropdown con obras disponibles (SIEMPRE debe seleccionar una)
   - Campo obligatorio marcado con asterisco (*)

2. **Sección Trabajo Extra** (Opcional):
   - Checkbox: "¿Este trabajo adicional proviene de un trabajo extra?"
   - Si marcado: Dropdown con trabajos extra de la obra seleccionada
   - Si no marcado: trabajoExtraId = null (trabajo adicional directo)

3. **Sección Profesionales** (Multi-select dinámico):
   - Botón: "+ Agregar Profesional"
   - Cada profesional:
     - ☑️ Checkbox: "Usar profesional registrado"
       - Si TRUE: Dropdown del catálogo (pre-carga nombre, tipo)
       - Si FALSE: Formulario manual (nombre, tipo, honorario, teléfono, email)

4. **Sección Estado**:
   - En creación: Siempre "PENDIENTE" (oculto/disabled)
   - En edición: Dropdown con 4 opciones

### Validaciones en Frontend (antes de enviar)

```javascript
function validarTrabajoAdicional(formData) {
  const errores = [];
  
  // Validar que obraId sea obligatorio
  if (!formData.obraId) {
    errores.push("El ID de la obra es obligatorio. Todo trabajo adicional debe pertenecer a una obra");
  }
  
  // Si hay trabajoExtraId, validar que existe (validación adicional en backend)
  // El backend validará que el trabajo extra pertenezca a la obra
  
  // Validar importe
  if (!formData.importe || formData.importe <= 0) {
    errores.push("El importe debe ser mayor a cero");
  }
  
  // Validar días
  if (!formData.diasNecesarios || formData.diasNecesarios < 1) {
    errores.push("Los días necesarios deben ser al menos 1");
  }
  
  // Validar profesionales
  formData.profesionales.forEach((prof, index) => {
    if (prof.esRegistrado && !prof.profesionalId) {
      errores.push(`Profesional ${index + 1}: Debe seleccionar un profesional del catálogo`);
    }
    if (!prof.esRegistrado && !prof.honorarioDia) {
      errores.push(`Profesional ${index + 1}: Debe ingresar el honorario diario`);
    }
  });
  
  return errores;
}
```

---

## 🔗 URLs de Swagger (cuando el backend esté corriendo)

**Swagger UI**: `http://localhost:8080/swagger-ui.html`  
**OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

---

## 📝 Notas Importantes

1. **Formato de Fechas**:
   - Request: `"YYYY-MM-DD"` (ej: `"2026-02-20"`)
   - Response: `"YYYY-MM-DDTHH:mm:ss"` (ej: `"2026-02-14T10:30:00"`)

2. **Números Decimales**:
   - Enviar como números, NO como strings: `15000.50` ✅ NO `"15000.50"` ❌

3. **Arrays Vacíos**:
   - Si no hay profesionales: enviar `"profesionales": []` (NO `null`)

4. **Obra Padre (obraId)**:
   - **SIEMPRE obligatorio** - todo trabajo adicional debe tener una obra padre
   - NO puede ser `null` en ningún caso

5. **Trabajo Extra (trabajoExtraId)**:
   - Opcional - puede ser `null` o tener un valor
   - Si tiene valor, el backend valida que ese trabajo extra pertenezca a la obra indicada en `obraId`
   - Si el trabajo extra pertenece a otra obra, la validación fallará con error 409

6. **Cascada en Eliminación**:
   - Si se elimina un trabajo adicional, se eliminan también sus profesionales asignados (CASCADE)
   - Si se elimina una obra, se eliminan los trabajos adicionales asociados (CASCADE)
   - Si se elimina un trabajo extra, se eliminan los trabajos adicionales asociados (CASCADE)

7. **Actualización de Profesionales**:
   - En PUT: Se reemplazan TODOS los profesionales (elimina antiguos, crea nuevos)
   - Para mantener profesionales existentes, incluirlos en el array

8. **Estados**:
   - Backend valida que el estado sea uno de los 4 permitidos (PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO)
   - No hay validación de transiciones de estado (puedes cambiar de cualquier estado a cualquiera)

9. **Jerarquía de Trazabilidad**:
   - Obra Padre → Trabajo Adicional (directo)
   - Obra Padre → Trabajo Extra → Trabajo Adicional (indirecto)
   - Esto permite saber siempre a qué obra pertenece cada trabajo adicional

---

## ✅ Checklist de Implementación Frontend

- [ ] Configurar base URL de la API
- [ ] Crear servicio/API client para trabajos adicionales
- [ ] Implementar formulario con validación de constraint exclusivo (obra XOR trabajo extra)
- [ ] Implementar selector dual de profesionales (registrados/ad-hoc)
- [ ] Agregar validaciones de frontend antes de enviar
- [ ] Manejar respuestas de error (400, 404, 409)
- [ ] Implementar vista de listado con filtros por estado
- [ ] Agregar botones de cambio de estado (PATCH)
- [ ] Implementar confirmación antes de eliminar
- [ ] Probar con Swagger UI primero para verificar estructura de datos

---

## 🔄 ANIDACIÓN DE TRABAJOS ADICIONALES ⭐ NUEVA FUNCIONALIDAD

### Descripción
Los trabajos adicionales ahora soportan **jerarquías anidadas**, permitiendo que un trabajo adicional pueda tener trabajos adicionales hijos.

### Ejemplo 3: Crear Trabajo Adicional Hijo ⭐ NUEVO

**Paso 1: Crear el trabajo adicional padre**
```json
POST /api/trabajos-adicionales
{
  "nombre": "Sistema de iluminación LED",
  "importe": 50000.00,
  "diasNecesarios": 10,
  "fechaInicio": "2026-03-15",
  "descripcion": "Instalación completa de sistema LED",
  "profesionales": [],
  "obraId": 45,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10
}
```

**Response 201 - Trabajo Padre Creado:**
```json
{
  "id": 100,
  "nombre": "Sistema de iluminación LED",
  "importe": 50000.00,
  "obraId": 45,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "trabajosAdicionalesHijos": [],
  "profesionales": []
}
```

**Paso 2: Crear trabajo adicional hijo del anterior**
```json
POST /api/trabajos-adicionales
{
  "nombre": "Paneles LED para cocina",
  "importe": 15000.00,
  "diasNecesarios": 3,
  "fechaInicio": "2026-03-16",
  "descripcion": "Sub-trabajo: instalación de paneles en cocina",
  "profesionales": [],
  "obraId": 45,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 100,  // ← HIJO del trabajo adicional #100
  "empresaId": 10
}
```

**Response 201 - Trabajo Hijo Creado:**
```json
{
  "id": 101,
  "nombre": "Paneles LED para cocina",
  "importe": 15000.00,
  "obraId": 45,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 100,  // ← Indica que es hijo del #100
  "empresaId": 10,
  "estado": "PENDIENTE",
  "trabajosAdicionalesHijos": [],
  "profesionales": []
}
```

**Paso 3: Obtener el padre con sus hijos**
```http
GET /api/trabajos-adicionales/100?empresaId=10
```

**Response 200 - Padre con Hijos:**
```json
{
  "id": 100,
  "nombre": "Sistema de iluminación LED",
  "importe": 50000.00,
  "obraId": 45,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "trabajosAdicionalesHijos": [
    {
      "id": 101,
      "nombre": "Paneles LED para cocina",
      "importe": 15000.00,
      "trabajoAdicionalPadreId": 100,
      "trabajosAdicionalesHijos": []  // Solo 1 nivel de profundidad en response
    }
  ],
  "profesionales": []
}
```

### Ejemplo 4: Trabajo Adicional Anidado en Trabajo Extra ⭐ NUEVO

**Escenario:** Crear un trabajo adicional hijo de un trabajo adicional que está dentro de un trabajo extra.

```json
POST /api/trabajos-adicionales
{
  "nombre": "Sub-instalación de gabinetes base",
  "importe": 5000.00,
  "diasNecesarios": 2,
  "fechaInicio": "2026-03-18",
  "descripcion": "Detalle de gabinetes base de cocina",
  "profesionales": [],
  "obraId": 45,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 50,  // ← Padre es un trabajo adicional que está en un trabajo extra
  "empresaId": 10
}
```

**Jerarquía resultante:**
```
Obra #45
└── Trabajo Extra #78
    └── Trabajo Adicional #50 "Instalación de gabinetes"
        └── Trabajo Adicional #102 "Sub-instalación de gabinetes base" ⭐ NUEVO
```

### Testing cURL - Anidación ⭐ NUEVO

**Crear trabajo adicional hijo:**
```bash
curl -X POST http://localhost:8080/api/trabajos-adicionales \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Paneles LED para baño",
    "importe": 8000.00,
    "diasNecesarios": 2,
    "fechaInicio": "2026-03-17",
    "descripcion": "Sub-trabajo: instalación de paneles en baño",
    "profesionales": [],
    "obraId": 45,
    "trabajoExtraId": null,
    "trabajoAdicionalPadreId": 100,
    "empresaId": 10
  }'
```

### Validaciones de Anidación

**❌ Error: trabajoExtraId y trabajoAdicionalPadreId simultáneos**
```json
{
  "obraId": 45,
  "trabajoExtraId": 78,
  "trabajoAdicionalPadreId": 100,  // ← ERROR: No puede tener ambos
  "empresaId": 10
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Bad Request",
  "message": "Un trabajo adicional no puede tener trabajoExtraId y trabajoAdicionalPadreId simultáneamente. Debe ser hijo de un trabajo extra O de otro trabajo adicional, no de ambos."
}
```

**❌ Error: Padre no existe**
```json
{
  "obraId": 45,
  "trabajoAdicionalPadreId": 99999,  // ← No existe
  "empresaId": 10
}
```

**Response 404 Not Found:**
```json
{
  "error": "Not Found",
  "message": "Trabajo adicional padre no encontrado con ID: 99999"
}
```

**❌ Error: Padre de diferente obra**
```json
{
  "obraId": 99,  // ← Obra diferente al padre
  "trabajoAdicionalPadreId": 100,  // ← Padre pertenece a obra #45
  "empresaId": 10
}
```

**Response 400 Bad Request:**
```json
{
  "error": "Bad Request",
  "message": "El trabajo adicional padre (ID: 100) no pertenece a la obra (ID: 99). El trabajo adicional padre pertenece a la obra ID: 45"
}
```

---

## 🧪 Testing con cURL (Ejemplos)

### Crear trabajo adicional
```bash
curl -X POST http://localhost:8080/api/trabajos-adicionales \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Test trabajo adicional",
    "importe": 10000.00,
    "diasNecesarios": 3,
    "fechaInicio": "2026-02-20",
    "descripcion": "Prueba desde cURL",
    "observaciones": null,
    "profesionales": [],
    "obraId": 1,
    "trabajoExtraId": null,
    "empresaId": 1
  }'
```

### Listar todos
```bash
curl -X GET http://localhost:8080/api/trabajos-adicionales
```

### Obtener por ID
```bash
curl -X GET http://localhost:8080/api/trabajos-adicionales/1
```

### Actualizar estado
```bash
curl -X PATCH http://localhost:8080/api/trabajos-adicionales/1/estado \
  -H "Content-Type: application/json" \
  -d '{"estado": "EN_PROGRESO"}'
```

### Eliminar
```bash
curl -X DELETE http://localhost:8080/api/trabajos-adicionales/1
```

---

## 📞 Contacto y Soporte

Si encuentras inconsistencias entre esta documentación y el comportamiento real del backend:
1. Verificar en Swagger UI la estructura exacta
2. Revisar logs del backend para mensajes de error detallados
3. Consultar con el equipo de backend

**Versión del documento**: 1.0  
**Fecha**: 14 de febrero de 2026  
**Backend Framework**: Spring Boot 3.x  
**Base de Datos**: PostgreSQL  

---

_Documento generado para facilitar la integración frontend-backend del módulo Trabajos Adicionales_
