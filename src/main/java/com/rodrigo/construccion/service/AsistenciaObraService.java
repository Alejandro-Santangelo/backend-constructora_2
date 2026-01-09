package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CheckInRequest;
import com.rodrigo.construccion.dto.request.CheckOutRequest;
import com.rodrigo.construccion.dto.response.AsistenciaObraResponse;
import com.rodrigo.construccion.exception.AsistenciaDuplicadaException;
import com.rodrigo.construccion.exception.CheckInNoEncontradoException;
import com.rodrigo.construccion.exception.HorarioInvalidoException;
import com.rodrigo.construccion.model.entity.AsistenciaObra;
import com.rodrigo.construccion.model.entity.Jornal;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.repository.AsistenciaObraRepository;
import com.rodrigo.construccion.repository.JornalRepository;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar asistencias de profesionales en obras
 */
@Service
@RequiredArgsConstructor
public class AsistenciaObraService {

    private final AsistenciaObraRepository asistenciaRepository;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final JornalRepository jornalRepository;

    /**
     * Registrar check-in (entrada)
     */
    @Transactional
    public AsistenciaObraResponse checkIn(CheckInRequest request) {
        // 1. Buscar profesional obra
        ProfesionalObra profesionalObra = profesionalObraRepository
            .findById(request.getProfesionalObraId())
            .orElseThrow(() -> new IllegalArgumentException("Profesional obra no encontrado"));

        // Validar empresa
        if (!profesionalObra.getEmpresaId().equals(request.getEmpresaId())) {
            throw new IllegalArgumentException("El profesional no pertenece a la empresa especificada");
        }

        // 2. Validar que no exista asistencia para ese día
        if (asistenciaRepository.existsByProfesionalObraIdAndFecha(
                request.getProfesionalObraId(), 
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

    /**
     * Registrar check-out (salida)
     */
    @Transactional
    public AsistenciaObraResponse checkOut(Long asistenciaId, CheckOutRequest request) {
        // 1. Buscar asistencia
        AsistenciaObra asistencia = asistenciaRepository
            .findByIdAndEmpresaId(asistenciaId, request.getEmpresaId())
            .orElseThrow(() -> new CheckInNoEncontradoException(
                "No se encontró el registro de check-in con ID: " + asistenciaId
            ));

        // 2. Validar que no tenga ya check-out
        if (asistencia.tieneCheckOut()) {
            throw new HorarioInvalidoException("Este registro ya tiene check-out registrado");
        }

        // 3. Validar que hora_salida > hora_entrada
        if (request.getHoraSalida().isBefore(asistencia.getHoraEntrada())) {
            throw new HorarioInvalidoException(
                "La hora de salida no puede ser anterior a la hora de entrada"
            );
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
        try {
            crearJornalDesdeAsistencia(asistencia);
        } catch (Exception e) {
            // Log pero no falla la transacción si no se puede crear el jornal
            System.err.println("No se pudo crear jornal automáticamente: " + e.getMessage());
        }

        return toResponse(asistencia);
    }

    /**
     * Crear automáticamente un Jornal a partir de la asistencia
     */
    private void crearJornalDesdeAsistencia(AsistenciaObra asistencia) {
        ProfesionalObra profesionalObra = asistencia.getProfesionalObra();
        
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

    /**
     * Listar historial de asistencias de un profesional
     */
    @Transactional(readOnly = true)
    public List<AsistenciaObraResponse> listarAsistenciasPorProfesional(Long profesionalObraId, Long empresaId) {
        List<AsistenciaObra> asistencias = asistenciaRepository
            .findByProfesionalObraId(profesionalObraId, empresaId);
        
        return asistencias.stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    /**
     * Obtener reporte de asistencias de una obra (por dirección)
     * Agrupa por profesional y suma total de horas
     */
    @Transactional(readOnly = true)
    public Map<String, Object> obtenerReporteObra(
            String calle, String altura, String piso, String depto, Long empresaId) {
        
        List<AsistenciaObra> asistencias = asistenciaRepository.findByDireccionObra(
            calle, altura,
            piso != null ? piso : "",
            depto != null ? depto : "",
            empresaId
        );

        // Agrupar por profesional y sumar horas
        Map<Long, Map<String, Object>> resumenPorProfesional = new HashMap<>();
        
        for (AsistenciaObra asistencia : asistencias) {
            Long profId = asistencia.getProfesionalObraId();
            
            resumenPorProfesional.putIfAbsent(profId, new HashMap<>());
            Map<String, Object> resumen = resumenPorProfesional.get(profId);
            
            if (!resumen.containsKey("nombreProfesional")) {
                resumen.put("profesionalObraId", profId);
                resumen.put("nombreProfesional", asistencia.getNombreProfesional());
                resumen.put("totalHoras", BigDecimal.ZERO);
                resumen.put("diasTrabajados", 0);
            }
            
            // Sumar horas si tiene check-out
            if (asistencia.getHorasTrabajadas() != null) {
                BigDecimal totalActual = (BigDecimal) resumen.get("totalHoras");
                resumen.put("totalHoras", totalActual.add(asistencia.getHorasTrabajadas()));
            }
            
            int diasActual = (int) resumen.get("diasTrabajados");
            resumen.put("diasTrabajados", diasActual + 1);
        }

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("direccionObra", String.format("%s %s%s%s", calle, altura,
            piso != null && !piso.isEmpty() ? " Piso " + piso : "",
            depto != null && !depto.isEmpty() ? " Depto " + depto : ""));
        resultado.put("totalAsistencias", asistencias.size());
        resultado.put("resumenPorProfesional", resumenPorProfesional.values());
        
        return resultado;
    }

    /**
     * Obtener asistencia de hoy para un profesional
     */
    @Transactional(readOnly = true)
    public AsistenciaObraResponse obtenerAsistenciaHoy(Long profesionalObraId, Long empresaId) {
        AsistenciaObra asistencia = asistenciaRepository
            .findHoyByProfesionalObraId(profesionalObraId, empresaId)
            .orElseThrow(() -> new IllegalArgumentException(
                "No se encontró asistencia de hoy para el profesional"
            ));
        
        return toResponse(asistencia);
    }

    /**
     * Convertir entidad a DTO Response
     */
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
}
