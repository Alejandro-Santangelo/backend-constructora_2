package com.rodrigo.construccion.service.impl;

import com.rodrigo.construccion.dto.request.AsignarOtroCostoRequestDTO;
import com.rodrigo.construccion.dto.response.ObraOtroCostoResponseDTO;
import com.rodrigo.construccion.entity.ObraOtroCosto;
import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import com.rodrigo.construccion.repository.IObraOtroCostoRepository;
import com.rodrigo.construccion.repository.PresupuestoGastoGeneralRepository;
import com.rodrigo.construccion.service.IObraOtroCostoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio para gestionar otros costos de obras
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class ObraOtroCostoServiceImpl implements IObraOtroCostoService {

    private final IObraOtroCostoRepository obraOtroCostoRepository;
    private final PresupuestoGastoGeneralRepository presupuestoGastoGeneralRepository;

    @Override
    public ObraOtroCostoResponseDTO asignar(Long empresaId, AsignarOtroCostoRequestDTO request) {
        log.info("🔄 Asignando otro costo - Empresa: {}, Obra: {}, Gasto: {}, Importe: {}, SEMANA: {}", 
                empresaId, request.getObraId(), request.getGastoGeneralId(), request.getImporteAsignado(), request.getSemana());

        // Crear nueva asignación
        ObraOtroCosto obraOtroCosto = new ObraOtroCosto();
        obraOtroCosto.setObraId(request.getObraId());
        obraOtroCosto.setEmpresaId(empresaId);
        obraOtroCosto.setGastoGeneralId(request.getGastoGeneralId());
        obraOtroCosto.setImporteAsignado(request.getImporteAsignado());
        obraOtroCosto.setSemana(request.getSemana());
        obraOtroCosto.setObservaciones(request.getObservaciones());
        
        log.info("📝 Entidad antes de guardar - Semana: {}, ObraId: {}", obraOtroCosto.getSemana(), obraOtroCosto.getObraId());

        // Guardar en BD
        ObraOtroCosto saved = obraOtroCostoRepository.save(obraOtroCosto);

        log.info("✅ Asignación guardada con ID: {}", saved.getId());

        // Convertir a DTO de respuesta
        return convertirAResponseDTO(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObraOtroCostoResponseDTO> obtenerPorObra(Long empresaId, Long obraId) {
        log.info("🔍 Obteniendo otros costos - Empresa: {}, Obra: {}", empresaId, obraId);

        List<ObraOtroCosto> asignaciones = obraOtroCostoRepository
                .findByObraIdAndEmpresaIdOrderByFechaAsignacionDesc(obraId, empresaId);

        log.info("📋 Encontradas {} asignaciones", asignaciones.size());

        return asignaciones.stream()
                .map(this::convertirAResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ObraOtroCostoResponseDTO obtenerPorId(Long empresaId, Long id) {
        log.info("🔍 Obteniendo asignación por ID - Empresa: {}, ID: {}", empresaId, id);

        ObraOtroCosto asignacion = obraOtroCostoRepository.findByIdAndEmpresaId(id, empresaId);
        
        if (asignacion == null) {
            log.warn("❌ Asignación no encontrada - ID: {}, Empresa: {}", id, empresaId);
            throw new RuntimeException("Asignación no encontrada");
        }

        return convertirAResponseDTO(asignacion);
    }

    @Override
    public ObraOtroCostoResponseDTO actualizar(Long empresaId, Long id, AsignarOtroCostoRequestDTO request) {
        log.info("🔄 Actualizando asignación - Empresa: {}, ID: {}", empresaId, id);

        ObraOtroCosto asignacion = obraOtroCostoRepository.findByIdAndEmpresaId(id, empresaId);
        
        if (asignacion == null) {
            log.warn("❌ Asignación no encontrada para actualizar - ID: {}, Empresa: {}", id, empresaId);
            throw new RuntimeException("Asignación no encontrada");
        }

        // Actualizar campos
        asignacion.setImporteAsignado(request.getImporteAsignado());
        asignacion.setSemana(request.getSemana());
        asignacion.setObservaciones(request.getObservaciones());

        ObraOtroCosto updated = obraOtroCostoRepository.save(asignacion);

        log.info("✅ Asignación actualizada correctamente");

        return convertirAResponseDTO(updated);
    }

    @Override
    public void eliminar(Long empresaId, Long id) {
        log.info("🗑️ Eliminando asignación - Empresa: {}, ID: {}", empresaId, id);

        ObraOtroCosto asignacion = obraOtroCostoRepository.findByIdAndEmpresaId(id, empresaId);
        
        if (asignacion == null) {
            log.warn("❌ Asignación no encontrada para eliminar - ID: {}, Empresa: {}", id, empresaId);
            throw new RuntimeException("Asignación no encontrada");
        }

        obraOtroCostoRepository.delete(asignacion);

        log.info("✅ Asignación eliminada correctamente");
    }

    /**
     * Convierte entidad a DTO de respuesta
     */
    private ObraOtroCostoResponseDTO convertirAResponseDTO(ObraOtroCosto entity) {
        // Buscar información del gasto general
        String descripcion = "Gasto General ID: " + entity.getGastoGeneralId();
        String categoria = "Otros Costos";
        
        if (entity.getGastoGeneralId() != null) {
            PresupuestoGastoGeneral gasto = presupuestoGastoGeneralRepository
                .findById(entity.getGastoGeneralId())
                .orElse(null);
            
            if (gasto != null) {
                descripcion = gasto.getDescripcion();
                if (gasto.getObservaciones() != null && !gasto.getObservaciones().isEmpty()) {
                    descripcion += " - " + gasto.getObservaciones();
                }
            }
        }
        
        return ObraOtroCostoResponseDTO.builder()
                .id(entity.getId())
                .obraId(entity.getObraId())
                .nombreObra("Obra ID: " + entity.getObraId())
                .gastoGeneralId(entity.getGastoGeneralId())
                .categoria(categoria)
                .descripcion(descripcion)
                .importeAsignado(entity.getImporteAsignado())
                .fechaAsignacion(entity.getFechaAsignacion())
                .semana(entity.getSemana())
                .observaciones(entity.getObservaciones())
                .build();
    }
}