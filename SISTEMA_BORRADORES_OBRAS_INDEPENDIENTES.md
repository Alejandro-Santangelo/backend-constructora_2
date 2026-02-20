# Sistema de Borradores para Obras Independientes 📝

## Descripción General

El sistema de borradores permite crear y modificar obras independientes por etapas, guardando automáticamente todos los datos del formulario (incluido el desglose del presupuesto) para que no se pierdan entre sesiones.

## 🔧 Funcionalidades Implementadas

### 1. Crear Borrador
- **Endpoint**: `POST /api/obras/borrador`
- **Descripción**: Crea una obra independiente en estado `BORRADOR`
- **Persistencia**: Todos los campos del formulario se guardan inmediatamente
- **Estado inicial**: `BORRADOR`

### 2. Actualizar Borrador  
- **Endpoint**: `PUT /api/obras/borrador/{id}`
- **Descripción**: Actualiza cualquier campo del borrador
- **Restricción**: Solo funciona si la obra está en estado `BORRADOR`
- **Uso**: Permite guardar cambios incrementales del formulario

### 3. Confirmar Borrador
- **Endpoint**: `POST /api/obras/borrador/{id}/confirmar`  
- **Descripción**: Convierte el borrador en obra activa
- **Validaciones**: Verifica datos mínimos requeridos
- **Estado final**: `A_ENVIAR`

### 4. Listar Borradores
- **Endpoint**: `GET /api/obras/borradores?empresaId={id}`
- **Descripción**: Obtiene todos los borradores de una empresa
- **Filtro**: Solo obras independientes en estado `BORRADOR`

## 📋 Flujo de Uso Recomendado

### Frontend (Integración Sugerida)

```javascript
// 1. Crear borrador al abrir el modal
async function crearBorradorObra() {
    const response = await fetch('/api/obras/borrador', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            empresaId: 1,
            nombre: "Borrador - " + new Date().toISOString(),
            direccionObraCalle: "", // Campos mínimos
            direccionObraAltura: "",
            // ... otros campos opcionales
        })
    });
    return response.json();
}

// 2. Auto-guardar cada vez que cambie un input
async function actualizarBorrador(borradorId, nuevosDatos) {
    await fetch(`/api/obras/borrador/${borradorId}`, {
        method: 'PUT', 
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(nuevosDatos)
    });
}

// 3. Confirmar obra cuando esté lista
async function confirmarObra(borradorId) {
    const response = await fetch(`/api/obras/borrador/${borradorId}/confirmar`, {
        method: 'POST'
    });
    return response.json();
}
```

### Estrategia de Auto-Guardado

```javascript
// Debounced auto-save (recomendado)
const debouncedSave = debounce(async (borradorId, datos) => {
    try {
        await actualizarBorrador(borradorId, datos);
        mostrarIndicadorGuardado(); // ✅ "Guardado"
    } catch (error) {
        mostrarIndicadorError(); // ❌ "Error al guardar" 
    }
}, 1000); // Espera 1 segundo después del último cambio

// Escuchar cambios en inputs
document.querySelectorAll('input, select, textarea').forEach(input => {
    input.addEventListener('input', (e) => {
        const formData = new FormData(document.querySelector('#form-obra'));
        const datos = Object.fromEntries(formData);
        debouncedSave(window.borradorId, datos);
    });
});
```

## 🎨 UI/UX Recomendaciones

### Indicadores Visuales
- **Estado Borrador**: Mostrar badge "🟡 BORRADOR" 
- **Auto-guardado**: Spinner y texto "Guardando..." → ✅ "Guardado"
- **Botón Confirmar**: Destacado cuando el borrador esté completo

### Formulario Progresivo
```html
<!-- Ejemplo de indicadores visuales -->
<div class="obra-header">
    <span class="badge badge-warning">🟡 BORRADOR</span>
    <div class="auto-save-indicator">
        <span id="save-status">✅ Guardado automáticamente</span>
    </div>
</div>

<form id="form-obra">
    <!-- Todos los campos del desglose del presupuesto -->
    <section class="presupuesto-desglose">
        <h3>💰 Desglose del Presupuesto</h3>
        
        <!-- Estos valores se persisten automáticamente -->
        <input type="number" name="presupuestoJornales" placeholder="Jornales" />
        <input type="number" name="presupuestoMateriales" placeholder="Materiales" />
        <input type="number" name="importeGastosGeneralesObra" placeholder="Gastos Generales" />
        
        <!-- Honorarios individuales por categoría -->
        <input type="number" name="honorarioJornalesObra" placeholder="Honorario Jornales" />
        <select name="tipoHonorarioJornalesObra">
            <option value="fijo">Monto Fijo</option>
            <option value="porcentaje">Porcentaje</option>
        </select>
        
        <!-- ... más campos de honorarios y descuentos ... -->
    </section>
    
    <div class="form-actions">
        <button type="button" onclick="confirmarObra()">
            🚀 Confirmar Obra
        </button>
    </div>
</form>
```

## 🔍 Estados de la Obra

| Estado | Descripción | Acciones Permitidas |
|--------|-------------|-------------------|
| `BORRADOR` | Obra en construcción | ✅ Crear, ✅ Actualizar, ✅ Confirmar |
| `A_ENVIAR` | Obra confirmada | ❌ No editable como borrador |

## 🗃️ Estructura de Datos Persistida

Todos estos campos se guardan automáticamente en el borrador:

### Información Básica
- `nombre`, `direccionObra*`, `fechaInicio`, `fechaFin`
- `descripcion`, `observaciones`

### Desglose del Presupuesto  
- `presupuestoJornales`, `presupuestoMateriales`
- `importeGastosGeneralesObra`
- `presupuestoHonorarios`, `tipoHonorarioPresupuesto`

### Honorarios por Categoría
- `honorarioJornalesObra`, `tipoHonorarioJornalesObra`
- `honorarioMaterialesObra`, `tipoHonorarioMaterialesObra`  
- `honorarioGastosGeneralesObra`, `tipoHonorarioGastosGeneralesObra`
- `honorarioMayoresCostosObra`, `tipoHonorarioMayoresCostosObra`

### Descuentos sobre Importes
- `descuentoJornalesObra`, `tipoDescuentoJornalesObra`
- `descuentoMaterialesObra`, `tipoDescuentoMaterialesObra`
- `descuentoGastosGeneralesObra`, `tipoDescuentoGastosGeneralesObra`

### Descuentos sobre Honorarios
- `descuentoHonorarioJornalesObra`, `tipoDescuentoHonorarioJornalesObra`
- `descuentoHonorarioMaterialesObra`, `tipoDescuentoHonorarioMaterialesObra`
- `descuentoHonorarioGastosGeneralesObra`, `tipoDescuentoHonorarioGastosGeneralesObra`

## ⚠️ Validaciones Implementadas

### Para Confirmar Borrador:
- ✅ Nombre de obra no debe estar vacío
- ✅ Dirección (calle) es obligatoria  
- ✅ Altura/número es obligatorio

### Para Actualizar Borrador:
- ✅ Solo permite actualización si estado = `BORRADOR`
- ✅ Todos los campos son opcionales (permite guardado parcial)

## 🚀 Ventajas del Sistema

1. **Persistencia Total**: Todos los campos se guardan automáticamente
2. **Trabajo por Etapas**: Permite completar la obra en múltiples sesiones  
3. **No Pérdida de Datos**: Protección contra cierres accidentales del navegador
4. **Experiencia Fluida**: Auto-guardado transparente para el usuario
5. **Validación Flexible**: Datos mínimos solo se requieren al confirmar

## 🔨 Implementación Técnica

### Backend
- ✅ Nuevos endpoints específicos para borradores
- ✅ Método `esBorrador()` en la entidad `Obra`  
- ✅ Validaciones de estado antes de operaciones
- ✅ Índices de base de datos para performance

### Base de Datos
- ✅ Usa el campo `estado` existente con valor `'BORRADOR'`
- ✅ Aprovecha `esObraManual = true` para obras independientes
- ✅ Nuevos índices para optimizar consultas

### Compatibilidad
- ✅ No afecta funcionalidades existentes
- ✅ Funciona con el sistema actual de estados
- ✅ Integración con entidades financieras

¡El sistema está listo para usar! 🎉