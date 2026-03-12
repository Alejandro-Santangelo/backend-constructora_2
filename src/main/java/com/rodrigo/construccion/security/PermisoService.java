package com.rodrigo.construccion.security;

import org.springframework.stereotype.Service;
import java.util.Arrays;
import java.util.List;

/**
 * Servicio para validar permisos según el rol del usuario.
 * 
 * Roles:
 * - SUPER_ADMIN: Acceso completo a todas las secciones
 * - CONTRATISTA: Solo acceso a Presupuestos y Obras
 */
@Service
public class PermisoService {

    /**
     * Retorna la lista de SECCIONES a las que el usuario tiene acceso.
     * 
     * SUPER_ADMIN: Todas las secciones
     * CONTRATISTA: Solo Presupuestos y Obras
     */
    public List<String> getSeccionesPermitidas(String rol) {
        if ("SUPER_ADMIN".equals(rol)) {
            return Arrays.asList(
                "empresas",
                "presupuestos", 
                "obras",
                "clientes",
                "profesionales",
                "materiales",
                "gastos-generales",
                "proveedores",
                "pagos-cobros-retiros",
                "profesionales-por-obra",
                "trabajos-diarios",
                "nuevos-clientes",
                "reportes"
            );
        }
        
        if ("CONTRATISTA".equals(rol)) {
            return Arrays.asList(
                "presupuestos",
                "obras",
                "usuarios"
            );
        }
        
        return Arrays.asList(); // Sin acceso por defecto
    }

    /**
     * Verifica si el usuario tiene acceso a una sección específica.
     */
    public boolean tieneAccesoASeccion(String rol, String seccion) {
        return getSeccionesPermitidas(rol).contains(seccion);
    }

    // ===================== MÉTODOS DE VALIDACIÓN ESPECÍFICOS =====================
    // Usados por los controllers para validar operaciones específicas

    public boolean puedeModificarPresupuestos(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    public boolean puedeEliminarPresupuestos(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    public boolean puedeModificarObras(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    public boolean puedeEliminarObras(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    public boolean puedeModificarEmpresa(String rol, Long empresaIdUsuario, Long empresaIdAModificar) {
        return "SUPER_ADMIN".equals(rol); // Solo super admin puede modificar empresas
    }
}
