# 🐛 BUG DE CÁLCULO EN FRONTEND - Presupuestos No Cliente

## Problema Detectado

El frontend está enviando valores incorrectos en los campos de totales al crear/actualizar presupuestos.

### Ejemplo Real (Presupuesto "Casa de Paula")

**UI muestra correctamente:**
```
Subtotal (Sin descuentos): $6.000.000,00
Total Descuentos: $600.000,00
TOTAL FINAL: $5.400.000,00
```

**Pero envía al backend:**
```json
{
  "totalPresupuesto": 3000000.00,           // ✅ OK
  "totalHonorarios": 3000000.00,            // ✅ OK
  "totalPresupuestoConHonorarios": 5700000.00,  // ❌ INCORRECTO (debería ser 6000000)
  "totalConDescuentos": 4800000.00          // ❌ INCORRECTO (debería ser 5400000)
}
```

**Error:** $300.000 en ambos campos (5% del total)

---

## Causa Probable

El código del frontend está aplicando **descuentos en el lugar equivocado** al calcular el JSON que se envía al backend.

### Cálculo Correcto vs Incorrecto

#### ✅ **CORRECTO:**
```javascript
const base = 3000000;
const honorarios = 3000000;
const totalSinDescuento = base + honorarios;  // 6000000 ← debe ir a totalPresupuestoConHonorarios
const descuentos = 600000;
const totalConDescuento = totalSinDescuento - descuentos;  // 5400000 ← debe ir a totalConDescuentos
```

#### ❌ **INCORRECTO (probablemente lo que está haciendo):**
```javascript
const base = 3000000;
const honorarios = 3000000;
const descuentosSobreHonorarios = 300000;  // 10% de honorarios
const totalSinDescuento = base + honorarios - descuentosSobreHonorarios;  // 5700000 ← MAL
const descuentos = 600000;
const totalConDescuento = totalSinDescuento - descuentos;  // 4800000 ← MAL
```

---

## Solución para el Frontend

### 1️⃣ **Encontrar dónde se calculan los totales antes de enviar**

Buscar en el código del frontend:
```javascript
// Buscar algo como:
prepareDtoParaBackend()
calcularTotalesPresupuesto()
getTotalPresupuestoConHonorarios()
```

### 2️⃣ **Aplicar esta lógica EXACTA:**

```javascript
/**
 * Cálculo correcto de totales para enviar al backend
 */
function calcularTotalesPresupuesto() {
  // PASO 1: Calcular BASE (sin honorarios, sin descuentos)
  const totalJornalesSinHonorarios = calcularTotalJornales();
  const totalMaterialesSinHonorarios = calcularTotalMateriales();
  const totalGastosGeneralesSinHonorarios = calcularTotalGastosGenerales();
  const totalOtrosCostosSinHonorarios = calcularTotalOtrosCostos();
  
  const totalPresupuesto = 
    totalJornalesSinHonorarios + 
    totalMaterialesSinHonorarios + 
    totalGastosGeneralesSinHonorarios + 
    totalOtrosCostosSinHonorarios;
  
  // PASO 2: Calcular HONORARIOS TOTALES
  const honorariosJornales = calcularHonorario(totalJornalesSinHonorarios, configuracionHonorarios.jornales);
  const honorariosMateriales = calcularHonorario(totalMaterialesSinHonorarios, configuracionHonorarios.materiales);
  const honorariosGastos = calcularHonorario(totalGastosGeneralesSinHonorarios, configuracionHonorarios.gastosGenerales);
  const honorariosOtros = calcularHonorario(totalOtrosCostosSinHonorarios, configuracionHonorarios.otros);
  
  const totalHonorarios = 
    honorariosJornales + 
    honorariosMateriales + 
    honorariosGastos + 
    honorariosOtros;
  
  // PASO 3: TOTAL SIN DESCUENTO = BASE + HONORARIOS
  // ⚠️ CRÍTICO: NO restar descuentos aquí
  const totalPresupuestoConHonorarios = totalPresupuesto + totalHonorarios;
  
  // PASO 4: Calcular DESCUENTOS (sobre base Y sobre honorarios)
  const descuentosBase = calcularDescuentosBase(
    totalJornalesSinHonorarios, 
    totalMaterialesSinHonorarios,
    totalGastosGeneralesSinHonorarios,
    totalOtrosCostosSinHonorarios
  );
  
  const descuentosHonorarios = calcularDescuentosHonorarios(
    honorariosJornales,
    honorariosMateriales,
    honorariosGastos,
    honorariosOtros
  );
  
  const totalDescuentos = descuentosBase + descuentosHonorarios;
  
  // PASO 5: TOTAL FINAL = TOTAL SIN DESCUENTO - DESCUENTOS
  const totalConDescuentos = totalPresupuestoConHonorarios - totalDescuentos;
  
  return {
    totalPresupuesto: totalPresupuesto,                      // Base sin honorarios
    totalHonorarios: totalHonorarios,                        // Honorarios totales
    totalPresupuestoConHonorarios: totalPresupuestoConHonorarios,  // Base + Honorarios (SIN descuentos)
    totalConDescuentos: totalConDescuentos,                  // Total final (CON descuentos)
    totalDescuentos: totalDescuentos                         // Total de descuentos aplicados
  };
}
```

### 3️⃣ **Validación ANTES de enviar al backend:**

```javascript
/**
 * Validar coherencia de totales antes de enviar
 */
function validarTotalesAntesDeEnviar(dto) {
  const sumaEsperada = dto.totalPresupuesto + dto.totalHonorarios;
  const diferencia = Math.abs(sumaEsperada - dto.totalPresupuestoConHonorarios);
  
  if (diferencia > 0.01) {  // Tolerancia de 1 centavo
    console.error('❌ ERROR DE CÁLCULO:', {
      totalPresupuesto: dto.totalPresupuesto,
      totalHonorarios: dto.totalHonorarios,
      sumaEsperada: sumaEsperada,
      totalPresupuestoConHonorarios: dto.totalPresupuestoConHonorarios,
      diferencia: diferencia
    });
    throw new Error(
      `Total con honorarios incorrecto. ` +
      `Esperado: $${sumaEsperada}, enviando: $${dto.totalPresupuestoConHonorarios}`
    );
  }
  
  if (dto.totalConDescuentos > dto.totalPresupuestoConHonorarios) {
    console.error('❌ ERROR: Total con descuentos mayor que total sin descuentos');
    throw new Error('Total con descuentos no puede ser mayor que total sin descuentos');
  }
  
  console.log('✅ Validación de totales OK');
}

// Usar antes de enviar:
const dto = calcularTotalesPresupuesto();
validarTotalesAntesDeEnviar(dto);
enviarAlBackend(dto);
```

---

## Impacto del Bug

### Antes (sin validación backend):
- ❌ Presupuestos guardados con totales incorrectos
- ❌ PDFs con montos incorrectos
- ❌ Reportes financieros con datos erróneos
- ❌ Pérdida de confianza del cliente

### Ahora (con validación backend):
- ✅ Backend rechaza presupuestos con cálculos incorrectos
- ❌ Pero el usuario ve error 400 sin explicación clara en UI
- ⚠️ **Solución:** Arreglar cálculo en frontend + mejorar mensaje de error en UI

---

## Testing Recomendado

### Test 1: Presupuesto sin descuentos
```javascript
const caso1 = {
  base: 1000000,
  honorarios: 500000,
  descuentos: 0
};
// Debe dar: totalPresupuestoConHonorarios = 1500000
//           totalConDescuentos = 1500000
```

### Test 2: Presupuesto con descuentos del 10%
```javascript
const caso2 = {
  base: 3000000,
  honorarios: 3000000,
  descuentosSobreBase: 300000,      // 10% de base
  descuentosSobreHonorarios: 300000  // 10% de honorarios
};
// Debe dar: totalPresupuestoConHonorarios = 6000000
//           totalConDescuentos = 5400000
```

### Test 3: Honorarios 100% + Descuentos 10%
```javascript
const caso3 = {
  base: 2000000,
  honorariosPorcentaje: 100,  // 100% → honorarios = 2000000
  descuentoPorcentaje: 10      // 10% sobre 4000000 = 400000
};
// Debe dar: totalPresupuestoConHonorarios = 4000000
//           totalConDescuentos = 3600000
```

---

## Campos del DTO que Envía el Frontend

### PresupuestoNoClienteRequestDTO (campos críticos)

```typescript
interface PresupuestoNoClienteRequestDTO {
  // ... otros campos ...
  
  // TOTALES CALCULADOS (deben ser coherentes)
  totalPresupuesto: number;              // Base SIN honorarios, SIN descuentos
  totalHonorarios: number;               // Honorarios totales calculados
  totalPresupuestoConHonorarios: number; // ⚠️ CRÍTICO: Base + Honorarios (SIN descuentos)
  totalConDescuentos?: number;           // Total final DESPUÉS de descuentos
  
  // DESGLOSE DE BASE
  importeJornales?: number;              // Solo base, sin honorarios
  importeMateriales?: number;            // Solo base, sin honorarios
  importeGastosGenerales?: number;       // Solo base, sin honorarios
  importeOtrosCostos?: number;           // Solo base, sin honorarios
  
  // CONFIGURACIÓN DE HONORARIOS
  honorariosJornalesActivo: boolean;
  honorariosJornalesValor: number;
  honorariosJornalesTipo: 'fijo' | 'porcentaje';
  // ... (mismo patrón para materiales, gastos, otros)
  
  // CONFIGURACIÓN DE DESCUENTOS
  descuentosJornalesActivo: boolean;
  descuentosJornalesValor: number;
  descuentosJornalesTipo: 'fijo' | 'porcentaje';
  // ... (mismo patrón para honorarios y otras categorías)
}
```

---

## Validación Implementada en Backend

El backend ahora valida automáticamente:

```java
private void validarCoherenciaTotales(PresupuestoNoCliente presupuesto) {
    BigDecimal sumaEsperada = totalPresupuesto.add(totalHonorarios);
    BigDecimal diferencia = sumaEsperada.subtract(totalConHonorarios).abs();
    
    if (diferencia.compareTo(new BigDecimal("0.01")) > 0) {
        throw new IllegalArgumentException(
            "Total con honorarios incorrecto. " +
            "Esperado: $" + sumaEsperada + 
            ", recibido: $" + totalConHonorarios
        );
    }
    
    if (totalConDescuentos > totalConHonorarios) {
        throw new IllegalArgumentException(
            "Total con descuentos no puede ser mayor que total sin descuentos"
        );
    }
}
```

**Esto se ejecuta en:**
- ✅ POST /presupuestos-no-cliente (crear)
- ✅ PUT /presupuestos-no-cliente/{id} (actualizar)
- ✅ POST /presupuestos-no-cliente/{id}/duplicar (duplicar)

---

## Checklist para el Desarrollador Frontend

- [ ] Encontrar función/método que calcula totales antes de enviar al backend
- [ ] Verificar que `totalPresupuestoConHonorarios` = `base + honorarios` (SIN restar descuentos)
- [ ] Verificar que `totalConDescuentos` = `totalPresupuestoConHonorarios - descuentos`
- [ ] Agregar validación en frontend ANTES de enviar (mejor UX que error del backend)
- [ ] Testear con casos de prueba arriba mencionados
- [ ] Verificar que la UI muestra los mismos valores que envía al backend
- [ ] Revisar presupuestos existentes con totales incorrectos y considerar recalcularlos

---

## Prioridad

🔴 **ALTA** - Bug crítico que afecta cálculos financieros

## Estado

- ✅ Backend: Validación implementada (rechaza cálculos incorrectos)
- ⏳ Frontend: Pendiente corrección del cálculo
- ⏳ Base de datos: Presupuesto ID 104 corregido manualmente (pueden existir otros)

## Contacto

Si necesitas ayuda para ubicar el código específico en el frontend, proporciona:
- Framework usado (React, Angular, Vue, etc.)
- Estructura de carpetas del proyecto frontend
- Archivo donde se prepara el DTO para enviar al backend
