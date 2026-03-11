# 🔄 MIGRACIÓN: Honorarios por Rubro - JSON a Relacional

**Fecha:** 10 de marzo de 2026  
**Impacto:** BREAKING CHANGE - Requiere actualización frontend  
**Backend Status:** ✅ Implementado y funcionando  

---

## 📋 Resumen del Cambio

El campo `honorariosPorRubro` migró de **JSON string** a **array de objetos** con tabla relacional en backend.

### ❌ ANTES (Deprecated)
```javascript
{
  "honorariosPorRubro": "{\"albañileria\":{\"activo\":true,\"profesionales\":{\"activo\":true,\"tipo\":\"porcentaje\",\"valor\":100}},\"pintura\":{...}}"
}
```

### ✅ AHORA (Nueva estructura)
```javascript
{
  "honorariosPorRubro": [
    {
      "id": 1,
      "nombreRubro": "albañileria",
      "activo": true,
      "tipo": "porcentaje",
      "valor": null,
      "profesionalesActivo": true,
      "profesionalesTipo": "porcentaje",
      "profesionalesValor": 100.00,
      "materialesActivo": true,
      "materialesTipo": "porcentaje",
      "materialesValor": 100.00,
      "otrosCostosActivo": true,
      "otrosCostosTipo": "porcentaje",
      "otrosCostosValor": null
    },
    {
      "id": 2,
      "nombreRubro": "pintura",
      "activo": true,
      "tipo": "porcentaje",
      "valor": null,
      "profesionalesActivo": true,
      "profesionalesTipo": "porcentaje",
      "profesionalesValor": 50.00,
      "materialesActivo": true,
      "materialesTipo": "porcentaje",
      "materialesValor": 50.00,
      "otrosCostosActivo": true,
      "otrosCostosTipo": "porcentaje",
      "otrosCostosValor": null
    }
  ]
}
```

---

## 🔧 Cambios Requeridos en Frontend

### 1. Estructura del Objeto `HonorarioPorRubroDTO`

```typescript
interface HonorarioPorRubroDTO {
  id?: number;                      // Solo en respuestas, omitir en POST
  nombreRubro: string;              // ej: "albañileria", "pintura", "herreria"
  activo: boolean;                  // Default: true
  tipo: "porcentaje" | "fijo";      // Default: "porcentaje"
  valor?: number | null;            // Valor del honorario general (opcional)
  
  // Profesionales
  profesionalesActivo: boolean;     // Default: true
  profesionalesTipo: "porcentaje" | "fijo";  // Default: "porcentaje"
  profesionalesValor?: number | null;
  
  // Materiales
  materialesActivo: boolean;        // Default: true
  materialesTipo: "porcentaje" | "fijo";  // Default: "porcentaje"
  materialesValor?: number | null;
  
  // Otros Costos
  otrosCostosActivo: boolean;       // Default: true
  otrosCostosTipo: "porcentaje" | "fijo";  // Default: "porcentaje"
  otrosCostosValor?: number | null;
}
```

### 2. Migración de Código Existente

#### ❌ Código Viejo (Eliminar)
```javascript
// ANTES: Parsear JSON string
const honorariosData = JSON.parse(presupuesto.honorariosPorRubro || '{}');
const albanileria = honorariosData.albañileria;
```

#### ✅ Código Nuevo (Implementar)
```javascript
// AHORA: Trabajar con array directamente
const honorariosArray = presupuesto.honorariosPorRubro || [];
const albanileria = honorariosArray.find(h => h.nombreRubro === 'albañileria');
```

### 3. Crear/Actualizar Presupuesto

#### Request Body Example (POST/PUT)
```json
{
  "idEmpresa": 1,
  "nombreObra": "Obra Test",
  "honorariosPorRubro": [
    {
      "nombreRubro": "albañileria",
      "activo": true,
      "tipo": "porcentaje",
      "valor": null,
      "profesionalesActivo": true,
      "profesionalesTipo": "porcentaje",
      "profesionalesValor": 100,
      "materialesActivo": true,
      "materialesTipo": "porcentaje",
      "materialesValor": 100,
      "otrosCostosActivo": true,
      "otrosCostosTipo": "porcentaje",
      "otrosCostosValor": null
    }
  ],
  ...otros campos
}
```

**⚠️ IMPORTANTE:**
- NO enviar `id` al crear nuevos honorarios (POST)
- SÍ enviar `id` al actualizar honorarios existentes (PUT)
- Enviar array vacío `[]` si no hay honorarios configurados
- NO enviar `null` en el campo `honorariosPorRubro`

### 4. Response Example (GET)
```json
{
  "id": 108,
  "numeroPresupuesto": 45,
  "numeroVersion": 1,
  "honorariosPorRubro": [
    {
      "id": 1,
      "nombreRubro": "albañileria",
      "activo": true,
      "tipo": "porcentaje",
      "valor": null,
      "profesionalesActivo": true,
      "profesionalesTipo": "porcentaje",
      "profesionalesValor": 100.00,
      "materialesActivo": true,
      "materialesTipo": "porcentaje",
      "materialesValor": 100.00,
      "otrosCostosActivo": true,
      "otrosCostosTipo": "porcentaje",
      "otrosCostosValor": null
    }
  ],
  ...otros campos
}
```

---

## 📝 Validaciones Frontend

### Campos Obligatorios
- `nombreRubro` (string, 1-100 caracteres)
- `activo` (boolean)
- `tipo` (string: "porcentaje" o "fijo")
- `profesionalesActivo` (boolean)
- `profesionalesTipo` (string: "porcentaje" o "fijo")
- `materialesActivo` (boolean)
- `materialesTipo` (string: "porcentaje" o "fijo")
- `otrosCostosActivo` (boolean)
- `otrosCostosTipo` (string: "porcentaje" o "fijo")

### Campos Opcionales
- `id` (backend lo genera)
- `valor` (puede ser null)
- `profesionalesValor` (puede ser null)
- `materialesValor` (puede ser null)
- `otrosCostosValor` (puede ser null)

### Reglas de Negocio
1. **Unicidad:** No puede haber dos honorarios con el mismo `nombreRubro` en un presupuesto
2. **Valores numéricos:** `valor`, `profesionalesValor`, `materialesValor`, `otrosCostosValor` deben ser >= 0
3. **Tipo porcentaje:** Si tipo = "porcentaje", el valor típicamente está entre 0-100
4. **Tipo fijo:** Si tipo = "fijo", el valor es un monto en pesos

---

## 🔄 Migración de Datos Existentes

### ¿Qué pasa con presupuestos creados antes de la migración?

**Backend ya migró los datos automáticamente:**
- Presupuestos con JSON string en BD fueron convertidos a tabla relacional
- El campo JSON viejo aún existe en BD (pero se eliminará pronto)
- Frontend debe trabajar SOLO con el nuevo formato array

### Testing en Desarrollo

**Presupuesto de prueba disponible:**
```
GET /api/presupuestos-no-cliente/108
```
Este presupuesto ya tiene datos migrados y te sirve como referencia.

---

## 🚨 Breaking Changes Checklist

- [ ] Actualizar interfaces TypeScript/tipos
- [ ] Eliminar todo código que parsea `JSON.parse(honorariosPorRubro)`
- [ ] Eliminar todo código que hace `JSON.stringify()` al enviar
- [ ] Actualizar formularios de creación/edición de presupuestos
- [ ] Actualizar vista de detalle de presupuestos
- [ ] Actualizar lógica de cálculo de honorarios si aplica
- [ ] Probar creación de presupuestos nuevos
- [ ] Probar edición de presupuestos existentes
- [ ] Probar duplicación/versionado de presupuestos

---

## 📚 Ejemplos de Casos de Uso

### Caso 1: Crear presupuesto con honorarios diferenciados
```javascript
const nuevoPresupuesto = {
  idEmpresa: 1,
  nombreObra: "Casa Rodríguez",
  // ... otros campos
  honorariosPorRubro: [
    {
      nombreRubro: "albañileria",
      activo: true,
      tipo: "porcentaje",
      profesionalesActivo: true,
      profesionalesTipo: "porcentaje",
      profesionalesValor: 150,  // 150% sobre costo
      materialesActivo: true,
      materialesTipo: "porcentaje",
      materialesValor: 120,     // 120% sobre costo
      otrosCostosActivo: true,
      otrosCostosTipo: "porcentaje",
      otrosCostosValor: 100
    },
    {
      nombreRubro: "pintura",
      activo: true,
      tipo: "porcentaje",
      profesionalesActivo: true,
      profesionalesTipo: "porcentaje",
      profesionalesValor: 80,
      materialesActivo: true,
      materialesTipo: "porcentaje",
      materialesValor: 60,
      otrosCostosActivo: false,
      otrosCostosTipo: "porcentaje"
    }
  ]
};

await api.post('/api/presupuestos-no-cliente', nuevoPresupuesto);
```

### Caso 2: Actualizar honorarios de presupuesto existente
```javascript
// 1. Obtener presupuesto actual
const presupuesto = await api.get('/api/presupuestos-no-cliente/108');

// 2. Modificar honorarios
const honorariosActualizados = presupuesto.data.honorariosPorRubro.map(h => {
  if (h.nombreRubro === 'albañileria') {
    return { ...h, profesionalesValor: 200 }; // Incrementar a 200%
  }
  return h;
});

// 3. Enviar actualización
await api.put('/api/presupuestos-no-cliente/108', {
  ...presupuesto.data,
  honorariosPorRubro: honorariosActualizados
});
```

### Caso 3: Agregar nuevo rubro dinámicamente
```javascript
const presupuesto = await api.get('/api/presupuestos-no-cliente/108');

const nuevoRubro = {
  nombreRubro: "herreria",
  activo: true,
  tipo: "porcentaje",
  profesionalesActivo: true,
  profesionalesTipo: "porcentaje",
  profesionalesValor: 90,
  materialesActivo: true,
  materialesTipo: "porcentaje",
  materialesValor: 80,
  otrosCostosActivo: true,
  otrosCostosTipo: "porcentaje",
  otrosCostosValor: 50
};

await api.put('/api/presupuestos-no-cliente/108', {
  ...presupuesto.data,
  honorariosPorRubro: [...presupuesto.data.honorariosPorRubro, nuevoRubro]
});
```

### Caso 4: Eliminar un rubro
```javascript
const presupuesto = await api.get('/api/presupuestos-no-cliente/108');

const honorariosFiltrados = presupuesto.data.honorariosPorRubro.filter(
  h => h.nombreRubro !== 'pintura'
);

await api.put('/api/presupuestos-no-cliente/108', {
  ...presupuesto.data,
  honorariosPorRubro: honorariosFiltrados
});
```

---

## 🐛 Troubleshooting

### Error: "Cannot parse JSON string"
**Causa:** Aún estás usando el código viejo  
**Solución:** Eliminar todo `JSON.parse()` relacionado a `honorariosPorRubro`

### Error: "honorariosPorRubro is not iterable"
**Causa:** Backend devolvió `null` o `undefined`  
**Solución:** Usar operador nullish: `honorariosPorRubro || []`

### Error: "Duplicate key value violates unique constraint"
**Causa:** Intentas enviar dos honorarios con el mismo `nombreRubro`  
**Solución:** Validar unicidad de `nombreRubro` antes de enviar

### Backend devuelve 500 al crear presupuesto
**Causa:** Formato incorrecto del array o campos faltantes  
**Solución:** Verificar que el objeto cumpla con las validaciones arriba

---

## ✅ Checklist de Testing

Antes de considerar completa la migración, verificar:

- [ ] GET `/api/presupuestos-no-cliente/{id}` devuelve array de objetos
- [ ] POST presupuesto nuevo con `honorariosPorRubro` funciona
- [ ] PUT presupuesto existente actualizando honorarios funciona
- [ ] Crear presupuesto sin `honorariosPorRubro` (array vacío) funciona
- [ ] Actualizar presupuesto agregando nuevos rubros funciona
- [ ] Actualizar presupuesto eliminando rubros existentes funciona
- [ ] UI muestra correctamente los honorarios por rubro
- [ ] Formularios permiten editar honorarios por rubro
- [ ] Cálculos de totales funcionan correctamente
- [ ] No hay referencias a la estructura JSON vieja en el código

---

## 📞 Soporte

Si encuentras problemas durante la migración:
1. Verificar que el backend esté actualizado (versión post-migración)
2. Revisar logs del navegador para errores de parsing
3. Verificar la estructura del request/response en Network tab
4. Contactar equipo backend si persisten errores 500

---

## 📅 Timeline

- **10/03/2026:** Backend migrado ✅
- **Próximos 7 días:** Período de migración frontend
- **17/03/2026:** Eliminación de columna JSON legacy en BD

**⚠️ URGENTE:** La columna JSON vieja se eliminará en 7 días. Todo el frontend debe estar migrado antes de esa fecha.
