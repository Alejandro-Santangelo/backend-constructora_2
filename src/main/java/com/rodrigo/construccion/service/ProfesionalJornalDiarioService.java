package com.rodrigo.construccion.service;

import com.rodrigo.construccion.config.TenantContext;
import com.rodrigo.construccion.dto.request.ProfesionalJornalDiarioRequestDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalDiarioResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalResumenDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.ProfesionalJornalDiario;
import com.rodrigo.construccion.model.entity.HonorarioPorRubro;
import com.rodrigo.construccion.repository.HonorarioPorRubroRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.ProfesionalJornalDiarioRepository;
import com.rodrigo.construccion.repository.ProfesionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProfesionalJornalDiarioService {

    private final ProfesionalJornalDiarioRepository jornalRepository;
    private final ProfesionalRepository profesionalRepository;
    private final ObraRepository obraRepository;
    private final HonorarioPorRubroRepository honorarioPorRubroRepository;

    /**
     * Crear un nuevo jornal diario
     */
    public ProfesionalJornalDiarioResponseDTO crear(ProfesionalJornalDiarioRequestDTO requestDTO) {
        log.info("Creando jornal diario: profesional={}, obra={}, rubro={}, fecha={}, horas={}", 
                 requestDTO.getProfesionalId(), requestDTO.getObraId(), requestDTO.getRubroId(),
                 requestDTO.getFecha(), requestDTO.getHorasTrabajadasDecimal());

        // DESHABILITAR FILTRO - Los profesionales son compartidos entre todas las empresas
        // Usamos el flag de super admin para que el HibernateFilterInterceptor no aplique el filtro
        TenantContext.setSuperAdmin(true);
        log.info("🔓 Flag SUPER_ADMIN activado para buscar profesionales y obras compartidas");
        
        try {
            // Validar que no exista un jornal duplicado
            if (jornalRepository.existsByProfesionalIdAndObraIdAndRubroIdAndFecha(
                    requestDTO.getProfesionalId(), 
                    requestDTO.getObraId(),
                    requestDTO.getRubroId(),
                    requestDTO.getFecha())) {
                throw new IllegalArgumentException(
                    "Ya existe un jornal registrado para este profesional en esta obra/rubro en la fecha " + requestDTO.getFecha()
                );
            }

            // Buscar profesional (sin filtro, compartido entre empresas)
            Profesional profesional = profesionalRepository.findById(requestDTO.getProfesionalId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Profesional no encontrado con id: " + requestDTO.getProfesionalId()));

            // Buscar obra
            Obra obra = obraRepository.findById(requestDTO.getObraId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Obra no encontrada con id: " + requestDTO.getObraId()));

            // Validar que el rubro exista en el presupuesto aprobado de la obra
            List<HonorarioPorRubro> rubrosActivos = honorarioPorRubroRepository.findRubrosActivosByObraId(obra.getId());
            
            if (rubrosActivos.isEmpty()) {
                throw new IllegalArgumentException(
                    "La obra no tiene un presupuesto aprobado con rubros activos. " +
                    "Por favor, cree y apruebe un presupuesto antes de registrar jornales."
                );
            }
            
            boolean rubroExiste = rubrosActivos.stream()
                .anyMatch(r -> r.getId().equals(requestDTO.getRubroId()));
            
            if (!rubroExiste) {
                String rubrosDisponibles = rubrosActivos.stream()
                    .map(r -> r.getNombreRubro())
                    .collect(Collectors.joining(", "));
                    
                throw new IllegalArgumentException(
                    "El rubro especificado (ID: " + requestDTO.getRubroId() + ") no existe en el presupuesto aprobado de esta obra. " +
                    "Rubros disponibles: " + rubrosDisponibles
                );
            }

            log.info("Rubro validado: rubro_id={} existe en presupuesto de obra_id={}", 
                     requestDTO.getRubroId(), obra.getId());

            // Crear jornal
            ProfesionalJornalDiario jornal = new ProfesionalJornalDiario();
            jornal.setProfesional(profesional);
            jornal.setObra(obra);
            jornal.setRubroId(requestDTO.getRubroId());
            jornal.setFecha(requestDTO.getFecha());
            jornal.setHorasTrabajadasDecimal(requestDTO.getHorasTrabajadasDecimal());
            
            // Si se especificó una tarifa personalizada, usarla; sino copiar del profesional
            if (requestDTO.getTarifaDiaria() != null) {
                jornal.setTarifaDiaria(requestDTO.getTarifaDiaria());
            } else {
                if (profesional.getHonorarioDia() == null) {
                    throw new IllegalArgumentException(
                        "El profesional no tiene configurado un honorario diario. " +
                        "Configure el honorario o especifique una tarifa personalizada."
                    );
                }
                jornal.setTarifaDiaria(profesional.getHonorarioDia());
            }

            jornal.setObservaciones(requestDTO.getObservaciones());
            
            // Obtener empresa del contexto de multi-tenancy
            Long empresaId = TenantContext.getTenantId();
            if (empresaId == null && requestDTO.getEmpresaId() != null) {
                empresaId = requestDTO.getEmpresaId();
            }
            if (empresaId == null) {
                throw new IllegalArgumentException("No se pudo determinar la empresa del contexto");
            }
            jornal.setEmpresaId(empresaId);

            // El método @PrePersist calculará automáticamente el montoCobrado
            ProfesionalJornalDiario jornalGuardado = jornalRepository.save(jornal);

            log.info("Jornal creado exitosamente: id={}, montoCobrado={}", 
                     jornalGuardado.getId(), jornalGuardado.getMontoCobrado());

            return toResponseDTO(jornalGuardado);
        } finally {
            // Siempre restaurar el flag de super admin
            TenantContext.setSuperAdmin(false);
            log.info("🔒 Flag SUPER_ADMIN desactivado");
        }
    }

    /**
     * Actualizar un jornal existente
     */
    public ProfesionalJornalDiarioResponseDTO actualizar(Long id, ProfesionalJornalDiarioRequestDTO requestDTO) {
        log.info("Actualizando jornal id={}", id);

        ProfesionalJornalDiario jornal = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con id: " + id));

        // Validar que no se cree duplicado al cambiar fecha
        if (!jornal.getFecha().equals(requestDTO.getFecha()) &&
            jornalRepository.existsByProfesionalIdAndObraIdAndFecha(
                    jornal.getProfesional().getId(),
                    jornal.getObra().getId(),
                    requestDTO.getFecha())) {
            throw new IllegalArgumentException(
                "Ya existe un jornal para este profesional en esta obra en la fecha " + requestDTO.getFecha()
            );
        }

        // Actualizar campos
        jornal.setFecha(requestDTO.getFecha());
        jornal.setHorasTrabajadasDecimal(requestDTO.getHorasTrabajadasDecimal());
        
        if (requestDTO.getTarifaDiaria() != null) {
            jornal.setTarifaDiaria(requestDTO.getTarifaDiaria());
        }
        
        jornal.setObservaciones(requestDTO.getObservaciones());

        // El método @PreUpdate calculará automáticamente el montoCobrado
        ProfesionalJornalDiario jornalActualizado = jornalRepository.save(jornal);

        log.info("Jornal actualizado exitosamente: id={}, montoCobrado={}", 
                 jornalActualizado.getId(), jornalActualizado.getMontoCobrado());

        return toResponseDTO(jornalActualizado);
    }

    /**
     * Obtener jornal por ID
     */
    @Transactional(readOnly = true)
    public ProfesionalJornalDiarioResponseDTO obtenerPorId(Long id) {
        ProfesionalJornalDiario jornal = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con id: " + id));
        return toResponseDTO(jornal);
    }

    /**
     * Eliminar jornal
     */
    public void eliminar(Long id) {
        log.info("Eliminando jornal id={}", id);
        
        if (!jornalRepository.existsById(id)) {
            throw new ResourceNotFoundException("Jornal no encontrado con id: " + id);
        }
        
        jornalRepository.deleteById(id);
        log.info("Jornal eliminado exitosamente: id={}", id);
    }

    /**
     * Obtener todos los jornales de un profesional en una obra
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalDiarioResponseDTO> obtenerJornalesPorProfesionalYObra(Long profesionalId, Long obraId) {
        List<ProfesionalJornalDiario> jornales = jornalRepository
                .findByProfesionalIdAndObraIdOrderByFechaDesc(profesionalId, obraId);
        return jornales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los jornales de un profesional
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalDiarioResponseDTO> obtenerJornalesPorProfesional(Long profesionalId) {
        List<ProfesionalJornalDiario> jornales = jornalRepository
                .findByProfesionalIdOrderByFechaDesc(profesionalId);
        return jornales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener todos los jornales de una obra
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalDiarioResponseDTO> obtenerJornalesPorObra(Long obraId) {
        List<ProfesionalJornalDiario> jornales = jornalRepository
                .findByObraIdOrderByFechaDesc(obraId);
        return jornales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener jornales en un rango de fechas para un profesional
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalDiarioResponseDTO> obtenerJornalesPorProfesionalYFechas(
            Long profesionalId, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<ProfesionalJornalDiario> jornales = jornalRepository
                .findByProfesionalIdAndFechaBetweenOrderByFechaDesc(profesionalId, fechaDesde, fechaHasta);
        return jornales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener jornales en un rango de fechas para una obra
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalDiarioResponseDTO> obtenerJornalesPorObraYFechas(
            Long obraId, LocalDate fechaDesde, LocalDate fechaHasta) {
        List<ProfesionalJornalDiario> jornales = jornalRepository
                .findByObraIdAndFechaBetweenOrderByFechaDesc(obraId, fechaDesde, fechaHasta);
        return jornales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener jornales en un rango de fechas (sin filtro de profesional u obra)
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalDiarioResponseDTO> obtenerJornalesPorFechasYEmpresa(
            LocalDate fechaDesde, LocalDate fechaHasta, Long empresaId) {
        List<ProfesionalJornalDiario> jornales = jornalRepository
                .findByFechaBetweenAndEmpresaIdOrderByFechaDesc(fechaDesde, fechaHasta, empresaId);
        return jornales.stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Obtener resumen de jornales por profesional en una obra
     */
    @Transactional(readOnly = true)
    public ProfesionalJornalResumenDTO obtenerResumenProfesionalEnObra(Long profesionalId, Long obraId) {
        BigDecimal totalHoras = jornalRepository.calcularTotalHorasPorProfesionalEnObra(profesionalId, obraId);
        BigDecimal totalCobrado = jornalRepository.calcularTotalCobradoPorProfesionalEnObra(profesionalId, obraId);
        long cantidadJornales = jornalRepository.countByProfesionalIdAndObraId(profesionalId, obraId);

        Profesional profesional = profesionalRepository.findById(profesionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con id: " + profesionalId));
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con id: " + obraId));

        return ProfesionalJornalResumenDTO.builder()
                .profesionalId(profesionalId)
                .profesionalNombre(profesional.getNombre())
                .obraId(obraId)
                .obraNombre(obra.getNombre())
                .cantidadJornales((int) cantidadJornales)
                .totalHorasDecimal(totalHoras)
                .totalCobrado(totalCobrado)
                .build();
    }

    /**
     * Obtener resumen de todos los profesionales en una obra
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalResumenDTO> obtenerResumenProfesionalesPorObra(Long obraId) {
        List<Object[]> resultados = jornalRepository.obtenerResumenProfesionalesPorObra(obraId);
        
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con id: " + obraId));

        List<ProfesionalJornalResumenDTO> resumenes = new ArrayList<>();
        for (Object[] row : resultados) {
            resumenes.add(ProfesionalJornalResumenDTO.builder()
                    .profesionalId((Long) row[0])
                    .profesionalNombre((String) row[1])
                    .obraId(obraId)
                    .obraNombre(obra.getNombre())
                    .cantidadJornales(((Long) row[2]).intValue())
                    .totalHorasDecimal((BigDecimal) row[3])
                    .totalCobrado((BigDecimal) row[4])
                    .build());
        }
        return resumenes;
    }

    /**
     * Obtener resumen de todas las obras para un profesional
     */
    @Transactional(readOnly = true)
    public List<ProfesionalJornalResumenDTO> obtenerResumenObrasPorProfesional(Long profesionalId) {
        List<Object[]> resultados = jornalRepository.obtenerResumenObrasPorProfesional(profesionalId);
        
        Profesional profesional = profesionalRepository.findById(profesionalId)
                .orElseThrow(() -> new ResourceNotFoundException("Profesional no encontrado con id: " + profesionalId));

        List<ProfesionalJornalResumenDTO> resumenes = new ArrayList<>();
        for (Object[] row : resultados) {
            resumenes.add(ProfesionalJornalResumenDTO.builder()
                    .profesionalId(profesionalId)
                    .profesionalNombre(profesional.getNombre())
                    .obraId((Long) row[0])
                    .obraNombre((String) row[1])
                    .cantidadJornales(((Long) row[2]).intValue())
                    .totalHorasDecimal((BigDecimal) row[3])
                    .totalCobrado((BigDecimal) row[4])
                    .build());
        }
        return resumenes;
    }

    /**
     * Convertir entidad a DTO de respuesta
     */
    private ProfesionalJornalDiarioResponseDTO toResponseDTO(ProfesionalJornalDiario jornal) {
        // Obtener nombre del rubro desde honorarios_por_rubro si existe
        String rubroNombre = null;
        if (jornal.getRubroId() != null) {
            try {
                rubroNombre = honorarioPorRubroRepository.findById(jornal.getRubroId())
                        .map(rubro -> rubro.getNombreRubro())
                        .orElse(null);
            } catch (Exception e) {
                log.warn("No se pudo obtener el rubroNombre para rubroId: {}", jornal.getRubroId());
            }
        }
        
        return ProfesionalJornalDiarioResponseDTO.builder()
                .id(jornal.getId())
                .profesionalId(jornal.getProfesional().getId())
                .profesionalNombre(jornal.getProfesional().getNombre())
                .tipoProfesional(jornal.getProfesional().getTipoProfesional())
                .obraId(jornal.getObra().getId())
                .obraNombre(jornal.getObra().getNombre())
                .rubroId(jornal.getRubroId())
                .rubroNombre(rubroNombre)
                .fecha(jornal.getFecha())
                .horasTrabajadasDecimal(jornal.getHorasTrabajadasDecimal())
                .tarifaDiaria(jornal.getTarifaDiaria())
                .montoCobrado(jornal.getMontoCobrado())
                .observaciones(jornal.getObservaciones())
                .empresaId(jornal.getEmpresaId())
                .fechaCreacion(jornal.getFechaCreacion())
                .fechaActualizacion(jornal.getFechaActualizacion())
                .build();
    }
}
