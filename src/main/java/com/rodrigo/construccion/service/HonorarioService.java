package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.HonorarioMapper;
import com.rodrigo.construccion.dto.request.HonorarioRequestDTO;
import com.rodrigo.construccion.dto.response.HonorarioProfesionalObraResponseDTO;
import com.rodrigo.construccion.dto.response.EstadisticasHonorarioResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoHonorarioDTO;
import com.rodrigo.construccion.dto.response.ResumenHonorariosProfesionalDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.model.entity.Honorario;
import com.rodrigo.construccion.repository.HonorarioRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import java.math.RoundingMode;

@Service
@RequiredArgsConstructor
@Transactional
public class HonorarioService {

    private final HonorarioRepository honorarioRepository;
    private final HonorarioMapper honorarioMapper;
    private final IProfesionalService profesionalService;
    private final IObraService obraService;
    private final IProfesionalObraService profesionalObraService;
    private final IEmpresaService empresaService;

    /* Obtener todos los honorarios que retornan en DTO */
    public List<ResumenHonorariosProfesionalDTO> obtenerTodosHonorarios() {
        List<Honorario> honorarios = honorarioRepository.findAll();
        return honorarioMapper.toResumenHonorariosProfesionalDTOList(honorarios);
    }

    /**
     * Obtiene una página de resúmenes de honorarios agrupados por profesional.
     * Este método es óptimo porque pagina sobre los profesionales, no sobre los
     * honorarios, evitando así la división de los datos de un mismo profesional en múltiples páginas.
     */
    public Page<ResumenHonorariosProfesionalDTO> obtenerTodosHonorariosPaginados(Pageable pageable, Long empresaId) {

        empresaService.findEmpresaById(empresaId);

        // 1. Paginar sobre los profesionales que tienen honorarios.
        Page<Profesional> profesionalesPaginados = profesionalService.findAllWithHonorarios(pageable, empresaId);

        // 2. Obtener la lista de honorarios de los profesionales de ESTA página.
        // Esto evita traer todos los honorarios de la base de datos.
        List<Long> profesionalIds = profesionalesPaginados.getContent().stream().map(Profesional::getId).toList();
        List<Honorario> honorariosDeLaPagina = honorarioRepository.findByProfesional_IdIn(profesionalIds);

        // 3. Usar el mapper para agrupar los honorarios y crear los resúmenes.
        List<ResumenHonorariosProfesionalDTO> resumenes = honorarioMapper
                .toResumenHonorariosProfesionalDTOList(honorariosDeLaPagina);

        // 4. Crear y devolver una nueva página con los DTOs de resumen.
        return new PageImpl<>(resumenes, pageable, profesionalesPaginados.getTotalElements());
    }

    /* Obtener honorario por ID */
    public HonorarioProfesionalObraResponseDTO obtenerHonorarioPorId(Long idHonorario) {
        Honorario honorarioEncontrado = honorarioRepository.findById(idHonorario)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró el honorario con ID: " + idHonorario));

        // 1. Mapear los datos básicos desde la entidad Honorario.
        HonorarioProfesionalObraResponseDTO dto = honorarioMapper
                .toHonorarioProfesionalObraResponseDTO(honorarioEncontrado);

        // 2. Buscar la asignación (ProfesionalObra) para obtener el rol y el valor hora.

        ProfesionalObra asignacion = profesionalObraService.buscarAsignacionEspecifica(
                honorarioEncontrado.getProfesionalId(), honorarioEncontrado.getObraId());

        if (asignacion != null) {
            dto.setRolEnObra(asignacion.getRolEnObra());
            dto.setValorHoraAsignado(asignacion.getValorHoraAsignado());
        }

        return dto;
    }

    /* Crear nuevo honorario */
    public HonorarioProfesionalObraResponseDTO crearHonorario(HonorarioRequestDTO requestDTO) {
        Obra obra = obraService.findById(requestDTO.getObraId());
        Profesional profesional = profesionalService.obtenerPorId(requestDTO.getProfesionalId());

        // 2. Usar el mapper para crear la entidad Honorario a partir del DTO
        Honorario nuevoHonorario = honorarioMapper.toEntity(requestDTO);

        // 3. Asignar las relaciones encontradas
        nuevoHonorario.setObra(obra);
        nuevoHonorario.setProfesional(profesional);

        // 4. Guardar la nueva entidad en la base de datos
        Honorario honorarioGuardado = honorarioRepository.save(nuevoHonorario);

        // 5. Convertir la entidad guardada al DTO de respuesta y retornarlo
        return honorarioMapper.toHonorarioProfesionalObraResponseDTO(honorarioGuardado);
    }

    /* Actualizar honorario existente */
    public HonorarioProfesionalObraResponseDTO actualizarHonorario(Long id, HonorarioRequestDTO requestDTO) {
        // 1. Buscar el honorario existente o lanzar una excepción si no se encuentra.
        Honorario honorarioExistente = honorarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Honorario no encontrado con ID: " + id));

        // 2. Usar el mapper para actualizar los campos simples (fecha, monto,
        // observaciones).
        honorarioMapper.updateEntity(honorarioExistente, requestDTO);

        // 3. Actualizar la relación con la Obra si el ID ha cambiado.
        if (!honorarioExistente.getObra().getId().equals(requestDTO.getObraId())) {
            Obra nuevaObra = obraService.findById(requestDTO.getObraId());
            honorarioExistente.setObra(nuevaObra);
        }

        // 4. Actualizar la relación con el Profesional si el ID ha cambiado.
        if (!honorarioExistente.getProfesional().getId().equals(requestDTO.getProfesionalId())) {
            Profesional nuevoProfesional = profesionalService.obtenerPorId(requestDTO.getProfesionalId());
            honorarioExistente.setProfesional(nuevoProfesional);
        }

        // 5. Guardar la entidad actualizada.
        Honorario honorarioGuardado = honorarioRepository.save(honorarioExistente);

        // 6. Convertir la entidad guardada al DTO de respuesta y retornarlo.
        return honorarioMapper.toHonorarioProfesionalObraResponseDTO(honorarioGuardado);
    }

    /* Eliminar honorario */
    public void eliminar(Long id) {
        Honorario honorarioEncontrado = honorarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Honorario no encontrado con ID: " + id));
        honorarioRepository.delete(honorarioEncontrado);
    }

    /* Buscar honorarios por rango de fechas */
    public List<ResumenHonorariosProfesionalDTO> buscarHonorariosPorRangoFechas(LocalDate fechaInicio,
                                                                                LocalDate fechaFin, Long empresaId) {

        empresaService.findEmpresaById(empresaId);
        // 1. Llamar al método optimizado del repositorio que filtra en la base de
        // datos.
        List<Honorario> honorariosEncontrados = honorarioRepository.findByEmpresaIdAndFechaBetween(empresaId,
                fechaInicio, fechaFin);

        // 2. Usar el mapper para agrupar los resultados por profesional.
        return honorarioMapper.toResumenHonorariosProfesionalDTOList(honorariosEncontrados);
    }

    /* Buscar honorarios por monto mínimo */
    public List<ResumenHonorariosProfesionalDTO> buscarHonorariosPorMontoMinimo(BigDecimal montoMinimo,
                                                                                Long empresaId) {

        empresaService.findEmpresaById(empresaId);
        // 1. Llamar al método optimizado del repositorio que filtra en la base de
        // datos.
        List<Honorario> honorariosEncontrados = honorarioRepository.findByEmpresaIdAndMontoGreaterThanEqual(empresaId,
                montoMinimo);

        // 2. Usar el mapper para agrupar los resultados por profesional.
        return honorarioMapper.toResumenHonorariosProfesionalDTOList(honorariosEncontrados);
    }

    /* Obtener honorarios del mes actual */
    public List<ResumenHonorariosProfesionalDTO> obtenerHonorariosMesActualDto(Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return buscarHonorariosPorRangoFechas(inicioMes, finMes, empresaId);
    }

    /**
     * Obtiene un resumen de estadísticas de honorarios para una empresa específica.
     * Optimizado para usar consultas directas a la base de datos y devolver un DTO
     * tipado.
     */
    public EstadisticasHonorarioResponseDTO obtenerEstadisticasPorEmpresa(Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        // 1. Definir el período del mes actual
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // 2. Realizar consultas eficientes a la base de datos
        long totalHonorarios = honorarioRepository.countByObra_Cliente_Empresa_Id(empresaId);
        long honorariosMesActual = honorarioRepository.countByEmpresaIdAndFechaBetween(empresaId, inicioMes, finMes);
        BigDecimal montoTotalMesActual = honorarioRepository.sumMontoByEmpresaIdAndFechaBetween(empresaId, inicioMes,
                finMes);

        // 3. Delegar la construcción del DTO al mapper.
        return honorarioMapper.toEstadisticasDTO(totalHonorarios, honorariosMesActual,
                montoTotalMesActual != null ? montoTotalMesActual : BigDecimal.ZERO);
    }

    /**
     * Obtiene un resumen de honorarios para un período y empresa específicos.
     * Optimizado para realizar cálculos directamente en la base de datos.
     */
    public ResumenPeriodoHonorarioDTO obtenerResumenPeriodo(LocalDate fechaInicio, LocalDate fechaFin, Long empresaId) {
        empresaService.findEmpresaById(empresaId);
        // 1. Realizar consultas eficientes a la base de datos
        long cantidadHonorarios = honorarioRepository.countByEmpresaIdAndFechaBetween(empresaId, fechaInicio,
                fechaFin);
        BigDecimal montoTotal = honorarioRepository.sumMontoByEmpresaIdAndFechaBetween(empresaId, fechaInicio,
                fechaFin);

        // 2. Calcular el promedio de forma segura
        BigDecimal montoPromedio = BigDecimal.ZERO;
        if (cantidadHonorarios > 0 && montoTotal != null) {
            montoPromedio = montoTotal.divide(BigDecimal.valueOf(cantidadHonorarios), 2, RoundingMode.HALF_UP);
        }

        // 3. Usar el mapper para construir el DTO de respuesta
        String periodo = fechaInicio.toString() + " - " + fechaFin.toString();
        return honorarioMapper.toResumenPeriodoDTO(periodo, cantidadHonorarios,
                montoTotal != null ? montoTotal : BigDecimal.ZERO, montoPromedio);
    }

    /**
     * Realiza una búsqueda avanzada y dinámica de honorarios.
     * Filtra por los criterios proporcionados directamente en la base de datos.
     */
    public List<ResumenHonorariosProfesionalDTO> busquedaAvanzada(Long empresaId, LocalDate fechaInicio,
                                                                  LocalDate fechaFin, BigDecimal montoMinimo) {
        empresaService.findEmpresaById(empresaId);
        // 1. Llamar al método optimizado del repositorio que filtra en la base de
        // datos.
        List<Honorario> honorariosEncontrados = honorarioRepository.busquedaAvanzada(empresaId, fechaInicio, fechaFin,
                montoMinimo);
        // 2. Usar el mapper para agrupar los resultados por profesional.
        return honorarioMapper.toResumenHonorariosProfesionalDTOList(honorariosEncontrados);
    }
}