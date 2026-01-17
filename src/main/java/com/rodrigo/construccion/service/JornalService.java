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
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class JornalService {

    private final JornalRepository jornalRepository;
    private final ProfesionalJornalMapper profesionalJornalMapper;
    private final IProfesionalObraService profesionalObraService;

    /* Peticiones con respuestas DTO para evitar bucles infinitos */

    public Page<ProfesionalJornalResponseDTO> obtenerTodosPaginados(Pageable pageable) {
        Page<Jornal> jornalesPage = jornalRepository.findAll(pageable);
        return jornalesPage.map(profesionalJornalMapper::toProfesionalJornalDTO);
    }

    /* Obtiene todos los jornales y los agrupa por profesional y obra. Sin importar la empresa. */
    public List<ResumenJornalesProfesionalDTO> obtenerTodosAgrupados() {
        List<Jornal> todosLosJornales = jornalRepository.findAll(Sort.by("fecha"));
        return profesionalJornalMapper.toResumenDTOListFromJornales(todosLosJornales);
    }

    /* Obtener jornal por ID */
    public ProfesionalJornalResponseDTO obtenerJornalPorId(Long id) {
        Jornal jornalEncontrado = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con ID: " + id));
        return profesionalJornalMapper.toProfesionalJornalDTO(jornalEncontrado);
    }

    /* Busca jornales por empresa */
    public List<ResumenJornalesProfesionalDTO> buscarPorEmpresa(Long empresaId) {
        List<Jornal> jornalesPorEmpresa = jornalRepository.findByEmpresaId(empresaId, Sort.by("fecha"));
        return profesionalJornalMapper.toResumenDTOListFromJornales(jornalesPorEmpresa);
    }

    /* Crea un nuevo jornal */
    public ProfesionalJornalResponseDTO crearJornal(JornalRequestDTO jornalRequestDto) {
        // 1. Buscar la entidad de asignación. Si no existe, lanzará una excepción que
        // ya viene desde profesionalObraService.
        ProfesionalObra asignacion = profesionalObraService.obtenerPorId(jornalRequestDto.getAsignacionId());
        Jornal jornal = profesionalJornalMapper.toEntity(jornalRequestDto);
        jornal.setAsignacion(asignacion);
        Jornal jornalCreado = jornalRepository.save(jornal);
        return profesionalJornalMapper.toProfesionalJornalDTO(jornalCreado);
    }

    /* Actualiza un jornal existente */
    public ProfesionalJornalResponseDTO actualizarJornal(Long id, JornalRequestDTO jornalRequestDto) {
        Jornal jornalExistente = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con ID: " + id));
        profesionalJornalMapper.updateEntityFromDto(jornalRequestDto, jornalExistente);
        Jornal jornalGuardado = jornalRepository.save(jornalExistente);
        return profesionalJornalMapper.toProfesionalJornalDTO(jornalGuardado);
    }

    /* Eliminar jornal */
    public void eliminar(Long id) {
        Jornal jornal = jornalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Jornal no encontrado con ID: " + id));
        jornalRepository.delete(jornal);
    }

    /* Buscar jornales por rango de fechas y empresa - Agrupa los resultados por profesional y obra */
    public List<ResumenJornalesProfesionalDTO> buscarJornalesPorRangoFechas(Long empresaId, LocalDate fechaInicio,
                                                                            LocalDate fechaFin) {
        List<Jornal> jornalesEnRango = jornalRepository.findByEmpresaIdAndFechaBetween(empresaId, fechaInicio,
                fechaFin, Sort.by("fecha"));
        return profesionalJornalMapper.toResumenDTOListFromJornales(jornalesEnRango);
    }

    /* Buscar jornales por valor mínimo por hora y empresa - Agrupa los resultados por profesional y obra */
    public List<ResumenJornalesProfesionalDTO> buscarJornalPorValorMinimo(Long empresaId, BigDecimal valorMinimo) {
        List<Jornal> jornales = jornalRepository.findByEmpresaIdAndValorHoraGreaterThanEqual(empresaId, valorMinimo,
                Sort.by("fecha"));
        return profesionalJornalMapper.toResumenDTOListFromJornales(jornales);
    }

    /* Obtener jornales del mes actual */
    public List<ResumenJornalesProfesionalDTO> obtenerJornalesDelMesActual(Long empresaId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
        return buscarJornalesPorRangoFechas(empresaId, inicioMes, finMes);
    }

    /**
     * Obtiene un resumen de estadísticas de jornales para una empresa específica
     * Incluye: total de jornales, jornales del mes actual, monto total y horas totales del mes
     */
    public EstadisticasJornalResponseDTO obtenerEstadisticas(Long empresaId) {
        LocalDate inicioMes = LocalDate.now().withDayOfMonth(1);
        LocalDate finMes = LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());

        long totalJornales = jornalRepository.countByEmpresaId(empresaId);
        long jornalesMesActual = jornalRepository.countByFechaBetweenAndEmpresaId(inicioMes, finMes, empresaId);
        BigDecimal montoTotalMesActual = jornalRepository.sumMontoTotalByFechaBetweenAndEmpresaId(empresaId, inicioMes, finMes);
        BigDecimal horasTotalMesActual = jornalRepository.sumHorasTrabajadasByFechaBetweenAndEmpresaId(empresaId, inicioMes, finMes);

        return profesionalJornalMapper.toEstadisticasDTO(
                totalJornales,
                jornalesMesActual,
                montoTotalMesActual,
                horasTotalMesActual
        );
    }

    /**
     * Obtiene un resumen de jornales para un período y empresa específicos
     * Incluye: cantidad, monto total, horas totales y valor promedio por hora
     */
    public ResumenPeriodoJornalDTO obtenerResumenPeriodo(Long empresaId, LocalDate fechaInicio, LocalDate fechaFin) {
        long cantidadJornales = jornalRepository.countByFechaBetweenAndEmpresaId(fechaInicio, fechaFin, empresaId);
        BigDecimal montoTotal = jornalRepository.sumMontoTotalByFechaBetweenAndEmpresaId(empresaId, fechaInicio, fechaFin);
        BigDecimal horasTotal = jornalRepository.sumHorasTrabajadasByFechaBetweenAndEmpresaId(empresaId, fechaInicio, fechaFin);

        // Calcular el valor promedio por hora de forma segura
        BigDecimal valorPromedioHora = BigDecimal.ZERO;
        if (horasTotal.compareTo(BigDecimal.ZERO) > 0) {
            valorPromedioHora = montoTotal.divide(horasTotal, 2, java.math.RoundingMode.HALF_UP);
        }

        // Usar el mapper para construir el DTO de respuesta
        String periodo = fechaInicio.toString() + " - " + fechaFin.toString();
        return profesionalJornalMapper.toResumenPeriodoDTO(
                periodo,
                cantidadJornales,
                montoTotal,
                horasTotal,
                valorPromedioHora
        );
    }

}