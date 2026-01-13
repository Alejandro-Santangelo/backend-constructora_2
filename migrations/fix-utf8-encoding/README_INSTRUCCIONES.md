# Corrección de Double-Encoding UTF-8 en Base de Datos

## 📋 Descripción del Problema

La base de datos tiene caracteres corruptos debido a double-encoding UTF-8:
- `ðŸ'¡` debería ser 💡
- `Ã­` debería ser í
- `Ã³` debería ser ó
- Y otros casos similares

## 🎯 Objetivo

Corregir estos caracteres de forma segura y controlada, con posibilidad de rollback.

---

## 📝 INSTRUCCIONES PASO A PASO

### ⚠️ ANTES DE EMPEZAR

1. **Realizar backup manual de la base de datos completa** (usar `pg_dump` o herramienta de backup)
2. **Verificar que NO haya usuarios conectados** modificando datos
3. **Tener acceso a la consola SQL** (pgAdmin, DBeaver, psql, etc.)

---

### PASO 1: Generar Backup de las Tablas Afectadas

**Archivo:** `1_BACKUP_tablas_afectadas.sql`

```sql
-- Ejecutar este script COMPLETO
-- Guardar los resultados en un archivo .sql o .csv
```

**Acciones:**
1. Abrir el archivo `1_BACKUP_tablas_afectadas.sql`
2. Ejecutar TODO el contenido
3. Exportar los resultados a un archivo seguro (ej: `backup_utf8_fix_2026-01-13.sql`)
4. **Verificar que el archivo de backup se creó correctamente**
5. Guardar el backup en un lugar seguro (fuera del proyecto si es posible)

---

### PASO 2: Diagnóstico - Identificar Registros Afectados

**Archivo:** `2_DIAGNOSTICO_registros_corruptos.sql`

```sql
-- Este script SOLO hace SELECT
-- NO modifica ningún dato
```

**Acciones:**
1. Abrir el archivo `2_DIAGNOSTICO_registros_corruptos.sql`
2. Ejecutar TODO el contenido
3. Revisar los resultados:
   - ¿Cuántos registros están afectados?
   - ¿En qué tablas?
   - ¿Qué campos tienen problemas?
4. Anotar los IDs de algunos registros de prueba

**Ejemplo de salida esperada:**
```
materiales: 15 registros afectados
obras: 8 registros afectados
gastos_generales: 23 registros afectados
...
```

---

### PASO 3: Prueba en UN Solo Registro

**Archivo:** `4_PRUEBA_un_registro.sql`

**⚠️ IMPORTANTE: Probar primero en UN registro**

**Acciones:**
1. Del diagnóstico, elegir UN registro que tenga problemas
2. Abrir el archivo `4_PRUEBA_un_registro.sql`
3. Reemplazar el ID de ejemplo (999) con el ID real del registro
4. Ejecutar la sección correspondiente a la tabla
5. **Revisar el resultado ANTES y DESPUÉS**
6. Si está correcto: ejecutar `COMMIT;`
7. Si hay problemas: ejecutar `ROLLBACK;` y ajustar

**Ejemplo:**
```sql
BEGIN;

-- Ver ANTES
SELECT nombre FROM materiales WHERE id_material = 5;
-- Resultado: "ConstrucciÃ³n ðŸ"¦"

-- Ejecutar corrección...

-- Ver DESPUÉS
SELECT nombre FROM materiales WHERE id_material = 5;
-- Resultado esperado: "Construcción 📦"

COMMIT; -- Si está correcto
```

---

### PASO 4: Corrección Masiva (Después de Prueba Exitosa)

**Archivo:** `3_CORRECCION_double_encoding.sql`

**⚠️ SOLO EJECUTAR SI LA PRUEBA DEL PASO 3 FUE EXITOSA**

**Acciones:**
1. Abrir el archivo `3_CORRECCION_double_encoding.sql`
2. **Leer TODO el contenido antes de ejecutar**
3. Ejecutar TODO el script (comenzará con `BEGIN;`)
4. El script incluye una verificación automática al final
5. **Revisar los resultados de la verificación**
6. **DECISIÓN CRÍTICA:**
   - Si todos los contadores muestran `0`: descomentar `COMMIT;` y ejecutar
   - Si hay algún problema: descomentar `ROLLBACK;` y ejecutar

**Verificación:**
```sql
-- El script mostrará algo como:
materiales: 0
obras: 0
gastos_generales: 0
asig_cobro: 0
...

-- Si TODOS son 0, entonces está correcto
```

---

### PASO 5: Verificación Post-Corrección

**Acciones manuales:**
1. Conectarse a la aplicación web/frontend
2. Revisar algunos registros corregidos visualmente
3. Verificar que los emojis y caracteres especiales se vean correctamente
4. Comprobar que no haya caracteres extraños nuevos

**Consulta de verificación adicional:**
```sql
-- Buscar si quedó algún patrón de double-encoding
SELECT table_name, column_name 
FROM information_schema.columns 
WHERE table_schema = 'public' 
  AND data_type IN ('text', 'character varying');

-- Revisar manualmente algunas de estas columnas
```

---

## 🔄 Orden de Ejecución

```
1. 1_BACKUP_tablas_afectadas.sql       → Guardar resultados
2. 2_DIAGNOSTICO_registros_corruptos.sql → Revisar resultados
3. 4_PRUEBA_un_registro.sql            → Probar en UN registro
   ├─ Si OK → COMMIT
   └─ Si falla → ROLLBACK y revisar
4. 3_CORRECCION_double_encoding.sql    → Solo si paso 3 fue exitoso
   ├─ Revisar verificación automática
   ├─ Si todo OK → COMMIT
   └─ Si hay problemas → ROLLBACK
5. Verificación manual en la aplicación
```

---

## ⚠️ Advertencias Importantes

1. **NO ejecutar en horario de producción** con usuarios activos
2. **Siempre tener el backup listo** antes de ejecutar correcciones
3. **NO saltar el paso de prueba unitaria** (paso 3)
4. **Si algo sale mal, hacer ROLLBACK inmediatamente**
5. **Verificar que la conexión SQL tenga encoding UTF-8** correcto

---

## 🆘 Plan de Contingencia

Si algo sale mal:

### Durante la corrección (antes del COMMIT):
```sql
ROLLBACK; -- Deshacer todos los cambios
```

### Después del COMMIT (si se detecta problema post-corrección):
1. Detener la aplicación inmediatamente
2. Restaurar desde el backup del PASO 1
3. Revisar qué salió mal
4. Ajustar los scripts de corrección
5. Volver a intentar

---

## 📊 Tablas y Campos Afectados

| Tabla | Campos Corregidos |
|-------|------------------|
| `materiales` | `nombre`, `descripcion` |
| `obras` | `nombre`, `descripcion`, `direccion_obra_calle`, `direccion_obra_barrio`, `observaciones` |
| `gastos_generales` | `nombre`, `descripcion`, `categoria` |
| `asignaciones_cobro_obra` | `observaciones` |
| `asignaciones_gasto_general_obra` | `concepto`, `observaciones`, `motivo_anulacion` |
| `asignaciones_material_obra` | `observaciones` |
| `asignaciones_otro_costo_obra` | `observaciones` |

---

## 🔧 Caracteres Corregidos

```
ðŸ"¦ → 📦
ðŸ"‹ → 📋
ðŸ'¡ → 💡
Ã­  → í
Ã³  → ó
Ã±  → ñ
Ã¡  → á
Ã©  → é
Ãº  → ú
Â²  → ²
Â³  → ³
Ã  → Ñ
```

---

## ✅ Checklist de Ejecución

- [ ] Backup manual de la base de datos completa realizado
- [ ] Script 1 (BACKUP) ejecutado y resultados guardados
- [ ] Script 2 (DIAGNÓSTICO) ejecutado y resultados revisados
- [ ] Script 4 (PRUEBA) ejecutado con éxito en UN registro
- [ ] Script 3 (CORRECCIÓN) revisado completamente
- [ ] Script 3 ejecutado y verificación automática revisada
- [ ] COMMIT ejecutado (solo si verificación fue exitosa)
- [ ] Verificación manual en la aplicación completada
- [ ] Documentar los cambios realizados

---

## 📞 Soporte

Si tienes dudas o problemas:
1. **NO ejecutar el siguiente paso** hasta resolver
2. Revisar este README completamente
3. Verificar logs de PostgreSQL
4. Consultar con el equipo técnico si es necesario

---

**Fecha de creación:** 13 de enero de 2026  
**Versión:** 1.0  
**Autor:** Sistema de Corrección UTF-8
