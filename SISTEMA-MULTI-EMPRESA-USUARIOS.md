# 🏢 Sistema Multi-Empresa para Usuarios

## ✅ Implementación Completada

**Fecha:** 13 de marzo de 2026  
**Versión:** 1.0

---

## 📋 ¿Qué se Implementó?

Se agregó la capacidad de que un usuario pueda tener acceso a **múltiples empresas** simultáneamente, en lugar de estar limitado a una sola empresa.

### Casos de Uso:

1. **Usuario Normal** (empleado, arquitecto, etc.)
   - Acceso a **1 empresa** específica
   - No puede cambiar sus permisos de empresa

2. **Usuario Multi-Empresa** (nuevo)
   - Acceso a **varias empresas** seleccionadas (ej: empresas 1, 3, 5)
   - Ve datos solo de esas empresas
   - Puede cambiar entre empresas desde el frontend

3. **SUPER_ADMIN**
   - Acceso a **TODAS las empresas** automáticamente
   - Puede crear usuarios con acceso multi-empresa
   - No necesita configurar empresas permitidas

---

## 🔧 Pasos para Activar el Sistema

### 1️⃣ Ejecutar Migración de Base de Datos

**IMPORTANTE:** Hacer backup antes de ejecutar.

```bash
# Conectarse a la base de datos PostgreSQL
psql -U tu_usuario -d tu_base_de_datos

# Ejecutar el script de migración
\i migracion-multi-empresa-usuarios.sql
```

O desde tu cliente SQL favorito (DBeaver, pgAdmin, etc.):
```
Abrir: backend-constructora_2/migracion-multi-empresa-usuarios.sql
Ejecutar todo el contenido
```

**Resultado esperado:**
- Tabla `usuario_empresas_permitidas` creada
- Usuarios existentes migrados automáticamente
- Cada usuario actual tendrá acceso solo a su empresa actual

### 2️⃣ Reiniciar el Backend

```powershell
cd backend-constructora_2
./mvnw spring-boot:run
```

### 3️⃣ Verificar que Funciona

1. Loguéate como **SUPER_ADMIN** (PIN: 3333)
2. Ve a la página **Usuarios**
3. Haz clic en **"Crear Usuario"**
4. Deberías ver un campo nuevo: **"Empresas Permitidas"** con un multi-select

---

## 🎯 Cómo Usar el Sistema

### Como SUPER_ADMIN - Crear Usuario Multi-Empresa

1. **Login con SUPER_ADMIN** (PIN: 3333)

2. **Ir a Usuarios → Crear Usuario**

3. **Llenar el formulario:**
   - Nombre: "Juan Pérez"
   - Email: "juan@ejemplo.com"
   - Rol: "manager" (o el que necesites)
   - PIN: "5678"
   - Confirmar PIN: "5678"
   
4. **Seleccionar Empresas Permitidas:**
   - Mantén presionado `Ctrl` (Windows) o `Cmd` (Mac)
   - Haz click en cada empresa que quieras darle acceso
   - Ejemplo: Seleccionar "Empresa Gisel" y "Empresa Construcciones SRL"

5. **Crear Usuario**

6. **Resultado:** El usuario Juan podrá:
   - Loguearse con PIN 5678
   - Ver en el selector de empresas: "Empresa Gisel" y "Empresa Construcciones SRL"
   - Cambiar entre ambas empresas
   - No ver datos de otras empresas

### Como CONTRATISTA/Admin - Crear Usuario Simple

1. **Login con tu PIN** (ej: 1111 para Empresa Gisel)

2. **Ir a Usuarios → Crear Usuario**

3. **NO verás el selector multi-empresa** (solo SUPER_ADMIN lo ve)

4. **El usuario creado tendrá acceso solo a TU empresa**

---

## 🔍 Verificar Empresas de un Usuario

### Desde la Base de Datos:

```sql
-- Ver empresas de un usuario específico
SELECT 
    u.id_usuario,
    u.nombre,
    u.rol,
    u.id_empresa AS empresa_principal,
    STRING_AGG(uep.empresa_id::TEXT, ', ' ORDER BY uep.empresa_id) AS empresas_permitidas
FROM usuarios u
LEFT JOIN usuario_empresas_permitidas uep ON u.id_usuario = uep.usuario_id
WHERE u.id_usuario = 1 -- Cambiar por el ID del usuario
GROUP BY u.id_usuario, u.nombre, u.rol, u.id_empresa;
```

### Desde el Frontend:

1. Login con el usuario
2. El selector de empresas mostrará solo las empresas a las que tiene acceso
3. Si solo ve 1 empresa → usuario simple
4. Si ve varias empresas → usuario multi-empresa
5. Si ve todas las empresas → SUPER_ADMIN

---

## 🛠️ Gestión Avanzada (SQL)

### Agregar Acceso a una Empresa

```sql
-- Dar acceso al usuario ID=1 a la empresa ID=3
INSERT INTO usuario_empresas_permitidas (usuario_id, empresa_id) 
VALUES (1, 3);
```

### Quitar Acceso a una Empresa

```sql
-- Quitar acceso del usuario ID=1 a la empresa ID=3
DELETE FROM usuario_empresas_permitidas 
WHERE usuario_id = 1 AND empresa_id = 3;
```

### Ver Todos los Usuarios y Sus Empresas

```sql
SELECT 
    u.id_usuario,
    u.nombre,
    u.email,
    u.rol,
    COUNT(uep.empresa_id) AS total_empresas,
    STRING_AGG(e.nombre_empresa, ', ') AS lista_empresas
FROM usuarios u
LEFT JOIN usuario_empresas_permitidas uep ON u.id_usuario = uep.usuario_id
LEFT JOIN empresas e ON uep.empresa_id = e.id_empresa
GROUP BY u.id_usuario, u.nombre, u.email, u.rol
ORDER BY u.id_usuario;
```

---

## 🔐 Jerarquía de Roles

### SUPER_ADMIN
- ✅ Ve **todas las empresas** automáticamente
- ✅ Puede crear usuarios multi-empresa
- ✅ Puede asignar cualquier rol
- ✅ Acceso total al sistema

### CONTRATISTA / admin
- ✅ Ve **todas las empresas permitidas** en su configuración
- ✅ Puede crear usuarios en SU empresa
- ✅ Los usuarios creados solo tienen acceso a SU empresa
- ❌ No puede crear usuarios multi-empresa

### manager, arquitecto, ingeniero, maestro_obra, empleado, user, viewer
- ✅ Ve solo **las empresas permitidas** en su configuración
- ❌ No puede crear usuarios
- ❌ Solo puede editar su propio perfil

---

## 📊 Ejemplos de Configuración

### Caso 1: Arquitecto que trabaja en 2 empresas

**Usuario:** María López  
**Rol:** arquitecto  
**Empresas:** 1 (Gisel) y 2 (Construcciones SRL)

```sql
-- Verificar configuración
SELECT * FROM usuario_empresas_permitidas WHERE usuario_id = 4; -- ID de María
-- Resultado:
-- usuario_id | empresa_id
-- -----------+-----------
--     4      |     1
--     4      |     2
```

**Comportamiento:**
- María puede ver presupuestos de ambas empresas
- Puede cambiar entre empresa 1 y 2 desde el selector
- NO puede ver datos de empresa 3

### Caso 2: Gerente General (acceso a todas menos una)

**Usuario:** Carlos Rodríguez  
**Rol:** manager  
**Empresas:** 1, 2, 3 (todas menos la 4)

```sql
-- Configurar
INSERT INTO usuario_empresas_permitidas (usuario_id, empresa_id) VALUES
(5, 1), (5, 2), (5, 3);
```

### Caso 3: Usuario Simple (solo su empresa)

**Usuario:** Pedro Gómez  
**Rol:** empleado  
**Empresa:** 1 (Gisel)

```sql
-- Solo tiene un registro
SELECT * FROM usuario_empresas_permitidas WHERE usuario_id = 6;
-- usuario_id | empresa_id
-- -----------+-----------
--     6      |     1
```

---

## ⚠️ Notas Importantes

1. **Empresa Principal (`id_empresa`):**
   - Se mantiene por compatibilidad con código existente
   - Es la empresa "base" del usuario
   - Siempre debe estar incluida en `empresas_permitidas`

2. **SUPER_ADMIN:**
   - NO necesita registros en `usuario_empresas_permitidas`
   - Lista vacía = acceso a todas las empresas

3. **Migración:**
   - Todos los usuarios existentes fueron migrados
   - Cada uno tiene acceso solo a su empresa actual
   - Puedes modificar manualmente después

4. **Performance:**
   - La tabla usa `EAGER` fetch para cargar empresas rápidamente
   - Índices creados para consultas eficientes

---

## 🐛 Troubleshooting

### El usuario no ve ninguna empresa

**Solución:**
```sql
-- Verificar que tenga al menos su empresa principal
INSERT INTO usuario_empresas_permitidas (usuario_id, empresa_id)
SELECT id_usuario, id_empresa
FROM usuarios
WHERE id_usuario = [ID_DEL_USUARIO]
  AND id_empresa IS NOT NULL;
```

### SUPER_ADMIN no ve todas las empresas

**Verificar:**
1. Que el rol sea exactamente "SUPER_ADMIN" (case sensitive)
2. Limpiar caché del navegador
3. Logout y login nuevamente

### Usuario multi-empresa solo ve una empresa

**Verificar en DB:**
```sql
SELECT * FROM usuario_empresas_permitidas WHERE usuario_id = [ID];
```

Si solo tiene 1 registro, agregar más:
```sql
INSERT INTO usuario_empresas_permitidas (usuario_id, empresa_id) 
VALUES ([ID_USUARIO], [ID_EMPRESA]);
```

---

## 📚 Archivos Modificados

### Backend:
1. `migracion-multi-empresa-usuarios.sql` - Script de migración (NUEVO)
2. `Usuario.java` - Agregado campo `empresasPermitidas` y métodos auxiliares
3. `UsuarioService.java` - Método `crearUsuarioMultiEmpresa()`
4. `AuthService.java` - Login retorna empresas según permisos
5. `UsuarioController.java` - Endpoint acepta parámetro `empresasPermitidas`

### Frontend:
1. `UsuariosPage.jsx` - Agregado multi-select de empresas para SUPER_ADMIN

---

## ✅ Testing

### Test Manual:

1. **Crear usuario multi-empresa:**
   - Login como SUPER_ADMIN
   - Crear usuario con 2-3 empresas
   - Logout

2. **Login con nuevo usuario:**
   - Verificar que ve solo las empresas seleccionadas
   - Cambiar entre empresas
   - Verificar que los datos filtran correctamente

3. **Crear usuario normal:**
   - Login como CONTRATISTA
   - Crear usuario (sin multi-select)
   - Verificar que solo tiene acceso a 1 empresa

---

## 🎉 ¡Sistema Listo!

El sistema multi-empresa está **completamente funcional**. Puedes empezar a usarlo inmediatamente después de ejecutar la migración de base de datos.

**¿Dudas o problemas?** Revisa la sección de Troubleshooting o consulta los ejemplos SQL en el archivo de migración.
