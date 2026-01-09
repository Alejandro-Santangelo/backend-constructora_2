package com.rodrigo.construccion.service;

import com.rodrigo.construccion.enums.PresupuestoEstado;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Servicio para la actualización automática de estados de presupuestos-no-cliente
 * basado en fechas.
 * 
 * REGLAS AUTOMÁTICAS:
 * 1. APROBADO → EN_EJECUCION cuando llega fechaProbableInicio
 * 2. EN_EJECUCION → TERMINADO cuando llega fechaInicio + tiempoEstimadoTerminacion (días hábiles)
 * 
 * SINCRONIZACIÓN: Cuando cambia el estado de un presupuesto, sincroniza automáticamente
 * con la obra asociada (si existe).
 * 
 * Ejecuta diariamente a las 00:00 AM
 */
@Service
public class PresupuestoEstadoAutomaticoService {

    private static final Logger log = LoggerFactory.getLogger(PresupuestoEstadoAutomaticoService.class);
    
    private final PresupuestoNoClienteRepository presupuestoRepository;
    private final ObraRepository obraRepository;
    
    public PresupuestoEstadoAutomaticoService(
            PresupuestoNoClienteRepository presupuestoRepository,
            ObraRepository obraRepository) {
        this.presupuestoRepository = presupuestoRepository;
        this.obraRepository = obraRepository;
    }
    
    /**
     * Proceso automático que se ejecuta diariamente a las 00:00
     * Actualiza estados de presupuestos según fechas
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void actualizarEstadosAutomaticos() {
        try {
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║  INICIO: Actualización automática de estados de presupuestos  ║");
            log.info("╚════════════════════════════════════════════════════════════════╝");
            log.info("📅 Fecha actual: {}", LocalDate.now());
            
            int cambiadosAEnEjecucion = cambiarAprobadosAEnEjecucion();
            int cambiadosATerminado = cambiarEnEjecucionATerminado();
            
            log.info("✅ Actualización completada:");
            log.info("   → {} presupuestos cambiados a EN_EJECUCION", cambiadosAEnEjecucion);
            log.info("   → {} presupuestos cambiados a TERMINADO", cambiadosATerminado);
            log.info("╔════════════════════════════════════════════════════════════════╗");
            log.info("║  FIN: Actualización automática de estados                     ║");
            log.info("╚════════════════════════════════════════════════════════════════╝");
            
        } catch (Exception e) {
            log.error("❌ Error en actualización automática de estados", e);
        }
    }
    
    /**
     * Cambia presupuestos APROBADOS a EN_EJECUCION cuando llega su fecha de inicio
     * 
     * @return Cantidad de presupuestos actualizados
     */
    private int cambiarAprobadosAEnEjecucion() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        
        log.info("🔍 Buscando presupuestos APROBADOS con fecha de inicio <= {}", hoy);
        
        List<PresupuestoNoCliente> presupuestos = presupuestoRepository
            .findByEstadoAndFechaProbableInicioLessThanEqual(PresupuestoEstado.APROBADO, hoy);
        
        log.info("📋 Encontrados {} presupuestos candidatos a cambiar a EN_EJECUCION", presupuestos.size());
        
        int contador = 0;
        for (PresupuestoNoCliente p : presupuestos) {
            // Protección: No sobrescribir cambios manuales recientes (últimas 24 horas)
            if (p.getFechaUltimaModificacionEstado() != null && 
                p.getFechaUltimaModificacionEstado().isAfter(hace24Horas)) {
                log.debug("⏭️  Presupuesto {} saltado - modificado manualmente hace menos de 24hs", p.getId());
                continue;
            }
            
            p.setEstado(PresupuestoEstado.EN_EJECUCION);
            p.setFechaUltimaModificacionEstado(LocalDateTime.now());
            presupuestoRepository.save(p);
            
            // 🔄 SYNC: Actualizar obra relacionada si existe
            try {
                if (p.getObra() != null) {
                    obraRepository.findById(p.getObra().getId()).ifPresent(obra -> {
                        com.rodrigo.construccion.enums.EstadoObra nuevoEstadoObra = convertirEstadoPresupuestoAObra(PresupuestoEstado.EN_EJECUCION);
                        obra.setEstado(nuevoEstadoObra);
                        obraRepository.save(obra);
                        log.info("✅ Estado sincronizado: Presupuesto {} → Obra {} (estado: {})", 
                                 p.getId(), obra.getId(), nuevoEstadoObra.getDisplayName());
                    });
                }
            } catch (Exception e) {
                log.error("❌ Error al sincronizar estado con obra para presupuesto {}: {}", p.getId(), e.getMessage());
            }
            
            contador++;
            
            log.info("🔄 Presupuesto ID {} → EN_EJECUCION (Fecha inicio: {}, Obra: {})", 
                     p.getId(), 
                     p.getFechaProbableInicio(),
                     p.getNombreObra());
        }
        
        return contador;
    }
    
    /**
     * Cambia presupuestos EN_EJECUCION a TERMINADO cuando se completa el tiempo estimado
     * 
     * @return Cantidad de presupuestos actualizados
     */
    private int cambiarEnEjecucionATerminado() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime hace24Horas = LocalDateTime.now().minusHours(24);
        
        log.info("🔍 Buscando presupuestos EN_EJECUCION que deberían estar terminados");
        
        List<PresupuestoNoCliente> presupuestos = presupuestoRepository
            .findByEstado(PresupuestoEstado.EN_EJECUCION);
        
        log.info("📋 Encontrados {} presupuestos EN_EJECUCION", presupuestos.size());
        
        int contador = 0;
        for (PresupuestoNoCliente p : presupuestos) {
            // Validar que tenga los datos necesarios
            if (p.getFechaProbableInicio() == null || p.getTiempoEstimadoTerminacion() == null) {
                log.debug("⏭️  Presupuesto {} saltado - falta fechaProbableInicio o tiempoEstimadoTerminacion", p.getId());
                continue;
            }
            
            // Protección: No sobrescribir cambios manuales recientes (últimas 24 horas)
            if (p.getFechaUltimaModificacionEstado() != null && 
                p.getFechaUltimaModificacionEstado().isAfter(hace24Horas)) {
                log.debug("⏭️  Presupuesto {} saltado - modificado manualmente hace menos de 24hs", p.getId());
                continue;
            }
            
            // Calcular fecha estimada de finalización (días hábiles)
            LocalDate fechaEstimadaFin = calcularFechaConDiasHabiles(
                p.getFechaProbableInicio(), 
                p.getTiempoEstimadoTerminacion()
            );
            
            log.debug("📊 Presupuesto {}: inicio={}, días={}, fin estimada={}, hoy={}", 
                      p.getId(), 
                      p.getFechaProbableInicio(), 
                      p.getTiempoEstimadoTerminacion(),
                      fechaEstimadaFin,
                      hoy);
            
            // Verificar si ya pasó la fecha de finalización
            if (!fechaEstimadaFin.isAfter(hoy)) {
                p.setEstado(PresupuestoEstado.TERMINADO);
                p.setFechaUltimaModificacionEstado(LocalDateTime.now());
                presupuestoRepository.save(p);
                
                // 🔄 SYNC: Actualizar obra relacionada si existe
                try {
                    if (p.getObra() != null) {
                        obraRepository.findById(p.getObra().getId()).ifPresent(obra -> {
                            com.rodrigo.construccion.enums.EstadoObra nuevoEstadoObra = convertirEstadoPresupuestoAObra(PresupuestoEstado.TERMINADO);
                            obra.setEstado(nuevoEstadoObra);
                            obraRepository.save(obra);
                            log.info("✅ Estado sincronizado: Presupuesto {} → Obra {} (estado: {})", 
                                     p.getId(), obra.getId(), nuevoEstadoObra.getDisplayName());
                        });
                    }
                } catch (Exception e) {
                    log.error("❌ Error al sincronizar estado con obra para presupuesto {}: {}", p.getId(), e.getMessage());
                }
                
                contador++;
                
                log.info("🏁 Presupuesto ID {} → TERMINADO (Inicio: {}, Días: {}, Fin estimada: {}, Obra: {})", 
                         p.getId(),
                         p.getFechaProbableInicio(),
                         p.getTiempoEstimadoTerminacion(),
                         fechaEstimadaFin,
                         p.getNombreObra());
            }
        }
        
        return contador;
    }
    
    /**
     * Calcula una fecha sumando días hábiles (lunes a viernes)
     * Excluye sábados y domingos
     * 
     * @param fechaInicio Fecha de inicio
     * @param diasHabiles Cantidad de días hábiles a sumar
     * @return Fecha resultante después de sumar los días hábiles
     */
    public LocalDate calcularFechaConDiasHabiles(LocalDate fechaInicio, int diasHabiles) {
        if (fechaInicio == null || diasHabiles <= 0) {
            return fechaInicio;
        }
        
        LocalDate fecha = fechaInicio;
        int diasContados = 0;
        
        while (diasContados < diasHabiles) {
            fecha = fecha.plusDays(1);
            
            // Verificar que no sea fin de semana
            DayOfWeek diaSemana = fecha.getDayOfWeek();
            if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {
                // No es fin de semana, contar como día hábil
                // TODO: Opcionalmente verificar contra tabla de feriados
                if (!esFeriado(fecha)) {
                    diasContados++;
                }
            }
        }
        
        return fecha;
    }
    
    /**
     * Verifica si una fecha es feriado
     * TODO: Implementar verificación contra tabla de feriados
     * 
     * @param fecha Fecha a verificar
     * @return true si es feriado, false en caso contrario
     */
    private boolean esFeriado(LocalDate fecha) {
        // Por ahora retorna false
        // En el futuro se puede implementar consulta a tabla de feriados
        // SELECT COUNT(*) > 0 FROM feriados WHERE fecha = ?
        return false;
    }
    
    /**
     * Método público para ejecutar manualmente la actualización
     * Útil para testing o ejecución bajo demanda
     */
    @Transactional
    public void ejecutarActualizacionManual() {
        log.info("⚡ Ejecución MANUAL de actualización de estados");
        actualizarEstadosAutomaticos();
    }
    
    /**
     * Convierte un PresupuestoEstado a su equivalente EstadoObra
     * Mapeo necesario porque algunos estados de presupuesto se combinan en uno solo para obras
     * 
     * @param estadoPresupuesto Estado del presupuesto
     * @return Estado equivalente para obra (enum EstadoObra)
     */
    private com.rodrigo.construccion.enums.EstadoObra convertirEstadoPresupuestoAObra(PresupuestoEstado estadoPresupuesto) {
        switch (estadoPresupuesto) {
            case BORRADOR:
            case A_ENVIAR:
                return com.rodrigo.construccion.enums.EstadoObra.BORRADOR;
            case ENVIADO:
                return com.rodrigo.construccion.enums.EstadoObra.ENVIADO;
            case APROBADO:
                return com.rodrigo.construccion.enums.EstadoObra.APROBADO;
            case MODIFICADO:
                return com.rodrigo.construccion.enums.EstadoObra.MODIFICADO;
            case EN_EJECUCION:
                return com.rodrigo.construccion.enums.EstadoObra.EN_EJECUCION;
            case TERMINADO:
                return com.rodrigo.construccion.enums.EstadoObra.TERMINADO;
            default:
                // Por defecto, mapear a BORRADOR para estados desconocidos
                return com.rodrigo.construccion.enums.EstadoObra.BORRADOR;
        }
    }
}
