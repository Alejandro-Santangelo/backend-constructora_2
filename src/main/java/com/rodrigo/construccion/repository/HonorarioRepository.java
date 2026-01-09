package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Honorario;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Honorario
 * 
 * Maneja el acceso a datos de honorarios con consultas especializadas
 * para la gestión multi-tenant y análisis de costos laborales.
 */
@Repository
public interface HonorarioRepository extends JpaRepository<Honorario, Long> {

       /* Buscar honorarios por empresa y período de fechas (sin paginación) */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId AND h.fecha BETWEEN :fechaDesde AND :fechaHasta")
       List<Honorario> findByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
                     @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta);

       /* Buscar honorarios por empresa y monto mínimo */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId AND h.monto >= :montoMinimo")
       List<Honorario> findByEmpresaIdAndMontoGreaterThanEqual(@Param("empresaId") Long empresaId,
                     @Param("montoMinimo") java.math.BigDecimal montoMinimo);

       List<Honorario> findByProfesional_IdIn(List<Long> profesionalIds);

       /* Sumar montos por empresa y período de fechas */
       @Query("SELECT SUM(h.monto) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId AND h.fecha BETWEEN :fechaDesde AND :fechaHasta")
       BigDecimal sumMontoByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId,
                     @Param("fechaDesde") LocalDate fechaDesde,
                     @Param("fechaHasta") LocalDate fechaHasta);

       /* Contar honorarios por empresa y período de fechas */
       @Query("SELECT COUNT(h) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId AND h.fecha BETWEEN :fechaDesde AND :fechaHasta")
       long countByEmpresaIdAndFechaBetween(@Param("empresaId") Long empresaId, @Param("fechaDesde") LocalDate fechaDesde,
                     @Param("fechaHasta") LocalDate fechaHasta);

       /* METODOS QUE NO ESTAN SIENDO USADOS */

       /* Buscar honorarios por empresa a través de la obra */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
       Page<Honorario> findByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId, Pageable pageable);

       /* Buscar honorario por ID y empresa */
       @Query("SELECT h FROM Honorario h WHERE h.id = :id AND :empresaId MEMBER OF h.obra.cliente.empresas")
       Optional<Honorario> findByIdAndObra_Cliente_Empresa_Id(@Param("id") Long id, @Param("empresaId") Long empresaId);

       /* Contar honorarios por empresa */
       @Query("SELECT COUNT(h) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
       long countByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId);

       /* CONSULTAS POR PROFESIONAL */

       /* Buscar honorarios por profesional y empresa */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e WHERE h.profesional.id = :profesionalId AND e.id = :empresaId")
       Page<Honorario> findByProfesional_IdAndObra_Cliente_Empresa_Id(@Param("profesionalId") Long profesionalId,
                     @Param("empresaId") Long empresaId, Pageable pageable);

       /* Contar honorarios por profesional y empresa */
       @Query("SELECT COUNT(h) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE h.profesional.id = :profesionalId AND e.id = :empresaId")
       long countByProfesional_IdAndObra_Cliente_Empresa_Id(@Param("profesionalId") Long profesionalId,
                     @Param("empresaId") Long empresaId);

       /* Sumar montos por profesional y empresa */
       @Query("SELECT SUM(h.monto) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE h.profesional.id = :profesionalId AND e.id = :empresaId")
       Double sumMontoByProfesional_IdAndObra_Cliente_Empresa_Id(@Param("profesionalId") Long profesionalId,
                     @Param("empresaId") Long empresaId);

       /* CONSULTAS POR OBRA */

       /* Buscar honorarios por obra y empresa */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e WHERE h.obra.id = :obraId AND e.id = :empresaId")
       Page<Honorario> findByObra_IdAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId,
                     @Param("empresaId") Long empresaId, Pageable pageable);

       /* Sumar montos por obra y empresa */
       @Query("SELECT SUM(h.monto) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE h.obra.id = :obraId AND e.id = :empresaId")
       Double sumMontoByObra_IdAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId,
                     @Param("empresaId") Long empresaId);

       /* Contar profesionales distintos por obra */
       @Query("SELECT COUNT(DISTINCT h.profesional.id) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE h.obra.id = :obraId AND e.id = :empresaId")
       long countDistinctProfesionalesByObra_IdAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId,
                     @Param("empresaId") Long empresaId);

       /* CONSULTAS POR FECHA */

       /*
        * Buscar honorarios por empresa y período de fechas (usando campo real de BD) - CON PAGINACIÓN
        */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId AND h.fecha BETWEEN :fechaDesde AND :fechaHasta")
       Page<Honorario> findByObra_Cliente_Empresa_IdAndFechaBetween(@Param("empresaId") Long empresaId,
                     @Param("fechaDesde") LocalDate fechaDesde,
                     @Param("fechaHasta") LocalDate fechaHasta,
                     Pageable pageable);

       /* CONSULTAS DE SUMAS Y AGREGACIONES */

       /* Sumar montos por empresa */
       @Query("SELECT SUM(h.monto) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
       Double sumMontoByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId);

       /* Sumar montos por empresa (método alternativo) */
       @Query("SELECT SUM(h.monto) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
       Double sumMontoByEmpresa(@Param("empresaId") Long empresaId);

       /**
        * BÚSQUEDAS DE TEXTO
        */

       /* Buscar honorarios por texto en observaciones */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "AND (LOWER(h.observaciones) LIKE LOWER(CONCAT('%', :texto, '%')) " +
                     "OR LOWER(h.profesional.nombre) LIKE LOWER(CONCAT('%', :texto, '%')))")
       Page<Honorario> findByEmpresaIdAndTextoContaining(@Param("empresaId") Long empresaId,
                     @Param("texto") String texto,
                     Pageable pageable);

       /* FILTROS COMPLEJOS */

       /* Filtrar honorarios por múltiples criterios */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "AND (:profesionalId IS NULL OR h.profesional.id = :profesionalId) " +
                     "AND (:obraId IS NULL OR h.obra.id = :obraId) " +
                     "AND (:montoMinimo IS NULL OR h.monto >= :montoMinimo) " +
                     "AND (:montoMaximo IS NULL OR h.monto <= :montoMaximo)")
       Page<Honorario> filtrarHonorarios(@Param("empresaId") Long empresaId,
                     @Param("profesionalId") Long profesionalId,
                     @Param("obraId") Long obraId,
                     @Param("montoMinimo") Double montoMinimo,
                     @Param("montoMaximo") Double montoMaximo,
                     Pageable pageable);

       /* Búsqueda avanzada y dinámica de honorarios */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "AND (:fechaInicio IS NULL OR h.fecha >= :fechaInicio) " +
                     "AND (:fechaFin IS NULL OR h.fecha <= :fechaFin) " +
                     "AND (:montoMinimo IS NULL OR h.monto >= :montoMinimo)")
       List<Honorario> busquedaAvanzada(@Param("empresaId") Long empresaId,
                     @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin,
                     @Param("montoMinimo") BigDecimal montoMinimo);

       /* CONSULTAS DE VALIDACIÓN */

       /* TODO: ADAPTAR - usa po.obra que ya no existe
       @Query("SELECT COUNT(po) > 0 FROM ProfesionalObra po WHERE po.profesional.id = :profesionalId AND po.obra.id = :obraId")
       boolean profesionalTrabajaEnObra(@Param("profesionalId") Long profesionalId, @Param("obraId") Long obraId);
       */

       /* CONSULTAS PARA ESTADOS */

       /* Buscar honorarios ordenados por fecha */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "ORDER BY h.fecha ASC")
       List<Honorario> findHonorariosPendientesLiquidacion(@Param("empresaId") Long empresaId);

       /* CONSULTAS PARA CÁLCULOS AUTOMÁTICOS */

       /* TODO: ADAPTAR - usa j.asignacion.obra que ya no existe
       @Query("SELECT j.asignacion.obra.id, j.asignacion.profesional.id, " +
                     "SUM(j.horasTrabajadas), " +
                     "j.asignacion.valorHoraAsignado " +
                     "FROM Jornal j " +
                     "JOIN j.asignacion.obra.cliente.empresas e " +
                     "WHERE j.asignacion.profesional.id = :profesionalId AND e.id = :empresaId " +
                     "AND j.fecha BETWEEN :fechaDesde AND :fechaHasta " +
                     "GROUP BY j.asignacion.obra.id, j.asignacion.profesional.id, j.asignacion.valorHoraAsignado")
       List<Object[]> getJornalesPorProfesionalEnPeriodo(@Param("profesionalId") Long profesionalId,
                     @Param("fechaDesde") LocalDate fechaDesde,
                     @Param("fechaHasta") LocalDate fechaHasta,
                     @Param("empresaId") Long empresaId);
       */

       /**
        * ESTADÍSTICAS
        */

       /* Obtener estadísticas por profesional */
       @Query("SELECT h.profesional.id, h.profesional.nombre, " +
                     "COUNT(h), SUM(h.monto) " +
                     "FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId " +
                     "GROUP BY h.profesional.id, h.profesional.nombre " +
                     "ORDER BY SUM(h.monto) DESC")
       List<Object[]> getEstadisticasPorProfesional(@Param("empresaId") Long empresaId);

       /* Obtener estadísticas por obra */
       @Query("SELECT h.obra.id, h.obra.nombre, COUNT(h), SUM(h.monto), " +
                     "COUNT(DISTINCT h.profesional.id) " +
                     "FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId " +
                     "GROUP BY h.obra.id, h.obra.nombre " +
                     "ORDER BY SUM(h.monto) DESC")
       List<Object[]> getEstadisticasPorObra(@Param("empresaId") Long empresaId);

       /* Obtener estadísticas mensuales */
       @Query("SELECT MONTH(h.fecha), YEAR(h.fecha), " +
                     "COUNT(h), SUM(h.monto) " +
                     "FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId " +
                     "GROUP BY MONTH(h.fecha), YEAR(h.fecha) " +
                     "ORDER BY YEAR(h.fecha) DESC, MONTH(h.fecha) DESC")
       List<Object[]> getEstadisticasMensuales(@Param("empresaId") Long empresaId);

       /* REPORTES ESPECIALIZADOS */

       /* Reporte de liquidaciones en un período */
       @Query("SELECT h.profesional.nombre, h.obra.nombre, h.monto, h.fecha " +
                     "FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "AND h.fecha BETWEEN :fechaDesde AND :fechaHasta " +
                     "ORDER BY h.fecha DESC")
       List<Object[]> getReporteLiquidaciones(@Param("fechaDesde") LocalDate fechaDesde,
                     @Param("fechaHasta") LocalDate fechaHasta,
                     @Param("empresaId") Long empresaId);

       /* TODO: ADAPTAR - usa j.asignacion.obra que ya no existe
       @Query("SELECT h.obra.id, h.obra.nombre, " +
                     "SUM(h.monto), " +
                     "COALESCE((SELECT SUM(j.asignacion.valorHoraAsignado * j.horasTrabajadas) " +
                     "FROM Jornal j WHERE j.asignacion.obra.id = h.obra.id), 0) " +
                     "FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "GROUP BY h.obra.id, h.obra.nombre " +
                     "ORDER BY (SUM(h.monto) + COALESCE((SELECT SUM(j.asignacion.valorHoraAsignado * j.horasTrabajadas) "
                     +
                     "FROM Jornal j WHERE j.asignacion.obra.id = h.obra.id), 0)) DESC")
       List<Object[]> getReporteCostosLaborales(@Param("empresaId") Long empresaId);
       */

       /* Reporte de rentabilidad por profesional */
       @Query("SELECT h.profesional.id, h.profesional.nombre, " +
                     "SUM(h.monto), " +
                     "COALESCE((SELECT SUM(j.horasTrabajadas) " +
                     "FROM Jornal j WHERE j.asignacion.profesional.id = h.profesional.id), 0) " +
                     "FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "GROUP BY h.profesional.id, h.profesional.nombre " +
                     "ORDER BY SUM(h.monto) DESC")
       List<Object[]> getReporteRentabilidadProfesional(@Param("empresaId") Long empresaId);

       /* CONSULTAS PARA HISTORIAL */

       /* Obtener historial completo de un profesional (campos reales) */
       @Query("SELECT h.fecha, h.obra.nombre, h.monto, h.observaciones " +
                     "FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e " +
                     "WHERE h.profesional.id = :profesionalId AND e.id = :empresaId " +
                     "ORDER BY h.fecha DESC")
       List<Object[]> getHistorialProfesional(@Param("profesionalId") Long profesionalId,
                     @Param("empresaId") Long empresaId);

       /* CONSULTAS PARA LISTAS Y SELECCIÓN */

       /* Obtener observaciones distintas (sustituto para tipos de honorario) */
       @Query("SELECT DISTINCT h.observaciones FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId AND h.observaciones IS NOT NULL " +
                     "ORDER BY h.observaciones")
       List<String> getTiposHonorario(@Param("empresaId") Long empresaId);

       /* CONSULTAS PARA DASHBOARDS */

       /* Obtener honorarios recientes */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "ORDER BY h.fechaCreacion DESC")
       Page<Honorario> findHonorariosRecientes(@Param("empresaId") Long empresaId, Pageable pageable);

       /* Obtener honorarios próximos (basado en fecha) */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "AND h.fecha <= :fechaLimite " +
                     "ORDER BY h.fecha ASC")
       List<Honorario> findHonorariosProximosAVencer(@Param("empresaId") Long empresaId,
                     @Param("fechaLimite") LocalDate fechaLimite);

       /* ANÁLISIS COMPARATIVO */

       /**
        * Comparar honorarios entre períodos (usando campos reales de la BD)
        */
       @Query("SELECT " +
                     "SUM(CASE WHEN h.fecha BETWEEN :fechaDesde1 AND :fechaHasta1 THEN h.monto ELSE 0 END), " +
                     "SUM(CASE WHEN h.fecha BETWEEN :fechaDesde2 AND :fechaHasta2 THEN h.monto ELSE 0 END) " +
                     "FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId")
       List<Object[]> compararHonorariosPeriodos(@Param("empresaId") Long empresaId,
                     @Param("fechaDesde1") LocalDate fechaDesde1,
                     @Param("fechaHasta1") LocalDate fechaHasta1,
                     @Param("fechaDesde2") LocalDate fechaDesde2,
                     @Param("fechaHasta2") LocalDate fechaHasta2);

       /* Análisis de eficiencia por profesional */
       @Query("SELECT h.profesional.id, h.profesional.nombre, " +
                     "COUNT(h), SUM(h.monto), AVG(h.monto) " +
                     "FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId " +
                     "GROUP BY h.profesional.id, h.profesional.nombre " +
                     "ORDER BY AVG(h.monto) DESC")
       List<Object[]> getAnalisisEficienciaPorProfesional(@Param("empresaId") Long empresaId);

       /* MÉTRICAS AVANZADAS */

       /* Obtener métricas de gestión de honorarios */
       @Query("SELECT COUNT(h), SUM(h.monto), AVG(h.monto), 100.0, COUNT(DISTINCT h.profesional.id) " +
                     "FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId")
       List<Object[]> getMetricasGestionHonorarios(@Param("empresaId") Long empresaId);

       /* Tendencia de honorarios mensuales */
       @Query("SELECT MONTH(h.fecha), YEAR(h.fecha), " +
                     "COUNT(h), SUM(h.monto), AVG(h.monto) " +
                     "FROM Honorario h " +
                     "JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId " +
                     "AND h.fecha >= :fechaDesde " +
                     "GROUP BY MONTH(h.fecha), YEAR(h.fecha) " +
                     "ORDER BY YEAR(h.fecha), MONTH(h.fecha)")
       List<Object[]> getTendenciaHonorariosMensuales(@Param("empresaId") Long empresaId,
                     @Param("fechaDesde") LocalDate fechaDesde);

       /* CONSULTAS DE ALERTAS */

       /* Obtener honorarios que exceden límites */
       @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "AND h.monto > :limite " +
                     "ORDER BY h.monto DESC")
       List<Honorario> findHonorariosQueExcedenLimite(@Param("empresaId") Long empresaId,
                     @Param("limite") Double limite);

       /* Obtener obras con alto costo laboral */
       @Query("SELECT h.obra.id, h.obra.nombre, SUM(h.monto), COUNT(DISTINCT h.profesional.id) " +
                     "FROM Honorario h JOIN h.obra.cliente.empresas e " +
                     "WHERE e.id = :empresaId " +
                     "GROUP BY h.obra.id, h.obra.nombre " +
                     "HAVING SUM(h.monto) > :limite " +
                     "ORDER BY SUM(h.monto) DESC")
       List<Object[]> findObrasConAltoCostoLaboral(@Param("empresaId") Long empresaId, @Param("limite") Double limite);

       // Método para eliminación en cascada
       @Query("SELECT h FROM Honorario h WHERE h.obra.id = :obraId")
       List<Honorario> findByObra_Id(@Param("obraId") Long obraId);
}
