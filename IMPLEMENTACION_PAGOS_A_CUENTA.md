# 🚀 Sistema de Pagos a Cuenta - Implementación Completa

## 📋 Descripción General

Se ha implementado un sistema completo de **Pagos a Cuenta** sobre items de rubros del presupuesto, funcionando en paralelo con el sistema existente de **Adelantos** a profesionales.

### Diferencia entre sistemas:
- **Adelantos**: Pagos a profesionales por su trabajo
- **Pagos a Cuenta**: Pagos parciales sobre items de rubros (Jornales, Materiales, Gastos Generales)

---

## 🏗️ Backend - Archivos Creados/Modificados

### 1. **Entity** - `PagoParcialRubro.java`
- Ubicación: `src/main/java/com/rodrigo/construccion/model/entity/`
- Tabla: `pagos_parciales_rubros`
- Campos principales:
  - `presupuesto_id`: ID del presupuesto
  - `nombre_rubro`: Nombre del rubro (ej: "Albañilería", "Plomería")
  - `tipo_item`: JORNALES | MATERIALES | GASTOS_GENERALES
  - `monto`: Monto del pago parcial
  - `metodo_pago`: EFECTIVO | TRANSFERENCIA | CHEQUE | TARJETA | OTRO
  - `fecha_pago`, `observaciones`, `usuario_registro`

### 2. **DTOs**
#### Request - `PagoCuentaRequestDTO.java`
```java
@Data @Builder
{
  presupuestoId, empresaId, nombreRubro, tipoItem,
  monto, metodoPago, observaciones, fechaPago, usuarioRegistro
}
```

#### Response - `PagoCuentaResponseDTO.java`
```java
@Data @Builder
{
  // Datos del pago
  id, presupuestoId, empresaId, nombreRubro, tipoItem,
  monto, metodoPago, observaciones, fechaPago, usuarioRegistro,
  
  // Totales calculados
  montoTotalItem, totalPagado, saldoPendiente, porcentajePagado
}
```

### 3. **Repository** - `PagoParcialRubroRepository.java`
- Métodos principales:
  - `findByPresupuestoIdAndEmpresaId...`
  - `calcularTotalPagadoItem()`
  - `calcularTotalPagadoRubro()`
  - `calcularTotalPagadoPresupuesto()`

### 4. **Service** - `PagoCuentaService.java`
- Implementa: `IPagoCuentaService`
- Métodos principales:
  - `crearPagoCuenta()` - Registra pago y valida saldo pendiente
  - `listarPagosPorPresupuesto()`
  - `listarPagosPorRubro()`
  - `listarPagosPorItem()`
  - `obtenerResumenPagos()` - Resumen completo por rubro e item
  - `calcularTotalesItem()` - Total, pagado, pendiente, %
  - `eliminarPago()`

### 5. **Controller** - `PagoCuentaController.java`
- Base URL: `/api/pagos-cuenta`
- Endpoints:
  ```
  POST   /api/pagos-cuenta                    - Crear pago
  GET    /api/pagos-cuenta/{id}               - Obtener por ID
  GET    /api/pagos-cuenta                    - Listar por presupuesto
  GET    /api/pagos-cuenta/rubro              - Listar por rubro
  GET    /api/pagos-cuenta/item               - Listar por item
  GET    /api/pagos-cuenta/resumen            - Resumen completo
  GET    /api/pagos-cuenta/totales-item       - Totales de un item
  DELETE /api/pagos-cuenta/{id}               - Eliminar pago
  ```

### 6. **Script SQL** - `crear_tabla_pagos_parciales_rubros.sql`
```sql
CREATE TABLE pagos_parciales_rubros (
  id BIGSERIAL PRIMARY KEY,
  presupuesto_id BIGINT NOT NULL,
  empresa_id BIGINT NOT NULL,
  nombre_rubro VARCHAR(255) NOT NULL,
  tipo_item VARCHAR(50) NOT NULL,
  monto DECIMAL(15,2) NOT NULL,
  metodo_pago VARCHAR(50) DEFAULT 'EFECTIVO',
  observaciones TEXT,
  fecha_pago TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  usuario_registro VARCHAR(100),
  fecha_registro TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT chk_tipo_item CHECK (tipo_item IN ('JORNALES', 'MATERIALES', 'GASTOS_GENERALES'))
);
-- Índices para optimización
```

---

## 💻 Frontend - Archivos Creados/Modificados

### 1. **Servicio** - `pagoCuentaService.js`
- Ubicación: `src/services/`
- Funciones:
  - `crearPagoCuenta(pagoData)`
  - `obtenerPagoPorId(id, empresaId)`
  - `listarPagosPorPresupuesto(presupuestoId, empresaId)`
  - `listarPagosPorRubro(...)`
  - `listarPagosPorItem(...)`
  - `obtenerResumenPagos(...)`
  - `calcularTotalesItem(...)`
  - `eliminarPago(id, empresaId)`
  - `formatearMoneda(valor)`

- Constantes:
  ```js
  TIPOS_ITEM = { JORNALES, MATERIALES, GASTOS_GENERALES }
  METODOS_PAGO = { EFECTIVO, TRANSFERENCIA, CHEQUE, TARJETA, OTRO }
  ```

### 2. **Modal** - `PagoCuentaModal.jsx`
- Ubicación: `src/components/`
- 3 Vistas principales:
 
  **a) Vista de Selección:**
  - Accordion con todos los rubros del presupuesto
  - Tabla por rubro mostrando 3 items:
    - Jornales (icono personas)
    - Materiales (icono caja)
    - Gastos Generales (icono recibo)
  - Por cada item muestra:
    - Total Presupuestado
    - Total Pagado (verde)
    - Saldo Pendiente (rojo)
    - Badge de estado (Sin pagos | X% pagado | ✓ Pagado)
    - Botón "Pagar"
  
  **b) Vista de Pago:**
  - Card superior con 3 columnas:
    - Total Presupuestado (gris)
    - Total Pagado (verde) con % pagado
    - Saldo Pendiente (rojo)
  - Formulario:
    - Monto del Pago *
    - Método de Pago (dropdown)
    - Fecha de Pago
    - Observaciones
    - Botón "Registrar Pago"
  - Validación: El monto no puede exceder el saldo pendiente
  
  **c) Vista de Historial:**
  - Tabla con todos los pagos registrados:
    - Fecha | Rubro | Item | Monto | Método | Observaciones
  - Ordenados por fecha descendente

- **Estilos:**
  - React Bootstrap (Modal, Button, Form, Badge, Alert, Table, Card, Accordion)
  - Iconos de Bootstrap Icons
  - Formato de moneda argentino (ARS)
  - Estados con colores: success (verde), danger (rojo), warning (amarillo), info (azul)

### 3. **Página** - `SistemaFinancieroPage.jsx` (Modificada)
- Agregados:
  - Import: `import PagoCuentaModal from '../components/PagoCuentaModal'`
  - Estados:
    ```js
    const [showPagoCuenta, setShowPagoCuenta] = useState(false);
    const [presupuestoParaPagoCuenta, setPresupuestoParaPagoCuenta] = useState(null);
    ```
  
  - **Tarjeta nueva:**
    ```jsx
    <div className="card border-info">
      <div className="card-header bg-info text-white">
        💰 Pagos a Cuenta
      </div>
      <div className="card-body">
        Registre pagos parciales sobre items de rubros
        (Jornales, Materiales, Gastos Generales)
      </div>
      <button onClick={() => setShowPagoCuenta(true)}>
        Abrir Tarjeta
      </button>
    </div>
    ```
  
  - **Modal renderizado:**
    ```jsx
    <PagoCuentaModal
      show={showPagoCuenta}
      onHide={() => { setShowPagoCuenta(false); setPresupuestoParaPagoCuenta(null); }}
      presupuesto={presupuestoParaPagoCuenta || obraSeleccionada}
      onSuccess={handleSuccess}
    />
    ```
  
  - Modificado `handleSuccess` para cerrar el modal: `setShowPagoCuenta(false)`

---

## 🗂️ Estructura de Datos

### Presupuesto → Rubros → Items

```
Presupuesto (ej: Obra Casa Barrio X)
├─ Rubro: Albañilería (tipoProfesional)
│  ├─ Jornales: $5,000,000
│  ├─ Materiales: $3,000,000
│  └─ Gastos Generales: $1,000,000
├─ Rubro: Plomería
│  ├─ Jornales: $2,000,000
│  ├─ Materiales: $1,500,000
│  └─ Gastos Generales: $500,000
└─ Rubro: Pintura
   ├─ Jornales: $1,500,000
   ├─ Materiales: $800,000
   └─ Gastos Generales: $300,000
```

### Cálculo de Totales por Item

El servicio calcula automáticamente:
- **Jornales**: Suma de `subtotal` de cada jornal en `jornales` array
- **Materiales**: Suma de `total` de cada material en `materialesLista` array
- **Gastos Generales**: Suma de `total` de cada gasto en `gastosGenerales` array

---

## 🔧 Pasos para Desplegar

### Backend
1. **Ejecutar SQL:**
   ```bash
   psql -U usuario -d construccion_db -f crear_tabla_pagos_parciales_rubros.sql
   ```

2. **Compilar:**
   ```bash
   ./mvnw clean install
   ```

3. **Verificar que no hay errores de compilación**

4. **Ejecutar:**
   ```bash
   ./mvnw spring-boot:run
   ```

### Frontend
1. **Instalar dependencias** (si es necesario):
   ```bash
   npm install
   ```

2. **Verificar imports correctos** en todos los archivos

3. **Ejecutar:**
   ```bash
   npm start
   ```

---

## 📊 Flujo de Uso

1. Usuario accede a **Sistema Financiero**
2. Selecciona una **obra** de la lista
3. Click en tarjeta **"💰 Pagos a Cuenta"**
4. Se abre modal mostrando todos los **rubros** (Accordion)
5. Usuario expande un rubro (ej: "Albañilería")
6. Ve tabla con 3 items y sus saldos
7. Click en **"Pagar"** en Jornales
8. Se muestra formulario con:
   - Total presupuestado: $5,000,000
   - Total pagado: $1,000,000
   - Saldo pendiente: $4,000,000
9. Ingresa monto: $500,000
10. Selecciona método: "Transferencia"
11. Agrega observaciones
12. Click **"Registrar Pago"**
13. Sistema valida que $500,000 ≤ $4,000,000  ✅
14. Guarda pago en BD
15. Actualiza totales:
    - Total pagado: $1,500,000
    - Saldo pendiente: $3,500,000
16. Muestra notificación de éxito
17. Usuario puede ver historial completo

---

## 🎨 Características de UI

✅ **Diseño consistente** con el sistema existente (DarAdelantoModal)
✅ **Accordion** por rubro para mejor organización
✅ **Badges de estado** visuales (Sin pagos, X% pagado, ✓ Pagado)
✅ **Iconos descriptivos** (personas, caja, recibo)
✅ **Colores semánticos** (verde=pagado, rojo=pendiente, amarillo=parcial)
✅ **Validación en tiempo real** del monto
✅ **Formato de moneda** argentino
✅ **Historial completo** de pagos
✅ **Responsive** con Bootstrap
✅ **Loading states** con spinners
✅ **Notificaciones** de éxito/error

---

## 🔒 Validaciones Implementadas

### Backend:
- ✅ Empresa existe
- ✅ Presupuesto existe y pertenece a la empresa
- ✅ Rubro existe en el presupuesto
- ✅ Tipo de item es válido (JORNALES | MATERIALES | GASTOS_GENERALES)
- ✅ Monto > 0
- ✅ Monto ≤ Saldo Pendiente
- ✅ Multi-tenancy (empresa_id)

### Frontend:
- ✅ Presupuesto seleccionado
- ✅ Rubro e item seleccionados
- ✅ Monto numérico > 0
- ✅ Monto ≤ Saldo disponible
- ✅ Fecha válida
- ✅ Método de pago seleccionado

---

## 📈 Mejoras Futuras Sugeridas

1. **Filtros de historial**: Por fecha, rubro, método de pago
2. **Exportar a PDF/Excel**: Reportes de pagos
3. **Gráficos**: Visualización de pagos por rubro
4. **Notificaciones**: Alertas cuando queda poco saldo
5. **Adjuntos**: Subir comprobantes de pago
6. **Aprobación**: Workflow de aprobación para pagos grandes
7. **Dashboard**: Panel consolidado de pagos por empresa/obra

---

## 🐛 Solución de Problemas

### Error: "Rubro no encontrado"
- Verificar que el presupuesto tenga `itemsCalculadora` cargados
- Verificar que `tipoProfesional` coincida con el nombre del rubro

### Error: "Monto excede saldo pendiente"
- Recargar totales antes de registrar otro pago
- Verificar cálculos en `calcularMontoTotalItem()`

### Items no se muestran
- Verificar que el item tenga al menos un elemento en su array (jornales/materiales/gastos)
- Verificar que `total` o `subtotal` > 0

---

## 👨‍💻 Mantenimiento

- **Tablas involucradas**: `pagos_parciales_rubros`, `presupuestos_no_cliente`, `items_calculadora_presupuesto`
- **Índices**: Optimizados para consultas por presupuesto, rubro e item
- **Logging**: Implementado con @Slf4j en todos los servicios
- **Swagger**: Documentación automática en `/swagger-ui.html`

---

## ✅ Checklist de Implementación

- [x] Entity PagoParcialRubro creada
- [x] DTOs Request/Response creados
- [x] Repository con queries optimizadas
- [x] Service con lógica de negocio
- [x] Controller con endpoints REST
- [x] Script SQL de creación de tabla
- [x] Servicio frontend pagoCuentaService.js
- [x] Componente PagoCuentaModal.jsx
- [x] Integración en SistemaFinancieroPage
- [x] Manejo de errores y validaciones
- [x] Estilos consistentes con UI existente
- [ ] Testing unitario backend
- [ ] Testing E2E frontend
- [ ] Documentación de API actualizada
- [ ] Deploy a Railway (producción)

---

## 📞 Soporte

Para cualquier duda o problema:
1. Revisar logs del backend: `logs/spring.log`
2. Revisar console del navegador (F12)
3. Verificar endpoints con Postman/Swagger
4. Revisar este documento para flujo correcto

---

**Fecha de creación**: 12 de Marzo de 2026  
**Versión**: 1.0  
**Estado**: ✅ Implementado y listo para deploy
