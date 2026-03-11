# ============================================
# RESUMEN DE CAMBIOS - Mayores Costos Honorarios
# Fecha: 2026-03-11
# Autor: GitHub Copilot
# ============================================

## 🐛 PROBLEMA REPORTADO

El frontend envía correctamente los campos de `honorarios` en `mayoresCostosPorRubro` al hacer PUT a `/api/v1/presupuestos-no-cliente/{id}`, pero el backend **NO los está guardando ni devolviendo**.

### Datos que envía el frontend:
```json
{
  "mayoresCostosPorRubro": [
    {
      "nombreRubro": "Pintura",
      "honorariosActivo": true,
      "honorariosTipo": "PORCENTAJE",
      "honorariosValor": 100,
      "profesionalesActivo": false,
      "profesionalesValor": 100,
      "materialesActivo": false,
      "materialesValor": 100
    }
  ]
}
```

### Datos que devolvía el backend (ANTES del fix):
```json
{
  "mayoresCostosPorRubro": [
    {
      "nombreRubro": "Pintura",
      "profesionalesActivo": false,
      "profesionalesValor": 100,
      "materialesActivo": false,
      "materialesValor": 100,
      "otrosCostosActivo": false,
      "otrosCostosValor": 0
      // ❌ FALTABAN: honorariosActivo, honorariosTipo, honorariosValor
    }
  ]
}
```

---

## ✅ SOLUCIÓN IMPLEMENTADA

### 1. **Migración de Base de Datos**

**Archivo:** `V43__agregar_honorarios_mayores_costos_por_rubro.sql`

```sql
ALTER TABLE mayores_costos_por_rubro 
ADD COLUMN IF NOT EXISTS honorarios_activo BOOLEAN NOT NULL DEFAULT true;

ALTER TABLE mayores_costos_por_rubro 
ADD COLUMN IF NOT EXISTS honorarios_tipo VARCHAR(20) NOT NULL DEFAULT 'porcentaje';

ALTER TABLE mayores_costos_por_rubro 
ADD COLUMN IF NOT EXISTS honorarios_valor DECIMAL(10,2);
```

**Ubicación:** `src/main/resources/db/migration/V43__agregar_honorarios_mayores_costos_por_rubro.sql`

---

### 2. **Actualización de la Entidad JPA**

**Archivo:** `MayorCostoPorRubro.java`

**Campos agregados:**
```java
// Honorarios
@Column(name = "honorarios_activo", nullable = false, columnDefinition = "BOOLEAN DEFAULT true")
private Boolean honorariosActivo = true;

@Column(name = "honorarios_tipo", length = 20, nullable = false, columnDefinition = "VARCHAR(20) DEFAULT 'porcentaje'")
private String honorariosTipo = "porcentaje";

@Column(name = "honorarios_valor", precision = 10, scale = 2)
private BigDecimal honorariosValor;
```

**Ubicación:** `src/main/java/com/rodrigo/construccion/model/entity/MayorCostoPorRubro.java`

---

### 3. **Actualización del DTO**

**Archivo:** `MayorCostoPorRubroDTO.java`

**Campos agregados:**
```java
// Honorarios
private Boolean honorariosActivo;
private String honorariosTipo;
private BigDecimal honorariosValor;
```

**Ubicación:** `src/main/java/com/rodrigo/construccion/dto/request/MayorCostoPorRubroDTO.java`

---

### 4. **Actualización del Mapper**

**Archivo:** `PresupuestoNoClienteService.java`

**Código agregado en el método `mapearMayoresCostosPorRubroDTO()`:**
```java
// Honorarios
mayorCosto.setHonorariosActivo(dto.getHonorariosActivo() != null ? dto.getHonorariosActivo() : true);
mayorCosto.setHonorariosTipo(dto.getHonorariosTipo() != null ? dto.getHonorariosTipo() : "porcentaje");
mayorCosto.setHonorariosValor(dto.getHonorariosValor());
```

**Ubicación:** `src/main/java/com/rodrigo/construccion/service/PresupuestoNoClienteService.java` (línea ~5888)

---

## 🔄 PASOS PARA APLICAR LOS CAMBIOS

### 1. **Detener el backend** (si está corriendo)
```powershell
# Encontrar proceso en puerto 8080
netstat -ano | findstr :8080

# Detener proceso (reemplazar PID con el número real)
taskkill /F /PID <PID>
```

### 2. **Crear backup de la base de datos**
```powershell
.\backup-bd-construccion-app.ps1
```

### 3. **Compilar el backend**
```powershell
./mvnw clean compile
```

### 4. **Ejecutar migraciones y levantar el backend**
```powershell
./mvnw spring-boot:run
```
- Flyway ejecutará automáticamente la migración V43
- El backend se levantará con las nuevas columnas

### 5. **Verificar que la migración se ejecutó**
```sql
-- Verificar las nuevas columnas
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'mayores_costos_por_rubro'
  AND column_name LIKE 'honorarios%'
ORDER BY column_name;
```

### 6. **Probar el endpoint**

Opción A - Con PowerShell (test-mayores-costos-honorarios.ps1):
```powershell
./test-mayores-costos-honorarios.ps1
```

Opción B - Con Postman/cURL:
```bash
# PUT /api/presupuestos-no-cliente/108
{
  "idEmpresa": 9,
  "idObra": 1,
  "idCliente": 2,
  "mayoresCostosPorRubro": [
    {
      "nombreRubro": "Pintura",
      "honorariosActivo": true,
      "honorariosTipo": "porcentaje",
      "honorariosValor": 15.0,
      "profesionalesActivo": false,
      "profesionalesValor": 10.0
    }
  ]
}
```

### 7. **Verificar el resultado**

El GET a `/api/presupuestos-no-cliente/108?empresaId=9` ahora DEBE devolver:

```json
{
  "id": 108,
  "mayoresCostosPorRubro": [
    {
      "nombreRubro": "Pintura",
      "honorariosActivo": true,
      "honorariosTipo": "porcentaje",
      "honorariosValor": 15.0,
      "profesionalesActivo": false,
      "profesionalesValor": 10.0,
      "materialesActivo": true,
      "materialesValor": null,
      "otrosCostosActivo": true,
      "otrosCostosValor": null
    }
  ]
}
```

---

## 📋 ARCHIVOS MODIFICADOS

1. ✅ `V43__agregar_honorarios_mayores_costos_por_rubro.sql` - **CREADO**
2. ✅ `MayorCostoPorRubro.java` - Agregados 3 campos
3. ✅ `MayorCostoPorRubroDTO.java` - Agregados 3 campos
4. ✅ `PresupuestoNoClienteService.java` - Actualizado mapper (3 líneas)

---

## ⚠️ IMPORTANTE

- **No revertir la migración V43** una vez aplicada (las columnas ya existen en producción si se deployó)
- **Los valores por defecto** son:
  - `honorarios_activo`: `true`
  - `honorarios_tipo`: `'porcentaje'`
  - `honorarios_valor`: `NULL`

---

## 🧪 VALIDACIÓN

1. ✅ El frontend envía los campos → Backend los recibe en el DTO
2. ✅ El mapper transforma DTO → Entidad JPA
3. ✅ JPA persiste en BD → Columnas creadas por migración V43
4. ✅ JPA recupera de BD → Devuelve en el GET
5. ✅ Backend serializa a JSON → Frontend los recibe

---

## 💡 CAMPOS RELACIONADOS

La tabla `mayores_costos_por_rubro` ahora tiene configuración para:

| Categoría          | Campos                                         |
|--------------------|------------------------------------------------|
| **Profesionales**  | `profesionales_activo`, `profesionales_tipo`, `profesionales_valor` |
| **Materiales**     | `materiales_activo`, `materiales_tipo`, `materiales_valor` |
| **Otros Costos**   | `otros_costos_activo`, `otros_costos_tipo`, `otros_costos_valor` |
| **Honorarios** ✨  | `honorarios_activo`, `honorarios_tipo`, `honorarios_valor` |

Todos tienen el mismo patrón de 3 columnas:
- `{categoria}_activo` (Boolean): Si se aplica o no
- `{categoria}_tipo` (String): 'porcentaje' o 'importe_fijo'
- `{categoria}_valor` (BigDecimal): El valor del porcentaje o importe

---

## 🔗 REFERENCIAS

- Issue original: "El frontend está enviando correctamente los campos de honorarios en mayoresCostosPorRubro..."
- Fecha del fix: 2026-03-11
- Relacionado con presupuesto ID: 108
