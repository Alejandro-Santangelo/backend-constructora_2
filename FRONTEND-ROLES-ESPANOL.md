# 🎯 GUÍA: ACTUALIZACIÓN ROLES A ESPAÑOL EN FRONTEND

## ✅ BACKEND YA ACTUALIZADO

El backend ha sido completamente actualizado con roles en español. **BUILD SUCCESS**.

## 📋 CAMBIOS NECESARIOS EN FRONTEND

### Archivo: `frontend-constructora_2/src/pages/UsuariosPage.jsx`

#### 1. Actualizar array `rolesDisponibles`

**BUSCAR (línea ~20-30):**
```javascript
const rolesDisponibles = [
  'admin',
  'contratista',
  'manager',
  'arquitecto',
  'ingeniero',
  'maestro_obra',
  'empleado',
  'user',
  'viewer'
];
```

**REEMPLAZAR POR:**
```javascript
const rolesDisponibles = [
  'SUPER_ADMINISTRADOR',  // Super admin global
  'contratista',           // Admin de empresa
  'administrador',         // Admin genérico
  'gerente',              // Ex-manager
  'arquitecto',
  'ingeniero',
  'maestro_obra',
  'empleado',
  'usuario',              // Ex-user
  'visualizador'          // Ex-viewer
];
```

#### 2. Actualizar comparaciones de roles

**BUSCAR ALL (usar Ctrl+F):**
```javascript
rol === 'admin'
```
**REEMPLAZAR POR:**
```javascript
rol === 'administrador' || rol.toLowerCase() === 'contratista'
```

**BUSCAR:**
```javascript
rolActual === 'admin'
```
**REEMPLAZAR POR:**
```javascript
rolActual === 'administrador' || rolActual.toLowerCase() === 'contratista'
```

**BUSCAR:**
```javascript
isSuperAdmin = rolActual === 'SUPER_ADMIN'
```
**REEMPLAZAR POR:**
```javascript
isSuperAdmin = rolActual === 'SUPER_ADMINISTRADOR'
```

**BUSCAR:**
```javascript
isAdmin = isSuperAdmin || rolActual === 'admin' || rolActual === 'contratista'
```
**REEMPLAZAR POR:**
```javascript
isAdmin = isSuperAdmin || rolActual === 'administrador' || rolActual.toLowerCase() === 'contratista'
```

#### 3. Actualizar labels de roles (opcional pero recomendado)

Si hay un objeto de traducción de roles, actualizar:
```javascript
const rolesLabels = {
  'SUPER_ADMINISTRADOR': 'Super Administrador',
  'contratista': 'Contratista',
  'administrador': 'Administrador',
  'gerente': 'Gerente',
  'arquitecto': 'Arquitecto',
  'ingeniero': 'Ingeniero',
  'maestro_obra': 'Maestro de Obra',
  'empleado': 'Empleado',
  'usuario': 'Usuario',
  'visualizador': 'Visualizador'
};
```

### Otros archivos del frontend a revisar

#### `frontend-constructora_2/src/App.jsx` o `Routes.jsx`
Buscar comparaciones de rol para rutas protegidas:
- `rol === 'admin'` → `rol === 'administrador' || rol.toLowerCase() === 'contratista'`
- `rol === 'SUPER_ADMIN'` → `rol === 'SUPER_ADMINISTRADOR'`

#### `frontend-constructora_2/src/context/AuthContext.jsx`
Buscar lógica de permisos:
- `user.rol === 'SUPER_ADMIN'` → `user.rol === 'SUPER_ADMINISTRADOR'`

#### `frontend-constructora_2/src/components/*` (cualquier componente con lógica de roles)
Buscar todas las comparaciones y actualizar según el mapeo.

## 🔧 MAPEO COMPLETO DE ROLES

| Inglés (viejo)     | Español (nuevo)        | Descripción                    |
|--------------------|------------------------|--------------------------------|
| `SUPER_ADMIN`      | `SUPER_ADMINISTRADOR`  | Acceso total global            |
| `CONTRATISTA`      | `contratista`          | Admin de empresa (lowercase)   |
| `admin`            | `administrador`        | Admin genérico                 |
| `manager`          | `gerente`              | Gerente                        |
| `arquitecto`       | `arquitecto`           | Sin cambios                    |
| `ingeniero`        | `ingeniero`            | Sin cambios                    |
| `maestro_obra`     | `maestro_obra`         | Sin cambios                    |
| `empleado`         | `empleado`             | Sin cambios                    |
| `user`             | `usuario`              | Usuario básico                 |
| `viewer`           | `visualizador`         | Solo lectura                   |

## ⚠️ IMPORTANTE: CONSISTENCIA CASE-SENSITIVE

- `SUPER_ADMINISTRADOR`: **UPPERCASE** (único rol en mayúsculas)
- `contratista`: **lowercase** (importante para comparaciones)
- Resto de roles: **lowercase**

En comparaciones, usar `.toLowerCase()` o `.equalsIgnoreCase()` cuando sea necesario para robustez.

## 🗃️ MIGRACIÓN BASE DE DATOS

**Archivo ya creado:** `backend-constructora_2/migracion-roles-espanol.sql`

**Ejecutar ANTES de desplegar:**
```powershell
$env:PGPASSWORD = $env:PSQL_PASSWORD
psql -U $env:PSQL_USER -d construccion_app_v3 -f migracion-roles-espanol.sql
```

Esto actualizará todos los usuarios existentes en la BD:
- `SUPER_ADMIN` → `SUPER_ADMINISTRADOR`
- `CONTRATISTA` → `contratista`
- `admin` → `administrador`
- `manager` → `gerente`
- `user` → `usuario`
- `viewer` → `visualizador`

## ✅ CHECKLIST FINAL

- [ ] Ejecutar migración SQL en base de datos
- [ ] Actualizar `rolesDisponibles` en UsuariosPage.jsx
- [ ] Actualizar todas las comparaciones de rol en frontend
- [ ] Buscar `SUPER_ADMIN` en todo el frontend y reemplazar
- [ ] Buscar `'admin'` en comparaciones y actualizar
- [ ] Buscar `'user'` en comparaciones y actualizar
- [ ] Buscar `'manager'` en comparaciones y actualizar
- [ ] Buscar `'viewer'` en comparaciones y actualizar
- [ ] Testing: Login con PIN 3333 (SUPER_ADMINISTRADOR)
- [ ] Testing: Login con PIN 1111 (contratista empresa 1)
- [ ] Testing: Crear nuevo usuario con roles en español
- [ ] Verificar selector multi-empresa para SUPER_ADMINISTRADOR

## 🚀 ORDEN DE DEPLOYMENT

1. **Ejecutar migración SQL** (actualiza BD primero)
2. **Deploy backend** (ya compilado y testeado)
3. **Actualizar frontend** (según esta guía)
4. **Deploy frontend**
5. **Testing completo** (login, roles, multi-empresa)

---

**Fecha:** 13 de marzo de 2026  
**Estado Backend:** ✅ Compilado exitosamente (BUILD SUCCESS)  
**Estado Frontend:** ⏳ Pendiente de actualización
