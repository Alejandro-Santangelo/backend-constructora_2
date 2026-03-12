# Diagnóstico Multi-Tenancy - Por qué seguías viendo datos de otras empresas

## 🔴 PROBLEMAS ENCONTRADOS

### Problema 1: Endpoint `/obras/activas` sin filtro de empresaId
**ANTES:**
```java
@GetMapping("/activas")
public ResponseEntity<List<ObraSimpleDTO>> obtenerObrasActivas() {
    List<ObraSimpleDTO> obrasActivasDto = obraService.obtenerActivas();
    return ResponseEntity.ok(obrasActivasDto);
}
```

**Query ejecutada:**
```sql
SELECT o FROM Obra o WHERE o.estado = 'En obra'
-- ❌ NO filtra por empresaId
```

**Resultado:** Devolvía obras activas de TODAS las empresas, sin importar quién hiciera la consulta.

**AHORA (CORREGIDO):**
```java
@GetMapping("/activas")
public ResponseEntity<List<ObraSimpleDTO>> obtenerObrasActivas(
        @RequestParam Long empresaId) {
    List<ObraSimpleDTO> obrasActivasDto = obraService.obtenerActivasPorEmpresa(empresaId);
    return ResponseEntity.ok(obrasActivasDto);
}
```

**Nueva query:**
```sql
SELECT o FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas AND o.estado = 'En obra'
-- ✅ Filtra por empresaId correctamente
```

---

### Problema 2: HibernateFilterInterceptor no estaba habilitado explícitamente en Railway

El `@ConditionalOnProperty` del interceptor:
```java
@ConditionalOnProperty(name = "app.hibernate-filter-interceptor.enabled", 
                       havingValue = "true", 
                       matchIfMissing = true)
```

Aunque `matchIfMissing = true` debería activarlo por defecto, en Railway podría estar siendo desactivado por alguna configuración.

**SOLUCIÓN:** Agregada property explícita en `application-railway.properties`:
```properties
app.hibernate-filter-interceptor.enabled=true
```

---

### Problema 3: Dependencia del filtro Hibernate @Filter sin fallback

Varios servicios confiaban 100% en que el filtro Hibernate funcionara:
- `PresupuestoNoClienteService.listarTodos()`
- `PresupuestoNoClienteService.findAllByEmpresaId()`

Estos métodos hacen `repository.findAll()` esperando que el filtro @Filter intercepte automáticamente.

**Si el filtro falla:** Exponen TODOS los datos del sistema.

---

## ✅ CORRECCIONES IMPLEMENTADAS

### Commit 1: f84a3dd - Deprecados métodos sin empresaId
- `ProfesionalObraService`: 5 métodos deprecados
- `MovimientoMaterialService`: Métodos seguros agregados
- `PagoConsolidadoService`: Reemplazados findAll() por métodos filtrados

### Commit 2: 663ca33 - Fix crítico endpoint /obras/activas
- Endpoint ahora REQUIERE empresaId como parámetro
- Usa método del repository que filtra explícitamente
- HibernateFilterInterceptor habilitado en Railway

---

## 🔍 CÓMO VERIFICAR QUE FUNCIONA

### 1. Verificar que el frontend envía empresaId

**En el frontend, TODAS las peticiones deben incluir empresaId:**

Como parámetro query:
```javascript
fetch(`/api/obras/activas?empresaId=${empresaId}`)
```

O como header:
```javascript
fetch('/api/presupuestos-no-cliente', {
  headers: {
    'empresaId': empresaId,
    // o alternativamente:
    'X-Empresa-Id': empresaId
  }
})
```

### 2. Ver logs del backend en Railway

Busca en los logs de Railway estos mensajes:

✅ **Filtro activo:**
```
🔍 TenantFilter procesando: GET /api/obras/activas
✅ EmpresaId obtenido del parámetro query: 1
💾 TenantContext.setTenantId: empresaId=1
🎯 HibernateFilterInterceptor: Filtro 'empresaFilter' HABILITADO para empresaId=1
```

❌ **Filtro NO activo:**
```
⚠️ No se proporcionó empresaId en /api/obras/activas
⚠️ HibernateFilterInterceptor: No hay empresaId en TenantContext - filtro NO habilitado
```

### 3. Prueba manual en Railway

1. Ingresa a Empresa A (empresaId=1)
2. Ve a la lista de obras
3. Abre DevTools → Network
4. Busca la petición `GET /api/obras/activas?empresaId=1`
5. Verifica que SOLO devuelve obras de empresaId=1

Luego:
1. Ingresa a Empresa B (empresaId=2)
2. Repite el proceso
3. Verifica que devuelve DIFERENTES obras (de empresaId=2)

---

## ⚠️ ENDPOINTS QUE NECESITAN VERIFICACIÓN

Si todavía ves datos de otras empresas, verifica estos endpoints:

1. **GET /api/presupuestos-no-cliente** → Debe recibir `empresaId` como query param
2. **GET /api/clientes/todos** → Debe recibir `empresaId` como query param
3. **GET /api/obras/empresa/{empresaId}** → OK, el empresaId está en el path

---

## 🛡️ CAPAS DE SEGURIDAD ACTUALES

### Capa 1: TenantFilter (OncePerRequestFilter)
- Captura empresaId del header o query param
- Lo guarda en TenantContext (ThreadLocal)
- Se ejecuta ANTES de llegar al controller

### Capa 2: HibernateFilterInterceptor (AOP)
- Intercepta ANTES de cada método de repository
- Habilita el filtro `empresaFilter` con el empresaId del TenantContext
- Aplica el filtro a todas las queries Hibernate

### Capa 3: Filtro @Filter en entidades
- Cada entidad privada tiene `@Filter(name = "empresaFilter")`
- Inyecta automáticamente `WHERE empresa_id = :empresaId` en TODAS las queries

### Capa 4: Métodos de repository filtrados
- `findByEmpresaId(empresaId)`
- `findObrasActivasByEmpresaId(empresaId)`
- Filtran explícitamente en la query JPQL

### Capa 5: Controllers con @RequestParam
- Obligan a pasar empresaId como parámetro
- Validan que existe antes de ejecutar lógica

---

## 🚨 SI TODAVÍA VES DATOS DE OTRAS EMPRESAS

Ejecuta esto en Railway logs para diagnosticar:

```bash
# Ver si el filtro se está activando
railway logs | grep "HibernateFilterInterceptor"

# Ver si TenantContext recibe empresaId
railway logs | grep "TenantContext.setTenantId"

# Ver si hay warnings de métodos deprecated
railway logs | grep "DEPRECADO\|SEGURIDAD"
```

Si NO ves logs de `HibernateFilterInterceptor`:
- El interceptor NO se está ejecutando
- Verifica que `app.hibernate-filter-interceptor.enabled=true` esté en Railway environment variables

Si ves `⚠️ No se proporcionó empresaId`:
- El frontend NO está enviando empresaId
- Agrega empresaId como query param o header en TODAS las peticiones

---

## 📋 CHECKLIST POST-DEPLOY

- [ ] Railway deployment completado exitosamente
- [ ] Logs muestran `HibernateFilterInterceptor: Filtro 'empresaFilter' HABILITADO`
- [ ] Frontend envía empresaId en todas las peticiones
- [ ] Prueba manual: Empresa A no ve datos de Empresa B
- [ ] Prueba manual: Empresa B no ve datos de Empresa A
- [ ] Consulta directa en BD: `SELECT COUNT(*) FROM obras WHERE empresa_id = 1` coincide con UI
