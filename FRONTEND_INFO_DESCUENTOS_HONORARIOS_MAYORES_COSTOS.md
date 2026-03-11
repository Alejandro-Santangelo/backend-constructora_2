# DOCUMENTACIÓN BACKEND: Descuentos por Rubro - Honorarios y Mayores Costos

## ✅ ESTADO DE LA IMPLEMENTACIÓN

### Base de Datos
- ✅ Columnas agregadas a tabla `descuentos_por_rubro`:
  - `honorarios_activo` (BOOLEAN, default: false)
  - `honorarios_tipo` (VARCHAR(20), default: 'PORCENTAJE')
  - `honorarios_valor` (NUMERIC(10,2), nullable)
  - `mayores_costos_activo` (BOOLEAN, default: false)
  - `mayores_costos_tipo` (VARCHAR(20), default: 'PORCENTAJE')
  - `mayores_costos_valor` (NUMERIC(10,2), nullable)

### Backend Java
- ✅ Entity `DescuentoPorRubro` actualizada con los 6 campos nuevos
- ✅ DTO `DescuentoPorRubroDTO` actualizado con los 6 campos nuevos
- ✅ Método `mapearDescuentosPorRubroDTO()` actualizado para mapear los 5 conceptos
- ✅ Compilación exitosa

---

## 📤 FORMATO JSON PARA ENVIAR AL BACKEND (POST/PUT)

### Endpoint: `POST /api/presupuestos-no-cliente`
### Endpoint: `PUT /api/presupuestos-no-cliente/{id}`

### Estructura del objeto `descuentosPorRubro`:

```json
{
  "...otros campos del presupuesto",
  "descuentosPorRubro": [
    {
      "id": null,
      "nombreRubro": "Herrería",
      "activo": true,
      
      "profesionalesActivo": true,
      "profesionalesTipo": "PORCENTAJE",
      "profesionalesValor": 10,
      
      "materialesActivo": true,
      "materialesTipo": "PORCENTAJE",
      "materialesValor": 10,
      
      "otrosCostosActivo": false,
      "otrosCostosTipo": "PORCENTAJE",
      "otrosCostosValor": 0,
      
      "honorariosActivo": true,
      "honorariosTipo": "PORCENTAJE",
      "honorariosValor": 10,
      
      "mayoresCostosActivo": true,
      "mayoresCostosTipo": "PORCENTAJE",
      "mayoresCostosValor": 10
    }
  ]
}
```

### Tipos de descuento válidos:
- `"PORCENTAJE"` → descuento = (base * valor) / 100
- `"IMPORTE_FIJO"` → descuento = valor

---

## 📥 FORMATO JSON QUE DEVUELVE EL BACKEND (GET)

### Endpoint: `GET /api/presupuestos-no-cliente/{id}?empresaId={empresaId}`

### Estructura de la respuesta:

```json
{
  "id": 123,
  "...otros campos del presupuesto",
  "descuentosPorRubro": [
    {
      "id": 1,
      "nombreRubro": "Herrería",
      "activo": true,
      "tipo": "porcentaje",
      "valor": null,
      
      "profesionalesActivo": true,
      "profesionalesTipo": "PORCENTAJE",
      "profesionalesValor": 10.00,
      
      "materialesActivo": true,
      "materialesTipo": "PORCENTAJE",
      "materialesValor": 10.00,
      
      "otrosCostosActivo": false,
      "otrosCostosTipo": "PORCENTAJE",
      "otrosCostosValor": 0.00,
      
      "honorariosActivo": true,
      "honorariosTipo": "PORCENTAJE",
      "honorariosValor": 10.00,
      
      "mayoresCostosActivo": true,
      "mayoresCostosTipo": "PORCENTAJE",
      "mayoresCostosValor": 10.00,
      
      "fechaCreacion": "2026-03-11T15:30:00",
      "fechaModificacion": "2026-03-11T15:30:00"
    }
  ]
}
```

**IMPORTANTE**: Jackson serializa automáticamente TODOS los campos de la Entity, 
incluyendo `honorariosActivo`, `honorariosTipo`, `honorariosValor`, 
`mayoresCostosActivo`, `mayoresCostosTipo` y `mayoresCostosValor`.

---

## 🔍 VERIFICACIÓN EN EL FRONTEND

### 1. Al recibir el presupuesto, verificar que los campos existan:

```javascript
console.log('Descuentos recibidos:', presupuesto.descuentosPorRubro);

presupuesto.descuentosPorRubro.forEach(descuento => {
  console.log('Rubro:', descuento.nombreRubro);
  console.log('Honorarios:', {
    activo: descuento.honorariosActivo,
    tipo: descuento.honorariosTipo,
    valor: descuento.honorariosValor
  });
  console.log('Mayores Costos:', {
    activo: descuento.mayoresCostosActivo,
    tipo: descuento.mayoresCostosTipo,
    valor: descuento.mayoresCostosValor
  });
});
```

### 2. Si los campos vienen `undefined`, verificar:

❌ **PROBLEMA**: Los campos no llegan al frontend
✅ **SOLUCIÓN**: 
1. Verificar en la consola del navegador la respuesta RAW del endpoint
2. Abrir DevTools → Network → Click en la petición GET
3. Ver la respuesta JSON completa
4. Buscar `"honorariosActivo"` y `"mayoresCostosActivo"` en el JSON

### 3. Verificar que se están enviando al backend:

```javascript
console.log('Enviando al backend:', JSON.stringify(presupuestoData, null, 2));

// Verificar que descuentosPorRubro incluya los campos:
presupuestoData.descuentosPorRubro.forEach(desc => {
  if (desc.honorariosActivo === undefined) {
    console.error('❌ FALTA honorariosActivo en:', desc.nombreRubro);
  }
  if (desc.mayoresCostosActivo === undefined) {
    console.error('❌ FALTA mayoresCostosActivo en:', desc.nombreRubro);
  }
});
```

---

## 🐛 DIAGNÓSTICO DE PROBLEMAS

### Problema 1: "Los campos no se guardan en la BD"

**Verificar en PostgreSQL:**
```sql
SELECT 
  id, 
  nombre_rubro,
  honorarios_activo, 
  honorarios_tipo, 
  honorarios_valor,
  mayores_costos_activo,
  mayores_costos_tipo,
  mayores_costos_valor
FROM descuentos_por_rubro
ORDER BY id DESC
LIMIT 5;
```

**Si los valores están NULL o false cuando deberían tener datos:**
→ El frontend NO está enviando los campos en el JSON del POST/PUT

### Problema 2: "Los campos se guardan pero no vuelven al frontend"

**Verificar directamente en el endpoint:**
```bash
curl -X GET "http://localhost:8080/api/presupuestos-no-cliente/123?empresaId=1" \
  -H "Accept: application/json" | jq '.descuentosPorRubro[0]'
```

**Debería mostrar:**
```json
{
  "id": 1,
  "nombreRubro": "Herrería",
  "honorariosActivo": true,
  "honorariosTipo": "PORCENTAJE",
  "honorariosValor": 10.00,
  "mayoresCostosActivo": true,
  "mayoresCostosTipo": "PORCENTAJE",
  "mayoresCostosValor": 10.00,
  ...
}
```

**Si NO aparecen los campos:**
→ Hay un problema con la serialización JSON (pero esto es improbable porque 
   Jackson serializa automáticamente todos los campos públicos con getters)

### Problema 3: "Error 400 Bad Request al guardar"

**Verificar en los logs del backend:**
```
tail -f backend-constructora_2/logs/spring.log | grep -i "error\|exception"
```

**Posibles causas:**
- Tipo de dato incorrecto (enviar String donde espera Number)
- Campo `null` donde el backend espera un valor
- Falta el campo `empresaId` en el request

---

## 📝 EJEMPLO COMPLETO DE FLUJO

### 1. Frontend crea un presupuesto con descuentos:

```javascript
const presupuestoData = {
  nombre: "Presupuesto Test",
  empresaId: 1,
  obraId: 10,
  // ... otros campos
  descuentosPorRubro: [
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
      // NUEVOS CAMPOS:
      honorariosActivo: true,
      honorariosTipo: "PORCENTAJE",
      honorariosValor: 10,
      mayoresCostosActivo: true,
      mayoresCostosTipo: "PORCENTAJE",
      mayoresCostosValor: 10
    }
  ]
};

const response = await fetch('/api/presupuestos-no-cliente', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify(presupuestoData)
});

const presupuestoGuardado = await response.json();
console.log('Guardado con ID:', presupuestoGuardado.id);
```

### 2. Backend guarda en PostgreSQL:

```sql
INSERT INTO descuentos_por_rubro (
  presupuesto_no_cliente_id,
  nombre_rubro,
  activo,
  profesionales_activo,
  profesionales_tipo,
  profesionales_valor,
  materiales_activo,
  materiales_tipo,
  materiales_valor,
  otros_costos_activo,
  otros_costos_tipo,
  otros_costos_valor,
  honorarios_activo,         -- ✅ NUEVO
  honorarios_tipo,           -- ✅ NUEVO
  honorarios_valor,          -- ✅ NUEVO
  mayores_costos_activo,     -- ✅ NUEVO
  mayores_costos_tipo,       -- ✅ NUEVO
  mayores_costos_valor       -- ✅ NUEVO
) VALUES (
  123, 'Herrería', true,
  false, 'PORCENTAJE', 10.00,
  false, 'PORCENTAJE', 10.00,
  false, 'PORCENTAJE', 0.00,
  true, 'PORCENTAJE', 10.00,   -- ✅ NUEVO
  true, 'PORCENTAJE', 10.00    -- ✅ NUEVO
);
```

### 3. Frontend obtiene el presupuesto:

```javascript
const response = await fetch('/api/presupuestos-no-cliente/123?empresaId=1');
const presupuesto = await response.json();

console.log('Descuentos:', presupuesto.descuentosPorRubro);
// [
//   {
//     id: 1,
//     nombreRubro: "Herrería",
//     honorariosActivo: true,      ✅
//     honorariosTipo: "PORCENTAJE", ✅
//     honorariosValor: 10.00,       ✅
//     mayoresCostosActivo: true,    ✅
//     mayoresCostosTipo: "PORCENTAJE", ✅
//     mayoresCostosValor: 10.00     ✅
//   }
// ]
```

---

## 🚨 CHECKLIST PARA EL FRONTEND

- [ ] Verificar que el estado `descuentosPorRubro` incluya los 6 campos nuevos
- [ ] Verificar que al hacer `console.log(descuentosPorRubro)` antes del POST, 
      los campos `honorariosActivo`, `honorariosTipo`, `honorariosValor`, etc. existan
- [ ] Verificar en DevTools → Network → Request Payload que los campos se envíen
- [ ] Verificar en DevTools → Network → Response que los campos vuelvan
- [ ] Si los campos NO vuelven, verificar directamente en la BD con el SQL de arriba
- [ ] Si los campos SÍ están en la BD pero no vuelven, reportar bug (serialización JSON)

---

## 📞 CONTACTO

Si después de verificar todo lo anterior los campos siguen sin funcionar:

1. Compartir el JSON exacto que se envía en el POST (sin datos sensibles)
2. Compartir el JSON exacto que devuelve el GET
3. Compartir el resultado del query SQL de verificación
4. Compartir cualquier error en la consola del navegador o logs del backend

Backend actualizado: 2026-03-11
Versión: 1.0.0
