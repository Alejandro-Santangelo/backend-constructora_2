# EJEMPLOS DE CÓDIGO - ANTES Y DESPUÉS

## 📋 GUÍA RÁPIDA PARA IA DEL FRONTEND

Esta guía muestra ejemplos concretos de código INCORRECTO y cómo debe quedar CORRECTO.

---

## 1️⃣ SERVICIOS HTTP

### ❌ INCORRECTO - trabajo-extra.service.ts

```typescript
@Injectable()
export class TrabajoExtraService {
  
  crear(data: TrabajoExtraForm): Observable<TrabajoExtra> {
    const semanas = data.tiempoEstimadoTerminacion;
    
    // ❌ PROBLEMA: Divide importes antes de enviar
    const payload = {
      nombre: data.nombre,
      obraId: data.obraId,
      tiempoEstimadoTerminacion: semanas,
      itemsCalculadora: data.items.map(item => ({
        tipoProfesional: item.tipo,
        gastosGenerales: item.gastos.map(gasto => ({
          descripcion: gasto.descripcion,
          cantidad: gasto.cantidad,
          precioUnitario: gasto.precio / semanas,  // ❌ DIVISIÓN INCORRECTA
          subtotal: (gasto.cantidad * gasto.precio) / semanas  // ❌ DIVISIÓN INCORRECTA
        })),
        materialesLista: item.materiales.map(material => ({
          nombre: material.nombre,
          cantidad: material.cantidad,
          precio: material.precio / semanas,  // ❌ DIVISIÓN INCORRECTA
          subtotal: (material.cantidad * material.precio) / semanas  // ❌ DIVISIÓN INCORRECTA
        }))
      }))
    };
    
    return this.http.post<TrabajoExtra>(`${this.apiUrl}/trabajos-extra`, payload);
  }
}
```

### ✅ CORRECTO - trabajo-extra.service.ts

```typescript
@Injectable()
export class TrabajoExtraService {
  
  crear(data: TrabajoExtraForm): Observable<TrabajoExtra> {
    // ✅ SOLUCIÓN: NO dividir, enviar valores tal cual
    const payload = {
      nombre: data.nombre,
      obraId: data.obraId,
      tiempoEstimadoTerminacion: data.tiempoEstimadoTerminacion,
      itemsCalculadora: data.items.map(item => ({
        tipoProfesional: item.tipo,
        gastosGenerales: item.gastos.map(gasto => ({
          descripcion: gasto.descripcion,
          cantidad: gasto.cantidad,
          precioUnitario: gasto.precio,  // ✅ VALOR TOTAL
          subtotal: gasto.cantidad * gasto.precio  // ✅ VALOR TOTAL
        })),
        materialesLista: item.materiales.map(material => ({
          nombre: material.nombre,
          cantidad: material.cantidad,
          precio: material.precio,  // ✅ VALOR TOTAL
          subtotal: material.cantidad * material.precio  // ✅ VALOR TOTAL
        }))
      }))
    };
    
    return this.http.post<TrabajoExtra>(`${this.apiUrl}/trabajos-extra`, payload);
  }
}
```

---

## 2️⃣ COMPONENTES DE FORMULARIO

### ❌ INCORRECTO - trabajo-extra-form.component.ts

```typescript
@Component({
  selector: 'app-trabajo-extra-form',
  templateUrl: './trabajo-extra-form.component.html'
})
export class TrabajoExtraFormComponent {
  form: FormGroup;
  
  onSubmit() {
    const formValue = this.form.value;
    const semanas = formValue.tiempoEstimadoTerminacion;
    
    // ❌ PROBLEMA: Modifica los valores antes de enviar
    const payload = {
      ...formValue,
      itemsCalculadora: formValue.itemsCalculadora.map(item => {
        return {
          ...item,
          gastosGenerales: item.gastosGenerales.map(gasto => ({
            ...gasto,
            precioUnitario: gasto.precioUnitario / semanas,  // ❌ DIVISIÓN
            subtotal: gasto.subtotal / semanas  // ❌ DIVISIÓN
          }))
        };
      })
    };
    
    this.trabajoExtraService.crear(payload).subscribe(...);
  }
  
  onPrecioChange(gasto: FormGroup, nuevoPrecio: number) {
    const semanas = this.form.get('tiempoEstimadoTerminacion').value;
    // ❌ PROBLEMA: Divide el precio al actualizar el formulario
    gasto.patchValue({
      precioUnitario: nuevoPrecio / semanas  // ❌ DIVISIÓN
    });
  }
}
```

### ✅ CORRECTO - trabajo-extra-form.component.ts

```typescript
@Component({
  selector: 'app-trabajo-extra-form',
  templateUrl: './trabajo-extra-form.component.html'
})
export class TrabajoExtraFormComponent {
  form: FormGroup;
  
  // Valores calculados SOLO para display (no se envían al backend)
  displayValues$ = new BehaviorSubject<any>(null);
  
  onSubmit() {
    // ✅ SOLUCIÓN: Enviar valores sin modificar
    const formValue = this.form.value;
    this.trabajoExtraService.crear(formValue).subscribe(...);
  }
  
  onPrecioChange(gasto: FormGroup, nuevoPrecio: number) {
    // ✅ SOLUCIÓN: Guardar el precio total sin dividir
    gasto.patchValue({
      precioUnitario: nuevoPrecio  // ✅ VALOR TOTAL
    });
    
    // Si necesitas mostrar el precio semanal en la UI:
    this.actualizarValoresDisplay();
  }
  
  // Función auxiliar SOLO para mostrar valores divididos en UI (no enviar)
  private actualizarValoresDisplay() {
    const formValue = this.form.value;
    const semanas = formValue.tiempoEstimadoTerminacion || 1;
    
    const valoresDisplay = {
      itemsCalculadora: formValue.itemsCalculadora.map(item => ({
        ...item,
        gastosGenerales: item.gastosGenerales.map(gasto => ({
          ...gasto,
          precioSemanalDisplay: gasto.precioUnitario / semanas,  // Solo display
          subtotalSemanalDisplay: gasto.subtotal / semanas  // Solo display
        }))
      }))
    };
    
    this.displayValues$.next(valoresDisplay);
  }
}
```

### ✅ TEMPLATE CORRECTO - trabajo-extra-form.component.html

```html
<!-- ✅ Mostrar valores semanales SI LOS NECESITAS, pero no enviarlos -->
<div *ngFor="let gasto of gastosGenerales.controls; let i = index">
  <mat-form-field>
    <mat-label>Precio Total</mat-label>
    <input matInput type="number" 
           [formControl]="gasto.get('precioUnitario')"
           (change)="onPrecioChange(gasto, $event.target.value)">
  </mat-form-field>
  
  <!-- Mostrar precio semanal solo como información (readonly) -->
  <div class="info-display">
    <small>Precio semanal: {{ calcularPrecioSemanal(gasto.value.precioUnitario) | currency }}</small>
  </div>
</div>
```

---

## 3️⃣ HELPERS Y UTILIDADES

### ❌ INCORRECTO - calculadora.helper.ts

```typescript
export class CalculadoraHelper {
  
  // ❌ PROBLEMA: Divide importes
  static prepararGastosParaGuardar(
    gastos: Gasto[], 
    semanas: number
  ): GastoDTO[] {
    return gastos.map(gasto => ({
      descripcion: gasto.descripcion,
      cantidad: gasto.cantidad,
      precioUnitario: gasto.precio / semanas,  // ❌ DIVISIÓN
      subtotal: this.calcularSubtotal(gasto, semanas)  // ❌ DIVISIÓN
    }));
  }
  
  static calcularSubtotal(gasto: Gasto, semanas: number): number {
    return (gasto.cantidad * gasto.precio) / semanas;  // ❌ DIVISIÓN
  }
  
  static calcularTotalPresupuesto(items: Item[], semanas: number): number {
    return items.reduce((total, item) => {
      return total + (item.total / semanas);  // ❌ DIVISIÓN
    }, 0);
  }
}
```

### ✅ CORRECTO - calculadora.helper.ts

```typescript
export class CalculadoraHelper {
  
  // ✅ SOLUCIÓN: NO dividir al preparar para guardar
  static prepararGastosParaGuardar(gastos: Gasto[]): GastoDTO[] {
    return gastos.map(gasto => ({
      descripcion: gasto.descripcion,
      cantidad: gasto.cantidad,
      precioUnitario: gasto.precio,  // ✅ VALOR TOTAL
      subtotal: this.calcularSubtotal(gasto)  // ✅ VALOR TOTAL
    }));
  }
  
  static calcularSubtotal(gasto: Gasto): number {
    return gasto.cantidad * gasto.precio;  // ✅ SIN DIVISIÓN
  }
  
  static calcularTotalPresupuesto(items: Item[]): number {
    return items.reduce((total, item) => {
      return total + item.total;  // ✅ SIN DIVISIÓN
    }, 0);
  }
  
  // ✅ Función SEPARADA solo para display (no usar para guardar)
  static calcularSubtotalSemanal(gasto: Gasto, semanas: number): number {
    return this.calcularSubtotal(gasto) / (semanas || 1);
  }
  
  static calcularTotalPresupuestoSemanal(items: Item[], semanas: number): number {
    return this.calcularTotalPresupuesto(items) / (semanas || 1);
  }
}
```

---

## 4️⃣ MODELOS Y TRANSFORMADORES

### ❌ INCORRECTO - trabajo-extra.model.ts

```typescript
export class TrabajoExtra {
  id: number;
  nombre: string;
  tiempoEstimadoTerminacion: number;
  gastosGenerales: Gasto[];
  
  // ❌ PROBLEMA: toDTO() divide valores
  toDTO(): TrabajoExtraDTO {
    return {
      id: this.id,
      nombre: this.nombre,
      tiempoEstimadoTerminacion: this.tiempoEstimadoTerminacion,
      gastosGenerales: this.gastosGenerales.map(g => ({
        descripcion: g.descripcion,
        cantidad: g.cantidad,
        precioUnitario: g.precio / this.tiempoEstimadoTerminacion,  // ❌ DIVISIÓN
        subtotal: (g.cantidad * g.precio) / this.tiempoEstimadoTerminacion  // ❌ DIVISIÓN
      }))
    };
  }
  
  get totalSemanal(): number {
    return this.total / this.tiempoEstimadoTerminacion;  // ❌ Si se usa para guardar
  }
}
```

### ✅ CORRECTO - trabajo-extra.model.ts

```typescript
export class TrabajoExtra {
  id: number;
  nombre: string;
  tiempoEstimadoTerminacion: number;
  gastosGenerales: Gasto[];
  
  // ✅ SOLUCIÓN: toDTO() NO divide valores
  toDTO(): TrabajoExtraDTO {
    return {
      id: this.id,
      nombre: this.nombre,
      tiempoEstimadoTerminacion: this.tiempoEstimadoTerminacion,
      gastosGenerales: this.gastosGenerales.map(g => ({
        descripcion: g.descripcion,
        cantidad: g.cantidad,
        precioUnitario: g.precio,  // ✅ VALOR TOTAL
        subtotal: g.cantidad * g.precio  // ✅ VALOR TOTAL
      }))
    };
  }
  
  // ✅ Getter SOLO para display (no enviar al backend)
  get totalSemanalDisplay(): number {
    return this.total / (this.tiempoEstimadoTerminacion || 1);
  }
  
  // ✅ Método SOLO para mostrar gastos semanales en UI
  getGastoSemanalDisplay(gasto: Gasto): number {
    return (gasto.cantidad * gasto.precio) / (this.tiempoEstimadoTerminacion || 1);
  }
}
```

---

## 5️⃣ INTERCEPTORS HTTP

### ❌ INCORRECTO - data-transform.interceptor.ts

```typescript
@Injectable()
export class DataTransformInterceptor implements HttpInterceptor {
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    
    // ❌ PROBLEMA: Modifica payloads de trabajos extra
    if (req.url.includes('/trabajos-extra') && req.method === 'POST') {
      const body = req.body;
      
      if (body?.tiempoEstimadoTerminacion) {
        const semanas = body.tiempoEstimadoTerminacion;
        
        const modifiedBody = {
          ...body,
          itemsCalculadora: body.itemsCalculadora?.map(item => ({
            ...item,
            gastosGenerales: item.gastosGenerales?.map(g => ({
              ...g,
              precioUnitario: g.precioUnitario / semanas,  // ❌ DIVISIÓN
              subtotal: g.subtotal / semanas  // ❌ DIVISIÓN
            }))
          }))
        };
        
        req = req.clone({ body: modifiedBody });
      }
    }
    
    return next.handle(req);
  }
}
```

### ✅ CORRECTO - data-transform.interceptor.ts

```typescript
@Injectable()
export class DataTransformInterceptor implements HttpInterceptor {
  
  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    
    // ✅ SOLUCIÓN: NO modificar importes/precios
    // Solo pasar el request sin alteraciones
    return next.handle(req);
    
    // Si necesitas hacer otras transformaciones (fechas, formato, etc.):
    // Hazlo, pero NUNCA dividas importes por semanas
  }
}
```

---

## 6️⃣ VALIDACIÓN EN DEVTOOLS

### Cómo verificar que el fix funciona:

1. **Abrir DevTools** (F12)
2. **Ir a Network Tab**
3. **Filtrar por:** `trabajos-extra` o `presupuestos`
4. **Crear un trabajo extra de prueba:**
   - Duración: 10 semanas
   - Gasto "Test": $100.000

5. **Buscar el request POST**
6. **Click en Request → Payload**
7. **Verificar:**

```json
// ✅ CORRECTO - Payload debe verse así:
{
  "nombre": "Trabajo Test",
  "tiempoEstimadoTerminacion": 10,
  "itemsCalculadora": [{
    "tipoProfesional": "Albañilería",
    "gastosGenerales": [{
      "descripcion": "Test",
      "cantidad": 1,
      "precioUnitario": 100000,   // ✅ CORRECTO: $100.000 (sin dividir)
      "subtotal": 100000          // ✅ CORRECTO: $100.000 (sin dividir)
    }]
  }]
}

// ❌ INCORRECTO - Si ves esto, AÚN hay división:
{
  "nombre": "Trabajo Test",
  "tiempoEstimadoTerminacion": 10,
  "itemsCalculadora": [{
    "tipoProfesional": "Albañilería",
    "gastosGenerales": [{
      "descripcion": "Test",
      "cantidad": 1,
      "precioUnitario": 10000,    // ❌ INCORRECTO: $10.000 (dividido por 10)
      "subtotal": 10000           // ❌ INCORRECTO: $10.000 (dividido por 10)
    }]
  }]
}
```

---

## 7️⃣ CHECKLIST FINAL

Después de aplicar todos los cambios:

- [ ] ✅ Eliminé TODAS las divisiones `/semanas` en servicios
- [ ] ✅ Eliminé TODAS las divisiones en componentes
- [ ] ✅ Eliminé TODAS las divisiones en helpers
- [ ] ✅ Eliminé TODAS las divisiones en modelos (método `toDTO()`)
- [ ] ✅ Eliminé TODAS las divisiones en interceptors
- [ ] ✅ Separé valores para guardar (sin dividir) de valores para display (pueden dividirse)
- [ ] ✅ Verifiqué en Network Tab: Payload correcto
- [ ] ✅ Probé crear trabajo extra: Se guarda correctamente en BD
- [ ] ✅ Probé editar trabajo extra: Valores se mantienen correctos
- [ ] ✅ Probé obtener trabajo extra: GET devuelve valores correctos

---

**IMPORTANTE:** Si después de estos cambios el problema persiste, busca en:
- State management (NgRx, Akita, etc.) - Puede haber transformaciones en effects/services
- Pipes personalizados que se usen en formularios
- Directivas que modifiquen valores de inputs
- Validadores personalizados que transformen valores
