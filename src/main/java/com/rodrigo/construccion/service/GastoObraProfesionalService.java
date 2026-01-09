package com.rodrigo.construccion.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.construccion.dto.request.RegistrarGastoRequest;
import com.rodrigo.construccion.dto.response.GastoObraProfesionalResponse;
import com.rodrigo.construccion.dto.response.SaldoCajaChicaResponse;
import com.rodrigo.construccion.exception.SaldoInsuficienteException;
import com.rodrigo.construccion.model.entity.GastoObraProfesional;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.repository.GastoObraProfesionalRepository;
import com.rodrigo.construccion.repository.PresupuestoNoClienteRepository;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Servicio para gestionar gastos de obra profesional con caja chica
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GastoObraProfesionalService {

    private final GastoObraProfesionalRepository gastoRepository;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final PresupuestoNoClienteRepository presupuestoRepository;
    private final ObjectMapper objectMapper;

    /**
     * Registrar un gasto de caja chica.
     * Valida saldo, descuenta del saldo_disponible y crea registro en Otros Costos del presupuesto.
     */
    @Transactional
    public GastoObraProfesionalResponse registrarGasto(RegistrarGastoRequest request) {
        // 1. Buscar el profesional obra
        ProfesionalObra profesionalObra = profesionalObraRepository
            .findById(request.getProfesionalObraId())
            .orElseThrow(() -> new IllegalArgumentException("Profesional obra no encontrado"));

        // Validar empresa
        if (!profesionalObra.getEmpresaId().equals(request.getEmpresaId())) {
            throw new IllegalArgumentException("El profesional no pertenece a la empresa especificada");
        }

        // 2. Validar saldo suficiente
        BigDecimal saldoActual = profesionalObra.getSaldoDisponible();
        if (saldoActual.compareTo(request.getMonto()) < 0) {
            throw new SaldoInsuficienteException(
                String.format("Saldo insuficiente. Disponible: %.2f, Requerido: %.2f", 
                    saldoActual, request.getMonto())
            );
        }

        // 3. Crear el gasto
        GastoObraProfesional gasto = new GastoObraProfesional();
        gasto.setProfesionalObra(profesionalObra);
        gasto.setMonto(request.getMonto());
        gasto.setDescripcion(request.getDescripcion());
        gasto.setFotoTicket(request.getFotoTicket());
        gasto.setEmpresaId(request.getEmpresaId());
        gasto.setFechaHora(LocalDateTime.now());

        // 4. Descontar del saldo
        BigDecimal nuevoSaldo = saldoActual.subtract(request.getMonto());
        profesionalObra.setSaldoDisponible(nuevoSaldo);
        profesionalObraRepository.save(profesionalObra);

        // 5. Guardar el gasto
        gasto = gastoRepository.save(gasto);

        // 6. AUTOMÁTICO: Agregar a Otros Costos del presupuesto (buscar por dirección)
        try {
            agregarGastoAPresupuesto(profesionalObra, request.getMonto(), request.getDescripcion());
        } catch (Exception e) {
            // Log pero no falla la transacción si no encuentra presupuesto
            System.err.println("No se pudo agregar gasto al presupuesto: " + e.getMessage());
        }

        // 7. Retornar response
        return toResponse(gasto, nuevoSaldo);
    }

    /**
     * Agregar gasto automáticamente al presupuesto en "Otros Costos"
     */
    private void agregarGastoAPresupuesto(ProfesionalObra profesionalObra, BigDecimal monto, String descripcion) {
        // Buscar presupuesto por los 4 campos de dirección
        List<PresupuestoNoCliente> presupuestos = presupuestoRepository.findByDireccionObra(
            profesionalObra.getDireccionObraCalle(),
            profesionalObra.getDireccionObraAltura(),
            profesionalObra.getDireccionObraPiso() != null ? profesionalObra.getDireccionObraPiso() : "",
            profesionalObra.getDireccionObraDepartamento() != null ? profesionalObra.getDireccionObraDepartamento() : ""
        );

        if (presupuestos.isEmpty()) {
            throw new IllegalArgumentException("No se encontró presupuesto para la dirección de obra especificada");
        }

        // Tomar el presupuesto más reciente (última versión)
        PresupuestoNoCliente presupuesto = presupuestos.get(0);

        // TODO: Migrar a tabla normalizada presupuesto_no_cliente_otro_costo
        // Por ahora, crear entidad PresupuestoNoClienteOtroCosto en lugar de JSON
        log.warn("⚠️ GastoObraProfesional: Actualización de otros costos deprecada. Migrar a tabla normalizada.");
        
        // TEMPORAL: Agregar gasto directamente a la colección de otros costos
        // Esto funcionará cuando se migre completamente a tablas relacionadas
    }

    /**
     * Listar gastos de un profesional
     */
    @Transactional(readOnly = true)
    public List<GastoObraProfesionalResponse> listarGastosPorProfesional(Long profesionalObraId, Long empresaId) {
        List<GastoObraProfesional> gastos = gastoRepository.findByProfesionalObraId(profesionalObraId, empresaId);
        
        // Obtener saldo actual para cada respuesta
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
            .orElseThrow(() -> new IllegalArgumentException("Profesional obra no encontrado"));
        
        BigDecimal saldoActual = profesionalObra.getSaldoDisponible();
        
        return gastos.stream()
            .map(g -> toResponse(g, saldoActual))
            .collect(Collectors.toList());
    }

    /**
     * Listar gastos de una obra por dirección
     */
    @Transactional(readOnly = true)
    public List<GastoObraProfesionalResponse> listarGastosPorObra(
            String calle, String altura, String piso, String depto, Long empresaId) {
        
        List<GastoObraProfesional> gastos = gastoRepository.findByDireccionObra(
            calle, altura, 
            piso != null ? piso : "", 
            depto != null ? depto : "", 
            empresaId
        );
        
        return gastos.stream()
            .map(g -> toResponse(g, g.getProfesionalObra().getSaldoDisponible()))
            .collect(Collectors.toList());
    }

    /**
     * Obtener detalle de un gasto
     */
    @Transactional(readOnly = true)
    public GastoObraProfesionalResponse obtenerGasto(Long id, Long empresaId) {
        GastoObraProfesional gasto = gastoRepository.findByIdAndEmpresaId(id, empresaId)
            .orElseThrow(() -> new IllegalArgumentException("Gasto no encontrado"));
        
        return toResponse(gasto, gasto.getProfesionalObra().getSaldoDisponible());
    }

    /**
     * Obtener saldo de caja chica de un profesional
     */
    @Transactional(readOnly = true)
    public SaldoCajaChicaResponse obtenerSaldoCajaChica(Long profesionalObraId, Long empresaId) {
        ProfesionalObra profesionalObra = profesionalObraRepository.findById(profesionalObraId)
            .orElseThrow(() -> new IllegalArgumentException("Profesional obra no encontrado"));

        if (!profesionalObra.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("El profesional no pertenece a la empresa especificada");
        }

        BigDecimal montoAsignado = profesionalObra.getMontoAsignado();
        BigDecimal saldoDisponible = profesionalObra.getSaldoDisponible();
        BigDecimal gastado = montoAsignado.subtract(saldoDisponible);
        
        Long cantidadGastos = gastoRepository.countByProfesionalObraId(profesionalObraId, empresaId);

        return new SaldoCajaChicaResponse(
            montoAsignado,
            saldoDisponible,
            gastado,
            cantidadGastos.intValue()
        );
    }

    /**
     * Convertir entidad a DTO Response
     */
    private GastoObraProfesionalResponse toResponse(GastoObraProfesional gasto, BigDecimal saldoRestante) {
        GastoObraProfesionalResponse response = new GastoObraProfesionalResponse();
        response.setId(gasto.getId());
        response.setProfesionalObraId(gasto.getProfesionalObraId());
        response.setNombreProfesional(gasto.getNombreProfesional());
        response.setDireccionObra(gasto.getDireccionObra());
        response.setMonto(gasto.getMonto());
        response.setDescripcion(gasto.getDescripcion());
        response.setFechaHora(gasto.getFechaHora());
        response.setFotoTicket(gasto.getFotoTicket());
        response.setSaldoRestante(saldoRestante);
        return response;
    }
}
