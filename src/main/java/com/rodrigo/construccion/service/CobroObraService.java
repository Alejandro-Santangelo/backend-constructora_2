package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignacionCobroObraRequestDTO;
import com.rodrigo.construccion.dto.request.CobroObraRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionCobroObraResponseDTO;
import com.rodrigo.construccion.dto.response.CobroObraResponseDTO;
import com.rodrigo.construccion.model.entity.AsignacionCobroObra;
import com.rodrigo.construccion.model.entity.CobroObra;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.repository.AsignacionCobroObraRepository;
import com.rodrigo.construccion.repository.CobroObraRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import com.rodrigo.construccion.config.TenantContext;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CobroObraService implements ICobroObraService {

    private final CobroObraRepository cobroObraRepository;
    private final ObraRepository obraRepository;
    private final PresupuestoNoClienteRepository presupuestoNoClienteRepository;
    private final AsignacionCobroObraRepository asignacionCobroObraRepository;

    @Override
    @Transactional
    public CobroObraResponseDTO crearCobro(CobroObraRequestDTO request) {
        // Validar presupuesto
        PresupuestoNoCliente presupuesto = presupuestoNoClienteRepository
                .findById(request.getPresupuestoNoClienteId())
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + request.getPresupuestoNoClienteId()));
        
        // Validar estado del presupuesto - usar enum PresupuestoEstado
        if (presupuesto.getEstado() != com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO 
            && presupuesto.getEstado() != com.rodrigo.construccion.enums.PresupuestoEstado.EN_EJECUCION) {
            throw new RuntimeException("No se puede crear cobro para presupuesto en estado: " + presupuesto.getEstado() 
                + ". Debe estar APROBADO o EN_EJECUCION");
        }
        
        // Validar fechas
        if (request.getFechaEmision() != null && request.getFechaEmision().isAfter(LocalDate.now())) {
            throw new RuntimeException("La fecha de emisión no puede ser futura");
        }
        
        if (request.getFechaVencimiento() != null && request.getFechaEmision() != null 
            && request.getFechaVencimiento().isBefore(request.getFechaEmision())) {
            throw new RuntimeException("La fecha de vencimiento no puede ser anterior a la fecha de emisión");
        }

        // Validar distribución por ítems
        validarDistribucionPorItems(request);

        CobroObra cobro = new CobroObra();
        mapearRequestAEntity(request, cobro);
        
        // Asignar obra si existe (puede ser null para presupuestos sin aprobar)
        if (request.getObraId() != null) {
            Obra obra = obraRepository.findById(request.getObraId())
                    .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + request.getObraId()));
            cobro.setObra(obra);
        }
        
        cobro.setPresupuestoNoCliente(presupuesto);
        
        // Establecer estado por defecto si no viene
        if (cobro.getEstado() == null || cobro.getEstado().isEmpty()) {
            cobro.setEstado(CobroObra.ESTADO_PENDIENTE);
        }

        CobroObra cobroGuardado = cobroObraRepository.save(cobro);
        
        // ========== CREAR ASIGNACIONES SI VIENEN EN EL REQUEST ==========
        if (request.getAsignaciones() != null && !request.getAsignaciones().isEmpty()) {
            crearAsignacionesParaCobro(cobroGuardado, request.getAsignaciones(), request.getEmpresaId());
        }
        
        return mapearEntityAResponse(cobroGuardado);
    }

    @Override
    @Transactional
    public CobroObraResponseDTO actualizarCobro(Long id, CobroObraRequestDTO request) {
        CobroObra cobro = cobroObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + id));

        // Validar que el cobro puede ser modificado
        if (!cobro.puedeSerModificado()) {
            throw new RuntimeException("El cobro no puede ser modificado en su estado actual: " + cobro.getEstado());
        }

        // Validar distribución por ítems
        validarDistribucionPorItems(request);

        mapearRequestAEntity(request, cobro);
        cobro.setFechaModificacion(LocalDateTime.now());

        CobroObra cobroActualizado = cobroObraRepository.save(cobro);
        
        // Si el request incluye asignaciones, procesarlas (crear nuevas)
        if (request.getAsignaciones() != null && !request.getAsignaciones().isEmpty()) {
            crearAsignacionesParaCobro(cobroActualizado, request.getAsignaciones(), request.getEmpresaId());
        }
        
        return mapearEntityAResponse(cobroActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public CobroObraResponseDTO obtenerCobroPorId(Long id) {
        CobroObra cobro = cobroObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + id));
        return mapearEntityAResponse(cobro);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosPorObra(Long obraId) {
        List<CobroObra> cobros = cobroObraRepository.findByObraId(obraId);
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosPendientes(Long obraId) {
        List<CobroObra> cobros = cobroObraRepository.findCobrosPendientesByObra(obraId);
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosVencidos() {
        List<CobroObra> cobros = cobroObraRepository.findCobrosVencidos(LocalDate.now());
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosPorEmpresa(Long empresaId) {
        List<CobroObra> cobros = cobroObraRepository.findByEmpresaId(empresaId);
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CobroObraResponseDTO marcarComoCobrado(Long id) {
        CobroObra cobro = cobroObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + id));

        if (!cobro.puedeSerCobrado()) {
            throw new RuntimeException("El cobro no puede ser marcado como cobrado en su estado actual: " + cobro.getEstado());
        }

        cobro.marcarComoCobrado();
        CobroObra cobroActualizado = cobroObraRepository.save(cobro);
        return mapearEntityAResponse(cobroActualizado);
    }

    @Transactional
    public CobroObraResponseDTO marcarComoCobrado(Long cobroId, Long empresaId, LocalDate fechaCobro) {
        CobroObra cobro = cobroObraRepository.findById(cobroId)
            .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + cobroId));
        
        if (!cobro.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El cobro no pertenece a esta empresa");
        }
        
        if (!cobro.puedeSerCobrado()) {
            throw new RuntimeException("El cobro no puede ser marcado como cobrado en su estado actual: " + cobro.getEstado());
        }
        
        cobro.setEstado(CobroObra.ESTADO_COBRADO);
        cobro.setFechaCobro(fechaCobro);
        cobro.setFechaModificacion(LocalDateTime.now());
        
        CobroObra actualizado = cobroObraRepository.save(cobro);
        return mapearEntityAResponse(actualizado);
    }

    @Override
    @Transactional
    public void anularCobro(Long id) {
        CobroObra cobro = cobroObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + id));

        cobro.anular();
        cobroObraRepository.save(cobro);
    }

    @Transactional
    public CobroObraResponseDTO anularCobro(Long cobroId, Long empresaId, String motivo) {
        CobroObra cobro = cobroObraRepository.findById(cobroId)
            .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + cobroId));
        
        if (!cobro.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El cobro no pertenece a esta empresa");
        }
        
        cobro.setEstado(CobroObra.ESTADO_ANULADO);
        cobro.setMotivoAnulacion(motivo);
        cobro.setFechaModificacion(LocalDateTime.now());
        
        CobroObra actualizado = cobroObraRepository.save(cobro);
        return mapearEntityAResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminarCobro(Long id) {
        CobroObra cobro = cobroObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + id));

        // Permitir eliminar cobros PENDIENTES, COBRADOS o ANULADOS
        // El usuario especificó que necesita poder borrar cobros en estos estados
        if (!cobro.esPendiente() 
            && !cobro.getEstado().equals(CobroObra.ESTADO_COBRADO) 
            && !cobro.getEstado().equals(CobroObra.ESTADO_ANULADO)) {
            throw new RuntimeException("Solo se pueden eliminar cobros en estado PENDIENTE, COBRADO o ANULADO. Estado actual: " + cobro.getEstado());
        }

        // IMPORTANTE: Eliminar primero las asignaciones relacionadas para evitar violación de llave foránea
        List<AsignacionCobroObra> asignaciones = asignacionCobroObraRepository.findByCobroObraId(id);
        if (!asignaciones.isEmpty()) {
            asignacionCobroObraRepository.deleteAll(asignaciones);
        }

        // Ahora sí eliminar el cobro
        cobroObraRepository.delete(cobro);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalCobrado(Long obraId) {
        return cobroObraRepository.calcularTotalCobradoByObra(obraId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPendiente(Long obraId) {
        return cobroObraRepository.calcularTotalPendienteByObra(obraId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosPorFechas(LocalDate desde, LocalDate hasta) {
        List<CobroObra> cobros = cobroObraRepository.findByFechaCobroBetween(desde, hasta);
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void actualizarCobrosVencidos() {
        List<CobroObra> cobrosVencidos = cobroObraRepository.findCobrosVencidos(LocalDate.now());
        for (CobroObra cobro : cobrosVencidos) {
            if (cobro.esPendiente() && cobro.tieneVencimiento()) {
                cobro.marcarComoVencido();
            }
        }
        cobroObraRepository.saveAll(cobrosVencidos);
    }

    // ========== MÉTODOS POR DIRECCIÓN ==========

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosPorDireccion(
            Long presupuestoNoClienteId,
            String calle,
            String altura,
            String barrio,
            String torre,
            String piso,
            String depto) {
        List<CobroObra> cobros = cobroObraRepository.findByDireccionCompleta(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CobroObraResponseDTO> obtenerCobrosPendientesPorDireccion(
            Long presupuestoNoClienteId,
            String calle,
            String altura,
            String barrio,
            String torre,
            String piso,
            String depto) {
        List<CobroObra> cobros = cobroObraRepository.findCobrosPendientesByDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
        return cobros.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalCobradoPorDireccion(
            Long presupuestoNoClienteId,
            String calle,
            String altura,
            String barrio,
            String torre,
            String piso,
            String depto) {
        return cobroObraRepository.calcularTotalCobradoByDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPendientePorDireccion(
            Long presupuestoNoClienteId,
            String calle,
            String altura,
            String barrio,
            String torre,
            String piso,
            String depto) {
        return cobroObraRepository.calcularTotalPendienteByDireccion(
                presupuestoNoClienteId, calle, altura, barrio, torre, piso, depto);
    }

    @Override
    @Transactional
    public CobroObraResponseDTO marcarComoVencido(Long id) {
        CobroObra cobro = cobroObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + id));

        if (!cobro.esPendiente()) {
            throw new RuntimeException("Solo se pueden marcar como vencidos los cobros pendientes");
        }

        cobro.marcarComoVencido();
        CobroObra cobroActualizado = cobroObraRepository.save(cobro);
        return mapearEntityAResponse(cobroActualizado);
    }

    @Transactional
    public CobroObraResponseDTO marcarComoVencido(Long cobroId, Long empresaId) {
        CobroObra cobro = cobroObraRepository.findById(cobroId)
            .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + cobroId));
        
        if (!cobro.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El cobro no pertenece a esta empresa");
        }
        
        if (!cobro.esPendiente()) {
            throw new RuntimeException("Solo se pueden marcar como vencidos los cobros pendientes");
        }
        
        cobro.setEstado(CobroObra.ESTADO_VENCIDO);
        cobro.setFechaModificacion(LocalDateTime.now());
        
        CobroObra actualizado = cobroObraRepository.save(cobro);
        return mapearEntityAResponse(actualizado);
    }

    // ========== MÉTODOS PRIVADOS DE MAPEO ==========

    private void mapearRequestAEntity(CobroObraRequestDTO request, CobroObra cobro) {
        // Mapear empresaId del request
        if (request.getEmpresaId() != null) {
            cobro.setEmpresaId(request.getEmpresaId());
        }
        
        // Campos de dirección - mapear desde direccionObra* a campos simples
        cobro.setCalle(request.getDireccionObraCalle());
        cobro.setAltura(request.getDireccionObraAltura());
        cobro.setBarrio(request.getDireccionObraBarrio());
        cobro.setTorre(request.getDireccionObraTorre());
        cobro.setPiso(request.getDireccionObraPiso());
        cobro.setDepto(request.getDireccionObraDepartamento());

        // Datos del cobro
        cobro.setTipoCobro(request.getTipoCobro());
        cobro.setMontoCobrar(request.getMontoCobrar());
        cobro.setMontoCobrado(request.getMontoCobrado());
        cobro.setFechaEmision(request.getFechaEmision());
        cobro.setFechaCobro(request.getFechaCobro());
        cobro.setFechaVencimiento(request.getFechaVencimiento());
        cobro.setMonto(request.getMonto());
        
        // Frontend envía "descripcion" - mapear a "concepto"
        cobro.setConcepto(request.getDescripcion());

        // Comprobantes - Frontend envía "numeroComprobante"
        cobro.setNumeroRecibo(request.getNumeroComprobante());
        cobro.setNumeroFactura(request.getNumeroFactura());
        cobro.setTipoComprobante(request.getTipoComprobante());

        // Pago y estado
        cobro.setMetodoPago(request.getMetodoPago());
        
        if (request.getEstado() != null && !request.getEstado().isEmpty()) {
            cobro.setEstado(request.getEstado());
        }
        
        cobro.setObservaciones(request.getObservaciones());
        cobro.setComprobanteUrl(request.getComprobanteUrl());

        // Distribución por ítems
        cobro.setModoDistribucion(request.getModoDistribucion() != null ? 
            request.getModoDistribucion() : CobroObra.MODO_GENERAL);
        cobro.setMontoProfesionales(request.getMontoProfesionales());
        cobro.setMontoMateriales(request.getMontoMateriales());
        cobro.setMontoGastosGenerales(request.getMontoGastosGenerales());
        cobro.setPorcentajeProfesionales(request.getPorcentajeProfesionales());
        cobro.setPorcentajeMateriales(request.getPorcentajeMateriales());
        cobro.setPorcentajeGastosGenerales(request.getPorcentajeGastosGenerales());
    }

    private CobroObraResponseDTO mapearEntityAResponse(CobroObra cobro) {
        CobroObraResponseDTO response = new CobroObraResponseDTO();
        response.setId(cobro.getId());
        response.setObraId(cobro.getObraId());
        response.setNombreObra(cobro.getNombreObra());
        response.setDireccionObra(cobro.getDireccionObra());
        response.setPresupuestoNoClienteId(cobro.getPresupuestoId());
        response.setEmpresaId(cobro.getEmpresaId());

        // Campos de dirección
        response.setCalle(cobro.getCalle());
        response.setAltura(cobro.getAltura());
        response.setBarrio(cobro.getBarrio());
        response.setTorre(cobro.getTorre());
        response.setPiso(cobro.getPiso());
        response.setDepto(cobro.getDepto());

        // Datos del cobro
        response.setTipoCobro(cobro.getTipoCobro());
        response.setMontoCobrar(cobro.getMontoCobrar());
        response.setMontoCobrado(cobro.getMontoCobrado());
        response.setFechaEmision(cobro.getFechaEmision());
        response.setFechaCobro(cobro.getFechaCobro());
        response.setFechaVencimiento(cobro.getFechaVencimiento());
        response.setMonto(cobro.getMonto());
        response.setConcepto(cobro.getConcepto());

        // Comprobantes
        response.setNumeroRecibo(cobro.getNumeroRecibo());
        response.setNumeroFactura(cobro.getNumeroFactura());
        response.setTipoComprobante(cobro.getTipoComprobante());

        // Pago y estado
        response.setMetodoPago(cobro.getMetodoPago());
        response.setEstado(cobro.getEstado());

        // Adicionales
        response.setObservaciones(cobro.getObservaciones());
        response.setMotivoAnulacion(cobro.getMotivoAnulacion());
        response.setComprobanteUrl(cobro.getComprobanteUrl());

        // Auditoría
        response.setFechaCreacion(cobro.getFechaCreacion());
        response.setFechaModificacion(cobro.getFechaModificacion());
        response.setUsuarioCreacionId(cobro.getUsuarioCreacionId());
        response.setUsuarioModificacionId(cobro.getUsuarioModificacionId());

        // Información calculada
        response.setEsPendiente(cobro.esPendiente());
        response.setEstaCobrado(cobro.estaCobrado());
        response.setEstaVencido(cobro.estaVencido());
        response.setTieneVencimiento(cobro.tieneVencimiento());

        // Distribución por ítems
        response.setModoDistribucion(cobro.getModoDistribucion());
        response.setMontoProfesionales(cobro.getMontoProfesionales());
        response.setMontoMateriales(cobro.getMontoMateriales());
        response.setMontoGastosGenerales(cobro.getMontoGastosGenerales());
        response.setPorcentajeProfesionales(cobro.getPorcentajeProfesionales());
        response.setPorcentajeMateriales(cobro.getPorcentajeMateriales());
        response.setPorcentajeGastosGenerales(cobro.getPorcentajeGastosGenerales());

        // Asignaciones a obras
        List<AsignacionCobroObra> asignaciones = asignacionCobroObraRepository.findByCobroObraId(cobro.getId());
        if (!asignaciones.isEmpty()) {
            response.setAsignaciones(asignaciones.stream()
                .map(this::mapearAsignacionAResponse)
                .collect(Collectors.toList()));
        }

        return response;
    }

    /**
     * Valida la distribución por ítems del cobro
     */
    private void validarDistribucionPorItems(CobroObraRequestDTO request) {
        String modo = request.getModoDistribucion();
        
        // Si no hay modo o es GENERAL, los campos de distribución deben ser null o 0
        if (modo == null || CobroObra.MODO_GENERAL.equals(modo)) {
            return;
        }
        
        // Si es POR_ITEMS, validar
        if (CobroObra.MODO_POR_ITEMS.equals(modo)) {
            BigDecimal montoProfesionales = request.getMontoProfesionales() != null ? 
                request.getMontoProfesionales() : BigDecimal.ZERO;
            BigDecimal montoMateriales = request.getMontoMateriales() != null ? 
                request.getMontoMateriales() : BigDecimal.ZERO;
            BigDecimal montoGastosGenerales = request.getMontoGastosGenerales() != null ? 
                request.getMontoGastosGenerales() : BigDecimal.ZERO;
            
            // Al menos uno de los montos debe ser mayor a 0
            if (montoProfesionales.compareTo(BigDecimal.ZERO) <= 0 
                && montoMateriales.compareTo(BigDecimal.ZERO) <= 0 
                && montoGastosGenerales.compareTo(BigDecimal.ZERO) <= 0) {
                throw new RuntimeException(
                    "Cuando el modo de distribución es POR_ITEMS, al menos uno de los montos debe ser mayor a 0");
            }
            
            // La suma de los montos debe ser igual al monto total (con tolerancia de 0.01)
            BigDecimal sumaDistribucion = montoProfesionales.add(montoMateriales).add(montoGastosGenerales);
            BigDecimal diferencia = sumaDistribucion.subtract(request.getMonto()).abs();
            
            if (diferencia.compareTo(new BigDecimal("0.01")) > 0) {
                throw new RuntimeException(
                    "La suma de la distribución por ítems (" + sumaDistribucion + 
                    ") no coincide con el monto total (" + request.getMonto() + ")");
            }
        }
    }

    /**
     * Crear asignaciones para un cobro recién creado
     */
    private void crearAsignacionesParaCobro(CobroObra cobro, List<AsignacionCobroObraRequestDTO> asignaciones, Long empresaId) {
        // Validar que la suma de asignaciones no exceda el monto del cobro
        BigDecimal sumaAsignaciones = asignaciones.stream()
            .map(AsignacionCobroObraRequestDTO::getMontoAsignado)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        if (sumaAsignaciones.compareTo(cobro.getMonto()) > 0) {
            throw new RuntimeException(
                "La suma de las asignaciones (" + sumaAsignaciones + 
                ") excede el monto del cobro (" + cobro.getMonto() + ")");
        }
        
        // Crear cada asignación
        for (AsignacionCobroObraRequestDTO asignacionDTO : asignaciones) {
            // Validar obra
            Obra obra = obraRepository.findById(asignacionDTO.getObraId())
                .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + asignacionDTO.getObraId()));
            
            AsignacionCobroObra asignacion = new AsignacionCobroObra();
            asignacion.setCobroObra(cobro);
            asignacion.setObra(obra);
            asignacion.setEmpresaId(empresaId);
            asignacion.setMontoAsignado(asignacionDTO.getMontoAsignado());
            
            // Distribución por items - OPCIONALES
            asignacion.setMontoProfesionales(asignacionDTO.getMontoProfesionales());
            asignacion.setMontoMateriales(asignacionDTO.getMontoMateriales());
            asignacion.setMontoGastosGenerales(asignacionDTO.getMontoGastosGenerales());
            asignacion.setPorcentajeProfesionales(asignacionDTO.getPorcentajeProfesionales());
            asignacion.setPorcentajeMateriales(asignacionDTO.getPorcentajeMateriales());
            asignacion.setPorcentajeGastosGenerales(asignacionDTO.getPorcentajeGastosGenerales());
            
            asignacion.setEstado(AsignacionCobroObra.ESTADO_ACTIVA);
            asignacion.setObservaciones(asignacionDTO.getObservaciones());
            
            // Asignar presupuesto si existe
            if (asignacionDTO.getPresupuestoNoClienteId() != null) {
                PresupuestoNoCliente presupuesto = presupuestoNoClienteRepository
                    .findById(asignacionDTO.getPresupuestoNoClienteId())
                    .orElse(null);
                asignacion.setPresupuestoNoCliente(presupuesto);
            }
            
            // Validar distribución (permite asignación parcial - suma de ítems <= monto asignado)
            if (!asignacion.validarDistribucion()) {
                throw new RuntimeException(
                    "La suma de la distribución por items de la asignación a obra " + 
                    obra.getNombre() + " excede el monto asignado");
            }
            
            asignacionCobroObraRepository.save(asignacion);
        }
    }

    /**
     * Mapear AsignacionCobroObra a Response DTO
     */
    private AsignacionCobroObraResponseDTO mapearAsignacionAResponse(AsignacionCobroObra asignacion) {
        AsignacionCobroObraResponseDTO dto = new AsignacionCobroObraResponseDTO();
        dto.setId(asignacion.getId());
        dto.setCobroObraId(asignacion.getCobroObra().getId());
        dto.setObraId(asignacion.getObra().getId());
        dto.setObraNombre(asignacion.getObra().getNombre());
        dto.setEmpresaId(asignacion.getEmpresaId());
        dto.setMontoAsignado(asignacion.getMontoAsignado());
        dto.setMontoProfesionales(asignacion.getMontoProfesionales());
        dto.setMontoMateriales(asignacion.getMontoMateriales());
        dto.setMontoGastosGenerales(asignacion.getMontoGastosGenerales());
        dto.setPorcentajeProfesionales(asignacion.getPorcentajeProfesionales());
        dto.setPorcentajeMateriales(asignacion.getPorcentajeMateriales());
        dto.setPorcentajeGastosGenerales(asignacion.getPorcentajeGastosGenerales());
        dto.setEstado(asignacion.getEstado());
        dto.setObservaciones(asignacion.getObservaciones());
        dto.setFechaCreacion(asignacion.getFechaCreacion());
        dto.setFechaModificacion(asignacion.getFechaModificacion());
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularSaldoDisponible(Long cobroId) {
        CobroObra cobro = cobroObraRepository.findById(cobroId)
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + cobroId));
        
        // Calcular total asignado en asignaciones activas
        BigDecimal totalAsignado = asignacionCobroObraRepository.calcularTotalAsignadoByCobro(cobroId);
        
        // Retornar saldo disponible
        return cobro.calcularSaldoDisponible(totalAsignado);
    }
}
