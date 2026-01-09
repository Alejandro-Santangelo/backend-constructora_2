# 🔄 Endpoint para Reactivar Obras Suspendidas/Canceladas

## Descripción

Este endpoint permite **reactivar** obras que fueron **SUSPENDIDAS** o **CANCELADAS**, vinculándolas con un nuevo presupuesto **APROBADO**.

### Casos de Uso
- ✅ Obra suspendida por meses que se quiere reactivar
- ✅ Obra cancelada que se retoma con nuevo presupuesto
- ✅ Actualización de presupuesto por inflación/cambios de precios
- ✅ Cliente que retoma obra pausada con presupuesto actualizado

---

## 📝 Endpoint

```http
PUT /api/presupuestos-no-cliente/obras/{obraId}/reactivar
```

### Parámetros

| Parámetro | Tipo | Ubicación | Requerido | Descripción |
|-----------|------|-----------|-----------|-------------|
| `obraId` | Long | Path | ✅ Sí | ID de la obra a reactivar |
| `nuevoPresupuestoId` | Long | Query | ✅ Sí | ID del nuevo presupuesto APROBADO |
| `empresaId` | Long | Query | ✅ Sí | ID de la empresa (multi-tenant) |

---

## 🔄 Proceso Interno

1. **Validación de Obra**
   - ✅ Verifica que la obra exista
   - ✅ Verifica que pertenezca a la empresa
   - ✅ Valida que esté en estado **SUSPENDIDA** o **CANCELADO**

2. **Validación de Presupuesto**
   - ✅ Verifica que el presupuesto exista
   - ✅ Verifica que pertenezca a la empresa
   - ✅ Valida que esté en estado **APROBADO**
   - ✅ Verifica que NO esté vinculado a otra obra

3. **Desvinculación**
   - 🔗 Desvincular presupuesto anterior (si existe)
   - 🔗 Limpia `obra.presupuestoNoClienteId`
   - 🔗 Limpia `presupuestoAnterior.obra_id`

4. **Vinculación**
   - 🔗 Vincula nuevo presupuesto a la obra
   - 🔗 Actualiza `obra.presupuestoNoClienteId = nuevoPresupuestoId`
   - 🔗 Actualiza `nuevoPresupuesto.obra_id = obraId`

5. **Sincronización de Estado**
   - 🔄 Sincroniza estado: obra ← presupuesto
   - 🔄 Si presupuesto APROBADO:
     - Sin fecha inicio → Obra pasa a **APROBADO**
     - Con fecha inicio futura → Obra pasa a **APROBADO**
     - Con fecha inicio pasada → Obra pasa a **EN_EJECUCION**

6. **Actualización Presupuesto**
   - 💰 Actualiza `obra.presupuestoEstimado` con el total del nuevo presupuesto

---

## 📊 Ejemplo de Uso

### Request

```http
PUT /api/presupuestos-no-cliente/obras/15/reactivar?nuevoPresupuestoId=42&empresaId=1
```

### Response (200 OK)

```json
{
  "mensaje": "Obra 15 reactivada exitosamente con Presupuesto 42",
  "obraId": 15,
  "estadoAnterior": "Suspendida",
  "estadoNuevo": "Aprobado",
  "presupuestoAnteriorId": 38,
  "nuevoPresupuestoId": 42,
  "nuevoPresupuestoTotal": 2850000.00
}
```

---

## ❌ Códigos de Error

### 400 Bad Request
**Presupuesto no está APROBADO**
```json
{
  "mensaje": "El presupuesto debe estar APROBADO. Estado actual: ENVIADO"
}
```

### 403 Forbidden
**Obra no pertenece a la empresa**
```json
{
  "mensaje": "La obra no pertenece a la empresa especificada"
}
```

### 404 Not Found
**Obra no encontrada**
```json
{
  "mensaje": "Obra con ID 15 no encontrada"
}
```

**Presupuesto no encontrado**
```json
{
  "mensaje": "Presupuesto con ID 42 no encontrado"
}
```

### 409 Conflict
**Obra no puede ser reactivada**
```json
{
  "mensaje": "Solo se pueden reactivar obras SUSPENDIDAS o CANCELADAS. Estado actual: En Ejecución"
}
```

**Presupuesto ya vinculado**
```json
{
  "mensaje": "El presupuesto ya está vinculado a la Obra ID: 12"
}
```

---

## 🎯 Estados Válidos

### Estados de Obra que PERMITEN Reactivación
- ✅ **SUSPENDIDA**
- ✅ **CANCELADO**

### Estados de Obra que NO PERMITEN Reactivación
- ❌ BORRADOR
- ❌ A_ENVIAR
- ❌ ENVIADO
- ❌ MODIFICADO
- ❌ APROBADO
- ❌ OBRA_A_CONFIRMAR
- ❌ EN_EJECUCION
- ❌ TERMINADO

### Estado Requerido del Presupuesto
- ✅ **APROBADO** (único estado válido)

---

## 🔍 Ejemplo de Flujo Completo

### Escenario: Reactivar Obra Suspendida

1. **Situación Inicial**
   - Obra ID: 15
   - Estado: **SUSPENDIDA**
   - Presupuesto actual: 38 ($2,500,000)
   - Razón: Cliente pausó obra hace 3 meses

2. **Nuevo Presupuesto**
   - Presupuesto ID: 42
   - Estado: **APROBADO**
   - Total: $2,850,000 (actualizado por inflación)

3. **Llamada al Endpoint**
   ```bash
   curl -X PUT "http://localhost:8080/api/presupuestos-no-cliente/obras/15/reactivar?nuevoPresupuestoId=42&empresaId=1" \
     -H "Content-Type: application/json"
   ```

4. **Resultado**
   - ✅ Presupuesto 38 desvinculado
   - ✅ Presupuesto 42 vinculado
   - ✅ Obra pasa a estado **APROBADO** (o **EN_EJECUCION** si tiene fecha inicio)
   - ✅ `obra.presupuestoEstimado` = $2,850,000

---

## 📌 Notas Importantes

### Multi-Tenant
- ⚠️ El `empresaId` es **obligatorio**
- ⚠️ Valida que obra y presupuesto pertenezcan a la misma empresa
- ⚠️ No puede vincular presupuestos de otras empresas

### Presupuesto Anterior
- Si la obra **no tenía presupuesto vinculado**, `presupuestoAnteriorId` será `null`
- Si tenía presupuesto, se devuelve su ID en la respuesta

### Sincronización de Estados
- El estado final de la obra depende del **nuevo presupuesto**
- La sincronización es **automática**
- Si el presupuesto tiene `fechaInicio` en el pasado → **EN_EJECUCION**
- Si no tiene `fechaInicio` o es futura → **APROBADO**

### Presupuesto Estimado
- Se actualiza automáticamente con el total del **nuevo presupuesto**
- Afecta los cálculos de `pendienteCobro` en la obra

---

## 🧪 Testing

### Postman / curl

```bash
# Ejemplo con curl
curl -X PUT \
  "http://localhost:8080/api/presupuestos-no-cliente/obras/15/reactivar?nuevoPresupuestoId=42&empresaId=1" \
  -H "Content-Type: application/json"

# Ejemplo con query params explícitos
curl -X PUT \
  "http://localhost:8080/api/presupuestos-no-cliente/obras/15/reactivar?nuevoPresupuestoId=42&empresaId=1"
```

### JavaScript (Frontend)

```javascript
async function reactivarObra(obraId, nuevoPresupuestoId, empresaId) {
  try {
    const response = await fetch(
      `/api/presupuestos-no-cliente/obras/${obraId}/reactivar?nuevoPresupuestoId=${nuevoPresupuestoId}&empresaId=${empresaId}`,
      {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );

    if (!response.ok) {
      const error = await response.json();
      throw new Error(error.mensaje);
    }

    const data = await response.json();
    
    console.log(`✅ Obra ${data.obraId} reactivada`);
    console.log(`📊 Estado: ${data.estadoAnterior} → ${data.estadoNuevo}`);
    console.log(`💰 Nuevo presupuesto: $${data.nuevoPresupuestoTotal}`);
    
    return data;
  } catch (error) {
    console.error('❌ Error al reactivar obra:', error.message);
    throw error;
  }
}

// Uso
reactivarObra(15, 42, 1)
  .then(response => {
    // Actualizar UI
    mostrarMensajeExito(response.mensaje);
    recargarObra(response.obraId);
  })
  .catch(error => {
    mostrarMensajeError(error.message);
  });
```

---

## 📚 Documentación Swagger

Accede a la documentación interactiva en:

```
http://localhost:8080/api/swagger-ui.html
```

Busca:
- **Tag**: "Presupuesto No Cliente"
- **Endpoint**: `PUT /api/presupuestos-no-cliente/obras/{obraId}/reactivar`

---

## 🔐 Seguridad

- ✅ Validación multi-tenant (empresaId)
- ✅ Validación de permisos por empresa
- ✅ Transaccional (@Transactional)
- ✅ Rollback automático en caso de error
- ✅ Logs detallados de operación

---

## 📝 Logs Generados

```
🔄 Iniciando reactivación de Obra 15 con Presupuesto 42
🔗 Presupuesto 38 desvinculado de Obra 15
✅ Obra 15 reactivada: Suspendida → Aprobado. Presupuesto anterior: 38, nuevo: 42
```

---

## ✅ Checklist de Implementación

- [x] Endpoint creado en PresupuestoNoClienteController
- [x] Lógica de negocio en PresupuestoNoClienteService
- [x] Validaciones de estado (obra y presupuesto)
- [x] Validación multi-tenant
- [x] Desvinculación de presupuesto anterior
- [x] Vinculación de nuevo presupuesto
- [x] Sincronización automática de estados
- [x] Actualización de presupuesto estimado
- [x] Manejo de errores (400, 403, 404, 409, 500)
- [x] DTO de respuesta (ReactivarObraResponse)
- [x] Logs informativos
- [x] Documentación Swagger
- [x] Transaccionalidad
- [x] Tests compilados

---

## 🎉 ¡Listo para usar!

El endpoint ya está **desplegado** y **funcionando** en:
```
http://localhost:8080/api/presupuestos-no-cliente/obras/{obraId}/reactivar
```

**Versión del Backend**: construccion-backend 1.0.0  
**Spring Boot**: 3.2.0  
**Java**: 17  
**Base de Datos**: PostgreSQL 17.4 (construccion_app_v2)
