package com.rodrigo.construccion.service.helper;

import com.rodrigo.construccion.dto.request.*;
import com.rodrigo.construccion.dto.response.*;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Helper para gestionar items calculadora de trabajos extra.
 * Maneja la creación, actualización y cálculos de rubros, jornales, materiales, etc.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TrabajoExtraItemCalculadoraHelper {

    private final TrabajoExtraItemCalculadoraRepository itemRepository;
    private final TrabajoExtraJornalCalculadoraRepository jornalRepository;
    private final TrabajoExtraMaterialCalculadoraRepository materialRepository;
    private final TrabajoExtraProfesionalCalculadoraRepository profesionalRepository;
    private final TrabajoExtraGastoGeneralRepository gastoRepository;
    private final EntityManager entityManager;

    /**
     * Guardar items calculadora para un trabajo extra.
     */
    @Transactional
    public void guardarItems(Long trabajoExtraId, Long empresaId, List<TrabajoExtraItemCalculadoraDTO> itemsDTO) {
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            log.warn("⚠️ No hay items calculadora para guardar - itemsDTO es null o vacío");
            return;
        }

        log.info("✅ Guardando {} items para trabajo extra {} (empresaId: {})", itemsDTO.size(), trabajoExtraId, empresaId);
        itemsDTO.forEach(item -> log.info("  📦 Item: {} - esRubroVacio: {}, jornales: {}, materiales: {}, profesionales: {}, gastosGenerales: {}",
            item.getTipoProfesional(),
            item.getEsRubroVacio(),
            item.getJornales() != null ? item.getJornales().size() : 0,
            item.getMaterialesLista() != null ? item.getMaterialesLista().size() : 0,
            item.getProfesionales() != null ? item.getProfesionales().size() : 0,
            item.getGastosGenerales() != null ? item.getGastosGenerales().size() : 0));

        // ============================================================================
        // ELIMINACIÓN CORRECTA: Primero eliminar entidades hijas, luego items
        // ============================================================================
        log.info("🗑️ Eliminando items existentes y sus relaciones del trabajo extra {}", trabajoExtraId);
        
        // Obtener todos los items existentes
        List<TrabajoExtraItemCalculadora> itemsExistentes = itemRepository.findByTrabajoExtraId(trabajoExtraId);
        
        if (!itemsExistentes.isEmpty()) {
            log.info("  📋 Encontrados {} items existentes a eliminar", itemsExistentes.size());
            
            // Eliminar las entidades hijas de cada item (profesionales, materiales, gastos, jornales)
            for (TrabajoExtraItemCalculadora item : itemsExistentes) {
                log.debug("  🗑️ Eliminando relaciones del item {} - {}", item.getId(), item.getTipoProfesional());
                profesionalRepository.deleteByItemCalculadoraId(item.getId());
                materialRepository.deleteByItemCalculadoraId(item.getId());
                gastoRepository.deleteByItemCalculadoraId(item.getId());
                jornalRepository.deleteByItemCalculadoraId(item.getId());
            }
            
            // Hacer flush para asegurar que las eliminaciones se ejecuten antes de eliminar los items
            entityManager.flush();
            log.debug("  ✅ Flush ejecutado - relaciones hijas eliminadas de la BD");
            
            // Ahora sí eliminar los items principales
            itemRepository.deleteByTrabajoExtraId(trabajoExtraId);
            entityManager.flush();
            log.info("✅ Items existentes y relaciones eliminados correctamente");
        } else {
            log.info("  ℹ️ No hay items existentes - trabajo extra nuevo");
        }

        for (TrabajoExtraItemCalculadoraDTO itemDTO : itemsDTO) {
            log.info("💾 Guardando item: {} (esRubroVacio: {}, esModoManual: {})", 
                itemDTO.getTipoProfesional(), itemDTO.getEsRubroVacio(), itemDTO.getEsModoManual());
            
            TrabajoExtraItemCalculadora item = new TrabajoExtraItemCalculadora();
            item.setEmpresaId(empresaId);
            item.setTipoProfesional(itemDTO.getTipoProfesional());
            item.setDescripcion(itemDTO.getDescripcion());
            item.setObservaciones(itemDTO.getObservaciones());
            
            item.setEsModoManual(itemDTO.getEsModoManual() != null ? itemDTO.getEsModoManual() : false);
            item.setCantidadJornales(itemDTO.getCantidadJornales());
            item.setImporteJornal(itemDTO.getImporteJornal());
            item.setSubtotalManoObra(itemDTO.getSubtotalManoObra());
            item.setMateriales(itemDTO.getMateriales());
            item.setSubtotalMateriales(itemDTO.getSubtotalMateriales());
            item.setTotalManual(itemDTO.getTotalManual());
            item.setDescripcionTotalManual(itemDTO.getDescripcionTotalManual());
            item.setObservacionesTotalManual(itemDTO.getObservacionesTotalManual());
            item.setTotal(itemDTO.getTotal());
            item.setIncluirEnCalculoDias(itemDTO.getIncluirEnCalculoDias());
            item.setTrabajaEnParalelo(itemDTO.getTrabajaEnParalelo() != null ? itemDTO.getTrabajaEnParalelo() : true);
            item.setEsRubroVacio(itemDTO.getEsRubroVacio() != null ? itemDTO.getEsRubroVacio() : false);
            
            item.setEsGastoGeneral(itemDTO.getEsGastoGeneral() != null ? itemDTO.getEsGastoGeneral() : false);
            item.setSubtotalGastosGenerales(itemDTO.getSubtotalGastosGenerales());
            item.setDescripcionGastosGenerales(itemDTO.getDescripcionGastosGenerales());
            item.setObservacionesGastosGenerales(itemDTO.getObservacionesGastosGenerales());
            
            item.setDescripcionProfesionales(itemDTO.getDescripcionProfesionales());
            item.setObservacionesProfesionales(itemDTO.getObservacionesProfesionales());
            item.setDescripcionMateriales(itemDTO.getDescripcionMateriales());
            item.setObservacionesMateriales(itemDTO.getObservacionesMateriales());

            // Crear relación con TrabajoExtra
            TrabajoExtra trabajoExtra = new TrabajoExtra();
            trabajoExtra.setId(trabajoExtraId);
            item.setTrabajoExtra(trabajoExtra);

            TrabajoExtraItemCalculadora itemGuardado = itemRepository.save(item);
            log.info("  ✅ Item guardado con ID: {}", itemGuardado.getId());

            // Guardar jornales
            if (itemDTO.getJornales() != null && !itemDTO.getJornales().isEmpty()) {
                log.debug("    💼 Guardando {} jornales", itemDTO.getJornales().size());
                guardarJornales(itemGuardado, empresaId, itemDTO.getJornales());
            }

            // Guardar materiales
            if (itemDTO.getMaterialesLista() != null && !itemDTO.getMaterialesLista().isEmpty()) {
                log.debug("    🧱 Guardando {} materiales", itemDTO.getMaterialesLista().size());
                guardarMateriales(itemGuardado, empresaId, itemDTO.getMaterialesLista());
            }

            // Guardar profesionales
            if (itemDTO.getProfesionales() != null && !itemDTO.getProfesionales().isEmpty()) {
                log.debug("    👷 Guardando {} profesionales", itemDTO.getProfesionales().size());
                guardarProfesionales(itemGuardado, empresaId, itemDTO.getProfesionales());
            }

            // Guardar gastos generales
            if (itemDTO.getGastosGenerales() != null && !itemDTO.getGastosGenerales().isEmpty()) {
                log.debug("    💰 Guardando {} gastos generales", itemDTO.getGastosGenerales().size());
                guardarGastosGenerales(itemGuardado, empresaId, itemDTO.getGastosGenerales());
            }
        }
        
        // Flush final para asegurar que todas las operaciones se persistan
        entityManager.flush();
        log.info("✅ Todos los items y sus relaciones han sido guardados exitosamente");
    }

    /**
     * Obtener items calculadora de un trabajo extra.
     */
    @Transactional(readOnly = true)
    /**
     * Obtiene los items del trabajo extra con todas las relaciones cargadas.
     * Usa múltiples queries para evitar MultipleBagFetchException de Hibernate.
     */
    public List<TrabajoExtraItemCalculadoraResponseDTO> obtenerItems(Long trabajoExtraId) {
        log.info("📋 Obteniendo items calculadora para trabajo extra {}", trabajoExtraId);
        
        // Cargar items base
        List<TrabajoExtraItemCalculadora> items = itemRepository.findByTrabajoExtraId(trabajoExtraId);
        log.info("✅ Encontrados {} items calculadora", items.size());
        
        // Cargar todas las relaciones en queries separadas para evitar MultipleBagFetchException
        if (!items.isEmpty()) {
            log.info("🔄 Cargando relaciones para {} items...", items.size());
            itemRepository.findByTrabajoExtraIdWithProfesionales(trabajoExtraId);
            itemRepository.findByTrabajoExtraIdWithMateriales(trabajoExtraId);
            itemRepository.findByTrabajoExtraIdWithJornales(trabajoExtraId);
            itemRepository.findByTrabajoExtraIdWithGastos(trabajoExtraId);
            log.info("✅ Relaciones cargadas exitosamente");
        }
        
        return items.stream()
                .map(item -> {
                    log.info("  📦 Item ID {}: {} jornales, {} materiales, {} gastos generales", 
                             item.getId(),
                             item.getJornales() != null ? item.getJornales().size() : 0,
                             item.getMaterialesLista() != null ? item.getMaterialesLista().size() : 0,
                             item.getGastosGenerales() != null ? item.getGastosGenerales().size() : 0);
                    return this.mapearItemAResponse(item);
                })
                .collect(Collectors.toList());
    }

    /**
     * Calcular totales del trabajo extra basado en items.
     */
    public TotalesTrabajoExtra calcularTotales(Long trabajoExtraId, TrabajoExtra trabajoExtra) {
        List<TrabajoExtraItemCalculadora> items = itemRepository.findByTrabajoExtraId(trabajoExtraId);
        
        BigDecimal totalPresupuesto = BigDecimal.ZERO;
        BigDecimal totalMateriales = BigDecimal.ZERO;
        BigDecimal totalProfesionales = BigDecimal.ZERO;

        for (TrabajoExtraItemCalculadora item : items) {
            if (item.getTotal() != null) {
                totalPresupuesto = totalPresupuesto.add(item.getTotal());
            }
            
            if (item.getSubtotalMateriales() != null) {
                totalMateriales = totalMateriales.add(item.getSubtotalMateriales());
            }
            
            if (item.getSubtotalManoObra() != null) {
                totalProfesionales = totalProfesionales.add(item.getSubtotalManoObra());
            }
        }

        // Calcular honorarios
        BigDecimal totalHonorarios = calcularHonorarios(trabajoExtra, totalPresupuesto, 
            totalMateriales, totalProfesionales);

        // Calcular mayores costos
        BigDecimal totalMayoresCostos = calcularMayoresCostos(trabajoExtra, totalPresupuesto, 
            totalHonorarios, totalMateriales, totalProfesionales);

        // Calcular totales finales
        BigDecimal totalPresupuestoConHonorarios = totalPresupuesto.add(totalHonorarios);
        BigDecimal totalFinal = totalPresupuestoConHonorarios.add(totalMayoresCostos);
        BigDecimal totalGeneral = totalFinal;

        return TotalesTrabajoExtra.builder()
                .totalPresupuesto(totalPresupuesto)
                .totalHonorarios(totalHonorarios)
                .totalHonorariosCalculado(totalHonorarios)
                .totalMayoresCostos(totalMayoresCostos)
                .totalPresupuestoConHonorarios(totalPresupuestoConHonorarios)
                .totalFinal(totalFinal)
                .montoTotal(totalFinal)
                .totalMateriales(totalMateriales)
                .totalProfesionales(totalProfesionales)
                .totalGeneral(totalGeneral)
                .build();
    }

    // ============================================================================
    // MÉTODOS PRIVADOS - GUARDAR DETALLES
    // ============================================================================

    private void guardarJornales(TrabajoExtraItemCalculadora item, Long empresaId, 
                                 List<TrabajoExtraJornalCalculadoraDTO> jornalesDTO) {
        log.debug("      💼 Guardando {} jornales para item {}", jornalesDTO.size(), item.getId());
        for (TrabajoExtraJornalCalculadoraDTO jornalDTO : jornalesDTO) {
            TrabajoExtraJornalCalculadora jornal = new TrabajoExtraJornalCalculadora();
            jornal.setItemCalculadora(item);
            jornal.setEmpresaId(empresaId);
            jornal.setProfesionalObraId(jornalDTO.getProfesionalObraId());
            jornal.setRol(jornalDTO.getRol());
            jornal.setCantidad(jornalDTO.getCantidad());
            jornal.setValorUnitario(jornalDTO.getValorUnitario());
            jornal.setSubtotal(jornalDTO.getSubtotal());
            jornal.setIncluirEnCalculoDias(jornalDTO.getIncluirEnCalculoDias());
            jornal.setFrontendId(jornalDTO.getFrontendId());
            jornal.setObservaciones(jornalDTO.getObservaciones());
            TrabajoExtraJornalCalculadora guardado = jornalRepository.save(jornal);
            log.trace("        ✓ Jornal guardado ID: {} - {}", guardado.getId(), guardado.getRol());
        }
        log.debug("      ✅ {} jornales guardados", jornalesDTO.size());
    }

    private void guardarMateriales(TrabajoExtraItemCalculadora item, Long empresaId,
                                   List<TrabajoExtraMaterialCalculadoraDTO> materialesDTO) {
        log.debug("      🧱 Guardando {} materiales para item {}", materialesDTO.size(), item.getId());
        for (TrabajoExtraMaterialCalculadoraDTO materialDTO : materialesDTO) {
            TrabajoExtraMaterialCalculadora material = new TrabajoExtraMaterialCalculadora();
            material.setItemCalculadora(item);
            material.setEmpresaId(empresaId);
            material.setObraMaterialId(materialDTO.getObraMaterialId());
            
            // Validar que nombre no sea null (campo obligatorio en BD)
            String nombre = materialDTO.getNombre();
            if (nombre == null || nombre.trim().isEmpty()) {
                // Usar descripción como nombre si está disponible, sino valor por defecto
                nombre = (materialDTO.getDescripcion() != null && !materialDTO.getDescripcion().trim().isEmpty()) 
                    ? materialDTO.getDescripcion() 
                    : "Material sin nombre";
                log.warn("⚠️ Material sin nombre recibido. Usando: '{}'", nombre);
            }
            material.setNombre(nombre);
            
            // Validar que unidad no sea null (campo obligatorio en BD)
            String unidad = materialDTO.getUnidad();
            if (unidad == null || unidad.trim().isEmpty()) {
                unidad = "Unidad";
                log.warn("⚠️ Material sin unidad recibido. Usando valor por defecto: 'Unidad'");
            }
            material.setUnidad(unidad);
            
            material.setDescripcion(materialDTO.getDescripcion());
            material.setCantidad(materialDTO.getCantidad());
            material.setPrecio(materialDTO.getPrecio());
            material.setSubtotal(materialDTO.getSubtotal());
            material.setFrontendId(materialDTO.getFrontendId());
            material.setObservaciones(materialDTO.getObservaciones());
            TrabajoExtraMaterialCalculadora guardado = materialRepository.save(material);
            log.trace("        ✓ Material guardado ID: {} - {}", guardado.getId(), guardado.getNombre());
        }
        log.debug("      ✅ {} materiales guardados", materialesDTO.size());
    }

    private void guardarProfesionales(TrabajoExtraItemCalculadora item, Long empresaId,
                                     List<TrabajoExtraProfesionalCalculadoraDTO> profesionalesDTO) {
        log.debug("      👷 Guardando {} profesionales para item {}", profesionalesDTO.size(), item.getId());
        for (TrabajoExtraProfesionalCalculadoraDTO profesionalDTO : profesionalesDTO) {
            TrabajoExtraProfesionalCalculadora profesional = new TrabajoExtraProfesionalCalculadora();
            profesional.setItemCalculadora(item);
            profesional.setEmpresaId(empresaId);
            profesional.setProfesionalObraId(profesionalDTO.getProfesionalObraId());
            profesional.setRol(profesionalDTO.getRol());
            profesional.setNombreCompleto(profesionalDTO.getNombreCompleto());
            profesional.setCantidadJornales(profesionalDTO.getCantidadJornales());
            profesional.setValorJornal(profesionalDTO.getValorJornal());
            profesional.setSubtotal(profesionalDTO.getSubtotal());
            profesional.setIncluirEnCalculoDias(profesionalDTO.getIncluirEnCalculoDias());
            profesional.setFrontendId(profesionalDTO.getFrontendId());
            profesional.setObservaciones(profesionalDTO.getObservaciones());
            TrabajoExtraProfesionalCalculadora guardado = profesionalRepository.save(profesional);
            log.trace("        ✓ Profesional guardado ID: {} - {}", guardado.getId(), guardado.getRol());
        }
        log.debug("      ✅ {} profesionales guardados", profesionalesDTO.size());
    }

    private void guardarGastosGenerales(TrabajoExtraItemCalculadora item, Long empresaId,
                                       List<TrabajoExtraGastoGeneralDTO> gastosDTO) {
        log.debug("      💰 Guardando {} gastos generales para item {}", gastosDTO.size(), item.getId());
        int ordenCounter = 1;
        for (TrabajoExtraGastoGeneralDTO gastoDTO : gastosDTO) {
            TrabajoExtraGastoGeneral gasto = new TrabajoExtraGastoGeneral();
            gasto.setItemCalculadora(item);
            gasto.setEmpresaId(empresaId);
            gasto.setDescripcion(gastoDTO.getDescripcion());
            gasto.setCantidad(gastoDTO.getCantidad());
            gasto.setPrecioUnitario(gastoDTO.getPrecioUnitario());
            gasto.setSubtotal(gastoDTO.getSubtotal());
            gasto.setSinCantidad(gastoDTO.getSinCantidad() != null ? gastoDTO.getSinCantidad() : false);
            gasto.setSinPrecio(gastoDTO.getSinPrecio() != null ? gastoDTO.getSinPrecio() : false);
            
            // Validar que orden no sea null (campo obligatorio en BD)
            Integer orden = gastoDTO.getOrden();
            if (orden == null) {
                orden = ordenCounter++;
                log.warn("⚠️ Gasto general sin orden recibido. Asignando orden: {}", orden);
            }
            gasto.setOrden(orden);
            
            gasto.setFrontendId(gastoDTO.getFrontendId());
            gasto.setObservaciones(gastoDTO.getObservaciones());
            TrabajoExtraGastoGeneral guardado = gastoRepository.save(gasto);
            log.trace("        ✓ Gasto general guardado ID: {} - {}", guardado.getId(), guardado.getDescripcion());
        }
        log.debug("      ✅ {} gastos generales guardados", gastosDTO.size());
    }

    // ============================================================================
    // MÉTODOS PRIVADOS - MAPEO
    // ============================================================================

    private TrabajoExtraItemCalculadoraResponseDTO mapearItemAResponse(TrabajoExtraItemCalculadora item) {
        return TrabajoExtraItemCalculadoraResponseDTO.builder()
                .id(item.getId())
                .trabajoExtraId(item.getTrabajoExtra().getId())
                .empresaId(item.getEmpresaId())
                .tipoProfesional(item.getTipoProfesional())
                .descripcion(item.getDescripcion())
                .observaciones(item.getObservaciones())
                .esModoManual(item.getEsModoManual())
                .cantidadJornales(item.getCantidadJornales())
                .importeJornal(item.getImporteJornal())
                .subtotalManoObra(item.getSubtotalManoObra())
                .materiales(item.getMateriales())
                .subtotalMateriales(item.getSubtotalMateriales())
                .totalManual(item.getTotalManual())
                .descripcionTotalManual(item.getDescripcionTotalManual())
                .observacionesTotalManual(item.getObservacionesTotalManual())
                .total(item.getTotal())
                .incluirEnCalculoDias(item.getIncluirEnCalculoDias())
                .trabajaEnParalelo(item.getTrabajaEnParalelo())
                .esRubroVacio(item.getEsRubroVacio())
                .esGastoGeneral(item.getEsGastoGeneral())
                .subtotalGastosGenerales(item.getSubtotalGastosGenerales())
                .descripcionGastosGenerales(item.getDescripcionGastosGenerales())
                .observacionesGastosGenerales(item.getObservacionesGastosGenerales())
                .descripcionProfesionales(item.getDescripcionProfesionales())
                .observacionesProfesionales(item.getObservacionesProfesionales())
                .descripcionMateriales(item.getDescripcionMateriales())
                .observacionesMateriales(item.getObservacionesMateriales())
                .profesionales(mapearProfesionalesAResponse(item.getProfesionales()))
                .materialesLista(mapearMaterialesAResponse(item.getMaterialesLista()))
                .jornales(mapearJornalesAResponse(item.getJornales()))
                .gastosGenerales(mapearGastosAResponse(item.getGastosGenerales()))
                .createdAt(item.getCreatedAt())
                .updatedAt(item.getUpdatedAt())
                .build();
    }

    private List<TrabajoExtraProfesionalCalculadoraResponseDTO> mapearProfesionalesAResponse(
            List<TrabajoExtraProfesionalCalculadora> profesionales) {
        if (profesionales == null) return new ArrayList<>();
        return profesionales.stream()
                .map(p -> TrabajoExtraProfesionalCalculadoraResponseDTO.builder()
                        .id(p.getId())
                        .itemCalculadoraId(p.getItemCalculadora().getId())
                        .empresaId(p.getEmpresaId())
                        .profesionalObraId(p.getProfesionalObraId())
                        .rol(p.getRol())
                        .nombreCompleto(p.getNombreCompleto())
                        .cantidadJornales(p.getCantidadJornales())
                        .valorJornal(p.getValorJornal())
                        .subtotal(p.getSubtotal())
                        .incluirEnCalculoDias(p.getIncluirEnCalculoDias())
                        .frontendId(p.getFrontendId())
                        .observaciones(p.getObservaciones())
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TrabajoExtraMaterialCalculadoraResponseDTO> mapearMaterialesAResponse(
            List<TrabajoExtraMaterialCalculadora> materiales) {
        if (materiales == null) return new ArrayList<>();
        return materiales.stream()
                .map(m -> TrabajoExtraMaterialCalculadoraResponseDTO.builder()
                        .id(m.getId())
                        .itemCalculadoraId(m.getItemCalculadora().getId())
                        .empresaId(m.getEmpresaId())
                        .obraMaterialId(m.getObraMaterialId())
                        .nombre(m.getNombre())
                        .descripcion(m.getDescripcion())
                        .unidad(m.getUnidad())
                        .cantidad(m.getCantidad())
                        .precio(m.getPrecio())
                        .subtotal(m.getSubtotal())
                        .frontendId(m.getFrontendId())
                        .observaciones(m.getObservaciones())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TrabajoExtraJornalCalculadoraResponseDTO> mapearJornalesAResponse(
            List<TrabajoExtraJornalCalculadora> jornales) {
        if (jornales == null) return new ArrayList<>();
        return jornales.stream()
                .map(j -> TrabajoExtraJornalCalculadoraResponseDTO.builder()
                        .id(j.getId())
                        .itemCalculadoraId(j.getItemCalculadora().getId())
                        .empresaId(j.getEmpresaId())
                        .profesionalObraId(j.getProfesionalObraId())
                        .rol(j.getRol())
                        .cantidad(j.getCantidad())
                        .valorUnitario(j.getValorUnitario())
                        .subtotal(j.getSubtotal())
                        .incluirEnCalculoDias(j.getIncluirEnCalculoDias())
                        .frontendId(j.getFrontendId())
                        .observaciones(j.getObservaciones())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TrabajoExtraGastoGeneralResponseDTO> mapearGastosAResponse(
            List<TrabajoExtraGastoGeneral> gastos) {
        if (gastos == null) return new ArrayList<>();
        return gastos.stream()
                .map(g -> TrabajoExtraGastoGeneralResponseDTO.builder()
                        .id(g.getId())
                        .itemCalculadoraId(g.getItemCalculadora().getId())
                        .empresaId(g.getEmpresaId())
                        .descripcion(g.getDescripcion())
                        .cantidad(g.getCantidad())
                        .precioUnitario(g.getPrecioUnitario())
                        .subtotal(g.getSubtotal())
                        .sinCantidad(g.getSinCantidad())
                        .sinPrecio(g.getSinPrecio())
                        .orden(g.getOrden())
                        .frontendId(g.getFrontendId())
                        .observaciones(g.getObservaciones())
                        .createdAt(g.getCreatedAt())
                        .updatedAt(g.getUpdatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    // ============================================================================
    // MÉTODOS PRIVADOS - CÁLCULOS
    // ============================================================================

    private BigDecimal calcularHonorarios(TrabajoExtra trabajoExtra, BigDecimal totalPresupuesto,
                                          BigDecimal totalMateriales, BigDecimal totalProfesionales) {
        BigDecimal honorarios = BigDecimal.ZERO;

        // Si se aplica a todos con valor general
        if (Boolean.TRUE.equals(trabajoExtra.getHonorariosAplicarATodos()) 
                && trabajoExtra.getHonorariosValorGeneral() != null) {
            return aplicarHonorario(totalPresupuesto, trabajoExtra.getHonorariosValorGeneral(), 
                    trabajoExtra.getHonorariosTipoGeneral());
        }

        // Por categoría - Jornales/Profesionales
        if (Boolean.TRUE.equals(trabajoExtra.getHonorariosProfesionalesActivo()) 
                && trabajoExtra.getHonorariosProfesionalesValor() != null) {
            honorarios = honorarios.add(aplicarHonorario(totalProfesionales, 
                    trabajoExtra.getHonorariosProfesionalesValor(), 
                    trabajoExtra.getHonorariosProfesionalesTipo()));
        }

        // Por categoría - Materiales
        if (Boolean.TRUE.equals(trabajoExtra.getHonorariosMaterialesActivo()) 
                && trabajoExtra.getHonorariosMaterialesValor() != null) {
            honorarios = honorarios.add(aplicarHonorario(totalMateriales, 
                    trabajoExtra.getHonorariosMaterialesValor(), 
                    trabajoExtra.getHonorariosMaterialesTipo()));
        }

        return honorarios;
    }

    private BigDecimal aplicarHonorario(BigDecimal base, BigDecimal valor, String tipo) {
        if ("PORCENTAJE".equals(tipo)) {
            return base.multiply(valor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return valor;
        }
    }

    private BigDecimal calcularMayoresCostos(TrabajoExtra trabajoExtra, BigDecimal totalPresupuesto,
                                             BigDecimal totalHonorarios, BigDecimal totalMateriales, 
                                             BigDecimal totalProfesionales) {
        BigDecimal mayoresCostos = BigDecimal.ZERO;

        // Si se aplica valor general
        if (Boolean.TRUE.equals(trabajoExtra.getMayoresCostosAplicarValorGeneral()) 
                && trabajoExtra.getMayoresCostosValorGeneral() != null) {
            return aplicarMayorCosto(totalPresupuesto, trabajoExtra.getMayoresCostosValorGeneral(), 
                    trabajoExtra.getMayoresCostosTipoGeneral());
        }

        // Por categoría
        if (Boolean.TRUE.equals(trabajoExtra.getMayoresCostosProfesionalesActivo()) 
                && trabajoExtra.getMayoresCostosProfesionalesValor() != null) {
            mayoresCostos = mayoresCostos.add(aplicarMayorCosto(totalProfesionales, 
                    trabajoExtra.getMayoresCostosProfesionalesValor(), 
                    trabajoExtra.getMayoresCostosProfesionalesTipo()));
        }

        if (Boolean.TRUE.equals(trabajoExtra.getMayoresCostosMaterialesActivo()) 
                && trabajoExtra.getMayoresCostosMaterialesValor() != null) {
            mayoresCostos = mayoresCostos.add(aplicarMayorCosto(totalMateriales, 
                    trabajoExtra.getMayoresCostosMaterialesValor(), 
                    trabajoExtra.getMayoresCostosMaterialesTipo()));
        }

        return mayoresCostos;
    }

    private BigDecimal aplicarMayorCosto(BigDecimal base, BigDecimal valor, String tipo) {
        if ("PORCENTAJE".equals(tipo)) {
            return base.multiply(valor)
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        } else {
            return valor;
        }
    }

    // ============================================================================
    // MÉTODOS PARA ELIMINAR ASIGNACIONES INDIVIDUALES
    // ============================================================================

    /**
     * Elimina un profesional específico de una asignación de trabajo extra
     */
    @Transactional
    public void eliminarProfesional(Long empresaId, Long profesionalId) {
        log.info("🗑️ Buscando profesional {} en BD...", profesionalId);
        
        TrabajoExtraProfesionalCalculadora profesional = profesionalRepository.findById(profesionalId)
                .orElseThrow(() -> {
                    log.error("❌ Profesional {} no encontrado", profesionalId);
                    return new RuntimeException("Profesional no encontrado con ID: " + profesionalId);
                });
        
        // Validar que pertenezca a la empresa
        if (!profesional.getEmpresaId().equals(empresaId)) {
            log.error("❌ Profesional {} no pertenece a empresaId {}", profesionalId, empresaId);
            throw new RuntimeException("Profesional no pertenece a la empresa");
        }
        
        log.info("✅ Eliminando profesional {} (rol: {}, nombreCompleto: {})", 
            profesionalId, profesional.getRol(), profesional.getNombreCompleto());
        
        profesionalRepository.deleteById(profesionalId);
        entityManager.flush();
        
        log.info("✅ Profesional {} eliminado y persistido en BD", profesionalId);
    }

    /**
     * Elimina un material específico de una asignación de trabajo extra
     */
    @Transactional
    public void eliminarMaterial(Long empresaId, Long materialId) {
        log.info("🗑️ Buscando material {} en BD...", materialId);
        
        TrabajoExtraMaterialCalculadora material = materialRepository.findById(materialId)
                .orElseThrow(() -> {
                    log.error("❌ Material {} no encontrado", materialId);
                    return new RuntimeException("Material no encontrado con ID: " + materialId);
                });
        
        // Validar que pertenezca a la empresa
        if (!material.getEmpresaId().equals(empresaId)) {
            log.error("❌ Material {} no pertenece a empresaId {}", materialId, empresaId);
            throw new RuntimeException("Material no pertenece a la empresa");
        }
        
        log.info("✅ Eliminando material {} (nombre: {}, cantidad: {})", 
            materialId, material.getNombre(), material.getCantidad());
        
        materialRepository.deleteById(materialId);
        entityManager.flush();
        
        log.info("✅ Material {} eliminado y persistido en BD", materialId);
    }

    /**
     * Elimina un gasto general específico de una asignación de trabajo extra
     */
    @Transactional
    public void eliminarGastoGeneral(Long empresaId, Long gastoId) {
        log.info("🗑️ Buscando gasto general {} en BD...", gastoId);
        
        TrabajoExtraGastoGeneral gasto = gastoRepository.findById(gastoId)
                .orElseThrow(() -> {
                    log.error("❌ Gasto general {} no encontrado", gastoId);
                    return new RuntimeException("Gasto general no encontrado con ID: " + gastoId);
                });
        
        // Validar que pertenezca a la empresa
        if (!gasto.getEmpresaId().equals(empresaId)) {
            log.error("❌ Gasto general {} no pertenece a empresaId {}", gastoId, empresaId);
            throw new RuntimeException("Gasto general no pertenece a la empresa");
        }
        
        log.info("✅ Eliminando gasto general {} (descripcion: {}, subtotal: {})", 
            gastoId, gasto.getDescripcion(), gasto.getSubtotal());
        
        gastoRepository.deleteById(gastoId);
        entityManager.flush();
        
        log.info("✅ Gasto general {} eliminado y persistido en BD", gastoId);
    }

    // ============================================================================
    // CLASE INTERNA PARA TOTALES
    // ============================================================================

    @lombok.Data
    @lombok.Builder
    public static class TotalesTrabajoExtra {
        private BigDecimal totalPresupuesto;
        private BigDecimal totalHonorarios;
        private BigDecimal totalHonorariosCalculado;
        private BigDecimal totalMayoresCostos;
        private BigDecimal totalPresupuestoConHonorarios;
        private BigDecimal totalFinal;
        private BigDecimal montoTotal;
        private BigDecimal totalMateriales;
        private BigDecimal totalProfesionales;
        private BigDecimal totalGeneral;
    }
}
