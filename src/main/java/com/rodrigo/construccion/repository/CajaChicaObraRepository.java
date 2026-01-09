package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.CajaChicaObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface CajaChicaObraRepository extends JpaRepository<CajaChicaObra, Long> {

    /**
     * Obtener todas las asignaciones de caja chica de una obra
     */
    List<CajaChicaObra> findByPresupuestoNoClienteId(Long presupuestoNoClienteId);

    /**
     * Obtener todas las asignaciones de caja chica de un profesional
     */
    List<CajaChicaObra> findByProfesionalObraId(Long profesionalObraId);

    /**
     * Obtener caja chica por profesional y estado
     */
    List<CajaChicaObra> findByProfesionalObraIdAndEstado(Long profesionalObraId, String estado);

    /**
     * Obtener caja chica por obra y estado
     */
    List<CajaChicaObra> findByPresupuestoNoClienteIdAndEstado(Long presupuestoNoClienteId, String estado);

    /**
     * Calcular total de caja chica activa por profesional
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CajaChicaObra c " +
           "WHERE c.profesionalObraId = :profesionalObraId " +
           "AND c.estado = 'ACTIVO'")
    BigDecimal calcularTotalActivoPorProfesional(@Param("profesionalObraId") Long profesionalObraId);

    /**
     * Calcular total de caja chica por obra y estado
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CajaChicaObra c " +
           "WHERE c.presupuestoNoClienteId = :presupuestoId " +
           "AND c.estado = :estado")
    BigDecimal calcularTotalPorObraYEstado(
        @Param("presupuestoId") Long presupuestoId,
        @Param("estado") String estado
    );
}
