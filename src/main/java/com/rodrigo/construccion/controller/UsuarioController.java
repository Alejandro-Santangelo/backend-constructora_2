package com.rodrigo.construccion.controller;


import com.rodrigo.construccion.dto.request.CambiarPasswordRequest;
import com.rodrigo.construccion.dto.response.UsuarioEstadisticasDTO;
import com.rodrigo.construccion.model.entity.Usuario;
import com.rodrigo.construccion.service.UsuarioService;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final UsuarioService usuarioService;

    /* Crear un nuevo usuario */
    @PostMapping
    @Operation(summary = "Crear usuario", description = "Registra un nuevo usuario. Puede asignarse a una o múltiples empresas.")
    public ResponseEntity<Usuario> crearUsuario(
            @Valid @RequestBody Usuario usuario, 
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa principal") Long empresaId,
            @RequestParam(required = false) @Parameter(description = "IDs de empresas permitidas (separadas por coma). Si no se envía, solo tendrá acceso a la empresa principal.") String empresasPermitidas) {
        
        // 🆕 SISTEMA MULTI-EMPRESA
        // Si viene el parámetro empresasPermitidas, parsearlo
        if (empresasPermitidas != null && !empresasPermitidas.trim().isEmpty()) {
            List<Long> empresasIds = new ArrayList<>();
            try {
                String[] ids = empresasPermitidas.split(",");
                for (String id : ids) {
                    empresasIds.add(Long.parseLong(id.trim()));
                }
                // Usar el método multi-empresa
                var usuarioNuevo = usuarioService.crearUsuarioMultiEmpresa(usuario, empresaId, empresasIds);
                return ResponseEntity.status(HttpStatus.CREATED).body(usuarioNuevo);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Formato inválido en empresasPermitidas. Use IDs numéricos separados por coma.");
            }
        } else {
            // Si no viene, usar el método tradicional (una sola empresa)
            var usuarioNuevo = usuarioService.crearUsuario(usuario, empresaId);
            return ResponseEntity.status(HttpStatus.CREATED).body(usuarioNuevo);
        }
    }

    /* Obtener todos los usuarios con paginación */
    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Obtiene todos los usuarios de la empresa con paginación")
    public ResponseEntity<Page<Usuario>> obtenerUsuarios(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.obtenerPorEmpresaConPaginacion(empresaId, pageable));
    }

    /* Obtener usuario por ID */
    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario", description = "Obtiene un usuario específico por su ID")
    public ResponseEntity<Usuario> obtenerUsuario(@PathVariable Long id, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.obtenerPorIdYEmpresa(id, empresaId));
    }

    /* Actualizar usuario */
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Actualiza un usuario existente")
    public ResponseEntity<Usuario> actualizarUsuario(@PathVariable Long id, @Valid @RequestBody Usuario usuarioActualizado,
                                                     @RequestHeader("X-Tenant-ID")
                                                     @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.actualizarUsuario(id, usuarioActualizado, empresaId));
    }

    /* Eliminar usuario */
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar usuario", description = "Elimina un usuario del sistema (soft delete)")
    public ResponseEntity<String> eliminarUsuario(@PathVariable Long id, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        usuarioService.eliminarUsuario(id, empresaId);
        return ResponseEntity.ok("Usuario eliminado exitosamente");
    }

    /* Obtener usuarios por rol */
    @GetMapping("/rol/{rol}")
    @Operation(summary = "Usuarios por rol", description = "Obtiene todos los usuarios con un rol específico en la empresa")
    public ResponseEntity<List<Usuario>> obtenerUsuariosPorRol(@PathVariable String rol, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.obtenerPorRolYEmpresa(rol, empresaId));
    }

    /* Buscar usuarios por nombre */
    @GetMapping("/buscar")
    @Operation(summary = "Buscar usuarios", description = "Busca usuarios por nombre con búsqueda parcial")
    public ResponseEntity<Page<Usuario>> buscarUsuarios(@RequestParam String nombre, @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId,
                                                        @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(usuarioService.buscarPorNombre(empresaId, nombre, pageable));
    }

    /* GESTIÓN DE CONTRASEÑAS Y AUTENTICACIÓN */

    /* Cambiar contraseña */
    @PutMapping("/{id}/password")
    @Operation(summary = "Cambiar contraseña", description = "Cambia la contraseña de un usuario")
    public ResponseEntity<String> cambiarPassword(@PathVariable Long id, @Valid @RequestBody CambiarPasswordRequest request,
                                                  @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        usuarioService.cambiarPassword(id, request.getPasswordActual(), request.getPasswordNueva(), empresaId);
        return ResponseEntity.ok("Contraseña actualizada exitosamente");
    }

    /* Activar/Desactivar usuario */
    @PutMapping("/{id}/estado")
    @Operation(summary = "Cambiar estado usuario", description = "Activa o desactiva un usuario")
    public ResponseEntity<String> cambiarEstado(@PathVariable Long id, @RequestParam Boolean activo,
                                                @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        usuarioService.cambiarEstado(id, activo, empresaId);
        String mensaje = activo ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";
        return ResponseEntity.ok(mensaje);
    }

    /* ESTADÍSTICAS Y MÉTRICAS */
    /* Obtener estadísticas de usuarios */
    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de usuarios", description = "Obtiene estadísticas generales de usuarios de la empresa")
    public ResponseEntity<UsuarioEstadisticasDTO> obtenerEstadisticas(@RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.obtenerEstadisticas(empresaId));
    }

    /* Contar usuarios activos */
    @GetMapping("/contar/activos")
    @Operation(summary = "Contar usuarios activos", description = "Cuenta el total de usuarios activos de la empresa")
    public ResponseEntity<Long> contarUsuariosActivos(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.contarUsuariosActivosPorEmpresa(empresaId));
    }

    /* ---------- MÉTODOS QUE DEBEN SER ELIMINADOS PORQUE NO ESTÁN SIENDO USADOS POR EL FRONTEND ----------- */

    /* Obtener administradores */
    @GetMapping("/administradores")
    @Operation(summary = "Obtener administradores", description = "Obtiene todos los usuarios administradores de la empresa")
    public ResponseEntity<List<Usuario>> obtenerAdministradores(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {

        return ResponseEntity.ok(usuarioService.obtenerAdministradores(empresaId));
    }

    /* Obtener usuarios activos */
    @GetMapping("/activos")
    @Operation(summary = "Usuarios activos", description = "Obtiene todos los usuarios activos de la empresa")
    public ResponseEntity<List<Usuario>> obtenerUsuariosActivos(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {

        return ResponseEntity.ok(usuarioService.obtenerActivosPorEmpresa(empresaId));
    }

    /* Obtener usuario por email */
    @GetMapping("/email/{email}")
    @Operation(summary = "Buscar por email", description = "Busca un usuario específico por su email en la empresa")
    public ResponseEntity<Usuario> obtenerUsuarioPorEmail(
            @PathVariable String email,
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {

        return usuarioService.obtenerPorEmailYEmpresa(email, empresaId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /* Contar usuarios por empresa */
    @GetMapping("/contar")
    @Operation(summary = "Contar usuarios", description = "Cuenta el total de usuarios de la empresa")
    public ResponseEntity<Long> contarUsuarios(
            @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.contarUsuariosPorEmpresa(empresaId));
    }

    /* Verificar si existe un usuario */
    @GetMapping("/{id}/existe")
    @Operation(summary = "Verificar existencia", description = "Verifica si existe un usuario con el ID especificado en la empresa")
    public ResponseEntity<String> verificarExistencia(@PathVariable Long id,
                                                      @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {
        return ResponseEntity.ok(usuarioService.existeUsuarioEnEmpresa(id, empresaId) ? "El usuario existe" : "El Usuario no existe");
    }

    /* Verificar si existe un email */
    @GetMapping("/email/{email}/existe")
    @Operation(summary = "Verificar email", description = "Verifica si ya existe un usuario con el email especificado en la empresa")
    public ResponseEntity<String> verificarEmail(@PathVariable String email,
                                                 @RequestHeader("X-Tenant-ID") @Parameter(description = "ID de la empresa") Long empresaId) {

        return ResponseEntity.ok(usuarioService.existeEmailEnEmpresa(email, empresaId) ? "El email existe" : "El email no existe");
    }

}

