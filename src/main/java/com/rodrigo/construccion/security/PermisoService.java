package com.rodrigo.construccion.security;

import org.springframework.stereotype.Service;

/**
 * Servicio para validar permisos según el rol del usuario
 * 
 * ROLES:
 * - SUPER_ADMIN: Acceso total a todas las empresas y todas las operaciones
 * - CONTRATISTA: Acceso limitado solo a su empresa con permisos específicos
 */
@Service
public class PermisoService {

    /**
     * Verifica si el usuario tiene permiso para MODIFICAR presupuestos
     * 
     * @param rol Rol del usuario (SUPER_ADMIN o CONTRATISTA)
     * @return true si puede modificar
     */
    public boolean puedeModificarPresupuestos(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para ELIMINAR presupuestos
     * 
     * @param rol Rol del usuario
     * @return true si puede eliminar
     */
    public boolean puedeEliminarPresupuestos(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para APROBAR presupuestos
     * 
     * @param rol Rol del usuario
     * @return true si puede aprobar
     */
    public boolean puedeAprobarPresupuestos(String rol) {
        // Tanto SUPER_ADMIN como CONTRATISTA pueden aprobar
        return "SUPER_ADMIN".equals(rol) || "CONTRATISTA".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para ENVIAR presupuestos (PDF)
     * 
     * @param rol Rol del usuario
     * @return true si puede enviar
     */
    public boolean puedeEnviarPresupuestos(String rol) {
        // Tanto SUPER_ADMIN como CONTRATISTA pueden enviar
        return "SUPER_ADMIN".equals(rol) || "CONTRATISTA".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para MODIFICAR obras
     * 
     * @param rol Rol del usuario
     * @return true si puede modificar
     */
    public boolean puedeModificarObras(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para ELIMINAR obras
     * 
     * @param rol Rol del usuario
     * @return true si puede eliminar
     */
    public boolean puedeEliminarObras(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para VER obras
     * 
     * @param rol Rol del usuario
     * @return true si puede ver
     */
    public boolean puedeVerObras(String rol) {
        // Todos pueden ver
        return "SUPER_ADMIN".equals(rol) || "CONTRATISTA".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para ENVIAR obras
     * 
     * @param rol Rol del usuario
     * @return true si puede enviar
     */
    public boolean puedeEnviarObras(String rol) {
        // Tanto SUPER_ADMIN como CONTRATISTA pueden enviar
        return "SUPER_ADMIN".equals(rol) || "CONTRATISTA".equals(rol);
    }

    /**
     * Verifica si el usuario tiene permiso para MODIFICAR empresas
     * 
     * @param rol Rol del usuario
     * @param empresaIdUsuario ID de la empresa del usuario
     * @param empresaIdAModificar ID de la empresa que quiere modificar
     * @return true si puede modificar
     */
    public boolean puedeModificarEmpresa(String rol, Long empresaIdUsuario, Long empresaIdAModificar) {
        if ("SUPER_ADMIN".equals(rol)) {
            return true; // Super admin puede modificar cualquier empresa
        }
        
        // CONTRATISTA solo puede modificar SU PROPIA empresa
        return "CONTRATISTA".equals(rol) && empresaIdUsuario.equals(empresaIdAModificar);
    }

    /**
     * Verifica si el usuario es Super Admin
     * 
     * @param rol Rol del usuario
     * @return true si es super admin
     */
    public boolean esSuperAdmin(String rol) {
        return "SUPER_ADMIN".equals(rol);
    }
}
