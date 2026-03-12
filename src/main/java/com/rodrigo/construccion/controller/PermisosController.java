package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.security.PermisoService;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.HashMap;
import java.util.Map;
import java.util.List;

/**
 * Controlador para obtener permisos y secciones permitidas por rol.
 * 
 * El frontend usa /api/permisos/secciones para saber qué tarjetas/menús mostrar:
 * - SUPER_ADMIN: Ve todas las secciones
 * - CONTRATISTA: Solo ve Presupuestos y Obras
 */
@Tag(name = "Permisos", description = "Gestión de permisos y acceso por rol")
@RestController
@RequestMapping("/api/permisos")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PermisosController {

    private final PermisoService permisoService;

    /**
     * Endpoint principal: retorna las secciones a las que el usuario tiene acceso.
     * 
     * SUPER_ADMIN retorna: ["empresas", "presupuestos", "obras", "clientes", ...]
     * CONTRATISTA retorna: ["presupuestos", "obras"]
     * 
     * El frontend oculta las tarjetas que no estén en la lista.
     */
    @GetMapping("/secciones")
    @Operation(
        summary = "Obtener secciones permitidas",
        description = "Retorna la lista de secciones a las que el usuario tiene acceso según su rol. " +
                     "SUPER_ADMIN: todas las secciones. CONTRATISTA: solo presupuestos y obras."
    )
    public Map<String, Object> getSeccionesPermitidas(
            @RequestHeader(value = "X-User-Rol", required = true) String rol) {
        
        List<String> secciones = permisoService.getSeccionesPermitidas(rol);
        
        Map<String, Object> response = new HashMap<>();
        response.put("rol", rol);
        response.put("secciones", secciones);
        response.put("esSuperAdmin", "SUPER_ADMIN".equals(rol));
        
        return response;
    }

    /**
     * Verifica si el usuario tiene acceso a una sección específica.
     */
    @GetMapping("/verificar/{seccion}")
    @Operation(
        summary = "Verificar acceso a sección",
        description = "Verifica si el usuario tiene acceso a una sección específica."
    )
    public Map<String, Object> verificarAccesoASeccion(
            @PathVariable String seccion,
            @RequestHeader(value = "X-User-Rol", required = true) String rol) {
        
        boolean tieneAcceso = permisoService.tieneAccesoASeccion(rol, seccion);
        
        Map<String, Object> response = new HashMap<>();
        response.put("seccion", seccion);
        response.put("tieneAcceso", tieneAcceso);
        
        return response;
    }
}
