package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.ActualizarAsignacionRequest;
import com.rodrigo.construccion.enums.RolEnObra;
import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.request.AsignarProfesionalesBatchRequest;
import com.rodrigo.construccion.dto.mapper.ProfesionalMapper;
import com.rodrigo.construccion.dto.request.SolicitudProfesionalesRequest;
import com.rodrigo.construccion.dto.request.SolicitudTipoProfesionalRequest;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalResponse;
import com.rodrigo.construccion.dto.mapper.ProfesionalObraMapper;
import com.rodrigo.construccion.dto.response.DisponibilidadProfesionalResponse;
import com.rodrigo.construccion.dto.response.ListaProfesionalesResponse;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO;
import com.rodrigo.construccion.dto.response.ObraPagosDTO;
import com.rodrigo.construccion.dto.response.RubroPagosDTO;
import com.rodrigo.construccion.dto.response.ProfesionalPagoDTO;
import com.rodrigo.construccion.dto.response.ProfesionalConsolidadoDTO;
import com.rodrigo.construccion.dto.response.ObraAsignacionDTO;
import com.rodrigo.construccion.dto.response.AsignacionRubroDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.AsignacionProfesionalObra;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;
import com.rodrigo.construccion.repository.PagoProfesionalObraRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.AsignacionProfesionalObraRepository;
import com.rodrigo.construccion.repository.HonorarioPorRubroRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de Asignaciones Profesional-Obra
 * Maneja la lógica de negocio para asignaciones de profesionales a obras
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ProfesionalObraService implements IProfesionalObraService {

    private final ProfesionalObraRepository profesionalObraRepository;
    private final PagoProfesionalObraRepository pagoRepository;
    private final ObraRepository obraRepository;
    private final AsignacionProfesionalObraRepository asignacionProfesionalObraRepository;
    private final HonorarioPorRubroRepository honorarioPorRubroRepository;
    private final IEmpresaService empresaService;
    private final ProfesionalObraMapper profesionalObraMapper;
    private final IProfesionalService profesionalService;
    private final ProfesionalMapper profesionalMapper;
    private final IObraService obraService;

    /**
     * Obtener todas las asignaciones como DTOs - USADO EN CONTROLLER
     * Utiliza findAllWithRelations() para cargar eagerly las relaciones de Profesional y Obra
     */
    @Override
    public List<AsignacionProfesionalResponse> obtenerTodasComoDTO() {
        List<ProfesionalObra> asignaciones = profesionalObraRepository.findAllWithRelations();
        return asignaciones.stream()
                .map(profesionalObraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Obtener todas las asignaciones filtradas por empresa - USADO EN CONTROLLER
     */
    @Override
    public List<AsignacionProfesionalResponse> obtenerTodasPorEmpresa(Long empresaId) {
        List<ProfesionalObra> asignaciones = profesionalObraRepository.findByEmpresaId(empresaId);
        return asignaciones.stream()
                .map(profesionalObraMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    /**
     * Asignar profesional por tipo y retornar DTO - USADO EN CONTROLLER
     */
    @Transactional
    @Override
    public AsignacionProfesionalResponse asignarProfesionalPorTipoComoDTO(AsignarProfesionalRequest request) {
        log.info("🔍 Iniciando asignación de profesional - Empresa: {}, Obra: {}, Tipo: {}, Nombre: {}", 
                request.empresaId, request.getObraId(), request.getTipoProfesional(), request.getNombre());
        
        try {
            // 1. Validar que la empresa y la obra existen
            empresaService.findEmpresaById(request.empresaId);
            Obra obra = obraService.findById(request.getObraId());
            log.info("✅ Empresa y obra validadas correctamente");

            // 2. Buscar al profesional delegando la lógica a ProfesionalService
            Profesional profesional = profesionalService.findProfesionalParaAsignacion(request);
            log.info("✅ Profesional encontrado: {} (ID: {})", profesional.getNombre(), profesional.getId());

        // 3. TODO: ADAPTAR - Validar que el profesional no esté ya asignado usando dirección
        // boolean yaAsignado = profesionalObraRepository
        //         .existsByProfesional_IdAndObra_IdAndActivoTrue(profesional.getId(), obra.getId());
        //
        // if (yaAsignado) {
        //     throw new RuntimeException(
        //             String.format("El profesional '%s' ya está asignado a la obra '%s'",
        //                     profesional.getNombre(), obra.getNombre()));
        // }

        // 4. Crear la nueva entidad de asignación
        ProfesionalObra asignacion = new ProfesionalObra();
        asignacion.setProfesional(profesional);
        // Copiar los 4 campos de dirección desde Obra
        asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
        asignacion.setDireccionObraAltura(obra.getDireccionObraAltura() != null ? obra.getDireccionObraAltura() : "");
        asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
        asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
        asignacion.setEmpresaId(obra.getEmpresaId());
        asignacion.setFechaDesde(request.getFechaDesde() != null ? request.getFechaDesde() : LocalDate.now());
        asignacion.setFechaHasta(request.getFechaHasta());
        // Se valida y estandariza el rol usando el enum.
        if (request.getRolEnObra() != null && !request.getRolEnObra().isBlank()) {
            try {
                asignacion.setRolEnObraEnum(RolEnObra.fromDisplayName(request.getRolEnObra()));
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Rol '{}' no reconocido, usando ROL_OFICIAL por defecto", request.getRolEnObra());
                asignacion.setRolEnObraEnum(RolEnObra.ROL_OFICIAL);
            }
        }
        BigDecimal valorHora = request.getValorHoraAsignado() != null 
                ? BigDecimal.valueOf(request.getValorHoraAsignado())
                : profesional.getValorHoraDefault();
        asignacion.setValorHoraAsignado(valorHora);
        asignacion.setActivo(request.getActivo() != null ? request.getActivo() : true);

        // 🔧 DATOS FINANCIEROS: Establecer campos requeridos para cálculos de adelantos y pagos
        asignacion.setImporteJornal(valorHora); // Mismo valor que valorHoraAsignado
        asignacion.setCantidadJornales(request.getCantidadJornales() != null ? request.getCantidadJornales() : BigDecimal.ZERO);
        asignacion.setJornalesUtilizados(BigDecimal.ZERO); // Inicializar en 0

        // 5. Guardar la asignación en la base de datos
        log.info("💾 Guardando asignación en base de datos...");
        ProfesionalObra asignacionGuardada = profesionalObraRepository.save(asignacion);
        log.info("✅ Asignación guardada con ID: {}", asignacionGuardada.getId());

        // 6. Mapear la entidad guardada a un DTO de respuesta y retornarla
        AsignacionProfesionalResponse response = profesionalObraMapper.toResponseDTO(asignacionGuardada);
        log.info("🎯 Asignación completada exitosamente - ID: {}", response.idAsignacion);
        return response;
        
        } catch (Exception e) {
            log.error("❌ Error en asignación de profesional: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Asignar múltiples profesionales a una obra en lote - USADO EN CONTROLLER
     */
    @Transactional
    @Override
    public List<AsignacionProfesionalResponse> asignarMultiplesProfesionales(AsignarProfesionalesBatchRequest request) {
        return request.getProfesionalesIds().stream()
                .map(profesionalId -> {
                    // TODO: ADAPTAR - Verificar si el profesional ya está asignado usando dirección
                    // boolean yaAsignado = profesionalObraRepository
                    //         .existsByProfesional_IdAndObra_IdAndActivoTrue(profesionalId, request.getObraId());
                    // if (yaAsignado) {
                    //     return null; // Si ya está asignado, lo omitimos.
                    // }

                    // 2. Crear la solicitud de asignación individual.
                    AsignarProfesionalRequest asignacionIndividualRequest = new AsignarProfesionalRequest();
                    asignacionIndividualRequest.setEmpresaId(request.getEmpresaId());
                    asignacionIndividualRequest.setObraId(request.getObraId());
                    asignacionIndividualRequest.setProfesionalId(profesionalId);

                    // 3. Reutilizar la lógica de asignación principal.
                    return asignarProfesionalPorTipoComoDTO(asignacionIndividualRequest);
                })
                .filter(java.util.Objects::nonNull) // Filtrar los profesionales que se omitieron (nulos).
                .collect(Collectors.toList());
    }

    @Override
    public List<AsignacionProfesionalResponse> obtenerAsignacionesPorTipo(String tipoProfesional, Long empresaId) {

        empresaService.findEmpresaById(empresaId);
        // Optimización: Se mantiene la lógica de variación de género pero se consolida
        // en una única consulta a la base de datos para mejorar el rendimiento.
        String terminoBase = tipoProfesional.trim();
        String terminoAlterno = "";

        // Lógica mejorada para generar la variación de género en términos compuestos.
        // Ejemplo: "Maestro Mayor" -> "Maestra Mayor"
        String[] palabras = terminoBase.split(" ");
        String ultimaPalabra = palabras[palabras.length - 1];
        String baseSinUltimaPalabra = terminoBase.substring(0, terminoBase.length() - ultimaPalabra.length()).trim();

        if (ultimaPalabra.toLowerCase().endsWith("o")) {
            String ultimaPalabraAlterna = ultimaPalabra.substring(0, ultimaPalabra.length() - 1) + "a";
            terminoAlterno = (baseSinUltimaPalabra + " " + ultimaPalabraAlterna).trim();
        } else if (ultimaPalabra.toLowerCase().endsWith("a")) {
            String ultimaPalabraAlterna = ultimaPalabra.substring(0, ultimaPalabra.length() - 1) + "o";
            terminoAlterno = (baseSinUltimaPalabra + " " + ultimaPalabraAlterna).trim();
        } else {
            terminoAlterno = terminoBase; // Si no termina en 'o' o 'a', no hay variación que generar.
        }

        // TODO: ADAPTAR - Llamar a método que no dependa de relación FK con Obra
        // List<ProfesionalObra> asignaciones = profesionalObraRepository.buscarPorTipoFlexible(terminoBase,
        //         terminoAlterno,
        //         empresaId);
        
        // Por ahora retornar lista vacía
        return new ArrayList<>();

        /* CÓDIGO ORIGINAL COMENTADO:
        if (asignaciones.isEmpty()) {
            throw new ResourceNotFoundException(
                    String.format(
                            "No se encontraron asignaciones para el tipo de profesional '%s' en la empresa con ID %d",
                            tipoProfesional, empresaId));
        }

        return profesionalObraMapper.toResponseDTOList(asignaciones);
        */
    }

    /**
     * Obtener disponibilidad de profesionales por tipo
     * Búsqueda flexible: case-insensitive y con variaciones de género - USADO EN
     * CONTROLLER
     */
    @Override
    public List<DisponibilidadProfesionalResponse> obtenerDisponibilidadPorTipo(String tipoProfesional,
            Long empresaId) {
        // 1. Validar que la empresa existe. El filtrado se delega al
        // ProfesionalService.
        empresaService.findEmpresaById(empresaId);

        // 2. Delegar la búsqueda de profesionales al servicio correspondiente.
        // Esto respeta la arquitectura de capas y centraliza la lógica de negocio.
        List<Profesional> profesionales = profesionalService.buscarActivosPorTipoFlexible(tipoProfesional);

        // 3. Usar el mapper para convertir la lista de entidades a DTOs de
        // disponibilidad.
        // Esto elimina el bucle y las consultas N+1.
        return profesionalMapper.toDisponibilidadDtoList(profesionales);
    }

    /**
     * Obtiene una lista de profesionales por tipo con su estado de disponibilidad.
     * Este método está optimizado para evitar el problema N+1. - USADO EN
     * CONTROLLER
     */
    @Override
    public ListaProfesionalesResponse obtenerDisponibilidadProfesionalesPorTipo(String tipoProfesional,
            Long empresaId) {
        // Se valida la empresa para mantener la lógica de negocio, aunque el filtro
        // principal es por tipo.
        empresaService.findEmpresaById(empresaId);

        // 1. Se delega la búsqueda al servicio de profesionales, que ahora es más
        // eficiente.
        List<Profesional> profesionales = profesionalService.buscarActivosPorTipoFlexible(tipoProfesional);

        List<DisponibilidadProfesionalResponse> dtoList = profesionalMapper.toDisponibilidadDtoList(profesionales);

        // Corrección del bug: Se ajusta el tipo de profesional en la respuesta para que
        // sea consistente
        // con los resultados encontrados, en lugar de simplemente repetir el término de
        // búsqueda.
        String tipoProfesionalEncontrado = dtoList.isEmpty() ? tipoProfesional : dtoList.get(0).getTipoProfesional();

        // 3. Se construye el objeto de respuesta final con el formato requerido.
        return new ListaProfesionalesResponse(dtoList.size(), tipoProfesionalEncontrado, dtoList);
    }

    /**
     * Actualizar asignación y retornar DTO - USADO EN CONTROLLER
     */
    @Override
    public AsignacionProfesionalResponse actualizarAsignacionComoDTO(Long asignacionId,
            ActualizarAsignacionRequest request, Long empresaId) {
        ProfesionalObra asignacionActualizada = actualizarAsignacion(asignacionId, request, empresaId);
        // La responsabilidad de traducir la excepción de la base de datos a un mensaje
        // amigable se delega al
        // GlobalExceptionHandler.
        return profesionalObraMapper.toResponseDTO(asignacionActualizada);

    }

    /**
     * Desactiva (marca como inactiva) una asignación delegando a
     * {@link #actualizarAsignacion(Long, ActualizarAsignacionRequest, Long)}.
     * Este método encapsula la intención de negocio "desactivar" y hace más
     * claro el uso desde el controlador.
     */
    @Transactional
    public AsignacionProfesionalResponse desactivarAsignacion(Long asignacionId, Long empresaId) {
        return desactivarAsignacion(asignacionId, empresaId, null);
    }

    @Override
    @Transactional
    public AsignacionProfesionalResponse desactivarAsignacion(Long asignacionId, Long empresaId, Long obraId) {
        if (asignacionId == null)
            throw new IllegalArgumentException("El ID de la asignación es obligatorio");

        ActualizarAsignacionRequest request = new ActualizarAsignacionRequest();
        request.setActivo(false);
        if (obraId != null) {
            request.setObraId(obraId);
        }

        ProfesionalObra asignacionDesactivada = actualizarAsignacion(asignacionId, request, empresaId);

        return profesionalObraMapper.toResponseDTO(asignacionDesactivada);
    }

    /**
     * Actualizar una asignación existente - este metodo lo usa los dos metodos de
     * arriba
     */
    @Transactional
    public ProfesionalObra actualizarAsignacion(Long asignacionId, ActualizarAsignacionRequest request,
            Long empresaId) {
        // --- MEJORA DE SEGURIDAD: Validación de empresaId ---
        if (empresaId == null) {
            throw new IllegalArgumentException("El ID de la empresa es obligatorio para actualizar una asignación.");
        }

        // Buscar la asignación existente (el filtro de Hibernate ya filtra por empresaId)
        ProfesionalObra asignacionExistente = profesionalObraRepository.findById(asignacionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    String.format("Asignación no encontrada con ID: %d para la empresa: %d", asignacionId, empresaId)));

        // --- LÓGICA DE ACTUALIZACIÓN OPTIMIZADA ---

        if (request.getProfesional() != null && !request.getProfesional().trim().isEmpty()) {
            // Se delega la búsqueda del profesional al servicio correspondiente.
            Profesional nuevoProfesional = profesionalService.findFirstActivoByTipo(request.getProfesional());
            asignacionExistente.setProfesional(nuevoProfesional);
        }

        if (request.getObraId() != null) {
            // Valida y copia los 4 campos de dirección desde la nueva obra
            Obra nuevaObra = obraService.findById(request.getObraId());
            asignacionExistente.setDireccionObraCalle(nuevaObra.getDireccionObraCalle());
            asignacionExistente.setDireccionObraAltura(nuevaObra.getDireccionObraAltura());
            asignacionExistente.setDireccionObraPiso(nuevaObra.getDireccionObraPiso());
            asignacionExistente.setDireccionObraDepartamento(nuevaObra.getDireccionObraDepartamento());
        }

        // Actualización de campos simples
        if (request.getFechaDesde() != null) {
            asignacionExistente.setFechaDesde(request.getFechaDesde());
        }

        if (request.getFechaHasta() != null) {
            asignacionExistente.setFechaHasta(request.getFechaHasta());
        }

        if (request.getRolEnObra() != null) {
            if (!request.getRolEnObra().isBlank()) {
                asignacionExistente.setRolEnObraEnum(RolEnObra.fromDisplayName(request.getRolEnObra()));
            }
        }

        if (request.getValorHoraAsignado() != null) {
            asignacionExistente.setValorHoraAsignado(BigDecimal.valueOf(request.getValorHoraAsignado()));
        }

        if (request.getActivo() != null) {
            asignacionExistente.setActivo(request.getActivo());
        }

        // Guardar y forzar la sincronización. Si hay un error de duplicado,
        // la excepción se propagará hacia arriba y será capturada por el método que lo
        // llamó.
        profesionalObraRepository.saveAndFlush(asignacionExistente);

        return asignacionExistente;
    }

    /**
     * ============================================
     * MÉTODOS DE DEPURACIÓN (TEMPORALES) - USADOS EN CONTROLLER Y EN PRESUPUESTOS
     * ============================================
     */

    /**
     * Obtener todos los profesionales para debug
     */
    @Override
    public List<Profesional> obtenerTodosProfesionales() {
        return profesionalService.findAllEntities();
    }

    /**
     * Obtener todas las asignaciones para debug
     * @deprecated VIOLACIÓN MULTI-TENANCY: No filtra por empresaId, expone datos de todas las empresas
     */
    @Override
    @Deprecated
    public List<ProfesionalObra> obtenerTodasLasAsignaciones() {
        log.warn("⚠️ SEGURIDAD: obtenerTodasLasAsignaciones() llamado - método deprecated que expone datos de todas las empresas");
        return new ArrayList<>();
    }

    /**
     * Obtener profesionales asignados a una obra de una empresa (método legacy)
     * @deprecated Usar obtenerProfesionalesConDatosFinancieros para datos financieros completos
     */
    @Override
    @Deprecated
    public List<ProfesionalResponseDTO> obtenerProfesionalesPorObraYEmpresa(Long empresaId, Long obraId) {
        log.warn("Método legacy en uso - Se recomienda usar obtenerProfesionalesConDatosFinancieros");
        empresaService.findEmpresaById(empresaId);
        obraService.findById(obraId);
        
        List<ProfesionalObra> asignaciones = profesionalObraRepository.findByObraIdAndEmpresaIdWithRelations(obraId, empresaId);
        return asignaciones.stream()
                .map(po -> {
                    ProfesionalResponseDTO dto = profesionalMapper.toResponseDTO(po.getProfesional());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtener profesionales asignados a una obra con datos financieros completos
     * Para sistema de adelantos y pagos
     */
    @Override
    public List<ProfesionalObraFinancieroDTO> obtenerProfesionalesConDatosFinancieros(Long empresaId, Long obraId) {
        log.info("Obteniendo profesionales con datos financieros para obra {} y empresa {}", obraId, empresaId);
        
        // Validar empresa y obra
        empresaService.findEmpresaById(empresaId);
        Obra obra = obraService.findById(obraId);
        
        // Obtener asignaciones con relaciones cargadas
        List<ProfesionalObra> asignaciones = profesionalObraRepository.findByObraIdAndEmpresaIdWithRelations(obraId, empresaId);
        
        log.info("Encontradas {} asignaciones para la obra", asignaciones.size());
        
        // Mapear a DTOs con datos financieros
        return asignaciones.stream()
                .map(po -> mapearAProfesionalFinancieroDTO(po, obra))
                .collect(Collectors.toList());
    }

    /**
     * Obtener TODOS los profesionales de la empresa con datos financieros completos
     * CONSOLIDADO: Trae profesionales de TODAS las obras activas de la empresa
     * INCLUYE: totales pagados, adelantos, saldo pendiente, información de la obra
     */
    public List<ProfesionalObraFinancieroDTO> obtenerTodosProfesionalesEmpresaConDatosFinancieros(Long empresaId) {
        log.info("🔍 Obteniendo TODOS los profesionales con datos financieros para empresa {}", empresaId);
        
        // Validar empresa
        empresaService.findEmpresaById(empresaId);
        
        // Obtener todas las obras activas de la empresa
        List<Obra> obrasActivas = obraRepository.findByEmpresaIdAndEstadoIn(
            empresaId, 
            Arrays.asList("APROBADO", "EN_EJECUCION", "TERMINADO") // Incluir TERMINADO para obras de trabajo diario
        );
        
        log.info("📊 Se encontraron {} obras activas para la empresa {}", obrasActivas.size(), empresaId);
        
        // Lista final de profesionales
        List<ProfesionalObraFinancieroDTO> todosProfesionales = new ArrayList<>();
        
        // Para cada obra, obtener sus profesionales asignados
        for (Obra obra : obrasActivas) {
            List<ProfesionalObra> profesionalesObra = profesionalObraRepository.findByObraIdAndEmpresaIdWithRelations(obra.getId(), empresaId);
            
            log.debug("📋 Obra {}: {} profesionales asignados", obra.getNombre(), profesionalesObra.size());
            
            // Mapear a DTOs con datos financieros
            List<ProfesionalObraFinancieroDTO> profesionalesDTO = profesionalesObra.stream()
                    .map(po -> mapearAProfesionalFinancieroDTO(po, obra))
                    .collect(Collectors.toList());
            
            todosProfesionales.addAll(profesionalesDTO);
        }
        
        log.info("✅ Se encontraron {} profesionales asignados en total para la empresa {}", todosProfesionales.size(), empresaId);
        
        return todosProfesionales;
    }

    /**
     * Mapear ProfesionalObra a DTO con datos financieros calculados
     */
    private ProfesionalObraFinancieroDTO mapearAProfesionalFinancieroDTO(ProfesionalObra po, Obra obra) {
        ProfesionalObraFinancieroDTO dto = new ProfesionalObraFinancieroDTO();
        
        // IDs
        dto.setId(po.getId());
        dto.setProfesionalObraId(po.getId());
        dto.setProfesionalId(po.getProfesionalId());
        dto.setEmpresaId(po.getEmpresaId());
        dto.setObraId(po.getIdObra());
        
        // Datos del profesional
        Profesional profesional = po.getProfesional();
        if (profesional != null) {
            String nombre = profesional.getNombre();
            dto.setNombre(nombre);
            dto.setNombreCompleto(nombre);
            
            String tipo = profesional.getTipoProfesional();
            dto.setTipoProfesional(tipo);
            dto.setTipo(tipo);
            
            dto.setEmail(profesional.getEmail());
            dto.setTelefono(profesional.getTelefono());
            dto.setEspecialidad(profesional.getEspecialidad());
            dto.setCuit(profesional.getCuit());
            dto.setCategoria(profesional.getCategoria());
        }
        
        // Datos de la obra
        dto.setNombreObra(obra.getNombre());
        
        // Datos de la asignación
        dto.setRolEnObra(po.getRolEnObra());
        dto.setFechaInicio(po.getFechaDesde());
        dto.setFechaFin(po.getFechaHasta());
        dto.setEstado(po.getEstado());
        dto.setModalidad(po.getModalidad());
        dto.setObservaciones(po.getObservaciones());
        
        // Datos financieros de jornales
        BigDecimal cantidadJornales = po.getCantidadJornales();
        BigDecimal importeJornal = po.getImporteJornal();
        BigDecimal jornalesUtilizados = po.getJornalesUtilizados();
        
        dto.setCantidadJornales(cantidadJornales != null ? cantidadJornales : BigDecimal.ZERO);
        dto.setJornalesUtilizados(jornalesUtilizados != null ? jornalesUtilizados : BigDecimal.ZERO);
        
        // Precio por jornal (3 aliases)
        BigDecimal precioJornal = importeJornal != null ? importeJornal : BigDecimal.ZERO;
        dto.setPrecioJornal(precioJornal);
        dto.setJornal(precioJornal);
        dto.setImporteJornal(precioJornal);
        
        // Precio total = cantidadJornales × precioJornal (3 aliases)
        BigDecimal precioTotal = BigDecimal.ZERO;
        if (cantidadJornales != null && cantidadJornales.compareTo(BigDecimal.ZERO) > 0 && importeJornal != null) {
            precioTotal = importeJornal.multiply(cantidadJornales);
        }
        dto.setPrecioTotal(precioTotal);
        dto.setPrecio(precioTotal);
        dto.setMontoTotal(precioTotal);
        
        // Calcular totales de pagos
        try {
            BigDecimal totalPagado = pagoRepository.calcularTotalPagadoByProfesional(po.getId());
            dto.setTotalPagado(totalPagado != null ? totalPagado : BigDecimal.ZERO);
            
            // Total de adelantos
            BigDecimal totalAdelantos = pagoRepository.calcularAdelantosPendientesDescuento(po.getId());
            dto.setTotalAdelantos(totalAdelantos != null ? totalAdelantos : BigDecimal.ZERO);
            
            // Saldo pendiente = precioTotal - totalPagado
            BigDecimal saldoPendiente = precioTotal.subtract(totalPagado != null ? totalPagado : BigDecimal.ZERO);
            dto.setSaldoPendiente(saldoPendiente);
        } catch (Exception e) {
            log.warn("Error calculando datos financieros para profesional {}: {}", po.getId(), e.getMessage());
            dto.setTotalPagado(BigDecimal.ZERO);
            dto.setTotalAdelantos(BigDecimal.ZERO);
            dto.setSaldoPendiente(precioTotal);
        }
        
        return dto;
    }

    /**
     * Obtener asignación por ID - Se esta usando en JornalService
     */
    @Override
    public ProfesionalObra obtenerPorId(Long id) {
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));
        return profesionalObra;
    }

    /**
     * Buscar asignación específica profesional-obra | Usado en HonorarioService
     * TODO: ADAPTAR - Usar búsqueda por dirección
     */
    @Override
    public ProfesionalObra buscarAsignacionEspecifica(Long profesionalId, Long obraId) {
        // return profesionalObraRepository.findByProfesional_IdAndObra_IdAndActivoTrue(profesionalId, obraId).orElse(null);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Actualizar asignación existente
     */
    @Override
    public void actualizarSaldoDisponible(Long profesionalObraId, BigDecimal nuevoSaldo) {
        ProfesionalObra asignacion = profesionalObraRepository.findById(profesionalObraId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + profesionalObraId));

        asignacion.setSaldoDisponible(nuevoSaldo);
        profesionalObraRepository.save(asignacion);
    }

    /*
     * ============================================
     * METODOS QUE NO SE ESTÁN USANDO LITERALMENTE EN NINGÚN LADO
     * ============================================
     */

    /**
     * Obtener todas las asignaciones
     * @deprecated VIOLACIÓN MULTI-TENANCY: No filtra por empresaId, expone datos de todas las empresas
     */
    @Deprecated
    public List<ProfesionalObra> obtenerTodas() {
        log.warn("⚠️ SEGURIDAD: obtenerTodas() llamado - método deprecated que expone datos de todas las empresas");
        return new ArrayList<>();
    }

    /**
     * Obtener asignaciones con paginación
     */
    public Page<ProfesionalObra> obtenerTodasPaginadas(Pageable pageable) {
        return profesionalObraRepository.findAll(pageable);
    }

    /**
     * Crear nueva asignación
     */
    public ProfesionalObra crear(ProfesionalObra asignacion) {
        return profesionalObraRepository.save(asignacion);
    }

    /**
     * Eliminar asignación
     */
    public void eliminar(Long id) {
        if (profesionalObraRepository.existsById(id)) {
            profesionalObraRepository.deleteById(id);
        } else {
            throw new RuntimeException("Asignación no encontrada con ID: " + id);
        }
    }

    /**
     * Asignar profesional por ID y retornar DTO
     */
    public AsignacionProfesionalResponse asignarProfesionalPorIdComoDTO(AsignarProfesionalRequest request) {
        ProfesionalObra asignacion = asignarProfesionalPorId(request);
        return profesionalObraMapper.toResponseDTO(asignacion);
    }

    /**
     * Asignar profesional por ID (implementación básica)
     */
    public ProfesionalObra asignarProfesionalPorId(AsignarProfesionalRequest request) {
        // Validaciones y búsquedas de relaciones
        Profesional profesional = profesionalService.obtenerPorId(request.getProfesionalId());
        Obra obra = obraService.findById(request.getObraId());

        // Usar el mapper para copiar campos simples desde el request
        ProfesionalObra asignacion = profesionalObraMapper.fromRequest(request);

        // Completar relaciones y valores por defecto
        asignacion.setProfesional(profesional);
        // Copiar los 4 campos de dirección desde Obra
        asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
        asignacion.setDireccionObraAltura(String.valueOf(obra.getDireccionObraAltura()));
        asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
        asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
        asignacion.setEmpresaId(obra.getEmpresaId());

        if (asignacion.getFechaDesde() == null) {
            asignacion.setFechaDesde(LocalDate.now());
        }

        if (asignacion.getValorHoraAsignado() == null && profesional.getValorHoraDefault() != null) {
            asignacion.setValorHoraAsignado(profesional.getValorHoraDefault());
        }

        if (asignacion.getActivo() == null) {
            asignacion.setActivo(true);
        }

        return profesionalObraRepository.save(asignacion);
    }

    /**
     * Buscar asignaciones por profesional
     * TODO: ADAPTAR - Este método ya no existe en el repositorio
     */
    public List<ProfesionalObra> buscarPorProfesional(Long profesionalId) {
        // return profesionalObraRepository.findByProfesional_Id(profesionalId);
        throw new UnsupportedOperationException("Método pendiente de adaptación");
    }

    /**
     * Buscar asignaciones activas por profesional
     */
    public List<ProfesionalObra> buscarActivasPorProfesional(Long profesionalId) {
        return profesionalObraRepository.findByProfesional_IdAndActivoTrue(profesionalId);
    }

    /**
     * Buscar asignaciones por obra
     * TODO: ADAPTAR - Este método ya no existe, usar búsqueda por dirección
     */
    public List<ProfesionalObra> buscarPorObra(Long obraId) {
        // return profesionalObraRepository.findByObra_Id(obraId);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Buscar asignaciones activas por obra
     * TODO: ADAPTAR - Este método ya no existe, usar búsqueda por dirección
     */
    public List<ProfesionalObra> buscarActivasPorObra(Long obraId) {
        // return profesionalObraRepository.findByObra_IdAndActivoTrue(obraId);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Buscar asignaciones por tipo de profesional
     * TODO: ADAPTAR - Este método ya no existe en el repositorio
     */
    public List<ProfesionalObra> buscarPorTipoProfesional(String tipoProfesional) {
        // return profesionalObraRepository.findByTipoProfesional(tipoProfesional);
        throw new UnsupportedOperationException("Método pendiente de adaptación");
    }

    /**
     * Buscar asignaciones activas por tipo de profesional
     * TODO: ADAPTAR - Este método ya no existe en el repositorio
     */
    public List<ProfesionalObra> buscarActivasPorTipoProfesional(String tipoProfesional) {
        // return profesionalObraRepository.findByTipoProfesionalAndActivoTrue(tipoProfesional);
        throw new UnsupportedOperationException("Método pendiente de adaptación");
    }

    /**
     * Verificar si existe asignación activa
     * TODO: ADAPTAR - Este método ya no existe, usar búsqueda por dirección
     */
    public boolean existeAsignacionActiva(Long profesionalId, Long obraId) {
        // return profesionalObraRepository.existsByProfesional_IdAndObra_IdAndActivoTrue(profesionalId, obraId);
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar búsqueda por dirección");
    }

    /**
     * Buscar asignaciones por rango de fechas
     * @deprecated VIOLACIÓN MULTI-TENANCY: No filtra por empresaId, expone datos de todas las empresas. Usar buscarPorRangoFechasYEmpresa(empresaId, fechaInicio, fechaFin)
     */
    @Deprecated
    public List<ProfesionalObra> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        log.warn("⚠️ SEGURIDAD: buscarPorRangoFechas() llamado sin empresaId - método deprecated que expone datos de todas las empresas");
        return new ArrayList<>();
    }

    /**
     * Buscar asignaciones por rol
     * @deprecated VIOLACIÓN MULTI-TENANCY: No filtra por empresaId, expone datos de todas las empresas
     */
    @Deprecated
    public List<ProfesionalObra> buscarPorRol(String rol) {
        log.warn("⚠️ SEGURIDAD: buscarPorRol() llamado sin empresaId - método deprecated que expone datos de todas las empresas");
        return new ArrayList<>();
    }

    /**
     * Obtener asignaciones activas
     * @deprecated VIOLACIÓN MULTI-TENANCY: No filtra por empresaId, expone datos de todas las empresas
     */
    @Deprecated
    public List<ProfesionalObra> obtenerAsignacionesActivas() {
        log.warn("⚠️ SEGURIDAD: obtenerAsignacionesActivas() llamado sin empresaId - método deprecated que expone datos de todas las empresas");
        return new ArrayList<>();
    }

    /**
     * Desactivar asignación
     */
    public ProfesionalObra desactivarAsignacion(Long id) {
        return profesionalObraRepository.findById(id)
                .map(asignacion -> {
                    asignacion.setActivo(false);
                    asignacion.setFechaHasta(LocalDate.now());
                    return profesionalObraRepository.save(asignacion);
                })
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));
    }

    /**
     * Obtener estadísticas básicas
     */
    public long contarTotal() {
        return profesionalObraRepository.count();
    }

    public long contarActivas() {
        return obtenerAsignacionesActivas().size();
    }

    /**
     * Procesar solicitud múltiple de profesionales para una obra
     */
    public List<ProfesionalObra> procesarSolicitudProfesionales(SolicitudProfesionalesRequest solicitud) {
        List<ProfesionalObra> asignacionesCreadas = new ArrayList<>();

        Obra obra = obraService.findById(solicitud.obraId);

        // Si no hay tipos de profesionales especificados, retornar lista vacía
        if (solicitud.tiposProfesionales == null || solicitud.tiposProfesionales.isEmpty()) {
            return asignacionesCreadas;
        }

        // Por cada tipo de profesional solicitado
        for (SolicitudTipoProfesionalRequest tipoProfesional : solicitud.tiposProfesionales) {

            // Validar datos mínimos
            if (tipoProfesional.tipoProfesional == null || tipoProfesional.tipoProfesional.trim().isEmpty()) {
                System.out.println("Saltando tipo de profesional sin especificar");
                continue;
            }

            // Establecer valores por defecto
            int cantidadSolicitada = tipoProfesional.cantidad != null ? tipoProfesional.cantidad : 1;

            // Buscar profesionales disponibles de este tipo
            List<Profesional> profesionalesDisponibles = profesionalService.buscarPorTipoProfesionalActivos(
                    tipoProfesional.tipoProfesional.trim());

            if (profesionalesDisponibles.size() < cantidadSolicitada) {
                System.out.println("No hay suficientes profesionales de tipo '" + tipoProfesional.tipoProfesional
                        + "'. Solicitados: " + cantidadSolicitada + ", Disponibles: "
                        + profesionalesDisponibles.size());
            }

            // Asignar la cantidad solicitada (o la máxima disponible)
            int cantidadAAsignar = Math.min(cantidadSolicitada, profesionalesDisponibles.size());

            for (int i = 0; i < cantidadAAsignar; i++) {
                Profesional profesional = profesionalesDisponibles.get(i);

                // TODO: ADAPTAR - Verificar que no esté ya asignado usando dirección
                // boolean yaAsignado = profesionalObraRepository.existsByProfesional_IdAndObra_IdAndActivoTrue(
                //         profesional.getId(), obra.getId());
                //
                // if (!yaAsignado) {

                    ProfesionalObra asignacion = new ProfesionalObra();
                    asignacion.setProfesional(profesional);
                    // Copiar los 4 campos de dirección desde Obra
                    asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
                    asignacion.setDireccionObraAltura(obra.getDireccionObraAltura());
                    asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
                    asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
                    asignacion.setEmpresaId(obra.getEmpresaId());
                    asignacion.setFechaDesde(solicitud.fechaDesde != null ? solicitud.fechaDesde : LocalDate.now());
                    asignacion.setFechaHasta(solicitud.fechaHasta);
                    asignacion.setRolEnObra(tipoProfesional.rolEnObra);
                    asignacion.setValorHoraAsignado(tipoProfesional.valorHoraSugerido);
                    asignacion.setActivo(true);

                    // 🔧 DATOS FINANCIEROS: Campos requeridos para sistema de adelantos
                    asignacion.setImporteJornal(tipoProfesional.valorHoraSugerido);
                    asignacion.setCantidadJornales(BigDecimal.ZERO); // Se actualizará después
                    asignacion.setJornalesUtilizados(BigDecimal.ZERO);

                    ProfesionalObra asignacionGuardada = profesionalObraRepository.save(asignacion);
                    asignacionesCreadas.add(asignacionGuardada);
                // } else {
                //     System.out.println("Profesional " + profesional.getNombre() + " ya está asignado a la obra "
                //             + obra.getNombre());
                // }
            }
        }

        return asignacionesCreadas;
    }

    /**
     * ============================================
     * MÉTODOS DE GESTIÓN DE CAJA CHICA
     * ============================================
     */

    /**
     * Asignar monto de caja chica a un profesional
     */
    @Transactional
    public ProfesionalObra asignarCajaChica(Long profesionalObraId, BigDecimal monto, Long empresaId) {
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
            .orElseThrow(() -> new ResourceNotFoundException("Profesional obra no encontrado con ID: " + profesionalObraId));

        // Validar empresa
        if (!profesionalObra.getEmpresaId().equals(empresaId)) {
            throw new SecurityException("El profesional no pertenece a la empresa especificada");
        }

        // Asignar monto
        profesionalObra.setMontoAsignado(monto);
        profesionalObra.setSaldoDisponible(monto);

        return profesionalObraRepository.save(profesionalObra);
    }

    /**
     * Obtiene todas las obras con sus rubros y profesionales asignados
     * Estructura jerárquica: Obra → Rubro → Profesionales
     * Usado para la gestión consolidada de pagos por rubro
     */
    public List<ObraPagosDTO> obtenerObrasPorRubroConProfesionales(Long empresaId) {
        log.info("🔍 Obteniendo obras agrupadas por rubro con profesionales para empresa {}", empresaId);
        
        // Validar empresa
        empresaService.findEmpresaById(empresaId);
        
        // Obtener TODAS las obras de la empresa (incluyendo terminadas)
        List<Obra> todasLasObras = obraRepository.findByEmpresaIdAndEstadoIn(
            empresaId, 
            Arrays.asList("PLANIFICACION", "EN_ESPERA", "APROBADO", "EN_EJECUCION", "TERMINADO")
        );
        
        log.info("📊 Se encontraron {} obras para la empresa {}", todasLasObras.size(), empresaId);
        
        List<ObraPagosDTO> obrasDTO = new ArrayList<>();
        
        // Para cada obra, obtener sus asignaciones agrupadas por rubro
        for (Obra obra : todasLasObras) {
            // Obtener todas las asignaciones ACTIVAS de la obra
            List<AsignacionProfesionalObra> asignaciones = asignacionProfesionalObraRepository
                .findByObra_IdAndEmpresaIdAndEstado(obra.getId(), empresaId, "ACTIVO");
            
            // Crear DTO de la obra (SIEMPRE, incluso sin asignaciones)
            ObraPagosDTO obraDTO = ObraPagosDTO.builder()
                .obraId(obra.getId())
                .obraNombre(obra.getNombre())
                .obraEstado(obra.getEstado())
                .direccionCompleta(construirDireccionCompleta(obra))
                .rubros(new ArrayList<>())
                .build();
            
            if (asignaciones.isEmpty()) {
                log.debug("⚠️ Obra {} no tiene asignaciones activas, se incluye sin rubros", obra.getNombre());
                // Calcular totales (serán cero)
                obraDTO.calcularTotales();
                obrasDTO.add(obraDTO);
                continue;
            }
            
            // Agrupar asignaciones por rubroId
            Map<Long, List<AsignacionProfesionalObra>> asignacionesPorRubro = asignaciones.stream()
                .collect(Collectors.groupingBy(AsignacionProfesionalObra::getRubroId));
            
            // Para cada rubro, crear su DTO con profesionales
            for (Map.Entry<Long, List<AsignacionProfesionalObra>> entry : asignacionesPorRubro.entrySet()) {
                Long rubroId = entry.getKey();
                List<AsignacionProfesionalObra> asignacionesRubro = entry.getValue();
                
                // Obtener el nombre del rubro de la primera asignación
                String rubroNombre = asignacionesRubro.get(0).getRubroNombre();
                
                // Crear DTO del rubro
                RubroPagosDTO rubroDTO = RubroPagosDTO.builder()
                    .rubroId(rubroId)
                    .rubroNombre(rubroNombre)
                    .profesionales(new ArrayList<>())
                    .build();
                
                // Mapear cada asignación a ProfesionalPagoDTO
                for (AsignacionProfesionalObra asig : asignacionesRubro) {
                    ProfesionalPagoDTO profesionalDTO = mapearAsignacionAProfesionalPagoDTO(asig);
                    rubroDTO.getProfesionales().add(profesionalDTO);
                }
                
                // Calcular totales del rubro
                rubroDTO.calcularTotales();
                obraDTO.getRubros().add(rubroDTO);
            }
            
            // Calcular totales de la obra
            obraDTO.calcularTotales();
            obrasDTO.add(obraDTO);
        }
        
        log.info("✅ Se procesaron {} obras con rubros y profesionales", obrasDTO.size());
        return obrasDTO;
    }
    
    /**
     * Mapea AsignacionProfesionalObra a ProfesionalPagoDTO con cálculos financieros
     */
    private ProfesionalPagoDTO mapearAsignacionAProfesionalPagoDTO(AsignacionProfesionalObra asig) {
        BigDecimal importeJornal = asig.getImporteJornal() != null ? asig.getImporteJornal() : BigDecimal.ZERO;
        BigDecimal cantidadJornales = asig.getCantidadJornales() != null ? asig.getCantidadJornales() : BigDecimal.ZERO;
        BigDecimal jornalesUtilizados = asig.getJornalesUtilizados() != null ? asig.getJornalesUtilizados() : BigDecimal.ZERO;
        BigDecimal jornalesRestantes = cantidadJornales.subtract(jornalesUtilizados);
        
        BigDecimal totalAsignado = importeJornal.multiply(cantidadJornales);
        BigDecimal totalUtilizado = importeJornal.multiply(jornalesUtilizados);
        BigDecimal saldoPendiente = totalAsignado.subtract(totalUtilizado);
        
        return ProfesionalPagoDTO.builder()
            .asignacionId(asig.getId())
            .profesionalId(asig.getProfesionalId())
            .profesionalNombre(asig.getProfesionalNombre())
            .profesionalTipo(asig.getProfesionalTipo())
            .tipoAsignacion(asig.getTipoAsignacion())
            .importeJornal(importeJornal)
            .cantidadJornales(cantidadJornales)
            .jornalesUtilizados(jornalesUtilizados)
            .jornalesRestantes(jornalesRestantes)
            .totalAsignado(totalAsignado)
            .totalUtilizado(totalUtilizado)
            .saldoPendiente(saldoPendiente)
            .fechaInicio(asig.getFechaInicio())
            .fechaFin(asig.getFechaFin())
            .estado(asig.getEstado())
            .modalidad(asig.getModalidad())
            .semanasObjetivo(asig.getSemanasObjetivo())
            .observaciones(asig.getObservaciones())
            .build();
    }
    
    /**
     * Construye la dirección completa de una obra formateada
     */
    private String construirDireccionCompleta(Obra obra) {
        StringBuilder direccion = new StringBuilder();
        
        if (obra.getDireccionObraCalle() != null) {
            direccion.append(obra.getDireccionObraCalle());
        }
        if (obra.getDireccionObraAltura() != null) {
            direccion.append(" ").append(obra.getDireccionObraAltura());
        }
        if (obra.getDireccionObraPiso() != null) {
            direccion.append(", Piso ").append(obra.getDireccionObraPiso());
        }
        if (obra.getDireccionObraDepartamento() != null) {
            direccion.append(", Depto ").append(obra.getDireccionObraDepartamento());
        }
        if (obra.getDireccionObraBarrio() != null) {
            direccion.append(", ").append(obra.getDireccionObraBarrio());
        }
        
        return direccion.toString().trim();
    }

    /**
     * Obtiene todos los profesionales con sus asignaciones consolidadas
     * Estructura jerárquica: Profesional → Obras → Asignaciones por rubro
     * Usado para la gestión de pagos agrupada por profesional
     */
    public List<ProfesionalConsolidadoDTO> obtenerProfesionalesConsolidados(Long empresaId) {
        log.info("🔍 [CONSOLIDADO] Obteniendo profesionales consolidados para empresa ID: {}", empresaId);
        
        // Validar empresa
        empresaService.findEmpresaById(empresaId);
        log.info("✅ [CONSOLIDADO] Empresa {} validada correctamente", empresaId);
        
        // Obtener TODAS las asignaciones de la empresa (SIN FILTRAR POR ESTADO)
        log.info("📋 [CONSOLIDADO] Buscando TODAS las asignaciones para empresa {}...", empresaId);
        List<AsignacionProfesionalObra> todasLasAsignaciones = asignacionProfesionalObraRepository
            .findByEmpresaId(empresaId);
        
        log.info("📊 [CONSOLIDADO] Total asignaciones encontradas: {}", todasLasAsignaciones.size());
        
        if (todasLasAsignaciones.isEmpty()) {
            log.error("❌ [CONSOLIDADO] NO HAY NINGUNA ASIGNACIÓN en la tabla asignaciones_profesional_obra para empresa {}", empresaId);
            log.error("❌ [CONSOLIDADO] Verifica que hayas asignado profesionales a obras desde 'Asignar por Día' o 'Asignar por Semana'");
            return new ArrayList<>();
        }
        
        // Log de estados encontrados (solo informativo)
        Map<String, Long> estadosCount = todasLasAsignaciones.stream()
            .collect(Collectors.groupingBy(
                a -> a.getEstado() != null ? a.getEstado() : "NULL",
                Collectors.counting()
            ));
        log.info("📊 [CONSOLIDADO] Estados encontrados: {}", estadosCount);
        
        // Log de modalidades encontradas
        Map<String, Long> modalidadesCount = todasLasAsignaciones.stream()
            .collect(Collectors.groupingBy(
                a -> a.getModalidad() != null ? a.getModalidad() : "NULL",
                Collectors.counting()
            ));
        log.info("📊 [CONSOLIDADO] Modalidades encontradas: {}", modalidadesCount);
        
        // Log de primeras 3 asignaciones para debug
        todasLasAsignaciones.stream().limit(3).forEach(a -> 
            log.info("   → Asignación ID={}, Obra={}, Profesional={}, Estado={}, Modalidad={}", 
                a.getId(), a.getObraId(), a.getProfesionalNombre(), a.getEstado(), a.getModalidad())
        );
        
        // Agrupar asignaciones por profesionalId
        Map<Long, List<AsignacionProfesionalObra>> asignacionesPorProfesional = todasLasAsignaciones.stream()
            .collect(Collectors.groupingBy(AsignacionProfesionalObra::getProfesionalId));
        
        List<ProfesionalConsolidadoDTO> profesionalesDTO = new ArrayList<>();
        
        // Para cada profesional, crear su DTO consolidado
        for (Map.Entry<Long, List<AsignacionProfesionalObra>> entry : asignacionesPorProfesional.entrySet()) {
            Long profesionalId = entry.getKey();
            List<AsignacionProfesionalObra> asignacionesProfesional = entry.getValue();
            
            // Obtener datos del profesional de la primera asignación
            AsignacionProfesionalObra primeraAsignacion = asignacionesProfesional.get(0);
            Profesional profesional = primeraAsignacion.getProfesional();
            
            // Crear DTO del profesional
            ProfesionalConsolidadoDTO profesionalDTO = ProfesionalConsolidadoDTO.builder()
                .profesionalId(profesionalId)
                .profesionalNombre(primeraAsignacion.getProfesionalNombre())
                .profesionalTipo(primeraAsignacion.getProfesionalTipo())
                .profesionalDni(null) // El modelo Profesional no tiene campo DNI
                .profesionalTelefono(profesional != null ? profesional.getTelefono() : null)
                .profesionalEmail(profesional != null ? profesional.getEmail() : null)
                .obras(new ArrayList<>())
                .build();
            
            // Agrupar asignaciones del profesional por obraId
            Map<Long, List<AsignacionProfesionalObra>> asignacionesPorObra = asignacionesProfesional.stream()
                .collect(Collectors.groupingBy(AsignacionProfesionalObra::getObraId));
            
            // Para cada obra, crear su DTO
            for (Map.Entry<Long, List<AsignacionProfesionalObra>> obraEntry : asignacionesPorObra.entrySet()) {
                Long obraId = obraEntry.getKey();
                List<AsignacionProfesionalObra> asignacionesObra = obraEntry.getValue();
                
                // Obtener datos de la obra de la primera asignación
                AsignacionProfesionalObra asignacionObra = asignacionesObra.get(0);
                Obra obra = asignacionObra.getObra();
                
                ObraAsignacionDTO obraDTO = ObraAsignacionDTO.builder()
                    .obraId(obraId)
                    .obraNombre(obra.getNombre())
                    .obraEstado(obra.getEstado())
                    .direccionCompleta(construirDireccionCompleta(obra))
                    .asignaciones(new ArrayList<>())
                    .build();
                
                // Mapear cada asignación a AsignacionRubroDTO
                for (AsignacionProfesionalObra asig : asignacionesObra) {
                    AsignacionRubroDTO asignacionDTO = mapearAsignacionARubroDTO(asig);
                    obraDTO.getAsignaciones().add(asignacionDTO);
                }
                
                // Calcular totales de la obra
                obraDTO.calcularTotales();
                profesionalDTO.getObras().add(obraDTO);
            }
            
            // Calcular totales del profesional
            profesionalDTO.calcularTotales();
            profesionalesDTO.add(profesionalDTO);
        }
        
        log.info("✅ Se procesaron {} profesionales con sus asignaciones", profesionalesDTO.size());
        return profesionalesDTO;
    }
    
    /**
     * Mapea AsignacionProfesionalObra a AsignacionRubroDTO
     */
    private AsignacionRubroDTO mapearAsignacionARubroDTO(AsignacionProfesionalObra asig) {
        BigDecimal importeJornal = asig.getImporteJornal() != null ? asig.getImporteJornal() : BigDecimal.ZERO;
        BigDecimal cantidadJornales = asig.getCantidadJornales() != null ? asig.getCantidadJornales() : BigDecimal.ZERO;
        BigDecimal jornalesUtilizados = asig.getJornalesUtilizados() != null ? asig.getJornalesUtilizados() : BigDecimal.ZERO;
        BigDecimal jornalesRestantes = cantidadJornales.subtract(jornalesUtilizados);
        
        BigDecimal totalAsignado = importeJornal.multiply(cantidadJornales);
        BigDecimal totalUtilizado = importeJornal.multiply(jornalesUtilizados);
        BigDecimal saldoPendiente = totalAsignado.subtract(totalUtilizado);
        
        // Obtener rubroNombre dinámicamente desde honorarios_por_rubro si no existe o es un valor por defecto
        String rubroNombre = asig.getRubroNombre();
        if (rubroNombre == null || rubroNombre.isEmpty() || rubroNombre.startsWith("Asignación Semanal")) {
            if (asig.getRubroId() != null && asig.getRubroId() > 0L) {
                try {
                    rubroNombre = honorarioPorRubroRepository.findById(asig.getRubroId())
                            .map(rubro -> rubro.getNombreRubro())
                            .orElse(asig.getRubroNombre());
                } catch (Exception e) {
                    log.warn("No se pudo obtener el rubroNombre para rubroId: {}", asig.getRubroId());
                }
            }
        }
        
        return AsignacionRubroDTO.builder()
            .asignacionId(asig.getId())
            .rubroId(asig.getRubroId())
            .rubroNombre(rubroNombre)
            .tipoAsignacion(asig.getTipoAsignacion())
            .importeJornal(importeJornal)
            .cantidadJornales(cantidadJornales)
            .jornalesUtilizados(jornalesUtilizados)
            .jornalesRestantes(jornalesRestantes)
            .totalAsignado(totalAsignado)
            .totalUtilizado(totalUtilizado)
            .saldoPendiente(saldoPendiente)
            .fechaInicio(asig.getFechaInicio())
            .fechaFin(asig.getFechaFin())
            .estado(asig.getEstado())
            .modalidad(asig.getModalidad())
            .semanasObjetivo(asig.getSemanasObjetivo())
            .observaciones(asig.getObservaciones())
            .build();
    }

}