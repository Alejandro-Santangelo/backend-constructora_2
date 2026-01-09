package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.dto.response.EmpresaEstadisticasDTO;
import com.rodrigo.construccion.dto.response.EmpresaEstadoResponseDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {

       /* Buscar empresas activas */
       List<Empresa> findByActivaTrue();

       /* Buscar empresa por CUIT, excluyendo un ID */
       Optional<Empresa> findByCuitAndActivaTrueAndIdNot(String cuit, Long id);

       /* Verificar si existe una empresa con un CUIT específico */
       boolean existsByCuit(String cuit);

       /* Obtener estadísticas de empresa */
       @Query("SELECT new com.rodrigo.construccion.dto.response.EmpresaEstadisticasDTO(" +
                     "e.id, e.nombreEmpresa, " +
                     "COUNT(DISTINCT c), " +
                     "COUNT(DISTINCT o), " +
                     "COUNT(DISTINCT u)) " +
                     "FROM Empresa e " +
                     "LEFT JOIN e.clientes c " +
                     "LEFT JOIN c.obras o " +
                     "LEFT JOIN e.usuarios u " +
                     "WHERE e.activa = true " +
                     "GROUP BY e.id, e.nombreEmpresa " +
                     "ORDER BY e.nombreEmpresa")
       List<EmpresaEstadisticasDTO> findEmpresaEstadisticas();

       /* Buscar empresas con clientes activos */
       @Query("SELECT DISTINCT e FROM Empresa e " +
                     "JOIN e.clientes c " +
                     "WHERE e.activa = true " +
                     "ORDER BY e.nombreEmpresa")
       List<Empresa> findEmpresasWithClientes();

       /* Métodos para búsqueda universal por identificador */
       
       /* Buscar por CUIT */
       Optional<Empresa> findByCuit(String cuit);
       
       /* Buscar por nombre exacto (case insensitive) */
       Optional<Empresa> findByNombreEmpresaIgnoreCase(String nombreEmpresa);
       
       /* Buscar por nombre parcial (case insensitive) */
       List<Empresa> findByNombreEmpresaContainingIgnoreCase(String nombreEmpresa);

       /* Verificar estado de empresa - consulta optimizada */
       @Query("SELECT new com.rodrigo.construccion.dto.response.EmpresaEstadoResponseDTO(" +
              "true, e.activa, e.nombreEmpresa) " +
              "FROM Empresa e " +
              "WHERE e.id = :id")
       Optional<EmpresaEstadoResponseDTO> findEstadoById(Long id);
}