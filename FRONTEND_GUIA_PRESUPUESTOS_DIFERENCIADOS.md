# 📋 GUÍA FRONTEND - Sistema de Presupuestos Diferenciados

**Fecha**: 26 de febrero de 2026  
**Backend API**: `/api/v1/presupuestos-no-cliente`  
**Versión**: 2.0 - Sistema Diferenciado

---

## 🎯 Resumen Ejecutivo

El sistema ahora soporta **4 tipos de presupuestos** con reglas de validación específicas para cada uno.

---

## 📊 Los 4 Tipos de Presupuestos

| Tipo | Código | Crea Obra | Aprobación | idObra |
|------|--------|-----------|------------|--------|
| **Tradicional** | `TRADICIONAL` | Al aprobar | Manual | ❌ DEBE ser `null` |
| **Trabajo Diario** | `TRABAJO_DIARIO` | Inmediatamente | Automática | ❌ DEBE ser `null` |
| **Trabajo Extra** | `TRABAJO_EXTRA` | No crea | Manual | ✅ OBLIGATORIO |
| **Tarea Leve** | `TAREA_LEVE` | No crea | Automática | ✅ OBLIGATORIO |

---

## 🔴 REGLAS CRÍTICAS (Causan Error 409 si no se cumplen)

### 1️⃣ **TRADICIONAL** y **TRABAJO_DIARIO**

**Estos presupuestos CREAN obra nueva, por lo tanto:**

```json
{
  "tipoPresupuesto": "TRADICIONAL",  // o "TRABAJO_DIARIO"
  "idObra": null,  // ⚠️ DEBE SER NULL (si envías un ID = ERROR 409)
  
  // ✅ CAMPOS OBLIGATORIOS para crear obra:
  "nombreObra": "Casa Rodríguez",  // ⚠️ OBLIGATORIO
  "direccionObraCalle": "Av. Siempre Viva",  // ⚠️ OBLIGATORIO
  "direccionObraAltura": "742",  // ⚠️ OBLIGATORIO
  
  // Opcionales:
  "direccionObraPiso": "3",
  "direccionObraDepartamento": "B",
  "direccionObraBarrio": "Springfield",
  "direccionObraTorre": "A"
}
```

**❌ ERRORES COMUNES:**
- Enviar `idObra: 123` → **ERROR 409**: "Presupuestos tipo TRADICIONAL no deben tener obraId"
- No enviar `nombreObra` → **ERROR 409**: "nombreObra es obligatorio"
- No enviar `direccionObraCalle` → **ERROR 409**: "direccionObraCalle es obligatorio"
- No enviar `direccionObraAltura` → **ERROR 409**: "direccionObraAltura es obligatorio"

---

### 2️⃣ **TRABAJO_EXTRA** y **TAREA_LEVE**

**Estos presupuestos se VINCULAN a obra existente:**

```json
{
  "tipoPresupuesto": "TRABAJO_EXTRA",  // o "TAREA_LEVE"
  "idObra": 123,  // ⚠️ OBLIGATORIO (debe existir en la BD)
  
  // ✅ EL CLIENTE SE HEREDA AUTOMÁTICAMENTE DE LA OBRA
  // NO enviar idCliente (se ignora)
  
  // Campos de obra NO son necesarios (se ignoran):
  "nombreObra": "Reparación adicional",  // Solo descriptivo, no crea obra
  "direccionObraCalle": null,  // Se ignoran
  "direccionObraAltura": null   // Se ignoran
}
```

**❌ ERRORES COMUNES:**
- No enviar `idObra` → **ERROR 409**: "Presupuestos tipo TRABAJO_EXTRA requieren obraId obligatorio"
- Enviar `idObra: 999` (obra inexistente) → **ERROR 409**: "Obra con ID 999 no existe"
- Enviar `idObra: null` → **ERROR 409**: "requieren obraId obligatorio"

---

## 🔧 Estructura Completa del Request

### **Endpoint**: `POST /api/v1/presupuestos-no-cliente?empresaId={id}`

### **Headers**:
```
Content-Type: application/json
X-Empresa-Id: 1
```

### **Body Base** (común para todos):
```json
{
  // ========== TIPO Y EMPRESA (OBLIGATORIOS) ==========
  "tipoPresupuesto": "TRADICIONAL",  // Ver tabla arriba
  "idEmpresa": 1,  // O usar header X-Empresa-Id
  
  // ========== CLIENTE (OPCIONAL PARA TRADICIONAL/TRABAJO_DIARIO) ==========
  "idCliente": 5,  // Opcional. Para TRABAJO_EXTRA/TAREA_LEVE se ignora (hereda de obra)
  
  // ========== OBRA (SEGÚN TIPO - VER REGLAS ARRIBA) ==========
  "idObra": null,  // null para TRADICIONAL/TRABAJO_DIARIO, obligatorio para TRABAJO_EXTRA/TAREA_LEVE
  "nombreObra": "Casa Rodríguez",  // Obligatorio para TRADICIONAL/TRABAJO_DIARIO
  "direccionObraCalle": "Av. Siempre Viva",  // Obligatorio para TRADICIONAL/TRABAJO_DIARIO
  "direccionObraAltura": "742",  // Obligatorio para TRADICIONAL/TRABAJO_DIARIO
  "direccionObraPiso": "3",
  "direccionObraDepartamento": "B",
  "direccionObraBarrio": "Springfield",
  "direccionObraTorre": "A",
  
  // ========== DATOS DEL PRESUPUESTO ==========
  "nombreSolicitante": "Juan Rodríguez",
  "telefono": "+54 9 11 1234-5678",
  "mail": "juan@example.com",
  "observaciones": "Presupuesto para ampliación",
  
  // ========== FECHAS ==========
  "fechaEmision": "2026-02-26",
  "fechaCreacion": "2026-02-26",
  "vencimiento": "2026-03-12",
  "fechaProbableInicio": "2026-03-15",
  
  // ========== MATERIALES Y PROFESIONALES ==========
  "materialesList": [
    {
      "idMaterial": 10,
      "cantidad": 100.0,
      "precioUnitario": 50.0,
      "descripcion": "Ladrillos"
    }
  ],
  "profesionales": [
    {
      "idProfesional": 3,
      "unidadActiva": "Hora",
      "cantidad": 40.0,
      "importePorUnidad": 1500.0,
      "descripcion": "Albañil"
    }
  ],
  
  // ========== HONORARIOS (OPCIONAL) ==========
  "honorariosConfiguracionPresupuestoActivo": true,
  "honorariosConfiguracionPresupuestoTipo": "PORCENTAJE",
  "honorariosConfiguracionPresupuestoValor": 15.0
}
```

---

## 📝 Ejemplos Completos por Tipo

### ✅ Ejemplo 1: TRADICIONAL (Presupuesto Normal)

```json
{
  "tipoPresupuesto": "TRADICIONAL",
  "idEmpresa": 1,
  "idCliente": 5,
  "idObra": null,  // ⚠️ NULL
  
  "nombreObra": "Ampliación Casa Rodríguez",  // ⚠️ OBLIGATORIO
  "direccionObraCalle": "Av. Libertador",  // ⚠️ OBLIGATORIO
  "direccionObraAltura": "1234",  // ⚠️ OBLIGATORIO
  "direccionObraBarrio": "Palermo",
  
  "nombreSolicitante": "Juan Rodríguez",
  "telefono": "+54 9 11 1234-5678",
  "mail": "juan@example.com",
  "vencimiento": "2026-03-12",
  
  "materialesList": [],
  "profesionales": []
}
```

**Resultado**: Se crea presupuesto en estado `A_ENVIAR`. Al aprobarlo, se crea la obra automáticamente.

---

### ✅ Ejemplo 2: TRABAJO_DIARIO (Presupuesto Aprobado Automáticamente)

```json
{
  "tipoPresupuesto": "TRABAJO_DIARIO",
  "idEmpresa": 1,
  "idCliente": 5,
  "idObra": null,  // ⚠️ NULL
  
  "nombreObra": "Trabajo del día 26/02",  // ⚠️ OBLIGATORIO
  "direccionObraCalle": "Av. Corrientes",  // ⚠️ OBLIGATORIO
  "direccionObraAltura": "5678",  // ⚠️ OBLIGATORIO
  
  "nombreSolicitante": "Pedro Gómez",
  "telefono": "+54 9 11 8765-4321",
  "mail": "pedro@example.com",
  
  "materialesList": [],
  "profesionales": [
    {
      "idProfesional": 3,
      "unidadActiva": "Dia",
      "cantidad": 1.0,
      "importePorUnidad": 8000.0
    }
  ]
}
```

**Resultado**: 
- Se crea presupuesto en estado `APROBADO` automáticamente
- Se crea la obra **inmediatamente** (sin esperar aprobación)

---

### ✅ Ejemplo 3: TRABAJO_EXTRA (Trabajo Adicional de Obra Existente)

```json
{
  "tipoPresupuesto": "TRABAJO_EXTRA",
  "idEmpresa": 1,
  "idObra": 123,  // ⚠️ OBLIGATORIO - obra existente
  
  "nombreObra": "Trabajo extra - Pintura adicional",  // Descriptivo
  
  // NO enviar idCliente - se hereda de la obra 123
  
  "nombreSolicitante": "María López",
  "telefono": "+54 9 11 2222-3333",
  "mail": "maria@example.com",
  "vencimiento": "2026-03-05",
  
  "materialesList": [
    {
      "idMaterial": 15,
      "cantidad": 20.0,
      "precioUnitario": 250.0
    }
  ],
  "profesionales": []
}
```

**Resultado**: 
- Se crea presupuesto en estado `A_ENVIAR`
- El cliente se hereda automáticamente de la obra 123
- **NO** se crea nueva obra (se vincula a la 123)

---

### ✅ Ejemplo 4: TAREA_LEVE (Tarea Menor Aprobada Automáticamente)

```json
{
  "tipoPresupuesto": "TAREA_LEVE",
  "idEmpresa": 1,
  "idObra": 123,  // ⚠️ OBLIGATORIO - obra existente
  
  "nombreObra": "Arreglo cerradura puerta",  // Descriptivo
  
  "nombreSolicitante": "Carlos Díaz",
  "telefono": "+54 9 11 4444-5555",
  "mail": "carlos@example.com",
  
  "materialesList": [
    {
      "idMaterial": 20,
      "cantidad": 1.0,
      "precioUnitario": 1500.0
    }
  ],
  "profesionales": []
}
```

**Resultado**: 
- Se crea presupuesto en estado `APROBADO` automáticamente
- El cliente se hereda automáticamente de la obra 123
- **NO** se crea nueva obra (se vincula a la 123)

---

## 🚨 Manejo de Errores

### **Error 409 - Conflict**

**Mensaje**: "Error de integridad de datos. Verifique que los datos no estén duplicados o que las referencias a otras entidades sean correctas."

**Causas comunes**:

1. **Tipo TRADICIONAL/TRABAJO_DIARIO con idObra no null**
   ```json
   {
     "tipoPresupuesto": "TRADICIONAL",
     "idObra": 123  // ❌ ERROR - debe ser null
   }
   ```
   **Solución**: `"idObra": null`

2. **Tipo TRABAJO_EXTRA/TAREA_LEVE sin idObra**
   ```json
   {
     "tipoPresupuesto": "TRABAJO_EXTRA",
     "idObra": null  // ❌ ERROR - debe tener un ID
   }
   ```
   **Solución**: `"idObra": 123` (obra existente)

3. **Falta nombreObra para TRADICIONAL/TRABAJO_DIARIO**
   ```json
   {
     "tipoPresupuesto": "TRADICIONAL",
     "idObra": null,
     "nombreObra": ""  // ❌ ERROR - es obligatorio
   }
   ```
   **Solución**: `"nombreObra": "Casa Rodríguez"`

4. **Falta direccionObraCalle para TRADICIONAL/TRABAJO_DIARIO**
   ```json
   {
     "tipoPresupuesto": "TRADICIONAL",
     "idObra": null,
     "nombreObra": "Casa",
     "direccionObraCalle": null  // ❌ ERROR - es obligatorio
   }
   ```
   **Solución**: `"direccionObraCalle": "Av. Libertador"`

5. **Falta direccionObraAltura para TRADICIONAL/TRABAJO_DIARIO**
   ```json
   {
     "tipoPresupuesto": "TRADICIONAL",
     "idObra": null,
     "nombreObra": "Casa",
     "direccionObraCalle": "Av. Libertador",
     "direccionObraAltura": null  // ❌ ERROR - es obligatorio
   }
   ```
   **Solución**: `"direccionObraAltura": "1234"`

6. **idObra apunta a obra inexistente**
   ```json
   {
     "tipoPresupuesto": "TRABAJO_EXTRA",
     "idObra": 999999  // ❌ ERROR - obra no existe
   }
   ```
   **Solución**: Usar un ID de obra existente

---

## 🔍 Validación en Frontend

### **Lógica Recomendada**:

```javascript
function validarPresupuestoAntesDeEnviar(presupuesto) {
  const tipo = presupuesto.tipoPresupuesto;
  
  // Validación 1: Tipos que NO deben tener idObra
  if (tipo === 'TRADICIONAL' || tipo === 'TRABAJO_DIARIO') {
    if (presupuesto.idObra !== null) {
      return {
        valido: false,
        error: `Presupuestos tipo ${tipo} no deben tener obra asociada. Dejar idObra en NULL.`
      };
    }
    
    // Validar campos obligatorios para crear obra
    if (!presupuesto.nombreObra || presupuesto.nombreObra.trim() === '') {
      return {
        valido: false,
        error: 'El campo "Nombre de Obra" es obligatorio para presupuestos que crean obra nueva.'
      };
    }
    
    if (!presupuesto.direccionObraCalle || presupuesto.direccionObraCalle.trim() === '') {
      return {
        valido: false,
        error: 'El campo "Calle" es obligatorio para presupuestos que crean obra nueva.'
      };
    }
    
    if (!presupuesto.direccionObraAltura || presupuesto.direccionObraAltura.trim() === '') {
      return {
        valido: false,
        error: 'El campo "Altura" es obligatorio para presupuestos que crean obra nueva.'
      };
    }
  }
  
  // Validación 2: Tipos que SÍ requieren idObra
  if (tipo === 'TRABAJO_EXTRA' || tipo === 'TAREA_LEVE') {
    if (!presupuesto.idObra) {
      return {
        valido: false,
        error: `Presupuestos tipo ${tipo} requieren seleccionar una obra existente.`
      };
    }
  }
  
  return { valido: true };
}
```

---

## 📝 Checklist de Implementación Frontend

### **Crear Presupuesto TRADICIONAL**:
- [ ] Campo "Tipo de Presupuesto" → Seleccionar "TRADICIONAL"
- [ ] Asegurar que `idObra = null` (no mostrar selector de obra)
- [ ] Mostrar campos obligatorios: nombreObra, direccionObraCalle, direccionObraAltura
- [ ] Marcar visualmente que son obligatorios (*)
- [ ] Validar antes de enviar
- [ ] Informar al usuario: "Este presupuesto creará una obra nueva al ser aprobado"

### **Crear Presupuesto TRABAJO_DIARIO**:
- [ ] Campo "Tipo de Presupuesto" → Seleccionar "TRABAJO_DIARIO"
- [ ] Asegurar que `idObra = null`
- [ ] Mostrar campos obligatorios: nombreObra, direccionObraCalle, direccionObraAltura
- [ ] Validar antes de enviar
- [ ] Informar al usuario: "Este presupuesto se aprobará automáticamente y creará una obra inmediatamente"

### **Crear Presupuesto TRABAJO_EXTRA**:
- [ ] Campo "Tipo de Presupuesto" → Seleccionar "TRABAJO_EXTRA"
- [ ] Mostrar selector de obra (obligatorio)
- [ ] **NO** pedir cliente (se hereda de la obra)
- [ ] Validar que se seleccionó una obra
- [ ] Informar al usuario: "Este presupuesto se vinculará a la obra existente y heredará su cliente"

### **Crear Presupuesto TAREA_LEVE**:
- [ ] Campo "Tipo de Presupuesto" → Seleccionar "TAREA_LEVE"
- [ ] Mostrar selector de obra (obligatorio)
- [ ] **NO** pedir cliente (se hereda de la obra)
- [ ] Validar que se seleccionó una obra
- [ ] Informar al usuario: "Este presupuesto se aprobará automáticamente y se vinculará a la obra existente"

---

## 🎨 Sugerencias de UX

### **Selector de Tipo de Presupuesto**:

```
┌─────────────────────────────────────────────────┐
│ Tipo de Presupuesto: *                          │
├─────────────────────────────────────────────────┤
│ ○ Tradicional                                   │
│   └─ Requiere aprobación manual                │
│   └─ Crea obra al aprobar                      │
│                                                 │
│ ○ Trabajo Diario                                │
│   └─ Se aprueba automáticamente                │
│   └─ Crea obra inmediatamente                  │
│                                                 │
│ ○ Trabajo Extra                                 │
│   └─ Requiere seleccionar obra existente       │
│   └─ Hereda cliente de la obra                 │
│                                                 │
│ ○ Tarea Leve                                    │
│   └─ Se aprueba automáticamente                │
│   └─ Requiere seleccionar obra existente       │
│   └─ Hereda cliente de la obra                 │
└─────────────────────────────────────────────────┘
```

### **Formulario Dinámico**:

```javascript
// Mostrar/ocultar campos según tipo seleccionado
if (tipoPresupuesto === 'TRADICIONAL' || tipoPresupuesto === 'TRABAJO_DIARIO') {
  mostrarCampos(['nombreObra', 'direccionObraCalle', 'direccionObraAltura']);
  ocultarCampos(['selectorObra']);
  marcarObligatorios(['nombreObra', 'direccionObraCalle', 'direccionObraAltura']);
}

if (tipoPresupuesto === 'TRABAJO_EXTRA' || tipoPresupuesto === 'TAREA_LEVE') {
  mostrarCampos(['selectorObra']);
  ocultarCampos(['cliente']);  // Cliente se hereda
  deshabilitarCampos(['direccionObraCalle', 'direccionObraAltura']);  // No necesarios
  marcarObligatorios(['selectorObra']);
}
```

---

## 📞 Soporte

**Si encuentras errores 409**:
1. Verificar que estás enviando `idObra` correctamente según el tipo
2. Verificar que los campos obligatorios están presentes
3. Revisar la consola del backend para ver el error específico

**Logs del backend para depuración**:
- Buscar: `🔍 Validando datos para tipo presupuesto:`
- Buscar: `❌ ERROR AL GUARDAR PRESUPUESTO`
- Buscar: `DataIntegrityViolationException`

---

**Última actualización**: 26 de febrero de 2026  
**Versión del documento**: 1.0
