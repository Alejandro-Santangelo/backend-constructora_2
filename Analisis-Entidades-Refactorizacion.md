# 📋 Análisis de Entidades para Refactorización - Backend Constructora
**Última actualización:** 14 de Enero 2026 | **Versión:** 3.0

## 📊 Vista Rápida

| Nivel | Dificultad | Entidades | Líneas Totales | Tiempo Estimado |
|-------|-----------|-----------|----------------|-----------------|
| 🟢 1 | Muy Fácil | 5 | 711 | 6-8 días |
| 🟡 2 | Fácil-Medio | 5 | 1,073 | 10-12 días |
| 🟠 3 | Medio | 7 | 1,663 | 14-17 días |
| 🔴 4 | Difícil | 5 | 2,760 | 20-23 días |
| 🔴🔴 5 | Muy Difícil | 8 | 3,716 | 31-36 días |
| 🔴🔴🔴 6 | **CRÍTICO** | 2 | 5,561 | 17-22 días |
| **TOTAL** | | **32** | **15,484** | **14-18 semanas** |

### ⚡ Inicio Recomendado:
1. **Usuario** (175 líneas) → Más fácil, ideal para familiarizarse con el patrón
2. **Empresa** (171 líneas) → Base del sistema multi-tenant
3. **Proveedor** (142 líneas) → Entidad simple CRUD

### ⚠️ Advertencia Crítica:
- **PresupuestoNoCliente**: 4,471 líneas (¡10x el promedio!) - Planificar con cuidado
- **Obra**: Entidad central que conecta todo el sistema

---

## 📌 Orden Recomendado: De Más Fácil a Más Difícil

Este análisis está basado en la revisión completa del código actual del backend. Las líneas de código y relaciones han sido verificadas directamente desde el repositorio.

---

## 🟢 **NIVEL 1 - ENTIDADES SIMPLES (Sin o pocas relaciones)**

### 1. **Usuario** ⭐ COMIENZA AQUÍ
- **Archivos**: `Usuario.java`, `UsuarioController.java`, `UsuarioService.java`, `UsuarioRepository.java`
- **Complejidad**: ⭐ Muy Baja
- **Relaciones**: Solo con `Empresa` (ManyToOne)
- **Líneas de Servicio**: 175 líneas
- **Líneas de Entidad**: 89 líneas
- **Motivo**: Entidad independiente, lógica simple de autenticación y roles (admin, manager, user, viewer), sin dependencias críticas con otras entidades del dominio de construcción. Sistema de permisos bien definido.

### 2. **Empresa**
- **Archivos**: `Empresa.java`, `EmpresaController.java`, `EmpresaService.java`, `IEmpresaService.java`, `EmpresaRepository.java`
- **Complejidad**: ⭐ Muy Baja
- **Relaciones**: Con `Cliente` (ManyToMany), `Usuario` (OneToMany), `PresupuestoNoCliente` (OneToMany)
- **Líneas de Servicio**: 171 líneas
- **Líneas de Entidad**: 96 líneas
- **Motivo**: Es la base del sistema multi-tenant pero tiene lógica simple CRUD, sin cálculos complejos. Gestiona datos básicos de empresas (nombre, CUIT, datos fiscales).

### 3. **Proveedor**
- **Archivos**: `Proveedor.java`, `ProveedorController.java`, `ProveedorService.java`, `ProveedorRepository.java`
- **Complejidad**: ⭐ Muy Baja
- **Relaciones**: Con `Empresa` (empresaId), `PedidoPago` (OneToMany)
- **Líneas de Servicio**: 142 líneas
- **Motivo**: Entidad maestro simple, solo almacena datos de proveedores sin lógica compleja. CRUD básico.

### 4. **GastoGeneral**
- **Archivos**: `GastoGeneral.java`, `GastoGeneralController.java`, `StockGastoGeneralService.java`, `GastoGeneralRepository.java`
- **Complejidad**: ⭐ Muy Baja
- **Relaciones**: Con `Empresa` (empresaId), `StockGastoGeneral` (OneToMany)
- **Líneas de Servicio**: 115 líneas
- **Motivo**: Catálogo simple de gastos generales (alquiler, servicios, etc.), sin lógica de negocio compleja.

### 5. **Material**
- **Archivos**: `Material.java`, `MaterialController.java`, `MaterialService.java`, `MaterialRepository.java`
- **Complejidad**: ⭐ Muy Baja
- **Relaciones**: Con `StockMaterial` (OneToMany), `MovimientoMaterial` (OneToMany), `ObraMaterial` (OneToMany)
- **Líneas de Servicio**: 108 líneas
- **Líneas de Entidad**: 94 líneas
- **Motivo**: Entidad catálogo con relaciones hacia stocks y movimientos, pero sin lógica financiera compleja. CRUD básico de materiales con precios unitarios.

---

## 🟡 **NIVEL 2 - ENTIDADES MODERADAS (Relaciones limitadas)**

### 6. **Cliente**
- **Archivos**: `Cliente.java`, `ClienteController.java`, `ClienteService.java`, `IClienteService.java`, `ClienteRepository.java`
- **Complejidad**: ⭐⭐ Baja-Media
- **Relaciones**: Con `Empresa` (ManyToMany), `Obra` (OneToMany)
- **Líneas de Servicio**: 186 líneas
- **Líneas de Entidad**: 111 líneas
- **Motivo**: Tiene relación ManyToMany con Empresa y es base para Obras, pero la lógica es manejable. CRUD con filtros multi-tenant.

### 7. **Honorario**
- **Archivos**: `Honorario.java`, `HonorarioController.java`, `HonorarioService.java`, `HonorarioRepository.java`
- **Complejidad**: ⭐⭐ Media
- **Relaciones**: Con `Obra` (ManyToOne), `Profesional` (ManyToOne)
- **Líneas de Servicio**: 205 líneas
- **Motivo**: Registra pagos de honorarios pero no tiene cálculos complejos, solo validaciones y tracking. Maneja estados de pago.

### 8. **Costo**
- **Archivos**: `Costo.java`, `CostoController.java`, `CostoService.java`, `CostoRepository.java`
- **Complejidad**: ⭐⭐ Media
- **Relaciones**: Con `Obra` (ManyToOne)
- **Líneas de Servicio**: 284 líneas
- **Motivo**: Tiene lógica de estados y validaciones, gestión de tipos de costos (MATERIALES, MANO_OBRA, EQUIPAMIENTO, etc.). Sistema de estados robusto.

### 9. **AsignacionProfesionalObra**
- **Archivos**: `AsignacionProfesionalObra.java`, `AsignacionProfesionalObraService.java`, `AsignacionProfesionalObraRepository.java`
- **Complejidad**: ⭐⭐ Media
- **Relaciones**: Con `ProfesionalObra`, `AsignacionProfesionalDia`
- **Líneas de Servicio**: 210 líneas
- **Motivo**: Gestiona asignaciones de profesionales a obras, pero es más simple que ProfesionalObra. No tiene lógica financiera compleja.

### 10. **GastoObraProfesional**
- **Archivos**: `GastoObraProfesional.java`, `GastoObraProfesionalController.java`, `GastoObraProfesionalService.java`, `GastoObraProfesionalRepository.java`
- **Complejidad**: ⭐⭐ Media
- **Relaciones**: Con `Obra`, `Profesional`
- **Líneas de Servicio**: 188 líneas
- **Motivo**: Gestiona gastos específicos de profesionales en obras. Lógica moderada de validaciones y tracking.

---

## 🟠 **NIVEL 3 - ENTIDADES INTERMEDIAS (Múltiples relaciones)**

### 11. **Profesional**
- **Archivos**: `Profesional.java`, `ProfesionalController.java`, `ProfesionalService.java`, `IProfesionalService.java`, `ProfesionalRepository.java`
- **Complejidad**: ⭐⭐⭐ Media
- **Relaciones**: Con `ProfesionalObra` (OneToMany), `Honorario` (OneToMany)
- **Líneas de Servicio**: 302 líneas
- **Líneas de Entidad**: 226 líneas
- **Motivo**: Tiene campos de cálculo (honorarios, porcentajes, horas), tipos de profesional (ARQUITECTO, MAESTRO_MAYOR, etc.). Cálculos de costos pero manejables.

### 12. **StockMaterial**
- **Archivos**: `StockMaterial.java`, `StockMaterialController.java`, `StockMaterialService.java`, `StockMaterialRepository.java`
- **Complejidad**: ⭐⭐⭐ Media
- **Relaciones**: Con `Material` (ManyToOne), `Empresa` (empresaId), `MovimientoMaterial` (OneToMany implícito)
- **Líneas de Servicio**: 220 líneas
- **Motivo**: Maneja inventario con lógica de stock mínimo/máximo, alertas de reabastecimiento, pero sin transacciones financieras complejas.

### 13. **MovimientoMaterial**
- **Archivos**: `MovimientoMaterial.java`, `MovimientoMaterialController.java`, `MovimientoMaterialService.java`, `MovimientoMaterialRepository.java`
- **Complejidad**: ⭐⭐⭐ Media
- **Relaciones**: Con `StockMaterial` (ManyToOne), `Material` (ManyToOne), `Empresa` (empresaId), `Obra` (obraId)
- **Líneas de Servicio**: 156 líneas
- **Motivo**: Gestiona entradas/salidas de stock con cálculos de valores, tipos (ENTRADA, SALIDA, AJUSTE), requiere sincronización con Stock.

### 14. **AsistenciaObra**
- **Archivos**: `AsistenciaObra.java`, `AsistenciaObraController.java`, `AsistenciaObraService.java`, `AsistenciaObraRepository.java`
- **Complejidad**: ⭐⭐⭐ Media
- **Relaciones**: Con `Obra`, `Profesional`, `Empresa`
- **Líneas de Servicio**: 217 líneas
- **Motivo**: Gestiona registro de asistencias diarias con validaciones de fechas y profesionales. Sistema de tracking simple pero efectivo.

### 15. **CajaChicaObra**
- **Archivos**: `CajaChicaObra.java`, `CajaChicaObraController.java`, `CajaChicaObraService.java`, `ICajaChicaObraService.java`, `CajaChicaObraRepository.java`
- **Complejidad**: ⭐⭐⭐ Media
- **Relaciones**: Con `Obra`, `PresupuestoNoCliente`, `ProfesionalObra`, `Empresa`, `CajaChicaMovimiento` (OneToMany)
- **Líneas de Servicio**: 106 líneas (+ CajaChicaService 127 líneas)
- **Motivo**: Maneja estados y movimientos de fondos de caja chica, pero la lógica es contenida. Estados: ACTIVA, CERRADA.

### 16. **Jornal**
- **Archivos**: `Jornal.java`, `JornalController.java`, `JornalService.java`, `JornalRepository.java`
- **Complejidad**: ⭐⭐⭐ Media-Alta
- **Relaciones**: Con `ProfesionalObra` (ManyToOne), indirectamente con `Obra`, `Profesional`
- **Líneas de Servicio**: 243 líneas
- **Motivo**: Cálculos de horas trabajadas, valores por día, requiere validaciones cruzadas con asignaciones. Maneja diferentes tipos de jornales.

### 17. **PedidoPago**
- **Archivos**: `PedidoPago.java`, `PedidoPagoController.java`, `PedidoPagoService.java`, `PedidoPagoRepository.java`
- **Complejidad**: ⭐⭐⭐ Media-Alta
- **Relaciones**: Con `Proveedor` (ManyToOne), `Obra` (ManyToOne), `Empresa` (empresaId)
- **Líneas de Servicio**: 292 líneas
- **Motivo**: Maneja flujo de estados complejo (BORRADOR → APROBADO → AUTORIZADO → PAGADO) con validaciones múltiples y fechas de vencimiento.

---

## 🔴 **NIVEL 4 - ENTIDADES COMPLEJAS (Muchas dependencias)**

### 18. **ObraMaterial**
- **Archivos**: `ObraMaterial.java`, `ObraMaterialController.java`, `ObraMaterialService.java`, `IObraMaterialService.java`, `ObraMaterialRepository.java`
- **Complejidad**: ⭐⭐⭐⭐ Alta
- **Relaciones**: Con `Obra` (ManyToOne), `Material` (ManyToOne)
- **Líneas de Servicio**: 298 líneas
- **Motivo**: Vincula materiales a obras con cantidades y costos, debe sincronizar con stock y presupuestos. Gestiona asignaciones y consumos.

### 19. **EtapaDiaria**
- **Archivos**: `EtapaDiaria.java`, `EtapaDiariaController.java`, `EtapaDiariaService.java`, `IEtapaDiariaService.java`, `EtapaDiariaRepository.java`, `EtapasDiariasService.java`
- **Complejidad**: ⭐⭐⭐⭐ Alta
- **Relaciones**: Con `Obra`, `Empresa`, `TareaEtapaDiaria` (OneToMany), `ProfesionalTareaEtapa` (relaciones complejas)
- **Líneas de Servicio**: 319 líneas (EtapaDiariaService) + 288 líneas (EtapasDiariasService) = 607 líneas
- **Motivo**: Gestiona avances diarios con tareas asociadas, profesionales asignados a tareas, tiene lógica de estado y validaciones de fechas únicas. Sistema complejo de tracking de progreso.

### 20. **RetiroPersonal**
- **Archivos**: `RetiroPersonal.java`, `RetiroPersonalController.java`, `RetiroPersonalService.java`, `RetiroPersonalRepository.java`
- **Complejidad**: ⭐⭐⭐⭐ Alta
- **Relaciones**: Con `ProfesionalObra`, `Empresa`
- **Líneas de Servicio**: 306 líneas
- **Motivo**: Maneja adelantos/retiros con estados (PENDIENTE, APROBADO, RECHAZADO, PAGADO) y validaciones financieras. Afecta cálculos de pagos.

### 21. **AsignacionSemanal**
- **Archivos**: `AsignacionSemanalController.java`, `AsignacionSemanalService.java`, `AsignacionProfesionalDiaRepository.java`
- **Complejidad**: ⭐⭐⭐⭐ Alta
- **Relaciones**: Con `ProfesionalObra`, `AsignacionProfesionalDia`, `AsignacionProfesionalObra`
- **Líneas de Servicio**: 397 líneas
- **Motivo**: Gestiona planificación semanal de profesionales, requiere validaciones temporales complejas, cálculos agregados semanales, y coordinación entre múltiples entidades.

### 22. **TrabajoExtra**
- **Archivos**: `TrabajoExtra.java`, `TrabajoExtraController.java`, `TrabajoExtraService.java`, `TrabajoExtraRepository.java`, `TrabajoExtraItemCalculadoraService.java`
- **Complejidad**: ⭐⭐⭐⭐ Alta
- **Relaciones**: Con `Obra`, `Cliente`, `Empresa`, `TrabajoExtroDia` (OneToMany), `TrabajoExtroProfesional` (OneToMany), `TrabajoExtraTarea` (OneToMany), múltiples calculadoras (Material, Jornal, Item, ProfesionalCalculadora)
- **Líneas de Servicio**: 937 líneas (TrabajoExtraService) + 215 líneas (TrabajoExtraItemCalculadoraService) = 1,152 líneas
- **Líneas de Entidad**: 400 líneas
- **Motivo**: **Mini-presupuestos complejos** con estructura completa: días, profesionales, tareas, materiales, jornales. Estados (BORRADOR, APROBADO, RECHAZADO, PAGADO). Generación de PDFs. Cálculos financieros complejos con múltiples calculadoras.

---

## 🔴🔴 **NIVEL 5 - ENTIDADES MUY COMPLEJAS (Núcleo del sistema)**

### 23. **ProfesionalObra**
- **Archivos**: `ProfesionalObra.java`, `ProfesionalObraController.java`, `ProfesionalObraService.java`, `IProfesionalObraService.java`, `ProfesionalObraRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `Profesional` (ManyToOne), `Obra` (ManyToOne), `Jornal` (OneToMany), `PagoProfesionalObra` (OneToMany), `RetiroPersonal` (OneToMany), `AsignacionProfesionalObra` (OneToMany), `GastoObraProfesional` (OneToMany)
- **Líneas de Servicio**: 651 líneas
- **Motivo**: Gestiona asignaciones de profesionales con estados complejos (ACTIVO, INACTIVO, SUSPENDIDO), modalidades (JORNAL_DIARIO, PORCENTAJE_OBRA, PRECIO_FIJO, CAJA_CHICA), cálculos de pagos, retiros, gastos. Lógica compleja de tracking y sincronización.

### 24. **CobroObra**
- **Archivos**: `CobroObra.java`, `CobroObraController.java`, `CobroObraService.java`, `ICobroObraService.java`, `CobroObraRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `Obra` (ManyToOne nullable), `PresupuestoNoCliente` (ManyToOne), `Empresa`, `AsignacionCobroObra` (OneToMany)
- **Líneas de Servicio**: 577 líneas
- **Motivo**: Maneja cobros al cliente con estados complejos (PENDIENTE, COBRADO, VENCIDO, PARCIAL), puede existir sin Obra (solo presupuesto), tiene lógica de vencimientos, tracking financiero y distribución de pagos.

### 25. **CobroEmpresa**
- **Archivos**: `CobroEmpresa.java`, `CobroEmpresaController.java`, `CobroEmpresaService.java`, `CobroEmpresaRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `Empresa`, `AsignacionCobroEmpresaObra` (OneToMany)
- **Líneas de Servicio**: 629 líneas
- **Motivo**: Gestiona consolidación de cobros desde múltiples fuentes, distribución proporcional entre obras, lógica financiera compleja, estados (PENDIENTE, COBRADO, DISTRIBUIDO).

### 26. **AsignacionCobroObra**
- **Archivos**: `AsignacionCobroObra.java`, `AsignacionCobroObraController.java`, `AsignacionCobroObraService.java`, `IAsignacionCobroObraService.java`, `AsignacionCobroObraRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `CobroObra`, `CobroEmpresa`, `Obra`
- **Líneas de Servicio**: 207 líneas
- **Motivo**: Vincula cobros de empresa con cobros de obra específica, lógica de distribución financiera, validaciones cruzadas. Pieza clave en el sistema financiero.

### 27. **PagoProfesionalObra**
- **Archivos**: `PagoProfesionalObra.java`, `PagoProfesionalObraController.java`, `PagoProfesionalObraService.java`, `IPagoProfesionalObraService.java`, `PagoProfesionalObraRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `ProfesionalObra`, múltiples cálculos agregados (jornales, retiros, descuentos)
- **Líneas de Servicio**: 426 líneas
- **Motivo**: Gestiona pagos a profesionales con cálculos complejos: suma de jornales, descuento de retiros, cálculo de saldos, estados de pago (PENDIENTE, PAGADO, PARCIAL). Sistema financiero crítico.

### 28. **PagoTrabajoExtraObra**
- **Archivos**: `PagoTrabajoExtraObra.java`, `PagoTrabajoExtraObraController.java`, `PagoTrabajoExtraObraService.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `TrabajoExtra`, `Obra`, `Empresa`
- **Líneas de Servicio**: 353 líneas
- **Motivo**: Pagos asociados a trabajos extra con lógica de estado (PENDIENTE, APROBADO, PAGADO), validaciones cruzadas con TrabajoExtra y Obra. Gestiona pagos parciales y totales.

### 29. **PagoConsolidado**
- **Archivos**: `PagoConsolidado.java`, `PagoConsolidadoController.java`, `PagoConsolidadoService.java`, `PagoConsolidadoRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Consolida múltiples tipos de pagos (profesionales, trabajos extra, gastos generales, etc.)
- **Líneas de Servicio**: 665 líneas
- **Motivo**: **Agregación compleja** de diferentes fuentes de pago, reportes consolidados por obra/profesional/periodo, cálculos financieros totales. Sistema de reporting crítico.

### 30. **ObraFinanciero**
- **Archivos**: `ObraFinancieroController.java`, `ObraFinancieroService.java`, `IObraFinancieroService.java`
- **Complejidad**: ⭐⭐⭐⭐⭐ Muy Alta
- **Relaciones**: Con `Obra`, calcula datos de múltiples entidades (Cobros, Pagos, Costos, Materiales, etc.)
- **Líneas de Servicio**: 208 líneas
- **Motivo**: **Servicio de cálculos financieros** que agrega datos de Obra, Cobros, Pagos, Costos, Materiales. Genera resúmenes financieros completos. Lógica de negocio crítica.

---

## 🔴🔴🔴 **NIVEL 6 - ENTIDADES CRÍTICAS (Corazón del sistema)**

### 31. **Obra**
- **Archivos**: `Obra.java`, `ObraController.java`, `ObraService.java`, `IObraService.java`, `ObraRepository.java`
- **Complejidad**: ⭐⭐⭐⭐⭐⭐ Crítica
- **Relaciones**: Con `Cliente`, `PresupuestoNoCliente`, `Costo`, `Honorario`, `CobroObra`, `PedidoPago`, `ProfesionalObra`, `ObraMaterial`, `TrabajoExtra`, `EtapaDiaria`, `AsistenciaObra`, `CajaChicaObra` y más.
- **Líneas de Servicio**: 359 líneas
- **Líneas de Entidad**: 243 líneas
- **Motivo**: **ENTIDAD CENTRAL** del sistema. Vincula prácticamente todas las demás entidades. Tiene lógica de estados complejos (PLANIFICADA, EN_PROCESO, SUSPENDIDA, FINALIZADA), direcciones desglosa en 6 campos (barrio, calle, altura, torre, piso, departamento), cálculos financieros agregados, sincronización bidireccional con presupuestos. Cualquier cambio aquí afecta a todo el sistema. Gestiona campos @Transient para cálculos (totalCobrado, totalPagadoProfesionales, etc.).

### 32. **PresupuestoNoCliente**
- **Archivos**: `PresupuestoNoCliente.java`, `PresupuestoNoClienteController.java`, `PresupuestoNoClienteService.java`, `PresupuestoNoClienteRepository.java`, servicios relacionados: `PresupuestoObraSyncService.java`, `PresupuestoEstadoAutomaticoService.java`, `PresupuestoAuditoriaService.java`
- **Complejidad**: ⭐⭐⭐⭐⭐⭐ CRÍTICA (La más compleja)
- **Relaciones**: Con `Empresa`, `Obra`, `CobroObra`, `CajaChicaObra`, múltiples entidades calculadoras (`MaterialCalculadora`, `ProfesionalCalculadora`, `JornalCalculadora`, `ItemCalculadoraPresupuesto`), `PresupuestoMaterial`, `PresupuestoGastoGeneral`, `PresupuestoCostoInicial`, `PresupuestoPdf`, `PresupuestoAuditoria`
- **Líneas de Servicio**: **4,471 líneas** (¡La más grande del sistema!)
  - PresupuestoNoClienteService: 4,471 líneas
  - PresupuestoEstadoAutomaticoService: 288 líneas
  - PresupuestoObraSyncService: 184 líneas
  - PresupuestoAuditoriaService: 16 líneas
  - **Total: ~4,959 líneas** de lógica de negocio
- **Líneas de Entidad**: 629 líneas
- **Motivo**: **LA ENTIDAD MÁS COMPLEJA DEL SISTEMA**. Es el corazón de la lógica de presupuestos. Tiene:
  - **Máquina de estados compleja**: BORRADOR → APROBADO → CONVERTIDO → RECHAZADO → EXPIRADO
  - **Cálculos financieros extremadamente complejos**: profesionales, materiales, gastos generales, honorarios, mayores costos
  - **Sincronización bidireccional con Obra**: cambios en presupuesto afectan obra y viceversa
  - **Versionado de presupuestos**: numeroPresupuesto, numeroVersion
  - **Generación de PDFs**: integración con PresupuestoPdf
  - **Auditoría completa**: PresupuestoAuditoria
  - **Integración con stock**: sincronización automática de materiales
  - **Lógica de conversión a Obra**: proceso complejo de transformación
  - **Dirección desglosada**: 6 campos (calle, altura, piso, departamento, barrio, torre)
  - **Múltiples totales calculados**: totalPresupuesto, totalHonorarios, totalMayoresCostos, totalFinal
  - **Gestión de vencimientos**: fechaEmision, vencimiento, fechaProbableInicio
  - **Estados automáticos**: scheduler que actualiza estados vencidos
  - **¡Servicio de 4,471 líneas!** (10 veces más grande que el promedio)

---

## 📊 **Resumen de Estrategia de Refactorización**

### Fase 1: Fundamentos (Semana 1-2)
**Objetivo**: Establecer bases sólidas con entidades simples
1. Usuario (175 líneas) - 1-2 días
2. Empresa (171 líneas) - 1-2 días
3. Proveedor (142 líneas) - 1 día
4. GastoGeneral (115 líneas) - 1 día
5. Material (108 líneas) - 1 día

**Total**: 711 líneas, ~6-8 días

### Fase 2: Catálogos y Registros Básicos (Semana 3-4)
**Objetivo**: Entidades maestras con relaciones moderadas
6. Cliente (186 líneas) - 2 días
7. Honorario (205 líneas) - 2 días
8. Costo (284 líneas) - 2-3 días
9. AsignacionProfesionalObra (210 líneas) - 2 días
10. GastoObraProfesional (188 líneas) - 2 días

**Total**: 1,073 líneas, ~10-12 días

### Fase 3: Gestión de Recursos y Stock (Semana 5-6)
**Objetivo**: Sistemas de inventario y movimientos
11. Profesional (302 líneas) - 2-3 días
12. StockMaterial (220 líneas) - 2 días
13. MovimientoMaterial (156 líneas) - 2 días
14. AsistenciaObra (217 líneas) - 2 días
15. CajaChicaObra (106 + 127 = 233 líneas) - 2 días
16. Jornal (243 líneas) - 2-3 días
17. PedidoPago (292 líneas) - 2-3 días

**Total**: 1,663 líneas, ~14-17 días

### Fase 4: Sistemas Complejos (Semana 7-9)
**Objetivo**: Entidades con múltiples relaciones y cálculos
18. ObraMaterial (298 líneas) - 3 días
19. EtapaDiaria (607 líneas total) - 4-5 días
20. RetiroPersonal (306 líneas) - 3 días
21. AsignacionSemanal (397 líneas) - 3-4 días
22. TrabajoExtra (1,152 líneas total) - 7-8 días

**Total**: 2,760 líneas, ~20-23 días

### Fase 5: Sistema Financiero (Semana 10-12)
**Objetivo**: Núcleo financiero del sistema
23. ProfesionalObra (651 líneas) - 5-6 días
24. CobroObra (577 líneas) - 5 días
25. CobroEmpresa (629 líneas) - 5-6 días
26. AsignacionCobroObra (207 líneas) - 2-3 días
27. PagoProfesionalObra (426 líneas) - 4 días
28. PagoTrabajoExtraObra (353 líneas) - 3 días
29. PagoConsolidado (665 líneas) - 5-6 días
30. ObraFinanciero (208 líneas) - 2-3 días

**Total**: 3,716 líneas, ~31-36 días

### Fase 6: Núcleo Crítico (Semana 13-18) ⚠️
**Objetivo**: Las entidades más críticas del sistema
31. **Obra** (359 líneas servicio + 243 entidad) - 5-7 días ⚠️
32. **PresupuestoNoCliente** (4,959 líneas total) - 12-15 días ⚠️⚠️

**Total**: 5,561 líneas, ~17-22 días

---

## ⚠️ **Advertencias Importantes**

### Para PresupuestoNoCliente:
- **NO tocar hasta tener experiencia con todas las demás entidades**
- Requiere conocimiento profundo de todo el sistema
- Tiene 4,406 líneas de lógica de negocio
- Cualquier error aquí afecta todo el flujo del negocio
- Considerar dividir el servicio en múltiples servicios especializados:
  - `PresupuestoCreacionService`
  - `PresupuestoCalculoService`
  - `PresupuestoEstadoService`
  - `PresupuestoObraSyncService` (ya existe parcialmente)
  - `PresupuestoPdfService`
  - `PresupuestoAuditoriaService` (ya existe)

### Para Obra:
- Segunda entidad más compleja
- Afecta a casi todo el sistema
- Requiere plan de migración cuidadoso
- Considerar refactorización incremental sin romper funcionalidad existente

---

## 🎯 **Recomendaciones Generales**

1. **Tests**: Escribir tests de integración antes de refactorizar cada entidad
2. **Branch por entidad**: Crear una rama Git separada para cada entidad
3. **Documentación**: Documentar la lógica de negocio antes de modificar
4. **Code Review**: Revisión exhaustiva antes de mergear cambios
5. **Rollback plan**: Siempre tener plan B para revertir cambios
6. **Performance**: Monitorear performance después de cada cambio (especialmente en Obra y Presupuesto)

---

## 📈 **Métricas de Complejidad**

| Nivel | Entidades | Promedio Líneas Servicio | Rango Líneas | Tiempo Estimado |
|-------|-----------|-------------------------|--------------|-----------------|
| 1     | 5         | 142 líneas              | 108-175      | 1-2 días c/u    |
| 2     | 5         | 215 líneas              | 186-284      | 2-3 días c/u    |
| 3     | 7         | 237 líneas              | 156-292      | 2-3 días c/u    |
| 4     | 5         | 552 líneas              | 298-1152     | 3-8 días c/u    |
| 5     | 8         | 465 líneas              | 207-665      | 2-6 días c/u    |
| 6     | 2         | 2,665 líneas            | 359-4959     | 5-15 días c/u   |

### Estadísticas Generales:
- **Total de Entidades**: 32 entidades principales
- **Total de Líneas de Servicio**: ~15,484 líneas
- **Promedio General**: 484 líneas por servicio
- **Servicio Más Pequeño**: StockGastoGeneralService (115 líneas)
- **Servicio Más Grande**: PresupuestoNoClienteService (4,471 líneas)
- **Mediana**: 243 líneas (Jornal)

### Distribución por Complejidad:
- **Simples (< 200 líneas)**: 10 entidades (31%)
- **Moderadas (200-350 líneas)**: 11 entidades (34%)
- **Complejas (350-700 líneas)**: 9 entidades (28%)
- **Críticas (> 700 líneas)**: 2 entidades (6%)

**Total estimado del proyecto**: 14-18 semanas de trabajo (3.5 - 4.5 meses)

---

## 📦 **Entidades Complementarias (No requieren refactorización individual)**

Estas entidades son principalmente **entidades de unión/calculadoras** que se refactorizarán junto con sus entidades principales:

### Entidades de Cálculo (se refactorizan con sus padres):
- `MaterialCalculadora` → con PresupuestoNoCliente
- `ProfesionalCalculadora` → con PresupuestoNoCliente
- `JornalCalculadora` → con PresupuestoNoCliente
- `ItemCalculadoraPresupuesto` → con PresupuestoNoCliente
- `TrabajoExtraItemCalculadora` → con TrabajoExtra
- `TrabajoExtraJornalCalculadora` → con TrabajoExtra
- `TrabajoExtraMaterialCalculadora` → con TrabajoExtra
- `TrabajoExtraProfesionalCalculadora` → con TrabajoExtra

### Entidades de Relación/Unión:
- `PresupuestoMaterial` → con PresupuestoNoCliente
- `PresupuestoCostoInicial` → con PresupuestoNoCliente
- `PresupuestoGastoGeneral` → con PresupuestoNoCliente
- `PresupuestoPdf` → con PresupuestoNoCliente
- `PresupuestoAuditoria` → con PresupuestoNoCliente
- `TrabajoExtraDia` → con TrabajoExtra
- `TrabajoExtroProfesional` → con TrabajoExtra
- `TrabajoExtraTarea` → con TrabajoExtra
- `TrabajoExtraPdf` → con TrabajoExtra
- `TrabajoExtraGastoGeneral` → con TrabajoExtra
- `AsignacionProfesionalDia` → con AsignacionSemanal
- `AsignacionCobroEmpresaObra` → con CobroEmpresa
- `TareaEtapaDiaria` → con EtapaDiaria
- `ProfesionalTareaEtapa` → con EtapaDiaria
- `ProfesionalTareaDia` → con EtapaDiaria
- `CajaChicaMovimiento` → con CajaChicaObra
- `ObraOtroCosto` → con Obra
- `PagoGastoGeneralObra` → con PagoConsolidado
- `StockGastoGeneral` → con GastoGeneral

**Total de Entidades Complementarias**: 26 entidades

---

## 🎯 **Recomendaciones Generales**

1. **Tests**: Escribir tests de integración antes de refactorizar cada entidad
2. **Branch por entidad**: Crear una rama Git separada para cada entidad
3. **Documentación**: Documentar la lógica de negocio antes de modificar
4. **Code Review**: Revisión exhaustiva antes de mergear cambios
5. **Rollback plan**: Siempre tener plan B para revertir cambios
6. **Performance**: Monitorear performance después de cada cambio (especialmente en Obra y Presupuesto)

---

## 📋 **Resumen Ejecutivo**

### Entidades Totales Analizadas: 58
- **32 Entidades Principales** (requieren refactorización individual)
- **26 Entidades Complementarias** (se refactorizan junto con sus padres)

### Distribución de Complejidad:
- 🟢 **Nivel 1 (Muy Fácil)**: 5 entidades - 711 líneas totales
- 🟡 **Nivel 2 (Fácil-Medio)**: 5 entidades - 1,073 líneas totales
- 🟠 **Nivel 3 (Medio)**: 7 entidades - 1,663 líneas totales
- 🔴 **Nivel 4 (Difícil)**: 5 entidades - 2,760 líneas totales
- 🔴🔴 **Nivel 5 (Muy Difícil)**: 8 entidades - 3,716 líneas totales
- 🔴🔴🔴 **Nivel 6 (Crítico)**: 2 entidades - 5,561 líneas totales

### Top 5 Servicios Más Complejos:
1. **PresupuestoNoClienteService**: 4,471 líneas ⚠️⚠️⚠️
2. **TrabajoExtraService**: 937 líneas
3. **PagoConsolidadoService**: 665 líneas
4. **ProfesionalObraService**: 651 líneas
5. **CobroEmpresaService**: 629 líneas

### Tiempo Estimado Total:
- **Optimista**: 14 semanas (3.5 meses)
- **Realista**: 16 semanas (4 meses)
- **Conservador**: 18 semanas (4.5 meses)

### Orden de Ejecución Recomendado:
1. Comenzar por **Usuario** (más simple, familiarización con el patrón)
2. Seguir con entidades **sin dependencias financieras** (Nivel 1-2)
3. Abordar entidades de **inventario y tracking** (Nivel 3)
4. Refactorizar **entidades complejas intermedias** (Nivel 4)
5. Atacar el **sistema financiero** (Nivel 5)
6. Dejar para el final las **entidades críticas** Obra y PresupuestoNoCliente (Nivel 6)

### Notas Críticas:
- ⚠️ **PresupuestoNoCliente** es 10x más grande que el promedio
- ⚠️ Considerar dividir **PresupuestoNoClienteService** en múltiples servicios especializados
- ⚠️ **Obra** y **PresupuestoNoCliente** requieren planificación exhaustiva
- ⚠️ Toda refactorización de Nivel 6 debe hacerse con el sistema en modo de mantenimiento o con feature flags

---

✨ **¡Mucho éxito con la refactorización!** ✨

