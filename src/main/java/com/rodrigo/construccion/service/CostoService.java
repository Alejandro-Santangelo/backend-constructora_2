package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.Costo;
import com.rodrigo.construccion.repository.CostoRepository;
import com.rodrigo.construccion.repository.EmpresaRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Costos
 * 
 * Maneja toda la lógica de negocio relacionada con costos de obras.
 * Incluye validaciones multi-tenant, análisis financiero y gestión de presupuestos.
 */
@Service
@Transactional
public class CostoService {

    private final CostoRepository costoRepository;
    private final EmpresaRepository empresaRepository;
    private final ObraRepository obraRepository;

    public CostoService(CostoRepository costoRepository, EmpresaRepository empresaRepository, ObraRepository obraRepository) {
        this.costoRepository = costoRepository;
        this.empresaRepository = empresaRepository;
        this.obraRepository = obraRepository;
    }

    /**
     * OPERACIONES CRUD
     */

    /**
     * Crear un nuevo costo
     */
    public Costo crearCosto(Costo costo, Long empresaId) {
    System.out.println("Creando costo - Concepto: " + costo.getConcepto() + " - Monto: " + costo.getMonto() + " - Empresa: " + empresaId);

        // Validar que la empresa existe
        empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));

        // Validaciones de negocio
        if (costo.getConcepto() == null || costo.getConcepto().trim().isEmpty()) {
            throw new IllegalArgumentException("El concepto del costo es obligatorio");
        }

        if (costo.getMonto() == null || costo.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El monto del costo debe ser mayor a cero");
        }

        if (costo.getObra() == null) {
            throw new IllegalArgumentException("La obra es obligatoria para el costo");
        }

        // Validar que la obra pertenece a la empresa
        // Valida usando la primera empresa asociada al cliente
        if (costo.getObra().getCliente().getEmpresas() == null || costo.getObra().getCliente().getEmpresas().isEmpty() ||
            !costo.getObra().getCliente().getEmpresas().get(0).getId().equals(empresaId)) {
            throw new IllegalArgumentException("La obra no pertenece a la empresa especificada");
        }

        // Establecer valores por defecto
        if (costo.getFecha() == null) {
            costo.setFecha(LocalDate.now());
        }

        if (costo.getEstado() == null || costo.getEstado().trim().isEmpty()) {
            costo.setEstado("Pendiente");
        }

        if (costo.getFechaCreacion() == null) {
            costo.setFechaCreacion(LocalDateTime.now());
        }

        if (costo.getCategoria() == null || costo.getCategoria().trim().isEmpty()) {
            costo.setCategoria("General");
        }

        if (costo.getTipoCosto() == null || costo.getTipoCosto().trim().isEmpty()) {
            costo.setTipoCosto("Directo");
        }

        var costoGuardado = costoRepository.save(costo);
        System.out.println("Costo creado exitosamente con ID: " + costoGuardado.getId());

        return costoGuardado;
    }

    /**
     * Obtener costos por empresa con paginación
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable) {
        return costoRepository.findByObra_Cliente_Empresa_Id(empresaId, pageable);
    }

    /**
     * Obtener costo por ID y empresa
     */
    @Transactional(readOnly = true)
    public Optional<Costo> obtenerPorIdYEmpresa(Long id, Long empresaId) {
        return costoRepository.findByIdAndObra_Cliente_Empresa_Id(id, empresaId);
    }

    /**
     * Actualizar costo
     */
    public Costo actualizarCosto(Long id, Costo costoActualizado, Long empresaId) {
    System.out.println("Actualizando costo ID: " + id + " - Empresa: " + empresaId);

        var costoExistente = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Costo no encontrado"));

        // No permitir modificar costos aprobados
        if ("Aprobado".equals(costoExistente.getEstado())) {
            throw new IllegalArgumentException("No se puede modificar un costo aprobado");
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
            System.out.println("Costo actualizado exitosamente: " + costoGuardado.getId());

        return costoGuardado;
    }

    /**
     * Eliminar costo
     */
    public void eliminarCosto(Long id, Long empresaId) {
    System.out.println("Eliminando costo ID: " + id + " - Empresa: " + empresaId);

        var costo = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Costo no encontrado"));

        if ("Aprobado".equals(costo.getEstado())) {
            throw new IllegalArgumentException("No se puede eliminar un costo aprobado");
        }

        costoRepository.deleteById(id);
        System.out.println("Costo eliminado exitosamente: " + id);
    }

    /**
     * CONSULTAS POR OBRA
     */

    /**
     * Obtener costos por obra
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerCostosPorObra(Long obraId, Long empresaId, Pageable pageable) {
        return costoRepository.findByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId, pageable);
    }

    /**
     * Calcular total de costos por obra
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calcularTotalCostosObra(Long obraId, Long empresaId) {
        var totalCostos = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var cantidadCostos = costoRepository.countByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        
        return Map.of(
                "obraId", obraId,
                "totalCostos", totalCostos != null ? totalCostos : 0.0,
                "cantidadCostos", cantidadCostos,
                "fechaCalculo", LocalDateTime.now()
        );
    }

    /**
     * CONSULTAS POR CATEGORÍA Y TIPO
     */

    /**
     * Obtener costos por categoría
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerCostosPorCategoria(String categoria, Long empresaId, Pageable pageable) {
        // Normaliza la categoría: quita acentos, pasa a minúsculas y recorta espacios
        String categoriaNormalizada = java.text.Normalizer.normalize(categoria, java.text.Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase().trim();
        return costoRepository.buscarPorCategoriaFlexible(empresaId, categoriaNormalizada, pageable);
    }

    /**
     * Obtener costos por tipo
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerCostosPorTipo(String tipo, Long empresaId, Pageable pageable) {
        return costoRepository.findByObra_Cliente_Empresa_IdAndTipoCosto(empresaId, tipo, pageable);
    }

    /**
     * CONSULTAS POR FECHA
     */

    /**
     * Obtener costos por período
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerCostosPorPeriodo(LocalDate fechaDesde, LocalDate fechaHasta, Long empresaId, Pageable pageable) {
        return costoRepository.findByObra_Cliente_Empresa_IdAndFechaBetween(empresaId, fechaDesde, fechaHasta, pageable);
    }

    /**
     * Obtener costos del mes actual
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerCostosDelMes(Long empresaId, Pageable pageable) {
        var inicioMes = LocalDate.now().withDayOfMonth(1);
        var finMes = inicioMes.plusMonths(1).minusDays(1);
        return obtenerCostosPorPeriodo(inicioMes, finMes, empresaId, pageable);
    }

    /**
     * BÚSQUEDAS Y FILTROS
     */

    /**
     * Buscar costos por texto
     */
    @Transactional(readOnly = true)
    public Page<Costo> buscarCostos(Long empresaId, String texto, Pageable pageable) {
        return costoRepository.buscarPorTexto(empresaId, texto, pageable);
    }

    /**
     * Filtrar costos por múltiples criterios
     */
    @Transactional(readOnly = true)
    public Page<Costo> filtrarCostos(Long empresaId, Long obraId, String categoria, String tipo, 
                                   Double montoMinimo, Double montoMaximo, Pageable pageable) {
        return costoRepository.filtrarCostos(empresaId, obraId, categoria, tipo, 
                montoMinimo != null ? BigDecimal.valueOf(montoMinimo) : null,
                montoMaximo != null ? BigDecimal.valueOf(montoMaximo) : null, 
                pageable);
    }

    /**
     * ANÁLISIS DE COSTOS
     */

    /**
     * Obtener análisis de costos por obra
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerAnalisisCostosObra(Long obraId, Long empresaId) {
        var totalCostos = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var costosAprobados = costoRepository.sumMontoByObra_IdAndEstadoAndObra_Cliente_Empresa_Id(obraId, "Aprobado", empresaId);
        var costosPendientes = costoRepository.sumMontoByObra_IdAndEstadoAndObra_Cliente_Empresa_Id(obraId, "Pendiente", empresaId);
        
        var distribucionCategorias = costoRepository.getDistribucionCostosCategoria(obraId, empresaId);
        
        return Map.of(
                "obraId", obraId,
                "totalCostos", totalCostos != null ? totalCostos : 0.0,
                "costosAprobados", costosAprobados != null ? costosAprobados : 0.0,
                "costosPendientes", costosPendientes != null ? costosPendientes : 0.0,
                "distribucionCategorias", distribucionCategorias.stream().map(r -> Map.of(
                    "categoria", r[0],
                    "monto", r[1],
                    "cantidad", r[2]
                )).collect(Collectors.toList())
        );
    }

    /**
     * Comparar presupuesto vs real
     */
    @Transactional(readOnly = true)
    public Map<String, Object> compararPresupuestoVsReal(Long obraId, Long empresaId) {
        var costosReales = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var presupuestoEstimado = BigDecimal.ZERO; // Obtener del presupuesto real
        
        var costosRealesValue = costosReales != null ? costosReales : BigDecimal.ZERO;
        var variacion = costosRealesValue.subtract(presupuestoEstimado);
        var porcentajeVariacion = presupuestoEstimado.compareTo(BigDecimal.ZERO) != 0 ? 
                variacion.divide(presupuestoEstimado, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        return Map.of(
                "obraId", obraId,
                "presupuestoEstimado", presupuestoEstimado,
                "costosReales", costosRealesValue,
                "variacion", variacion,
                "porcentajeVariacion", porcentajeVariacion,
                "estado", porcentajeVariacion.compareTo(BigDecimal.valueOf(10)) <= 0 ? "Dentro del rango" : "Fuera del presupuesto"
        );
    }

    /**
     * Calcular rentabilidad
     */
    @Transactional(readOnly = true)
    public Map<String, Object> calcularRentabilidad(Long obraId, Long empresaId) {
        var totalCostos = costoRepository.sumMontoByObra_IdAndObra_Cliente_Empresa_Id(obraId, empresaId);
        var totalIngresos = BigDecimal.ZERO; // Obtener de facturas/contratos
        
        var totalCostosValue = totalCostos != null ? totalCostos : BigDecimal.ZERO;
        var utilidad = totalIngresos.subtract(totalCostosValue);
        var margenRentabilidad = totalIngresos.compareTo(BigDecimal.ZERO) != 0 ? 
                utilidad.divide(totalIngresos, 2, java.math.RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100)) : BigDecimal.ZERO;
        
        return Map.of(
                "obraId", obraId,
                "totalIngresos", totalIngresos,
                "totalCostos", totalCostosValue,
                "utilidad", utilidad,
                "margenRentabilidad", margenRentabilidad,
                "estado", margenRentabilidad.compareTo(BigDecimal.valueOf(15)) >= 0 ? "Rentable" : 
                         margenRentabilidad.compareTo(BigDecimal.valueOf(5)) >= 0 ? "Aceptable" : "Baja rentabilidad"
        );
    }

    /**
     * ESTADÍSTICAS Y MÉTRICAS
     */

    /**
     * Obtener estadísticas generales
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas(Long empresaId) {
        var totalCostos = costoRepository.countByObra_Cliente_Empresa_Id(empresaId);
        var costosAprobados = costoRepository.countByObra_Cliente_Empresa_IdAndEstado(empresaId, "Aprobado");
        var costosPendientes = costoRepository.countByObra_Cliente_Empresa_IdAndEstado(empresaId, "Pendiente");
        var costosRechazados = costoRepository.countByObra_Cliente_Empresa_IdAndEstado(empresaId, "Rechazado");
        
        var montoTotal = costoRepository.sumMontoByObra_Cliente_Empresa_Id(empresaId);
        var montoAprobado = costoRepository.sumMontoByObra_Cliente_Empresa_IdAndEstado(empresaId, "Aprobado");
        
        var montoTotalValue = montoTotal != null ? montoTotal : BigDecimal.ZERO;
        var montoAprobadoValue = montoAprobado != null ? montoAprobado : BigDecimal.ZERO;
        
        return Map.of(
                "totalCostos", totalCostos,
                "costosAprobados", costosAprobados,
                "costosPendientes", costosPendientes,
                "costosRechazados", costosRechazados,
                "montoTotal", montoTotalValue,
                "montoAprobado", montoAprobadoValue,
                "montoPendiente", montoTotalValue.subtract(montoAprobadoValue)
        );
    }

    /**
     * Obtener estadísticas por categoría
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerEstadisticasPorCategoria(Long empresaId) {
        var resultados = costoRepository.getEstadisticasPorCategoria(empresaId);
        
        return resultados.stream().map(r -> Map.of(
                "categoria", r[0] != null ? r[0] : "Sin categoría",
                "cantidadCostos", r[1],
                "montoTotal", r[2] != null ? r[2] : 0.0
        )).collect(Collectors.toList());
    }

    /**
     * Obtener estadísticas por obra
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerEstadisticasPorObra(Long empresaId) {
        var resultados = costoRepository.getEstadisticasPorObra(empresaId);
        
        return resultados.stream().map(r -> Map.of(
                "obraId", r[0],
                "obraNombre", r[1],
                "cantidadCostos", r[2],
                "montoTotal", r[3] != null ? r[3] : 0.0
        )).collect(Collectors.toList());
    }

    /**
     * Obtener estadísticas mensuales
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerEstadisticasMensuales(Long empresaId) {
        var resultados = costoRepository.getEstadisticasMensuales(empresaId);
        
        return resultados.stream().map(r -> Map.of(
                "mes", r[0],
                "año", r[1],
                "cantidadCostos", r[2],
                "montoTotal", r[3] != null ? r[3] : 0.0
        )).collect(Collectors.toList());
    }

    /**
     * REPORTES
     */

    /**
     * Obtener reporte resumen de obras
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerReporteResumenObras(Long empresaId) {
        var resultados = costoRepository.getReporteResumenObras(empresaId);
        
        return resultados.stream().map(r -> Map.of(
                "obraId", r[0],
                "obraNombre", r[1],
                "cliente", r[2],
                "totalCostos", r[3] != null ? r[3] : 0.0,
                "costosAprobados", r[4] != null ? r[4] : 0.0,
                "porcentajeAprobado", r[5] != null ? r[5] : 0.0
        )).collect(Collectors.toList());
    }

    /**
     * Obtener reporte de variaciones presupuestarias
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerReporteVariacionesPresupuesto(Long empresaId) {
        var resultados = costoRepository.getReporteVariacionesPresupuesto(empresaId);
        
        return resultados.stream().map(r -> Map.of(
                "obraId", r[0],
                "obraNombre", r[1],
                "presupuestoEstimado", r[2] != null ? r[2] : 0.0,
                "costosReales", r[3] != null ? r[3] : 0.0,
                "variacion", r[4] != null ? r[4] : 0.0,
                "porcentajeVariacion", r[5] != null ? r[5] : 0.0
        )).collect(Collectors.toList());
    }

    /**
     * Obtener top costos
     */
    @Transactional(readOnly = true)
    public List<Map<String, Object>> obtenerTopCostos(Long empresaId, int limite) {
        var resultados = costoRepository.getTopCostos(empresaId, limite);
        
        return resultados.stream().map(r -> Map.of(
                "costoId", r[0],
                "concepto", r[1],
                "monto", r[2],
                "obra", r[3],
                "categoria", r[4],
                "fecha", r[5]
        )).collect(Collectors.toList());
    }

    /**
     * GESTIÓN DE ESTADOS
     */

    /**
     * Aprobar costo
     */
    public Costo aprobarCosto(Long id, String comentarios, Long empresaId) {
    System.out.println("Aprobando costo ID: " + id + " - Empresa: " + empresaId);

        var costo = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Costo no encontrado"));

        if ("Aprobado".equals(costo.getEstado())) {
            throw new IllegalArgumentException("El costo ya está aprobado");
        }

        costo.setEstado("Aprobado");
        costo.setFechaAprobacion(LocalDate.now());
        
        if (comentarios != null && !comentarios.trim().isEmpty()) {
            costo.setDescripcion(
                (costo.getDescripcion() != null ? costo.getDescripcion() + "\n" : "") +
                "APROBADO: " + comentarios
            );
        }

        return costoRepository.save(costo);
    }

    /**
     * Rechazar costo
     */
    public Costo rechazarCosto(Long id, String motivoRechazo, Long empresaId) {
    System.out.println("Rechazando costo ID: " + id + " - Empresa: " + empresaId);

        var costo = obtenerPorIdYEmpresa(id, empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Costo no encontrado"));

        costo.setEstado("Rechazado");
        
        if (motivoRechazo != null && !motivoRechazo.trim().isEmpty()) {
            costo.setDescripcion(
                (costo.getDescripcion() != null ? costo.getDescripcion() + "\n" : "") +
                "RECHAZADO: " + motivoRechazo
            );
        }

        return costoRepository.save(costo);
    }

    /**
     * MÉTODOS AUXILIARES
     */

    /**
     * Listar categorías disponibles
     */
    @Transactional(readOnly = true)
    public List<String> listarCategorias(Long empresaId) {
        return costoRepository.getCategorias(empresaId);
    }

    /**
     * Listar tipos disponibles
     */
    @Transactional(readOnly = true)
    public List<String> listarTipos(Long empresaId) {
        return costoRepository.getTipos(empresaId);
    }

    /**
     * Contar costos por empresa
     */
    @Transactional(readOnly = true)
    public long contarCostosPorEmpresa(Long empresaId) {
        return costoRepository.countByObra_Cliente_Empresa_Id(empresaId);
    }

    /**
     * Crear un nuevo costo a partir de un DTO
     */
    public Costo crearCostoDesdeDTO(com.rodrigo.construccion.dto.request.CostoRequestDTO dto, Long empresaId) {
        if (dto == null) {
            throw new IllegalArgumentException("El DTO de costo no puede ser nulo");
        }
        if (dto.getIdObra() == null) {
            throw new IllegalArgumentException("El idObra es obligatorio");
        }
        // Validar que la empresa existe
        empresaRepository.findById(empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        // Validar que la obra existe y pertenece a la empresa
        var obra = obraRepository.findByIdAndEmpresaId(dto.getIdObra(), empresaId)
                .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada o no pertenece a la empresa"));
        // Crear entidad Costo usando getters/setters de Lombok
        Costo costo = new Costo();
        costo.setObra(obra);
        costo.setConcepto(dto.getConcepto());
        costo.setDescripcion(dto.getDescripcion());
        costo.setMonto(dto.getMonto());
        costo.setFecha(dto.getFecha() != null ? dto.getFecha() : java.time.LocalDate.now());
        costo.setCategoria(dto.getCategoria() != null ? dto.getCategoria() : "General");
        costo.setTipoCosto(dto.getTipoCosto() != null ? dto.getTipoCosto() : "Directo");
        costo.setEstado(dto.getEstado() != null ? dto.getEstado() : "Pendiente");
        costo.setFechaAprobacion(dto.getFechaAprobacion());
        costo.setComentarios(dto.getComentarios());
        costo.setMotivoRechazo(dto.getMotivoRechazo());
        costo.setImputable(dto.getImputable() != null ? dto.getImputable() : true);
        // fechaCreacion y fechaActualizacion se setean automáticamente
        // semana y anio se calculan automáticamente
        return costoRepository.save(costo);
    }

    /**
     * Listar costos por empresa
     */
    @Transactional(readOnly = true)
    public Page<Costo> listarCostosPorEmpresa(Long empresaId, Pageable pageable) {
        return costoRepository.findByObra_Cliente_Empresa_Id(empresaId, pageable);
    }

    /**
     * Listar costos por tipo
     */
    @Transactional(readOnly = true)
    public Page<Costo> listarCostosPorTipo(Long empresaId, String tipo, Pageable pageRequest) {
        // Normaliza el tipo: quita acentos, pasa a minúsculas y recorta espacios
        String tipoNormalizado = java.text.Normalizer.normalize(tipo, java.text.Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase().trim();
        return costoRepository.buscarPorTipoFlexible(empresaId, tipoNormalizado, pageRequest);
    }

    /**
     * Obtener costos por estado
     */
    @Transactional(readOnly = true)
    public Page<Costo> obtenerCostosPorEstado(String estado, Long empresaId, Pageable pageable) {
        // Normaliza el estado: quita acentos, pasa a minúsculas y recorta espacios
        String estadoNormalizado = java.text.Normalizer.normalize(estado, java.text.Normalizer.Form.NFD)
            .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
            .toLowerCase().trim();
        return costoRepository.buscarPorEstadoFlexible(empresaId, estadoNormalizado, pageable);
    }
}