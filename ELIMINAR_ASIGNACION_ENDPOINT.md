# Endpoint para Eliminar Asignaciones de Cobros Empresa

## Descripción General
Este endpoint permite eliminar asignaciones individuales de cobros empresa, liberando el monto asignado y actualizando automáticamente el estado del cobro.

---

## Endpoint

### DELETE `/api/v1/cobros-empresa/{cobroEmpresaId}/asignaciones/{asignacionId}`

Elimina una asignación específica de un cobro empresa y actualiza los montos disponibles.

#### Parámetros de Ruta
- `cobroEmpresaId` (Long): ID del cobro empresa
- `asignacionId` (Long): ID de la asignación a eliminar

#### Parámetros de Query
- `empresaId` (Long, requerido): ID de la empresa (multi-tenant)

#### Respuesta Exitosa (200 OK)
```json
{
  "mensaje": "Asignación eliminada exitosamente",
  "montoLiberado": 15000.00,
  "cobroActualizado": {
    "id": 1,
    "montoTotal": 50000.00,
    "montoAsignado": 20000.00,
    "montoDisponible": 30000.00,
    "estado": "ASIGNADO_PARCIAL"
  }
}
```

#### Errores Posibles
- **400 Bad Request**: Parámetros inválidos
  ```json
  {
    "error": "Bad Request",
    "mensaje": "La asignación 123 no pertenece al cobro empresa 456"
  }
  ```

- **404 Not Found**: Cobro o asignación no encontrada
  ```json
  {
    "error": "Not Found",
    "mensaje": "Asignación con ID 123 no encontrada"
  }
  ```

---

## Lógica de Negocio

### Flujo de Eliminación

1. **Validación de Existencia**
   - Busca el cobro empresa por ID y empresaId
   - Busca la asignación por ID
   - Valida que la asignación pertenezca al cobro especificado

2. **Eliminación en Cascada**
   - Elimina las distribuciones asociadas (asignaciones a profesionales/materiales) si existen
   - Elimina el cobro obra asociado
   - Elimina la asignación empresa-obra

3. **Actualización de Montos**
   - Reduce `montoAsignado` del cobro empresa
   - Incrementa `montoDisponible` del cobro empresa
   - Recalcula el estado del cobro

4. **Actualización de Estado**
   - `DISPONIBLE`: Si montoAsignado == 0
   - `ASIGNADO_PARCIAL`: Si 0 < montoAsignado < montoTotal
   - `ASIGNADO_COMPLETO`: Si montoAsignado == montoTotal

---

## Ejemplos de Uso

### Ejemplo 1: Eliminar Asignación Parcial
**Request:**
```
DELETE /api/v1/cobros-empresa/1/asignaciones/5?empresaId=1
```

**Response:**
```json
{
  "mensaje": "Asignación eliminada exitosamente",
  "montoLiberado": 8500.00,
  "cobroActualizado": {
    "id": 1,
    "montoTotal": 25000.00,
    "montoAsignado": 10500.00,
    "montoDisponible": 14500.00,
    "estado": "ASIGNADO_PARCIAL"
  }
}
```

### Ejemplo 2: Liberar Cobro Completamente Asignado
**Request:**
```
DELETE /api/v1/cobros-empresa/2/asignaciones/8?empresaId=1
```

**Response:**
```json
{
  "mensaje": "Asignación eliminada exitosamente",
  "montoLiberado": 100000.00,
  "cobroActualizado": {
    "id": 2,
    "montoTotal": 100000.00,
    "montoAsignado": 0.00,
    "montoDisponible": 100000.00,
    "estado": "DISPONIBLE"
  }
}
```

---

## Casos de Uso

### Workflow: Liberar Fondos para Reasignación

**Problema:** Un cobro empresa está completamente asignado pero necesitamos redistribuirlo

**Solución:**
1. Listar asignaciones del cobro empresa
2. Eliminar las asignaciones que no se necesitan usando este endpoint
3. El monto se libera automáticamente en `montoDisponible`
4. Crear nuevas asignaciones con el monto liberado

### Workflow: Eliminar Cobro con Asignaciones

**Problema:** Intentar eliminar un cobro empresa con asignaciones arroja error 400

**Solución:**
1. Obtener todas las asignaciones del cobro
2. Eliminar cada asignación usando este endpoint
3. Cuando `montoAsignado` == 0, usar `DELETE /api/v1/cobros-empresa/{id}` para eliminar el cobro

---

## Validaciones Implementadas

### Multi-Tenant
- Valida que el cobro pertenezca a la `empresaId` especificada
- Filtro de Hibernate activo para todos los queries

### Integridad de Datos
- Verifica que la asignación pertenezca al cobro especificado
- Elimina en cascada todas las distribuciones asociadas
- Actualiza montos atómicamente en transacción

### Estados
- Recalcula automáticamente el estado según los nuevos montos:
  - `DISPONIBLE`: Sin asignaciones
  - `ASIGNADO_PARCIAL`: Parcialmente asignado
  - `ASIGNADO_COMPLETO`: Totalmente asignado
  - `ANULADO`: Estado inmutable (no afectado por este endpoint)

---

## Consideraciones Técnicas

### Transaccionalidad
- Método `@Transactional` garantiza atomicidad
- Si falla la eliminación, rollback completo
- Montos siempre consistentes

### Eliminación en Cascada
```
AsignacionCobroEmpresaObra
  └─> CobroObra
       └─> List<AsignacionCobroObra> (distribuciones)
```

**Orden de eliminación:**
1. Distribuciones (asignaciones a profesionales/materiales)
2. Cobro obra
3. Asignación empresa-obra
4. Actualización del cobro empresa

### Logs
```
INFO: Eliminando asignación 5 del cobro empresa 1
INFO: Eliminando 3 distribuciones asociadas
INFO: Eliminando cobro obra 7
INFO: Asignación 5 eliminada. Monto liberado: 8500.00. Nuevo disponible: 14500.00
```

---

## Comparación con Anular Cobro

| Característica | Eliminar Asignación | Anular Cobro |
|---------------|---------------------|--------------|
| Libera monto | ✅ Sí, parcialmente | ❌ No |
| Permite reasignar | ✅ Sí | ❌ No |
| Modifica estado | ✅ Sí | ✅ Sí (ANULADO) |
| Reversible | ❌ No | ❌ No |
| Requiere motivo | ❌ No | ✅ Sí |

**Recomendación:**
- **Eliminar asignación**: Para corregir errores o redistribuir fondos
- **Anular cobro**: Para invalidar cobros por motivos administrativos/contables

---

## Ejemplos con cURL

### Eliminar Asignación
```bash
curl -X DELETE \
  'http://localhost:8080/api/v1/cobros-empresa/1/asignaciones/5?empresaId=1' \
  -H 'Content-Type: application/json'
```

### Verificar Estado del Cobro
```bash
curl -X GET \
  'http://localhost:8080/api/v1/cobros-empresa/1?empresaId=1' \
  -H 'Content-Type: application/json'
```

---

## Código Relacionado

### Service
- `CobroEmpresaService.eliminarAsignacionCobroEmpresa()`
- Ubicación: [CobroEmpresaService.java](src/main/java/com/rodrigo/construccion/service/CobroEmpresaService.java)

### Controller
- `CobroEmpresaController.eliminarAsignacionCobroEmpresa()`
- Ubicación: [CobroEmpresaController.java](src/main/java/com/rodrigo/construccion/controller/CobroEmpresaController.java)

### DTO
- `EliminarAsignacionResponseDTO`
- Ubicación: [EliminarAsignacionResponseDTO.java](src/main/java/com/rodrigo/construccion/dto/cobros/EliminarAsignacionResponseDTO.java)

### Repositorios
- `AsignacionCobroEmpresaObraRepository`
- `CobroObraRepository`
- `AsignacionCobroObraRepository`

---

## Testing

### Test Manual con Postman
1. Crear cobro empresa con monto total $50,000
2. Asignar $20,000 a obra A (asignación ID: 10)
3. Asignar $15,000 a obra B (asignación ID: 11)
4. Verificar estado: `ASIGNADO_PARCIAL`
5. Eliminar asignación 10: `DELETE /cobros-empresa/1/asignaciones/10?empresaId=1`
6. Verificar:
   - Monto liberado: $20,000
   - Monto asignado: $15,000
   - Monto disponible: $35,000
   - Estado: `ASIGNADO_PARCIAL`

### Escenarios de Prueba
- ✅ Eliminar asignación con distribuciones
- ✅ Eliminar última asignación (estado → DISPONIBLE)
- ✅ Eliminar asignación de cobro parcialmente asignado
- ✅ Validación multi-tenant
- ✅ Error al especificar asignación de otro cobro
- ✅ Error con IDs inexistentes

---

## Notas de Implementación

### Fecha de Implementación
- 4 de enero de 2026

### Versión
- 1.0.0

### Estado
- ✅ Implementado
- ✅ Compilado
- ⏳ Pendiente de testing en producción

### Próximas Mejoras
- [ ] Agregar auditoría de eliminaciones
- [ ] Endpoint para listar asignaciones por cobro
- [ ] Soft delete opcional
- [ ] Webhook/notificación al eliminar asignaciones

---

## Soporte

Para dudas o problemas, contactar al equipo de desarrollo backend.
