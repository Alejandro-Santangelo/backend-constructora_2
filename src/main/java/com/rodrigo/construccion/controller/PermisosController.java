package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.security.PermisoService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Controlador para obtener los permisos del usuario actual
 * El frontend usa esto para mostrar/ocultar botones
 */
@RestController
@RequestMapping("/api/permisos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PermisosController {

    private final PermisoService permisoService;

    /**
     * Obtiene todos los permisos del usuario actual
     * 
     * @param rol Rol del usuario autenticado
     * @param empresaId ID de la empresa del usuario
     * @return Map con permisos booleanos
     */
    @GetMapping
    public Map<String, Object> obtenerPermisos(
            @RequestHeader("X-User-Rol") String rol,
            @RequestHeader(value = "X-User-Empresa-Id", required = false) Long empresaId) {
        
        Map<String, Object> permisos = new HashMap<>();
        
        // Información del usuario
        permisos.put("rol", rol);
        permisos.put("empresaId", empresaId);
        permisos.put("esSuperAdmin", permisoService.esSuperAdmin(rol));
        
        // Permisos de Presupuestos
        Map<String, Boolean> presupuestos = new HashMap<>();
        presupuestos.put("ver", true); // Todos pueden ver
        presupuestos.put("modificar", permisoService.puedeModificarPresupuestos(rol));
        presupuestos.put("eliminar", permisoService.puedeEliminarPresupuestos(rol));
        presupuestos.put("aprobar", permisoService.puedeAprobarPresupuestos(rol));
        presupuestos.put("enviar", permisoService.puedeEnviarPresupuestos(rol));
        permisos.put("presupuestos", presupuestos);
        
        // Permisos de Obras
        Map<String, Boolean> obras = new HashMap<>();
        obras.put("ver", permisoService.puedeVerObras(rol));
        obras.put("modificar", permisoService.puedeModificarObras(rol));
        obras.put("eliminar", permisoService.puedeEliminarObras(rol));
        obras.put("enviar", permisoService.puedeEnviarObras(rol));
        permisos.put("obras", obras);
        
        // Permisos de Empresas (el frontend debe pasar la empresaId que quiere modificar)
        Map<String, Boolean> empresas = new HashMap<>();
        empresas.put("ver", true); // Todos pueden ver su empresa
        // Para saber si puede modificar una empresa específica, el frontend debe llamar al endpoint con ese ID
        empresas.put("modificarPropia", empresaId != null && permisoService.puedeModificarEmpresa(rol, empresaId, empresaId));
        permisos.put("empresas", empresas);
        
        return permisos;
    }

    /**
     * Verifica si puede modificar una empresa específica
     * 
     * @param rol Rol del usuario
     * @param empresaIdUsuario ID de la empresa del usuario
     * @param empresaId ID de la empresa a modificar
     * @return true si puede modificar
     */
    @GetMapping("/puede-modificar-empresa/{empresaId}")
    public Map<String, Boolean> puedeModificarEmpresa(
            @RequestHeader("X-User-Rol") String rol,
            @RequestHeader("X-User-Empresa-Id") Long empresaIdUsuario,
            @PathVariable Long empresaId) {
        
        boolean puede = permisoService.puedeModificarEmpresa(rol, empresaIdUsuario, empresaId);
        
        Map<String, Boolean> resultado = new HashMap<>();
        resultado.put("puedeModificar", puede);
        return resultado;
    }
}
