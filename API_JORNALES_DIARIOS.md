# API de Jornales Diarios de Profesionales

## Descripción General

API REST para registrar y consultar el tiempo trabajado por profesionales en obras por día. 

**Características principales:**
- Un profesional puede trabajar en múltiples obras el mismo día
- Las horas se registran como decimal: `1.0` = día completo, `0.5` = medio día, `0.25` = cuarto día
- El monto cobrado se calcula automáticamente: `tarifaDiaria * horasTrabajadasDecimal`
- La tarifa diaria se copia del maestro del profesional (campo `honorarioDia`)
- Permite sobreescribir la tarifa para casos especiales

---

## Endpoints Disponibles

### 1. Crear Jornal Diario

**POST** `/api/jornales-diarios`

Registra un nuevo jornal diario para un profesional en una obra.

**Request Body:**
```json
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 1.0,
  "tarifaDiaria": 100000.00,  // OPCIONAL - si no se envía, se toma del profesional
  "observaciones": "Trabajo completo en cimientos",  // OPCIONAL
  "empresaId": 1  // OPCIONAL - se toma del contexto si no se envía
}
```

**Validaciones:**
- `profesionalId`: requerido, debe existir
- `obraId`: requerido, debe existir
- `fecha`: requerido, formato `yyyy-MM-dd`
- `horasTrabajadasDecimal`: requerido, entre 0.01 y 1.5, máximo 2 decimales
- `tarifaDiaria`: opcional, si se especifica debe ser >= 0
- No puede existir otro jornal con el mismo profesional + obra + fecha

**Response (201 Created):**
```json
{
  "id": 123,
  "profesionalId": 1,
  "profesionalNombre": "Juan Pérez",
  "obraId": 5,
  "obraNombre": "Casa Rodríguez",
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 1.0,
  "tarifaDiaria": 100000.00,
  "montoCobrado": 100000.00,  // Calculado automáticamente
  "observaciones": "Trabajo completo en cimientos",
  "empresaId": 1,
  "fechaCreacion": "2026-03-14 10:30:00",
  "fechaActualizacion": null
}
```

**Ejemplos de uso:**

**Día completo:**
```json
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 1.0
}
// Si profesional.honorarioDia = 100000
// => montoCobrado = 100000
```

**Medio día:**
```json
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 0.5
}
// Si profesional.honorarioDia = 100000
// => montoCobrado = 50000
```

**Cuarto de día:**
```json
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 0.25
}
// Si profesional.honorarioDia = 100000
// => montoCobrado = 25000
```

---

### 2. Actualizar Jornal Diario

**PUT** `/api/jornales-diarios/{id}`

Actualiza un jornal existente.

**Request Body:** (igual que POST)
```json
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 0.5,
  "observaciones": "Trabajó solo medio día"
}
```

**Response (200 OK):** (igual estructura que POST)

---

### 3. Obtener Jornal por ID

**GET** `/api/jornales-diarios/{id}`

**Response (200 OK):**
```json
{
  "id": 123,
  "profesionalId": 1,
  "profesionalNombre": "Juan Pérez",
  "obraId": 5,
  "obraNombre": "Casa Rodríguez",
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 1.0,
  "tarifaDiaria": 100000.00,
  "montoCobrado": 100000.00,
  "observaciones": "Trabajo completo",
  "empresaId": 1,
  "fechaCreacion": "2026-03-14 10:30:00",
  "fechaActualizacion": null
}
```

---

### 4. Eliminar Jornal

**DELETE** `/api/jornales-diarios/{id}`

**Response (204 No Content):** Sin body

---

### 5. Obtener Jornales de un Profesional en una Obra

**GET** `/api/jornales-diarios/profesional/{profesionalId}/obra/{obraId}`

Retorna todos los jornales de un profesional específico en una obra específica, ordenados por fecha descendente.

**Response (200 OK):**
```json
[
  {
    "id": 123,
    "profesionalId": 1,
    "profesionalNombre": "Juan Pérez",
    "obraId": 5,
    "obraNombre": "Casa Rodríguez",
    "fecha": "2026-03-14",
    "horasTrabajadasDecimal": 1.0,
    "tarifaDiaria": 100000.00,
    "montoCobrado": 100000.00,
    "observaciones": null,
    "empresaId": 1,
    "fechaCreacion": "2026-03-14 10:30:00",
    "fechaActualizacion": null
  },
  {
    "id": 122,
    "profesionalId": 1,
    "profesionalNombre": "Juan Pérez",
    "obraId": 5,
    "obraNombre": "Casa Rodríguez",
    "fecha": "2026-03-13",
    "horasTrabajadasDecimal": 0.5,
    "tarifaDiaria": 100000.00,
    "montoCobrado": 50000.00,
    "observaciones": "Medio día",
    "empresaId": 1,
    "fechaCreacion": "2026-03-13 09:15:00",
    "fechaActualizacion": null
  }
]
```

---

### 6. Obtener Todos los Jornales de un Profesional

**GET** `/api/jornales-diarios/profesional/{profesionalId}`

Retorna todos los jornales de un profesional en todas sus obras.

**Response (200 OK):** Array de jornales (mismo formato anterior)

---

### 7. Obtener Todos los Jornales de una Obra

**GET** `/api/jornales-diarios/obra/{obraId}`

Retorna todos los jornales de una obra (todos los profesionales que trabajaron).

**Response (200 OK):** Array de jornales

---

### 8. Obtener Jornales por Profesional y Rango de Fechas

**GET** `/api/jornales-diarios/profesional/{profesionalId}/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31`

**Query Parameters:**
- `fechaDesde`: fecha inicio (formato: `yyyy-MM-dd`)
- `fechaHasta`: fecha fin (formato: `yyyy-MM-dd`)

**Response (200 OK):** Array de jornales

---

### 9. Obtener Jornales por Obra y Rango de Fechas

**GET** `/api/jornales-diarios/obra/{obraId}/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31`

**Query Parameters:**
- `fechaDesde`: fecha inicio (formato: `yyyy-MM-dd`)
- `fechaHasta`: fecha fin (formato: `yyyy-MM-dd`)

**Response (200 OK):** Array de jornales

---

### 10. Resumen: Profesional en Obra

**GET** `/api/jornales-diarios/resumen/profesional/{profesionalId}/obra/{obraId}`

Retorna totales agregados de un profesional en una obra específica.

**Response (200 OK):**
```json
{
  "profesionalId": 1,
  "profesionalNombre": "Juan Pérez",
  "obraId": 5,
  "obraNombre": "Casa Rodríguez",
  "cantidadJornales": 10,
  "totalHorasDecimal": 8.5,   // 8 días completos + 0.5 medio día
  "totalCobrado": 850000.00,
  "promedioHorasPorJornal": 0.85,
  "promedioMontoPorJornal": 85000.00
}
```

**Campos calculados:**
- `totalHorasDecimal`: suma de todas las horas trabajadas
- `totalCobrado`: suma de todos los montos cobrados
- `promedioHorasPorJornal`: totalHorasDecimal / cantidadJornales
- `promedioMontoPorJornal`: totalCobrado / cantidadJornales

---

### 11. Resumen: Todos los Profesionales de una Obra

**GET** `/api/jornales-diarios/resumen/obra/{obraId}/profesionales`

Retorna totales por cada profesional que trabajó en la obra.

**Response (200 OK):**
```json
[
  {
    "profesionalId": 1,
    "profesionalNombre": "Juan Pérez",
    "obraId": 5,
    "obraNombre": "Casa Rodríguez",
    "cantidadJornales": 10,
    "totalHorasDecimal": 8.5,
    "totalCobrado": 850000.00,
    "promedioHorasPorJornal": 0.85,
    "promedioMontoPorJornal": 85000.00
  },
  {
    "profesionalId": 2,
    "profesionalNombre": "María García",
    "obraId": 5,
    "obraNombre": "Casa Rodríguez",
    "cantidadJornales": 5,
    "totalHorasDecimal": 4.0,
    "totalCobrado": 400000.00,
    "promedioHorasPorJornal": 0.8,
    "promedioMontoPorJornal": 80000.00
  }
]
```

---

### 12. Resumen: Todas las Obras de un Profesional

**GET** `/api/jornales-diarios/resumen/profesional/{profesionalId}/obras`

Retorna totales por cada obra en la que trabajó el profesional.

**Response (200 OK):** Array similar al anterior, agrupado por obra

---

## Códigos de Error

### 400 Bad Request
```json
{
  "timestamp": "2026-03-14T10:30:00",
  "status": 400,
  "error": "Bad Request",
  "message": "Ya existe un jornal registrado para este profesional en esta obra en la fecha 2026-03-14"
}
```

**Causas comunes:**
- Jornal duplicado (mismos profesional + obra + fecha)
- Validación fallida (horas fuera de rango, tarifa negativa, etc.)
- Profesional sin honorario diario configurado

### 404 Not Found
```json
{
  "timestamp": "2026-03-14T10:30:00",
  "status": 404,
  "error": "Not Found",
  "message": "Profesional no encontrado con id: 999"
}
```

**Causas:**
- ID de profesional no existe
- ID de obra no existe
- ID de jornal no existe

---

## Ejemplos de Implementación Frontend

### React/TypeScript

```typescript
interface JornalDiarioRequest {
  profesionalId: number;
  obraId: number;
  fecha: string;  // "yyyy-MM-dd"
  horasTrabajadasDecimal: number;
  tarifaDiaria?: number;
  observaciones?: string;
}

interface JornalDiarioResponse {
  id: number;
  profesionalId: number;
  profesionalNombre: string;
  obraId: number;
  obraNombre: string;
  fecha: string;
  horasTrabajadasDecimal: number;
  tarifaDiaria: number;
  montoCobrado: number;
  observaciones?: string;
  empresaId: number;
  fechaCreacion: string;
  fechaActualizacion?: string;
}

// Crear jornal
const crearJornal = async (data: JornalDiarioRequest) => {
  const response = await fetch('/api/jornales-diarios', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': `Bearer ${token}`
    },
    body: JSON.stringify(data)
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return response.json() as Promise<JornalDiarioResponse>;
};

// Obtener jornales de un profesional en una obra
const obtenerJornales = async (profesionalId: number, obraId: number) => {
  const response = await fetch(
    `/api/jornales-diarios/profesional/${profesionalId}/obra/${obraId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  return response.json() as Promise<JornalDiarioResponse[]>;
};

// Obtener resumen
const obtenerResumen = async (profesionalId: number, obraId: number) => {
  const response = await fetch(
    `/api/jornales-diarios/resumen/profesional/${profesionalId}/obra/${obraId}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  return response.json();
};
```

### Angular Service

```typescript
import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

@Injectable({
  providedIn: 'root'
})
export class JornalDiarioService {
  private apiUrl = '/api/jornales-diarios';

  constructor(private http: HttpClient) {}

  crear(data: JornalDiarioRequest): Observable<JornalDiarioResponse> {
    return this.http.post<JornalDiarioResponse>(this.apiUrl, data);
  }

  obtenerPorProfesionalYObra(
    profesionalId: number, 
    obraId: number
  ): Observable<JornalDiarioResponse[]> {
    return this.http.get<JornalDiarioResponse[]>(
      `${this.apiUrl}/profesional/${profesionalId}/obra/${obraId}`
    );
  }

  obtenerResumen(
    profesionalId: number, 
    obraId: number
  ): Observable<JornalResumen> {
    return this.http.get<JornalResumen>(
      `${this.apiUrl}/resumen/profesional/${profesionalId}/obra/${obraId}`
    );
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
```

---

## Casos de Uso Comunes

### 1. Profesional trabaja en 2 obras el mismo día

```json
// Obra 1 - Mañana (medio día)
POST /api/jornales-diarios
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 0.5,
  "observaciones": "Mañana"
}

// Obra 2 - Tarde (medio día)
POST /api/jornales-diarios
{
  "profesionalId": 1,
  "obraId": 8,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 0.5,
  "observaciones": "Tarde"
}
```

### 2. Ver cuánto trabajó un profesional en el mes

```
GET /api/jornales-diarios/profesional/1/fechas?fechaDesde=2026-03-01&fechaHasta=2026-03-31
```

### 3. Ver el costo total de mano de obra de una obra

```
GET /api/jornales-diarios/resumen/obra/5/profesionales
```

Sumar el campo `totalCobrado` de cada profesional.

### 4. Tarifa especial para un día específico

Si un profesional cobra diferente en una obra específica:

```json
POST /api/jornales-diarios
{
  "profesionalId": 1,
  "obraId": 5,
  "fecha": "2026-03-14",
  "horasTrabajadasDecimal": 1.0,
  "tarifaDiaria": 120000.00,  // Especificar tarifa custom
  "observaciones": "Tarifa especial para esta obra"
}
```

---

## Notas Importantes

1. **Multi-tenancy**: Todos los endpoints respetan el `empresaId` del contexto del usuario autenticado.

2. **Validación de duplicados**: No se puede crear un jornal con el mismo `profesionalId + obraId + fecha`. Si se intenta, retorna error 400.

3. **Cálculo automático**: El campo `montoCobrado` siempre se calcula automáticamente en el backend, no hace falta enviarlo desde el frontend.

4. **Tarifa histórica**: Cuando se crea un jornal, la tarifa diaria se copia del profesional en ese momento. Si luego se cambia el `honorarioDia` del profesional, los jornales anteriores mantienen la tarifa que tenían (histórico).

5. **Horas máximas**: El valor máximo permitido es 1.5 (día y medio). Si necesitas registrar más horas, debes crear jornales en días diferentes.

6. **Fechas**: Siempre usar formato ISO 8601: `yyyy-MM-dd` (ej: `2026-03-14`)

7. **Decimales**: Los valores de horas trabajadas admiten máximo 2 decimales (ej: `0.25`, `0.50`, `1.00`)
