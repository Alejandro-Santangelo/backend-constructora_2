package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignarMaterialRequestDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialResponseDTO;
import com.rodrigo.construccion.exception.BusinessException;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Material;
import com.rodrigo.construccion.model.entity.MaterialCalculadora;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.ObraMaterial;
import com.rodrigo.construccion.repository.ObraMaterialRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.Session;
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
    private final IObraService obraService;
    private final IMaterialCalculadoraService materialCalculadoraService;
    private final IMaterialService materialService;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public ObraMaterialResponseDTO asignar(Long empresaId, AsignarMaterialRequestDTO request) {
        // Validar que la obra existe y pertenece a la empresa
        Obra obra = obraService.encontrarObraPorIdYEmpresa(request.getObraId(), empresaId);

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
            // Validaciones básicas (el servicio se encarga de normalizar)
            if (request.getDescripcion() == null || request.getDescripcion().trim().isEmpty()) {
                throw new BusinessException("La descripción es requerida para materiales globales");
            }
            if (request.getUnidadMedida() == null || request.getUnidadMedida().trim().isEmpty()) {
                throw new BusinessException("La unidad de medida es requerida para materiales globales");
            }

            // Buscar o crear material en catálogo usando el servicio
            // El servicio MaterialService se encarga de normalizar el nombre (trim)
            materialCatalogo = materialService.buscarOCrearPorNombre(request.getDescripcion(), request.getUnidadMedida(), request.getPrecioUnitario()
            );

            obraMaterial.setMaterialCalculadoraId(null);
            obraMaterial.setMaterialCatalogoId(materialCatalogo.getId());
            obraMaterial.setDescripcion(request.getDescripcion());
            obraMaterial.setUnidadMedida(request.getUnidadMedida());

        } else {
            if (request.getPresupuestoMaterialId() == null) {
                throw new BusinessException("El ID del material del presupuesto es requerido para modo ELEMENTO_DETALLADO");
            }

            materialCalculadora = materialCalculadoraService.buscarPorId(request.getPresupuestoMaterialId());

            // Validar que el material pertenece a la empresa
            if (!materialCalculadora.getEmpresa().getId().equals(empresaId)) {
                throw new BusinessException("El material no pertenece a la empresa especificada");
            }

            obraMaterial.setMaterialCalculadoraId(request.getPresupuestoMaterialId());
            obraMaterial.setMaterialCatalogoId(null);
            obraMaterial.setDescripcion(materialCalculadora.getNombre());
            obraMaterial.setUnidadMedida(materialCalculadora.getUnidad());
        }

        ObraMaterial saved = obraMaterialRepository.save(obraMaterial);

        return toResponseDTO(saved, obra, materialCalculadora, materialCatalogo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ObraMaterialResponseDTO> obtenerPorObra(Long empresaId, Long obraId) {
        // Validar que la obra existe
        Obra obra = obraService.encontrarObraPorIdYEmpresa(obraId, empresaId);

        List<ObraMaterial> asignaciones = obraMaterialRepository.findByObraIdAndEmpresaId(obraId, empresaId);

        return asignaciones.stream()
                .map(asignacion -> {
                    MaterialCalculadora materialCalc = asignacion.getMaterialCalculadoraId() != null
                            ? materialCalculadoraService.buscarPorIdOpcional(asignacion.getMaterialCalculadoraId())
                            : null;

                    Material materialCat = asignacion.getMaterialCatalogoId() != null
                            ? materialService.buscarPorIdOpcional(asignacion.getMaterialCatalogoId())
                            : null;

                    return toResponseDTO(asignacion, obra, materialCalc, materialCat);
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ObraMaterialResponseDTO obtenerPorId(Long empresaId, Long id) {
        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación de material no encontrada con ID: " + id));

        Obra obra = obraService.findById(asignacion.getObraId());

        MaterialCalculadora materialCalc = materialCalculadoraService.buscarPorIdOpcional(asignacion.getMaterialCalculadoraId());

        Material materialCat = materialService.buscarPorIdOpcional(asignacion.getMaterialCatalogoId());

        return toResponseDTO(asignacion, obra, materialCalc, materialCat);
    }

    @Override
    @Transactional
    public ObraMaterialResponseDTO actualizar(Long empresaId, Long id, AsignarMaterialRequestDTO request) {
        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación de material no encontrada con ID: " + id));

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

        Obra obra = obraService.buscarPorIdOpcional(asignacion.getObraId());

        MaterialCalculadora materialCalc = materialCalculadoraService.buscarPorIdOpcional(asignacion.getMaterialCalculadoraId());

        Material materialCat = materialService.buscarPorIdOpcional(asignacion.getMaterialCatalogoId());

        return toResponseDTO(updated, obra, materialCalc, materialCat);
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long obraId, Long id) {
        // Buscar la asignación validando empresa
        ObraMaterial asignacion = obraMaterialRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Asignación de material no encontrada con ID: " + id));

        // Validación adicional de seguridad: verificar que la asignación pertenece a la obra especificada
        if (!asignacion.getObraId().equals(obraId)) {
            throw new BusinessException(
                    "La asignación de material no pertenece a la obra especificada. " +
                    "Obra esperada: " + obraId + ", Obra real: " + asignacion.getObraId());
        }

        obraMaterialRepository.delete(asignacion);
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
