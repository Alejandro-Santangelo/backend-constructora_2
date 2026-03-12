package com.rodrigo.construccion.config;

import jakarta.persistence.EntityManager;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * HibernateFilterInterceptor - Interceptor que habilita filtros de Hibernate DENTRO de la transacción
 * 
 * Este interceptor se ejecuta ANTES de cualquier método de repository,
 * asegurando que el filtro de Hibernate esté activo en la sesión correcta.
 * 
 * DESACTIVADO en producción Railway (causa hang en startup con 61 repos)
 */
@Aspect
@Component
@ConditionalOnProperty(name = "app.hibernate-filter-interceptor.enabled", havingValue = "true", matchIfMissing = true)
public class HibernateFilterInterceptor {

    private final EntityManager entityManager;

    public HibernateFilterInterceptor(EntityManager entityManager) {
        this.entityManager = entityManager;
        System.out.println("✅ HibernateFilterInterceptor CREADO");
    }

    /**
     * Intercepta TODOS los métodos de los repositories antes de ejecutarse
     * y habilita el filtro empresaFilter si existe un empresaId en TenantContext
     * 
     * ⚠️ EXCEPCIÓN: No se activa el filtro cuando el usuario es SUPER_ADMIN
     * porque el super admin debe poder ver datos de TODAS las empresas
     */
    @Before("execution(* org.springframework.data.repository.Repository+.*(..))")
    public void enableFilter() {
        Long empresaId = TenantContext.getTenantId();
        Boolean isSuperAdmin = TenantContext.isSuperAdmin();
        
        if (empresaId != null) {
            // 🔓 SUPER_ADMIN: NO aplicar filtro para que vea todos los datos
            if (isSuperAdmin) {
                System.out.println("🔓 HibernateFilterInterceptor: SUPER_ADMIN detectado - filtro DESACTIVADO (ve todas las empresas)");
                return;
            }
            
            try {
                Session session = entityManager.unwrap(Session.class);
                org.hibernate.Filter filter = session.enableFilter("empresaFilter");
                filter.setParameter("empresaId", empresaId);
                
                System.out.println("🎯 HibernateFilterInterceptor: Filtro 'empresaFilter' HABILITADO para empresaId=" + empresaId);
            } catch (Exception e) {
                System.err.println("❌ Error habilitando filtro Hibernate: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("⚠️ HibernateFilterInterceptor: No hay empresaId en TenantContext - filtro NO habilitado");
        }
    }
}
