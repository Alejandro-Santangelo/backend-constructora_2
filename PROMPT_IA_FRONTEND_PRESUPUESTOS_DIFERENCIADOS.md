# 🤖 PROMPT PARA IA DEL FRONTEND - Sistema de Presupuestos Diferenciados

> **⚡ Última actualización**: 28 de febrero de 2026 — Correcciones críticas tras verificación end-to-end del backend.

---

## 📋 CONTEXTO DE LA IMPLEMENTACIÓN

El backend tiene un **sistema diferenciado de presupuestos** con 4 tipos activos. Cada tipo tiene reglas distintas de validación, estados, y comportamiento sobre obras.

---

## 🗂️ TABLA RÁPIDA DE REFERENCIA

| Tipo (técnico) | Alias frontend | Estado inicial | ¿Crea obra? | ¿Requiere `idObra`? |
|---|---|---|---|---|
| `TRADICIONAL` | `PRESUPUESTO_PRINCIPAL` | `BORRADOR` | ✅ Al aprobar (Obra Principal) | ❌ null |
| `TRABAJO_DIARIO` | `PRESUPUESTO_TRABAJO_DIARIO` | `APROBADO` | ✅ Inmediatamente | ❌ null |
| `TRABAJO_EXTRA` | `PRESUPUESTO_ADICIONAL_OBRA` | `BORRADOR` | ✅ Al aprobar (**Sub-Obra**) | ✅ obligatorio |
| `TAREA_LEVE` | `PRESUPUESTO_TAREA_LEVE` | `BORRADOR` | ✅ Su propia obra (BORRADOR→TERMINADO) | ✅ obligatorio (obra padre) |

> ⚠️ **El backend acepta TANTO el nombre técnico como el alias.** Por ejemplo, enviar `"tipoPresupuesto": "PRESUPUESTO_PRINCIPAL"` es equivalente a `"TRADICIONAL"`.

---

## 🎯 OBJETIVO

Modificar el formulario de creación/edición de presupuestos para:
1. Permitir seleccionar entre 4 tipos de presupuestos
2. Mostrar/ocultar campos dinámicamente según el tipo seleccionado
3. Validar datos ANTES de enviar al backend para evitar errores
4. Proporcionar feedback claro al usuario sobre las reglas de cada tipo

---

## 📊 LOS 4 TIPOS DE PRESUPUESTOS

### **TIPO 1: TRADICIONAL** (Flujo estándar)
- **Código**: `"TRADICIONAL"` / alias `"PRESUPUESTO_PRINCIPAL"`
- **Comportamiento**: 
  - Se crea en estado `BORRADOR`
  - Requiere aprobación manual del usuario (botón "Aprobar y Crear Obra" cuando está en ENVIADO)
  - Al aprobar, se crea automáticamente una **Obra Principal** nueva
- **Campo `idObra`**: ❌ **DEBE SER `null`** (NO seleccionar obra existente)
- **Campos obligatorios**:
  - ✅ `nombreObra` (nombre de la obra que se creará)
  - ✅ `direccionObraCalle` (calle de la obra)
  - ✅ `direccionObraAltura` (número de calle)
- **Uso típico**: Presupuestos normales para obras nuevas

---

### **TIPO 2: TRABAJO_DIARIO** (Aprobación automática + obra inmediata)
- **Código**: `"TRABAJO_DIARIO"` / alias `"PRESUPUESTO_TRABAJO_DIARIO"`
- **Comportamiento**: 
  - Se crea directamente en estado `APROBADO` (sin pasar por borrador)
  - Se crea la **Obra Principal** inmediatamente al guardar (sin esperar aprobación)
- **Campo `idObra`**: ❌ **DEBE SER `null`**
- **Campos obligatorios**:
  - ✅ `nombreObra`
  - ✅ `direccionObraCalle`
  - ✅ `direccionObraAltura`
- **Uso típico**: Trabajos del día que se ejecutan inmediatamente

---

### **TIPO 3: TRABAJO_EXTRA** (Trabajo adicional de obra existente → crea Sub-Obra)
- **Código**: `"TRABAJO_EXTRA"` / alias `"PRESUPUESTO_ADICIONAL_OBRA"`
- **Comportamiento**: 
  - Se crea en estado `BORRADOR`
  - Se vincula a una **obra PADRE existente** (Obra Principal o Sub-Obra)
  - El cliente se **hereda automáticamente** de la obra padre
  - **NO** crea obra al crearse. Al aprobar `→` **crea una Sub-Obra** vinculada a la obra padre
  - La sub-obra tiene `obraOrigenId = id_obra_padre` y `esObraTrabajoExtra = true`
- **Campo `idObra`**: ✅ **OBLIGATORIO** — debe ser el ID de una obra EXISTENTE (la obra padre)
- **Campo `idCliente`**: ⚠️ No enviar — se hereda automáticamente de la obra padre
- **Campos de dirección**: No obligatorios al crear — se usan para nombrar la sub-obra
- **Para aprobar**: Usar `POST /api/presupuestos-no-cliente/{id}/aprobar-y-crear-obra`
- **Uso típico**: Ampliación, reforma o trabajo adicional en una obra ya en curso

---

### **TIPO 4: TAREA_LEVE** (Tarea menor con obra propia, flujo BORRADOR→TERMINADO)
- **Código**: `"TAREA_LEVE"` / alias `"PRESUPUESTO_TAREA_LEVE"`
- **Comportamiento**: 
  - Se crea en estado `BORRADOR` (el usuario puede editarlo antes de finalizar)
  - Al guardarse, **crea su propia obra independiente** en estado `BORRADOR`
  - La nueva obra queda vinculada a la **obra padre** mediante `obraOrigenId`
  - El cliente se **hereda automáticamente** de la obra padre
  - El usuario finaliza la tarea → cambia estado del presupuesto a `TERMINADO` → la obra sincroniza a `TERMINADO`
  - La obra propia permite tracking financiero independiente (cobros/pagos separados del padre)
- **Campo `idObra`**: ✅ **OBLIGATORIO** — es la **obra padre** (puede ser Obra Principal **o** Sub-Obra creada por TRABAJO_EXTRA)
- **Campo `idCliente`**: ⚠️ No enviar — se hereda automáticamente
- **Campos de dirección**: No obligatorios (puede heredar los del padre visualmente)
- **Flujo de estados**: `BORRADOR` → (usuario edita) → `TERMINADO`
- **Uso típico**: Arreglos menores, mantenimientos, tareas ligeras. Tanto en obras principales como en sub-obras de TRABAJO_EXTRA

---

## 🔴 REGLAS DE VALIDACIÓN CRÍTICAS

### ❌ **ERROR 409 - Validaciones que debes implementar**

#### **Validación 1: Tipos TRADICIONAL y TRABAJO_DIARIO NO deben tener `idObra`**
```javascript
if (tipoPresupuesto === 'TRADICIONAL' || tipoPresupuesto === 'TRABAJO_DIARIO') {
  if (presupuesto.idObra !== null && presupuesto.idObra !== undefined) {
    mostrarError('Presupuestos tipo ' + tipoPresupuesto + ' no deben tener obra asociada. Debe crear una obra nueva.');
    return false; // NO ENVIAR AL BACKEND
  }
}
```

#### **Validación 2: Tipos TRADICIONAL y TRABAJO_DIARIO requieren campos de obra**
```javascript
if (tipoPresupuesto === 'TRADICIONAL' || tipoPresupuesto === 'TRABAJO_DIARIO') {
  if (!presupuesto.nombreObra || presupuesto.nombreObra.trim() === '') {
    mostrarError('El campo "Nombre de Obra" es obligatorio para crear una obra nueva.');
    return false;
  }
  
  if (!presupuesto.direccionObraCalle || presupuesto.direccionObraCalle.trim() === '') {
    mostrarError('El campo "Calle" es obligatorio para crear una obra nueva.');
    return false;
  }
  
  if (!presupuesto.direccionObraAltura || presupuesto.direccionObraAltura.trim() === '') {
    mostrarError('El campo "Altura/Número" es obligatorio para crear una obra nueva.');
    return false;
  }
}
```

#### **Validación 3: Tipos TRABAJO_EXTRA y TAREA_LEVE requieren `idObra`**
```javascript
if (tipoPresupuesto === 'TRABAJO_EXTRA' || tipoPresupuesto === 'TAREA_LEVE') {
  if (!presupuesto.idObra) {
    mostrarError('Presupuestos tipo ' + tipoPresupuesto + ' requieren seleccionar una obra existente.');
    return false;
  }
}
```

---

## 🎨 CAMBIOS EN LA INTERFAZ DE USUARIO

### **1. Agregar Selector de Tipo de Presupuesto**

**Ubicación**: Al inicio del formulario, antes de los demás campos.

**Implementación sugerida**:

```html
<!-- Radio buttons con información explicativa -->
<div class="tipo-presupuesto-selector">
  <h3>Tipo de Presupuesto *</h3>
  
  <div class="tipo-option">
    <input type="radio" id="tipo-tradicional" name="tipoPresupuesto" value="TRADICIONAL" checked>
    <label for="tipo-tradicional">
      <strong>Tradicional</strong>
      <span class="descripcion">Presupuesto estándar que crea una obra nueva</span>
      <span class="badge">Requiere aprobación</span>
    </label>
  </div>
  
  <div class="tipo-option">
    <input type="radio" id="tipo-trabajo-diario" name="tipoPresupuesto" value="TRABAJO_DIARIO">
    <label for="tipo-trabajo-diario">
      <strong>Trabajo Diario</strong>
      <span class="descripcion">Se aprueba automáticamente y crea obra inmediatamente</span>
      <span class="badge badge-success">Aprobación automática</span>
    </label>
  </div>
  
  <div class="tipo-option">
    <input type="radio" id="tipo-trabajo-extra" name="tipoPresupuesto" value="TRABAJO_EXTRA">
    <label for="tipo-trabajo-extra">
      <strong>Trabajo Extra</strong>
      <span class="descripcion">Trabajo adicional vinculado a obra existente</span>
      <span class="badge badge-info">Requiere seleccionar obra</span>
    </label>
  </div>
  
  <div class="tipo-option">
    <input type="radio" id="tipo-tarea-leve" name="tipoPresupuesto" value="TAREA_LEVE">
    <label for="tipo-tarea-leve">
      <strong>Tarea Leve</strong>
      <span class="descripcion">Tarea menor con aprobación automática en obra existente</span>
      <span class="badge badge-success">Aprobación automática</span>
    </label>
  </div>
</div>
```

---

### **2. Lógica de Mostrar/Ocultar Campos Dinámicamente**

**Cuando el usuario cambia el tipo de presupuesto**:

```javascript
function onTipoPresupuestoChange(tipoSeleccionado) {
  const esObraNueva = tipoSeleccionado === 'TRADICIONAL' || tipoSeleccionado === 'TRABAJO_DIARIO';
  const esTrabajoExtra = tipoSeleccionado === 'TRABAJO_EXTRA' || tipoSeleccionado === 'TAREA_LEVE';
  
  // 1. Selector de obra existente
  if (esTrabajoExtra) {
    mostrarCampo('selectorObra');
    marcarObligatorio('selectorObra', true);
    ocultarCampo('selectorCliente'); // Cliente se hereda de la obra
  } else {
    ocultarCampo('selectorObra');
    marcarObligatorio('selectorObra', false);
    mostrarCampo('selectorCliente'); // Permitir seleccionar cliente
  }
  
  // 2. Campos de dirección de obra
  if (esObraNueva) {
    mostrarCampo('nombreObra');
    mostrarCampo('direccionObraCalle');
    mostrarCampo('direccionObraAltura');
    marcarObligatorio('nombreObra', true);
    marcarObligatorio('direccionObraCalle', true);
    marcarObligatorio('direccionObraAltura', true);
    
    mostrarMensajeInformativo('Este presupuesto creará una nueva obra.');
  } else {
    // Para trabajos extra, nombreObra es solo descriptivo
    mostrarCampo('nombreObra'); 
    deshabilitarCampos(['direccionObraCalle', 'direccionObraAltura']); // No necesarios
    marcarObligatorio('nombreObra', false);
    marcarObligatorio('direccionObraCalle', false);
    marcarObligatorio('direccionObraAltura', false);
    
    mostrarMensajeInformativo('Este presupuesto se vinculará a la obra seleccionada.');
  }
  
  // 3. Mensaje de aprobación automática
  if (tipoSeleccionado === 'TRABAJO_DIARIO' || tipoSeleccionado === 'TAREA_LEVE') {
    mostrarAlerta('info', 'Este presupuesto se aprobará automáticamente al guardarlo.');
  }
}
```

---

### **3. Validación Antes de Enviar al Backend**

```javascript
function validarFormularioPresupuesto(presupuesto) {
  const tipo = presupuesto.tipoPresupuesto || 'TRADICIONAL';
  
  // Validaciones según tipo
  if (tipo === 'TRADICIONAL' || tipo === 'TRABAJO_DIARIO') {
    // VALIDACIÓN 1: No debe tener idObra
    if (presupuesto.idObra !== null && presupuesto.idObra !== undefined) {
      return {
        valido: false,
        error: `Los presupuestos tipo ${tipo} no deben tener obra asociada. El sistema creará una obra nueva automáticamente.`,
        campo: 'idObra'
      };
    }
    
    // VALIDACIÓN 2: Campos obligatorios para crear obra
    if (!presupuesto.nombreObra || presupuesto.nombreObra.trim() === '') {
      return {
        valido: false,
        error: 'El campo "Nombre de Obra" es obligatorio para presupuestos que crean obra nueva.',
        campo: 'nombreObra'
      };
    }
    
    if (!presupuesto.direccionObraCalle || presupuesto.direccionObraCalle.trim() === '') {
      return {
        valido: false,
        error: 'El campo "Calle" es obligatorio para presupuestos que crean obra nueva.',
        campo: 'direccionObraCalle'
      };
    }
    
    if (!presupuesto.direccionObraAltura || presupuesto.direccionObraAltura.trim() === '') {
      return {
        valido: false,
        error: 'El campo "Altura/Número" es obligatorio para presupuestos que crean obra nueva.',
        campo: 'direccionObraAltura'
      };
    }
  }
  
  if (tipo === 'TRABAJO_EXTRA' || tipo === 'TAREA_LEVE') {
    // VALIDACIÓN 3: Debe tener idObra
    if (!presupuesto.idObra) {
      return {
        valido: false,
        error: `Los presupuestos tipo ${tipo} requieren seleccionar una obra existente.`,
        campo: 'idObra'
      };
    }
  }
  
  return { valido: true };
}

// Usar antes de enviar
function guardarPresupuesto() {
  const validacion = validarFormularioPresupuesto(presupuestoActual);
  
  if (!validacion.valido) {
    mostrarError(validacion.error);
    enfocarCampo(validacion.campo);
    return;
  }
  
  // Continuar con el envío al backend
  enviarAlBackend(presupuestoActual);
}
```

---

## 📤 ESTRUCTURA DEL REQUEST AL BACKEND

### **Endpoint crear presupuesto**: `POST /api/presupuestos-no-cliente`
> ⚠️ La URL correcta es `/api/presupuestos-no-cliente` (sin `/v1/` y sin `?empresaId`)

### **Campos obligatorios siempre** (NOT NULL en BD, sin default):
| Campo | Tipo | Notas |
|---|---|---|
| `idEmpresa` | Long | ID de la empresa |
| `tipoPresupuesto` | String | Ver tabla de tipos arriba |
| `modoPresupuesto` | String | Usar el tipo como valor, ej: `"TRADICIONAL"` |
| `calculoAutomaticoDiasHabiles` | boolean | Enviar `false` si no se usa |
| `direccionObraCalle` | String | ⚠️ NOT NULL aunque sea trabajos extra |
| `direccionObraAltura` | String | ⚠️ NOT NULL aunque sea trabajos extra |

### **Request Body - Ejemplo TRADICIONAL**:
```json
{
  "tipoPresupuesto": "TRADICIONAL",
  "modoPresupuesto": "TRADICIONAL",
  "calculoAutomaticoDiasHabiles": false,
  "idEmpresa": 1,
  "idCliente": 5,
  "idObra": null,
  
  "nombreObra": "Ampliación Casa Rodríguez",
  "direccionObraCalle": "Av. Libertador",
  "direccionObraAltura": "1234",
  "direccionObraBarrio": "Palermo",
  "direccionObraPiso": "",
  "direccionObraDepartamento": "",
  "direccionObraTorre": "",
  
  "nombreSolicitante": "Juan Rodríguez",
  "telefono": "+54 9 11 1234-5678",
  "mail": "juan@example.com",
  "vencimiento": "2026-03-12",
  "fechaProbableInicio": "2026-03-15",
  
  "totalPresupuesto": 150000.0,
  "totalPresupuestoConHonorarios": 165000.0,
  "materialesList": [],
  "profesionales": []
}
```

### **Request Body - Ejemplo TRABAJO_EXTRA** (vinculado a obra padre existente):
```json
{
  "tipoPresupuesto": "TRABAJO_EXTRA",
  "modoPresupuesto": "TRADICIONAL",
  "calculoAutomaticoDiasHabiles": false,
  "idEmpresa": 1,
  "idObra": 46,
  
  "nombreObra": "Cabaña 2 - Pintura adicional",
  "direccionObraCalle": "La Granja",
  "direccionObraAltura": "193",
  
  "vencimiento": "2026-03-05",
  "totalPresupuesto": 80000.0,
  "totalPresupuestoConHonorarios": 88000.0,
  "materialesList": [],
  "profesionales": []
}
```

> ℹ️ Para TRABAJO_EXTRA y TAREA_LEVE, **NO enviar `idCliente`** — se hereda automáticamente de la obra.

### **Request Body - Ejemplo TAREA_LEVE** (crea obra propia, estado BORRADOR):
```json
{
  "tipoPresupuesto": "TAREA_LEVE",
  "modoPresupuesto": "TAREA_LEVE",
  "calculoAutomaticoDiasHabiles": false,
  "idEmpresa": 1,
  "idObra": 46,

  "nombreObra": "Reparación cañeria baño",
  "direccionObraCalle": "La Granja",
  "direccionObraAltura": "193",

  "totalPresupuesto": 15000.0,
  "totalPresupuestoConHonorarios": 15000.0
}
```

> **Resultado**: Presupuesto en `BORRADOR` + obra propia creada en `BORRADOR` con `obraOrigenId = 46`  
> Para finalizar: `PATCH /api/presupuestos-no-cliente/{id}/estado` con `{"estado": "TERMINADO"}` → la obra sincroniza a `TERMINADO`

---

## 🔄 FLUJO DE APROBACIÓN PARA TRABAJO_EXTRA

> ⚠️ Para `TRABAJO_EXTRA`, el endpoint `/aprobar-y-crear-obra` es distinto al de `TRADICIONAL`. Ambos usan el mismo endpoint pero el comportamiento varía según el tipo.

### **Endpoint aprobar**: `POST /api/presupuestos-no-cliente/{id}/aprobar-y-crear-obra`

**Condiciones para llamarlo**:  
- El presupuesto debe estar en estado `ENVIADO` (no BORRADOR)
- Para pasar de BORRADOR → ENVIADO existe un endpoint de transición de estado

**Respuesta exitosa** (`200 OK`):
```json
{
  "obraId": 47,
  "obraCreada": true,
  "mensaje": "Sub-obra creada exitosamente para el trabajo extra",
  "obraPadreId": 46,
  "nombreSubObra": "Cabaña 2 - Pintura adicional",
  "presupuestosActualizados": 1,
  "clienteReutilizado": true,
  "clienteId": 47
}
```

**Qué crea el backend**:
- Una **Sub-Obra** con `estado=APROBADO`, vinculada al padre mediante `obra_origen_id`
- La sub-obra tiene `es_obra_trabajo_extra = true`
- El cliente y empresa se heredan de la obra padre

---

## 🗄️ TABLA DE ITEMS — DATO CRÍTICO PARA EL FRONTEND

> 🚨 **TODOS** los items de la calculadora (para CUALQUIER tipo de presupuesto) se guardan en `items_calculadora_presupuesto` (FK: `presupuesto_no_cliente_id`).

La tabla `trabajos_extra_items_calculadora` es de una entidad SEPARADA (`TrabajoExtra`, endpoint `/api/v1/trabajos-extra`) y **NO tiene relación** con los presupuesto diferenciados. No buscar ahí los items de un `PresupuestoNoCliente`.

---

## 🔴 REGLAS DE VALIDACIÓN CRÍTICAS

---

## 🚨 MANEJO DE ERRORES

### **Error 409 - Conflict**

Hay dos causas distintas que producen 409:

**Causa A — DataIntegrityViolationException** (campo obligatorio nulo):
```json
{
  "status": 409,
  "error": "Error de Integridad",
  "message": "Error de integridad de datos. Verifique que los datos no estén duplicados..."
}
```
- La causa más común: olvidar `modoPresupuesto` o `calculoAutomaticoDiasHabiles` en el payload
- **Solución**: Incluir siempre esos dos campos en todos los requests de creación

**Causa B — IllegalStateException** (conflicto de reglas de negocio):
```json
{
  "status": 409,
  "message": "ERROR: Presupuestos tipo TRABAJO_EXTRA requieren obraId obligatorio."
}
```
- **Solución frontend**: Validar antes de enviar (ver sección de validaciones)

### **Mensajes de error del backend**:

1. `"ERROR: Presupuestos tipo TRADICIONAL no deben tener obraId."` → Limpiar `idObra = null`
2. `"ERROR: nombreObra es obligatorio..."` → Validar campo
3. `"ERROR: Presupuestos tipo TRABAJO_EXTRA requieren obraId obligatorio."` → Seleccionar obra
4. `"ERROR: Obra con ID 999 no existe."` → ID de obra inválido
5. `"Error de integridad de datos..."` → Revisar campos NOT NULL (`modoPresupuesto`, `calculoAutomaticoDiasHabiles`, etc.)

**Implementación del manejo de errores**:

```javascript
async function enviarAlBackend(presupuesto) {
  try {
    const response = await fetch('/api/presupuestos-no-cliente', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Empresa-Id': presupuesto.idEmpresa
      },
      body: JSON.stringify(presupuesto)
    });
    
    if (response.status === 409) {
      const error = await response.json();
      mostrarError('Error: ' + error.message);
      return;
    }
    
    if (!response.ok) {
      throw new Error('Error al guardar presupuesto: ' + response.status);
    }
    
    const presupuestoGuardado = await response.json();
    mostrarExito('Presupuesto creado. Estado: ' + presupuestoGuardado.estado);
    
    // Para TRABAJO_DIARIO y TAREA_LEVE → ya está APROBADO, mostrar info de obra
    if (presupuestoGuardado.estado === 'APROBADO' && presupuestoGuardado.obra) {
      mostrarInfo('Obra vinculada: ' + presupuestoGuardado.obra.nombre);
    }
    
    redirigirAListado();
    
  } catch (error) {
    console.error('Error:', error);
    mostrarError('Error al guardar el presupuesto.');
  }
}

// Para aprobar TRABAJO_EXTRA (presupuesto debe estar en ENVIADO)
async function aprobarYCrearSubObra(presupuestoId) {
  const response = await fetch(`/api/presupuestos-no-cliente/${presupuestoId}/aprobar-y-crear-obra`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' }
  });
  
  if (response.ok) {
    const resultado = await response.json();
    // resultado.obraId → ID de la sub-obra creada
    // resultado.obraPadreId → ID de la obra padre
    // resultado.nombreSubObra → nombre de la sub-obra
    mostrarExito(`Sub-Obra "${resultado.nombreSubObra}" creada (ID: ${resultado.obraId})`);
  }
}
```

---

## 📋 CHECKLIST DE IMPLEMENTACIÓN

### **Paso 1: UI - Selector de Tipo**
- [ ] Agregar radio buttons o dropdown para seleccionar tipo de presupuesto
- [ ] Marcar "TRADICIONAL" como opción por defecto
- [ ] Mostrar descripciones claras de cada tipo
- [ ] Agregar badges visuales (ej: "Aprobación automática", "Requiere obra")

### **Paso 2: Lógica de Campos Dinámicos**
- [ ] Implementar función `onTipoPresupuestoChange()`
- [ ] Mostrar/ocultar selector de obra según tipo
- [ ] Habilitar/deshabilitar campos de dirección según tipo
- [ ] Marcar campos como obligatorios/opcionales dinámicamente

### **Paso 3: Validaciones Frontend**
- [ ] Implementar función `validarFormularioPresupuesto()`
- [ ] Validar que TRADICIONAL/TRABAJO_DIARIO tengan `idObra = null`
- [ ] Validar que TRADICIONAL/TRABAJO_DIARIO tengan campos de obra completos
- [ ] Validar que TRABAJO_EXTRA/TAREA_LEVE tengan obra seleccionada
- [ ] Mostrar mensajes de error claros y específicos

### **Paso 4: Preparación del Request**
- [ ] Asegurar que `tipoPresupuesto` se envía correctamente
- [ ] Para TRADICIONAL/TRABAJO_DIARIO: forzar `idObra = null`
- [ ] Para TRABAJO_EXTRA/TAREA_LEVE: NO enviar `idCliente` (se hereda)
- [ ] Limpiar campos no necesarios según tipo

### **Paso 5: Manejo de Errores**
- [ ] Capturar errores 409 específicamente
- [ ] Mostrar mensajes de error del backend al usuario
- [ ] Enfocar el campo problemático en caso de error
- [ ] Logging de errores para depuración

### **Paso 6: Feedback al Usuario**
- [ ] Mensaje informativo cuando selecciona cada tipo
- [ ] Alerta cuando selecciona tipos con aprobación automática
- [ ] Confirmación de éxito al guardar
- [ ] Indicadores visuales de campos obligatorios según tipo

---

## 🎨 ESTILOS CSS SUGERIDOS

```css
/* Selector de tipo de presupuesto */
.tipo-presupuesto-selector {
  margin-bottom: 30px;
  padding: 20px;
  border: 2px solid #e0e0e0;
  border-radius: 8px;
  background: #f9f9f9;
}

.tipo-option {
  margin: 15px 0;
  padding: 15px;
  border: 2px solid #ddd;
  border-radius: 6px;
  background: white;
  transition: all 0.3s ease;
  cursor: pointer;
}

.tipo-option:hover {
  border-color: #4CAF50;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.tipo-option input[type="radio"]:checked + label {
  font-weight: bold;
  color: #4CAF50;
}

.tipo-option label {
  display: flex;
  flex-direction: column;
  gap: 5px;
  cursor: pointer;
}

.tipo-option .descripcion {
  font-size: 0.9em;
  color: #666;
}

.tipo-option .badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.8em;
  background: #2196F3;
  color: white;
  width: fit-content;
}

.tipo-option .badge-success {
  background: #4CAF50;
}

.tipo-option .badge-info {
  background: #FF9800;
}

/* Mensajes informativos */
.mensaje-informativo {
  padding: 12px 16px;
  margin: 15px 0;
  border-radius: 6px;
  background: #E3F2FD;
  border-left: 4px solid #2196F3;
  color: #1565C0;
}

.alerta-auto-aprobacion {
  background: #E8F5E9;
  border-left-color: #4CAF50;
  color: #2E7D32;
}

/* Campos obligatorios dinámicos */
.campo-obligatorio::after {
  content: " *";
  color: red;
  font-weight: bold;
}

.campo-deshabilitado {
  opacity: 0.5;
  pointer-events: none;
}
```

---

## 🧪 CASOS DE PRUEBA

### **Test 1: Crear Presupuesto TRADICIONAL**
1. Seleccionar tipo "TRADICIONAL"
2. Verificar que selector de obra está oculto
3. Completar `nombreObra`, `direccionObraCalle`, `direccionObraAltura`, `modoPresupuesto`, `calculoAutomaticoDiasHabiles`
4. Guardar
5. **Resultado esperado**: Presupuesto creado en estado `BORRADOR`, sin obra

### **Test 2: Crear Presupuesto TRABAJO_DIARIO**
1. Seleccionar tipo "TRABAJO_DIARIO"
2. Verificar que selector de obra está oculto
3. Completar campos de obra y `modoPresupuesto`, `calculoAutomaticoDiasHabiles`
4. Guardar
5. **Resultado esperado**: Presupuesto creado en estado `APROBADO` + Obra Principal creada inmediatamente

### **Test 3: Crear Presupuesto TRABAJO_EXTRA y aprobarlo**
1. Seleccionar tipo "TRABAJO_EXTRA"
2. Verificar que selector de obra está visible y obligatorio
3. Seleccionar obra existente (ej: Obra Principal ID 46)
4. Guardar
5. **Resultado esperado 3a**: Presupuesto en estado `BORRADOR`, vinculado a obra 46, cliente heredado
6. Cambiar estado a ENVIADO (si aplica el flujo del sistema)
7. Llamar `POST /api/presupuestos-no-cliente/{id}/aprobar-y-crear-obra`
8. **Resultado esperado 3b**: Sub-Obra creada, estado `APROBADO`, con `obraOrigenId = 46`, `esObraTrabajoExtra = true`

### **Test 4: Crear Presupuesto TAREA_LEVE (vinculado a Obra Principal)**
1. Seleccionar tipo "TAREA_LEVE"
2. Seleccionar obra padre existente (Obra Principal ej: ID 46)
3. Completar `nombreObra`, `modoPresupuesto`, `calculoAutomaticoDiasHabiles`, `direccionObraCalle`, `direccionObraAltura`
4. Guardar
5. **Resultado esperado**: Presupuesto en `BORRADOR` + obra propia nueva con `obraOrigenId = 46`
6. Editar si necesario, luego cambiar estado a `TERMINADO`
7. **Resultado esperado**: Presupuesto `TERMINADO` + obra sincronizada a `TERMINADO`

### **Test 5: Crear Presupuesto TAREA_LEVE (vinculado a Sub-Obra de TRABAJO_EXTRA)**
1. Seleccionar tipo "TAREA_LEVE"
2. Seleccionar obra padre existente (**Sub-Obra** ej: ID 47, creada desde TRABAJO_EXTRA)
3. Guardar
4. **Resultado esperado**: Presupuesto en `BORRADOR` + obra propia nueva con `obraOrigenId = 47` (sub-obra padre)

### **Test 5: Validación - TRADICIONAL sin campos de obra**
1. Seleccionar tipo "TRADICIONAL"
2. Dejar `nombreObra` vacío
3. Intentar guardar
4. **Resultado esperado**: Error de validación "nombreObra es obligatorio"

### **Test 6: Verificar que items se guardan en la tabla correcta**
Al crear cualquier presupuesto con items → verificar en BD:
```sql
-- ✅ CORRECTO: Items siempre en esta tabla
SELECT * FROM items_calculadora_presupuesto WHERE presupuesto_no_cliente_id = {id};

-- ❌ INCORRECTO (esta tabla es para otra entidad)
-- NO buscar items de PresupuestoNoCliente en trabajos_extra_items_calculadora
```

---

## 📞 RESUMEN PARA LA IA DEL FRONTEND

**IMPLEMENTA LO SIGUIENTE**:

1. **Selector de tipo con 4 opciones**: `TRADICIONAL`, `TRABAJO_DIARIO`, `TRABAJO_EXTRA`, `TAREA_LEVE`
   - El backend también acepta los aliases: `PRESUPUESTO_PRINCIPAL`, `PRESUPUESTO_TRABAJO_DIARIO`, `PRESUPUESTO_ADICIONAL_OBRA`, `PRESUPUESTO_TAREA_LEVE`

2. **Lógica dinámica**:
   - TRADICIONAL/TRABAJO_DIARIO → Sin `idObra`, campos de dirección obligatorios
   - TRABAJO_EXTRA/TAREA_LEVE → `idObra` obligatorio, cliente oculto (se hereda)

3. **Campos SIEMPRE requeridos en el request** (NOT NULL, sin default en BD):
   - `modoPresupuesto` → usar el mismo valor que `tipoPresupuesto` (ej: `"TRADICIONAL"`)
   - `calculoAutomaticoDiasHabiles` → enviar `false` si no aplica
   - `direccionObraCalle` y `direccionObraAltura` → enviar aunque sea para TRABAJO_EXTRA/TAREA_LEVE

4. **Flujo de aprobación**:
   - TRADICIONAL/TRABAJO_EXTRA: Requieren aprobación manual → `POST /api/presupuestos-no-cliente/{id}/aprobar-y-crear-obra`
   - TRABAJO_DIARIO: Se aprueba automáticamente al crearse
   - TAREA_LEVE: Comienza en BORRADOR, el usuario lo marca TERMINADO cuando finaliza

5. **Items de calculadora**: Siempre en tabla `items_calculadora_presupuesto`. **NUNCA** en `trabajos_extra_items_calculadora`

6. **TRABAJO_EXTRA aprobado** → crea Sub-Obra con:
   - `estado = APROBADO`
   - `obraOrigenId = id_obra_padre`
   - `esObraTrabajoExtra = true`
   - La response incluye: `obraId`, `obraPadreId`, `nombreSubObra`, `obraCreada: true`

7. **TAREA_LEVE** nuevo comportamiento:
   - Crea su **propia obra** (con `obraOrigenId` = ID del padre)
   - `idObra` del request = obra PADRE (Obra Principal **o** Sub-Obra de TRABAJO_EXTRA)
   - Estado inicial: `BORRADOR` (el usuario puede editar)
   - Flujo: `BORRADOR` → (editar) → `TERMINADO` (presupuesto + su obra se sincronizan)
   - Tiene tracking financiero independiente (cobros/pagos separados del padre)

8. **Endpoint correcto**: `/api/presupuestos-no-cliente` (NO `/api/v1/presupuestos-no-cliente`)

---

**DOCUMENTACIÓN COMPLETA**: Ver archivos adjuntos
- `FRONTEND_GUIA_PRESUPUESTOS_DIFERENCIADOS.md` - Guía técnica detallada
- `INFORME_COMPATIBILIDAD_TIPO_PRESUPUESTO.md` - Análisis de compatibilidad
- `SISTEMA_PRESUPUESTOS_DIFERENCIADOS_README.md` - README del sistema

---

**Última actualización**: 28 de febrero de 2026  
**Versión Backend**: 2.2 — TAREA_LEVE genera obra propia con flujo BORRADOR→TERMINADO  
**Cambios**: TAREA_LEVE ahora inicia en BORRADOR, crea obra propia con obraOrigenId=padre (compatible con Obra Principal y Sub-Obra de TRABAJO_EXTRA)
