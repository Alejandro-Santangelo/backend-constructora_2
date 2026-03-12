# INSTRUCCIONES FRONTEND - Enviar empresaId en peticiones

## 🎯 PROBLEMA ACTUAL

El backend YA ESTÁ CORREGIDO y filtra correctamente por empresaId, PERO solo si el frontend envía el parámetro.

**Si todavía ves datos de otras empresas** = El frontend NO está enviando `empresaId`.

---

## ✅ SOLUCIÓN: Enviar empresaId en TODAS las peticiones

### Dónde obtener el empresaId

El empresaId debe estar almacenado cuando el usuario hace login. Busca en tu código:

```javascript
// localStorage
const empresaId = localStorage.getItem('empresaId');

// sessionStorage  
const empresaId = sessionStorage.getItem('empresaId');

// Context/State de React
const { empresaId } = useAuth(); // o similar
```

---

## 📝 CAMBIOS NECESARIOS EN EL FRONTEND

### 1. Servicio de Obras (`obraService.js` o similar)

**ANTES (INCORRECTO):**
```javascript
export const obtenerObrasActivas = async () => {
  const response = await fetch('/api/obras/activas');
  return response.json();
};
```

**AHORA (CORRECTO):**
```javascript
export const obtenerObrasActivas = async (empresaId) => {
  const response = await fetch(`/api/obras/activas?empresaId=${empresaId}`);
  return response.json();
};
```

### 2. Servicio de Presupuestos

**ANTES (INCORRECTO):**
```javascript
export const obtenerPresupuestos = async () => {
  const response = await fetch('/api/presupuestos-no-cliente');
  return response.json();
};
```

**AHORA (CORRECTO):**
```javascript
export const obtenerPresupuestos = async (empresaId) => {
  const response = await fetch(`/api/presupuestos-no-cliente?empresaId=${empresaId}`);
  return response.json();
};
```

### 3. Servicio de Clientes

**ANTES (INCORRECTO):**
```javascript
export const obtenerClientes = async () => {
  const response = await fetch('/api/clientes/todos');
  return response.json();
};
```

**AHORA (CORRECTO):**
```javascript
export const obtenerClientes = async (empresaId) => {
  const response = await fetch(`/api/clientes/todos?empresaId=${empresaId}`);
  return response.json();
};
```

---

## 🔧 MÉTODO ALTERNATIVO: Interceptor Global de Axios

Si usas Axios, puedes agregar empresaId automáticamente a TODAS las peticiones:

```javascript
import axios from 'axios';

// Crear una instancia de axios
const api = axios.create({
  baseURL: '/api'
});

// Interceptor que agrega empresaId a todas las peticiones
api.interceptors.request.use(
  (config) => {
    const empresaId = localStorage.getItem('empresaId');
    
    if (empresaId) {
      // Como query param
      config.params = {
        ...config.params,
        empresaId: empresaId
      };
      
      // O como header (alternativamente)
      config.headers['empresaId'] = empresaId;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

export default api;
```

Luego en tus servicios:
```javascript
import api from './axiosConfig';

export const obtenerObrasActivas = async () => {
  // empresaId se agrega automáticamente
  const response = await api.get('/obras/activas');
  return response.data;
};
```

---

## 🔧 MÉTODO ALTERNATIVO: Fetch Wrapper

Si usas fetch nativo, crea un wrapper:

```javascript
// utils/api.js
export const apiFetch = async (url, options = {}) => {
  const empresaId = localStorage.getItem('empresaId');
  
  // Agregar empresaId como query param
  const separator = url.includes('?') ? '&' : '?';
  const fullUrl = empresaId ? `${url}${separator}empresaId=${empresaId}` : url;
  
  // Agregar empresaId como header
  const headers = {
    ...options.headers,
    'empresaId': empresaId
  };
  
  return fetch(fullUrl, {
    ...options,
    headers
  });
};
```

Luego en tus servicios:
```javascript
import { apiFetch } from './utils/api';

export const obtenerObrasActivas = async () => {
  const response = await apiFetch('/api/obras/activas');
  return response.json();
};
```

---

## 🧪 PRUEBA RÁPIDA EN CONSOLA DEL NAVEGADOR

Abre DevTools → Console y ejecuta:

```javascript
// 1. Verificar que tienes empresaId guardado
console.log('empresaId:', localStorage.getItem('empresaId'));

// 2. Hacer petición manual CON empresaId
fetch('/api/obras/activas?empresaId=1')
  .then(r => r.json())
  .then(obras => console.log('Obras empresa 1:', obras));

// 3. Repetir con empresaId diferente
fetch('/api/obras/activas?empresaId=2')
  .then(r => r.json())
  .then(obras => console.log('Obras empresa 2:', obras));
```

**Resultado esperado:** Deberías ver diferentes obras para cada empresaId.

---

## 📍 ARCHIVOS QUE PROBABLEMENTE NECESITAS MODIFICAR

Busca estos archivos en tu proyecto frontend:

```
frontend-constructora_2/
├── src/
│   ├── services/
│   │   ├── obraService.js       ← MODIFICAR: agregar empresaId a todas las funciones
│   │   ├── presupuestoService.js ← MODIFICAR: agregar empresaId a todas las funciones
│   │   ├── clienteService.js     ← MODIFICAR: agregar empresaId a todas las funciones
│   │   ├── materialService.js    ← VERIFICAR: si usa endpoints que necesitan empresaId
│   │   └── api.js               ← CREAR/MODIFICAR: interceptor global
│   ├── context/
│   │   └── AuthContext.js       ← VERIFICAR: que guarde empresaId en login
│   └── components/
│       ├── Obras/
│       │   └── ListaObras.jsx   ← VERIFICAR: que pase empresaId al servicio
│       └── Presupuestos/
│           └── ListaPresupuestos.jsx ← VERIFICAR: que pase empresaId al servicio
```

---

## 🚨 ADVERTENCIA: Componentes que llaman a servicios

Asegúrate de que los componentes pasen el empresaId:

**ANTES (INCORRECTO):**
```javascript
const ListaObras = () => {
  useEffect(() => {
    obtenerObrasActivas().then(setObras);
  }, []);
  
  return <div>...</div>;
};
```

**AHORA (CORRECTO):**
```javascript
const ListaObras = () => {
  const empresaId = localStorage.getItem('empresaId'); // o desde context
  
  useEffect(() => {
    if (empresaId) {
      obtenerObrasActivas(empresaId).then(setObras);
    }
  }, [empresaId]);
  
  return <div>...</div>;
};
```

---

## ✅ CHECKLIST FRONTEND

- [ ] Verificar que empresaId se guarda en login
- [ ] Modificar TODOS los servicios para incluir empresaId como parámetro
- [ ] Modificar TODOS los componentes para pasar empresaId a los servicios
- [ ] O ALTERNATIVAMENTE: Implementar interceptor global de Axios/Fetch
- [ ] Abrir DevTools → Network y verificar que las peticiones incluyen `?empresaId=X`
- [ ] Probar con 2 empresas diferentes y verificar que los datos son distintos
- [ ] Verificar en Railway logs que aparece `✅ EmpresaId obtenido del parámetro query`

---

## 🎯 PRÓXIMOS PASOS

1. **Implementa UNA de estas opciones:**
   - Opción A: Modificar cada servicio manualmente (más control)
   - Opción B: Interceptor global de Axios (más automático)
   - Opción C: Fetch wrapper global (si no usas Axios)

2. **Prueba localmente:**
   ```bash
   npm start
   # Abre http://localhost:3000 (o tu puerto)
   # Login con Empresa 1 → ver datos
   # Logout → Login con Empresa 2 → deberías ver OTROS datos
   ```

3. **Deploy a Railway/Vercel:**
   ```bash
   git add .
   git commit -m "fix: Agregar empresaId a todas las peticiones API"
   git push origin main
   ```

4. **Verifica en producción:**
   - Ingresa como Empresa A
   - Verifica que solo ves tus datos
   - Ingresa como Empresa B
   - Verifica que ves datos DIFERENTES
