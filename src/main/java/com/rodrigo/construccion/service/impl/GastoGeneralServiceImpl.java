package com.rodrigo.construccion.service.impl;

import com.rodrigo.construccion.model.entity.GastoGeneral;
import com.rodrigo.construccion.repository.GastoGeneralRepository;
import com.rodrigo.construccion.service.IGastoGeneralService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.util.List;

/**
 * Implementación del servicio para gestionar gastos generales
 */
@Service
@Transactional
@Slf4j
public class GastoGeneralServiceImpl implements IGastoGeneralService {

    private final GastoGeneralRepository gastoGeneralRepository;
    private final EntityManager entityManager;

    public GastoGeneralServiceImpl(GastoGeneralRepository gastoGeneralRepository, EntityManager entityManager) {
        this.gastoGeneralRepository = gastoGeneralRepository;
        this.entityManager = entityManager;
    }

    @Override
    public GastoGeneral crear(Long empresaId, GastoGeneral gastoGeneral) {
        log.info("🔄 Creando gasto general - Empresa: {}, Nombre: {}", empresaId, gastoGeneral.getNombre());
        
        gastoGeneral.setEmpresaId(empresaId);
        GastoGeneral saved = gastoGeneralRepository.save(gastoGeneral);
        
        log.info("✅ Gasto general creado con ID: {}", saved.getId());
        return saved;
    }

    @Override
    public GastoGeneral actualizar(Long empresaId, Long id, GastoGeneral gastoGeneral) {
        log.info("🔄 Actualizando gasto general - Empresa: {}, ID: {}", empresaId, id);
        
        GastoGeneral existente = gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
        
        existente.setNombre(gastoGeneral.getNombre());
        existente.setDescripcion(gastoGeneral.getDescripcion());
        existente.setUnidadMedida(gastoGeneral.getUnidadMedida());
        existente.setCategoria(gastoGeneral.getCategoria());
        existente.setPrecioUnitarioBase(gastoGeneral.getPrecioUnitarioBase());
        
        GastoGeneral updated = gastoGeneralRepository.save(existente);
        
        log.info("✅ Gasto general actualizado correctamente");
        return updated;
    }

    @Override
    public void eliminar(Long empresaId, Long id) {
        log.info("🗑️ Eliminando gasto general - Empresa: {}, ID: {}", empresaId, id);
        
        GastoGeneral existente = gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
        
        gastoGeneralRepository.delete(existente);
        
        log.info("✅ Gasto general eliminado correctamente");
    }

    @Override
    @Transactional(readOnly = true)
    public GastoGeneral obtenerPorId(Long empresaId, Long id) {
        log.info("🔍 Obteniendo gasto general - Empresa: {}, ID: {}", empresaId, id);
        
        return gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoGeneral> listarPorEmpresa(Long empresaId) {
        log.info("🔍 Listando gastos generales - TODOS (compartidos entre empresas)");
        
        Session session = entityManager.unwrap(Session.class);
        
        // Deshabilitar filtro empresaFilter para que devuelva TODOS los gastos generales
        session.disableFilter("empresaFilter");
        
        return gastoGeneralRepository.findByEmpresaIdOrderByNombre(empresaId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<GastoGeneral> listarPorCategoria(Long empresaId, String categoria) {
        log.info("🔍 Listando gastos generales por categoría - Empresa: {}, Categoría: {}", empresaId, categoria);
        
        return gastoGeneralRepository.findByEmpresaIdAndCategoriaOrderByNombre(empresaId, categoria);
    }

    @Override
    public void actualizarPrecioTodos(Long empresaId, double porcentaje) {
        log.info("💰 Actualizando precio de todos los gastos generales - Empresa: {}, Porcentaje: {}%", empresaId, porcentaje);
        
        List<GastoGeneral> gastos = gastoGeneralRepository.findByEmpresaIdOrderByNombre(empresaId);
        java.math.BigDecimal factor = java.math.BigDecimal.ONE.add(java.math.BigDecimal.valueOf(porcentaje / 100));
        
        for (GastoGeneral gasto : gastos) {
            if (gasto.getPrecioUnitarioBase() != null) {
                java.math.BigDecimal nuevoPrecio = gasto.getPrecioUnitarioBase().multiply(factor);
                gasto.setPrecioUnitarioBase(nuevoPrecio);
            }
        }
        
        gastoGeneralRepository.saveAll(gastos);
        log.info("✅ Precios actualizados para {} gastos generales", gastos.size());
    }

    @Override
    public void actualizarPrecioPorId(Long empresaId, Long id, double porcentaje) {
        log.info("💰 Actualizando precio de gasto general - Empresa: {}, ID: {}, Porcentaje: {}%", empresaId, id, porcentaje);
        
        GastoGeneral gasto = gastoGeneralRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new RuntimeException("Gasto general no encontrado"));
        
        if (gasto.getPrecioUnitarioBase() != null) {
            java.math.BigDecimal factor = java.math.BigDecimal.ONE.add(java.math.BigDecimal.valueOf(porcentaje / 100));
            java.math.BigDecimal nuevoPrecio = gasto.getPrecioUnitarioBase().multiply(factor);
            gasto.setPrecioUnitarioBase(nuevoPrecio);
            gastoGeneralRepository.save(gasto);
        }
        
        log.info("✅ Precio actualizado correctamente");
    }

    @Override
    public void actualizarPrecioVarios(Long empresaId, List<Long> ids, double porcentaje) {
        log.info("💰 Actualizando precio de varios gastos generales - Empresa: {}, IDs: {}, Porcentaje: {}%", empresaId, ids, porcentaje);
        
        List<GastoGeneral> gastos = gastoGeneralRepository.findAllById(ids);
        // Filtrar solo los de la empresa
        gastos = gastos.stream()
            .filter(g -> g.getEmpresaId().equals(empresaId))
            .collect(java.util.stream.Collectors.toList());
        
        if (gastos.isEmpty()) {
            throw new RuntimeException("No se encontraron gastos generales con los IDs proporcionados para esta empresa");
        }
        
        java.math.BigDecimal factor = java.math.BigDecimal.ONE.add(java.math.BigDecimal.valueOf(porcentaje / 100));
        for (GastoGeneral gasto : gastos) {
            if (gasto.getPrecioUnitarioBase() != null) {
                java.math.BigDecimal nuevoPrecio = gasto.getPrecioUnitarioBase().multiply(factor);
                gasto.setPrecioUnitarioBase(nuevoPrecio);
            }
        }
        
        gastoGeneralRepository.saveAll(gastos);
        log.info("✅ Precios actualizados para {} gastos generales", gastos.size());
    }
}
