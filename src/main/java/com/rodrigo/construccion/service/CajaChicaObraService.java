package com.rodrigo.construccion.service;

import com.rodrigo.construccion.config.TenantContext;
import com.rodrigo.construccion.dto.request.AsignarCajaChicaMultipleRequest;
import com.rodrigo.construccion.dto.response.CajaChicaObraResponseDTO;
import com.rodrigo.construccion.model.entity.CajaChicaObra;
import com.rodrigo.construccion.repository.CajaChicaObraRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
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
        log.info("Asignando caja chica a {} profesionales de la obra {}", 
                request.getProfesionalesIds().size(), request.getPresupuestoNoClienteId());

        List<CajaChicaObra> asignaciones = new ArrayList<>();
        LocalDate fechaAsignacion = request.getFecha() != null ? request.getFecha() : LocalDate.now();

        for (Long profesionalId : request.getProfesionalesIds()) {
            CajaChicaObra cajaChica = new CajaChicaObra();
            cajaChica.setEmpresaId(request.getEmpresaId());
            cajaChica.setPresupuestoNoClienteId(request.getPresupuestoNoClienteId());
            cajaChica.setProfesionalObraId(profesionalId);
            cajaChica.setMonto(request.getMonto());
            cajaChica.setFecha(fechaAsignacion);
            cajaChica.setObservaciones(request.getObservaciones());
            cajaChica.setEstado(CajaChicaObra.ESTADO_ACTIVO);

            asignaciones.add(cajaChica);
        }

        List<CajaChicaObra> guardadas = cajaChicaObraRepository.saveAll(asignaciones);
        
        log.info("Se asignaron {} registros de caja chica exitosamente", guardadas.size());

        return guardadas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaChicaObraResponseDTO> obtenerPorObra(Long empresaId, Long presupuestoNoClienteId) {
        List<CajaChicaObra> cajas = cajaChicaObraRepository
                .findByPresupuestoNoClienteId(presupuestoNoClienteId);
        
        return cajas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CajaChicaObraResponseDTO> obtenerPorProfesional(Long empresaId, Long profesionalObraId) {
        List<CajaChicaObra> cajas = cajaChicaObraRepository
                .findByProfesionalObraId(profesionalObraId);
        
        return cajas.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CajaChicaObraResponseDTO rendir(Long empresaId, Long id) {
        CajaChicaObra cajaChica = cajaChicaObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada con ID: " + id));

        cajaChica.rendir();
        CajaChicaObra guardada = cajaChicaObraRepository.save(cajaChica);

        log.info("Caja chica {} rendida exitosamente", id);

        return mapearEntityAResponse(guardada);
    }

    @Override
    @Transactional
    public void anular(Long empresaId, Long id) {
        CajaChicaObra cajaChica = cajaChicaObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada con ID: " + id));

        cajaChica.anular();
        cajaChicaObraRepository.save(cajaChica);

        log.info("Caja chica {} anulada exitosamente", id);
    }

    @Override
    @Transactional(readOnly = true)
    public CajaChicaObraResponseDTO obtenerPorId(Long empresaId, Long id) {
        CajaChicaObra cajaChica = cajaChicaObraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Caja chica no encontrada con ID: " + id));

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
