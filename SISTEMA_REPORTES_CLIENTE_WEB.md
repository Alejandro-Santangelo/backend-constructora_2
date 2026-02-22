# 📊 Sistema de Reportes y Backups - Documentación Completa

## 🎯 Descripción General

Sistema automático para gestionar reportes de auditoría de integridad de datos y backups de base de datos, accesibles desde la aplicación web.

## 📁 Estructura del Sistema

```
backend-constructora_2/
├── reportes-auditoria/           # Reportes HTML de auditorías semanales
│   ├── auditoria_2026-02-22_14-17-05.html
│   ├── auditoria_2026-02-29_09-00-00.html
│   └── ...
├── db-backups/                   # Backups de base de datos (.sql)
│   ├── backup_construccion_app_v3_2026-02-22_14-30-00.sql
│   ├── backup_construccion_app_v3_2026-02-29_09-00-00.sql
│   └── ...
├── auditoria_datos_huerfanos_clean.sql      # Script SQL de auditoría
├── ejecutar_auditoria_semanal.ps1           # Ejecuta auditoría y genera reporte
├── backup-bd-construccion-app.ps1           # Crea backup de BD
└── SISTEMA_REPORTES_CLIENTE_WEB.md          # Este archivo
```

## 🔧 Componentes del Backend

### 1. DTOs (Objetos de Transferencia de Datos)

#### `ReporteArchivoDTO.java`
```java
{
  "nombre": "auditoria_2026-02-22_14-17-05.html",
  "tipo": "AUDITORIA",           // "AUDITORIA" o "BACKUP"
  "tamanoBytes": 45678,
  "tamanoLegible": "44.61 KB",
  "fechaCreacion": "2026-02-22T14:17:05",
  "rutaRelativa": "auditoria_2026-02-22_14-17-05.html"
}
```

#### `ReportesResponseDTO.java`
```java
{
  "auditorias": [ /* array de ReporteArchivoDTO */ ],
  "backups": [ /* array de ReporteArchivoDTO */ ],
  "totalAuditorias": 12,
  "totalBackups": 8
}
```

### 2. Servicio: `ReporteSistemaService.java`

**Responsabilidades:**
- Listar archivos de auditorías y backups
- Proporcionar archivos para descarga
- Limpiar archivos antiguos (mantener N más recientes)
- Formatear tamaños de archivo
- Validar seguridad de acceso a archivos

**Métodos principales:**
- `obtenerTodosLosReportes()` - Lista todo
- `obtenerAuditorias()` - Solo auditorías
- `obtenerBackups()` - Solo backups
- `obtenerArchivo(tipo, nombre)` - Descarga archivo específico
- `limpiarReportesAntiguos(n)` - Limpieza automática

### 3. Controlador REST: `ReporteSistemaController.java`

Expone endpoints para el frontend.

---

## 🌐 API REST - Endpoints Disponibles

### 📋 **GET** `/api/reportes-sistema`
Obtiene todos los reportes disponibles (auditorías + backups)

**Respuesta exitosa (200):**
```json
{
  "auditorias": [
    {
      "nombre": "auditoria_2026-02-22_14-17-05.html",
      "tipo": "AUDITORIA",
      "tamanoBytes": 45678,
      "tamanoLegible": "44.61 KB",
      "fechaCreacion": "2026-02-22T14:17:05",
      "rutaRelativa": "auditoria_2026-02-22_14-17-05.html"
    }
  ],
  "backups": [
    {
      "nombre": "backup_construccion_app_v3_2026-02-22_14-30-00.sql",
      "tipo": "BACKUP",
      "tamanoBytes": 2456789,
      "tamanoLegible": "2.34 MB",
      "fechaCreacion": "2026-02-22T14:30:00",
      "rutaRelativa": "backup_construccion_app_v3_2026-02-22_14-30-00.sql"
    }
  ],
  "totalAuditorias": 12,
  "totalBackups": 8
}
```

**Ejemplo de uso (Frontend):**
```javascript
// React/Vue/Angular
const response = await fetch('http://localhost:8080/api/reportes-sistema');
const datos = await response.json();

console.log(`Total auditorías: ${datos.totalAuditorias}`);
console.log(`Total backups: ${datos.totalBackups}`);

// Mostrar lista de auditorías
datos.auditorias.forEach(audit => {
  console.log(`${audit.nombre} - ${audit.tamanoLegible} - ${audit.fechaCreacion}`);
});
```

---

### 📊 **GET** `/api/reportes-sistema/auditorias`
Obtiene solo los reportes de auditoría

**Respuesta exitosa (200):**
```json
[
  {
    "nombre": "auditoria_2026-02-22_14-17-05.html",
    "tipo": "AUDITORIA",
    "tamanoBytes": 45678,
    "tamanoLegible": "44.61 KB",
    "fechaCreacion": "2026-02-22T14:17:05",
    "rutaRelativa": "auditoria_2026-02-22_14-17-05.html"
  }
]
```

**Ejemplo de uso:**
```javascript
const response = await fetch('http://localhost:8080/api/reportes-sistema/auditorias');
const auditorias = await response.json();
```

---

### 💾 **GET** `/api/reportes-sistema/backups`
Obtiene solo los backups de base de datos

**Respuesta exitosa (200):**
```json
[
  {
    "nombre": "backup_construccion_app_v3_2026-02-22_14-30-00.sql",
    "tipo": "BACKUP",
    "tamanoBytes": 2456789,
    "tamanoLegible": "2.34 MB",
    "fechaCreacion": "2026-02-22T14:30:00",
    "rutaRelativa": "backup_construccion_app_v3_2026-02-22_14-30-00.sql"
  }
]
```

**Ejemplo de uso:**
```javascript
const response = await fetch('http://localhost:8080/api/reportes-sistema/backups');
const backups = await response.json();
```

---

### ⬇️ **GET** `/api/reportes-sistema/descargar/{tipo}/{nombreArchivo}`
Descarga un archivo específico (fuerza descarga)

**Parámetros:**
- `{tipo}`: "AUDITORIA" o "BACKUP"
- `{nombreArchivo}`: nombre exacto del archivo

**Respuesta exitosa (200):**
- Archivo binario para descarga
- Header `Content-Disposition: attachment`

**Códigos de error:**
- `400 Bad Request` - Tipo inválido
- `403 Forbidden` - Intento acceso no autorizado (path traversal)
- `404 Not Found` - Archivo no existe
- `500 Internal Server Error` - Error del servidor

**Ejemplo de uso (Frontend):**
```javascript
// Función para descargar archivo
function descargarArchivo(tipo, nombreArchivo) {
  const url = `http://localhost:8080/api/reportes-sistema/descargar/${tipo}/${nombreArchivo}`;
  
  // Opción 1: Abrir en nueva pestaña (deja que el navegador descargue)
  window.open(url, '_blank');
  
  // Opción 2: Descarga programática
  fetch(url)
    .then(response => response.blob())
    .then(blob => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = nombreArchivo;
      document.body.appendChild(a);
      a.click();
      window.URL.revokeObjectURL(url);
      document.body.removeChild(a);
    });
}

// Uso
descargarArchivo('AUDITORIA', 'auditoria_2026-02-22_14-17-05.html');
descargarArchivo('BACKUP', 'backup_construccion_app_v3_2026-02-22_14-30-00.sql');
```

**Ejemplo HTML:**
```html
<!-- Botón de descarga directo -->
<a href="http://localhost:8080/api/reportes-sistema/descargar/AUDITORIA/auditoria_2026-02-22_14-17-05.html" 
   download
   class="btn btn-primary">
  Descargar Auditoría
</a>
```

---

### 👁️ **GET** `/api/reportes-sistema/ver/auditoria/{nombreArchivo}`
Visualiza un reporte de auditoría en el navegador (sin forzar descarga)

**Parámetros:**
- `{nombreArchivo}`: nombre del archivo HTML

**Respuesta exitosa (200):**
- Archivo HTML para visualizar
- Header `Content-Disposition: inline`

**Ejemplo de uso:**
```javascript
// Abrir auditoría en nueva pestaña
function verAuditoria(nombreArchivo) {
  const url = `http://localhost:8080/api/reportes-sistema/ver/auditoria/${nombreArchivo}`;
  window.open(url, '_blank');
}

verAuditoria('auditoria_2026-02-22_14-17-05.html');
```

**Ejemplo en iframe:**
```html
<iframe 
  src="http://localhost:8080/api/reportes-sistema/ver/auditoria/auditoria_2026-02-22_14-17-05.html"
  width="100%" 
  height="600px"
  frameborder="0">
</iframe>
```

---

### 🗑️ **POST** `/api/reportes-sistema/limpiar?mantener=12`
Limpia reportes antiguos (mantiene solo los N más recientes)

**Query Parameter:**
- `mantener` (opcional, default=12): número de reportes a conservar

**Respuesta exitosa (200):**
```
Reportes antiguos eliminados correctamente
```

**⚠️ IMPORTANTE:** En producción, este endpoint debería requerir autenticación de administrador.

**Ejemplo de uso:**
```javascript
// Limpiar manteniendo últimos 10 reportes
fetch('http://localhost:8080/api/reportes-sistema/limpiar?mantener=10', {
  method: 'POST',
  headers: {
    'Authorization': 'Bearer ' + token // Si implementas auth
  }
})
.then(response => response.text())
.then(mensaje => console.log(mensaje));
```

---

## 🎨 Ejemplos de Implementación en Frontend

### React Component Completo

```jsx
import React, { useState, useEffect } from 'react';

function ReportesSistema() {
  const [reportes, setReportes] = useState({ auditorias: [], backups: [] });
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    cargarReportes();
  }, []);

  const cargarReportes = async () => {
    try {
      const response = await fetch('http://localhost:8080/api/reportes-sistema');
      const datos = await response.json();
      setReportes(datos);
    } catch (error) {
      console.error('Error al cargar reportes:', error);
    } finally {
      setLoading(false);
    }
  };

  const descargar = (tipo, nombre) => {
    window.open(
      `http://localhost:8080/api/reportes-sistema/descargar/${tipo}/${nombre}`,
      '_blank'
    );
  };

  const verAuditoria = (nombre) => {
    window.open(
      `http://localhost:8080/api/reportes-sistema/ver/auditoria/${nombre}`,
      '_blank'
    );
  };

  if (loading) return <div>Cargando reportes...</div>;

  return (
    <div className="reportes-container">
      <h2>Reportes del Sistema</h2>
      
      {/* Auditorías */}
      <section>
        <h3>Auditorías de Integridad ({reportes.totalAuditorias})</h3>
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Archivo</th>
              <th>Tamaño</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {reportes.auditorias.map(audit => (
              <tr key={audit.nombre}>
                <td>{new Date(audit.fechaCreacion).toLocaleString()}</td>
                <td>{audit.nombre}</td>
                <td>{audit.tamanoLegible}</td>
                <td>
                  <button onClick={() => verAuditoria(audit.nombre)}>
                    Ver
                  </button>
                  <button onClick={() => descargar('AUDITORIA', audit.nombre)}>
                    Descargar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>

      {/* Backups */}
      <section>
        <h3>Backups de Base de Datos ({reportes.totalBackups})</h3>
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Archivo</th>
              <th>Tamaño</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            {reportes.backups.map(backup => (
              <tr key={backup.nombre}>
                <td>{new Date(backup.fechaCreacion).toLocaleString()}</td>
                <td>{backup.nombre}</td>
                <td>{backup.tamanoLegible}</td>
                <td>
                  <button onClick={() => descargar('BACKUP', backup.nombre)}>
                    Descargar
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </section>
    </div>
  );
}

export default ReportesSistema;
```

### Vue.js Component

```vue
<template>
  <div class="reportes-sistema">
    <h2>Reportes del Sistema</h2>
    
    <div v-if="loading">Cargando reportes...</div>
    
    <div v-else>
      <!-- Auditorías -->
      <section>
        <h3>Auditorías ({{ reportes.totalAuditorias }})</h3>
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Archivo</th>
              <th>Tamaño</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="audit in reportes.auditorias" :key="audit.nombre">
              <td>{{ formatearFecha(audit.fechaCreacion) }}</td>
              <td>{{ audit.nombre }}</td>
              <td>{{ audit.tamanoLegible }}</td>
              <td>
                <button @click="verAuditoria(audit.nombre)">Ver</button>
                <button @click="descargar('AUDITORIA', audit.nombre)">Descargar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- Backups -->
      <section>
        <h3>Backups ({{ reportes.totalBackups }})</h3>
        <table>
          <thead>
            <tr>
              <th>Fecha</th>
              <th>Archivo</th>
              <th>Tamaño</th>
              <th>Acciones</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="backup in reportes.backups" :key="backup.nombre">
              <td>{{ formatearFecha(backup.fechaCreacion) }}</td>
              <td>{{ backup.nombre }}</td>
              <td>{{ backup.tamanoLegible }}</td>
              <td>
                <button @click="descargar('BACKUP', backup.nombre)">Descargar</button>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </div>
  </div>
</template>

<script>
export default {
  data() {
    return {
      reportes: { auditorias: [], backups: [] },
      loading: true
    };
  },
  
  mounted() {
    this.cargarReportes();
  },
  
  methods: {
    async cargarReportes() {
      try {
        const response = await fetch('http://localhost:8080/api/reportes-sistema');
        this.reportes = await response.json();
      } catch (error) {
        console.error('Error:', error);
      } finally {
        this.loading = false;
      }
    },
    
    descargar(tipo, nombre) {
      window.open(
        `http://localhost:8080/api/reportes-sistema/descargar/${tipo}/${nombre}`,
        '_blank'
      );
    },
    
    verAuditoria(nombre) {
      window.open(
        `http://localhost:8080/api/reportes-sistema/ver/auditoria/${nombre}`,
        '_blank'
      );
    },
    
    formatearFecha(fecha) {
      return new Date(fecha).toLocaleString('es-AR');
    }
  }
};
</script>
```

---

## ⚙️ Configuración de Tareas Automáticas

### Windows - Programador de Tareas

Consultar: `INSTRUCCIONES_AUDITORIA_AUTOMATICA.md` para configurar:
- ✅ Auditoría semanal automática
- ✅ Backup semanal automático

---

## 🔒 Seguridad

### Protecciones Implementadas

1. **Path Traversal Prevention**: Valida que los archivos estén dentro de los directorios permitidos
2. **Validación de Tipo**: Solo permite "AUDITORIA" o "BACKUP"
3. **CORS**: Configurado con `@CrossOrigin(origins = "*")` (ajustar en producción)

### Recomendaciones para Producción

```java
// Agregar autenticación/autorización
@PreAuthorize("hasRole('ADMIN')")
@PostMapping("/limpiar")
public ResponseEntity<String> limpiarReportesAntiguos(...) { ... }

// Limitar CORS
@CrossOrigin(origins = "https://tudominio.com")
```

---

## 📝 Logs

El sistema registra automáticamente:
- Listado de reportes
- Descargas de archivos
- Intentos de acceso no autorizado
- Limpiezas de archivos antiguos

**Ver logs:**
```
2026-02-22 14:17:05 - Obteniendo listado de todos los reportes del sistema
2026-02-22 14:17:10 - Descargando archivo auditoria_2026-02-22_14-17-05.html del tipo AUDITORIA
2026-02-22 14:20:00 - Limpiando reportes antiguos, manteniendo últimos 12
```

---

## 🧪 Testing

### Prueba Manual con cURL

```bash
# Listar todos los reportes
curl http://localhost:8080/api/reportes-sistema

# Listar solo auditorías
curl http://localhost:8080/api/reportes-sistema/auditorias

# Listar solo backups
curl http://localhost:8080/api/reportes-sistema/backups

# Descargar auditoría
curl -O http://localhost:8080/api/reportes-sistema/descargar/AUDITORIA/auditoria_2026-02-22_14-17-05.html

# Limpiar reportes antiguos
curl -X POST http://localhost:8080/api/reportes-sistema/limpiar?mantener=10
```

### Prueba desde Navegador

```
http://localhost:8080/api/reportes-sistema
http://localhost:8080/api/reportes-sistema/ver/auditoria/auditoria_2026-02-22_14-17-05.html
```

---

## 📞 Soporte

Para problemas o consultas:
1. Verificar logs del backend
2. Verificar que los directorios existan y tengan permisos
3. Confirmar que el backend esté corriendo en puerto 8080
4. Verificar CORS si hay errores desde el frontend

---

**Fecha de creación:** 22/02/2026  
**Autor:** Sistema Backend Constructora  
**Versión:** 1.0
