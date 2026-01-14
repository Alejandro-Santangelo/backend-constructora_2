package com.rodrigo.construccion.service.impl;

import com.rodrigo.construccion.dto.request.AsignarOtroCostoRequestDTO;
import com.rodrigo.construccion.dto.response.ObraOtroCostoResponseDTO;
import com.rodrigo.construccion.model.entity.ObraOtroCosto;
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
        log.info("🔄 Asignando otro costo - Empresa: {}, Obra: {}, Gasto: {}, Importe: {}, SEMANA: {}, Fecha: {}", 
                empresaId, request.getObraId(), request.getGastoGeneralId(), request.getImporteAsignado(), 
                request.getSemana(), request.getFechaAsignacion());

        // Detectar si es asignación semanal o diaria
        boolean esSemanal = request.getFechaAsignacion() == null;
        boolean esManual = request.getPresupuestoOtroCostoId() == null;
        
        log.info("📊 Tipo de asignación - Semanal: {}, Manual: {}", esSemanal, esManual);

        // Validaciones
        if (esSemanal && request.getFechaAsignacion() != null) {
            throw new IllegalArgumentException("Una asignación semanal no debe tener fecha específica");
        }
        
        if (!esSemanal && request.getFechaAsignacion() == null) {
            throw new IllegalArgumentException("Una asignación diaria debe tener fecha específica");
        }
        
        if (esManual && (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty())) {
            throw new IllegalArgumentException("Una asignación manual debe tener descripción");
        }

        if (esManual && (request.getCategoria() == null || request.getCategoria().trim().isEmpty())) {
            throw new IllegalArgumentException("Una asignación manual debe tener categoría");
        }

        // Crear nueva asignación
        ObraOtroCosto obraOtroCosto = new ObraOtroCosto();
        obraOtroCosto.setObraId(request.getObraId());
        obraOtroCosto.setEmpresaId(empresaId);
        obraOtroCosto.setPresupuestoOtroCostoId(request.getPresupuestoOtroCostoId());
        obraOtroCosto.setGastoGeneralId(request.getGastoGeneralId());
        obraOtroCosto.setImporteAsignado(request.getImporteAsignado());
        obraOtroCosto.setSemana(request.getSemana());
        obraOtroCosto.setFechaAsignacion(request.getFechaAsignacion());
        obraOtroCosto.setDescripcion(request.getDescripcion());
        obraOtroCosto.setCategoria(request.getCategoria());
        obraOtroCosto.setObservaciones(request.getObservaciones());
        obraOtroCosto.setEsSemanal(esSemanal);
        obraOtroCosto.setEsManual(esManual);
        obraOtroCosto.setEsGlobal(request.getEsGlobal() != null ? request.getEsGlobal() : false);
        
        log.info("📝 Entidad antes de guardar - Semana: {}, ObraId: {}, EsSemanal: {}, EsManual: {}, EsGlobal: {}", 
                obraOtroCosto.getSemana(), obraOtroCosto.getObraId(), obraOtroCosto.getEsSemanal(), 
                obraOtroCosto.getEsManual(), obraOtroCosto.getEsGlobal());

        // Guardar en BD
        ObraOtroCosto saved = obraOtroCostoRepository.save(obraOtroCosto);

        log.info("✅ Asignación guardada con ID: {} (Semanal: {}, Manual: {})", saved.getId(), saved.getEsSemanal(), saved.getEsManual());

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

        // Detectar si es asignación semanal o diaria
        boolean esSemanal = request.getFechaAsignacion() == null;
        boolean esManual = request.getPresupuestoOtroCostoId() == null;

        // Validaciones
        if (esSemanal && request.getFechaAsignacion() != null) {
            throw new IllegalArgumentException("Una asignación semanal no debe tener fecha específica");
        }
        
        if (!esSemanal && request.getFechaAsignacion() == null) {
            throw new IllegalArgumentException("Una asignación diaria debe tener fecha específica");
        }

        // Actualizar campos
        asignacion.setImporteAsignado(request.getImporteAsignado());
        asignacion.setSemana(request.getSemana());
        asignacion.setFechaAsignacion(request.getFechaAsignacion());
        asignacion.setDescripcion(request.getDescripcion());
        asignacion.setCategoria(request.getCategoria());
        asignacion.setObservaciones(request.getObservaciones());
        asignacion.setEsSemanal(esSemanal);
        asignacion.setEsManual(esManual);
        asignacion.setEsGlobal(request.getEsGlobal() != null ? request.getEsGlobal() : false);
        asignacion.setPresupuestoOtroCostoId(request.getPresupuestoOtroCostoId());
        asignacion.setGastoGeneralId(request.getGastoGeneralId());

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
        // Buscar información del gasto general si existe
        String descripcion = entity.getDescripcion();
        String categoria = entity.getCategoria();
        
        // Si no tiene descripción propia, buscar del gasto general
        if ((descripcion == null || descripcion.isEmpty()) && entity.getGastoGeneralId() != null) {
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
        
        // Si aún no tiene categoría, usar default
        if (categoria == null || categoria.isEmpty()) {
            categoria = "Otros Costos";
        }
        
        // Si no tiene descripción, usar fallback
        if (descripcion == null || descripcion.isEmpty()) {
            descripcion = "Gasto General ID: " + entity.getGastoGeneralId();
        }
        
        return ObraOtroCostoResponseDTO.builder()
                .id(entity.getId())
                .obraId(entity.getObraId())
                .nombreObra("Obra ID: " + entity.getObraId())
                .presupuestoOtroCostoId(entity.getPresupuestoOtroCostoId())
                .gastoGeneralId(entity.getGastoGeneralId())
                .categoria(categoria)
                .descripcion(descripcion)
                .nombreOtroCosto(descripcion) // Alias para compatibilidad
                .importeAsignado(entity.getImporteAsignado())
                .fechaAsignacion(entity.getFechaAsignacion()) // Solo presente si no es semanal
                .semana(entity.getSemana())
                .observaciones(entity.getObservaciones())
                .esSemanal(entity.getEsSemanal())
                .esManual(entity.getEsManual())
                .esGlobal(entity.getEsGlobal())
                .build();
    }
}