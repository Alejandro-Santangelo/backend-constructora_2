package com.rodrigo.construccion.config;

/**
 * TenantContext - ThreadLocal para gestionar el empresaId actual en cada request
 * 
 * Esta clase mantiene el contexto de tenant (empresa) por hilo de ejecución,
 * permitiendo filtrado automático de datos por empresa en Hibernate.
 */
public class TenantContext {
    
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Boolean> IS_SUPER_ADMIN = new ThreadLocal<>();

    /**
     * Establece el ID de la empresa actual para el hilo de ejecución actual
     * 
     * @param tenantId ID de la empresa (tenant)
     */
    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    /**
     * Obtiene el ID de la empresa actual del hilo de ejecución actual
     * 
     * @return ID de la empresa o null si no está establecido
     */
    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }
    
    /**
     * Establece si el usuario actual es Super Admin
     * 
     * @param superAdmin true si es super admin, false si no
     */
    public static void setSuperAdmin(Boolean superAdmin) {
        IS_SUPER_ADMIN.set(superAdmin);
    }
    
    /**
     * Verifica si el usuario actual es Super Admin
     * 
     * @return true si es super admin, false si no
     */
    public static Boolean isSuperAdmin() {
        return Boolean.TRUE.equals(IS_SUPER_ADMIN.get());
    }

    /**
     * Limpia el contexto del tenant para el hilo actual
     * Debe llamarse siempre en un bloque finally para evitar memory leaks
     */
    public static void clear() {
        CURRENT_TENANT.remove();
        IS_SUPER_ADMIN.remove();
    }
}
