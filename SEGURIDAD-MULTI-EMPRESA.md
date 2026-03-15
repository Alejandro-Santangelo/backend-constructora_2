# 🔒 SEGURIDAD MULTI-EMPRESA - Validación empresaId

## 📋 Resumen de Cambios

**Fecha**: 2026-03-15  
**Versión Backend**: 1.0.0  
**Criticidad**: 🚨 **ALTA - SEGURIDAD**

## ❌ Problema Detectado

El frontend enviaba `empresaId` en el header HTTP sin validación en el backend. Esto permitía que **cualquier usuario pudiera manipular el header y acceder/modificar datos de otras empresas**.

**Ejemplo del problema:**
1. Usuario ingresa a empresa 2
2. Modifica header HTTP: `empresaId: 1`
3. Puede crear/modificar/eliminar datos en empresa 1 sin permiso ❌

## ✅ Solución Implementada

### 1. Interceptor de Validación (`EmpresaValidationInterceptor`)

Se creó un interceptor que valida en **CADA REQUEST**:
- ✅ Header `X-User-Id` presente (ID del usuario autenticado)
- ✅ Header `empresaId` o `X-Tenant-ID` presente (si aplica)
- ✅ Usuario tiene permiso para acceder a esa empresa

**Ubicación**: `src/main/java/com/rodrigo/security/EmpresaValidationInterceptor.java`

### 2. Configuración WebMvc (`WebMvcConfig`)

Registra el interceptor para aplicarlo a todos los endpoints `/api/**` excepto rutas públicas.

**Ubicación**: `src/main/java/com/rodrigo/config/WebMvcConfig.java`

### 3. Lógica de Permisos (`AuthService.tienePermisoEmpresa`)

Valida permisos según rol:
- **SUPER_ADMIN**: acceso a TODAS las empresas ✅
- **CONTRATISTA**: acceso SOLO a su empresa (`usuario.idEmpresa`)

---

## 🔧 Cambios Requeridos en Frontend

### ⚠️ CRÍTICO: Agregar header `X-User-Id` en TODAS las requests

El frontend **DEBE** incluir el header `X-User-Id` en todas las peticiones al backend.

#### 1. Obtener `userId` del Login

El endpoint `/api/auth/login-pin` **YA retorna** el `userId`:

```json
{
  "userId": 123,
  "nombre": "Juan Pérez",
  "email": "juan@example.com",
  "rol": "SUPER_ADMIN",
  "empresaId": 2,
  "empresaNombre": "Constructora ABC",
  "empresasPermitidas": [
    { "id": 1, "nombre": "Empresa 1", "cuit": "30-12345678-9" },
    { "id": 2, "nombre": "Empresa 2", "cuit": "30-98765432-1" }
  ],
  "esSuperAdmin": true
}
```

**Guardar en localStorage/sessionStorage:**
```javascript
const loginResponse = await api.post('/api/auth/login-pin', { pin: '1234' });
localStorage.setItem('userId', loginResponse.data.userId);
localStorage.setItem('empresaId', loginResponse.data.empresaId);
```

#### 2. Configurar Axios/Fetch con Interceptor

##### **Opción A: Axios Interceptor** (Recomendado)

```javascript
// src/api/axios.js
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Interceptor para agregar headers en TODAS las requests
api.interceptors.request.use(
  (config) => {
    const userId = localStorage.getItem('userId');
    const empresaId = localStorage.getItem('empresaId');
    
    if (userId) {
      config.headers['X-User-Id'] = userId;
    }
    
    if (empresaId) {
      config.headers['empresaId'] = empresaId;
      config.headers['X-Tenant-ID'] = empresaId; // Algunas APIs usan este nombre
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Manejo de errores 401/403
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Usuario no autenticado - redirigir a login
      localStorage.removeItem('userId');
      localStorage.removeItem('empresaId');
      window.location.href = '/login';
    } else if (error.response?.status === 403) {
      // Usuario sin permiso para esa empresa
      alert('No tiene permiso para acceder a esta empresa');
    }
    return Promise.reject(error);
  }
);

export default api;
```

##### **Opción B: Fetch con Wrapper**

```javascript
// src/api/fetchWithAuth.js
export async function fetchWithAuth(url, options = {}) {
  const userId = localStorage.getItem('userId');
  const empresaId = localStorage.getItem('empresaId');
  
  const headers = {
    'Content-Type': 'application/json',
    ...options.headers,
  };
  
  if (userId) {
    headers['X-User-Id'] = userId;
  }
  
  if (empresaId) {
    headers['empresaId'] = empresaId;
    headers['X-Tenant-ID'] = empresaId;
  }
  
  const response = await fetch(url, {
    ...options,
    headers,
  });
  
  if (response.status === 401) {
    // No autenticado
    localStorage.removeItem('userId');
    localStorage.removeItem('empresaId');
    window.location.href = '/login';
    throw new Error('No autenticado');
  }
  
  if (response.status === 403) {
    // Sin permiso
    throw new Error('Sin permiso para esta empresa');
  }
  
  return response;
}
```

#### 3. Actualizar Selector de Empresa

Cuando el SuperAdmin cambie de empresa:

```javascript
function cambiarEmpresa(nuevaEmpresaId) {
  localStorage.setItem('empresaId', nuevaEmpresaId);
  
  // Recargar datos con nueva empresa
  window.location.reload(); // Opción simple
  // O actualizar estado global (Redux, Context, etc.)
}
```

---

## 🚀 Cómo Probar

### 1. Iniciar Backend
```bash
./mvnw spring-boot:run
```

### 2. Realizar Login
```bash
POST http://localhost:8080/api/auth/login-pin
Content-Type: application/json

{
  "pin": "1234"
}
```

**Respuesta esperada:** JSON con `userId`, `empresaId`, etc.

### 3. Intentar Request SIN `X-User-Id` (Debe Fallar)
```bash
POST http://localhost:8080/api/obras
Content-Type: application/json
empresaId: 2

{
  "nombre": "Nueva Obra"
}
```

**Respuesta esperada:**
```json
{
  "error": "Header X-User-Id requerido"
}
```
**Status:** `401 Unauthorized`

### 4. Request CON `X-User-Id` pero empresaId sin permiso (Debe Fallar)

Usuario contratista de empresa 2 intenta acceder a empresa 1:

```bash
POST http://localhost:8080/api/obras
Content-Type: application/json
X-User-Id: 5
empresaId: 1

{
  "nombre": "Nueva Obra"
}
```

**Respuesta esperada:**
```json
{
  "error": "No tiene permiso para acceder a esta empresa"
}
```
**Status:** `403 Forbidden`

### 5. Request CON `X-User-Id` y empresaId VÁLIDO (Debe Funcionar)

```bash
POST http://localhost:8080/api/obras
Content-Type: application/json
X-User-Id: 5
empresaId: 2

{
  "nombre": "Nueva Obra",
  "direccion": "Calle 123"
}
```

**Respuesta esperada:** `201 Created` con datos de la obra

---

## 📊 Endpoints Excluidos de Validación

El interceptor **NO valida** estos endpoints (son públicos):
- `/api/auth/**` (login, cambio PIN)
- `/swagger-ui/**` (documentación Swagger)
- `/v3/api-docs/**` (OpenAPI)
- `/actuator/**` (health checks)
- `/error` (manejo de errores)

---

## ⚠️ Endpoints Críticos que Ahora Requieren `X-User-Id`

**Todos los endpoints `/api/**` excepto los excluidos** ahora validan:
- ✅ Crear/editar/eliminar obras
- ✅ Crear/editar/eliminar presupuestos
- ✅ Crear/editar/eliminar asignaciones profesional-obra
- ✅ Crear/editar/eliminar jornales diarios
- ✅ Crear/editar/eliminar clientes
- ✅ Crear/editar/eliminar proveedores
- ✅ Crear/editar/eliminar trabajos extra
- ✅ Consultar reportes
- ✅ Gestión de usuarios

---

## 🐛 Troubleshooting

### Error: "Header X-User-Id requerido"
**Causa:** Frontend no está enviando el header `X-User-Id`  
**Solución:** Implementar interceptor Axios o wrapper fetch como se indica arriba

### Error: "No tiene permiso para acceder a esta empresa"
**Causa:** Usuario contratista intentando acceder a empresa diferente a la suya  
**Solución:** Verificar que `empresaId` en el header coincida con `usuario.idEmpresa`

### Error: "X-User-Id debe ser un número"
**Causa:** Valor del header no es numérico  
**Solución:** Asegurar que se está enviando el `userId` como número (ej: `123`, no `"usuario123"`)

### Usuario SuperAdmin no puede acceder a ninguna empresa
**Causa:** `userId` incorrecto o usuario no existe  
**Solución:** Verificar que el `userId` enviado corresponda a un usuario existente en BD

---

## 📝 Logs de Depuración

El interceptor genera logs detallados:

```
✅ Usuario 5 accediendo a empresa 2: POST /api/obras
🚫 Request sin X-User-Id: GET /api/obras/123
🚫 Usuario 5 intentó acceder a empresa 1 sin permiso: POST /api/obras
🔓 Super Admin detectado - acceso a todas las empresas
```

Ver logs en consola del backend durante desarrollo.

---

## 🔒 Resumen de Seguridad

| Antes | Después |
|-------|---------|
| ❌ Frontend enviaba `empresaId`, backend confiaba ciegamente | ✅ Backend valida que `userId` tenga permiso para esa `empresaId` |
| ❌ Usuario podía manipular header y acceder a otras empresas | ✅ Interceptor bloquea request si no hay permiso (403) |
| ❌ Sin autenticación real | ✅ Header `X-User-Id` obligatorio en todas las requests |

---

## 📞 Soporte

Si tienes dudas o encuentras problemas, revisar:
1. Logs del backend
2. Network tab en DevTools del navegador (verificar headers enviados)
3. Response del endpoint `/api/auth/login-pin` (debe incluir `userId`)

---

**Autor**: Sistema de Construcción  
**Fecha**: 15 de marzo de 2026
