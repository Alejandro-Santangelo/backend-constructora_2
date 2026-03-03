# 📋 GUÍA FRONTEND - Presupuestos TAREA_LEVE con TrabajoAdicional

## 🎯 **RESUMEN EJECUTIVO**

El sistema ahora permite que los **presupuestos tipo TAREA_LEVE** puedan estar vinculados a:

1. **Obra Directa** (comportamiento existente)
2. **Trabajo Adicional** (nueva funcionalidad)

### Jerarquía Completa Soportada:

```
Obra Principal
├── Presupuesto TAREA_LEVE (vinculado a obra)
├── Trabajo Adicional 1
│   ├── Presupuesto TAREA_LEVE (vinculado a trabajo adicional)
│   └── Trabajo Adicional 1.1 (hijo)
│       └── Presupuesto TAREA_LEVE (vinculado a trabajo adicional hijo)
└── Trabajo Adicional 2
    └── Presupuesto TAREA_LEVE (vinculado a trabajo adicional)
```

---

## 🔧 **CAMBIOS EN EL MODELO DE DATOS**

### PresupuestoNoCliente - Nuevos Campos

```typescript
interface PresupuestoNoCliente {
  // ... campos existentes ...
  
  // CAMPO NUEVO - Vinculación a TrabajoAdicional
  trabajoAdicionalId?: number | null;  // ✨ NUEVO
  
  // Vinculación existente a Obra
  obraId?: number | null;
  
  // Tipo de presupuesto
  tipoPresupuesto: 'TRADICIONAL' | 'TRABAJO_DIARIO' | 'TRABAJO_EXTRA' | 'TAREA_LEVE' | 'TRABAJOS_SEMANALES';
}
```

### TrabajoAdicional - Nuevos Campos

```typescript
interface TrabajoAdicional {
  // ... campos existentes ...
  
  // COLECCIÓN NUEVA - Presupuestos TAREA_LEVE asociados
  presupuestosTareasLeves?: PresupuestoNoCliente[];  // ✨ NUEVO
}
```

---

## 📡 **ENDPOINTS API**

### 1. Crear Presupuesto TAREA_LEVE vinculado a TrabajoAdicional

**Endpoint:** `POST /api/presupuestos-no-cliente`

**Request Body:**
```json
{
  "tipoPresupuesto": "TAREA_LEVE",
  "trabajoAdicionalId": 123,
  "fechaEmision": "2026-03-03",
  "estado": "BORRADOR",
  "nombreObra": "Reparación urgente pared",
  "descripcion": "Reparación de fisura en pared exterior",
  
  // ⚠️ CAMPOS OPCIONALES (se heredan automáticamente si no se envían)
  "direccionObraCalle": null,    // Se hereda de la obra padre
  "direccionObraAltura": null,   // Se hereda de la obra padre
  "direccionObraPiso": null,     // Se hereda de la obra padre
  "direccionObraDepartamento": null,  // Se hereda de la obra padre
  "direccionObraBarrio": null,   // Se hereda de la obra padre
  "direccionObraTorre": null,    // Se hereda de la obra padre
  
  // CAMPOS DE CONTENIDO (siempre propios del presupuesto)
  "totalPresupuesto": 150000.00,
  "totalPresupuestoConHonorarios": 180000.00,
  "vencimiento": "2026-03-15"
}
```

**Response (201 Created):**
```json
{
  "id": 103,
  "tipoPresupuesto": "TAREA_LEVE",
  "trabajoAdicionalId": 123,
  "obraId": null,
  "empresaId": 1,
  "clienteId": 48,
  "fechaEmision": "2026-03-03",
  "estado": "BORRADOR",
  "nombreObra": "Reparación urgente pared",
  "descripcion": "Reparación de fisura en pared exterior",
  
  // Dirección heredada automáticamente de la obra padre
  "direccionObraCalle": "Av. Libertador",
  "direccionObraAltura": "4500",
  "direccionObraPiso": "3",
  "direccionObraDepartamento": "B",
  "direccionObraBarrio": "Palermo",
  "direccionObraTorre": null,
  
  "totalPresupuesto": 150000.00,
  "totalPresupuestoConHonorarios": 180000.00,
  "vencimiento": "2026-03-15",
  "fechaCreacion": "2026-03-03",
  "numeroPresupuesto": 5
}
```

---

### 2. Listar Presupuestos TAREA_LEVE de un TrabajoAdicional

**Endpoint:** `GET /api/trabajos-adicionales/{id}`

**Response:**
```json
{
  "id": 123,
  "nombre": "Instalación eléctrica adicional",
  "descripcion": "Puntos de luz extra en sala de estar",
  "obraId": 55,
  "empresaId": 1,
  "estado": "EN_PROGRESO",
  
  // ✨ NUEVA COLECCIÓN
  "presupuestosTareasLeves": [
    {
      "id": 103,
      "tipoPresupuesto": "TAREA_LEVE",
      "nombreObra": "Reparación urgente pared",
      "estado": "BORRADOR",
      "totalPresupuestoConHonorarios": 180000.00,
      "fechaEmision": "2026-03-03"
    }
  ],
  
  // Campos existentes
  "importe": 500000.00,
  "fechaInicio": "2026-02-15",
  "profesionales": [...],
  "trabajosAdicionalesHijos": [...]
}
```

---

### 3. Consulta Vista de Reporte (SQL Directo)

**Vista:** `v_presupuestos_tarea_leve_vinculacion`

**Query SQL:**
```sql
SELECT * FROM v_presupuestos_tarea_leve_vinculacion 
ORDER BY presupuesto_id DESC;
```

**Resultado:**
```sql
presupuesto_id | tipo_vinculacion    | obra_id | obra_nombre       | trabajo_adicional_id | cliente_nombre | empresa_nombre | total_final
---------------|---------------------|---------|-------------------|----------------------|----------------|----------------|-------------
103            | TRABAJO_ADICIONAL   | null    | null              | 123                  | Juan Pérez     | Gisel          | 180000.00
102            | OBRA_DIRECTA        | 54      | Casa de Cacho ... | null                 |                | Gisel          | 1500000.00
101            | OBRA_DIRECTA        | 55      | Casa de Cacho ... | null                 |                | Gisel          | 4000000.00
```

---

## 🔒 **VALIDACIONES AUTOMÁTICAS DEL BACKEND**

### Regla XOR (Exclusividad)

Para presupuestos tipo `TAREA_LEVE`, el backend valida:

```
✅ VÁLIDO: obraId = 55, trabajoAdicionalId = null
✅ VÁLIDO: obraId = null, trabajoAdicionalId = 123
❌ INVÁLIDO: obraId = 55, trabajoAdicionalId = 123 (ambos con valor)
❌ INVÁLIDO: obraId = null, trabajoAdicionalId = null (ambos nulos)
```

**Respuesta de Error:**
```json
{
  "timestamp": "2026-03-03T14:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Para presupuestos TAREA_LEVE: debe especificar EXACTAMENTE UNO de (obraId, trabajoAdicionalId)",
  "path": "/api/presupuestos-no-cliente"
}
```

### Validación de Referencias

```javascript
// ❌ ERROR: TrabajoAdicional no existe
{
  "tipoPresupuesto": "TAREA_LEVE",
  "trabajoAdicionalId": 999999  // No existe
}

// Response 404:
{
  "status": 404,
  "message": "TrabajoAdicional con ID 999999 no encontrado"
}
```

---

## 🧬 **HERENCIA AUTOMÁTICA DE DATOS**

### Flujo de Herencia

Cuando se crea un presupuesto TAREA_LEVE vinculado a un `TrabajoAdicional`:

```
TrabajoAdicional (ID: 123)
    └── obraId: 55 ──────────────┐
                                 │
                                 ▼
                         Obra (ID: 55)
                         ├── empresaId: 1 ──────► Se hereda al presupuesto
                         ├── clienteId: 48 ─────► Se hereda al presupuesto
                         ├── direccionObraCalle: "Av. Libertador" ─► Se hereda si no viene en el request
                         ├── direccionObraAltura: "4500" ──────────► Se hereda si no viene en el request
                         ├── direccionObraPiso: "3" ───────────────► Se hereda si no viene en el request
                         ├── direccionObraDepartamento: "B" ───────► Se hereda si no viene en el request
                         ├── direccionObraBarrio: "Palermo" ───────► Se hereda si no viene en el request
                         └── direccionObraTorre: null ─────────────► Se hereda si no viene en el request
```

### Datos Heredados vs Propios

| Campo | ¿Se Hereda? | Origen | ¿Puede Sobreescribirse? |
|-------|-------------|--------|-------------------------|
| `empresaId` | ✅ Sí | Del TrabajoAdicional | ❌ No (automático) |
| `clienteId` | ✅ Sí | De la Obra padre | ❌ No (automático) |
| `direccionObraCalle` | ✅ Sí | De la Obra padre | ✅ Sí (opcional en request) |
| `direccionObraAltura` | ✅ Sí | De la Obra padre | ✅ Sí (opcional en request) |
| `direccionObraPiso` | ✅ Sí | De la Obra padre | ✅ Sí (opcional en request) |
| `direccionObraDepartamento` | ✅ Sí | De la Obra padre | ✅ Sí (opcional en request) |
| `direccionObraBarrio` | ✅ Sí | De la Obra padre | ✅ Sí (opcional en request) |
| `direccionObraTorre` | ✅ Sí | De la Obra padre | ✅ Sí (opcional en request) |
| `nombreObra` | ❌ No | Obligatorio en request | ✅ Sí |
| `descripcion` | ❌ No | Opcional en request | ✅ Sí |
| `totalPresupuesto` | ❌ No | Propio del presupuesto | ✅ Sí |
| `fechaEmision` | ❌ No | Obligatorio en request | ✅ Sí |
| `estado` | ❌ No | Obligatorio en request | ✅ Sí |
| `vencimiento` | ❌ No | Opcional en request | ✅ Sí |

---

## 💡 **CASOS DE USO FRONTEND**

### Caso 1: Crear TAREA_LEVE desde Obra (Existente)

**Escenario:** Usuario en vista de Obra quiere crear una tarea leve.

```typescript
// Frontend - Formulario de Obra
async crearTareaLeveDesdeObra(obraId: number, formData: any) {
  const request = {
    tipoPresupuesto: 'TAREA_LEVE',
    obraId: obraId,  // ✅ Vinculado a obra
    trabajoAdicionalId: null,  // ❌ No vinculado a trabajo adicional
    nombreObra: formData.nombreObra,
    fechaEmision: formData.fechaEmision,
    estado: 'BORRADOR',
    // ... más campos
  };
  
  const response = await fetch('/api/presupuestos-no-cliente', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  });
  
  return response.json();
}
```

---

### Caso 2: Crear TAREA_LEVE desde TrabajoAdicional (Nuevo)

**Escenario:** Usuario en vista de TrabajoAdicional quiere crear una tarea leve.

```typescript
// Frontend - Formulario de TrabajoAdicional
async crearTareaLeveDesdeTrabajoAdicional(trabajoAdicionalId: number, formData: any) {
  const request = {
    tipoPresupuesto: 'TAREA_LEVE',
    obraId: null,  // ❌ No vinculado a obra
    trabajoAdicionalId: trabajoAdicionalId,  // ✅ Vinculado a trabajo adicional
    nombreObra: formData.nombreObra,
    fechaEmision: formData.fechaEmision,
    estado: 'BORRADOR',
    
    // ⚠️ Dirección: Opcional (se hereda automáticamente de la obra padre)
    // Solo incluir si el usuario quiere una dirección diferente
    direccionObraCalle: formData.direccionPersonalizada || undefined,
    
    // Contenido del presupuesto (siempre propio)
    totalPresupuesto: formData.totalPresupuesto,
    totalPresupuestoConHonorarios: formData.totalConHonorarios,
    vencimiento: formData.vencimiento
  };
  
  const response = await fetch('/api/presupuestos-no-cliente', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  });
  
  return response.json();
}
```

---

### Caso 3: Mostrar Presupuestos TAREA_LEVE en Vista de TrabajoAdicional

**Escenario:** Mostrar todos los presupuestos TAREA_LEVE asociados a un trabajo adicional.

```typescript
// Frontend - Componente TrabajoAdicionalDetalle
interface TrabajoAdicionalDetalle {
  id: number;
  nombre: string;
  descripcion: string;
  presupuestosTareasLeves: PresupuestoNoCliente[];
}

async cargarTrabajoAdicionalConPresupuestos(id: number) {
  const response = await fetch(`/api/trabajos-adicionales/${id}`);
  const data: TrabajoAdicionalDetalle = await response.json();
  
  // Renderizar lista de presupuestos
  return {
    trabajo: data,
    cantidadTareasLeves: data.presupuestosTareasLeves?.length || 0,
    tareasLeves: data.presupuestosTareasLeves || []
  };
}
```

**HTML Template:**
```html
<div class="trabajo-adicional-card">
  <h2>{{ trabajo.nombre }}</h2>
  <p>{{ trabajo.descripcion }}</p>
  
  <!-- ✨ Nueva sección: Tareas Leves -->
  <div class="tareas-leves-section">
    <h3>Tareas Leves Asociadas ({{ cantidadTareasLeves }})</h3>
    
    <div *ngFor="let tarea of tareasLeves" class="tarea-leve-item">
      <span class="badge-tarea">TAREA_LEVE</span>
      <h4>{{ tarea.nombreObra }}</h4>
      <p>Estado: {{ tarea.estado }}</p>
      <p>Total: {{ tarea.totalPresupuestoConHonorarios | currency }}</p>
      <p>Fecha: {{ tarea.fechaEmision | date }}</p>
      <button (click)="verDetalleTarea(tarea.id)">Ver Detalle</button>
    </div>
    
    <button class="btn-crear" (click)="crearNuevaTareaLeve()">
      + Nueva Tarea Leve
    </button>
  </div>
</div>
```

---

### Caso 4: Validación Frontend antes de Enviar

**Escenario:** Validar datos del formulario antes de enviar al backend.

```typescript
// Frontend - Validación
function validarPresupuestoTareaLeve(formData: any): string[] {
  const errores: string[] = [];
  
  // Validar XOR: EXACTAMENTE uno de (obraId, trabajoAdicionalId)
  const tieneObra = formData.obraId != null;
  const tieneTrabajo = formData.trabajoAdicionalId != null;
  
  if (formData.tipoPresupuesto === 'TAREA_LEVE') {
    if (!tieneObra && !tieneTrabajo) {
      errores.push('Debe seleccionar una Obra o un Trabajo Adicional');
    }
    if (tieneObra && tieneTrabajo) {
      errores.push('El presupuesto no puede estar vinculado a Obra y Trabajo Adicional al mismo tiempo');
    }
  }
  
  // Validar campos obligatorios
  if (!formData.nombreObra || formData.nombreObra.trim() === '') {
    errores.push('El nombre de la obra es obligatorio');
  }
  if (!formData.fechaEmision) {
    errores.push('La fecha de emisión es obligatoria');
  }
  if (!formData.estado) {
    errores.push('El estado es obligatorio');
  }
  
  return errores;
}

// Uso en el componente
async onSubmit() {
  const errores = validarPresupuestoTareaLeve(this.formData);
  
  if (errores.length > 0) {
    this.mostrarErrores(errores);
    return;
  }
  
  // Enviar al backend
  await this.crearPresupuesto(this.formData);
}
```

---

## 🎨 **COMPONENTES UI SUGERIDOS**

### Componente: Selector de Vinculación

```typescript
// selector-vinculacion-tarea-leve.component.ts
@Component({
  selector: 'app-selector-vinculacion-tarea-leve',
  template: `
    <div class="vinculacion-selector">
      <h4>¿Dónde se realizará esta tarea leve?</h4>
      
      <div class="radio-group">
        <label>
          <input type="radio" 
                 name="tipoVinculacion" 
                 value="obra"
                 [(ngModel)]="tipoVinculacion"
                 (change)="onCambioVinculacion()">
          En la Obra Principal
        </label>
        
        <label>
          <input type="radio" 
                 name="tipoVinculacion" 
                 value="trabajoAdicional"
                 [(ngModel)]="tipoVinculacion"
                 (change)="onCambioVinculacion()">
          En un Trabajo Adicional
        </label>
      </div>
      
      <!-- Selector de Obra -->
      <div *ngIf="tipoVinculacion === 'obra'" class="selector-obra">
        <label>Seleccionar Obra:</label>
        <select [(ngModel)]="obraSeleccionada">
          <option *ngFor="let obra of obrasDisponibles" [value]="obra.id">
            {{ obra.nombre }}
          </option>
        </select>
      </div>
      
      <!-- Selector de Trabajo Adicional -->
      <div *ngIf="tipoVinculacion === 'trabajoAdicional'" class="selector-trabajo">
        <label>Seleccionar Trabajo Adicional:</label>
        <select [(ngModel)]="trabajoAdicionalSeleccionado">
          <option *ngFor="let trabajo of trabajosDisponibles" [value]="trabajo.id">
            {{ trabajo.nombre }}
          </option>
        </select>
        
        <div class="info-herencia" *ngIf="trabajoAdicionalSeleccionado">
          <i class="icon-info"></i>
          <p>
            Se heredarán automáticamente: empresa, cliente y dirección 
            del Trabajo Adicional seleccionado.
          </p>
        </div>
      </div>
    </div>
  `
})
export class SelectorVinculacionTareaLeveComponent {
  tipoVinculacion: 'obra' | 'trabajoAdicional' = 'obra';
  obraSeleccionada: number | null = null;
  trabajoAdicionalSeleccionado: number | null = null;
  
  @Output() vinculacionChange = new EventEmitter<{
    obraId: number | null;
    trabajoAdicionalId: number | null;
  }>();
  
  onCambioVinculacion() {
    if (this.tipoVinculacion === 'obra') {
      this.trabajoAdicionalSeleccionado = null;
      this.vinculacionChange.emit({
        obraId: this.obraSeleccionada,
        trabajoAdicionalId: null
      });
    } else {
      this.obraSeleccionada = null;
      this.vinculacionChange.emit({
        obraId: null,
        trabajoAdicionalId: this.trabajoAdicionalSeleccionado
      });
    }
  }
}
```

---

### Componente: Badge de Tipo de Vinculación

```typescript
// badge-vinculacion.component.ts
@Component({
  selector: 'app-badge-vinculacion',
  template: `
    <span class="badge" [ngClass]="claseBadge">
      <i [class]="iconoBadge"></i>
      {{ textoVinculacion }}
    </span>
  `,
  styles: [`
    .badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 4px 8px;
      border-radius: 4px;
      font-size: 12px;
      font-weight: 600;
    }
    .badge-obra {
      background-color: #e3f2fd;
      color: #1976d2;
    }
    .badge-trabajo {
      background-color: #f3e5f5;
      color: #7b1fa2;
    }
  `]
})
export class BadgeVinculacionComponent {
  @Input() obraId: number | null = null;
  @Input() trabajoAdicionalId: number | null = null;
  
  get claseBadge(): string {
    return this.trabajoAdicionalId ? 'badge-trabajo' : 'badge-obra';
  }
  
  get iconoBadge(): string {
    return this.trabajoAdicionalId ? 'icon-briefcase' : 'icon-building';
  }
  
  get textoVinculacion(): string {
    return this.trabajoAdicionalId ? 'Trabajo Adicional' : 'Obra Principal';
  }
}
```

---

## 📊 **QUERIES SQL ÚTILES PARA REPORTES**

### Todos los Presupuestos TAREA_LEVE con Vinculación

```sql
SELECT * 
FROM v_presupuestos_tarea_leve_vinculacion
ORDER BY fecha_emision DESC;
```

### Solo TAREA_LEVE vinculados a TrabajoAdicional

```sql
SELECT * 
FROM v_presupuestos_tarea_leve_vinculacion
WHERE tipo_vinculacion = 'TRABAJO_ADICIONAL'
ORDER BY presupuesto_id DESC;
```

### Agrupado por Tipo de Vinculación

```sql
SELECT 
    tipo_vinculacion,
    COUNT(*) as cantidad,
    SUM(total_final) as total_acumulado
FROM v_presupuestos_tarea_leve_vinculacion
GROUP BY tipo_vinculacion;
```

### Jerarquía Completa (Obra → Trabajo → Presupuesto)

```sql
SELECT 
    o.nombre AS obra_principal,
    ta.nombre AS trabajo_adicional,
    p.numero_presupuesto,
    p.nombre_obra AS nombre_tarea_leve,
    p.total_presupuesto_con_honorarios,
    p.estado
FROM presupuesto_no_cliente p
LEFT JOIN trabajos_adicionales ta ON p.trabajo_adicional_id = ta.id
LEFT JOIN obras o ON COALESCE(ta.obra_id, p.obra_id) = o.id_obra
WHERE p.tipo_presupuesto = 'TAREA_LEVE'
ORDER BY o.nombre, ta.nombre, p.numero_presupuesto;
```

---

## 🚨 **ERRORES COMUNES Y SOLUCIONES**

### Error 1: Violación de Constraint XOR

```json
// Request INCORRECTO
{
  "tipoPresupuesto": "TAREA_LEVE",
  "obraId": 55,
  "trabajoAdicionalId": 123  // ❌ Ambos con valor
}

// Response 400
{
  "message": "Para presupuestos TAREA_LEVE: debe especificar EXACTAMENTE UNO de (obraId, trabajoAdicionalId)"
}

// SOLUCIÓN: Enviar solo uno de los dos
{
  "tipoPresupuesto": "TAREA_LEVE",
  "obraId": null,
  "trabajoAdicionalId": 123  // ✅ Solo uno
}
```

---

### Error 2: TrabajoAdicional No Existe

```json
// Request con ID inexistente
{
  "tipoPresupuesto": "TAREA_LEVE",
  "trabajoAdicionalId": 999999  // ❌ No existe
}

// Response 404
{
  "status": 404,
  "message": "TrabajoAdicional con ID 999999 no encontrado"
}

// SOLUCIÓN: Verificar que el ID sea válido
async function verificarTrabajoAdicional(id: number): Promise<boolean> {
  try {
    const response = await fetch(`/api/trabajos-adicionales/${id}`);
    return response.ok;
  } catch {
    return false;
  }
}
```

---

### Error 3: Presupuesto Huérfano (sin vinculación)

```json
// Request SIN vinculación
{
  "tipoPresupuesto": "TAREA_LEVE",
  "obraId": null,
  "trabajoAdicionalId": null  // ❌ Ambos nulos
}

// Response 400
{
  "message": "Para presupuestos TAREA_LEVE: debe especificar EXACTAMENTE UNO de (obraId, trabajoAdicionalId)"
}

// SOLUCIÓN: Siempre enviar uno de los dos
{
  "tipoPresupuesto": "TAREA_LEVE",
  "trabajoAdicionalId": 123  // ✅ Al menos uno
}
```

---

## 📝 **CHECKLIST DE IMPLEMENTACIÓN FRONTEND**

### Fase 1: Actualizar Modelos TypeScript
- [ ] Agregar `trabajoAdicionalId?: number | null` a `PresupuestoNoCliente`
- [ ] Agregar `presupuestosTareasLeves?: PresupuestoNoCliente[]` a `TrabajoAdicional`

### Fase 2: Formularios
- [ ] Crear componente `SelectorVinculacionTareaLeveComponent`
- [ ] Actualizar formulario de creación de presupuestos
- [ ] Agregar validación XOR en frontend
- [ ] Mostrar mensaje de herencia automática

### Fase 3: Vistas Detalle
- [ ] Agregar sección "Tareas Leves" en vista de TrabajoAdicional
- [ ] Mostrar lista de presupuestos TAREA_LEVE asociados
- [ ] Agregar botón "Nueva Tarea Leve" en TrabajoAdicional

### Fase 4: Listas y Tablas
- [ ] Agregar columna "Vinculación" en tabla de presupuestos
- [ ] Implementar `BadgeVinculacionComponent`
- [ ] Agregar filtros por tipo de vinculación

### Fase 5: Validaciones
- [ ] Validar XOR antes de enviar request
- [ ] Validar campos obligatorios
- [ ] Mostrar errores de forma amigable

### Fase 6: Testing
- [ ] Crear TAREA_LEVE desde Obra (caso existente)
- [ ] Crear TAREA_LEVE desde TrabajoAdicional (caso nuevo)
- [ ] Verificar herencia automática de datos
- [ ] Probar validaciones de error
- [ ] Verificar visualización en listas y detalles

---

## 🔗 **RECURSOS ADICIONALES**

### Documentación Backend
- Archivo: `PRESUPUESTOS_TAREA_LEVE_TRABAJO_ADICIONAL_README.md`
- Ubicación: `/backend-constructora_2/`

### Scripts SQL Ejecutados
- Script 1: Agregar columna `trabajo_adicional_id`
- Script 2: FK constraint e índice
- Script 3: CHECK constraint XOR
- Script 4: Vista de reporte `v_presupuestos_tarea_leve_vinculacion`

### Endpoints Relacionados
- `POST /api/presupuestos-no-cliente` - Crear presupuesto
- `GET /api/presupuestos-no-cliente/{id}` - Obtener presupuesto
- `PUT /api/presupuestos-no-cliente/{id}` - Actualizar presupuesto
- `GET /api/trabajos-adicionales/{id}` - Obtener trabajo adicional con presupuestos
- `GET /api/obras/{id}` - Obtener obra

---

## 🎯 **EJEMPLO COMPLETO END-TO-END**

### Escenario: Usuario crea una Tarea Leve desde un Trabajo Adicional

#### 1. Frontend: Cargar TrabajoAdicional

```typescript
async cargarTrabajoAdicional(id: number) {
  const response = await fetch(`/api/trabajos-adicionales/${id}`);
  const trabajo = await response.json();
  
  console.log('Trabajo Adicional:', trabajo.nombre);
  console.log('Obra Padre ID:', trabajo.obraId);
  console.log('Presupuestos TAREA_LEVE:', trabajo.presupuestosTareasLeves.length);
}
```

#### 2. Frontend: Usuario completa formulario

```html
<form (ngSubmit)="crearTareaLeve()">
  <h3>Nueva Tarea Leve</h3>
  
  <input type="text" 
         [(ngModel)]="form.nombreObra" 
         placeholder="Nombre de la tarea"
         required>
  
  <textarea [(ngModel)]="form.descripcion" 
            placeholder="Descripción"></textarea>
  
  <input type="number" 
         [(ngModel)]="form.totalPresupuesto"
         placeholder="Total Presupuesto"
         required>
  
  <div class="info-herencia">
    <i class="icon-info"></i>
    <p>
      Se heredarán automáticamente:
      - Empresa: {{ trabajo.empresaId }}
      - Cliente y Dirección de la Obra Padre
    </p>
  </div>
  
  <button type="submit">Crear Tarea Leve</button>
</form>
```

#### 3. Frontend: Enviar Request

```typescript
async crearTareaLeve() {
  const request = {
    tipoPresupuesto: 'TAREA_LEVE',
    trabajoAdicionalId: this.trabajo.id,  // 123
    obraId: null,
    nombreObra: this.form.nombreObra,
    descripcion: this.form.descripcion,
    fechaEmision: new Date().toISOString().split('T')[0],
    estado: 'BORRADOR',
    totalPresupuesto: this.form.totalPresupuesto,
    totalPresupuestoConHonorarios: this.calcularConHonorarios()
  };
  
  const response = await fetch('/api/presupuestos-no-cliente', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(request)
  });
  
  if (response.ok) {
    const presupuesto = await response.json();
    console.log('✅ Presupuesto creado:', presupuesto.id);
    console.log('✅ Cliente heredado:', presupuesto.clienteId);
    console.log('✅ Dirección heredada:', presupuesto.direccionObraCalle);
    this.mostrarExito('Tarea Leve creada exitosamente');
    this.recargarTrabajoAdicional();
  } else {
    const error = await response.json();
    this.mostrarError(error.message);
  }
}
```

#### 4. Backend: Procesamiento (Automático)

```
1. Validar XOR: ✅ Solo trabajoAdicionalId tiene valor
2. Buscar TrabajoAdicional ID 123: ✅ Encontrado
3. Obtener empresa del TrabajoAdicional: empresaId = 1
4. Buscar Obra padre (obraId del TrabajoAdicional): ✅ Obra ID 55
5. Heredar cliente de Obra 55: clienteId = 48
6. Heredar dirección de Obra 55:
   - calle: "Av. Libertador"
   - altura: "4500"
   - piso: "3"
   - departamento: "B"
   - barrio: "Palermo"
7. Crear presupuesto con datos heredados + propios
8. Guardar en BD
9. Retornar presupuesto completo
```

#### 5. Frontend: Mostrar Resultado

```typescript
mostrarPresupuestoCreado(presupuesto: PresupuestoNoCliente) {
  console.log('═══════════════════════════════════════');
  console.log('✅ PRESUPUESTO TAREA_LEVE CREADO');
  console.log('═══════════════════════════════════════');
  console.log(`ID: ${presupuesto.id}`);
  console.log(`Nombre: ${presupuesto.nombreObra}`);
  console.log(`Vinculado a: Trabajo Adicional ${presupuesto.trabajoAdicionalId}`);
  console.log('');
  console.log('DATOS HEREDADOS:');
  console.log(`  Empresa: ${presupuesto.empresaId}`);
  console.log(`  Cliente: ${presupuesto.clienteId}`);
  console.log(`  Dirección: ${presupuesto.direccionObraCalle} ${presupuesto.direccionObraAltura}`);
  console.log('');
  console.log('DATOS PROPIOS:');
  console.log(`  Total: $${presupuesto.totalPresupuestoConHonorarios}`);
  console.log(`  Estado: ${presupuesto.estado}`);
  console.log(`  Fecha: ${presupuesto.fechaEmision}`);
  console.log('═══════════════════════════════════════');
}
```

---

## ✅ **CONCLUSIÓN**

Esta implementación permite una jerarquía flexible donde:

1. **Obras** pueden tener presupuestos TAREA_LEVE directamente (existente)
2. **Trabajos Adicionales** pueden tener sus propios presupuestos TAREA_LEVE (nuevo)
3. Los datos de contexto (empresa, cliente, dirección) se **heredan automáticamente**
4. Cada presupuesto mantiene su **contenido propio** (totales, fechas, descripciones)
5. La validación XOR garantiza **integridad relacional** en base de datos

El frontend debe implementar:
- Selector de vinculación (Obra vs TrabajoAdicional)
- Validación XOR antes de enviar requests
- Visualización de presupuestos en vistas de TrabajoAdicional
- Badges/indicadores de tipo de vinculación
- Manejo de errores específicos

---

**Fecha de generación:** 3 de marzo de 2026  
**Versión:** 1.0  
**Backend status:** ✅ Compilado y listo para usar
