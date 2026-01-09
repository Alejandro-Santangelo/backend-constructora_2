package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.Usuario;
import com.rodrigo.construccion.service.UsuarioService;
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
 * Controlador REST para la gestión de Usuarios
 * 
 * Proporciona todas las operaciones CRUD y consultas especializadas para usuarios del sistema.
 * Incluye soporte Multi-Tenant donde todas las operaciones se filtran por empresa.
 */
@RestController
@RequestMapping("/usuarios")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema multi-tenant")
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema multi-tenant")
public class UsuarioController {
    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    /**
     * OPERACIONES CRUD
     */

    /**
     * Crear un nuevo usuario
     */
    @PostMapping
    @Operation(
        summary = "Crear usuario",
        description = "Registra un nuevo usuario en la empresa especificada"
    )
    public ResponseEntity<Usuario> crearUsuario(
            @Valid @RequestBody Usuario usuario,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
    System.out.println("POST /usuarios - Empresa: " + empresaId + ", Usuario: " + usuario.getEmail());
        
        try {
            var usuarioNuevo = usuarioService.crearUsuario(usuario, empresaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioNuevo);
        } catch (IllegalArgumentException e) {
            System.out.println("Error al crear usuario - Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            System.out.println("Error inesperado al crear usuario - Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Obtener todos los usuarios con paginación
     */
    @GetMapping
    @Operation(
        summary = "Listar usuarios",
        description = "Obtiene todos los usuarios de la empresa con paginación"
    )
    public ResponseEntity<Page<Usuario>> obtenerUsuarios(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        
    System.out.println("GET /usuarios - Empresa: " + empresaId + ", Página: " + pageable.getPageNumber());
        var usuarios = usuarioService.obtenerPorEmpresaConPaginacion(empresaId, pageable);
        
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtener usuario por ID
     */
    @GetMapping("/{id}")
    @Operation(
        summary = "Obtener usuario",
        description = "Obtiene un usuario específico por su ID"
    )
    public ResponseEntity<Usuario> obtenerUsuario(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
            System.out.println("GET /usuarios/" + id + " - Empresa: " + empresaId);
        
        return usuarioService.obtenerPorIdYEmpresa(id, empresaId)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Actualizar usuario
     */
    @PutMapping("/{id}")
    @Operation(
        summary = "Actualizar usuario",
        description = "Actualiza un usuario existente"
    )
    public ResponseEntity<Usuario> actualizarUsuario(
            @PathVariable Long id,
            @Valid @RequestBody Usuario usuarioActualizado,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
            System.out.println("PUT /usuarios/" + id + " - Empresa: " + empresaId);
        
        try {
            var usuario = usuarioService.actualizarUsuario(id, usuarioActualizado, empresaId);
            return ResponseEntity.ok(usuario);
        } catch (IllegalArgumentException e) {
                System.out.println("Error al actualizar usuario - ID: " + id + ", Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
                System.out.println("Error inesperado al actualizar usuario - ID: " + id + ", Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Eliminar usuario
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Eliminar usuario",
        description = "Elimina un usuario del sistema (soft delete)"
    )
    public ResponseEntity<Void> eliminarUsuario(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
            System.out.println("DELETE /usuarios/" + id + " - Empresa: " + empresaId);
        
        try {
            usuarioService.eliminarUsuario(id, empresaId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
                System.out.println("Error al eliminar usuario - ID: " + id + ", Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
                System.out.println("Error inesperado al eliminar usuario - ID: " + id + ", Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * CONSULTAS ESPECIALIZADAS POR ROL
     */

    /**
     * Obtener usuarios por rol
     */
    @GetMapping("/rol/{rol}")
    @Operation(
        summary = "Usuarios por rol",
        description = "Obtiene todos los usuarios con un rol específico en la empresa"
    )
    public ResponseEntity<List<Usuario>> obtenerUsuariosPorRol(
            @PathVariable String rol,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/rol/" + rol + " - Empresa: " + empresaId);
        var usuarios = usuarioService.obtenerPorRolYEmpresa(rol, empresaId);
        
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtener administradores
     */
    @GetMapping("/administradores")
    @Operation(
        summary = "Obtener administradores",
        description = "Obtiene todos los usuarios administradores de la empresa"
    )
    public ResponseEntity<List<Usuario>> obtenerAdministradores(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/administradores - Empresa: " + empresaId);
        var usuarios = usuarioService.obtenerAdministradores(empresaId);
        
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Obtener usuarios activos
     */
    @GetMapping("/activos")
    @Operation(
        summary = "Usuarios activos",
        description = "Obtiene todos los usuarios activos de la empresa"
    )
    public ResponseEntity<List<Usuario>> obtenerUsuariosActivos(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/activos - Empresa: " + empresaId);
        var usuarios = usuarioService.obtenerActivosPorEmpresa(empresaId);
        
        return ResponseEntity.ok(usuarios);
    }

    /**
     * Buscar usuarios por nombre
     */
    @GetMapping("/buscar")
    @Operation(
        summary = "Buscar usuarios",
        description = "Busca usuarios por nombre con búsqueda parcial"
    )
    public ResponseEntity<Page<Usuario>> buscarUsuarios(
            @RequestParam String nombre,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        
              System.out.println("GET /usuarios/buscar?nombre=" + nombre + " - Empresa: " + empresaId);
        var usuarios = usuarioService.buscarPorNombre(empresaId, nombre, pageable);
        
        return ResponseEntity.ok(usuarios);
    }

    /**
     * GESTIÓN DE CONTRASEÑAS Y AUTENTICACIÓN
     */

    /**
     * Cambiar contraseña
     */
    @PutMapping("/{id}/password")
    @Operation(
        summary = "Cambiar contraseña",
        description = "Cambia la contraseña de un usuario"
    )
    public ResponseEntity<Map<String, String>> cambiarPassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> passwordData,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
            System.out.println("PUT /usuarios/" + id + "/password - Empresa: " + empresaId);
        
        try {
            String passwordActual = passwordData.get("passwordActual");
            String passwordNueva = passwordData.get("passwordNueva");
            
            usuarioService.cambiarPassword(id, passwordActual, passwordNueva, empresaId);
            return ResponseEntity.ok(Map.of("mensaje", "Contraseña actualizada exitosamente"));
        } catch (IllegalArgumentException e) {
                System.out.println("Error al cambiar contraseña - ID: " + id + ", Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
                System.out.println("Error inesperado al cambiar contraseña - ID: " + id + ", Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Activar/Desactivar usuario
     */
    @PutMapping("/{id}/estado")
    @Operation(
        summary = "Cambiar estado usuario",
        description = "Activa o desactiva un usuario"
    )
    public ResponseEntity<Map<String, String>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
            System.out.println("PUT /usuarios/" + id + "/estado?activo=" + activo + " - Empresa: " + empresaId);
        
        try {
            usuarioService.cambiarEstado(id, activo, empresaId);
            String mensaje = activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
            return ResponseEntity.ok(Map.of("mensaje", mensaje));
        } catch (IllegalArgumentException e) {
                System.out.println("Error al cambiar estado - ID: " + id + ", Empresa: " + empresaId + ", Error: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
                System.out.println("Error inesperado al cambiar estado - ID: " + id + ", Empresa: " + empresaId + ", " + e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error interno del servidor"));
        }
    }

    /**
     * Obtener usuario por email
     */
    @GetMapping("/email/{email}")
    @Operation(
        summary = "Buscar por email",
        description = "Busca un usuario específico por su email en la empresa"
    )
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(
            @PathVariable String email,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
        System.out.println("GET /usuarios/email/" + email + " - Empresa: " + empresaId);
        
        return usuarioService.obtenerPorEmailYEmpresa(email, empresaId)
                .map(usuario -> ResponseEntity.ok(usuario))
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * ESTADÍSTICAS Y MÉTRICAS
     */

    /**
     * Obtener estadísticas de usuarios
     */
    @GetMapping("/estadisticas")
    @Operation(
        summary = "Estadísticas de usuarios",
        description = "Obtiene estadísticas generales de usuarios de la empresa"
    )
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/estadisticas - Empresa: " + empresaId);
        var estadisticas = usuarioService.obtenerEstadisticas(empresaId);
        
        return ResponseEntity.ok(estadisticas);
    }

    /**
     * Contar usuarios por empresa
     */
    @GetMapping("/contar")
    @Operation(
        summary = "Contar usuarios",
        description = "Cuenta el total de usuarios de la empresa"
    )
    public ResponseEntity<Map<String, Long>> contarUsuarios(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/contar - Empresa: " + empresaId);
        var total = usuarioService.contarUsuariosPorEmpresa(empresaId);
        
        return ResponseEntity.ok(Map.of("total", total));
    }

    /**
     * Contar usuarios activos
     */
    @GetMapping("/contar/activos")
    @Operation(
        summary = "Contar usuarios activos",
        description = "Cuenta el total de usuarios activos de la empresa"
    )
    public ResponseEntity<Map<String, Long>> contarUsuariosActivos(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/contar/activos - Empresa: " + empresaId);
        var total = usuarioService.contarUsuariosActivosPorEmpresa(empresaId);
        
        return ResponseEntity.ok(Map.of("totalActivos", total));
    }

    /**
     * VALIDACIONES
     */

    /**
     * Verificar si existe un usuario
     */
    @GetMapping("/{id}/existe")
    @Operation(
        summary = "Verificar existencia",
        description = "Verifica si existe un usuario con el ID especificado en la empresa"
    )
    public ResponseEntity<Map<String, Boolean>> verificarExistencia(
            @PathVariable Long id,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/" + id + "/existe - Empresa: " + empresaId);
        var existe = usuarioService.existeUsuarioEnEmpresa(id, empresaId);
        
        return ResponseEntity.ok(Map.of("existe", existe));
    }

    /**
     * Verificar si existe un email
     */
    @GetMapping("/email/{email}/existe")
    @Operation(
        summary = "Verificar email",
        description = "Verifica si ya existe un usuario con el email especificado en la empresa"
    )
    public ResponseEntity<Map<String, Boolean>> verificarEmail(
            @PathVariable String email,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        
              System.out.println("GET /usuarios/email/" + email + "/existe - Empresa: " + empresaId);
        var existe = usuarioService.existeEmailEnEmpresa(email, empresaId);
        
        return ResponseEntity.ok(Map.of("existe", existe));
    }
}