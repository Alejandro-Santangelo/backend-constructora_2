package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.Proveedor;
import com.rodrigo.construccion.repository.ProveedorRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Servicio para la gestión de Proveedores
 * 
 * Proporciona la lógica de negocio para todas las operaciones relacionadas con proveedores.
 * Incluye soporte Multi-Tenant donde todas las operaciones se filtran por empresa.
 */
@Service
@Transactional
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;
        public ProveedorService(ProveedorRepository proveedorRepository) {
            this.proveedorRepository = proveedorRepository;
        }
    // Logger eliminado

    /**
     * OPERACIONES CRUD
     */

    /**
     * Crear un nuevo proveedor
     */
    public Proveedor crearProveedor(Proveedor proveedor, Long empresaId) {
    System.out.println("Creando proveedor para empresa: " + empresaId);
        
        // Validaciones
        if (proveedor.getNombre() == null || proveedor.getNombre().trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del proveedor es obligatorio");
        }
        
        // Verificar si el RUT ya existe (si se proporciona)
        if (proveedor.getRut() != null && !proveedor.getRut().trim().isEmpty()) {
            if (proveedorRepository.existsByRutAndEmpresaId(proveedor.getRut(), empresaId)) {
                throw new IllegalArgumentException("Ya existe un proveedor con este RUT");
            }
        }
        
        // Setear valores por defecto
        proveedor.setEmpresaId(empresaId);
        proveedor.setActivo(true);
        proveedor.setFechaCreacion(LocalDateTime.now());
        proveedor.setFechaModificacion(LocalDateTime.now());
        
        return proveedorRepository.save(proveedor);
    }

    /**
     * Obtener proveedores por empresa con paginación
     */
    @Transactional(readOnly = true)
    public Page<Proveedor> obtenerPorEmpresaConPaginacion(Long empresaId, Pageable pageable) {
    System.out.println("Obteniendo proveedores paginados para empresa: " + empresaId);
        return proveedorRepository.findByEmpresaIdOrderByNombre(empresaId, pageable);
    }

    /**
     * Obtener proveedor por ID y empresa
     */
    @Transactional(readOnly = true)
    public Proveedor obtenerPorIdYEmpresa(Long id, Long empresaId) {
    System.out.println("Obteniendo proveedor ID: " + id + " para empresa: " + empresaId);
        return proveedorRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    /**
     * Actualizar proveedor
     */
    public Proveedor actualizarProveedor(Long id, Proveedor proveedorActualizado, Long empresaId) {
    System.out.println("Actualizando proveedor ID: " + id + " para empresa: " + empresaId);
        
        var proveedorExistente = obtenerPorIdYEmpresa(id, empresaId);
        
        // Validar RUT si cambió
        if (proveedorActualizado.getRut() != null && !proveedorActualizado.getRut().equals(proveedorExistente.getRut())) {
            if (proveedorRepository.existsByRutAndEmpresaIdAndIdNot(proveedorActualizado.getRut(), empresaId, id)) {
                throw new IllegalArgumentException("Ya existe otro proveedor con este RUT");
            }
        }
        
        // Actualizar campos
        proveedorExistente.setNombre(proveedorActualizado.getNombre());
        proveedorExistente.setRut(proveedorActualizado.getRut());
        proveedorExistente.setTelefono(proveedorActualizado.getTelefono());
        proveedorExistente.setEmail(proveedorActualizado.getEmail());
        proveedorExistente.setDireccion(proveedorActualizado.getDireccion());
        proveedorExistente.setCiudad(proveedorActualizado.getCiudad());
        proveedorExistente.setRegion(proveedorActualizado.getRegion());
        proveedorExistente.setCodigoPostal(proveedorActualizado.getCodigoPostal());
        proveedorExistente.setFechaModificacion(LocalDateTime.now());
        
        return proveedorRepository.save(proveedorExistente);
    }

    /**
     * Eliminar proveedor
     */
    public void eliminarProveedor(Long id, Long empresaId) {
    System.out.println("Eliminando proveedor ID: " + id + " para empresa: " + empresaId);
        
        var proveedor = obtenerPorIdYEmpresa(id, empresaId);        
        proveedorRepository.delete(proveedor);
    }

    /**
     * CONSULTAS ESPECIALIZADAS
     */

    /**
     * Buscar proveedores por nombre
     */
    @Transactional(readOnly = true)
    public List<Proveedor> buscarPorNombre(String nombre, Long empresaId) {
    System.out.println("Buscando proveedores por nombre '" + nombre + "' para empresa: " + empresaId);
        return proveedorRepository.findByNombreContainingIgnoreCaseAndEmpresaId(nombre, empresaId);
    }

    /**
     * Obtener proveedores por ciudad
     */
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerPorCiudad(String ciudad, Long empresaId) {
    System.out.println("Obteniendo proveedores por ciudad '" + ciudad + "' para empresa: " + empresaId);
        return proveedorRepository.findByCiudadIgnoreCaseAndEmpresaId(ciudad, empresaId);
    }

    /**
     * Obtener proveedores por región
     */
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerPorRegion(String region, Long empresaId) {
    System.out.println("Obteniendo proveedores por región '" + region + "' para empresa: " + empresaId);
        return proveedorRepository.findByRegionIgnoreCaseAndEmpresaId(region, empresaId);
    }

    /**
     * Validar si un RUT está disponible
     */
    @Transactional(readOnly = true)
    public boolean validarRutDisponible(String rut, Long empresaId) {
    System.out.println("Validando disponibilidad de RUT '" + rut + "' para empresa: " + empresaId);
        return !proveedorRepository.existsByRutAndEmpresaId(rut, empresaId);
    }

    /**
     * Obtener proveedores activos
     */
    @Transactional(readOnly = true)
    public List<Proveedor> obtenerProveedoresActivos(Long empresaId) {
    System.out.println("Obteniendo proveedores activos para empresa: " + empresaId);
        return proveedorRepository.findByActivoTrueAndEmpresaIdOrderByNombre(empresaId);
    }

    /**
     * Obtener estadísticas de proveedores
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerEstadisticas(Long empresaId) {
    System.out.println("Obteniendo estadísticas de proveedores para empresa: " + empresaId);
        
        Map<String, Object> estadisticas = new HashMap<>();
        
        // Totales
        long totalProveedores = proveedorRepository.countByEmpresaId(empresaId);
        long proveedoresActivos = proveedorRepository.countByActivoTrueAndEmpresaId(empresaId);
        long proveedoresInactivos = totalProveedores - proveedoresActivos;
        
        estadisticas.put("total", totalProveedores);
        estadisticas.put("activos", proveedoresActivos);
        estadisticas.put("inactivos", proveedoresInactivos);
        
        // Distribución por ciudad
        var distribucionCiudad = proveedorRepository.countProveedoresPorCiudad(empresaId);
        estadisticas.put("distribuccionCiudad", distribucionCiudad);
        
        // Distribución por región
        var distribuccionRegion = proveedorRepository.countProveedoresPorRegion(empresaId);
        estadisticas.put("distribuccionRegion", distribuccionRegion);
        
        return estadisticas;
    }

    /**
     * Cambiar estado del proveedor
     */
    public Proveedor cambiarEstado(Long id, boolean activo, Long empresaId) {
    System.out.println("Cambiando estado de proveedor ID: " + id + " a " + activo + " para empresa: " + empresaId);
        
        var proveedor = obtenerPorIdYEmpresa(id, empresaId);
        proveedor.setActivo(activo);
        proveedor.setFechaModificacion(LocalDateTime.now());
        
        return proveedorRepository.save(proveedor);
    }
}