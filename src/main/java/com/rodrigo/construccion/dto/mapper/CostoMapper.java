package com.rodrigo.construccion.dto.mapper;

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
import com.rodrigo.construccion.model.entity.Costo;
import com.rodrigo.construccion.model.entity.Obra;
import org.springframework.data.domain.Page;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public interface CostoMapper {

    /**
     * Convierte un CostoRequestDTO a una entidad Costo.
     * Aplica valores por defecto cuando los campos del DTO son nulos.
     *
     * @param dto El DTO con los datos del costo
     * @param obra La obra a la que pertenece el costo
     * @return La entidad Costo con los datos del DTO
     */
    static Costo toEntity(CostoRequestDTO dto, Obra obra) {
        Costo costo = new Costo();
        costo.setObra(obra);
        costo.setConcepto(dto.getConcepto());
        costo.setDescripcion(dto.getDescripcion());
        costo.setMonto(dto.getMonto());
        costo.setFecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now());
        costo.setCategoria(dto.getCategoria() != null ? dto.getCategoria() : "General");
        costo.setTipoCosto(dto.getTipoCosto() != null ? dto.getTipoCosto() : "Directo");
        costo.setEstado(dto.getEstado() != null ? dto.getEstado() : "Pendiente");
        costo.setFechaAprobacion(dto.getFechaAprobacion());
        costo.setComentarios(dto.getComentarios());
        costo.setMotivoRechazo(dto.getMotivoRechazo());
        costo.setImputable(dto.getImputable() != null ? dto.getImputable() : true);
        costo.setSemana(dto.getSemana());
        costo.setAnio(dto.getAnio());

        return costo;
    }

    /**
     * Convierte una entidad Costo a un CostoResponseDTO.
     *
     * @param costo La entidad Costo
     * @return El DTO con los datos del costo
     */
    static CostoResponseDTO toResponseDTO(Costo costo) {
        CostoResponseDTO dto = new CostoResponseDTO();
        dto.setId_costo(costo.getId());
        dto.setConcepto(costo.getConcepto());
        dto.setDescripcion(costo.getDescripcion());
        dto.setMonto(costo.getMonto());
        dto.setFecha(costo.getFecha());
        dto.setCategoria(costo.getCategoria());
        dto.setTipo_costo(costo.getTipoCosto());
        dto.setEstado(costo.getEstado());
        dto.setFecha_aprobacion(costo.getFechaAprobacion());
        dto.setComentarios(costo.getComentarios());
        dto.setMotivo_rechazo(costo.getMotivoRechazo());
        dto.setImputable(costo.getImputable());
        dto.setSemana(costo.getSemana());
        dto.setAnio(costo.getAnio());
        dto.setFecha_creacion(costo.getFechaCreacion() != null ? costo.getFechaCreacion().toLocalDate() : null);
        dto.setFecha_actualizacion(costo.getFechaActualizacion() != null ? costo.getFechaActualizacion().toLocalDate() : null);
        dto.setId_obra(costo.getObra() != null ? costo.getObra().getId() : null);

        return dto;
    }

    /**
     * Convierte un Page<Costo> a PaginacionCostosResponse.
     *
     * @param costosPage El Page con los costos
     * @return El DTO de respuesta con la paginación
     */
    static PaginacionCostosResponse toPaginacionResponse(Page<Costo> costosPage) {
        List<CostoResponseDTO> contenido = costosPage.getContent().stream()
                .map(CostoMapper::toResponseDTO)
                .toList();

        PaginacionCostosResponse.PaginacionInfo paginacion = new PaginacionCostosResponse.PaginacionInfo();
        paginacion.numeroPagina = costosPage.getNumber();
        paginacion.tamanoPagina = costosPage.getSize();

        PaginacionCostosResponse.OrdenInfo orden = new PaginacionCostosResponse.OrdenInfo();
        orden.vacio = costosPage.getSort().isEmpty();
        orden.ordenado = costosPage.getSort().isSorted();
        orden.noOrdenado = costosPage.getSort().isUnsorted();
        paginacion.orden = orden;

        paginacion.desplazamiento = (int) costosPage.getPageable().getOffset();
        paginacion.paginado = costosPage.getPageable().isPaged();
        paginacion.noPaginado = costosPage.getPageable().isUnpaged();

        PaginacionCostosResponse response = new PaginacionCostosResponse();
        response.contenido = contenido;
        response.paginacion = paginacion;
        response.ultimo = costosPage.isLast();
        response.totalElementos = (int) costosPage.getTotalElements();
        response.totalPaginas = costosPage.getTotalPages();
        response.tamano = costosPage.getSize();
        response.numero = costosPage.getNumber();
        response.primero = costosPage.isFirst();
        response.numeroElementos = costosPage.getNumberOfElements();
        response.vacio = costosPage.isEmpty();

        if (contenido.isEmpty()) {
            response.mensaje = "No hay costos registrados para los parámetros solicitados.";
        }

        return response;
    }

    /**
     * Convierte los datos de total de costos de una obra a TotalCostosObraResponseDTO.
     *
     * @param obraId ID de la obra
     * @param totalCostos Total de costos (puede ser null)
     * @param cantidadCostos Cantidad de costos
     * @return El DTO con el resumen de costos de la obra
     */
    static TotalCostosObraResponseDTO toTotalCostosObraResponse(Long obraId, BigDecimal totalCostos, Long cantidadCostos) {
        return TotalCostosObraResponseDTO.builder()
                .obraId(obraId)
                .totalCostos(totalCostos != null ? totalCostos : BigDecimal.ZERO)
                .cantidadCostos(cantidadCostos)
                .fechaCalculo(LocalDateTime.now())
                .build();
    }

    /**
     * Convierte los datos de análisis de costos de una obra a AnalisisCostosObraResponseDTO.
     *
     * @param obraId ID de la obra
     * @param totalCostos Total de costos (puede ser null)
     * @param costosAprobados Costos aprobados (puede ser null)
     * @param costosPendientes Costos pendientes (puede ser null)
     * @param distribucionCategorias Lista con datos de distribución por categoría [categoria, monto, cantidad]
     * @return El DTO con el análisis de costos de la obra
     */
    static AnalisisCostosObraResponseDTO toAnalisisCostosObraResponse(Long obraId, BigDecimal totalCostos,
                                                                       BigDecimal costosAprobados, BigDecimal costosPendientes,
                                                                       List<Object[]> distribucionCategorias) {
        // Mapear distribución de categorías a DTOs
        List<AnalisisCostosObraResponseDTO.DistribucionCategoriaDTO> distribucionDTOs = distribucionCategorias.stream()
                .map(r -> AnalisisCostosObraResponseDTO.DistribucionCategoriaDTO.builder()
                        .categoria((String) r[0])
                        .monto((BigDecimal) r[1])
                        .cantidad((Long) r[2])
                        .build())
                .collect(Collectors.toList());

        return AnalisisCostosObraResponseDTO.builder()
                .obraId(obraId)
                .totalCostos(totalCostos != null ? totalCostos : BigDecimal.ZERO)
                .costosAprobados(costosAprobados != null ? costosAprobados : BigDecimal.ZERO)
                .costosPendientes(costosPendientes != null ? costosPendientes : BigDecimal.ZERO)
                .distribucionCategorias(distribucionDTOs)
                .build();
    }

    /**
     * Convierte los datos de comparación de presupuesto vs real a PresupuestoVsRealResponseDTO.
     *
     * @param obraId ID de la obra
     * @param presupuestoEstimado Presupuesto estimado
     * @param costosReales Costos reales (puede ser null)
     * @return El DTO con la comparación de presupuesto vs real
     */
    static PresupuestoVsRealResponseDTO toPresupuestoVsRealResponse(Long obraId, BigDecimal presupuestoEstimado, BigDecimal costosReales) {
        BigDecimal costosRealesValue = costosReales != null ? costosReales : BigDecimal.ZERO;
        BigDecimal variacion = costosRealesValue.subtract(presupuestoEstimado);
        BigDecimal porcentajeVariacion = presupuestoEstimado.compareTo(BigDecimal.ZERO) != 0 ?
                variacion.divide(presupuestoEstimado, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        String estado = porcentajeVariacion.compareTo(BigDecimal.valueOf(10)) <= 0 ? "Dentro del rango" : "Fuera del presupuesto";

        return PresupuestoVsRealResponseDTO.builder()
                .obraId(obraId)
                .presupuestoEstimado(presupuestoEstimado)
                .costosReales(costosRealesValue)
                .variacion(variacion)
                .porcentajeVariacion(porcentajeVariacion)
                .estado(estado)
                .build();
    }

    /**
     * Convierte los datos de cálculo de rentabilidad a RentabilidadObraResponseDTO.
     *
     * @param obraId ID de la obra
     * @param totalIngresos Total de ingresos
     * @param totalCostos Total de costos (puede ser null)
     * @return El DTO con el cálculo de rentabilidad
     */
    static RentabilidadObraResponseDTO toRentabilidadObraResponse(Long obraId, BigDecimal totalIngresos, BigDecimal totalCostos) {
        BigDecimal totalCostosValue = totalCostos != null ? totalCostos : BigDecimal.ZERO;
        BigDecimal utilidad = totalIngresos.subtract(totalCostosValue);
        BigDecimal margenRentabilidad = totalIngresos.compareTo(BigDecimal.ZERO) != 0 ?
                utilidad.divide(totalIngresos, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;

        String estado = margenRentabilidad.compareTo(BigDecimal.valueOf(15)) >= 0 ? "Rentable" :
                        margenRentabilidad.compareTo(BigDecimal.valueOf(5)) >= 0 ? "Aceptable" : "Baja rentabilidad";

        return RentabilidadObraResponseDTO.builder()
                .obraId(obraId)
                .totalIngresos(totalIngresos)
                .totalCostos(totalCostosValue)
                .utilidad(utilidad)
                .margenRentabilidad(margenRentabilidad)
                .estado(estado)
                .build();
    }

    /**
     * Convierte los datos de estadísticas generales a EstadisticasCostosResponseDTO.
     *
     * @param totalCostos Total de costos registrados
     * @param costosAprobados Cantidad de costos aprobados
     * @param costosPendientes Cantidad de costos pendientes
     * @param costosRechazados Cantidad de costos rechazados
     * @param montoTotal Monto total de costos (puede ser null)
     * @param montoAprobado Monto de costos aprobados (puede ser null)
     * @return El DTO con las estadísticas generales
     */
    static EstadisticasCostosResponseDTO toEstadisticasCostosResponse(Long totalCostos, Long costosAprobados,
                                                                       Long costosPendientes, Long costosRechazados,
                                                                       BigDecimal montoTotal, BigDecimal montoAprobado) {
        BigDecimal montoTotalValue = montoTotal != null ? montoTotal : BigDecimal.ZERO;
        BigDecimal montoAprobadoValue = montoAprobado != null ? montoAprobado : BigDecimal.ZERO;

        return EstadisticasCostosResponseDTO.builder()
                .totalCostos(totalCostos)
                .costosAprobados(costosAprobados)
                .costosPendientes(costosPendientes)
                .costosRechazados(costosRechazados)
                .montoTotal(montoTotalValue)
                .montoAprobado(montoAprobadoValue)
                .montoPendiente(montoTotalValue.subtract(montoAprobadoValue))
                .build();
    }

    /**
     * Convierte una lista de resultados de estadísticas por categoría a List<EstadisticasPorCategoriaDTO>.
     *
     * @param resultados Lista de arrays con datos [categoria, cantidadCostos, montoTotal]
     * @return Lista de DTOs con estadísticas por categoría
     */
    static List<EstadisticasPorCategoriaDTO> toEstadisticasPorCategoriaList(List<Object[]> resultados) {
        return resultados.stream()
                .map(r -> EstadisticasPorCategoriaDTO.builder()
                        .categoria(r[0] != null ? (String) r[0] : "Sin categoría")
                        .cantidadCostos((Long) r[1])
                        .montoTotal(r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de resultados de estadísticas por obra a List<EstadisticasPorObraDTO>.
     *
     * @param resultados Lista de arrays con datos [obraId, obraNombre, cantidadCostos, montoTotal]
     * @return Lista de DTOs con estadísticas por obra
     */
    static List<EstadisticasPorObraDTO> toEstadisticasPorObraList(List<Object[]> resultados) {
        return resultados.stream()
                .map(r -> EstadisticasPorObraDTO.builder()
                        .obraId((Long) r[0])
                        .obraNombre((String) r[1])
                        .cantidadCostos((Long) r[2])
                        .montoTotal(r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de resultados de estadísticas mensuales a List<EstadisticasMensualesDTO>.
     *
     * @param resultados Lista de arrays con datos [mes, año, cantidadCostos, montoTotal]
     * @return Lista de DTOs con estadísticas mensuales
     */
    static List<EstadisticasMensualesDTO> toEstadisticasMensualesList(List<Object[]> resultados) {
        return resultados.stream()
                .map(r -> EstadisticasMensualesDTO.builder()
                        .mes((Integer) r[0])
                        .anio((Integer) r[1])
                        .cantidadCostos((Long) r[2])
                        .montoTotal(r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de resultados de reporte resumen de obras a List<ReporteResumenObraDTO>.
     *
     * @param resultados Lista de arrays con datos [obraId, obraNombre, cliente, totalCostos, costosAprobados, porcentajeAprobado]
     * @return Lista de DTOs con reporte resumen de obras
     */
    static List<ReporteResumenObraDTO> toReporteResumenObraList(List<Object[]> resultados) {
        return resultados.stream()
                .map(r -> ReporteResumenObraDTO.builder()
                        .obraId((Long) r[0])
                        .obraNombre((String) r[1])
                        .cliente((String) r[2])
                        .totalCostos(r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO)
                        .costosAprobados(r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO)
                        .porcentajeAprobado(r[5] != null ? (BigDecimal) r[5] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de resultados de reporte de variaciones presupuestarias a List<ReporteVariacionesPresupuestoDTO>.
     *
     * @param resultados Lista de arrays con datos [obraId, obraNombre, presupuestoEstimado, costosReales, variacion, porcentajeVariacion]
     * @return Lista de DTOs con reporte de variaciones presupuestarias
     */
    static List<ReporteVariacionesPresupuestoDTO> toReporteVariacionesPresupuestoList(List<Object[]> resultados) {
        return resultados.stream()
                .map(r -> ReporteVariacionesPresupuestoDTO.builder()
                        .obraId((Long) r[0])
                        .obraNombre((String) r[1])
                        .presupuestoEstimado(r[2] != null ? (BigDecimal) r[2] : BigDecimal.ZERO)
                        .costosReales(r[3] != null ? (BigDecimal) r[3] : BigDecimal.ZERO)
                        .variacion(r[4] != null ? (BigDecimal) r[4] : BigDecimal.ZERO)
                        .porcentajeVariacion(r[5] != null ? (BigDecimal) r[5] : BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
    }

    /**
     * Convierte una lista de resultados de top costos a List<TopCostosDTO>.
     *
     * @param resultados Lista de arrays con datos [costoId, concepto, monto, obra, categoria, fecha]
     * @return Lista de DTOs con los costos más altos
     */
    static List<TopCostosDTO> toTopCostosList(List<Object[]> resultados) {
        return resultados.stream()
                .map(r -> TopCostosDTO.builder()
                        .costoId((Long) r[0])
                        .concepto((String) r[1])
                        .monto((BigDecimal) r[2])
                        .obra((String) r[3])
                        .categoria((String) r[4])
                        .fecha((LocalDate) r[5])
                        .build())
                .collect(Collectors.toList());
    }
}

