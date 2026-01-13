package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Honorario;

import java.math.BigDecimal;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

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

    /* Contar honorarios por empresa */
    @Query("SELECT COUNT(h) FROM Honorario h JOIN h.obra.cliente.empresas e WHERE e.id = :empresaId")
    long countByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId);

    /* Búsqueda avanzada y dinámica de honorarios */
    @Query("SELECT h FROM Honorario h JOIN h.obra.cliente.empresas e " +
            "WHERE e.id = :empresaId " +
            "AND (:fechaInicio IS NULL OR h.fecha >= :fechaInicio) " +
            "AND (:fechaFin IS NULL OR h.fecha <= :fechaFin) " +
            "AND (:montoMinimo IS NULL OR h.monto >= :montoMinimo)")
    List<Honorario> busquedaAvanzada(@Param("empresaId") Long empresaId,
                                     @Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin,
                                     @Param("montoMinimo") BigDecimal montoMinimo);

}
