package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.MaterialCalculadora;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.ObraMaterial;
import com.rodrigo.construccion.repository.MaterialCalculadoraRepository;
import com.rodrigo.construccion.repository.ObraMaterialRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar la asignación de materiales a obras.
 * Usa MaterialCalculadora en lugar de la entidad legacy PresupuestoNoClienteMaterial
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObraMaterialService implements IObraMaterialService {

    private final ObraMaterialRepository obraMaterialRepository;
    private final ObraRepository obraRepository;
    private final MaterialCalculadoraRepository materialRepository;

    @Override
    @Transactional
    public ObraMaterialResponseDTO asignar(Long empresaId, AsignarMaterialRequestDTO request) {
        log.info("📦 Asignando material calculadora ID {} a obra ID {} (Empresa: {})", 
                request.getPresupuestoMaterialId(), request.getObraId(), empresaId);

        // Validar que la obra existe y pertenece a la empresa
        Obra obra = obraRepository.findByIdAndEmpresaId(request.getObraId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Obra no encontrada con ID: " + request.getObraId()));

        // Validar que el material calculadora existe
        MaterialCalculadora material = materialRepository.findById(request.getPresupuestoMaterialId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Material calculadora no encontrado con ID: " + request.getPresupuestoMaterialId()));

        // Validar que el material pertenece a la empresa
        if (!material.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El material no pertenece a la empresa especificada");
        }

        // Crear la asignación
        ObraMaterial obraMaterial = new ObraMaterial();
        obraMaterial.setObraId(request.getObraId());
        obraMaterial.setMaterialCalculadoraId(request.getPresupuestoMaterialId());
        obraMaterial.setCantidadAsignada(request.getCantidadAsignada());
        obraMaterial.setSemana(request.getSemana());
        obraMaterial.setObservaciones(request.getObservaciones());
        obraMaterial.setEmpresaId(empresaId);

        ObraMaterial saved = obraMaterialRepository.save(obraMaterial);
        log.info("✅ Material asignado exitosamente. Asignación ID: {}", saved.getId());

        return toResponseDTO(saved, obra, material);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObraMaterialResponseDTO> obtenerPorObra(Long empresaId, Long obraId) {
        log.info("🔍 Obteniendo materiales asignados a obra ID {} (Empresa: {})", obraId, empresaId);

        // Validar que la obra existe
        Obra obra = obraRepository.findByIdAndEmpresaId(obraId, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra no encontrada con ID: " + obraId));

        List<ObraMaterial> asignaciones = obraMaterialRepository.findByObraIdAndEmpresaId(obraId, empresaId);
        
        log.info("📊 Encontrados {} materiales asignados", asignaciones.size());

        return asignaciones.stream()
                .map(asignacion -> {
                    MaterialCalculadora material = materialRepository
                            .findById(asignacion.getMaterialCalculadoraId())
                            .orElse(null);
                    return toResponseDTO(asignacion, obra, material);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ObraMaterialResponseDTO obtenerPorId(Long empresaId, Long id) {
        log.info("🔍 Obteniendo asignación de material ID {} (Empresa: {})", id, empresaId);

        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación de material no encontrada con ID: " + id));

        Obra obra = obraRepository.findById(asignacion.getObraId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Obra no encontrada con ID: " + asignacion.getObraId()));

        MaterialCalculadora material = materialRepository
                .findById(asignacion.getMaterialCalculadoraId())
                .orElse(null);

        return toResponseDTO(asignacion, obra, material);
    }

    @Override
    @Transactional
    public ObraMaterialResponseDTO actualizar(Long empresaId, Long id, AsignarMaterialRequestDTO request) {
        log.info("✏️ Actualizando asignación de material ID {} (Empresa: {})", id, empresaId);

        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación de material no encontrada con ID: " + id));

        // Actualizar campos
        asignacion.setCantidadAsignada(request.getCantidadAsignada());
        asignacion.setSemana(request.getSemana());
        asignacion.setObservaciones(request.getObservaciones());

        ObraMaterial updated = obraMaterialRepository.save(asignacion);
        log.info("✅ Asignación actualizada exitosamente");

        Obra obra = obraRepository.findById(asignacion.getObraId()).orElse(null);
        MaterialCalculadora material = materialRepository
                .findById(asignacion.getMaterialCalculadoraId())
                .orElse(null);

        return toResponseDTO(updated, obra, material);
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long id) {
        log.info("🗑️ Eliminando asignación de material ID {} (Empresa: {})", id, empresaId);

        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación de material no encontrada con ID: " + id));

        obraMaterialRepository.delete(asignacion);
        log.info("✅ Asignación eliminada exitosamente");
    }

    /**
     * Convierte una entidad ObraMaterial a DTO de respuesta
     */
    private ObraMaterialResponseDTO toResponseDTO(ObraMaterial asignacion, Obra obra, MaterialCalculadora material) {
        ObraMaterialResponseDTO dto = new ObraMaterialResponseDTO();
        dto.setId(asignacion.getId());
        dto.setObraId(asignacion.getObraId());
        dto.setObservaciones(asignacion.getObservaciones());
        dto.setCantidadAsignada(asignacion.getCantidadAsignada());
        dto.setSemana(asignacion.getSemana());
        dto.setFechaAsignacion(asignacion.getFechaAsignacion());

        // Datos de la obra
        if (obra != null) {
            String nombreObra = obra.getNombre();
            if (nombreObra == null || nombreObra.isEmpty()) {
                // Construir dirección desde campos separados
                StringBuilder direccion = new StringBuilder();
                if (obra.getDireccionObraCalle() != null) {
                    direccion.append(obra.getDireccionObraCalle());
                }
                if (obra.getDireccionObraAltura() != null) {
                    if (direccion.length() > 0) direccion.append(" ");
                    direccion.append(obra.getDireccionObraAltura());
                }
                nombreObra = direccion.length() > 0 ? direccion.toString() : "Obra #" + obra.getId();
            }
            dto.setNombreObra(nombreObra);
        }

        // Datos del material calculadora
        if (material != null) {
            dto.setPresupuestoMaterialId(material.getId());
            dto.setNombreMaterial(material.getNombre());
            dto.setDescripcionMaterial(material.getDescripcion());
            dto.setUnidadMedida(material.getUnidad());
            dto.setPrecioUnitario(material.getPrecio());
            
            // Calcular total
            if (asignacion.getCantidadAsignada() != null && material.getPrecio() != null) {
                BigDecimal total = asignacion.getCantidadAsignada().multiply(material.getPrecio());
                dto.setTotalCalculado(total);
            }
        }

        return dto;
    }
}
