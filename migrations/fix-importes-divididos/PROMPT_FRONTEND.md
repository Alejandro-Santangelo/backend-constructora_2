# PROMPT PARA FRONTEND - DIVISIÓN INCORRECTA DE IMPORTES CONFIRMADA 🚨

## ✅ CONFIRMACIÓN DEL BACKEND

**ANÁLISIS EXHAUSTIVO DEL CÓDIGO BACKEND COMPLETADO:**

1. ✅ El código Java/Spring Boot NO divide importes por semanas
2. ✅ Revisados servicios, helpers, mappers, entidades - NO hay lógica de división
3. ✅ Los DTOs mapean directamente los valores sin modificación
4. ✅ @PrePersist/@PreUpdate no realizan cálculos de división
5. ✅ Los datos se guardan EXACTAMENTE como llegan del frontend

**CONCLUSIÓN:** El frontend está enviando los datos ya divididos al backend.

## PROBLEMA CRÍTICO DETECTADO 🚨

El frontend está **dividiendo los importes/precios por la cantidad de semanas ANTES de enviarlos** al backend. El backend guarda exactamente lo que recibe, sin modificarlo.

## EJEMPLO DEL PROBLEMA (CASO REAL)

**Trabajo Extra ID 2 - "Casa de Cacho Galpon":**
- **Duración:** 20 semanas
- **Usuario ingresa:** "Escalera" = $1.000.000 (importe total del trabajo)

**❌ COMPORTAMIENTO ACTUAL (INCORRECTO):**
```typescript
// Frontend calcula:
const importePorSemana = 1000000 / 20; // = 50000

// Frontend envía al backend:
{
  "descripcion": "Escalera",
  "cantidad": 1,
  "precioUnitario": 50000,    // ❌ DIVIDIDO
  "subtotal": 50000           // ❌ DIVIDIDO
}

// Backend guarda: $50.000 (INCORRECTO)
// Base de datos contiene: $50.000 
// GET devuelve: $50.000
```

**✅ COMPORTAMIENTO CORRECTO ESPERADO:**
```typescript
// Usuario ingresa: $1.000.000

// Frontend debe enviar SIN modificar:
{
  "descripcion": "Escalera",
  "cantidad": 1,
  "precioUnitario": 1000000,   // ✅ VALOR TOTAL
  "subtotal": 1000000          // ✅ VALOR TOTAL
}

// Backend guardará: $1.000.000 (CORRECTO)
// Base de datos contendrá: $1.000.000
// GET devolverá: $1.000.000
```

## CONTEXTO

El sistema maneja dos tipos de presupuestos:
1. **Presupuestos No Cliente** (`presupuesto_no_cliente`)
2. **Trabajos Extra** (`trabajos_extra`)

Ambos tienen:
- Campo `tiempo_estimado_terminacion` (cantidad de semanas)
- Múltiples tablas relacionadas con importes:
  - Materiales
  - Gastos Generales (Otros Costos)
  - Profesionales
  - Jornales

## ⚠️ URGENTE: DÓNDE BUSCAR EL BUG

**El frontend está dividiendo en uno de estos lugares:**

### 1. Al preparar el payload ANTES de enviar al backend
```typescript
// ❌ Buscar código como este:
const payload = {
  gastosGenerales: items.map(item => ({
    precioUnitario: item.precio / this.semanas,  // ❌ ELIMINAR DIVISIÓN
    subtotal: item.total / this.tiempoEstimado   // ❌ ELIMINAR DIVISIÓN
  }))
};
```

### 2. En servicios o helpers de cálculo
```typescript
// ❌ Buscar funciones como:
calcularPrecioSemanal(precio: number, semanas: number) {
  return precio / semanas;  // ❌ Este valor NO debe enviarse al backend
}
```

### 3. En formularios al capturar el valor
```typescript
// ❌ Buscar:
onPrecioChange(valor: number) {
  this.item.precioUnitario = valor / this.form.get('semanas').value;  // ❌ ELIMINAR
}
```

### 4. En interceptors HTTP o transformadores
```typescript
// ❌ Buscar interceptors que modifiquen datos:
intercept(req: HttpRequest<any>, next: HttpHandler) {
  if (req.body.tiempoEstimadoTerminacion) {
    // ❌ NO dividir valores aquí
  }
}
```

## COMPORTAMIENTO CORRECTO ESPERADO

Los importes deben enviarse **SIN DIVIDIR** al backend:

```typescript
// ✅ CORRECTO - Enviar el importe TOTAL
const precioUnitario = precioIngresadoPorUsuario; // Sin modificar
const subtotal = cantidad * precioUnitario; // Sin dividir por semanas

// ✅ CORRECTO - Payload sin división
payload.precioUnitario = item.precio; // Valor real completo
payload.subtotal = item.subtotal; // Valor real completo
```

**REGLA CLARA:** El backend se encarga de todos los cálculos relacionados con semanas si es necesario. El frontend SOLO debe enviar los valores exactos que ingresa el usuario.

## ENDPOINTS AFECTADOS

Revisar TODOS los endpoints que crean/actualizan estos recursos:

### Trabajos Extra:
- `POST /api/v1/trabajos-extra`
- `PUT /api/v1/trabajos-extra/{id}`
- Cualquier endpoint que maneje `itemsCalculadora`

### Presupuestos No Cliente:
- `POST /api/v1/presupuestos-no-cliente`
- `PUT /api/v1/presupuestos-no-cliente/{id}`
- Endpoints de items calculadora

## TABLAS/ENTIDADES AFECTADAS

Verificar que NO se dividan importes en:

1. **Trabajos Extra:**
   - `trabajos_extra_gasto_general` → campos: `precio_unitario`, `subtotal`
   - `trabajos_extra_material_calculadora` → campos: `precio`, `subtotal`
   - `trabajos_extra_profesional_calculadora` → campos: `valor_jornal`, `subtotal`
   - `trabajos_extra_jornal_calculadora` → campos: `valor_unitario`, `subtotal`
   - `trabajos_extra_items_calculadora` → campos: `total`, `subtotal_materiales`, `subtotal_mano_obra`, `subtotal_gastos_generales`

2. **Presupuestos No Cliente:**
   - `presupuesto_gasto_general` → campos: `precio_unitario`, `subtotal`
   - `presupuesto_material` → campos: `precio_unitario`, `subtotal`
   - Cualquier otra tabla relacionada con items

## 🤖 INSTRUCCIONES PARA IA DEL FRONTEND

### TAREA PRINCIPAL
Encontrar y eliminar TODA la lógica que divide importes/precios por la cantidad de semanas antes de enviarlos al backend.

### PASO 1: BÚSQUEDA EXHAUSTIVA

Busca en TODO el código frontend estos patrones:

#### A) Operaciones de división sospechosas
```typescript
// ❌ Buscar y ELIMINAR patrones como:
valor / semanas
precio / tiempoEstimadoTerminacion
subtotal / cantidadSemanas
importe / this.semanas
total / duracion
.divide(semanas)
.dividedBy(tiempoEstimado)
```

#### B) Funciones de cálculo de "precio semanal"
```typescript
// ❌ Buscar funciones como:
calcularPrecioSemanal()
getPrecioSemanal()
calcularImporteSemanal()
dividirPorSemanas()
ajustarPorDuracion()
normalizarPorTiempo()
```

#### C) Transformaciones en servicios HTTP
```typescript
// ❌ En archivos *.service.ts, buscar:
crearTrabajoExtra(data) {
  const payload = {
    ...data,
    precioUnitario: data.precio / data.semanas  // ❌ ELIMINAR
  };
  return this.http.post(...);
}

actualizarPresupuesto(data) {
  return this.http.put(url, this.transformarDatos(data));  // ❌ Revisar transformarDatos
}
```

#### D) Modificaciones en formularios
```typescript
// ❌ En componentes de formularios:
onSubmit() {
  const formValue = this.form.value;
  const semanas = formValue.tiempoEstimado;
  
  // ❌ Buscar divisiones aquí:
  formValue.items.forEach(item => {
    item.precio = item.precioTotal / semanas;  // ❌ ELIMINAR
  });
  
  this.service.guardar(formValue);
}
```

#### E) Mappers y transformadores
```typescript
// ❌ Buscar archivos: *mapper.ts, *transformer.ts, *adapter.ts
class TrabajoExtraMapper {
  toDTO(model: TrabajoExtra): TrabajoExtraDTO {
    return {
      ...model,
      gastosGenerales: model.gastos.map(g => ({
        precioUnitario: g.precio / model.semanas  // ❌ ELIMINAR
      }))
    };
  }
}
```

### PASO 2: REVISIÓN DE ARCHIVOS ESPECÍFICOS

Revisa estos archivos en orden de prioridad:

#### 1. Servicios HTTP (ALTA PRIORIDAD)
```
src/app/services/trabajo-extra.service.ts
src/app/services/presupuesto-no-cliente.service.ts  
src/app/services/obra.service.ts
src/app/core/services/api/*.service.ts
```

**Qué revisar:**
- Métodos `create()`, `update()`, `save()`
- Transformaciones de datos antes de `http.post()` o `http.put()`
- Cualquier función que prepare payloads

**Qué cambiar:**
```typescript
// ❌ ANTES (INCORRECTO):
crearTrabajoExtra(data: TrabajoExtraForm): Observable<TrabajoExtra> {
  const semanas = data.tiempoEstimadoTerminacion;
  const payload = {
    ...data,
    gastosGenerales: data.gastos.map(g => ({
      descripcion: g.descripcion,
      cantidad: g.cantidad,
      precioUnitario: g.precio / semanas,  // ❌ DIVISIÓN
      subtotal: (g.precio * g.cantidad) / semanas  // ❌ DIVISIÓN
    }))
  };
  return this.http.post<TrabajoExtra>(this.apiUrl, payload);
}

// ✅ DESPUÉS (CORRECTO):
crearTrabajoExtra(data: TrabajoExtraForm): Observable<TrabajoExtra> {
  const payload = {
    ...data,
    gastosGenerales: data.gastos.map(g => ({
      descripcion: g.descripcion,
      cantidad: g.cantidad,
      precioUnitario: g.precio,  // ✅ SIN DIVISIÓN
      subtotal: g.precio * g.cantidad  // ✅ SIN DIVISIÓN
    }))
  };
  return this.http.post<TrabajoExtra>(this.apiUrl, payload);
}
```

#### 2. Componentes de Formularios (ALTA PRIORIDAD)
```
src/app/components/trabajos-extra/trabajo-extra-form.component.ts
src/app/components/presupuestos/presupuesto-form.component.ts
src/app/components/shared/calculadora-items/*.component.ts
src/app/modules/obras/components/*.component.ts
```

**Qué revisar:**
- Métodos `onSubmit()`, `guardar()`, `actualizar()`
- Event handlers de inputs: `onPrecioChange()`, `onCantidadChange()`
- Cálculos de totales y subtotales

**Qué cambiar:**
```typescript
// ❌ ANTES (INCORRECTO):
onPrecioChange(precio: number) {
  const semanas = this.form.get('tiempoEstimadoTerminacion').value;
  this.form.patchValue({
    precioUnitario: precio / semanas  // ❌ DIVISIÓN
  });
}

// ✅ DESPUÉS (CORRECTO):
onPrecioChange(precio: number) {
  this.form.patchValue({
    precioUnitario: precio  // ✅ SIN DIVISIÓN
  });
  
  // Si necesitas mostrar el precio semanal (solo visual):
  this.precioSemanalDisplay = this.calcularPrecioSemanal(precio);
}

// Función auxiliar SOLO para display (no enviar al backend):
private calcularPrecioSemanal(precioTotal: number): number {
  const semanas = this.form.get('tiempoEstimadoTerminacion').value || 1;
  return precioTotal / semanas;
}
```

#### 3. Helpers y Utilidades (MEDIA PRIORIDAD)
```
src/app/shared/helpers/calculadora.helper.ts
src/app/shared/helpers/presupuesto.helper.ts
src/app/shared/utils/math.utils.ts
src/app/core/helpers/*.ts
```

**Qué revisar:**
- Funciones de cálculo de totales
- Funciones de transformación de datos
- Utilidades matemáticas

**Qué cambiar:**
```typescript
// ❌ ANTES (INCORRECTO):
export class CalculadoraHelper {
  static calcularSubtotal(item: Item, semanas: number): number {
    return (item.cantidad * item.precio) / semanas;  // ❌ DIVISIÓN
  }
  
  static prepararParaGuardar(items: Item[], semanas: number): ItemDTO[] {
    return items.map(item => ({
      ...item,
      subtotal: this.calcularSubtotal(item, semanas)  // ❌ DIVISIÓN
    }));
  }
}

// ✅ DESPUÉS (CORRECTO):
export class CalculadoraHelper {
  static calcularSubtotal(item: Item): number {
    return item.cantidad * item.precio;  // ✅ SIN DIVISIÓN
  }
  
  static prepararParaGuardar(items: Item[]): ItemDTO[] {
    return items.map(item => ({
      ...item,
      subtotal: this.calcularSubtotal(item)  // ✅ SIN DIVISIÓN
    }));
  }
  
  // Función separada SOLO para mostrar (no enviar):
  static calcularSubtotalSemanal(item: Item, semanas: number): number {
    return this.calcularSubtotal(item) / (semanas || 1);
  }
}
```

#### 4. Modelos y DTOs (BAJA PRIORIDAD)
```
src/app/models/trabajo-extra.model.ts
src/app/models/presupuesto.model.ts
src/app/core/models/*.ts
```

**Qué revisar:**
- Getters calculados
- Métodos `toDTO()`, `fromDTO()`, `toJSON()`

**Qué cambiar:**
```typescript
// ❌ ANTES (INCORRECTO):
export class TrabajoExtra {
  tiempoEstimadoTerminacion: number;
  gastosGenerales: Gasto[];
  
  toDTO(): TrabajoExtraDTO {
    return {
      gastosGenerales: this.gastosGenerales.map(g => ({
        precioUnitario: g.precio / this.tiempoEstimadoTerminacion  // ❌ DIVISIÓN
      }))
    };
  }
}

// ✅ DESPUÉS (CORRECTO):
export class TrabajoExtra {
  tiempoEstimadoTerminacion: number;
  gastosGenerales: Gasto[];
  
  toDTO(): TrabajoExtraDTO {
    return {
      gastosGenerales: this.gastosGenerales.map(g => ({
        precioUnitario: g.precio  // ✅ SIN DIVISIÓN
      }))
    };
  }
  
  // Método separado para cálculos de display:
  getPrecioSemanalGasto(gasto: Gasto): number {
    return gasto.precio / (this.tiempoEstimadoTerminacion || 1);
  }
}
```

#### 5. Interceptors HTTP (MEDIA PRIORIDAD)
```
src/app/core/interceptors/*.interceptor.ts
src/app/interceptors/*.ts
```

**Qué revisar:**
- Transformaciones de request body
- Modificaciones de payloads

**Qué cambiar:**
```typescript
// ❌ ANTES (INCORRECTO):
export class DataTransformInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    if (req.url.includes('trabajos-extra') && req.body?.tiempoEstimadoTerminacion) {
      const modifiedBody = {
        ...req.body,
        items: req.body.items.map(item => ({
          ...item,
          precio: item.precio / req.body.tiempoEstimadoTerminacion  // ❌ DIVISIÓN
        }))
      };
      req = req.clone({ body: modifiedBody });
    }
    return next.handle(req);
  }
}

// ✅ DESPUÉS (CORRECTO):
export class DataTransformInterceptor implements HttpInterceptor {
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    // ✅ NO modificar precios/importes
    return next.handle(req);
  }
}
```

### PASO 3: SEPARAR LÓGICA DE DISPLAY vs LÓGICA DE GUARDADO

**REGLA DE ORO:** 
- **Valores para GUARDAR:** Siempre valores totales (sin dividir)
- **Valores para MOSTRAR:** Pueden calcularse divididos, pero NUNCA enviarlos al backend

```typescript
export class TrabajoExtraFormComponent {
  form: FormGroup;
  
  // Valores que se GUARDAN (sin dividir)
  get valoresToGuardar(): TrabajoExtraDTO {
    return {
      tiempoEstimadoTerminacion: this.form.value.semanas,
      gastosGenerales: this.form.value.gastos.map(g => ({
        precioUnitario: g.precio,  // ✅ Valor TOTAL
        subtotal: g.cantidad * g.precio  // ✅ Valor TOTAL
      }))
    };
  }
  
  // Valores para DISPLAY (pueden estar divididos)
  get valoresParaMostrar(): DisplayData {
    const semanas = this.form.value.semanas || 1;
    return {
      gastosSemanales: this.form.value.gastos.map(g => ({
        precioSemanal: g.precio / semanas,  // ✅ Solo display
        subtotalSemanal: (g.cantidad * g.precio) / semanas  // ✅ Solo display
      }))
    };
  }
  
  guardar() {
    // ✅ Usar valoresToGuardar, NO valoresParaMostrar
    this.service.crear(this.valoresToGuardar).subscribe(...);
  }
}
```

### PASO 4: VALIDACIÓN POST-FIX

Después de hacer los cambios, ejecuta estas validaciones:

#### A) Prueba de creación
```typescript
// Test: Crear trabajo extra con duración 10 semanas
const test = {
  nombre: "Test División",
  tiempoEstimadoTerminacion: 10,
  gastosGenerales: [{
    descripcion: "Gasto Test",
    cantidad: 1,
    precio: 100000  // Usuario ingresa $100.000
  }]
};

// Verificar payload enviado al backend:
// ✅ DEBE enviar: precioUnitario: 100000, subtotal: 100000
// ❌ NO debe enviar: precioUnitario: 10000, subtotal: 10000
```

#### B) Verificación en Network Tab
1. Abrir DevTools → Network
2. Crear/editar un trabajo extra
3. Buscar request POST/PUT a `/api/v1/trabajos-extra`
4. Revisar Request Payload
5. **Verificar:** `precioUnitario` y `subtotal` deben ser valores TOTALES (sin dividir)

#### C) Verificación en Base de Datos
```sql
-- Ejecutar en PostgreSQL después de crear/editar:
SELECT descripcion, precio_unitario, subtotal 
FROM trabajos_extra_gasto_general 
WHERE descripcion LIKE '%Test%'
ORDER BY id DESC LIMIT 1;

-- ✅ DEBE mostrar: precio_unitario = 100000, subtotal = 100000
-- ❌ NO debe mostrar: precio_unitario = 10000, subtotal = 10000
```

### PASO 5: CHECKLIST FINAL

Antes de dar por terminado, verificar:

- [ ] ✅ Eliminadas TODAS las divisiones por semanas en servicios
- [ ] ✅ Eliminadas TODAS las divisiones en componentes de formularios
- [ ] ✅ Eliminadas TODAS las divisiones en helpers/utils
- [ ] ✅ Eliminadas TODAS las divisiones en interceptors
- [ ] ✅ Separada lógica de display (puede dividir) vs guardado (nunca dividir)
- [ ] ✅ Prueba manual: Crear trabajo extra → Verificar en BD → Valores correctos
- [ ] ✅ Prueba manual: Editar trabajo extra → Verificar en BD → Valores se mantienen
- [ ] ✅ Prueba manual: GET trabajo extra → Frontend muestra valores correctos
- [ ] ✅ Revisión de Network Tab → Payloads correctos
- [ ] ✅ No quedan referencias a funciones como `calcularPrecioSemanal()` usadas para guardado

---

## 🔍 BÚSQUEDA SISTEMÁTICA

**Ejecutá estos comandos en la consola del proyecto frontend:**

### 1. Buscar divisiones por semanas/tiempo
```bash
grep -r "/ semanas" src/
grep -r "/ tiempoEstimado" src/
grep -r "/ cantidadSemanas" src/
grep -r ".divide(" src/
grep -r "dividedBy" src/
```

### 2. Buscar en archivos TypeScript/JavaScript
```bash
find src/ -name "*.ts" -o -name "*.js" | xargs grep -l "semanas\|tiempoEstimado"
```

### 3. Buscar payload de trabajos extra
```bash
grep -r "POST.*trabajos-extra" src/
grep -r "PUT.*trabajos-extra" src/
grep -r "precioUnitario.*:" src/
```

### 4. Revisar estos archivos específicamente:
```
src/app/services/trabajo-extra.service.ts
src/app/services/presupuesto-no-cliente.service.ts
src/app/components/trabajo-extra/trabajo-extra-form.component.ts
src/app/components/presupuesto/presupuesto-form.component.ts
src/app/shared/helpers/calculadora.helper.ts
src/app/shared/helpers/presupuesto.helper.ts
```

## EJEMPLO DE CÓDIGO CORRECTO

```typescript
// Crear/Actualizar Trabajo Extra - CORRECTO ✅
crearTrabajoExtra(data: TrabajoExtraDTO) {
  const payload = {
    nombre: data.nombre,
    tiempoEstimadoTerminacion: data.semanas, // OK: enviar semanas
    itemsCalculadora: data.items.map(item => ({
      tipoProfesional: item.tipo,
      total: item.total, // ✅ SIN dividir por semanas
      gastosGenerales: item.gastos.map(gasto => ({
        descripcion: gasto.descripcion,
        cantidad: gasto.cantidad,
        precioUnitario: gasto.precio, // ✅ Precio TOTAL ingresado
        subtotal: gasto.cantidad * gasto.precio // ✅ SIN dividir
      })),
      materialesLista: item.materiales.map(material => ({
        nombre: material.nombre,
        cantidad: material.cantidad,
        precio: material.precio, // ✅ Precio TOTAL
        subtotal: material.cantidad * material.precio // ✅ SIN dividir
      }))
    }))
  };
  
  return this.http.post('/api/v1/trabajos-extra', payload);
}

// Si necesitas mostrar el "importe semanal" en UI (solo visualización)
calcularImporteSemanal(importeTotal: number, semanas: number): number {
  return semanas > 0 ? importeTotal / semanas : importeTotal;
  // Pero NUNCA envíes este valor al backend
}
```

## VERIFICACIÓN POST-FIX

Después de corregir el código:

1. Crear un nuevo Trabajo Extra de prueba:
   - Duración: 10 semanas
   - Agregar un gasto "Test" con importe $100.000
   - **Verificar en BD:** `subtotal` debe ser $100.000 (NO $10.000)

2. Editar un Trabajo Extra existente:
   - Cambiar un importe de $50.000 a $75.000
   - **Verificar en BD:** debe guardarse $75.000 (NO dividido)

3. Llamar al endpoint GET y verificar que devuelva los valores correctos

## 📝 EVIDENCIA DEL BACKEND (PARA EL DESARROLLADOR FRONTEND)

**Archivos Backend Revisados (NO modifican los datos):**

1. `TrabajoExtraItemCalculadoraHelper.java` (línea 287-313):
```java
private void guardarGastosGenerales(TrabajoExtraItemCalculadora item, Long empresaId,
                                   List<TrabajoExtraGastoGeneralDTO> gastosDTO) {
    for (TrabajoExtraGastoGeneralDTO gastoDTO : gastosDTO) {
        TrabajoExtraGastoGeneral gasto = new TrabajoExtraGastoGeneral();
        // ... otros campos ...
        gasto.setPrecioUnitario(gastoDTO.getPrecioUnitario());  // ✅ Guarda TAL CUAL llega
        gasto.setSubtotal(gastoDTO.getSubtotal());              // ✅ Guarda TAL CUAL llega
        gastoRepository.save(gasto);
    }
}
```

2. `TrabajoExtraItemCalculadoraHelper.java` (línea 427-441) - Mapeo para GET:
```java
private List<TrabajoExtraGastoGeneralResponseDTO> mapearGastosAResponse(
        List<TrabajoExtraGastoGeneral> gastos) {
    return gastos.stream()
            .map(g -> TrabajoExtraGastoGeneralResponseDTO.builder()
                    .precioUnitario(g.getPrecioUnitario())  // ✅ Devuelve TAL CUAL está en BD
                    .subtotal(g.getSubtotal())              // ✅ Devuelve TAL CUAL está en BD
                    .build())
            .collect(Collectors.toList());
}
```

3. `TrabajoExtraGastoGeneral.java` (línea 73-80) - @PrePersist NO divide:
```java
@PrePersist
protected void onCreate() {
    createdAt = LocalDateTime.now();
    updatedAt = LocalDateTime.now();
    // ✅ NO hay lógica de división aquí
}
```

**CONCLUSIÓN:** El backend es transparente - lo que entra es lo que se guarda y devuelve.

---

**NO DIVIDIR** importes por semanas en ningún lugar del código frontend. Los importes deben enviarse al backend **tal como el usuario los ingresa** (valores totales del presupuesto).

La división por semanas (si es necesaria) debe hacerse:
- ✅ En el backend para cálculos internos
- ✅ En el frontend SOLO para mostrar información, pero sin enviar esos valores divididos

---

**IMPORTANTE:** Este es un bug crítico que afecta la integridad de los datos financieros. Debe corregirse cuanto antes y validarse exhaustivamente.
