# 💸 PROMPT PARA IA FRONTEND - SISTEMA DE ADELANTOS A PROFESIONALES

**Fecha:** 02/03/2026  
**Backend:** ✅ COMPLETAMENTE IMPLEMENTADO  
**Objetivo:** Implementar frontend completo para Sistema de Adelantos

---

## 🎯 RESUMEN EJECUTIVO

Implementar interfaz frontend completa para el **Sistema de Adelantos a Profesionales** que permita:

1. **Registrar adelantos** a profesionales de obra
2. **Visualizar adelantos activos** y su estado de descuento
3. **Ver descuentos automáticos** aplicados en pagos semanales
4. **Consultar historial** de adelantos completados
5. **Monitorear saldos pendientes** en tiempo real

El backend ya está 100% funcional con descuento automático implementado.

---

## 📡 ESPECIFICACIÓN TÉCNICA DEL BACKEND

### **Base URL**
```
http://localhost:8080/api/v1
```

### **Endpoint Principal**
```
POST   /pagos-profesional-obra          (Crear adelanto o pago)
GET    /pagos-profesional-obra/{id}      (Obtener por ID)
GET    /pagos-profesional-obra           (Listar con filtros)
PUT    /pagos-profesional-obra/{id}      (Actualizar)
DELETE /pagos-profesional-obra/{id}      (Eliminar/Cancelar)
```

---

## 📦 ESTRUCTURA DE DATOS

### **PagoProfesionalObraRequestDTO** (Para crear adelanto)

```json
{
  "profesionalObraId": 1,              // REQUERIDO - ID del profesional en la obra
  "empresaId": 1,                       // REQUERIDO - ID de la empresa
  "tipoPago": "ADELANTO",               // REQUERIDO - Valores: "SEMANAL", "QUINCENAL", "MENSUAL", "ADELANTO", "EXTRA", "AJUSTE"
  "esAdelanto": true,                   // REQUERIDO para adelantos - Boolean
  "periodoAdelanto": "1_SEMANA",        // REQUERIDO si esAdelanto=true - Valores: "1_SEMANA", "2_SEMANAS", "1_MES", "OBRA_COMPLETA"
  "montoBruto": 50000.00,               // REQUERIDO - Decimal(15,2)
  "fechaPago": "2026-03-02",            // REQUERIDO - Formato: YYYY-MM-DD
  "metodoPago": "EFECTIVO",             // REQUERIDO - Valores: "EFECTIVO", "TRANSFERENCIA", "CHEQUE"
  "comprobantePago": "COMP-001",        // OPCIONAL - String
  "observaciones": "Adelanto urgente"   // OPCIONAL - String
}
```

### **PagoProfesionalObraRequestDTO** (Para crear pago semanal)

```json
{
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "SEMANAL",
  "montoBruto": 100000.00,
  "diasTrabajados": 6,                  // REQUERIDO para SEMANAL
  "diasEsperados": 6,                   // REQUERIDO para SEMANAL
  "fechaPago": "2026-03-09",
  "metodoPago": "TRANSFERENCIA",
  "comprobantePago": "TRANS-123",
  "observaciones": "Pago semana 10"
}
```

### **PagoProfesionalObraResponseDTO** (Respuesta del backend)

```json
{
  "id": 1,
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "ADELANTO",
  "esAdelanto": true,
  "periodoAdelanto": "1_SEMANA",
  "estadoAdelanto": "ACTIVO",              // "ACTIVO", "COMPLETADO", "CANCELADO"
  "saldoAdelantoPorDescontar": 50000.00,   // Saldo pendiente actual
  "montoOriginalAdelanto": 50000.00,       // Monto original del adelanto
  "adelantosAplicadosIds": "[1, 2]",       // JSON - IDs de adelantos descontados en este pago
  "semanaReferencia": "2026-03-02",        // Fecha de referencia
  "montoBruto": 50000.00,
  "descuentoPresentismo": 0.00,
  "descuentoAdelantos": 0.00,              // Descuento aplicado por adelantos
  "otrosDescuentos": 0.00,
  "bonificaciones": 0.00,
  "montoFinal": 50000.00,                  // Monto neto a pagar
  "diasTrabajados": null,
  "diasEsperados": null,
  "porcentajePresentismo": 100.00,
  "fechaPago": "2026-03-02T00:00:00",
  "metodoPago": "EFECTIVO",
  "comprobantePago": "COMP-001",
  "observaciones": "Adelanto urgente",
  "createdAt": "2026-03-02T10:30:00",
  "updatedAt": "2026-03-02T10:30:00"
}
```

---

## 🔄 FLUJOS DE TRABAJO IMPLEMENTADOS EN BACKEND

### **Flujo 1: Crear Adelanto**

**Request:**
```http
POST /api/v1/pagos-profesional-obra
Content-Type: application/json

{
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "ADELANTO",
  "esAdelanto": true,
  "periodoAdelanto": "1_SEMANA",
  "montoBruto": 50000.00,
  "fechaPago": "2026-03-02",
  "metodoPago": "EFECTIVO"
}
```

**Backend ejecuta automáticamente:**
1. ✅ Valida datos requeridos
2. ✅ Crea registro en BD
3. ✅ Inicializa `estadoAdelanto = "ACTIVO"`
4. ✅ Inicializa `saldoAdelantoPorDescontar = 50000.00`
5. ✅ Inicializa `montoOriginalAdelanto = 50000.00`
6. ✅ Retorna ResponseDTO completo

**Response exitosa (201 Created):**
```json
{
  "id": 1,
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "ADELANTO",
  "esAdelanto": true,
  "periodoAdelanto": "1_SEMANA",
  "estadoAdelanto": "ACTIVO",
  "saldoAdelantoPorDescontar": 50000.00,
  "montoOriginalAdelanto": 50000.00,
  "adelantosAplicadosIds": null,
  "montoBruto": 50000.00,
  "montoFinal": 50000.00,
  "fechaPago": "2026-03-02T00:00:00",
  "metodoPago": "EFECTIVO",
  "createdAt": "2026-03-02T10:30:00"
}
```

---

### **Flujo 2: Crear Pago Semanal (Descuento Automático)**

**Request:**
```http
POST /api/v1/pagos-profesional-obra
Content-Type: application/json

{
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "SEMANAL",
  "montoBruto": 100000.00,
  "diasTrabajados": 6,
  "diasEsperados": 6,
  "fechaPago": "2026-03-09",
  "metodoPago": "TRANSFERENCIA"
}
```

**Backend ejecuta automáticamente:**
1. ✅ Crea pago semanal
2. ✅ **Busca adelantos activos** del profesional
3. ✅ **Calcula descuento** (máx 40% del monto disponible)
4. ✅ **Aplica descuento** proporcionalmente
5. ✅ **Actualiza saldos** de adelantos
6. ✅ **Marca COMPLETADO** si saldo = 0
7. ✅ **Registra IDs** en `adelantosAplicadosIds`
8. ✅ **Agrega observaciones** automáticas

**Response exitosa (201 Created):**
```json
{
  "id": 2,
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "SEMANAL",
  "esAdelanto": false,
  "montoBruto": 100000.00,
  "descuentoPresentismo": 0.00,
  "descuentoAdelantos": 40000.00,          // ⭐ DESCUENTO AUTOMÁTICO
  "montoFinal": 60000.00,                   // 100000 - 40000
  "diasTrabajados": 6,
  "diasEsperados": 6,
  "porcentajePresentismo": 100.00,
  "adelantosAplicadosIds": "[1]",           // ⭐ IDs descontados
  "fechaPago": "2026-03-09T00:00:00",
  "metodoPago": "TRANSFERENCIA",
  "observaciones": "💸 Descuento de adelantos aplicado: $40,000.00 (IDs: [1])",
  "createdAt": "2026-03-09T11:00:00"
}
```

**Adelanto actualizado (GET /api/v1/pagos-profesional-obra/1):**
```json
{
  "id": 1,
  "estadoAdelanto": "ACTIVO",
  "saldoAdelantoPorDescontar": 10000.00,    // ⭐ ACTUALIZADO (50000 - 40000)
  "montoOriginalAdelanto": 50000.00
}
```

---

### **Flujo 3: Segundo Pago Semanal (Completar Adelanto)**

**Request:**
```http
POST /api/v1/pagos-profesional-obra
Content-Type: application/json

{
  "profesionalObraId": 1,
  "empresaId": 1,
  "tipoPago": "SEMANAL",
  "montoBruto": 100000.00,
  "diasTrabajados": 6,
  "diasEsperados": 6,
  "fechaPago": "2026-03-16",
  "metodoPago": "TRANSFERENCIA"
}
```

**Backend ejecuta:**
1. ✅ Busca adelantos activos (encuentra adelanto ID 1 con saldo $10,000)
2. ✅ Calcula descuento máximo: $40,000
3. ✅ **Aplica solo $10,000** (no puede exceder saldo)
4. ✅ **Marca adelanto como COMPLETADO** ⭐
5. ✅ Retorna pago con descuento final

**Response:**
```json
{
  "id": 3,
  "tipoPago": "SEMANAL",
  "montoBruto": 100000.00,
  "descuentoAdelantos": 10000.00,           // ⭐ ÚLTIMO DESCUENTO
  "montoFinal": 90000.00,
  "adelantosAplicadosIds": "[1]",
  "observaciones": "💸 Descuento de adelantos aplicado: $10,000.00 (IDs: [1])"
}
```

**Adelanto completado (GET /api/v1/pagos-profesional-obra/1):**
```json
{
  "id": 1,
  "estadoAdelanto": "COMPLETADO",           // ⭐ COMPLETADO
  "saldoAdelantoPorDescontar": 0.00,        // ⭐ SALDO CERO
  "montoOriginalAdelanto": 50000.00
}
```

---

## 🎨 COMPONENTES FRONTEND A IMPLEMENTAR

### **1. Formulario de Registro de Adelanto**

**Ubicación sugerida:** Modal o página dentro de "Gestión de Pagos"

**Campos del formulario:**
```typescript
interface FormAdelanto {
  profesionalObraId: number;      // Select - Profesionales activos en la obra
  empresaId: number;              // Hidden - ID de la empresa actual
  periodoAdelanto: string;        // Select - ["1_SEMANA", "2_SEMANAS", "1_MES", "OBRA_COMPLETA"]
  montoBruto: number;             // Input Number - Validar > 0
  fechaPago: Date;                // DatePicker - Default: Hoy
  metodoPago: string;             // Select - ["EFECTIVO", "TRANSFERENCIA", "CHEQUE"]
  comprobantePago?: string;       // Input Text - Opcional
  observaciones?: string;         // Textarea - Opcional
}
```

**Validaciones:**
- ✅ Todos los campos son requeridos excepto comprobante y observaciones
- ✅ montoBruto debe ser mayor a 0
- ✅ No permitir fecha futura
- ✅ Confirmar antes de enviar (ej: "¿Registrar adelanto de $50,000 al profesional Juan Pérez?")

**Labels sugeridos:**
```
Profesional: [Select con nombre completo]
Período de adelanto: [Select: 1 semana / 2 semanas / 1 mes / Obra completa]
Monto: [$______]
Fecha de pago: [dd/mm/yyyy]
Método de pago: [Efectivo / Transferencia / Cheque]
Comprobante: [Opcional - Ej: COMP-001]
Observaciones: [Opcional - Textarea]
```

**Submit:**
```typescript
async function registrarAdelanto(form: FormAdelanto) {
  const requestDTO = {
    profesionalObraId: form.profesionalObraId,
    empresaId: form.empresaId,
    tipoPago: "ADELANTO",
    esAdelanto: true,
    periodoAdelanto: form.periodoAdelanto,
    montoBruto: form.montoBruto,
    fechaPago: formatDate(form.fechaPago), // "YYYY-MM-DD"
    metodoPago: form.metodoPago,
    comprobantePago: form.comprobantePago,
    observaciones: form.observaciones
  };

  const response = await fetch('http://localhost:8080/api/v1/pagos-profesional-obra', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(requestDTO)
  });

  if (response.ok) {
    const adelanto = await response.json();
    // Mostrar success: "Adelanto registrado exitosamente"
    // Actualizar lista de adelantos activos
    // Cerrar modal
  }
}
```

---

### **2. Tabla de Adelantos Activos**

**Ubicación:** Dashboard de profesional o sección "Adelantos"

**Consulta al backend:**
```typescript
async function obtenerAdelantosActivos(profesionalObraId: number) {
  // Filtrar por profesional + esAdelanto=true + estadoAdelanto=ACTIVO
  const params = new URLSearchParams({
    'profesionalObraId': profesionalObraId.toString(),
    // Nota: El backend actual no tiene filtro directo, implementar localmente o pedir endpoint específico
  });
  
  const response = await fetch(`http://localhost:8080/api/v1/pagos-profesional-obra?${params}`);
  const pagos = await response.json();
  
  // Filtrar localmente si es necesario
  return pagos.filter(p => 
    p.esAdelanto === true && 
    p.estadoAdelanto === 'ACTIVO' &&
    p.saldoAdelantoPorDescontar > 0
  );
}
```

**Columnas de la tabla:**
```
| ID | Fecha | Período | Monto Original | Saldo Pendiente | Progreso | Estado | Acciones |
|----|-------|---------|----------------|-----------------|----------|--------|----------|
| 1  | 02/03 | 1 Sem   | $50,000        | $10,000         | [====80%]| Activo | [Ver]   |
| 2  | 05/03 | 2 Sem   | $80,000        | $80,000         | [0%]     | Activo | [Ver]   |
```

**Cálculo del progreso:**
```typescript
function calcularProgreso(montoOriginal: number, saldoPendiente: number): number {
  return ((montoOriginal - saldoPendiente) / montoOriginal) * 100;
}
```

**Componente ejemplo (React/Vue/Angular):**
```typescript
interface AdelantoActivo {
  id: number;
  fechaPago: string;
  periodoAdelanto: string;
  montoOriginalAdelanto: number;
  saldoAdelantoPorDescontar: number;
  estadoAdelanto: string;
}

function TablaAdelantosActivos({ profesionalObraId }: { profesionalObraId: number }) {
  const [adelantos, setAdelantos] = useState<AdelantoActivo[]>([]);

  useEffect(() => {
    obtenerAdelantosActivos(profesionalObraId).then(setAdelantos);
  }, [profesionalObraId]);

  return (
    <table>
      <thead>
        <tr>
          <th>Fecha</th>
          <th>Período</th>
          <th>Monto Original</th>
          <th>Saldo Pendiente</th>
          <th>Progreso</th>
        </tr>
      </thead>
      <tbody>
        {adelantos.map(a => (
          <tr key={a.id}>
            <td>{formatDate(a.fechaPago)}</td>
            <td>{formatPeriodo(a.periodoAdelanto)}</td>
            <td>${formatMoney(a.montoOriginalAdelanto)}</td>
            <td>${formatMoney(a.saldoAdelantoPorDescontar)}</td>
            <td>
              <ProgressBar 
                value={calcularProgreso(a.montoOriginalAdelanto, a.saldoAdelantoPorDescontar)} 
              />
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}
```

---

### **3. Visualización de Descuentos en Pago Semanal**

**Ubicación:** Detalle de pago semanal o recibo de pago

**Componente de Desglose de Pago:**
```typescript
interface DetallePago {
  montoBruto: number;
  descuentoPresentismo: number;
  descuentoAdelantos: number;
  otrosDescuentos: number;
  bonificaciones: number;
  montoFinal: number;
  adelantosAplicadosIds: string; // JSON array como string
}

function DesglosePagoSemanal({ pago }: { pago: DetallePago }) {
  const adelantosIds = pago.adelantosAplicadosIds 
    ? JSON.parse(pago.adelantosAplicadosIds) 
    : [];

  return (
    <div className="desglose-pago">
      <h3>Desglose de Pago</h3>
      <div className="linea">
        <span>Monto Bruto:</span>
        <span className="monto">${formatMoney(pago.montoBruto)}</span>
      </div>
      
      {pago.descuentoPresentismo > 0 && (
        <div className="linea descuento">
          <span>- Descuento Presentismo:</span>
          <span className="monto">-${formatMoney(pago.descuentoPresentismo)}</span>
        </div>
      )}
      
      {pago.descuentoAdelantos > 0 && (
        <div className="linea descuento adelanto">
          <span>💸 Descuento Adelantos {adelantosIds.length > 0 && `(IDs: ${adelantosIds.join(', ')})`}:</span>
          <span className="monto">-${formatMoney(pago.descuentoAdelantos)}</span>
        </div>
      )}
      
      {pago.otrosDescuentos > 0 && (
        <div className="linea descuento">
          <span>- Otros Descuentos:</span>
          <span className="monto">-${formatMoney(pago.otrosDescuentos)}</span>
        </div>
      )}
      
      {pago.bonificaciones > 0 && (
        <div className="linea bonificacion">
          <span>+ Bonificaciones:</span>
          <span className="monto">+${formatMoney(pago.bonificaciones)}</span>
        </div>
      )}
      
      <div className="linea total">
        <span><strong>Monto Final a Pagar:</strong></span>
        <span className="monto"><strong>${formatMoney(pago.montoFinal)}</strong></span>
      </div>
    </div>
  );
}
```

**Estilos sugeridos (CSS):**
```css
.desglose-pago {
  border: 1px solid #ddd;
  padding: 16px;
  border-radius: 8px;
  background: #fafafa;
}

.linea {
  display: flex;
  justify-content: space-between;
  padding: 8px 0;
  border-bottom: 1px solid #eee;
}

.linea.descuento .monto {
  color: #d32f2f;
}

.linea.bonificacion .monto {
  color: #388e3c;
}

.linea.adelanto {
  background: #fff3e0;
  padding: 8px;
  margin: 4px -8px;
  border-radius: 4px;
}

.linea.total {
  margin-top: 12px;
  border-top: 2px solid #333;
  border-bottom: none;
  font-size: 1.1em;
}
```

---

### **4. Historial de Adelantos Completados**

**Consulta:**
```typescript
async function obtenerAdelantosCompletados(profesionalObraId: number) {
  const response = await fetch(`http://localhost:8080/api/v1/pagos-profesional-obra`);
  const pagos = await response.json();
  
  return pagos.filter(p => 
    p.profesionalObraId === profesionalObraId &&
    p.esAdelanto === true && 
    p.estadoAdelanto === 'COMPLETADO'
  ).sort((a, b) => new Date(b.fechaPago) - new Date(a.fechaPago)); // Más reciente primero
}
```

**Tabla:**
```
| Fecha | Monto | Período | Estado | Fecha Completado |
|-------|-------|---------|--------|------------------|
| 02/03 | $50k  | 1 Sem   | ✅     | 16/03            |
| 25/02 | $80k  | 2 Sem   | ✅     | 10/03            |
```

---

### **5. Dashboard / Tarjetas Resumen**

**Tarjetas informativas:**
```typescript
interface ResumenAdelantos {
  totalAdelantosActivos: number;
  montoTotalPendiente: number;
  promedioDescuentoSemanal: number;
  adelantosCompletadosMes: number;
}

function TarjetasResumen({ profesionalObraId }: { profesionalObraId: number }) {
  const [resumen, setResumen] = useState<ResumenAdelantos | null>(null);

  useEffect(() => {
    async function cargarResumen() {
      const activos = await obtenerAdelantosActivos(profesionalObraId);
      const totalPendiente = activos.reduce((sum, a) => sum + a.saldoAdelantoPorDescontar, 0);
      
      // Calcular promedio de descuentos de los últimos pagos
      const pagosRecientes = await obtenerPagosSemanalesRecientes(profesionalObraId, 4);
      const promedioDescuento = pagosRecientes.reduce((sum, p) => sum + p.descuentoAdelantos, 0) / pagosRecientes.length;
      
      setResumen({
        totalAdelantosActivos: activos.length,
        montoTotalPendiente: totalPendiente,
        promedioDescuentoSemanal: promedioDescuento,
        adelantosCompletadosMes: await contarCompletadosEnMes()
      });
    }
    
    cargarResumen();
  }, [profesionalObraId]);

  return (
    <div className="tarjetas-resumen">
      <Tarjeta 
        icono="💰"
        titulo="Adelantos Activos"
        valor={resumen?.totalAdelantosActivos}
        color="blue"
      />
      <Tarjeta 
        icono="📊"
        titulo="Saldo Pendiente"
        valor={`$${formatMoney(resumen?.montoTotalPendiente)}`}
        color="orange"
      />
      <Tarjeta 
        icono="📉"
        titulo="Descuento Semanal Promedio"
        valor={`$${formatMoney(resumen?.promedioDescuentoSemanal)}`}
        color="red"
      />
      <Tarjeta 
        icono="✅"
        titulo="Completados Este Mes"
        valor={resumen?.adelantosCompletadosMes}
        color="green"
      />
    </div>
  );
}
```

---

### **6. Integración en Formulario de Pago Semanal**

**Pre-visualización de descuento antes de crear:**
```typescript
function FormularioPagoSemanal({ profesionalObraId }: { profesionalObraId: number }) {
  const [montoBruto, setMontoBruto] = useState(0);
  const [descuentoEstimado, setDescuentoEstimado] = useState(0);
  const [adelantosActivos, setAdelantosActivos] = useState([]);

  useEffect(() => {
    obtenerAdelantosActivos(profesionalObraId).then(setAdelantosActivos);
  }, [profesionalObraId]);

  useEffect(() => {
    // Calcular descuento estimado del 40%
    if (adelantosActivos.length > 0 && montoBruto > 0) {
      const montoDisponible = montoBruto; // Simplificado, considerar presentismo
      const descuentoMaximo = montoDisponible * 0.40;
      const totalSaldoPendiente = adelantosActivos.reduce((sum, a) => sum + a.saldoAdelantoPorDescontar, 0);
      const descuento = Math.min(descuentoMaximo, totalSaldoPendiente);
      setDescuentoEstimado(descuento);
    } else {
      setDescuentoEstimado(0);
    }
  }, [montoBruto, adelantosActivos]);

  return (
    <form>
      <input 
        type="number" 
        value={montoBruto} 
        onChange={(e) => setMontoBruto(Number(e.target.value))}
        placeholder="Monto bruto"
      />
      
      {adelantosActivos.length > 0 && (
        <div className="alerta-adelantos">
          <p>⚠️ Este profesional tiene <strong>{adelantosActivos.length} adelanto(s) activo(s)</strong></p>
          <p>Saldo pendiente total: <strong>${formatMoney(adelantosActivos.reduce((s, a) => s + a.saldoAdelantoPorDescontar, 0))}</strong></p>
          {descuentoEstimado > 0 && (
            <p className="descuento-estimado">
              💸 Se aplicará un descuento estimado de: <strong>${formatMoney(descuentoEstimado)}</strong>
              <br />
              <small>Monto final aproximado: ${formatMoney(montoBruto - descuentoEstimado)}</small>
            </p>
          )}
        </div>
      )}
      
      {/* Resto del formulario */}
    </form>
  );
}
```

---

## 📊 ENDPOINTS ADICIONALES RECOMENDADOS

Aunque el backend funciona con el endpoint principal, **recomendar al backend** implementar estos endpoints específicos para mejor UX:

### **1. Obtener Adelantos Activos de un Profesional**
```
GET /api/v1/pagos-profesional-obra/adelantos/activos/profesional/{profesionalObraId}

Response:
[
  {
    "id": 1,
    "periodoAdelanto": "1_SEMANA",
    "montoOriginalAdelanto": 50000.00,
    "saldoAdelantoPorDescontar": 10000.00,
    "fechaPago": "2026-03-02",
    "estadoAdelanto": "ACTIVO"
  }
]
```

### **2. Obtener Resumen de Adelantos**
```
GET /api/v1/pagos-profesional-obra/adelantos/resumen/profesional/{profesionalObraId}

Response:
{
  "totalActivos": 2,
  "montoTotalPendiente": 90000.00,
  "totalCompletados": 5,
  "montoTotalRecuperado": 250000.00,
  "ultimoDescuentoAplicado": 40000.00,
  "fechaUltimoDescuento": "2026-03-09"
}
```

### **3. Cancelar Adelanto**
```
PUT /api/v1/pagos-profesional-obra/{id}/cancelar

Request: (vacío o con motivo)
{
  "motivoCancelacion": "Error en registro"
}

Response:
{
  "id": 1,
  "estadoAdelanto": "CANCELADO",
  "observaciones": "Cancelado: Error en registro"
}
```

---

## ⚠️ VALIDACIONES Y REGLAS DE NEGOCIO

### **Validaciones en Frontend**

1. **Al crear adelanto:**
   - ✅ No permitir monto <= 0
   - ✅ No permitir fecha futura
   - ✅ Validar que profesionalObraId exista
   - ✅ Confirmar acción antes de enviar
   - ⚠️ **OPCIONAL:** Validar que no haya otro adelanto activo del mismo profesional (regla de negocio a definir)

2. **Al crear pago semanal:**
   - ✅ Mostrar alerta si hay adelantos activos
   - ✅ Pre-visualizar descuento estimado
   - ✅ Confirmar si descuento es significativo (ej: > 30%)

3. **Al visualizar datos:**
   - ✅ Formatear montos con separadores de miles y 2 decimales
   - ✅ Formatear fechas según locale (DD/MM/YYYY)
   - ✅ Mostrar íconos/colores según estado:
     - 🟢 ACTIVO → verde
     - ✅ COMPLETADO → gris/verde oscuro
     - ❌ CANCELADO → rojo

### **Reglas del Backend (Ya implementadas)**

1. ✅ Descuento máximo: **40%** del monto disponible
2. ✅ Descuento aplicado solo en pagos **SEMANALES**
3. ✅ No se descuenta en pagos tipo **ADELANTO** (evita recursión)
4. ✅ Distribución proporcional entre múltiples adelantos activos
5. ✅ Cambio automático a **COMPLETADO** cuando saldo = 0
6. ✅ Ordenamiento FIFO (First In, First Out) por fecha de pago

---

## 🎯 CASOS DE USO COMPLETOS

### **Caso 1: Adelanto Simple**
```
Profesional: Juan Pérez
Adelanto: $50,000 a 1 semana
Pago semanal: $100,000

= Resultado:
  - Descuento: $40,000 (40% de $100,000)
  - Saldo adelanto: $10,000
  - Pago neto: $60,000
  
Siguiente pago semanal: $100,000
= Resultado:
  - Descuento: $10,000 (saldo restante)
  - Adelanto COMPLETADO
  - Pago neto: $90,000
```

### **Caso 2: Múltiples Adelantos**
```
Adelanto 1: $50,000 (Saldo: $50,000)
Adelanto 2: $30,000 (Saldo: $30,000)
Total pendiente: $80,000

Pago semanal: $100,000
Descuento máximo: $40,000 (40%)

Distribución proporcional:
  - Adelanto 1: $40,000 * (50,000/80,000) = $25,000
  - Adelanto 2: $40,000 * (30,000/80,000) = $15,000

= Resultado:
  - Adelanto 1: Saldo $25,000
  - Adelanto 2: Saldo $15,000
  - Pago neto: $60,000
```

### **Caso 3: Presentismo Bajo**
```
Adelanto: $50,000 (Saldo: $50,000)
Pago semanal: $100,000
Días trabajados: 4/6 (66.66% presentismo)
Descuento presentismo: $33,333.33

Monto disponible: $100,000 - $33,333.33 = $66,666.67
Descuento máximo adelanto: $66,666.67 * 40% = $26,666.67

= Resultado:
  - Descuento adelanto: $26,666.67
  - Saldo adelanto: $23,333.33
  - Pago neto: $40,000
```

---

## 🎨 DISEÑO UI/UX RECOMENDADO

### **Colores y Estados**

```css
/* Estados de Adelanto */
.estado-activo {
  color: #2196F3;
  background: #E3F2FD;
}

.estado-completado {
  color: #4CAF50;
  background: #E8F5E9;
}

.estado-cancelado {
  color: #F44336;
  background: #FFEBEE;
}

/* Alertas */
.alerta-adelantos-activos {
  background: #FFF3E0;
  border-left: 4px solid #FF9800;
  padding: 12px;
  margin: 16px 0;
}

/* Desglose de pago */
.descuento-adelantos {
  background: #E3F2FD;
  border-left: 4px solid #2196F3;
  padding: 8px;
}
```

### **Iconografía**

```
💰 Adelantos / Dinero
📊 Estadísticas / Resumen
📉 Descuentos
✅ Completado
🟢 Activo
❌ Cancelado
💸 Pago / Transferencia
📅 Fechas
👤 Profesional
🏗️ Obra
```

### **Responsive Design**

```typescript
// Mobile: Lista con cards
<div className="adelantos-grid">
  <Card>
    <h4>💰 Adelanto #1</h4>
    <p>Período: 1 semana</p>
    <p>Monto original: $50,000</p>
    <p>Saldo: $10,000</p>
    <ProgressBar value={80} />
  </Card>
</div>

// Desktop: Tabla completa
<Table>
  <tr>
    <td>1</td>
    <td>02/03/2026</td>
    <td>1 semana</td>
    <td>$50,000</td>
    <td>$10,000</td>
    <td><ProgressBar value={80} /></td>
    <td><Badge color="blue">Activo</Badge></td>
  </tr>
</Table>
```

---

## 🧪 TESTING FRONTEND

### **Escenarios de Prueba**

1. **Crear adelanto**
   - ✅ Crear con datos válidos
   - ❌ Intentar crear con monto 0
   - ❌ Intentar crear con fecha futura
   - ✅ Verificar que aparece en tabla de activos

2. **Crear pago semanal con adelanto activo**
   - ✅ Verificar alerta de adelantos activos
   - ✅ Verificar pre-visualización de descuento
   - ✅ Crear pago y verificar descuento aplicado
   - ✅ Verificar actualización de saldo en tabla

3. **Completar adelanto**
   - ✅ Crear pagos hasta completar saldo
   - ✅ Verificar que cambia a estado COMPLETADO
   - ✅ Verificar que desaparece de tabla de activos
   - ✅ Verificar que aparece en historial completados

4. **Múltiples adelantos**
   - ✅ Crear 2+ adelantos activos
   - ✅ Crear pago semanal
   - ✅ Verificar distribución proporcional

---

## 📚 UTILIDADES Y HELPERS

### **Formatters**

```typescript
// Formatear dinero
function formatMoney(amount: number): string {
  return new Intl.NumberFormat('es-AR', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  }).format(amount);
}

// Formatear fecha
function formatDate(date: string | Date): string {
  const d = typeof date === 'string' ? new Date(date) : date;
  return new Intl.DateTimeFormat('es-AR').format(d);
}

// Formatear período
function formatPeriodo(periodo: string): string {
  const map = {
    '1_SEMANA': '1 semana',
    '2_SEMANAS': '2 semanas',
    '1_MES': '1 mes',
    'OBRA_COMPLETA': 'Obra completa'
  };
  return map[periodo] || periodo;
}

// Formatear estado
function formatEstado(estado: string): { label: string; color: string; icon: string } {
  const map = {
    'ACTIVO': { label: 'Activo', color: 'blue', icon: '🟢' },
    'COMPLETADO': { label: 'Completado', color: 'green', icon: '✅' },
    'CANCELADO': { label: 'Cancelado', color: 'red', icon: '❌' }
  };
  return map[estado] || { label: estado, color: 'gray', icon: '⚪' };
}
```

### **Validadores**

```typescript
function validarFormularioAdelanto(form: FormAdelanto): string[] {
  const errores: string[] = [];
  
  if (!form.profesionalObraId) {
    errores.push('Debe seleccionar un profesional');
  }
  
  if (!form.montoBruto || form.montoBruto <= 0) {
    errores.push('El monto debe ser mayor a 0');
  }
  
  if (!form.periodoAdelanto) {
    errores.push('Debe seleccionar un período');
  }
  
  if (!form.fechaPago) {
    errores.push('Debe seleccionar una fecha');
  } else if (new Date(form.fechaPago) > new Date()) {
    errores.push('La fecha no puede ser futura');
  }
  
  if (!form.metodoPago) {
    errores.push('Debe seleccionar un método de pago');
  }
  
  return errores;
}
```

### **Calculadores**

```typescript
// Calcular progreso de adelanto
function calcularProgreso(montoOriginal: number, saldoPendiente: number): number {
  if (montoOriginal === 0) return 100;
  const descontado = montoOriginal - saldoPendiente;
  return (descontado / montoOriginal) * 100;
}

// Estimar descuento próximo pago
function estimarDescuentoProximoPago(
  adelantosActivos: AdelantoActivo[],
  montoBrutoPago: number,
  presentismo: number = 100
): number {
  const totalSaldoPendiente = adelantosActivos.reduce((sum, a) => sum + a.saldoAdelantoPorDescontar, 0);
  const montoDisponible = montoBrutoPago * (presentismo / 100);
  const descuentoMaximo = montoDisponible * 0.40;
  return Math.min(descuentoMaximo, totalSaldoPendiente);
}

// Estimar semanas restantes
function estimarSemanasRestantes(
  saldoPendiente: number,
  pagoSemanalPromedio: number
): number {
  const descuentoSemanalPromedio = pagoSemanalPromedio * 0.40;
  return Math.ceil(saldoPendiente / descuentoSemanalPromedio);
}
```

---

## 🔐 SEGURIDAD Y PERMISOS

### **Validaciones de Permisos**

```typescript
// Solo roles autorizados pueden crear adelantos
const ROLES_ADELANTOS = ['ADMIN', 'CONTADOR', 'GERENTE'];

function puedeCrearAdelanto(userRole: string): boolean {
  return ROLES_ADELANTOS.includes(userRole);
}

// Solo admin puede cancelar adelantos
function puedeCancelarAdelanto(userRole: string): boolean {
  return userRole === 'ADMIN';
}
```

### **Validación de Datos Sensibles**

```typescript
// No permitir adelantos mayores al presupuesto mensual estimado
async function validarMontoAdelanto(
  profesionalObraId: number,
  montoAdelanto: number
): Promise<boolean> {
  const profesional = await obtenerProfesionalObra(profesionalObraId);
  const pagoMensualEstimado = profesional.pagoSemanalPromedio * 4;
  
  if (montoAdelanto > pagoMensualEstimado) {
    throw new Error(`El adelanto no puede superar el pago mensual estimado ($${pagoMensualEstimado})`);
  }
  
  return true;
}
```

---

## 📱 NOTIFICACIONES Y FEEDBACK

### **Mensajes de Éxito**

```typescript
// Al crear adelanto
toast.success('✅ Adelanto registrado exitosamente');

// Al completar adelanto automáticamente
toast.info(`💸 Adelanto #${id} completado - Saldo recuperado completamente`);

// Al crear pago con descuento
toast.info(`💸 Descuento de $${descuento} aplicado automáticamente`);
```

### **Mensajes de Error**

```typescript
// Error de validación
toast.error('❌ El monto debe ser mayor a 0');

// Error de servidor
toast.error('❌ Error al registrar adelanto. Intente nuevamente.');

// Error de permiso
toast.error('🔒 No tiene permisos para realizar esta acción');
```

### **Confirmaciones**

```typescript
// Antes de crear adelanto
const confirmado = await confirm(
  '¿Registrar adelanto?',
  `Se registrará un adelanto de $${monto} a ${nombreProfesional}. Esta acción no se puede deshacer.`
);

// Antes de cancelar adelanto
const confirmado = await confirm(
  '¿Cancelar adelanto?',
  'El adelanto será marcado como cancelado. Los descuentos ya aplicados no serán revertidos.'
);
```

---

## 🚀 PLAN DE IMPLEMENTACIÓN SUGERIDO

### **Fase 1: Core Functionality (1-2 días)**
1. ✅ Crear servicio API para adelantos
2. ✅ Implementar formulario de registro de adelanto
3. ✅ Implementar tabla de adelantos activos
4. ✅ Integrar en formulario de pago semanal (alerta)

### **Fase 2: Visualización (1 día)**
1. ✅ Implementar desglose de pago con descuentos
2. ✅ Crear componente de progreso de adelanto
3. ✅ Implementar historial de completados

### **Fase 3: UX Avanzada (1 día)**
1. ✅ Dashboard con tarjetas resumen
2. ✅ Pre-visualización de descuentos
3. ✅ Estimador de semanas restantes

### **Fase 4: Testing y Ajustes (1 día)**
1. ✅ Testing de flujos completos
2. ✅ Ajustes de UI/UX
3. ✅ Validaciones adicionales

---

## 📞 SOPORTE Y CONSULTAS

### **Endpoints de Verificación**

```bash
# Verificar adelanto creado
curl http://localhost:8080/api/v1/pagos-profesional-obra/1

# Listar todos los pagos
curl http://localhost:8080/api/v1/pagos-profesional-obra

# Crear adelanto de prueba
curl -X POST http://localhost:8080/api/v1/pagos-profesional-obra \
  -H "Content-Type: application/json" \
  -d '{
    "profesionalObraId": 1,
    "empresaId": 1,
    "tipoPago": "ADELANTO",
    "esAdelanto": true,
    "periodoAdelanto": "1_SEMANA",
    "montoBruto": 50000,
    "fechaPago": "2026-03-02",
    "metodoPago": "EFECTIVO"
  }'
```

### **Consultas SQL para Desarrollo**

```sql
-- Ver adelantos activos
SELECT * FROM pagos_profesional_obra 
WHERE es_adelanto = true AND estado_adelanto = 'ACTIVO';

-- Ver pagos con descuentos
SELECT * FROM pagos_profesional_obra 
WHERE descuento_adelantos > 0
ORDER BY fecha_pago DESC;

-- Ver progreso de adelanto específico
SELECT 
  id,
  monto_original_adelanto as original,
  saldo_adelanto_por_descontar as saldo,
  (monto_original_adelanto - saldo_adelanto_por_descontar) as descontado,
  ROUND((monto_original_adelanto - saldo_adelanto_por_descontar) / monto_original_adelanto * 100, 2) as progreso_pct
FROM pagos_profesional_obra
WHERE id = 1;
```

---

## ✅ CHECKLIST DE IMPLEMENTACIÓN

```
Frontend Adelantos - Checklist:

UI Components:
[ ] Formulario de registro de adelanto
[ ] Tabla de adelantos activos
[ ] Tarjetas de resumen
[ ] Desglose de pago con descuentos
[ ] Historial de completados
[ ] Componente de progreso (progress bar)
[ ] Badges de estado
[ ] Alertas de adelantos activos

Services/API:
[ ] Función obtenerAdelantosActivos()
[ ] Función registrarAdelanto()
[ ] Función obtenerResumen()
[ ] Función obtenerHistorialCompletados()
[ ] Función estimarDescuento()

Validaciones:
[ ] Validación de formulario
[ ] Validación de permisos
[ ] Validación de montos
[ ] Validación de fechas

UX/UI:
[ ] Formateo de montos
[ ] Formateo de fechas
[ ] Formateo de estados
[ ] Responsive design (mobile/desktop)
[ ] Loading states
[ ] Error handling
[ ] Success messages

Testing:
[ ] Test: Crear adelanto
[ ] Test: Visualizar activos
[ ] Test: Pre-visualización descuento
[ ] Test: Pago con descuento automático
[ ] Test: Completar adelanto
[ ] Test: Múltiples adelantos
[ ] Test: Validaciones
```

---

## 🎉 RESULTADO ESPERADO

Al finalizar la implementación, el usuario debe poder:

1. ✅ **Registrar adelantos** desde el frontend con todos los campos necesarios
2. ✅ **Visualizar adelantos activos** con su progreso de descuento en tiempo real
3. ✅ **Ver alertas automáticas** al crear pagos semanales cuando hay adelantos activos
4. ✅ **Consultar historial** de adelantos completados con fechas y montos
5. ✅ **Estimar impacto** de descuentos antes de crear pagos
6. ✅ **Monitorear estado financiero** con dashboard de resumen

El sistema debe ser:
- 🎯 **Intuitivo:** Fácil de usar y entender
- 🔒 **Seguro:** Con validaciones y permisos adecuados
- 📊 **Informativo:** Con visualizaciones claras del estado
- ⚡ **Rápido:** Sin carga innecesaria de datos
- 💪 **Robusto:** Con manejo de errores completo
- 📱 **Responsive:** Funcional en mobile y desktop

---

**🎊 BACKEND 100% LISTO - FRONTEND POR IMPLEMENTAR 🎊**

---

## 📎 ANEXOS

### **Ejemplo Completo de Flujo API**

```typescript
// 1. CREAR ADELANTO
const adelanto = await fetch('http://localhost:8080/api/v1/pagos-profesional-obra', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    profesionalObraId: 1,
    empresaId: 1,
    tipoPago: 'ADELANTO',
    esAdelanto: true,
    periodoAdelanto: '1_SEMANA',
    montoBruto: 50000,
    fechaPago: '2026-03-02',
    metodoPago: 'EFECTIVO'
  })
}).then(r => r.json());

console.log('Adelanto creado:', adelanto);
// { id: 1, estadoAdelanto: 'ACTIVO', saldoAdelantoPorDescontar: 50000, ... }

// 2. LISTAR ADELANTOS ACTIVOS
const pagos = await fetch('http://localhost:8080/api/v1/pagos-profesional-obra')
  .then(r => r.json());

const activos = pagos.filter(p => 
  p.profesionalObraId === 1 && 
  p.esAdelanto === true && 
  p.estadoAdelanto === 'ACTIVO'
);

console.log('Adelantos activos:', activos);

// 3. CREAR PAGO SEMANAL (descuento automático)
const pagoSemanal = await fetch('http://localhost:8080/api/v1/pagos-profesional-obra', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    profesionalObraId: 1,
    empresaId: 1,
    tipoPago: 'SEMANAL',
    montoBruto: 100000,
    diasTrabajados: 6,
    diasEsperados: 6,
    fechaPago: '2026-03-09',
    metodoPago: 'TRANSFERENCIA'
  })
}).then(r => r.json());

console.log('Pago semanal con descuento:', pagoSemanal);
// { 
//   descuentoAdelantos: 40000, 
//   adelantosAplicadosIds: '[1]',
//   montoFinal: 60000,
//   observaciones: '💸 Descuento de adelantos aplicado: $40,000.00 (IDs: [1])'
// }

// 4. VERIFICAR ADELANTO ACTUALIZADO
const adelantoActualizado = await fetch(`http://localhost:8080/api/v1/pagos-profesional-obra/${adelanto.id}`)
  .then(r => r.json());

console.log('Adelanto actualizado:', adelantoActualizado);
// { saldoAdelantoPorDescontar: 10000, estadoAdelanto: 'ACTIVO' }

// 5. CREAR SEGUNDO PAGO (completar adelanto)
const pagoSemanal2 = await fetch('http://localhost:8080/api/v1/pagos-profesional-obra', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({
    profesionalObraId: 1,
    empresaId: 1,
    tipoPago: 'SEMANAL',
    montoBruto: 100000,
    diasTrabajados: 6,
    diasEsperados: 6,
    fechaPago: '2026-03-16',
    metodoPago: 'TRANSFERENCIA'
  })
}).then(r => r.json());

console.log('Segundo pago:', pagoSemanal2);
// { descuentoAdelantos: 10000, montoFinal: 90000 }

// 6. VERIFICAR ADELANTO COMPLETADO
const adelantoCompletado = await fetch(`http://localhost:8080/api/v1/pagos-profesional-obra/${adelanto.id}`)
  .then(r => r.json());

console.log('Adelanto completado:', adelantoCompletado);
// { saldoAdelantoPorDescontar: 0, estadoAdelanto: 'COMPLETADO' }
```

---

**FIN DEL PROMPT - COPIA ESTE ARCHIVO COMPLETO Y PROPORCIÓNALO A LA IA DEL FRONTEND**
