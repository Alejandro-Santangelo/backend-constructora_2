package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.ObraMapper;
import com.rodrigo.construccion.dto.mapper.ProfesionalMapper;
import com.rodrigo.construccion.dto.request.ClienteRequestDTO;
import com.rodrigo.construccion.dto.request.ProfesionalFormDTO;
import com.rodrigo.construccion.dto.request.ProfesionalRequestDTO;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* Servicio simplificado para gestión de obras */
@Service
@RequiredArgsConstructor
public class ObraService implements IObraService {

    private final ObraRepository obraRepository;
    private final IClienteService clienteService;
    private final IProfesionalService profesionalService;
    private final ObraMapper obraMapper;
    private final IEmpresaService empresaService;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final PresupuestoNoClienteRepository presupuestoNoClienteRepository;

    /* Obtener obra por ID */
    @Override
    public ObraSimpleDTO obtenerPorId(Long id) {
        Obra obraEncontrada = obraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + id));

        return obraMapper.toSimpleDTO(obraEncontrada);
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

    /* Obtener obras por cliente */
    @Override
    public List<ObraResponseDTO> obtenerPorCliente(Long clienteId) {
        clienteService.obtenerPorId(clienteId);
        List<Obra> obrasEncontradasPorCliente = obraRepository.findByCliente_Id(clienteId);
        return obraMapper.toResponseDTOList(obrasEncontradasPorCliente);
    }

    /* Obtener obras activas (simplificado) */
    @Override
    public List<ObraSimpleDTO> obtenerActivas() {
        List<Obra> obrasActivas = obraRepository.findObrasActivas();
        return obraMapper.toSimpleDTOList(obrasActivas);
    }

    @Override
    public List<ObraSimpleDTO> obtenerPorEstado(EstadoObra estado) {
        List<Obra> obrasEncontradas = obraRepository.findByEstado(estado.getDisplayName());
        return obraMapper.toSimpleDTOList(obrasEncontradas);
    }

    /* Obtener obras por empresa */
    @Override
    public List<ObraResponseDTO> obtenerPorEmpresa(Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        List<Obra> obrasPorEmpresa = obraRepository.findByEmpresaId(empresaId);
        return obraMapper.toResponseDTOList(obrasPorEmpresa);
    }

    /* Obtener todas las obras */
    @Override
    public List<ObraResponseDTO> obtenerTodas() {
        List<Obra> obras = obraRepository.findAll();
        return obraMapper.toResponseDTOList(obras);
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
        Obra obra = obraMapper.toEntity(obraRequestDto);
        obra.setCliente(cliente);

        // LÓGICA 3: Generar nombre automáticamente si está vacío
        if (obra.getNombre() == null || obra.getNombre().isBlank()) {
            obra.setNombre(generarNombreObra(obraRequestDto));
        }

        // Si no se proporciona una fecha de inicio, se asigna la fecha actual
        if (obra.getFechaInicio() == null) {
            obra.setFechaInicio(LocalDate.now());
        }

        // Guardar la obra primero
        Obra obraGuardada = obraRepository.save(obra);

        // LÓGICA 4: Asignar profesionales si hay en el formulario
        if (obraRequestDto.getProfesionalesAsignadosForm() != null && !obraRequestDto.getProfesionalesAsignadosForm().isEmpty()) {
            asignarProfesionalesDesdeFormulario(obraGuardada, obraRequestDto.getProfesionalesAsignadosForm());
        }

        return obraMapper.toResponseDTO(obraGuardada);
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
            asignacion.setValorHoraAsignado(profForm.getValorHora());
            asignacion.setActivo(true);
            asignacion.setMontoAsignado(BigDecimal.ZERO);
            asignacion.setSaldoDisponible(BigDecimal.ZERO);

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
        obraMapper.updateEntityFromDto(obraRequestDTO, obraExistente);

        // Guardamos la entidad actualizada.
        Obra obraGuardada = obraRepository.save(obraExistente);

        return obraMapper.toResponseDTO(obraGuardada);
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

        return obraMapper.toResponseDTO(obra);
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
     * Devuelve una lista con todos los roles disponibles definidos en el enum
     * RolEnObra.
     * Ideal para poblar dropdowns en el frontend.
     */
    @Override
    public List<String> obtenerEstadosObra() {
        return Arrays.stream(EstadoObra.values())
                .map(EstadoObra::getDisplayName)
                .collect(Collectors.toList());
    }

    /* Buscar obras por nombre (simplificado) - NO USADA  */
    public Page<Obra> buscarPorNombre(String nombre, Pageable pageable) {
        return obraRepository.findAll(pageable);
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

}