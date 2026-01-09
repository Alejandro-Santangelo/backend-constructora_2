package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.*;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Servicio helper para gestión de items calculadora de trabajos extra.
 * Maneja el CRUD y cálculos de rubros, jornales, materiales, profesionales y gastos generales.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class TrabajoExtraItemCalculadoraService {

    private final TrabajoExtraItemCalculadoraRepository itemCalculadoraRepository;
    private final TrabajoExtraJornalCalculadoraRepository jornalRepository;
    private final TrabajoExtraMaterialCalculadoraRepository materialRepository;
    private final TrabajoExtraProfesionalCalculadoraRepository profesionalRepository;
    private final TrabajoExtraGastoGeneralRepository gastoGeneralRepository;

    /**
     * Guarda todos los items calculadora de un trabajo extra.
     */
    @Transactional
    public void guardarItemsCalculadora(TrabajoExtra trabajoExtra, List<TrabajoExtraItemCalculadoraDTO> itemsDTO, Long empresaId) {
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            log.debug("No hay items calculadora para guardar");
            return;
        }

        // Eliminar items existentes si se está actualizando
        if (trabajoExtra.getId() != null) {
            itemCalculadoraRepository.deleteByTrabajoExtraId(trabajoExtra.getId());
        }

        List<TrabajoExtraItemCalculadora> items = new ArrayList<>();
        
        for (TrabajoExtraItemCalculadoraDTO dto : itemsDTO) {
            TrabajoExtraItemCalculadora item = convertirDTOAEntidad(dto, trabajoExtra, empresaId);
            items.add(item);
        }

        itemCalculadoraRepository.saveAll(items);
        log.info("Guardados {} items calculadora para trabajo extra {}", items.size(), trabajoExtra.getId());
    }

    /**
     * Convierte un DTO a entidad y calcula totales.
     */
    private TrabajoExtraItemCalculadora convertirDTOAEntidad(TrabajoExtraItemCalculadoraDTO dto, TrabajoExtra trabajoExtra, Long empresaId) {
        TrabajoExtraItemCalculadora item = new TrabajoExtraItemCalculadora();
        
        item.setTrabajoExtra(trabajoExtra);
        item.setEmpresaId(empresaId);
        item.setTipoProfesional(dto.getTipoProfesional());
        item.setDescripcion(dto.getDescripcion());
        item.setObservaciones(dto.getObservaciones());
        item.setEsModoManual(dto.getEsModoManual() != null ? dto.getEsModoManual() : false);
        item.setIncluirEnCalculoDias(dto.getIncluirEnCalculoDias());
        item.setTrabajaEnParalelo(dto.getTrabajaEnParalelo() != null ? dto.getTrabajaEnParalelo() : true);
        item.setEsGastoGeneral(dto.getEsGastoGeneral() != null ? dto.getEsGastoGeneral() : false);
        
        // Descripciones por categoría
        item.setDescripcionProfesionales(dto.getDescripcionProfesionales());
        item.setObservacionesProfesionales(dto.getObservacionesProfesionales());
        item.setDescripcionMateriales(dto.getDescripcionMateriales());
        item.setObservacionesMateriales(dto.getObservacionesMateriales());
        item.setDescripcionGastosGenerales(dto.getDescripcionGastosGenerales());
        item.setObservacionesGastosGenerales(dto.getObservacionesGastosGenerales());
        
        if (item.getEsModoManual()) {
            // Modo manual: usar el total ingresado directamente
            item.setTotalManual(dto.getTotalManual());
            item.setDescripcionTotalManual(dto.getDescripcionTotalManual());
            item.setObservacionesTotalManual(dto.getObservacionesTotalManual());
            item.setTotal(dto.getTotalManual());
        } else {
            // Modo automático: calcular desde jornales y materiales
            BigDecimal subtotalManoObra = BigDecimal.ZERO;
            BigDecimal subtotalMateriales = BigDecimal.ZERO;
            BigDecimal subtotalGastosGenerales = BigDecimal.ZERO;
            
            // Calcular jornales
            if (dto.getJornales() != null && !dto.getJornales().isEmpty()) {
                for (TrabajoExtraJornalCalculadoraDTO jornalDTO : dto.getJornales()) {
                    TrabajoExtraJornalCalculadora jornal = convertirJornalDTO(jornalDTO, item, empresaId);
                    item.getJornales().add(jornal);
                    subtotalManoObra = subtotalManoObra.add(jornal.getSubtotal());
                }
            } else if (dto.getCantidadJornales() != null && dto.getImporteJornal() != null) {
                // Modo simplificado: cantidad × importe
                subtotalManoObra = dto.getCantidadJornales().multiply(dto.getImporteJornal());
                item.setCantidadJornales(dto.getCantidadJornales());
                item.setImporteJornal(dto.getImporteJornal());
            }
            
            // Calcular materiales
            if (dto.getMaterialesLista() != null && !dto.getMaterialesLista().isEmpty()) {
                for (TrabajoExtraMaterialCalculadoraDTO materialDTO : dto.getMaterialesLista()) {
                    TrabajoExtraMaterialCalculadora material = convertirMaterialDTO(materialDTO, item, empresaId);
                    item.getMaterialesLista().add(material);
                    subtotalMateriales = subtotalMateriales.add(material.getSubtotal());
                }
            } else if (dto.getMateriales() != null) {
                // Modo simplificado: total directo
                subtotalMateriales = dto.getMateriales();
                item.setMateriales(dto.getMateriales());
            }
            
            // Calcular profesionales
            if (dto.getProfesionales() != null && !dto.getProfesionales().isEmpty()) {
                for (TrabajoExtraProfesionalCalculadoraDTO profDTO : dto.getProfesionales()) {
                    TrabajoExtraProfesionalCalculadora profesional = convertirProfesionalDTO(profDTO, item, empresaId);
                    item.getProfesionales().add(profesional);
                }
            }
            
            // Calcular gastos generales
            if (dto.getGastosGenerales() != null && !dto.getGastosGenerales().isEmpty()) {
                for (TrabajoExtraGastoGeneralDTO gastoDTO : dto.getGastosGenerales()) {
                    TrabajoExtraGastoGeneral gasto = convertirGastoGeneralDTO(gastoDTO, item, empresaId);
                    item.getGastosGenerales().add(gasto);
                    subtotalGastosGenerales = subtotalGastosGenerales.add(gasto.getSubtotal());
                }
            }
            
            item.setSubtotalManoObra(subtotalManoObra);
            item.setSubtotalMateriales(subtotalMateriales);
            item.setSubtotalGastosGenerales(subtotalGastosGenerales);
            
            // Total = mano de obra + materiales + gastos generales
            BigDecimal total = subtotalManoObra
                .add(subtotalMateriales)
                .add(subtotalGastosGenerales);
            item.setTotal(total);
        }
        
        return item;
    }

    private TrabajoExtraJornalCalculadora convertirJornalDTO(TrabajoExtraJornalCalculadoraDTO dto, TrabajoExtraItemCalculadora item, Long empresaId) {
        TrabajoExtraJornalCalculadora jornal = new TrabajoExtraJornalCalculadora();
        jornal.setItemCalculadora(item);
        jornal.setEmpresaId(empresaId);
        jornal.setProfesionalObraId(dto.getProfesionalObraId());
        jornal.setRol(dto.getRol());
        jornal.setCantidad(dto.getCantidad());
        jornal.setValorUnitario(dto.getValorUnitario());
        jornal.setSubtotal(dto.getSubtotal() != null ? dto.getSubtotal() : 
            dto.getCantidad().multiply(dto.getValorUnitario()));
        jornal.setIncluirEnCalculoDias(dto.getIncluirEnCalculoDias());
        jornal.setFrontendId(dto.getFrontendId());
        jornal.setObservaciones(dto.getObservaciones());
        return jornal;
    }

    private TrabajoExtraMaterialCalculadora convertirMaterialDTO(TrabajoExtraMaterialCalculadoraDTO dto, TrabajoExtraItemCalculadora item, Long empresaId) {
        TrabajoExtraMaterialCalculadora material = new TrabajoExtraMaterialCalculadora();
        material.setItemCalculadora(item);
        material.setEmpresaId(empresaId);
        material.setObraMaterialId(dto.getObraMaterialId());
        material.setNombre(dto.getNombre());
        material.setDescripcion(dto.getDescripcion());
        material.setUnidad(dto.getUnidad());
        material.setCantidad(dto.getCantidad());
        material.setPrecio(dto.getPrecio());
        material.setSubtotal(dto.getSubtotal() != null ? dto.getSubtotal() : 
            (dto.getCantidad() != null && dto.getPrecio() != null ? dto.getCantidad().multiply(dto.getPrecio()) : BigDecimal.ZERO));
        material.setFrontendId(dto.getFrontendId());
        material.setObservaciones(dto.getObservaciones());
        return material;
    }

    private TrabajoExtraProfesionalCalculadora convertirProfesionalDTO(TrabajoExtraProfesionalCalculadoraDTO dto, TrabajoExtraItemCalculadora item, Long empresaId) {
        TrabajoExtraProfesionalCalculadora profesional = new TrabajoExtraProfesionalCalculadora();
        profesional.setItemCalculadora(item);
        profesional.setEmpresaId(empresaId);
        profesional.setProfesionalObraId(dto.getProfesionalObraId());
        profesional.setRol(dto.getRol());
        profesional.setNombreCompleto(dto.getNombreCompleto());
        profesional.setCantidadJornales(dto.getCantidadJornales());
        profesional.setValorJornal(dto.getValorJornal());
        profesional.setSubtotal(dto.getSubtotal() != null ? dto.getSubtotal() : 
            (dto.getCantidadJornales() != null && dto.getValorJornal() != null ? 
                dto.getCantidadJornales().multiply(dto.getValorJornal()) : BigDecimal.ZERO));
        profesional.setIncluirEnCalculoDias(dto.getIncluirEnCalculoDias());
        profesional.setFrontendId(dto.getFrontendId());
        profesional.setObservaciones(dto.getObservaciones());
        return profesional;
    }

    private TrabajoExtraGastoGeneral convertirGastoGeneralDTO(TrabajoExtraGastoGeneralDTO dto, TrabajoExtraItemCalculadora item, Long empresaId) {
        TrabajoExtraGastoGeneral gasto = new TrabajoExtraGastoGeneral();
        gasto.setItemCalculadora(item);
        gasto.setEmpresaId(empresaId);
        gasto.setDescripcion(dto.getDescripcion());
        gasto.setCantidad(dto.getCantidad());
        gasto.setPrecioUnitario(dto.getPrecioUnitario());
        gasto.setSubtotal(dto.getSubtotal() != null ? dto.getSubtotal() : 
            dto.getCantidad().multiply(dto.getPrecioUnitario()));
        gasto.setSinCantidad(dto.getSinCantidad() != null ? dto.getSinCantidad() : false);
        gasto.setSinPrecio(dto.getSinPrecio() != null ? dto.getSinPrecio() : false);
        gasto.setOrden(dto.getOrden());
        gasto.setFrontendId(dto.getFrontendId());
        gasto.setObservaciones(dto.getObservaciones());
        return gasto;
    }

    /**
     * Calcula el total de todos los items de un trabajo extra.
     */
    public BigDecimal calcularTotalItems(Long trabajoExtraId) {
        List<TrabajoExtraItemCalculadora> items = itemCalculadoraRepository.findByTrabajoExtraId(trabajoExtraId);
        return items.stream()
            .map(TrabajoExtraItemCalculadora::getTotal)
            .filter(total -> total != null)
            .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
