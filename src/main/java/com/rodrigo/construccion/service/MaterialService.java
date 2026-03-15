package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.MaterialRequestDTO;
import com.rodrigo.construccion.dto.response.MaterialEstadisticaResponseDTO;
import com.rodrigo.construccion.dto.response.MaterialConsolidadoDTO;
import com.rodrigo.construccion.dto.response.ObraMaterialDTO;
import com.rodrigo.construccion.dto.response.AsignacionMaterialDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Material;
import com.rodrigo.construccion.model.entity.PagoConsolidado;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.repository.MaterialRepository;
import com.rodrigo.construccion.repository.PagoConsolidadoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class MaterialService implements IMaterialService {

    private final MaterialRepository materialRepository;
    private final PagoConsolidadoRepository pagoConsolidadoRepository;
    private final EntityManager entityManager;
    private final EmpresaService empresaService;

    public MaterialService(MaterialRepository materialRepository, 
                          PagoConsolidadoRepository pagoConsolidadoRepository,
                          EntityManager entityManager,
                          EmpresaService empresaService) {
        this.materialRepository = materialRepository;
        this.pagoConsolidadoRepository = pagoConsolidadoRepository;
        this.entityManager = entityManager;
        this.empresaService = empresaService;
    }

    /* Obtener todos los materiales activos */
    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerTodosActivos() {
        return materialRepository.findByActivoTrue();
    }

    /* Obtener material por ID */
    @Override
    @Transactional(readOnly = true)
    public Material obtenerPorId(Long id) {
        return materialRepository.findByIdAndActivoTrue(id)
                .orElseThrow(() -> new ResourceNotFoundException("Material no encontrado con ID: " + id));
    }


    @Override
    @Transactional(readOnly = true)
    public Material buscarPorIdOpcional(Long id) {
        return materialRepository.findById(id).orElse(null);
    }

    /* Buscar materiales por texto */
    @Override
    @Transactional(readOnly = true)
    public Page<Material> buscarPorTexto(String texto, Pageable pageable) {
        return materialRepository.findByTextoContaining(texto, pageable);
    }

    /* Obtener materiales por rango de precio */
    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        return materialRepository.findByActivoTrueAndPrecioBetween(precioMin, precioMax);
    }

    /* Crear nuevo material */
    @Override
    public Material crear(MaterialRequestDTO material) {
        Material materialEntity = new Material();
        materialEntity.setNombre(material.getNombre());
        materialEntity.setDescripcion(material.getDescripcion());
        materialEntity.setUnidadMedida(material.getUnidadMedida());
        materialEntity.setPrecioUnitario(material.getPrecioUnitario());
        return materialRepository.save(materialEntity);
    }

    /* Actualizar material */
    @Override
    public Material actualizar(Long id, MaterialRequestDTO materialActualizado) {
        Material materialExistente = obtenerPorId(id);

        // Actualizar campos permitidos
        if (materialActualizado.getNombre() != null) {
            materialExistente.setNombre(materialActualizado.getNombre());
        }

        if (materialActualizado.getDescripcion() != null) {
            materialExistente.setDescripcion(materialActualizado.getDescripcion());
        }

        if (materialActualizado.getUnidadMedida() != null) {
            materialExistente.setUnidadMedida(materialActualizado.getUnidadMedida());
        }

        if (materialActualizado.getPrecioUnitario() != null) {
            materialExistente.setPrecioUnitario(materialActualizado.getPrecioUnitario());
        }

        return materialRepository.save(materialExistente);
    }

    /* Eliminar material (desactivar) */
    @Override
    public void eliminar(Long id) {
        Material material = obtenerPorId(id);
        material.setActivo(false);
        materialRepository.save(material);
    }


    /* MÉTODOS QUE NO ESTÁN SIENDO USADOS POR EL FRONTEND - PARA BORRAR */

    /* Obtener materiales con paginación */
    @Override
    @Transactional(readOnly = true)
    public Page<Material> obtenerMaterialesPaginados(Pageable pageable) {
        List<Material> materiales = materialRepository.findByActivoTrue();
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), materiales.size());

        List<Material> pageContent = materiales.subList(start, end);
        return new PageImpl<>(pageContent, pageable, materiales.size());
    }

    /* Obtener materiales por unidad de medida */
    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerPorUnidadMedida(String unidadMedida) {
        return materialRepository.findByActivoTrueAndUnidadMedida(unidadMedida);
    }

    /* Obtener estadísticas generales */
    @Override
    @Transactional(readOnly = true)
    public MaterialEstadisticaResponseDTO obtenerEstadisticas() {
        long totalMateriales = materialRepository.countByActivoTrue();
        BigDecimal precioPromedio = materialRepository.findPrecioPromedio();

        return new MaterialEstadisticaResponseDTO(
                totalMateriales,
                precioPromedio != null ? precioPromedio : BigDecimal.ZERO
        );
    }

    /* Obtener precio promedio */
    @Override
    @Transactional(readOnly = true)
    public BigDecimal obtenerPrecioPromedio() {
        BigDecimal precio = materialRepository.findPrecioPromedio();
        return precio != null ? precio : BigDecimal.ZERO;
    }

    /* Obtener todos los materiales ordenados por nombre - SIN FILTRAR POR EMPRESA (compartidos entre todas) */
    @Override
    @Transactional(readOnly = true)
    public List<Material> obtenerTodosOrdenadosPorNombre() {
        Session session = entityManager.unwrap(Session.class);
        
        // Deshabilitar filtro empresaFilter para que devuelva TODOS los materiales
        session.disableFilter("empresaFilter");
        
        return materialRepository.findAllActivosOrdenadosPorNombre();
    }

    /**
     * Busca un material por nombre (case-insensitive).
     * Si no existe, crea uno nuevo con los datos proporcionados.
     * Este método es útil para asignaciones de materiales globales.
     */
    @Override
    public Material buscarOCrearPorNombre(String nombre, String unidadMedida, BigDecimal precioUnitario) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre del material es requerido");
        }

        String nombreNormalizado = nombre.trim();

        // Buscar material existente por nombre (case-insensitive)
        return materialRepository.findAllActivosOrdenadosPorNombre().stream()
                .filter(m -> m.getNombre().equalsIgnoreCase(nombreNormalizado))
                .findFirst()
                .orElseGet(() -> {
                    // Crear nuevo material si no existe
                    Material nuevoMaterial = new Material();
                    nuevoMaterial.setNombre(nombreNormalizado);
                    nuevoMaterial.setUnidadMedida(unidadMedida);
                    nuevoMaterial.setPrecioUnitario(
                            precioUnitario != null ? precioUnitario : BigDecimal.ZERO
                    );
                    nuevoMaterial.setActivo(true);
                    return materialRepository.save(nuevoMaterial);
                });
    }

    /**
     * Obtener materiales consolidados con sus pagos
     * Similar a obtenerGastosGeneralesConsolidados - usa tabla de PAGOS
     */
    @Override
    @Transactional(readOnly = true)
    public List<MaterialConsolidadoDTO> obtenerMaterialesConsolidados(Long empresaId) {
        log.info("🔍 [CONSOLIDADO-MAT] Obteniendo materiales consolidados para empresa ID: {}", empresaId);
        
        // Validar empresa
        empresaService.findEmpresaById(empresaId);
        log.info("✅ [CONSOLIDADO-MAT] Empresa {} validada correctamente", empresaId);
        
        // Obtener TODOS los pagos de materiales de la empresa (tabla pagos_material_obra)
        log.info("📋 [CONSOLIDADO-MAT] Buscando TODOS los pagos de materiales para empresa {}...", empresaId);
        List<PagoConsolidado> todosLosPagos = pagoConsolidadoRepository.findByEmpresaIdOrderByFechaPagoDesc(empresaId);
        
        log.info("📊 [CONSOLIDADO-MAT] Total pagos encontrados: {}", todosLosPagos.size());
        
        if (todosLosPagos.isEmpty()) {
            log.warn("⚠️ [CONSOLIDADO-MAT] NO HAY PAGOS de materiales para empresa {}", empresaId);
            return new ArrayList<>();
        }
        
        // Agrupar pagos por concepto (tipo de material)
        Map<String, List<PagoConsolidado>> pagosPorMaterial = todosLosPagos.stream()
            .collect(Collectors.groupingBy(pago -> 
                pago.getConcepto() != null ? pago.getConcepto() : "Material"
            ));
        
        List<MaterialConsolidadoDTO> materialesDTO = new ArrayList<>();
        
        // Para cada tipo de material, crear su DTO consolidado
        for (Map.Entry<String, List<PagoConsolidado>> entry : pagosPorMaterial.entrySet()) {
            String conceptoMaterial = entry.getKey();
            List<PagoConsolidado> pagosMaterial = entry.getValue();
            
            // Obtener datos del primer pago
            PagoConsolidado primerPago = pagosMaterial.get(0);
            
            MaterialConsolidadoDTO materialDTO = MaterialConsolidadoDTO.builder()
                .materialId(primerPago.getMaterialCalculadora() != null && primerPago.getMaterialCalculadora().getId() != null ? 
                    primerPago.getMaterialCalculadora().getId() : null)
                .materialNombre(primerPago.getConcepto())
                .materialDescripcion(primerPago.getConcepto())
                .unidadMedida(null)
                .precioReferencia(primerPago.getPrecioUnitario())
                .obras(new ArrayList<>())
                .build();
            
            // Agrupar pagos por presupuestoNoClienteId (que representa la obra)
            Map<Long, List<PagoConsolidado>> pagosPorObra = pagosMaterial.stream()
                .filter(p -> p.getPresupuestoNoCliente() != null && p.getPresupuestoNoCliente().getId() != null)
                .collect(Collectors.groupingBy(p -> p.getPresupuestoNoCliente().getId()));
            
            // Para cada obra, crear su DTO
            for (Map.Entry<Long, List<PagoConsolidado>> obraEntry : pagosPorObra.entrySet()) {
                Long presupuestoId = obraEntry.getKey();
                List<PagoConsolidado> pagosObra = obraEntry.getValue();
                
                // Obtener datos de la obra
                PagoConsolidado pagoObra = pagosObra.get(0);
                PresupuestoNoCliente presupuesto = pagoObra.getPresupuestoNoCliente();
                
                ObraMaterialDTO obraDTO = ObraMaterialDTO.builder()
                    .obraId(presupuestoId)
                    .obraNombre(presupuesto != null && presupuesto.getNombreObra() != null ? 
                        presupuesto.getNombreObra() : "Obra " + presupuestoId)
                    .obraEstado(presupuesto != null && presupuesto.getEstado() != null ? 
                        presupuesto.getEstado().name() : "DESCONOCIDO")
                    .direccionCompleta(presupuesto != null ? construirDireccionPresupuesto(presupuesto) : "")
                    .asignaciones(new ArrayList<>())
                    .build();
                
                // Mapear cada pago a AsignacionMaterialDTO
                for (PagoConsolidado pago : pagosObra) {
                    AsignacionMaterialDTO asignacionDTO = mapearPagoAMaterialDTO(pago);
                    obraDTO.getAsignaciones().add(asignacionDTO);
                }
                
                // Calcular totales de la obra
                obraDTO.calcularTotales();
                materialDTO.getObras().add(obraDTO);
            }
            
            // Calcular totales del material
            materialDTO.calcularTotales();
            materialesDTO.add(materialDTO);
        }
        
        log.info("✅ [CONSOLIDADO-MAT] Se procesaron {} materiales con sus pagos", materialesDTO.size());
        return materialesDTO;
    }
    
    /**
     * Mapea PagoConsolidado a AsignacionMaterialDTO
     */
    private AsignacionMaterialDTO mapearPagoAMaterialDTO(PagoConsolidado pago) {
        BigDecimal cantidad = pago.getCantidad() != null ? pago.getCantidad() : BigDecimal.ONE;
        BigDecimal precioUnitario = pago.getPrecioUnitario() != null ? pago.getPrecioUnitario() : BigDecimal.ZERO;
        BigDecimal montoTotal = pago.getMonto() != null ? pago.getMonto() : 
            precioUnitario.multiply(cantidad);
        
        return AsignacionMaterialDTO.builder()
            .asignacionId(pago.getId())
            .descripcion(pago.getConcepto())
            .unidadMedida(null)
            .esGlobal(false)
            .cantidadAsignada(cantidad)
            .cantidadUtilizada(cantidad) // Ya está pagado
            .cantidadPendiente(BigDecimal.ZERO)
            .precioUnitario(precioUnitario)
            .totalAsignado(montoTotal)
            .totalUtilizado(montoTotal) // Ya está pagado
            .saldoPendiente(BigDecimal.ZERO)
            .fechaAsignacion(pago.getFechaPago() != null ? pago.getFechaPago().atStartOfDay() : null)
            .semana(null)
            .observaciones(pago.getObservaciones())
            .build();
    }
    
    /**
     * Construye dirección completa de un presupuesto
     */
    private String construirDireccionPresupuesto(PresupuestoNoCliente presupuesto) {
        if (presupuesto == null) return "";
        
        StringBuilder direccion = new StringBuilder();
        if (presupuesto.getDireccionObraCalle() != null && !presupuesto.getDireccionObraCalle().isBlank()) {
            direccion.append(presupuesto.getDireccionObraCalle());
        }
        if (presupuesto.getDireccionObraAltura() != null && !presupuesto.getDireccionObraAltura().isBlank()) {
            direccion.append(" ").append(presupuesto.getDireccionObraAltura());
        }
        return direccion.toString();
    }
}

