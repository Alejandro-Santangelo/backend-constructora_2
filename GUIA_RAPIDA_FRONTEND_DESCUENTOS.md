# GUÍA RÁPIDA PARA EL FRONTEND: Descuentos en Honorarios y Mayores Costos

## ⚡ CAMBIOS NECESARIOS EN EL FRONTEND

### 1. Actualizar el estado inicial de `descuentosPorRubro`

**ANTES (solo 3 conceptos):**
```javascript
{
  nombreRubro: "Herrería",
  activo: true,
  profesionalesActivo: false,
  profesionalesTipo: "PORCENTAJE",
  profesionalesValor: 10,
  materialesActivo: false,
  materialesTipo: "PORCENTAJE",
  materialesValor: 10,
  otrosCostosActivo: false,
  otrosCostosTipo: "PORCENTAJE",
  otrosCostosValor: 0
}
```

**DESPUÉS (5 conceptos):**
```javascript
{
  nombreRubro: "Herrería",
  activo: true,
  profesionalesActivo: false,
  profesionalesTipo: "PORCENTAJE",
  profesionalesValor: 10,
  materialesActivo: false,
  materialesTipo: "PORCENTAJE",
  materialesValor: 10,
  otrosCostosActivo: false,
  otrosCostosTipo: "PORCENTAJE",
  otrosCostosValor: 0,
  // ⬇️ AGREGAR ESTOS 6 CAMPOS:
  honorariosActivo: false,
  honorariosTipo: "PORCENTAJE",
  honorariosValor: 10,
  mayoresCostosActivo: false,
  mayoresCostosTipo: "PORCENTAJE",
  mayoresCostosValor: 10
}
```

---

## 2. Actualizar la función de mapeo al cargar presupuesto

**Problema común:** Si el backend devuelve los campos pero el frontend los ignora

```javascript
// ❌ MALO: Solo mapea 3 conceptos
const mapDescuentosFromBackend = (descuentos) => {
  return descuentos.map(d => ({
    nombreRubro: d.nombreRubro,
    activo: d.activo,
    profesionalesActivo: d.profesionalesActivo,
    profesionalesTipo: d.profesionalesTipo,
    profesionalesValor: d.profesionalesValor,
    materialesActivo: d.materialesActivo,
    materialesTipo: d.materialesTipo,
    materialesValor: d.materialesValor,
    otrosCostosActivo: d.otrosCostosActivo,
    otrosCostosTipo: d.otrosCostosTipo,
    otrosCostosValor: d.otrosCostosValor
    // ⚠️ FALTA HONORARIOS Y MAYORES COSTOS
  }));
};

// ✅ BUENO: Mapea todos los campos
const mapDescuentosFromBackend = (descuentos) => {
  return descuentos.map(d => ({
    nombreRubro: d.nombreRubro,
    activo: d.activo ?? true,
    
    profesionalesActivo: d.profesionalesActivo ?? false,
    profesionalesTipo: d.profesionalesTipo ?? "PORCENTAJE",
    profesionalesValor: d.profesionalesValor ?? 0,
    
    materialesActivo: d.materialesActivo ?? false,
    materialesTipo: d.materialesTipo ?? "PORCENTAJE",
    materialesValor: d.materialesValor ?? 0,
    
    otrosCostosActivo: d.otrosCostosActivo ?? false,
    otrosCostosTipo: d.otrosCostosTipo ?? "PORCENTAJE",
    otrosCostosValor: d.otrosCostosValor ?? 0,
    
    // ✅ AGREGAR:
    honorariosActivo: d.honorariosActivo ?? false,
    honorariosTipo: d.honorariosTipo ?? "PORCENTAJE",
    honorariosValor: d.honorariosValor ?? 0,
    
    mayoresCostosActivo: d.mayoresCostosActivo ?? false,
    mayoresCostosTipo: d.mayoresCostosTipo ?? "PORCENTAJE",
    mayoresCostosValor: d.mayoresCostosValor ?? 0
  }));
};
```

---

## 3. Actualizar el cálculo de descuentos

**ANTES:**
```javascript
const calcularDescuentoRubro = (descuento, rubro) => {
  let totalDescuento = 0;
  
  // Descuento en profesionales
  if (descuento.profesionalesActivo) {
    const base = rubro.totalProfesionales;
    totalDescuento += calcularDescuento(base, descuento.profesionalesTipo, descuento.profesionalesValor);
  }
  
  // Descuento en materiales
  if (descuento.materialesActivo) {
    const base = rubro.totalMateriales;
    totalDescuento += calcularDescuento(base, descuento.materialesTipo, descuento.materialesValor);
  }
  
  // Descuento en otros costos
  if (descuento.otrosCostosActivo) {
    const base = rubro.totalOtrosCostos;
    totalDescuento += calcularDescuento(base, descuento.otrosCostosTipo, descuento.otrosCostosValor);
  }
  
  return totalDescuento;
};
```

**DESPUÉS:**
```javascript
const calcularDescuentoRubro = (descuento, rubro) => {
  let totalDescuento = 0;
  
  // Descuento en profesionales
  if (descuento.profesionalesActivo) {
    const base = rubro.totalProfesionales;
    totalDescuento += calcularDescuento(base, descuento.profesionalesTipo, descuento.profesionalesValor);
  }
  
  // Descuento en materiales
  if (descuento.materialesActivo) {
    const base = rubro.totalMateriales;
    totalDescuento += calcularDescuento(base, descuento.materialesTipo, descuento.materialesValor);
  }
  
  // Descuento en otros costos
  if (descuento.otrosCostosActivo) {
    const base = rubro.totalOtrosCostos;
    totalDescuento += calcularDescuento(base, descuento.otrosCostosTipo, descuento.otrosCostosValor);
  }
  
  // ✅ AGREGAR: Descuento en honorarios
  if (descuento.honorariosActivo) {
    const base = rubro.totalHonorarios; // Total de honorarios del rubro
    totalDescuento += calcularDescuento(base, descuento.honorariosTipo, descuento.honorariosValor);
  }
  
  // ✅ AGREGAR: Descuento en mayores costos
  if (descuento.mayoresCostosActivo) {
    const base = rubro.totalMayoresCostos; // Total de mayores costos del rubro
    totalDescuento += calcularDescuento(base, descuento.mayoresCostosTipo, descuento.mayoresCostosValor);
  }
  
  return totalDescuento;
};

// Función auxiliar (si no existe)
const calcularDescuento = (base, tipo, valor) => {
  if (tipo === "PORCENTAJE") {
    return (base * valor) / 100;
  } else if (tipo === "IMPORTE_FIJO") {
    return valor;
  }
  return 0;
};
```

---

## 4. Verificar antes de enviar al backend

**Agregar esto ANTES de hacer POST/PUT:**

```javascript
const handleGuardarPresupuesto = async () => {
  // ✅ VERIFICACIÓN: Asegurar que todos los campos existan
  const presupuestoData = {
    ...formData,
    descuentosPorRubro: descuentosPorRubro.map(desc => {
      // Verificar que los campos nuevos existan
      if (desc.honorariosActivo === undefined) {
        console.error('❌ FALTA honorariosActivo en rubro:', desc.nombreRubro);
      }
      if (desc.mayoresCostosActivo === undefined) {
        console.error('❌ FALTA mayoresCostosActivo en rubro:', desc.nombreRubro);
      }
      
      return {
        ...desc,
        // Asegurar valores por defecto
        honorariosActivo: desc.honorariosActivo ?? false,
        honorariosTipo: desc.honorariosTipo ?? "PORCENTAJE",
        honorariosValor: desc.honorariosValor ?? 0,
        mayoresCostosActivo: desc.mayoresCostosActivo ?? false,
        mayoresCostosTipo: desc.mayoresCostosTipo ?? "PORCENTAJE",
        mayoresCostosValor: desc.mayoresCostosValor ?? 0
      };
    })
  };
  
  // Debug: Ver qué se envía
  console.log('📤 Enviando al backend:', JSON.stringify(presupuestoData.descuentosPorRubro, null, 2));
  
  // Enviar al backend
  const response = await fetch('/api/presupuestos-no-cliente', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(presupuestoData)
  });
  
  const resultado = await response.json();
  
  // Debug: Ver qué devuelve el backend
  console.log('📥 Recibido del backend:', JSON.stringify(resultado.descuentosPorRubro, null, 2));
};
```

---

## 5. Actualizar el componente visual (UI)

**Si tienes un componente para configurar descuentos, agregar:**

```jsx
{/* Sección Honorarios */}
<div className="descuento-seccion">
  <label>
    <input
      type="checkbox"
      checked={descuento.honorariosActivo}
      onChange={(e) => handleToggleDescuento('honorariosActivo', e.target.checked)}
    />
    Descuento en Honorarios
  </label>
  
  {descuento.honorariosActivo && (
    <>
      <select
        value={descuento.honorariosTipo}
        onChange={(e) => handleChangeTipo('honorariosTipo', e.target.value)}
      >
        <option value="PORCENTAJE">Porcentaje</option>
        <option value="IMPORTE_FIJO">Importe Fijo</option>
      </select>
      
      <input
        type="number"
        value={descuento.honorariosValor}
        onChange={(e) => handleChangeValor('honorariosValor', parseFloat(e.target.value))}
        placeholder={descuento.honorariosTipo === "PORCENTAJE" ? "%" : "$"}
      />
    </>
  )}
</div>

{/* Sección Mayores Costos */}
<div className="descuento-seccion">
  <label>
    <input
      type="checkbox"
      checked={descuento.mayoresCostosActivo}
      onChange={(e) => handleToggleDescuento('mayoresCostosActivo', e.target.checked)}
    />
    Descuento en Mayores Costos
  </label>
  
  {descuento.mayoresCostosActivo && (
    <>
      <select
        value={descuento.mayoresCostosTipo}
        onChange={(e) => handleChangeTipo('mayoresCostosTipo', e.target.value)}
      >
        <option value="PORCENTAJE">Porcentaje</option>
        <option value="IMPORTE_FIJO">Importe Fijo</option>
      </select>
      
      <input
        type="number"
        value={descuento.mayoresCostosValor}
        onChange={(e) => handleChangeValor('mayoresCostosValor', parseFloat(e.target.value))}
        placeholder={descuento.mayoresCostosTipo === "PORCENTAJE" ? "%" : "$"}
      />
    </>
  )}
</div>
```

---

## 🧪 TEST RÁPIDO

**1. En la consola del navegador:**
```javascript
// Después de cargar un presupuesto
console.log('Descuentos:', presupuesto.descuentosPorRubro);

// Verificar que existan los campos
const primerDescuento = presupuesto.descuentosPorRubro[0];
console.log('honorariosActivo:', primerDescuento.honorariosActivo);
console.log('mayoresCostosActivo:', primerDescuento.mayoresCostosActivo);

// Si sale 'undefined' → el frontend NO está mapeando los campos
// Si sale 'true' o 'false' → ✅ funciona correctamente
```

**2. Ejecutar script de prueba del backend:**
```powershell
.\test-descuentos-endpoint.ps1
```

Esto verificará que el backend está devolviendo los campos correctamente.

---

## 🐛 SI SIGUE FALLANDO

1. **Verificar en Network DevTools:**
   - Ir a la pestaña Network
   - Filtrar por "presupuesto"
   - Click en la petición GET
   - Ver la respuesta JSON RAW
   - Buscar "honorariosActivo" en el JSON
   - Si NO aparece → problema en el backend (reportar)
   - Si SÍ aparece → problema en el frontend (revisar mapeo)

2. **Verificar en la base de datos:**
```sql
SELECT * FROM descuentos_por_rubro WHERE presupuesto_no_cliente_id = 123;
```

3. **Compartir logs:**
   - JSON que se envía (POST/PUT)
   - JSON que se recibe (GET)
   - Resultado del query SQL
