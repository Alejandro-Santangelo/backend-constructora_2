package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarCajaChicaMultipleRequest;
import com.rodrigo.construccion.dto.response.CajaChicaObraResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.CajaChicaObra;
import com.rodrigo.construccion.repository.CajaChicaObraRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CajaChicaObraService implements ICajaChicaObraService {

    private final CajaChicaObraRepository cajaChicaObraRepository;

    @Override
    @Transactional
    public List<CajaChicaObraResponseDTO> asignarCajaChicaMultiple(AsignarCajaChicaMultipleRequest request) {
        LocalDate fechaAsignacion = request.getFecha() != null ? request.getFecha() : LocalDate.now();

        // Crear entidades de caja chica para cada profesional
        List<CajaChicaObra> asignaciones = request.getProfesionalesIds().stream()
                .map(profesionalId -> crearCajaChicaParaProfesional(request.getEmpresaId(),
                        request.getPresupuestoNoClienteId(),
                        profesionalId,
                        request.getMonto(),
                        fechaAsignacion,
                        request.getObservaciones()
                ))
                .collect(Collectors.toList());

        // Guardar todas las asignaciones en batch
        List<CajaChicaObra> guardadas = cajaChicaObraRepository.saveAll(asignaciones);

        return guardadas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    /* Crea una entidad CajaChicaObra para un profesional específico */
    private CajaChicaObra crearCajaChicaParaProfesional(Long empresaId, Long presupuestoNoClienteId, Long profesionalId,
                                                        BigDecimal monto,
                                                        LocalDate fecha,
                                                        String observaciones) {

        CajaChicaObra cajaChica = new CajaChicaObra();
        cajaChica.setEmpresaId(empresaId);
        cajaChica.setPresupuestoNoClienteId(presupuestoNoClienteId);
        cajaChica.setProfesionalObraId(profesionalId);
        cajaChica.setMonto(monto);
        cajaChica.setFecha(fecha);
        cajaChica.setObservaciones(observaciones);
        cajaChica.setEstado(CajaChicaObra.ESTADO_ACTIVO);

        return cajaChica;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaChicaObraResponseDTO> obtenerPorObra(Long empresaId, Long presupuestoNoClienteId) {
        List<CajaChicaObra> cajas = cajaChicaObraRepository.findByPresupuestoNoClienteId(presupuestoNoClienteId);

        return cajas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaChicaObraResponseDTO> obtenerPorProfesional(Long empresaId, Long profesionalObraId) {
        List<CajaChicaObra> cajas = cajaChicaObraRepository.findByProfesionalObraId(profesionalObraId);

        return cajas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CajaChicaObraResponseDTO rendir(Long empresaId, Long id) {
        CajaChicaObra cajaChica = cajaChicaObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja chica no encontrada con ID: " + id));

        cajaChica.rendir();
        CajaChicaObra guardada = cajaChicaObraRepository.save(cajaChica);

        return mapearEntityAResponse(guardada);
    }

    @Override
    @Transactional
    public void anular(Long empresaId, Long id) {
        CajaChicaObra cajaChica = cajaChicaObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja chica no encontrada con ID: " + id));

        cajaChica.anular();
        cajaChicaObraRepository.save(cajaChica);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaChicaObraResponseDTO obtenerPorId(Long empresaId, Long id) {
        CajaChicaObra cajaChica = cajaChicaObraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Caja chica no encontrada con ID: " + id));

        return mapearEntityAResponse(cajaChica);
    }

    // ========== MAPPERS ==========

    private CajaChicaObraResponseDTO mapearEntityAResponse(CajaChicaObra entity) {
        return CajaChicaObraResponseDTO.builder()
                .id(entity.getId())
                .empresaId(entity.getEmpresaId())
                .presupuestoNoClienteId(entity.getPresupuestoNoClienteId())
                .profesionalObraId(entity.getProfesionalObraId())
                .monto(entity.getMonto())
                .fecha(entity.getFecha())
                .observaciones(entity.getObservaciones())
                .estado(entity.getEstado())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
