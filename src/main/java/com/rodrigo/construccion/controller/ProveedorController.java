package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.Proveedor;
import com.rodrigo.construccion.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Controlador REST para la gestión de Proveedores
 * 
 * Proporciona todas las operaciones CRUD y consultas especializadas para proveedores.
 * Incluye soporte Multi-Tenant donde todas las operaciones se filtran por empresa.
 */
@RestController
@RequestMapping("/proveedores")
@Tag(name = "Proveedores", description = "Gestión de proveedores del sistema multi-tenant")
@Tag(name = "Proveedores", description = "Gestión de proveedores del sistema multi-tenant")
public class ProveedorController {
    private final ProveedorService proveedorService;

    public ProveedorController(ProveedorService proveedorService) {
        this.proveedorService = proveedorService;
    }

    /**
     * OPERACIONES CRUD
     */

    /**
     * Crear un nuevo proveedor
     */
    @PostMapping
    @Operation(
        summary = "Crear proveedor",
        description = "Crea un nuevo proveedor en la empresa especificada"
    )
    public ResponseEntity<Proveedor> crearProveedor(
            @Valid @RequestBody Proveedor proveedor,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("POST /proveedores - Empresa: " + empresaId + ", Proveedor: " + proveedor.getNombre());
        
        try {
            var proveedorNuevo = proveedorService.crearProveedor(proveedor, empresaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(proveedorNuevo);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al crear proveedor - Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Error inesperado al crear proveedor - Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todos los proveedores con paginación
     */
    @GetMapping
    @Operation(
        summary = "Listar proveedores",
        description = "Obtiene todos los proveedores de la empresa con paginación"
    )
    public ResponseEntity<Page<Proveedor>> obtenerProveedores(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        
    System.out.println("GET /proveedores - Empresa: " + empresaId + ", Página: " + pageable.getPageNumber());
        var proveedores = proveedorService.obtenerPorEmpresaConPaginacion(empresaId, pageable);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Obtener proveedor por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener proveedor",
        description = "Obtiene un proveedor específico por su ID"
    )
    public ResponseEntity<Proveedor> obtenerProveedor(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/" + id + " - Empresa: " + empresaId);
        
        try {
            var proveedor = proveedorService.obtenerPorIdYEmpresa(id, empresaId);
            return ResponseEntity.ok(proveedor);
        } catch (RuntimeException e) {
            System.out.println("Proveedor no encontrado - ID: " + id + ", Empresa: " + empresaId);
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Actualizar proveedor
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar proveedor",
        description = "Actualiza un proveedor existente"
    )
    public ResponseEntity<Proveedor> actualizarProveedor(
            @PathVariable Long id,
            @Valid @RequestBody Proveedor proveedor,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("PUT /proveedores/" + id + " - Empresa: " + empresaId);
        
        try {
            var proveedorActualizado = proveedorService.actualizarProveedor(id, proveedor, empresaId);
            return ResponseEntity.ok(proveedorActualizado);
        } catch (RuntimeException e) {
            System.out.println("Error al actualizar proveedor - ID: " + id + ", Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Error inesperado al actualizar proveedor - ID: " + id + ", Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar proveedor
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar proveedor",
        description = "Elimina un proveedor del sistema"
    )
    public ResponseEntity<Void> eliminarProveedor(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("DELETE /proveedores/" + id + " - Empresa: " + empresaId);
        
        try {
            proveedorService.eliminarProveedor(id, empresaId);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.out.println("Error al eliminar proveedor - ID: " + id + ", Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            System.out.println("Error inesperado al eliminar proveedor - ID: " + id + ", Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CONSULTAS ESPECIALIZADAS
     */

    /**
     * Buscar proveedores por nombre
     */
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar proveedores",
        description = "Busca proveedores por nombre (búsqueda parcial)"
    )
    public ResponseEntity<List<Proveedor>> buscarProveedores(
            @RequestParam String nombre,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/buscar - Empresa: " + empresaId + ", Nombre: " + nombre);
        var proveedores = proveedorService.buscarPorNombre(nombre, empresaId);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Obtener proveedores por ciudad
     */
    @GetMapping("/por-ciudad/{ciudad}")
    @Operation(
        summary = "Proveedores por ciudad",
        description = "Obtiene todos los proveedores de una ciudad específica"
    )
    public ResponseEntity<List<Proveedor>> obtenerProveedoresPorCiudad(
            @PathVariable String ciudad,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/por-ciudad/" + ciudad + " - Empresa: " + empresaId);
        var proveedores = proveedorService.obtenerPorCiudad(ciudad, empresaId);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Obtener proveedores por región
     */
    @GetMapping("/por-region/{region}")
    @Operation(
        summary = "Proveedores por región",
        description = "Obtiene todos los proveedores de una región específica"
    )
    public ResponseEntity<List<Proveedor>> obtenerProveedoresPorRegion(
            @PathVariable String region,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/por-region/" + region + " - Empresa: " + empresaId);
        var proveedores = proveedorService.obtenerPorRegion(region, empresaId);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Validar RUT de proveedor
     */
    @GetMapping("/validar-rut/{rut}")
    @Operation(
        summary = "Validar RUT",
        description = "Valida si un RUT está disponible para un nuevo proveedor"
    )
    public ResponseEntity<Map<String, Boolean>> validarRut(
            @PathVariable String rut,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/validar-rut/" + rut + " - Empresa: " + empresaId);
        boolean disponible = proveedorService.validarRutDisponible(rut, empresaId);
        return ResponseEntity.ok(Map.of("disponible", disponible));
    }

    /**
     * Obtener proveedores activos
     */
    @GetMapping("/activos")
    @Operation(
        summary = "Proveedores activos",
        description = "Obtiene solo los proveedores activos de la empresa"
    )
    public ResponseEntity<List<Proveedor>> obtenerProveedoresActivos(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/activos - Empresa: " + empresaId);
        var proveedores = proveedorService.obtenerProveedoresActivos(empresaId);
        return ResponseEntity.ok(proveedores);
    }

    /**
     * Estadísticas de proveedores
     */
    @GetMapping("/estadisticas")
    @Operation(
        summary = "Estadísticas de proveedores",
        description = "Obtiene estadísticas básicas de los proveedores"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("GET /proveedores/estadisticas - Empresa: " + empresaId);
        var estadisticas = proveedorService.obtenerEstadisticas(empresaId);
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Activar/Desactivar proveedor
     */
    @PutMapping("/{id}/estado")
    @Operation(
        summary = "Cambiar estado del proveedor",
        description = "Activa o desactiva un proveedor"
    )
    public ResponseEntity<Proveedor> cambiarEstadoProveedor(
            @PathVariable Long id,
            @RequestParam boolean activo,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("PUT /proveedores/" + id + "/estado - Empresa: " + empresaId + ", Activo: " + activo);
        
        try {
            var proveedor = proveedorService.cambiarEstado(id, activo, empresaId);
            return ResponseEntity.ok(proveedor);
        } catch (RuntimeException e) {
            System.out.println("Error al cambiar estado del proveedor - ID: " + id + ", Empresa: " + empresaId);
            return ResponseEntity.notFound().build();
        }
    }
}