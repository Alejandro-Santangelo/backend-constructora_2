package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CostoRequestDTO;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.model.entity.Costo;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ICostoService {
    public CostoResponseDTO crearCostoDesdeDTO(CostoRequestDTO dto, Long empresaId);

    public PaginacionCostosResponse listarCostosConPaginacion(Long empresaId, Pageable pageable);

    public Optional<Costo> obtenerPorIdYEmpresa(Long id, Long empresaId);

    public CostoResponseDTO actualizarCosto(Long id, CostoRequestDTO costoActualizado, Long empresaId);

    public void eliminarCosto(Long id, Long empresaId);

    public PaginacionCostosResponse obtenerCostosPorObraConPaginacion(Long obraId, Long empresaId, Pageable pageable);

    public CostoResponseDTO aprobarCosto(Long id, String comentarios, Long empresaId);

    public CostoResponseDTO rechazarCosto(Long id, String motivoRechazo, Long empresaId);

    public TotalCostosObraResponseDTO calcularTotalCostosObra(Long obraId, Long empresaId);

    public PaginacionCostosResponse obtenerCostosPorCategoriaConPaginacion(String categoria, Long empresaId, Pageable pageable);

    public PaginacionCostosResponse listarCostosPorTipoConPaginacion(Long empresaId, String tipo, Pageable pageRequest);

    public PaginacionCostosResponse obtenerCostosPorPeriodoConPaginacion(LocalDate fechaDesde, LocalDate fechaHasta, Long empresaId, Pageable pageable);

    public PaginacionCostosResponse obtenerCostosDelMesConPaginacion(Long empresaId, Pageable pageable);

    public PaginacionCostosResponse buscarCostosConPaginacion(Long empresaId, String texto, Pageable pageable);

    public PaginacionCostosResponse filtrarCostosConPaginacion(Long empresaId, Long obraId, String categoria, String tipo,
                                                               Double montoMinimo, Double montoMaximo, Pageable pageable);

    public AnalisisCostosObraResponseDTO obtenerAnalisisCostosObra(Long obraId, Long empresaId);

    public PresupuestoVsRealResponseDTO compararPresupuestoVsReal(Long obraId, Long empresaId);

    public RentabilidadObraResponseDTO calcularRentabilidad(Long obraId, Long empresaId);

    public EstadisticasCostosResponseDTO obtenerEstadisticas(Long empresaId);

    public List<EstadisticasPorCategoriaDTO> obtenerEstadisticasPorCategoria(Long empresaId);

    public List<EstadisticasPorObraDTO> obtenerEstadisticasPorObra(Long empresaId);

    public List<EstadisticasMensualesDTO> obtenerEstadisticasMensuales(Long empresaId);

    public List<ReporteResumenObraDTO> obtenerReporteResumenObras(Long empresaId);

    public List<ReporteVariacionesPresupuestoDTO> obtenerReporteVariacionesPresupuesto(Long empresaId);

    public List<TopCostosDTO> obtenerTopCostos(Long empresaId, int limite);

    public List<String> listarCategorias(Long empresaId);

    public List<String> listarTipos(Long empresaId);

    public long contarCostosPorEmpresa(Long empresaId);
}
