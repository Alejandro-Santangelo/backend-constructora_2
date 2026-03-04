package com.rodrigo.construccion.service;

import com.rodrigo.construccion.config.TenantContext;
import com.rodrigo.construccion.dto.mapper.ObraMapper;
import com.rodrigo.construccion.dto.request.ClienteRequestDTO;
import com.rodrigo.construccion.dto.request.ProfesionalFormDTO;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.enums.TipoOrigen;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ObraService implements IObraService {

    private final ObraRepository obraRepository;
    private final IClienteService clienteService;
    private final IProfesionalService profesionalService;
    // TODO: Temporalmente comentado hasta resolver MapStruct
    // private final ObraMapper obraMapper;
    private final IEmpresaService empresaService;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final PresupuestoNoClienteRepository presupuestoNoClienteRepository;
    private final EntidadFinancieraService entidadFinancieraService;

    /* Obtener obra por ID */
    @Override
    public ObraSimpleDTO obtenerPorId(Long id) {
        Obra obraEncontrada = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + id));

        return mapToSimpleDTO(obraEncontrada);
    }

    @Override
    public Obra encontrarObraPorIdYEmpresa(Long id, Long idEmpresa) {
        return obraRepository.findByIdAndEmpresaId(id, idEmpresa)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró la obra especificada o no pertenece a la empresa."));
    }

    /* Obtener obra por ID usado por otros servicios */
    @Override
    public Obra findById(Long id) {
        return obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + id));
    }

    @Override
    public Obra buscarPorIdOpcional(Long id) {
        return obraRepository.findById(id).orElse(null);
    }

    /* Obtener obras por cliente */
    @Override
    public List<ObraResponseDTO> obtenerPorCliente(Long clienteId) {
        clienteService.obtenerPorId(clienteId);
        List<Obra> obrasEncontradasPorCliente = obraRepository.findByCliente_Id(clienteId);
        // TODO: Implementar cuando se arregle MapStruct
        // List<ObraResponseDTO> dtos = obraMapper.toResponseDTOList(obrasEncontradasPorCliente);
        throw new RuntimeException("Método temporalmente deshabilitado - pendiente arreglo MapStruct");
        // return enriquecerConPresupuestos(dtos);
    }

    /* Obtener obras activas (simplificado) */
    @Override
    public List<ObraSimpleDTO> obtenerActivas() {
        List<Obra> obrasActivas = obraRepository.findObrasActivas();
        return mapToSimpleDTOList(obrasActivas);
    }

    @Override
    public List<ObraSimpleDTO> obtenerPorEstado(EstadoObra estado) {
        List<Obra> obrasEncontradas = obraRepository.findByEstado(estado.getDisplayName());
        return mapToSimpleDTOList(obrasEncontradas);
    }

    /* Obtener obras por empresa */
    @Override
    public List<ObraResponseDTO> obtenerPorEmpresa(Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        List<Obra> obrasPorEmpresa = obraRepository.findByEmpresaId(empresaId);
        return mapToResponseDTOList(obrasPorEmpresa);
    }

    /* Obtener SOLO obras manuales por empresa (sin presupuesto previo) */
    @Override
    public List<ObraResponseDTO> obtenerObrasManualesPorEmpresa(Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        List<Obra> obrasManuales = obraRepository.findObrasManualesByEmpresaId(empresaId);
        return mapToResponseDTOList(obrasManuales);
    }

    /* Obtener todas las obras */
    @Override
    public List<ObraResponseDTO> obtenerTodas() {
        List<Obra> obras = obraRepository.findAll();
        return mapToResponseDTOList(obras);
    }

    /* Crear nueva obra con cliente específico */
    @Override
    @Transactional
    public ObraResponseDTO crear(ObraRequestDTO obraRequestDto, Long clienteId) {
        Cliente cliente;

        // LÓGICA 1: Determinar el cliente
        if (obraRequestDto.getIdCliente() != null) {
            // Caso A: Cliente existente proporcionado en el DTO
            cliente = clienteService.obtenerPorId(obraRequestDto.getIdCliente());
        } else if (clienteId != null) {
            // Caso B: Cliente existente proporcionado como parámetro (compatibilidad con código existente)
            cliente = clienteService.obtenerPorId(clienteId);
        } else if (obraRequestDto.getNombreSolicitante() != null && !obraRequestDto.getNombreSolicitante().isBlank()) {
            // Caso C: Crear nuevo cliente automáticamente
            ClienteRequestDTO nuevoClienteDTO = new ClienteRequestDTO();
            nuevoClienteDTO.setNombre(obraRequestDto.getNombreSolicitante());
            nuevoClienteDTO.setNombreSolicitante(obraRequestDto.getNombreSolicitante());
            nuevoClienteDTO.setTelefono(obraRequestDto.getTelefono());
            nuevoClienteDTO.setEmail(obraRequestDto.getMail());
            nuevoClienteDTO.setDireccion(obraRequestDto.getDireccionParticular());

            // Crear el cliente asociado a la empresa
            Long empresaIdParaCliente = obraRequestDto.getEmpresaId() != null
                    ? obraRequestDto.getEmpresaId()
                    : 1L; // Default empresa ID

            ClienteResponseDTO clienteCreado = clienteService.crearCliente(nuevoClienteDTO, List.of(empresaIdParaCliente));
            cliente = clienteService.obtenerPorId(clienteCreado.getId_cliente());
        } else {
            // Caso D: Error - debe haber un cliente o datos para crear uno
            throw new IllegalArgumentException("Debe proporcionar un ID de cliente existente o datos para crear uno nuevo (nombreSolicitante)");
        }

        // LÓGICA 2: Crear la entidad Obra
        // TODO: Crear obra desde DTO manualmente por ahora
        Obra obra = new Obra();
        obra.setNombre(obraRequestDto.getNombre());
        obra.setDescripcion(obraRequestDto.getDescripcion());
        obra.setObservaciones(obraRequestDto.getObservaciones());
        obra.setFechaInicio(obraRequestDto.getFechaInicio());
        obra.setFechaFin(obraRequestDto.getFechaFin());
        obra.setPresupuestoEstimado(obraRequestDto.getPresupuestoEstimado());
        obra.setDireccionObraCalle(obraRequestDto.getDireccionObraCalle());
        obra.setDireccionObraAltura(obraRequestDto.getDireccionObraAltura());
        obra.setDireccionObraBarrio(obraRequestDto.getDireccionObraBarrio());
        obra.setHonorarioJornalesObra(obraRequestDto.getHonorarioJornalesObra());
        obra.setTipoHonorarioJornalesObra(obraRequestDto.getTipoHonorarioJornalesObra());
        obra.setCliente(cliente);

        // LÓGICA 3: Generar nombre automáticamente si está vacío
        if (obra.getNombre() == null || obra.getNombre().isBlank()) {
            obra.setNombre(generarNombreObra(obraRequestDto));
        }

        // LÓGICA 4: Marcar como obra manual si NO tiene presupuesto asociado
        // Las obras manuales son creadas directamente sin presupuesto previo
        if (obra.getPresupuestoNoClienteId() == null) {
            obra.setEsObraManual(true);
            obra.setTipoOrigen(TipoOrigen.OBRA_INDEPENDIENTE);
            // Compatibilidad: extraer desglose si el frontend lo envió embebido en observaciones
            extraerDesgloseDeObservaciones(obra);
        } else {
            obra.setEsObraManual(false);
        }

        // Campos de desglose de presupuesto: solo aplican a obras independientes
        if (!Boolean.TRUE.equals(obra.getEsObraManual())) {
            obra.setPresupuestoJornales(null);
            obra.setPresupuestoMateriales(null);
            obra.setPresupuestoHonorarios(null);
            obra.setTipoHonorarioPresupuesto(null);
            obra.setPresupuestoMayoresCostos(null);
            obra.setTipoMayoresCostosPresupuesto(null);
        }

        // Si no se proporciona una fecha de inicio, se asigna la fecha actual
        if (obra.getFechaInicio() == null) {
            obra.setFechaInicio(LocalDate.now());
        }

        // Guardar la obra primero
        Obra obraGuardada = obraRepository.save(obra);

        // SINCRONIZACIÓN: registrar en el sistema unificado de entidades financieras
        entidadFinancieraService.sincronizarDesdeObra(obraGuardada);

        // LÓGICA 5: Asignar profesionales si hay en el formulario
        if (obraRequestDto.getProfesionalesAsignadosForm() != null && !obraRequestDto.getProfesionalesAsignadosForm().isEmpty()) {
            asignarProfesionalesDesdeFormulario(obraGuardada, obraRequestDto.getProfesionalesAsignadosForm());
        }

        return mapToResponseDTO(obraGuardada);
    }

    /**
     * Genera el nombre de la obra concatenando los campos de dirección
     */
    private String generarNombreObra(ObraRequestDTO dto) {
        StringBuilder nombre = new StringBuilder();

        if (dto.getDireccionObraCalle() != null && !dto.getDireccionObraCalle().isBlank()) {
            nombre.append(dto.getDireccionObraCalle());
        }

        if (dto.getDireccionObraAltura() != null && !dto.getDireccionObraAltura().isBlank()) {
            nombre.append(" ").append(dto.getDireccionObraAltura());
        }

        if (dto.getDireccionObraBarrio() != null && !dto.getDireccionObraBarrio().isBlank()) {
            nombre.append(" ").append(dto.getDireccionObraBarrio());
        }

        if (dto.getDireccionObraTorre() != null && !dto.getDireccionObraTorre().isBlank()) {
            nombre.append(" Torre ").append(dto.getDireccionObraTorre());
        }

        if (dto.getDireccionObraPiso() != null && !dto.getDireccionObraPiso().isBlank()) {
            nombre.append(" Piso ").append(dto.getDireccionObraPiso());
        }

        if (dto.getDireccionObraDepartamento() != null && !dto.getDireccionObraDepartamento().isBlank()) {
            nombre.append(" Depto ").append(dto.getDireccionObraDepartamento());
        }

        return nombre.toString().trim();
    }

    /**
     * Asigna profesionales a la obra desde el formulario
     * Crea profesionales nuevos si esManual = true
     */
    private void asignarProfesionalesDesdeFormulario(Obra obra, List<ProfesionalFormDTO> profesionalesForm) {
        for (ProfesionalFormDTO profForm : profesionalesForm) {
            Profesional profesional;

            if (Boolean.TRUE.equals(profForm.getEsManual())) {
                // Crear nuevo profesional
                ProfesionalRequestDTO nuevoProfDTO = new ProfesionalRequestDTO();
                nuevoProfDTO.setNombre(profForm.getNombre());
                nuevoProfDTO.setTipoProfesional(profForm.getTipoProfesional());
                nuevoProfDTO.setValorHoraDefault(profForm.getValorHora());
                nuevoProfDTO.setActivo(true);

                ProfesionalResponseDTO profCreado = profesionalService.crearProfesional(nuevoProfDTO);
                profesional = profesionalService.obtenerPorId(profCreado.getId());
            } else {
                // Usar profesional existente
                Long profesionalId = Long.parseLong(profForm.getId());
                profesional = profesionalService.obtenerPorId(profesionalId);
            }

            // Crear la asignación en profesionales_obras
            ProfesionalObra asignacion = new ProfesionalObra();
            asignacion.setProfesional(profesional);
            asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
            asignacion.setDireccionObraAltura(obra.getDireccionObraAltura());
            asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
            asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
            asignacion.setEmpresaId(obra.getEmpresaId());
            asignacion.setFechaDesde(LocalDate.now());
            BigDecimal valorHora = profForm.getValorHora();
            asignacion.setValorHoraAsignado(valorHora);
            asignacion.setActivo(true);
            asignacion.setMontoAsignado(BigDecimal.ZERO);
            asignacion.setSaldoDisponible(BigDecimal.ZERO);

            // 🔧 DATOS FINANCIEROS: Campos críticos para adelantos y pagos
            asignacion.setImporteJornal(valorHora);
            asignacion.setCantidadJornales(profForm.getCantidadJornales() != null ? profForm.getCantidadJornales() : 0);
            asignacion.setJornalesUtilizados(0);

            profesionalObraRepository.save(asignacion);
        }
    }

    /* Actualizar obra existente */
    @Override
    @Transactional
    public ObraResponseDTO actualizar(Long id, ObraRequestDTO obraRequestDTO) {
        // Primero, obtenemos la entidad real, no el DTO.
        Obra obraExistente = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + id));

        // Usamos el mapper para actualizar la entidad existente con los datos del DTO.
        // TODO: Actualizar campos manualmente por ahora
        if (obraRequestDTO.getNombre() != null) {
            obraExistente.setNombre(obraRequestDTO.getNombre());
        }
        if (obraRequestDTO.getDescripcion() != null) {
            obraExistente.setDescripcion(obraRequestDTO.getDescripcion());
        }

        // Compatibilidad: extraer desglose si el frontend lo envió embebido en observaciones
        if (Boolean.TRUE.equals(obraExistente.getEsObraManual())) {
            extraerDesgloseDeObservaciones(obraExistente);
        }

        // Campos de desglose de presupuesto: solo aplican a obras independientes
        if (!Boolean.TRUE.equals(obraExistente.getEsObraManual())) {
            obraExistente.setPresupuestoJornales(null);
            obraExistente.setPresupuestoMateriales(null);
            obraExistente.setPresupuestoHonorarios(null);
            obraExistente.setTipoHonorarioPresupuesto(null);
            obraExistente.setPresupuestoMayoresCostos(null);
            obraExistente.setTipoMayoresCostosPresupuesto(null);
        }

        // Guardamos la entidad actualizada.
        Obra obraGuardada = obraRepository.save(obraExistente);

        return mapToResponseDTO(obraGuardada);
    }

    /* Eliminar obra y todas sus relaciones en cascada */
    @Override
    @Transactional
    public void eliminarEnCascada(Long id, Long empresaId) {
        // 1. Verificar que la obra existe
        Obra obra = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + id));

        // 2. Validar que la obra pertenece a la empresa (multi-tenancy)
        if (obra.getEmpresaId() == null || !obra.getEmpresaId().equals(empresaId)) {
            throw new RuntimeException("La obra no pertenece a la empresa especificada");
        }

        // 3. Eliminar la obra
        // JPA eliminará automáticamente TODAS las entidades relacionadas con CascadeType.ALL:
        // - ProfesionalObra (cascade = ALL, orphanRemoval = true)
        //   └─ PagoProfesionalObra (cascade desde ProfesionalObra)
        //   └─ Jornal (cascade desde ProfesionalObra)
        // - PresupuestoNoCliente (cascade = ALL, orphanRemoval = true)
        //   └─ ItemCalculadoraPresupuesto (cascade desde PresupuestoNoCliente)
        //      └─ PagoConsolidado (cascade desde ItemCalculadoraPresupuesto)
        //      └─ MaterialCalculadora (cascade desde ItemCalculadoraPresupuesto)
        //         └─ PagoConsolidado (cascade desde MaterialCalculadora)
        // - Costo (cascade = ALL)
        // - Honorario (cascade = ALL)
        // - CobroObra (cascade = ALL)
        // - PedidoPago (cascade = ALL)

        obraRepository.delete(obra);
    }

    /* Cambiar estado de obra (simplificado) */
    @Override
    @Transactional
    public ObraResponseDTO cambiarEstado(Long id, EstadoObra nuevoEstado) {
        // Obtenemos la entidad real para modificarla.
        Obra obra = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + id));

        obra.setEstado(nuevoEstado);

        // ⭐ SINCRONIZACIÓN BIDIRECCIONAL: Actualizar estado del presupuesto vinculado
        sincronizarEstadoObraConPresupuesto(obra, nuevoEstado);

        // TODO: Implementar cuando se arregle MapStruct
        // return obraMapper.toResponseDTO(obra);
        throw new RuntimeException("Método temporalmente deshabilitado - pendiente arreglo MapStruct");
    }

    /**
     * Sincroniza el estado de la obra con el presupuesto vinculado.
     * Ahora que los enums están sincronizados, la conversión es directa por nombre.
     */
    private void sincronizarEstadoObraConPresupuesto(Obra obra, EstadoObra estadoObra) {
        // Buscar el presupuesto vinculado a esta obra
        List<com.rodrigo.construccion.model.entity.PresupuestoNoCliente> presupuestos =
                presupuestoNoClienteRepository.findByObra_IdOrderByNumeroVersionDesc(obra.getId());

        if (presupuestos.isEmpty()) {
            return; // No hay presupuesto vinculado, no hacer nada
        }

        // Obtener el presupuesto más reciente (primero de la lista ordenada DESC)
        com.rodrigo.construccion.model.entity.PresupuestoNoCliente presupuesto = presupuestos.get(0);

        // Convertir estado de obra a presupuesto (conversión directa por nombre)
        try {
            com.rodrigo.construccion.enums.PresupuestoEstado nuevoEstadoPresupuesto =
                    com.rodrigo.construccion.enums.PresupuestoEstado.valueOf(estadoObra.name());

            if (presupuesto.getEstado() != nuevoEstadoPresupuesto) {
                presupuesto.setEstado(nuevoEstadoPresupuesto);
                presupuestoNoClienteRepository.save(presupuesto);

                org.slf4j.LoggerFactory.getLogger(ObraService.class).info(
                        "🔄 Estado sincronizado: Obra {} ({}) → Presupuesto {} ({})",
                        obra.getId(), estadoObra.getDisplayName(),
                        presupuesto.getId(), nuevoEstadoPresupuesto.getDisplayValue()
                );
            }
        } catch (IllegalArgumentException e) {
            org.slf4j.LoggerFactory.getLogger(ObraService.class).warn(
                    "⚠️ No se pudo sincronizar estado de obra {} a presupuesto: estado {} no existe en PresupuestoEstado",
                    obra.getId(), estadoObra.name()
            );
        }
    }

    /* Obtener estadísticas básicas de obras */
    @Override
    public EstadisticasObraDTO obtenerEstadisticas() {
        long totalObras = obraRepository.count();
        String descripcion = "Estadísticas básicas de obras";
        return new EstadisticasObraDTO(totalObras, descripcion);
    }

    /**
     * Devuelve una lista con todos los roles disponibles definidos en el enum RolEnObra.
     * Ideal para poblar dropdowns en el frontend.
     */
    @Override
    public List<String> obtenerEstadosObra() {
        return Arrays.stream(EstadoObra.values())
                .map(EstadoObra::getDisplayName)
                .collect(Collectors.toList());
    }

    /* Obtener todos los profesionales asignados a una obra */
    @Override
    public List<ProfesionalResponseDTO> obtenerProfesionalesAsignados(Long obraId) {
        // TODO: Este método debe migrar a ProfesionalObraService
        throw new UnsupportedOperationException("Usar ProfesionalObraService para obtener profesionales asignados");
    }

    @Override
    public List<ProfesionalResponseDTO> actualizarPorcentajeGananciaTodosAsignados(Long obraId, double porcentaje) {
        // TODO: ADAPTAR - Ya no existe relación bidireccional con ProfesionalObra
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar ProfesionalObraRepository");

        /* CÓDIGO ORIGINAL COMENTADO:
        // 1. Buscamos la obra por su ID.
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        // 2. Si no hay profesionales asignados, no hacemos nada.
        if (obra.getProfesionalesAsignados() == null || obra.getProfesionalesAsignados().isEmpty()) {
            return List.of();
        }

        // 3. Procesamos la lista de profesionales asignados de forma más limpia.
        final var porcentajeBD = BigDecimal.valueOf(porcentaje);
        List<Profesional> profesionalesActualizados = obra.getProfesionalesAsignados().stream()
                .map(asignacion -> asignacion.getProfesional())
                .peek(profesional -> profesional.actualizarGanancia(porcentajeBD))
                .toList();

        return profesionalMapper.toResponseDTOList(profesionalesActualizados);
        */
    }

    @Override
    public ProfesionalResponseDTO actualizarPorcentajeGananciaProfesionalAsignado(Long obraId, Long profesionalId,
                                                                                  double porcentaje) {
        // TODO: ADAPTAR - Ya no existe relación bidireccional con ProfesionalObra
        throw new UnsupportedOperationException("Método pendiente de adaptación - usar ProfesionalObraRepository");

        /* CÓDIGO ORIGINAL COMENTADO:
        // 1. Buscamos la obra por su ID.
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        // 2. Si no hay profesionales asignados, no hacemos nada.
        if (obra.getProfesionalesAsignados() == null || obra.getProfesionalesAsignados().isEmpty()) {
            throw new ResourceNotFoundException(
                    "No hay profesionales asignados a la obra con ID: " + obraId);
        }

        // 3. Buscamos la asignación específica y actualizamos la ganancia del
        // profesional.
        final var porcentajeBD = BigDecimal.valueOf(porcentaje);
        Profesional profesionalActualizado = obra.getProfesionalesAsignados().stream()
                .filter(asignacion -> asignacion.getProfesional().getId().equals(profesionalId))
                .findFirst()
                .map(asignacion -> asignacion.getProfesional())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "El profesional con ID " + profesionalId + " no está asignado a esta obra."));

        profesionalActualizado.actualizarGanancia(porcentajeBD);
        return profesionalMapper.toResponseDTO(profesionalActualizado);
        */
    }

    @Override
    public boolean existeObra(Long empresaId, Long obraId) {
        return obraRepository.existsByIdAndEmpresaId(obraId, empresaId);
    }

    /**
     * Enriquece los DTOs de obras con información del presupuesto no cliente que las originó.
     * Este método carga el presupuesto asociado para obras creadas desde presupuestos.
     * 
     * @param dtos Lista de ObraResponseDTO a enriquecer
     * @return Lista de ObraResponseDTO enriquecida con presupuestoNoCliente
     */
    private List<ObraResponseDTO> enriquecerConPresupuestos(List<ObraResponseDTO> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return dtos;
        }

        for (ObraResponseDTO dto : dtos) {
            if (dto.getPresupuestoNoClienteId() != null) {
                presupuestoNoClienteRepository.findById(dto.getPresupuestoNoClienteId())
                        .ifPresent(presupuesto -> {
                            // Crear DTO simple para evitar referencias circulares
                            PresupuestoNoClienteSimpleDTO presupuestoDTO = new PresupuestoNoClienteSimpleDTO();
                            presupuestoDTO.setId(presupuesto.getId());
                            presupuestoDTO.setEsPresupuestoTrabajoExtra(presupuesto.getEsPresupuestoTrabajoExtra());
                            presupuestoDTO.setObraId(presupuesto.getObra() != null ? presupuesto.getObra().getId() : null);
                            presupuestoDTO.setNombreObra(presupuesto.getNombreObra());
                            presupuestoDTO.setEstado(presupuesto.getEstado());
                            presupuestoDTO.setFechaEmision(presupuesto.getFechaEmision());
                            presupuestoDTO.setFechaProbableInicio(presupuesto.getFechaProbableInicio());
                            presupuestoDTO.setTotalPresupuesto(presupuesto.getTotalPresupuesto());
                            presupuestoDTO.setTotalHonorariosCalculado(presupuesto.getTotalHonorariosCalculado());
                            presupuestoDTO.setTotalPresupuestoConHonorarios(presupuesto.getTotalPresupuestoConHonorarios());
                            presupuestoDTO.setNombreSolicitante(presupuesto.getNombreSolicitante());
                            presupuestoDTO.setObservaciones(presupuesto.getObservaciones());
                            
                            dto.setPresupuestoNoCliente(presupuestoDTO);
                        });
            }
        }

        return dtos;
    }

    // =========================================================================
    // HELPERS: Compatibilidad con frontend que envía desglose en observaciones
    // =========================================================================

    /**
     * Si observaciones contiene un bloque "[DESGLOSE_OBRA]{...}[/DESGLOSE_OBRA]"
     * enviado por versiones del frontend que aún no usan los campos relacionales,
     * extrae los valores y los persiste en las columnas correspondientes,
     * limpiando el texto de observaciones.
     */
    private void extraerDesgloseDeObservaciones(Obra obra) {
        String obs = obra.getObservaciones();
        if (obs == null || !obs.contains("[DESGLOSE_OBRA]")) return;
        try {
            int inicio = obs.indexOf("[DESGLOSE_OBRA]") + "[DESGLOSE_OBRA]".length();
            int fin = obs.indexOf("[/DESGLOSE_OBRA]");
            if (fin <= inicio) return;

            String json = obs.substring(inicio, fin).trim();

            if (obra.getPresupuestoJornales() == null)
                extractBigDecimalFromJson(json, "jornales").ifPresent(obra::setPresupuestoJornales);
            if (obra.getPresupuestoMateriales() == null)
                extractBigDecimalFromJson(json, "materiales").ifPresent(obra::setPresupuestoMateriales);
            if (obra.getPresupuestoHonorarios() == null)
                extractBigDecimalFromJson(json, "honorarios").ifPresent(obra::setPresupuestoHonorarios);
            if (obra.getTipoHonorarioPresupuesto() == null)
                extractStringFromJson(json, "tipoHonorarios").ifPresent(obra::setTipoHonorarioPresupuesto);
            if (obra.getPresupuestoMayoresCostos() == null)
                extractBigDecimalFromJson(json, "mayoresCostos").ifPresent(obra::setPresupuestoMayoresCostos);
            if (obra.getTipoMayoresCostosPresupuesto() == null)
                extractStringFromJson(json, "tipoMayoresCostos").ifPresent(obra::setTipoMayoresCostosPresupuesto);

            // Eliminar el bloque del texto de observaciones
            String bloque = "[DESGLOSE_OBRA]" + obs.substring(inicio, fin) + "[/DESGLOSE_OBRA]";
            String obsLimpia = obs.replace(bloque, "").trim();
            obra.setObservaciones(obsLimpia.isBlank() ? null : obsLimpia);

            log.info("Desglose extraído de observaciones para obra '{}'", obra.getNombre());
        } catch (Exception e) {
            log.warn("No se pudo extraer desglose de observaciones: {}", e.getMessage());
        }
    }

    private java.util.Optional<BigDecimal> extractBigDecimalFromJson(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*([0-9]+(?:\\.[0-9]+)?)");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) {
            try { return java.util.Optional.of(new BigDecimal(m.group(1))); }
            catch (Exception ignored) {}
        }
        return java.util.Optional.empty();
    }

    private java.util.Optional<String> extractStringFromJson(String json, String key) {
        java.util.regex.Pattern p = java.util.regex.Pattern.compile(
                "\"" + key + "\"\\s*:\\s*\"([^\"]+)\"");
        java.util.regex.Matcher m = p.matcher(json);
        if (m.find()) return java.util.Optional.of(m.group(1));
        return java.util.Optional.empty();
    }

    // === IMPLEMENTACIÓN MÉTODOS DE BORRADORES ===
    
    /**
     * Crea una obra independiente en estado BORRADOR.
     * Permite persistir datos del formulario por etapas.
     */
    @Override
    @Transactional
    public ObraResponseDTO crearBorrador(ObraRequestDTO obraRequestDto, Long clienteId) {
        log.info("🔧 Creando obra independiente como BORRADOR...");
        
        // Crear obra base igual que el método normal
        Obra nuevaObra = crearObraBase(obraRequestDto, clienteId);
        
        // Establecer específicamente como BORRADOR
        nuevaObra.setEstado(EstadoObra.BORRADOR);
        nuevaObra.setEsObraManual(true); // Es obra independiente
        nuevaObra.setTipoOrigen(TipoOrigen.OBRA_INDEPENDIENTE);
        
        // Guardar inmediatamente para obtener ID
        Obra obraBorrador = obraRepository.save(nuevaObra);
        log.info("✅ Obra borrador creada con ID: {} en estado: {}", 
                 obraBorrador.getId(), obraBorrador.getEstado());
                
        // Sincronizar con entidades financieras si corresponde
        try {
            entidadFinancieraService.sincronizarDesdeObra(obraBorrador);
        } catch (Exception e) {
            log.warn("⚠️ Error al sincronizar borrador con entidades financieras: {}", e.getMessage());
        }
        
        return mapToResponseDTO(obraBorrador);
    }
    
    /**
     * Actualiza un borrador de obra independiente.
     * Solo permite actualización si está en estado BORRADOR.
     */
    @Override
    @Transactional
    public ObraResponseDTO actualizarBorrador(Long id, ObraRequestDTO obraRequestDto) {
        log.info("🔧 Actualizando borrador de obra ID: {}", id);
        
        Obra obraExistente = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra borrador no encontrada con ID: " + id));
        
        // Verificar que sea realmente un borrador
        if (!obraExistente.esBorrador()) {
            throw new IllegalStateException("Solo se pueden actualizar obras en estado BORRADOR. Estado actual: " + obraExistente.getEstado());
        }
        
        // Actualizar todos los campos del formulario
        actualizarCamposObra(obraExistente, obraRequestDto);
        
        // Mantener estado BORRADOR
        obraExistente.setEstado(EstadoObra.BORRADOR);
        
        Obra obraActualizada = obraRepository.save(obraExistente);
        log.info("✅ Borrador actualizado exitosamente. Campos persistidos.");
        
        return mapToResponseDTO(obraActualizada);
    }
    
    /**
     * Convierte un borrador en obra activa.
     * Cambia del estado BORRADOR a A_ENVIAR.
     */
    @Override
    @Transactional
    public ObraResponseDTO confirmarBorrador(Long id) {
        log.info("🔧 Confirmando borrador de obra ID: {} -> obra activa", id);
        
        Obra obraBorrador = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra borrador no encontrada con ID: " + id));
        
        // Verificar que sea borrador
        if (!obraBorrador.esBorrador()) {
            throw new IllegalStateException("Solo se pueden confirmar obras en estado BORRADOR. Estado actual: " + obraBorrador.getEstado());
        }
        
        // Validar que tiene los datos mínimos requeridos
        validarDatosMinimosParaConfirmacion(obraBorrador);
        
        // Cambiar a estado activo
        obraBorrador.setEstado(EstadoObra.A_ENVIAR); // O el estado inicial que prefieras
        
        Obra obraConfirmada = obraRepository.save(obraBorrador);
        log.info("✅ Obra confirmada. Estado cambiado de BORRADOR a {}", obraConfirmada.getEstado());
        
        // Re-sincronizar con entidades financieras como obra activa
        try {
            entidadFinancieraService.sincronizarDesdeObra(obraConfirmada);
        } catch (Exception e) {
            log.warn("⚠️ Error al re-sincronizar obra confirmada: {}", e.getMessage());
        }
        
        return mapToResponseDTO(obraConfirmada);
    }
    
    /**
     * Obtiene todos los borradores por empresa.
     */
    @Override
    public List<ObraResponseDTO> obtenerBorradores(Long empresaId) {
        log.info("📋 Obteniendo borradores de obras para empresa ID: {}", empresaId);
        
        List<Obra> borradores = obraRepository.findByEsObraManualTrueAndEstado(EstadoObra.BORRADOR.getDisplayName());
        
        // Filtrar por empresa usando el filtro de Hibernate
        return borradores.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * Método auxiliar para validar datos mínimos antes de confirmar.
     */
    private void validarDatosMinimosParaConfirmacion(Obra obra) {
        if (obra.getNombre() == null || obra.getNombre().trim().isEmpty()) {
            throw new IllegalStateException("El nombre de la obra es obligatorio para confirmar el borrador");
        }
        
        if (obra.getDireccionObraCalle() == null || obra.getDireccionObraCalle().trim().isEmpty()) {
            throw new IllegalStateException("La dirección de obra es obligatoria para confirmar el borrador");
        }
        
        if (obra.getDireccionObraAltura() == null || obra.getDireccionObraAltura().trim().isEmpty()) {
            throw new IllegalStateException("La altura/número de obra es obligatorio para confirmar el borrador");
        }
        
        // Agregar más validaciones según tus reglas de negocio
    }
    
    /**
     * Método auxiliar para crear obra base (reutilizado del método crear original).
     */
    private Obra crearObraBase(ObraRequestDTO obraRequestDto, Long clienteId) {
        // TODO: Crear obra desde DTO manualmente por ahora
        Obra nuevaObra = new Obra();
        nuevaObra.setNombre(obraRequestDto.getNombre());
        nuevaObra.setDescripcion(obraRequestDto.getDescripcion());
        nuevaObra.setObservaciones(obraRequestDto.getObservaciones());
        nuevaObra.setDireccionObraCalle(obraRequestDto.getDireccionObraCalle());
        nuevaObra.setDireccionObraAltura(obraRequestDto.getDireccionObraAltura());
        
        // Establecer empresaId obligatorio
        if (obraRequestDto.getEmpresaId() != null) {
            nuevaObra.setEmpresaId(obraRequestDto.getEmpresaId());
        } else {
            // Si no viene en el DTO, intentar obtener del contexto de tenant
            Long empresaIdContexto = TenantContext.getTenantId();
            if (empresaIdContexto != null) {
                nuevaObra.setEmpresaId(empresaIdContexto);
            } else {
                throw new IllegalArgumentException("empresaId es obligatorio para crear una obra");
            }
        }
        
        // Lógica de cliente
        if (clienteId != null) {
            Cliente clienteExistente = clienteService.obtenerPorId(clienteId);
            nuevaObra.setCliente(clienteExistente);
        } else if (obraRequestDto.getNombreSolicitante() != null) {
            // Crear cliente automáticamente
            Cliente nuevoCliente = crearClienteDesdeObra(obraRequestDto);
            nuevaObra.setCliente(nuevoCliente);
        }
        
        // Generar nombre automático si está vacío
        if (nuevaObra.getNombre() == null || nuevaObra.getNombre().trim().isEmpty()) {
            nuevaObra.setNombre(generarNombreObra(nuevaObra));
        }
        
        return nuevaObra;
    }
    
    /**
     * Método auxiliar para crear cliente desde datos de obra.
     */
    private Cliente crearClienteDesdeObra(ObraRequestDTO obraRequestDto) {
        ClienteRequestDTO nuevoClienteDTO = new ClienteRequestDTO();
        nuevoClienteDTO.setNombre(obraRequestDto.getNombreSolicitante());
        nuevoClienteDTO.setNombreSolicitante(obraRequestDto.getNombreSolicitante());
        nuevoClienteDTO.setTelefono(obraRequestDto.getTelefono());
        nuevoClienteDTO.setEmail(obraRequestDto.getMail());
        nuevoClienteDTO.setDireccion(obraRequestDto.getDireccionParticular());

        // Crear el cliente asociado a la empresa
        Long empresaIdParaCliente = obraRequestDto.getEmpresaId() != null
                ? obraRequestDto.getEmpresaId()
                : 1L; // Default empresa ID

        ClienteResponseDTO clienteCreado = clienteService.crearCliente(nuevoClienteDTO, List.of(empresaIdParaCliente));
        return clienteService.obtenerPorId(clienteCreado.getId_cliente());
    }
    
    /**
     * Sobrecarga del método generarNombreObra para entidades Obra.
     */
    private String generarNombreObra(Obra obra) {
        StringBuilder nombre = new StringBuilder();

        if (obra.getDireccionObraCalle() != null && !obra.getDireccionObraCalle().isBlank()) {
            nombre.append(obra.getDireccionObraCalle());
        }

        if (obra.getDireccionObraAltura() != null && !obra.getDireccionObraAltura().isBlank()) {
            nombre.append(" ").append(obra.getDireccionObraAltura());
        }

        if (obra.getDireccionObraBarrio() != null && !obra.getDireccionObraBarrio().isBlank()) {
            nombre.append(" ").append(obra.getDireccionObraBarrio());
        }

        if (obra.getDireccionObraTorre() != null && !obra.getDireccionObraTorre().isBlank()) {
            nombre.append(" Torre ").append(obra.getDireccionObraTorre());
        }

        if (obra.getDireccionObraPiso() != null && !obra.getDireccionObraPiso().isBlank()) {
            nombre.append(" Piso ").append(obra.getDireccionObraPiso());
        }

        if (obra.getDireccionObraDepartamento() != null && !obra.getDireccionObraDepartamento().isBlank()) {
            nombre.append(" Depto ").append(obra.getDireccionObraDepartamento());
        }

        return nombre.toString().trim();
    }
    
    /**
     * Método auxiliar para actualizar campos de obra.
     */
    private void actualizarCamposObra(Obra obraExistente, ObraRequestDTO nuevosData) {
        // Actualizar todos los campos del DTO
        if (nuevosData.getNombre() != null) {
            obraExistente.setNombre(nuevosData.getNombre());
        }
        
        // Dirección
        if (nuevosData.getDireccionObraBarrio() != null) {
            obraExistente.setDireccionObraBarrio(nuevosData.getDireccionObraBarrio());
        }
        if (nuevosData.getDireccionObraCalle() != null) {
            obraExistente.setDireccionObraCalle(nuevosData.getDireccionObraCalle());
        }
        if (nuevosData.getDireccionObraAltura() != null) {
            obraExistente.setDireccionObraAltura(nuevosData.getDireccionObraAltura());
        }
        if (nuevosData.getDireccionObraTorre() != null) {
            obraExistente.setDireccionObraTorre(nuevosData.getDireccionObraTorre());
        }
        if (nuevosData.getDireccionObraPiso() != null) {
            obraExistente.setDireccionObraPiso(nuevosData.getDireccionObraPiso());
        }
        if (nuevosData.getDireccionObraDepartamento() != null) {
            obraExistente.setDireccionObraDepartamento(nuevosData.getDireccionObraDepartamento());
        }
        
        // Fechas
        if (nuevosData.getFechaInicio() != null) {
            obraExistente.setFechaInicio(nuevosData.getFechaInicio());
        }
        if (nuevosData.getFechaFin() != null) {
            obraExistente.setFechaFin(nuevosData.getFechaFin());
        }
        
        // Presupuestos y desglose
        if (nuevosData.getPresupuestoEstimado() != null) {
            obraExistente.setPresupuestoEstimado(nuevosData.getPresupuestoEstimado());
        }
        if (nuevosData.getPresupuestoJornales() != null) {
            obraExistente.setPresupuestoJornales(nuevosData.getPresupuestoJornales());
        }
        if (nuevosData.getPresupuestoMateriales() != null) {
            obraExistente.setPresupuestoMateriales(nuevosData.getPresupuestoMateriales());
        }
        if (nuevosData.getImporteGastosGeneralesObra() != null) {
            obraExistente.setImporteGastosGeneralesObra(nuevosData.getImporteGastosGeneralesObra());
        }
        
        // Honorarios
        if (nuevosData.getPresupuestoHonorarios() != null) {
            obraExistente.setPresupuestoHonorarios(nuevosData.getPresupuestoHonorarios());
        }
        if (nuevosData.getTipoHonorarioPresupuesto() != null) {
            obraExistente.setTipoHonorarioPresupuesto(nuevosData.getTipoHonorarioPresupuesto());
        }
        
        // Honorarios individuales por categoría
        if (nuevosData.getHonorarioJornalesObra() != null) {
            obraExistente.setHonorarioJornalesObra(nuevosData.getHonorarioJornalesObra());
        }
        if (nuevosData.getTipoHonorarioJornalesObra() != null) {
            obraExistente.setTipoHonorarioJornalesObra(nuevosData.getTipoHonorarioJornalesObra());
        }
        if (nuevosData.getHonorarioMaterialesObra() != null) {
            obraExistente.setHonorarioMaterialesObra(nuevosData.getHonorarioMaterialesObra());
        }
        if (nuevosData.getTipoHonorarioMaterialesObra() != null) {
            obraExistente.setTipoHonorarioMaterialesObra(nuevosData.getTipoHonorarioMaterialesObra());
        }
        if (nuevosData.getHonorarioGastosGeneralesObra() != null) {
            obraExistente.setHonorarioGastosGeneralesObra(nuevosData.getHonorarioGastosGeneralesObra());
        }
        if (nuevosData.getTipoHonorarioGastosGeneralesObra() != null) {
            obraExistente.setTipoHonorarioGastosGeneralesObra(nuevosData.getTipoHonorarioGastosGeneralesObra());
        }
        if (nuevosData.getHonorarioMayoresCostosObra() != null) {
            obraExistente.setHonorarioMayoresCostosObra(nuevosData.getHonorarioMayoresCostosObra());
        }
        if (nuevosData.getTipoHonorarioMayoresCostosObra() != null) {
            obraExistente.setTipoHonorarioMayoresCostosObra(nuevosData.getTipoHonorarioMayoresCostosObra());
        }
        
        // Descuentos sobre importes base
        if (nuevosData.getDescuentoJornalesObra() != null) {
            obraExistente.setDescuentoJornalesObra(nuevosData.getDescuentoJornalesObra());
        }
        if (nuevosData.getTipoDescuentoJornalesObra() != null) {
            obraExistente.setTipoDescuentoJornalesObra(nuevosData.getTipoDescuentoJornalesObra());
        }
        if (nuevosData.getDescuentoMaterialesObra() != null) {
            obraExistente.setDescuentoMaterialesObra(nuevosData.getDescuentoMaterialesObra());
        }
        if (nuevosData.getTipoDescuentoMaterialesObra() != null) {
            obraExistente.setTipoDescuentoMaterialesObra(nuevosData.getTipoDescuentoMaterialesObra());
        }
        if (nuevosData.getDescuentoGastosGeneralesObra() != null) {
            obraExistente.setDescuentoGastosGeneralesObra(nuevosData.getDescuentoGastosGeneralesObra());
        }
        if (nuevosData.getTipoDescuentoGastosGeneralesObra() != null) {
            obraExistente.setTipoDescuentoGastosGeneralesObra(nuevosData.getTipoDescuentoGastosGeneralesObra());
        }
        if (nuevosData.getDescuentoMayoresCostosObra() != null) {
            obraExistente.setDescuentoMayoresCostosObra(nuevosData.getDescuentoMayoresCostosObra());
        }
        if (nuevosData.getTipoDescuentoMayoresCostosObra() != null) {
            obraExistente.setTipoDescuentoMayoresCostosObra(nuevosData.getTipoDescuentoMayoresCostosObra());
        }
        
        // Descuentos sobre honorarios
        if (nuevosData.getDescuentoHonorarioJornalesObra() != null) {
            obraExistente.setDescuentoHonorarioJornalesObra(nuevosData.getDescuentoHonorarioJornalesObra());
        }
        if (nuevosData.getTipoDescuentoHonorarioJornalesObra() != null) {
            obraExistente.setTipoDescuentoHonorarioJornalesObra(nuevosData.getTipoDescuentoHonorarioJornalesObra());
        }
        if (nuevosData.getDescuentoHonorarioMaterialesObra() != null) {
            obraExistente.setDescuentoHonorarioMaterialesObra(nuevosData.getDescuentoHonorarioMaterialesObra());
        }
        if (nuevosData.getTipoDescuentoHonorarioMaterialesObra() != null) {
            obraExistente.setTipoDescuentoHonorarioMaterialesObra(nuevosData.getTipoDescuentoHonorarioMaterialesObra());
        }
        if (nuevosData.getDescuentoHonorarioGastosGeneralesObra() != null) {
            obraExistente.setDescuentoHonorarioGastosGeneralesObra(nuevosData.getDescuentoHonorarioGastosGeneralesObra());
        }
        if (nuevosData.getTipoDescuentoHonorarioGastosGeneralesObra() != null) {
            obraExistente.setTipoDescuentoHonorarioGastosGeneralesObra(nuevosData.getTipoDescuentoHonorarioGastosGeneralesObra());
        }
        if (nuevosData.getDescuentoHonorarioMayoresCostosObra() != null) {
            obraExistente.setDescuentoHonorarioMayoresCostosObra(nuevosData.getDescuentoHonorarioMayoresCostosObra());
        }
        if (nuevosData.getTipoDescuentoHonorarioMayoresCostosObra() != null) {
            obraExistente.setTipoDescuentoHonorarioMayoresCostosObra(nuevosData.getTipoDescuentoHonorarioMayoresCostosObra());
        }
        
        // Descripción y observaciones
        if (nuevosData.getDescripcion() != null) {
            obraExistente.setDescripcion(nuevosData.getDescripcion());
        }
        if (nuevosData.getObservaciones() != null) {
            obraExistente.setObservaciones(nuevosData.getObservaciones());
        }
    }

    // ================== MAPPERS TEMPORALES (REEMPLAZAR MAPSTRUCT) ==================
    
    private ObraSimpleDTO mapToSimpleDTO(Obra obra) {
        ObraSimpleDTO dto = new ObraSimpleDTO();
        dto.id = obra.getId();
        dto.nombre = obra.getNombre();
        dto.estado = EstadoObra.fromDisplayName(obra.getEstado());
        dto.clienteId = obra.getCliente() != null ? obra.getCliente().getId() : null;
        return dto;
    }
    
    private ObraResponseDTO mapToResponseDTO(Obra obra) {
        ObraResponseDTO dto = new ObraResponseDTO();
        dto.setId(obra.getId());
        dto.setNombre(obra.getNombre());
        dto.setEstado(EstadoObra.fromDisplayName(obra.getEstado()));
        dto.setIdCliente(obra.getCliente() != null ? obra.getCliente().getId() : null);
        dto.setDescripcion(obra.getDescripcion());
        dto.setObservaciones(obra.getObservaciones());
        dto.setFechaInicio(obra.getFechaInicio());
        dto.setFechaFin(obra.getFechaFin());
        dto.setPresupuestoEstimado(obra.getPresupuestoEstimado());
        
        // Mapear TODOS los campos de honorarios/descuentos
        dto.setHonorarioJornalesObra(obra.getHonorarioJornalesObra());
        dto.setTipoHonorarioJornalesObra(obra.getTipoHonorarioJornalesObra());
        dto.setHonorarioMaterialesObra(obra.getHonorarioMaterialesObra());
        dto.setTipoHonorarioMaterialesObra(obra.getTipoHonorarioMaterialesObra());
        dto.setHonorarioGastosGeneralesObra(obra.getHonorarioGastosGeneralesObra());
        dto.setTipoHonorarioGastosGeneralesObra(obra.getTipoHonorarioGastosGeneralesObra());
        dto.setHonorarioMayoresCostosObra(obra.getHonorarioMayoresCostosObra());
        dto.setTipoHonorarioMayoresCostosObra(obra.getTipoHonorarioMayoresCostosObra());
        
        // Descuentos sobre importes base (8 campos)
        dto.setDescuentoJornalesObra(obra.getDescuentoJornalesObra());
        dto.setTipoDescuentoJornalesObra(obra.getTipoDescuentoJornalesObra());
        dto.setDescuentoMaterialesObra(obra.getDescuentoMaterialesObra());
        dto.setTipoDescuentoMaterialesObra(obra.getTipoDescuentoMaterialesObra());
        dto.setDescuentoGastosGeneralesObra(obra.getDescuentoGastosGeneralesObra());
        dto.setTipoDescuentoGastosGeneralesObra(obra.getTipoDescuentoGastosGeneralesObra());
        dto.setDescuentoMayoresCostosObra(obra.getDescuentoMayoresCostosObra());
        dto.setTipoDescuentoMayoresCostosObra(obra.getTipoDescuentoMayoresCostosObra());
        
        // Descuentos sobre honorarios (8 campos)
        dto.setDescuentoHonorarioJornalesObra(obra.getDescuentoHonorarioJornalesObra());
        dto.setTipoDescuentoHonorarioJornalesObra(obra.getTipoDescuentoHonorarioJornalesObra());
        dto.setDescuentoHonorarioMaterialesObra(obra.getDescuentoHonorarioMaterialesObra());
        dto.setTipoDescuentoHonorarioMaterialesObra(obra.getTipoDescuentoHonorarioMaterialesObra());
        dto.setDescuentoHonorarioGastosGeneralesObra(obra.getDescuentoHonorarioGastosGeneralesObra());
        dto.setTipoDescuentoHonorarioGastosGeneralesObra(obra.getTipoDescuentoHonorarioGastosGeneralesObra());
        dto.setDescuentoHonorarioMayoresCostosObra(obra.getDescuentoHonorarioMayoresCostosObra());
        dto.setTipoDescuentoHonorarioMayoresCostosObra(obra.getTipoDescuentoHonorarioMayoresCostosObra());
        
        // Dirección (6 campos)
        dto.setDireccionObraCalle(obra.getDireccionObraCalle());
        dto.setDireccionObraAltura(obra.getDireccionObraAltura());
        dto.setDireccionObraBarrio(obra.getDireccionObraBarrio());
        dto.setDireccionObraTorre(obra.getDireccionObraTorre());
        dto.setDireccionObraPiso(obra.getDireccionObraPiso());
        dto.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
        
        // Presupuesto base (4 categorías)
        dto.setPresupuestoJornales(obra.getPresupuestoJornales());
        dto.setPresupuestoMateriales(obra.getPresupuestoMateriales());
        dto.setImporteGastosGeneralesObra(obra.getImporteGastosGeneralesObra());
        dto.setPresupuestoMayoresCostos(obra.getPresupuestoMayoresCostos());
        
        // Relaciones
        dto.setPresupuestoNoClienteId(obra.getPresupuestoNoClienteId());
        dto.setObraOrigenId(obra.getObraOrigenId());
        dto.setEmpresaId(obra.getEmpresaId());
        dto.setEsObraManual(obra.getEsObraManual());
        dto.setEsObraTrabajoExtra(obra.getEsObraTrabajoExtra());
        dto.setFechaCreacion(obra.getFechaCreacion());
        
        // Datos del cliente
        if (obra.getCliente() != null) {
            dto.setNombreSolicitante(obra.getCliente().getNombreSolicitante());
            dto.setTelefono(obra.getCliente().getTelefono());
            dto.setMail(obra.getCliente().getEmail());
            dto.setDireccionParticular(obra.getCliente().getDireccion());
        }
        
        return dto;
    }
    
    // ================== MÉTODOS AUXILIARES PARA LISTAS ==================
    private List<ObraResponseDTO> mapToResponseDTOList(List<Obra> obras) {
        if (obras == null || obras.isEmpty()) {
            return List.of();
        }
        return obras.stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }
    
    private List<ObraSimpleDTO> mapToSimpleDTOList(List<Obra> obras) {
        if (obras == null || obras.isEmpty()) {
            return List.of();
        }
        return obras.stream()
                .map(this::mapToSimpleDTO)
                .collect(Collectors.toList());
    }

}