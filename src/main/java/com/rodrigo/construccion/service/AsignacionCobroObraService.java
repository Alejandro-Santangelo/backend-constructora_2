package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.AsignacionCobroObraRequestDTO;
import com.rodrigo.construccion.dto.response.AsignacionCobroObraResponseDTO;
import com.rodrigo.construccion.model.entity.AsignacionCobroObra;
import com.rodrigo.construccion.model.entity.CobroObra;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.repository.AsignacionCobroObraRepository;
import com.rodrigo.construccion.repository.CobroObraRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AsignacionCobroObraService implements IAsignacionCobroObraService {

    private final AsignacionCobroObraRepository asignacionRepository;
    private final CobroObraRepository cobroObraRepository;
    private final ObraRepository obraRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;

    @Override
    @Transactional
    public AsignacionCobroObraResponseDTO crearAsignacion(AsignacionCobroObraRequestDTO request) {
        // Validar cobro
        CobroObra cobro = cobroObraRepository.findById(request.getCobroObraId())
                .orElseThrow(() -> new RuntimeException("Cobro no encontrado con ID: " + request.getCobroObraId()));

        // Validar obra
        Obra obra = obraRepository.findById(request.getObraId())
                .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + request.getObraId()));

        // Validar que el monto asignado no exceda el monto del cobro
        BigDecimal totalAsignado = asignacionRepository.calcularTotalAsignadoByCobro(request.getCobroObraId());
        BigDecimal nuevoTotal = totalAsignado.add(request.getMontoAsignado());
        
        if (nuevoTotal.compareTo(cobro.getMonto()) > 0) {
            throw new RuntimeException("El total asignado (" + nuevoTotal + 
                ") excede el monto del cobro (" + cobro.getMonto() + ")");
        }

        AsignacionCobroObra asignacion = new AsignacionCobroObra();
        mapearRequestAEntity(request, asignacion);
        
        asignacion.setCobroObra(cobro);
        asignacion.setObra(obra);

        // Asignar presupuesto si existe
        if (request.getPresupuestoNoClienteId() != null) {
            PresupuestoNoCliente presupuesto = presupuestoRepository.findById(request.getPresupuestoNoClienteId())
                    .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado"));
            asignacion.setPresupuestoNoCliente(presupuesto);
        }

        // Validar distribución
        if (!asignacion.validarDistribucion()) {
            throw new RuntimeException("La suma de la distribución por items no coincide con el monto asignado");
        }

        AsignacionCobroObra guardada = asignacionRepository.save(asignacion);
        return mapearEntityAResponse(guardada);
    }

    @Override
    @Transactional
    public AsignacionCobroObraResponseDTO actualizarAsignacion(Long id, AsignacionCobroObraRequestDTO request) {
        AsignacionCobroObra asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));

        if (!asignacion.estaActiva()) {
            throw new RuntimeException("No se puede actualizar una asignación anulada");
        }

        // Validar que el nuevo monto no exceda el total del cobro
        BigDecimal totalAsignado = asignacionRepository.calcularTotalAsignadoByCobro(request.getCobroObraId());
        // Restar el monto actual de esta asignación para recalcular
        BigDecimal totalSinEsta = totalAsignado.subtract(asignacion.getMontoAsignado());
        BigDecimal nuevoTotal = totalSinEsta.add(request.getMontoAsignado());
        
        if (nuevoTotal.compareTo(asignacion.getCobroObra().getMonto()) > 0) {
            throw new RuntimeException("El total asignado excede el monto del cobro");
        }

        mapearRequestAEntity(request, asignacion);
        asignacion.setFechaModificacion(LocalDateTime.now());

        // Validar distribución (permite asignación parcial - suma de ítems <= monto asignado)
        if (!asignacion.validarDistribucion()) {
            throw new RuntimeException("La suma de la distribución por items excede el monto asignado");
        }

        AsignacionCobroObra actualizada = asignacionRepository.save(asignacion);
        return mapearEntityAResponse(actualizada);
    }

    @Override
    @Transactional
    public void eliminarAsignacion(Long id) {
        AsignacionCobroObra asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));
        
        asignacionRepository.delete(asignacion);
    }

    @Override
    @Transactional(readOnly = true)
    public AsignacionCobroObraResponseDTO obtenerAsignacionPorId(Long id) {
        AsignacionCobroObra asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));
        return mapearEntityAResponse(asignacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionCobroObraResponseDTO> obtenerAsignacionesPorCobro(Long cobroId) {
        List<AsignacionCobroObra> asignaciones = asignacionRepository.findByCobroObraId(cobroId);
        return asignaciones.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionCobroObraResponseDTO> obtenerAsignacionesPorObra(Long obraId) {
        List<AsignacionCobroObra> asignaciones = asignacionRepository.findByObraId(obraId);
        return asignaciones.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionCobroObraResponseDTO> obtenerAsignacionesActivasPorCobro(Long cobroId) {
        List<AsignacionCobroObra> asignaciones = asignacionRepository.findActivasByCobroObraId(cobroId);
        return asignaciones.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalAsignadoPorCobro(Long cobroId) {
        return asignacionRepository.calcularTotalAsignadoByCobro(cobroId);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal calcularTotalRecibidoPorObra(Long obraId) {
        return asignacionRepository.calcularTotalRecibidoByObra(obraId);
    }

    @Override
    @Transactional
    public AsignacionCobroObraResponseDTO anularAsignacion(Long id) {
        AsignacionCobroObra asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada con ID: " + id));

        asignacion.anular();
        AsignacionCobroObra actualizada = asignacionRepository.save(asignacion);
        return mapearEntityAResponse(actualizada);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AsignacionCobroObraResponseDTO> obtenerAsignacionesPorEmpresa(Long empresaId) {
        List<AsignacionCobroObra> asignaciones = asignacionRepository.findByEmpresaId(empresaId);
        return asignaciones.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    // ========== MÉTODOS PRIVADOS DE MAPEO ==========

    private void mapearRequestAEntity(AsignacionCobroObraRequestDTO request, AsignacionCobroObra asignacion) {
        asignacion.setEmpresaId(request.getEmpresaId());
        asignacion.setMontoAsignado(request.getMontoAsignado());
        
        // Distribución por items - OPCIONALES
        asignacion.setMontoProfesionales(request.getMontoProfesionales());
        asignacion.setMontoMateriales(request.getMontoMateriales());
        asignacion.setMontoGastosGenerales(request.getMontoGastosGenerales());
        
        asignacion.setPorcentajeProfesionales(request.getPorcentajeProfesionales());
        asignacion.setPorcentajeMateriales(request.getPorcentajeMateriales());
        asignacion.setPorcentajeGastosGenerales(request.getPorcentajeGastosGenerales());
        
        if (request.getEstado() != null && !request.getEstado().isEmpty()) {
            asignacion.setEstado(request.getEstado());
        }
        asignacion.setObservaciones(request.getObservaciones());
    }

    private AsignacionCobroObraResponseDTO mapearEntityAResponse(AsignacionCobroObra asignacion) {
        AsignacionCobroObraResponseDTO response = new AsignacionCobroObraResponseDTO();
        
        response.setId(asignacion.getId());
        response.setCobroObraId(asignacion.getCobroObra().getId());
        response.setObraId(asignacion.getObra().getId());
        response.setObraNombre(asignacion.getObra().getNombre());
        
        if (asignacion.getPresupuestoNoCliente() != null) {
            response.setPresupuestoNoClienteId(asignacion.getPresupuestoNoCliente().getId());
        }
        
        response.setEmpresaId(asignacion.getEmpresaId());
        response.setMontoAsignado(asignacion.getMontoAsignado());
        
        // Distribución por items
        response.setMontoProfesionales(asignacion.getMontoProfesionales());
        response.setMontoMateriales(asignacion.getMontoMateriales());
        response.setMontoGastosGenerales(asignacion.getMontoGastosGenerales());
        
        response.setPorcentajeProfesionales(asignacion.getPorcentajeProfesionales());
        response.setPorcentajeMateriales(asignacion.getPorcentajeMateriales());
        response.setPorcentajeGastosGenerales(asignacion.getPorcentajeGastosGenerales());
        
        response.setEstado(asignacion.getEstado());
        response.setObservaciones(asignacion.getObservaciones());
        
        // Auditoría
        response.setFechaCreacion(asignacion.getFechaCreacion());
        response.setFechaModificacion(asignacion.getFechaModificacion());
        response.setUsuarioCreacionId(asignacion.getUsuarioCreacionId());
        response.setUsuarioModificacionId(asignacion.getUsuarioModificacionId());
        
        return response;
    }
}
