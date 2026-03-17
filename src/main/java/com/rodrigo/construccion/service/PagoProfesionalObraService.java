package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.PagoProfesionalObraRequestDTO;
import com.rodrigo.construccion.dto.response.PagoProfesionalObraResponseDTO;
import com.rodrigo.construccion.model.entity.AsignacionProfesionalObra;
import com.rodrigo.construccion.model.entity.PagoProfesionalObra;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.model.entity.PagoAdelantoAplicado;
import com.rodrigo.construccion.repository.AsignacionProfesionalObraRepository;
import com.rodrigo.construccion.repository.PagoProfesionalObraRepository;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;
import com.rodrigo.construccion.repository.PagoAdelantoAplicadoRepository;
import com.rodrigo.construccion.repository.ProfesionalJornalDiarioRepository;
import com.rodrigo.construccion.repository.HonorarioPorRubroRepository;
import com.rodrigo.construccion.repository.ProfesionalRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.config.TenantContext;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PagoProfesionalObraService implements IPagoProfesionalObraService {

    private final PagoProfesionalObraRepository pagoRepository;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final AsignacionProfesionalObraRepository asignacionRepository;
    private final PagoAdelantoAplicadoRepository pagoAdelantoAplicadoRepository;
    private final ProfesionalJornalDiarioRepository jornalDiarioRepository;
    private final HonorarioPorRubroRepository honorarioRubroRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ObraRepository obraRepository;

    @Override
    @Transactional(readOnly = true)
    public List<PagoProfesionalObraResponseDTO> listarTodosPorEmpresa(Long empresaId) {
        List<PagoProfesionalObra> pagos = pagoRepository.findByEmpresaId(empresaId);
        return pagos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PagoProfesionalObraResponseDTO crearPago(PagoProfesionalObraRequestDTO request) {
        // Validar que al menos un monto esté presente
        if (request.getMontoBruto() == null && request.getMontoPagado() == null && request.getMontoBase() == null) {
            throw new RuntimeException("Debe proporcionar al menos uno de los siguientes campos: montoBruto, montoPagado o montoBase");
        }
        
        // Validar que el profesional obra O asignación existe (depende del tipo de pago)
        ProfesionalObra profesionalObra = null;
        
        if (PagoProfesionalObra.TIPO_SEMANAL.equals(request.getTipoPago())) {
            // Para pagos semanales, buscar en asignaciones_profesional_obra
            AsignacionProfesionalObra asignacion = asignacionRepository.findById(request.getProfesionalObraId())
                    .orElseThrow(() -> new RuntimeException(
                        String.format("La asignación con ID %d no existe. Verifica que el profesional esté asignado a la obra.",
                                    request.getProfesionalObraId())
                    ));
            
            // Buscar o crear ProfesionalObra desde la asignación
            profesionalObra = profesionalObraRepository
                    .findByProfesionalIdAndObraId(asignacion.getProfesionalId(), asignacion.getObraId())
                    .orElseGet(() -> {
                        // Si no existe, crear la relación ProfesionalObra
                        ProfesionalObra nuevaRelacion = new ProfesionalObra();
                        nuevaRelacion.setProfesional(asignacion.getProfesional());
                        nuevaRelacion.setObra(asignacion.getObra());
                        nuevaRelacion.setEmpresaId(asignacion.getEmpresaId());
                        nuevaRelacion.setFechaDesde(asignacion.getFechaInicio());
                        nuevaRelacion.setEstado("ACTIVO");
                        return profesionalObraRepository.save(nuevaRelacion);
                    });
        } else {
            // Para otros tipos de pago (ADELANTO, PREMIO, etc), buscar en profesionales_obra
            profesionalObra = profesionalObraRepository.findById(request.getProfesionalObraId())
                    .orElseThrow(() -> new RuntimeException(
                        String.format("El profesional con ID de asignación %d no existe o fue eliminado.",
                                    request.getProfesionalObraId())
                    ));
        }

        // Si es pago semanal, validar que no exista otro pago para el mismo período
        if (PagoProfesionalObra.TIPO_SEMANAL.equals(request.getTipoPago())) {
            if (request.getPeriodoDesde() != null && request.getPeriodoHasta() != null) {
                boolean existePago = pagoRepository.existsPagoSemanalInPeriodo(
                        request.getProfesionalObraId(),
                        request.getPeriodoDesde(),
                        request.getPeriodoHasta()
                );
                if (existePago) {
                    throw new RuntimeException("Ya existe un pago semanal para el período especificado");
                }
            }
        }

        PagoProfesionalObra pago = new PagoProfesionalObra();
        mapearRequestAEntity(request, pago);
        pago.setProfesionalObra(profesionalObra);
        
        // Asignar empresa del DTO (prioridad) o del contexto (fallback)
        if (request.getEmpresaId() != null) {
            pago.setEmpresaId(request.getEmpresaId());
        } else {
            pago.setEmpresaId(TenantContext.getTenantId());
        }
        
        // Calcular presentismo si no viene
        if (pago.getPorcentajePresentismo() == null && pago.getDiasTrabajados() != null && pago.getDiasEsperados() != null) {
            pago.setPorcentajePresentismo(pago.calcularPorcentajePresentismo());
        }
        
        // Calcular monto final si no viene
        if (pago.getMontoFinal() == null) {
            pago.setMontoFinal(pago.calcularMontoFinal());
        }

        // ═══════════════════════════════════════════════════════════════
        // ⭐ NUEVO: Aplicar descuentos de adelantos si es pago semanal
        // ═══════════════════════════════════════════════════════════════
        
        if (PagoProfesionalObra.TIPO_SEMANAL.equals(request.getTipoPago()) && !Boolean.TRUE.equals(pago.getEsAdelanto())) {
            // Solo aplicar descuentos en pagos semanales regulares (no en adelantos)
            aplicarDescuentosDeAdelantos(pago);
        }
        
        // ═══════════════════════════════════════════════════════════════

        PagoProfesionalObra pagoGuardado = pagoRepository.save(pago);
        return mapearEntityAResponse(pagoGuardado);
    }

    @Override
    @Transactional
    public PagoProfesionalObraResponseDTO actualizarPago(Long id, PagoProfesionalObraRequestDTO request) {
        PagoProfesionalObra pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));

        // Validar que el pago puede ser modificado
        if (!pago.puedeSerModificado()) {
            throw new RuntimeException("El pago no puede ser modificado en su estado actual: " + pago.getEstado());
        }

        mapearRequestAEntity(request, pago);
        pago.setFechaModificacion(LocalDateTime.now());

        // Recalcular presentismo y monto final
        if (pago.getDiasTrabajados() != null && pago.getDiasEsperados() != null) {
            pago.setPorcentajePresentismo(pago.calcularPorcentajePresentismo());
        }
        pago.setMontoFinal(pago.calcularMontoFinal());

        PagoProfesionalObra pagoActualizado = pagoRepository.save(pago);
        return mapearEntityAResponse(pagoActualizado);
    }

    @Override
    @Transactional(readOnly = true)
    public PagoProfesionalObraResponseDTO obtenerPagoPorId(Long id) {
        PagoProfesionalObra pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));
        return mapearEntityAResponse(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoProfesionalObraResponseDTO> obtenerPagosPorProfesional(Long profesionalObraId) {
        List<PagoProfesionalObra> pagos = pagoRepository.findByProfesionalObraId(profesionalObraId);
        return pagos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoProfesionalObraResponseDTO> obtenerPagosPorTipo(Long profesionalObraId, String tipoPago) {
        List<PagoProfesionalObra> pagos = pagoRepository.findByProfesionalObraIdAndTipoPago(profesionalObraId, tipoPago);
        return pagos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoProfesionalObraResponseDTO> obtenerAdelantos(Long profesionalObraId) {
        List<PagoProfesionalObra> adelantos = pagoRepository.findAdelantosByProfesional(profesionalObraId);
        return adelantos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularAdelantosPendientes(Long profesionalObraId) {
        // Calcular total de adelantos
        BigDecimal totalAdelantos = pagoRepository.findAdelantosByProfesional(profesionalObraId).stream()
                .map(PagoProfesionalObra::getMontoFinal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Calcular total descontado en pagos semanales
        BigDecimal totalDescontado = pagoRepository.findByProfesionalObraIdAndTipoPago(
                profesionalObraId, 
                PagoProfesionalObra.TIPO_SEMANAL
        ).stream()
                .map(PagoProfesionalObra::getDescuentoAdelantos)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        BigDecimal pendiente = totalAdelantos.subtract(totalDescontado);
        return pendiente.max(BigDecimal.ZERO);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalPagado(Long profesionalObraId) {
        return pagoRepository.calcularTotalPagadoByProfesional(profesionalObraId);
    }

    @Override
    @Transactional
    public void anularPago(Long id) {
        PagoProfesionalObra pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));

        if (!pago.puedeSerModificado()) {
            throw new RuntimeException("El pago no puede ser anulado");
        }

        pago.anular();
        pagoRepository.save(pago);
    }

    @Transactional
    public PagoProfesionalObraResponseDTO anularPago(Long pagoId, Long empresaId, String motivo) {
        PagoProfesionalObra pago = pagoRepository.findById(pagoId)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));
        
        if (!pago.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El pago no pertenece a esta empresa");
        }
        
        if (!pago.puedeSerModificado()) {
            throw new RuntimeException("El pago no puede ser anulado");
        }
        
        pago.setEstado(PagoProfesionalObra.ESTADO_ANULADO);
        pago.setMotivoAnulacion(motivo);
        pago.setFechaModificacion(LocalDateTime.now());
        
        PagoProfesionalObra actualizado = pagoRepository.save(pago);
        return mapearEntityAResponse(actualizado);
    }

    @Override
    @Transactional
    public void eliminarPago(Long id) {
        PagoProfesionalObra pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));

        // Validar que se puede eliminar (por ejemplo, solo si es reciente o en borrador)
        if (!pago.puedeSerModificado()) {
            throw new RuntimeException("El pago no puede ser eliminado");
        }

        pagoRepository.delete(pago);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoProfesionalObraResponseDTO> obtenerPagosPorFechas(LocalDate desde, LocalDate hasta) {
        List<PagoProfesionalObra> pagos = pagoRepository.findByFechaPagoBetween(desde, hasta);
        return pagos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePagoSemanalEnPeriodo(Long profesionalObraId, LocalDate periodoDesde, LocalDate periodoHasta) {
        return pagoRepository.existsPagoSemanalInPeriodo(profesionalObraId, periodoDesde, periodoHasta);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularPromedioPresentismo(Long profesionalObraId) {
        BigDecimal promedio = pagoRepository.calcularPromedioPresentismo(profesionalObraId);
        return promedio != null ? promedio : BigDecimal.ZERO;
    }

    // ========== MÉTODOS PRIVADOS DE MAPEO ==========

    private void mapearRequestAEntity(PagoProfesionalObraRequestDTO request, PagoProfesionalObra pago) {
        pago.setTipoPago(request.getTipoPago());
        pago.setFechaPago(request.getFechaPago());
        pago.setPeriodoDesde(request.getPeriodoDesde());
        pago.setPeriodoHasta(request.getPeriodoHasta());
        pago.setSemana(request.getSemana());
        
        // Campos nuevos (frontend requirements)
        // Manejar montoPagado como alias de montoBruto para compatibilidad
        if (request.getMontoBruto() != null) {
            pago.setMontoBruto(request.getMontoBruto());
            pago.setMontoBase(request.getMontoBruto()); // Compatibilidad legacy
        } else if (request.getMontoPagado() != null) {
            pago.setMontoBruto(request.getMontoPagado());
            pago.setMontoBase(request.getMontoPagado()); // Compatibilidad
        } else if (request.getMontoBase() != null) {
            pago.setMontoBase(request.getMontoBase());
            pago.setMontoBruto(request.getMontoBase()); // Compatibilidad
        }
        
        if (request.getDescuentoAdelantos() != null) {
            pago.setDescuentoAdelantos(request.getDescuentoAdelantos());
        }
        
        if (request.getDescuentoPresentismo() != null) {
            pago.setDescuentoPresentismo(request.getDescuentoPresentismo());
        }
        
        // montoNeto se calculará automáticamente en @PrePersist/@PreUpdate
        
        if (request.getAjustes() != null) {
            pago.setAjustes(request.getAjustes());
        }
        
        if (request.getMontoFinal() != null) {
            pago.setMontoFinal(request.getMontoFinal());
        }
        
        // Datos de premios/bonos
        pago.setPremioTipo(request.getPremioTipo());
        pago.setPremioValor(request.getPremioValor());
        pago.setPremioBase(request.getPremioBase());
        pago.setPremioConcepto(request.getPremioConcepto());
        
        // Presentismo
        pago.setDiasTrabajados(request.getDiasTrabajados());
        pago.setDiasEsperados(request.getDiasEsperados());
        
        if (request.getPorcentajePresentismo() != null) {
            pago.setPorcentajePresentismo(request.getPorcentajePresentismo());
        }
        
        // Información del comprobante
        pago.setNumeroRecibo(request.getNumeroRecibo());
        pago.setMetodoPago(request.getMetodoPago());
        pago.setNumeroComprobante(request.getNumeroComprobante());
        
        if (request.getEstado() != null && !request.getEstado().isEmpty()) {
            pago.setEstado(request.getEstado());
        }
        
        pago.setConcepto(request.getConcepto());
        pago.setObservaciones(request.getObservaciones());
        pago.setComprobanteUrl(request.getComprobanteUrl());
        
        // ========== CAMPOS DE ADELANTOS ==========
        if (request.getEsAdelanto() != null) {
            pago.setEsAdelanto(request.getEsAdelanto());
        }
        
        if (request.getPeriodoAdelanto() != null) {
            pago.setPeriodoAdelanto(request.getPeriodoAdelanto());
        }
        
        if (request.getEstadoAdelanto() != null) {
            pago.setEstadoAdelanto(request.getEstadoAdelanto());
        }
        
        if (request.getSaldoAdelantoPorDescontar() != null) {
            pago.setSaldoAdelantoPorDescontar(request.getSaldoAdelantoPorDescontar());
        }
        
        if (request.getMontoOriginalAdelanto() != null) {
            pago.setMontoOriginalAdelanto(request.getMontoOriginalAdelanto());
        }
        
        // NOTA: adelantosAplicadosIds ahora se maneja mediante tabla relacional
        // No hay campo en el request para setear
        
        if (request.getSemanaReferencia() != null) {
            pago.setSemanaReferencia(request.getSemanaReferencia());
        }
    }

    private PagoProfesionalObraResponseDTO mapearEntityAResponse(PagoProfesionalObra pago) {
        PagoProfesionalObraResponseDTO response = new PagoProfesionalObraResponseDTO();
        response.setId(pago.getId());
        response.setProfesionalObraId(pago.getProfesionalObraId());
        response.setPresupuestoNoClienteId(pago.getPresupuestoNoClienteId());
        response.setNombreProfesional(pago.getNombreProfesional());
        response.setDireccionObra(pago.getDireccionObra());
        response.setEmpresaId(pago.getEmpresaId());

        response.setTipoPago(pago.getTipoPago());
        response.setFechaPago(pago.getFechaPago());

        response.setPeriodoDesde(pago.getPeriodoDesde());
        response.setPeriodoHasta(pago.getPeriodoHasta());

        // Campos nuevos (frontend requirements)
        response.setMontoBruto(pago.getMontoBruto());
        response.setDescuentoAdelantos(pago.getDescuentoAdelantos());
        response.setDescuentoPresentismo(pago.getDescuentoPresentismo());
        response.setMontoNeto(pago.getMontoNeto());
        
        // montoPagado: alias para compatibilidad con frontend
        // Prioridad: montoNeto > montoFinal > montoBruto
        BigDecimal montoPagado = pago.getMontoNeto();
        if (montoPagado == null) {
            montoPagado = pago.getMontoFinal();
        }
        if (montoPagado == null) {
            montoPagado = pago.getMontoBruto();
        }
        response.setMontoPagado(montoPagado);
        
        // Campos legacy (compatibilidad)
        response.setMontoBase(pago.getMontoBase());
        response.setAjustes(pago.getAjustes());
        response.setMontoFinal(pago.getMontoFinal());

        response.setPremioTipo(pago.getPremioTipo());
        response.setPremioValor(pago.getPremioValor());
        response.setPremioBase(pago.getPremioBase());
        response.setPremioConcepto(pago.getPremioConcepto());
        response.setMontoPremioCalculado(pago.calcularMontoPremio());

        response.setDiasTrabajados(pago.getDiasTrabajados());
        response.setDiasEsperados(pago.getDiasEsperados());
        response.setPorcentajePresentismo(pago.getPorcentajePresentismo());

        response.setNumeroRecibo(pago.getNumeroRecibo());
        response.setMetodoPago(pago.getMetodoPago());
        response.setNumeroComprobante(pago.getNumeroComprobante());

        response.setEstado(pago.getEstado());

        response.setConcepto(pago.getConcepto());
        response.setObservaciones(pago.getObservaciones());
        response.setComprobanteUrl(pago.getComprobanteUrl());

        response.setFechaCreacion(pago.getFechaCreacion());
        response.setFechaModificacion(pago.getFechaModificacion());
        response.setUsuarioCreacionId(pago.getUsuarioCreacionId());
        response.setUsuarioModificacionId(pago.getUsuarioModificacionId());

        // Información calculada
        response.setEsPagoSemanal(pago.esPagoSemanal());
        response.setEsAdelanto(pago.esAdelanto());
        response.setEsPremio(pago.esPremio());
        response.setEsBono(pago.esBono());
        response.setEstaPagado(pago.estaPagado());
        
        // ========== CAMPOS DE ADELANTOS ==========
        response.setEsAdelantoRegistrado(pago.getEsAdelanto());
        response.setPeriodoAdelanto(pago.getPeriodoAdelanto());
        response.setEstadoAdelanto(pago.getEstadoAdelanto());
        response.setSaldoAdelantoPorDescontar(pago.getSaldoAdelantoPorDescontar());
        response.setMontoOriginalAdelanto(pago.getMontoOriginalAdelanto());
        
        // Obtener IDs de adelantos aplicados desde tabla relacional
        if (pago.getAdelantosAplicados() != null && !pago.getAdelantosAplicados().isEmpty()) {
            List<Long> idsAplicados = pago.getAdelantosAplicados().stream()
                .map(paa -> paa.getAdelanto().getId())
                .collect(Collectors.toList());
            response.setAdelantosAplicadosIds(idsAplicados);
        }
        
        response.setSemanaReferencia(pago.getSemanaReferencia());

        return response;
    }

    @Override
    public List<PagoProfesionalObraResponseDTO> obtenerPagosPendientes(Long profesionalObraId) {
        List<PagoProfesionalObra> pagos = pagoRepository.findByProfesionalObraIdAndTipoPago(
            profesionalObraId, "PENDIENTE");
        return pagos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PagoProfesionalObraResponseDTO> obtenerPagosPorTipoEmpresa(String tipoPago) {
        Long empresaId = TenantContext.getTenantId();
        List<PagoProfesionalObra> pagos = pagoRepository.findByEmpresaIdAndTipoPago(empresaId, tipoPago);
        return pagos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public PagoProfesionalObraResponseDTO marcarComoPagado(Long id, LocalDate fechaPago) {
        PagoProfesionalObra pago = pagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + id));

        pago.setEstado(PagoProfesionalObra.ESTADO_PAGADO);
        if (fechaPago != null) {
            pago.setFechaPago(fechaPago);
        } else {
            pago.setFechaPago(LocalDate.now());
        }
        pago.setFechaModificacion(LocalDateTime.now());

        PagoProfesionalObra pagoActualizado = pagoRepository.save(pago);
        return mapearEntityAResponse(pagoActualizado);
    }

    @Transactional
    public PagoProfesionalObraResponseDTO marcarComoPagado(Long pagoId, Long empresaId, LocalDate fechaPago) {
        PagoProfesionalObra pago = pagoRepository.findById(pagoId)
            .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + pagoId));
        
        if (!pago.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("El pago no pertenece a esta empresa");
        }
        
        pago.setEstado(PagoProfesionalObra.ESTADO_PAGADO);
        pago.setFechaPago(fechaPago);
        pago.setFechaModificacion(LocalDateTime.now());
        
        PagoProfesionalObra actualizado = pagoRepository.save(pago);
        return mapearEntityAResponse(actualizado);
    }

    // ══════════════════════════════════════════════════════════════════════════════════
    // LÓGICA DE ADELANTOS
    // ══════════════════════════════════════════════════════════════════════════════════

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
        if (!PagoProfesionalObra.TIPO_SEMANAL.equals(pagoRegular.getTipoPago())) {
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
            
            // ✅ GUARDAR EN TABLA RELACIONAL (Reemplaza JSONB)
            PagoAdelantoAplicado pagoAdelantoAplicado = new PagoAdelantoAplicado(
                pagoRegular, 
                adelanto, 
                descuentoAdelanto
            );
            pagoAdelantoAplicadoRepository.save(pagoAdelantoAplicado);
            
            // Agregar ID a la lista para las observaciones
            adelantosAplicadosIds.add(adelanto.getId());
            descuentoAcumulado = descuentoAcumulado.add(descuentoAdelanto);
        }
        
        // Actualizar el pago regular con la información de adelantos aplicados
        pagoRegular.setDescuentoAdelantos(descuentoAcumulado);
        
        // ✅ YA NO SE USA JSONB - La información está en tabla relacional pago_adelantos_aplicados
        
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

    /**
     * Crear múltiples pagos parciales por asignación (batch)
     */
    @Override
    @Transactional
    public com.rodrigo.construccion.dto.response.PagoProfesionalBatchResponseDTO crearPagosBatch(
            com.rodrigo.construccion.dto.request.PagoProfesionalBatchRequestDTO request) {
        
        log.info("📦 Iniciando creación de pagos batch para empresa: {}", request.getEmpresaId());
        log.info("📋 Total de asignaciones a pagar: {}", request.getImportesPorAsignacion().size());
        
        List<PagoProfesionalObraResponseDTO> pagosCreados = new ArrayList<>();
        BigDecimal montoTotal = BigDecimal.ZERO;
        
        // Validar que no esté vacío
        if (request.getImportesPorAsignacion().isEmpty()) {
            throw new RuntimeException("No hay importes para procesar");
        }
        
        // Procesar cada asignación
        for (var entry : request.getImportesPorAsignacion().entrySet()) {
            Long asignacionId = entry.getKey();
            BigDecimal importe = entry.getValue();
            
            // Validar importe positivo
            if (importe == null || importe.compareTo(BigDecimal.ZERO) <= 0) {
                log.warn("⚠️ Importe inválido para asignación {}: {}", asignacionId, importe);
                continue;
            }
            
            try {
                AsignacionProfesionalObra asignacion;
                
                // 🔍 Detectar asignaciones virtuales (IDs negativos de jornales diarios)
                if (asignacionId < 0) {
                    log.info("🔄 Asignación virtual detectada (ID negativo): {}", asignacionId);
                    
                    // Decodificar el ID virtual: -1 * (profesionalId * 100000 + obraId)
                    long idPositivo = Math.abs(asignacionId);
                    long profesionalId = idPositivo / 100000;
                    long obraId = idPositivo % 100000;
                    
                    log.info("📍 Decodificado - ProfesionalId: {}, ObraId: {}", profesionalId, obraId);
                    
                    // Buscar o crear asignación real para este profesional/obra
                    asignacion = asignacionRepository
                        .findByProfesionalIdAndObraIdAndEmpresaId(profesionalId, obraId, request.getEmpresaId())
                        .stream()
                        .findFirst()
                        .orElseGet(() -> {
                            log.info("➕ Creando nueva asignación real para profesional {} en obra {}", profesionalId, obraId);
                            
                            AsignacionProfesionalObra nueva = new AsignacionProfesionalObra();
                            nueva.setEmpresaId(request.getEmpresaId());
                            nueva.setProfesionalId(profesionalId);
                            nueva.setObraId(obraId);
                            nueva.setEstado("ACTIVO");
                            nueva.setFechaInicio(LocalDate.now());
                            nueva.setModalidad("JORNAL_DIARIO");
                            nueva.setTipoAsignacion("JORNAL"); // Campo obligatorio
                            
                            // Cargar entidades relacionadas
                            var profesional = profesionalRepository.findById(profesionalId)
                                .orElseThrow(() -> new RuntimeException("Profesional no encontrado: " + profesionalId));
                            var obra = obraRepository.findById(obraId)
                                .orElseThrow(() -> new RuntimeException("Obra no encontrada: " + obraId));
                            
                            nueva.setProfesional(profesional);
                            nueva.setObra(obra);
                            
                            // Buscar el rubro desde los jornales diarios
                            var jornales = jornalDiarioRepository.findByProfesionalIdAndObraIdOrderByFechaDesc(
                                profesionalId, obraId);
                            
                            if (!jornales.isEmpty() && jornales.get(0).getRubroId() != null) {
                                Long rubroId = jornales.get(0).getRubroId();
                                nueva.setRubroId(rubroId);
                                
                                // Obtener el nombre del rubro
                                honorarioRubroRepository.findById(rubroId).ifPresent(rubro -> {
                                    nueva.setRubroNombre(rubro.getNombreRubro());
                                    log.info("✅ Rubro asignado: {} (ID: {})", rubro.getNombreRubro(), rubroId);
                                });
                            } else {
                                // Si no hay jornales, usar valores por defecto
                                nueva.setRubroId(0L);
                                nueva.setRubroNombre("Sin rubro");
                                log.warn("⚠️ No se encontraron jornales para profesional {} en obra {}", profesionalId, obraId);
                            }
                            
                            return asignacionRepository.save(nueva);
                        });
                    
                    log.info("✅ Asignación real obtenida/creada con ID: {}", asignacion.getId());
                } else {
                    // Buscar asignación tradicional
                    asignacion = asignacionRepository.findById(asignacionId)
                        .orElseThrow(() -> new RuntimeException("Asignación no encontrada: " + asignacionId));
                }
                
                log.info("💰 Procesando pago: Asig={}, Prof={}, Obra={}, Rubro={}, Monto=${}",
                    asignacionId,
                    asignacion.getProfesionalId(),
                    asignacion.getObraId(),
                    asignacion.getRubroId(),
                    importe
                );
                
                // Buscar o crear ProfesionalObra
                ProfesionalObra profesionalObra = profesionalObraRepository
                    .findByProfesionalIdAndObraId(asignacion.getProfesionalId(), asignacion.getObraId())
                    .orElseGet(() -> {
                        ProfesionalObra nuevo = new ProfesionalObra();
                        nuevo.setProfesional(asignacion.getProfesional());
                        nuevo.setObra(asignacion.getObra());
                        nuevo.setEmpresaId(request.getEmpresaId());
                        nuevo.setFechaDesde(LocalDate.now());
                        nuevo.setEstado("ACTIVO");
                        return profesionalObraRepository.save(nuevo);
                    });
                
                // Crear el pago
                PagoProfesionalObra pago = new PagoProfesionalObra();
                pago.setEmpresaId(request.getEmpresaId());
                pago.setProfesionalObra(profesionalObra);
                pago.setAsignacion(asignacion);
                pago.setTipoPago(request.getTipoPago() != null ? request.getTipoPago() : "PAGO_PARCIAL");
                
                // 📅 Usar fecha personalizada si existe, sino usar fecha actual
                LocalDate fechaPago = LocalDate.now();
                if (request.getFechasPorAsignacion() != null && request.getFechasPorAsignacion().containsKey(asignacionId)) {
                    fechaPago = request.getFechasPorAsignacion().get(asignacionId);
                    log.info("📅 Usando fecha personalizada para asignación {}: {}", asignacionId, fechaPago);
                }
                pago.setFechaPago(fechaPago);
                
                pago.setMontoBase(importe);
                pago.setMontoBruto(importe);
                pago.setMontoNeto(importe);
                pago.setDescuentoAdelantos(BigDecimal.ZERO);
                pago.setDescuentoPresentismo(BigDecimal.ZERO);
                pago.setAjustes(BigDecimal.ZERO);
                pago.setEstado(PagoProfesionalObra.ESTADO_PAGADO); // ✅ Estado correcto para que se cuente en totalPagado
                pago.setMetodoPago("EFECTIVO");
                
                String obs = String.format("Pago parcial - Rubro: %s | ", 
                    asignacion.getRubroNombre() != null ? asignacion.getRubroNombre() : "N/A");
                if (request.getObservaciones() != null) {
                    obs += request.getObservaciones();
                }
                pago.setObservaciones(obs);
                
                PagoProfesionalObra pagoGuardado = pagoRepository.save(pago);
                log.info("✅ Pago creado: ID={}, Monto=${}", pagoGuardado.getId(), importe);
                
                pagosCreados.add(mapearEntityAResponse(pagoGuardado));
                montoTotal = montoTotal.add(importe);
                
            } catch (Exception e) {
                log.error("❌ Error al crear pago para asignación {}: {}", asignacionId, e.getMessage());
            }
        }
        
        com.rodrigo.construccion.dto.response.PagoProfesionalBatchResponseDTO response = 
            new com.rodrigo.construccion.dto.response.PagoProfesionalBatchResponseDTO();
        response.setTotalPagosCreados(pagosCreados.size());
        response.setMontoTotalPagado(montoTotal);
        response.setPagosCreados(pagosCreados);
        response.setMensaje(String.format("Se crearon %d pagos por un total de $%s",
            pagosCreados.size(), montoTotal));
        
        log.info("📦 ✅ Batch completado: {} pagos creados, total: ${}", 
            pagosCreados.size(), montoTotal);
        
        return response;
    }
}
