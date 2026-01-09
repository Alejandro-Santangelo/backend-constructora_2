package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CajaChicaMovimientoDTO;
import com.rodrigo.construccion.dto.response.SaldoCajaChicaDTO;
import com.rodrigo.construccion.model.entity.CajaChicaMovimiento;
import com.rodrigo.construccion.repository.CajaChicaMovimientoRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Service para gestionar caja chica de profesionales
 */
@Service
@Slf4j
public class CajaChicaService {

    @Autowired
    private CajaChicaMovimientoRepository cajaChicaRepository;

    /**
     * Asignar caja chica a un profesional
     */
    @Transactional
    public CajaChicaMovimiento asignarCajaChica(CajaChicaMovimientoDTO dto, Long empresaId) {
        log.info("✅ Asignando caja chica: {} a {} ({})", dto.getMonto(), dto.getProfesionalNombre(), dto.getProfesionalTipo());

        // 1. Guardar en tabla relacional (principal)
        CajaChicaMovimiento movimiento = CajaChicaMovimiento.builder()
            .empresaId(empresaId)
            .presupuestoId(dto.getPresupuestoId())
            .profesionalNombre(dto.getProfesionalNombre())
            .profesionalTipo(dto.getProfesionalTipo())
            .tipo("ASIGNACION")
            .monto(dto.getMonto())
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
            .descripcion(dto.getDescripcion())
            .usuarioRegistro(dto.getUsuarioRegistro())
            .build();

        CajaChicaMovimiento saved = cajaChicaRepository.save(movimiento);
        log.info("✅ Movimiento guardado en tabla con ID: {}", saved.getId());

        return saved;
    }

    /**
     * Registrar un gasto de caja chica
     */
    @Transactional
    public CajaChicaMovimiento registrarGasto(CajaChicaMovimientoDTO dto, Long empresaId) {
        log.info("💰 Registrando gasto: {} por {} ({})", dto.getMonto(), dto.getProfesionalNombre(), dto.getProfesionalTipo());

        // Validar que el profesional tenga saldo suficiente
        BigDecimal saldoActual = cajaChicaRepository.calcularSaldo(
            dto.getPresupuestoId(),
            empresaId,
            dto.getProfesionalNombre(),
            dto.getProfesionalTipo()
        );

        if (saldoActual.compareTo(dto.getMonto()) < 0) {
            String mensaje = String.format(
                "❌ Saldo insuficiente para %s (%s). Disponible: $%.2f, Solicitado: $%.2f",
                dto.getProfesionalNombre(),
                dto.getProfesionalTipo(),
                saldoActual,
                dto.getMonto()
            );
            log.error(mensaje);
            throw new IllegalArgumentException(mensaje);
        }

        // Guardar gasto
        CajaChicaMovimiento movimiento = CajaChicaMovimiento.builder()
            .empresaId(empresaId)
            .presupuestoId(dto.getPresupuestoId())
            .profesionalNombre(dto.getProfesionalNombre())
            .profesionalTipo(dto.getProfesionalTipo())
            .tipo("GASTO")
            .monto(dto.getMonto())
            .fecha(dto.getFecha() != null ? dto.getFecha() : LocalDate.now())
            .descripcion(dto.getDescripcion())
            .usuarioRegistro(dto.getUsuarioRegistro())
            .build();

        CajaChicaMovimiento saved = cajaChicaRepository.save(movimiento);
        log.info("✅ Gasto registrado con ID: {}. Saldo restante: {}", saved.getId(), saldoActual.subtract(dto.getMonto()));

        return saved;
    }

    /**
     * Consultar saldo de un profesional
     */
    public SaldoCajaChicaDTO consultarSaldo(Long presupuestoId, Long empresaId, String profesionalNombre, String profesionalTipo) {
        log.info("📊 Consultando saldo de {} ({}) en presupuesto {}", profesionalNombre, profesionalTipo, presupuestoId);

        BigDecimal totalAsignado = cajaChicaRepository.calcularTotalAsignaciones(
            presupuestoId, empresaId, profesionalNombre, profesionalTipo
        );

        BigDecimal totalGastado = cajaChicaRepository.calcularTotalGastos(
            presupuestoId, empresaId, profesionalNombre, profesionalTipo
        );

        BigDecimal saldoActual = totalAsignado.subtract(totalGastado);

        log.info("📊 Resultado: Asignado={}, Gastado={}, Saldo={}", totalAsignado, totalGastado, saldoActual);

        return SaldoCajaChicaDTO.builder()
            .profesionalNombre(profesionalNombre)
            .profesionalTipo(profesionalTipo)
            .totalAsignado(totalAsignado)
            .totalGastado(totalGastado)
            .saldoActual(saldoActual)
            .presupuestoId(presupuestoId)
            .build();
    }

    /**
     * Listar todos los movimientos de un presupuesto
     */
    public List<CajaChicaMovimiento> listarMovimientos(Long presupuestoId, Long empresaId) {
        log.info("📋 Listando movimientos del presupuesto {}", presupuestoId);
        return cajaChicaRepository.findByPresupuestoIdAndEmpresaIdOrderByFechaDesc(presupuestoId, empresaId);
    }

    /**
     * Listar movimientos de un profesional específico
     */
    public List<CajaChicaMovimiento> listarMovimientosProfesional(
        Long presupuestoId, 
        Long empresaId, 
        String profesionalNombre, 
        String profesionalTipo
    ) {
        log.info("📋 Listando movimientos de {} ({}) en presupuesto {}", profesionalNombre, profesionalTipo, presupuestoId);
        return cajaChicaRepository
            .findByPresupuestoIdAndEmpresaIdAndProfesionalNombreAndProfesionalTipoOrderByFechaDesc(
                presupuestoId, empresaId, profesionalNombre, profesionalTipo
            );
    }
}
