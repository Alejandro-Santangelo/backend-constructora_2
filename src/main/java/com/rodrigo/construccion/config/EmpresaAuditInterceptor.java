package com.rodrigo.construccion.config;

import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.repository.EmpresaRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.CallbackException;
import org.hibernate.Interceptor;
import org.hibernate.type.Type;
import org.springframework.stereotype.Component;

/**
 * Interceptor de Hibernate que setea automáticamente la empresa
 * en entidades que tienen relación con Empresa antes de guardarlas
 */
@Component
@RequiredArgsConstructor
public class EmpresaAuditInterceptor implements Interceptor {

    private final EmpresaRepository empresaRepository;

    @Override
    public boolean onSave(Object entity, Object id, Object[] state, String[] propertyNames, Type[] types) throws CallbackException {
        if (entity instanceof Profesional) {
            Profesional profesional = (Profesional) entity;
            
            // Solo setear si no tiene empresa ya asignada
            if (profesional.getEmpresa() == null) {
                Long empresaId = TenantContext.getTenantId();
                
                if (empresaId != null) {
                    System.out.println("🔧 EmpresaAuditInterceptor - Seteando empresa " + empresaId + " en Profesional");
                    
                    Empresa empresa = empresaRepository.findById(empresaId).orElse(null);
                    if (empresa != null) {
                        profesional.setEmpresa(empresa);
                        
                        // Actualizar el state para que Hibernate guarde el cambio
                        for (int i = 0; i < propertyNames.length; i++) {
                            if ("empresa".equals(propertyNames[i])) {
                                state[i] = empresa;
                                return true; // Indica que el state fue modificado
                            }
                        }
                    }
                }
            }
        }
        
        return false;
    }
}
