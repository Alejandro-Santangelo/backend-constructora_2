# IMPLEMENTACIÓN DE ASIGNACIONES SEMANALES DE OTROS COSTOS

## 📋 RESUMEN DE CAMBIOS

Se ha implementado exitosamente el soporte para asignaciones semanales de "Otros Costos/Gastos Generales" en el backend.

### ✅ Cambios Realizados

1. **Migración de Base de Datos** (`V31__agregar_campos_asignaciones_semanales_otros_costos.sql`)
   - ✅ Columna `fecha_asignacion` ahora es **nullable**
   - ✅ Nueva columna `es_semanal` (BOOLEAN)
   - ✅ Nueva columna `es_manual` (BOOLEAN)
   - ✅ Nueva columna `presupuesto_otro_costo_id` (BIGINT)
   - ✅ Nueva columna `descripcion` (VARCHAR 500)
   - ✅ Nueva columna `categoria` (VARCHAR 100)
   - ✅ Migración automática de datos existentes
   - ✅ Índices creados para optimización

2. **Entidad ObraOtroCosto**
   - ✅ `fechaAsignacion` cambió de `LocalDateTime` a `LocalDate` (nullable)
   - ✅ Agregado campo `esSemanal` (Boolean)
   - ✅ Agregado campo `esManual` (Boolean)
   - ✅ Agregado campo `presupuestoOtroCostoId` (Long)
   - ✅ Agregado campo `descripcion` (String)
   - ✅ Agregado campo `categoria` (String)

3. **DTOs Actualizados**
   - ✅ `AsignarOtroCostoRequestDTO`: campos opcionales agregados
   - ✅ `ObraOtroCostoResponseDTO`: campos `esSemanal`, `esManual`, `nombreOtroCosto`

4. **Servicio (ObraOtroCostoServiceImpl)**
   - ✅ Lógica para detectar asignaciones semanales (sin `fechaAsignacion`)
   - ✅ Validaciones implementadas
   - ✅ Soporte para gastos manuales
   - ✅ Conversión correcta en el DTO de respuesta

5. **Repositorio (IObraOtroCostoRepository)**
   - ✅ Ordenamiento corregido: `ORDER BY semana ASC, fechaAsignacion ASC NULLS FIRST`

---

## 🔧 ESTRUCTURA DE LA API

### POST `/api/obras/{obraId}/otros-costos`

**Headers:**
```json
{
  "empresaId": 123,
  "Content-Type": "application/json"
}
```

#### Caso 1: Asignación SEMANAL (para toda la semana)

```json
{
  "obraId": 456,
  "presupuestoOtroCostoId": null,
  "gastoGeneralId": null,
  "importeAsignado": 500000,
  "semana": 1,
  "descripcion": "Volquetes",
  "categoria": "Albañilería",
  "observaciones": "Volquetes [Gasto Semanal Global]"
}
```

**Características:**
- ❌ Sin `fechaAsignacion`
- ✅ `esSemanal` = true (automático)
- ✅ `esManual` = true (si presupuestoOtroCostoId es null)

#### Caso 2: Asignación DIARIA (día específico)

```json
{
  "obraId": 456,
  "presupuestoOtroCostoId": 10,
  "gastoGeneralId": 5,
  "importeAsignado": 100000,
  "semana": 1,
  "fechaAsignacion": "2026-01-12",
  "descripcion": "Cemento",
  "categoria": "Materiales",
  "observaciones": "Compra del lunes"
}
```

**Características:**
- ✅ Con `fechaAsignacion`
- ✅ `esSemanal` = false (automático)
- ✅ `esManual` = false (si presupuestoOtroCostoId existe)

---

### GET `/api/obras/{obraId}/otros-costos`

**Headers:**
```json
{
  "empresaId": 123
}
```

**Respuesta esperada:**

```json
[
  {
    "id": 789,
    "obraId": 456,
    "nombreObra": "Obra ID: 456",
    "presupuestoOtroCostoId": null,
    "gastoGeneralId": null,
    "categoria": "Albañilería",
    "descripcion": "Volquetes",
    "nombreOtroCosto": "Volquetes",
    "importeAsignado": 500000,
    "semana": 1,
    "observaciones": "Volquetes [Gasto Semanal Global]",
    "esSemanal": true,
    "esManual": true
    // ❌ Sin fechaAsignacion (null o no presente)
  },
  {
    "id": 790,
    "obraId": 456,
    "nombreObra": "Obra ID: 456",
    "presupuestoOtroCostoId": 10,
    "gastoGeneralId": 5,
    "categoria": "Materiales",
    "descripcion": "Cemento",
    "nombreOtroCosto": "Cemento",
    "importeAsignado": 100000,
    "fechaAsignacion": "2026-01-12",
    "semana": 1,
    "observaciones": "Compra del lunes",
    "esSemanal": false,
    "esManual": false
  }
]
```

**Orden de resultados:**
1. Por `semana` ASC
2. Dentro de cada semana: asignaciones semanales primero (fecha null)
3. Luego asignaciones diarias ordenadas por fecha ASC

---

### PUT `/api/obras/{obraId}/otros-costos/{asignacionId}`

Actualiza una asignación existente. Mismo payload que POST.

### DELETE `/api/obras/{obraId}/otros-costos/{asignacionId}`

Elimina una asignación.

---

## ⚠️ VALIDACIONES IMPLEMENTADAS

1. **Asignación Semanal:**
   - ❌ NO debe tener `fechaAsignacion`
   - ✅ DEBE tener `semana`
   - ✅ Si es manual: REQUIERE `descripcion` y `categoria`

2. **Asignación Diaria:**
   - ✅ DEBE tener `fechaAsignacion`
   - ✅ DEBE tener `semana`

3. **Gastos Manuales:**
   - Si `presupuestoOtroCostoId` es null:
     - ✅ REQUIERE `descripcion`
     - ✅ REQUIERE `categoria`

---

## 🚀 CÓMO PROBAR

### 1. Ejecutar migración de BD

La migración se aplicará automáticamente al iniciar Spring Boot (Flyway).

```bash
./mvnw spring-boot:run
```

### 2. Probar con Postman/Insomnia

#### Test 1: Crear asignación semanal manual

```http
POST http://localhost:8080/api/obras/1/otros-costos
Content-Type: application/json
empresaId: 1

{
  "obraId": 1,
  "importeAsignado": 500000,
  "semana": 1,
  "descripcion": "Volquetes - Toda la semana",
  "categoria": "Albañilería",
  "observaciones": "Volquetes [Gasto Semanal Global]"
}
```

**Resultado esperado:** `esSemanal: true`, `esManual: true`, sin `fechaAsignacion`

#### Test 2: Crear asignación diaria

```http
POST http://localhost:8080/api/obras/1/otros-costos
Content-Type: application/json
empresaId: 1

{
  "obraId": 1,
  "presupuestoOtroCostoId": 10,
  "gastoGeneralId": 5,
  "importeAsignado": 100000,
  "semana": 1,
  "fechaAsignacion": "2026-01-13",
  "descripcion": "Cemento",
  "categoria": "Materiales"
}
```

**Resultado esperado:** `esSemanal: false`, `esManual: false`, con `fechaAsignacion`

#### Test 3: Listar todas las asignaciones

```http
GET http://localhost:8080/api/obras/1/otros-costos
empresaId: 1
```

**Verificar:**
- ✅ Orden correcto (semana -> semanal primero -> diarias)
- ✅ Campo `esSemanal` presente en cada objeto
- ✅ Asignaciones semanales SIN `fechaAsignacion`
- ✅ Campo `nombreOtroCosto` presente (alias de descripcion)

---

## 📊 COMPATIBILIDAD CON FRONTEND

El backend ahora retorna exactamente la estructura esperada por el frontend:

```typescript
interface OtroCostoAsignado {
  id: number;
  descripcion: string;
  nombreOtroCosto: string;  // ✅ Agregado
  importeAsignado: number;
  categoria: string;
  semana: number;
  observaciones?: string;
  
  // CAMPOS CRÍTICOS PARA DIFERENCIAR:
  esSemanal: boolean;       // ✅ Agregado
  esManual: boolean;        // ✅ Agregado
  fechaAsignacion?: string; // ✅ Solo presente si esSemanal = false
}
```

---

## 🔍 NOTAS IMPORTANTES

1. **Serialización JSON:** Se agregó `@JsonInclude(JsonInclude.Include.NON_NULL)` al DTO de respuesta para que `fechaAsignacion` no aparezca en el JSON si es null.

2. **Ordenamiento:** Las asignaciones semanales aparecen primero dentro de cada semana (NULLS FIRST).

3. **Migración de datos existentes:** Los registros antiguos se marcaron automáticamente como `esSemanal=false` si tienen fecha, o `esSemanal=true` si no la tienen.

4. **Campos opcionales:** `presupuestoOtroCostoId` y `gastoGeneralId` ahora son opcionales (nullable) para soportar gastos manuales.

---

## ✅ CHECKLIST DE INTEGRACIÓN

- [x] Migración SQL creada y probada
- [x] Entidad actualizada
- [x] DTOs actualizados
- [x] Servicio con lógica de validación
- [x] Repositorio con ordenamiento correcto
- [x] Compilación exitosa
- [ ] Pruebas con Postman
- [ ] Integración con frontend
- [ ] Verificación de casos edge

---

## 🐛 TROUBLESHOOTING

### Error: "fecha_asignacion cannot be null"
**Solución:** Verifica que la migración V31 se haya ejecutado correctamente. Revisa `flyway_schema_history`.

### Frontend no muestra "esSemanal"
**Solución:** Verifica que el backend esté retornando el campo en el JSON. Usa DevTools > Network.

### Asignaciones semanales aparecen con fecha
**Solución:** Verifica que `@JsonInclude(JsonInclude.Include.NON_NULL)` esté en el DTO de respuesta.

---

**Última actualización:** 10 de enero de 2026
**Backend versión:** 1.0.0
**Compilación:** ✅ Exitosa
