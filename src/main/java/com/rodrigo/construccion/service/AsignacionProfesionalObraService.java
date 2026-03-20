package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignacionProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalObraDTO;
import com.rodrigo.construccion.exception.BusinessException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.AsignacionProfesionalObraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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
    private final IProfesionalService profesionalService;
    private final IObraService obraService;
    private final IPresupuestoNoClienteService presupuestoNoClienteService;
    private final IEmpresaService empresaService;

    /* Obtiene todas las asignaciones de una obra */
    @Transactional(readOnly = true)
    public List<AsignacionProfesionalObraDTO> obtenerAsignacionesPorObra(Long obraId, Long empresaId) {
        // Filtrar solo asignaciones ACTIVAS
        List<AsignacionProfesionalObra> asignaciones = asignacionRepository.findByObra_IdAndEmpresaIdAndEstado(obraId, empresaId, "ACTIVO");

        return asignaciones.stream()
                .map(this::convertirADTO)
                .collect(Collectors.toList());
    }

    /* Crea una nueva asignación de profesional a rubro de obra  */
    @Transactional
    public AsignacionProfesionalObraDTO crearAsignacion(Long obraId, Long empresaId, AsignacionProfesionalRequestDTO request) {
        // Validar profesional
        Profesional profesional = profesionalService.obtenerPorId(request.getProfesionalId());

        if (!profesional.getActivo()) throw new BusinessException("El profesional no está activo");

        // Validar obra
        Obra obra = obraService.findById(obraId);

        // Validar que la empresa existe
        empresaService.findEmpresaById(empresaId);

        // Obtener presupuesto de la obra (toma la versión más reciente ordenada por numeroVersion DESC)
        PresupuestoNoCliente presupuesto = presupuestoNoClienteService.findAllByObraId(obraId)
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el presupuesto para la obra ID: " + obraId));

        // VALIDACIÓN DE ESTADO DEL PRESUPUESTO: Solo se permiten asignaciones si el presupuesto está en estados válidos
        if (!List.of("APROBADO", "EN_EJECUCION", "TERMINADO").contains(presupuesto.getEstado())) {
            throw new BusinessException(String.format(
                    "No se pueden crear asignaciones para esta obra. El presupuesto debe estar APROBADO, EN_EJECUCION o TERMINADO. Estado actual: %s",
                    presupuesto.getEstado()
            ));
        }

        // VALIDACIÓN DE RUBRO: Verificar que el rubro existe en el presupuesto (no se permiten rubros nuevos)
        // Esta validación aplica a TODOS los tipos de asignación
        boolean rubroExisteEnPresupuesto = presupuesto.getItemsCalculadora().stream()
                .anyMatch(item -> item.getId().equals(request.getRubroId()));

        if (!rubroExisteEnPresupuesto) {
            throw new BusinessException(String.format(
                    "El rubro ID %d no existe en el presupuesto de la obra. " +
                    "Solo se pueden asignar profesionales a rubros existentes en el presupuesto APROBADO/EN_EJECUCION/TERMINADO. " +
                    "Para agregar nuevos rubros, edite el presupuesto.",
                    request.getRubroId()
            ));
        }

        // Validaciones específicas para asignación por JORNAL
        if ("JORNAL".equals(request.getTipoAsignacion())) {
            if (request.getCantidadJornales() == null || request.getCantidadJornales().compareTo(BigDecimal.ZERO) <= 0) {
                throw new BusinessException("Debe especificar cantidad de jornales para asignación tipo JORNAL");
            }

            // Paso 1: Buscar el rubro (ItemCalculadoraPresupuesto) dentro del presupuesto
            ItemCalculadoraPresupuesto rubro = presupuesto.getItemsCalculadora().stream()
                    .filter(item -> item.getId().equals(request.getRubroId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró el rubro ID: " + request.getRubroId() +
                                    " en el presupuesto ID: " + presupuesto.getId()));

            // Paso 2: Buscar el jornal específico dentro del rubro
            JornalCalculadora jornal = rubro.getJornales().stream()
                    .filter(j -> j.getId().equals(request.getItemId()))
                    .findFirst()
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "No se encontró el jornal ID: " + request.getItemId() +
                                    " en el rubro ID: " + request.getRubroId()));

            // Paso 3: Calcular jornales ya asignados para este item específico
            Integer jornalesAsignados = asignacionRepository.sumJornalesAsignadosByObraAndItem(obraId, request.getItemId());

            if (jornalesAsignados == null) jornalesAsignados = 0;

            // Paso 4: Calcular jornales disponibles
            int cantidadTotal = jornal.getCantidad().intValue();
            int jornalesDisponibles = cantidadTotal - jornalesAsignados;

            // Paso 5: Validar que hay suficientes jornales disponibles
            if (request.getCantidadJornales().compareTo(BigDecimal.valueOf(jornalesDisponibles)) > 0) {
                throw new BusinessException(String.format("Jornales insuficientes. Disponibles: %d, Solicitados: %s", jornalesDisponibles,
                        request.getCantidadJornales()
                ));
            }
        }

        // Validar fechas
        if (request.getFechaInicio() != null && request.getFechaFin() != null) {
            if (request.getFechaFin().isBefore(request.getFechaInicio())) {
                throw new BusinessException("La fecha de fin no puede ser anterior a la fecha de inicio");
            }
        }

        // Crear la asignación
        AsignacionProfesionalObra asignacion = new AsignacionProfesionalObra();

        // Relaciones
        asignacion.setProfesional(profesional);
        asignacion.setProfesionalId(profesional.getId());
        asignacion.setObra(obra);
        asignacion.setObraId(obra.getId());
        asignacion.setEmpresaId(empresaId);
        // Datos del rubro y presupuesto
        asignacion.setRubroId(request.getRubroId());
        asignacion.setItemId(request.getItemId());
        asignacion.setRubroNombre(request.getRubroNombre());
        asignacion.setPresupuestoNoCliente(presupuesto);
        asignacion.setPresupuestoNoClienteId(presupuesto.getId());
        // Tipo de asignación y jornales
        asignacion.setTipoAsignacion(request.getTipoAsignacion());
        asignacion.setCantidadJornales(request.getCantidadJornales());
        asignacion.setJornalesUtilizados(BigDecimal.ZERO);
        // Fechas
        asignacion.setFechaInicio(request.getFechaInicio() != null ? request.getFechaInicio() : LocalDate.now());
        asignacion.setFechaFin(request.getFechaFin());
        asignacion.setEstado("ACTIVO");
        asignacion.setObservaciones(request.getObservaciones());
        asignacion.setFechaModificacion(LocalDateTime.now());

        return convertirADTO(asignacionRepository.save(asignacion));
    }

    /**
     * Elimina (desactiva) una asignación de profesional a rubro de obra
     * Soft delete: cambia el estado a INACTIVO en lugar de eliminar físicamente
     */
    @Transactional
    public void eliminarAsignacion(Long asignacionId, Long obraId, Long empresaId) {
        // Buscar la asignación
        AsignacionProfesionalObra asignacion = asignacionRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        String.format("Asignación no encontrada con ID: %d para la empresa: %d", asignacionId, empresaId)));

        // Verificar que pertenece a la obra correcta (usar campo ID directo)
        if (!asignacion.getObraId().equals(obraId)) {
            throw new BusinessException("La asignación no pertenece a la obra especificada");
        }

        // Verificar que pertenece a la empresa correcta (usar campo ID directo)
        if (!asignacion.getEmpresaId().equals(empresaId)) {
            throw new BusinessException("La asignación no pertenece a la empresa especificada");
        }

        // Soft delete: cambiar estado a INACTIVO
        asignacion.setEstado("INACTIVO");
        asignacion.setFechaModificacion(LocalDateTime.now());

        asignacionRepository.save(asignacion);
    }

    /* Convierte entidad a DTO */
    private AsignacionProfesionalObraDTO convertirADTO(AsignacionProfesionalObra asignacion) {
        AsignacionProfesionalObraDTO dto = new AsignacionProfesionalObraDTO();
        dto.setId(asignacion.getId());

        // Usar campos ID directos para evitar lazy loading
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
