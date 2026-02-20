# Sistema de Borradores para Trabajos Adicionales 📋

## Descripción General

El sistema de borradores permite crear y modificar trabajos adicionales por etapas, guardando automáticamente todos los datos del formulario (incluido el desglose completo de honorarios y descuentos) para que no se pierdan entre sesiones.

## 🔧 Funcionalidades Implementadas

### 1. Crear Borrador
- **Endpoint**: `POST /api/trabajos-adicionales/borrador`
- **Descripción**: Crea un trabajo adicional en estado `BORRADOR`
- **Persistencia**: Todos los campos del formulario se guardan inmediatamente
- **Estado inicial**: `BORRADOR`

### 2. Actualizar Borrador  
- **Endpoint**: `PUT /api/trabajos-adicionales/borrador/{id}`
- **Descripción**: Actualiza cualquier campo del borrador
- **Restricción**: Solo funciona si el trabajo está en estado `BORRADOR`
- **Uso**: Permite guardar cambios incrementales del formulario

### 3. Confirmar Borrador
- **Endpoint**: `POST /api/trabajos-adicionales/borrador/{id}/confirmar`  
- **Descripción**: Convierte el borrador en trabajo adicional activo
- **Validaciones**: Verifica datos mínimos requeridos
- **Estado final**: `PENDIENTE`

### 4. Listar Borradores
- **Endpoint**: `GET /api/trabajos-adicionales/borradores?empresaId={id}&obraId={id}&trabajoExtraId={id}`
- **Descripción**: Obtiene todos los borradores con filtros opcionales
- **Filtros**: Por empresa (obligatorio), obra o trabajo extra (opcionales)

## 📋 Flujo de Uso Recomendado

### Frontend (Integración Sugerida)

```javascript
// 1. Crear borrador al abrir el modal de trabajo adicional
async function crearBorradorTrabajoAdicional(obraId) {
    const response = await fetch('/api/trabajos-adicionales/borrador', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            empresaId: 1,
            obraId: obraId, // o trabajoExtraId si viene de trabajo extra
            nombre: "Borrador - " + new Date().toISOString(),
            importe: 0, // Campos mínimos
            // ... todos los campos de desglose opcionales
        })
    });
    return response.json();
}

// 2. Auto-guardar cada vez que cambie un input
async function actualizarBorradorTrabajoAdicional(borradorId, nuevosDatos) {
    await fetch(`/api/trabajos-adicionales/borrador/${borradorId}`, {
        method: 'PUT', 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(nuevosDatos)
    });
}

// 3. Confirmar trabajo adicional cuando esté listo
async function confirmarTrabajoAdicional(borradorId) {
    const response = await fetch(`/api/trabajos-adicionales/borrador/${borradorId}/confirmar`, {
        method: 'POST'
    });
    return response.json();
}

// 4. Listar borradores existentes
async function obtenerBorradoresTrabajoAdicional(empresaId, obraId = null) {
    const params = new URLSearchParams({ empresaId });
    if (obraId) params.append('obraId', obraId);
    
    const response = await fetch(`/api/trabajos-adicionales/borradores?${params}`);
    return response.json();
}
```

### Estrategia de Auto-Guardado

```javascript
// Debounced auto-save específico para trabajos adicionales
const debouncedSaveTrabajoAdicional = debounce(async (borradorId, datos) => {
    try {
        await actualizarBorradorTrabajoAdicional(borradorId, datos);
        mostrarIndicadorGuardado(); // ✅ "Guardado automáticamente"
    } catch (error) {
        mostrarIndicadorError(); // ❌ "Error al guardar" 
    }
}, 1000); 

// Escuchar cambios específicos en el formulario de trabajos adicionales
document.querySelector('#form-trabajo-adicional').addEventListener('input', (e) => {
    const formData = new FormData(e.currentTarget);
    const datos = Object.fromEntries(formData);
    
    // Conversión automática de campos numéricos
    ['importe', 'importeJornales', 'importeMateriales', 'importeGastosGenerales',
     'honorarioJornales', 'honorarioMateriales', 'descuentoJornales', 'descuentoMateriales',
     // ... más campos numéricos del desglose
    ].forEach(campo => {
        if (datos[campo]) datos[campo] = parseFloat(datos[campo]);
    });
    
    debouncedSaveTrabajoAdicional(window.borradorTrabajoAdicionalId, datos);
});
```

## 🎨 UI/UX Recomendaciones

### Indicadores Visuales
- **Estado Borrador**: Mostrar badge "🟡 BORRADOR" 
- **Auto-guardado**: Spinner y texto "Guardando..." → ✅ "Guardado"
- **Botón Confirmar**: Destacado cuando el borrador esté completo

### Formulario de Trabajo Adicional con Persistencia
```html
<!-- Ejemplo de formulario persistente -->
<div class="trabajo-adicional-header">
    <span class="badge badge-warning">🟡 BORRADOR</span>
    <div class="auto-save-indicator">
        <span id="save-status-trabajo">✅ Guardado automáticamente</span>
    </div>
</div>

<form id="form-trabajo-adicional">
    <!-- Información Básica -->
    <section class="info-basica">
        <input type="text" name="nombre" placeholder="Nombre del trabajo adicional" required />
        <textarea name="descripcion" placeholder="Descripción detallada"></textarea>
        <input type="date" name="fechaInicio" />
        <input type="date" name="fechaFin" />
        <input type="number" name="importe" placeholder="Importe Total" step="0.01" />
    </section>

    <!-- Desglose de Importes Base -->
    <section class="importes-base">
        <h3>💰 Importes Base</h3>
        <input type="number" name="importeJornales" placeholder="Jornales" step="0.01" />
        <input type="number" name="importeMateriales" placeholder="Materiales" step="0.01" />
        <input type="number" name="importeGastosGenerales" placeholder="Gastos Generales" step="0.01" />
    </section>

    <!-- Honorarios por Categoría (4 categorías x 2 campos = 8 campos) -->
    <section class="honorarios-categoria">
        <h3>🎯 Honorarios por Categoría</h3>
        
        <!-- Jornales -->
        <div class="categoria-group">
            <label>Honorarios Jornales:</label>
            <input type="number" name="honorarioJornales" placeholder="Valor" step="0.01" />
            <select name="tipoHonorarioJornales">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>

        <!-- Materiales -->
        <div class="categoria-group">
            <label>Honorarios Materiales:</label>
            <input type="number" name="honorarioMateriales" placeholder="Valor" step="0.01" />
            <select name="tipoHonorarioMateriales">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>

        <!-- Gastos Generales -->
        <div class="categoria-group">
            <label>Honorarios Gastos Generales:</label>
            <input type="number" name="honorarioGastosGenerales" placeholder="Valor" step="0.01" />
            <select name="tipoHonorarioGastosGenerales">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>

        <!-- Mayores Costos -->
        <div class="categoria-group">
            <label>Honorarios Mayores Costos:</label>
            <input type="number" name="honorarioMayoresCostos" placeholder="Valor" step="0.01" />
            <select name="tipoHonorarioMayoresCostos">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>
    </section>

    <!-- Descuentos sobre Importes Base -->
    <section class="descuentos-base">
        <h3>📉 Descuentos sobre Importes Base</h3>
        
        <!-- Descuento Jornales -->
        <div class="categoria-group">
            <label>Descuento Jornales:</label>
            <input type="number" name="descuentoJornales" placeholder="Valor" step="0.01" />
            <select name="tipoDescuentoJornales">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>

        <!-- Descuento Materiales -->
        <div class="categoria-group">
            <label>Descuento Materiales:</label>
            <input type="number" name="descuentoMateriales" placeholder="Valor" step="0.01" />
            <select name="tipoDescuentoMateriales">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>

        <!-- Repetir para Gastos Generales y Mayores Costos... -->
    </section>

    <!-- Descuentos sobre Honorarios -->
    <section class="descuentos-honorarios">
        <h3>📉 Descuentos sobre Honorarios</h3>
        
        <!-- Descuento Honorario Jornales -->
        <div class="categoria-group">
            <label>Descuento Honorario Jornales:</label>
            <input type="number" name="descuentoHonorarioJornales" placeholder="Valor" step="0.01" />
            <select name="tipoDescuentoHonorarioJornales">
                <option value="fijo">Monto Fijo</option>
                <option value="porcentaje">Porcentaje</option>
            </select>
        </div>

        <!-- Repetir para otras categorías... -->
    </section>
    
    <div class="form-actions">
        <button type="button" onclick="confirmarTrabajoAdicional()">
            🚀 Confirmar Trabajo Adicional
        </button>
    </div>
</form>
```

## 🔍 Estados del Trabajo Adicional

| Estado | Descripción | Acciones Permitidas |
|--------|-------------|-------------------|
| `BORRADOR` | Trabajo en construcción | ✅ Crear, ✅ Actualizar, ✅ Confirmar |
| `PENDIENTE` | Trabajo confirmado, listo para iniciar | ❌ No editable como borrador |
| `EN_PROGRESO` | Trabajo en ejecución | ❌ No editable como borrador |
| `COMPLETADO` | Trabajo finalizado | ❌ No editable como borrador |
| `CANCELADO` | Trabajo cancelado | ❌ No editable como borrador |

## 🗃️ Estructura de Datos Persistida

Todos estos campos se guardan automáticamente en el borrador:

### Información Básica
- `nombre`, `descripcion`, `fechaInicio`, `fechaFin`
- `importe` (total), `obraId`, `trabajoExtraId`, `empresaId`

### Importes Base
- `importeJornales`, `importeMateriales`, `importeGastosGenerales`
- `importeHonorarios`, `tipoHonorarios`
- `importeMayoresCostos`, `tipoMayoresCostos`

### Honorarios Individuales por Categoría (8 campos)
- `honorarioJornales`, `tipoHonorarioJornales`
- `honorarioMateriales`, `tipoHonorarioMateriales`  
- `honorarioGastosGenerales`, `tipoHonorarioGastosGenerales`
- `honorarioMayoresCostos`, `tipoHonorarioMayoresCostos`

### Descuentos sobre Importes Base (8 campos)
- `descuentoJornales`, `tipoDescuentoJornales`
- `descuentoMateriales`, `tipoDescuentoMateriales`
- `descuentoGastosGenerales`, `tipoDescuentoGastosGenerales`
- `descuentoMayoresCostos`, `tipoDescuentoMayoresCostos`

### Descuentos sobre Honorarios (8 campos)
- `descuentoHonorarioJornales`, `tipoDescuentoHonorarioJornales`
- `descuentoHonorarioMateriales`, `tipoDescuentoHonorarioMateriales`
- `descuentoHonorarioGastosGenerales`, `tipoDescuentoHonorarioGastosGenerales`
- `descuentoHonorarioMayoresCostos`, `tipoDescuentoHonorarioMayoresCostos`

**Total: 33 campos persistidos automáticamente** ✨

## ⚠️ Validaciones Implementadas

### Para Confirmar Borrador:
- ✅ Nombre del trabajo adicional no debe estar vacío
- ✅ Importe total es obligatorio
- ✅ Al menos un importe base debe ser mayor a 0 (jornales, materiales o gastos generales)

### Para Actualizar Borrador:
- ✅ Solo permite actualización si estado = `BORRADOR`
- ✅ Todos los campos son opcionales (permite guardado parcial)

## 🚀 Ventajas del Sistema

1. **Persistencia Total**: Todos los 33 campos se guardan automáticamente
2. **Trabajo por Etapas**: Permite completar el trabajo en múltiples sesiones  
3. **No Pérdida de Datos**: Protección contra cierres accidentales del navegador
4. **Experiencia Fluida**: Auto-guardado transparente para el usuario
5. **Desglose Completo**: Soporte completo para honorarios y descuentos complejos
6. **Validación Flexible**: Datos mínimos solo se requieren al confirmar

## 🔗 Integración con Obras y Trabajos Extra

### Desde Obras
```javascript
// Crear trabajo adicional vinculado a obra
const borrador = await crearBorradorTrabajoAdicional({
    obraId: 123,
    empresaId: 1,
    // ... otros campos
});
```

### Desde Trabajos Extra
```javascript
// Crear trabajo adicional vinculado a trabajo extra
const borrador = await crearBorradorTrabajoAdicional({
    trabajoExtraId: 456,
    empresaId: 1,
    // ... otros campos
});
```

## 🔨 Implementación Técnica

### Backend
- ✅ Nuevos endpoints específicos para borradores
- ✅ Métodos `esBorrador()`, `estaPendiente()`, `esEditable()` en la entidad
- ✅ Validaciones de estado antes de operaciones
- ✅ Constantes de estado para mejor manejo de códigos

### Base de Datos
- ✅ Usa el campo `estado` existente con valor `'BORRADOR'`
- ✅ Aprovecha todas las columnas de honorarios y descuentos de la migración V7
- ✅ Nuevos métodos de búsqueda optimizados para borradores

### Compatibilidad
- ✅ No afecta funcionalidades existentes de trabajos adicionales
- ✅ Funciona con el sistema actual de estados
- ✅ Integración con entidades financieras
- ✅ Soporte para vinculación con obras y trabajos extra

¡El sistema de borradores para trabajos adicionales está completo y listo para usar! 🎉

Ahora puedes trabajar en el formulario de trabajo adicional por etapas sin perder ningún dato de los honorarios y descuentos complejos.