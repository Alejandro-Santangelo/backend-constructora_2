# 📋 PRESUPUESTOS TAREA_LEVE - VINCULACIÓN A TRABAJOS ADICIONALES

## 🎯 Resumen de la Funcionalidad

**IMPLEMENTADO**: Presupuestos tipo `TAREA_LEVE` ahora pueden vincularse a:
- ✅ **Obra** (comportamiento existente)
- ⭐ **TrabajoAdicional** (NUEVA FUNCIONALIDAD)
- ⭐ **TrabajoAdicional anidado** (hijo de otro TrabajoAdicional)

## 🏗️ Jerarquía Completa Soportada

```
Obra Principal (#123)
├── Presupuesto TAREA_LEVE #1 (obraId: 123, trabajoAdicionalId: null) ✓ Existente
├── Presupuesto TAREA_LEVE #2 (obraId: 123, trabajoAdicionalId: null) ✓ Existente
│
├── TrabajoAdicional #100 (directo de obra)
│   ├── Presupuesto TAREA_LEVE #3 (obraId: null, trabajoAdicionalId: 100) ⭐ NUEVO
│   ├── Presupuesto TAREA_LEVE #4 (obraId: null, trabajoAdicionalId: 100) ⭐ NUEVO
│   └── Presupuesto TAREA_LEVE #5 (obraId: null, trabajoAdicionalId: 100) ⭐ NUEVO
│
├── TrabajoAdicional #101 (directo de obra)
│   └── TrabajoAdicional hijo #102 (anidado, padre: #101)
│       ├── Presupuesto TAREA_LEVE #6 (obraId: null, trabajoAdicionalId: 102) ⭐ NUEVO
│       └── Presupuesto TAREA_LEVE #7 (obraId: null, trabajoAdicionalId: 102) ⭐ NUEVO
│
└── TrabajoExtra #200
    └── TrabajoAdicional #103 (desde trabajo extra)
        ├── Presupuesto TAREA_LEVE #8 (obraId: null, trabajoAdicionalId: 103) ⭐ NUEVO
        └── TrabajoAdicional hijo #104 (anidado, padre: #103)
            └── Presupuesto TAREA_LEVE #9 (obraId: null, trabajoAdicionalId: 104) ⭐ NUEVO
```

## 📝 Reglas de Negocio

### 1. **Mutua Exclusividad (XOR)**
Para presupuestos tipo `TAREA_LEVE`:
- ✅ **Válido**: `obraId` tiene valor, `trabajoAdicionalId` es `null`
- ✅ **Válido**: `obraId` es `null`, `trabajoAdicionalId` tiene valor
- ❌ **Inválido**: ambos `obraId` y `trabajoAdicionalId` tienen valor
- ❌ **Inválido**: ambos `obraId` y `trabajoAdicionalId` son `null`

### 2. **Restricciones por Tipo de Presupuesto**
- `TRADICIONAL`: No requiere `obraId` ni `trabajoAdicionalId`
- `TRABAJO_DIARIO`: No requiere `obraId` ni `trabajoAdicionalId`
- `TRABAJO_EXTRA`: Requiere `obraId` (obligatorio), `trabajoAdicionalId` debe ser `null`
- `TAREA_LEVE`: Requiere `obraId` **O** `trabajoAdicionalId` (mutuamente excluyentes)

### 3. **Constraint de Base de Datos**
```sql
ALTER TABLE presupuesto_no_cliente
ADD CONSTRAINT chk_tarea_leve_vinculo_exclusivo
CHECK (
    tipo_presupuesto != 'TAREA_LEVE' OR
    (
        (obra_id IS NOT NULL AND trabajo_adicional_id IS NULL) OR
        (obra_id IS NULL AND trabajo_adicional_id IS NOT NULL)
    )
);
```

## 🔌 API Endpoints

### POST `/api/presupuestos-no-cliente`

#### Ejemplo 1: TAREA_LEVE vinculado a Obra (comportamiento actual)
```json
{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": 123,
  "trabajoAdicionalId": null,
  "nombreObra": "Tarea leve - Reparación baño",
  "direccionObraCalle": "Av. Corrientes",
  "direccionObraAltura": "1234",
  "totalPresupuesto": 50000.00,
  "profesionales": [...],
  "materialesList": [...]
}
```

#### Ejemplo 2: TAREA_LEVE vinculado a TrabajoAdicional ⭐ NUEVO
```json
{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": null,
  "trabajoAdicionalId": 100,
  "nombreObra": "Tarea leve - Corrección eléctrica adicional",
  "direccionObraCalle": "Av. Corrientes",
  "direccionObraAltura": "1234",
  "totalPresupuesto": 25000.00,
  "profesionales": [...],
  "materialesList": [...]
}
```

#### Ejemplo 3: TAREA_LEVE vinculado a TrabajoAdicional anidado ⭐ NUEVO
```json
{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": null,
  "trabajoAdicionalId": 102,
  "nombreObra": "Tarea leve - Ajuste final",
  "direccionObraCalle": "Av. Corrientes",
  "direccionObraAltura": "1234",
  "totalPresupuesto": 15000.00,
  "profesionales": [...],
  "materialesList": [...]
}
```

## ❌ Ejemplos de Solicitudes Inválidas

### Error 1: Sin vinculación
```json
{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": null,           // ❌
  "trabajoAdicionalId": null // ❌
}
```
**Respuesta**: `400 Bad Request`  
**Error**: `"ERROR: Presupuestos tipo TAREA_LEVE requieren obraId O trabajoAdicionalId."`

### Error 2: Ambos vínculos
```json
{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": 123,              // ❌ No puede tener ambos
  "trabajoAdicionalId": 100   // ❌ No puede tener ambos
}
```
**Respuesta**: `400 Bad Request`  
**Error**: `"ERROR: Presupuestos tipo TAREA_LEVE no pueden tener obraId Y trabajoAdicionalId simultáneamente."`

### Error 3: TrabajoAdicional inexistente
```json
{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": null,
  "trabajoAdicionalId": 99999 // ❌ No existe
}
```
**Respuesta**: `400 Bad Request`  
**Error**: `"TrabajoAdicional no encontrado con ID: 99999"`

## 📊 Modelo de Datos

### Tabla: `presupuesto_no_cliente`
```sql
CREATE TABLE presupuesto_no_cliente (
    id BIGSERIAL PRIMARY KEY,
    numero_presupuesto BIGINT NOT NULL,
    numero_version INT NOT NULL,
    tipo_presupuesto VARCHAR(50) NOT NULL,
    
    -- Campos de vinculación (mutuamente excluyentes para TAREA_LEVE)
    obra_id BIGINT REFERENCES obras(id) ON DELETE CASCADE,
    trabajo_adicional_id BIGINT REFERENCES trabajos_adicionales(id) ON DELETE SET NULL, -- ⭐ NUEVO
    
    -- Otros campos...
    nombre_obra VARCHAR(255),
    total_presupuesto_con_honorarios NUMERIC(15,2),
    estado VARCHAR(50),
    empresa_id BIGINT NOT NULL,
    fecha_creacion DATE,
    
    -- Constraint de validación
    CONSTRAINT chk_tarea_leve_vinculo_exclusivo CHECK (
        tipo_presupuesto != 'TAREA_LEVE' OR
        ((obra_id IS NOT NULL AND trabajo_adicional_id IS NULL) OR
         (obra_id IS NULL AND trabajo_adicional_id IS NOT NULL))
    )
);

-- Índices
CREATE INDEX idx_presupuesto_obra ON presupuesto_no_cliente(obra_id);
CREATE INDEX idx_presupuesto_trabajo_adicional ON presupuesto_no_cliente(trabajo_adicional_id); -- ⭐ NUEVO
CREATE INDEX idx_presupuesto_tipo ON presupuesto_no_cliente(tipo_presupuesto);
```

### Tabla: `trabajos_adicionales`
```sql
CREATE TABLE trabajos_adicionales (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    obra_id BIGINT NOT NULL REFERENCES obras(id),
    trabajo_extra_id BIGINT REFERENCES trabajos_extra(id),
    trabajo_adicional_padre_id BIGINT REFERENCES trabajos_adicionales(id), -- Anidación recursiva
    estado VARCHAR(50) NOT NULL,
    importe NUMERIC(15,2) NOT NULL,
    empresa_id BIGINT NOT NULL
    -- ... otros campos
);
```

## 🔗 Relaciones JPA

### PresupuestoNoCliente Entity
```java
@Entity
@Table(name = "presupuesto_no_cliente")
public class PresupuestoNoCliente {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_presupuesto", nullable = false)
    private TipoPresupuesto tipoPresupuesto;
    
    // Vinculación a Obra (existente)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "obra_id")
    private Obra obra;
    
    // Vinculación a TrabajoAdicional (NUEVO) ⭐
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trabajo_adicional_id")
    private TrabajoAdicional trabajoAdicional;
    
    // Getters
    @JsonProperty("obraId")
    public Long getObraId() {
        return this.obra != null ? this.obra.getId() : null;
    }
    
    @JsonProperty("trabajoAdicionalId")
    public Long getTrabajoAdicionalId() {
        return this.trabajoAdicional != null ? this.trabajoAdicional.getId() : null;
    }
}
```

### TrabajoAdicional Entity
```java
@Entity
@Table(name = "trabajos_adicionales")
public class TrabajoAdicional {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    // Relación con Presupuestos TAREA_LEVE (NUEVO) ⭐
    @OneToMany(mappedBy = "trabajoAdicional", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @Builder.Default
    private List<PresupuestoNoCliente> presupuestosTareasLeves = new ArrayList<>();
    
    // Métodos helper
    public void addPresupuestoTareaLeve(PresupuestoNoCliente presupuesto) {
        presupuestosTareasLeves.add(presupuesto);
        presupuesto.setTrabajoAdicional(this);
    }
    
    public boolean tienePresupuestosTareasLeves() {
        return presupuestosTareasLeves != null && !presupuestosTareasLeves.isEmpty();
    }
}
```

## 🔍 Consultas SQL Útiles

### 1. Listar todos los presupuestos TAREA_LEVE con vinculación
```sql
SELECT * FROM v_presupuestos_tarea_leve_vinculacion;
```

### 2. Contar presupuestos por tipo de vinculación
```sql
SELECT 
    CASE 
        WHEN obra_id IS NOT NULL THEN 'VINCULADO_A_OBRA'
        WHEN trabajo_adicional_id IS NOT NULL THEN 'VINCULADO_A_TRABAJO_ADICIONAL'
        ELSE 'SIN_VINCULACION'
    END AS tipo_vinculacion,
    COUNT(*) as cantidad
FROM presupuesto_no_cliente
WHERE tipo_presupuesto = 'TAREA_LEVE'
GROUP BY tipo_vinculacion
ORDER BY cantidad DESC;
```

### 3. Ver TrabajoAdicional con sus presupuestos TAREA_LEVE
```sql
SELECT 
    ta.id,
    ta.nombre as trabajo_adicional,
    ta.estado,
    ta.obra_id,
    ta.trabajo_adicional_padre_id,
    COUNT(p.id) as cantidad_tareas_leves,
    SUM(p.total_presupuesto_con_honorarios) as total_tareas_leves
FROM trabajos_adicionales ta
LEFT JOIN presupuesto_no_cliente p 
    ON p.trabajo_adicional_id = ta.id 
    AND p.tipo_presupuesto = 'TAREA_LEVE'
GROUP BY ta.id, ta.nombre, ta.estado, ta.obra_id, ta.trabajo_adicional_padre_id
HAVING COUNT(p.id) > 0
ORDER BY cantidad_tareas_leves DESC;
```

### 4. Jerarquía completa: Obra → TrabajoAdicional → Presupuestos TAREA_LEVE
```sql
WITH RECURSIVE jerarquia AS (
    -- Nivel 0: Trabajos adicionales raíz (sin padre)
    SELECT 
        ta.id,
        ta.nombre,
        ta.obra_id,
        ta.trabajo_adicional_padre_id,
        0 as nivel,
        ta.id::TEXT as ruta
    FROM trabajos_adicionales ta
    WHERE ta.trabajo_adicional_padre_id IS NULL
    
    UNION ALL
    
    -- Niveles siguientes: trabajos adicionales hijos
    SELECT 
        ta.id,
        ta.nombre,
        ta.obra_id,
        ta.trabajo_adicional_padre_id,
        j.nivel + 1,
        j.ruta || ' → ' || ta.id::TEXT
    FROM trabajos_adicionales ta
    INNER JOIN jerarquia j ON ta.trabajo_adicional_padre_id = j.id
)
SELECT 
    j.nivel,
    j.ruta,
    j.id as trabajo_adicional_id,
    j.nombre as trabajo_adicional_nombre,
    o.nombre as obra_nombre,
    COUNT(p.id) as cantidad_presupuestos_tarea_leve,
    SUM(p.total_presupuesto_con_honorarios) as total
FROM jerarquia j
LEFT JOIN obras o ON j.obra_id = o.id
LEFT JOIN presupuesto_no_cliente p 
    ON p.trabajo_adicional_id = j.id 
    AND p.tipo_presupuesto = 'TAREA_LEVE'
GROUP BY j.nivel, j.ruta, j.id, j.nombre, o.nombre
ORDER BY j.nivel, j.id;
```

## 📦 DTO - PresupuestoNoClienteRequestDTO

```java
@Data
public class PresupuestoNoClienteRequestDTO {
    
    private Long idEmpresa; // Obligatorio
    private Long idCliente; // Opcional
    
    // Vinculación (mutuamente excluyente para TAREA_LEVE)
    private Long idObra; // Opcional para TAREA_LEVE (si no hay trabajoAdicionalId)
    
    @Schema(description = "ID del trabajo adicional (solo para TAREA_LEVE)", example = "100")
    private Long trabajoAdicionalId; // ⭐ NUEVO - Opcional para TAREA_LEVE (si no hay idObra)
    
    // Tipo de presupuesto
    private String tipoPresupuesto; // TRADICIONAL, TRABAJO_DIARIO, TRABAJO_EXTRA, TAREA_LEVE
    
    // Campos descriptivos
    private String nombreObra;
    private String direccionObraCalle;
    private String direccionObraAltura;
    
    // Datos del presupuesto
    private List<ProfesionalNecesarioDTO> profesionales;
    private List<MaterialDTO> materialesList;
    private BigDecimal totalPresupuesto;
    
    // ... otros campos
}
```

## 🧪 Casos de Prueba

### Test 1: Crear TAREA_LEVE vinculado a Obra
```bash
POST http://localhost:8080/api/presupuestos-no-cliente
Content-Type: application/json

{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": 123,
  "trabajoAdicionalId": null,
  "nombreObra": "Test Tarea Leve Obra",
  "direccionObraCalle": "Test",
  "direccionObraAltura": "123"
}
```
**Esperado**: `201 Created` con `obraId: 123, trabajoAdicionalId: null`

### Test 2: Crear TAREA_LEVE vinculado a TrabajoAdicional
```bash
POST http://localhost:8080/api/presupuestos-no-cliente
Content-Type: application/json

{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": null,
  "trabajoAdicionalId": 100,
  "nombreObra": "Test Tarea Leve TrabajoAdicional",
  "direccionObraCalle": "Test",
  "direccionObraAltura": "123"
}
```
**Esperado**: `201 Created` con `obraId: null, trabajoAdicionalId: 100`

### Test 3: Error - Ambos vínculos
```bash
POST http://localhost:8080/api/presupuestos-no-cliente
Content-Type: application/json

{
  "idEmpresa": 1,
  "tipoPresupuesto": "TAREA_LEVE",
  "idObra": 123,
  "trabajoAdicionalId": 100,
  "nombreObra": "Test Error",
  "direccionObraCalle": "Test",
  "direccionObraAltura": "123"
}
```
**Esperado**: `400 Bad Request` - "no pueden tener obraId Y trabajoAdicionalId simultáneamente"

## 📄 Archivos Modificados

### Backend (Java)
1. ✅ `PresupuestoNoCliente.java` - Agregada relación `@ManyToOne` con TrabajoAdicional
2. ✅ `TrabajoAdicional.java` - Agregada relación `@OneToMany` con PresupuestoNoCliente
3. ✅ `PresupuestoNoClienteRequestDTO.java` - Agregado campo `trabajoAdicionalId`
4. ✅ `PresupuestoNoClienteService.java` - Validaciones y mapeo para `trabajoAdicionalId`

### Base de Datos (SQL)
5. ✅ `migration_presupuestos_tarea_leve_trabajo_adicional.sql` - Script de migración completo

### Documentación
6. ✅ `PRESUPUESTOS_TAREA_LEVE_TRABAJO_ADICIONAL_README.md` - Este documento

## 🚀 Despliegue

### 1. Ejecutar Migración SQL
```bash
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f migration_presupuestos_tarea_leve_trabajo_adicional.sql
```

### 2. Reiniciar Aplicación
```bash
# Detener Spring Boot (si está corriendo)
# Compilar y ejecutar
./mvnw clean install
./mvnw spring-boot:run
```

### 3. Verificar
```bash
# Test endpoint
curl -X GET http://localhost:8080/api/trabajos-adicionales/1
```

## ✅ Checklist de Implementación

- [x] Agregar campo `trabajo_adicional_id` a `presupuesto_no_cliente`
- [x] Crear FK constraint y índice
- [x] Implementar constraint de validación (XOR)
- [x] Actualizar entidad `PresupuestoNoCliente`
- [x] Actualizar entidad `TrabajoAdicional`
- [x] Actualizar DTO `PresupuestoNoClienteRequestDTO`
- [x] Implementar validaciones en `PresupuestoNoClienteService`
- [x] Crear script de migración SQL
- [x] Crear vista de reporte `v_presupuestos_tarea_leve_vinculacion`
- [x] Documentar API y casos de uso
- [ ] Ejecutar migración en producción ⚠️
- [ ] Pruebas de integración ⚠️
- [ ] Actualizar frontend ⚠️

## 📞 Soporte

Si encuentras problemas:
1. Verificar que la migración SQL se ejecutó correctamente
2. Verificar logs de Spring Boot para errores de validación
3. Consultar vista `v_presupuestos_tarea_leve_vinculacion` para debugging

---

**Última actualización**: 2026-03-03  
**Versión**: 1.0.0
