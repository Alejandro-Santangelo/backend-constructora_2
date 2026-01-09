package com.rodrigo.construccion.scheduler;

import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.enums.PresupuestoEstado;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Job programado para actualizar automáticamente el estado de presupuestos y obras
 * 
 * FUNCIÓN:
 * - Se ejecuta diariamente a las 7:00 AM (hora de Argentina/Buenos Aires)
 * - Cambia presupuestos APROBADOS → EN_EJECUCION cuando llega fechaProbableInicio
 * - Cambia obras APROBADAS → EN_EJECUCION sincronizadas con sus presupuestos
 * - Cambia presupuestos EN_EJECUCION → TERMINADO cuando se completa el tiempo estimado
 * - Cambia obras EN_EJECUCION → TERMINADO sincronizadas con sus presupuestos
 * 
 * REQUISITOS para APROBADO → EN_EJECUCION:
 * - Presupuesto debe estar en estado APROBADO
 * - Presupuesto debe tener fechaProbableInicio no nula
 * - La fechaProbableInicio debe ser <= fecha actual
 * - Si tiene obra asociada, sincronizar el estado
 * 
 * REQUISITOS para EN_EJECUCION → TERMINADO:
 * - Presupuesto debe estar en estado EN_EJECUCION
 * - Debe tener fechaProbableInicio y tiempoEstimadoTerminacion
 * - La fecha calculada de finalización debe ser <= fecha actual
 * - Si tiene obra asociada, sincronizar el estado
 * 
 * @author Sistema Rodrigo
 * @since 2025-12-16
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ObraEstadoScheduler {

    private final ObraRepository obraRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;

    /**
     * Job que se ejecuta diariamente a las 7:00 AM (hora de Argentina)
     * 
     * Cron expression: "0 0 7 * * ?" = segundo minuto hora día mes día-semana
     * - 0: segundo 0
     * - 0: minuto 0
     * - 7: hora 7
     * - *: todos los días del mes
     * - *: todos los meses
     * - ?: cualquier día de la semana
     * 
     * Zona horaria: America/Argentina/Buenos_Aires
     */
    @Scheduled(cron = "0 0 7 * * ?", zone = "America/Argentina/Buenos_Aires")
    @Transactional
    public void actualizarEstadosAutomaticos() {
        ZonedDateTime horaEjecucion = ZonedDateTime.now(ZoneId.of("America/Argentina/Buenos_Aires"));
        LocalDate fechaActual = horaEjecucion.toLocalDate();
        
        log.info("══════════════════════════════════════════════════════════════════════");
        log.info("🔄 INICIANDO JOB: Actualización automática de estados");
        log.info("⏰ Hora de ejecución: {}", horaEjecucion);
        log.info("📅 Fecha de evaluación: {}", fechaActual);
        log.info("══════════════════════════════════════════════════════════════════════");

        try {
            // 1. Cambiar APROBADOS → EN_EJECUCION
            int cambiadosAEnEjecucion = cambiarAprobadosAEnEjecucion(fechaActual);
            
            // 2. Cambiar EN_EJECUCION → TERMINADO
            int cambiadosATerminado = cambiarEnEjecucionATerminado(fechaActual);

            // 3. Resumen de ejecución
            log.info("══════════════════════════════════════════════════════════════════════");
            log.info("📈 RESUMEN DE EJECUCIÓN:");
            log.info("   ✅ Presupuestos cambiados a EN_EJECUCION: {}", cambiadosAEnEjecucion);
            log.info("   ✅ Presupuestos cambiados a TERMINADO: {}", cambiadosATerminado);
            log.info("══════════════════════════════════════════════════════════════════════");
            log.info("✅ JOB FINALIZADO CORRECTAMENTE");
            log.info("══════════════════════════════════════════════════════════════════════");

        } catch (Exception e) {
            log.error("══════════════════════════════════════════════════════════════════════");
            log.error("💥 ERROR CRÍTICO en job de actualización de estados: {}", e.getMessage(), e);
            log.error("══════════════════════════════════════════════════════════════════════");
            // No lanzamos excepción para que el scheduler continúe funcionando
        }
    }

    /**
     * Cambia presupuestos APROBADOS a EN_EJECUCION cuando llega su fecha de inicio
     * También sincroniza las obras asociadas
     * 
     * @param fechaActual Fecha actual de evaluación
     * @return Cantidad de presupuestos actualizados
     */
    private int cambiarAprobadosAEnEjecucion(LocalDate fechaActual) {
        log.info("───────────────────────────────────────────────────────────────────────");
        log.info("🔍 ETAPA 1: Buscando presupuestos APROBADOS con fecha de inicio <= {}", fechaActual);
        
        // Buscar presupuestos APROBADOS cuya fecha de inicio ya llegó
        List<PresupuestoNoCliente> presupuestos = presupuestoRepository
            .findByEstadoAndFechaProbableInicioLessThanEqual(PresupuestoEstado.APROBADO, fechaActual);
        
        log.info("📋 Encontrados {} presupuestos candidatos", presupuestos.size());
        
        if (presupuestos.isEmpty()) {
            log.info("✅ No hay presupuestos APROBADOS que deban cambiar a EN_EJECUCION");
            return 0;
        }
        
        int contador = 0;
        for (PresupuestoNoCliente presupuesto : presupuestos) {
            try {
                // 1. Actualizar estado del presupuesto
                PresupuestoEstado estadoAnterior = presupuesto.getEstado();
                presupuesto.setEstado(PresupuestoEstado.EN_EJECUCION);
                presupuestoRepository.save(presupuesto);
                
                log.info("✅ Presupuesto ID {} → EN_EJECUCION", presupuesto.getId());
                log.info("   📅 Fecha probable inicio: {}", presupuesto.getFechaProbableInicio());
                log.info("   📝 Nombre: {}", presupuesto.getNombreObra());
                log.info("   🔄 Estado anterior: {}", estadoAnterior.getDisplayValue());
                
                // 2. Sincronizar obra asociada si existe
                if (presupuesto.getObra() != null) {
                    Obra obra = presupuesto.getObra();
                    String estadoObraAnterior = obra.getEstado();
                    
                    // Setear estado usando el enum
                    obra.setEstado(EstadoObra.EN_EJECUCION);
                    
                    // Si no tiene fecha de inicio, asignarle la actual
                    if (obra.getFechaInicio() == null) {
                        obra.setFechaInicio(fechaActual);
                        log.info("   📅 Obra: Asignada fecha de inicio: {}", fechaActual);
                    }
                    
                    obraRepository.save(obra);
                    
                    log.info("   🏗️ Obra ID {} sincronizada → EN_EJECUCION", obra.getId());
                    log.info("   🔄 Obra estado anterior: {}", estadoObraAnterior);
                }
                
                contador++;
                log.info("───────────────────────────────────────────────────────────────────────");
                
            } catch (Exception e) {
                log.error("❌ Error al actualizar presupuesto ID {}: {}", presupuesto.getId(), e.getMessage(), e);
            }
        }
        
        return contador;
    }
    
    /**
     * Cambia presupuestos EN_EJECUCION a TERMINADO cuando se completa el tiempo estimado
     * También sincroniza las obras asociadas
     * 
     * @param fechaActual Fecha actual de evaluación
     * @return Cantidad de presupuestos actualizados
     */
    private int cambiarEnEjecucionATerminado(LocalDate fechaActual) {
        log.info("───────────────────────────────────────────────────────────────────────");
        log.info("🔍 ETAPA 2: Buscando presupuestos EN_EJECUCION que deberían estar terminados");
        
        List<PresupuestoNoCliente> presupuestos = presupuestoRepository
            .findByEstado(PresupuestoEstado.EN_EJECUCION);
        
        log.info("📋 Encontrados {} presupuestos EN_EJECUCION", presupuestos.size());
        
        if (presupuestos.isEmpty()) {
            log.info("✅ No hay presupuestos EN_EJECUCION");
            return 0;
        }
        
        int contador = 0;
        for (PresupuestoNoCliente presupuesto : presupuestos) {
            try {
                // Validar que tenga los datos necesarios
                if (presupuesto.getFechaProbableInicio() == null || 
                    presupuesto.getTiempoEstimadoTerminacion() == null) {
                    log.debug("⏭️ Presupuesto {} saltado - falta fechaProbableInicio o tiempoEstimadoTerminacion", 
                             presupuesto.getId());
                    continue;
                }
                
                // Calcular fecha estimada de finalización (días hábiles)
                LocalDate fechaEstimadaFin = calcularFechaEstimadaFinalizacion(
                    presupuesto.getFechaProbableInicio(),
                    presupuesto.getTiempoEstimadoTerminacion()
                );
                
                // Verificar si ya debe estar terminado
                if (fechaActual.isBefore(fechaEstimadaFin)) {
                    log.debug("⏳ Presupuesto {} aún no debe terminar - Fecha estimada fin: {}", 
                             presupuesto.getId(), fechaEstimadaFin);
                    continue;
                }
                
                // 1. Actualizar estado del presupuesto
                PresupuestoEstado estadoAnterior = presupuesto.getEstado();
                presupuesto.setEstado(PresupuestoEstado.TERMINADO);
                presupuestoRepository.save(presupuesto);
                
                log.info("✅ Presupuesto ID {} → TERMINADO", presupuesto.getId());
                log.info("   📅 Fecha inicio: {}", presupuesto.getFechaProbableInicio());
                log.info("   📅 Fecha estimada fin: {}", fechaEstimadaFin);
                log.info("   ⏱️ Tiempo estimado: {} días hábiles", presupuesto.getTiempoEstimadoTerminacion());
                log.info("   📝 Nombre: {}", presupuesto.getNombreObra());
                
                // 2. Sincronizar obra asociada si existe
                if (presupuesto.getObra() != null) {
                    Obra obra = presupuesto.getObra();
                    String estadoObraAnterior = obra.getEstado();
                    
                    // Setear estado usando el enum
                    obra.setEstado(EstadoObra.TERMINADO);
                    
                    // Si no tiene fecha de fin, asignarle la estimada
                    if (obra.getFechaFin() == null) {
                        obra.setFechaFin(fechaEstimadaFin);
                        log.info("   📅 Obra: Asignada fecha de fin: {}", fechaEstimadaFin);
                    }
                    
                    obraRepository.save(obra);
                    
                    log.info("   🏗️ Obra ID {} sincronizada → TERMINADO", obra.getId());
                    log.info("   🔄 Obra estado anterior: {}", estadoObraAnterior);
                }
                
                contador++;
                log.info("───────────────────────────────────────────────────────────────────────");
                
            } catch (Exception e) {
                log.error("❌ Error al actualizar presupuesto ID {}: {}", presupuesto.getId(), e.getMessage(), e);
            }
        }
        
        return contador;
    }
    
    /**
     * Calcula la fecha estimada de finalización sumando días hábiles a la fecha de inicio
     * Omite sábados y domingos
     * 
     * @param fechaInicio Fecha de inicio de la obra
     * @param diasHabiles Cantidad de días hábiles estimados
     * @return Fecha estimada de finalización
     */
    private LocalDate calcularFechaEstimadaFinalizacion(LocalDate fechaInicio, Integer diasHabiles) {
        LocalDate fecha = fechaInicio;
        int diasAgregados = 0;
        
        while (diasAgregados < diasHabiles) {
            fecha = fecha.plusDays(1);
            
            // Omitir sábados y domingos
            DayOfWeek diaSemana = fecha.getDayOfWeek();
            if (diaSemana != DayOfWeek.SATURDAY && diaSemana != DayOfWeek.SUNDAY) {
                diasAgregados++;
            }
        }
        
        return fecha;
    }

    /**
     * Método auxiliar para testing manual (NO se ejecuta automáticamente)
     * Puede ser invocado desde un endpoint REST si es necesario
     */
    public void ejecutarManualmente() {
        log.warn("⚠️ EJECUCIÓN MANUAL del job de actualización de estados");
        actualizarEstadosAutomaticos();
    }
}
