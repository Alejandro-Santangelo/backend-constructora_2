package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.MovimientoMaterial;
import com.rodrigo.construccion.repository.MovimientoMaterialRepository;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Movimientos de Material
 * Maneja la lógica de negocio para operaciones con movimientos de inventario
 */
@Service
@Transactional
public class MovimientoMaterialService {

    private final MovimientoMaterialRepository movimientoMaterialRepository;
    // Logger eliminado, usar System.out.println si es necesario
    public MovimientoMaterialService(MovimientoMaterialRepository movimientoMaterialRepository) {
        this.movimientoMaterialRepository = movimientoMaterialRepository;
    }

    /**
     * Obtener todos los movimientos
     */
    public List<MovimientoMaterial> obtenerTodos() {
    System.out.println("Obteniendo todos los movimientos de material");
        return movimientoMaterialRepository.findAll();
    }

    /**
     * Obtener movimientos con paginación
     */
    public Page<MovimientoMaterial> obtenerTodosPaginados(Pageable pageable) {
    System.out.println("Obteniendo movimientos paginados: " + pageable);
        return movimientoMaterialRepository.findAll(pageable);
    }

    /**
     * Obtener movimiento por ID
     */
    public Optional<MovimientoMaterial> obtenerPorId(Long id) {
    System.out.println("Obteniendo movimiento por ID: " + id);
        return movimientoMaterialRepository.findById(id);
    }

    /**
     * Crear nuevo movimiento
     */
    public MovimientoMaterial crear(MovimientoMaterial movimiento) {
    System.out.println("Creando nuevo movimiento de material");
        return movimientoMaterialRepository.save(movimiento);
    }

    /**
     * Actualizar movimiento existente
     */
    public MovimientoMaterial actualizar(Long id, MovimientoMaterial movimientoActualizado) {
    System.out.println("Actualizando movimiento ID: " + id);
        return movimientoMaterialRepository.findById(id)
                .map(movimiento -> {
                    // Actualizar campos disponibles
                    if (movimientoActualizado.getTipoMovimiento() != null) {
                        movimiento.setTipoMovimiento(movimientoActualizado.getTipoMovimiento());
                    }
                    if (movimientoActualizado.getCantidad() != null) {
                        movimiento.setCantidad(movimientoActualizado.getCantidad());
                    }
                    if (movimientoActualizado.getPrecioUnitario() != null) {
                        movimiento.setPrecioUnitario(movimientoActualizado.getPrecioUnitario());
                    }
                    if (movimientoActualizado.getMotivo() != null) {
                        movimiento.setMotivo(movimientoActualizado.getMotivo());
                    }
                    if (movimientoActualizado.getObservaciones() != null) {
                        movimiento.setObservaciones(movimientoActualizado.getObservaciones());
                    }
                    if (movimientoActualizado.getEstado() != null) {
                        movimiento.setEstado(movimientoActualizado.getEstado());
                    }
                    
                    return movimientoMaterialRepository.save(movimiento);
                })
                .orElseThrow(() -> new RuntimeException("Movimiento no encontrado con ID: " + id));
    }

    /**
     * Eliminar movimiento
     */
    public void eliminar(Long id) {
    System.out.println("Eliminando movimiento ID: " + id);
        if (movimientoMaterialRepository.existsById(id)) {
            movimientoMaterialRepository.deleteById(id);
        } else {
            throw new RuntimeException("Movimiento no encontrado con ID: " + id);
        }
    }

    /**
     * Buscar movimientos por empresa
     */
    public List<MovimientoMaterial> buscarPorEmpresa(Long empresaId) {
    System.out.println("Buscando movimientos por empresa: " + empresaId);
        return movimientoMaterialRepository.findByEmpresaId(empresaId);
    }

    /**
     * Buscar movimientos por obra
     */
    public List<MovimientoMaterial> buscarPorObra(Long obraId) {
    System.out.println("Buscando movimientos por obra: " + obraId);
        return movimientoMaterialRepository.findByObraId(obraId);
    }

    /**
     * Buscar movimientos por tipo
     */
    public List<MovimientoMaterial> buscarPorTipo(String tipoMovimiento) {
    System.out.println("Buscando movimientos por tipo: " + tipoMovimiento);
        return movimientoMaterialRepository.findAll().stream()
                .filter(mov -> tipoMovimiento.equals(mov.getTipoMovimiento()))
                .toList();
    }

    /**
     * Buscar movimientos por rango de fechas
     */
    public List<MovimientoMaterial> buscarPorRangoFechas(LocalDateTime fechaInicio, LocalDateTime fechaFin) {
    System.out.println("Buscando movimientos entre " + fechaInicio + " y " + fechaFin);
        return movimientoMaterialRepository.findAll().stream()
                .filter(mov -> {
                    LocalDateTime fecha = mov.getFechaMovimiento();
                    return fecha != null && 
                           !fecha.isBefore(fechaInicio) && 
                           !fecha.isAfter(fechaFin);
                })
                .toList();
    }

    /**
     * Calcular valor total de movimientos
     */
    public BigDecimal calcularValorTotal(List<MovimientoMaterial> movimientos) {
        return movimientos.stream()
                .map(MovimientoMaterial::getValorTotal)
                .filter(valor -> valor != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Obtener movimientos del mes actual
     */
    public List<MovimientoMaterial> obtenerMovimientosMesActual() {
        LocalDateTime inicioMes = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime finMes = inicioMes.plusMonths(1).minusNanos(1);
        return buscarPorRangoFechas(inicioMes, finMes);
    }

    /**
     * Obtener estadísticas básicas
     */
    public long contarTotal() {
        return movimientoMaterialRepository.count();
    }
}