package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.CostoMapper;
import com.rodrigo.construccion.dto.request.CostoRequestDTO;
import com.rodrigo.construccion.dto.response.AnalisisCostosObraResponseDTO;
import com.rodrigo.construccion.dto.response.CostoResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasCostosResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasMensualesDTO;
import com.rodrigo.construccion.dto.response.EstadisticasPorCategoriaDTO;
import com.rodrigo.construccion.dto.response.EstadisticasPorObraDTO;
import com.rodrigo.construccion.dto.response.PaginacionCostosResponse;
import com.rodrigo.construccion.dto.response.PresupuestoVsRealResponseDTO;
import com.rodrigo.construccion.dto.response.RentabilidadObraResponseDTO;
import com.rodrigo.construccion.dto.response.ReporteResumenObraDTO;
import com.rodrigo.construccion.dto.response.ReporteVariacionesPresupuestoDTO;
import com.rodrigo.construccion.dto.response.TopCostosDTO;
import com.rodrigo.construccion.dto.response.TotalCostosObraResponseDTO;
import com.rodrigo.construccion.exception.ApprovedCostoException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Costo;
import com.rodrigo.construccion.repository.CostoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class CostoService implements ICostoService{

    private final CostoRepository costoRepository;
    private final IEmpresaService empresaService;
    private final IObraService obraService;

    /* Crear un nuevo costo a partir de un DTO */
    public CostoResponseDTO crearCostoDesdeDTO(CostoRequestDTO dto, Long empresaId) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO de costo no puede ser nulo");
        }
        if (dto.getIdObra() == null) {
            throw new IllegalArgumentException("El idObra es obligatorio");
        }
        // Validar que la empresa existe
        empresaService.findEmpresaById(empresaId);

        // Validar que la obra existe y pertenece a la empresa
        var obra = obraService.encontrarObraPorIdYEmpresa(dto.getIdObra(), empresaId);

        // Usar mapper para convertir DTO a entidad
        Costo costo = CostoMapper.toEntity(dto, obra);

        // Guardar y retornar como ResponseDTO
        Costo costoGuardado = costoRepository.save(costo);
        return CostoMapper.toResponseDTO(costoGuardado);
    }

    /* Listar costos por empresa con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse listarCostosConPaginacion(Long empresaId, Pageable pageable) {
        Page<Costo> costosPage = costoRepository.findByObra_Cliente_Empresa_Id(empresaId, pageable);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Obtener costo por ID y empresa */
    @Transactional(readOnly = true)
    public Optional<Costo> obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return costoRepository.findByIdAndObra_Cliente_Empresa_Id(id, empresaId);
    }

    /* Actualizar costo */
    public CostoResponseDTO actualizarCosto(Long id, CostoRequestDTO costoActualizado, Long empresaId) {
        var costoExistente = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Costo no encontrado o no pertenece a la empresa"));

        // No permitir modificar costos aprobados
        if ("Aprobado".equals(costoExistente.getEstado())) {
            throw new ApprovedCostoException("No se puede modificar un costo aprobado");
        }

        // Actualizar campos permitidos
        if (costoActualizado.getConcepto() != null) {
            costoExistente.setConcepto(costoActualizado.getConcepto());
        }
        if (costoActualizado.getMonto() != null && costoActualizado.getMonto().compareTo(BigDecimal.ZERO) > 0) {
            costoExistente.setMonto(costoActualizado.getMonto());
        }
        if (costoActualizado.getFecha() != null) {
            costoExistente.setFecha(costoActualizado.getFecha());
        }
        if (costoActualizado.getCategoria() != null) {
            costoExistente.setCategoria(costoActualizado.getCategoria());
        }
        if (costoActualizado.getTipoCosto() != null) {
            costoExistente.setTipoCosto(costoActualizado.getTipoCosto());
        }
        if (costoActualizado.getDescripcion() != null) {
            costoExistente.setDescripcion(costoActualizado.getDescripcion());
        }

        var costoGuardado = costoRepository.save(costoExistente);

        return CostoMapper.toResponseDTO(costoGuardado);
    }

    /* Eliminar costo */
    public void eliminarCosto(Long id, Long empresaId) {

        var costo = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Costo no encontrado o no pertenece a la empresa"));

        if ("Aprobado".equals(costo.getEstado())) {
            throw new ApprovedCostoException("No se puede eliminar un costo aprobado");
        }

        costoRepository.deleteById(id);
    }

    /* Obtener costos por obra con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse obtenerCostosPorObraConPaginacion(Long obraId, Long empresaId, Pageable pageable) {
        Page<Costo> costosPage = costoRepository.findByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId, pageable);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Aprobar costo */
    public CostoResponseDTO aprobarCosto(Long id, String comentarios, Long empresaId) {
        var costo = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Costo no encontrado"));

        if ("Aprobado".equals(costo.getEstado())) {
            throw new ApprovedCostoException("El costo ya está aprobado");
        }

        costo.setEstado("Aprobado");
        costo.setFechaAprobacion(LocalDate.now());

        if (comentarios != null && !comentarios.trim().isEmpty()) {
            costo.setDescripcion(
                    (costo.getDescripcion() != null ? costo.getDescripcion() + "\n" : "") +
                            "APROBADO: " + comentarios
            );
        }

        Costo costoGuardado = costoRepository.save(costo);
        return CostoMapper.toResponseDTO(costoGuardado);
    }

    /* Rechazar costo */
    public CostoResponseDTO rechazarCosto(Long id, String motivoRechazo, Long empresaId) {
        var costo = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Costo no encontrado"));

        costo.setEstado("Rechazado");

        if (motivoRechazo != null && !motivoRechazo.trim().isEmpty()) {
            costo.setMotivoRechazo(motivoRechazo);
        }

        Costo costoGuardado = costoRepository.save(costo);
        return CostoMapper.toResponseDTO(costoGuardado);
    }


    /* MÉTODOS QUE NO ESTÁN SIENDO USADOS EN EL FRONTEND */

    /* Calcular total de costos por obra */
    @Transactional(readOnly = true)
    public TotalCostosObraResponseDTO calcularTotalCostosObra(Long obraId, Long empresaId) {
        var totalCostos = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var cantidadCostos = costoRepository.countByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);

        return CostoMapper.toTotalCostosObraResponse(obraId, totalCostos, cantidadCostos);
    }

    /* Obtener costos por categoría con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse obtenerCostosPorCategoriaConPaginacion(String categoria, Long empresaId, Pageable pageable) {
        // Normaliza la categoría: quita acentos, pasa a minúsculas y recorta espacios
        String categoriaNormalizada = Normalizer.normalize(categoria, Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase().trim();

        Page<Costo> costosPage = costoRepository.buscarPorCategoriaFlexible(empresaId, categoriaNormalizada, pageable);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Listar costos por tipo con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse listarCostosPorTipoConPaginacion(Long empresaId, String tipo, Pageable pageRequest) {
        // Normaliza el tipo: quita acentos, pasa a minúsculas y recorta espacios
        String tipoNormalizado = java.text.Normalizer.normalize(tipo, java.text.Normalizer.Form.NFD)
                .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                .toLowerCase().trim();

        Page<Costo> costosPage = costoRepository.buscarPorTipoFlexible(empresaId, tipoNormalizado, pageRequest);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Obtener costos por período con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse obtenerCostosPorPeriodoConPaginacion(LocalDate fechaDesde, LocalDate fechaHasta, Long empresaId, Pageable pageable) {
        Page<Costo> costosPage = costoRepository.findByObra_Cliente_Empresa_IdAndFechaBetween(empresaId, fechaDesde, fechaHasta, pageable);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Obtener costos del mes actual con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse obtenerCostosDelMesConPaginacion(Long empresaId, Pageable pageable) {
        var inicioMes = LocalDate.now().withDayOfMonth(1);
        var finMes = inicioMes.plusMonths(1).minusDays(1);
        return obtenerCostosPorPeriodoConPaginacion(inicioMes, finMes, empresaId, pageable);
    }

    /* Buscar costos por texto con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse buscarCostosConPaginacion(Long empresaId, String texto, Pageable pageable) {
        Page<Costo> costosPage = costoRepository.buscarPorTexto(empresaId, texto, pageable);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Filtrar costos por múltiples criterios con respuesta de paginación completa */
    @Transactional(readOnly = true)
    public PaginacionCostosResponse filtrarCostosConPaginacion(Long empresaId, Long obraId, String categoria, String tipo,
                                                               Double montoMinimo, Double montoMaximo, Pageable pageable) {
        Page<Costo> costosPage = costoRepository.filtrarCostos(empresaId, obraId, categoria, tipo,
                montoMinimo != null ? BigDecimal.valueOf(montoMinimo) : null,
                montoMaximo != null ? BigDecimal.valueOf(montoMaximo) : null,
                pageable);
        return CostoMapper.toPaginacionResponse(costosPage);
    }

    /* Obtener análisis de costos por obra */
    @Transactional(readOnly = true)
    public AnalisisCostosObraResponseDTO obtenerAnalisisCostosObra(Long obraId, Long empresaId) {
        var totalCostos = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var costosAprobados = costoRepository.sumMontoByObra_IdAndEstadoAndObra_Cliente_Empresa_Id(obraId, "Aprobado", empresaId);
        var costosPendientes = costoRepository.sumMontoByObra_IdAndEstadoAndObra_Cliente_Empresa_Id(obraId, "Pendiente", empresaId);
        var distribucionCategorias = costoRepository.getDistribucionCostosCategoria(obraId, empresaId);

        return CostoMapper.toAnalisisCostosObraResponse(obraId, totalCostos, costosAprobados, costosPendientes, distribucionCategorias);
    }

    /* Comparar presupuesto vs real */
    @Transactional(readOnly = true)
    public PresupuestoVsRealResponseDTO compararPresupuestoVsReal(Long obraId, Long empresaId) {
        var costosReales = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var presupuestoEstimado = BigDecimal.ZERO; // Obtener del presupuesto real

        return CostoMapper.toPresupuestoVsRealResponse(obraId, presupuestoEstimado, costosReales);
    }

    /* Calcular rentabilidad */
    @Transactional(readOnly = true)
    public RentabilidadObraResponseDTO calcularRentabilidad(Long obraId, Long empresaId) {
        var totalCostos = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var totalIngresos = BigDecimal.ZERO; // Obtener de facturas/contratos

        return CostoMapper.toRentabilidadObraResponse(obraId, totalIngresos, totalCostos);
    }

    /* Obtener estadísticas generales */
    @Transactional(readOnly = true)
    public EstadisticasCostosResponseDTO obtenerEstadisticas(Long empresaId) {
        var totalCostos = costoRepository.countByObra_Cliente_Empresa_Id(empresaId);
        var costosAprobados = costoRepository.countByObra_Cliente_Empresa_IdAndEstado(empresaId, "Aprobado");
        var costosPendientes = costoRepository.countByObra_Cliente_Empresa_IdAndEstado(empresaId, "Pendiente");
        var costosRechazados = costoRepository.countByObra_Cliente_Empresa_IdAndEstado(empresaId, "Rechazado");
        var montoTotal = costoRepository.sumMontoByObra_Cliente_Empresa_Id(empresaId);
        var montoAprobado = costoRepository.sumMontoByObra_Cliente_Empresa_IdAndEstado(empresaId, "Aprobado");

        return CostoMapper.toEstadisticasCostosResponse(totalCostos, costosAprobados, costosPendientes,
                                                         costosRechazados, montoTotal, montoAprobado);
    }

    /* Obtener estadísticas por categoría */
    @Transactional(readOnly = true)
    public List<EstadisticasPorCategoriaDTO> obtenerEstadisticasPorCategoria(Long empresaId) {
        var resultados = costoRepository.getEstadisticasPorCategoria(empresaId);
        return CostoMapper.toEstadisticasPorCategoriaList(resultados);
    }

    /* Obtener estadísticas por obra */
    @Transactional(readOnly = true)
    public List<EstadisticasPorObraDTO> obtenerEstadisticasPorObra(Long empresaId) {
        var resultados = costoRepository.getEstadisticasPorObra(empresaId);
        return CostoMapper.toEstadisticasPorObraList(resultados);
    }

    /* Obtener estadísticas mensuales */
    @Transactional(readOnly = true)
    public List<EstadisticasMensualesDTO> obtenerEstadisticasMensuales(Long empresaId) {
        var resultados = costoRepository.getEstadisticasMensuales(empresaId);
        return CostoMapper.toEstadisticasMensualesList(resultados);
    }

    /* Obtener reporte resumen de obras*/
    @Transactional(readOnly = true)
    public List<ReporteResumenObraDTO> obtenerReporteResumenObras(Long empresaId) {
        var resultados = costoRepository.getReporteResumenObras(empresaId);
        return CostoMapper.toReporteResumenObraList(resultados);
    }

    /* Obtener reporte de variaciones presupuestarias */
    @Transactional(readOnly = true)
    public List<ReporteVariacionesPresupuestoDTO> obtenerReporteVariacionesPresupuesto(Long empresaId) {
        var resultados = costoRepository.getReporteVariacionesPresupuesto(empresaId);
        return CostoMapper.toReporteVariacionesPresupuestoList(resultados);
    }

    /* Obtener top costos */
    @Transactional(readOnly = true)
    public List<TopCostosDTO> obtenerTopCostos(Long empresaId, int limite) {
        var resultados = costoRepository.getTopCostos(empresaId, limite);
        return CostoMapper.toTopCostosList(resultados);
    }

    /* Listar categorías disponibles */
    @Transactional(readOnly = true)
    public List<String> listarCategorias(Long empresaId) {
        return costoRepository.getCategorias(empresaId);
    }

    /* Listar tipos disponibles */
    @Transactional(readOnly = true)
    public List<String> listarTipos(Long empresaId) {
        return costoRepository.getTipos(empresaId);
    }

    /* Contar costos por empresa */
    @Transactional(readOnly = true)
    public long contarCostosPorEmpresa(Long empresaId) {
        return costoRepository.countByObra_Cliente_Empresa_Id(empresaId);
    }
}