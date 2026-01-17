package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CheckInRequest;
import com.rodrigo.construccion.dto.request.CheckOutRequest;
import com.rodrigo.construccion.dto.response.AsistenciaObraResponse;
import com.rodrigo.construccion.dto.response.ReporteAsistenciasObraResponseDTO;
import com.rodrigo.construccion.dto.response.ReporteAsistenciasProfesionalDTO;
import com.rodrigo.construccion.exception.AsistenciaDuplicadaException;
import com.rodrigo.construccion.exception.HorarioInvalidoException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.AsistenciaObra;
import com.rodrigo.construccion.model.entity.Jornal;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.repository.AsistenciaObraRepository;
import com.rodrigo.construccion.repository.JornalRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsistenciaObraService implements IAsistenciaObraService{

    private final AsistenciaObraRepository asistenciaRepository;
    private final IProfesionalObraService profesionalObraService;
    private final JornalRepository jornalRepository;

    /* Registrar check-in (entrada) */
    @Override
    @Transactional
    public AsistenciaObraResponse checkIn(CheckInRequest request) {
        // 1. Buscar profesional obra
        ProfesionalObra profesionalObra = profesionalObraService.obtenerPorId(request.getProfesionalObraId());

        // Validar empresa
        if (!profesionalObra.getEmpresaId().equals(request.getEmpresaId())) {
            throw new IllegalArgumentException("El profesional no pertenece a la empresa especificada");
        }

        // 2. Validar que no exista asistencia para ese día
        if (asistenciaRepository.existsByProfesionalObraIdAndFecha(request.getProfesionalObraId(),
                request.getFecha(),
                request.getEmpresaId())) {
            throw new AsistenciaDuplicadaException(
                    "Ya existe un registro de asistencia para este profesional en la fecha: " + request.getFecha()
            );
        }

        // 3. Validar que la fecha no sea futura
        if (request.getFecha().isAfter(LocalDate.now())) {
            throw new HorarioInvalidoException("La fecha no puede ser futura");
        }

        // 4. Crear asistencia
        AsistenciaObra asistencia = new AsistenciaObra();
        asistencia.setProfesionalObra(profesionalObra);
        asistencia.setFecha(request.getFecha());
        asistencia.setHoraEntrada(request.getHoraEntrada());
        asistencia.setLatitudEntrada(request.getLatitudEntrada());
        asistencia.setLongitudEntrada(request.getLongitudEntrada());
        asistencia.setEmpresaId(request.getEmpresaId());

        // 5. Guardar
        asistencia = asistenciaRepository.save(asistencia);

        return toResponse(asistencia);
    }

    /* Registrar check-out (salida) */
    @Override
    @Transactional
    public AsistenciaObraResponse checkOut(Long asistenciaId, CheckOutRequest request) {
        // 1. Buscar asistencia
        AsistenciaObra asistencia = asistenciaRepository.findByIdAndEmpresaId(asistenciaId, request.getEmpresaId())
                .orElseThrow(() -> new ResourceNotFoundException("Asistencia", asistenciaId));

        // 2. Validar que no tenga ya check-out
        if (asistencia.tieneCheckOut()) {
            throw new HorarioInvalidoException("Este registro ya tiene check-out registrado");
        }

        // 3. Validar que hora_salida > hora_entrada
        if (request.getHoraSalida().isBefore(asistencia.getHoraEntrada())) {
            throw new HorarioInvalidoException("La hora de salida no puede ser anterior a la hora de entrada");
        }

        // 4. Registrar salida
        asistencia.setHoraSalida(request.getHoraSalida());
        asistencia.setLatitudSalida(request.getLatitudSalida());
        asistencia.setLongitudSalida(request.getLongitudSalida());

        // 5. Calcular horas trabajadas automáticamente
        asistencia.calcularHorasTrabajadas();

        // 6. Guardar
        asistencia = asistenciaRepository.save(asistencia);

        // 7. AUTOMÁTICO: Crear Jornal con las horas trabajadas
        crearJornalDesdeAsistencia(asistencia);

        return toResponse(asistencia);
    }

    /* Listar historial de asistencias de un profesional */
    @Override
    @Transactional(readOnly = true)
    public List<AsistenciaObraResponse> listarAsistenciasPorProfesional(Long profesionalObraId, Long empresaId) {
        List<AsistenciaObra> asistencias = asistenciaRepository.findByProfesionalObraId(profesionalObraId, empresaId);

        return asistencias.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /* Obtener asistencia de hoy para un profesional */
    @Override
    @Transactional(readOnly = true)
    public AsistenciaObraResponse obtenerAsistenciaHoy(Long profesionalObraId, Long empresaId) {
        AsistenciaObra asistencia = asistenciaRepository
                .findHoyByProfesionalObraId(profesionalObraId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("No se encontró asistencia de hoy para el profesional"));

        return toResponse(asistencia);
    }

    /**
     * Crear automáticamente un Jornal a partir de la asistencia
     * Valida que no exista un jornal duplicado para la misma asignación y fecha
     */
    private void crearJornalDesdeAsistencia(AsistenciaObra asistencia) {
        ProfesionalObra profesionalObra = asistencia.getProfesionalObra();

        // Validar que no exista ya un jornal para esta asignación y fecha
        // (esto podría pasar si se ejecuta el checkout múltiples veces por algún error)
        boolean existeJornal = jornalRepository.existsByAsignacionIdAndFecha(
                profesionalObra.getId(),
                asistencia.getFecha()
        );

        /* Si ya existe un jornal para la asignacion en la fecha, no se crea un duplicado. */
        if (existeJornal) {
            return;
        }

        Jornal jornal = new Jornal();
        jornal.setAsignacion(profesionalObra);
        jornal.setFecha(asistencia.getFecha());
        jornal.setHorasTrabajadas(asistencia.getHorasTrabajadas());
        jornal.setValorHora(profesionalObra.getValorHoraAsignado() != null
                ? profesionalObra.getValorHoraAsignado()
                : BigDecimal.ZERO);
        jornal.setObservaciones("Generado automáticamente desde asistencia ID: " + asistencia.getId());

        jornalRepository.save(jornal);
    }

    /* Convertir entidad a DTO Response */
    private AsistenciaObraResponse toResponse(AsistenciaObra asistencia) {
        AsistenciaObraResponse response = new AsistenciaObraResponse();
        response.setId(asistencia.getId());
        response.setProfesionalObraId(asistencia.getProfesionalObraId());
        response.setNombreProfesional(asistencia.getNombreProfesional());
        response.setDireccionObra(asistencia.getDireccionObra());
        response.setFecha(asistencia.getFecha());
        response.setHoraEntrada(asistencia.getHoraEntrada());
        response.setLatitudEntrada(asistencia.getLatitudEntrada());
        response.setLongitudEntrada(asistencia.getLongitudEntrada());
        response.setHoraSalida(asistencia.getHoraSalida());
        response.setLatitudSalida(asistencia.getLatitudSalida());
        response.setLongitudSalida(asistencia.getLongitudSalida());
        response.setHorasTrabajadas(asistencia.getHorasTrabajadas());
        return response;
    }

    /**
     * Obtener reporte de asistencias de una obra (por dirección)
     * Agrupa por profesional y suma total de horas - NO USADO EN EL FRONTEND
     */
    @Override
    @Transactional(readOnly = true)
    public ReporteAsistenciasObraResponseDTO obtenerReporteObra(String calle, String altura, String piso, String depto, Long empresaId) {

        List<AsistenciaObra> asistencias = asistenciaRepository.findByDireccionObra(
                calle, altura,
                piso != null ? piso : "",
                depto != null ? depto : "",
                empresaId
        );

        // Agrupar por profesional y sumar horas
        Map<Long, ReporteAsistenciasProfesionalDTO> resumenPorProfesional = new HashMap<>();

        for (AsistenciaObra asistencia : asistencias) {
            Long profId = asistencia.getProfesionalObraId();

            ReporteAsistenciasProfesionalDTO resumen = resumenPorProfesional.computeIfAbsent(profId,
                    id -> ReporteAsistenciasProfesionalDTO.builder()
                            .profesionalObraId(profId)
                            .nombreProfesional(asistencia.getNombreProfesional())
                            .totalHoras(BigDecimal.ZERO)
                            .diasTrabajados(0)
                            .build()
            );

            // Sumar horas si tiene check-out
            if (asistencia.getHorasTrabajadas() != null) {
                resumen.setTotalHoras(resumen.getTotalHoras().add(asistencia.getHorasTrabajadas()));
            }

            resumen.setDiasTrabajados(resumen.getDiasTrabajados() + 1);
        }

        // Construir dirección completa
        String direccionCompleta = String.format("%s %s%s%s",
                calle,
                altura,
                piso != null && !piso.isEmpty() ? " Piso " + piso : "",
                depto != null && !depto.isEmpty() ? " Depto " + depto : "");

        return ReporteAsistenciasObraResponseDTO.builder()
                .direccionObra(direccionCompleta)
                .totalAsistencias(asistencias.size())
                .resumenPorProfesional(List.copyOf(resumenPorProfesional.values()))
                .build();
    }
}
