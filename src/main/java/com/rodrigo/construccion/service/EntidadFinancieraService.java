package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CobroEntidadRequestDTO;
import com.rodrigo.construccion.dto.request.EstadisticasMultiplesRequestDTO;
import com.rodrigo.construccion.dto.request.SincronizarEntidadFinancieraRequestDTO;
import com.rodrigo.construccion.dto.response.CobroEntidadResponseDTO;
import com.rodrigo.construccion.dto.response.EntidadFinancieraResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasEntidadResponseDTO;
import com.rodrigo.construccion.enums.TipoEntidadFinanciera;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.CobroEntidad;
import com.rodrigo.construccion.model.entity.EntidadFinanciera;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.TrabajoAdicional;
import com.rodrigo.construccion.model.entity.TrabajoExtra;
import com.rodrigo.construccion.repository.CobroEntidadRepository;
import com.rodrigo.construccion.repository.CobroObraRepository;
import com.rodrigo.construccion.repository.EntidadFinancieraRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PagoTrabajoExtraObraRepository;
import com.rodrigo.construccion.repository.TrabajoAdicionalRepository;
import com.rodrigo.construccion.repository.TrabajoExtraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio principal del sistema unificado de entidades financieras.
 *
 * <h2>Responsabilidades</h2>
 * <ol>
 *   <li>Sincronizar entidades originales (Obra, TrabajoExtra, TrabajoAdicional)
 *       en la tabla {@code entidades_financieras}.</li>
 *   <li>Registrar y consultar cobros via {@code cobros_entidad}.</li>
 *   <li>Calcular estadísticas unificadas (totalCobrado, totalGastos, saldo) para
 *       listas mixtas de entidades de distintos tipos.</li>
 * </ol>
 *
 * <h2>Estrategia por tipo de entidad</h2>
 * <ul>
 *   <li><b>OBRA_PRINCIPAL / OBRA_INDEPENDIENTE</b>: cobros en {@code cobros_entidad}
 *       (nuevo sistema); gastos consultados en {@code CobroObraRepository}.</li>
 *   <li><b>TRABAJO_EXTRA</b>: cobros en {@code cobros_entidad};
 *       referencia de gasto = importe del trabajo extra.</li>
 *   <li><b>TRABAJO_ADICIONAL</b>: cobros en {@code cobros_entidad};
 *       referencia de gasto = importe del trabajo adicional.</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EntidadFinancieraService {

    private final EntidadFinancieraRepository entidadFinancieraRepository;
    private final CobroEntidadRepository cobroEntidadRepository;

    // Repositorios de entidades originales (para enriquecer estadísticas)
    private final ObraRepository obraRepository;
    private final TrabajoExtraRepository trabajoExtraRepository;
    private final TrabajoAdicionalRepository trabajoAdicionalRepository;
    private final CobroObraRepository cobroObraRepository;
    private final PagoTrabajoExtraObraRepository pagoTrabajoExtraObraRepository;

    // =========================================================================
    // SINCRONIZACIÓN
    // =========================================================================

    /**
     * Crea o actualiza el registro en {@code entidades_financieras} para
     * una entidad de cualquier tipo. Idempotente (upsert).
     *
     * @param request datos de la entidad a sincronizar
     * @return el registro creado o actualizado
     */
    @Transactional
    public EntidadFinancieraResponseDTO sincronizar(SincronizarEntidadFinancieraRequestDTO request) {
        log.info("Sincronizando entidad financiera: tipo={}, entidadId={}, empresa={}",
                request.getTipoEntidad(), request.getEntidadId(), request.getEmpresaId());

        Optional<EntidadFinanciera> existente = entidadFinancieraRepository
                .findByEmpresaIdAndTipoEntidadAndEntidadId(
                        request.getEmpresaId(), request.getTipoEntidad(), request.getEntidadId());

        EntidadFinanciera entidad = existente.orElseGet(() -> EntidadFinanciera.builder()
                .empresaId(request.getEmpresaId())
                .tipoEntidad(request.getTipoEntidad())
                .entidadId(request.getEntidadId())
                .build());

        // Actualizar campos que pueden cambiar
        entidad.setPresupuestoNoClienteId(request.getPresupuestoNoClienteId());
        entidad.setNombreDisplay(request.getNombreDisplay());
        entidad.setActivo(true);

        EntidadFinanciera guardada = entidadFinancieraRepository.save(entidad);
        log.info("Entidad financiera {} con ID={}", existente.isPresent() ? "actualizada" : "creada", guardada.getId());

        return mapearEntidad(guardada);
    }

    /**
     * Sincroniza automáticamente desde una {@link Obra}.
     * Si la obra tiene presupuesto → OBRA_PRINCIPAL, si no → OBRA_INDEPENDIENTE.
     *
     * @param obra la obra creada o actualizada
     */
    @Transactional
    public void sincronizarDesdeObra(Obra obra) {
        try {
            TipoEntidadFinanciera tipo = (obra.getPresupuestoNoClienteId() != null)
                    ? TipoEntidadFinanciera.OBRA_PRINCIPAL
                    : TipoEntidadFinanciera.OBRA_INDEPENDIENTE;

            SincronizarEntidadFinancieraRequestDTO req = new SincronizarEntidadFinancieraRequestDTO();
            req.setEmpresaId(obra.getEmpresaId());
            req.setTipoEntidad(tipo);
            req.setEntidadId(obra.getId());
            req.setPresupuestoNoClienteId(obra.getPresupuestoNoClienteId());
            req.setNombreDisplay(obra.getNombre());

            sincronizar(req);
        } catch (Exception e) {
            log.error("Error sincronizando entidad financiera desde obra {}: {}", obra.getId(), e.getMessage(), e);
            // No propagamos el error para no romper el flujo principal
        }
    }

    /**
     * Sincroniza automáticamente desde un {@link TrabajoExtra}.
     *
     * @param trabajoExtra el trabajo extra creado o actualizado
     */
    @Transactional
    public void sincronizarDesdeTrabajoExtra(TrabajoExtra trabajoExtra) {
        try {
            SincronizarEntidadFinancieraRequestDTO req = new SincronizarEntidadFinancieraRequestDTO();
            req.setEmpresaId(trabajoExtra.getEmpresaId());
            req.setTipoEntidad(TipoEntidadFinanciera.TRABAJO_EXTRA);
            req.setEntidadId(trabajoExtra.getId());
            req.setPresupuestoNoClienteId(null); // TrabajoExtra no tiene presupuesto propio
            req.setNombreDisplay(trabajoExtra.getNombre());

            sincronizar(req);
        } catch (Exception e) {
            log.error("Error sincronizando entidad financiera desde trabajo extra {}: {}", trabajoExtra.getId(), e.getMessage(), e);
        }
    }

    /**
     * Sincroniza automáticamente desde un {@link TrabajoAdicional}.
     *
     * @param trabajoAdicional el trabajo adicional creado o actualizado
     */
    @Transactional
    public EntidadFinanciera sincronizarDesdeTrabajoAdicional(TrabajoAdicional trabajoAdicional) {
        try {
            SincronizarEntidadFinancieraRequestDTO req = new SincronizarEntidadFinancieraRequestDTO();
            req.setEmpresaId(trabajoAdicional.getEmpresaId());
            req.setTipoEntidad(TipoEntidadFinanciera.TRABAJO_ADICIONAL);
            req.setEntidadId(trabajoAdicional.getId());
            req.setPresupuestoNoClienteId(null);
            req.setNombreDisplay(trabajoAdicional.getNombre());

            sincronizar(req);

            return entidadFinancieraRepository
                    .findByEmpresaIdAndTipoEntidadAndEntidadId(
                            trabajoAdicional.getEmpresaId(),
                            TipoEntidadFinanciera.TRABAJO_ADICIONAL,
                            trabajoAdicional.getId())
                    .orElse(null);
        } catch (Exception e) {
            log.error("Error sincronizando entidad financiera desde trabajo adicional {}: {}", trabajoAdicional.getId(), e.getMessage(), e);
            return null;
        }
    }

    // =========================================================================
    // COBROS
    // =========================================================================

    /**
     * Registra un cobro contra una entidad financiera.
     *
     * Validaciones:
     * - La entidad financiera debe existir y pertenecer a la empresa.
     * - El monto debe ser positivo.
     */
    @Transactional
    public CobroEntidadResponseDTO registrarCobro(CobroEntidadRequestDTO request) {
        log.info("Registrando cobro: entidadFinancieraId={}, monto={}, empresa={}",
                request.getEntidadFinancieraId(), request.getMonto(), request.getEmpresaId());

        EntidadFinanciera entidad = entidadFinancieraRepository
                .findByIdAndEmpresaId(request.getEntidadFinancieraId(), request.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "EntidadFinanciera", request.getEntidadFinancieraId()));

        if (!Boolean.TRUE.equals(entidad.getActivo())) {
            throw new IllegalStateException("No se puede registrar cobros en una entidad inactiva");
        }

        if (request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto debe ser mayor a cero");
        }

        CobroEntidad cobro = CobroEntidad.builder()
                .entidadFinanciera(entidad)
                .empresaId(request.getEmpresaId())
                .monto(request.getMonto())
                .fechaCobro(request.getFechaCobro())
                .metodoPago(request.getMetodoPago())
                .referencia(request.getReferencia())
                .notas(request.getNotas())
                .creadoPor(request.getCreadoPor())
                .build();

        CobroEntidad cobradoGuardado = cobroEntidadRepository.save(cobro);
        log.info("Cobro registrado con ID={}", cobradoGuardado.getId());

        return mapearCobro(cobradoGuardado);
    }

    /**
     * Lista los cobros de una entidad financiera, ordenados por fecha desc.
     */
    @Transactional(readOnly = true)
    public List<CobroEntidadResponseDTO> obtenerCobros(Long entidadFinancieraId, Long empresaId) {
        // Validar que la entidad pertenece a la empresa
        entidadFinancieraRepository.findByIdAndEmpresaId(entidadFinancieraId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("EntidadFinanciera", entidadFinancieraId));

        return cobroEntidadRepository
                .findByEntidadFinanciera_IdAndEmpresaIdOrderByFechaCobroDesc(entidadFinancieraId, empresaId)
                .stream()
                .map(this::mapearCobro)
                .collect(Collectors.toList());
    }

    /**
     * Retorna la suma total cobrado de una entidad financiera.
     */
    @Transactional(readOnly = true)
    public BigDecimal obtenerTotalCobrado(Long entidadFinancieraId, Long empresaId) {
        entidadFinancieraRepository.findByIdAndEmpresaId(entidadFinancieraId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("EntidadFinanciera", entidadFinancieraId));

        return cobroEntidadRepository.sumMontoByEntidadFinancieraId(entidadFinancieraId, empresaId);
    }

    // =========================================================================
    // ESTADÍSTICAS MÚLTIPLES
    // =========================================================================

    /**
     * Calcula estadísticas financieras para una lista de entidades financieras
     * de distintos tipos.
     *
     * <ul>
     *   <li>{@code totalCobrado}: suma de cobros_entidad para esa entidad.</li>
     *   <li>{@code totalGastos}: calculado según el tipo de entidad.</li>
     *   <li>{@code saldo}: totalCobrado - totalGastos.</li>
     * </ul>
     *
     * Entidades no encontradas o que no pertenecen a la empresa son ignoradas
     * silenciosamente para no romper listas mixtas.
     */
    @Transactional(readOnly = true)
    public List<EstadisticasEntidadResponseDTO> calcularEstadisticasMultiples(
            EstadisticasMultiplesRequestDTO request) {

        log.info("Calculando estadísticas para {} entidades, empresa={}",
                request.getEntidadesFinancierasIds().size(), request.getEmpresaId());

        List<Long> ids = request.getEntidadesFinancierasIds();
        Long empresaId = request.getEmpresaId();

        // Obtener solo las entidades que pertenecen a la empresa
        List<EntidadFinanciera> entidades = entidadFinancieraRepository
                .findByIdsAndEmpresaId(ids, empresaId);

        if (entidades.isEmpty()) {
            return Collections.emptyList();
        }

        // Pre-cargar totales cobrados en un solo query (evita N+1)
        List<Long> entidadesIds = entidades.stream()
                .map(EntidadFinanciera::getId).collect(Collectors.toList());

        Map<Long, BigDecimal> totalCobradoMap = construirMapaTotalCobrado(entidadesIds, empresaId);

        List<EstadisticasEntidadResponseDTO> resultado = new ArrayList<>();

        for (EntidadFinanciera ef : entidades) {
            try {
                BigDecimal totalCobrado = totalCobradoMap.getOrDefault(ef.getId(), BigDecimal.ZERO);
                BigDecimal totalGastos = calcularGastosPorTipo(ef);
                BigDecimal saldo = totalCobrado.subtract(totalGastos);

                resultado.add(EstadisticasEntidadResponseDTO.builder()
                        .entidadFinancieraId(ef.getId())
                        .tipoEntidad(ef.getTipoEntidad())
                        .entidadId(ef.getEntidadId())
                        .nombreDisplay(ef.getNombreDisplay())
                        .totalCobrado(totalCobrado)
                        .totalGastos(totalGastos)
                        .saldo(saldo)
                        .build());

            } catch (Exception e) {
                log.warn("Error calculando estadísticas para entidad {}: {}", ef.getId(), e.getMessage());
                // Incluir la entidad con ceros para no romper la lista
                resultado.add(EstadisticasEntidadResponseDTO.builder()
                        .entidadFinancieraId(ef.getId())
                        .tipoEntidad(ef.getTipoEntidad())
                        .entidadId(ef.getEntidadId())
                        .nombreDisplay(ef.getNombreDisplay())
                        .totalCobrado(BigDecimal.ZERO)
                        .totalGastos(BigDecimal.ZERO)
                        .saldo(BigDecimal.ZERO)
                        .build());
            }
        }

        return resultado;
    }

    // =========================================================================
    // MIGRACIÓN MASIVA
    // =========================================================================

    /**
     * Migra todas las obras, trabajos extra y trabajos adicionales existentes
     * a la tabla {@code entidades_financieras}. Idempotente: entidades ya
     * registradas se actualizan, no se duplican.
     *
     * @param empresaId empresa a migrar (null = migrar todas las empresas)
     * @return número de registros procesados
     */
    @Transactional
    public int migrarEntidadesExistentes(Long empresaId) {
        int total = 0;

        log.info("Iniciando migración de entidades financieras existentes{}",
                empresaId != null ? " para empresa " + empresaId : " (todas las empresas)");

        // Migrar Obras (principal e independiente)
        List<Obra> obras = empresaId != null
                ? obraRepository.findByEmpresaId(empresaId)
                : obraRepository.findAll();

        for (Obra obra : obras) {
            if ("ELIMINADA".equals(obra.getEstado())) continue;
            sincronizarDesdeObra(obra);
            total++;
        }

        // Migrar Trabajos Extra
        List<TrabajoExtra> trabajosExtra = empresaId != null
                ? trabajoExtraRepository.findByEmpresaId(empresaId)
                : trabajoExtraRepository.findAll();

        for (TrabajoExtra te : trabajosExtra) {
            sincronizarDesdeTrabajoExtra(te);
            total++;
        }

        // Migrar Trabajos Adicionales
        List<TrabajoAdicional> trabajosAdicionales = empresaId != null
                ? trabajoAdicionalRepository.findByEmpresaId(empresaId)
                : trabajoAdicionalRepository.findAll();

        for (TrabajoAdicional ta : trabajosAdicionales) {
            if ("CANCELADO".equals(ta.getEstado())) continue;
            sincronizarDesdeTrabajoAdicional(ta);
            total++;
        }

        log.info("Migración completada. Total entidades procesadas: {}", total);
        return total;
    }

    // =========================================================================
    // CONSULTAS
    // =========================================================================

    @Transactional(readOnly = true)
    public EntidadFinancieraResponseDTO obtenerPorId(Long id, Long empresaId) {
        EntidadFinanciera ef = entidadFinancieraRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("EntidadFinanciera", id));
        return mapearEntidad(ef);
    }

    @Transactional(readOnly = true)
    public List<EntidadFinancieraResponseDTO> listarPorEmpresa(Long empresaId) {
        return entidadFinancieraRepository.findByEmpresaIdAndActivoTrue(empresaId)
                .stream().map(this::mapearEntidad).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<EntidadFinancieraResponseDTO> buscarPorPresupuesto(Long presupuestoNoClienteId, Long empresaId) {
        return entidadFinancieraRepository
                .findByPresupuestoNoClienteIdAndEmpresaId(presupuestoNoClienteId, empresaId)
                .map(this::mapearEntidad);
    }

    // =========================================================================
    // HELPERS PRIVADOS
    // =========================================================================

    /**
     * Construye un mapa {entidadFinancieraId → totalCobrado} con una sola consulta.
     */
    private Map<Long, BigDecimal> construirMapaTotalCobrado(List<Long> entidadesIds, Long empresaId) {
        Map<Long, BigDecimal> mapa = new HashMap<>();
        List<Object[]> resultados = cobroEntidadRepository
                .sumMontoGroupByEntidadFinancieraId(entidadesIds, empresaId);

        for (Object[] row : resultados) {
            Long efId = ((Number) row[0]).longValue();
            BigDecimal total = row[1] instanceof BigDecimal
                    ? (BigDecimal) row[1]
                    : new BigDecimal(row[1].toString());
            mapa.put(efId, total);
        }

        return mapa;
    }

    /**
     * Calcula el total de gastos de una entidad según su tipo.
     *
     * Estrategia conservadora: para entidades sin sistema de gastos propio
     * se retorna 0 (extensible en el futuro sin romper la API).
     */
    private BigDecimal calcularGastosPorTipo(EntidadFinanciera ef) {
        switch (ef.getTipoEntidad()) {

            case OBRA_PRINCIPAL:
            case OBRA_INDEPENDIENTE: {
                // Para obras: suma de cobros cobrados en el sistema antiguo
                // (representan "lo que se gastó/cobró al cliente")
                // El frontend puede complementar con el endpoint ObraFinancieroService
                return cobroObraRepository.findByObraId(ef.getEntidadId())
                        .stream()
                        .filter(c -> "COBRADO".equals(c.getEstado()))
                        .map(c -> c.getMonto() != null ? c.getMonto() : BigDecimal.ZERO)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
            }

            case TRABAJO_EXTRA: {
                // Para trabajos extra: importe estimado del trabajo
                return trabajoExtraRepository.findById(ef.getEntidadId())
                        .map(te -> te.getTotalPresupuesto() != null ? te.getTotalPresupuesto() : BigDecimal.ZERO)
                        .orElse(BigDecimal.ZERO);
            }

            case TRABAJO_ADICIONAL: {
                // Para trabajos adicionales: importe del trabajo
                return trabajoAdicionalRepository.findById(ef.getEntidadId())
                        .map(ta -> ta.getImporte() != null ? ta.getImporte() : BigDecimal.ZERO)
                        .orElse(BigDecimal.ZERO);
            }

            default:
                return BigDecimal.ZERO;
        }
    }

    // =========================================================================
    // MAPPERS
    // =========================================================================

    private EntidadFinancieraResponseDTO mapearEntidad(EntidadFinanciera e) {
        return EntidadFinancieraResponseDTO.builder()
                .id(e.getId())
                .empresaId(e.getEmpresaId())
                .tipoEntidad(e.getTipoEntidad())
                .entidadId(e.getEntidadId())
                .presupuestoNoClienteId(e.getPresupuestoNoClienteId())
                .nombreDisplay(e.getNombreDisplay())
                .activo(e.getActivo())
                .fechaCreacion(e.getFechaCreacion())
                .fechaActualizacion(e.getFechaActualizacion())
                .build();
    }

    private CobroEntidadResponseDTO mapearCobro(CobroEntidad c) {
        return CobroEntidadResponseDTO.builder()
                .id(c.getId())
                .entidadFinancieraId(c.getEntidadFinanciera().getId())
                .empresaId(c.getEmpresaId())
                .monto(c.getMonto())
                .fechaCobro(c.getFechaCobro())
                .metodoPago(c.getMetodoPago())
                .referencia(c.getReferencia())
                .notas(c.getNotas())
                .creadoPor(c.getCreadoPor())
                .fechaCreacion(c.getFechaCreacion())
                .build();
    }
}
