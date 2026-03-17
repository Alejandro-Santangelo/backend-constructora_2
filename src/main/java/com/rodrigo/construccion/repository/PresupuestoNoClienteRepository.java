package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.enums.PresupuestoEstado;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PresupuestoNoClienteRepository extends JpaRepository<PresupuestoNoCliente, Long>, JpaSpecificationExecutor<PresupuestoNoCliente> {

    /* Carga un PresupuestoNoCliente por ID
     * Los datos de itemsCalculadora están en formato JSON (no requieren EntityGraph)
     */
    Optional<PresupuestoNoCliente> findById(Long id);

    /* Lista todos los presupuestos */
    @Override
    List<PresupuestoNoCliente> findAll();

    /**
     * Lista todos los presupuestos asociados a una obra específica
     * Ordenados por versión descendente (versión más reciente primero)
     */
    List<PresupuestoNoCliente> findByObra_IdOrderByNumeroVersionDesc(Long obraId);
    
    /**
     * Lista todos los presupuestos de una empresa específica
     * ⚠️ FILTRO EXPLÍCITO: No confía en HibernateFilterInterceptor, filtra directamente por empresaId
     */
    List<PresupuestoNoCliente> findByEmpresaId(Long empresaId);

    @Query("select max(p.numeroPresupuesto) from PresupuestoNoCliente p")
    Long findMaxNumeroPresupuesto();

    @Query("select max(p.numeroVersion) from PresupuestoNoCliente p where p.numeroPresupuesto = :numero")
    Integer findMaxNumeroVersionByNumero(@Param("numero") Long numero);

    @Query("SELECT p FROM PresupuestoNoCliente p WHERE " +
            "p.direccionObraCalle = :calle AND " +
            "p.direccionObraAltura = :altura AND " +
            "(:piso IS NULL OR p.direccionObraPiso = :piso) AND " +
            "(:departamento IS NULL OR p.direccionObraDepartamento = :departamento) " +
            "ORDER BY p.numeroVersion DESC")
    List<PresupuestoNoCliente> findByDireccionObra(@Param("calle") String calle, @Param("altura") String altura, @Param("piso") String piso, @Param("departamento") String departamento);

    /**
     * Busca presupuestos por estado
     * Usado por el proceso automático de actualización de estados
     */
    List<PresupuestoNoCliente> findByEstado(PresupuestoEstado estado);

    /**
     * Busca presupuestos por obra_id y estado (String)
     * Usado para obtener el presupuesto compartido de un rubro en una obra
     */
    @Query("SELECT p FROM PresupuestoNoCliente p WHERE p.obra.id = :obraId AND p.estado = :estado ORDER BY p.numeroVersion DESC")
    List<PresupuestoNoCliente> findByObraIdAndEstado(@Param("obraId") Long obraId, @Param("estado") String estado);

    /**
     * Busca presupuestos por estado y fecha probable de inicio menor o igual a la fecha especificada
     * Usado para cambiar APROBADOS a EN_EJECUCION cuando llega la fecha de inicio
     */
    @Query("SELECT p FROM PresupuestoNoCliente p WHERE p.estado = :estado AND p.fechaProbableInicio IS NOT NULL AND p.fechaProbableInicio <= :fecha")
    List<PresupuestoNoCliente> findByEstadoAndFechaProbableInicioLessThanEqual(
            @Param("estado") PresupuestoEstado estado,
            @Param("fecha") java.time.LocalDate fecha
    );

}