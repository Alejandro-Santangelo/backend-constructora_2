package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.CobroObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CobroObraRepository extends JpaRepository<CobroObra, Long> {

    /**
     * Buscar todos los cobros de una obra específica
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.obra.id = :obraId " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<CobroObra> findByObraId(@Param("obraId") Long obraId);

    /**
     * Buscar cobros por obra y estado
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.obra.id = :obraId AND c.estado = :estado " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<CobroObra> findByObraIdAndEstado(@Param("obraId") Long obraId, @Param("estado") String estado);

    /**
     * Buscar cobros por presupuesto
     */
    @Query("SELECT c FROM CobroObra c WHERE c.presupuestoNoCliente.id = :presupuestoId")
    List<CobroObra> findByPresupuestoNoClienteId(@Param("presupuestoId") Long presupuestoId);

    /**
     * Buscar cobros por empresa
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.empresaId = :empresaId " +
           "AND (c.obra IS NULL OR c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<CobroObra> findByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Buscar cobros pendientes de una obra
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.obra.id = :obraId AND c.estado = 'PENDIENTE' " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<CobroObra> findCobrosPendientesByObra(@Param("obraId") Long obraId);

    /**
     * Buscar cobros vencidos
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.estado = 'PENDIENTE' " +
           "AND c.fechaVencimiento IS NOT NULL " +
           "AND c.fechaVencimiento < :fecha " +
           "AND (c.obra IS NULL OR c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<CobroObra> findCobrosVencidos(@Param("fecha") LocalDate fecha);

    /**
     * Calcular total cobrado de una obra (solo cobros en estado COBRADO)
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CobroObra c " +
           "WHERE c.obra.id = :obraId AND c.estado = 'COBRADO' " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    BigDecimal calcularTotalCobradoByObra(@Param("obraId") Long obraId);

    /**
     * Calcular total pendiente de cobro de una obra
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CobroObra c " +
           "WHERE c.obra.id = :obraId AND c.estado = 'PENDIENTE' " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    BigDecimal calcularTotalPendienteByObra(@Param("obraId") Long obraId);

    /**
     * Buscar cobros por rango de fechas
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.fechaCobro BETWEEN :desde AND :hasta " +
           "AND (c.obra IS NULL OR c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<CobroObra> findByFechaCobroBetween(@Param("desde") LocalDate desde, @Param("hasta") LocalDate hasta);

    /**
     * Buscar cobros por obra y rango de fechas
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.obra.id = :obraId " +
           "AND c.fechaCobro BETWEEN :desde AND :hasta " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    List<CobroObra> findByObraAndFechaCobroBetween(
            @Param("obraId") Long obraId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta);

    /**
     * Contar cobros pendientes de una obra
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT COUNT(c) FROM CobroObra c WHERE c.obra.id = :obraId AND c.estado = 'PENDIENTE' " +
           "AND c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO')")
    Long countCobrosPendientesByObra(@Param("obraId") Long obraId);

    /**
     * Buscar cobros por método de pago
     * EXCLUYE obras en estado SUSPENDIDA o CANCELADO
     */
    @Query("SELECT c FROM CobroObra c WHERE c.metodoPago = :metodoPago " +
           "AND (c.obra IS NULL OR c.obra.estado NOT IN ('SUSPENDIDA', 'CANCELADO'))")
    List<CobroObra> findByMetodoPago(@Param("metodoPago") String metodoPago);

    /**
     * Obtener último cobro de una obra
     */
    @Query("SELECT c FROM CobroObra c WHERE c.obra.id = :obraId " +
           "ORDER BY c.fechaCobro DESC, c.id DESC")
    List<CobroObra> findUltimoCobroByObra(@Param("obraId") Long obraId);

    /**
     * Verificar si existe algún cobro para una obra
     */
    @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CobroObra c WHERE c.obra.id = :obraId")
    boolean existsByObraId(@Param("obraId") Long obraId);

    // ========== BÚSQUEDA POR DIRECCIÓN ==========

    /**
     * Buscar cobros por dirección exacta (todos los campos)
     * Comparación NULL-safe para barrio, torre, piso, depto
     */
    @Query("SELECT c FROM CobroObra c WHERE c.presupuestoNoCliente.id = :presupuestoId " +
           "AND c.calle = :calle " +
           "AND c.altura = :altura " +
           "AND ((:barrio IS NULL AND c.barrio IS NULL) OR c.barrio = :barrio) " +
           "AND ((:torre IS NULL AND c.torre IS NULL) OR c.torre = :torre) " +
           "AND ((:piso IS NULL AND c.piso IS NULL) OR c.piso = :piso) " +
           "AND ((:depto IS NULL AND c.depto IS NULL) OR c.depto = :depto) " +
           "ORDER BY c.fechaEmision DESC")
    List<CobroObra> findByDireccionCompleta(
        @Param("presupuestoId") Long presupuestoId,
        @Param("calle") String calle,
        @Param("altura") String altura,
        @Param("barrio") String barrio,
        @Param("torre") String torre,
        @Param("piso") String piso,
        @Param("depto") String depto
    );

    /**
     * Buscar cobros pendientes por dirección
     */
    @Query("SELECT c FROM CobroObra c WHERE c.presupuestoNoCliente.id = :presupuestoId " +
           "AND c.calle = :calle " +
           "AND c.altura = :altura " +
           "AND ((:barrio IS NULL AND c.barrio IS NULL) OR c.barrio = :barrio) " +
           "AND ((:torre IS NULL AND c.torre IS NULL) OR c.torre = :torre) " +
           "AND ((:piso IS NULL AND c.piso IS NULL) OR c.piso = :piso) " +
           "AND ((:depto IS NULL AND c.depto IS NULL) OR c.depto = :depto) " +
           "AND c.estado = 'PENDIENTE' " +
           "ORDER BY c.fechaEmision DESC")
    List<CobroObra> findCobrosPendientesByDireccion(
        @Param("presupuestoId") Long presupuestoId,
        @Param("calle") String calle,
        @Param("altura") String altura,
        @Param("barrio") String barrio,
        @Param("torre") String torre,
        @Param("piso") String piso,
        @Param("depto") String depto
    );

    /**
     * Calcular total cobrado por dirección
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CobroObra c " +
           "WHERE c.presupuestoNoCliente.id = :presupuestoId " +
           "AND c.calle = :calle " +
           "AND c.altura = :altura " +
           "AND ((:barrio IS NULL AND c.barrio IS NULL) OR c.barrio = :barrio) " +
           "AND ((:torre IS NULL AND c.torre IS NULL) OR c.torre = :torre) " +
           "AND ((:piso IS NULL AND c.piso IS NULL) OR c.piso = :piso) " +
           "AND ((:depto IS NULL AND c.depto IS NULL) OR c.depto = :depto) " +
           "AND c.estado = 'COBRADO'")
    BigDecimal calcularTotalCobradoByDireccion(
        @Param("presupuestoId") Long presupuestoId,
        @Param("calle") String calle,
        @Param("altura") String altura,
        @Param("barrio") String barrio,
        @Param("torre") String torre,
        @Param("piso") String piso,
        @Param("depto") String depto
    );

    /**
     * Calcular total pendiente por dirección
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CobroObra c " +
           "WHERE c.presupuestoNoCliente.id = :presupuestoId " +
           "AND c.calle = :calle " +
           "AND c.altura = :altura " +
           "AND ((:barrio IS NULL AND c.barrio IS NULL) OR c.barrio = :barrio) " +
           "AND ((:torre IS NULL AND c.torre IS NULL) OR c.torre = :torre) " +
           "AND ((:piso IS NULL AND c.piso IS NULL) OR c.piso = :piso) " +
           "AND ((:depto IS NULL AND c.depto IS NULL) OR c.depto = :depto) " +
           "AND c.estado = 'PENDIENTE'")
    BigDecimal calcularTotalPendienteByDireccion(
        @Param("presupuestoId") Long presupuestoId,
        @Param("calle") String calle,
        @Param("altura") String altura,
        @Param("barrio") String barrio,
        @Param("torre") String torre,
        @Param("piso") String piso,
        @Param("depto") String depto
    );

    /**
     * NUEVO: Calcular suma total de cobros por empresa y estado
     * CRÍTICO para cálculo de saldo disponible en retiros personales
     */
    @Query("SELECT COALESCE(SUM(c.monto), 0) FROM CobroObra c " +
           "WHERE c.empresaId = :empresaId AND c.estado = :estado")
    BigDecimal sumMontoByEmpresaIdAndEstado(
        @Param("empresaId") Long empresaId,
        @Param("estado") String estado
    );
}
