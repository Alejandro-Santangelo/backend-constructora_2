// ═══════════════════════════════════════════════════════════════════════
// LÓGICA PENDIENTE DE IMPLEMENTAR EN PagoProfesionalObraService.java
// Sistema de Adelantos - Descuento Automático
// Fecha: 02/03/2026
// ═══════════════════════════════════════════════════════════════════════

// ── PASO 1: Agregar método en PagoProfesionalObraRepository ──

// Ubicación: src/main/java/com/rodrigo/construccion/repository/PagoProfesionalObraRepository.java

@Query("SELECT p FROM PagoProfesionalObra p " +
       "WHERE p.profesionalObra.id = :profesionalObraId " +
       "AND p.esAdelanto = true " +
       "AND p.estadoAdelanto = 'ACTIVO' " +
       "AND p.saldoAdelantoPorDescontar > 0 " +
       "ORDER BY p.fechaPago ASC")
List<PagoProfesionalObra> findAdelantosActivosByProfesionalObraId(@Param("profesionalObraId") Long profesionalObraId);


// ── PASO 2: Agregar lógica en PagoProfesionalObraService.java ──

// Ubicación: src/main/java/com/rodrigo/construccion/service/PagoProfesionalObraService.java

/**
 * Aplica descuentos de adelantos activos a un pago regular (semanal).
 * 
 * Busca todos los adelantos activos del profesional y descuenta proporcionalmente
 * del pago regular. Actualiza el saldo pendiente de cada adelanto y marca como
 * COMPLETADO cuando el saldo llega a cero.
 * 
 * @param pagoRegular Pago semanal al que se aplicarán los descuentos
 */
private void aplicarDescuentosDeAdelantos(PagoProfesionalObra pagoRegular) {
    // Solo aplicar en pagos semanales regulares (no en adelantos, premios, etc.)
    if (!pagoRegular.esPagoSemanal()) {
        return;
    }
    
    Long profesionalObraId = pagoRegular.getProfesionalObraId();
    
    // Buscar adelantos activos del profesional
    List<PagoProfesionalObra> adelantosActivos = pagoRepository.findAdelantosActivosByProfesionalObraId(profesionalObraId);
    
    if (adelantosActivos == null || adelantosActivos.isEmpty()) {
        log.debug("No hay adelantos activos para el profesional obra ID: {}", profesionalObraId);
        return;
    }
    
    log.info("💸 Aplicando descuentos de {} adelantos activos para profesional obra ID: {}", 
             adelantosActivos.size(), profesionalObraId);
    
    // Calcular monto disponible para descontar del pago regular
    BigDecimal montoBruto = pagoRegular.getMontoBruto();
    if (montoBruto == null || montoBruto.compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("El pago regular no tiene monto bruto válido para aplicar descuentos");
        return;
    }
    
    BigDecimal descuentoPresentismo = pagoRegular.getDescuentoPresentismo();
    if (descuentoPresentismo == null) {
        descuentoPresentismo = BigDecimal.ZERO;
    }
    
    // Monto disponible = monto bruto - descuento por presentismo
    BigDecimal montoDisponible = montoBruto.subtract(descuentoPresentismo);
    
    if (montoDisponible.compareTo(BigDecimal.ZERO) <= 0) {
        log.warn("El pago regular no tiene monto disponible después del descuento de presentismo");
        return;
    }
    
    // Calcular total de adelantos pendientes
    BigDecimal totalAdelantosPendientes = adelantosActivos.stream()
        .map(PagoProfesionalObra::getSaldoAdelantoPorDescontar)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
    
    log.info("Total de adelantos pendientes: ${}, Monto disponible para descontar: ${}", 
             totalAdelantosPendientes, montoDisponible);
    
    // Calcular monto a descontar (máximo 40% del monto disponible)
    BigDecimal porcentajeMaximo = new BigDecimal("0.40"); // 40%
    BigDecimal montoMaximoDescuento = montoDisponible.multiply(porcentajeMaximo)
        .setScale(2, RoundingMode.HALF_UP);
    
    // El descuento no debe exceder el total de adelantos pendientes
    BigDecimal descuentoTotal = montoMaximoDescuento.min(totalAdelantosPendientes);
    
    log.info("Descuento total a aplicar: ${} (máximo 40% = ${})", 
             descuentoTotal, montoMaximoDescuento);
    
    // Distribuir el descuento proporcionalmente entre los adelantos
    BigDecimal descuentoAcumulado = BigDecimal.ZERO;
    List<Long> adelantosAplicadosIds = new ArrayList<>();
    
    for (int i = 0; i < adelantosActivos.size(); i++) {
        PagoProfesionalObra adelanto = adelantosActivos.get(i);
        BigDecimal saldoPendiente = adelanto.getSaldoAdelantoPorDescontar();
        
        BigDecimal descuentoAdelanto;
        
        if (i == adelantosActivos.size() - 1) {
            // Último adelanto: descontar lo que reste para evitar errores de redondeo
            descuentoAdelanto = descuentoTotal.subtract(descuentoAcumulado);
        } else {
            // Calcular descuento proporcional
            BigDecimal proporcion = saldoPendiente.divide(totalAdelantosPendientes, 4, RoundingMode.HALF_UP);
            descuentoAdelanto = descuentoTotal.multiply(proporcion).setScale(2, RoundingMode.HALF_UP);
        }
        
        // No descontar más del saldo pendiente del adelanto
        descuentoAdelanto = descuentoAdelanto.min(saldoPendiente);
        
        // Actualizar saldo del adelanto
        BigDecimal nuevoSaldo = saldoPendiente.subtract(descuentoAdelanto);
        adelanto.setSaldoAdelantoPorDescontar(nuevoSaldo);
        
        log.info("Adelanto ID {}: Descuento ${}, Saldo anterior ${}, Nuevo saldo ${}", 
                 adelanto.getId(), descuentoAdelanto, saldoPendiente, nuevoSaldo);
        
        // Si el saldo llegó a cero, marcar como COMPLETADO
        if (nuevoSaldo.compareTo(BigDecimal.ZERO) <= 0) {
            adelanto.setEstadoAdelanto(PagoProfesionalObra.ESTADO_ADELANTO_COMPLETADO);
            log.info("✅ Adelanto ID {} COMPLETADO", adelanto.getId());
        }
        
        // Guardar adelanto actualizado
        pagoRepository.save(adelanto);
        
        // Agregar ID a la lista de adelantos aplicados
        adelantosAplicadosIds.add(adelanto.getId());
        descuentoAcumulado = descuentoAcumulado.add(descuentoAdelanto);
    }
    
    // Actualizar el pago regular con la información de adelantos aplicados
    pagoRegular.setDescuentoAdelantos(descuentoAcumulado);
    
    // Convertir lista de IDs a JSON string
    try {
        String adelantosIdsJson = new ObjectMapper().writeValueAsString(adelantosAplicadosIds);
        pagoRegular.setAdelantosAplicadosIds(adelantosIdsJson);
    } catch (Exception e) {
        log.error("Error al convertir IDs de adelantos a JSON", e);
        // Como fallback, guardar como string simple
        pagoRegular.setAdelantosAplicadosIds(adelantosAplicadosIds.toString());
    }
    
    // Actualizar observaciones del pago regular
    String observaciones = pagoRegular.getObservaciones();
    if (observaciones == null) {
        observaciones = "";
    }
    observaciones += String.format(" | 💸 Descuento de adelantos aplicado: $%s (IDs: %s)", 
                                   descuentoAcumulado, adelantosAplicadosIds);
    pagoRegular.setObservaciones(observaciones);
    
    log.info("✅ Descuento total de adelantos aplicado: ${}", descuentoAcumulado);
}


// ── PASO 3: Modificar método crear() para llamar a la lógica de adelantos ──

// Ubicación: src/main/java/com/rodrigo/construccion/service/PagoProfesionalObraService.java
// Modificar el método crear() existente

@Override
@Transactional
public PagoProfesionalObraResponseDTO crear(PagoProfesionalObraRequestDTO request, Long empresaId) {
    log.info("Creando pago para profesional obra ID: {}, tipo: {}", 
             request.getProfesionalObraId(), request.getTipoPago());

    // Validar empresa
    if (empresaId == null) {
        throw new IllegalArgumentException("El ID de empresa es obligatorio");
    }
    
    // Validar que el profesional pertenezca a la empresa
    ProfesionalObra profesionalObra = profesionalObraRepository.findById(request.getProfesionalObraId())
            .orElseThrow(() -> new IllegalArgumentException("Profesional en obra no encontrado"));

    // Crear entidad
    PagoProfesionalObra pago = new PagoProfesionalObra();
    pago.setProfesionalObra(profesionalObra);
    pago.setEmpresaId(empresaId);

    // Mapear campos del request
    mapearRequestAEntity(request, pago);

    // ═══════════════════════════════════════════════════════════════
    // ⭐ NUEVO: Aplicar descuentos de adelantos si es pago semanal
    // ═══════════════════════════════════════════════════════════════
    
    if (pago.esPagoSemanal() && !Boolean.TRUE.equals(pago.getEsAdelanto())) {
        // Solo aplicar descuentos en pagos semanales regulares (no en adelantos)
        aplicarDescuentosDeAdelantos(pago);
    }
    
    // ═══════════════════════════════════════════════════════════════

    // Guardar
    PagoProfesionalObra pagoGuardado = pagoRepository.save(pago);
    
    log.info("✅ Pago creado con ID: {}, Monto final: ${}", 
             pagoGuardado.getId(), pagoGuardado.getMontoFinal());

    return mapearEntityAResponse(pagoGuardado);
}


// ── PASO 4: Agregar import necesario ──

// Al inicio del archivo PagoProfesionalObraService.java, agregar:

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;


// ═══════════════════════════════════════════════════════════════════════
// TESTING
// ═══════════════════════════════════════════════════════════════════════

// Flujo de prueba:
// 
// 1. Crear un adelanto:
//    POST /api/v1/pagos-profesional-obra
//    {
//      "profesionalObraId": 1,
//      "empresaId": 1,
//      "tipoPago": "ADELANTO",
//      "esAdelanto": true,
//      "periodoAdelanto": "1_SEMANA",
//      "montoBruto": 50000.00,
//      "fechaPago": "2026-03-02",
//      "metodoPago": "EFECTIVO"
//    }
//
// 2. Verificar que se creó:
//    SELECT * FROM pagos_profesional_obra WHERE es_adelanto = true;
//
// 3. Crear un pago semanal regular:
//    POST /api/v1/pagos-profesional-obra
//    {
//      "profesionalObraId": 1,
//      "empresaId": 1,
//      "tipoPago": "SEMANAL",
//      "montoBruto": 100000.00,
//      "diasTrabajados": 6,
//      "diasEsperados": 6,
//      "fechaPago": "2026-03-09",
//      "metodoPago": "TRANSFERENCIA"
//    }
//
// 4. Verificar que se aplicó el descuento:
//    - descuento_adelantos debería ser aprox. 40000 (40% de 100000)
//    - saldo_adelanto_por_descontar del adelanto debería ser 10000 (50000 - 40000)
//    - adelantos_aplicados_ids debería contener el ID del adelanto
//
// 5. Crear otro pago semanal:
//    POST /api/v1/pagos-profesional-obra
//    {
//      "profesionalObraId": 1,
//      "empresaId": 1,
//      "tipoPago": "SEMANAL",
//      "montoBruto": 100000.00,
//      "diasTrabajados": 6,
//      "diasEsperados": 6,
//      "fechaPago": "2026-03-16",
//      "metodoPago": "TRANSFERENCIA"
//    }
//
// 6. Verificar que el adelanto se completó:
//    - saldo_adelanto_por_descontar debería ser 0
//    - estado_adelanto debería ser 'COMPLETADO'

// ═══════════════════════════════════════════════════════════════════════
// CONFIGURACIÓN ADICIONAL (OPCIONAL)
// ═══════════════════════════════════════════════════════════════════════

// Si se desea configurar el porcentaje máximo de descuento, agregar a application.yml:
//
// adelantos:
//   porcentaje-maximo-descuento: 0.40  # 40%
//   permitir-multiples-activos: false
//
// Y leer con @Value en el servicio:
//
// @Value("${adelantos.porcentaje-maximo-descuento:0.40}")
// private BigDecimal porcentajeMaximoDescuento;
