# 🔄 TRABAJOS ADICIONALES ANIDADOS - DOCUMENTACIÓN COMPLETA

## 📋 ÍNDICE
1. [Descripción General](#descripción-general)
2. [Jerarquías Soportadas](#jerarquías-soportadas)
3. [Cambios en la Base de Datos](#cambios-en-la-base-de-datos)
4. [Cambios en la API](#cambios-en-la-api)
5. [Ejemplos de Uso](#ejemplos-de-uso)
6. [Validaciones](#validaciones)
7. [Migración](#migración)

---

## 🎯 DESCRIPCIÓN GENERAL

**NUEVA FUNCIONALIDAD:** Los trabajos adicionales ahora soportan **anidación recursiva**, permitiendo que un trabajo adicional pueda tener sus propios trabajos adicionales hijos.

### Antes (Sistema Original)
```
Obra #123
├── Trabajo Adicional #1 (obraId: 123, trabajoExtraId: null)
├── Trabajo Adicional #2 (obraId: 123, trabajoExtraId: null)
└── Trabajo Extra #456
    ├── Trabajo Adicional #3 (obraId: 123, trabajoExtraId: 456)
    └── Trabajo Adicional #4 (obraId: 123, trabajoExtraId: 456)
```

### Ahora (Con Anidación)
```
Obra #123
├── Trabajo Adicional #1 (Padre)
│   ├── Trabajo Adicional #5 (Hijo del #1)
│   │   └── Trabajo Adicional #8 (Hijo del #5)  ← Soporte multinivel
│   └── Trabajo Adicional #6 (Hijo del #1)
├── Trabajo Adicional #2 (Directo de obra)
└── Trabajo Extra #456
    └── Trabajo Adicional #3 (Padre)
        └── Trabajo Adicional #7 (Hijo del #3)
```

---

## 🏗️ JERARQUÍAS SOPORTADAS

### 1️⃣ Trabajo Adicional Directo de Obra (Sin Cambios)
```json
{
  "nombre": "Instalación eléctrica adicional",
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10
}
```
**Jeraquía:** Obra → Trabajo Adicional

---

### 2️⃣ Trabajo Adicional Desde Trabajo Extra (Sin Cambios)
```json
{
  "nombre": "Reparación urgente",
  "obraId": 123,
  "trabajoExtraId": 456,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10
}
```
**Jerarquía:** Obra → Trabajo Extra → Trabajo Adicional

---

### 3️⃣ Trabajo Adicional Hijo de Otro Trabajo Adicional ⭐ NUEVO
```json
{
  "nombre": "Sub-instalación de paneles",
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 1,
  "empresaId": 10
}
```
**Jerarquía:** Obra → Trabajo Adicional Padre → Trabajo Adicional Hijo

---

### 4️⃣ Trabajo Adicional Anidado en Trabajo Extra ⭐ NUEVO
```json
{
  "nombre": "Sub-trabajo de reparación",
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 3,
  "empresaId": 10
}
```
**Jerarquía:** Obra → Trabajo Extra → Trabajo Adicional Padre → Trabajo Adicional Hijo

---

## 💾 CAMBIOS EN LA BASE DE DATOS

### Nueva Columna
| Columna | Tipo | Nullable | Descripción |
|---------|------|----------|-------------|
| `trabajo_adicional_padre_id` | BIGINT | YES | FK a trabajos_adicionales (recursiva) |

### Nuevo Índice
```sql
CREATE INDEX idx_trabajos_adicionales_padre 
ON trabajos_adicionales(trabajo_adicional_padre_id);
```

### Nueva Foreign Key
```sql
ALTER TABLE trabajos_adicionales
ADD CONSTRAINT fk_trabajo_adicional_padre 
FOREIGN KEY (trabajo_adicional_padre_id) 
REFERENCES trabajos_adicionales(id) 
ON DELETE CASCADE;
```

### Constraints
- `trabajoExtraId` y `trabajoAdicionalPadreId` son **mutuamente excluyentes**
- Un trabajo adicional solo puede tener UNO de estos valores:
  - `trabajoExtraId` → Hijo de un trabajo extra
  - `trabajoAdicionalPadreId` → Hijo de otro trabajo adicional
  - Ambos `null` → Directo de obra

---

## 🔌 CAMBIOS EN LA API

### Request DTO (TrabajoAdicionalRequestDTO)

#### Nuevo Campo
```java
/**
 * ID del trabajo adicional padre (opcional)
 * - null: trabajo adicional raíz
 * - valor: trabajo adicional hijo de otro trabajo adicional
 */
private Long trabajoAdicionalPadreId;
```

### Response DTO (TrabajoAdicionalResponseDTO)

#### Nuevos Campos
```java
// ID del padre (si es hijo)
private Long trabajoAdicionalPadreId;

// Lista de hijos (si es padre)
private List<TrabajoAdicionalResponseDTO> trabajosAdicionalesHijos;
```

### Endpoints (Sin Cambios)
```
POST   /api/trabajos-adicionales
GET    /api/trabajos-adicionales
GET    /api/trabajos-adicionales/{id}
PUT    /api/trabajos-adicionales/{id}
DELETE /api/trabajos-adicionales/{id}
PATCH  /api/trabajos-adicionales/{id}/estado
```

---

## 📝 EJEMPLOS DE USO

### Ejemplo 1: Crear Trabajo Adicional Padre (Raíz)
```http
POST /api/trabajos-adicionales
Content-Type: application/json

{
  "nombre": "Sistema de iluminación LED",
  "importe": 50000.00,
  "diasNecesarios": 10,
  "fechaInicio": "2026-03-15",
  "descripcion": "Instalación completa de sistema LED",
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10,
  "profesionales": []
}
```

**Response (201 Created):**
```json
{
  "id": 100,
  "nombre": "Sistema de iluminación LED",
  "importe": 50000.00,
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "trabajosAdicionalesHijos": [],
  "profesionales": []
}
```

---

### Ejemplo 2: Crear Trabajo Adicional Hijo
```http
POST /api/trabajos-adicionales
Content-Type: application/json

{
  "nombre": "Paneles LED para cocina",
  "importe": 15000.00,
  "diasNecesarios": 3,
  "fechaInicio": "2026-03-16",
  "descripcion": "Sub-trabajo: paneles específicos para cocina",
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 100,  // ← Hijo del trabajo adicional #100
  "empresaId": 10,
  "profesionales": []
}
```

**Response (201 Created):**
```json
{
  "id": 101,
  "nombre": "Paneles LED para cocina",
  "importe": 15000.00,
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": 100,  // ← Tiene padre
  "empresaId": 10,
  "estado": "PENDIENTE",
  "trabajosAdicionalesHijos": [],  // ← Este es hijo, no tiene hijos propios
  "profesionales": []
}
```

---

### Ejemplo 3: Obtener Trabajo Adicional Padre con Hijos
```http
GET /api/trabajos-adicionales/100?empresaId=10
```

**Response (200 OK):**
```json
{
  "id": 100,
  "nombre": "Sistema de iluminación LED",
  "importe": 50000.00,
  "obraId": 123,
  "trabajoExtraId": null,
  "trabajoAdicionalPadreId": null,
  "empresaId": 10,
  "estado": "PENDIENTE",
  "trabajosAdicionalesHijos": [
    {
      "id": 101,
      "nombre": "Paneles LED para cocina",
      "importe": 15000.00,
      "trabajoAdicionalPadreId": 100,
      "trabajosAdicionalesHijos": []  // ← Solo 1 nivel de profundidad
    },
    {
      "id": 102,
      "nombre": "Paneles LED para baño",
      "importe": 8000.00,
      "trabajoAdicionalPadreId": 100,
      "trabajosAdicionalesHijos": []
    }
  ],
  "profesionales": []
}
```

---

## ✅ VALIDACIONES

### 1. Constraint de Exclusión Mutua
```java
if (trabajoExtraId != null && trabajoAdicionalPadreId != null) {
    throw new TrabajoAdicionalValidationException(
        "Un trabajo adicional no puede tener trabajoExtraId y trabajoAdicionalPadreId simultáneamente"
    );
}
```

**❌ INVÁLIDO:**
```json
{
  "obraId": 123,
  "trabajoExtraId": 456,
  "trabajoAdicionalPadreId": 100  // ← ERROR: No puede tener ambos
}
```

---

### 2. Validación de Padre Existente
```java
// El padre debe existir y pertenecer a la misma obra y empresa
```

**❌ INVÁLIDO:**
```json
{
  "obraId": 123,
  "trabajoAdicionalPadreId": 999,  // ← ERROR: No existe
  "empresaId": 10
}
```

---

### 3. Validación de Obra Consistente
```java
// El padre debe pertenecer a la misma obra que el hijo
```

**❌ INVÁLIDO:**
```json
{
  "obraId": 99999,  // ← Obra diferente al padre
  "trabajoAdicionalPadreId": 100,  // ← Padre pertenece a obra #123
  "empresaId": 10
}
```

---

### 4. Advertencia de Anidación Profunda
```java
// Se permite pero se registra una advertencia en logs
if (trabajoAdicionalPadre.getTrabajoAdicionalPadreId() != null) {
    log.warn("ADVERTENCIA: Creando trabajo adicional hijo de otro trabajo hijo");
}
```

**⚠️ PERMITIDO PERO CON ADVERTENCIA:**
```
Trabajo Adicional #100 (abuelo)
└── Trabajo Adicional #101 (padre)
    └── Trabajo Adicional #102 (hijo)  ← Nivel 3 de anidación
```

---

## 🔄 MIGRACIÓN

### Paso 1: Ejecutar Script SQL
```bash
# Opción A: Desde línea de comandos
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 \
  -f migration_trabajos_adicionales_anidacion.sql

# Opción B: Desde pgAdmin/DBeaver
# Copiar y ejecutar: migration_trabajos_adicionales_anidacion.sql
```

### Paso 2: Reiniciar Backend
```bash
# PowerShell
.\mvnw clean install
.\mvnw spring-boot:run
```

### Paso 3: Verificar Migración
```sql
-- Verificar que la columna existe
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'trabajos_adicionales'
  AND column_name = 'trabajo_adicional_padre_id';

-- Verificar que la FK existe
SELECT constraint_name
FROM information_schema.table_constraints
WHERE table_name = 'trabajos_adicionales'
  AND constraint_name = 'fk_trabajo_adicional_padre';
```

---

## 🔍 CONSULTAS ÚTILES

### Ver Jerarquía Completa
```sql
SELECT 
    ta.id,
    ta.nombre,
    ta.obra_id,
    ta.trabajo_extra_id,
    ta.trabajo_adicional_padre_id,
    tap.nombre AS nombre_padre,
    ta.estado,
    ta.importe
FROM trabajos_adicionales ta
LEFT JOIN trabajos_adicionales tap 
  ON ta.trabajo_adicional_padre_id = tap.id
ORDER BY ta.obra_id, COALESCE(ta.trabajo_adicional_padre_id, ta.id), ta.id;
```

### Contar por Tipo de Vinculación
```sql
SELECT 
    CASE 
        WHEN trabajo_extra_id IS NOT NULL AND trabajo_adicional_padre_id IS NULL 
            THEN 'Vinculado a Trabajo Extra'
        WHEN trabajo_extra_id IS NULL AND trabajo_adicional_padre_id IS NOT NULL 
            THEN 'Vinculado a Trabajo Adicional Padre'
        WHEN trabajo_extra_id IS NULL AND trabajo_adicional_padre_id IS NULL 
            THEN 'Directo de Obra'
        ELSE 'INVÁLIDO (ambos no nulos)'
    END AS tipo_vinculacion,
    COUNT(*) AS cantidad
FROM trabajos_adicionales
GROUP BY tipo_vinculacion;
```

---

## 📊 MATRIZ DE COMPATIBILIDAD

| obraId | trabajoExtraId | trabajoAdicionalPadreId | Resultado |
|--------|----------------|-------------------------|-----------|
| ✅ 123 | ❌ null        | ❌ null                 | ✅ Directo de obra |
| ✅ 123 | ✅ 456         | ❌ null                 | ✅ Hijo de trabajo extra |
| ✅ 123 | ❌ null        | ✅ 100                  | ✅ Hijo de trabajo adicional |
| ✅ 123 | ✅ 456         | ✅ 100                  | ❌ ERROR: Excluyentes |
| ❌ null | ❌ null       | ❌ null                 | ❌ ERROR: obraId obligatorio |

---

## 🎯 CASOS DE USO

### Caso 1: Obra con Sub-trabajos
```
Obra: "Edificio Residencial"
└── Trabajo Adicional: "Instalación eléctrica completa"
    ├── Trabajo Adicional Hijo: "Cableado planta baja"
    ├── Trabajo Adicional Hijo: "Cableado primer piso"
    └── Trabajo Adicional Hijo: "Cableado segundo piso"
```

### Caso 2: Trabajo Extra con Detalles
```
Obra: "Casa Familiar"
└── Trabajo Extra: "Remodelación de cocina"
    └── Trabajo Adicional: "Instalación de gabinetes"
        ├── Trabajo Adicional Hijo: "Gabinetes base"
        ├── Trabajo Adicional Hijo: "Gabinetes aéreos"
        └── Trabajo Adicional Hijo: "Isla central"
```

---

## 🚀 CARACTERÍSTICAS TÉCNICAS

### Mapeo de Entidades
- **Relación Padre:** `@ManyToOne` con `@JoinColumn(name = "trabajo_adicional_padre_id")`
- **Relación Hijos:** `@OneToMany(mappedBy = "trabajoAdicionalPadre")`
- **Cascada:** `CascadeType.ALL` con `orphanRemoval = true`
- **Fetch:** `FetchType.LAZY` para optimizar rendimiento

### Prevención de Recursión Infinita
- El mapeo a DTO solo incluye **1 nivel de profundidad**
- Los hijos se mapean con `mapearAResponseDTOLigero()` sin incluir sus propios hijos
- Evita loops infinitos en serialización JSON

### Métodos Helper
```java
// Agregar hijo
trabajoAdicionalPadre.addTrabajoAdicionalHijo(hijo);

// Remover hijo
trabajoAdicionalPadre.removeTrabajoAdicionalHijo(hijo);

// Verificar si tiene padre
boolean tienepadre = trabajoAdicional.tieneTrabajoAdicionalPadre();

// Verificar si tiene hijos
boolean tieneHijos = trabajoAdicional.tieneTrabajosAdicionalesHijos();
```

---

## 📚 DOCUMENTACIÓN RELACIONADA

- [TRABAJOS_ADICIONALES_README.md](TRABAJOS_ADICIONALES_README.md) - Documentación original
- [FRONTEND_API_TRABAJOS_ADICIONALES.md](FRONTEND_API_TRABAJOS_ADICIONALES.md) - API para frontend
- [migration_trabajos_adicionales_anidacion.sql](migration_trabajos_adicionales_anidacion.sql) - Script de migración

---

## 🎉 RESUMEN

✅ **Funcionalidad implementada:**
- Soporte completo para anidación recursiva de trabajos adicionales
- Validaciones robustas en backend
- Prevención de recursión infinita
- Compatibilidad total con el sistema existente
- Sin cambios en endpoints (solo nuevos campos opcionales)

✅ **Compatibilidad hacia atrás:**
- Los trabajos adicionales existentes siguen funcionando sin cambios
- El campo `trabajoAdicionalPadreId` es opcional y nullable
- Los trabajos sin padre simplemente tienen `trabajoAdicionalPadreId = null`

✅ **Listo para usar:**
- Ejecutar script SQL de migración
- Reiniciar backend
- Actualizar frontend para usar el nuevo campo (opcional)

---

**Fecha de implementación:** 3 de marzo de 2026  
**Versión:** 1.0.0  
**Estado:** ✅ Producción Ready
