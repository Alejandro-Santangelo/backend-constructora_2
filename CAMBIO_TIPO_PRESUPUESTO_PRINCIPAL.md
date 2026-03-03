# 🔄 CAMBIOS EN API - Actualización backend

**Fecha:** 3 de marzo de 2026  
**Impacto:** ⚠️ BREAKING CHANGE + Mejoras funcionales  
**Módulos afectados:** `presupuesto_no_cliente`, `trabajos_adicionales`  
**Cambios:** 4 cambios principales (1 breaking change crítico)

---

## 📋 Resumen de cambios

### 1. Renombrado de tipo_presupuesto: TRADICIONAL → PRINCIPAL

El valor del enum `tipo_presupuesto` **TRADICIONAL** se renombró a **PRINCIPAL** para evitar confusión con la columna `modo_presupuesto`.

### 2. TRABAJO_DIARIO ahora tiene flujo simplificado directo ⚠️ CAMBIO IMPORTANTE

Los presupuestos **TRABAJO_DIARIO** ahora tienen un **flujo simplificado de 2 pasos**: `BORRADOR` → `TERMINADO` → `APROBADO` (sin pasar por estados de negociación como A_ENVIAR, ENVIADO).

### 3. Herencia automática mejorada en TAREA_LEVE

Los presupuestos TAREA_LEVE ahora heredan automáticamente más campos de la obra padre.

### 4. Soporte para TrabajoAdicional recursivo

Implementación de relación self-referencing para crear trabajos adicionales desde otros trabajos adicionales.

---

## 🔧 CAMBIO 1: tipo_presupuesto TRADICIONAL → PRINCIPAL

### ❌ Antes
```json
{
  "tipo_presupuesto": "TRADICIONAL",
  "modo_presupuesto": "TRADICIONAL"
}
```

### ✅ Ahora
```json
{
  "tipo_presupuesto": "PRINCIPAL",
  "modo_presupuesto": "TRADICIONAL"
}
```

---

## 🔍 Diferencia entre columnas tipo_presupuesto y modo_presupuesto

| Columna | Propósito | Valores válidos |
|---------|-----------|----------------|
| `tipo_presupuesto` | **Origen del presupuesto** | `PRINCIPAL`, `TRABAJO_DIARIO`, `TRABAJO_EXTRA`, `TAREA_LEVE` |
| `modo_presupuesto` | **Método de cálculo** | `TRADICIONAL`, `TRABAJOS_SEMANALES` |

**No hay conflicto ahora** ✅

---

## ⚠️ CAMBIO 2: TRABAJO_DIARIO ahora requiere aprobación manual

### ❌ Comportamiento ANTERIOR

Los presupuestos **TRABAJO_DIARIO** se creaban con estado **APROBADO** y generaban la obra **automáticamente** al hacer POST:

```json
// POST /presupuestos
{
  "tipoPresupuesto": "TRABAJO_DIARIO",
  "nombreObra": "Obra rápida",
  "direccionObraCalle": "Av. Principal"
}

// Response: estado APROBADO + obra creada inmediatamente
{
  "id": 200,
  "estado": "APROBADO",  // ← Auto-aprobado
  "obraId": 50           // ← Obra creada automáticamente
}
```

### ✅ Comportamiento NUEVO

Ahora **TRABAJO_DIARIO** tiene un **flujo simplificado directo**:

1. **Estado inicial**: `BORRADOR` (en lugar de `APROBADO`)
2. **Flujo directo**: BORRADOR → TERMINADO → Aprobar → APROBADO + Obra
3. **NO pasa por estados de negociación** (A_ENVIAR, ENVIADO, etc.)
4. **NO crea obra automáticamente** al hacer POST

```json
// Paso 1: POST /presupuestos - Crear presupuesto
{
  "tipoPresupuesto": "TRABAJO_DIARIO",
  "nombreObra": "Obra rápida",
  "direccionObraCalle": "Av. Principal"
}

// Response: estado BORRADOR
{
  "id": 200,
  "estado": "BORRADOR",  // ← Usuario carga datos
  "obraId": null
}

// Paso 2: PUT /presupuestos/200 - Cambiar a TERMINADO cuando termina de cargar
{
  "estado": "TERMINADO"
}

// Response: estado TERMINADO
{
  "id": 200,
  "estado": "TERMINADO",  // ← Listo para aprobar
  "obraId": null
}

// Paso 3: PUT /presupuestos/200/aprobar - Aprobar directamente
// Response: APROBADO + obra creada
{
  "id": 200,
  "estado": "APROBADO",  // ← Aprobado
  "obraId": 50           // ← Obra creada
}
```

### 🎯 Razón del cambio

**Flujo simplificado**: TRABAJO_DIARIO tiene un flujo **directo sin negociación** (ideal para obras rápidas), mientras que PRINCIPAL sigue el flujo completo con estados intermedios.

| Tipo | Estado inicial | Flujo de aprobación | Crea obra |
|------|----------------|---------------------|-----------|
| **PRINCIPAL** | BORRADOR | BORRADOR → A_ENVIAR → ENVIADO → APROBADO | Al aprobar |
| **TRABAJO_DIARIO** | BORRADOR | **BORRADOR → TERMINADO → APROBADO** ⚡ | Al aprobar |
| **TRABAJO_EXTRA** | BORRADOR | BORRADOR → A_ENVIAR → ENVIADO → APROBADO | NO (vincula a obra existente) |
| **TAREA_LEVE** | BORRADOR | BORRADOR → TERMINADO (al aprobar crea obra) | Al cambiar a TERMINADO |

### 📝 Resumen visual del flujo TRABAJO_DIARIO

```
┌─────────────────────────────────────────────────────────────┐
│ TRABAJO_DIARIO: Flujo simplificado (2 pasos)                │
└─────────────────────────────────────────────────────────────┘

1️⃣ POST /presupuestos
   └─> Estado: BORRADOR
   └─> Obra: null
   └─> Usuario carga items, profesionales, materiales

2️⃣ PUT /presupuestos/{id}  (body: {"estado": "TERMINADO"})
   └─> Estado: TERMINADO
   └─> Obra: null
   └─> Presupuesto listo para aprobar

3️⃣ PUT /presupuestos/{id}/aprobar
   └─> Estado: APROBADO
   └─> Obra: CREADA ✅
   └─> Presupuesto finalizado

⚡ Ventaja: Solo 2 pasos vs 4+ de PRINCIPAL
   (No pasa por A_ENVIAR, ENVIADO, etc.)
```

### 💻 Implementación en Frontend (ejemplo)

```typescript
// Función para crear y aprobar TRABAJO_DIARIO
async function crearTrabajoDiario(datos: PresupuestoDTO) {
  // Paso 1: Crear presupuesto (estado inicial: BORRADOR)
  const response1 = await api.post('/presupuestos', {
    tipoPresupuesto: 'TRABAJO_DIARIO',
    nombreObra: datos.nombreObra,
    direccionObraCalle: datos.direccion,
    // ... otros campos
  });
  
  const presupuestoId = response1.data.id;
  console.log('Presupuesto creado:', presupuestoId, 'Estado:', response1.data.estado); // BORRADOR
  
  // Usuario carga items, profesionales, materiales...
  // await cargarItems(presupuestoId, datos.items);
  
  // Paso 2: Marcar como TERMINADO (usuario termina de cargar datos)
  const response2 = await api.put(`/presupuestos/${presupuestoId}`, {
    estado: 'TERMINADO'
  });
  
  console.log('Estado actualizado:', response2.data.estado); // TERMINADO
  
  // Paso 3: Aprobar (crea la obra)
  const response3 = await api.put(`/presupuestos/${presupuestoId}/aprobar`);
  
  console.log('Presupuesto aprobado. Obra ID:', response3.data.obraId); // ✅ Obra creada
  
  return response3.data;
}

// Validación de estados permitidos para TRABAJO_DIARIO
function esEstadoValidoParaTrabajoDiario(estado: string): boolean {
  // TRABAJO_DIARIO solo usa: BORRADOR, TERMINADO, APROBADO
  const estadosValidos = ['BORRADOR', 'TERMINADO', 'APROBADO', 'CANCELADO'];
  return estadosValidos.includes(estado);
}

// NO mostrar estos estados para TRABAJO_DIARIO:
const estadosNoAplicanTrabajoDiario = ['A_ENVIAR', 'ENVIADO', 'MODIFICADO', 'OBRA_A_CONFIRMAR'];
```

---

## 🆕 CAMBIO 3: Herencia automática mejorada en TAREA_LEVE

### Campos que se heredan automáticamente

Cuando se crea un presupuesto **TAREA_LEVE** desde un `TrabajoAdicional`, el backend ahora hereda automáticamente los siguientes campos de la obra padre (si no vienen en el request):

| Campo | Descripción | Heredado desde |
|-------|-------------|----------------|
| `empresaId` | Empresa responsable | Obra padre |
| `clienteId` | Cliente (si existe en obra padre) | Obra padre |
| `telefono` | Teléfono de contacto | ✅ **NUEVO** Obra padre |
| `mail` | Email de contacto | ✅ **NUEVO** Obra padre |
| `nombreSolicitante` | Nombre del solicitante | ✅ **NUEVO** Obra padre |
| `direccionObraCalle` | Calle de la obra | Obra padre |
| `direccionObraAltura` | Altura/número | Obra padre |
| `direccionObraBarrio` | Barrio | Obra padre |
| `direccionObraPiso` | Piso | Obra padre |
| `direccionObraDepartamento` | Departamento | Obra padre |
| `direccionObraTorre` | Torre | Obra padre |

### Comportamiento de herencia

**Si el campo viene en el request:** Se usa el valor enviado (no se sobrescribe)  
**Si el campo está vacío/null:** Se hereda automáticamente de la obra padre

**Ejemplo request TAREA_LEVE:**
```json
{
  "tipoPresupuesto": "TAREA_LEVE",
  "trabajoAdicionalId": 123,
  "nombreObra": "Pintura de fachada",
  "totalPresupuesto": 50000
  // telefono, mail, nombreSolicitante NO enviados → se heredan automáticamente
}
```

**Response (campos heredados):**
```json
{
  "id": 105,
  "tipoPresupuesto": "TAREA_LEVE",
  "nombreObra": "Pintura de fachada",
  "telefono": "351-123-4567",           // ✅ Heredado de obra padre
  "mail": "cacho@ejemplo.com",          // ✅ Heredado de obra padre
  "nombreSolicitante": "Cacho",         // ✅ Heredado de obra padre
  "direccionObraCalle": "La Granja",    // ✅ Heredado de obra padre
  "direccionObraAltura": "193"          // ✅ Heredado de obra padre
}
```

---

## 🔄 CAMBIO 4: Soporte para TrabajoAdicional recursivo

### Nueva funcionalidad

Ahora es posible crear un **TrabajoAdicional hijo** desde otro **TrabajoAdicional padre**.

**Estructura de datos:**
- Columna nueva: `trabajo_adicional_padre_id` (FK a `trabajos_adicionales`)
- Constraint XOR: Un TrabajoAdicional debe tener **trabajo_extra_id** O **trabajo_adicional_padre_id** (mutuamente excluyente)
- Relación recursiva: `trabajoAdicionalPadre` ↔ `trabajosAdicionalesHijos`

**Caso de uso:**
```
Obra Principal (ID: 52)
  └─ TrabajoExtra (ID: 1) "Ampliación"
      └─ TrabajoAdicional (ID: 10) "Instalación eléctrica"
          └─ TrabajoAdicional hijo (ID: 20) "Tablero secundario"  ← NUEVO
```

**No afecta al frontend** - Es transparente, solo permite mayor flexibilidad en la jerarquía.

---

## 📥 Responses del Backend (GET) - CAMBIO 1

### Cambios en las respuestas

**Antes:**
```json
{
  "id": 90,
  "nombreObra": "Casa de Cacho",
  "tipoPresupuesto": "TRADICIONAL",
  "modoPresupuesto": "TRADICIONAL"
}
```

**Ahora:**
```json
{
  "id": 90,
  "nombreObra": "Casa de Cacho",
  "tipoPresupuesto": "PRINCIPAL",
  "modoPresupuesto": "TRADICIONAL"
}
```

---

## 📤 Requests al Backend (POST/PUT)

### ✅ Valores aceptados (backward compatible)

El backend acepta **3 valores** para `tipo_presupuesto = PRINCIPAL`:

1. **`"PRINCIPAL"`** *(nuevo, recomendado)*
2. **`"PRESUPUESTO_PRINCIPAL"`** *(alias semántico)*
3. **`"TRADICIONAL"`** *(legacy, funciona pero deprecated)*

**Ejemplo POST válido:**
```json
{
  "nombreObra": "Nueva Obra",
  "tipoPresupuesto": "PRINCIPAL",  // ✅ Recomendado
  "modoPresupuesto": "TRADICIONAL",
  "direccionObraCalle": "Av. Colón",
  "direccionObraAltura": "123"
}
```

**También válido (backward compatibility):**
```json
{
  "tipoPresupuesto": "TRADICIONAL"  // ✅ Funciona pero deprecated
}
```

---

## 🛠️ Acciones requeridas en Frontend

### 1️⃣ Actualizar comparaciones/validaciones

**Buscar y reemplazar:**

❌ **Antes:**
```typescript
if (presupuesto.tipoPresupuesto === 'TRADICIONAL') {
  // Lógica para presupuesto principal
}
```

✅ **Ahora:**
```typescript
if (presupuesto.tipoPresupuesto === 'PRINCIPAL') {
  // Lógica para presupuesto principal
}
```

### 2️⃣ Actualizar switches/enums

❌ **Antes:**
```typescript
enum TipoPresupuesto {
  TRADICIONAL = 'TRADICIONAL',
  TRABAJO_DIARIO = 'TRABAJO_DIARIO',
  TRABAJO_EXTRA = 'TRABAJO_EXTRA',
  TAREA_LEVE = 'TAREA_LEVE'
}
```

✅ **Ahora:**
```typescript
enum TipoPresupuesto {
  PRINCIPAL = 'PRINCIPAL',           // ← Cambio aquí
  TRABAJO_DIARIO = 'TRABAJO_DIARIO',
  TRABAJO_EXTRA = 'TRABAJO_EXTRA',
  TAREA_LEVE = 'TAREA_LEVE'
}
```

### 3️⃣ Actualizar labels en UI

❌ **Antes:**
```html
<span>{{ presupuesto.tipoPresupuesto }}</span>
<!-- Mostraba: "TRADICIONAL" -->
```

✅ **Ahora:**
```html
<span>{{ getTipoPresupuestoLabel(presupuesto.tipoPresupuesto) }}</span>

<!-- Helper function -->
function getTipoPresupuestoLabel(tipo: string): string {
  const labels = {
    'PRINCIPAL': 'Presupuesto Principal',
    'TRABAJO_DIARIO': 'Trabajo Diario',
    'TRABAJO_EXTRA': 'Trabajo Extra',
    'TAREA_LEVE': 'Tarea Leve'
  };
  return labels[tipo] || tipo;
}
```

### 4️⃣ Actualizar formularios de creación

**Recomendación:** Usar alias semánticos

```typescript
// Al crear presupuesto, enviar valor semántico
const nuevoPresupuesto = {
  nombreObra: 'Mi Obra',
  tipoPresupuesto: 'PRESUPUESTO_PRINCIPAL',  // ✅ Alias semántico
  modoPresupuesto: 'TRADICIONAL'
};
```

### 5️⃣ Simplificar creación de TAREA_LEVE (NUEVO)

**Beneficio:** Ya no es necesario enviar todos los campos en el request

❌ **Antes (innecesariamente complejo):**
```typescript
const tareaLeve = {
  tipoPresupuesto: 'TAREA_LEVE',
  trabajoAdicionalId: 123,
  nombreObra: 'Pintura de fachada',
  totalPresupuesto: 50000,
  telefono: '351-123-4567',              // ← Ya no necesario
  mail: 'cacho@ejemplo.com',              // ← Ya no necesario
  nombreSolicitante: 'Cacho',             // ← Ya no necesario
  direccionObraCalle: 'La Granja',        // ← Ya no necesario
  direccionObraAltura: '193',             // ← Ya no necesario
  // ... más campos de dirección
};
```

✅ **Ahora (simplificado - herencia automática):**
```typescript
const tareaLeve = {
  tipoPresupuesto: 'PRESUPUESTO_TAREA_LEVE',  // o 'TAREA_LEVE'
  trabajoAdicionalId: 123,
  nombreObra: 'Pintura de fachada',
  totalPresupuesto: 50000
  // telefono, mail, nombreSolicitante, dirección → se heredan automáticamente
};
```

**El backend responderá con todos los campos heredados de la obra padre** ✅

**Excepciones:** Si necesitas **sobrescribir** algún campo heredado, simplemente envíalo en el request:
```typescript
const tareaLeve = {
  tipoPresupuesto: 'TAREA_LEVE',
  trabajoAdicionalId: 123,
  nombreObra: 'Pintura de fachada',
  totalPresupuesto: 50000,
  telefono: '351-999-8888'  // ✅ Sobrescribe el heredado
  // mail, nombreSolicitante → se heredan igual
};
```

---

## 🧪 Testing

### Casos de prueba - CAMBIO 1 (tipo_presupuesto)

1. **GET presupuestos**: Verificar que retorna `"PRINCIPAL"` en lugar de `"TRADICIONAL"`
2. **POST con "PRINCIPAL"**: Debe crear presupuesto correctamente
3. **POST con "PRESUPUESTO_PRINCIPAL"**: Debe funcionar (alias)
4. **POST con "TRADICIONAL"**: Debe funcionar (backward compatibility)
5. **Filtros/búsquedas**: Si filtran por tipo, usar `"PRINCIPAL"`

### Casos de prueba - CAMBIO 2 (TRABAJO_DIARIO flujo simplificado) ⚠️ CRÍTICO

6. **POST TRABAJO_DIARIO**: Verificar que retorna estado `"BORRADOR"` (no `"APROBADO"`)
7. **POST TRABAJO_DIARIO**: Verificar que `obraId` es `null` (NO se crea obra automáticamente)
8. **PUT estado a TERMINADO**: Cambiar presupuesto TRABAJO_DIARIO de BORRADOR a TERMINADO
9. **Desde TERMINADO aprobar**: Aprobar un TRABAJO_DIARIO en estado TERMINADO y verificar que cambia a `"APROBADO"`
10. **Después de aprobar**: Verificar que `obraId` tiene valor (obra creada al aprobar)
11. **Flujo completo TRABAJO_DIARIO**: POST → BORRADOR → cambiar a TERMINADO → Aprobar → APROBADO + obra creada
12. **NO debe pasar por A_ENVIAR/ENVIADO**: Verificar que TRABAJO_DIARIO puede ir directo de BORRADOR a TERMINADO

### Casos de prueba - CAMBIO 3 (Herencia automática TAREA_LEVE)

11. **POST TAREA_LEVE sin contacto**: Verificar que response incluye `telefono`, `mail`, `nombreSolicitante` heredados
12. **POST TAREA_LEVE con contacto**: Verificar que usa valores enviados (no hereda)
13. **POST TAREA_LEVE sin dirección**: Verificar herencia de `direccionObraCalle`, `direccionObraAltura`, etc.
14. **GET TAREA_LEVE existente**: Verificar que todos los campos están completos
15. **Comparar presupuesto con obra padre**: Verificar que campos heredados coinciden

**Ejemplo test automatizado:**
```typescript
// Test: Herencia automática en TAREA_LEVE
it('debe heredar contacto de obra padre al crear TAREA_LEVE', async () => {
  const request = {
    tipoPresupuesto: 'TAREA_LEVE',
    trabajoAdicionalId: 123,
    nombreObra: 'Nueva tarea',
    totalPresupuesto: 10000
    // NO enviar: telefono, mail, nombreSolicitante
  };
  
  const response = await api.post('/presupuestos', request);
  
  expect(response.data.telefono).toBe('351-123-4567');  // Heredado
  expect(response.data.mail).toBe('cacho@ejemplo.com'); // Heredado
  expect(response.data.nombreSolicitante).toBe('Cacho'); // Heredado
  expect(response.data.direccionObraCalle).toBe('La Granja'); // Heredado
});
```

---

## 📊 Mapeo completo de valores

### Valores backend → frontend

| Valor en BD/Response | Valor para enviar (POST) | Label para mostrar |
|---------------------|------------------------|-------------------|
| `PRINCIPAL` | `PRINCIPAL` o `PRESUPUESTO_PRINCIPAL` | "Presupuesto Principal" |
| `TRABAJO_DIARIO` | `TRABAJO_DIARIO` o `PRESUPUESTO_TRABAJO_DIARIO` | "Trabajo Diario" |
| `TRABAJO_EXTRA` | `TRABAJO_EXTRA` o `PRESUPUESTO_ADICIONAL_OBRA` | "Trabajo Extra" |
| `TAREA_LEVE` | `TAREA_LEVE` o `PRESUPUESTO_TAREA_LEVE` | "Tarea Leve" |

## ⏰ Timeline de implementación

1. **✅ Completado (backend)**: 
   - Renombrado TRADICIONAL → PRINCIPAL con backward compatibility
   - **TRABAJO_DIARIO ahora requiere aprobación manual** (no auto-aprueba, no crea obra al POST)
   - Herencia automática en TAREA_LEVE (telefono, mail, nombreSolicitante, dirección)
   - Soporte TrabajoAdicional recursivo
   
2. **⚠️ Frontend (próximos días) - ACCIÓN REQUERIDA**: 
   - **CRÍTICO**: Actualizar lógica de TRABAJO_DIARIO (flujo simplificado: BORRADOR → TERMINADO → APROBADO)
   - **CRÍTICO**: NO mostrar estados A_ENVIAR/ENVIADO para TRABAJO_DIARIO (solo BORRADOR → TERMINADO)
   - Actualizar enums/constants (TRADICIONAL → PRINCIPAL)
   - Corregir comparaciones
   - Actualizar labels en UI
   - Simplificar formularios TAREA_LEVE (remover campos que se heredan automáticamente)
   - Testing completo de flujo simplificado TRABAJO_DIARIO

---

## 🆘 Soporte

**Backend:** Equipo backend - construccion-backend  
**Archivos técnicos:**  
- `TipoPresupuesto.java` - Enum con valores y aliases
- `PresupuestoNoClienteService.java` - Lógica de herencia automática (líneas 266-350)
- `TrabajoAdicional.java` - Relación recursiva self-referencing
- `CAMBIO_TIPO_PRESUPUESTO_PRINCIPAL.md` - Este documento

---

## ✅ Checklist Frontend

### Cambio 1: tipo_presupuesto TRADICIONAL → PRINCIPAL
- [ ] Actualizar enum `TipoPresupuesto` (cambiar TRADICIONAL → PRINCIPAL)
### Cambio 2: TRABAJO_DIARIO flujo simplificado ⚠️ CRÍTICO
- [ ] **Actualizar lógica**: TRABAJO_DIARIO ya NO crea obra automáticamente
- [ ] **Flujo simplificado**: BORRADOR → botón "Marcar como terminado" → TERMINADO → botón "Aprobar" → APROBADO + obra
- [ ] **NO mostrar estados intermedios**: A_ENVIAR, ENVIADO no aplican para TRABAJO_DIARIO
- [ ] **Eliminar asunciones** de que TRABAJO_DIARIO tiene obra inmediatamente
- [ ] **Validar estado**: No asumir que TRABAJO_DIARIO siempre está APROBADO
- [ ] **Implementar transición**: BORRADOR → TERMINADO (PUT /presupuestos/{id} con estado: TERMINADO)
- [ ] **Implementar aprobación**: TERMINADO → APROBADO (PUT /presupuestos/{id}/aprobar)
- [ ] Testing: POST TRABAJO_DIARIO retorna BORRADOR sin obraId
- [ ] Testing: Cambiar a TERMINADO funciona sin pasar por A_ENVIAR/ENVIADO
- [ ] Testing: Aprobar desde TERMINADO cambia estado y crea obra
- [ ] Testing: Validar que UI muestra flujo simplificado (2 pasos en lugar de 4+)

### Cambio 3: odas las referencias a `'TRADICIONAL'` en el código
- [ ] Actualizar comparaciones (`===`, `switch`, etc.)
- [ ] Actualizar labels en componentes de UI
- [ ] Revisar filtros/búsquedas por tipo
- [ ] Testing: GET presupuestos muestra "PRINCIPAL"
- [ ] Testing: POST con "PRINCIPAL" funciona
- [ ] Testing: POST con "TRADICIONAL" aún funciona (backward compat)

### Cambio 2: Herencia automática TAREA_LEVE
- [ ] Simplificar formulario creación TAREA_LEVE (remover campos: telefono, mail, nombreSolicitante, dirección)
- [ ] Mostrar campos heredados en UI con indicador visual (ej: "Heredado de obra padre")
- [ ] Permitir sobrescribir campos heredados si usuario lo necesita
- [ Flujo unificado** - PRINCIPAL y TRABAJO_DIARIO comparten la misma lógica (simplifica código)  
✅ **Menos campos obligatorios** en formularios TAREA_LEVE  
✅ **Mejor UX** - Usuario no repite información ya conocida  
✅ **Consistencia de datos** - Garantizada por backend  
✅ **Código más limpio** - Menos validaciones manuales  
✅ **Mayor control** - Todos los presupuestos pasan por aprobación explícita
- [ ] Actualizar documentación interna del frontend
- [ ] Informar al equipo sobre cambios y beneficios

---

## 📈 Beneficios para el frontend

✅ **Flujo simplificado TRABAJO_DIARIO** - Solo 2 pasos (BORRADOR → TERMINADO → APROBADO) vs 4+ pasos de PRINCIPAL  
✅ **Mejor UX para obras rápidas** - Sin estados de negociación innecesarios (A_ENVIAR, ENVIADO)  
✅ **Menos campos obligatorios** en formularios TAREA_LEVE  
✅ **Usuario no repite información** - Herencia automática en TAREA_LEVE  
✅ **Consistencia de datos** - Garantizada por backend  
✅ **Código más limpio** - Menos validaciones manuales  
✅ **Mayor control** - Todos los presupuestos pasan por aprobación explícita

---

**Fecha de aplicación:** 3 de marzo de 2026  
**Versión backend:** 1.0.0  
**Estado:** ✅ Aplicado y compilado en backend - Pendiente implementación en frontend
