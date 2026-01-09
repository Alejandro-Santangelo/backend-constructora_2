package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.PagoConsolidadoRequestDTO;
import com.rodrigo.construccion.dto.response.PagoConsolidadoBatchResponseDTO;
import com.rodrigo.construccion.dto.response.PagoConsolidadoResponseDTO;
import com.rodrigo.construccion.dto.response.TotalesPagosConsolidadosDTO;
import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.enums.MetodoPago;
import com.rodrigo.construccion.enums.TipoPagoConsolidado;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service para gestión de pagos consolidados (materiales, gastos generales, otros).
 */
@Service
@RequiredArgsConstructor
public class PagoConsolidadoService {

    private static final Logger log = LoggerFactory.getLogger(PagoConsolidadoService.class);

    private final PagoConsolidadoRepository pagoRepository;
    private final PagoGastoGeneralObraRepository pagoGastoGeneralRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;
    private final ItemCalculadoraPresupuestoRepository itemCalculadoraRepository;
    private final MaterialCalculadoraRepository materialCalculadoraRepository;

    /**
     * Listar todos los pagos consolidados de una empresa (materiales + gastos generales).
     */
    @Transactional(readOnly = true)
    public List<PagoConsolidadoResponseDTO> listarTodosPorEmpresa(Long empresaId) {
        log.info("📋 Listando todos los pagos consolidados para empresa {}", empresaId);

        List<PagoConsolidadoResponseDTO> todosPagos = new ArrayList<>();

        // Obtener pagos de materiales
        List<PagoConsolidado> pagosMateriales = pagoRepository.findByEmpresaIdOrderByFechaPagoDesc(empresaId);
        pagosMateriales.forEach(pago -> todosPagos.add(convertirAResponseDTO(pago)));
        log.info("✅ Encontrados {} pagos de materiales", pagosMateriales.size());

        // Obtener pagos de gastos generales
        List<PagoGastoGeneralObra> pagosGastos = pagoGastoGeneralRepository.findByEmpresaIdOrderByFechaPagoDesc(empresaId);
        pagosGastos.forEach(pago -> todosPagos.add(convertirGastoGeneralAResponseDTO(pago)));
        log.info("✅ Encontrados {} pagos de gastos generales", pagosGastos.size());

        log.info("🎉 Total de pagos consolidados: {}", todosPagos.size());
        return todosPagos;
    }

    /**
     * Registrar múltiples pagos en batch (transaccional).
     * Si uno falla, se hace rollback completo.
     * Detecta automáticamente si es pago de materiales o gastos generales según tipoPago.
     */
    @Transactional
    public PagoConsolidadoBatchResponseDTO registrarPagosEnBatch(
            List<PagoConsolidadoRequestDTO> requests, Long empresaId) {
        
        log.info("📥 Registrando batch de {} pagos consolidados para empresa {}", 
            requests.size(), empresaId);

        List<PagoConsolidadoResponseDTO> pagosRegistrados = new ArrayList<>();
        BigDecimal totalMonto = BigDecimal.ZERO;

        for (PagoConsolidadoRequestDTO request : requests) {
            // Asegurar empresaId
            if (request.getEmpresaId() == null) {
                request.setEmpresaId(empresaId);
            }

            // Detectar tipo de pago y guardar en tabla correspondiente
            TipoPagoConsolidado tipoPago = TipoPagoConsolidado.fromString(request.getTipoPago());
            if (tipoPago == null) {
                throw new IllegalArgumentException("Tipo de pago inválido: " + request.getTipoPago());
            }

            PagoConsolidadoResponseDTO response;

            if (tipoPago == TipoPagoConsolidado.GASTOS_GENERALES || 
                tipoPago == TipoPagoConsolidado.OTROS_COSTOS) {
                // Guardar en tabla pagos_gastos_generales_obra
                PagoGastoGeneralObra pagoGasto = crearPagoGastoGeneralDesdeDTO(request);
                PagoGastoGeneralObra pagoGuardado = pagoGastoGeneralRepository.save(pagoGasto);
                response = convertirGastoGeneralAResponseDTO(pagoGuardado);
                log.info("✅ Pago GASTO GENERAL registrado - ID: {}, Concepto: {}, Monto: ${}", 
                    pagoGuardado.getId(), pagoGuardado.getConcepto(), pagoGuardado.getMonto());
            } else {
                // Guardar en tabla pagos_material_obra (materiales)
                PagoConsolidado pago = crearPagoDesdeDTO(request);
                PagoConsolidado pagoGuardado = pagoRepository.save(pago);
                response = convertirAResponseDTO(pagoGuardado);
                log.info("✅ Pago MATERIAL registrado - ID: {}, Tipo: {}, Monto: ${}", 
                    pagoGuardado.getId(), pagoGuardado.getTipoPago(), pagoGuardado.getMonto());
            }

            pagosRegistrados.add(response);
            totalMonto = totalMonto.add(response.getMonto());
        }

        log.info("🎉 Batch completado - {} pagos registrados, Total: ${}", 
            pagosRegistrados.size(), totalMonto);

        return new PagoConsolidadoBatchResponseDTO(
            "Pagos registrados correctamente",
            pagosRegistrados.size(),
            totalMonto,
            pagosRegistrados
        );
    }

    /**
     * Registrar un pago consolidado individual.
     * Wrapper del método batch para facilitar llamadas individuales.
     */
    @Transactional
    public PagoConsolidadoResponseDTO registrarPago(
            PagoConsolidadoRequestDTO request, Long empresaId) {
        
        log.info("📥 Registrando pago consolidado individual para empresa {}", empresaId);
        
        // Reutilizar lógica de batch con una lista de un solo elemento
        List<PagoConsolidadoRequestDTO> requests = List.of(request);
        PagoConsolidadoBatchResponseDTO batchResponse = registrarPagosEnBatch(requests, empresaId);
        
        if (batchResponse.getPagos() == null || batchResponse.getPagos().isEmpty()) {
            throw new RuntimeException("Error al registrar el pago");
        }
        
        return batchResponse.getPagos().get(0);
    }

    /**
     * Crear una entidad PagoConsolidado desde el DTO con todas las validaciones.
     */
    private PagoConsolidado crearPagoDesdeDTO(PagoConsolidadoRequestDTO dto) {
        PagoConsolidado pago = new PagoConsolidado();

        // Validar y obtener presupuesto
        PresupuestoNoCliente presupuesto = presupuestoRepository
            .findById(dto.getPresupuestoNoClienteId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Presupuesto no encontrado con ID: " + dto.getPresupuestoNoClienteId()));

        // Validar que pertenece a la empresa
        if (!presupuesto.getEmpresa().getId().equals(dto.getEmpresaId())) {
            throw new IllegalArgumentException(
                "El presupuesto no pertenece a la empresa especificada");
        }

        // Validar y obtener item calculadora (OPCIONAL)
        ItemCalculadoraPresupuesto item = null;
        if (dto.getItemCalculadoraId() != null) {
            item = itemCalculadoraRepository
                .findById(dto.getItemCalculadoraId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Item de calculadora no encontrado con ID: " + dto.getItemCalculadoraId()));

            // Validar que el item pertenece al presupuesto
            if (!item.getPresupuestoNoCliente().getId().equals(dto.getPresupuestoNoClienteId())) {
                throw new IllegalArgumentException(
                    "El item de calculadora no pertenece al presupuesto especificado");
            }
        }

        // Si viene materialCalculadoraId, validar
        MaterialCalculadora material = null;
        if (dto.getMaterialCalculadoraId() != null) {
            material = materialCalculadoraRepository
                .findById(dto.getMaterialCalculadoraId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Material de calculadora no encontrado con ID: " + dto.getMaterialCalculadoraId()));

            // Validar que el material pertenece al item (si item existe)
            if (dto.getItemCalculadoraId() != null && material.getItemCalculadora() != null) {
                if (!material.getItemCalculadora().getId().equals(dto.getItemCalculadoraId())) {
                    throw new IllegalArgumentException(
                        "El material no pertenece al item de calculadora especificado");
                }
            }
        }

        // Convertir enums
        TipoPagoConsolidado tipoPago = TipoPagoConsolidado.fromString(dto.getTipoPago());
        if (tipoPago == null) {
            throw new IllegalArgumentException("Tipo de pago inválido: " + dto.getTipoPago());
        }

        MetodoPago metodoPago = MetodoPago.fromString(dto.getMetodoPago());
        if (metodoPago == null) {
            throw new IllegalArgumentException("Método de pago inválido: " + dto.getMetodoPago());
        }

        EstadoPago estado = EstadoPago.fromString(dto.getEstado());
        if (estado == null) {
            throw new IllegalArgumentException("Estado inválido: " + dto.getEstado());
        }

        // Verificar duplicados (sin validar itemCalculadoraId si es null)
        if (dto.getItemCalculadoraId() != null) {
            Optional<PagoConsolidado> duplicado = pagoRepository.findDuplicado(
                dto.getPresupuestoNoClienteId(),
                dto.getItemCalculadoraId(),
                dto.getConcepto(),
                dto.getFechaPago(),
                dto.getMonto(),
                dto.getEmpresaId()
            );
            if (duplicado.isPresent()) {
                throw new IllegalArgumentException(
                    "Ya existe un pago similar registrado (mismo presupuesto, item, concepto, fecha y monto)");
            }
        }

        // Asignar campos
        pago.setPresupuestoNoCliente(presupuesto);
        pago.setItemCalculadora(item);
        pago.setMaterialCalculadora(material);
        pago.setEmpresaId(dto.getEmpresaId());
        pago.setTipoPago(tipoPago);
        pago.setConcepto(dto.getConcepto());
        pago.setCantidad(dto.getCantidad());
        pago.setPrecioUnitario(dto.getPrecioUnitario());
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(metodoPago);
        pago.setFechaPago(dto.getFechaPago());
        pago.setEstado(estado);
        pago.setObservaciones(dto.getObservaciones());

        // Validar reglas de negocio
        pago.validar();

        return pago;
    }

    /**
     * Convertir entidad a DTO de response.
     */
    private PagoConsolidadoResponseDTO convertirAResponseDTO(PagoConsolidado pago) {
        PagoConsolidadoResponseDTO dto = new PagoConsolidadoResponseDTO();
        dto.setId(pago.getId());
        dto.setPresupuestoNoClienteId(pago.getPresupuestoNoCliente() != null 
            ? pago.getPresupuestoNoCliente().getId() : null);
        dto.setItemCalculadoraId(pago.getItemCalculadora() != null ? 
            pago.getItemCalculadora().getId() : null);
        dto.setMaterialCalculadoraId(pago.getMaterialCalculadora() != null ? 
            pago.getMaterialCalculadora().getId() : null);
        dto.setGastoGeneralCalculadoraId(null); // No aplica para materiales
        dto.setGastoGeneralId(pago.getGastoGeneralId());
        dto.setEmpresaId(pago.getEmpresaId());
        dto.setTipoPago(pago.getTipoPago().name());
        dto.setConcepto(pago.getConcepto());
        dto.setCantidad(pago.getCantidad());
        dto.setPrecioUnitario(pago.getPrecioUnitario());
        dto.setMonto(pago.getMonto());
        dto.setMetodoPago(pago.getMetodoPago().name());
        dto.setFechaPago(pago.getFechaPago());
        dto.setEstado(pago.getEstado().name());
        dto.setObservaciones(pago.getObservaciones());
        dto.setMotivoAnulacion(pago.getMotivoAnulacion());
        dto.setComprobanteUrl(pago.getComprobanteUrl());
        dto.setNumeroComprobante(pago.getNumeroComprobante());
        dto.setFechaRegistro(pago.getFechaRegistro());
        dto.setUsuarioRegistro(pago.getUsuarioRegistro());
        return dto;
    }

    /**
     * Obtener pago por ID.
     * Busca en AMBAS tablas (materiales y gastos generales).
     */
    @Transactional(readOnly = true)
    public PagoConsolidadoResponseDTO obtenerPorId(Long id, Long empresaId) {
        // 1. Intentar buscar en pagos de MATERIALES
        Optional<PagoConsolidado> pagoMaterial = pagoRepository.findById(id);
        
        if (pagoMaterial.isPresent()) {
            // Validar multi-tenant
            if (!pagoMaterial.get().getEmpresaId().equals(empresaId)) {
                throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
            }
            return convertirAResponseDTO(pagoMaterial.get());
        }
        
        // 2. Si no está en materiales, buscar en pagos de GASTOS GENERALES
        Optional<PagoGastoGeneralObra> pagoGastoGeneral = pagoGastoGeneralRepository.findById(id);
        
        if (pagoGastoGeneral.isPresent()) {
            // Validar multi-tenant
            if (!pagoGastoGeneral.get().getEmpresaId().equals(empresaId)) {
                throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
            }
            return convertirGastoGeneralAResponseDTO(pagoGastoGeneral.get());
        }
        
        // 3. Si no existe en ninguna tabla, lanzar excepción
        throw new IllegalArgumentException("Pago no encontrado con ID: " + id);
    }

    /**
     * Listar pagos por presupuesto.
     * COMBINA pagos de materiales (pagos_material_obra) + gastos generales (pagos_gastos_generales_obra).
     */
    @Transactional(readOnly = true)
    public List<PagoConsolidadoResponseDTO> listarPorPresupuesto(Long presupuestoId, Long empresaId) {
        // 1. Obtener pagos de MATERIALES
        List<PagoConsolidado> pagosMateriales = pagoRepository
            .findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(presupuestoId, empresaId);
        
        // 2. Obtener pagos de GASTOS GENERALES
        List<PagoGastoGeneralObra> pagosGastosGenerales = pagoGastoGeneralRepository
            .findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(presupuestoId, empresaId);
        
        // 3. Combinar ambos tipos en una lista unificada
        List<PagoConsolidadoResponseDTO> resultado = new ArrayList<>();
        
        // Convertir pagos de materiales
        resultado.addAll(pagosMateriales.stream()
            .map(this::convertirAResponseDTO)
            .collect(Collectors.toList()));
        
        // Convertir pagos de gastos generales
        resultado.addAll(pagosGastosGenerales.stream()
            .map(this::convertirGastoGeneralAResponseDTO)
            .collect(Collectors.toList()));
        
        // 4. Ordenar por fecha descendente (más recientes primero)
        resultado.sort((p1, p2) -> p2.getFechaPago().compareTo(p1.getFechaPago()));
        
        log.info("📋 Listado presupuesto {}: {} materiales + {} gastos generales = {} total", 
            presupuestoId, pagosMateriales.size(), pagosGastosGenerales.size(), resultado.size());
        
        return resultado;
    }

    /**
     * Listar pagos por item de calculadora.
     * COMBINA pagos de materiales (pagos_material_obra) + gastos generales (pagos_gastos_generales_obra).
     */
    @Transactional(readOnly = true)
    public List<PagoConsolidadoResponseDTO> listarPorItem(Long itemId, Long empresaId) {
        // 1. Obtener pagos de MATERIALES
        List<PagoConsolidado> pagosMateriales = pagoRepository
            .findByItemCalculadoraIdAndEmpresaIdOrderByFechaPagoDesc(itemId, empresaId);
        
        // 2. Obtener pagos de GASTOS GENERALES
        List<PagoGastoGeneralObra> pagosGastosGenerales = pagoGastoGeneralRepository
            .findByItemCalculadoraIdAndEmpresaIdOrderByFechaPagoDesc(itemId, empresaId);
        
        // 3. Combinar ambos tipos en una lista unificada
        List<PagoConsolidadoResponseDTO> resultado = new ArrayList<>();
        
        // Convertir pagos de materiales
        resultado.addAll(pagosMateriales.stream()
            .map(this::convertirAResponseDTO)
            .collect(Collectors.toList()));
        
        // Convertir pagos de gastos generales
        resultado.addAll(pagosGastosGenerales.stream()
            .map(this::convertirGastoGeneralAResponseDTO)
            .collect(Collectors.toList()));
        
        // 4. Ordenar por fecha descendente (más recientes primero)
        resultado.sort((p1, p2) -> p2.getFechaPago().compareTo(p1.getFechaPago()));
        
        log.info("📋 Listado item {}: {} materiales + {} gastos generales = {} total", 
            itemId, pagosMateriales.size(), pagosGastosGenerales.size(), resultado.size());
        
        return resultado;
    }

    /**
     * Anular un pago.
     * Busca en AMBAS tablas (materiales y gastos generales).
     */
    @Transactional
    public PagoConsolidadoResponseDTO anular(Long id, Long empresaId) {
        // 1. Intentar anular en pagos de MATERIALES
        Optional<PagoConsolidado> pagoMaterial = pagoRepository.findById(id);
        
        if (pagoMaterial.isPresent()) {
            // Validar multi-tenant
            if (!pagoMaterial.get().getEmpresaId().equals(empresaId)) {
                throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
            }
            
            pagoMaterial.get().anular();
            PagoConsolidado pagoAnulado = pagoRepository.save(pagoMaterial.get());
            
            log.info("❌ Pago MATERIAL anulado - ID: {}, Concepto: {}", id, pagoAnulado.getConcepto());
            
            return convertirAResponseDTO(pagoAnulado);
        }
        
        // 2. Si no está en materiales, intentar anular en GASTOS GENERALES
        Optional<PagoGastoGeneralObra> pagoGastoGeneral = pagoGastoGeneralRepository.findById(id);
        
        if (pagoGastoGeneral.isPresent()) {
            // Validar multi-tenant
            if (!pagoGastoGeneral.get().getEmpresaId().equals(empresaId)) {
                throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
            }
            
            pagoGastoGeneral.get().anular(null);
            PagoGastoGeneralObra pagoAnulado = pagoGastoGeneralRepository.save(pagoGastoGeneral.get());
            
            log.info("❌ Pago GASTO GENERAL anulado - ID: {}, Concepto: {}", id, pagoAnulado.getConcepto());
            
            return convertirGastoGeneralAResponseDTO(pagoAnulado);
        }
        
        // 3. Si no existe en ninguna tabla, lanzar excepción
        throw new IllegalArgumentException("Pago no encontrado con ID: " + id);
    }

    /**
     * Eliminar un pago consolidado.
     * Busca en AMBAS tablas (materiales y gastos generales) y elimina permanentemente.
     */
    @Transactional
    public void eliminarPago(Long id, Long empresaId) {
        // 1. Intentar eliminar en pagos de MATERIALES
        Optional<PagoConsolidado> pagoMaterial = pagoRepository.findById(id);
        
        if (pagoMaterial.isPresent()) {
            // Validar multi-tenant
            if (!pagoMaterial.get().getEmpresaId().equals(empresaId)) {
                throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
            }
            
            pagoRepository.delete(pagoMaterial.get());
            log.info("🗑️ Pago MATERIAL eliminado - ID: {}, Concepto: {}", id, pagoMaterial.get().getConcepto());
            return;
        }
        
        // 2. Si no está en materiales, intentar eliminar en GASTOS GENERALES
        Optional<PagoGastoGeneralObra> pagoGastoGeneral = pagoGastoGeneralRepository.findById(id);
        
        if (pagoGastoGeneral.isPresent()) {
            // Validar multi-tenant
            if (!pagoGastoGeneral.get().getEmpresaId().equals(empresaId)) {
                throw new IllegalArgumentException("El pago no pertenece a la empresa especificada");
            }
            
            pagoGastoGeneralRepository.delete(pagoGastoGeneral.get());
            log.info("🗑️ Pago GASTO GENERAL eliminado - ID: {}, Concepto: {}", id, pagoGastoGeneral.get().getConcepto());
            return;
        }
        
        // 3. Si no existe en ninguna tabla, lanzar excepción
        throw new IllegalArgumentException("Pago no encontrado con ID: " + id);
    }

    /**
     * Calcular totales consolidados.
     * COMBINA pagos de materiales (pagos_material_obra) + gastos generales (pagos_gastos_generales_obra).
     */
    @Transactional(readOnly = true)
    public TotalesPagosConsolidadosDTO calcularTotales(
            Long empresaId, 
            Long presupuestoId,
            LocalDate fechaDesde,
            LocalDate fechaHasta) {
        
        // 1. Obtener pagos de MATERIALES
        List<PagoConsolidado> pagosMateriales;
        if (fechaDesde != null && fechaHasta != null) {
            pagosMateriales = pagoRepository.findByFechaRange(empresaId, fechaDesde, fechaHasta);
            if (presupuestoId != null) {
                pagosMateriales = pagosMateriales.stream()
                    .filter(p -> p.getPresupuestoNoCliente().getId().equals(presupuestoId))
                    .collect(Collectors.toList());
            }
        } else if (presupuestoId != null) {
            pagosMateriales = pagoRepository
                .findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(presupuestoId, empresaId);
        } else {
            pagosMateriales = pagoRepository.findAll().stream()
                .filter(p -> p.getEmpresaId().equals(empresaId))
                .collect(Collectors.toList());
        }

        // 2. Obtener pagos de GASTOS GENERALES / OTROS COSTOS
        List<PagoGastoGeneralObra> pagosGastosGenerales;
        if (fechaDesde != null && fechaHasta != null) {
            pagosGastosGenerales = pagoGastoGeneralRepository
                .findByEmpresaIdAndFechaPagoBetween(empresaId, fechaDesde, fechaHasta);
            if (presupuestoId != null) {
                pagosGastosGenerales = pagosGastosGenerales.stream()
                    .filter(p -> p.getPresupuestoNoCliente().getId().equals(presupuestoId))
                    .collect(Collectors.toList());
            }
        } else if (presupuestoId != null) {
            pagosGastosGenerales = pagoGastoGeneralRepository
                .findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(presupuestoId, empresaId);
        } else {
            pagosGastosGenerales = pagoGastoGeneralRepository.findAll().stream()
                .filter(p -> p.getEmpresaId().equals(empresaId))
                .collect(Collectors.toList());
        }

        // 3. Filtrar solo PAGADOS de materiales
        List<PagoConsolidado> pagosMaterialesPagados = pagosMateriales.stream()
            .filter(p -> p.getEstado() == EstadoPago.PAGADO)
            .collect(Collectors.toList());
        
        // 4. Filtrar solo PAGADOS de gastos generales
        List<PagoGastoGeneralObra> pagosGastosGeneralesPagados = pagosGastosGenerales.stream()
            .filter(p -> p.getEstado() == EstadoPago.PAGADO)
            .collect(Collectors.toList());

        // 5. Calcular totales por tipo
        BigDecimal totalMateriales = BigDecimal.ZERO;
        BigDecimal totalGastosGenerales = BigDecimal.ZERO;
        BigDecimal totalOtros = BigDecimal.ZERO;

        // Sumar materiales
        for (PagoConsolidado pago : pagosMaterialesPagados) {
            totalMateriales = totalMateriales.add(pago.getMonto());
        }

        // Sumar gastos generales y otros costos
        for (PagoGastoGeneralObra pago : pagosGastosGeneralesPagados) {
            if (pago.getTipoPago() == TipoPagoConsolidado.GASTOS_GENERALES) {
                totalGastosGenerales = totalGastosGenerales.add(pago.getMonto());
            } else if (pago.getTipoPago() == TipoPagoConsolidado.OTROS_COSTOS) {
                totalOtros = totalOtros.add(pago.getMonto());
            }
        }

        BigDecimal totalGeneral = totalMateriales.add(totalGastosGenerales).add(totalOtros);
        
        int cantidadTotal = pagosMaterialesPagados.size() + pagosGastosGeneralesPagados.size();

        log.info("💰 Totales calculados - Materiales: {}, GastosGenerales: {}, OtrosCostos: {}, Total: {} ({} pagos)",
            totalMateriales, totalGastosGenerales, totalOtros, totalGeneral, cantidadTotal);

        return new TotalesPagosConsolidadosDTO(
            totalMateriales,
            totalGastosGenerales,
            totalOtros,
            totalGeneral,
            cantidadTotal
        );
    }

    /**
     * Buscar pagos por dirección de obra.
     * COMBINA pagos de materiales (pagos_material_obra) + gastos generales (pagos_gastos_generales_obra).
     */
    @Transactional(readOnly = true)
    public List<PagoConsolidadoResponseDTO> buscarPorDireccion(
            String calle, String altura, Long empresaId) {
        
        // 1. Obtener pagos de MATERIALES
        List<PagoConsolidado> pagosMateriales = pagoRepository
            .findByDireccionObra(calle, altura, empresaId);
        
        // 2. Obtener pagos de GASTOS GENERALES
        List<PagoGastoGeneralObra> pagosGastosGenerales = pagoGastoGeneralRepository
            .findByDireccionObra(calle, altura, empresaId);
        
        // 3. Combinar ambos tipos en una lista unificada
        List<PagoConsolidadoResponseDTO> resultado = new ArrayList<>();
        
        // Convertir pagos de materiales
        resultado.addAll(pagosMateriales.stream()
            .map(this::convertirAResponseDTO)
            .collect(Collectors.toList()));
        
        // Convertir pagos de gastos generales
        resultado.addAll(pagosGastosGenerales.stream()
            .map(this::convertirGastoGeneralAResponseDTO)
            .collect(Collectors.toList()));
        
        // 4. Ordenar por fecha descendente (más recientes primero)
        resultado.sort((p1, p2) -> p2.getFechaPago().compareTo(p1.getFechaPago()));
        
        log.info("📋 Búsqueda dirección {},{}: {} materiales + {} gastos generales = {} total", 
            calle, altura, pagosMateriales.size(), pagosGastosGenerales.size(), resultado.size());
        
        return resultado;
    }

    // ========== MÉTODOS PARA GASTOS GENERALES ==========

    /**
     * Crear entidad PagoGastoGeneralObra desde DTO con validaciones.
     */
    private PagoGastoGeneralObra crearPagoGastoGeneralDesdeDTO(PagoConsolidadoRequestDTO dto) {
        // Log detallado de IDs recibidos
        log.info("🔍 Creando pago gasto general - gastoGeneralCalculadoraId: {}, gastoGeneralId: {}, itemCalculadoraId: {}",
            dto.getGastoGeneralCalculadoraId(), dto.getGastoGeneralId(), dto.getItemCalculadoraId());
        
        PagoGastoGeneralObra pago = new PagoGastoGeneralObra();

        // Validar y obtener presupuesto
        PresupuestoNoCliente presupuesto = presupuestoRepository
            .findById(dto.getPresupuestoNoClienteId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Presupuesto no encontrado con ID: " + dto.getPresupuestoNoClienteId()));

        // Validar que pertenece a la empresa
        if (!presupuesto.getEmpresa().getId().equals(dto.getEmpresaId())) {
            throw new IllegalArgumentException(
                "El presupuesto no pertenece a la empresa especificada");
        }

        // Validar y obtener item calculadora (OPCIONAL)
        ItemCalculadoraPresupuesto item = null;
        if (dto.getItemCalculadoraId() != null) {
            item = itemCalculadoraRepository
                .findById(dto.getItemCalculadoraId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Item de calculadora no encontrado con ID: " + dto.getItemCalculadoraId()));

            // Validar que el item pertenece al presupuesto
            if (!item.getPresupuestoNoCliente().getId().equals(dto.getPresupuestoNoClienteId())) {
                throw new IllegalArgumentException(
                    "El item de calculadora no pertenece al presupuesto especificado");
            }
        }

        // Convertir enums
        TipoPagoConsolidado tipoPago = TipoPagoConsolidado.fromString(dto.getTipoPago());
        if (tipoPago == null) {
            throw new IllegalArgumentException("Tipo de pago inválido: " + dto.getTipoPago());
        }

        MetodoPago metodoPago = MetodoPago.fromString(dto.getMetodoPago());
        if (metodoPago == null) {
            throw new IllegalArgumentException("Método de pago inválido: " + dto.getMetodoPago());
        }

        EstadoPago estado = EstadoPago.fromString(dto.getEstado());
        if (estado == null) {
            throw new IllegalArgumentException("Estado inválido: " + dto.getEstado());
        }

        // Verificar duplicados (opcional para gastos generales)
        if (dto.getItemCalculadoraId() != null && dto.getGastoGeneralId() != null) {
            boolean existe = pagoGastoGeneralRepository.existePagoDuplicado(
                dto.getPresupuestoNoClienteId(),
                dto.getItemCalculadoraId(),
                dto.getGastoGeneralId(),
                dto.getFechaPago(),
                dto.getMonto(),
                dto.getEmpresaId()
            );
            
            if (existe) {
                log.warn("⚠️ Posible pago duplicado - Presupuesto: {}, Item: {}, Gasto: {}, Fecha: {}", 
                    dto.getPresupuestoNoClienteId(), dto.getItemCalculadoraId(), 
                    dto.getGastoGeneralId(), dto.getFechaPago());
            }
        }

        // Mapear campos
        pago.setPresupuestoNoCliente(presupuesto);
        pago.setItemCalculadora(item);
        pago.setGastoGeneralCalculadoraId(dto.getGastoGeneralCalculadoraId());
        pago.setGastoGeneralId(dto.getGastoGeneralId());
        pago.setEmpresaId(dto.getEmpresaId());
        pago.setTipoPago(tipoPago);
        pago.setConcepto(dto.getConcepto());
        pago.setCantidad(dto.getCantidad());
        pago.setPrecioUnitario(dto.getPrecioUnitario());
        pago.setMonto(dto.getMonto());
        pago.setMetodoPago(metodoPago);
        pago.setFechaPago(dto.getFechaPago());
        pago.setEstado(estado);
        pago.setObservaciones(dto.getObservaciones());
        pago.setNumeroComprobante(dto.getNumeroComprobante());
        pago.setComprobanteUrl(dto.getComprobanteUrl());

        // Log antes de guardar para verificar valores
        log.info("💾 Guardando pago gasto general - ID calculadora: {}, ID gasto: {}, Item: {}, Monto: {}",
            pago.getGastoGeneralCalculadoraId(), pago.getGastoGeneralId(), 
            pago.getItemCalculadora() != null ? pago.getItemCalculadora().getId() : null, pago.getMonto());

        // Validar entidad
        pago.validar();

        return pago;
    }

    /**
     * Convertir PagoGastoGeneralObra a ResponseDTO.
     */
    private PagoConsolidadoResponseDTO convertirGastoGeneralAResponseDTO(PagoGastoGeneralObra pago) {
        PagoConsolidadoResponseDTO dto = new PagoConsolidadoResponseDTO();
        
        dto.setId(pago.getId());
        dto.setPresupuestoNoClienteId(pago.getPresupuestoNoCliente() != null 
            ? pago.getPresupuestoNoCliente().getId() : null);
        dto.setItemCalculadoraId(pago.getItemCalculadora() != null 
            ? pago.getItemCalculadora().getId() : null);
        dto.setGastoGeneralCalculadoraId(pago.getGastoGeneralCalculadoraId());
        dto.setGastoGeneralId(pago.getGastoGeneralId());
        dto.setMaterialCalculadoraId(null); // No aplica para gastos generales
        dto.setEmpresaId(pago.getEmpresaId());
        dto.setTipoPago(pago.getTipoPago().name());
        dto.setConcepto(pago.getConcepto());
        dto.setCantidad(pago.getCantidad());
        dto.setPrecioUnitario(pago.getPrecioUnitario());
        dto.setMonto(pago.getMonto());
        dto.setMetodoPago(pago.getMetodoPago() != null ? pago.getMetodoPago().name() : null);
        dto.setFechaPago(pago.getFechaPago());
        dto.setEstado(pago.getEstado().name());
        dto.setObservaciones(pago.getObservaciones());
        dto.setMotivoAnulacion(pago.getMotivoAnulacion());
        dto.setComprobanteUrl(pago.getComprobanteUrl());
        dto.setNumeroComprobante(pago.getNumeroComprobante());
        dto.setFechaRegistro(pago.getFechaRegistro());
        
        return dto;
    }
}
