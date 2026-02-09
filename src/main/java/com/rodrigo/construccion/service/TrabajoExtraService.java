package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.TrabajoExtraRequestDTO;
import com.rodrigo.construccion.dto.request.TrabajoExtraProfesionalDTO;
import com.rodrigo.construccion.dto.request.TrabajoExtraTareaDTO;
import com.rodrigo.construccion.dto.request.HonorariosConfigDTO;
import com.rodrigo.construccion.dto.request.MayoresCostosConfigDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraResponseDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraProfesionalResponseDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraTareaResponseDTO;
import com.rodrigo.construccion.dto.response.TrabajoExtraPdfResponseDTO;
import com.rodrigo.construccion.enums.EstadoTareaTrabajoExtra;
import com.rodrigo.construccion.model.entity.*;
import com.rodrigo.construccion.repository.*;
import com.rodrigo.construccion.service.helper.TrabajoExtraItemCalculadoraHelper;
import com.rodrigo.construccion.service.helper.TrabajoExtraItemCalculadoraHelper.TotalesTrabajoExtra;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Servicio para la gestión de trabajos extra por día
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TrabajoExtraService implements ITrabajoExtraService {

    private final TrabajoExtraRepository trabajoExtraRepository;
    private final TrabajoExtraDiaRepository trabajoExtraDiaRepository;
    private final TrabajoExtroProfesionalRepository trabajoExtroProfesionalRepository;
    private final TrabajoExtraTareaRepository trabajoExtraTareaRepository;
    private final TrabajoExtraPdfRepository trabajoExtraPdfRepository;
    private final ObraRepository obraRepository;
    private final TrabajoExtraItemCalculadoraHelper itemCalculadoraHelper;

    @Override
    @Transactional(readOnly = true)
    public List<TrabajoExtraResponseDTO> obtenerPorObra(Long empresaId, Long obraId) {
        log.info("📋 Obteniendo trabajos extra para obra {} y empresa {}", obraId, empresaId);
        
        validarObraPerteneciaEmpresa(obraId, empresaId);
        
        List<TrabajoExtra> trabajos = trabajoExtraRepository.findByObraIdAndEmpresaId(obraId, empresaId);
        
        log.info("✅ Encontrados {} trabajos extra para obra {}", trabajos.size(), obraId);
        trabajos.forEach(t -> log.info("  📄 Trabajo Extra ID: {}, Nombre: {}, Obra ID: {}", t.getId(), t.getNombre(), t.getObraId()));
        
        return trabajos.stream()
                .map(this::mapearEntityAResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TrabajoExtraResponseDTO obtenerPorId(Long empresaId, Long id) {
        log.info("Obteniendo trabajo extra {} para empresa {}", id, empresaId);
        
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Trabajo extra no encontrado con ID: " + id + " o no pertenece a la empresa"));
        
        return mapearEntityAResponse(trabajoExtra);
    }

    @Override
    @Transactional
    public TrabajoExtraResponseDTO crear(Long empresaId, TrabajoExtraRequestDTO request) {
        log.info("Creando trabajo extra para obra {} y empresa {}", request.getObraId(), empresaId);
        
        // Validar y obtener la obra
        Obra obra = obraRepository.findByIdAndEmpresaId(request.getObraId(), empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "La obra con ID " + request.getObraId() + " no existe o no pertenece a la empresa " + empresaId));
        
        // Obtener el clienteId de la obra
        Long clienteId = obra.getClienteId();
        if (clienteId == null) {
            throw new RuntimeException("La obra no tiene un cliente asignado");
        }
        
        // Crear el trabajo extra principal
        TrabajoExtra trabajoExtra = TrabajoExtra.builder()
                .obraId(request.getObraId())
                .clienteId(clienteId)
                .empresaId(empresaId)
                .nombre(request.getNombre())
                .observaciones(request.getObservaciones())
                // Datos de contacto
                .nombreEmpresa(request.getNombreEmpresa())
                .nombreSolicitante(request.getNombreSolicitante())
                .telefono(request.getTelefono())
                .mail(request.getMail())
                .direccionParticular(request.getDireccionParticular())
                // Dirección de obra
                .direccionObraCalle(request.getDireccionObraCalle())
                .direccionObraAltura(request.getDireccionObraAltura())
                .direccionObraBarrio(request.getDireccionObraBarrio())
                .direccionObraTorre(request.getDireccionObraTorre())
                .direccionObraPiso(request.getDireccionObraPiso())
                .direccionObraDepartamento(request.getDireccionObraDepartamento())
                .direccionObraLocalidad(request.getDireccionObraLocalidad())
                .direccionObraProvincia(request.getDireccionObraProvincia())
                .direccionObraCodigoPostal(request.getDireccionObraCodigoPostal())
                // Datos del presupuesto
                .nombreObra(request.getNombreObra())
                .descripcion(request.getDescripcion())
                .fechaProbableInicio(request.getFechaProbableInicio())
                .vencimiento(request.getVencimiento())
                .fechaCreacion(request.getFechaCreacion())
                .fechaEmision(request.getFechaEmision())
                .tiempoEstimadoTerminacion(request.getTiempoEstimadoTerminacion())
                .calculoAutomaticoDiasHabiles(request.getCalculoAutomaticoDiasHabiles() != null ? request.getCalculoAutomaticoDiasHabiles() : false)
                // Control y estado
                .version(request.getVersion() != null ? request.getVersion() : 1)
                .numeroPresupuesto(request.getNumeroPresupuesto())
                .estado(request.getEstado() != null ? request.getEstado() : "ENVIADO")
                // Totales
                .totalPresupuesto(request.getTotalPresupuesto() != null ? request.getTotalPresupuesto() : java.math.BigDecimal.ZERO)
                .totalHonorarios(request.getTotalHonorarios() != null ? request.getTotalHonorarios() : java.math.BigDecimal.ZERO)
                .totalMayoresCostos(request.getTotalMayoresCostos() != null ? request.getTotalMayoresCostos() : java.math.BigDecimal.ZERO)
                .totalFinal(request.getTotalFinal() != null ? request.getTotalFinal() : java.math.BigDecimal.ZERO)
                .montoTotal(request.getMontoTotal() != null ? request.getMontoTotal() : java.math.BigDecimal.ZERO)
                .esTrabajoExtra(request.getEsTrabajoExtra() != null ? request.getEsTrabajoExtra() : true)
                .build();
        
        TrabajoExtra guardado = trabajoExtraRepository.save(trabajoExtra);
        log.info("Trabajo extra creado exitosamente con ID: {}", guardado.getId());
        
        // Guardar días
        if (request.getDias() != null && !request.getDias().isEmpty()) {
            guardarDias(guardado.getId(), request.getDias());
        }
        
        // Guardar profesionales
        if (request.getProfesionales() != null && !request.getProfesionales().isEmpty()) {
            guardarProfesionales(guardado.getId(), request.getProfesionales());
        }
        
        // Guardar tareas
        if (request.getTareas() != null && !request.getTareas().isEmpty()) {
            guardarTareas(guardado.getId(), request.getTareas());
        }
        
        // ============================================================================
        // NUEVO: Guardar items calculadora (rubros) con honorarios y mayores costos
        // ============================================================================
        
        // Aplicar configuración de honorarios (SIEMPRE usar campos planos)
        log.info("💰 Aplicando honorarios desde campos planos del request (crear)");
        guardado.setHonorariosAplicarATodos(request.getHonorariosAplicarATodos());
        guardado.setHonorariosValorGeneral(request.getHonorariosValorGeneral());
        guardado.setHonorariosTipoGeneral(request.getHonorariosTipoGeneral());
        guardado.setHonorariosJornalesActivo(request.getHonorariosJornalesActivo());
        guardado.setHonorariosJornalesValor(request.getHonorariosJornalesValor());
        guardado.setHonorariosJornalesTipo(request.getHonorariosJornalesTipo());
        guardado.setHonorariosMaterialesActivo(request.getHonorariosMaterialesActivo());
        guardado.setHonorariosMaterialesValor(request.getHonorariosMaterialesValor());
        guardado.setHonorariosMaterialesTipo(request.getHonorariosMaterialesTipo());
        guardado.setHonorariosProfesionalesActivo(request.getHonorariosProfesionalesActivo());
        guardado.setHonorariosProfesionalesValor(request.getHonorariosProfesionalesValor());
        guardado.setHonorariosProfesionalesTipo(request.getHonorariosProfesionalesTipo());
        guardado.setHonorariosOtrosCostosActivo(request.getHonorariosOtrosCostosActivo());
        guardado.setHonorariosOtrosCostosValor(request.getHonorariosOtrosCostosValor());
        guardado.setHonorariosOtrosCostosTipo(request.getHonorariosOtrosCostosTipo());
        guardado.setHonorariosConfiguracionPresupuestoActivo(request.getHonorariosConfiguracionPresupuestoActivo());
        guardado.setHonorariosConfiguracionPresupuestoValor(request.getHonorariosConfiguracionPresupuestoValor());
        guardado.setHonorariosConfiguracionPresupuestoTipo(request.getHonorariosConfiguracionPresupuestoTipo());
        
        // Aplicar configuración de mayores costos (objeto anidado o campos planos)
        if (request.getMayoresCostos() != null) {
            aplicarMayoresCostos(guardado, request.getMayoresCostos());
        } else {
            // Si no viene el objeto anidado, mapear campos planos directamente
            log.info("📊 Aplicando mayores costos desde campos planos del request (crear)");
            guardado.setMayoresCostosAplicarValorGeneral(request.getMayoresCostosAplicarValorGeneral());
            guardado.setMayoresCostosValorGeneral(request.getMayoresCostosValorGeneral());
            guardado.setMayoresCostosTipoGeneral(request.getMayoresCostosTipoGeneral());
            guardado.setMayoresCostosJornalesActivo(request.getMayoresCostosJornalesActivo());
            guardado.setMayoresCostosJornalesValor(request.getMayoresCostosJornalesValor());
            guardado.setMayoresCostosJornalesTipo(request.getMayoresCostosJornalesTipo());
            guardado.setMayoresCostosMaterialesActivo(request.getMayoresCostosMaterialesActivo());
            guardado.setMayoresCostosMaterialesValor(request.getMayoresCostosMaterialesValor());
            guardado.setMayoresCostosMaterialesTipo(request.getMayoresCostosMaterialesTipo());
            guardado.setMayoresCostosProfesionalesActivo(request.getMayoresCostosProfesionalesActivo());
            guardado.setMayoresCostosProfesionalesValor(request.getMayoresCostosProfesionalesValor());
            guardado.setMayoresCostosProfesionalesTipo(request.getMayoresCostosProfesionalesTipo());
            guardado.setMayoresCostosOtrosCostosActivo(request.getMayoresCostosOtrosCostosActivo());
            guardado.setMayoresCostosOtrosCostosValor(request.getMayoresCostosOtrosCostosValor());
            guardado.setMayoresCostosOtrosCostosTipo(request.getMayoresCostosOtrosCostosTipo());
            guardado.setMayoresCostosHonorariosActivo(request.getMayoresCostosHonorariosActivo());
            guardado.setMayoresCostosHonorariosValor(request.getMayoresCostosHonorariosValor());
            guardado.setMayoresCostosHonorariosTipo(request.getMayoresCostosHonorariosTipo());
            guardado.setMayoresCostosConfiguracionPresupuestoActivo(request.getMayoresCostosConfiguracionPresupuestoActivo());
            guardado.setMayoresCostosConfiguracionPresupuestoValor(request.getMayoresCostosConfiguracionPresupuestoValor());
            guardado.setMayoresCostosConfiguracionPresupuestoTipo(request.getMayoresCostosConfiguracionPresupuestoTipo());
            guardado.setMayoresCostosGeneralImportado(request.getMayoresCostosGeneralImportado());
            guardado.setMayoresCostosRubroImportado(request.getMayoresCostosRubroImportado());
            guardado.setMayoresCostosNombreRubroImportado(request.getMayoresCostosNombreRubroImportado());
            guardado.setMayoresCostosExplicacion(request.getMayoresCostosExplicacion());
        }
        
        // Aplicar honorario de dirección
        if (request.getHonorarioDireccionPorcentaje() != null) {
            guardado.setHonorarioDireccionPorcentaje(request.getHonorarioDireccionPorcentaje());
        }
        if (request.getHonorarioDireccionImporte() != null) {
            guardado.setHonorarioDireccionImporte(request.getHonorarioDireccionImporte());
        }
        if (request.getHonorarioDireccionValorFijo() != null) {
            guardado.setHonorarioDireccionValorFijo(request.getHonorarioDireccionValorFijo());
        }
        
        // Guardar items calculadora
        if (request.getItemsCalculadora() != null && !request.getItemsCalculadora().isEmpty()) {
            itemCalculadoraHelper.guardarItems(guardado.getId(), empresaId, request.getItemsCalculadora());
            
            // Calcular y actualizar totales
            TotalesTrabajoExtra totales = itemCalculadoraHelper.calcularTotales(guardado.getId(), guardado);
            guardado.setTotalPresupuesto(totales.getTotalPresupuesto());
            guardado.setTotalHonorarios(totales.getTotalHonorarios());
            guardado.setTotalHonorariosCalculado(totales.getTotalHonorariosCalculado());
            guardado.setTotalMayoresCostos(totales.getTotalMayoresCostos());
            guardado.setTotalPresupuestoConHonorarios(totales.getTotalPresupuestoConHonorarios());
            guardado.setTotalFinal(totales.getTotalFinal());
            guardado.setMontoTotal(totales.getMontoTotal());
            guardado.setTotalMateriales(totales.getTotalMateriales());
            guardado.setTotalProfesionales(totales.getTotalProfesionales());
            guardado.setTotalGeneral(totales.getTotalGeneral());
            
            trabajoExtraRepository.save(guardado);
        }
        
        return mapearEntityAResponse(trabajoExtraRepository.findById(guardado.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public TrabajoExtraResponseDTO actualizar(Long empresaId, Long id, TrabajoExtraRequestDTO request) {
        log.info("Actualizando trabajo extra {} para empresa {}", id, empresaId);
        log.info("📋 Request obraId recibido: {}", request.getObraId());
        
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Trabajo extra no encontrado con ID: " + id + " o no pertenece a la empresa"));
        
        // Validar y obtener la obra
        Obra obra = obraRepository.findByIdAndEmpresaId(request.getObraId(), empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "La obra con ID " + request.getObraId() + " no existe o no pertenece a la empresa " + empresaId));
        
        // Obtener el clienteId de la obra
        Long clienteId = obra.getClienteId();
        if (clienteId == null) {
            throw new RuntimeException("La obra no tiene un cliente asignado");
        }
        
        // Actualizar todos los campos
        trabajoExtra.setObraId(request.getObraId());
        trabajoExtra.setClienteId(clienteId);
        trabajoExtra.setNombre(request.getNombre());
        trabajoExtra.setObservaciones(request.getObservaciones());
        
        // Datos de contacto
        trabajoExtra.setNombreEmpresa(request.getNombreEmpresa());
        trabajoExtra.setNombreSolicitante(request.getNombreSolicitante());
        trabajoExtra.setTelefono(request.getTelefono());
        trabajoExtra.setMail(request.getMail());
        trabajoExtra.setDireccionParticular(request.getDireccionParticular());
        
        // Dirección de obra
        trabajoExtra.setDireccionObraCalle(request.getDireccionObraCalle());
        trabajoExtra.setDireccionObraAltura(request.getDireccionObraAltura());
        trabajoExtra.setDireccionObraBarrio(request.getDireccionObraBarrio());
        trabajoExtra.setDireccionObraTorre(request.getDireccionObraTorre());
        trabajoExtra.setDireccionObraPiso(request.getDireccionObraPiso());
        trabajoExtra.setDireccionObraDepartamento(request.getDireccionObraDepartamento());
        trabajoExtra.setDireccionObraLocalidad(request.getDireccionObraLocalidad());
        trabajoExtra.setDireccionObraProvincia(request.getDireccionObraProvincia());
        trabajoExtra.setDireccionObraCodigoPostal(request.getDireccionObraCodigoPostal());
        
        // Datos del presupuesto
        trabajoExtra.setNombreObra(request.getNombreObra());
        trabajoExtra.setDescripcion(request.getDescripcion());
        trabajoExtra.setFechaProbableInicio(request.getFechaProbableInicio());
        trabajoExtra.setVencimiento(request.getVencimiento());
        trabajoExtra.setFechaCreacion(request.getFechaCreacion());
        trabajoExtra.setFechaEmision(request.getFechaEmision());
        trabajoExtra.setTiempoEstimadoTerminacion(request.getTiempoEstimadoTerminacion());
        trabajoExtra.setCalculoAutomaticoDiasHabiles(request.getCalculoAutomaticoDiasHabiles());
        
        // Control y estado
        if (request.getVersion() != null) {
            trabajoExtra.setVersion(request.getVersion());
        }
        trabajoExtra.setNumeroPresupuesto(request.getNumeroPresupuesto());
        if (request.getEstado() != null) {
            trabajoExtra.setEstado(request.getEstado());
        }
        
        // Totales
        if (request.getTotalPresupuesto() != null) {
            trabajoExtra.setTotalPresupuesto(request.getTotalPresupuesto());
        }
        if (request.getTotalHonorarios() != null) {
            trabajoExtra.setTotalHonorarios(request.getTotalHonorarios());
        }
        if (request.getTotalMayoresCostos() != null) {
            trabajoExtra.setTotalMayoresCostos(request.getTotalMayoresCostos());
        }
        if (request.getTotalFinal() != null) {
            trabajoExtra.setTotalFinal(request.getTotalFinal());
        }
        if (request.getMontoTotal() != null) {
            trabajoExtra.setMontoTotal(request.getMontoTotal());
        }
        if (request.getEsTrabajoExtra() != null) {
            trabajoExtra.setEsTrabajoExtra(request.getEsTrabajoExtra());
        }
        
        // ============================================================================
        // Aplicar configuración de honorarios (SIEMPRE usar campos planos)
        // ============================================================================
        log.info("💰 Aplicando honorarios desde campos planos del request");
        trabajoExtra.setHonorariosAplicarATodos(request.getHonorariosAplicarATodos());
        trabajoExtra.setHonorariosValorGeneral(request.getHonorariosValorGeneral());
        trabajoExtra.setHonorariosTipoGeneral(request.getHonorariosTipoGeneral());
        trabajoExtra.setHonorariosJornalesActivo(request.getHonorariosJornalesActivo());
        trabajoExtra.setHonorariosJornalesValor(request.getHonorariosJornalesValor());
        trabajoExtra.setHonorariosJornalesTipo(request.getHonorariosJornalesTipo());
        trabajoExtra.setHonorariosMaterialesActivo(request.getHonorariosMaterialesActivo());
        trabajoExtra.setHonorariosMaterialesValor(request.getHonorariosMaterialesValor());
        trabajoExtra.setHonorariosMaterialesTipo(request.getHonorariosMaterialesTipo());
        trabajoExtra.setHonorariosProfesionalesActivo(request.getHonorariosProfesionalesActivo());
        trabajoExtra.setHonorariosProfesionalesValor(request.getHonorariosProfesionalesValor());
        trabajoExtra.setHonorariosProfesionalesTipo(request.getHonorariosProfesionalesTipo());
        trabajoExtra.setHonorariosOtrosCostosActivo(request.getHonorariosOtrosCostosActivo());
        trabajoExtra.setHonorariosOtrosCostosValor(request.getHonorariosOtrosCostosValor());
        trabajoExtra.setHonorariosOtrosCostosTipo(request.getHonorariosOtrosCostosTipo());
        trabajoExtra.setHonorariosConfiguracionPresupuestoActivo(request.getHonorariosConfiguracionPresupuestoActivo());
        trabajoExtra.setHonorariosConfiguracionPresupuestoValor(request.getHonorariosConfiguracionPresupuestoValor());
        trabajoExtra.setHonorariosConfiguracionPresupuestoTipo(request.getHonorariosConfiguracionPresupuestoTipo());
        
        // ============================================================================
        // Aplicar configuración de mayores costos (SIEMPRE usar campos planos)
        // ============================================================================
        log.info("📊 Aplicando mayores costos desde campos planos del request (actualizar)");
        trabajoExtra.setMayoresCostosAplicarValorGeneral(request.getMayoresCostosAplicarValorGeneral());
        trabajoExtra.setMayoresCostosValorGeneral(request.getMayoresCostosValorGeneral());
        trabajoExtra.setMayoresCostosTipoGeneral(request.getMayoresCostosTipoGeneral());
        trabajoExtra.setMayoresCostosJornalesActivo(request.getMayoresCostosJornalesActivo());
        trabajoExtra.setMayoresCostosJornalesValor(request.getMayoresCostosJornalesValor());
        trabajoExtra.setMayoresCostosJornalesTipo(request.getMayoresCostosJornalesTipo());
        trabajoExtra.setMayoresCostosMaterialesActivo(request.getMayoresCostosMaterialesActivo());
        trabajoExtra.setMayoresCostosMaterialesValor(request.getMayoresCostosMaterialesValor());
        trabajoExtra.setMayoresCostosMaterialesTipo(request.getMayoresCostosMaterialesTipo());
        trabajoExtra.setMayoresCostosProfesionalesActivo(request.getMayoresCostosProfesionalesActivo());
        trabajoExtra.setMayoresCostosProfesionalesValor(request.getMayoresCostosProfesionalesValor());
        trabajoExtra.setMayoresCostosProfesionalesTipo(request.getMayoresCostosProfesionalesTipo());
        trabajoExtra.setMayoresCostosOtrosCostosActivo(request.getMayoresCostosOtrosCostosActivo());
        trabajoExtra.setMayoresCostosOtrosCostosValor(request.getMayoresCostosOtrosCostosValor());
        trabajoExtra.setMayoresCostosOtrosCostosTipo(request.getMayoresCostosOtrosCostosTipo());
        trabajoExtra.setMayoresCostosHonorariosActivo(request.getMayoresCostosHonorariosActivo());
        trabajoExtra.setMayoresCostosHonorariosValor(request.getMayoresCostosHonorariosValor());
        trabajoExtra.setMayoresCostosHonorariosTipo(request.getMayoresCostosHonorariosTipo());
        trabajoExtra.setMayoresCostosConfiguracionPresupuestoActivo(request.getMayoresCostosConfiguracionPresupuestoActivo());
        trabajoExtra.setMayoresCostosConfiguracionPresupuestoValor(request.getMayoresCostosConfiguracionPresupuestoValor());
        trabajoExtra.setMayoresCostosConfiguracionPresupuestoTipo(request.getMayoresCostosConfiguracionPresupuestoTipo());
        
        trabajoExtra.setMayoresCostosGeneralImportado(request.getMayoresCostosGeneralImportado());
        trabajoExtra.setMayoresCostosRubroImportado(request.getMayoresCostosRubroImportado());
        trabajoExtra.setMayoresCostosNombreRubroImportado(request.getMayoresCostosNombreRubroImportado());
        trabajoExtra.setMayoresCostosExplicacion(request.getMayoresCostosExplicacion());

        // Log para verificar que se guardaron los valores
        log.info("✅ Verificación post-aplicación honorarios: aplicarATodos={}, valorGeneral={}, jornalesActivo={}, jornalesValor={}", 
                 trabajoExtra.getHonorariosAplicarATodos(), 
                 trabajoExtra.getHonorariosValorGeneral(),
                 trabajoExtra.getHonorariosJornalesActivo(),
                 trabajoExtra.getHonorariosJornalesValor());
        
        TrabajoExtra actualizado = trabajoExtraRepository.save(trabajoExtra);
        
        // Recrear días (eliminamos los anteriores y creamos los nuevos)
        trabajoExtraDiaRepository.deleteByTrabajoExtraId(id);
        if (request.getDias() != null && !request.getDias().isEmpty()) {
            guardarDias(id, request.getDias());
        }
        
        // Recrear profesionales
        trabajoExtroProfesionalRepository.deleteByTrabajoExtraId(id);
        if (request.getProfesionales() != null && !request.getProfesionales().isEmpty()) {
            guardarProfesionales(id, request.getProfesionales());
        }
        
        // Recrear tareas
        trabajoExtraTareaRepository.deleteByTrabajoExtraId(id);
        if (request.getTareas() != null && !request.getTareas().isEmpty()) {
            guardarTareas(id, request.getTareas());
        }
        
        // Aplicar honorario de dirección
        if (request.getHonorarioDireccionPorcentaje() != null) {
            actualizado.setHonorarioDireccionPorcentaje(request.getHonorarioDireccionPorcentaje());
        }
        if (request.getHonorarioDireccionImporte() != null) {
            actualizado.setHonorarioDireccionImporte(request.getHonorarioDireccionImporte());
        }
        if (request.getHonorarioDireccionValorFijo() != null) {
            actualizado.setHonorarioDireccionValorFijo(request.getHonorarioDireccionValorFijo());
        }
        
        // Recrear items calculadora (mismo flujo que presupuestoNoCliente)
        log.info("📋 Procesando items de calculadora para trabajo extra {}", id);
        if (request.getItemsCalculadora() != null) {
            log.info("✅ Guardando {} items calculadora", request.getItemsCalculadora().size());
            itemCalculadoraHelper.guardarItems(id, empresaId, request.getItemsCalculadora());
            
            // Recalcular totales
            TotalesTrabajoExtra totales = itemCalculadoraHelper.calcularTotales(id, actualizado);
            actualizado.setTotalPresupuesto(totales.getTotalPresupuesto());
            actualizado.setTotalHonorarios(totales.getTotalHonorarios());
            actualizado.setTotalHonorariosCalculado(totales.getTotalHonorariosCalculado());
            actualizado.setTotalMayoresCostos(totales.getTotalMayoresCostos());
            actualizado.setTotalPresupuestoConHonorarios(totales.getTotalPresupuestoConHonorarios());
            actualizado.setTotalFinal(totales.getTotalFinal());
            actualizado.setMontoTotal(totales.getMontoTotal());
            actualizado.setTotalMateriales(totales.getTotalMateriales());
            actualizado.setTotalProfesionales(totales.getTotalProfesionales());
            actualizado.setTotalGeneral(totales.getTotalGeneral());
        } else {
            log.info("⚠️ No se recibieron items calculadora en el request");
        }
        
        trabajoExtraRepository.save(actualizado);
        
        log.info("Trabajo extra {} actualizado exitosamente", id);
        
        return mapearEntityAResponse(trabajoExtraRepository.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public TrabajoExtraResponseDTO actualizarParcial(Long empresaId, Long id, TrabajoExtraRequestDTO request) {
        log.info("Actualizando parcialmente trabajo extra {} para empresa {}", id, empresaId);
        
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findByIdAndEmpresaId(id, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Trabajo extra no encontrado con ID: " + id + " o no pertenece a la empresa"));
        
        // Actualizar solo los campos que vienen en el request (no nullos)
        if (request.getObraId() != null) {
            Obra obra = obraRepository.findByIdAndEmpresaId(request.getObraId(), empresaId)
                    .orElseThrow(() -> new RuntimeException(
                        "La obra con ID " + request.getObraId() + " no existe o no pertenece a la empresa " + empresaId));
            trabajoExtra.setObraId(request.getObraId());
            if (obra.getClienteId() != null) {
                trabajoExtra.setClienteId(obra.getClienteId());
            }
        }
        
        if (request.getNombre() != null) trabajoExtra.setNombre(request.getNombre());
        if (request.getObservaciones() != null) trabajoExtra.setObservaciones(request.getObservaciones());
        
        // Datos de contacto
        if (request.getNombreEmpresa() != null) trabajoExtra.setNombreEmpresa(request.getNombreEmpresa());
        if (request.getNombreSolicitante() != null) trabajoExtra.setNombreSolicitante(request.getNombreSolicitante());
        if (request.getTelefono() != null) trabajoExtra.setTelefono(request.getTelefono());
        if (request.getMail() != null) trabajoExtra.setMail(request.getMail());
        if (request.getDireccionParticular() != null) trabajoExtra.setDireccionParticular(request.getDireccionParticular());
        
        // Dirección de obra
        if (request.getDireccionObraCalle() != null) trabajoExtra.setDireccionObraCalle(request.getDireccionObraCalle());
        if (request.getDireccionObraAltura() != null) trabajoExtra.setDireccionObraAltura(request.getDireccionObraAltura());
        if (request.getDireccionObraBarrio() != null) trabajoExtra.setDireccionObraBarrio(request.getDireccionObraBarrio());
        if (request.getDireccionObraTorre() != null) trabajoExtra.setDireccionObraTorre(request.getDireccionObraTorre());
        if (request.getDireccionObraPiso() != null) trabajoExtra.setDireccionObraPiso(request.getDireccionObraPiso());
        if (request.getDireccionObraDepartamento() != null) trabajoExtra.setDireccionObraDepartamento(request.getDireccionObraDepartamento());
        if (request.getDireccionObraLocalidad() != null) trabajoExtra.setDireccionObraLocalidad(request.getDireccionObraLocalidad());
        if (request.getDireccionObraProvincia() != null) trabajoExtra.setDireccionObraProvincia(request.getDireccionObraProvincia());
        if (request.getDireccionObraCodigoPostal() != null) trabajoExtra.setDireccionObraCodigoPostal(request.getDireccionObraCodigoPostal());
        
        // Datos del presupuesto
        if (request.getNombreObra() != null) trabajoExtra.setNombreObra(request.getNombreObra());
        if (request.getDescripcion() != null) trabajoExtra.setDescripcion(request.getDescripcion());
        if (request.getFechaProbableInicio() != null) trabajoExtra.setFechaProbableInicio(request.getFechaProbableInicio());
        if (request.getVencimiento() != null) trabajoExtra.setVencimiento(request.getVencimiento());
        if (request.getFechaCreacion() != null) trabajoExtra.setFechaCreacion(request.getFechaCreacion());
        if (request.getFechaEmision() != null) trabajoExtra.setFechaEmision(request.getFechaEmision());
        if (request.getTiempoEstimadoTerminacion() != null) trabajoExtra.setTiempoEstimadoTerminacion(request.getTiempoEstimadoTerminacion());
        if (request.getCalculoAutomaticoDiasHabiles() != null) trabajoExtra.setCalculoAutomaticoDiasHabiles(request.getCalculoAutomaticoDiasHabiles());
        
        // Control y estado
        if (request.getVersion() != null) trabajoExtra.setVersion(request.getVersion());
        if (request.getNumeroPresupuesto() != null) trabajoExtra.setNumeroPresupuesto(request.getNumeroPresupuesto());
        if (request.getEstado() != null) trabajoExtra.setEstado(request.getEstado());
        
        // Totales
        if (request.getTotalPresupuesto() != null) trabajoExtra.setTotalPresupuesto(request.getTotalPresupuesto());
        if (request.getTotalHonorarios() != null) trabajoExtra.setTotalHonorarios(request.getTotalHonorarios());
        if (request.getTotalMayoresCostos() != null) trabajoExtra.setTotalMayoresCostos(request.getTotalMayoresCostos());
        if (request.getTotalFinal() != null) trabajoExtra.setTotalFinal(request.getTotalFinal());
        if (request.getMontoTotal() != null) trabajoExtra.setMontoTotal(request.getMontoTotal());
        if (request.getEsTrabajoExtra() != null) trabajoExtra.setEsTrabajoExtra(request.getEsTrabajoExtra());
        
        TrabajoExtra actualizado = trabajoExtraRepository.save(trabajoExtra);
        
        // Actualizar días solo si vienen en el request
        if (request.getDias() != null) {
            trabajoExtraDiaRepository.deleteByTrabajoExtraId(id);
            if (!request.getDias().isEmpty()) {
                guardarDias(id, request.getDias());
            }
        }
        
        // Actualizar profesionales solo si vienen en el request
        if (request.getProfesionales() != null) {
            trabajoExtroProfesionalRepository.deleteByTrabajoExtraId(id);
            if (!request.getProfesionales().isEmpty()) {
                guardarProfesionales(id, request.getProfesionales());
            }
        }
        
        // Actualizar tareas solo si vienen en el request
        if (request.getTareas() != null) {
            trabajoExtraTareaRepository.deleteByTrabajoExtraId(id);
            if (!request.getTareas().isEmpty()) {
                guardarTareas(id, request.getTareas());
            }
        }
        
        log.info("Trabajo extra {} actualizado parcialmente exitosamente", id);
        
        return mapearEntityAResponse(trabajoExtraRepository.findById(id).orElseThrow());
    }

    @Override
    @Transactional
    public void eliminar(Long empresaId, Long id) {
        log.info("Eliminando trabajo extra {} para empresa {}", id, empresaId);
        
        if (!trabajoExtraRepository.existsByIdAndEmpresaId(id, empresaId)) {
            throw new RuntimeException(
                "Trabajo extra no encontrado con ID: " + id + " o no pertenece a la empresa");
        }
        
        // El cascade se encarga de eliminar días, profesionales y tareas automáticamente
        trabajoExtraRepository.deleteById(id);
        log.info("Trabajo extra {} eliminado exitosamente", id);
    }

    @Override
    @Transactional
    public void eliminarProfesional(Long empresaId, Long profesionalId) {
        log.info("🗑️ Eliminando profesional {} de asignación trabajo extra (empresaId: {})", profesionalId, empresaId);
        
        itemCalculadoraHelper.eliminarProfesional(empresaId, profesionalId);
        
        log.info("✅ Profesional {} eliminado exitosamente", profesionalId);
    }

    @Override
    @Transactional
    public void eliminarMaterial(Long empresaId, Long materialId) {
        log.info("🗑️ Eliminando material {} de asignación trabajo extra (empresaId: {})", materialId, empresaId);
        
        itemCalculadoraHelper.eliminarMaterial(empresaId, materialId);
        
        log.info("✅ Material {} eliminado exitosamente", materialId);
    }

    @Override
    @Transactional
    public void eliminarGastoGeneral(Long empresaId, Long gastoId) {
        log.info("🗑️ Eliminando gasto general {} de asignación trabajo extra (empresaId: {})", gastoId, empresaId);
        
        itemCalculadoraHelper.eliminarGastoGeneral(empresaId, gastoId);
        
        log.info("✅ Gasto general {} eliminado exitosamente", gastoId);
    }

    // ==================== MÉTODOS PRIVADOS ====================

    private void guardarDias(Long trabajoExtraId, List<LocalDate> dias) {
        List<TrabajoExtraDia> diasEntities = dias.stream()
                .map(fecha -> TrabajoExtraDia.builder()
                        .trabajoExtraId(trabajoExtraId)
                        .fecha(fecha)
                        .build())
                .collect(Collectors.toList());
        
        trabajoExtraDiaRepository.saveAll(diasEntities);
        log.debug("Guardados {} días para trabajo extra {}", diasEntities.size(), trabajoExtraId);
    }

    private void guardarProfesionales(Long trabajoExtraId, List<TrabajoExtraProfesionalDTO> profesionales) {
        List<TrabajoExtroProfesional> profesionalesEntities = profesionales.stream()
                .map(dto -> TrabajoExtroProfesional.builder()
                        .trabajoExtraId(trabajoExtraId)
                        .profesionalId(dto.getProfesionalId())
                        .nombre(dto.getNombre())
                        .especialidad(dto.getEspecialidad())
                        .tipo(dto.getTipo())
                        .importe(dto.getImporte() != null ? dto.getImporte() : java.math.BigDecimal.ZERO)
                        .build())
                .collect(Collectors.toList());
        
        trabajoExtroProfesionalRepository.saveAll(profesionalesEntities);
        log.debug("Guardados {} profesionales para trabajo extra {}", profesionalesEntities.size(), trabajoExtraId);
    }

    private void guardarTareas(Long trabajoExtraId, List<TrabajoExtraTareaDTO> tareas) {
        List<TrabajoExtraTarea> tareasEntities = tareas.stream()
                .map(dto -> TrabajoExtraTarea.builder()
                        .trabajoExtraId(trabajoExtraId)
                        .descripcion(dto.getDescripcion())
                        .estado(dto.getEstado())
                        .importe(dto.getImporte())
                        .profesionalesIndices(dto.getProfesionalesIndices() != null ? 
                                new ArrayList<>(dto.getProfesionalesIndices()) : new ArrayList<>())
                        .build())
                .collect(Collectors.toList());
        
        trabajoExtraTareaRepository.saveAll(tareasEntities);
        log.debug("Guardadas {} tareas para trabajo extra {}", tareasEntities.size(), trabajoExtraId);
    }

    private void validarObraPerteneciaEmpresa(Long obraId, Long empresaId) {
        boolean existe = obraRepository.findByIdAndEmpresaId(obraId, empresaId).isPresent();
        
        if (!existe) {
            throw new RuntimeException(
                "La obra con ID " + obraId + " no existe o no pertenece a la empresa " + empresaId);
        }
    }

    private TrabajoExtraResponseDTO mapearEntityAResponse(TrabajoExtra entity) {
        // Obtener días
        List<LocalDate> dias = trabajoExtraDiaRepository.findByTrabajoExtraId(entity.getId())
                .stream()
                .map(TrabajoExtraDia::getFecha)
                .sorted()
                .collect(Collectors.toList());
        
        // Obtener profesionales
        List<TrabajoExtraProfesionalResponseDTO> profesionales = trabajoExtroProfesionalRepository.findByTrabajoExtraId(entity.getId())
                .stream()
                .map(prof -> TrabajoExtraProfesionalResponseDTO.builder()
                        .id(prof.getId())
                        .profesionalId(prof.getProfesionalId())
                        .nombre(prof.getNombre())
                        .especialidad(prof.getEspecialidad())
                        .tipo(prof.getTipo())
                        .importe(prof.getImporte())
                        .build())
                .collect(Collectors.toList());
        
        // Obtener tareas
        List<TrabajoExtraTareaResponseDTO> tareas = trabajoExtraTareaRepository.findByTrabajoExtraId(entity.getId())
                .stream()
                .map(tarea -> TrabajoExtraTareaResponseDTO.builder()
                        .id(tarea.getId())
                        .descripcion(tarea.getDescripcion())
                        .estado(tarea.getEstado())
                        .importe(tarea.getImporte())
                        .profesionalesAsignados(tarea.getProfesionalesIndices())
                        .build())
                .collect(Collectors.toList());
        
        return TrabajoExtraResponseDTO.builder()
                .id(entity.getId())
                .obraId(entity.getObraId())
                .clienteId(entity.getClienteId())
                .empresaId(entity.getEmpresaId())
                .nombre(entity.getNombre())
                .observaciones(entity.getObservaciones())
                // Datos de contacto
                .nombreEmpresa(entity.getNombreEmpresa())
                .nombreSolicitante(entity.getNombreSolicitante())
                .telefono(entity.getTelefono())
                .mail(entity.getMail())
                .direccionParticular(entity.getDireccionParticular())
                // Dirección de obra
                .direccionObraCalle(entity.getDireccionObraCalle())
                .direccionObraAltura(entity.getDireccionObraAltura())
                .direccionObraBarrio(entity.getDireccionObraBarrio())
                .direccionObraTorre(entity.getDireccionObraTorre())
                .direccionObraPiso(entity.getDireccionObraPiso())
                .direccionObraDepartamento(entity.getDireccionObraDepartamento())
                .direccionObraLocalidad(entity.getDireccionObraLocalidad())
                .direccionObraProvincia(entity.getDireccionObraProvincia())
                .direccionObraCodigoPostal(entity.getDireccionObraCodigoPostal())
                // Datos del presupuesto
                .nombreObra(entity.getNombreObra())
                .descripcion(entity.getDescripcion())
                .fechaProbableInicio(entity.getFechaProbableInicio())
                .vencimiento(entity.getVencimiento())
                .fechaCreacion(entity.getFechaCreacion())
                .fechaEmision(entity.getFechaEmision())
                .tiempoEstimadoTerminacion(entity.getTiempoEstimadoTerminacion())
                .calculoAutomaticoDiasHabiles(entity.getCalculoAutomaticoDiasHabiles())
                // Control y estado
                .version(entity.getVersion())
                .numeroPresupuesto(entity.getNumeroPresupuesto())
                .estado(entity.getEstado())
                // Totales
                .totalPresupuesto(entity.getTotalPresupuesto())
                .totalHonorarios(entity.getTotalHonorarios())
                .totalMayoresCostos(entity.getTotalMayoresCostos())
                .totalFinal(entity.getTotalFinal())
                .montoTotal(entity.getMontoTotal())
                .esTrabajoExtra(entity.getEsTrabajoExtra())
                // Listas relacionales
                .dias(dias)
                .profesionales(profesionales)
                .tareas(tareas)
                // NUEVO: Items calculadora y totales adicionales
                .itemsCalculadora(itemCalculadoraHelper.obtenerItems(entity.getId()))
                .totalHonorariosCalculado(entity.getTotalHonorariosCalculado())
                .totalPresupuestoConHonorarios(entity.getTotalPresupuestoConHonorarios())
                .totalMateriales(entity.getTotalMateriales())
                .totalProfesionales(entity.getTotalProfesionales())
                .totalGeneral(entity.getTotalGeneral())
                .honorarioDireccionPorcentaje(entity.getHonorarioDireccionPorcentaje())
                .honorarioDireccionImporte(entity.getHonorarioDireccionImporte())
                .honorarioDireccionValorFijo(entity.getHonorarioDireccionValorFijo())
                // Configuración de honorarios campos planos
                .honorariosAplicarATodos(entity.getHonorariosAplicarATodos())
                .honorariosValorGeneral(entity.getHonorariosValorGeneral())
                .honorariosTipoGeneral(entity.getHonorariosTipoGeneral())
                .honorariosJornalesActivo(entity.getHonorariosJornalesActivo())
                .honorariosJornalesValor(entity.getHonorariosJornalesValor())
                .honorariosJornalesTipo(entity.getHonorariosJornalesTipo())
                .honorariosMaterialesActivo(entity.getHonorariosMaterialesActivo())
                .honorariosMaterialesValor(entity.getHonorariosMaterialesValor())
                .honorariosMaterialesTipo(entity.getHonorariosMaterialesTipo())
                .honorariosProfesionalesActivo(entity.getHonorariosProfesionalesActivo())
                .honorariosProfesionalesValor(entity.getHonorariosProfesionalesValor())
                .honorariosProfesionalesTipo(entity.getHonorariosProfesionalesTipo())
                .honorariosOtrosCostosActivo(entity.getHonorariosOtrosCostosActivo())
                .honorariosOtrosCostosValor(entity.getHonorariosOtrosCostosValor())
                .honorariosOtrosCostosTipo(entity.getHonorariosOtrosCostosTipo())
                .honorariosConfiguracionPresupuestoActivo(entity.getHonorariosConfiguracionPresupuestoActivo())
                .honorariosConfiguracionPresupuestoValor(entity.getHonorariosConfiguracionPresupuestoValor())
                .honorariosConfiguracionPresupuestoTipo(entity.getHonorariosConfiguracionPresupuestoTipo())
                // Configuración de mayores costos campos planos
                .mayoresCostosAplicarValorGeneral(entity.getMayoresCostosAplicarValorGeneral())
                .mayoresCostosValorGeneral(entity.getMayoresCostosValorGeneral())
                .mayoresCostosTipoGeneral(entity.getMayoresCostosTipoGeneral())
                .mayoresCostosJornalesActivo(entity.getMayoresCostosJornalesActivo())
                .mayoresCostosJornalesValor(entity.getMayoresCostosJornalesValor())
                .mayoresCostosJornalesTipo(entity.getMayoresCostosJornalesTipo())
                .mayoresCostosMaterialesActivo(entity.getMayoresCostosMaterialesActivo())
                .mayoresCostosMaterialesValor(entity.getMayoresCostosMaterialesValor())
                .mayoresCostosMaterialesTipo(entity.getMayoresCostosMaterialesTipo())
                .mayoresCostosProfesionalesActivo(entity.getMayoresCostosProfesionalesActivo())
                .mayoresCostosProfesionalesValor(entity.getMayoresCostosProfesionalesValor())
                .mayoresCostosProfesionalesTipo(entity.getMayoresCostosProfesionalesTipo())
                .mayoresCostosOtrosCostosActivo(entity.getMayoresCostosOtrosCostosActivo())
                .mayoresCostosOtrosCostosValor(entity.getMayoresCostosOtrosCostosValor())
                .mayoresCostosOtrosCostosTipo(entity.getMayoresCostosOtrosCostosTipo())
                .mayoresCostosHonorariosActivo(entity.getMayoresCostosHonorariosActivo())
                .mayoresCostosHonorariosValor(entity.getMayoresCostosHonorariosValor())
                .mayoresCostosHonorariosTipo(entity.getMayoresCostosHonorariosTipo())
                .mayoresCostosConfiguracionPresupuestoActivo(entity.getMayoresCostosConfiguracionPresupuestoActivo())
                .mayoresCostosConfiguracionPresupuestoValor(entity.getMayoresCostosConfiguracionPresupuestoValor())
                .mayoresCostosConfiguracionPresupuestoTipo(entity.getMayoresCostosConfiguracionPresupuestoTipo())
                .mayoresCostosGeneralImportado(entity.getMayoresCostosGeneralImportado())
                .mayoresCostosRubroImportado(entity.getMayoresCostosRubroImportado())
                .mayoresCostosNombreRubroImportado(entity.getMayoresCostosNombreRubroImportado())
                .mayoresCostosExplicacion(entity.getMayoresCostosExplicacion())
                // Configuración de honorarios
                .honorarios(mapearHonorarios(entity))
                // Configuración de mayores costos
                .mayoresCostos(mapearMayoresCostos(entity))
                // Fechas de auditoría
                .fechaCreacionRegistro(entity.getCreatedAt())
                .fechaModificacionRegistro(entity.getUpdatedAt())
                .build();
    }

    // ============================================================================
    // MÉTODOS PRIVADOS - HONORARIOS Y MAYORES COSTOS
    // ============================================================================

    private HonorariosConfigDTO mapearHonorarios(TrabajoExtra entity) {
        log.info("💰 Mapeando honorarios desde entidad: aplicarATodos={}, valorGeneral={}, tipoGeneral={}", 
                 entity.getHonorariosAplicarATodos(), 
                 entity.getHonorariosValorGeneral(), 
                 entity.getHonorariosTipoGeneral());
        
        return HonorariosConfigDTO.builder()
                .aplicarATodos(entity.getHonorariosAplicarATodos())
                .valorGeneral(entity.getHonorariosValorGeneral())
                .tipoGeneral(entity.getHonorariosTipoGeneral())
                .jornalesActivo(entity.getHonorariosJornalesActivo())
                .jornalesValor(entity.getHonorariosJornalesValor())
                .jornalesTipo(entity.getHonorariosJornalesTipo())
                .materialesActivo(entity.getHonorariosMaterialesActivo())
                .materialesValor(entity.getHonorariosMaterialesValor())
                .materialesTipo(entity.getHonorariosMaterialesTipo())
                .profesionalesActivo(entity.getHonorariosProfesionalesActivo())
                .profesionalesValor(entity.getHonorariosProfesionalesValor())
                .profesionalesTipo(entity.getHonorariosProfesionalesTipo())
                .otrosCostosActivo(entity.getHonorariosOtrosCostosActivo())
                .otrosCostosValor(entity.getHonorariosOtrosCostosValor())
                .otrosCostosTipo(entity.getHonorariosOtrosCostosTipo())
                .configuracionPresupuestoActivo(entity.getHonorariosConfiguracionPresupuestoActivo())
                .configuracionPresupuestoValor(entity.getHonorariosConfiguracionPresupuestoValor())
                .configuracionPresupuestoTipo(entity.getHonorariosConfiguracionPresupuestoTipo())
                .build();
    }

    private MayoresCostosConfigDTO mapearMayoresCostos(TrabajoExtra entity) {
        log.info("📊 Mapeando mayores costos desde entidad: aplicarValorGeneral={}, valorGeneral={}, tipoGeneral={}", 
                 entity.getMayoresCostosAplicarValorGeneral(), 
                 entity.getMayoresCostosValorGeneral(), 
                 entity.getMayoresCostosTipoGeneral());
        
        return MayoresCostosConfigDTO.builder()
                .aplicarValorGeneral(entity.getMayoresCostosAplicarValorGeneral())
                .valorGeneral(entity.getMayoresCostosValorGeneral())
                .tipoGeneral(entity.getMayoresCostosTipoGeneral())
                .jornalesActivo(entity.getMayoresCostosJornalesActivo())
                .jornalesValor(entity.getMayoresCostosJornalesValor())
                .jornalesTipo(entity.getMayoresCostosJornalesTipo())
                .materialesActivo(entity.getMayoresCostosMaterialesActivo())
                .materialesValor(entity.getMayoresCostosMaterialesValor())
                .materialesTipo(entity.getMayoresCostosMaterialesTipo())
                .profesionalesActivo(entity.getMayoresCostosProfesionalesActivo())
                .profesionalesValor(entity.getMayoresCostosProfesionalesValor())
                .profesionalesTipo(entity.getMayoresCostosProfesionalesTipo())
                .otrosCostosActivo(entity.getMayoresCostosOtrosCostosActivo())
                .otrosCostosValor(entity.getMayoresCostosOtrosCostosValor())
                .otrosCostosTipo(entity.getMayoresCostosOtrosCostosTipo())
                .honorariosActivo(entity.getMayoresCostosHonorariosActivo())
                .honorariosValor(entity.getMayoresCostosHonorariosValor())
                .honorariosTipo(entity.getMayoresCostosHonorariosTipo())
                .configuracionPresupuestoActivo(entity.getMayoresCostosConfiguracionPresupuestoActivo())
                .configuracionPresupuestoValor(entity.getMayoresCostosConfiguracionPresupuestoValor())
                .configuracionPresupuestoTipo(entity.getMayoresCostosConfiguracionPresupuestoTipo())
                .generalImportado(entity.getMayoresCostosGeneralImportado())
                .rubroImportado(entity.getMayoresCostosRubroImportado())
                .nombreRubroImportado(entity.getMayoresCostosNombreRubroImportado())
                .explicacion(entity.getMayoresCostosExplicacion())
                .build();
    }

    private void aplicarHonorarios(TrabajoExtra trabajoExtra, HonorariosConfigDTO config) {
        log.info("💰 Aplicando configuración de honorarios: aplicarATodos={}, valorGeneral={}, tipoGeneral={}", 
                 config.getAplicarATodos(), config.getValorGeneral(), config.getTipoGeneral());
        
        // General
        trabajoExtra.setHonorariosAplicarATodos(config.getAplicarATodos());
        trabajoExtra.setHonorariosValorGeneral(config.getValorGeneral());
        trabajoExtra.setHonorariosTipoGeneral(config.getTipoGeneral());
        
        // Jornales
        trabajoExtra.setHonorariosJornalesActivo(config.getJornalesActivo());
        trabajoExtra.setHonorariosJornalesValor(config.getJornalesValor());
        trabajoExtra.setHonorariosJornalesTipo(config.getJornalesTipo());
        
        // Materiales
        trabajoExtra.setHonorariosMaterialesActivo(config.getMaterialesActivo());
        trabajoExtra.setHonorariosMaterialesValor(config.getMaterialesValor());
        trabajoExtra.setHonorariosMaterialesTipo(config.getMaterialesTipo());
        
        // Profesionales
        trabajoExtra.setHonorariosProfesionalesActivo(config.getProfesionalesActivo());
        trabajoExtra.setHonorariosProfesionalesValor(config.getProfesionalesValor());
        trabajoExtra.setHonorariosProfesionalesTipo(config.getProfesionalesTipo());
        
        // Otros Costos
        trabajoExtra.setHonorariosOtrosCostosActivo(config.getOtrosCostosActivo());
        trabajoExtra.setHonorariosOtrosCostosValor(config.getOtrosCostosValor());
        trabajoExtra.setHonorariosOtrosCostosTipo(config.getOtrosCostosTipo());
        
        // Configuración Presupuesto
        trabajoExtra.setHonorariosConfiguracionPresupuestoActivo(config.getConfiguracionPresupuestoActivo());
        trabajoExtra.setHonorariosConfiguracionPresupuestoValor(config.getConfiguracionPresupuestoValor());
        trabajoExtra.setHonorariosConfiguracionPresupuestoTipo(config.getConfiguracionPresupuestoTipo());
        
        log.info("✅ Configuración de honorarios aplicada correctamente");
    }

    private void aplicarMayoresCostos(TrabajoExtra trabajoExtra, MayoresCostosConfigDTO config) {
        // General
        trabajoExtra.setMayoresCostosAplicarValorGeneral(config.getAplicarValorGeneral());
        trabajoExtra.setMayoresCostosValorGeneral(config.getValorGeneral());
        trabajoExtra.setMayoresCostosTipoGeneral(config.getTipoGeneral());
        
        // Jornales
        trabajoExtra.setMayoresCostosJornalesActivo(config.getJornalesActivo());
        trabajoExtra.setMayoresCostosJornalesValor(config.getJornalesValor());
        trabajoExtra.setMayoresCostosJornalesTipo(config.getJornalesTipo());
        
        // Materiales
        trabajoExtra.setMayoresCostosMaterialesActivo(config.getMaterialesActivo());
        trabajoExtra.setMayoresCostosMaterialesValor(config.getMaterialesValor());
        trabajoExtra.setMayoresCostosMaterialesTipo(config.getMaterialesTipo());
        
        // Profesionales
        trabajoExtra.setMayoresCostosProfesionalesActivo(config.getProfesionalesActivo());
        trabajoExtra.setMayoresCostosProfesionalesValor(config.getProfesionalesValor());
        trabajoExtra.setMayoresCostosProfesionalesTipo(config.getProfesionalesTipo());
        
        // Otros Costos
        trabajoExtra.setMayoresCostosOtrosCostosActivo(config.getOtrosCostosActivo());
        trabajoExtra.setMayoresCostosOtrosCostosValor(config.getOtrosCostosValor());
        trabajoExtra.setMayoresCostosOtrosCostosTipo(config.getOtrosCostosTipo());
        
        // Honorarios
        trabajoExtra.setMayoresCostosHonorariosActivo(config.getHonorariosActivo());
        trabajoExtra.setMayoresCostosHonorariosValor(config.getHonorariosValor());
        trabajoExtra.setMayoresCostosHonorariosTipo(config.getHonorariosTipo());
        
        // Configuración Presupuesto
        trabajoExtra.setMayoresCostosConfiguracionPresupuestoActivo(config.getConfiguracionPresupuestoActivo());
        trabajoExtra.setMayoresCostosConfiguracionPresupuestoValor(config.getConfiguracionPresupuestoValor());
        trabajoExtra.setMayoresCostosConfiguracionPresupuestoTipo(config.getConfiguracionPresupuestoTipo());
        
        // Importado
        trabajoExtra.setMayoresCostosGeneralImportado(config.getGeneralImportado());
        trabajoExtra.setMayoresCostosRubroImportado(config.getRubroImportado());
        trabajoExtra.setMayoresCostosNombreRubroImportado(config.getNombreRubroImportado());
        trabajoExtra.setMayoresCostosExplicacion(config.getExplicacion());
    }

    @Override
    @Transactional
    public TrabajoExtraPdfResponseDTO guardarPdf(Long empresaId, Long trabajoExtraId, MultipartFile archivo, String generadoPor) {
        log.info("Guardando PDF para trabajo extra {} de empresa {}", trabajoExtraId, empresaId);
        
        // Validar que el trabajo extra exista y pertenezca a la empresa
        TrabajoExtra trabajoExtra = trabajoExtraRepository.findByIdAndEmpresaId(trabajoExtraId, empresaId)
                .orElseThrow(() -> new RuntimeException(
                    "Trabajo extra no encontrado con ID: " + trabajoExtraId + " o no pertenece a la empresa"));
        
        // Validar que sea un archivo PDF
        String contentType = archivo.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("El archivo debe ser un PDF. Tipo recibido: " + contentType);
        }
        
        try {
            // Crear entidad PDF
            TrabajoExtraPdf pdf = TrabajoExtraPdf.builder()
                    .trabajoExtraId(trabajoExtraId)
                    .empresaId(empresaId)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .contenidoPdf(archivo.getBytes())
                    .tamanioBytes(archivo.getSize())
                    .fechaGeneracion(LocalDateTime.now())
                    .generadoPor(generadoPor)
                    .versionTrabajoExtra(trabajoExtra.getVersion())
                    .incluyeHonorarios(false)
                    .incluyeConfiguracion(false)
                    .build();
            
            TrabajoExtraPdf guardado = trabajoExtraPdfRepository.save(pdf);
            log.info("PDF guardado exitosamente con ID: {}", guardado.getId());
            
            return TrabajoExtraPdfResponseDTO.builder()
                    .id(guardado.getId())
                    .trabajoExtraId(guardado.getTrabajoExtraId())
                    .nombreArchivo(guardado.getNombreArchivo())
                    .tamanioBytes(guardado.getTamanioBytes())
                    .fechaGeneracion(guardado.getFechaGeneracion())
                    .generadoPor(guardado.getGeneradoPor())
                    .versionTrabajoExtra(guardado.getVersionTrabajoExtra())
                    .incluyeHonorarios(guardado.getIncluyeHonorarios())
                    .incluyeConfiguracion(guardado.getIncluyeConfiguracion())
                    .build();
            
        } catch (IOException e) {
            log.error("Error al leer el archivo PDF: {}", e.getMessage());
            throw new RuntimeException("Error al procesar el archivo PDF: " + e.getMessage());
        }
    }
}
