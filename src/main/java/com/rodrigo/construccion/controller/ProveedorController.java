package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.ProveedorRequestDTO;
import com.rodrigo.construccion.dto.response.ProveedorEstadisticaResponseDTO;
import com.rodrigo.construccion.dto.response.ProveedorResponseDTO;
import com.rodrigo.construccion.dto.response.RutValidacionResponseDTO;
import com.rodrigo.construccion.model.entity.Proveedor;
import com.rodrigo.construccion.service.ProveedorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/proveedores")
@RequiredArgsConstructor
public class ProveedorController {
    private final ProveedorService proveedorService;

    /* Crear un nuevo proveedor */
    @PostMapping
    @Operation(summary = "Crear proveedor", description = "Crea un nuevo proveedor en la empresa especificada")
    public ResponseEntity<ProveedorResponseDTO> crearProveedor(@Valid @RequestBody ProveedorRequestDTO proveedorDTO, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(proveedorService.crearProveedor(proveedorDTO, empresaId));
    }

    /* Obtener todos los proveedores con paginación */
    @GetMapping
    @Operation(summary = "Listar proveedores", description = "Obtiene todos los proveedores de la empresa con paginación")
    public ResponseEntity<Page<ProveedorResponseDTO>> obtenerProveedores(@RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(proveedorService.obtenerPorEmpresaConPaginacion(empresaId, pageable));
    }

    /* Obtener proveedor por ID */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener proveedor", description = "Obtiene un proveedor específico por su ID")
    public ResponseEntity<ProveedorResponseDTO> obtenerProveedor(@PathVariable Long id, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(proveedorService.obtenerPorIdYEmpresaDTO(id, empresaId));
    }

    /* Actualizar proveedor */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar proveedor", description = "Actualiza un proveedor existente")
    public ResponseEntity<ProveedorResponseDTO> actualizarProveedor(@PathVariable Long id, @Valid @RequestBody ProveedorRequestDTO proveedorDTO, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(proveedorService.actualizarProveedor(id, proveedorDTO, empresaId));
    }

    /* Eliminar proveedor */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar proveedor", description = "Elimina un proveedor del sistema")
    public ResponseEntity<String> eliminarProveedor(@PathVariable Long id, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        proveedorService.eliminarProveedor(id, empresaId);
        return ResponseEntity.ok("Proveedor eliminado exitosamente");
    }

    /* Buscar proveedores por nombre */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar proveedores", description = "Busca proveedores por nombre (búsqueda parcial)")
    public ResponseEntity<List<ProveedorResponseDTO>> buscarProveedores(@RequestParam String nombre, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(proveedorService.buscarPorNombre(nombre, empresaId));
    }

    /* Obtener proveedores activos */
    @GetMapping("/activos")
    @Operation(summary = "Proveedores activos", description = "Obtiene solo los proveedores activos de la empresa")
    public ResponseEntity<List<ProveedorResponseDTO>> obtenerProveedoresActivos(@RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(proveedorService.obtenerProveedoresActivos(empresaId));
    }
}