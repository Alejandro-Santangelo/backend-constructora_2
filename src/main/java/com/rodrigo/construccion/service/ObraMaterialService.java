package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Material;
import com.rodrigo.construccion.model.entity.MaterialCalculadora;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.ObraMaterial;
import com.rodrigo.construccion.repository.MaterialCalculadoraRepository;
import com.rodrigo.construccion.repository.MaterialRepository;
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
 * Soporta dos modos:
 * - ELEMENTO_DETALLADO: Material del presupuesto (MaterialCalculadora)
 * - CANTIDAD_GLOBAL: Material del catálogo general (Material)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObraMaterialService implements IObraMaterialService {

    private final ObraMaterialRepository obraMaterialRepository;
    private final ObraRepository obraRepository;
    private final MaterialCalculadoraRepository materialCalculadoraRepository;
    private final MaterialRepository materialCatalogoRepository;

    @Override
    @Transactional
    public ObraMaterialResponseDTO asignar(Long empresaId, AsignarMaterialRequestDTO request) {
        log.info("📦 Asignando material a obra ID {} (Empresa: {}, EsGlobal: {})", 
                request.getObraId(), empresaId, request.getEsGlobal());

        // Validar que la obra existe y pertenece a la empresa
        Obra obra = obraRepository.findByIdAndEmpresaId(request.getObraId(), empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Obra no encontrada con ID: " + request.getObraId()));

        // Detectar modo: GLOBAL vs DETALLADO
        boolean esGlobal = request.getEsGlobal() != null && request.getEsGlobal();
        
        ObraMaterial obraMaterial = new ObraMaterial();
        obraMaterial.setObraId(request.getObraId());
        obraMaterial.setCantidadAsignada(request.getCantidadAsignada());
        obraMaterial.setSemana(request.getSemana());
        obraMaterial.setObservaciones(request.getObservaciones());
        obraMaterial.setEmpresaId(empresaId);
        obraMaterial.setEsGlobal(esGlobal);
        
        MaterialCalculadora materialCalculadora = null;
        Material materialCatalogo = null;

        if (esGlobal) {
            // MODO GLOBAL: Material creado desde modo CANTIDAD_GLOBAL (no del presupuesto)
            log.info("📝 Modo CANTIDAD_GLOBAL - Creando/obteniendo material del catálogo");
            
            // Validaciones
            if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
                throw new IllegalArgumentException("La descripción es requerida para materiales globales");
            }
            if (request.getUnidadMedida() == null || request.getUnidadMedida().trim().isEmpty()) {
                throw new IllegalArgumentException("La unidad de medida es requerida para materiales globales");
            }

            // Buscar o crear material en catálogo
            materialCatalogo = materialCatalogoRepository.findAllActivosOrdenadosPorNombre().stream()
                    .filter(m -> m.getNombre().equalsIgnoreCase(request.getDescripcion().trim()))
                    .findFirst()
                    .orElseGet(() -> {
                        log.info("🆕 Creando nuevo material en catálogo: {}", request.getDescripcion());
                        Material nuevoMaterial = new Material();
                        nuevoMaterial.setNombre(request.getDescripcion().trim());
                        nuevoMaterial.setUnidadMedida(request.getUnidadMedida());
                        // Usar precio del request si viene, sino 0.00
                        nuevoMaterial.setPrecioUnitario(
                            request.getPrecioUnitario() != null ? request.getPrecioUnitario() : BigDecimal.ZERO
                        );
                        nuevoMaterial.setActivo(true);
                        log.info("💰 Precio unitario: {}", nuevoMaterial.getPrecioUnitario());
                        return materialCatalogoRepository.save(nuevoMaterial);
                    });

            obraMaterial.setMaterialCalculadoraId(null);
            obraMaterial.setMaterialCatalogoId(materialCatalogo.getId());
            obraMaterial.setDescripcion(request.getDescripcion());
            obraMaterial.setUnidadMedida(request.getUnidadMedida());
            
            log.info("✅ Material catálogo ID: {} asignado en modo GLOBAL", materialCatalogo.getId());
            
        } else {
            // MODO DETALLADO: Material del presupuesto (lógica actual)
            log.info("📋 Modo ELEMENTO_DETALLADO - Material del presupuesto");
            
            if (request.getPresupuestoMaterialId() == null) {
                throw new IllegalArgumentException("El ID del material del presupuesto es requerido para modo ELEMENTO_DETALLADO");
            }
            
            materialCalculadora = materialCalculadoraRepository.findById(request.getPresupuestoMaterialId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Material calculadora no encontrado con ID: " + request.getPresupuestoMaterialId()));

            // Validar que el material pertenece a la empresa
            if (!materialCalculadora.getEmpresa().getId().equals(empresaId)) {
                throw new IllegalArgumentException("El material no pertenece a la empresa especificada");
            }

            obraMaterial.setMaterialCalculadoraId(request.getPresupuestoMaterialId());
            obraMaterial.setMaterialCatalogoId(null);
            obraMaterial.setDescripcion(materialCalculadora.getNombre());
            obraMaterial.setUnidadMedida(materialCalculadora.getUnidad());
            
            log.info("✅ Material presupuesto ID: {} asignado en modo DETALLADO", materialCalculadora.getId());
        }

        ObraMaterial saved = obraMaterialRepository.save(obraMaterial);
        log.info("✅ Material asignado exitosamente. Asignación ID: {}", saved.getId());

        return toResponseDTO(saved, obra, materialCalculadora, materialCatalogo);
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
                    MaterialCalculadora materialCalc = null;
                    Material materialCat = null;
                    
                    if (asignacion.getMaterialCalculadoraId() != null) {
                        materialCalc = materialCalculadoraRepository
                                .findById(asignacion.getMaterialCalculadoraId())
                                .orElse(null);
                    }
                    
                    if (asignacion.getMaterialCatalogoId() != null) {
                        materialCat = materialCatalogoRepository
                                .findById(asignacion.getMaterialCatalogoId())
                                .orElse(null);
                    }
                    
                    return toResponseDTO(asignacion, obra, materialCalc, materialCat);
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

        MaterialCalculadora materialCalc = null;
        Material materialCat = null;
        
        if (asignacion.getMaterialCalculadoraId() != null) {
            materialCalc = materialCalculadoraRepository
                    .findById(asignacion.getMaterialCalculadoraId())
                    .orElse(null);
        }
        
        if (asignacion.getMaterialCatalogoId() != null) {
            materialCat = materialCatalogoRepository
                    .findById(asignacion.getMaterialCatalogoId())
                    .orElse(null);
        }

        return toResponseDTO(asignacion, obra, materialCalc, materialCat);
    }

    @Override
    @Transactional
    public ObraMaterialResponseDTO actualizar(Long empresaId, Long id, AsignarMaterialRequestDTO request) {
        log.info("✏️ Actualizando asignación de material ID {} (Empresa: {})", id, empresaId);

        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación de material no encontrada con ID: " + id));

        // Actualizar campos comunes
        asignacion.setCantidadAsignada(request.getCantidadAsignada());
        asignacion.setSemana(request.getSemana());
        asignacion.setObservaciones(request.getObservaciones());
        
        // Actualizar campos específicos si cambió el modo
        if (request.getEsGlobal() != null) {
            asignacion.setEsGlobal(request.getEsGlobal());
            
            if (request.getEsGlobal()) {
                // Actualizar campos de modo GLOBAL
                if (request.getDescripcion() != null) {
                    asignacion.setDescripcion(request.getDescripcion());
                }
                if (request.getUnidadMedida() != null) {
                    asignacion.setUnidadMedida(request.getUnidadMedida());
                }
            }
        }

        ObraMaterial updated = obraMaterialRepository.save(asignacion);
        log.info("✅ Asignación actualizada exitosamente");

        Obra obra = obraRepository.findById(asignacion.getObraId()).orElse(null);
        
        MaterialCalculadora materialCalc = null;
        Material materialCat = null;
        
        if (asignacion.getMaterialCalculadoraId() != null) {
            materialCalc = materialCalculadoraRepository
                    .findById(asignacion.getMaterialCalculadoraId())
                    .orElse(null);
        }
        
        if (asignacion.getMaterialCatalogoId() != null) {
            materialCat = materialCatalogoRepository
                    .findById(asignacion.getMaterialCatalogoId())
                    .orElse(null);
        }

        return toResponseDTO(updated, obra, materialCalc, materialCat);
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
     * Soporta materiales del presupuesto (modo DETALLADO) y del catálogo (modo GLOBAL)
     */
    private ObraMaterialResponseDTO toResponseDTO(ObraMaterial asignacion, Obra obra, 
                                                   MaterialCalculadora materialCalc, 
                                                   Material materialCat) {
        ObraMaterialResponseDTO dto = new ObraMaterialResponseDTO();
        dto.setId(asignacion.getId());
        dto.setObraId(asignacion.getObraId());
        dto.setObservaciones(asignacion.getObservaciones());
        dto.setCantidadAsignada(asignacion.getCantidadAsignada());
        dto.setSemana(asignacion.getSemana());
        dto.setFechaAsignacion(asignacion.getFechaAsignacion());
        
        // Campos nuevos para soporte global
        dto.setMaterialCalculadoraId(asignacion.getMaterialCalculadoraId());
        dto.setMaterialCatalogoId(asignacion.getMaterialCatalogoId());
        dto.setDescripcion(asignacion.getDescripcion());
        dto.setUnidadMedida(asignacion.getUnidadMedida());
        dto.setEsGlobal(asignacion.getEsGlobal());

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

        // Datos del material calculadora (modo DETALLADO)
        if (materialCalc != null) {
            dto.setPresupuestoMaterialId(materialCalc.getId());
            dto.setNombreMaterial(materialCalc.getNombre());
            dto.setDescripcionMaterial(materialCalc.getDescripcion());
            dto.setUnidadMedida(materialCalc.getUnidad());
            dto.setPrecioUnitario(materialCalc.getPrecio());
            
            // Calcular total
            if (asignacion.getCantidadAsignada() != null && materialCalc.getPrecio() != null) {
                BigDecimal total = asignacion.getCantidadAsignada().multiply(materialCalc.getPrecio());
                dto.setTotalCalculado(total);
            }
        }
        
        // Datos del material catálogo (modo GLOBAL)
        if (materialCat != null) {
            dto.setNombreMaterial(materialCat.getNombre());
            dto.setDescripcionMaterial(materialCat.getDescripcion());
            dto.setUnidadMedida(materialCat.getUnidadMedida());
            dto.setPrecioUnitario(materialCat.getPrecioUnitario());
            
            // Calcular total
            if (asignacion.getCantidadAsignada() != null && materialCat.getPrecioUnitario() != null) {
                BigDecimal total = asignacion.getCantidadAsignada().multiply(materialCat.getPrecioUnitario());
                dto.setTotalCalculado(total);
            }
        }

        return dto;
    }
}
