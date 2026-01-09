package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PedidoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repositorio para la entidad PedidoPago
 * 
 * Maneja las consultas de pedidos de pago por empresa, proveedor y obra.
 */
@Repository
public interface PedidoPagoRepository extends JpaRepository<PedidoPago, Long> {

    /**
     * CONSULTAS POR RELACIONES
     */
    
    /**
     * Buscar pedidos por proveedor
     */
    List<PedidoPago> findByProveedor_Id(Long proveedorId);
    
    /**
     * Buscar pedidos por obra
     */
    List<PedidoPago> findByObra_Id(Long obraId);
    
    /**
     * Buscar pedidos por empresa
     */
    List<PedidoPago> findByEmpresa_Id(Long empresaId);

    /**
     * CONSULTAS POR ESTADO
     */
    
    /**
     * Buscar pedidos por estado
     */
    List<PedidoPago> findByEstado(String estado);
    
    /**
     * Buscar pedidos por estado y empresa
     */
    List<PedidoPago> findByEstadoAndEmpresa_Id(String estado, Long empresaId);

    /**
     * CONSULTAS POR FECHAS
     */
    
    /**
     * Buscar pedidos entre fechas
     */
    List<PedidoPago> findByFechaPedidoBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    /**
     * Buscar pedidos por fecha y empresa
     */
    List<PedidoPago> findByFechaPedidoBetweenAndEmpresa_Id(LocalDate fechaInicio, LocalDate fechaFin, Long empresaId);

    /**
     * CONSULTAS COMBINADAS
     */
    
    /**
     * Buscar pedidos por proveedor y estado
     */
    List<PedidoPago> findByProveedor_IdAndEstado(Long proveedorId, String estado);
    
    /**
     * Buscar pedidos por obra y estado
     */
    List<PedidoPago> findByObra_IdAndEstado(Long obraId, String estado);

    /**
     * CONSULTAS COMPLEJAS CON @Query
     */
    
    /**
     * Obtener pedidos pendientes de aprobación por empresa
     */
    @Query("SELECT pp FROM PedidoPago pp " +
           "WHERE pp.empresa.id = :empresaId " +
           "AND pp.estado = 'PENDIENTE' " +
           "ORDER BY pp.fechaPedido ASC")
    List<PedidoPago> findPendientesAprobacionByEmpresa(@Param("empresaId") Long empresaId);
    
    /**
     * Obtener pedidos por vencer
     */
    @Query("SELECT pp FROM PedidoPago pp " +
           "WHERE pp.empresa.id = :empresaId " +
           "AND pp.fechaVencimiento <= :fechaLimite " +
           "AND pp.estado IN ('PENDIENTE', 'APROBADO', 'AUTORIZADO') " +
           "ORDER BY pp.fechaVencimiento ASC")
    List<PedidoPago> findPorVencerByEmpresa(@Param("empresaId") Long empresaId, 
                                           @Param("fechaLimite") LocalDate fechaLimite);
    
    /**
     * Obtener resumen de importes por estado
     */
    @Query("SELECT pp.estado, SUM(pp.importe), COUNT(pp) " +
           "FROM PedidoPago pp " +
           "WHERE pp.empresa.id = :empresaId " +
           "GROUP BY pp.estado")
    List<Object[]> findResumenImportesPorEstado(@Param("empresaId") Long empresaId);
    
    /**
     * Obtener pedidos de un proveedor en un período
     */
    @Query("SELECT pp FROM PedidoPago pp " +
           "WHERE pp.proveedor.id = :proveedorId " +
           "AND pp.empresa.id = :empresaId " +
           "AND pp.fechaPedido BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY pp.fechaPedido DESC")
    List<PedidoPago> findByProveedorAndPeriodo(@Param("proveedorId") Long proveedorId,
                                               @Param("empresaId") Long empresaId,
                                               @Param("fechaInicio") LocalDate fechaInicio,
                                               @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Obtener actividad reciente de pedidos
     */
    @Query("SELECT 'Pedido' as tipo, pp.numeroPedido, pp.fechaModificacion as fecha, pp.concepto " +
           "FROM PedidoPago pp " +
           "WHERE pp.empresa.id = :empresaId " +
           "ORDER BY pp.fechaModificacion DESC")
    List<Object[]> findActividadRecientePedidos(@Param("empresaId") Long empresaId);
    
    /**
     * Verificar existencia de pedidos activos por proveedor
     */
    @Query("SELECT COUNT(pp) > 0 FROM PedidoPago pp " +
           "WHERE pp.proveedor.id = :proveedorId " +
           "AND pp.empresa.id = :empresaId " +
           "AND pp.estado NOT IN ('PAGADO', 'CANCELADO', 'RECHAZADO')")
    boolean existenPedidosActivosByProveedor(@Param("proveedorId") Long proveedorId, 
                                            @Param("empresaId") Long empresaId);
    
    /**
     * Obtener total de importes pendientes por empresa
     */
    @Query("SELECT SUM(pp.importe) FROM PedidoPago pp " +
           "WHERE pp.empresa.id = :empresaId " +
           "AND pp.estado IN ('PENDIENTE', 'APROBADO', 'AUTORIZADO')")
    Double getTotalImportesPendientes(@Param("empresaId") Long empresaId);
}