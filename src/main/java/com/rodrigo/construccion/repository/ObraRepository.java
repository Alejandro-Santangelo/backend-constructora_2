package com.rodrigo.construccion.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.rodrigo.construccion.model.entity.Obra;

/* Repositorio para la entidad Obra  
 * Todas las consultas incluyen filtrado por empresa (Multi-Tenant).
 */
@Repository
public interface ObraRepository extends JpaRepository<Obra, Long> {

  /* CONSULTAS MULTI-TENANT: Filtradas por empresa */

  /* Buscar obras por empresa (por empresa_id directo O a través del cliente) */
  @Query("SELECT o FROM Obra o LEFT JOIN o.cliente c LEFT JOIN c.empresas e WHERE o.empresaId = :empresaId OR e.id = :empresaId")
  List<Obra> findByEmpresaId(@Param("empresaId") Long empresaId);

  /* Buscar obras MANUALES por empresa (sin presupuesto previo) */
  @Query("SELECT o FROM Obra o JOIN o.cliente.empresas e WHERE e.id = :empresaId AND o.esObraManual = true")
  List<Obra> findObrasManualesByEmpresaId(@Param("empresaId") Long empresaId);

  /* Buscar obra por ID y empresa (incluye obras con cliente null como TAREA_LEVE) */
  @Query("SELECT o FROM Obra o LEFT JOIN o.cliente c LEFT JOIN c.empresas e WHERE o.id = :id AND (o.empresaId = :empresaId OR e.id = :empresaId)")
  Optional<Obra> findByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

  /* Buscar obras por cliente específico */
  List<Obra> findByCliente_Id(Long clienteId);

  /* Buscar todas las obras por estado (sin filtro de empresa) - NO USADA */
  List<Obra> findByEstado(String estado);

  /* Buscar todas las obras activas (estado EN_OBRA) en todo el sistema */
  @Query("SELECT o FROM Obra o WHERE o.estado = 'En obra'")
  List<Obra> findObrasActivas();

  /* Buscar obras manuales por estado (para borradores) */
  List<Obra> findByEsObraManualTrueAndEstado(String estado);



  /* ------------- * METODOS QUE NO SE ESTÁN USANDO EN NINGÚN LADO  * ------------------  */

  /* Buscar obras por empresa con paginación - NO USADA */
  @Query("SELECT o FROM Obra o JOIN o.cliente.empresas e WHERE e.id = :empresaId")
  Page<Obra> findByEmpresaId(@Param("empresaId") Long empresaId, Pageable pageable);

  /* Buscar obras por cliente y empresa - NO USADA */
  @Query("SELECT o FROM Obra o WHERE o.cliente.id = :clienteId AND :empresaId MEMBER OF o.cliente.empresas")
  List<Obra> findByClienteIdAndEmpresaId(@Param("clienteId") Long clienteId, @Param("empresaId") Long empresaId);

  /* CONSULTAS POR ESTADO */

  /* Buscar obras por estado en una empresa - NO USADA */
  @Query("SELECT o FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas AND o.estado = :estado")
  List<Obra> findByEmpresaIdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);

  /* Buscar obras por múltiples estados en una empresa */
  @Query("SELECT o FROM Obra o LEFT JOIN o.cliente c LEFT JOIN c.empresas e WHERE (o.empresaId = :empresaId OR e.id = :empresaId) AND o.estado IN :estados")
  List<Obra> findByEmpresaIdAndEstadoIn(@Param("empresaId") Long empresaId, @Param("estados") List<String> estados);

  /* Buscar obras activas (en obra) por empresa - NO USADA */
  @Query("SELECT o FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas AND o.estado = 'En obra'")
  List<Obra> findObrasActivasByEmpresaId(@Param("empresaId") Long empresaId);

  /* Buscar obras en planificación por empresa - NO USADA */
  @Query("SELECT o FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas AND o.estado = 'En planificación'")
  List<Obra> findObrasEnPlanificacionByEmpresaId(@Param("empresaId") Long empresaId);

  /* CONSULTAS POR FECHAS */

  /* Buscar obras por rango de fechas de inicio en una empresa - NO USADA */
  @Query("SELECT o FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas AND o.fechaInicio BETWEEN :fechaDesde AND :fechaHasta")
  List<Obra> findByEmpresaIdAndFechaInicioBetween(@Param("empresaId") Long empresaId,
      @Param("fechaDesde") LocalDate fechaDesde,
      @Param("fechaHasta") LocalDate fechaHasta);

  /* CONSULTAS COMPLEJAS CON RESÚMENES FINANCIEROS */

  /* TODO: ADAPTAR - usa o.profesionalesAsignados que ya no existe en Obra
  @Query("SELECT o.id as obraId, o.nombre as obraNombre, o.estado, o.fechaInicio, o.fechaFin, " +
      "COALESCE(SUM(j.horasTrabajadas * j.valorHora), 0) as totalJornales, " +
      "COALESCE(SUM(h.monto), 0) as totalHonorarios, " +
      "COALESCE(SUM(c.monto), 0) as totalCostos, " +
      "COALESCE((SELECT SUM(CASE WHEN mm2.tipoMovimiento = 'Salida' THEN mm2.cantidad * mm2.precioUnitario ELSE 0 END) FROM MovimientoMaterial mm2 WHERE mm2.obraId = o.id), 0) as totalMateriales, "
      +
      "COALESCE(SUM(p.montoTotal), 0) as totalPresupuestos, " +
      "COALESCE(SUM(f.importe), 0) as totalFacturas " +
      "FROM Obra o " +
      "LEFT JOIN o.profesionalesAsignados po " +
      "LEFT JOIN po.jornales j " +
      "LEFT JOIN o.honorarios h " +
      "LEFT JOIN o.costos c " +
      "LEFT JOIN o.presupuestos p " +
      "LEFT JOIN p.facturas f " +
      "WHERE :empresaId MEMBER OF o.cliente.empresas " +
      "GROUP BY o.id, o.nombre, o.estado, o.fechaInicio, o.fechaFin " +
      "ORDER BY o.fechaInicio DESC")
  List<Object[]> findResumenGastosPorEmpresa(@Param("empresaId") Long empresaId);
  */

  /* TODO: ADAPTAR - usa o.profesionalesAsignados que ya no existe en Obra
  @Query("SELECT DISTINCT o FROM Obra o " +
      "JOIN o.profesionalesAsignados po " +
      "WHERE :empresaId MEMBER OF o.cliente.empresas AND po.activo = true " +
      "ORDER BY o.nombre")
  List<Obra> findObrasConProfesionalesAsignados(@Param("empresaId") Long empresaId);
  */

  /* Buscar obras por nombre (búsqueda parcial) en una empresa - NO USADA */
  @Query("SELECT o FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas AND LOWER(o.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
  Page<Obra> findByEmpresaIdAndNombreContainingIgnoreCase(@Param("empresaId") Long empresaId,
      @Param("nombre") String nombre,
      Pageable pageable);

  /* Contar obras por estado en una empresa - NO USADA */
  @Query("SELECT o.estado, COUNT(o) FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas GROUP BY o.estado")
  List<Object[]> countObrasPorEstado(@Param("empresaId") Long empresaId);

  /* Obtener estadísticas generales de obras por empresa - NO USADA */
  @Query("SELECT COUNT(o) as totalObras, " +
      "COUNT(CASE WHEN o.estado = 'En obra' THEN 1 END) as obrasActivas, " +
      "COUNT(CASE WHEN o.estado = 'Finalizada' THEN 1 END) as obrasFinalizadas, " +
      "COUNT(CASE WHEN o.estado = 'En planificación' THEN 1 END) as obrasEnPlanificacion, " +
      "COALESCE(SUM(o.presupuestoEstimado), 0) as totalPresupuestado " +
      "FROM Obra o WHERE :empresaId MEMBER OF o.cliente.empresas")
  Object[] getEstadisticasObras(@Param("empresaId") Long empresaId);

  /* Buscar obra por empresa y dirección (para aprobar presupuesto) */
  @Query("SELECT o FROM Obra o JOIN o.cliente.empresas e " +
      "WHERE e.id = :empresaId " +
      "AND LOWER(o.direccionObraCalle) = LOWER(:calle) " +
      "AND o.direccionObraAltura = :altura")
  Optional<Obra> findByEmpresaIdAndDireccion(
      @Param("empresaId") Long empresaId,
      @Param("calle") String calle,
      @Param("altura") Integer altura
  );

  /* Verificar si existe una obra por ID y empresa (incluye obras con cliente null como TAREA_LEVE) */
  @Query("SELECT CASE WHEN COUNT(o) > 0 THEN true ELSE false END FROM Obra o LEFT JOIN o.cliente c LEFT JOIN c.empresas e WHERE o.id = :id AND (o.empresaId = :empresaId OR e.id = :empresaId)")
  boolean existsByIdAndEmpresaId(@Param("id") Long id, @Param("empresaId") Long empresaId);

  /* Buscar obra creada a partir de un presupuesto específico (para TAREA_LEVE) */
  @Query("SELECT o FROM Obra o WHERE o.presupuestoOriginalId = :presupuestoId")
  Optional<Obra> findByPresupuestoOriginalId(@Param("presupuestoId") Long presupuestoId);

  /**
   * Actualiza el campo presupuesto_no_cliente_id de una obra específica
   * Usado para mantener sincronizado el vínculo con el presupuesto de mayor versión
   * DESACTIVADO: Causa cambio de estado automático a "MODIFICADO"
   */
  /*
  @Modifying
  @Query("UPDATE Obra o SET o.presupuestoNoClienteId = :presupuestoId WHERE o.id = :obraId")
  int updatePresupuestoNoClienteId(@Param("obraId") Long obraId, @Param("presupuestoId") Long presupuestoId);
  */
}