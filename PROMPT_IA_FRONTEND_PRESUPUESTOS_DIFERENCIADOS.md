# 🤖 PROMPT PARA IA DEL FRONTEND - Sistema de Presupuestos Diferenciados

---

## 📋 CONTEXTO DE LA IMPLEMENTACIÓN

El backend ha implementado un **sistema diferenciado de presupuestos** con 4 tipos distintos, cada uno con reglas de validación específicas. Necesitas adaptar el frontend para soportar esta nueva funcionalidad.

---

## 🎯 OBJETIVO

Modificar el formulario de creación/edición de presupuestos para:
1. Permitir seleccionar entre 4 tipos de presupuestos
2. Mostrar/ocultar campos dinámicamente según el tipo seleccionado
3. Validar datos ANTES de enviar al backend para evitar errores 409
4. Proporcionar feedback claro al usuario sobre las reglas de cada tipo

---

## 📊 LOS 4 TIPOS DE PRESUPUESTOS

### **TIPO 1: TRADICIONAL** (Flujo estándar)
- **Código**: `"TRADICIONAL"`
- **Comportamiento**: 
  - Se crea en estado `A_ENVIAR` (borrador)
  - Requiere aprobación manual del usuario
  - Al aprobar, se crea automáticamente una **obra nueva**
- **Campo `idObra`**: ❌ **DEBE SER `null`** (NO seleccionar obra existente)
- **Campos obligatorios**:
  - ✅ `nombreObra` (nombre de la obra que se creará)
  - ✅ `direccionObraCalle` (calle de la obra)
  - ✅ `direccionObraAltura` (número de calle)
- **Uso típico**: Presupuestos normales para obras nuevas

---

### **TIPO 2: TRABAJO_DIARIO** (Aprobación automática + obra inmediata)
- **Código**: `"TRABAJO_DIARIO"`
- **Comportamiento**: 
  - Se crea directamente en estado `APROBADO` (sin pasar por borrador)
  - Se crea la **obra inmediatamente** al guardar (sin esperar aprobación)
- **Campo `idObra`**: ❌ **DEBE SER `null`**
- **Campos obligatorios**:
  - ✅ `nombreObra`
  - ✅ `direccionObraCalle`
  - ✅ `direccionObraAltura`
- **Uso típico**: Trabajos del día que se ejecutan inmediatamente

---

### **TIPO 3: TRABAJO_EXTRA** (Trabajo adicional de obra existente)
- **Código**: `"TRABAJO_EXTRA"`
- **Comportamiento**: 
  - Se crea en estado `A_ENVIAR` (borrador)
  - Se vincula a una **obra existente** (obra padre)
  - El cliente se **hereda automáticamente** de la obra padre
  - **NO** crea una obra nueva
- **Campo `idObra`**: ✅ **OBLIGATORIO** (debe seleccionar obra existente)
- **Campo `idCliente`**: ⚠️ No enviarlo (se ignora, se hereda de la obra)
- **Campos de dirección**: No obligatorios (se ignoran)
- **Uso típico**: Presupuestos de trabajos extras/adicionales en una obra en curso

---

### **TIPO 4: TAREA_LEVE** (Tarea menor con aprobación automática)
- **Código**: `"TAREA_LEVE"`
- **Comportamiento**: 
  - Se crea directamente en estado `APROBADO` (sin borrador)
  - Se vincula a una **obra existente**
  - El cliente se **hereda automáticamente** de la obra padre
  - **NO** crea una obra nueva
- **Campo `idObra`**: ✅ **OBLIGATORIO** (debe seleccionar obra existente)
- **Campo `idCliente`**: ⚠️ No enviarlo (se ignora, se hereda de la obra)
- **Campos de dirección**: No obligatorios (se ignoran)
- **Uso típico**: Arreglos menores, mantenimientos, tareas ligeras en obra existente

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

### **Endpoint**: `POST /api/v1/presupuestos-no-cliente?empresaId={id}`

### **Request Body - Ejemplo TRADICIONAL**:
```json
{
  "tipoPresupuesto": "TRADICIONAL",
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
  
  "materialesList": [],
  "profesionales": []
}
```

### **Request Body - Ejemplo TRABAJO_EXTRA**:
```json
{
  "tipoPresupuesto": "TRABAJO_EXTRA",
  "idEmpresa": 1,
  "idObra": 123,
  
  "nombreObra": "Trabajo extra - Pintura adicional",
  
  "nombreSolicitante": "María López",
  "telefono": "+54 9 11 2222-3333",
  "mail": "maria@example.com",
  "vencimiento": "2026-03-05",
  
  "materialesList": [
    {
      "idMaterial": 15,
      "cantidad": 20.0,
      "precioUnitario": 250.0
    }
  ],
  "profesionales": []
}
```

**NOTA IMPORTANTE**: Para TRABAJO_EXTRA y TAREA_LEVE, **NO enviar `idCliente`** - se hereda automáticamente de la obra.

---

## 🚨 MANEJO DE ERRORES

### **Error 409 - Conflict**

**Causa**: Violación de reglas de validación del backend.

**Mensajes de error que puede devolver el backend**:

1. `"ERROR: Presupuestos tipo TRADICIONAL no deben tener obraId. Debe ser NULL para crear obra nueva."`
   - **Solución frontend**: Asegurar que `idObra = null` para TRADICIONAL/TRABAJO_DIARIO

2. `"ERROR: nombreObra es obligatorio para presupuestos que crean obra nueva."`
   - **Solución frontend**: Validar que `nombreObra` tenga texto

3. `"ERROR: direccionObraCalle es obligatorio para presupuestos que crean obra nueva."`
   - **Solución frontend**: Validar que `direccionObraCalle` tenga texto

4. `"ERROR: direccionObraAltura es obligatorio para presupuestos que crean obra nueva."`
   - **Solución frontend**: Validar que `direccionObraAltura` tenga texto

5. `"ERROR: Presupuestos tipo TRABAJO_EXTRA requieren obraId obligatorio."`
   - **Solución frontend**: Asegurar que se seleccionó una obra para TRABAJO_EXTRA/TAREA_LEVE

6. `"ERROR: Obra con ID 999 no existe."`
   - **Solución frontend**: Validar que la obra seleccionada existe antes de enviar

**Implementación del manejo de errores**:

```javascript
async function enviarAlBackend(presupuesto) {
  try {
    const response = await fetch('/api/v1/presupuestos-no-cliente?empresaId=' + empresaId, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'X-Empresa-Id': empresaId
      },
      body: JSON.stringify(presupuesto)
    });
    
    if (response.status === 409) {
      // Error de validación
      const error = await response.json();
      mostrarError('Error de validación: ' + error.message);
      return;
    }
    
    if (!response.ok) {
      throw new Error('Error al guardar presupuesto');
    }
    
    const presupuestoGuardado = await response.json();
    mostrarExito('Presupuesto creado exitosamente');
    redirigirAListado();
    
  } catch (error) {
    console.error('Error:', error);
    mostrarError('Error al guardar el presupuesto. Por favor, inténtelo nuevamente.');
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
3. Completar nombreObra, direccionObraCalle, direccionObraAltura
4. Guardar
5. **Resultado esperado**: Presupuesto creado en estado A_ENVIAR

### **Test 2: Crear Presupuesto TRABAJO_DIARIO**
1. Seleccionar tipo "TRABAJO_DIARIO"
2. Verificar que selector de obra está oculto
3. Completar campos de obra
4. Guardar
5. **Resultado esperado**: Presupuesto creado en estado APROBADO + obra creada inmediatamente

### **Test 3: Crear Presupuesto TRABAJO_EXTRA**
1. Seleccionar tipo "TRABAJO_EXTRA"
2. Verificar que selector de obra está visible y obligatorio
3. Seleccionar obra existente
4. Guardar
5. **Resultado esperado**: Presupuesto vinculado a obra seleccionada + cliente heredado de obra

### **Test 4: Validación - TRADICIONAL sin campos de obra**
1. Seleccionar tipo "TRADICIONAL"
2. Dejar nombreObra vacío
3. Intentar guardar
4. **Resultado esperado**: Error de validación "nombreObra es obligatorio"

### **Test 5: Validación - TRABAJO_EXTRA sin obra seleccionada**
1. Seleccionar tipo "TRABAJO_EXTRA"
2. No seleccionar obra
3. Intentar guardar
4. **Resultado esperado**: Error de validación "requiere seleccionar obra"

---

## 📞 RESUMEN PARA LA IA DEL FRONTEND

**IMPLEMENTA LO SIGUIENTE**:

1. **Selector de tipo de presupuesto** con 4 opciones: TRADICIONAL, TRABAJO_DIARIO, TRABAJO_EXTRA, TAREA_LEVE

2. **Lógica dinámica**:
   - TRADICIONAL/TRABAJO_DIARIO → Ocultar selector de obra, mostrar campos de dirección obligatorios
   - TRABAJO_EXTRA/TAREA_LEVE → Mostrar selector de obra obligatorio, ocultar cliente

3. **Validaciones cliente**:
   - TRADICIONAL/TRABAJO_DIARIO: `idObra = null`, campos de obra obligatorios
   - TRABAJO_EXTRA/TAREA_LEVE: `idObra` obligatorio

4. **Request al backend**:
   - Incluir campo `"tipoPresupuesto": "TRADICIONAL"` (o el tipo seleccionado)
   - Para TRABAJO_EXTRA/TAREA_LEVE: NO enviar `idCliente`

5. **Manejo de errores 409**: Capturar y mostrar mensajes específicos del backend

6. **Feedback visual**: Mensajes informativos, badges, indicadores de campos obligatorios

---

**DOCUMENTACIÓN COMPLETA**: Ver archivos adjuntos
- `FRONTEND_GUIA_PRESUPUESTOS_DIFERENCIADOS.md` - Guía técnica detallada
- `INFORME_COMPATIBILIDAD_TIPO_PRESUPUESTO.md` - Análisis de compatibilidad

---

**Última actualización**: 26 de febrero de 2026  
**Versión Backend**: 2.0 - Sistema Diferenciado  
**Autor**: Sistema Backend Constructora
