package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Costo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface CostoRepository extends JpaRepository<Costo, Long> {

    // Consultas básicas por empresa (a través de obra->cliente->empresa)
    @Query("SELECT c FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas")
    Page<Costo> findByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId, Pageable pageable);

    @Query("SELECT c FROM Costo c WHERE c.id = :id AND :empresaId MEMBER OF c.obra.cliente.empresas")
    Optional<Costo> findByIdAndObra_Cliente_Empresa_Id(@Param("id") Long id, @Param("empresaId") Long empresaId);

    // Consultas por obra
    @Query("SELECT c FROM Costo c WHERE c.obra.id = :obraId AND :empresaId MEMBER OF c.obra.cliente.empresas")
    Page<Costo> findByObra_IdAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId, Pageable pageable);

    // Consultas por fecha
    @Query("SELECT c FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND c.fecha BETWEEN :fechaDesde AND :fechaHasta")
    Page<Costo> findByObra_Cliente_Empresa_IdAndFechaBetween(@Param("empresaId") Long empresaId, @Param("fechaDesde") LocalDate fechaDesde, @Param("fechaHasta") LocalDate fechaHasta, Pageable pageable);

    // Búsquedas de texto
    @Query("SELECT c FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas " +
        "AND (LOWER(c.concepto) LIKE LOWER(CONCAT('%', :texto, '%')) OR LOWER(c.descripcion) LIKE LOWER(CONCAT('%', :texto, '%')))")
    Page<Costo> buscarPorTexto(@Param("empresaId") Long empresaId, @Param("texto") String texto, Pageable pageable);

    // Filtros avanzados
    @Query("SELECT c FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas " +
        "AND (:obraId IS NULL OR c.obra.id = :obraId) " +
        "AND (:categoria IS NULL OR c.categoria = :categoria) " +
        "AND (:tipoCosto IS NULL OR c.tipoCosto = :tipoCosto) " +
        "AND (:montoMinimo IS NULL OR c.monto >= :montoMinimo) " +
        "AND (:montoMaximo IS NULL OR c.monto <= :montoMaximo)")
    Page<Costo> filtrarCostos(@Param("empresaId") Long empresaId,
                 @Param("obraId") Long obraId,
                 @Param("categoria") String categoria,
                 @Param("tipoCosto") String tipoCosto,
                 @Param("montoMinimo") BigDecimal montoMinimo,
                 @Param("montoMaximo") BigDecimal montoMaximo,
                 Pageable pageable);

    // Agregaciones - Sumas de montos
    @Query("SELECT SUM(c.monto) FROM Costo c WHERE c.obra.id = :obraId AND :empresaId MEMBER OF c.obra.cliente.empresas")
    BigDecimal sumMontoByObra_IdAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    @Query("SELECT SUM(c.monto) FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas")
    BigDecimal sumMontoByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId);

    @Query("SELECT SUM(c.monto) FROM Costo c WHERE c.obra.id = :obraId AND c.estado = :estado AND :empresaId MEMBER OF c.obra.cliente.empresas")
    BigDecimal sumMontoByObra_IdAndEstadoAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId, @Param("estado") String estado, @Param("empresaId") Long empresaId);

    @Query("SELECT SUM(c.monto) FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND c.estado = :estado")
    BigDecimal sumMontoByObra_Cliente_Empresa_IdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);

    // Conteos
    @Query("SELECT COUNT(c) FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas")
    long countByObra_Cliente_Empresa_Id(@Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(c) FROM Costo c WHERE c.obra.id = :obraId AND :empresaId MEMBER OF c.obra.cliente.empresas")
    long countByObra_IdAndObra_Cliente_Empresa_Id(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    @Query("SELECT COUNT(c) FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND c.estado = :estado")
    long countByObra_Cliente_Empresa_IdAndEstado(@Param("empresaId") Long empresaId, @Param("estado") String estado);

    // Listas de categorías y tipos
    @Query("SELECT DISTINCT c.categoria FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND c.categoria IS NOT NULL ORDER BY c.categoria")
    List<String> getCategorias(@Param("empresaId") Long empresaId);

    @Query("SELECT DISTINCT c.tipoCosto FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND c.tipoCosto IS NOT NULL ORDER BY c.tipoCosto")
    List<String> getTipos(@Param("empresaId") Long empresaId);

    // Consultas para análisis y reportes (retornando Object[] para simplificar)
    @Query("SELECT c.categoria, SUM(c.monto), COUNT(c) FROM Costo c WHERE c.obra.id = :obraId AND :empresaId MEMBER OF c.obra.cliente.empresas GROUP BY c.categoria")
    List<Object[]> getDistribucionCostosCategoria(@Param("obraId") Long obraId, @Param("empresaId") Long empresaId);

    @Query("SELECT c.categoria, SUM(c.monto), COUNT(c) FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas GROUP BY c.categoria ORDER BY SUM(c.monto) DESC")
    List<Object[]> getEstadisticasPorCategoria(@Param("empresaId") Long empresaId);

    @Query("SELECT o.nombre, SUM(c.monto), COUNT(c) FROM Costo c JOIN c.obra o WHERE :empresaId MEMBER OF c.obra.cliente.empresas GROUP BY o.id, o.nombre ORDER BY SUM(c.monto) DESC")
    List<Object[]> getEstadisticasPorObra(@Param("empresaId") Long empresaId);

    @Query("SELECT YEAR(c.fecha), MONTH(c.fecha), SUM(c.monto), COUNT(c) FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas GROUP BY YEAR(c.fecha), MONTH(c.fecha) ORDER BY YEAR(c.fecha), MONTH(c.fecha)")
    List<Object[]> getEstadisticasMensuales(@Param("empresaId") Long empresaId);

    @Query("SELECT o.nombre, SUM(c.monto), o.presupuestoEstimado FROM Costo c JOIN c.obra o WHERE :empresaId MEMBER OF c.obra.cliente.empresas GROUP BY o.id, o.nombre, o.presupuestoEstimado ORDER BY o.nombre")
    List<Object[]> getReporteResumenObras(@Param("empresaId") Long empresaId);

    @Query("SELECT o.nombre, SUM(c.monto), o.presupuestoEstimado, (SUM(c.monto) - o.presupuestoEstimado) as variacion FROM Costo c JOIN c.obra o WHERE :empresaId MEMBER OF c.obra.cliente.empresas GROUP BY o.id, o.nombre, o.presupuestoEstimado HAVING ABS(SUM(c.monto) - o.presupuestoEstimado) > 0 ORDER BY ABS(SUM(c.monto) - o.presupuestoEstimado) DESC")
    List<Object[]> getReporteVariacionesPresupuesto(@Param("empresaId") Long empresaId);

    @Query("SELECT c.concepto, c.monto, o.nombre, c.fecha FROM Costo c JOIN c.obra o WHERE :empresaId MEMBER OF c.obra.cliente.empresas ORDER BY c.monto DESC")
    List<Object[]> getTopCostos(@Param("empresaId") Long empresaId, Pageable pageable);

    // Método de conveniencia para top costos
    default List<Object[]> getTopCostos(Long empresaId, int limite) {
        return getTopCostos(empresaId, Pageable.ofSize(limite));
    }

    // Validación de existencia

    @Query("SELECT c FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND LOWER(FUNCTION('unaccent', c.tipoCosto)) = LOWER(FUNCTION('unaccent', :tipo))")
    Page<Costo> buscarPorTipoFlexible(@Param("empresaId") Long empresaId, @Param("tipo") String tipo, Pageable pageable);

    @Query("SELECT c FROM Costo c WHERE :empresaId MEMBER OF c.obra.cliente.empresas AND LOWER(FUNCTION('unaccent', c.categoria)) = LOWER(FUNCTION('unaccent', :categoria))")
    Page<Costo> buscarPorCategoriaFlexible(@Param("empresaId") Long empresaId, @Param("categoria") String categoria, Pageable pageable);

}