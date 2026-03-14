# 🎯 URLs de Endpoints - Jornales Diarios

**Base URL:** `http://localhost:8080/api/jornales-diarios`

## 📝 CRUD Básico

```
POST   /api/jornales-diarios              → Crear jornal
PUT    /api/jornales-diarios/{id}         → Actualizar jornal
GET    /api/jornales-diarios/{id}         → Obtener jornal por ID
DELETE /api/jornales-diarios/{id}         → Eliminar jornal
```

## 👷 Por Profesional

```
GET /api/jornales-diarios/profesional/{profesionalId}
    → Todos los jornales del profesional (todas las obras)

GET /api/jornales-diarios/profesional/{profesionalId}/obra/{obraId}
    → Jornales del profesional en una obra específica

GET /api/jornales-diarios/profesional/{profesionalId}/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31
    → Jornales del profesional en un rango de fechas
```

## 🏗️ Por Obra

```
GET /api/jornales-diarios/obra/{obraId}
    → Todos los jornales de la obra (todos los profesionales)

GET /api/jornales-diarios/obra/{obraId}/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31
    → Jornales de la obra en un rango de fechas
```

## 📊 Resúmenes (Totales)

```
GET /api/jornales-diarios/resumen/profesional/{profesionalId}/obra/{obraId}
    → Total cobrado por un profesional en una obra
    Retorna: { cantidadJornales, totalHorasDecimal, totalCobrado, ... }

GET /api/jornales-diarios/resumen/obra/{obraId}/profesionales
    → Resumen de todos los profesionales en una obra
    Retorna: [{ profesionalNombre, totalCobrado, ... }, ...]

GET /api/jornales-diarios/resumen/profesional/{profesionalId}/obras
    → Resumen de todas las obras de un profesional
    Retorna: [{ obraNombre, totalCobrado, ... }, ...]
```

---

## 🧪 Ejemplos de URLs Completas

### Crear jornal
```
POST http://localhost:8080/api/jornales-diarios
Body: {
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 1.0
}
```

### Ver jornales de Juan (ID 1) en obra Casa Rodríguez (ID 5)
```
GET http://localhost:8080/api/jornales-diarios/profesional/1/obra/5
```

### Ver cuánto cobró Juan en total en esa obra
```
GET http://localhost:8080/api/jornales-diarios/resumen/profesional/1/obra/5
```

### Ver todos los profesionales que trabajaron en la obra 5
```
GET http://localhost:8080/api/jornales-diarios/resumen/obra/5/profesionales
```

### Ver jornales de marzo 2026 de un profesional
```
GET http://localhost:8080/api/jornales-diarios/profesional/1/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31
```

---

## 🔐 Headers Requeridos

Todos los endpoints requieren:
```javascript
headers: {
  'Content-Type': 'application/json',
  'Authorization': 'Bearer TU_TOKEN_JWT'
}
```

---

## ✅ Tabla en PostgreSQL

La tabla ya está creada y lista:
```sql
SELECT * FROM profesional_jornales_diarios;
```

Columnas principales:
- `id_jornal_diario` (PK)
- `id_profesional` (FK)
- `id_obra` (FK)
- `fecha`
- `horas_trabajadas_decimal`
- `tarifa_diaria`
- `monto_cobrado` (calculado automáticamente)
- `observaciones`
- `empresa_id`

---

## 🚀 ¡Todo listo para usar!

1. Abre tu frontend
2. Copia los ejemplos de [FRONTEND_JORNALES_DIARIOS.md](FRONTEND_JORNALES_DIARIOS.md)
3. Reemplaza los IDs con los reales de tu BD
4. ¡Prueba!
