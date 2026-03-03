# 🔍 DIFERENCIAS: TAREA_LEVE vs TRABAJO_ADICIONAL

## ❌ PROBLEMA IDENTIFICADO

El frontend está mostrando los **presupuestos tipo TAREA_LEVE** como si fueran **trabajos adicionales**. Son entidades completamente diferentes que deben mostrarse por separado.

---

## 📊 COMPARACIÓN LADO A LADO

| Aspecto | PresupuestoNoCliente TAREA_LEVE | TrabajoAdicional |
|---------|--------------------------------|------------------|
| **Entidad** | `PresupuestoNoCliente` | `TrabajoAdicional` |
| **Tabla BD** | `presupuesto_no_cliente` | `trabajos_adicionales` |
| **Propósito** | Presupuesto/cotización para tareas menores | Trabajo adicional real (ejecución) |
| **Endpoint Listado** | `/api/presupuestos-no-cliente/por-obra/{obraId}` | `/api/trabajos-adicionales/obra/{obraId}` |
| **Campo Tipo** | `tipoPresupuesto: "TAREA_LEVE"` | No tiene campo tipo (siempre es trabajo adicional) |
| **Identificador JSON** | `id` + `tipoPresupuesto` | `id` |
| **Puede tener hijos** | ❌ No | ✅ Sí (anidación recursiva) |
| **Relación con Obra** | `obraId` (FK) | `obraId` (FK) |
| **Relación entre sí** | ⭐ **NUEVA**: puede tener `trabajoAdicionalId` (FK) | Independiente de presupuestos |
| **Tiene presupuestos** | Es EN SÍ MISMO un presupuesto | ⭐ **NUEVA**: puede tener múltiples TAREA_LEVE asociados |

---

## 🔑 CÓMO DISTINGUIRLOS EN EL FRONTEND

### Detección por Estructura de Datos

```typescript
// PresupuestoNoCliente tipo TAREA_LEVE
interface PresupuestoTareaLeve {
  id: number;
  tipoPresupuesto: "TAREA_LEVE";  // 🔑 Campo clave para identificar
  numeroPresupuesto: number;
  fechaEmision: string;
  estado: "BORRADOR" | "A_ENVIAR" | "ENVIADO" | "APROBADO" | "EN_EJECUCION" | ...;
  nombreObra: string;
  obraId: number | null;
  trabajoAdicionalId: number | null;  // ⭐ NUEVO - Puede estar vinculado a TrabajoAdicional
  totalPresupuesto: number;
  totalPresupuestoConHonorarios: number;
  profesionales: [...];
  materialesList: [...];
}

// TrabajoAdicional
interface TrabajoAdicional {
  id: number;
  // ❌ NO tiene campo tipoPresupuesto
  nombre: string;
  descripcion: string;
  estado: "BORRADOR" | "PENDIENTE" | "EN_PROGRESO" | "COMPLETADO" | "CANCELADO";
  obraId: number;
  empresaId: number;
  importe: number;
  importeJornales: number;
  importeMateriales: number;
  importeGastosGenerales: number;
  importeHonorarios: number;
  profesionales: [...];
  trabajosAdicionalesHijos: TrabajoAdicional[];  // 🔑 Puede tener hijos
  presupuestosTareasLeves: PresupuestoTareaLeve[];  // ⭐ NUEVO - Lista de presupuestos TAREA_LEVE
}
```

### Función Helper para Identificación

```typescript
/**
 * Determina si un objeto es un PresupuestoNoCliente de tipo TAREA_LEVE
 */
function esTareaLeve(obj: any): obj is PresupuestoTareaLeve {
  return obj && 
         obj.tipoPresupuesto === 'TAREA_LEVE' &&
         obj.hasOwnProperty('numeroPresupuesto') &&
         obj.hasOwnProperty('fechaEmision');
}

/**
 * Determina si un objeto es un TrabajoAdicional
 */
function esTrabajoAdicional(obj: any): obj is TrabajoAdicional {
  return obj && 
         !obj.hasOwnProperty('tipoPresupuesto') &&  // No tiene tipoPresupuesto
         obj.hasOwnProperty('importe') &&
         obj.hasOwnProperty('obraId');
}
```

---

## 🚨 ENDPOINTS CORRECTOS POR ENTIDAD

### Presupuestos TAREA_LEVE de una Obra

```typescript
// ✅ CORRECTO - Obtener presupuestos TAREA_LEVE de una obra
async function obtenerTareasLevesDeObra(obraId: number): Promise<PresupuestoTareaLeve[]> {
  const response = await fetch(`/api/presupuestos-no-cliente/por-obra/${obraId}`, {
    headers: { 'empresaId': '1' }
  });
  const todos = await response.json();
  
  // Filtrar solo los tipo TAREA_LEVE
  return todos.filter(p => p.tipoPresupuesto === 'TAREA_LEVE');
}
```

### Trabajos Adicionales de una Obra

```typescript
// ✅ CORRECTO - Obtener trabajos adicionales de una obra
async function obtenerTrabajosAdicionalesDeObra(obraId: number): Promise<TrabajoAdicional[]> {
  const response = await fetch(`/api/trabajos-adicionales/obra/${obraId}`);
  return await response.json();
}
```

### Presupuestos TAREA_LEVE de un TrabajoAdicional ⭐ NUEVO

```typescript
// ⭐ NUEVO - Obtener presupuestos TAREA_LEVE de un trabajo adicional
async function obtenerTareasLevesDeTrabajoAdicional(trabajoAdicionalId: number): Promise<PresupuestoTareaLeve[]> {
  const response = await fetch(`/api/trabajos-adicionales/${trabajoAdicionalId}`);
  const trabajo = await response.json();
  
  // La propiedad presupuestosTareasLeves contiene los TAREA_LEVE asociados
  return trabajo.presupuestosTareasLeves || [];
}
```

---

## 🎨 CÓMO RENDERIZAR EN LA UI

### Vista de Obra - Tabs Separados

```html
<div class="obra-detalle">
  <h2>{{ obra.nombre }}</h2>
  
  <!-- TABS CON SECCIONES SEPARADAS -->
  <mat-tab-group>
    
    <!-- TAB 1: Presupuestos (incluyendo TAREA_LEVE) -->
    <mat-tab label="Presupuestos">
      <div class="presupuestos-section">
        <h3>Presupuestos de la Obra</h3>
        
        <!-- Subsección: TAREA_LEVE -->
        <div class="tareas-leves-subsection">
          <h4>
            <mat-icon>task</mat-icon>
            Tareas Leves ({{ tareasLevesDirectas.length }})
          </h4>
          <mat-list>
            <mat-list-item *ngFor="let tarea of tareasLevesDirectas">
              <span class="badge badge-tarea-leve">TAREA LEVE</span>
              <span class="nombre">{{ tarea.nombreObra }}</span>
              <span class="total">{{ tarea.totalPresupuestoConHonorarios | currency }}</span>
              <span class="estado badge-estado-{{ tarea.estado }}">{{ tarea.estado }}</span>
              <button mat-button (click)="verDetallePresupuesto(tarea.id)">
                Ver Detalle
              </button>
            </mat-list-item>
          </mat-list>
        </div>
        
        <!-- Subsección: Otros Presupuestos -->
        <div class="otros-presupuestos-subsection">
          <h4>Otros Presupuestos ({{ otrosPresupuestos.length }})</h4>
          <!-- ... -->
        </div>
      </div>
    </mat-tab>
    
    <!-- TAB 2: Trabajos Adicionales -->
    <mat-tab label="Trabajos Adicionales">
      <div class="trabajos-adicionales-section">
        <h3>
          <mat-icon>engineering</mat-icon>
          Trabajos Adicionales ({{ trabajosAdicionales.length }})
        </h3>
        <mat-list>
          <mat-list-item *ngFor="let trabajo of trabajosAdicionales">
            <span class="badge badge-trabajo-adicional">TRABAJO ADICIONAL</span>
            <span class="nombre">{{ trabajo.nombre }}</span>
            <span class="total">{{ trabajo.importe | currency }}</span>
            <span class="estado badge-estado-{{ trabajo.estado }}">{{ trabajo.estado }}</span>
            
            <!-- ⭐ NUEVO: Mostrar si tiene tareas leves asociadas -->
            <span *ngIf="trabajo.presupuestosTareasLeves?.length > 0" 
                  class="badge badge-info">
              {{ trabajo.presupuestosTareasLeves.length }} tarea(s) leve(s)
            </span>
            
            <button mat-button (click)="verDetalleTrabajoAdicional(trabajo.id)">
              Ver Detalle
            </button>
          </mat-list-item>
        </mat-list>
      </div>
    </mat-tab>
    
  </mat-tab-group>
</div>
```

### Componente TypeScript

```typescript
@Component({
  selector: 'app-obra-detalle',
  templateUrl: './obra-detalle.component.html'
})
export class ObraDetalleComponent implements OnInit {
  obra: Obra;
  presupuestos: PresupuestoNoCliente[] = [];
  trabajosAdicionales: TrabajoAdicional[] = [];
  
  // Computed properties
  get tareasLevesDirectas(): PresupuestoTareaLeve[] {
    return this.presupuestos.filter(p => 
      p.tipoPresupuesto === 'TAREA_LEVE' && 
      p.obraId === this.obra.id &&  // Vinculado directamente a obra
      !p.trabajoAdicionalId  // NO vinculado a trabajo adicional
    );
  }
  
  get otrosPresupuestos(): PresupuestoNoCliente[] {
    return this.presupuestos.filter(p => p.tipoPresupuesto !== 'TAREA_LEVE');
  }
  
  async ngOnInit() {
    const obraId = this.route.snapshot.params['id'];
    
    // Cargar datos en paralelo
    await Promise.all([
      this.cargarObra(obraId),
      this.cargarPresupuestos(obraId),
      this.cargarTrabajosAdicionales(obraId)
    ]);
  }
  
  async cargarPresupuestos(obraId: number) {
    const response = await fetch(`/api/presupuestos-no-cliente/por-obra/${obraId}`, {
      headers: { 'empresaId': '1' }
    });
    this.presupuestos = await response.json();
  }
  
  async cargarTrabajosAdicionales(obraId: number) {
    const response = await fetch(`/api/trabajos-adicionales/obra/${obraId}`);
    this.trabajosAdicionales = await response.json();
  }
  
  verDetallePresupuesto(id: number) {
    this.router.navigate(['/presupuestos', id]);
  }
  
  verDetalleTrabajoAdicional(id: number) {
    this.router.navigate(['/trabajos-adicionales', id]);
  }
}
```

---

## 🎯 VISTA DETALLE DE TRABAJO ADICIONAL

Cuando el usuario abre un TrabajoAdicional, debe mostrar sus presupuestos TAREA_LEVE asociados:

```html
<div class="trabajo-adicional-detalle">
  <h2>{{ trabajo.nombre }}</h2>
  <p>{{ trabajo.descripcion }}</p>
  
  <!-- Información general -->
  <div class="info-general">
    <div class="campo">
      <label>Estado:</label>
      <span class="badge badge-estado-{{ trabajo.estado }}">{{ trabajo.estado }}</span>
    </div>
    <div class="campo">
      <label>Importe Total:</label>
      <span>{{ trabajo.importe | currency }}</span>
    </div>
    <div class="campo">
      <label>Obra Principal:</label>
      <a [routerLink]="['/obras', trabajo.obraId]">Ver Obra #{‌{ trabajo.obraId }}</a>
    </div>
  </div>
  
  <!-- ⭐ NUEVA SECCIÓN: Tareas Leves Asociadas -->
  <div class="tareas-leves-asociadas" *ngIf="trabajo.presupuestosTareasLeves?.length > 0">
    <h3>
      <mat-icon>task</mat-icon>
      Tareas Leves Asociadas a este Trabajo Adicional
      <span class="badge badge-count">{{ trabajo.presupuestosTareasLeves.length }}</span>
    </h3>
    
    <mat-card *ngFor="let tarea of trabajo.presupuestosTareasLeves" class="tarea-leve-card">
      <mat-card-header>
        <mat-card-title>
          <span class="badge badge-tarea-leve">TAREA LEVE</span>
          {{ tarea.nombreObra }}
        </mat-card-title>
        <mat-card-subtitle>
          Presupuesto #{{ tarea.numeroPresupuesto }} | 
          Emitido: {{ tarea.fechaEmision | date }}
        </mat-card-subtitle>
      </mat-card-header>
      
      <mat-card-content>
        <div class="tarea-info">
          <div class="campo">
            <label>Total:</label>
            <strong>{{ tarea.totalPresupuestoConHonorarios | currency }}</strong>
          </div>
          <div class="campo">
            <label>Estado:</label>
            <span class="badge badge-estado-{{ tarea.estado }}">{{ tarea.estado }}</span>
          </div>
          <div class="campo" *ngIf="tarea.descripcion">
            <label>Descripción:</label>
            <p>{{ tarea.descripcion }}</p>
          </div>
        </div>
        
        <!-- Datos heredados (solo informativo) -->
        <div class="datos-heredados">
          <mat-icon>info</mat-icon>
          <small>
            Datos heredados de Obra #{‌{ trabajo.obraId }}: 
            Empresa, Cliente, Dirección
          </small>
        </div>
      </mat-card-content>
      
      <mat-card-actions>
        <button mat-button color="primary" (click)="verDetallePresupuesto(tarea.id)">
          <mat-icon>visibility</mat-icon>
          Ver Detalle Completo
        </button>
        <button mat-button (click)="editarPresupuesto(tarea.id)">
          <mat-icon>edit</mat-icon>
          Editar
        </button>
      </mat-card-actions>
    </mat-card>
    
    <button mat-raised-button color="accent" (click)="crearNuevaTareaLeve()">
      <mat-icon>add</mat-icon>
      Nueva Tarea Leve en este Trabajo Adicional
    </button>
  </div>
  
  <!-- Mensaje si no tiene tareas leves -->
  <div class="sin-tareas-leves" *ngIf="!trabajo.presupuestosTareasLeves || trabajo.presupuestosTareasLeves.length === 0">
    <mat-icon>info_outline</mat-icon>
    <p>Este trabajo adicional no tiene tareas leves asociadas.</p>
    <button mat-raised-button color="primary" (click)="crearNuevaTareaLeve()">
      <mat-icon>add</mat-icon>
      Crear Primera Tarea Leve
    </button>
  </div>
  
  <!-- Sección de profesionales, materiales, etc. -->
  <!-- ... -->
</div>
```

---

## 🔄 FLUJO CORRECTO DE DATOS

### Caso 1: Usuario en Vista de Obra

```
Usuario ve Obra #55
    ↓
Frontend hace 2 llamadas:
    ├─> GET /api/presupuestos-no-cliente/por-obra/55
    │   Retorna: [
    │     { id: 101, tipoPresupuesto: "TAREA_LEVE", obraId: 55, trabajoAdicionalId: null },
    │     { id: 102, tipoPresupuesto: "TRADICIONAL", obraId: 55 }
    │   ]
    │
    └─> GET /api/trabajos-adicionales/obra/55
        Retorna: [
          { id: 100, nombre: "Instalación eléctrica", obraId: 55, presupuestosTareasLeves: [...] }
        ]
    ↓
Frontend renderiza:
    - TAB "Presupuestos" → Muestra presupuesto 101 (TAREA_LEVE) y 102 (TRADICIONAL)
    - TAB "Trabajos Adicionales" → Muestra trabajo 100
```

### Caso 2: Usuario en Vista de TrabajoAdicional

```
Usuario ve TrabajoAdicional #100
    ↓
Frontend hace 1 llamada:
    └─> GET /api/trabajos-adicionales/100
        Retorna: {
          id: 100,
          nombre: "Instalación eléctrica",
          obraId: 55,
          importe: 500000,
          presupuestosTareasLeves: [
            { id: 103, tipoPresupuesto: "TAREA_LEVE", trabajoAdicionalId: 100 }
          ]
        }
    ↓
Frontend renderiza:
    - Datos del TrabajoAdicional (nombre, importe, estado)
    - Sección "Tareas Leves Asociadas" → Muestra presupuesto 103
```

---

## 🚫 ERRORES COMUNES A EVITAR

### ❌ Error 1: Mezclar ambas entidades en la misma lista

```typescript
// ❌ MAL - Mezclando TrabajoAdicional con Presupuestos TAREA_LEVE
const items = [
  ...trabajosAdicionales,  // Array de TrabajoAdicional
  ...tareasLeves           // Array de PresupuestoNoCliente
];

// ❌ Problema: No puede distinguir tipos, ambos tienen "id"
```

### ❌ Error 2: Usar el mismo componente para renderizar

```html
<!-- ❌ MAL - Usando mismo componente para ambos -->
<app-item-card *ngFor="let item of items" [item]="item"></app-item-card>
```

### ✅ Solución: Componentes separados

```html
<!-- ✅ BIEN - Componentes específicos por tipo -->
<app-trabajo-adicional-card 
  *ngFor="let trabajo of trabajosAdicionales" 
  [trabajo]="trabajo">
</app-trabajo-adicional-card>

<app-tarea-leve-card 
  *ngFor="let tarea of tareasLeves" 
  [tarea]="tarea">
</app-tarea-leve-card>
```

---

## 📋 CSS SUGERIDO PARA DISTINGUIR VISUALMENTE

```css
/* Badge para Tarea Leve */
.badge-tarea-leve {
  background-color: #e3f2fd;
  color: #1976d2;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
}

.badge-tarea-leve::before {
  content: "📋";
  margin-right: 4px;
}

/* Badge para Trabajo Adicional */
.badge-trabajo-adicional {
  background-color: #f3e5f5;
  color: #7b1fa2;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;  
}

.badge-trabajo-adicional::before {
  content: "🔧";
  margin-right: 4px;
}

/* Cards diferentes */
.tarea-leve-card {
  border-left: 4px solid #1976d2;
}

.trabajo-adicional-card {
  border-left: 4px solid #7b1fa2;
}
```

---

## ✅ CHECKLIST DE CORRECCIÓN FRONTEND

### Paso 1: Verificar Endpoints
- [ ] Endpoint para Tareas Leves: `/api/presupuestos-no-cliente/por-obra/{id}`
- [ ] Endpoint para Trabajos Adicionales: `/api/trabajos-adicionales/obra/{id}`
- [ ] **NO mezclar** ambos endpoints en una sola respuesta

### Paso 2: Crear Type Guards
- [ ] Función `esTareaLeve(obj): boolean`
- [ ] Función `esTrabajoAdicional(obj): boolean`
- [ ] Usar TypeScript para tipos estrictos

### Paso 3: Separar UI
- [ ] Tab separado "Presupuestos" y "Trabajos Adicionales"
- [ ] Componente específico para Tarea Leve
- [ ] Componente específico para Trabajo Adicional
- [ ] CSS diferenciado (colores, iconos, badges)

### Paso 4: Vista Detalle TrabajoAdicional
- [ ] Sección para mostrar `presupuestosTareasLeves` del trabajo adicional
- [ ] Botón "Nueva Tarea Leve" en vista de Trabajo Adicional
- [ ] Link para ver detalle completo del presupuesto TAREA_LEVE

### Paso 5: Vista Detalle Obra
- [ ] Filtrar presupuestos TAREA_LEVE vinculados directamente a obra (`obraId && !trabajoAdicionalId`)
- [ ] No mostrar en "Trabajos Adicionales" los presupuestos TAREA_LEVE

### Paso 6: Testing
- [ ] Crear TAREA_LEVE desde Obra → debe aparecer en tab "Presupuestos"
- [ ] Crear TrabajoAdicional → debe aparecer en tab "Trabajos Adicionales"
- [ ] Crear TAREA_LEVE desde TrabajoAdicional → debe aparecer dentro del detalle del Trabajo Adicional
- [ ] Verificar que badges muestran correctamente el tipo de entidad

---

## 🎓 RESUMEN CONCEPTUAL

### PresupuestoNoCliente tipo TAREA_LEVE
- **Qué es**: Un presupuesto/cotización para una tarea menor
- **Cuándo se usa**: Antes de ejecutar trabajos pequeños que requieren aprobación
- **Dónde se muestra**: En la sección "Presupuestos" de la Obra o dentro de un TrabajoAdicional
- **Campo distintivo**: `tipoPresupuesto: "TAREA_LEVE"`
- **Ejemplo**: "Reparación urgente de pared - $150.000"

### TrabajoAdicional
- **Qué es**: Un trabajo adicional real que se ejecuta en la obra
- **Cuándo se usa**: Para gestionar trabajos adicionales complejos que se ejecutan
- **Dónde se muestra**: En la sección "Trabajos Adicionales" de la Obra
- **Campo distintivo**: No tiene `tipoPresupuesto`
- **Ejemplo**: "Instalación eléctrica completa - $500.000"

### Relación ⭐ NUEVA
- Un **TrabajoAdicional** puede tener múltiples **presupuestos TAREA_LEVE** asociados
- Esto permite sub-presupuestos dentro de un trabajo adicional
- El frontend debe mostrar esta jerarquía claramente

---

**Fecha**: 3 de marzo de 2026  
**Versión**: 1.0  
**Para**: Equipo Frontend
