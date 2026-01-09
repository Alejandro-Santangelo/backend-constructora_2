package com.rodrigo.construccion.exception;

/**
 * Excepción para errores relacionados con multi-tenancy
 */
public class TenantException extends RuntimeException {
    
    public TenantException(String message) {
        super(message);
    }
    
    public TenantException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public static TenantException empresaNoEncontrada(Long empresaId) {
        return new TenantException(String.format("Empresa no encontrada o inactiva: %d", empresaId));
    }
    
    public static TenantException accesoNoAutorizado(Long empresaId) {
        return new TenantException(String.format("No tienes acceso a la empresa: %d", empresaId));
    }
    
    public static TenantException tenantHeaderFaltante() {
        return new TenantException("Header X-Tenant-ID es requerido");
    }
}