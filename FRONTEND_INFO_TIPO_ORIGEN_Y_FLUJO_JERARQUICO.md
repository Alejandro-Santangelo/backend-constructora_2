# 📋 INFORMACIÓN PARA FRONTEND - Sistema de Obras Jerárquicas

## 🆕 NUEVO CAMPO DISPONIBLE: `tipoOrigen`

### Endpoints actualizados:
- `GET /api/obras` → `ObraSimpleDTO` ahora incluye `tipoOrigen`
- `GET /api/obras/{id}` → `ObraResponseDTO` ahora incluye `tipoOrigen`

### Valores posibles de `tipoOrigen`:
```typescript
type TipoOrigen = 
  | 'PRESUPUESTO_PRINCIPAL'      // Obra generada desde presupuesto PRINCIPAL
  | 'PRESUPUESTO_TRABAJO_DIARIO' // Obra generada desde presupuesto TRABAJO_DIARIO  
  | 'PRESUPUESTO_TRABAJO_EXTRA'  // Sub-obra generada desde presupuesto TRABAJO_EXTRA
  | 'PRESUPUESTO_TAREA_LEVE'     // Sub-obra generada desde presupuesto TAREA_LEVE
  | 'OBRA_MANUAL';               // Obra creada manualmente (sin presupuesto)
```

---

## 📊 FLUJO JERÁRQUICO DE OBRAS Y PRESUPUESTOS

### 1️⃣ NIVEL RAÍZ (solo estos dos tipos):
```
📋 PRESUPUESTO PRINCIPAL
   └─ estado: APROBADO → genera OBRA automáticamente
   
📋 PRESUPUESTO TRABAJO_DIARIO  
   └─ estado: APROBADO → genera OBRA automáticamente
```

### 2️⃣ DESDE CUALQUIER OBRA (Principal, Trabajo Diario, o Sub-obras):
```
📁 OBRA (cualquier tipo)
   ├─ 📋 Presupuesto TRABAJO_EXTRA (máximo 3)
   │     └─ estado: APROBADO → genera SUB-OBRA
   │
   └─ 📋 Presupuesto TAREA_LEVE (ilimitados)
         └─ estado: APROBADO → genera SUB-OBRA
```

### 3️⃣ ANIDAMIENTO (recursivo):
```
📋 PRINCIPAL/TRABAJO_DIARIO
   ↓
📁 Obra Raíz
   ├─ 📋 TRABAJO_EXTRA → 📁 Sub-Obra 1
   │     ├─ 📋 TRABAJO_EXTRA → 📁 Sub-Sub-Obra 1.1
   │     └─ 📋 TAREA_LEVE → 📁 Sub-Sub-Obra 1.2
   │
   └─ 📋 TAREA_LEVE → 📁 Sub-Obra 2
         └─ 📋 TRABAJO_EXTRA → 📁 Sub-Sub-Obra 2.1
```

---

## 🔗 RELACIONES BIDIRECCIONALES

### Backend mantiene automáticamente:

**Obra → Presupuesto Padre:**
```json
{
  "id": 56,
  "nombre": "Casa de Paula",
  "presupuestoNoClienteId": 104,  // ⭐ Presupuesto que generó esta obra
  "tipoOrigen": "PRESUPUESTO_TRABAJO_DIARIO"
}
```

**Presupuesto → Obra Generada:**
```json
{
  "id": 104,
  "tipoPresupuesto": "TRABAJO_DIARIO",
  "obraId": 56,  // ⭐ Obra que fue generada por este presupuesto
  "estado": "APROBADO"
}
```

---

## 🎯 CASOS DE USO PARA EL FRONTEND

### 1. Mostrar árbol jerárquico de obras:
```typescript
// Usar tipoOrigen para iconos/badges
function getIconPorTipo(tipoOrigen: TipoOrigen) {
  switch(tipoOrigen) {
    case 'PRESUPUESTO_PRINCIPAL':
    case 'PRESUPUESTO_TRABAJO_DIARIO':
      return '🏠'; // Obra principal
    case 'PRESUPUESTO_TRABAJO_EXTRA':
      return '🔧'; // Trabajo adicional
    case 'PRESUPUESTO_TAREA_LEVE':
      return '⚡'; // Tarea leve
    case 'OBRA_MANUAL':
      return '✏️'; // Manual
  }
}
```

### 2. Validar límites de presupuestos:
```typescript
// Al crear presupuesto desde una obra
function puedeCrearTrabajoExtra(obra: Obra): boolean {
  const trabajoExtraActuales = obra.presupuestosHijos
    .filter(p => p.tipoPresupuesto === 'TRABAJO_EXTRA')
    .length;
  
  return trabajoExtraActuales < 3; // ⚠️ Máximo 3
}

function puedeCrearTareaLeve(obra: Obra): boolean {
  return true; // ✅ Ilimitados
}
```

### 3. Navegación padre-hijo:
```typescript
// Obtener presupuesto padre de una obra
async function obtenerPresupuestoPadre(obra: Obra) {
  if (obra.presupuestoNoClienteId) {
    return await fetch(`/api/presupuestos-no-cliente/${obra.presupuestoNoClienteId}`);
  }
  return null;
}

// Obtener obra generada por un presupuesto
async function obtenerObraGenerada(presupuesto: PresupuestoNoCliente) {
  if (presupuesto.obraId) {
    return await fetch(`/api/obras/${presupuesto.obraId}`);
  }
  return null;
}
```

---

## ⚙️ SINCRONIZACIÓN AUTOMÁTICA DE ESTADOS

**Estados disponibles (10 en total, sincronizados entre Obra y Presupuesto):**
```typescript
type Estado = 
  | 'BORRADOR'           // Estado inicial
  | 'A_ENVIAR'           // Listo para enviar
  | 'ENVIADO'            // Enviado al cliente
  | 'MODIFICADO'         // Modificado después de enviado
  | 'APROBADO'           // Aprobado por cliente
  | 'OBRA_A_CONFIRMAR'   // Pendiente de confirmación
  | 'EN_EJECUCION'       // 🏗️ Obra en ejecución (NO "EN_CURSO")
  | 'SUSPENDIDA'         // Temporalmente suspendida
  | 'TERMINADO'          // Finalizado
  | 'CANCELADO';         // Cancelado
```

**El backend sincroniza automáticamente:**

### 1️⃣ Cuando se CREA una obra desde un presupuesto:
```
PRESUPUESTO estado BORRADOR/A_ENVIAR/MODIFICADO/ENVIADO/OBRA_A_CONFIRMAR 
  → OBRA estado BORRADOR

PRESUPUESTO estado APROBADO
  → OBRA estado APROBADO (o EN_EJECUCION si fecha inicio ya pasó)

PRESUPUESTO estado EN_EJECUCION 
  → OBRA estado EN_EJECUCION

PRESUPUESTO estado TERMINADO 
  → OBRA estado TERMINADO

PRESUPUESTO estado CANCELADO 
  → OBRA estado CANCELADO
```

### 2️⃣ Cuando CAMBIA el estado del presupuesto (después de creada la obra):
```
Sincronización 1:1 directa por nombre de enum:
APROBADO → APROBADO
EN_EJECUCION → EN_EJECUCION
TERMINADO → TERMINADO
CANCELADO → CANCELADO
SUSPENDIDA → SUSPENDIDA
etc.
```

### 3️⃣ Cuando CAMBIA el estado de la obra:
```
Sincronización 1:1 directa por nombre de enum:
APROBADO → APROBADO
EN_EJECUCION → EN_EJECUCION
TERMINADO → TERMINADO
CANCELADO → CANCELADO
SUSPENDIDA → SUSPENDIDA
etc.
```

**⚠️ IMPORTANTE:** El estado correcto es **`EN_EJECUCION`**, NO "EN_CURSO".

**El frontend NO necesita hacer esta sincronización manualmente.**

---

## 📝 DATOS FINANCIEROS

Cuando un presupuesto genera una obra, el backend copia automáticamente:
- ✅ `presupuestoEstimado`
- ✅ `presupuestoJornales`
- ✅ `presupuestoMateriales`
- ✅ `presupuestoHonorarios`
- ✅ Todos los descuentos (40+ campos)

**El frontend NO necesita copiar estos datos manualmente.**

---

## 🚨 IMPORTANTE

### ✅ Lo que el frontend DEBE hacer:
1. Mostrar el campo `tipoOrigen` en la UI (badges, iconos, filtros)
2. Validar límite de 3 TRABAJO_EXTRA por obra
3. Permitir crear TAREA_LEVE ilimitados
4. Mostrar navegación padre↔hijo usando `presupuestoNoClienteId` y `obraId`

### ❌ Lo que el frontend NO debe hacer:
1. ~~Sincronizar estados manualmente~~ (el backend lo hace automático)
2. ~~Copiar datos financieros~~ (el backend lo hace automático)
3. ~~Validar relaciones bidireccionales~~ (el backend garantiza consistencia)

---

## 📞 CONSULTAS

Si tenés dudas sobre:
- Endpoints específicos
- Estructura de DTOs
- Validaciones adicionales

Revisá los archivos:
- `ObraResponseDTO.java`
- `ObraSimpleDTO.java`
- `PresupuestoNoClienteResponseDTO.java`
