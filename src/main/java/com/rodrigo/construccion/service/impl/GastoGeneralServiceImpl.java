package com.rodrigo.construccion.service.impl;

import com.rodrigo.construccion.dto.response.GastoGeneralConsolidadoDTO;
import com.rodrigo.construccion.dto.response.ObraGastoGeneralDTO;
import com.rodrigo.construccion.dto.response.AsignacionGastoDTO;
import com.rodrigo.construccion.model.entity.GastoGeneral;
import com.rodrigo.construccion.model.entity.PagoGastoGeneralObra;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import com.rodrigo.construccion.repository.GastoGeneralRepository;
import com.rodrigo.construccion.repository.PagoGastoGeneralObraRepository;
import com.rodrigo.construccion.repository.PresupuestoGastoGeneralRepository;
import com.rodrigo.construccion.service.IGastoGeneralService;
import com.rodrigo.construccion.service.EmpresaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar gastos generales
 */
@Service
@Transactional
@Slf4j
public class GastoGeneralServiceImpl implements IGastoGeneralService {

    private final GastoGeneralRepository gastoGeneralRepository;
    private final PagoGastoGeneralObraRepository pagoGastoGeneralObraRepository;
    private final PresupuestoGastoGeneralRepository presupuestoGastoGeneralRepository;
    private final EntityManager entityManager;
    private final EmpresaService empresaService;

    public GastoGeneralServiceImpl(GastoGeneralRepository gastoGeneralRepository,
                                  PagoGastoGeneralObraRepository pagoGastoGeneralObraRepository,
                                  PresupuestoGastoGeneralRepository presupuestoGastoGeneralRepository,
                                  EntityManager entityManager,
                                  EmpresaService empresaService) {
        this.gastoGeneralRepository = gastoGeneralRepository;
        this.pagoGastoGeneralObraRepository = pagoGastoGeneralObraRepository;
        this.presupuestoGastoGeneralRepository = presupuestoGastoGeneralRepository;
        this.entityManager = entityManager;
        this.empresaService = empresaService;
    }

    @Override
    public GastoGeneral crear(Long empresaId, GastoGeneral gastoGeneral) {
        log.info("🔄 Creando gasto general - Empresa: {}, Nombre: {}", empresaId, gastoGeneral.getNombre());
        
        gastoGeneral.setEmpresaId(empresaId);
        GastoGeneral saved = gastoGeneralRepository.save(gastoGeneral);
        
        log.info("✅ Gasto general creado con ID: {}", saved.getId());
        return saved;
    }

    @Override
    public GastoGeneral actualizar(Long empresaId, Long id, GastoGeneral gastoGeneral) {
        log.info("🔄 Actualizando gasto general - Empresa: {}, ID: {}", empresaId, id);
        
        GastoGeneral existente = gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
        
        existente.setNombre(gastoGeneral.getNombre());
        existente.setDescripcion(gastoGeneral.getDescripcion());
        existente.setUnidadMedida(gastoGeneral.getUnidadMedida());
        existente.setCategoria(gastoGeneral.getCategoria());
        existente.setPrecioUnitarioBase(gastoGeneral.getPrecioUnitarioBase());
        
        GastoGeneral updated = gastoGeneralRepository.save(existente);
        
        log.info("✅ Gasto general actualizado correctamente");
        return updated;
    }

    @Override
    public void eliminar(Long empresaId, Long id) {
        log.info("🗑️ Eliminando gasto general - Empresa: {}, ID: {}", empresaId, id);
        
        GastoGeneral existente = gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
        
        gastoGeneralRepository.delete(existente);
        
        log.info("✅ Gasto general eliminado correctamente");
    }

    @Override
    @Transactional(readOnly = true)
    public GastoGeneral obtenerPorId(Long empresaId, Long id) {
        log.info("🔍 Obteniendo gasto general - Empresa: {}, ID: {}", empresaId, id);
        
        return gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoGeneral> listarPorEmpresa(Long empresaId) {
        log.info("🔍 Listando gastos generales - TODOS (compartidos entre empresas)");
        
        Session session = entityManager.unwrap(Session.class);
        
        // Deshabilitar filtro empresaFilter para que devuelva TODOS los gastos generales
        session.disableFilter("empresaFilter");
        
        return gastoGeneralRepository.findByEmpresaIdOrderByNombre(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoGeneral> listarPorCategoria(Long empresaId, String categoria) {
        log.info("🔍 Listando gastos generales por categoría - Empresa: {}, Categoría: {}", empresaId, categoria);
        
        return gastoGeneralRepository.findByEmpresaIdAndCategoriaOrderByNombre(empresaId, categoria);
    }

    @Override
    public void actualizarPrecioTodos(Long empresaId, double porcentaje) {
        log.info("💰 Actualizando precio de todos los gastos generales - Empresa: {}, Porcentaje: {}%", empresaId, porcentaje);
        
        List<GastoGeneral> gastos = gastoGeneralRepository.findByEmpresaIdOrderByNombre(empresaId);
        java.math.BigDecimal factor = java.math.BigDecimal.ONE.add(java.math.BigDecimal.valueOf(porcentaje / 100));
        
        for (GastoGeneral gasto : gastos) {
            if (gasto.getPrecioUnitarioBase() != null) {
                java.math.BigDecimal nuevoPrecio = gasto.getPrecioUnitarioBase().multiply(factor);
                gasto.setPrecioUnitarioBase(nuevoPrecio);
            }
        }
        
        gastoGeneralRepository.saveAll(gastos);
        log.info("✅ Precios actualizados para {} gastos generales", gastos.size());
    }

    @Override
    public void actualizarPrecioPorId(Long empresaId, Long id, double porcentaje) {
        log.info("💰 Actualizando precio de gasto general - Empresa: {}, ID: {}, Porcentaje: {}%", empresaId, id, porcentaje);
        
        GastoGeneral gasto = gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
        
        if (gasto.getPrecioUnitarioBase() != null) {
            java.math.BigDecimal factor = java.math.BigDecimal.ONE.add(java.math.BigDecimal.valueOf(porcentaje / 100));
            java.math.BigDecimal nuevoPrecio = gasto.getPrecioUnitarioBase().multiply(factor);
            gasto.setPrecioUnitarioBase(nuevoPrecio);
            gastoGeneralRepository.save(gasto);
        }
        
        log.info("✅ Precio actualizado correctamente");
    }

    @Override
    public void actualizarPrecioVarios(Long empresaId, List<Long> ids, double porcentaje) {
        log.info("💰 Actualizando precio de varios gastos generales - Empresa: {}, IDs: {}, Porcentaje: {}%", empresaId, ids, porcentaje);
        
        List<GastoGeneral> gastos = gastoGeneralRepository.findAllById(ids);
        // Filtrar solo los de la empresa
        gastos = gastos.stream()
            .filter(g -> g.getEmpresaId().equals(empresaId))
            .collect(java.util.stream.Collectors.toList());
        
        if (gastos.isEmpty()) {
            throw new RuntimeException("No se encontraron gastos generales con los IDs proporcionados para esta empresa");
        }
        
        java.math.BigDecimal factor = java.math.BigDecimal.ONE.add(java.math.BigDecimal.valueOf(porcentaje / 100));
        for (GastoGeneral gasto : gastos) {
            if (gasto.getPrecioUnitarioBase() != null) {
                java.math.BigDecimal nuevoPrecio = gasto.getPrecioUnitarioBase().multiply(factor);
                gasto.setPrecioUnitarioBase(nuevoPrecio);
            }
        }
        
        gastoGeneralRepository.saveAll(gastos);
        log.info("✅ Precios actualizados para {} gastos generales", gastos.size());
    }

    /**
     * Obtener gastos generales consolidados con sus pagos
     * Similar a obtenerProfesionalesConsolidados pero para gastos generales
     */
    @Override
    @Transactional(readOnly = true)
    public List<GastoGeneralConsolidadoDTO> obtenerGastosGeneralesConsolidados(Long empresaId) {
        log.info("🔍 [CONSOLIDADO-GG] Obteniendo gastos generales consolidados para empresa ID: {}", empresaId);
        
        // Validar empresa
        empresaService.findEmpresaById(empresaId);
        log.info("✅ [CONSOLIDADO-GG] Empresa {} validada correctamente", empresaId);
        
        // Obtener TODOS los pagos de gastos generales de la empresa
        log.info("📋 [CONSOLIDADO-GG] Buscando TODOS los pagos de gastos generales para empresa {}...", empresaId);
        List<PagoGastoGeneralObra> todosLosPagos = pagoGastoGeneralObraRepository.findByEmpresaIdOrderByFechaPagoDesc(empresaId);
        
        log.info("📊 [CONSOLIDADO-GG] Total pagos encontrados: {}", todosLosPagos.size());
        
        if (todosLosPagos.isEmpty()) {
            log.warn("⚠️ [CONSOLIDADO-GG] NO HAY PAGOS de gastos generales para empresa {}", empresaId);
            return new ArrayList<>();
        }
        
        // Agrupar pagos por concepto (tipo de gasto)
        Map<String, List<PagoGastoGeneralObra>> pagosPorGasto = todosLosPagos.stream()
            .collect(Collectors.groupingBy(pago -> 
                pago.getConcepto() != null ? pago.getConcepto() : "Gasto General"
            ));
        
        List<GastoGeneralConsolidadoDTO> gastosDTO = new ArrayList<>();
        
        // Para cada tipo de gasto, crear su DTO consolidado
        for (Map.Entry<String, List<PagoGastoGeneralObra>> entry : pagosPorGasto.entrySet()) {
            String conceptoGasto = entry.getKey();
            List<PagoGastoGeneralObra> pagosGasto = entry.getValue();
            
            // Obtener datos del primer pago
            PagoGastoGeneralObra primerPago = pagosGasto.get(0);
            
            GastoGeneralConsolidadoDTO gastoDTO = GastoGeneralConsolidadoDTO.builder()
                .gastoId(primerPago.getGastoGeneralCalculadoraId())
                .gastoNombre(primerPago.getConcepto())
                .gastoDescripcion(primerPago.getConcepto())
                .categoria(primerPago.getTipoPago() != null ? primerPago.getTipoPago().name() : "GENERAL")
                .unidadMedida(null)
                .obras(new ArrayList<>())
                .build();
            
            // Agrupar pagos por presupuestoNoClienteId (que representa la obra) y gastoGeneralCalculadoraId
            Map<String, List<PagoGastoGeneralObra>> pagosPorObraYGasto = pagosGasto.stream()
                .filter(p -> p.getPresupuestoNoCliente() != null && p.getPresupuestoNoCliente().getId() != null)
                .collect(Collectors.groupingBy(p -> 
                    p.getPresupuestoNoCliente().getId() + "_" + 
                    (p.getGastoGeneralCalculadoraId() != null ? p.getGastoGeneralCalculadoraId() : "0")
                ));
            
            // Para cada obra/gasto, crear su DTO
            Map<Long, ObraGastoGeneralDTO> obrasMap = new HashMap<>();
            
            for (Map.Entry<String, List<PagoGastoGeneralObra>> obraGastoEntry : pagosPorObraYGasto.entrySet()) {
                List<PagoGastoGeneralObra> pagosObraGasto = obraGastoEntry.getValue();
                PagoGastoGeneralObra pagoObra = pagosObraGasto.get(0);
                PresupuestoNoCliente presupuesto = pagoObra.getPresupuestoNoCliente();
                Long presupuestoId = presupuesto.getId();
                
                // Obtener o crear ObraDTO
                ObraGastoGeneralDTO obraDTO = obrasMap.computeIfAbsent(presupuestoId, id ->
                    ObraGastoGeneralDTO.builder()
                        .obraId(id)
                        .obraNombre(presupuesto.getNombreObra() != null ? 
                            presupuesto.getNombreObra() : "Obra " + id)
                        .obraEstado(presupuesto.getEstado() != null ? 
                            presupuesto.getEstado().name() : "DESCONOCIDO")
                        .direccionCompleta(construirDireccionPresupuesto(presupuesto))
                        .asignaciones(new ArrayList<>())
                        .build()
                );
                
                // 💰 Calcular presupuesto base desde PresupuestoGastoGeneral
                BigDecimal presupuestoBase = BigDecimal.ZERO;
                Long gastoGeneralCalculadoraId = pagoObra.getGastoGeneralCalculadoraId();
                
                if (gastoGeneralCalculadoraId != null) {
                    try {
                        PresupuestoGastoGeneral gastoGeneral = presupuestoGastoGeneralRepository.findById(gastoGeneralCalculadoraId).orElse(null);
                        if (gastoGeneral != null && gastoGeneral.getSubtotal() != null) {
                            presupuestoBase = gastoGeneral.getSubtotal();
                            log.debug("💰 Presupuesto base para gasto {}: {}", conceptoGasto, presupuestoBase);
                        }
                    } catch (Exception e) {
                        log.warn("⚠️ No se pudo obtener presupuesto base para gasto_id {}: {}", gastoGeneralCalculadoraId, e.getMessage());
                    }
                }
                
                // 💰 Calcular total pagado (suma de todos los pagos)
                BigDecimal totalPagado = pagosObraGasto.stream()
                    .map(p -> p.getMonto() != null ? p.getMonto() : BigDecimal.ZERO)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
                
                // 💰 Calcular pendiente correctamente
                BigDecimal saldoPendiente = presupuestoBase.subtract(totalPagado);
                
                log.debug("📊 Gasto '{}' en obra {}: Presupuesto={}, Pagado={}, Pendiente={}", 
                    conceptoGasto, presupuestoId, presupuestoBase, totalPagado, saldoPendiente);
                
                // Crear AsignacionGastoDTO consolidado (representa la agrupación)
                AsignacionGastoDTO asignacionConsolidada = AsignacionGastoDTO.builder()
                    .asignacionId(gastoGeneralCalculadoraId != null ? gastoGeneralCalculadoraId : pagoObra.getId())
                    .tipoPago(pagoObra.getTipoPago() != null ? pagoObra.getTipoPago().name() : "")
                    .concepto(pagoObra.getConcepto())
                    .categoria(pagoObra.getTipoPago() != null ? pagoObra.getTipoPago().name() : "GENERAL")
                    .cantidad(BigDecimal.valueOf(pagosObraGasto.size()))
                    .precioUnitario(BigDecimal.ZERO)
                    .totalAsignado(presupuestoBase) // ✅ Presupuesto base
                    .totalUtilizado(totalPagado)     // ✅ Total pagado
                    .saldoPendiente(saldoPendiente)  // ✅ Presupuesto - Pagado
                    .fechaPago(pagoObra.getFechaPago())
                    .metodoPago(pagoObra.getMetodoPago() != null ? pagoObra.getMetodoPago().name() : "")
                    .estado(pagoObra.getEstado() != null ? pagoObra.getEstado().name() : "")
                    .observaciones(pagosObraGasto.size() + " pago(s) realizados")
                    .build();
                
                obraDTO.getAsignaciones().add(asignacionConsolidada);
            }
            
            // Agregar todas las obras al gastoDTO
            gastoDTO.getObras().addAll(obrasMap.values());
            
            // Calcular totales de cada obra y del gasto
            gastoDTO.getObras().forEach(ObraGastoGeneralDTO::calcularTotales);
            gastoDTO.calcularTotales();
            gastosDTO.add(gastoDTO);
        }
        
        log.info("✅ [CONSOLIDADO-GG] Se procesaron {} tipos de gastos con sus pagos", gastosDTO.size());
        return gastosDTO;
    }
    
    /**
     * Mapea PagoGastoGeneralObra a AsignacionGastoDTO
     */
    private AsignacionGastoDTO mapearPagoAGastoDTO(PagoGastoGeneralObra pago) {
        BigDecimal cantidad = pago.getCantidad() != null ? pago.getCantidad() : BigDecimal.ONE;
        BigDecimal precioUnitario = pago.getPrecioUnitario() != null ? pago.getPrecioUnitario() : BigDecimal.ZERO;
        BigDecimal montoTotal = pago.getMonto() != null ? pago.getMonto() : 
            precioUnitario.multiply(cantidad);
        
        return AsignacionGastoDTO.builder()
            .asignacionId(pago.getId())
            .tipoPago(pago.getTipoPago() != null ? pago.getTipoPago().name() : "")
            .concepto(pago.getConcepto())
            .categoria(pago.getTipoPago() != null ? pago.getTipoPago().name() : "GENERAL")
            .cantidad(cantidad)
            .precioUnitario(precioUnitario)
            .totalAsignado(montoTotal)
            .totalUtilizado(montoTotal) // Ya está pagado
            .saldoPendiente(BigDecimal.ZERO)
            .fechaPago(pago.getFechaPago())
            .metodoPago(pago.getMetodoPago() != null ? pago.getMetodoPago().name() : "")
            .estado(pago.getEstado() != null ? pago.getEstado().name() : "")
            .observaciones(pago.getObservaciones())
            .build();
    }
    
    /**
     * Construye dirección completa de un presupuesto
     */
    private String construirDireccionPresupuesto(PresupuestoNoCliente presupuesto) {
        if (presupuesto == null) return "";
        
        StringBuilder direccion = new StringBuilder();
        if (presupuesto.getDireccionObraCalle() != null && !presupuesto.getDireccionObraCalle().isBlank()) {
            direccion.append(presupuesto.getDireccionObraCalle());
        }
        if (presupuesto.getDireccionObraAltura() != null && !presupuesto.getDireccionObraAltura().isBlank()) {
            direccion.append(" ").append(presupuesto.getDireccionObraAltura());
        }
        return direccion.toString();
    }
}
