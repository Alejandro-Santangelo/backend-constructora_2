package com.rodrigo.construccion.service;

import com.rodrigo.construccion.config.TenantContext;
import com.rodrigo.construccion.dto.response.ObraResumenFinancieroDTO;
import com.rodrigo.construccion.enums.EstadoPago;
import com.rodrigo.construccion.model.entity.CobroObra;
import com.rodrigo.construccion.model.entity.GastoObraProfesional;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.PagoProfesionalObra;
import com.rodrigo.construccion.model.entity.PagoConsolidado;
import com.rodrigo.construccion.model.entity.PagoGastoGeneralObra;
import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.repository.CobroObraRepository;
import com.rodrigo.construccion.repository.GastoObraProfesionalRepository;
import com.rodrigo.construccion.repository.ObraRepository;
import com.rodrigo.construccion.repository.PagoProfesionalObraRepository;
import com.rodrigo.construccion.repository.PagoConsolidadoRepository;
import com.rodrigo.construccion.repository.PagoGastoGeneralObraRepository;
import com.rodrigo.construccion.repository.ProfesionalObraRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ObraFinancieroService implements IObraFinancieroService {

    private final ObraRepository obraRepository;
    private final CobroObraRepository cobroRepository;
    private final PagoProfesionalObraRepository pagoRepository;
    private final PagoConsolidadoRepository pagoConsolidadoRepository;
    private final PagoGastoGeneralObraRepository pagoGastoGeneralRepository;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final GastoObraProfesionalRepository gastoRepository;

    @Override
    @Transactional(readOnly = true)
    public ObraResumenFinancieroDTO obtenerResumenFinanciero(Long obraId) {
        // Obtener la obra
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new RuntimeException("Obra no encontrada con ID: " + obraId));

        ObraResumenFinancieroDTO resumen = new ObraResumenFinancieroDTO();
        resumen.setObraId(obra.getId());
        resumen.setNombreObra(obra.getNombre());
        resumen.setDireccionObra(obra.getDireccionCompleta());

        // ========== PRESUPUESTO ==========
        BigDecimal presupuestoEstimado = obra.getPresupuestoEstimado() != null 
                ? obra.getPresupuestoEstimado() 
                : BigDecimal.ZERO;
        resumen.setPresupuestoEstimado(presupuestoEstimado);

        // Total presupuesto con honorarios (del primer presupuesto si existe)
        BigDecimal totalPresupuestoConHonorarios = presupuestoEstimado;
        if (obra.getPresupuestosNoCliente() != null && !obra.getPresupuestosNoCliente().isEmpty()) {
            PresupuestoNoCliente primerPresupuesto = obra.getPresupuestosNoCliente().get(0);
            if (primerPresupuesto.getTotalPresupuestoConHonorarios() != null) {
                totalPresupuestoConHonorarios = primerPresupuesto.getTotalPresupuestoConHonorarios();
            }
        }
        resumen.setTotalPresupuestoConHonorarios(totalPresupuestoConHonorarios);

        // ========== COBROS AL CLIENTE ==========
        List<CobroObra> cobros = cobroRepository.findByObraId(obraId);
        
        BigDecimal totalCobrado = cobros.stream()
                .filter(c -> CobroObra.ESTADO_COBRADO.equals(c.getEstado()))
                .map(CobroObra::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalCobrado(totalCobrado);

        BigDecimal totalPendienteCobro = cobros.stream()
                .filter(c -> CobroObra.ESTADO_PENDIENTE.equals(c.getEstado()))
                .map(CobroObra::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalPendienteCobro(totalPendienteCobro);

        BigDecimal totalVencido = cobros.stream()
                .filter(c -> CobroObra.ESTADO_VENCIDO.equals(c.getEstado()))
                .map(CobroObra::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        resumen.setTotalVencido(totalVencido);

        long cantidadCobrosPendientes = cobros.stream()
                .filter(c -> CobroObra.ESTADO_PENDIENTE.equals(c.getEstado()))
                .count();
        resumen.setCantidadCobrosPendientes((int) cantidadCobrosPendientes);

        long cantidadCobrosRealizados = cobros.stream()
                .filter(c -> CobroObra.ESTADO_COBRADO.equals(c.getEstado()))
                .count();
        resumen.setCantidadCobrosRealizados((int) cantidadCobrosRealizados);

        // ========== PAGOS A PROFESIONALES ==========
        // Obtener todos los profesionales de ESTA OBRA ESPECÍFICA (no por dirección)
        // CORRECCIÓN: Filtrar por obra ID para evitar duplicación entre obras en la misma dirección
        Long empresaId = TenantContext.getTenantId();
        List<ProfesionalObra> profesionalesObra = profesionalObraRepository
                .findByObraIdAndEmpresaIdWithRelations(obraId, empresaId);

        BigDecimal totalPagadoProfesionales = BigDecimal.ZERO;
        BigDecimal totalPagosSemanales = BigDecimal.ZERO;
        BigDecimal totalAdelantos = BigDecimal.ZERO;
        BigDecimal totalPremiosBonos = BigDecimal.ZERO;
        BigDecimal adelantosPendientesDescuento = BigDecimal.ZERO;

        for (ProfesionalObra profesionalObra : profesionalesObra) {
            List<PagoProfesionalObra> pagos = pagoRepository.findByProfesionalObraId(profesionalObra.getId());
            
            for (PagoProfesionalObra pago : pagos) {
                if (PagoProfesionalObra.ESTADO_PAGADO.equals(pago.getEstado())) {
                    totalPagadoProfesionales = totalPagadoProfesionales.add(pago.getMontoFinal());
                    
                    if (PagoProfesionalObra.TIPO_SEMANAL.equals(pago.getTipoPago())) {
                        totalPagosSemanales = totalPagosSemanales.add(pago.getMontoFinal());
                    } else if (PagoProfesionalObra.TIPO_ADELANTO.equals(pago.getTipoPago())) {
                        totalAdelantos = totalAdelantos.add(pago.getMontoFinal());
                    } else if (PagoProfesionalObra.TIPO_PREMIO.equals(pago.getTipoPago()) 
                            || PagoProfesionalObra.TIPO_BONO.equals(pago.getTipoPago())) {
                        totalPremiosBonos = totalPremiosBonos.add(pago.getMontoFinal());
                    }
                }
            }
        }

        resumen.setTotalPagadoProfesionales(totalPagadoProfesionales);
        resumen.setTotalPagosSemanales(totalPagosSemanales);
        resumen.setTotalAdelantos(totalAdelantos);
        resumen.setTotalPremiosBonos(totalPremiosBonos);
        resumen.setAdelantosPendientesDescuento(adelantosPendientesDescuento);

        // ========== PAGOS DE MATERIALES ==========
        BigDecimal totalPagadoMateriales = BigDecimal.ZERO;
        if (obra.getPresupuestoNoClienteId() != null) {
            List<PagoConsolidado> pagosMateriales = pagoConsolidadoRepository
                    .findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(
                            obra.getPresupuestoNoClienteId(), 
                            empresaId
                    );
            totalPagadoMateriales = pagosMateriales.stream()
                    .filter(p -> EstadoPago.PAGADO.equals(p.getEstado()))
                    .map(PagoConsolidado::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        resumen.setTotalPagadoMateriales(totalPagadoMateriales);

        // ========== PAGOS DE GASTOS GENERALES ==========
        BigDecimal totalPagadoGastosGenerales = BigDecimal.ZERO;
        if (obra.getPresupuestoNoClienteId() != null) {
            List<PagoGastoGeneralObra> pagosGastos = pagoGastoGeneralRepository
                    .findByPresupuestoNoClienteIdAndEmpresaIdOrderByFechaPagoDesc(
                            obra.getPresupuestoNoClienteId(), 
                            empresaId
                    );
            totalPagadoGastosGenerales = pagosGastos.stream()
                    .filter(p -> EstadoPago.PAGADO.equals(p.getEstado()))
                    .map(PagoGastoGeneralObra::getMonto)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }
        resumen.setTotalPagadoGastosGenerales(totalPagadoGastosGenerales);

        // ========== TOTAL PAGADO GENERAL (SUMA DE TODOS LOS PAGOS) ==========
        BigDecimal totalPagadoGeneral = totalPagadoProfesionales
                .add(totalPagadoMateriales)
                .add(totalPagadoGastosGenerales);
        resumen.setTotalPagadoGeneral(totalPagadoGeneral);

        // ========== GASTOS DE CAJA CHICA ==========
        BigDecimal totalGastosCajaChica = BigDecimal.ZERO;
        for (ProfesionalObra profesionalObra : profesionalesObra) {
            List<GastoObraProfesional> gastos = gastoRepository.findByProfesionalObraId(profesionalObra.getId(), empresaId);
            for (GastoObraProfesional gasto : gastos) {
                totalGastosCajaChica = totalGastosCajaChica.add(gasto.getMonto());
            }
        }
        resumen.setTotalGastosCajaChica(totalGastosCajaChica);

        // ========== BALANCES ==========
        // Saldo obra = cobrado - (total pagado general + gastos caja chica)
        BigDecimal saldoObra = totalCobrado
                .subtract(totalPagadoGeneral)
                .subtract(totalGastosCajaChica);
        resumen.setSaldoObra(saldoObra);

        // Margen actual = cobrado - pagado profesionales
        BigDecimal margenActual = totalCobrado.subtract(totalPagadoProfesionales);
        resumen.setMargenActual(margenActual);

        // Porcentaje ejecución presupuesto
        BigDecimal porcentajeEjecucionPresupuesto = BigDecimal.ZERO;
        if (presupuestoEstimado.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeEjecucionPresupuesto = totalCobrado
                    .multiply(BigDecimal.valueOf(100))
                    .divide(presupuestoEstimado, 2, RoundingMode.HALF_UP);
        }
        resumen.setPorcentajeEjecucionPresupuesto(porcentajeEjecucionPresupuesto);

        // Porcentaje ejecución pagos
        BigDecimal porcentajeEjecucionPagos = BigDecimal.ZERO;
        if (presupuestoEstimado.compareTo(BigDecimal.ZERO) > 0) {
            porcentajeEjecucionPagos = totalPagadoProfesionales
                    .multiply(BigDecimal.valueOf(100))
                    .divide(presupuestoEstimado, 2, RoundingMode.HALF_UP);
        }
        resumen.setPorcentajeEjecucionPagos(porcentajeEjecucionPagos);

        // ========== ESTADO GENERAL ==========
        String estadoFinanciero;
        if (saldoObra.compareTo(BigDecimal.ZERO) > 0) {
            estadoFinanciero = "POSITIVO";
        } else if (saldoObra.compareTo(BigDecimal.ZERO) == 0) {
            estadoFinanciero = "NEUTRAL";
        } else {
            estadoFinanciero = "NEGATIVO";
        }
        resumen.setEstadoFinanciero(estadoFinanciero);

        resumen.setTieneCobrosPendientes(cantidadCobrosPendientes > 0);
        resumen.setTieneCobrosVencidos(totalVencido.compareTo(BigDecimal.ZERO) > 0);

        return resumen;
    }
}
