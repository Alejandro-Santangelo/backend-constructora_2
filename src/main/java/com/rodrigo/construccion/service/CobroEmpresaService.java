package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.*;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.enums.EstadoCobroEmpresa;
import com.rodrigo.construccion.enums.MetodoPago;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Cobros a nivel Empresa
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CobroEmpresaService {

    private final CobroEmpresaRepository cobroEmpresaRepository;
    private final AsignacionCobroEmpresaObraRepository asignacionRepository;
    private final CobroObraRepository cobroObraRepository;
    private final ObraRepository obraRepository;
    private final AsignacionCobroObraRepository asignacionCobroObraRepository;
    private final TrabajoAdicionalRepository trabajoAdicionalRepository;
    private final EntidadFinancieraRepository entidadFinancieraRepository;
    private final CobroEntidadRepository cobroEntidadRepository;
    private final EntidadFinancieraService entidadFinancieraService;

    /**
     * Crear un nuevo cobro a nivel empresa
     */
    @Transactional
    public CobroEmpresaResponseDTO crearCobroEmpresa(CobroEmpresaRequestDTO request) {
        log.info("Creando cobro empresa para empresaId: {}, monto: {}", 
                 request.getEmpresaId(), request.getMontoTotal());

        // Validaciones
        validarMontoPositivo(request.getMontoTotal());

        // Crear entidad
        CobroEmpresa cobro = new CobroEmpresa();
        cobro.setEmpresaId(request.getEmpresaId());
        cobro.setMontoTotal(request.getMontoTotal());
        cobro.setMontoAsignado(BigDecimal.ZERO);
        cobro.setMontoDisponible(request.getMontoTotal());
        cobro.setDescripcion(request.getDescripcion());
        cobro.setFechaCobro(request.getFechaCobro());
        cobro.setNumeroComprobante(request.getNumeroComprobante());
        cobro.setTipoComprobante(request.getTipoComprobante());
        cobro.setObservaciones(request.getObservaciones());
        cobro.setEstado(EstadoCobroEmpresa.DISPONIBLE);

        // Convertir método de pago
        if (request.getMetodoPago() != null) {
            cobro.setMetodoPago(MetodoPago.fromString(request.getMetodoPago()));
        }

        CobroEmpresa cobroGuardado = cobroEmpresaRepository.save(cobro);
        log.info("Cobro empresa creado con ID: {}", cobroGuardado.getId());

        return mapearAResponseDTO(cobroGuardado, false);
    }

    /**
     * Asignar cobro empresa a una o varias obras
     */
    @Transactional
    public AsignarCobroEmpresaResponseDTO asignarCobroAObras(
            Long cobroEmpresaId, 
            Long empresaId,
            AsignarCobroEmpresaRequestDTO request) {
        
        log.info("Asignando cobro empresa {} a {} obras", 
                 cobroEmpresaId, request.getAsignaciones().size());

        // Buscar cobro empresa
        CobroEmpresa cobroEmpresa = buscarCobroEmpresaPorIdYEmpresa(cobroEmpresaId, empresaId);

        // Validar que no esté anulado
        if (cobroEmpresa.getEstado() == EstadoCobroEmpresa.ANULADO) {
            throw new IllegalStateException("No se puede asignar un cobro anulado");
        }

        // Calcular total a asignar
        BigDecimal totalAsignar = request.getAsignaciones().stream()
                .map(AsignacionObraDTO::getMontoAsignado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Validar que hay saldo suficiente
        if (totalAsignar.compareTo(cobroEmpresa.getMontoDisponible()) > 0) {
            throw new IllegalArgumentException(
                String.format("Monto a asignar (%s) supera el disponible (%s)", 
                              totalAsignar, cobroEmpresa.getMontoDisponible())
            );
        }

        // Procesar cada asignación
        List<AsignarCobroEmpresaResponseDTO.AsignacionCreadaDTO> asignacionesCreadas = 
            new ArrayList<>();

        for (AsignacionObraDTO asignacionDTO : request.getAsignaciones()) {
            AsignarCobroEmpresaResponseDTO.AsignacionCreadaDTO asignacionCreada;
            if (asignacionDTO.getTrabajoAdicionalId() != null) {
                asignacionCreada = procesarAsignacionTrabajoAdicional(
                    cobroEmpresa, asignacionDTO, empresaId);
            } else if (asignacionDTO.getObraId() != null) {
                asignacionCreada = procesarAsignacionObra(
                    cobroEmpresa, asignacionDTO, empresaId);
            } else {
                throw new IllegalArgumentException(
                    "Cada asignación debe tener obraId o trabajoAdicionalId");
            }
            asignacionesCreadas.add(asignacionCreada);
        }

        // Actualizar montos del cobro empresa
        BigDecimal nuevoMontoAsignado = cobroEmpresa.getMontoAsignado().add(totalAsignar);
        cobroEmpresa.setMontoAsignado(nuevoMontoAsignado);
        cobroEmpresa.calcularMontoDisponible();
        cobroEmpresa.actualizarEstado();

        CobroEmpresa cobroActualizado = cobroEmpresaRepository.save(cobroEmpresa);

        log.info("Cobro empresa {} actualizado. Nuevo estado: {}, Disponible: {}", 
                 cobroEmpresaId, cobroActualizado.getEstado(), 
                 cobroActualizado.getMontoDisponible());

        // Preparar respuesta
        return AsignarCobroEmpresaResponseDTO.builder()
                .cobroEmpresa(mapearAResponseDTO(cobroActualizado, false))
                .asignacionesCreadas(asignacionesCreadas)
                .build();
    }

    /**
     * Procesar una asignación individual a una obra
     */
    private AsignarCobroEmpresaResponseDTO.AsignacionCreadaDTO procesarAsignacionObra(
            CobroEmpresa cobroEmpresa,
            AsignacionObraDTO asignacionDTO,
            Long empresaId) {
        
        log.debug("Procesando asignación para obra {}: {}", 
                  asignacionDTO.getObraId(), asignacionDTO.getMontoAsignado());

        // Validar obra
        Obra obra = obraRepository.findById(asignacionDTO.getObraId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Obra no encontrada: " + asignacionDTO.getObraId()));

        // Validar que la obra pertenece a la empresa
        if (!obra.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException(
                "La obra no pertenece a la empresa especificada");
        }

        // 1. Crear CobroObra
        CobroObra cobroObra = crearCobroObra(obra, asignacionDTO, empresaId);
        CobroObra cobroObraGuardado = cobroObraRepository.save(cobroObra);

        // 2. Si hay distribución por ítems, crear AsignacionesCobroObra
        boolean tieneDistribucion = false;
        if (asignacionDTO.getDistribucionItems() != null) {
            crearDistribucionPorItems(cobroObraGuardado, obra, 
                                     asignacionDTO.getDistribucionItems());
            tieneDistribucion = true;
        }

        // 3. Crear AsignacionCobroEmpresaObra
        AsignacionCobroEmpresaObra asignacion = new AsignacionCobroEmpresaObra();
        asignacion.setCobroEmpresa(cobroEmpresa);
        asignacion.setCobroObra(cobroObraGuardado);
        asignacion.setMontoAsignado(asignacionDTO.getMontoAsignado());
        asignacion.setObservaciones(asignacionDTO.getDescripcion());
        asignacionRepository.save(asignacion);

        return AsignarCobroEmpresaResponseDTO.AsignacionCreadaDTO.builder()
                .cobroObraId(cobroObraGuardado.getId())
                .obraId(obra.getId())
                .montoAsignado(asignacionDTO.getMontoAsignado())
                .tieneDistribucionItems(tieneDistribucion)
                .build();
    }

    /**
     * Procesar asignación de cobro empresa a un trabajo adicional.
     * Usa el sistema de entidades financieras (cobros_entidad).
     */
    private AsignarCobroEmpresaResponseDTO.AsignacionCreadaDTO procesarAsignacionTrabajoAdicional(
            CobroEmpresa cobroEmpresa,
            AsignacionObraDTO asignacionDTO,
            Long empresaId) {

        Long taId = asignacionDTO.getTrabajoAdicionalId();
        log.debug("Procesando asignación para trabajo adicional {}: {}",
                  taId, asignacionDTO.getMontoAsignado());

        // Validar que el trabajo adicional existe y pertenece a la empresa
        TrabajoAdicional ta = trabajoAdicionalRepository.findById(taId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Trabajo adicional no encontrado: " + taId));
        if (!ta.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException(
                "El trabajo adicional no pertenece a la empresa especificada");
        }

        // Obtener o crear el registro en entidades_financieras
        EntidadFinanciera ef = entidadFinancieraRepository
                .findByEmpresaIdAndTipoEntidadAndEntidadId(
                    empresaId,
                    com.rodrigo.construccion.enums.TipoEntidadFinanciera.TRABAJO_ADICIONAL,
                    taId)
                .orElseGet(() -> entidadFinancieraService.sincronizarDesdeTrabajoAdicional(ta));

        if (ef == null) {
            throw new IllegalStateException(
                "No se pudo obtener/crear la entidad financiera para el trabajo adicional " + taId);
        }

        // Registrar cobro en cobros_entidad
        CobroEntidad cobro = new CobroEntidad();
        cobro.setEntidadFinanciera(ef);
        cobro.setEmpresaId(empresaId);
        cobro.setMonto(asignacionDTO.getMontoAsignado());
        cobro.setFechaCobro(java.time.LocalDate.now());
        cobro.setMetodoPago("ASIGNACION_EMPRESA");
        cobro.setNotas(asignacionDTO.getDescripcion() != null
                ? asignacionDTO.getDescripcion()
                : "Asignación desde cobro empresa #" + cobroEmpresa.getId());
        CobroEntidad cobroGuardado = cobroEntidadRepository.save(cobro);

        log.info("Cobro entidad {} creado para trabajo adicional {}", cobroGuardado.getId(), taId);

        return AsignarCobroEmpresaResponseDTO.AsignacionCreadaDTO.builder()
                .cobroEntidadId(cobroGuardado.getId())
                .trabajoAdicionalId(taId)
                .montoAsignado(asignacionDTO.getMontoAsignado())
                .tieneDistribucionItems(false)
                .build();
    }

    /**
     * Crear CobroObra a partir de la asignación
     */
    private CobroObra crearCobroObra(Obra obra, AsignacionObraDTO asignacionDTO, Long empresaId) {
        CobroObra cobroObra = new CobroObra();
        cobroObra.setObra(obra);
        // No seteamos presupuestoNoCliente porque Obra tiene @OneToMany, no ManyToOne
        cobroObra.setEmpresaId(empresaId);
        cobroObra.setMonto(asignacionDTO.getMontoAsignado());
        cobroObra.setConcepto(asignacionDTO.getDescripcion() != null 
                              ? asignacionDTO.getDescripcion() 
                              : "Asignación desde cobro empresa");
        cobroObra.setFechaCobro(java.time.LocalDate.now());
        cobroObra.setMetodoPago("ASIGNACION_EMPRESA");
        cobroObra.setEstado(CobroObra.ESTADO_COBRADO);
        
        // Copiar dirección de la obra
        if (obra.getDireccionObraBarrio() != null) cobroObra.setBarrio(obra.getDireccionObraBarrio());
        if (obra.getDireccionObraCalle() != null) cobroObra.setCalle(obra.getDireccionObraCalle());
        if (obra.getDireccionObraAltura() != null) cobroObra.setAltura(obra.getDireccionObraAltura());
        if (obra.getDireccionObraTorre() != null) cobroObra.setTorre(obra.getDireccionObraTorre());
        if (obra.getDireccionObraPiso() != null) cobroObra.setPiso(obra.getDireccionObraPiso());
        if (obra.getDireccionObraDepartamento() != null) cobroObra.setDepto(obra.getDireccionObraDepartamento());

        return cobroObra;
    }

    /**
     * Crear distribución por ítems para un cobro obra
     * IMPORTANTE: AsignacionCobroObra usa UN SOLO registro con todos los montos
     */
    private void crearDistribucionPorItems(CobroObra cobroObra, Obra obra,
                                           DistribucionItemsDTO distribucion) {
        log.debug("Creando distribución por ítems para cobro obra {}", cobroObra.getId());

        // Validar que la suma coincide con el monto del cobro
        BigDecimal sumaMontos = BigDecimal.ZERO;
        if (distribucion.getMontoProfesionales() != null) 
            sumaMontos = sumaMontos.add(distribucion.getMontoProfesionales());
        if (distribucion.getMontoMateriales() != null) 
            sumaMontos = sumaMontos.add(distribucion.getMontoMateriales());
        if (distribucion.getMontoGastosGenerales() != null) 
            sumaMontos = sumaMontos.add(distribucion.getMontoGastosGenerales());
        if (distribucion.getMontoTrabajosExtra() != null) 
            sumaMontos = sumaMontos.add(distribucion.getMontoTrabajosExtra());

        if (sumaMontos.compareTo(cobroObra.getMonto()) > 0) {
            throw new IllegalArgumentException(
                String.format("La suma de distribución (%s) supera el monto asignado (%s)",
                              sumaMontos, cobroObra.getMonto()));
        }

        // Crear UNA asignación con toda la distribución
        crearAsignacionCompleta(cobroObra, obra, distribucion, cobroObra.getMonto());
    }

    /**
     * Crear asignación de cobro con distribución completa por ítems
     * Crea UNA SOLA asignación con todos los ítems distribuidos
     */
    private void crearAsignacionCompleta(CobroObra cobroObra, Obra obra, 
                                         DistribucionItemsDTO distribucion,
                                         BigDecimal montoTotal) {
        AsignacionCobroObra asignacion = new AsignacionCobroObra();
        asignacion.setCobroObra(cobroObra);
        asignacion.setObra(obra);
        // No seteamos presupuestoNoCliente porque Obra tiene @OneToMany, no ManyToOne
        asignacion.setEmpresaId(obra.getEmpresaId());
        asignacion.setMontoAsignado(montoTotal);
        
        // Setear distribución por ítems
        asignacion.setMontoProfesionales(distribucion.getMontoProfesionales());
        asignacion.setPorcentajeProfesionales(distribucion.getPorcentajeProfesionales());
        
        asignacion.setMontoMateriales(distribucion.getMontoMateriales());
        asignacion.setPorcentajeMateriales(distribucion.getPorcentajeMateriales());
        
        asignacion.setMontoGastosGenerales(distribucion.getMontoGastosGenerales());
        asignacion.setPorcentajeGastosGenerales(distribucion.getPorcentajeGastosGenerales());
        
        asignacion.setMontoTrabajosExtra(distribucion.getMontoTrabajosExtra());
        asignacion.setPorcentajeTrabajosExtra(distribucion.getPorcentajeTrabajosExtra());
        
        asignacion.setEstado(AsignacionCobroObra.ESTADO_ACTIVA);
        asignacion.setObservaciones("Asignación automática desde cobro empresa");
        
        asignacionCobroObraRepository.save(asignacion);
    }

    /**
     * Listar cobros de empresa
     */
    @Transactional(readOnly = true)
    public List<CobroEmpresaResponseDTO> listarCobrosEmpresa(Long empresaId, String estado) {
        log.info("Listando cobros empresa para empresaId: {}, estado: {}", empresaId, estado);

        List<CobroEmpresa> cobros;
        
        if (estado != null && !estado.isBlank()) {
            EstadoCobroEmpresa estadoEnum = EstadoCobroEmpresa.fromString(estado);
            cobros = cobroEmpresaRepository.findByEmpresaIdAndEstadoOrderByFechaCobroDesc(
                empresaId, estadoEnum);
        } else {
            cobros = cobroEmpresaRepository.findByEmpresaIdOrderByFechaCobroDesc(empresaId);
        }

        return cobros.stream()
                .map(c -> mapearAResponseDTO(c, true))
                .collect(Collectors.toList());
    }

    /**
     * Obtener detalle de un cobro empresa
     */
    @Transactional(readOnly = true)
    public CobroEmpresaResponseDTO obtenerDetalleCobroEmpresa(Long id, Long empresaId) {
        log.info("Obteniendo detalle de cobro empresa {}", id);

        CobroEmpresa cobro = buscarCobroEmpresaPorIdYEmpresa(id, empresaId);
        return mapearAResponseDTOConAsignaciones(cobro);
    }

    /**
     * Obtener saldo disponible total
     */
    @Transactional(readOnly = true)
    public SaldoDisponibleResponseDTO obtenerSaldoDisponible(Long empresaId) {
        log.info("Calculando saldo disponible para empresa {}", empresaId);

        BigDecimal totalCobrado = cobroEmpresaRepository.calcularTotalCobradoByEmpresa(empresaId);
        BigDecimal totalAsignado = cobroEmpresaRepository.calcularTotalAsignadoByEmpresa(empresaId);
        BigDecimal saldoDisponible = cobroEmpresaRepository.calcularTotalDisponibleByEmpresa(empresaId);

        return SaldoDisponibleResponseDTO.builder()
                .empresaId(empresaId)
                .totalCobrado(totalCobrado)
                .totalPagado(totalAsignado)
                .saldoDisponible(saldoDisponible)
                .build();
    }

    /**
     * Obtener resumen de cobros empresa
     */
    @Transactional(readOnly = true)
    public ResumenCobrosEmpresaResponseDTO obtenerResumen(Long empresaId) {
        log.info("Generando resumen de cobros empresa para empresa {}", empresaId);

        BigDecimal totalCobrado = cobroEmpresaRepository.calcularTotalCobradoByEmpresa(empresaId);
        BigDecimal totalAsignado = cobroEmpresaRepository.calcularTotalAsignadoByEmpresa(empresaId);
        BigDecimal totalDisponible = cobroEmpresaRepository.calcularTotalDisponibleByEmpresa(empresaId);

        Long disponibles = cobroEmpresaRepository.contarCobrosPorEstado(
            empresaId, EstadoCobroEmpresa.DISPONIBLE);
        Long asignadosParcial = cobroEmpresaRepository.contarCobrosPorEstado(
            empresaId, EstadoCobroEmpresa.ASIGNADO_PARCIAL);
        Long asignadosTotal = cobroEmpresaRepository.contarCobrosPorEstado(
            empresaId, EstadoCobroEmpresa.ASIGNADO_TOTAL);
        Long anulados = cobroEmpresaRepository.contarCobrosPorEstado(
            empresaId, EstadoCobroEmpresa.ANULADO);

        ResumenCobrosEmpresaResponseDTO.CantidadesPorEstado cantidades = 
            ResumenCobrosEmpresaResponseDTO.CantidadesPorEstado.builder()
                .disponibles(disponibles.intValue())
                .asignadosParcial(asignadosParcial.intValue())
                .asignadosTotal(asignadosTotal.intValue())
                .anulados(anulados.intValue())
                .build();

        return ResumenCobrosEmpresaResponseDTO.builder()
                .totalCobrado(totalCobrado)
                .totalDisponible(totalDisponible)
                .totalAsignado(totalAsignado)
                .cobros(cantidades)
                .build();
    }

    /**
     * Eliminar cobro empresa (solo si no tiene asignaciones)
     */
    @Transactional
    public void eliminarCobroEmpresa(Long id, Long empresaId) {
        log.info("Eliminando cobro empresa {}", id);

        CobroEmpresa cobro = buscarCobroEmpresaPorIdYEmpresa(id, empresaId);

        // Validar que no tiene asignaciones
        if (cobro.getMontoAsignado().compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException(
                "No se puede eliminar un cobro que tiene asignaciones. " +
                "Considere anularlo en su lugar.");
        }

        cobroEmpresaRepository.delete(cobro);
        log.info("Cobro empresa {} eliminado exitosamente", id);
    }

    /**
     * Eliminar una asignación individual de cobro empresa
     * Libera el monto asignado y actualiza el estado del cobro
     */
    @Transactional
    public EliminarAsignacionResponseDTO eliminarAsignacionCobroEmpresa(
            Long cobroEmpresaId,
            Long asignacionId,
            Long empresaId) {
        
        log.info("Eliminando asignación {} del cobro empresa {}", asignacionId, cobroEmpresaId);

        // Buscar cobro empresa
        CobroEmpresa cobroEmpresa = buscarCobroEmpresaPorIdYEmpresa(cobroEmpresaId, empresaId);

        // Buscar asignación
        AsignacionCobroEmpresaObra asignacion = asignacionRepository.findById(asignacionId)
            .orElseThrow(() -> new IllegalArgumentException(
                "Asignación con ID " + asignacionId + " no encontrada"));

        // Verificar que la asignación pertenece al cobro empresa especificado
        if (!asignacion.getCobroEmpresa().getId().equals(cobroEmpresaId)) {
            throw new IllegalArgumentException(
                "La asignación " + asignacionId + " no pertenece al cobro empresa " + cobroEmpresaId);
        }

        // Obtener monto a liberar ANTES de eliminar
        BigDecimal montoLiberado = asignacion.getMontoAsignado();
        Long cobroObraId = asignacion.getCobroObra() != null ? asignacion.getCobroObra().getId() : null;

        // PASO 1: Eliminar la asignación primero (esto libera la FK)
        asignacionRepository.delete(asignacion);
        asignacionRepository.flush(); // Forzar eliminación inmediata

        // PASO 2: Eliminar distribuciones asociadas si existen
        if (cobroObraId != null) {
            List<AsignacionCobroObra> distribucionItems = 
                asignacionCobroObraRepository.findByCobroObraId(cobroObraId);
            
            if (!distribucionItems.isEmpty()) {
                log.info("Eliminando {} distribuciones asociadas", distribucionItems.size());
                asignacionCobroObraRepository.deleteAll(distribucionItems);
                asignacionCobroObraRepository.flush();
            }

            // PASO 3: Eliminar el cobro obra
            log.info("Eliminando cobro obra {}", cobroObraId);
            cobroObraRepository.deleteById(cobroObraId);
            cobroObraRepository.flush();
        }

        // PASO 4: Actualizar montos del cobro empresa
        cobroEmpresa.setMontoAsignado(cobroEmpresa.getMontoAsignado().subtract(montoLiberado));
        cobroEmpresa.setMontoDisponible(cobroEmpresa.getMontoDisponible().add(montoLiberado));

        // Actualizar estado
        cobroEmpresa.actualizarEstado();

        CobroEmpresa cobroActualizado = cobroEmpresaRepository.save(cobroEmpresa);

        log.info("Asignación {} eliminada. Monto liberado: {}. Nuevo disponible: {}", 
                 asignacionId, montoLiberado, cobroActualizado.getMontoDisponible());

        // Construir respuesta
        EliminarAsignacionResponseDTO.CobroActualizadoDTO cobroDTO = 
            new EliminarAsignacionResponseDTO.CobroActualizadoDTO(
                cobroActualizado.getId(),
                cobroActualizado.getMontoTotal(),
                cobroActualizado.getMontoAsignado(),
                cobroActualizado.getMontoDisponible(),
                cobroActualizado.getEstado().name()
            );

        return new EliminarAsignacionResponseDTO(
            "Asignación eliminada exitosamente",
            montoLiberado,
            cobroDTO
        );
    }

    /**
     * Listar todas las asignaciones de un cobro empresa
     */
    @Transactional(readOnly = true)
    public List<AsignacionCobroEmpresaObraResponseDTO> listarAsignacionesCobroEmpresa(
            Long cobroEmpresaId,
            Long empresaId) {
        
        log.info("Listando asignaciones del cobro empresa {}", cobroEmpresaId);

        // Buscar cobro empresa (valida que pertenezca a la empresa)
        buscarCobroEmpresaPorIdYEmpresa(cobroEmpresaId, empresaId);

        // Buscar asignaciones
        List<AsignacionCobroEmpresaObra> asignaciones = 
            asignacionRepository.findByCobroEmpresaId(cobroEmpresaId);

        log.info("Encontradas {} asignaciones", asignaciones.size());

        // Mapear a DTOs
        return asignaciones.stream()
            .map(this::mapearAsignacionADTO)
            .toList();
    }

    /**
     * Mapear AsignacionCobroEmpresaObra a DTO
     */
    private AsignacionCobroEmpresaObraResponseDTO mapearAsignacionADTO(AsignacionCobroEmpresaObra asignacion) {
        return AsignacionCobroEmpresaObraResponseDTO.builder()
            .id(asignacion.getId())
            .cobroEmpresaId(asignacion.getCobroEmpresa().getId())
            .cobroObraId(asignacion.getCobroObra() != null ? asignacion.getCobroObra().getId() : null)
            .obraId(asignacion.getCobroObra() != null && asignacion.getCobroObra().getObra() != null 
                ? asignacion.getCobroObra().getObra().getId() : null)
            .obraNombre(asignacion.getCobroObra() != null && asignacion.getCobroObra().getObra() != null 
                ? asignacion.getCobroObra().getObra().getNombre() : "Sin obra")
            .montoAsignado(asignacion.getMontoAsignado())
            .fechaAsignacion(asignacion.getFechaAsignacion())
            .build();
    }

    /**
     * Anular cobro empresa
     */
    @Transactional
    public CobroEmpresaResponseDTO anularCobroEmpresa(
            Long id, 
            Long empresaId, 
            AnularCobroEmpresaRequestDTO request) {
        
        log.info("Anulando cobro empresa {} por: {}", id, request.getMotivo());

        CobroEmpresa cobro = buscarCobroEmpresaPorIdYEmpresa(id, empresaId);

        // Validar que no esté ya anulado
        if (cobro.getEstado() == EstadoCobroEmpresa.ANULADO) {
            throw new IllegalStateException("El cobro ya está anulado");
        }

        // Cambiar estado
        cobro.setEstado(EstadoCobroEmpresa.ANULADO);
        cobro.setObservaciones(
            (cobro.getObservaciones() != null ? cobro.getObservaciones() + "\n" : "") +
            "ANULADO: " + request.getMotivo() + " - " + LocalDateTime.now()
        );

        CobroEmpresa cobroAnulado = cobroEmpresaRepository.save(cobro);
        log.info("Cobro empresa {} anulado exitosamente", id);

        return mapearAResponseDTO(cobroAnulado, false);
    }

    // ========== MÉTODOS AUXILIARES ==========

    private CobroEmpresa buscarCobroEmpresaPorIdYEmpresa(Long id, Long empresaId) {
        return cobroEmpresaRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException(
                    "Cobro empresa no encontrado con ID: " + id));
    }

    private void validarMontoPositivo(BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }
    }

    private CobroEmpresaResponseDTO mapearAResponseDTO(CobroEmpresa cobro, boolean incluirCantidadAsignaciones) {
        CobroEmpresaResponseDTO dto = CobroEmpresaResponseDTO.builder()
                .id(cobro.getId())
                .empresaId(cobro.getEmpresaId())
                .montoTotal(cobro.getMontoTotal())
                .montoAsignado(cobro.getMontoAsignado())
                .montoDisponible(cobro.getMontoDisponible())
                .descripcion(cobro.getDescripcion())
                .fechaCobro(cobro.getFechaCobro())
                .metodoPago(cobro.getMetodoPago() != null ? cobro.getMetodoPago().name() : null)
                .numeroComprobante(cobro.getNumeroComprobante())
                .tipoComprobante(cobro.getTipoComprobante())
                .observaciones(cobro.getObservaciones())
                .estado(cobro.getEstado() != null ? cobro.getEstado().name() : null)
                .createdAt(cobro.getCreatedAt())
                .updatedAt(cobro.getUpdatedAt())
                .build();

        if (incluirCantidadAsignaciones) {
            Long cantidad = asignacionRepository.contarAsignacionesByCobroEmpresa(cobro.getId());
            dto.setCantidadAsignaciones(cantidad.intValue());
        }

        return dto;
    }

    private CobroEmpresaResponseDTO mapearAResponseDTOConAsignaciones(CobroEmpresa cobro) {
        CobroEmpresaResponseDTO dto = mapearAResponseDTO(cobro, false);

        // Cargar asignaciones
        List<AsignacionCobroEmpresaObra> asignaciones = 
            asignacionRepository.findByCobroEmpresaId(cobro.getId());

        List<AsignacionCobroEmpresaObraResponseDTO> asignacionesDTOs = asignaciones.stream()
                .map(this::mapearAsignacionAResponseDTO)
                .collect(Collectors.toList());

        dto.setAsignaciones(asignacionesDTOs);
        dto.setCantidadAsignaciones(asignacionesDTOs.size());

        return dto;
    }

    private AsignacionCobroEmpresaObraResponseDTO mapearAsignacionAResponseDTO(
            AsignacionCobroEmpresaObra asignacion) {
        
        String obraDireccion = null;
        Long obraId = null;
        boolean tieneDistribucion = false;

        CobroObra cobroObra = asignacion.getCobroObra();
        if (cobroObra != null) {
            
            if (cobroObra.getObra() != null) {
                obraId = cobroObra.getObra().getId();
                obraDireccion = construirDireccionObra(cobroObra.getObra());
            }

            // Verificar si tiene distribución por ítems
            Long countAsignaciones = asignacionCobroObraRepository
                .countByCobroObraId(cobroObra.getId());
            tieneDistribucion = countAsignaciones > 0;
        }

        return AsignacionCobroEmpresaObraResponseDTO.builder()
                .id(asignacion.getId())
                .cobroEmpresaId(asignacion.getCobroEmpresaId())
                .cobroObraId(asignacion.getCobroObraId())
                .obraId(obraId)
                .obraDireccion(obraDireccion)
                .montoAsignado(asignacion.getMontoAsignado())
                .fechaAsignacion(asignacion.getFechaAsignacion())
                .usuarioAsignacion(asignacion.getUsuarioAsignacion())
                .observaciones(asignacion.getObservaciones())
                .tieneDistribucionItems(tieneDistribucion)
                .build();
    }

    private String construirDireccionObra(Obra obra) {
        StringBuilder sb = new StringBuilder();
        
        if (obra.getDireccionObraCalle() != null) {
            sb.append(obra.getDireccionObraCalle());
        }
        if (obra.getDireccionObraAltura() != null) {
            if (sb.length() > 0) sb.append(" ");
            sb.append(obra.getDireccionObraAltura());
        }
        if (obra.getDireccionObraTorre() != null) {
            if (sb.length() > 0) sb.append(", Torre ");
            sb.append(obra.getDireccionObraTorre());
        }
        if (obra.getDireccionObraPiso() != null) {
            if (sb.length() > 0) sb.append(", Piso ");
            sb.append(obra.getDireccionObraPiso());
        }
        if (obra.getDireccionObraDepartamento() != null) {
            if (sb.length() > 0) sb.append(", Depto ");
            sb.append(obra.getDireccionObraDepartamento());
        }
        
        return sb.length() > 0 ? sb.toString() : "Sin dirección";
    }

    /**
     * Obtener distribución consolidada de cobros empresa por obra
     * Agrupa todas las asignaciones de cobros empresa por obra y suma sus distribuciones
     */
    @Transactional(readOnly = true)
    public List<DistribucionCobroObraResponseDTO> obtenerDistribucionPorObra(Long empresaId) {
        log.info("Obteniendo distribución de cobros empresa por obra para empresaId: {}", empresaId);

        // Obtener todas las asignaciones de cobros obra de la empresa
        List<AsignacionCobroObra> asignaciones = asignacionCobroObraRepository.findByEmpresaIdAndEstado(empresaId, "ACTIVA");

        // Map para agrupar por obra (clave: obraId, valor: DTO consolidado)
        Map<Long, DistribucionCobroObraResponseDTO> distribucionMap = new HashMap<>();

        for (AsignacionCobroObra asignacion : asignaciones) {
            Obra obra = asignacion.getObra();
            Long obraId = obra.getId();

            // Obtener o crear DTO para esta obra
            DistribucionCobroObraResponseDTO dto = distribucionMap.get(obraId);
            
            if (dto == null) {
                // Primera vez que vemos esta obra - crear DTO
                Integer numeroPresupuesto = null;
                if (obra.getPresupuestosNoCliente() != null && !obra.getPresupuestosNoCliente().isEmpty()) {
                    Long numeroLong = obra.getPresupuestosNoCliente().get(0).getNumeroPresupuesto();
                    numeroPresupuesto = numeroLong != null ? numeroLong.intValue() : null;
                }
                
                dto = DistribucionCobroObraResponseDTO.builder()
                    .obraId(obraId)
                    .nombreObra(construirDireccionObra(obra))
                    .numeroPresupuesto(numeroPresupuesto)
                    .totalCobradoAsignado(BigDecimal.ZERO)
                    .montoProfesionales(BigDecimal.ZERO)
                    .montoMateriales(BigDecimal.ZERO)
                    .montoGastosGenerales(BigDecimal.ZERO)
                    .montoTrabajosExtra(BigDecimal.ZERO)
                    .build();
            }

            // Acumular montos de esta asignación
            dto.setTotalCobradoAsignado(dto.getTotalCobradoAsignado().add(
                asignacion.getMontoAsignado() != null ? asignacion.getMontoAsignado() : BigDecimal.ZERO));
            
            dto.setMontoProfesionales(dto.getMontoProfesionales().add(
                asignacion.getMontoProfesionales() != null ? asignacion.getMontoProfesionales() : BigDecimal.ZERO));
            
            dto.setMontoMateriales(dto.getMontoMateriales().add(
                asignacion.getMontoMateriales() != null ? asignacion.getMontoMateriales() : BigDecimal.ZERO));
            
            dto.setMontoGastosGenerales(dto.getMontoGastosGenerales().add(
                asignacion.getMontoGastosGenerales() != null ? asignacion.getMontoGastosGenerales() : BigDecimal.ZERO));
            
            dto.setMontoTrabajosExtra(dto.getMontoTrabajosExtra().add(
                asignacion.getMontoTrabajosExtra() != null ? asignacion.getMontoTrabajosExtra() : BigDecimal.ZERO));

            // Guardar DTO actualizado en el map
            distribucionMap.put(obraId, dto);
        }

        // Calcular totales y convertir a lista
        List<DistribucionCobroObraResponseDTO> resultado = new ArrayList<>();
        
        for (DistribucionCobroObraResponseDTO dto : distribucionMap.values()) {
            BigDecimal totalDistribuido = dto.getMontoProfesionales()
                .add(dto.getMontoMateriales())
                .add(dto.getMontoGastosGenerales())
                .add(dto.getMontoTrabajosExtra());
            
            dto.setTotalDistribuido(totalDistribuido);
            dto.setSaldoSinDistribuir(dto.getTotalCobradoAsignado().subtract(totalDistribuido));
            
            resultado.add(dto);
        }

        // Ordenar por obraId
        resultado.sort((a, b) -> Long.compare(a.getObraId(), b.getObraId()));
        
        return resultado;
    }
}
