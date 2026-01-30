package com.rodrigo.construccion.repository;

import com.rodrigo.construccion.model.entity.PedidoPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoPagoRepository extends JpaRepository<PedidoPago, Long> {

    /* Buscar pedido por ID y empresa */
    Optional<PedidoPago> findByIdAndEmpresa_Id(Long id, Long empresaId);

    /* Buscar pedidos por empresa */
    List<PedidoPago> findByEmpresa_Id(Long empresaId);

    /* Buscar pedidos por empresa con paginación */
    Page<PedidoPago> findByEmpresa_Id(Long empresaId, Pageable pageable);

    /* Buscar pedidos por estado y empresa */
    List<PedidoPago> findByEstadoAndEmpresa_Id(String estado, Long empresaId);

    /* Buscar pedidos entre fechas */
    List<PedidoPago> findByFechaPedidoBetween(LocalDate fechaInicio, LocalDate fechaFin);

}