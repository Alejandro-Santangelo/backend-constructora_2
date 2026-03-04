package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PagoAdelantoAplicado;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestión de adelantos aplicados a pagos.
 * Implementación 100% relacional.
 */
@Repository
public interface PagoAdelantoAplicadoRepository extends JpaRepository<PagoAdelantoAplicado, Long> {

    /**
     * Obtener todos los adelantos que fueron aplicados en un pago específico
     */
    @Query("SELECT paa FROM PagoAdelantoAplicado paa WHERE paa.pago.id = :pagoId")
    List<PagoAdelantoAplicado> findByPagoId(@Param("pagoId") Long pagoId);

    /**
     * Obtener todos los pagos donde se aplicó un adelanto específico
     */
    @Query("SELECT paa FROM PagoAdelantoAplicado paa WHERE paa.adelanto.id = :adelantoId")
    List<PagoAdelantoAplicado> findByAdelantoId(@Param("adelantoId") Long adelantoId);

    /**
     * Verificar si un adelanto ya fue aplicado en un pago
     */
    boolean existsByPagoIdAndAdelantoId(Long pagoId, Long adelantoId);

    /**
     * Eliminar todas las relaciones de un pago específico
     */
    void deleteByPagoId(Long pagoId);
}
