package com.rodrigo.construccion.config;

/**
 * TenantContext - ThreadLocal para gestionar el empresaId actual en cada request
 * 
 * Esta clase mantiene el contexto de tenant (empresa) por hilo de ejecución,
 * permitiendo filtrado automático de datos por empresa en Hibernate.
 */
public class TenantContext {
    
    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();

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
     * Limpia el contexto del tenant para el hilo actual
     * Debe llamarse siempre en un bloque finally para evitar memory leaks
     */
    public static void clear() {
        CURRENT_TENANT.remove();
    }
}
