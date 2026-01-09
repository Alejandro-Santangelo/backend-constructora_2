package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para gestión de gastos generales de items de calculadora.
 * Implementa operaciones CRUD específicas para gastos generales relacionales.
 */
@Repository
public interface PresupuestoGastoGeneralRepository extends JpaRepository<PresupuestoGastoGeneral, Long> {

    /**
     * Encuentra todos los gastos generales de un item específico.
     * Ordena por el campo 'orden' para mantener la secuencia de ingreso.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return Lista de gastos generales ordenados
     */
    @Query("SELECT g FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId " +
           "ORDER BY g.orden ASC")
    List<PresupuestoGastoGeneral> findByItemCalculadoraIdOrderByOrden(@Param("itemCalculadoraId") Long itemCalculadoraId);

    /**
     * Encuentra todos los gastos generales de un item específico sin ordenar.
     * Útil para operaciones de merge/upsert.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return Lista de gastos generales
     */
    List<PresupuestoGastoGeneral> findByItemCalculadoraId(Long itemCalculadoraId);

    /**
     * Encuentra todos los gastos generales de un item con filtro por empresa.
     * Útil para validación multi-tenant explícita.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @param empresaId ID de la empresa (multi-tenant)
     * @return Lista de gastos generales ordenados
     */
    @Query("SELECT g FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId " +
           "AND g.empresa.id = :empresaId " +
           "ORDER BY g.orden ASC")
    List<PresupuestoGastoGeneral> findByItemCalculadoraIdAndEmpresaIdOrderByOrden(
        @Param("itemCalculadoraId") Long itemCalculadoraId, 
        @Param("empresaId") Long empresaId);

    /**
     * Cuenta la cantidad de gastos generales de un item específico.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return Cantidad de gastos generales
     */
    @Query("SELECT COUNT(g) FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId")
    Long countByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);

    /**
     * Calcula la suma total de subtotales de gastos generales de un item.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return Suma total de subtotales (puede ser null si no hay gastos)
     */
    @Query("SELECT COALESCE(SUM(g.subtotal), 0.0) FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId")
    Double sumSubtotalByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);

    /**
     * Elimina todos los gastos generales de un item específico.
     * Operación en cascada para actualización completa de items.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return Cantidad de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId")
    int deleteByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);

    /**
     * Elimina todos los gastos generales de un item con validación de empresa.
     * Operación segura multi-tenant.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @param empresaId ID de la empresa (multi-tenant)
     * @return Cantidad de registros eliminados
     */
    @Modifying
    @Query("DELETE FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId " +
           "AND g.empresa.id = :empresaId")
    int deleteByItemCalculadoraIdAndEmpresaId(
        @Param("itemCalculadoraId") Long itemCalculadoraId, 
        @Param("empresaId") Long empresaId);

    /**
     * Encuentra el orden máximo actual para un item específico.
     * Útil para asignar nuevos números de orden secuenciales.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return Orden máximo actual (null si no hay gastos)
     */
    @Query("SELECT MAX(g.orden) FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId")
    Integer findMaxOrdenByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);

    /**
     * Busca gastos generales por descripción (búsqueda parcial case-insensitive).
     * Útil para funciones de búsqueda y autocompletado.
     * 
     * @param descripcion Texto a buscar en las descripciones
     * @param empresaId ID de la empresa (multi-tenant)
     * @return Lista de gastos que coinciden con la búsqueda
     */
    @Query("SELECT g FROM PresupuestoGastoGeneral g " +
           "WHERE LOWER(g.descripcion) LIKE LOWER(CONCAT('%', :descripcion, '%')) " +
           "AND g.empresa.id = :empresaId " +
           "ORDER BY g.descripcion ASC")
    List<PresupuestoGastoGeneral> findByDescripcionContainingIgnoreCaseAndEmpresaId(
        @Param("descripcion") String descripcion, 
        @Param("empresaId") Long empresaId);

    /**
     * Busca todos los gastos generales de un presupuesto específico.
     * Atraviesa la relación item_calculadora -> presupuesto_no_cliente.
     * 
     * @param presupuestoNoClienteId ID del presupuesto
     * @return Lista de todos los gastos generales del presupuesto
     */
    @Query("SELECT g FROM PresupuestoGastoGeneral g " +
           "JOIN g.itemCalculadora i " +
           "WHERE i.presupuestoNoCliente.id = :presupuestoNoClienteId " +
           "ORDER BY i.id ASC, g.orden ASC")
    List<PresupuestoGastoGeneral> findByPresupuestoNoClienteId(@Param("presupuestoNoClienteId") Long presupuestoNoClienteId);

    /**
     * Cuenta gastos generales por empresa.
     * Útil para estadísticas y reportes.
     * 
     * @param empresaId ID de la empresa
     * @return Cantidad total de gastos generales de la empresa
     */
    @Query("SELECT COUNT(g) FROM PresupuestoGastoGeneral g " +
           "WHERE g.empresa.id = :empresaId")
    Long countByEmpresaId(@Param("empresaId") Long empresaId);

    /**
     * Verifica si un item de calculadora tiene gastos generales.
     * 
     * @param itemCalculadoraId ID del item de calculadora
     * @return true si tiene gastos generales, false si no
     */
    @Query("SELECT CASE WHEN COUNT(g) > 0 THEN true ELSE false END " +
           "FROM PresupuestoGastoGeneral g " +
           "WHERE g.itemCalculadora.id = :itemCalculadoraId")
    Boolean existsByItemCalculadoraId(@Param("itemCalculadoraId") Long itemCalculadoraId);

    /**
     * Encuentra todos los gastos generales de una empresa.
     * 
     * @param empresaId ID de la empresa
     * @return Lista de gastos generales de la empresa
     */
    @Query("SELECT g FROM PresupuestoGastoGeneral g " +
           "WHERE g.empresa.id = :empresaId " +
           "ORDER BY g.id DESC")
    List<PresupuestoGastoGeneral> findByEmpresaId(@Param("empresaId") Long empresaId);
}