package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.RegistrarGastoRequest;
import com.rodrigo.construccion.dto.response.GastoObraProfesionalResponse;
import com.rodrigo.construccion.dto.response.SaldoCajaChicaResponse;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.exception.SaldoInsuficienteException;
import com.rodrigo.construccion.model.entity.GastoObraProfesional;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.repository.GastoObraProfesionalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GastoObraProfesionalService implements IGastoObraProfesionalService{

    private final GastoObraProfesionalRepository gastoRepository;
    private final IPresupuestoNoClienteService presupuestoService;
    private final IProfesionalObraService profesionalObraService;

    /**
     * Registrar un gasto de caja chica.
     * Valida saldo, descuenta del saldo_disponible y crea registro en Otros Costos del presupuesto.
     */
    @Override
    @Transactional
    public GastoObraProfesionalResponse registrarGasto(RegistrarGastoRequest request) {
        // 1. Buscar el profesional obra
        ProfesionalObra profesionalObra = profesionalObraService.obtenerPorId(request.getProfesionalObraId());

        // Validar empresa
        if (!profesionalObra.getEmpresaId().equals(request.getEmpresaId())) {
            throw new IllegalArgumentException("El profesional no pertenece a la empresa especificada");
        }

        // 2. Validar saldo suficiente
        BigDecimal saldoActual = profesionalObra.getSaldoDisponible();
        if (saldoActual.compareTo(request.getMonto()) < 0) {
            throw new SaldoInsuficienteException(String.format("Saldo insuficiente. Disponible: %.2f, Requerido: %.2f",
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

        // 4. Descontar del saldo y actualizar
        BigDecimal nuevoSaldo = saldoActual.subtract(request.getMonto());

        profesionalObraService.actualizarSaldoDisponible(profesionalObra.getId(), nuevoSaldo);

        // 5. Guardar el gasto
        gasto = gastoRepository.save(gasto);

        // 6. AUTOMÁTICO: Agregar a Otros Costos del presupuesto (buscar por dirección)
        agregarGastoAPresupuesto(profesionalObra, request.getMonto(), request.getDescripcion());

        // 7. Retornar response
        return toResponse(gasto, nuevoSaldo);
    }

    /* Agregar gasto automáticamente al presupuesto en "Otros Costos" */
    private void agregarGastoAPresupuesto(ProfesionalObra profesionalObra, BigDecimal monto, String descripcion) {
        // Buscar presupuesto por los 4 campos de dirección
        List<PresupuestoNoCliente> presupuestos = presupuestoService.buscarPorDireccionObra(
                profesionalObra.getDireccionObraCalle(),
                profesionalObra.getDireccionObraAltura(),
                profesionalObra.getDireccionObraPiso() != null ? profesionalObra.getDireccionObraPiso() : "",
                profesionalObra.getDireccionObraDepartamento() != null ? profesionalObra.getDireccionObraDepartamento() : ""
        );

        if (presupuestos.isEmpty()) {
            throw new ResourceNotFoundException("No se encontró presupuesto para la dirección de obra especificada");
        }

        // Tomar el presupuesto más reciente (última versión)
        PresupuestoNoCliente presupuesto = presupuestos.get(0);
    }

    /* Listar gastos de un profesional */
    @Override
    @Transactional(readOnly = true)
    public List<GastoObraProfesionalResponse> listarGastosPorProfesional(Long profesionalObraId, Long empresaId) {
        List<GastoObraProfesional> gastos = gastoRepository.findByProfesionalObraId(profesionalObraId, empresaId);

        // Obtener saldo actual para cada respuesta
        ProfesionalObra profesionalObra = profesionalObraService.obtenerPorId(profesionalObraId);

        BigDecimal saldoActual = profesionalObra.getSaldoDisponible();

        return gastos.stream()
                .map(g -> toResponse(g, saldoActual))
                .collect(Collectors.toList());
    }

    /* Obtener detalle de un gasto */
    @Override
    @Transactional(readOnly = true)
    public GastoObraProfesionalResponse obtenerGasto(Long id, Long empresaId) {
        GastoObraProfesional gasto = gastoRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Gasto no encontrado"));

        return toResponse(gasto, gasto.getProfesionalObra().getSaldoDisponible());
    }

    /* Convertir entidad a DTO Response */
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

    /* Listar gastos de una obra por dirección */
    @Override
    @Transactional(readOnly = true)
    public List<GastoObraProfesionalResponse> listarGastosPorObra(String calle, String altura, String piso, String depto, Long empresaId) {

        List<GastoObraProfesional> gastos = gastoRepository.findByDireccionObra(calle, altura,
                piso != null ? piso : "",
                depto != null ? depto : "",
                empresaId
        );

        return gastos.stream()
                .map(g -> toResponse(g, g.getProfesionalObra().getSaldoDisponible()))
                .collect(Collectors.toList());
    }

    /* Obtener saldo de caja chica de un profesional - Está siendo usado en ProfesionalObraController */
    @Override
    @Transactional(readOnly = true)
    public SaldoCajaChicaResponse obtenerSaldoCajaChica(Long profesionalObraId, Long empresaId) {
        ProfesionalObra profesionalObra = profesionalObraService.obtenerPorId(profesionalObraId);

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

}
