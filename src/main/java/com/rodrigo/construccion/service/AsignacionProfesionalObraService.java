package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignacionProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalObraDTO;
import com.rodrigo.construccion.exception.BusinessException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.AsignacionProfesionalObra;
import com.rodrigo.construccion.model.entity.ItemCalculadoraPresupuesto;
import com.rodrigo.construccion.model.entity.JornalCalculadora;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.repository.AsignacionProfesionalObraRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import com.rodrigo.construccion.repository.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service para gestionar asignaciones de profesionales a rubros de obras
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AsignacionProfesionalObraService {

    private final AsignacionProfesionalObraRepository asignacionRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ObraRepository obraRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;

    /**
     * Obtiene todas las asignaciones de una obra
     */
    @Transactional(readOnly = true)
    public List<AsignacionProfesionalObraDTO> obtenerAsignacionesPorObra(Long obraId, Long empresaId) {
        log.info("📋 Obteniendo asignaciones para obra {} empresa {}", obraId, empresaId);
        
        // Filtrar solo asignaciones ACTIVAS
        List<AsignacionProfesionalObra> asignaciones = asignacionRepository.findByObra_IdAndEmpresaIdAndEstado(obraId, empresaId, "ACTIVO");
        
        log.info("✅ Encontradas {} asignaciones activas", asignaciones.size());
        
        return asignaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea una nueva asignación de profesional a rubro de obra
     */
    @Transactional
    public AsignacionProfesionalObraDTO crearAsignacion(Long obraId, Long empresaId, AsignacionProfesionalRequestDTO request) {
        log.info("🆕 Creando asignación: profesional={}, obra={}, rubro={}, tipo={}", 
                 request.getProfesionalId(), obraId, request.getRubroId(), request.getTipoAsignacion());

        // Validar profesional
        Profesional profesional = profesionalRepository.findById(request.getProfesionalId())
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con ID: " + request.getProfesionalId()));

        if (!profesional.getActivo()) {
            throw new BusinessException("El profesional no está activo");
        }

        // Validar obra
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        // Obtener presupuesto de la obra (toma la versión más reciente ordenada por numeroVersion DESC)
        PresupuestoNoCliente presupuesto = presupuestoRepository.findByObra_IdOrderByNumeroVersionDesc(obraId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                    "No se encontró presupuesto asociado a la obra ID: " + obraId));

        log.info("✅ Presupuesto encontrado: ID={}", presupuesto.getId());

        // Validaciones específicas para asignación por JORNAL
        if ("JORNAL".equals(request.getTipoAsignacion())) {
            if (request.getCantidadJornales() == null || request.getCantidadJornales() <= 0) {
                throw new BusinessException("Debe especificar cantidad de jornales para asignación tipo JORNAL");
            }

            // NUEVA LÓGICA: Validar jornales disponibles EN EL PRESUPUESTO, no en el profesional
            log.info("🔍 Validando jornales del presupuesto: obraId={}, rubroId={}, itemId={}", 
                     obraId, request.getRubroId(), request.getItemId());

            // Paso 1: Buscar el rubro (ItemCalculadoraPresupuesto) dentro del presupuesto
            ItemCalculadoraPresupuesto rubro = presupuesto.getItemsCalculadora().stream()
                    .filter(item -> item.getId().equals(request.getRubroId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el rubro ID: " + request.getRubroId() + 
                        " en el presupuesto ID: " + presupuesto.getId()));

            log.info("✅ Rubro encontrado: ID={}, tipo={}", rubro.getId(), rubro.getTipoProfesional());

            // Paso 2: Buscar el jornal específico dentro del rubro
            JornalCalculadora jornal = rubro.getJornales().stream()
                    .filter(j -> j.getId().equals(request.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "No se encontró el jornal ID: " + request.getItemId() + 
                        " en el rubro ID: " + request.getRubroId()));

            log.info("✅ Jornal encontrado: ID={}, rol={}, cantidad={}", 
                     jornal.getId(), jornal.getRol(), jornal.getCantidad());

            // Paso 3: Calcular jornales ya asignados para este item específico
            Integer jornalesAsignados = asignacionRepository.sumJornalesAsignadosByObraAndItem(
                obraId, 
                request.getItemId()
            );
            
            if (jornalesAsignados == null) {
                jornalesAsignados = 0;
            }

            // Paso 4: Calcular jornales disponibles
            int cantidadTotal = jornal.getCantidad().intValue();
            int jornalesDisponibles = cantidadTotal - jornalesAsignados;

            log.info("📊 Jornales del presupuesto - Total: {}, Asignados: {}, Disponibles: {}, Solicitados: {}", 
                     cantidadTotal, jornalesAsignados, jornalesDisponibles, request.getCantidadJornales());

            // Paso 5: Validar que hay suficientes jornales disponibles
            if (request.getCantidadJornales() > jornalesDisponibles) {
                throw new BusinessException(String.format(
                    "Jornales insuficientes. Disponibles: %d, Solicitados: %d", 
                    jornalesDisponibles, 
                    request.getCantidadJornales()
                ));
            }

            log.info("✅ Validación de jornales exitosa");
        }

        // Validar fechas
        if (request.getFechaInicio() != null && request.getFechaFin() != null) {
            if (request.getFechaFin().isBefore(request.getFechaInicio())) {
                throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
            }
        }

        // Crear la asignación
        AsignacionProfesionalObra asignacion = new AsignacionProfesionalObra();
        asignacion.setProfesional(profesional);
        asignacion.setObra(obra);
        asignacion.setRubroId(request.getRubroId());
        asignacion.setItemId(request.getItemId());
        asignacion.setRubroNombre(request.getRubroNombre());
        asignacion.setPresupuestoNoClienteId(presupuesto.getId()); // ✅ Guardar el ID del presupuesto
        asignacion.setTipoAsignacion(request.getTipoAsignacion());
        asignacion.setCantidadJornales(request.getCantidadJornales());
        asignacion.setJornalesUtilizados(0);
        asignacion.setFechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now());
        asignacion.setFechaFin(request.getFechaFin());
        asignacion.setEstado("ACTIVO");
        asignacion.setObservaciones(request.getObservaciones());
        asignacion.setEmpresaId(empresaId);
        asignacion.setFechaModificacion(LocalDateTime.now());

        AsignacionProfesionalObra saved = asignacionRepository.save(asignacion);
        
        log.info("✅ Asignación creada exitosamente con ID: {}", saved.getId());
        
        return convertirADTO(saved);
    }

    /**
     * Elimina (desactiva) una asignación de profesional a rubro de obra
     * Soft delete: cambia el estado a INACTIVO en lugar de eliminar físicamente
     */
    @Transactional
    public void eliminarAsignacion(Long asignacionId, Long obraId, Long empresaId) {
        log.info("🗑️ Eliminando asignación ID: {} de obra: {} empresa: {}", asignacionId, obraId, empresaId);
        
        // Buscar la asignación
        AsignacionProfesionalObra asignacion = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Asignación no encontrada con ID: %d para la empresa: %d", asignacionId, empresaId)));
        
        // Verificar que pertenece a la obra correcta
        if (!asignacion.getObraId().equals(obraId)) {
            throw new BusinessException("La asignación no pertenece a la obra especificada");
        }
        
        // Verificar que pertenece a la empresa correcta
        if (!asignacion.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La asignación no pertenece a la empresa especificada");
        }
        
        // Soft delete: cambiar estado a INACTIVO
        asignacion.setEstado("INACTIVO");
        asignacion.setFechaModificacion(LocalDateTime.now());
        
        asignacionRepository.save(asignacion);
        
        log.info("✅ Asignación {} desactivada exitosamente", asignacionId);
    }

    /**
     * Convierte entidad a DTO
     */
    private AsignacionProfesionalObraDTO convertirADTO(AsignacionProfesionalObra asignacion) {
        AsignacionProfesionalObraDTO dto = new AsignacionProfesionalObraDTO();
        dto.setId(asignacion.getId());
        dto.setProfesionalId(asignacion.getProfesionalId());
        dto.setProfesionalNombre(asignacion.getProfesionalNombre());
        dto.setProfesionalTipo(asignacion.getProfesionalTipo());
        dto.setObraId(asignacion.getObraId());
        dto.setObraNombre(asignacion.getObra() != null ? asignacion.getObra().getNombre() : null);
        dto.setRubroId(asignacion.getRubroId());
        dto.setItemId(asignacion.getItemId());
        dto.setRubroNombre(asignacion.getRubroNombre());
        dto.setPresupuestoNoClienteId(asignacion.getPresupuestoNoClienteId());
        dto.setTipoAsignacion(asignacion.getTipoAsignacion());
        dto.setCantidadJornales(asignacion.getCantidadJornales());
        dto.setJornalesUtilizados(asignacion.getJornalesUtilizados());
        dto.setJornalesRestantes(asignacion.getJornalesRestantes());
        dto.setFechaInicio(asignacion.getFechaInicio());
        dto.setFechaFin(asignacion.getFechaFin());
        dto.setEstado(asignacion.getEstado());
        dto.setObservaciones(asignacion.getObservaciones());
        dto.setModalidad(asignacion.getModalidad());
        dto.setSemanasObjetivo(asignacion.getSemanasObjetivo());
        return dto;
    }
}
