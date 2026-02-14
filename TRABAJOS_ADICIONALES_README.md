# MÓDULO TRABAJOS ADICIONALES - DOCUMENTACIÓN COMPLETA

## 📋 ÍNDICE
1. [Descripción General](#descripción-general)
2. [Archivos Creados](#archivos-creados)
3. [Base de Datos](#base-de-datos)
4. [API Endpoints](#api-endpoints)
5. [Ejemplos de Uso](#ejemplos-de-uso)
6. [Estructura de Datos](#estructura-de-datos)
7. [Validaciones](#validaciones)

---

## 🎯 DESCRIPCIÓN GENERAL

El módulo de **Trabajos Adicionales** permite gestionar tareas suplementarias que se pueden asociar tanto a **obras normales** como a **trabajos extra**. Cada trabajo adicional puede tener profesionales asignados, ya sean del catálogo de profesionales registrados o profesionales ad-hoc.

### Características Principales:
- ✅ Asociación flexible: obra O trabajo extra (excluyente)
- ✅ Profesionales registrados (del catálogo) o ad-hoc
- ✅ Estados: PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO
- ✅ Auditoría completa (fecha creación/actualización)
- ✅ Validaciones de negocio robustas
- ✅ API REST completa con Swagger

---

## 📁 ARCHIVOS CREADOS

### Entidades JPA
```
src/main/java/com/rodrigo/construccion/model/entity/
├── TrabajoAdicional.java
└── TrabajoAdicionalProfesional.java
```

### DTOs
```
src/main/java/com/rodrigo/construccion/dto/
├── TrabajoAdicionalRequestDTO.java
├── TrabajoAdicionalResponseDTO.java
├── TrabajoAdicionalProfesionalDTO.java
└── ActualizarEstadoTrabajoAdicionalDTO.java
```

### Repositorios
```
src/main/java/com/rodrigo/construccion/repository/
├── TrabajoAdicionalRepository.java
└── TrabajoAdicionalProfesionalRepository.java
```

### Servicios
```
src/main/java/com/rodrigo/construccion/service/
└── TrabajoAdicionalService.java
```

### Controladores
```
src/main/java/com/rodrigo/construccion/controller/
└── TrabajoAdicionalController.java
```

### Excepciones
```
src/main/java/com/rodrigo/construccion/exception/
└── TrabajoAdicionalValidationException.java
```

### Scripts SQL
```
script_trabajos_adicionales.sql (raíz del proyecto)
```

---

## 💾 BASE DE DATOS

### Paso 1: Ejecutar el Script SQL

#### Opción A: Desde línea de comandos
```bash
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f script_trabajos_adicionales.sql
```

#### Opción B: Desde pgAdmin/DBeaver
1. Abrir el archivo `script_trabajos_adicionales.sql`
2. Copiar todo el contenido
3. Ejecutar en una consulta SQL

### Tablas Creadas

#### 1. `trabajos_adicionales`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | ID único |
| nombre | VARCHAR(255) | Nombre del trabajo |
| importe | DECIMAL(15,2) | Costo total |
| dias_necesarios | INTEGER | Días/jornales necesarios |
| fecha_inicio | DATE | Fecha de inicio |
| descripcion | TEXT | Descripción detallada |
| observaciones | TEXT | Notas adicionales |
| obra_id | BIGINT | FK a obras (nullable) |
| trabajo_extra_id | BIGINT | FK a trabajos_extra (nullable) |
| empresa_id | BIGINT | FK a empresas |
| estado | VARCHAR(50) | PENDIENTE/EN_PROGRESO/COMPLETADO/CANCELADO |
| fecha_creacion | TIMESTAMP | Fecha de creación |
| fecha_actualizacion | TIMESTAMP | Última actualización |

#### 2. `trabajos_adicionales_profesionales`
| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | BIGSERIAL | ID único |
| trabajo_adicional_id | BIGINT | FK a trabajos_adicionales |
| profesional_id | BIGINT | FK a profesionales (nullable) |
| nombre | VARCHAR(255) | Nombre del profesional |
| tipo_profesional | VARCHAR(100) | Tipo/especialidad |
| honorario_dia | DECIMAL(10,2) | Honorario por día |
| telefono | VARCHAR(50) | Teléfono |
| email | VARCHAR(255) | Email |
| es_registrado | BOOLEAN | true=catálogo, false=ad-hoc |
| fecha_asignacion | TIMESTAMP | Fecha de asignación |

### Constraints Importantes

```sql
-- Solo puede tener obra_id O trabajo_extra_id (no ambos)
CHECK ((obra_id IS NOT NULL AND trabajo_extra_id IS NULL) OR 
       (obra_id IS NULL AND trabajo_extra_id IS NOT NULL))

-- Estado válido
CHECK (estado IN ('PENDIENTE', 'EN_PROGRESO', 'COMPLETADO', 'CANCELADO'))
```

---

## 🔌 API ENDPOINTS

### Base URL
```
http://localhost:8080/api/trabajos-adicionales
```

### 1. Crear Trabajo Adicional
```http
POST /api/trabajos-adicionales
Content-Type: application/json

{
  "nombre": "Instalación de alarmas",
  "importe": 5000.00,
  "diasNecesarios": 3,
  "fechaInicio": "2026-02-20",
  "descripcion": "Instalación completa de sistema de alarmas",
  "observaciones": "Cliente solicita cámaras adicionales",
  "obraId": 15,
  "trabajoExtraId": null,
  "empresaId": 1,
  "profesionales": [
    {
      "profesionalId": 8,
      "nombre": "Juan Pérez",
      "tipoProfesional": "Electricista",
      "honorarioDia": null,
      "telefono": null,
      "email": null,
      "esRegistrado": true
    },
    {
      "profesionalId": null,
      "nombre": "Carlos Rodríguez",
      "tipoProfesional": "Técnico en Seguridad",
      "honorarioDia": 350.00,
      "telefono": "123456789",
      "email": "carlos@email.com",
      "esRegistrado": false
    }
  ]
}
```

**Response 201 Created:**
```json
{
  "id": 1,
  "nombre": "Instalación de alarmas",
  "importe": 5000.00,
  "diasNecesarios": 3,
  "fechaInicio": "2026-02-20",
  "descripcion": "Instalación completa de sistema de alarmas",
  "observaciones": "Cliente solicita cámaras adicionales",
  "obraId": 15,
  "trabajoExtraId": null,
  "empresaId": 1,
  "estado": "PENDIENTE",
  "fechaCreacion": "2026-02-14 15:30:00",
  "fechaActualizacion": "2026-02-14 15:30:00",
  "profesionales": [...]
}
```

### 2. Listar Trabajos Adicionales
```http
GET /api/trabajos-adicionales?empresaId=1
GET /api/trabajos-adicionales?empresaId=1&obraId=15
GET /api/trabajos-adicionales?empresaId=1&trabajoExtraId=5
```

**Response 200 OK:** Array de trabajos adicionales

### 3. Obtener por ID
```http
GET /api/trabajos-adicionales/1?empresaId=1
```

### 4. Actualizar
```http
PUT /api/trabajos-adicionales/1?empresaId=1
Content-Type: application/json

{
  "nombre": "Instalación de alarmas (modificado)",
  "importe": 5500.00,
  ...
}
```

### 5. Actualizar Estado
```http
PATCH /api/trabajos-adicionales/1/estado?empresaId=1
Content-Type: application/json

{
  "estado": "EN_PROGRESO"
}
```

### 6. Eliminar
```http
DELETE /api/trabajos-adicionales/1?empresaId=1
```

**Response 204 No Content**

---

## 📘 EJEMPLOS DE USO

### Ejemplo 1: Trabajo Adicional para Obra con Profesional Registrado

```json
{
  "nombre": "Reparación de grietas",
  "importe": 2500.00,
  "diasNecesarios": 2,
  "fechaInicio": "2026-02-25",
  "descripcion": "Reparar grietas en paredes del segundo piso",
  "obraId": 10,
  "trabajoExtraId": null,
  "empresaId": 1,
  "profesionales": [
    {
      "profesionalId": 3,
      "nombre": "Pedro Martínez",
      "tipoProfesional": "Albañil",
      "esRegistrado": true
    }
  ]
}
```

### Ejemplo 2: Trabajo Adicional para Trabajo Extra con Profesionales Ad-hoc

```json
{
  "nombre": "Limpieza profunda post obra",
  "importe": 1500.00,
  "diasNecesarios": 1,
  "fechaInicio": "2026-03-01",
  "obraId": null,
  "trabajoExtraId": 7,
  "empresaId": 1,
  "profesionales": [
    {
      "nombre": "María González",
      "tipoProfesional": "Personal de limpieza",
      "honorarioDia": 200.00,
      "telefono": "987654321",
      "email": "maria@limpieza.com",
      "esRegistrado": false
    },
    {
      "nombre": "Ana López",
      "tipoProfesional": "Personal de limpieza",
      "honorarioDia": 200.00,
      "telefono": "987654322",
      "esRegistrado": false
    }
  ]
}
```

### Ejemplo 3: Cambiar Estado a COMPLETADO

```json
{
  "estado": "COMPLETADO"
}
```

---

## 📊 ESTRUCTURA DE DATOS

### TrabajoAdicionalRequestDTO (Request)
```typescript
{
  nombre: string (obligatorio),
  importe: decimal (obligatorio, > 0),
  diasNecesarios: integer (obligatorio, >= 1),
  fechaInicio: date (obligatorio),
  descripcion: string (opcional),
  observaciones: string (opcional),
  obraId: integer (nullable - exclusivo con trabajoExtraId),
  trabajoExtraId: integer (nullable - exclusivo con obraId),
  empresaId: integer (obligatorio),
  profesionales: [
    {
      id: integer (solo en response),
      profesionalId: integer (obligatorio si esRegistrado=true),
      nombre: string (obligatorio),
      tipoProfesional: string (obligatorio),
      honorarioDia: decimal (opcional),
      telefono: string (opcional),
      email: string (opcional),
      esRegistrado: boolean (obligatorio),
      fechaAsignacion: string (solo en response)
    }
  ]
}
```

---

## ✅ VALIDACIONES

### Validaciones del Backend

1. **Constraint Obra/Trabajo Extra:**
   - Debe tener `obraId` O `trabajoExtraId`, no ambos
   - Error: "Debe especificar una obra o un trabajo extra"

2. **Validación de Existencia:**
   - La obra/trabajo extra debe existir en la BD
   - Error: "Obra/Trabajo extra no encontrado con ID: X"

3. **Validación de Empresa:**
   - La obra/trabajo extra debe pertenecer a la empresa especificada
   - Error: "No pertenece a la empresa especificada"

4. **Profesionales Registrados:**
   - Si `esRegistrado=true`, el profesional debe existir en catálogo
   - Error: "Profesional no encontrado con ID: X"

5. **Estados Válidos:**
   - Solo: PENDIENTE, EN_PROGRESO, COMPLETADO, CANCELADO
   - Error: "El estado debe ser: PENDIENTE, EN_PROGRESO..."

6. **Valores Numéricos:**
   - `importe` > 0
   - `diasNecesarios` >= 1

---

## 🔧 PRUEBAS CON POSTMAN

### Collection JSON (Importar en Postman)

```json
{
  "info": {
    "name": "Trabajos Adicionales",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "Crear Trabajo Adicional",
      "request": {
        "method": "POST",
        "header": [],
        "body": {
          "mode": "raw",
          "raw": "{\n  \"nombre\": \"Instalación de alarmas\",\n  \"importe\": 5000.00,\n  \"diasNecesarios\": 3,\n  \"fechaInicio\": \"2026-02-20\",\n  \"obraId\": 1,\n  \"empresaId\": 1,\n  \"profesionales\": []\n}",
          "options": {
            "raw": {
              "language": "json"
            }
          }
        },
        "url": {
          "raw": "http://localhost:8080/api/trabajos-adicionales",
          "protocol": "http",
          "host": ["localhost"],
          "port": "8080",
          "path": ["api", "trabajos-adicionales"]
        }
      }
    }
  ]
}
```

---

## 📌 NOTAS IMPORTANTES

1. **Cascada en Eliminación:**
   - Al eliminar un trabajo adicional, se eliminan automáticamente todos los profesionales asociados

2. **Actualización de Profesionales:**
   - Al actualizar un trabajo adicional, se eliminan TODOS los profesionales anteriores
   - Se insertan los nuevos profesionales del array

3. **Timestamp Automático:**
   - `fecha_actualizacion` se actualiza automáticamente con un trigger de PostgreSQL

4. **Swagger UI:**
   - Accede a la documentación interactiva en:
   - `http://localhost:8080/swagger-ui.html`

5. **Filtros de Búsqueda:**
   - Puedes filtrar por `empresaId` (obligatorio)
   - Opcionalmente por `obraId` O `trabajoExtraId`

---

## 🚀 COMPILACIÓN Y EJECUCIÓN

### 1. Ejecutar Script SQL
```bash
psql -h localhost -p 5432 -U postgres -d construccion_app_v3 -f script_trabajos_adicionales.sql
```

### 2. Compilar el Proyecto
```bash
./mvnw clean compile
```

### 3. Ejecutar el Backend
```bash
./mvnw spring-boot:run
```

### 4. Verificar Swagger
```
http://localhost:8080/swagger-ui.html
```

---

## 📞 SOPORTE

Para cualquier duda o problema con el módulo de Trabajos Adicionales, verificar:
- Logs del backend (búsqueda por "TrabajoAdicionalService")
- Estado de la base de datos (tablas y constraints)
- Formato correcto del JSON en las peticiones

---

**Versión:** 1.0  
**Fecha:** 14 de febrero de 2026  
**Estado:** ✅ Compilado y listo para usar
