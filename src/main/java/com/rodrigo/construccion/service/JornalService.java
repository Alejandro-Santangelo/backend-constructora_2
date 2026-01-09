package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.ProfesionalJornalMapper;
import com.rodrigo.construccion.dto.request.JornalRequestDTO;
import com.rodrigo.construccion.dto.response.EstadisticasJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalJornalResponseDTO;
import com.rodrigo.construccion.dto.response.ResumenJornalesProfesionalDTO;
import com.rodrigo.construccion.dto.response.ResumenPeriodoJornalDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Jornal;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.repository.JornalRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Jornales
 * Maneja la lógica de negocio para operaciones con jornales de trabajo
 */
@Service
@RequiredArgsConstructor
@Transactional
public class JornalService {

    private final JornalRepository jornalRepository;
    private final ProfesionalJornalMapper profesionalJornalMapper;
    private final IProfesionalObraService profesionalObraService;

    /* Peticiones con respuestas DTO para evitar bucles infinitos */

    /* Obtener jornal por ID */
    public ProfesionalJornalResponseDTO obtenerJornalPorId(Long id) {
        Jornal jornalEncontrado = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con ID: " + id));
        return profesionalJornalMapper.toProfesionalJornalDTO(jornalEncontrado);
    }

    /**
     * Busca jornales por empresa y los agrupa por profesional y obra para una
     * respuesta más limpia y eficiente.
     * TODO: ADAPTAR - Repositorio comentado temporalmente
     */
    public List<ResumenJornalesProfesionalDTO> buscarPorEmpresaAgrupado(Long empresaId) {
        // List<Jornal> jornalesPorEmpresa = jornalRepository.findByEmpresaId(empresaId, Sort.by("fecha"));
        // return profesionalJornalMapper.toResumenDTOListFromJornales(jornalesPorEmpresa);
        return new ArrayList<>();
    }

    /* Buscar jornales por rango de fechas 
     * TODO: ADAPTAR - Repositorio comentado temporalmente
     */
    public List<ResumenJornalesProfesionalDTO> buscarJornalesPorRangoFechas(Long empresaId, LocalDate fechaInicio,
            LocalDate fechaFin) {
        // List<Jornal> jornalesEnRango = jornalRepository.findByEmpresaIdAndFechaBetween(empresaId, fechaInicio,
        //         fechaFin, Sort.by("fecha"));
        // return profesionalJornalMapper.toResumenDTOListFromJornales(jornalesEnRango);
        return new ArrayList<>();
    }

    /* Buscar jornales por valor mínimo por hora 
     * TODO: ADAPTAR - Repositorio comentado temporalmente
     */
    public List<ResumenJornalesProfesionalDTO> buscarJornalPorValorMinimo(Long empresaId, BigDecimal valorMinimo) {
        // List<Jornal> jornales = jornalRepository.findByEmpresaIdAndValorHoraGreaterThanEqual(empresaId, valorMinimo,
        //         Sort.by("fecha"));
        // return profesionalJornalMapper.toResumenDTOListFromJornales(jornales);
        return new ArrayList<>();
    }

    /* Obtener jornales del mes actual */
    public List<ResumenJornalesProfesionalDTO> obtenerJornalesDelMesActual(Long empresaId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return buscarJornalesPorRangoFechas(empresaId, inicioMes, finMes);
    }

    /**
     * Obtiene todos los jornales y los agrupa por profesional y obra. Sin importar
     * la empresa.
     */
    public List<ResumenJornalesProfesionalDTO> obtenerTodosAgrupados() {
        List<Jornal> todosLosJornales = jornalRepository.findAll(Sort.by("fecha"));
        return profesionalJornalMapper.toResumenDTOListFromJornales(todosLosJornales);
    }

    /**
     * Crea un nuevo jornal a partir de un DTO, lo asocia a una asignación
     * existente y devuelve un DTO de respuesta completo.
     */
    public ProfesionalJornalResponseDTO crearJornal(JornalRequestDTO jornalRequestDto) {
        // 1. Buscar la entidad de asignación. Si no existe, lanzará una excepción que
        // ya viene desde profesionalObraService.
        ProfesionalObra asignacion = profesionalObraService.obtenerPorId(jornalRequestDto.getAsignacionId());
        Jornal jornal = profesionalJornalMapper.toEntity(jornalRequestDto);
        jornal.setAsignacion(asignacion);
        Jornal jornalCreado = jornalRepository.save(jornal);
        return profesionalJornalMapper.toProfesionalJornalDTO(jornalCreado);
    }

    /**
     * Actualiza un jornal existente a partir de un DTO y devuelve la versión
     * actualizada como un DTO de respuesta.
     */
    public ProfesionalJornalResponseDTO actualizarJornal(Long id, JornalRequestDTO jornalRequestDto) {
        Jornal jornalExistente = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con ID: " + id));
        profesionalJornalMapper.updateEntityFromDto(jornalRequestDto, jornalExistente);
        Jornal jornalGuardado = jornalRepository.save(jornalExistente);
        return profesionalJornalMapper.toProfesionalJornalDTO(jornalGuardado);
    }

    /**
     * Obtiene un resumen de estadísticas de jornales para una empresa específica.
     * TODO: ADAPTAR - Repositorio comentado temporalmente
     */
    public EstadisticasJornalResponseDTO obtenerEstadisticas(Long empresaId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        // TODO: Reactivar cuando se adapte el repositorio
        // long totalJornales = jornalRepository.countByEmpresaId(empresaId);
        // long jornalesMesActual = jornalRepository.countByFechaBetweenAndEmpresaId(inicioMes, finMes, empresaId);
        // BigDecimal montoTotalMesActual = jornalRepository.sumMontoTotalByFechaBetweenAndEmpresaId(empresaId, inicioMes,
        //         finMes);
        // BigDecimal horasTotalMesActual = jornalRepository.sumHorasTrabajadasByFechaBetweenAndEmpresaId(empresaId,
        //         inicioMes, finMes);

        return profesionalJornalMapper.toEstadisticasDTO(
                0L,
                0L,
                BigDecimal.ZERO,
                BigDecimal.ZERO);
    }

    /* Obtiene un resumen de jornales para un período y empresa específicos.
     * TODO: ADAPTAR - Repositorio comentado temporalmente
     */
    public ResumenPeriodoJornalDTO obtenerResumenPeriodo(Long empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        // TODO: Reactivar cuando se adapte el repositorio
        // long cantidadJornales = jornalRepository.countByFechaBetweenAndEmpresaId(fechaInicio, fechaFin, empresaId);
        // BigDecimal montoTotal = jornalRepository.sumMontoTotalByFechaBetweenAndEmpresaId(empresaId, fechaInicio,
        //         fechaFin);
        // BigDecimal horasTotal = jornalRepository.sumHorasTrabajadasByFechaBetweenAndEmpresaId(empresaId, fechaInicio,
        //         fechaFin);

        // 2. Calcular el valor promedio de forma segura
        BigDecimal valorPromedioPorHora = BigDecimal.ZERO;
        BigDecimal valorPromedioHora = BigDecimal.ZERO;
        BigDecimal horasTotal = BigDecimal.ZERO;
        BigDecimal montoTotal = BigDecimal.ZERO;
        long cantidadJornales = 0L;
        
        // if (horasTotal.compareTo(BigDecimal.ZERO) > 0) {
        //     valorPromedioHora = montoTotal.divide(horasTotal, 2, java.math.RoundingMode.HALF_UP);
        // }

        // 3. Usar el mapper para construir el DTO de respuesta
        String periodo = fechaInicio.toString() + " - " + fechaFin.toString();
        return profesionalJornalMapper.toResumenPeriodoDTO(
                periodo,
                cantidadJornales,
                montoTotal,
                horasTotal,
                valorPromedioHora);
    }


    /* MÉTODOS NO RECOMENDADOS PARA USAR */
    /* Obtener todos los jornales */
    public List<Jornal> obtenerTodos() {
        return jornalRepository.findAll();
    }

    /* Obtener jornales con paginación */
    public Page<Jornal> obtenerTodosPaginados(Pageable pageable) {
        return jornalRepository.findAll(pageable);
    }

    /* Obtener jornal por ID */
    public Optional<Jornal> obtenerPorId(Long id) {
        return jornalRepository.findById(id);
    }

    /* Crear nuevo jornal */
    public Jornal crear(Jornal jornal) {
        return jornalRepository.save(jornal);
    }

    /* Actualizar jornal existente */
    public Jornal actualizar(Long id, Jornal jornalActualizado) {
        return jornalRepository.findById(id)
                .map(jornal -> {
                    // Actualizar campos disponibles
                    if (jornalActualizado.getFecha() != null) {
                        jornal.setFecha(jornalActualizado.getFecha());
                    }
                    if (jornalActualizado.getHorasTrabajadas() != null) {
                        jornal.setHorasTrabajadas(jornalActualizado.getHorasTrabajadas());
                    }
                    if (jornalActualizado.getValorHora() != null) {
                        jornal.setValorHora(jornalActualizado.getValorHora());
                    }
                    if (jornalActualizado.getObservaciones() != null) {
                        jornal.setObservaciones(jornalActualizado.getObservaciones());
                    }

                    return jornalRepository.save(jornal);
                })
                .orElseThrow(() -> new RuntimeException("Jornal no encontrado con ID: " + id));
    }

    /* Eliminar jornal */
    public void eliminar(Long id) {
        Jornal jornal = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con ID: " + id));
        jornalRepository.delete(jornal);
    }

    /* Buscar jornales por rango de fechas */
    public List<Jornal> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return jornalRepository.findAll().stream()
                .filter(jornal -> {
                    LocalDate fecha = jornal.getFecha();
                    return fecha != null &&
                            !fecha.isBefore(fechaInicio) &&
                            !fecha.isAfter(fechaFin);
                })
                .toList();
    }

    /* Buscar jornales por valor mínimo por hora */
    public List<Jornal> buscarPorValorMinimo(BigDecimal valorMinimo) {
        return jornalRepository.findAll().stream()
                .filter(jornal -> jornal.getValorHora() != null &&
                        jornal.getValorHora().compareTo(valorMinimo) >= 0)
                .toList();
    }

    /* Calcular total ganado en un período */
    public BigDecimal calcularTotalPeriodo(LocalDate fechaInicio, LocalDate fechaFin) {
        return buscarPorRangoFechas(fechaInicio, fechaFin).stream()
                .filter(jornal -> jornal.getHorasTrabajadas() != null && jornal.getValorHora() != null)
                .map(jornal -> jornal.getValorHora().multiply(jornal.getHorasTrabajadas()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /* Obtener jornales del mes actual */
    public List<Jornal> obtenerJornalesMesActual() {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return buscarPorRangoFechas(inicioMes, finMes);
    }

    /* Obtener estadísticas básicas */
    public long contarTotal() {
        return jornalRepository.count();
    }

    /* Calcular horas totales trabajadas */
    public BigDecimal calcularHorasTotales(LocalDate fechaInicio, LocalDate fechaFin) {
        return buscarPorRangoFechas(fechaInicio, fechaFin).stream()
                .map(Jornal::getHorasTrabajadas)
                .filter(horas -> horas != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

}