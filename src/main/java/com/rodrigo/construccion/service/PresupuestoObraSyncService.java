package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Servicio encargado de mantener sincronizado el vínculo entre obras y sus presupuestos.
 * Asegura que obras.presupuesto_no_cliente_id siempre apunte al presupuesto de mayor versión.
 */
@Service
@Transactional
public class PresupuestoObraSyncService {
    
    private static final Logger log = LoggerFactory.getLogger(PresupuestoObraSyncService.class);
    
    @Autowired
    private PresupuestoNoClienteRepository presupuestoRepository;
    
    @Autowired
    private ObraRepository obraRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * Método principal para sincronizar el vínculo obra-presupuesto.
     * Busca el presupuesto de mayor versión para la obra especificada y actualiza el vínculo.
     * 
     * @param obraId ID de la obra a sincronizar
     */
    public void vincularPresupuestoMasReciente(Long obraId) {
        if (obraId == null) {
            log.warn("⚠️ vincularPresupuestoMasReciente llamado con obraId null - se omite la sincronización");
            return;
        }
        
        log.info("🔄 Iniciando sincronización de presupuesto para obra ID: {}", obraId);
        
        try {
            // Buscar todos los presupuestos de esta obra ordenados por versión descendente
            List<PresupuestoNoCliente> presupuestos = presupuestoRepository.findByObra_IdOrderByNumeroVersionDesc(obraId);
            
            if (presupuestos.isEmpty()) {
                // No hay presupuestos para esta obra - establecer vínculo como null
                log.info("📋 No se encontraron presupuestos para obra ID: {} - estableciendo vínculo como NULL", obraId);
                int filasAfectadas = actualizarVinculoSinDisparafListeners(obraId, null);
                if (filasAfectadas > 0) {
                    log.info("✅ Vínculo obra-presupuesto establecido como NULL (sin cambio de estado) para obra ID: {}", obraId);
                } else {
                    log.warn("⚠️ No se pudo actualizar el vínculo para obra ID: {} (obra no encontrada)", obraId);
                }
            } else {
                // Tomar el presupuesto de mayor versión (primero en la lista)
                PresupuestoNoCliente masReciente = presupuestos.get(0);
                
                log.info("📊 Encontrados {} presupuestos para obra ID: {} - El más reciente es ID: {} (versión: {})", 
                    presupuestos.size(), obraId, masReciente.getId(), masReciente.getNumeroVersion());
                
                // Actualizar el vínculo usando SQL nativo para evitar disparar listeners de JPA
                // que pueden cambiar el estado de la obra automáticamente
                int filasAfectadas = actualizarVinculoSinDisparafListeners(obraId, masReciente.getId());
                
                if (filasAfectadas > 0) {
                    log.info("✅ Vínculo actualizado exitosamente (sin cambio de estado) - Obra ID: {} ahora apunta al presupuesto ID: {} (versión: {})", 
                        obraId, masReciente.getId(), masReciente.getNumeroVersion());
                } else {
                    log.warn("⚠️ No se pudo actualizar el vínculo para obra ID: {} (obra no encontrada)", obraId);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error al sincronizar presupuesto para obra ID: {}", obraId, e);
            throw new RuntimeException("Error en la sincronización obra-presupuesto: " + e.getMessage(), e);
        }
    }
    
    /**
     * Método específico para cuando se crea un nuevo presupuesto.
     * Verifica si es la versión más alta y actualiza el vínculo si es necesario.
     * 
     * @param presupuesto El presupuesto recién creado
     */
    public void procesarCreacionPresupuesto(PresupuestoNoCliente presupuesto) {
        if (presupuesto == null || presupuesto.getObra() == null) {
            log.debug("🔍 Presupuesto creado sin obra asociada - no requiere sincronización");
            return;
        }
        
        Long obraId = presupuesto.getObra().getId();
        log.info("🆕 Procesando creación de presupuesto ID: {} (versión: {}) para obra ID: {}", 
            presupuesto.getId(), presupuesto.getNumeroVersion(), obraId);
        
        // Sincronizar para asegurar que el vínculo apunte a la versión más reciente
        vincularPresupuestoMasReciente(obraId);
    }
    
    /**
     * Método específico para cuando se elimina un presupuesto.
     * Busca la siguiente versión más alta y actualiza el vínculo.
     * 
     * @param obraId ID de la obra afectada
     * @param presupuestoEliminadoId ID del presupuesto que fue eliminado (para logging)
     */
    public void procesarEliminacionPresupuesto(Long obraId, Long presupuestoEliminadoId) {
        if (obraId == null) {
            log.debug("🔍 Presupuesto eliminado sin obra asociada - no requiere sincronización");
            return;
        }
        
        log.info("🗑️ Procesando eliminación de presupuesto ID: {} de obra ID: {}", presupuestoEliminadoId, obraId);
        
        // Sincronizar para encontrar la nueva versión más alta o establecer null si no quedan presupuestos
        vincularPresupuestoMasReciente(obraId);
    }
    
    /**
     * Método de verificación para debugging.
     * Muestra el estado actual del vínculo obra-presupuesto.
     * 
     * @param obraId ID de la obra a verificar
     */
    @Transactional(readOnly = true)
    public void verificarVinculoActual(Long obraId) {
        if (obraId == null) {
            return;
        }
        
        log.info("🔍 VERIFICACIÓN - Estado actual del vínculo para obra ID: {}", obraId);
        
        try {
            List<PresupuestoNoCliente> presupuestos = presupuestoRepository.findByObra_IdOrderByNumeroVersionDesc(obraId);
            
            if (presupuestos.isEmpty()) {
                log.info("📋 La obra ID: {} no tiene presupuestos asociados", obraId);
            } else {
                log.info("📊 La obra ID: {} tiene {} presupuestos:", obraId, presupuestos.size());
                for (int i = 0; i < presupuestos.size(); i++) {
                    PresupuestoNoCliente p = presupuestos.get(i);
                    String marcador = (i == 0) ? " ⭐ (MÁS RECIENTE)" : "";
                    log.info("   - ID: {}, Versión: {}, Estado: {}{}", 
                        p.getId(), p.getNumeroVersion(), p.getEstado(), marcador);
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error al verificar vínculo para obra ID: {}", obraId, e);
        }
    }
    
    /**
     * Actualiza el campo presupuesto_no_cliente_id usando SQL nativo para evitar 
     * disparar listeners/interceptores de JPA que puedan cambiar el estado de la obra.
     * 
     * @param obraId ID de la obra a actualizar
     * @param presupuestoId ID del presupuesto a vincular (puede ser null)
     * @return Número de filas afectadas (0 si la obra no existe, 1 si fue exitoso)
     */
    private int actualizarVinculoSinDisparafListeners(Long obraId, Long presupuestoId) {
        try {
            String sql = "UPDATE obras SET presupuesto_no_cliente_id = ? WHERE id_obra = ?";
            
            int filasActualizadas = entityManager.createNativeQuery(sql)
                .setParameter(1, presupuestoId)
                .setParameter(2, obraId)
                .executeUpdate();
                
            log.debug("🔧 SQL ejecutado: {} - Obra ID: {}, Presupuesto ID: {}, Filas afectadas: {}", 
                sql, obraId, presupuestoId, filasActualizadas);
                
            return filasActualizadas;
            
        } catch (Exception e) {
            log.error("❌ Error ejecutando actualización SQL nativa para obra {}: {}", obraId, e.getMessage());
            throw new RuntimeException("Error en actualización SQL nativa: " + e.getMessage(), e);
        }
    }
}