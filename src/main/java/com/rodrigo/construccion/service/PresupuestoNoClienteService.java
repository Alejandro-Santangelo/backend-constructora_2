package com.rodrigo.construccion.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodrigo.construccion.enums.TipoPresupuesto;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.PresupuestoAuditoria;
import com.rodrigo.construccion.repository.*;
import org.springframework.beans.factory.annotation.Autowired;

import com.rodrigo.construccion.model.entity.PresupuestoNoCliente;
import com.rodrigo.construccion.model.entity.JornalCalculadora;
import com.rodrigo.construccion.model.entity.PresupuestoCostoInicial;
import com.rodrigo.construccion.model.entity.ItemCalculadoraPresupuesto;
import com.rodrigo.construccion.model.entity.MaterialCalculadora;
import com.rodrigo.construccion.model.entity.ProfesionalCalculadora;
import com.rodrigo.construccion.model.entity.PresupuestoGastoGeneral;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.Cliente;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.ProfesionalObra;
import com.rodrigo.construccion.model.entity.TrabajoAdicional;
import com.rodrigo.construccion.dto.request.PresupuestoNoClienteRequestDTO;
import com.rodrigo.construccion.dto.request.ItemCalculadoraPresupuestoDTO;
import com.rodrigo.construccion.dto.request.MaterialCalculadoraDTO;
import com.rodrigo.construccion.dto.request.JornalCalculadoraDTO;
import com.rodrigo.construccion.dto.request.ProfesionalCalculadoraDTO;
import com.rodrigo.construccion.dto.request.GastoGeneralDTO;
import com.rodrigo.construccion.dto.PresupuestoCostoInicialDTO;
import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.enums.PresupuestoEstado;

// IMPORTS PARA GASTOS GENERALES CON STOCK
import com.rodrigo.construccion.dto.response.GastoGeneralConStockResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Service
public class PresupuestoNoClienteService implements IPresupuestoNoClienteService {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PresupuestoAuditoriaService auditoriaService;

    @Autowired
    private StockMaterialService stockMaterialService;

    @Autowired
    private StockGastoGeneralService stockGastoGeneralService;

    @Autowired
    private PresupuestoObraSyncService presupuestoObraSyncService;

    private static final Logger log = LoggerFactory.getLogger(PresupuestoNoClienteService.class);

    // Getter para testing y debug
    public PresupuestoObraSyncService getPresupuestoObraSyncService() {
        return presupuestoObraSyncService;
    }

    private final PresupuestoNoClienteRepository repository;
    private final PresupuestoCostoInicialRepository costoInicialRepository;
    private final ItemCalculadoraPresupuestoRepository itemCalculadoraRepository;
    private final ProfesionalCalculadoraRepository profesionalCalculadoraRepository;
    private final MaterialCalculadoraRepository materialCalculadoraRepository;
    private final PresupuestoGastoGeneralRepository gastoGeneralRepository;
    private final EmpresaRepository empresaRepository;
    private final ObraRepository obraRepository;
    private final ClienteRepository clienteRepository;
    private final ProfesionalObraRepository profesionalObraRepository;
    private final ProfesionalRepository profesionalRepository;
    private final JornalCalculadoraRepository jornalCalculadoraRepository;
    private final PagoGastoGeneralObraRepository pagoGastoGeneralObraRepository;
    private final ObraMaterialRepository obraMaterialRepository;
    private final TrabajoAdicionalRepository trabajoAdicionalRepository;
    private final ProfesionalObraService profesionalObraService;

    public PresupuestoNoClienteService(
            PresupuestoNoClienteRepository repository,
            PresupuestoCostoInicialRepository costoInicialRepository,
            ItemCalculadoraPresupuestoRepository itemCalculadoraRepository,
            ProfesionalCalculadoraRepository profesionalCalculadoraRepository,
            MaterialCalculadoraRepository materialCalculadoraRepository,
            PresupuestoGastoGeneralRepository gastoGeneralRepository,
            EmpresaRepository empresaRepository,
            ObraRepository obraRepository,
            ClienteRepository clienteRepository,
            ProfesionalObraRepository profesionalObraRepository,
            ProfesionalRepository profesionalRepository,
            JornalCalculadoraRepository jornalCalculadoraRepository,
            PagoGastoGeneralObraRepository pagoGastoGeneralObraRepository,
            ObraMaterialRepository obraMaterialRepository,
            TrabajoAdicionalRepository trabajoAdicionalRepository,
            ProfesionalObraService profesionalObraService) {
        this.repository = repository;
        this.costoInicialRepository = costoInicialRepository;
        this.itemCalculadoraRepository = itemCalculadoraRepository;
        this.profesionalCalculadoraRepository = profesionalCalculadoraRepository;
        this.materialCalculadoraRepository = materialCalculadoraRepository;
        this.gastoGeneralRepository = gastoGeneralRepository;
        this.empresaRepository = empresaRepository;
        this.obraRepository = obraRepository;
        this.clienteRepository = clienteRepository;
        this.profesionalObraRepository = profesionalObraRepository;
        this.profesionalRepository = profesionalRepository;
        this.jornalCalculadoraRepository = jornalCalculadoraRepository;
        this.pagoGastoGeneralObraRepository = pagoGastoGeneralObraRepository;
        this.obraMaterialRepository = obraMaterialRepository;
        this.trabajoAdicionalRepository = trabajoAdicionalRepository;
        this.profesionalObraService = profesionalObraService;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoNoCliente> buscarPorDireccionObra (String calle, String altura, String piso, String departamento) {
        return repository.findByDireccionObra(calle, altura, piso, departamento);
    }

    /**
     * Obtiene todos los presupuestos de una obra específica
     * Ordenados por versión descendente (versión más reciente primero)
     */
    @Override
    @Transactional(readOnly = true)
    public List<PresupuestoNoCliente> findAllByObraId(Long obraId) {
        List<PresupuestoNoCliente> presupuestos = repository.findByObra_IdOrderByNumeroVersionDesc(obraId);
        presupuestos.forEach(p -> {
            if (p.getItemsCalculadora() != null) p.getItemsCalculadora().size();
            // ELIMINADO: jornales legacy ya no existen
            // if (p.getJornales() != null) p.getJornales().size();
            // NO llamar a calcularCamposCalculados aquí
        });
        return presupuestos;
    }

    public List<PresupuestoNoCliente> listarTodos() {
        List<PresupuestoNoCliente> presupuestos = repository.findAll();
        // Calcular campos calculados para cada presupuesto
        presupuestos.forEach(p -> {
            // Forzar carga de items de calculadora
            p.getItemsCalculadora().size();
            // NO llamar a calcularCamposCalculados aquí
        });
        return presupuestos;
    }

    /**
     * Obtiene todos los presupuestos de una empresa específica
     */
    @Transactional(readOnly = true)
    public List<PresupuestoNoCliente> findAllByEmpresaId(Long empresaId) {
        log.info("🔍 Buscando presupuestos para empresaId: {}", empresaId);

        try {
            // El filtro de Hibernate automáticamente filtra por empresaId
            // Solo necesitamos llamar a findAll() y el filtro se aplica automáticamente
            List<PresupuestoNoCliente> presupuestos = repository.findAll();

            log.info("📊 Encontrados {} presupuestos para empresaId {}", presupuestos.size(), empresaId);

            // Calcular campos calculados y enriquecer profesionalObraId si aplica
            presupuestos.forEach(p -> {
                try {
                    // Forzar carga de items de calculadora dentro de transacción
                    if (p.getItemsCalculadora() != null) {
                        p.getItemsCalculadora().size();
                    }
                    // LEGACY: Profesionales, Materiales y Jornales ya no existen como colecciones
                    // Ahora todo está en items_calculadora_presupuesto

                    // NO llamar a calcularCamposCalculados aquí

                    // Si está APROBADO, enriquecer profesionalObraId
                    if (p.getEstado() == PresupuestoEstado.APROBADO && p.getObra() != null) {
                        enriquecerProfesionalesConObraId(p);
                    }
                } catch (Exception e) {
                    log.error("❌ Error procesando presupuesto ID {}: {}", p.getId(), e.getMessage(), e);
                    // Continuar con el siguiente presupuesto sin romper el listado completo
                }
            });

            return presupuestos;
        } catch (Exception e) {
            log.error("❌ Error crítico al buscar presupuestos para empresaId {}: {}", empresaId, e.getMessage(), e);
            // Retornar lista vacía en caso de error crítico para no romper el frontend
            return new ArrayList<>();
        }
    }

    @Transactional
    public PresupuestoNoCliente crear(PresupuestoNoClienteRequestDTO dto) {
        log.info("🔍 DEBUG CREAR - honorariosConfiguracionPresupuesto recibido: activo={}, tipo={}, valor={}",
                dto.getHonorariosConfiguracionPresupuestoActivo(),
                dto.getHonorariosConfiguracionPresupuestoTipo(),
                dto.getHonorariosConfiguracionPresupuestoValor());

        log.info("📋 CREAR PRESUPUESTO - idEmpresa: {}, idCliente: {}, idObra: {}",
                dto.getIdEmpresa(), dto.getIdCliente(), dto.getIdObra());

        PresupuestoNoCliente pnc = new PresupuestoNoCliente();
        LocalDate ahora = LocalDate.now();
        pnc.setFechaEmision(ahora);
        pnc.setFechaCreacion(dto.getFechaCreacion() != null ? dto.getFechaCreacion() : ahora);
        pnc.setNombreObra(dto.getNombreObra());
        // ELIMINADO: esPresupuestoTrabajoExtra ahora se setea automáticamente según TipoPresupuesto en configurarPresupuestoPorTipo()

        // Empresa
        if (dto.getIdEmpresa() == null) {
            throw new IllegalArgumentException("Debe especificar idEmpresa (o enviar X-Empresa-Id en el request)");
        }
        Empresa empresa = empresaRepository.findById(dto.getIdEmpresa()).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
        pnc.setEmpresa(empresa);

        // Cliente (asociación opcional)
        if (dto.getIdCliente() != null) {
            log.info("🔗 Vinculando presupuesto a cliente ID: {}", dto.getIdCliente());
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + dto.getIdCliente()));

            // Validar que el cliente pertenece a la empresa
            boolean perteneceAEmpresa = cliente.getEmpresas().stream()
                    .anyMatch(e -> e.getId().equals(dto.getIdEmpresa()));

            if (!perteneceAEmpresa) {
                throw new IllegalArgumentException("El cliente no pertenece a esta empresa");
            }

            pnc.setCliente(cliente);
            log.info("✅ Presupuesto vinculado al cliente ID: {} ({})",
                    cliente.getId(),
                    cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante());
        } else {
            log.info("⚠️ idCliente es NULL - el presupuesto NO tendrá cliente vinculado hasta que se apruebe");
        }

        // Obra (asociación nueva) - IMPORTANTE para trabajos extra
        if (dto.getIdObra() != null) {
            log.info("🏗️ Asociando presupuesto a obra ID: {}", dto.getIdObra());
            Obra obra = obraRepository.findById(dto.getIdObra())
                    .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada con ID: " + dto.getIdObra()));
            pnc.setObra(obra);
            log.info("✅ Presupuesto vinculado a obra ID: {} ({})", obra.getId(), obra.getNombre());
        } else {
            log.info("⚠️ idObra es NULL - el presupuesto NO tendrá obra vinculada");
        }

        // TrabajoAdicional (asociación nueva) - NUEVA FUNCIONALIDAD para TAREA_LEVE
        if (dto.getTrabajoAdicionalId() != null) {
            log.info("🔧 Asociando presupuesto TAREA_LEVE a TrabajoAdicional ID: {}", dto.getTrabajoAdicionalId());
            TrabajoAdicional trabajoAdicional = trabajoAdicionalRepository.findById(dto.getTrabajoAdicionalId())
                    .orElseThrow(() -> new IllegalArgumentException("TrabajoAdicional no encontrado con ID: " + dto.getTrabajoAdicionalId()));
            pnc.setTrabajoAdicional(trabajoAdicional);
            
            // ========== HERENCIA AUTOMÁTICA DE DATOS DE CONTEXTO ==========
            // El presupuesto TAREA_LEVE es PROPIO (independiente), pero hereda datos de contexto
            log.info("📋 Heredando datos de contexto desde TrabajoAdicional...");
            
            // 1. Heredar empresa (ya está configurado más arriba, verificar que coincida)
            if (dto.getIdEmpresa() != null && !dto.getIdEmpresa().equals(trabajoAdicional.getEmpresaId())) {
                log.warn("⚠️ empresaId del DTO ({}) difiere del TrabajoAdicional ({}). Se usará el del TrabajoAdicional.", 
                    dto.getIdEmpresa(), trabajoAdicional.getEmpresaId());
            }
            // La empresa ya fue seteada arriba, no cambiar
            
            // 2. Heredar obra padre del TrabajoAdicional (para obtener cliente y dirección)
            Obra obraPadre = obraRepository.findById(trabajoAdicional.getObraId())
                    .orElseThrow(() -> new IllegalArgumentException("Obra padre del TrabajoAdicional no encontrada: " + trabajoAdicional.getObraId()));
            
            // 3. Heredar cliente de la obra padre (si existe)
            if (obraPadre.getCliente() != null) {
                pnc.setCliente(obraPadre.getCliente());
                log.info("✅ Cliente heredado de obra padre: {} (ID: {})", 
                    obraPadre.getCliente().getNombre(), obraPadre.getCliente().getId());
            } else {
                log.info("ℹ️ Obra padre no tiene cliente asignado");
            }
            
            // 4. Heredar dirección de la obra padre (si no viene en el DTO)
            if (dto.getDireccionObraCalle() == null || dto.getDireccionObraCalle().trim().isEmpty()) {
                pnc.setDireccionObraCalle(obraPadre.getDireccionObraCalle());
                log.info("✅ Dirección (calle) heredada: {}", obraPadre.getDireccionObraCalle());
            }
            if (dto.getDireccionObraAltura() == null || dto.getDireccionObraAltura().trim().isEmpty()) {
                pnc.setDireccionObraAltura(obraPadre.getDireccionObraAltura());
                log.info("✅ Dirección (altura) heredada: {}", obraPadre.getDireccionObraAltura());
            }
            if (dto.getDireccionObraPiso() == null || dto.getDireccionObraPiso().trim().isEmpty()) {
                pnc.setDireccionObraPiso(obraPadre.getDireccionObraPiso());
            }
            if (dto.getDireccionObraDepartamento() == null || dto.getDireccionObraDepartamento().trim().isEmpty()) {
                pnc.setDireccionObraDepartamento(obraPadre.getDireccionObraDepartamento());
            }
            if (dto.getDireccionObraBarrio() == null || dto.getDireccionObraBarrio().trim().isEmpty()) {
                pnc.setDireccionObraBarrio(obraPadre.getDireccionObraBarrio());
            }
            if (dto.getDireccionObraTorre() == null || dto.getDireccionObraTorre().trim().isEmpty()) {
                pnc.setDireccionObraTorre(obraPadre.getDireccionObraTorre());
            }
            
            // 5. Heredar contacto de la obra padre (si no viene en el DTO)
            if (dto.getTelefono() == null || dto.getTelefono().trim().isEmpty()) {
                pnc.setTelefono(obraPadre.getTelefono());
                log.info("✅ Teléfono heredado: {}", obraPadre.getTelefono());
            }
            if (dto.getMail() == null || dto.getMail().trim().isEmpty()) {
                pnc.setMail(obraPadre.getMail());
                log.info("✅ Mail heredado: {}", obraPadre.getMail());
            }
            
            // 6. Heredar nombre_solicitante de la obra padre (si no viene en el DTO)
            if (dto.getNombreSolicitante() == null || dto.getNombreSolicitante().trim().isEmpty()) {
                pnc.setNombreSolicitante(obraPadre.getNombreSolicitante());
                log.info("✅ Nombre solicitante heredado: {}", obraPadre.getNombreSolicitante());
            }
            
            log.info("✅ Presupuesto TAREA_LEVE vinculado a TrabajoAdicional ID: {} ({})", 
                trabajoAdicional.getId(), trabajoAdicional.getNombre());
            log.info("📋 Datos heredados: empresa, cliente (si existe), dirección completa, teléfono, mail, nombre_solicitante");
            log.info("🆕 Datos propios del presupuesto: nombre='{}', fechas, contenido", dto.getNombreObra());
            
        } else {
            log.info("⚠️ trabajoAdicionalId es NULL - el presupuesto TAREA_LEVE NO estará vinculado a TrabajoAdicional");
        }

        // campos nulleables
        if (dto.getNombreSolicitante() != null && !dto.getNombreSolicitante().trim().isEmpty()) {
            pnc.setNombreSolicitante(dto.getNombreSolicitante());
        }
        pnc.setDireccionParticular(dto.getDireccionParticular());
        pnc.setDireccionObraCalle(dto.getDireccionObraCalle());
        pnc.setDireccionObraAltura(dto.getDireccionObraAltura());
        pnc.setDireccionObraPiso(dto.getDireccionObraPiso());
        pnc.setDireccionObraDepartamento(dto.getDireccionObraDepartamento());
        pnc.setDireccionObraBarrio(dto.getDireccionObraBarrio());
        pnc.setDireccionObraTorre(dto.getDireccionObraTorre());
        pnc.setDescripcion(dto.getDescripcion());
        pnc.setDescripcionDetallada(dto.getDescripcionDetallada());
        pnc.setObservacionesInternas(dto.getObservacionesInternas());
        pnc.setNotasAdicionales(dto.getNotasAdicionales());
        pnc.setEspecificacionesTecnicas(dto.getEspecificacionesTecnicas());
        pnc.setComentariosCliente(dto.getComentariosCliente());
        pnc.setRequisitosEspeciales(dto.getRequisitosEspeciales());
        pnc.setTiempoEstimadoTerminacion(dto.getTiempoEstimadoTerminacion());
        pnc.setFechaProbableInicio(dto.getFechaProbableInicio() != null ? dto.getFechaProbableInicio() : null);
        if (dto.getTelefono() != null && !dto.getTelefono().trim().isEmpty()) {
            pnc.setTelefono(dto.getTelefono());
        }
        if (dto.getMail() != null && !dto.getMail().trim().isEmpty()) {
            pnc.setMail(dto.getMail());
        }
        if (dto.getVencimiento() != null) {
            pnc.setVencimiento(dto.getVencimiento());
        } else {
            pnc.setVencimiento(ahora.plusDays(15));
        }
        pnc.setObservaciones(dto.getObservaciones());
        // ========== TIPO DE PRESUPUESTO ==========
        if (dto.getTipoPresupuesto() == null || dto.getTipoPresupuesto().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El campo 'tipoPresupuesto' es obligatorio. " +
                "Valores técnicos: TRADICIONAL, TRABAJO_DIARIO, TRABAJO_EXTRA, TAREA_LEVE. " +
                "Aliases semánticos aceptados: PRESUPUESTO_PRINCIPAL, PRESUPUESTO_TRABAJO_DIARIO, PRESUPUESTO_ADICIONAL_OBRA, PRESUPUESTO_TAREA_LEVE");
        }
        TipoPresupuesto tipoPresupuesto;
        try {
            tipoPresupuesto = TipoPresupuesto.fromString(dto.getTipoPresupuesto());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e.getMessage());
        }
        pnc.setTipoPresupuesto(tipoPresupuesto);
        log.info("📋 Creando presupuesto tipo: {} para empresa: {}", tipoPresupuesto, empresa.getId());
        
        // ========== VALIDACIONES POR TIPO ==========
        validarDatosPorTipoPresupuesto(dto, tipoPresupuesto);
        
        // ========== CONFIGURAR ESTADO Y PROPIEDADES SEGÚN TIPO ==========
        configurarPresupuestoPorTipo(pnc, dto, tipoPresupuesto);

        // numeroPresupuesto / numeroVersion
        if (dto.getNumeroPresupuesto() != null) {
            Long numero = dto.getNumeroPresupuesto();
            Integer maxVersion = repository.findMaxNumeroVersionByNumero(numero);
            if (maxVersion == null) maxVersion = 0;
            pnc.setNumeroPresupuesto(numero);
            pnc.setNumeroVersion(maxVersion + 1);
        } else {
            Long maxNumero = repository.findMaxNumeroPresupuesto();
            if (maxNumero == null) maxNumero = 0L;
            pnc.setNumeroPresupuesto(maxNumero + 1);
            pnc.setNumeroVersion(1);
        }

        // Calcular totales (sin JSON)
        double totalProf = 0.0;
        if (dto.getProfesionales() != null) {
            for (var prof : dto.getProfesionales()) {
                if (prof.getUnidadActiva() == null)
                    throw new IllegalArgumentException("unidadActiva es obligatoria para cada profesional");
                Double cantidad = prof.getCantidad() != null ? prof.getCantidad() : 0.0;
                Double importe = prof.getImportePorUnidad();
                if (importe == null) {
                    String u = prof.getUnidadActiva().toLowerCase();
                    if (u.contains("hora")) importe = prof.getImporteXHora();
                    else if (u.contains("dia") || u.contains("dí")) importe = prof.getImporteXDia();
                    else if (u.contains("semana")) importe = prof.getImporteXSemana();
                    else if (u.contains("mes")) importe = prof.getImporteXMes();
                    else if (u.contains("obra")) importe = prof.getImporteXObra();
                }
                importe = importe != null ? importe : 0.0;
                if (cantidad < 0 || importe < 0)
                    throw new IllegalArgumentException("cantidad e importe deben ser >= 0");
                totalProf += cantidad * importe;
            }
        }
        pnc.setTotalProfesionales(totalProf);

        double totalMat = 0.0;
        if (dto.getMaterialesList() != null) {
            for (var mat : dto.getMaterialesList()) {
                Double cantidad = mat.getCantidad() != null ? mat.getCantidad() : 0.0;
                Double precio = mat.getPrecioUnitario() != null ? mat.getPrecioUnitario() : 0.0;
                if (cantidad < 0 || precio < 0)
                    throw new IllegalArgumentException("cantidad y precioUnitario deben ser >= 0");
                totalMat += cantidad * precio;
            }
        }
        pnc.setTotalMateriales(totalMat);

        // Total otros costos removido - sistema antiguo eliminado
        double totalOtrosCostos = 0.0;

        // Honorarios de dirección de obra
        pnc.setHonorarioDireccionValorFijo(dto.getHonorarioDireccionValorFijo());
        pnc.setHonorarioDireccionPorcentaje(dto.getHonorarioDireccionPorcentaje());

        // Calcular importe de honorarios de dirección
        double honorarioDireccionImporte = 0.0;
        if (dto.getHonorarioDireccionPorcentaje() != null && dto.getHonorarioDireccionPorcentaje() > 0) {
            double baseCalculo = totalProf + totalMat + totalOtrosCostos;
            honorarioDireccionImporte = baseCalculo * (dto.getHonorarioDireccionPorcentaje() / 100.0);
        } else if (dto.getHonorarioDireccionValorFijo() != null && dto.getHonorarioDireccionValorFijo() > 0) {
            honorarioDireccionImporte = dto.getHonorarioDireccionValorFijo();
        }
        pnc.setHonorarioDireccionImporte(honorarioDireccionImporte);

        // ========== MAPEAR CONFIGURACIÓN DE HONORARIOS ==========
        pnc.setHonorariosAplicarATodos(dto.getHonorariosAplicarATodos());
        pnc.setHonorariosValorGeneral(dto.getHonorariosValorGeneral());
        pnc.setHonorariosTipoGeneral(dto.getHonorariosTipoGeneral());

        pnc.setHonorariosProfesionalesActivo(dto.getHonorariosProfesionalesActivo());
        pnc.setHonorariosProfesionalesTipo(dto.getHonorariosProfesionalesTipo());
        pnc.setHonorariosProfesionalesValor(dto.getHonorariosProfesionalesValor());

        pnc.setHonorariosMaterialesActivo(dto.getHonorariosMaterialesActivo());
        pnc.setHonorariosMaterialesTipo(dto.getHonorariosMaterialesTipo());
        pnc.setHonorariosMaterialesValor(dto.getHonorariosMaterialesValor());

        pnc.setHonorariosOtrosCostosActivo(dto.getHonorariosOtrosCostosActivo());
        pnc.setHonorariosOtrosCostosTipo(dto.getHonorariosOtrosCostosTipo());
        pnc.setHonorariosOtrosCostosValor(dto.getHonorariosOtrosCostosValor());

        // ⚡ MAPEAR HONORARIOS JORNALES (CAMPOS FALTANTES - FIX BUG)
        pnc.setHonorariosJornalesActivo(dto.getHonorariosJornalesActivo());
        pnc.setHonorariosJornalesTipo(dto.getHonorariosJornalesTipo());
        pnc.setHonorariosJornalesValor(dto.getHonorariosJornalesValor());

        pnc.setHonorariosConfiguracionPresupuestoActivo(dto.getHonorariosConfiguracionPresupuestoActivo());
        pnc.setHonorariosConfiguracionPresupuestoTipo(dto.getHonorariosConfiguracionPresupuestoTipo());
        pnc.setHonorariosConfiguracionPresupuestoValor(dto.getHonorariosConfiguracionPresupuestoValor());

        // Honorarios por Rubro (relación @OneToMany)
        if (dto.getHonorariosPorRubro() != null) {
            pnc.getHonorariosPorRubro().clear();
            pnc.getHonorariosPorRubro().addAll(mapearHonorariosPorRubroDTO(dto.getHonorariosPorRubro(), pnc));
        }

        // ========== MAPEAR CONFIGURACIÓN DE CÁLCULO DE DÍAS HÁBILES ==========
        pnc.setCalculoAutomaticoDiasHabiles(dto.getCalculoAutomaticoDiasHabiles());

        // ========== MAPEAR CONFIGURACIÓN DE MAYORES COSTOS ==========
        pnc.setMayoresCostosAplicarValorGeneral(dto.getMayoresCostosAplicarValorGeneral());
        pnc.setMayoresCostosValorGeneral(dto.getMayoresCostosValorGeneral());
        pnc.setMayoresCostosTipoGeneral(dto.getMayoresCostosTipoGeneral());
        pnc.setMayoresCostosGeneralImportado(dto.getMayoresCostosGeneralImportado());
        pnc.setMayoresCostosRubroImportado(dto.getMayoresCostosRubroImportado());
        pnc.setMayoresCostosNombreRubroImportado(dto.getMayoresCostosNombreRubroImportado());

        pnc.setMayoresCostosProfesionalesActivo(dto.getMayoresCostosProfesionalesActivo());
        pnc.setMayoresCostosProfesionalesTipo(dto.getMayoresCostosProfesionalesTipo());
        pnc.setMayoresCostosProfesionalesValor(dto.getMayoresCostosProfesionalesValor());

        pnc.setMayoresCostosMaterialesActivo(dto.getMayoresCostosMaterialesActivo());
        pnc.setMayoresCostosMaterialesTipo(dto.getMayoresCostosMaterialesTipo());
        pnc.setMayoresCostosMaterialesValor(dto.getMayoresCostosMaterialesValor());

        pnc.setMayoresCostosOtrosCostosActivo(dto.getMayoresCostosOtrosCostosActivo());
        pnc.setMayoresCostosOtrosCostosTipo(dto.getMayoresCostosOtrosCostosTipo());
        pnc.setMayoresCostosOtrosCostosValor(dto.getMayoresCostosOtrosCostosValor());

        pnc.setMayoresCostosConfiguracionPresupuestoActivo(dto.getMayoresCostosConfiguracionPresupuestoActivo());
        pnc.setMayoresCostosConfiguracionPresupuestoTipo(dto.getMayoresCostosConfiguracionPresupuestoTipo());
        pnc.setMayoresCostosConfiguracionPresupuestoValor(dto.getMayoresCostosConfiguracionPresupuestoValor());

        pnc.setMayoresCostosHonorariosActivo(dto.getMayoresCostosHonorariosActivo());
        pnc.setMayoresCostosHonorariosTipo(dto.getMayoresCostosHonorariosTipo());
        pnc.setMayoresCostosHonorariosValor(dto.getMayoresCostosHonorariosValor());

        // ⚡ MAPEAR MAYORES COSTOS JORNALES (CAMPOS FALTANTES - FIX BUG)
        pnc.setMayoresCostosJornalesActivo(dto.getMayoresCostosJornalesActivo());
        pnc.setMayoresCostosJornalesTipo(dto.getMayoresCostosJornalesTipo());
        pnc.setMayoresCostosJornalesValor(dto.getMayoresCostosJornalesValor());

        // Explicación/justificación INTERNA de mayores costos
        pnc.setMayoresCostosExplicacion(dto.getMayoresCostosExplicacion());

        // Mayores Costos por Rubro (relación @OneToMany)
        if (dto.getMayoresCostosPorRubro() != null) {
            pnc.getMayoresCostosPorRubro().clear();
            pnc.getMayoresCostosPorRubro().addAll(mapearMayoresCostosPorRubroDTO(dto.getMayoresCostosPorRubro(), pnc));
        }

        // ========== MAPEAR CONFIGURACIÓN DE DESCUENTOS (Modelo Relacional) ==========
        // Mapear explicación de descuentos
        pnc.setDescuentosExplicacion(dto.getDescuentosExplicacion());
        
        // Mapear descuentos sobre JORNALES
        pnc.setDescuentosJornalesActivo(dto.getDescuentosJornalesActivo());
        pnc.setDescuentosJornalesTipo(dto.getDescuentosJornalesTipo());
        if (dto.getDescuentosJornalesValor() != null) {
            pnc.setDescuentosJornalesValor(BigDecimal.valueOf(dto.getDescuentosJornalesValor()));
        }
        
        // Mapear descuentos sobre MATERIALES
        pnc.setDescuentosMaterialesActivo(dto.getDescuentosMaterialesActivo());
        pnc.setDescuentosMaterialesTipo(dto.getDescuentosMaterialesTipo());
        if (dto.getDescuentosMaterialesValor() != null) {
            pnc.setDescuentosMaterialesValor(BigDecimal.valueOf(dto.getDescuentosMaterialesValor()));
        }
        
        // Mapear descuentos sobre HONORARIOS
        pnc.setDescuentosHonorariosActivo(dto.getDescuentosHonorariosActivo());
        pnc.setDescuentosHonorariosTipo(dto.getDescuentosHonorariosTipo());
        if (dto.getDescuentosHonorariosValor() != null) {
            pnc.setDescuentosHonorariosValor(BigDecimal.valueOf(dto.getDescuentosHonorariosValor()));
        }
        
        // Mapear descuentos sobre MAYORES COSTOS
        pnc.setDescuentosMayoresCostosActivo(dto.getDescuentosMayoresCostosActivo());
        pnc.setDescuentosMayoresCostosTipo(dto.getDescuentosMayoresCostosTipo());
        if (dto.getDescuentosMayoresCostosValor() != null) {
            pnc.setDescuentosMayoresCostosValor(BigDecimal.valueOf(dto.getDescuentosMayoresCostosValor()));
        }
        
        // ========== MAPEAR SUB-TIPOS DE DESCUENTOS SOBRE HONORARIOS ==========
        // Descuentos sobre Honorarios de JORNALES
        pnc.setDescuentosHonorariosJornalesActivo(dto.getDescuentosHonorariosJornalesActivo());
        pnc.setDescuentosHonorariosJornalesTipo(dto.getDescuentosHonorariosJornalesTipo());
        if (dto.getDescuentosHonorariosJornalesValor() != null) {
            pnc.setDescuentosHonorariosJornalesValor(dto.getDescuentosHonorariosJornalesValor());
        }
        
        // Descuentos sobre Honorarios de PROFESIONALES
        pnc.setDescuentosHonorariosProfesionalesActivo(dto.getDescuentosHonorariosProfesionalesActivo());
        pnc.setDescuentosHonorariosProfesionalesTipo(dto.getDescuentosHonorariosProfesionalesTipo());
        if (dto.getDescuentosHonorariosProfesionalesValor() != null) {
            pnc.setDescuentosHonorariosProfesionalesValor(dto.getDescuentosHonorariosProfesionalesValor());
        }
        
        // Descuentos sobre Honorarios de MATERIALES
        pnc.setDescuentosHonorariosMaterialesActivo(dto.getDescuentosHonorariosMaterialesActivo());
        pnc.setDescuentosHonorariosMaterialesTipo(dto.getDescuentosHonorariosMaterialesTipo());
        if (dto.getDescuentosHonorariosMaterialesValor() != null) {
            pnc.setDescuentosHonorariosMaterialesValor(dto.getDescuentosHonorariosMaterialesValor());
        }
        
        // Descuentos sobre Honorarios de OTROS COSTOS
        pnc.setDescuentosHonorariosOtrosActivo(dto.getDescuentosHonorariosOtrosActivo());
        pnc.setDescuentosHonorariosOtrosTipo(dto.getDescuentosHonorariosOtrosTipo());
        if (dto.getDescuentosHonorariosOtrosValor() != null) {
            pnc.setDescuentosHonorariosOtrosValor(dto.getDescuentosHonorariosOtrosValor());
        }
        
        // Descuentos sobre Honorarios de GASTOS GENERALES
        pnc.setDescuentosHonorariosGastosGeneralesActivo(dto.getDescuentosHonorariosGastosGeneralesActivo());
        pnc.setDescuentosHonorariosGastosGeneralesTipo(dto.getDescuentosHonorariosGastosGeneralesTipo());
        if (dto.getDescuentosHonorariosGastosGeneralesValor() != null) {
            pnc.setDescuentosHonorariosGastosGeneralesValor(dto.getDescuentosHonorariosGastosGeneralesValor());
        }
        
        // Descuentos sobre Honorarios de CONFIGURACIÓN DE PRESUPUESTO
        pnc.setDescuentosHonorariosConfiguracionActivo(dto.getDescuentosHonorariosConfiguracionActivo());
        pnc.setDescuentosHonorariosConfiguracionTipo(dto.getDescuentosHonorariosConfiguracionTipo());
        if (dto.getDescuentosHonorariosConfiguracionValor() != null) {
            pnc.setDescuentosHonorariosConfiguracionValor(dto.getDescuentosHonorariosConfiguracionValor());
        }
        
        // Validar descuentos antes de guardar
        String errorValidacion = pnc.validarDescuentos();
        if (errorValidacion != null) {
            log.error("❌ Error en validación de descuentos: {}", errorValidacion);
            throw new IllegalArgumentException("Descuentos inválidos: " + errorValidacion);
        }

        // Descuentos por Rubro (relación @OneToMany)
        if (dto.getDescuentosPorRubro() != null) {
            pnc.getDescuentosPorRubro().clear();
            pnc.getDescuentosPorRubro().addAll(mapearDescuentosPorRubroDTO(dto.getDescuentosPorRubro(), pnc));
        }

        // total general: profesionales + materiales + otros costos + honorarios dirección
        pnc.setTotalGeneral(totalProf + totalMat + totalOtrosCostos + honorarioDireccionImporte);

        // ========== MAPEAR TOTALES ESPECÍFICOS DEL FRONTEND ==========
        // Estos campos vienen calculados desde el frontend y se guardan tal como llegan
        pnc.setTotalPresupuesto(dto.getTotalPresupuesto());
        pnc.setTotalHonorariosCalculado(dto.getTotalHonorarios());
        pnc.setTotalPresupuestoConHonorarios(dto.getTotalPresupuestoConHonorarios());
        if (dto.getTotalConDescuentos() != null) {
            pnc.setTotalConDescuentos(dto.getTotalConDescuentos());
        }
        
        // Mapear totales calculados de mayores costos y descuentos por rubro
        pnc.setTotalMayoresCostosPorRubro(dto.getTotalMayoresCostosPorRubro() != null ? dto.getTotalMayoresCostosPorRubro() : java.math.BigDecimal.ZERO);
        pnc.setTotalDescuentosPorRubro(dto.getTotalDescuentosPorRubro() != null ? dto.getTotalDescuentosPorRubro() : java.math.BigDecimal.ZERO);

        log.info("📊 Totales mapeados del frontend: totalPresupuesto={}, totalHonorarios={}, totalFinal={}",
                dto.getTotalPresupuesto(), dto.getTotalHonorarios(), dto.getTotalPresupuestoConHonorarios());

        // ========== VALIDAR COHERENCIA DE TOTALES ==========
        validarCoherenciaTotales(pnc);

        log.info("🔍 DEBUG DESPUÉS DE MAPEO - Valor en entity antes de guardar: {}",
                pnc.getHonorariosConfiguracionPresupuestoValor());

        // Guardar el presupuesto primero para obtener su ID
        log.info("💾 INICIANDO GUARDADO - Presupuesto ID: {}", pnc.getId());
        PresupuestoNoCliente guardado = repository.save(pnc);
        
        // ============= AUTO-CREACIÓN DE OBRA SEGÚN TIPO =============
        if (tipoPresupuesto.creaObraInmediatamente()) {
            log.info("🏗️ Tipo {} requiere crear obra inmediatamente", tipoPresupuesto);
            crearObraAutomaticamente(guardado);
            guardado = repository.findById(guardado.getId()).orElse(guardado); // Recargar con obra asociada
        }
        
        log.info("✅ PRESUPUESTO GUARDADO - ID: {}, Total: {}", guardado.getId(), guardado.getTotalPresupuesto());
        log.info("🔍 Obra vinculada: {}", guardado.getObra() != null ? "ID " + guardado.getObra().getId() : "NULL");
        log.info("🔍 esPresupuestoTrabajoExtra: {}", guardado.getEsPresupuestoTrabajoExtra());

        // Procesar items de calculadora si vienen en el DTO
        procesarItemsCalculadora(guardado, dto.getItemsCalculadora());

        // ...existing code...

        // Volver a guardar después del procesamiento con totales actualizados
        guardado = repository.save(guardado);
        log.info("✅ Items de calculadora procesados y presupuesto guardado con totales actualizados");

        // Procesar costos iniciales si vienen en el DTO
        procesarCostosIniciales(guardado, dto.getCostosIniciales());

        // Sincronizar datos del cliente con la obra (si existe obra asociada)
        sincronizarDatosClienteConObra(guardado);

        // ...existing code...

        log.info("🎯 GUARDADO COMPLETADO - ID: {}, Versión: {}, Total Final: {}",
                guardado.getId(), guardado.getNumeroVersion(), guardado.getTotalPresupuesto());

        log.info("🔍 DEBUG DESPUÉS DE GUARDAR - Valor persistido en BD: activo={}, tipo={}, valor={}",
                guardado.getHonorariosConfiguracionPresupuestoActivo(),
                guardado.getHonorariosConfiguracionPresupuestoTipo(),
                guardado.getHonorariosConfiguracionPresupuestoValor());

        // 🔥 ENRIQUECER CON profesionalObraId SI SE CREÓ CON ESTADO APROBADO Y TIENE OBRA
        if (guardado.getEstado() == PresupuestoEstado.APROBADO && guardado.getObra() != null) {
            log.info("✅ Ejecutando enriquecimiento para presupuesto creado como APROBADO {} con obraId {}...",
                    guardado.getId(), guardado.getObra().getId());
            enriquecerProfesionalesConObraId(guardado);
        }

        // 🔗 SINCRONIZACIÓN AUTOMÁTICA OBRA-PRESUPUESTO
        presupuestoObraSyncService.procesarCreacionPresupuesto(guardado);

        return guardado;
    }


    @Transactional(readOnly = true)
    public PresupuestoNoCliente obtenerPorId(Long id) {
        PresupuestoNoCliente presupuesto = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no cliente no encontrado"));

        // Forzar carga de la obra (relación LAZY) para que esté disponible en el JSON
        if (presupuesto.getObra() != null) {
            presupuesto.getObra().getId(); // Esto inicializa el proxy lazy
            log.debug("📤 Obra cargada para presupuesto {}: obraId={}", 
                presupuesto.getId(), presupuesto.getObra().getId());
        }

        // Forzar carga de items de calculadora y sus relaciones
        for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
            // Cargar profesionales desglosados
            item.getProfesionales().size();
            // Cargar materiales desglosados
            item.getMaterialesLista().size();
            // Cargar jornales desglosados
            if (item.getJornales() != null) {
                item.getJornales().size();
                log.debug("📤 Cargados {} jornales para item_id {}",
                        item.getJornales().size(), item.getId());
            }
            // 📥 SIEMPRE cargar gastos generales (cualquier item puede tenerlos)
            item.getGastosGenerales().size();

            if (!item.getGastosGenerales().isEmpty()) {
                log.debug("📤 Cargados {} gastos generales para item_id {}",
                        item.getGastosGenerales().size(), item.getId());
            }
        }

        return presupuesto;
    }

    /**
     * Enriquece los profesionales en itemsCalculadora con el campo profesionalObraId
     * cuando el presupuesto está APROBADO. PERSISTE los cambios en la BD.
     */
    private void enriquecerProfesionalesConObraId(PresupuestoNoCliente presupuesto) {
        boolean cambiosRealizados = false;
        Long empresaId = presupuesto.getEmpresa().getId();

        log.info("📍 Iniciando enriquecimiento - ObraId: {}, Dirección: {} {}",
                presupuesto.getObra() != null ? presupuesto.getObra().getId() : null, presupuesto.getDireccionObraCalle(), presupuesto.getDireccionObraAltura());

        for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
            for (ProfesionalCalculadora prof : item.getProfesionales()) {
                log.info("🔍 Procesando ProfesionalCalculadora ID: {}, Tipo: {}, profesionalObraId actual: {}",
                        prof.getId(), prof.getTipo(), prof.getProfesionalObraId());

                // Solo procesar si no tiene ya el profesionalObraId asignado
                if (prof.getProfesionalObraId() == null) {
                    try {
                        // PASO 1: Buscar el Profesional real por tipo
                        String tipoProfesional = prof.getTipo();
                        if (tipoProfesional == null || tipoProfesional.trim().isEmpty()) {
                            log.warn("⚠️ ProfesionalCalculadora {} sin tipo. Saltando.", prof.getId());
                            continue;
                        }

                        List<Profesional> profesionales = profesionalRepository
                                .findByTipoProfesionalIgnoreCaseAndActivoTrue(tipoProfesional);

                        if (profesionales.isEmpty()) {
                            log.warn("⚠️ No se encontró Profesional con tipo '{}'. Saltando.", tipoProfesional);
                            continue;
                        }

                        Profesional profesional = profesionales.get(0);
                        log.info("✅ Profesional encontrado: ID={}, Tipo={}", profesional.getId(), profesional.getTipoProfesional());

                        // PASO 2: Buscar la asignación profesional_obra usando dirección de obra
                        Optional<com.rodrigo.construccion.model.entity.ProfesionalObra> profesionalObraOpt =
                                profesionalObraRepository.findByProfesionalAndDireccionObra(
                                        profesional.getId(),  // ✅ CORRECCIÓN: usar ID del Profesional, no de ProfesionalCalculadora
                                        presupuesto.getDireccionObraCalle(),
                                        presupuesto.getDireccionObraAltura(),
                                        presupuesto.getDireccionObraPiso(),
                                        presupuesto.getDireccionObraDepartamento(),
                                        empresaId
                                );

                        if (profesionalObraOpt.isPresent()) {
                            Long profesionalObraId = profesionalObraOpt.get().getId();
                            prof.setProfesionalObraId(profesionalObraId);
                            profesionalCalculadoraRepository.save(prof);
                            cambiosRealizados = true;
                            log.info("✅ ProfesionalCalculadora {} enriquecido y GUARDADO con profesionalObraId: {}",
                                    prof.getId(), profesionalObraId);
                        } else {
                            log.warn("❌ NO se encontró asignación profesional-obra para:");
                            log.warn("   - ProfesionalId: {}", profesional.getId());
                            log.warn("   - Tipo: {}", tipoProfesional);
                            log.warn("   - Dirección: {} {}", presupuesto.getDireccionObraCalle(), presupuesto.getDireccionObraAltura());
                            log.warn("   - EmpresaId: {}", empresaId);
                        }
                    } catch (Exception e) {
                        log.error("❌ Error al buscar/guardar profesionalObraId para ProfesionalCalculadora {}: {}",
                                prof.getId(), e.getMessage(), e);
                    }
                } else {
                    log.info("⏭️ ProfesionalCalculadora {} ya tiene profesionalObraId: {}. Saltando.",
                            prof.getId(), prof.getProfesionalObraId());
                }
            }
        }

        if (cambiosRealizados) {
            log.info("💾 Cambios de profesionalObraId persistidos en BD para presupuesto {}", presupuesto.getId());
        } else {
            log.info("ℹ️ No se realizaron cambios de profesionalObraId para presupuesto {}", presupuesto.getId());
        }
    }

    @Transactional
    public PresupuestoNoCliente actualizar(Long id, PresupuestoNoClienteRequestDTO dto) {
        try {
            log.info("🔄 ACTUALIZANDO PRESUPUESTO POR ID - ID: {} (SIN crear nueva versión)", id);

            // PASO 1: Obtener presupuesto existente
            PresupuestoNoCliente existente = obtenerPorId(id);
            log.info("📊 Presupuesto actual - Estado: {}, Versión: {}, Total: {}",
                    existente.getEstado(), existente.getNumeroVersion(), existente.getTotalPresupuesto());

            // ========== IMPORTANTE: PUT /{id} SIEMPRE ACTUALIZA EL MISMO REGISTRO ==========
            // Este endpoint NO crea versiones nuevas independientemente del estado.
            // Para crear versiones, usar PUT sin /{id} con parámetros de dirección.
            
            log.info("📝 Actualizando el MISMO registro (ID: {}, Versión: {}) sin cambiar versión",
                    existente.getId(), existente.getNumeroVersion());

            PresupuestoNoCliente actualizado = actualizarVersionExistenteCompleto(existente, dto);
            log.info("✅ Presupuesto ACTUALIZADO (mismo registro) - ID: {}, Versión: {}, Estado: {}, Total: {}",
                    actualizado.getId(), actualizado.getNumeroVersion(), actualizado.getEstado(), actualizado.getTotalPresupuesto());

            return actualizado;

        } catch (Exception e) {
            log.error("❌ ERROR EN ACTUALIZACIÓN - ID: {}", id, e);
            log.error("❌ Causa: {}", e.getMessage());
            log.error("❌ Stack trace completo:", e);
            throw new RuntimeException("Error al actualizar presupuesto: " + e.getMessage(), e);
        }
    }

    /**
     * Crea una nueva versión del presupuesto incrementando el número de versión
     */
    protected PresupuestoNoCliente crearNuevaVersion(PresupuestoNoCliente existente, PresupuestoNoClienteRequestDTO dto) {
        log.info("🔄 Creando nueva versión del presupuesto ID: {} | Versión actual: {}",
                existente.getId(), existente.getNumeroVersion());

        // 🔍 DEBUG: Ver qué trae el DTO para campos problemáticos
        log.info("📋 DTO recibido - honorariosJornales: activo={}, tipo={}, valor={}",
                dto.getHonorariosJornalesActivo(), dto.getHonorariosJornalesTipo(), dto.getHonorariosJornalesValor());
        log.info("📋 DTO recibido - mayoresCostosJornales: activo={}, tipo={}, valor={}",
                dto.getMayoresCostosJornalesActivo(), dto.getMayoresCostosJornalesTipo(), dto.getMayoresCostosJornalesValor());
        log.info("📋 Presupuesto existente ANTES de mapear - honorariosJornales: activo={}, tipo={}, valor={}",
                existente.getHonorariosJornalesActivo(), existente.getHonorariosJornalesTipo(), existente.getHonorariosJornalesValor());
        log.info("📋 Presupuesto existente ANTES de mapear - mayoresCostosJornales: activo={}, tipo={}, valor={}",
                existente.getMayoresCostosJornalesActivo(), existente.getMayoresCostosJornalesTipo(), existente.getMayoresCostosJornalesValor());

        // ========== PASO 1: ACTUALIZAR EL PRESUPUESTO EXISTENTE CON LOS DATOS DEL DTO ==========
        // Esto es CRÍTICO para que tenga los valores más recientes antes de copiar
        // IMPORTANTE: Guardar obra, cliente Y esPresupuestoTrabajoExtra original por si se pierden en el mapeo
        Obra obraOriginal = existente.getObra();
        Cliente clienteOriginal = existente.getCliente();
        Boolean esPresupuestoTrabajoExtraOriginal = existente.getEsPresupuestoTrabajoExtra();
        log.info("🔒 PRESERVANDO esPresupuestoTrabajoExtra ORIGINAL para nueva versión: {}", esPresupuestoTrabajoExtraOriginal);

        PresupuestoNoCliente presupuestoConDatosActualizados = actualizarVersionExistente(existente, dto);

        // 🔥 CRÍTICO: Si se perdió la obra o cliente en el mapeo, restaurarlos
        if (presupuestoConDatosActualizados.getObra() == null && obraOriginal != null) {
            log.warn("⚠️ Obra se perdió en mapeo. Restaurando obra original ID: {}", obraOriginal.getId());
            presupuestoConDatosActualizados.setObra(obraOriginal);
        }
        if (presupuestoConDatosActualizados.getCliente() == null && clienteOriginal != null) {
            log.warn("⚠️ Cliente se perdió en mapeo. Restaurando cliente original ID: {}", clienteOriginal.getId());
            presupuestoConDatosActualizados.setCliente(clienteOriginal);
        }

        // ========== PASO 2: GUARDAR LOS CAMBIOS EN EL PRESUPUESTO EXISTENTE ==========
        // Forzar persistencia para asegurar que los items calculadora están actualizados
        presupuestoConDatosActualizados = repository.saveAndFlush(presupuestoConDatosActualizados);

        log.info("✅ Presupuesto existente actualizado con datos del DTO. Total items calculadora: {}",
                presupuestoConDatosActualizados.getItemsCalculadora() != null ?
                        presupuestoConDatosActualizados.getItemsCalculadora().size() : 0);

        // ========== PASO 3: CREAR NUEVA VERSIÓN COPIANDO TODOS LOS DATOS ==========
        PresupuestoNoCliente nuevaVersion = new PresupuestoNoCliente();

        // Mantener información de la versión anterior PERO con número incrementado
        nuevaVersion.setNumeroPresupuesto(presupuestoConDatosActualizados.getNumeroPresupuesto());
        nuevaVersion.setNumeroVersion(presupuestoConDatosActualizados.getNumeroVersion() + 1);
        nuevaVersion.setEmpresa(presupuestoConDatosActualizados.getEmpresa());
        nuevaVersion.setObra(presupuestoConDatosActualizados.getObra());
        nuevaVersion.setCliente(presupuestoConDatosActualizados.getCliente());
        nuevaVersion.setFechaEmision(LocalDate.now());
        nuevaVersion.setFechaCreacion(presupuestoConDatosActualizados.getFechaCreacion()); // Mantener fecha de creación original

        log.info("📋 COPIANDO RELACIONES - Obra: {} | Cliente: {}",
                nuevaVersion.getObra() != null ? nuevaVersion.getObra().getId() : "NULL",
                nuevaVersion.getCliente() != null ? nuevaVersion.getCliente().getId() : "NULL");

        // ========== COPIAR ABSOLUTAMENTE TODOS LOS DATOS DEL PRESUPUESTO ANTERIOR ==========
        // DATOS DEL SOLICITANTE
        nuevaVersion.setNombreObra(presupuestoConDatosActualizados.getNombreObra());
        nuevaVersion.setNombreSolicitante(presupuestoConDatosActualizados.getNombreSolicitante());
        nuevaVersion.setTelefono(presupuestoConDatosActualizados.getTelefono());
        nuevaVersion.setDireccionParticular(presupuestoConDatosActualizados.getDireccionParticular());
        nuevaVersion.setMail(presupuestoConDatosActualizados.getMail());

        // DIRECCIÓN DE OBRA COMPLETA
        nuevaVersion.setDireccionObraBarrio(presupuestoConDatosActualizados.getDireccionObraBarrio());
        nuevaVersion.setDireccionObraCalle(presupuestoConDatosActualizados.getDireccionObraCalle());
        nuevaVersion.setDireccionObraAltura(presupuestoConDatosActualizados.getDireccionObraAltura());
        nuevaVersion.setDireccionObraTorre(presupuestoConDatosActualizados.getDireccionObraTorre());
        nuevaVersion.setDireccionObraPiso(presupuestoConDatosActualizados.getDireccionObraPiso());
        nuevaVersion.setDireccionObraDepartamento(presupuestoConDatosActualizados.getDireccionObraDepartamento());

        // DESCRIPCIONES Y OBSERVACIONES COMPLETAS
        nuevaVersion.setDescripcion(presupuestoConDatosActualizados.getDescripcion());
        nuevaVersion.setObservaciones(presupuestoConDatosActualizados.getObservaciones());
        nuevaVersion.setDescripcionDetallada(presupuestoConDatosActualizados.getDescripcionDetallada());
        nuevaVersion.setObservacionesInternas(presupuestoConDatosActualizados.getObservacionesInternas());
        nuevaVersion.setNotasAdicionales(presupuestoConDatosActualizados.getNotasAdicionales());
        nuevaVersion.setEspecificacionesTecnicas(presupuestoConDatosActualizados.getEspecificacionesTecnicas());
        nuevaVersion.setComentariosCliente(presupuestoConDatosActualizados.getComentariosCliente());
        nuevaVersion.setRequisitosEspeciales(presupuestoConDatosActualizados.getRequisitosEspeciales());

        // TIEMPOS Y FECHAS
        nuevaVersion.setTiempoEstimadoTerminacion(presupuestoConDatosActualizados.getTiempoEstimadoTerminacion());
        nuevaVersion.setFechaProbableInicio(presupuestoConDatosActualizados.getFechaProbableInicio());
        nuevaVersion.setVencimiento(presupuestoConDatosActualizados.getVencimiento());

        // ========== TIPO DE PRESUPUESTO ==========
        nuevaVersion.setTipoPresupuesto(presupuestoConDatosActualizados.getTipoPresupuesto());
        
        // ========== COPIAR CAMPO ES TRABAJO EXTRA (CRÍTICO - INMUTABLE EN VERSIONADO) ==========
        // IMPORTANTE: Usar el valor ORIGINAL guardado al inicio, NO el del presupuesto actualizado
        // Este campo es INMUTABLE: una vez que un presupuesto se crea como trabajo extra o normal,
        // todas sus versiones futuras deben mantener ese mismo valor
        nuevaVersion.setEsPresupuestoTrabajoExtra(esPresupuestoTrabajoExtraOriginal);
        log.info("🔗 Campo esPresupuestoTrabajoExtra INMUTABLE copiado desde ORIGINAL: {} (ignorando DTO)", esPresupuestoTrabajoExtraOriginal);

        // ========== ESTADO SEGÚN TIPO DE PRESUPUESTO ==========
        // Si es TRABAJOS_SEMANALES, mantener APROBADO; si es TRADICIONAL, resetear a A_ENVIAR
        if (presupuestoConDatosActualizados.getTipoPresupuesto() == TipoPresupuesto.TRABAJOS_SEMANALES) {
            nuevaVersion.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO);
            log.info("✅ Nueva versión TRABAJOS_SEMANALES → Estado: APROBADO (automático)");
        } else {
            nuevaVersion.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.A_ENVIAR);
            log.info("📤 Nueva versión TRADICIONAL → Estado: A_ENVIAR");
        }

        // TOTALES CONSOLIDADOS (CRÍTICO)
        nuevaVersion.setTotalProfesionales(presupuestoConDatosActualizados.getTotalProfesionales());
        nuevaVersion.setTotalMateriales(presupuestoConDatosActualizados.getTotalMateriales());
        nuevaVersion.setTotalGeneral(presupuestoConDatosActualizados.getTotalGeneral());
        nuevaVersion.setTotalPresupuesto(presupuestoConDatosActualizados.getTotalPresupuesto());
        nuevaVersion.setTotalHonorariosCalculado(presupuestoConDatosActualizados.getTotalHonorariosCalculado());
        nuevaVersion.setTotalMayoresCostos(presupuestoConDatosActualizados.getTotalMayoresCostos());
        nuevaVersion.setTotalPresupuestoConHonorarios(presupuestoConDatosActualizados.getTotalPresupuestoConHonorarios());
        nuevaVersion.setTotalConDescuentos(presupuestoConDatosActualizados.getTotalConDescuentos());
        nuevaVersion.setTotalMayoresCostosPorRubro(presupuestoConDatosActualizados.getTotalMayoresCostosPorRubro() != null ? presupuestoConDatosActualizados.getTotalMayoresCostosPorRubro() : java.math.BigDecimal.ZERO);
        nuevaVersion.setTotalDescuentosPorRubro(presupuestoConDatosActualizados.getTotalDescuentosPorRubro() != null ? presupuestoConDatosActualizados.getTotalDescuentosPorRubro() : java.math.BigDecimal.ZERO);

        // CONFIGURACIÓN DE HONORARIOS COMPLETA
        nuevaVersion.setHonorarioDireccionValorFijo(presupuestoConDatosActualizados.getHonorarioDireccionValorFijo());
        nuevaVersion.setHonorarioDireccionPorcentaje(presupuestoConDatosActualizados.getHonorarioDireccionPorcentaje());
        nuevaVersion.setHonorarioDireccionImporte(presupuestoConDatosActualizados.getHonorarioDireccionImporte());

        nuevaVersion.setHonorariosAplicarATodos(presupuestoConDatosActualizados.getHonorariosAplicarATodos());
        nuevaVersion.setHonorariosValorGeneral(presupuestoConDatosActualizados.getHonorariosValorGeneral());
        nuevaVersion.setHonorariosTipoGeneral(presupuestoConDatosActualizados.getHonorariosTipoGeneral());

        nuevaVersion.setHonorariosProfesionalesActivo(presupuestoConDatosActualizados.getHonorariosProfesionalesActivo());
        nuevaVersion.setHonorariosProfesionalesTipo(presupuestoConDatosActualizados.getHonorariosProfesionalesTipo());
        nuevaVersion.setHonorariosProfesionalesValor(presupuestoConDatosActualizados.getHonorariosProfesionalesValor());

        nuevaVersion.setHonorariosMaterialesActivo(presupuestoConDatosActualizados.getHonorariosMaterialesActivo());
        nuevaVersion.setHonorariosMaterialesTipo(presupuestoConDatosActualizados.getHonorariosMaterialesTipo());
        nuevaVersion.setHonorariosMaterialesValor(presupuestoConDatosActualizados.getHonorariosMaterialesValor());

        nuevaVersion.setHonorariosOtrosCostosActivo(presupuestoConDatosActualizados.getHonorariosOtrosCostosActivo());
        nuevaVersion.setHonorariosOtrosCostosTipo(presupuestoConDatosActualizados.getHonorariosOtrosCostosTipo());
        nuevaVersion.setHonorariosOtrosCostosValor(presupuestoConDatosActualizados.getHonorariosOtrosCostosValor());

        nuevaVersion.setHonorariosJornalesActivo(presupuestoConDatosActualizados.getHonorariosJornalesActivo());
        nuevaVersion.setHonorariosJornalesTipo(presupuestoConDatosActualizados.getHonorariosJornalesTipo());
        nuevaVersion.setHonorariosJornalesValor(presupuestoConDatosActualizados.getHonorariosJornalesValor());

        nuevaVersion.setHonorariosConfiguracionPresupuestoActivo(presupuestoConDatosActualizados.getHonorariosConfiguracionPresupuestoActivo());
        nuevaVersion.setHonorariosConfiguracionPresupuestoTipo(presupuestoConDatosActualizados.getHonorariosConfiguracionPresupuestoTipo());
        nuevaVersion.setHonorariosConfiguracionPresupuestoValor(presupuestoConDatosActualizados.getHonorariosConfiguracionPresupuestoValor());

        // Honorarios por Rubro - copiar colección desde presupuesto original
        if (presupuestoConDatosActualizados.getHonorariosPorRubro() != null) {
            for (com.rodrigo.construccion.model.entity.HonorarioPorRubro honorario : presupuestoConDatosActualizados.getHonorariosPorRubro()) {
                com.rodrigo.construccion.model.entity.HonorarioPorRubro nuevoHonorario = new com.rodrigo.construccion.model.entity.HonorarioPorRubro();
                nuevoHonorario.setPresupuestoNoCliente(nuevaVersion);
                nuevoHonorario.setNombreRubro(honorario.getNombreRubro());
                nuevoHonorario.setActivo(honorario.getActivo());
                nuevoHonorario.setTipo(honorario.getTipo());
                nuevoHonorario.setValor(honorario.getValor());
                nuevoHonorario.setProfesionalesActivo(honorario.getProfesionalesActivo());
                nuevoHonorario.setProfesionalesTipo(honorario.getProfesionalesTipo());
                nuevoHonorario.setProfesionalesValor(honorario.getProfesionalesValor());
                nuevoHonorario.setMaterialesActivo(honorario.getMaterialesActivo());
                nuevoHonorario.setMaterialesTipo(honorario.getMaterialesTipo());
                nuevoHonorario.setMaterialesValor(honorario.getMaterialesValor());
                nuevoHonorario.setOtrosCostosActivo(honorario.getOtrosCostosActivo());
                nuevoHonorario.setOtrosCostosTipo(honorario.getOtrosCostosTipo());
                nuevoHonorario.setOtrosCostosValor(honorario.getOtrosCostosValor());
                nuevaVersion.getHonorariosPorRubro().add(nuevoHonorario);
            }
        }

        // ========== MAPEAR CONFIGURACIÓN DE CÁLCULO DE DÍAS HÁBILES ==========
        nuevaVersion.setCalculoAutomaticoDiasHabiles(presupuestoConDatosActualizados.getCalculoAutomaticoDiasHabiles());

        // ========== CONFIGURACIÓN DE MAYORES COSTOS COMPLETA ==========
        nuevaVersion.setMayoresCostosAplicarValorGeneral(presupuestoConDatosActualizados.getMayoresCostosAplicarValorGeneral());
        nuevaVersion.setMayoresCostosValorGeneral(presupuestoConDatosActualizados.getMayoresCostosValorGeneral());
        nuevaVersion.setMayoresCostosTipoGeneral(presupuestoConDatosActualizados.getMayoresCostosTipoGeneral());
        nuevaVersion.setMayoresCostosGeneralImportado(presupuestoConDatosActualizados.getMayoresCostosGeneralImportado());
        nuevaVersion.setMayoresCostosRubroImportado(presupuestoConDatosActualizados.getMayoresCostosRubroImportado());
        nuevaVersion.setMayoresCostosNombreRubroImportado(presupuestoConDatosActualizados.getMayoresCostosNombreRubroImportado());

        nuevaVersion.setMayoresCostosProfesionalesActivo(presupuestoConDatosActualizados.getMayoresCostosProfesionalesActivo());
        nuevaVersion.setMayoresCostosProfesionalesTipo(presupuestoConDatosActualizados.getMayoresCostosProfesionalesTipo());
        nuevaVersion.setMayoresCostosProfesionalesValor(presupuestoConDatosActualizados.getMayoresCostosProfesionalesValor());

        nuevaVersion.setMayoresCostosMaterialesActivo(presupuestoConDatosActualizados.getMayoresCostosMaterialesActivo());
        nuevaVersion.setMayoresCostosMaterialesTipo(presupuestoConDatosActualizados.getMayoresCostosMaterialesTipo());
        nuevaVersion.setMayoresCostosMaterialesValor(presupuestoConDatosActualizados.getMayoresCostosMaterialesValor());

        nuevaVersion.setMayoresCostosOtrosCostosActivo(presupuestoConDatosActualizados.getMayoresCostosOtrosCostosActivo());
        nuevaVersion.setMayoresCostosOtrosCostosTipo(presupuestoConDatosActualizados.getMayoresCostosOtrosCostosTipo());
        nuevaVersion.setMayoresCostosOtrosCostosValor(presupuestoConDatosActualizados.getMayoresCostosOtrosCostosValor());

        nuevaVersion.setMayoresCostosJornalesActivo(presupuestoConDatosActualizados.getMayoresCostosJornalesActivo());
        nuevaVersion.setMayoresCostosJornalesTipo(presupuestoConDatosActualizados.getMayoresCostosJornalesTipo());
        nuevaVersion.setMayoresCostosJornalesValor(presupuestoConDatosActualizados.getMayoresCostosJornalesValor());

        nuevaVersion.setMayoresCostosConfiguracionPresupuestoActivo(presupuestoConDatosActualizados.getMayoresCostosConfiguracionPresupuestoActivo());
        nuevaVersion.setMayoresCostosConfiguracionPresupuestoTipo(presupuestoConDatosActualizados.getMayoresCostosConfiguracionPresupuestoTipo());
        nuevaVersion.setMayoresCostosConfiguracionPresupuestoValor(presupuestoConDatosActualizados.getMayoresCostosConfiguracionPresupuestoValor());

        nuevaVersion.setMayoresCostosHonorariosActivo(presupuestoConDatosActualizados.getMayoresCostosHonorariosActivo());
        nuevaVersion.setMayoresCostosHonorariosTipo(presupuestoConDatosActualizados.getMayoresCostosHonorariosTipo());
        nuevaVersion.setMayoresCostosHonorariosValor(presupuestoConDatosActualizados.getMayoresCostosHonorariosValor());

        // Explicación/justificación INTERNA de mayores costos
        nuevaVersion.setMayoresCostosExplicacion(presupuestoConDatosActualizados.getMayoresCostosExplicacion());

        // ========== CONFIGURACIÓN DE DESCUENTOS (Modelo Relacional) ==========
        nuevaVersion.setDescuentosExplicacion(presupuestoConDatosActualizados.getDescuentosExplicacion());
        nuevaVersion.setDescuentosJornalesActivo(presupuestoConDatosActualizados.getDescuentosJornalesActivo());
        nuevaVersion.setDescuentosJornalesTipo(presupuestoConDatosActualizados.getDescuentosJornalesTipo());
        nuevaVersion.setDescuentosJornalesValor(presupuestoConDatosActualizados.getDescuentosJornalesValor());
        nuevaVersion.setDescuentosMaterialesActivo(presupuestoConDatosActualizados.getDescuentosMaterialesActivo());
        nuevaVersion.setDescuentosMaterialesTipo(presupuestoConDatosActualizados.getDescuentosMaterialesTipo());
        nuevaVersion.setDescuentosMaterialesValor(presupuestoConDatosActualizados.getDescuentosMaterialesValor());
        nuevaVersion.setDescuentosHonorariosActivo(presupuestoConDatosActualizados.getDescuentosHonorariosActivo());
        nuevaVersion.setDescuentosHonorariosTipo(presupuestoConDatosActualizados.getDescuentosHonorariosTipo());
        nuevaVersion.setDescuentosHonorariosValor(presupuestoConDatosActualizados.getDescuentosHonorariosValor());
        nuevaVersion.setDescuentosMayoresCostosActivo(presupuestoConDatosActualizados.getDescuentosMayoresCostosActivo());
        nuevaVersion.setDescuentosMayoresCostosTipo(presupuestoConDatosActualizados.getDescuentosMayoresCostosTipo());
        nuevaVersion.setDescuentosMayoresCostosValor(presupuestoConDatosActualizados.getDescuentosMayoresCostosValor());
        
        // ========== SUB-TIPOS DE DESCUENTOS SOBRE HONORARIOS ==========
        nuevaVersion.setDescuentosHonorariosJornalesActivo(presupuestoConDatosActualizados.getDescuentosHonorariosJornalesActivo());
        nuevaVersion.setDescuentosHonorariosJornalesTipo(presupuestoConDatosActualizados.getDescuentosHonorariosJornalesTipo());
        nuevaVersion.setDescuentosHonorariosJornalesValor(presupuestoConDatosActualizados.getDescuentosHonorariosJornalesValor());
        nuevaVersion.setDescuentosHonorariosProfesionalesActivo(presupuestoConDatosActualizados.getDescuentosHonorariosProfesionalesActivo());
        nuevaVersion.setDescuentosHonorariosProfesionalesTipo(presupuestoConDatosActualizados.getDescuentosHonorariosProfesionalesTipo());
        nuevaVersion.setDescuentosHonorariosProfesionalesValor(presupuestoConDatosActualizados.getDescuentosHonorariosProfesionalesValor());
        nuevaVersion.setDescuentosHonorariosMaterialesActivo(presupuestoConDatosActualizados.getDescuentosHonorariosMaterialesActivo());
        nuevaVersion.setDescuentosHonorariosMaterialesTipo(presupuestoConDatosActualizados.getDescuentosHonorariosMaterialesTipo());
        nuevaVersion.setDescuentosHonorariosMaterialesValor(presupuestoConDatosActualizados.getDescuentosHonorariosMaterialesValor());
        nuevaVersion.setDescuentosHonorariosOtrosActivo(presupuestoConDatosActualizados.getDescuentosHonorariosOtrosActivo());
        nuevaVersion.setDescuentosHonorariosOtrosTipo(presupuestoConDatosActualizados.getDescuentosHonorariosOtrosTipo());
        nuevaVersion.setDescuentosHonorariosOtrosValor(presupuestoConDatosActualizados.getDescuentosHonorariosOtrosValor());
        nuevaVersion.setDescuentosHonorariosGastosGeneralesActivo(presupuestoConDatosActualizados.getDescuentosHonorariosGastosGeneralesActivo());
        nuevaVersion.setDescuentosHonorariosGastosGeneralesTipo(presupuestoConDatosActualizados.getDescuentosHonorariosGastosGeneralesTipo());
        nuevaVersion.setDescuentosHonorariosGastosGeneralesValor(presupuestoConDatosActualizados.getDescuentosHonorariosGastosGeneralesValor());
        nuevaVersion.setDescuentosHonorariosConfiguracionActivo(presupuestoConDatosActualizados.getDescuentosHonorariosConfiguracionActivo());
        nuevaVersion.setDescuentosHonorariosConfiguracionTipo(presupuestoConDatosActualizados.getDescuentosHonorariosConfiguracionTipo());
        nuevaVersion.setDescuentosHonorariosConfiguracionValor(presupuestoConDatosActualizados.getDescuentosHonorariosConfiguracionValor());
        
        log.info("📊 Descuentos copiados a nueva versión: {}", 
                 nuevaVersion.getDescuentosJornalesActivo() != null && nuevaVersion.getDescuentosJornalesActivo() ? "SI" : "NO");

        // ========== COPIAR COLECCIONES NORMALIZADAS ==========
        /* LEGACY CODE COMENTADO - Estas tablas ya no existen
        // PROFESIONALES
        if (presupuestoConDatosActualizados.getProfesionales() != null) {
            for (PresupuestoNoClienteProfesional profAnterior : presupuestoConDatosActualizados.getProfesionales()) {
                // ... código eliminado ...
                nuevaVersion.getProfesionales().add(nuevoProf);
            }
        }
        
        // MATERIALES
        if (presupuestoConDatosActualizados.getMateriales() != null) {
            for (PresupuestoNoClienteMaterial matAnterior : presupuestoConDatosActualizados.getMateriales()) {
                // ... código eliminado ...
                nuevaVersion.getMateriales().add(nuevoMat);
            }
        }
        
        // JORNALES
        if (presupuestoConDatosActualizados.getJornales() != null) {
            for (PresupuestoNoClienteJornal jornalAnterior : presupuestoConDatosActualizados.getJornales()) {
                // ... código eliminado ...
                nuevaVersion.getJornales().add(nuevoJornal);
            }
        }
        */

        log.info("✅ NUEVA VERSIÓN: Copiados TODOS los datos del presupuesto anterior");
        // NOTA: Los profesionales, materiales y jornales ahora están en items_calculadora_presupuesto
        log.info("   💰 Total presupuesto: ${}", nuevaVersion.getTotalPresupuesto());

        // ========== PASO 4: APLICAR CAMBIOS DESDE DTO (SI VIENEN) ==========
        // Solo sobrescribir campos que vengan específicamente en el DTO
        // IMPORTANTE: NO procesar items calculadora aquí porque se copiarán después
        if (dto.getNombreSolicitante() != null) nuevaVersion.setNombreSolicitante(dto.getNombreSolicitante());
        if (dto.getTelefono() != null) nuevaVersion.setTelefono(dto.getTelefono());
        if (dto.getMail() != null) nuevaVersion.setMail(dto.getMail());
        if (dto.getDescripcion() != null) nuevaVersion.setDescripcion(dto.getDescripcion());
        if (dto.getObservaciones() != null) nuevaVersion.setObservaciones(dto.getObservaciones());
        // ... otros campos que vengan en el DTO pueden sobrescribir los copiados

        log.info("✅ Cambios del DTO aplicados sobre la nueva versión (si venían)");

        // ========== PASO 5: GUARDAR LA NUEVA VERSIÓN ==========
        PresupuestoNoCliente guardado = repository.save(nuevaVersion);

        log.info("✅ Nueva versión creada con ID: {} | Versión: {}",
                guardado.getId(), guardado.getNumeroVersion());

        // ========== VALIDACIÓN: VERIFICAR QUE TODOS LOS CAMPOS CRÍTICOS SE COPIARON ==========
        log.info("📊 VALIDACIÓN DE COPIA - Versión anterior ID: {} → Nueva versión ID: {}",
                presupuestoConDatosActualizados.getId(), guardado.getId());
        log.info("   💰 Totales - General: ${} | Profesionales: ${} | Materiales: ${}",
                guardado.getTotalGeneral(), guardado.getTotalProfesionales(), guardado.getTotalMateriales());
        log.info("   💵 Honorarios calculado: ${} | Mayores costos: ${}",
                guardado.getTotalHonorariosCalculado(), guardado.getTotalMayoresCostos());
        log.info("   🎯 Total final: ${} | Con honorarios: ${}",
                guardado.getTotalPresupuesto(), guardado.getTotalPresupuestoConHonorarios());

        // Advertencias si hay campos NULL críticos
        if (guardado.getTotalMayoresCostos() == null && presupuestoConDatosActualizados.getTotalMayoresCostos() != null) {
            log.warn("⚠️ ADVERTENCIA: totalMayoresCostos es NULL en nueva versión pero NO ERA NULL en versión anterior");
        }
        if (guardado.getTotalHonorariosCalculado() == null && presupuestoConDatosActualizados.getTotalHonorariosCalculado() != null) {
            log.warn("⚠️ ADVERTENCIA: totalHonorariosCalculado es NULL en nueva versión pero NO ERA NULL en versión anterior");
        }

        // ========== PASO 6: COPIAR ITEMS CALCULADORA DESDE EL PRESUPUESTO ACTUALIZADO ==========
        // IMPORTANTE: Solo COPIAMOS, NO recalculamos para evitar duplicación de totales
        // Los totales ya fueron copiados correctamente en el paso anterior
        // Solo copiar si el DTO no incluye items de calculadora (para evitar sobrescribir)
        if ((dto.getItemsCalculadora() == null || dto.getItemsCalculadora().isEmpty()) &&
                presupuestoConDatosActualizados.getItemsCalculadora() != null &&
                !presupuestoConDatosActualizados.getItemsCalculadora().isEmpty()) {

            try {
                log.info("🔄 Copiando {} items calculadora desde presupuesto actualizado ID: {} hacia nueva versión ID: {}",
                        presupuestoConDatosActualizados.getItemsCalculadora().size(),
                        presupuestoConDatosActualizados.getId(),
                        guardado.getId());

                copiarItemsCalculadoraDePresupuestoBase(guardado.getId(), presupuestoConDatosActualizados.getId());

                log.info("✅ Items calculadora copiados exitosamente desde presupuesto actualizado");
            } catch (Exception e) {
                log.error("❌ Error al copiar items calculadora: {}", e.getMessage(), e);
            }
        }

        // Sincronizar datos del cliente con la obra (si existe obra asociada)
        sincronizarDatosClienteConObra(guardado);

        // 🔗 SINCRONIZACIÓN AUTOMÁTICA OBRA-PRESUPUESTO (nueva versión creada)
        presupuestoObraSyncService.procesarCreacionPresupuesto(guardado);

        // ...existing code...

        return guardado;
    }

    /**
     * Actualiza la versión existente sin crear una nueva
     */
    protected PresupuestoNoCliente actualizarVersionExistente(PresupuestoNoCliente pnc, PresupuestoNoClienteRequestDTO dto) {
        // Mantener ID, versión y fecha de creación original
        Integer versionActual = pnc.getNumeroVersion();
        LocalDate fechaCreacionOriginal = pnc.getFechaCreacion();

        // Actualizar campos desde DTO
        pnc = mapearDtoAPresupuesto(pnc, dto, false);

        // 🔍 DEBUG: Verificar si el mapeo funcionó correctamente
        log.info("🔍 DESPUÉS DE MAPEAR - honorariosJornales: activo={}, tipo={}, valor={}",
                pnc.getHonorariosJornalesActivo(), pnc.getHonorariosJornalesTipo(), pnc.getHonorariosJornalesValor());
        log.info("🔍 DESPUÉS DE MAPEAR - mayoresCostosJornales: activo={}, tipo={}, valor={}",
                pnc.getMayoresCostosJornalesActivo(), pnc.getMayoresCostosJornalesTipo(), pnc.getMayoresCostosJornalesValor());

        // Asegurar que se mantienen los valores originales
        pnc.setNumeroVersion(versionActual);
        pnc.setFechaCreacion(fechaCreacionOriginal);

        return pnc;
    }

    /**
     * Actualiza una versión existente con procesamiento completo (guardado + consolidación)
     */
    protected PresupuestoNoCliente actualizarVersionExistenteCompleto(PresupuestoNoCliente existente, PresupuestoNoClienteRequestDTO dto) {
        try {
            log.info("🔄 PROCESAMIENTO COMPLETO - Actualizando versión existente ID: {}", existente.getId());

            log.info("🔍 DEBUG ACTUALIZAR - honorariosConfiguracionPresupuesto recibido: activo={}, tipo={}, valor={}",
                    dto.getHonorariosConfiguracionPresupuestoActivo(),
                    dto.getHonorariosConfiguracionPresupuestoTipo(),
                    dto.getHonorariosConfiguracionPresupuestoValor());

            log.info("🔍 DEBUG ACTUALIZAR - mayoresCostosConfiguracionPresupuesto recibido: activo={}, tipo={}, valor={}",
                    dto.getMayoresCostosConfiguracionPresupuestoActivo(),
                    dto.getMayoresCostosConfiguracionPresupuestoTipo(),
                    dto.getMayoresCostosConfiguracionPresupuestoValor());

            // PASO 1: Mapear datos del DTO
            PresupuestoNoCliente pnc = actualizarVersionExistente(existente, dto);

            // PASO 2: Guardar el presupuesto
            log.info("💾 GUARDANDO presupuesto actualizado...");

            log.info("🔍 DEBUG ANTES DE GUARDAR - honorariosJornales en entidad: activo={}, tipo={}, valor={}",
                    pnc.getHonorariosJornalesActivo(),
                    pnc.getHonorariosJornalesTipo(),
                    pnc.getHonorariosJornalesValor());

            PresupuestoNoCliente actualizado = repository.save(pnc);
            log.info("✅ Presupuesto GUARDADO - ID: {}, Total: {}", actualizado.getId(), actualizado.getTotalPresupuesto());

            log.info("🔍 DEBUG DESPUÉS DE GUARDAR - honorariosJornales persistido: activo={}, tipo={}, valor={}",
                    actualizado.getHonorariosJornalesActivo(),
                    actualizado.getHonorariosJornalesTipo(),
                    actualizado.getHonorariosJornalesValor());

            log.info("🔍 DEBUG DESPUÉS DE GUARDAR UPDATE - honorarios persistido: activo={}, tipo={}, valor={}",
                    actualizado.getHonorariosConfiguracionPresupuestoActivo(),
                    actualizado.getHonorariosConfiguracionPresupuestoTipo(),
                    actualizado.getHonorariosConfiguracionPresupuestoValor());

            log.info("🔍 DEBUG DESPUÉS DE GUARDAR UPDATE - mayoresCostos persistido: activo={}, tipo={}, valor={}",
                    actualizado.getMayoresCostosConfiguracionPresupuestoActivo(),
                    actualizado.getMayoresCostosConfiguracionPresupuestoTipo(),
                    actualizado.getMayoresCostosConfiguracionPresupuestoValor());
            
            log.info("🔍 DEBUG DESPUÉS DE GUARDAR UPDATE - esPresupuestoTrabajoExtra: {}", actualizado.getEsPresupuestoTrabajoExtra());
            log.info("🔍 DEBUG DESPUÉS DE GUARDAR UPDATE - obra vinculada: {}", actualizado.getObra() != null ? actualizado.getObra().getId() + " (nombre: '" + actualizado.getObra().getNombre() + "')" : "null");

            // PASO 3: Procesar items de calculadora si vienen en el DTO
            log.info("📋 Procesando items de calculadora...");
            procesarItemsCalculadora(actualizado, dto.getItemsCalculadora());

            // PASO 4: Procesar costos iniciales si vienen en el DTO
            log.info("💰 Procesando costos iniciales...");
            procesarCostosIniciales(actualizado, dto.getCostosIniciales());

            // PASO 5: Sincronizar datos del cliente con la obra
            log.info("🔗 Sincronizando datos del cliente...");
            sincronizarDatosClienteConObra(actualizado);

            // PASO 6: Calcular campos calculados antes de devolver
            log.info("🧮 Calculando campos calculados...");
            actualizado.calcularCamposCalculados();

            // ✅ NUEVO: Sincronizar TODOS los campos de la obra vinculada
            log.info("🔄 Sincronizando todos los campos con obra vinculada...");
            sincronizarPresupuestoConObra(actualizado);

            log.info("🎯 PROCESAMIENTO COMPLETO TERMINADO - Total final: {}", actualizado.getTotalPresupuesto());
            return actualizado;

        } catch (Exception e) {
            log.error("❌ ERROR EN PROCESAMIENTO COMPLETO - ID: {}", existente.getId(), e);
            log.error("❌ Detalles del error: {}", e.getMessage());
            throw new RuntimeException("Error en procesamiento completo: " + e.getMessage(), e);
        }
    }

    /**
     * Mapea los datos del DTO a la entidad presupuesto
     *
     * @param pnc            Presupuesto a actualizar
     * @param dto            DTO con los datos nuevos
     * @param esNuevaVersion Si es true, se considera una nueva versión (cambia fechaEmision)
     */
    protected PresupuestoNoCliente mapearDtoAPresupuesto(PresupuestoNoCliente pnc, PresupuestoNoClienteRequestDTO dto, boolean esNuevaVersion) {
        if (dto.getIdEmpresa() != null) {
            Empresa empresa = empresaRepository.findById(dto.getIdEmpresa()).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            pnc.setEmpresa(empresa);
        }

        // ========== MAPEAR OBRA (CRÍTICO PARA VERSIONADO) ==========
        // Si el DTO trae idObra, actualizar la relación
        // Si NO trae idObra, MANTENER la obra existente (no sobrescribir a null)
        if (dto.getIdObra() != null) {
            Obra obra = obraRepository.findById(dto.getIdObra())
                    .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada con ID: " + dto.getIdObra()));
            pnc.setObra(obra);
            log.info("🔗 Obra actualizada: ID={}", obra.getId());
        }
        // Si dto.getIdObra() == null, NO hacer nada (mantener pnc.getObra() como está)

        // ========== MAPEAR CLIENTE (CRÍTICO PARA VERSIONADO) ==========
        // Si el DTO trae idCliente, actualizar la relación
        // Si NO trae idCliente, MANTENER el cliente existente (no sobrescribir a null)
        if (dto.getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + dto.getIdCliente()));

            // Validar que el cliente pertenece a la empresa del presupuesto
            boolean perteneceAEmpresa = cliente.getEmpresas().stream()
                    .anyMatch(e -> e.getId().equals(pnc.getEmpresa().getId()));

            if (!perteneceAEmpresa) {
                throw new IllegalArgumentException("El cliente no pertenece a esta empresa");
            }

            pnc.setCliente(cliente);
            log.info("🔗 Cliente actualizado: ID={}", cliente.getId());
        }
        // Si dto.getIdCliente() == null, NO hacer nada (mantener pnc.getCliente() como está)

        pnc.setNombreSolicitante(dto.getNombreSolicitante());
        pnc.setDireccionParticular(dto.getDireccionParticular());
        
        // CRÍTICO: Preservar esPresupuestoTrabajoExtra ANTES de cualquier otra operación
        Boolean valorActualEsTrabajoExtra = pnc.getEsPresupuestoTrabajoExtra();
        Boolean valorDtoEsTrabajoExtra = dto.getEsPresupuestoTrabajoExtra();
        
        log.info("🔍 MAPEO - esPresupuestoTrabajoExtra → Entity actual: {}, DTO recibido: {}", 
                valorActualEsTrabajoExtra, valorDtoEsTrabajoExtra);
        
        if (dto.getEsPresupuestoTrabajoExtra() != null) {
            pnc.setEsPresupuestoTrabajoExtra(dto.getEsPresupuestoTrabajoExtra());
            log.info("✅ esPresupuestoTrabajoExtra actualizado desde DTO: {}", dto.getEsPresupuestoTrabajoExtra());
        } else {
            // PRESERVAR valor existente - NO sobrescribir con null o false
            log.info("⚠️ DTO no trae esPresupuestoTrabajoExtra - PRESERVANDO valor actual: {}", valorActualEsTrabajoExtra);
        }

        pnc.setDireccionObraCalle(dto.getDireccionObraCalle());
        pnc.setDireccionObraAltura(dto.getDireccionObraAltura());
        pnc.setDireccionObraPiso(dto.getDireccionObraPiso());
        pnc.setDireccionObraDepartamento(dto.getDireccionObraDepartamento());
        pnc.setDireccionObraBarrio(dto.getDireccionObraBarrio());
        pnc.setDireccionObraTorre(dto.getDireccionObraTorre());

        // Mapear nombreObra (DESPUÉS de setear/preservar esPresupuestoTrabajoExtra)
        if (dto.getNombreObra() != null) {
            pnc.setNombreObra(dto.getNombreObra());

            log.info("🔍 VALIDACIÓN NOMBRE - Obra asociada: {}, esPresupuestoTrabajoExtra: {}", 
                    pnc.getObra() != null ? pnc.getObra().getId() : "null",
                    pnc.getEsPresupuestoTrabajoExtra());

            // Si el presupuesto tiene obra asociada, actualizar también el nombre de la obra
            // IMPORTANTE: NO actualizar si es presupuesto trabajo extra (la obra asociada es la obra PADRE)
            if (pnc.getObra() != null && Boolean.TRUE.equals(pnc.getEsPresupuestoTrabajoExtra())) {
                // ES TRABAJO EXTRA → NO TOCAR LA OBRA PADRE
                log.warn("🚫 PRESUPUESTO TRABAJO EXTRA {} - NO SE MODIFICA OBRA PADRE {} (nombre presupuesto: '{}', nombre obra padre se mantiene: '{}')",
                        pnc.getId(), pnc.getObra().getId(), dto.getNombreObra(), pnc.getObra().getNombre());
            } else if (pnc.getObra() != null) {
                // NO ES TRABAJO EXTRA → SÍ ACTUALIZAR LA OBRA NORMAL
                log.info("🔗 Presupuesto NORMAL - Actualizando nombre en obra asociada ID: {}", pnc.getObra().getId());
                pnc.getObra().setNombre(dto.getNombreObra());
                obraRepository.save(pnc.getObra());
                log.info("✅ Nombre de obra actualizado en tabla obras: '{}'", dto.getNombreObra());
            }
        }

        pnc.setDescripcion(dto.getDescripcion());

        // Campos adicionales de descripción y observaciones
        pnc.setDescripcionDetallada(dto.getDescripcionDetallada());
        pnc.setObservacionesInternas(dto.getObservacionesInternas());
        pnc.setNotasAdicionales(dto.getNotasAdicionales());
        pnc.setEspecificacionesTecnicas(dto.getEspecificacionesTecnicas());
        pnc.setComentariosCliente(dto.getComentariosCliente());
        pnc.setRequisitosEspeciales(dto.getRequisitosEspeciales());

        // Calcular totales (sin JSON)
        double totalProf = 0.0;
        if (dto.getProfesionales() != null) {
            for (var prof : dto.getProfesionales()) {
                if (prof.getUnidadActiva() == null)
                    throw new IllegalArgumentException("unidadActiva es obligatoria para cada profesional");
                Double cantidad = prof.getCantidad() != null ? prof.getCantidad() : 0.0;
                Double importe = prof.getImportePorUnidad();
                if (importe == null) {
                    String u = prof.getUnidadActiva().toLowerCase();
                    if (u.contains("hora")) importe = prof.getImporteXHora();
                    else if (u.contains("dia") || u.contains("dí")) importe = prof.getImporteXDia();
                    else if (u.contains("semana")) importe = prof.getImporteXSemana();
                    else if (u.contains("mes")) importe = prof.getImporteXMes();
                    else if (u.contains("obra")) importe = prof.getImporteXObra();
                }
                importe = importe != null ? importe : 0.0;
                if (cantidad < 0 || importe < 0)
                    throw new IllegalArgumentException("cantidad e importe deben ser >= 0");
                totalProf += cantidad * importe;
            }
        }
        pnc.setTotalProfesionales(totalProf);

        double totalMat = 0.0;
        if (dto.getMaterialesList() != null) {
            for (var mat : dto.getMaterialesList()) {
                Double cantidad = mat.getCantidad() != null ? mat.getCantidad() : 0.0;
                Double precio = mat.getPrecioUnitario() != null ? mat.getPrecioUnitario() : 0.0;
                if (cantidad < 0 || precio < 0)
                    throw new IllegalArgumentException("cantidad y precioUnitario deben ser >= 0");
                totalMat += cantidad * precio;
            }
        }
        pnc.setTotalMateriales(totalMat);

        // Total otros costos removido - sistema antiguo eliminado
        double totalOtrosCostos = 0.0;

        // Honorarios de dirección de obra
        pnc.setHonorarioDireccionValorFijo(dto.getHonorarioDireccionValorFijo());
        pnc.setHonorarioDireccionPorcentaje(dto.getHonorarioDireccionPorcentaje());

        // Calcular importe de honorarios de dirección
        double honorarioDireccionImporte = 0.0;
        if (dto.getHonorarioDireccionPorcentaje() != null && dto.getHonorarioDireccionPorcentaje() > 0) {
            double baseCalculo = totalProf + totalMat + totalOtrosCostos;
            honorarioDireccionImporte = baseCalculo * (dto.getHonorarioDireccionPorcentaje() / 100.0);
        } else if (dto.getHonorarioDireccionValorFijo() != null && dto.getHonorarioDireccionValorFijo() > 0) {
            honorarioDireccionImporte = dto.getHonorarioDireccionValorFijo();
        }
        pnc.setHonorarioDireccionImporte(honorarioDireccionImporte);

        // ========== MAPEAR CONFIGURACIÓN DE HONORARIOS ==========
        pnc.setHonorariosAplicarATodos(dto.getHonorariosAplicarATodos());
        pnc.setHonorariosValorGeneral(dto.getHonorariosValorGeneral());
        pnc.setHonorariosTipoGeneral(dto.getHonorariosTipoGeneral());

        pnc.setHonorariosProfesionalesActivo(dto.getHonorariosProfesionalesActivo());
        pnc.setHonorariosProfesionalesTipo(dto.getHonorariosProfesionalesTipo());
        pnc.setHonorariosProfesionalesValor(dto.getHonorariosProfesionalesValor());

        pnc.setHonorariosMaterialesActivo(dto.getHonorariosMaterialesActivo());
        pnc.setHonorariosMaterialesTipo(dto.getHonorariosMaterialesTipo());
        pnc.setHonorariosMaterialesValor(dto.getHonorariosMaterialesValor());

        pnc.setHonorariosOtrosCostosActivo(dto.getHonorariosOtrosCostosActivo());
        pnc.setHonorariosOtrosCostosTipo(dto.getHonorariosOtrosCostosTipo());
        pnc.setHonorariosOtrosCostosValor(dto.getHonorariosOtrosCostosValor());

        pnc.setHonorariosConfiguracionPresupuestoActivo(dto.getHonorariosConfiguracionPresupuestoActivo());
        pnc.setHonorariosConfiguracionPresupuestoTipo(dto.getHonorariosConfiguracionPresupuestoTipo());
        pnc.setHonorariosConfiguracionPresupuestoValor(dto.getHonorariosConfiguracionPresupuestoValor());

        // Honorarios por Rubro (relación @OneToMany)
        if (dto.getHonorariosPorRubro() != null) {
            pnc.getHonorariosPorRubro().clear();
            pnc.getHonorariosPorRubro().addAll(mapearHonorariosPorRubroDTO(dto.getHonorariosPorRubro(), pnc));
        }

        // ========== MAPEAR CONFIGURACIÓN DE CÁLCULO DE DÍAS HÁBILES ==========
        pnc.setCalculoAutomaticoDiasHabiles(dto.getCalculoAutomaticoDiasHabiles());

        // ========== MAPEAR CONFIGURACIÓN DE MAYORES COSTOS ==========
        pnc.setMayoresCostosAplicarValorGeneral(dto.getMayoresCostosAplicarValorGeneral());
        pnc.setMayoresCostosValorGeneral(dto.getMayoresCostosValorGeneral());
        pnc.setMayoresCostosTipoGeneral(dto.getMayoresCostosTipoGeneral());
        pnc.setMayoresCostosGeneralImportado(dto.getMayoresCostosGeneralImportado());
        pnc.setMayoresCostosRubroImportado(dto.getMayoresCostosRubroImportado());
        pnc.setMayoresCostosNombreRubroImportado(dto.getMayoresCostosNombreRubroImportado());

        pnc.setMayoresCostosProfesionalesActivo(dto.getMayoresCostosProfesionalesActivo());
        pnc.setMayoresCostosProfesionalesTipo(dto.getMayoresCostosProfesionalesTipo());
        pnc.setMayoresCostosProfesionalesValor(dto.getMayoresCostosProfesionalesValor());

        pnc.setMayoresCostosMaterialesActivo(dto.getMayoresCostosMaterialesActivo());
        pnc.setMayoresCostosMaterialesTipo(dto.getMayoresCostosMaterialesTipo());
        pnc.setMayoresCostosMaterialesValor(dto.getMayoresCostosMaterialesValor());

        pnc.setMayoresCostosOtrosCostosActivo(dto.getMayoresCostosOtrosCostosActivo());
        pnc.setMayoresCostosOtrosCostosTipo(dto.getMayoresCostosOtrosCostosTipo());
        pnc.setMayoresCostosOtrosCostosValor(dto.getMayoresCostosOtrosCostosValor());

        pnc.setMayoresCostosConfiguracionPresupuestoActivo(dto.getMayoresCostosConfiguracionPresupuestoActivo());
        pnc.setMayoresCostosConfiguracionPresupuestoTipo(dto.getMayoresCostosConfiguracionPresupuestoTipo());
        pnc.setMayoresCostosConfiguracionPresupuestoValor(dto.getMayoresCostosConfiguracionPresupuestoValor());

        pnc.setMayoresCostosHonorariosActivo(dto.getMayoresCostosHonorariosActivo());
        pnc.setMayoresCostosHonorariosTipo(dto.getMayoresCostosHonorariosTipo());
        pnc.setMayoresCostosHonorariosValor(dto.getMayoresCostosHonorariosValor());

        // Explicación/justificación INTERNA de mayores costos
        pnc.setMayoresCostosExplicacion(dto.getMayoresCostosExplicacion());

        // Mayores Costos por Rubro (relación @OneToMany)
        if (dto.getMayoresCostosPorRubro() != null) {
            pnc.getMayoresCostosPorRubro().clear();
            pnc.getMayoresCostosPorRubro().addAll(mapearMayoresCostosPorRubroDTO(dto.getMayoresCostosPorRubro(), pnc));
        }

        // ========== MAPEAR CONFIGURACIÓN DE DESCUENTOS (Modelo Relacional) ==========
        pnc.setDescuentosExplicacion(dto.getDescuentosExplicacion());
        
        pnc.setDescuentosJornalesActivo(dto.getDescuentosJornalesActivo());
        pnc.setDescuentosJornalesTipo(dto.getDescuentosJornalesTipo());
        if (dto.getDescuentosJornalesValor() != null) {
            pnc.setDescuentosJornalesValor(BigDecimal.valueOf(dto.getDescuentosJornalesValor()));
        }
        
        pnc.setDescuentosMaterialesActivo(dto.getDescuentosMaterialesActivo());
        pnc.setDescuentosMaterialesTipo(dto.getDescuentosMaterialesTipo());
        if (dto.getDescuentosMaterialesValor() != null) {
            pnc.setDescuentosMaterialesValor(BigDecimal.valueOf(dto.getDescuentosMaterialesValor()));
        }
        
        pnc.setDescuentosHonorariosActivo(dto.getDescuentosHonorariosActivo());
        pnc.setDescuentosHonorariosTipo(dto.getDescuentosHonorariosTipo());
        if (dto.getDescuentosHonorariosValor() != null) {
            pnc.setDescuentosHonorariosValor(BigDecimal.valueOf(dto.getDescuentosHonorariosValor()));
        }
        
        pnc.setDescuentosMayoresCostosActivo(dto.getDescuentosMayoresCostosActivo());
        pnc.setDescuentosMayoresCostosTipo(dto.getDescuentosMayoresCostosTipo());
        if (dto.getDescuentosMayoresCostosValor() != null) {
            pnc.setDescuentosMayoresCostosValor(BigDecimal.valueOf(dto.getDescuentosMayoresCostosValor()));
        }
        
        // ========== MAPEAR SUB-TIPOS DE DESCUENTOS SOBRE HONORARIOS ==========
        // Descuentos sobre Honorarios de JORNALES
        pnc.setDescuentosHonorariosJornalesActivo(dto.getDescuentosHonorariosJornalesActivo());
        pnc.setDescuentosHonorariosJornalesTipo(dto.getDescuentosHonorariosJornalesTipo());
        if (dto.getDescuentosHonorariosJornalesValor() != null) {
            pnc.setDescuentosHonorariosJornalesValor(dto.getDescuentosHonorariosJornalesValor());
        }
        
        // Descuentos sobre Honorarios de PROFESIONALES
        pnc.setDescuentosHonorariosProfesionalesActivo(dto.getDescuentosHonorariosProfesionalesActivo());
        pnc.setDescuentosHonorariosProfesionalesTipo(dto.getDescuentosHonorariosProfesionalesTipo());
        if (dto.getDescuentosHonorariosProfesionalesValor() != null) {
            pnc.setDescuentosHonorariosProfesionalesValor(dto.getDescuentosHonorariosProfesionalesValor());
        }
        
        // Descuentos sobre Honorarios de MATERIALES
        pnc.setDescuentosHonorariosMaterialesActivo(dto.getDescuentosHonorariosMaterialesActivo());
        pnc.setDescuentosHonorariosMaterialesTipo(dto.getDescuentosHonorariosMaterialesTipo());
        if (dto.getDescuentosHonorariosMaterialesValor() != null) {
            pnc.setDescuentosHonorariosMaterialesValor(dto.getDescuentosHonorariosMaterialesValor());
        }
        
        // Descuentos sobre Honorarios de OTROS COSTOS
        pnc.setDescuentosHonorariosOtrosActivo(dto.getDescuentosHonorariosOtrosActivo());
        pnc.setDescuentosHonorariosOtrosTipo(dto.getDescuentosHonorariosOtrosTipo());
        if (dto.getDescuentosHonorariosOtrosValor() != null) {
            pnc.setDescuentosHonorariosOtrosValor(dto.getDescuentosHonorariosOtrosValor());
        }
        
        // Descuentos sobre Honorarios de GASTOS GENERALES
        pnc.setDescuentosHonorariosGastosGeneralesActivo(dto.getDescuentosHonorariosGastosGeneralesActivo());
        pnc.setDescuentosHonorariosGastosGeneralesTipo(dto.getDescuentosHonorariosGastosGeneralesTipo());
        if (dto.getDescuentosHonorariosGastosGeneralesValor() != null) {
            pnc.setDescuentosHonorariosGastosGeneralesValor(dto.getDescuentosHonorariosGastosGeneralesValor());
        }
        
        // Descuentos sobre Honorarios de CONFIGURACIÓN DE PRESUPUESTO
        pnc.setDescuentosHonorariosConfiguracionActivo(dto.getDescuentosHonorariosConfiguracionActivo());
        pnc.setDescuentosHonorariosConfiguracionTipo(dto.getDescuentosHonorariosConfiguracionTipo());
        if (dto.getDescuentosHonorariosConfiguracionValor() != null) {
            pnc.setDescuentosHonorariosConfiguracionValor(dto.getDescuentosHonorariosConfiguracionValor());
        }
        
        // Validar descuentos antes de guardar
        String errorValidacionDesc = pnc.validarDescuentos();
        if (errorValidacionDesc != null) {
            log.error("❌ Error en validación de descuentos: {}", errorValidacionDesc);
            throw new IllegalArgumentException("Descuentos inválidos: " + errorValidacionDesc);
        }

        // Descuentos por Rubro (relación @OneToMany)
        if (dto.getDescuentosPorRubro() != null) {
            pnc.getDescuentosPorRubro().clear();
            pnc.getDescuentosPorRubro().addAll(mapearDescuentosPorRubroDTO(dto.getDescuentosPorRubro(), pnc));
        }

        // total general actualizado
        pnc.setTotalGeneral(totalProf + totalMat + totalOtrosCostos + honorarioDireccionImporte);

        // ========== MAPEAR TOTALES ESPECÍFICOS DEL FRONTEND ==========
        // Estos campos vienen calculados desde el frontend y se guardan tal como llegan
        pnc.setTotalPresupuesto(dto.getTotalPresupuesto());
        pnc.setTotalHonorariosCalculado(dto.getTotalHonorarios());
        pnc.setTotalPresupuestoConHonorarios(dto.getTotalPresupuestoConHonorarios());
        if (dto.getTotalConDescuentos() != null) {
            pnc.setTotalConDescuentos(dto.getTotalConDescuentos());
        }
        
        // Mapear totales calculados de mayores costos y descuentos por rubro
        pnc.setTotalMayoresCostosPorRubro(dto.getTotalMayoresCostosPorRubro() != null ? dto.getTotalMayoresCostosPorRubro() : java.math.BigDecimal.ZERO);
        pnc.setTotalDescuentosPorRubro(dto.getTotalDescuentosPorRubro() != null ? dto.getTotalDescuentosPorRubro() : java.math.BigDecimal.ZERO);

        // ========== VALIDAR COHERENCIA DE TOTALES ==========
        validarCoherenciaTotales(pnc);

        // ========== NO recalcular si los totales vienen del frontend ==========
        // Si el DTO trae los totales, no recalcular aquí para evitar perder el valor correcto
        // Solo recalcular si los campos son nulos
        if (pnc.getTotalPresupuestoConHonorarios() == null) {
            pnc.calcularCamposCalculados();
        }

        log.info("📊 Totales del frontend: base={}, honorarios={}, total={}",
                dto.getTotalPresupuesto(), dto.getTotalHonorarios(), dto.getTotalPresupuestoConHonorarios());
        log.info("📊 Totales recalculados: base={}, honorarios={}, mayoresCostos={}, total={}",
                pnc.getTotalPresupuesto(), pnc.getTotalHonorariosCalculado(),
                pnc.getTotalMayoresCostos(), pnc.getTotalPresupuestoConHonorarios());
        
        /* LEGACY CODE: Profesionales, Materiales y Jornales ahora están en items_calculadora
        // Limpiar colecciones existentes y mapear nuevas
        pnc.getProfesionales().clear();
        if (dto.getProfesionales() != null) {
            for (var profDto : dto.getProfesionales()) {
                PresupuestoNoClienteProfesional prof = new PresupuestoNoClienteProfesional();
                prof.setPresupuestoNoCliente(pnc);
                prof.setEmpresa(pnc.getEmpresa());
                prof.setTipoProfesional(profDto.getTipoProfesional());
                prof.setImporteHora(profDto.getImporteXHora() != null ? BigDecimal.valueOf(profDto.getImporteXHora()) : null);
                prof.setImporteDia(profDto.getImporteXDia() != null ? BigDecimal.valueOf(profDto.getImporteXDia()) : null);
                prof.setImporteSemana(profDto.getImporteXSemana() != null ? BigDecimal.valueOf(profDto.getImporteXSemana()) : null);
                prof.setImporteMes(profDto.getImporteXMes() != null ? BigDecimal.valueOf(profDto.getImporteXMes()) : null);
                prof.setCantidadHoras(profDto.getCantidadHoras());
                prof.setCantidadDias(profDto.getCantidadDias());
                prof.setCantidadSemanas(profDto.getCantidadSemanas());
                prof.setCantidadMeses(profDto.getCantidadMeses());
                prof.setSubtotal(profDto.getImporteCalculado() != null ? BigDecimal.valueOf(profDto.getImporteCalculado()) : BigDecimal.ZERO);
                
                // Nuevos campos para tipoUnidad y cantidadJornales
                prof.setTipoUnidad(profDto.getTipoUnidad() != null && !profDto.getTipoUnidad().isEmpty() ? profDto.getTipoUnidad().toLowerCase() : "jornales");
                prof.setCantidadJornales(profDto.getCantidadJornales() != null ? BigDecimal.valueOf(profDto.getCantidadJornales()) : null);
                
                pnc.getProfesionales().add(prof);
            }
        }

        pnc.getMateriales().clear();
        if (dto.getMaterialesList() != null) {
            for (var matDto : dto.getMaterialesList()) {
                PresupuestoNoClienteMaterial mat = new PresupuestoNoClienteMaterial();
                mat.setPresupuestoNoCliente(pnc);
                mat.setEmpresa(pnc.getEmpresa());
                mat.setNombreMaterial(matDto.getTipoMaterial());
                mat.setCantidad(matDto.getCantidad() != null ? BigDecimal.valueOf(matDto.getCantidad()) : BigDecimal.ZERO);
                mat.setPrecioUnitario(matDto.getPrecioUnitario() != null ? BigDecimal.valueOf(matDto.getPrecioUnitario()) : BigDecimal.ZERO);
                mat.setUnidadMedida("unidad");
                BigDecimal subtotal = mat.getCantidad().multiply(mat.getPrecioUnitario());
                mat.setSubtotal(subtotal);
                pnc.getMateriales().add(mat);
            }
        }

        // ========== MAPEAR JORNALES ==========
        // NO usar clear() porque orphanRemoval=true eliminará los registros ANTES de agregar los nuevos
        // En su lugar, eliminar todos manualmente y agregar los nuevos
        if (pnc.getId() != null) {
            // Si es actualización, eliminar jornales existentes explícitamente
            if (!pnc.getJornales().isEmpty()) {
                log.info("🗑️ Eliminando {} jornales existentes", pnc.getJornales().size());
                // jornalRepository.deleteAll(pnc.getJornales());
                pnc.getJornales().clear();
            }
        } else {
            // Si es creación nueva, simplemente limpiar
            pnc.getJornales().clear();
        }
        
        if (dto.getJornales() != null && !dto.getJornales().isEmpty()) {
            log.info("🛠️ Procesando {} jornales desde DTO", dto.getJornales().size());
            for (var jornalDto : dto.getJornales()) {
                PresupuestoNoClienteJornal jornal = new PresupuestoNoClienteJornal();
                jornal.setPresupuestoNoCliente(pnc);
                jornal.setEmpresa(pnc.getEmpresa());
                jornal.setRol(jornalDto.getRol());
                jornal.setCantidad(jornalDto.getCantidad() != null ? jornalDto.getCantidad() : BigDecimal.ZERO);
                jornal.setValorUnitario(jornalDto.getValorUnitario() != null ? jornalDto.getValorUnitario() : BigDecimal.ZERO);
                BigDecimal subtotal = jornal.getCantidad().multiply(jornal.getValorUnitario());
                jornal.setSubtotal(subtotal);
                jornal.setObservaciones(jornalDto.getObservaciones());
                pnc.getJornales().add(jornal);
            }
            log.info("✅ {} jornales añadidos al presupuesto", pnc.getJornales().size());
        }
        */

        log.info("ℹ️ Código legacy comentado - ahora se usa items_calculadora_presupuesto");

        // TODO: Procesar items de calculadora si vienen en el DTO

        // ========== MAPEAR CONFIGURACIÓN DE JORNALES EN HONORARIOS Y MAYORES COSTOS ==========
        // DEFENSIVO: Solo actualizar si el DTO trae valores (evitar sobrescribir con NULL)
        log.info("🔍 DEBUG HONORARIOS JORNALES - Recibido del DTO: activo={}, tipo={}, valor={}",
                dto.getHonorariosJornalesActivo(), dto.getHonorariosJornalesTipo(), dto.getHonorariosJornalesValor());

        if (dto.getHonorariosJornalesActivo() != null) {
            pnc.setHonorariosJornalesActivo(dto.getHonorariosJornalesActivo());
        }
        if (dto.getHonorariosJornalesTipo() != null) {
            pnc.setHonorariosJornalesTipo(dto.getHonorariosJornalesTipo());
        }
        if (dto.getHonorariosJornalesValor() != null) {
            pnc.setHonorariosJornalesValor(dto.getHonorariosJornalesValor());
        }

        log.info("✅ HONORARIOS JORNALES - Asignado a entidad: activo={}, tipo={}, valor={}",
                pnc.getHonorariosJornalesActivo(), pnc.getHonorariosJornalesTipo(), pnc.getHonorariosJornalesValor());

        if (dto.getMayoresCostosJornalesActivo() != null) {
            pnc.setMayoresCostosJornalesActivo(dto.getMayoresCostosJornalesActivo());
        }
        if (dto.getMayoresCostosJornalesTipo() != null) {
            pnc.setMayoresCostosJornalesTipo(dto.getMayoresCostosJornalesTipo());
        }
        if (dto.getMayoresCostosJornalesValor() != null) {
            pnc.setMayoresCostosJornalesValor(dto.getMayoresCostosJornalesValor());
        }

        // OtrosCostos eliminado - tabla presupuesto_otro_costo removida del sistema

        pnc.setTiempoEstimadoTerminacion(dto.getTiempoEstimadoTerminacion());
        if (dto.getFechaCreacion() != null) {
            pnc.setFechaCreacion(dto.getFechaCreacion());
        }

        // ========== TIPO DE PRESUPUESTO ==========
        if (dto.getTipoPresupuesto() != null && !dto.getTipoPresupuesto().trim().isEmpty()) {
            try {
                TipoPresupuesto tipo =
                        TipoPresupuesto.valueOf(dto.getTipoPresupuesto().toUpperCase());
                pnc.setTipoPresupuesto(tipo);
                log.info("📋 Tipo de presupuesto actualizado a: {}", tipo);
            } catch (IllegalArgumentException e) {
                log.warn("⚠️ Tipo de presupuesto inválido: {}. Manteniendo valor actual.", dto.getTipoPresupuesto());
            }
        }

        // ========== ESTADO ==========
        if (dto.getEstado() != null) {
            com.rodrigo.construccion.enums.PresupuestoEstado e = com.rodrigo.construccion.enums.PresupuestoEstado.fromString(dto.getEstado());
            if (e != null) {
                pnc.setEstado(e);
                // Registrar timestamp de modificación manual para protección contra cambios automáticos
                pnc.setFechaUltimaModificacionEstado(java.time.LocalDateTime.now());
                log.debug("📝 Estado modificado manualmente a: {}", e);
            }
        }
        pnc.setFechaProbableInicio(dto.getFechaProbableInicio());
        pnc.setTelefono(dto.getTelefono());
        pnc.setMail(dto.getMail());
        pnc.setVencimiento(dto.getVencimiento());
        pnc.setObservaciones(dto.getObservaciones());

        // Guardar el presupuesto
        PresupuestoNoCliente actualizado = repository.save(pnc);

        // Procesar items de calculadora si vienen en el DTO (actualizar o crear)
        procesarItemsCalculadora(actualizado, dto.getItemsCalculadora());

        // Procesar costos iniciales si vienen en el DTO (actualizar o crear)
        procesarCostosIniciales(actualizado, dto.getCostosIniciales());

        // Sincronizar datos del cliente con la obra (si existe obra asociada)
        sincronizarDatosClienteConObra(actualizado);

        // ...existing code...

        return actualizado;
    }

    /**
     * Versión de mapearDtoAPresupuesto que NO procesa items calculadora
     * Se usa cuando se va a copiar items desde otro presupuesto
     */
    protected PresupuestoNoCliente mapearDtoAPresupuestoSinItemsCalculadora(PresupuestoNoCliente pnc, PresupuestoNoClienteRequestDTO dto, boolean esNuevaVersion) {
        // ========== MAPEAR TODOS LOS CAMPOS EXCEPTO ITEMS CALCULADORA ==========
        if (dto.getIdEmpresa() != null) {
            Empresa empresa = empresaRepository.findById(dto.getIdEmpresa()).orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            pnc.setEmpresa(empresa);
        }

        // ========== MAPEAR OBRA (CRÍTICO PARA VERSIONADO) ==========
        if (dto.getIdObra() != null) {
            Obra obra = obraRepository.findById(dto.getIdObra())
                    .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada con ID: " + dto.getIdObra()));
            pnc.setObra(obra);
            log.info("🔗 Obra actualizada: ID={}", obra.getId());
        }

        // ========== MAPEAR CLIENTE (CRÍTICO PARA VERSIONADO) ==========
        if (dto.getIdCliente() != null) {
            Cliente cliente = clienteRepository.findById(dto.getIdCliente())
                    .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + dto.getIdCliente()));

            boolean perteneceAEmpresa = cliente.getEmpresas().stream()
                    .anyMatch(e -> e.getId().equals(pnc.getEmpresa().getId()));

            if (!perteneceAEmpresa) {
                throw new IllegalArgumentException("El cliente no pertenece a esta empresa");
            }

            pnc.setCliente(cliente);
            log.info("🔗 Cliente actualizado: ID={}", cliente.getId());
        }

        pnc.setNombreSolicitante(dto.getNombreSolicitante());
        pnc.setDireccionParticular(dto.getDireccionParticular());

        pnc.setDireccionObraCalle(dto.getDireccionObraCalle());
        pnc.setDireccionObraAltura(dto.getDireccionObraAltura());
        pnc.setDireccionObraPiso(dto.getDireccionObraPiso());
        pnc.setDireccionObraDepartamento(dto.getDireccionObraDepartamento());
        pnc.setDireccionObraBarrio(dto.getDireccionObraBarrio());
        pnc.setDireccionObraTorre(dto.getDireccionObraTorre());

        pnc.setDescripcion(dto.getDescripcion());

        // Campos adicionales de descripción y observaciones
        pnc.setDescripcionDetallada(dto.getDescripcionDetallada());
        pnc.setObservacionesInternas(dto.getObservacionesInternas());
        pnc.setNotasAdicionales(dto.getNotasAdicionales());
        pnc.setEspecificacionesTecnicas(dto.getEspecificacionesTecnicas());
        pnc.setComentariosCliente(dto.getComentariosCliente());
        pnc.setRequisitosEspeciales(dto.getRequisitosEspeciales());

        // Calcular totales básicos
        double totalProf = 0.0;
        if (dto.getProfesionales() != null) {
            for (var prof : dto.getProfesionales()) {
                if (prof.getUnidadActiva() == null)
                    throw new IllegalArgumentException("unidadActiva es obligatoria para cada profesional");
                Double cantidad = prof.getCantidad() != null ? prof.getCantidad() : 0.0;
                Double importe = prof.getImportePorUnidad();
                if (importe == null) {
                    String u = prof.getUnidadActiva().toLowerCase();
                    if (u.contains("hora")) importe = prof.getImporteXHora();
                    else if (u.contains("dia") || u.contains("dí")) importe = prof.getImporteXDia();
                    else if (u.contains("semana")) importe = prof.getImporteXSemana();
                    else if (u.contains("mes")) importe = prof.getImporteXMes();
                    else if (u.contains("obra")) importe = prof.getImporteXObra();
                }
                importe = importe != null ? importe : 0.0;
                if (cantidad < 0 || importe < 0)
                    throw new IllegalArgumentException("cantidad e importe deben ser >= 0");
                totalProf += cantidad * importe;
            }
        }
        pnc.setTotalProfesionales(totalProf);

        double totalMat = 0.0;
        if (dto.getMaterialesList() != null) {
            for (var mat : dto.getMaterialesList()) {
                Double cantidad = mat.getCantidad() != null ? mat.getCantidad() : 0.0;
                Double precio = mat.getPrecioUnitario() != null ? mat.getPrecioUnitario() : 0.0;
                if (cantidad < 0 || precio < 0)
                    throw new IllegalArgumentException("cantidad y precioUnitario deben ser >= 0");
                totalMat += cantidad * precio;
            }
        }
        pnc.setTotalMateriales(totalMat);

        // Total otros costos removido - sistema antiguo eliminado
        double totalOtrosCostos = 0.0;

        // Honorarios y configuración
        pnc.setHonorarioDireccionValorFijo(dto.getHonorarioDireccionValorFijo());
        pnc.setHonorarioDireccionPorcentaje(dto.getHonorarioDireccionPorcentaje());

        // Calcular importe de honorarios de dirección
        double honorarioDireccionImporte = 0.0;
        if (dto.getHonorarioDireccionPorcentaje() != null && dto.getHonorarioDireccionPorcentaje() > 0) {
            double baseCalculo = totalProf + totalMat + totalOtrosCostos;
            honorarioDireccionImporte = baseCalculo * (dto.getHonorarioDireccionPorcentaje() / 100.0);
        } else if (dto.getHonorarioDireccionValorFijo() != null && dto.getHonorarioDireccionValorFijo() > 0) {
            honorarioDireccionImporte = dto.getHonorarioDireccionValorFijo();
        }
        pnc.setHonorarioDireccionImporte(honorarioDireccionImporte);

        // ========== MAPEAR CONFIGURACIÓN DE HONORARIOS ==========
        pnc.setHonorariosAplicarATodos(dto.getHonorariosAplicarATodos());
        pnc.setHonorariosValorGeneral(dto.getHonorariosValorGeneral());
        pnc.setHonorariosTipoGeneral(dto.getHonorariosTipoGeneral());

        pnc.setHonorariosProfesionalesActivo(dto.getHonorariosProfesionalesActivo());
        pnc.setHonorariosProfesionalesTipo(dto.getHonorariosProfesionalesTipo());
        pnc.setHonorariosProfesionalesValor(dto.getHonorariosProfesionalesValor());

        pnc.setHonorariosMaterialesActivo(dto.getHonorariosMaterialesActivo());
        pnc.setHonorariosMaterialesTipo(dto.getHonorariosMaterialesTipo());
        pnc.setHonorariosMaterialesValor(dto.getHonorariosMaterialesValor());

        pnc.setHonorariosOtrosCostosActivo(dto.getHonorariosOtrosCostosActivo());
        pnc.setHonorariosOtrosCostosTipo(dto.getHonorariosOtrosCostosTipo());
        pnc.setHonorariosOtrosCostosValor(dto.getHonorariosOtrosCostosValor());

        pnc.setHonorariosConfiguracionPresupuestoActivo(dto.getHonorariosConfiguracionPresupuestoActivo());
        pnc.setHonorariosConfiguracionPresupuestoTipo(dto.getHonorariosConfiguracionPresupuestoTipo());
        pnc.setHonorariosConfiguracionPresupuestoValor(dto.getHonorariosConfiguracionPresupuestoValor());

        // Honorarios por Rubro (relación @OneToMany)
        if (dto.getHonorariosPorRubro() != null) {
            pnc.getHonorariosPorRubro().clear();
            pnc.getHonorariosPorRubro().addAll(mapearHonorariosPorRubroDTO(dto.getHonorariosPorRubro(), pnc));
        }

        // ========== MAPEAR CONFIGURACIÓN DE CÁLCULO DE DÍAS HÁBILES ==========
        pnc.setCalculoAutomaticoDiasHabiles(dto.getCalculoAutomaticoDiasHabiles());

        // ========== MAPEAR CONFIGURACIÓN DE MAYORES COSTOS ==========
        pnc.setMayoresCostosAplicarValorGeneral(dto.getMayoresCostosAplicarValorGeneral());
        pnc.setMayoresCostosValorGeneral(dto.getMayoresCostosValorGeneral());
        pnc.setMayoresCostosTipoGeneral(dto.getMayoresCostosTipoGeneral());
        pnc.setMayoresCostosGeneralImportado(dto.getMayoresCostosGeneralImportado());
        pnc.setMayoresCostosRubroImportado(dto.getMayoresCostosRubroImportado());
        pnc.setMayoresCostosNombreRubroImportado(dto.getMayoresCostosNombreRubroImportado());

        pnc.setMayoresCostosProfesionalesActivo(dto.getMayoresCostosProfesionalesActivo());
        pnc.setMayoresCostosProfesionalesTipo(dto.getMayoresCostosProfesionalesTipo());
        pnc.setMayoresCostosProfesionalesValor(dto.getMayoresCostosProfesionalesValor());

        pnc.setMayoresCostosMaterialesActivo(dto.getMayoresCostosMaterialesActivo());
        pnc.setMayoresCostosMaterialesTipo(dto.getMayoresCostosMaterialesTipo());
        pnc.setMayoresCostosMaterialesValor(dto.getMayoresCostosMaterialesValor());

        pnc.setMayoresCostosOtrosCostosActivo(dto.getMayoresCostosOtrosCostosActivo());
        pnc.setMayoresCostosOtrosCostosTipo(dto.getMayoresCostosOtrosCostosTipo());
        pnc.setMayoresCostosOtrosCostosValor(dto.getMayoresCostosOtrosCostosValor());

        pnc.setMayoresCostosConfiguracionPresupuestoActivo(dto.getMayoresCostosConfiguracionPresupuestoActivo());
        pnc.setMayoresCostosConfiguracionPresupuestoTipo(dto.getMayoresCostosConfiguracionPresupuestoTipo());
        pnc.setMayoresCostosConfiguracionPresupuestoValor(dto.getMayoresCostosConfiguracionPresupuestoValor());

        pnc.setMayoresCostosHonorariosActivo(dto.getMayoresCostosHonorariosActivo());
        pnc.setMayoresCostosHonorariosTipo(dto.getMayoresCostosHonorariosTipo());
        pnc.setMayoresCostosHonorariosValor(dto.getMayoresCostosHonorariosValor());

        // Explicación/justificación INTERNA de mayores costos
        pnc.setMayoresCostosExplicacion(dto.getMayoresCostosExplicacion());

        // Mayores Costos por Rubro (relación @OneToMany)
        if (dto.getMayoresCostosPorRubro() != null) {
            pnc.getMayoresCostosPorRubro().clear();
            pnc.getMayoresCostosPorRubro().addAll(mapearMayoresCostosPorRubroDTO(dto.getMayoresCostosPorRubro(), pnc));
        }

        // ========== MAPEAR CONFIGURACIÓN DE DESCUENTOS (Modelo Relacional) ==========
        pnc.setDescuentosExplicacion(dto.getDescuentosExplicacion());
        
        pnc.setDescuentosJornalesActivo(dto.getDescuentosJornalesActivo());
        pnc.setDescuentosJornalesTipo(dto.getDescuentosJornalesTipo());
        if (dto.getDescuentosJornalesValor() != null) {
            pnc.setDescuentosJornalesValor(BigDecimal.valueOf(dto.getDescuentosJornalesValor()));
        }
        
        pnc.setDescuentosMaterialesActivo(dto.getDescuentosMaterialesActivo());
        pnc.setDescuentosMaterialesTipo(dto.getDescuentosMaterialesTipo());
        if (dto.getDescuentosMaterialesValor() != null) {
            pnc.setDescuentosMaterialesValor(BigDecimal.valueOf(dto.getDescuentosMaterialesValor()));
        }
        
        pnc.setDescuentosHonorariosActivo(dto.getDescuentosHonorariosActivo());
        pnc.setDescuentosHonorariosTipo(dto.getDescuentosHonorariosTipo());
        if (dto.getDescuentosHonorariosValor() != null) {
            pnc.setDescuentosHonorariosValor(BigDecimal.valueOf(dto.getDescuentosHonorariosValor()));
        }
        
        pnc.setDescuentosMayoresCostosActivo(dto.getDescuentosMayoresCostosActivo());
        pnc.setDescuentosMayoresCostosTipo(dto.getDescuentosMayoresCostosTipo());
        if (dto.getDescuentosMayoresCostosValor() != null) {
            pnc.setDescuentosMayoresCostosValor(BigDecimal.valueOf(dto.getDescuentosMayoresCostosValor()));
        }
        
        // ========== MAPEAR SUB-TIPOS DE DESCUENTOS SOBRE HONORARIOS ==========
        // Descuentos sobre Honorarios de JORNALES
        pnc.setDescuentosHonorariosJornalesActivo(dto.getDescuentosHonorariosJornalesActivo());
        pnc.setDescuentosHonorariosJornalesTipo(dto.getDescuentosHonorariosJornalesTipo());
        if (dto.getDescuentosHonorariosJornalesValor() != null) {
            pnc.setDescuentosHonorariosJornalesValor(dto.getDescuentosHonorariosJornalesValor());
        }
        
        // Descuentos sobre Honorarios de PROFESIONALES
        pnc.setDescuentosHonorariosProfesionalesActivo(dto.getDescuentosHonorariosProfesionalesActivo());
        pnc.setDescuentosHonorariosProfesionalesTipo(dto.getDescuentosHonorariosProfesionalesTipo());
        if (dto.getDescuentosHonorariosProfesionalesValor() != null) {
            pnc.setDescuentosHonorariosProfesionalesValor(dto.getDescuentosHonorariosProfesionalesValor());
        }
        
        // Descuentos sobre Honorarios de MATERIALES
        pnc.setDescuentosHonorariosMaterialesActivo(dto.getDescuentosHonorariosMaterialesActivo());
        pnc.setDescuentosHonorariosMaterialesTipo(dto.getDescuentosHonorariosMaterialesTipo());
        if (dto.getDescuentosHonorariosMaterialesValor() != null) {
            pnc.setDescuentosHonorariosMaterialesValor(dto.getDescuentosHonorariosMaterialesValor());
        }
        
        // Descuentos sobre Honorarios de OTROS COSTOS
        pnc.setDescuentosHonorariosOtrosActivo(dto.getDescuentosHonorariosOtrosActivo());
        pnc.setDescuentosHonorariosOtrosTipo(dto.getDescuentosHonorariosOtrosTipo());
        if (dto.getDescuentosHonorariosOtrosValor() != null) {
            pnc.setDescuentosHonorariosOtrosValor(dto.getDescuentosHonorariosOtrosValor());
        }
        
        // Descuentos sobre Honorarios de GASTOS GENERALES
        pnc.setDescuentosHonorariosGastosGeneralesActivo(dto.getDescuentosHonorariosGastosGeneralesActivo());
        pnc.setDescuentosHonorariosGastosGeneralesTipo(dto.getDescuentosHonorariosGastosGeneralesTipo());
        if (dto.getDescuentosHonorariosGastosGeneralesValor() != null) {
            pnc.setDescuentosHonorariosGastosGeneralesValor(dto.getDescuentosHonorariosGastosGeneralesValor());
        }
        
        // Descuentos sobre Honorarios de CONFIGURACIÓN DE PRESUPUESTO
        pnc.setDescuentosHonorariosConfiguracionActivo(dto.getDescuentosHonorariosConfiguracionActivo());
        pnc.setDescuentosHonorariosConfiguracionTipo(dto.getDescuentosHonorariosConfiguracionTipo());
        if (dto.getDescuentosHonorariosConfiguracionValor() != null) {
            pnc.setDescuentosHonorariosConfiguracionValor(dto.getDescuentosHonorariosConfiguracionValor());
        }
        
        // Validar descuentos antes de guardar
        String errorValidacionDesc = pnc.validarDescuentos();
        if (errorValidacionDesc != null) {
            log.error("❌ Error en validación de descuentos: {}", errorValidacionDesc);
            throw new IllegalArgumentException("Descuentos inválidos: " + errorValidacionDesc);
        }

        // Descuentos por Rubro (relación @OneToMany)
        if (dto.getDescuentosPorRubro() != null) {
            pnc.getDescuentosPorRubro().clear();
            pnc.getDescuentosPorRubro().addAll(mapearDescuentosPorRubroDTO(dto.getDescuentosPorRubro(), pnc));
        }

        // total general actualizado
        pnc.setTotalGeneral(totalProf + totalMat + totalOtrosCostos + honorarioDireccionImporte);

        // ========== MAPEAR TOTALES ESPECÍFICOS DEL FRONTEND ==========
        pnc.setTotalPresupuesto(dto.getTotalPresupuesto());
        pnc.setTotalHonorariosCalculado(dto.getTotalHonorarios());
        pnc.setTotalPresupuestoConHonorarios(dto.getTotalPresupuestoConHonorarios());
        if (dto.getTotalConDescuentos() != null) {
            pnc.setTotalConDescuentos(dto.getTotalConDescuentos());
        }
        
        // Mapear totales calculados de mayores costos y descuentos por rubro
        pnc.setTotalMayoresCostosPorRubro(dto.getTotalMayoresCostosPorRubro() != null ? dto.getTotalMayoresCostosPorRubro() : java.math.BigDecimal.ZERO);
        pnc.setTotalDescuentosPorRubro(dto.getTotalDescuentosPorRubro() != null ? dto.getTotalDescuentosPorRubro() : java.math.BigDecimal.ZERO);
        
        /* LEGACY: Profesionales y Materiales ahora en items_calculadora
        // Mapear colecciones básicas (profesionales, materiales)
        pnc.getProfesionales().clear();
        if (dto.getProfesionales() != null) {
            for (var profDto : dto.getProfesionales()) {
                PresupuestoNoClienteProfesional prof = new PresupuestoNoClienteProfesional();
                // ... código comentado ...
                pnc.getProfesionales().add(prof);
            }
        }

        pnc.getMateriales().clear();
        if (dto.getMaterialesList() != null) {
            for (var matDto : dto.getMaterialesList()) {
                PresupuestoNoClienteMaterial mat = new PresupuestoNoClienteMaterial();
                // ... código comentado ...
                pnc.getMateriales().add(mat);
            }
        }
        */

        // Mapear campos restantes
        pnc.setTiempoEstimadoTerminacion(dto.getTiempoEstimadoTerminacion());
        if (dto.getFechaCreacion() != null) {
            pnc.setFechaCreacion(dto.getFechaCreacion());
        }
        if (dto.getEstado() != null) {
            com.rodrigo.construccion.enums.PresupuestoEstado e = com.rodrigo.construccion.enums.PresupuestoEstado.fromString(dto.getEstado());
            if (e != null) {
                pnc.setEstado(e);
                // Registrar timestamp de modificación manual para protección contra cambios automáticos
                pnc.setFechaUltimaModificacionEstado(java.time.LocalDateTime.now());
                log.debug("📝 Estado modificado manualmente a: {}", e);
            }
        }
        pnc.setFechaProbableInicio(dto.getFechaProbableInicio());
        pnc.setTelefono(dto.getTelefono());
        pnc.setMail(dto.getMail());
        pnc.setVencimiento(dto.getVencimiento());
        pnc.setObservaciones(dto.getObservaciones());

        // Guardar el presupuesto (SIN procesar items calculadora)
        PresupuestoNoCliente actualizado = repository.save(pnc);

        // ❌ NO procesar items calculadora - se copiarán después desde presupuesto base
        log.info("🔄 Presupuesto guardado SIN procesar items calculadora (se copiarán después)");

        // Procesar costos iniciales si vienen en el DTO
        procesarCostosIniciales(actualizado, dto.getCostosIniciales());

        // Sincronizar datos del cliente con la obra (si existe obra asociada)
        sincronizarDatosClienteConObra(actualizado);

        // Calcular campos calculados antes de devolver
        actualizado.calcularCamposCalculados();

        log.info("✅ Nueva versión creada (sin items calculadora): ID {} | Versión {}",
                actualizado.getId(), actualizado.getNumeroVersion());

        return actualizado;
    }

    /**
     * Elimina un presupuesto específico sin renumerar las versiones restantes.
     *
     * @param id        ID del presupuesto a eliminar
     * @param empresaId ID de la empresa (para validación multi-tenant)
     * @return Información sobre la eliminación
     * @throws IllegalArgumentException si empresaId es null
     * @throws IllegalStateException    si el presupuesto está aprobado con obra asociada
     */
    @Transactional
    public EliminarPresupuestoResponse eliminar(Long id, Long empresaId) {
        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId es requerido");
        }

        // Obtener el presupuesto con validación de existencia y empresa
        PresupuestoNoCliente presupuesto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto con ID " + id + " no encontrado"));

        // Validar que pertenece a la empresa
        if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El presupuesto no pertenece a la empresa especificada");
        }

        // Guardar información antes de eliminar (incluyendo obraId si existe)
        Long numeroPresupuesto = presupuesto.getNumeroPresupuesto();
        Integer numeroVersion = presupuesto.getNumeroVersion();
        Long obraIdAsociado = presupuesto.getObra() != null ? presupuesto.getObra().getId() : null;

        // 🗑️ Eliminar dependencias primero (para evitar violación de FK)

        // 1. Eliminar asignaciones de materiales a obras que referencian materiales de este presupuesto
        Set<ItemCalculadoraPresupuesto> items = presupuesto.getItemsCalculadora();
        if (items != null) {
            for (ItemCalculadoraPresupuesto item : items) {
                List<MaterialCalculadora> materiales = item.getMaterialesLista();
                if (materiales != null) {
                    for (MaterialCalculadora material : materiales) {
                        log.info("🔗 Eliminando asignaciones de material {} a obras", material.getId());
                        obraMaterialRepository.deleteByMaterialCalculadoraId(material.getId());
                    }
                }
            }
        }

        // 2. Eliminar pagos de gastos generales
        log.info("🔗 Eliminando pagos de gastos generales asociados al presupuesto ID {}", id);
        pagoGastoGeneralObraRepository.deleteByPresupuestoNoClienteId(id);

        // Eliminar el presupuesto (cascada automática por JPA)
        // Las obras asociadas quedan intactas (el campo obra_id en presupuesto solo es informativo)
        repository.deleteById(id);

        String mensaje = String.format("🗑️ Presupuesto ID %d (número: %d, versión: %d) eliminado exitosamente",
                id, numeroPresupuesto, numeroVersion);

        if (obraIdAsociado != null) {
            mensaje += String.format(" (estaba asociado a Obra ID: %d)", obraIdAsociado);
        }

        log.info(mensaje);

        // 🔗 SINCRONIZACIÓN AUTOMÁTICA OBRA-PRESUPUESTO (después de eliminar)
        presupuestoObraSyncService.procesarEliminacionPresupuesto(obraIdAsociado, id);

        // Obtener información de versiones restantes
        List<PresupuestoNoCliente> versionesRestantes = repository.findAll().stream()
                .filter(p -> p.getNumeroPresupuesto().equals(numeroPresupuesto))
                .toList();

        Integer ultimaVersion = versionesRestantes.stream()
                .map(PresupuestoNoCliente::getNumeroVersion)
                .max(Integer::compareTo)
                .orElse(null);

        return new EliminarPresupuestoResponse(
                String.format("Presupuesto ID %d (versión %d) eliminado exitosamente", id, numeroVersion),
                versionesRestantes.size(),
                ultimaVersion
        );
    }

    /**
     * Reactiva una obra suspendida o cancelada vinculándola con un nuevo presupuesto aprobado.
     * <p>
     * Casos de uso:
     * - Obra suspendida por meses que se quiere reactivar
     * - Obra cancelada que se retoma con nuevo presupuesto
     * - Actualización de presupuesto por inflación/cambios de precios
     * <p>
     * Proceso:
     * 1. Valida que la obra esté SUSPENDIDA o CANCELADO
     * 2. Valida que el nuevo presupuesto esté APROBADO y sin obra vinculada
     * 3. Desvincular presupuesto anterior (si existe)
     * 4. Vincular nuevo presupuesto
     * 5. Sincronizar estado obra ← presupuesto
     * 6. Actualizar presupuesto estimado de la obra
     *
     * @param obraId             ID de la obra a reactivar
     * @param nuevoPresupuestoId ID del presupuesto aprobado para vincular
     * @param empresaId          ID de la empresa (validación multi-tenant)
     * @return Información sobre la revinculación
     * @throws IllegalArgumentException si obra o presupuesto no existen o no pertenecen a la empresa
     * @throws IllegalStateException    si las validaciones de estado fallan
     */
    @Transactional
    public ReactivarObraResponse reactivarObraConNuevoPresupuesto(
            Long obraId,
            Long nuevoPresupuestoId,
            Long empresaId) {

        log.info("🔄 Iniciando reactivación de Obra {} con Presupuesto {}", obraId, nuevoPresupuestoId);

        if (empresaId == null) {
            throw new IllegalArgumentException("empresaId es requerido");
        }

        // 1. Validar y obtener obra
        Obra obra = obraRepository.findById(obraId)
                .orElseThrow(() -> new IllegalArgumentException("Obra con ID " + obraId + " no encontrada"));

        if (!obra.getEmpresaId().equals(empresaId)) {
            throw new IllegalArgumentException("La obra no pertenece a la empresa especificada");
        }

        EstadoObra estadoActual = obra.getEstadoEnum();
        if (!estadoActual.equals(EstadoObra.SUSPENDIDA) &&
                !estadoActual.equals(EstadoObra.CANCELADO)) {
            throw new IllegalStateException(
                    String.format("Solo se pueden reactivar obras SUSPENDIDAS o CANCELADAS. Estado actual: %s",
                            estadoActual.getDisplayName())
            );
        }

        // 2. Validar y obtener nuevo presupuesto
        PresupuestoNoCliente nuevoPresupuesto = repository.findById(nuevoPresupuestoId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto con ID " + nuevoPresupuestoId + " no encontrado"));

        if (!nuevoPresupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException("El presupuesto no pertenece a la empresa especificada");
        }

        if (!nuevoPresupuesto.getEstado().equals(PresupuestoEstado.APROBADO)) {
            throw new IllegalStateException(
                    String.format("El presupuesto debe estar APROBADO. Estado actual: %s",
                            nuevoPresupuesto.getEstado())
            );
        }

        if (nuevoPresupuesto.getObra() != null) {
            throw new IllegalStateException(
                    String.format("El presupuesto ya está vinculado a la Obra ID: %d",
                            nuevoPresupuesto.getObra().getId())
            );
        }

        // 3. Desvincular presupuesto anterior (si existe)
        Long presupuestoAnteriorId = obra.getPresupuestoNoClienteId();
        if (presupuestoAnteriorId != null) {
            // Buscar el presupuesto anterior para desvincularlo
            PresupuestoNoCliente presupuestoAnterior = repository.findById(presupuestoAnteriorId)
                    .orElse(null); // Puede que ya no exista

            if (presupuestoAnterior != null) {
                presupuestoAnterior.setObra(null);
                repository.save(presupuestoAnterior);
                log.info("🔗 Presupuesto {} desvinculado de Obra {}",
                        presupuestoAnteriorId, obraId);
            }

            // Limpiar el ID en la obra
            obra.setPresupuestoNoClienteId(null);
        }

        // 4. Vincular nuevo presupuesto
        nuevoPresupuesto.setObra(obra);
        obra.setPresupuestoNoClienteId(nuevoPresupuesto.getId());

        // 5. Actualizar presupuesto estimado de la obra
        BigDecimal nuevoTotal = nuevoPresupuesto.getTotalPresupuesto();
        obra.setPresupuestoEstimado(nuevoTotal);

        // 6. Sincronizar estado obra ← presupuesto
        // Como el presupuesto está APROBADO, la obra pasará a APROBADO o EN_EJECUCION
        // dependiendo de la fecha de inicio
        EstadoObra estadoAnterior = obra.getEstadoEnum();
        sincronizarEstado(nuevoPresupuesto, obra);
        EstadoObra estadoNuevo = obra.getEstadoEnum();

        // 7. Guardar cambios
        repository.save(nuevoPresupuesto);
        obraRepository.save(obra);

        log.info("✅ Obra {} reactivada: {} → {}. Presupuesto anterior: {}, nuevo: {}",
                obraId,
                estadoAnterior.getDisplayName(),
                estadoNuevo.getDisplayName(),
                presupuestoAnteriorId != null ? presupuestoAnteriorId : "ninguno",
                nuevoPresupuestoId);

        return new ReactivarObraResponse(
                String.format("Obra %d reactivada exitosamente con Presupuesto %d", obraId, nuevoPresupuestoId),
                obraId,
                estadoAnterior.getDisplayName(),
                estadoNuevo.getDisplayName(),
                presupuestoAnteriorId,
                nuevoPresupuestoId,
                nuevoTotal
        );
    }

    /**
     * DTO de respuesta para reactivación de obra
     */
    public static class ReactivarObraResponse {
        private final String mensaje;
        private final Long obraId;
        private final String estadoAnterior;
        private final String estadoNuevo;
        private final Long presupuestoAnteriorId;
        private final Long nuevoPresupuestoId;
        private final BigDecimal nuevoPresupuestoTotal;

        public ReactivarObraResponse(
                String mensaje,
                Long obraId,
                String estadoAnterior,
                String estadoNuevo,
                Long presupuestoAnteriorId,
                Long nuevoPresupuestoId,
                BigDecimal nuevoPresupuestoTotal) {
            this.mensaje = mensaje;
            this.obraId = obraId;
            this.estadoAnterior = estadoAnterior;
            this.estadoNuevo = estadoNuevo;
            this.presupuestoAnteriorId = presupuestoAnteriorId;
            this.nuevoPresupuestoId = nuevoPresupuestoId;
            this.nuevoPresupuestoTotal = nuevoPresupuestoTotal;
        }

        public String getMensaje() {
            return mensaje;
        }

        public Long getObraId() {
            return obraId;
        }

        public String getEstadoAnterior() {
            return estadoAnterior;
        }

        public String getEstadoNuevo() {
            return estadoNuevo;
        }

        public Long getPresupuestoAnteriorId() {
            return presupuestoAnteriorId;
        }

        public Long getNuevoPresupuestoId() {
            return nuevoPresupuestoId;
        }

        public BigDecimal getNuevoPresupuestoTotal() {
            return nuevoPresupuestoTotal;
        }
    }

    /**
     * DTO de respuesta para eliminación de presupuesto
     */
    public static class EliminarPresupuestoResponse {
        private final String mensaje;
        private final int versionesRestantes;
        private final Integer ultimaVersion;

        public EliminarPresupuestoResponse(String mensaje, int versionesRestantes, Integer ultimaVersion) {
            this.mensaje = mensaje;
            this.versionesRestantes = versionesRestantes;
            this.ultimaVersion = ultimaVersion;
        }

        public String getMensaje() {
            return mensaje;
        }

        public int getVersionesRestantes() {
            return versionesRestantes;
        }

        public Integer getUltimaVersion() {
            return ultimaVersion;
        }
    }

    @Transactional
    public PresupuestoNoCliente actualizarPorDireccion(String direccionObraCalle, String direccionObraAltura, String direccionObraPiso, String direccionObraDepartamento,
                                                       Integer numeroVersion,
                                                       PresupuestoNoClienteRequestDTO dto) {
        // Buscar presupuestos por dirección
        List<PresupuestoNoCliente> presupuestos = repository.findByDireccionObra(direccionObraCalle, direccionObraAltura, direccionObraPiso, direccionObraDepartamento);

        if (presupuestos.isEmpty()) {
            throw new IllegalArgumentException("No se encontró ningún presupuesto con la dirección especificada");
        }

        // Encontrar el presupuesto base (el que queremos versionar)
        PresupuestoNoCliente presupuestoBase;
        if (numeroVersion != null) {
            presupuestoBase = presupuestos.stream()
                    .filter(p -> numeroVersion.equals(p.getNumeroVersion()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException(
                            "No se encontró presupuesto con la dirección y versión especificadas"));
        } else {
            // Si no se especifica versión, tomar la más reciente
            presupuestoBase = presupuestos.get(0);
        }

        // CREAR NUEVO PRESUPUESTO (nueva versión) en lugar de modificar el existente
        PresupuestoNoCliente nuevaVersion = new PresupuestoNoCliente();

        // Copiar número de presupuesto del original e incrementar versión
        nuevaVersion.setNumeroPresupuesto(presupuestoBase.getNumeroPresupuesto());
        nuevaVersion.setNumeroVersion(presupuestoBase.getNumeroVersion() + 1);

        // ========== COPIAR CAMPO ES TRABAJO EXTRA (INMUTABLE) ==========
        // Este campo NO debe cambiar entre versiones - copiar SIEMPRE del presupuesto base
        nuevaVersion.setEsPresupuestoTrabajoExtra(presupuestoBase.getEsPresupuestoTrabajoExtra());
        log.info("🔒 esPresupuestoTrabajoExtra INMUTABLE copiado desde presupuesto base: {}", 
                presupuestoBase.getEsPresupuestoTrabajoExtra());

        // Copiar empresa del presupuesto base (o del DTO si viene)
        if (dto.getIdEmpresa() != null) {
            Empresa empresa = empresaRepository.findById(dto.getIdEmpresa())
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada"));
            nuevaVersion.setEmpresa(empresa);
        } else {
            nuevaVersion.setEmpresa(presupuestoBase.getEmpresa());
        }

        // Establecer fechas
        LocalDate ahora = LocalDate.now();
        nuevaVersion.setFechaEmision(ahora);
        nuevaVersion.setFechaCreacion(dto.getFechaCreacion() != null ? dto.getFechaCreacion() : ahora);

        // Copiar datos del DTO
        nuevaVersion.setNombreSolicitante(dto.getNombreSolicitante());
        nuevaVersion.setDireccionParticular(dto.getDireccionParticular());
        nuevaVersion.setDireccionObraCalle(dto.getDireccionObraCalle());
        nuevaVersion.setDireccionObraAltura(dto.getDireccionObraAltura());
        nuevaVersion.setDireccionObraPiso(dto.getDireccionObraPiso());
        nuevaVersion.setDireccionObraDepartamento(dto.getDireccionObraDepartamento());
        nuevaVersion.setDireccionObraBarrio(dto.getDireccionObraBarrio());
        nuevaVersion.setDireccionObraTorre(dto.getDireccionObraTorre());
        nuevaVersion.setDescripcion(dto.getDescripcion());

        // Campos adicionales de descripción y observaciones
        nuevaVersion.setDescripcionDetallada(dto.getDescripcionDetallada());
        nuevaVersion.setObservacionesInternas(dto.getObservacionesInternas());
        nuevaVersion.setNotasAdicionales(dto.getNotasAdicionales());
        nuevaVersion.setEspecificacionesTecnicas(dto.getEspecificacionesTecnicas());
        nuevaVersion.setComentariosCliente(dto.getComentariosCliente());
        nuevaVersion.setRequisitosEspeciales(dto.getRequisitosEspeciales());

        // Calcular totales (sin JSON)
        double totalProf = 0.0;
        if (dto.getProfesionales() != null) {
            for (var prof : dto.getProfesionales()) {
                if (prof.getUnidadActiva() == null)
                    throw new IllegalArgumentException("unidadActiva es obligatoria para cada profesional");
                Double cantidad = prof.getCantidad() != null ? prof.getCantidad() : 0.0;
                Double importe = prof.getImportePorUnidad();
                if (importe == null) {
                    String u = prof.getUnidadActiva().toLowerCase();
                    if (u.contains("hora")) importe = prof.getImporteXHora();
                    else if (u.contains("dia") || u.contains("dí")) importe = prof.getImporteXDia();
                    else if (u.contains("semana")) importe = prof.getImporteXSemana();
                    else if (u.contains("mes")) importe = prof.getImporteXMes();
                    else if (u.contains("obra")) importe = prof.getImporteXObra();
                }
                importe = importe != null ? importe : 0.0;
                if (cantidad < 0 || importe < 0)
                    throw new IllegalArgumentException("cantidad e importe deben ser >= 0");
                totalProf += cantidad * importe;
            }
        }
        nuevaVersion.setTotalProfesionales(totalProf);

        double totalMat = 0.0;
        if (dto.getMaterialesList() != null) {
            for (var mat : dto.getMaterialesList()) {
                Double cantidad = mat.getCantidad() != null ? mat.getCantidad() : 0.0;
                Double precio = mat.getPrecioUnitario() != null ? mat.getPrecioUnitario() : 0.0;
                if (cantidad < 0 || precio < 0)
                    throw new IllegalArgumentException("cantidad y precioUnitario deben ser >= 0");
                totalMat += cantidad * precio;
            }
        }
        nuevaVersion.setTotalMateriales(totalMat);

        // Total de otros costos eliminado - tabla presupuesto_otro_costo removida del sistema
        double totalOtrosCostos = 0.0;

        // Honorarios de dirección de obra
        nuevaVersion.setHonorarioDireccionValorFijo(dto.getHonorarioDireccionValorFijo());
        nuevaVersion.setHonorarioDireccionPorcentaje(dto.getHonorarioDireccionPorcentaje());

        // Calcular importe de honorarios de dirección
        double honorarioDireccionImporte = 0.0;
        if (dto.getHonorarioDireccionPorcentaje() != null && dto.getHonorarioDireccionPorcentaje() > 0) {
            double baseCalculo = totalProf + totalMat + totalOtrosCostos;
            honorarioDireccionImporte = baseCalculo * (dto.getHonorarioDireccionPorcentaje() / 100.0);
        } else if (dto.getHonorarioDireccionValorFijo() != null && dto.getHonorarioDireccionValorFijo() > 0) {
            honorarioDireccionImporte = dto.getHonorarioDireccionValorFijo();
        }
        nuevaVersion.setHonorarioDireccionImporte(honorarioDireccionImporte);

        // ========== MAPEAR CONFIGURACIÓN DE HONORARIOS ==========
        nuevaVersion.setHonorariosAplicarATodos(dto.getHonorariosAplicarATodos());
        nuevaVersion.setHonorariosValorGeneral(dto.getHonorariosValorGeneral());
        nuevaVersion.setHonorariosTipoGeneral(dto.getHonorariosTipoGeneral());

        nuevaVersion.setHonorariosProfesionalesActivo(dto.getHonorariosProfesionalesActivo());
        nuevaVersion.setHonorariosProfesionalesTipo(dto.getHonorariosProfesionalesTipo());
        nuevaVersion.setHonorariosProfesionalesValor(dto.getHonorariosProfesionalesValor());

        nuevaVersion.setHonorariosMaterialesActivo(dto.getHonorariosMaterialesActivo());
        nuevaVersion.setHonorariosMaterialesTipo(dto.getHonorariosMaterialesTipo());
        nuevaVersion.setHonorariosMaterialesValor(dto.getHonorariosMaterialesValor());

        nuevaVersion.setHonorariosOtrosCostosActivo(dto.getHonorariosOtrosCostosActivo());
        nuevaVersion.setHonorariosOtrosCostosTipo(dto.getHonorariosOtrosCostosTipo());
        nuevaVersion.setHonorariosOtrosCostosValor(dto.getHonorariosOtrosCostosValor());

        // ========== MAPEAR CONFIGURACIÓN DE MAYORES COSTOS ==========
        nuevaVersion.setMayoresCostosAplicarValorGeneral(dto.getMayoresCostosAplicarValorGeneral());
        nuevaVersion.setMayoresCostosValorGeneral(dto.getMayoresCostosValorGeneral());
        nuevaVersion.setMayoresCostosTipoGeneral(dto.getMayoresCostosTipoGeneral());
        nuevaVersion.setMayoresCostosGeneralImportado(dto.getMayoresCostosGeneralImportado());
        nuevaVersion.setMayoresCostosRubroImportado(dto.getMayoresCostosRubroImportado());
        nuevaVersion.setMayoresCostosNombreRubroImportado(dto.getMayoresCostosNombreRubroImportado());

        nuevaVersion.setMayoresCostosProfesionalesActivo(dto.getMayoresCostosProfesionalesActivo());
        nuevaVersion.setMayoresCostosProfesionalesTipo(dto.getMayoresCostosProfesionalesTipo());
        nuevaVersion.setMayoresCostosProfesionalesValor(dto.getMayoresCostosProfesionalesValor());

        nuevaVersion.setMayoresCostosMaterialesActivo(dto.getMayoresCostosMaterialesActivo());
        nuevaVersion.setMayoresCostosMaterialesTipo(dto.getMayoresCostosMaterialesTipo());
        nuevaVersion.setMayoresCostosMaterialesValor(dto.getMayoresCostosMaterialesValor());

        nuevaVersion.setMayoresCostosOtrosCostosActivo(dto.getMayoresCostosOtrosCostosActivo());
        nuevaVersion.setMayoresCostosOtrosCostosTipo(dto.getMayoresCostosOtrosCostosTipo());
        nuevaVersion.setMayoresCostosOtrosCostosValor(dto.getMayoresCostosOtrosCostosValor());

        nuevaVersion.setMayoresCostosConfiguracionPresupuestoActivo(dto.getMayoresCostosConfiguracionPresupuestoActivo());
        nuevaVersion.setMayoresCostosConfiguracionPresupuestoTipo(dto.getMayoresCostosConfiguracionPresupuestoTipo());
        nuevaVersion.setMayoresCostosConfiguracionPresupuestoValor(dto.getMayoresCostosConfiguracionPresupuestoValor());

        nuevaVersion.setMayoresCostosHonorariosActivo(dto.getMayoresCostosHonorariosActivo());
        nuevaVersion.setMayoresCostosHonorariosTipo(dto.getMayoresCostosHonorariosTipo());
        nuevaVersion.setMayoresCostosHonorariosValor(dto.getMayoresCostosHonorariosValor());

        // Total general
        nuevaVersion.setTotalGeneral(totalProf + totalMat + totalOtrosCostos + honorarioDireccionImporte);

        // ========== MAPEAR TOTALES ESPECÍFICOS DEL FRONTEND ==========
        // Estos campos vienen calculados desde el frontend y se guardan tal como llegan
        nuevaVersion.setTotalPresupuesto(dto.getTotalPresupuesto());
        nuevaVersion.setTotalHonorariosCalculado(dto.getTotalHonorarios());
        nuevaVersion.setTotalPresupuestoConHonorarios(dto.getTotalPresupuestoConHonorarios());
        if (dto.getTotalConDescuentos() != null) {
            nuevaVersion.setTotalConDescuentos(dto.getTotalConDescuentos());
        }
        
        // Mapear totales calculados de mayores costos y descuentos por rubro
        nuevaVersion.setTotalMayoresCostosPorRubro(dto.getTotalMayoresCostosPorRubro() != null ? dto.getTotalMayoresCostosPorRubro() : java.math.BigDecimal.ZERO);
        nuevaVersion.setTotalDescuentosPorRubro(dto.getTotalDescuentosPorRubro() != null ? dto.getTotalDescuentosPorRubro() : java.math.BigDecimal.ZERO);


        // Actualizar otros campos
        nuevaVersion.setTiempoEstimadoTerminacion(dto.getTiempoEstimadoTerminacion());

        if (dto.getEstado() != null) {
            com.rodrigo.construccion.enums.PresupuestoEstado e =
                    com.rodrigo.construccion.enums.PresupuestoEstado.fromString(dto.getEstado());
            if (e != null) nuevaVersion.setEstado(e);
        } else {
            nuevaVersion.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.A_ENVIAR);
        }

        nuevaVersion.setFechaProbableInicio(dto.getFechaProbableInicio());
        nuevaVersion.setTelefono(dto.getTelefono());
        nuevaVersion.setMail(dto.getMail());

        if (dto.getVencimiento() != null) {
            nuevaVersion.setVencimiento(dto.getVencimiento());
        } else {
            nuevaVersion.setVencimiento(ahora.plusDays(15));
        }

        nuevaVersion.setObservaciones(dto.getObservaciones());
        
        /* LEGACY: Profesionales y Materiales ahora en items_calculadora
        // Mapear profesionales a tabla normalizada
        if (dto.getProfesionales() != null) {
            for (var profDto : dto.getProfesionales()) {
                PresupuestoNoClienteProfesional prof = new PresupuestoNoClienteProfesional();
                // ... código comentado ...
                nuevaVersion.getProfesionales().add(prof);
            }
        }

        // Mapear materiales a tabla normalizada
        if (dto.getMaterialesList() != null) {
            for (var matDto : dto.getMaterialesList()) {
                PresupuestoNoClienteMaterial mat = new PresupuestoNoClienteMaterial();
                // ... código comentado ...
                nuevaVersion.getMateriales().add(mat);
            }
        }
        */

        // Guardar la NUEVA versión (no modifica la anterior)
        PresupuestoNoCliente guardado = repository.save(nuevaVersion);

        // ========== PROCESAR ITEMS CALCULADORA ==========
        // REGLA: Lo que venga en el DTO (modal de edición) es lo que se debe persistir

        if (dto.getItemsCalculadora() != null && !dto.getItemsCalculadora().isEmpty()) {
            // CASO 1: El DTO TRAE items → Procesar lo que el usuario editó en el modal
            log.info("📝 Procesando {} items calculadora desde el DTO (datos del modal)",
                    dto.getItemsCalculadora().size());
            procesarItemsCalculadora(guardado, dto.getItemsCalculadora());

        } else if (presupuestoBase.getItemsCalculadora() != null && !presupuestoBase.getItemsCalculadora().isEmpty()) {
            // CASO 2: El DTO NO trae items → Copiar de la versión anterior
            log.info("📋 DTO sin items calculadora, copiando {} items desde presupuesto base ID: {}",
                    presupuestoBase.getItemsCalculadora().size(), presupuestoBase.getId());

            try {
                copiarItemsCalculadoraDePresupuestoBase(guardado.getId(), presupuestoBase.getId());
                log.info("✅ Items calculadora copiados exitosamente desde versión anterior");
            } catch (Exception e) {
                log.error("❌ Error al copiar items calculadora: {}", e.getMessage(), e);
                throw new RuntimeException("Error al copiar items calculadora: " + e.getMessage(), e);
            }
        } else {
            // CASO 3: Ni el DTO ni la versión anterior tienen items
            log.warn("⚠️ Nueva versión creada SIN items calculadora (ni en DTO ni en versión anterior)");
        }

        // Sincronizar datos del cliente con la obra (si existe obra asociada)
        sincronizarDatosClienteConObra(guardado);
        // Calcular campos calculados antes de devolver
        guardado.calcularCamposCalculados();
        return guardado;
    }

    /**
     * Aprueba un presupuesto cambiando SOLO su estado a APROBADO sin crear nueva versión.
     * Este método realiza un UPDATE in-place del registro existente.
     *
     * @param id ID del presupuesto a aprobar
     * @return El presupuesto aprobado
     * @throws IllegalArgumentException si el presupuesto no existe o ya está aprobado
     */
    @Transactional
    public PresupuestoNoCliente aprobar(Long id) {
        // Buscar el presupuesto (el filtro multi-tenant de Hibernate se aplica automáticamente)
        PresupuestoNoCliente presupuesto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado o no pertenece a la empresa"));

        // Verificar que no esté ya aprobado
        if (presupuesto.getEstado() == com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO) {
            throw new IllegalArgumentException("El presupuesto ya está en estado APROBADO");
        }

        // Cambiar SOLO el estado a APROBADO (UPDATE in-place, sin versionar)
        presupuesto.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO);

        // Guardar el cambio (JPA hace UPDATE del registro existente)
        PresupuestoNoCliente aprobado = repository.save(presupuesto);

        // 🔥 ENRIQUECER CON profesionalObraId SI TIENE OBRA ASOCIADA
        if (aprobado.getObra() != null) {
            log.info("✅ Ejecutando enriquecimiento para presupuesto aprobado {} con obraId {}...",
                    id, aprobado.getObra().getId());
            enriquecerProfesionalesConObraId(aprobado);
        }

        return aprobado;
    }

    // Métodos adicionales (stubs - requieren implementación completa)
    @Transactional
    public com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse aprobarYCrearObra(
            Long id,
            Long obraReferenciaId,
            Long clienteReferenciaId) {

        log.info("🚀 INICIO aprobarYCrearObra - presupuestoId: {}, obraReferenciaId: {}, clienteReferenciaId: {}",
                id, obraReferenciaId, clienteReferenciaId);

        try {
            // 1. VALIDAR que el presupuesto existe
            PresupuestoNoCliente presupuesto = repository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Presupuesto no encontrado con ID: " + id));

            // 2. VALIDAR que NO está ya aprobado
            if (presupuesto.getEstado() == com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO) {
                throw new IllegalArgumentException("El presupuesto ya está aprobado");
            }

            // 3. Detectar si es un trabajo extra
            boolean esTrabajoExtra = presupuesto.getEsPresupuestoTrabajoExtra() != null 
                    && presupuesto.getEsPresupuestoTrabajoExtra();
            boolean tieneObraPadre = presupuesto.getObra() != null;

            log.info("📊 Tipo de presupuesto - esTrabajoExtra: {}, tieneObraPadre: {}", 
                    esTrabajoExtra, tieneObraPadre);

            // ========== CASO 1: TRABAJO EXTRA - Crear sub-obra vinculada a obra padre ==========
            if (esTrabajoExtra && tieneObraPadre) {
                log.info("🔨 CASO 1: Trabajo Extra - Creando sub-obra vinculada a obra padre ID: {}", 
                        presupuesto.getObra().getId());
                return procesarTrabajoExtra(presupuesto);
            }

            // ========== CASO 2: Presupuesto normal con obra ya asignada - Solo aprobar ==========
            if (tieneObraPadre && !esTrabajoExtra) {
                log.info("✅ CASO 2: Presupuesto normal con obra - Solo aprobar (ID obra: {})", 
                        presupuesto.getObra().getId());

                PresupuestoNoCliente aprobado = aprobar(id);

                // Construir respuesta con datos de obra existente
                com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse response =
                        new com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse();
                response.setObraId(aprobado.getObra().getId());
                response.setPresupuestosActualizados(1);
                response.setObraCreada(false); // NO se creó, ya existía
                response.setClienteReutilizado(true); // Se reutilizó el existente
                response.setClienteId(aprobado.getCliente() != null ? aprobado.getCliente().getId() : null);
                response.setMensaje(String.format(
                        "Presupuesto #%d aprobado exitosamente. Obra existente #%d: '%s'. Cliente ID: %d",
                        aprobado.getId(),
                        aprobado.getObra().getId(),
                        aprobado.getObra().getNombre(),
                        aprobado.getCliente() != null ? aprobado.getCliente().getId() : null
                ));

                log.info("✅ Respuesta con obra existente: {}", response);
                return response;
            }

            // 4. VALIDAR que obraReferenciaId y clienteReferenciaId NO vengan ambos
            if (obraReferenciaId != null && clienteReferenciaId != null) {
                throw new IllegalArgumentException("No se puede proporcionar obraReferenciaId y clienteReferenciaId al mismo tiempo. Son mutuamente excluyentes.");
            }

            // 5. VALIDAR campos obligatorios para crear obra
            if (presupuesto.getDireccionObraCalle() == null || presupuesto.getDireccionObraCalle().trim().isEmpty()) {
                throw new IllegalArgumentException("El presupuesto debe tener direccionObraCalle para crear la obra");
            }
            if (presupuesto.getDireccionObraAltura() == null || presupuesto.getDireccionObraAltura().trim().isEmpty()) {
                throw new IllegalArgumentException("El presupuesto debe tener direccionObraAltura para crear la obra");
            }

            log.info("📊 Validaciones pasadas - Presupuesto estado: {}, tiene obra: {}",
                    presupuesto.getEstado(), presupuesto.getObra() != null);

            // 6. Obtener empresa del presupuesto
            Long empresaId = presupuesto.getEmpresa().getId();
            log.info("🏢 Empresa del presupuesto: ID {}", empresaId);

            Empresa empresa = empresaRepository.findById(empresaId)
                    .orElseThrow(() -> new IllegalArgumentException("Empresa no encontrada con ID: " + empresaId));

            // 7. Determinar cliente: PRIORIDAD al cliente YA vinculado en el presupuesto
            Cliente cliente;
            boolean clienteReutilizado = false;

            log.info("👤 Buscando/creando cliente - presupuesto.cliente: {}",
                    presupuesto.getCliente() != null ? presupuesto.getCliente().getId() : "null");

            // CASO 0: El presupuesto YA tiene un cliente vinculado → USAR ESE (MÁXIMA PRIORIDAD)
            if (presupuesto.getCliente() != null) {
                cliente = presupuesto.getCliente();
                clienteReutilizado = true;
                log.info("✅ Cliente YA VINCULADO al presupuesto: {} (ID: {})",
                        cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante(),
                        cliente.getId());

            } else if (obraReferenciaId != null) {
                // CASO A: Hay obra de referencia → Reutilizar su cliente
                log.info("🔗 Obra de referencia proporcionada: ID {}", obraReferenciaId);

                Obra obraReferencia = obraRepository.findById(obraReferenciaId)
                        .orElseThrow(() -> new IllegalArgumentException("Obra de referencia no encontrada con ID: " + obraReferenciaId));

                // Validar que la obra pertenece a la misma empresa
                boolean perteneceAEmpresa = obraReferencia.getCliente().getEmpresas().stream()
                        .anyMatch(e -> e.getId().equals(empresaId));

                if (!perteneceAEmpresa) {
                    throw new IllegalArgumentException("La obra de referencia no pertenece a la empresa actual");
                }

                cliente = obraReferencia.getCliente();
                clienteReutilizado = true;
                log.info("✅ Cliente REUTILIZADO de obra de referencia: {} (ID: {})",
                        cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante(),
                        cliente.getId());

            } else if (clienteReferenciaId != null) {
                // CASO B: Hay cliente de referencia → Reutilizar ese cliente directamente
                log.info("👤 Cliente de referencia proporcionado: ID {}", clienteReferenciaId);

                cliente = clienteRepository.findById(clienteReferenciaId)
                        .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + clienteReferenciaId));

                // Validar que el cliente pertenece a la misma empresa
                boolean perteneceAEmpresa = cliente.getEmpresas().stream()
                        .anyMatch(e -> e.getId().equals(empresaId));

                if (!perteneceAEmpresa) {
                    throw new IllegalArgumentException("El cliente no pertenece a esta empresa");
                }

                clienteReutilizado = true;
                log.info("✅ Cliente REUTILIZADO directamente: {} (ID: {})",
                        cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante(),
                        cliente.getId());

            } else {
                // CASO C: NO hay referencias NI cliente vinculado → Buscar o crear cliente desde datos del presupuesto
                log.info("🆕 No hay cliente vinculado, buscando/creando cliente desde datos del presupuesto");
                cliente = buscarOCrearClienteDesdePresupuesto(presupuesto, empresa);
                clienteReutilizado = false;
                log.info("✅ Cliente {} (ID: {}) asociado al presupuesto {}",
                        cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante(),
                        cliente.getId(),
                        presupuesto.getId());
            }

            log.info("✅ Cliente determinado: ID {}, reutilizado: {}", cliente.getId(), clienteReutilizado);

            // 6. CREAR nueva Obra con datos del presupuesto
            log.info("🏗️ Creando nueva Obra...");
            Obra obra = new Obra();

            // ⭐ IMPORTANTE: Asignar empresa y cliente a la obra
            obra.setEmpresaId(empresa.getId());
            obra.setCliente(cliente);
            log.info("✅ Empresa asignada a la obra: ID {}", empresa.getId());

            // Generar nombreObra si no existe
            if (presupuesto.getNombreObra() == null || presupuesto.getNombreObra().trim().isEmpty()) {
                // CASO: Si NO tiene nombreObra, formatear dirección completa
                StringBuilder direccionFormateada = new StringBuilder();

                // Barrio (opcional, entre paréntesis)
                if (presupuesto.getDireccionObraBarrio() != null && !presupuesto.getDireccionObraBarrio().trim().isEmpty()) {
                    direccionFormateada.append("(").append(presupuesto.getDireccionObraBarrio()).append(") ");
                }

                // Calle + Altura (obligatorios)
                direccionFormateada.append(presupuesto.getDireccionObraCalle())
                        .append(" ")
                        .append(presupuesto.getDireccionObraAltura());

                // Torre (opcional)
                if (presupuesto.getDireccionObraTorre() != null && !presupuesto.getDireccionObraTorre().trim().isEmpty()) {
                    direccionFormateada.append(" Torre ").append(presupuesto.getDireccionObraTorre());
                }

                // Piso (opcional)
                if (presupuesto.getDireccionObraPiso() != null && !presupuesto.getDireccionObraPiso().trim().isEmpty()) {
                    direccionFormateada.append(" Piso ").append(presupuesto.getDireccionObraPiso());
                }

                // Departamento (opcional)
                if (presupuesto.getDireccionObraDepartamento() != null && !presupuesto.getDireccionObraDepartamento().trim().isEmpty()) {
                    direccionFormateada.append(" Depto ").append(presupuesto.getDireccionObraDepartamento());
                }

                String nombreObra = direccionFormateada.toString().trim();
                log.info("✅ Nombre de obra generado desde dirección: '{}'", nombreObra);

                // Guardar el nombre generado en el presupuesto para que sea editable después
                presupuesto.setNombreObra(nombreObra);
            } else {
                log.info("✅ Usando nombreObra del presupuesto: '{}'", presupuesto.getNombreObra());
            }

            // Aprobar el presupuesto
            presupuesto.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO);
            log.info("✅ Presupuesto aprobado - Estado actualizado a: APROBADO");

            // ✅ NUEVO: Sincronizar TODOS los campos del presupuesto a la obra
            sincronizarPresupuestoAObra(presupuesto, obra);

            // Log para debugging
            log.info("✅ Obra creada y sincronizada con presupuesto ID: {}", presupuesto.getId());
            log.info("🔍 DEBUG - Estado de obra: {}", obra.getEstado());
            log.info("🔍 DEBUG - EmpresaId: {}", obra.getEmpresaId());

            // Guardar obra
            obra = obraRepository.save(obra);

            // DEBUG: Verificar después de guardar
            log.info("🔍 DEBUG - Obra guardada con ID: {}", obra.getId());
            log.info("🔍 DEBUG - esObraTrabajoExtra: {}", obra.getEsObraTrabajoExtra());
            log.info("🔍 DEBUG - obraOrigenId: {}", obra.getObraOrigenId());
            log.info("🔍 DEBUG - Estado de obra DESPUÉS de guardar: {}", obra.getEstado());
            log.info("🔍 DEBUG - presupuestoNoClienteId DESPUÉS de guardar: {}", obra.getPresupuestoNoClienteId());
            log.info("🔍 DEBUG - EmpresaId DESPUÉS de guardar: {}", obra.getEmpresaId());

            // 7. CREAR asignaciones profesional-obra desde itemsCalculadora
            crearAsignacionesProfesionalesObra(presupuesto, obra);

            // 8. ACTUALIZAR presupuesto: estado APROBADO, relación con obra Y cliente
            presupuesto.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO);
            presupuesto.setObra(obra);
            presupuesto.setCliente(cliente);  // Vincular el cliente al presupuesto
            presupuesto.calcularCamposCalculados();
            presupuesto = repository.save(presupuesto);

            log.info("✅ Presupuesto {} vinculado a Cliente ID {} y Obra ID {}",
                    presupuesto.getId(), cliente.getId(), obra.getId());

            // ⭐ IMPORTANTE: Actualizar obra con el ID del presupuesto (relación bidireccional)
            obra.setPresupuestoNoClienteId(presupuesto.getId());
            obra = obraRepository.save(obra);
            log.info("🔗 Obra {} actualizada con presupuestoNoClienteId: {}", obra.getId(), obra.getPresupuestoNoClienteId());

            // 🔥 ENRIQUECER CON profesionalObraId DESPUÉS DE VINCULAR LA OBRA
            log.info("✅ Ejecutando enriquecimiento para presupuesto aprobado {} con obraId {}...",
                    presupuesto.getId(), obra.getId());
            enriquecerProfesionalesConObraId(presupuesto);

            // 9. RETORNAR respuesta
            com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse response =
                    new com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse();
            response.setObraId(obra.getId());
            response.setPresupuestosActualizados(1);
            response.setObraCreada(true);
            response.setClienteReutilizado(clienteReutilizado);
            response.setClienteId(cliente.getId());

            // Mensaje personalizado según si el cliente fue reutilizado o no
            if (clienteReutilizado) {
                response.setMensaje(String.format(
                        "Presupuesto #%d aprobado exitosamente. Obra #%d creada: '%s'. Cliente reutilizado de obra de referencia (ID: %d)",
                        presupuesto.getId(),
                        obra.getId(),
                        obra.getNombre(),
                        cliente.getId()
                ));
            } else {
                response.setMensaje(String.format(
                        "Presupuesto #%d aprobado exitosamente. Obra #%d creada: '%s'. Cliente ID: %d",
                        presupuesto.getId(),
                        obra.getId(),
                        obra.getNombre(),
                        cliente.getId()
                ));
            }

            log.info("✅ aprobarYCrearObra COMPLETADO - Obra ID: {}, Cliente ID: {}", obra.getId(), cliente.getId());
            return response;

        } catch (IllegalArgumentException e) {
            log.error("❌ Error de validación en aprobarYCrearObra: {}", e.getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("❌ ERROR INESPERADO en aprobarYCrearObra - presupuestoId: {}", id, e);
            throw new RuntimeException("Error al aprobar presupuesto y crear obra: " + e.getMessage(), e);
        }
    }

    /**
     * Procesa un trabajo extra creando una sub-obra vinculada a la obra padre.
     * 
     * FLUJO:
     * 1. Valida que el presupuesto tenga nombreObra (requerido para nombrar la sub-obra)
     * 2. Obtiene la obra padre desde presupuesto.obra
     * 3. Hereda el cliente de la obra padre
     * 4. Crea una nueva obra (sub-obra) vinculada mediante obra_origen_id
     * 5. Actualiza el presupuesto para que apunte a la nueva sub-obra (en lugar del padre)
     * 6. Crea las asignaciones profesional-obra para la nueva sub-obra
     * 
     * @param presupuesto El presupuesto de trabajo extra a procesar
     * @return AprobarPresupuestoResponse con datos de la sub-obra creada
     */
    private com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse procesarTrabajoExtra(
            PresupuestoNoCliente presupuesto) {
        
        log.info("🔨 INICIANDO procesamiento de Trabajo Extra - Presupuesto ID: {}", presupuesto.getId());
        
        // 1. VALIDAR que tenga nombreObra (requerido para crear la sub-obra)
        if (presupuesto.getNombreObra() == null || presupuesto.getNombreObra().trim().isEmpty()) {
            throw new IllegalArgumentException(
                "El trabajo extra requiere un nombre de obra para crear la sub-obra. " +
                "Por favor proporcione el campo 'nombreObra'."
            );
        }
        
        // 2. Obtener obra padre
        Obra obraPadre = presupuesto.getObra();
        Long obraPadreId = obraPadre.getId();
        
        log.info("📋 Obra Padre encontrada - ID: {}, Nombre: '{}'", obraPadreId, obraPadre.getNombre());
        
        // 3. Heredar cliente de la obra padre
        Cliente cliente = obraPadre.getCliente();
        if (cliente == null) {
            throw new IllegalArgumentException(
                "La obra padre (ID: " + obraPadreId + ") no tiene cliente asignado. " +
                "No se puede crear el trabajo extra."
            );
        }
        
        log.info("👤 Cliente heredado de obra padre - ID: {}, Nombre: {}", 
                cliente.getId(), 
                cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante());
        
        // 4. Crear nueva sub-obra
        log.info("✨ Creando sub-obra para trabajo extra...");
        
        Obra subObra = new Obra();
        subObra.setNombre(presupuesto.getNombreObra()); // "Cabaña 1", "Cabaña 2", etc.
        subObra.setObraOrigenId(obraPadreId); // 🔗 VINCULAR CON OBRA PADRE (CRÍTICO)
        subObra.setPresupuestoEstimado(presupuesto.getTotalPresupuestoConHonorarios()); // Total final
        subObra.setCliente(cliente); // Heredar cliente del padre
        subObra.setEmpresaId(presupuesto.getEmpresa().getId());
        subObra.setFechaInicio(presupuesto.getFechaProbableInicio() != null 
                ? presupuesto.getFechaProbableInicio() 
                : LocalDate.now());
        
        // Marcar como obra de trabajo extra
        subObra.setEsObraTrabajoExtra(true);
        subObra.setTipoOrigen(com.rodrigo.construccion.enums.TipoOrigen.TRABAJO_EXTRA);
        subObra.setPresupuestoOriginalId(presupuesto.getId());
        
        // ⚡ FIX: Setear APROBADO en el presupuesto ANTES de sincronizar,
        // para que sincronizarEstado() lea APROBADO y mapee correctamente el estado de la sub-obra.
        // Si se hace después, sincronizarEstado lee BORRADOR y sobreescribe el estado de la obra.
        presupuesto.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO);

        // Copiar datos del presupuesto a la sub-obra (incluye sincronización de estado)
        sincronizarPresupuestoAObra(presupuesto, subObra);
        
        // Agregar observación indicando el origen
        String obsOriginal = presupuesto.getObservaciones() != null ? presupuesto.getObservaciones() : "";
        String obsExtra = String.format(
            "\n[Sub-obra creada desde trabajo extra - Presupuesto ID: %d - Obra Padre: '%s' (ID: %d)]",
            presupuesto.getId(),
            obraPadre.getNombre(),
            obraPadreId
        );
        subObra.setObservaciones(obsOriginal + obsExtra);
        
        // Guardar sub-obra
        subObra = obraRepository.save(subObra);
        
        log.info("✅ Sub-obra creada exitosamente - ID: {}, Nombre: '{}', ObraPadreId: {}", 
                subObra.getId(), subObra.getNombre(), subObra.getObraOrigenId());
        
        // 5. Actualizar presupuesto: cambiar obra_id del padre a la sub-obra
        log.info("🔄 Actualizando presupuesto: cambiando obra_id de {} (padre) a {} (sub-obra)", 
                obraPadreId, subObra.getId());
        
        presupuesto.setObra(subObra); // CRÍTICO: apuntar a la sub-obra recién creada
        presupuesto.setCliente(cliente); // Asegurar vínculo con cliente
        presupuesto.setEstado(com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO);
        presupuesto.calcularCamposCalculados();
        presupuesto = repository.save(presupuesto);
        
        log.info("✅ Presupuesto actualizado - Estado: APROBADO, Obra: {} (sub-obra)", subObra.getId());
        
        // Actualizar obra con el ID del presupuesto (relación bidireccional)
        subObra.setPresupuestoNoClienteId(presupuesto.getId());
        subObra = obraRepository.save(subObra);
        log.info("🔗 Sub-obra actualizada con presupuestoNoClienteId: {}", presupuesto.getId());
        
        // 6. Crear asignaciones profesional-obra para la SUB-OBRA
        log.info("👷 Creando asignaciones profesionales para la sub-obra...");
        crearAsignacionesProfesionalesObra(presupuesto, subObra);
        
        // 7. Enriquecer profesionales con obraId
        log.info("🔗 Enriqueciendo profesionales con profesionalObraId...");
        enriquecerProfesionalesConObraId(presupuesto);
        
        // 8. Construir respuesta
        com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse response =
                new com.rodrigo.construccion.dto.response.AprobarPresupuestoResponse();
        response.setObraId(subObra.getId());
        response.setObraCreada(true);
        response.setObraPadreId(obraPadreId); // Incluir ID de obra padre
        response.setNombreSubObra(subObra.getNombre()); // Incluir nombre de sub-obra
        response.setClienteId(cliente.getId());
        response.setClienteReutilizado(true); // El cliente se heredó del padre
        response.setPresupuestosActualizados(1);
        response.setMensaje(String.format(
                "Trabajo Extra aprobado exitosamente. Sub-obra '%s' (ID: %d) creada y vinculada a obra padre '%s' (ID: %d)",
                subObra.getNombre(),
                subObra.getId(),
                obraPadre.getNombre(),
                obraPadreId
        ));
        
        log.info("🎉 Trabajo Extra procesado exitosamente - Sub-obra: {} | Padre: {} | Cliente: {}", 
                subObra.getId(), obraPadreId, cliente.getId());
        
        return response;
    }

    /**
     * Crea las asignaciones profesional-obra a partir de los profesionales en itemsCalculadora.
     * IMPORTANTE: Cada ProfesionalCalculadora es INDEPENDIENTE aunque sean del mismo tipo,
     * porque pueden tener diferentes cantidades de jornales e importes.
     * Se crea UNA asignación en profesionales_obras por CADA profesional_calculadora.
     */
    private void crearAsignacionesProfesionalesObra(PresupuestoNoCliente presupuesto, Obra obra) {
        if (presupuesto.getItemsCalculadora() == null || presupuesto.getItemsCalculadora().isEmpty()) {
            log.warn("No hay items calculadora para crear asignaciones profesional-obra. Presupuesto ID: {}", presupuesto.getId());
            return;
        }

        Long empresaId = presupuesto.getEmpresa().getId();
        int asignacionesCreadas = 0;

        for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
            if (item.getProfesionales() == null || item.getProfesionales().isEmpty()) {
                continue;
            }

            for (ProfesionalCalculadora profCalc : item.getProfesionales()) {
                // Obtener tipo de profesional
                String tipoProfesional = profCalc.getTipo();
                if (tipoProfesional == null || tipoProfesional.trim().isEmpty()) {
                    log.warn("ProfesionalCalculadora {} sin tipo. Saltando.", profCalc.getId());
                    continue;
                }

                // 🆕 CREAR SIEMPRE UN NUEVO PROFESIONAL MAESTRO para cada ProfesionalCalculadora
                // Esto evita el constraint de unicidad (id_profesional, id_obra) y permite
                // tener múltiples profesionales del mismo tipo en la misma obra con diferentes jornales
                log.info("✨ Creando profesional maestro para tipo '{}'...", tipoProfesional);

                Profesional profesional = new Profesional();
                profesional.setTipoProfesional(tipoProfesional);
                profesional.setNombre(profCalc.getNombre() != null && !profCalc.getNombre().trim().isEmpty()
                        ? profCalc.getNombre()
                        : tipoProfesional); // Usar el tipo como nombre si no hay nombre
                profesional.setValorHoraDefault(profCalc.getImporteJornal() != null
                        ? profCalc.getImporteJornal()
                        : BigDecimal.valueOf(5000)); // Valor por defecto
                profesional.setActivo(true);

                // Guardar el nuevo profesional
                profesional = profesionalRepository.save(profesional);
                log.info("✅ Profesional maestro creado: ID={}, Tipo={}, Nombre={}",
                        profesional.getId(), profesional.getTipoProfesional(), profesional.getNombre());

                // ✅ CREAR SIEMPRE UNA NUEVA ASIGNACIÓN para cada ProfesionalCalculadora
                // porque cada uno representa un trabajo independiente con sus propios jornales/importes
                ProfesionalObra asignacion = new ProfesionalObra();

                // ⚠️ CRÍTICO: Asignar obra PRIMERO (NOT NULL en BD)
                asignacion.setObra(obra);

                asignacion.setProfesional(profesional);
                asignacion.setDireccionObraCalle(obra.getDireccionObraCalle());
                asignacion.setDireccionObraAltura(obra.getDireccionObraAltura());
                asignacion.setDireccionObraPiso(obra.getDireccionObraPiso());
                asignacion.setDireccionObraDepartamento(obra.getDireccionObraDepartamento());
                asignacion.setEmpresaId(empresaId);
                asignacion.setFechaDesde(presupuesto.getFechaProbableInicio() != null
                        ? presupuesto.getFechaProbableInicio()
                        : LocalDate.now());
                asignacion.setActivo(true);

                // Usar el importeJornal del ProfesionalCalculadora específico
                BigDecimal valorHora = profCalc.getImporteJornal() != null
                        ? profCalc.getImporteJornal()
                        : profesional.getValorHoraDefault();
                asignacion.setValorHoraAsignado(valorHora);

                // 🔧 FIX: Guardar cantidad de jornales e importe jornal para cálculos financieros
                asignacion.setCantidadJornales(profCalc.getCantidadJornales() != null 
                        ? profCalc.getCantidadJornales().intValue() 
                        : 0);
                asignacion.setImporteJornal(valorHora);
                asignacion.setJornalesUtilizados(0); // Inicializar en 0

                // Guardar asignación
                ProfesionalObra asignacionGuardada = profesionalObraRepository.save(asignacion);
                asignacionesCreadas++;

                log.info("✅ Asignación #{} creada - ID: {}, Profesional: {} ({}), Jornales: {}, Importe: ${}",
                        asignacionesCreadas,
                        asignacionGuardada.getId(),
                        profesional.getId(),
                        tipoProfesional,
                        profCalc.getCantidadJornales(),
                        profCalc.getImporteJornal());

                // Actualizar profesionalObraId en profesionalCalculadora
                profCalc.setProfesionalObraId(asignacionGuardada.getId());
                profesionalCalculadoraRepository.save(profCalc);
            }
        }

        log.info("🎉 Proceso completado: {} asignaciones profesional-obra creadas para presupuesto {}",
                asignacionesCreadas, presupuesto.getId());
    }

    /**
     * Actualiza el estado de un presupuesto y sincroniza automáticamente con la obra vinculada.
     * <p>
     * SINCRONIZACIÓN BIDIRECCIONAL:
     * - Si el presupuesto tiene obra asociada, el estado de la obra se actualiza automáticamente
     * - Los enums PresupuestoEstado y EstadoObra están sincronizados (mismo conjunto de estados)
     * - La conversión entre estados es directa por nombre
     * <p>
     * CREACIÓN DE OBRA:
     * - Una obra se crea cuando el presupuesto pasa de cualquier estado a APROBADO
     * - Se usa el método aprobarYCrearObra() para este flujo
     *
     * @param id          ID del presupuesto
     * @param empresaId   ID de la empresa (validación multi-tenant)
     * @param nuevoEstado Estado nuevo a aplicar
     * @return Presupuesto actualizado
     * @throws IllegalArgumentException si el presupuesto no existe, no pertenece a la empresa o el estado es inválido
     */
    @Transactional
    public PresupuestoNoCliente actualizarEstado(Long id, Long empresaId, String nuevoEstado) {
        log.info("🔄 Actualizando estado de presupuesto ID: {} (empresaId: {}, nuevo estado: {})",
                id, empresaId, nuevoEstado);

        // 1. Buscar el presupuesto
        PresupuestoNoCliente presupuesto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto con ID " + id + " no encontrado"));

        // 2. VALIDAR multi-tenant
        if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException(
                    "El presupuesto no pertenece a la empresa especificada");
        }

        // 3. Convertir y validar estado
        com.rodrigo.construccion.enums.PresupuestoEstado estado =
                com.rodrigo.construccion.enums.PresupuestoEstado.fromString(nuevoEstado);
        if (estado == null) {
            throw new IllegalArgumentException("Estado inválido: " + nuevoEstado);
        }

        // 4. Actualizar estado
        com.rodrigo.construccion.enums.PresupuestoEstado estadoAnterior = presupuesto.getEstado();
        presupuesto.setEstado(estado);
        PresupuestoNoCliente presupuestoGuardado = repository.save(presupuesto);

        log.info("✅ Estado actualizado: Presupuesto {} → {} (anterior: {}, empresaId: {})",
                id, estado.name(), estadoAnterior != null ? estadoAnterior.name() : "null", empresaId);

        // 5. 🔄 LÓGICA ESPECIAL PARA TAREA_LEVE: crear obra al aprobar (APROBADO o TERMINADO)
        if (presupuestoGuardado.getTipoPresupuesto() == com.rodrigo.construccion.enums.TipoPresupuesto.TAREA_LEVE
                && (estado == com.rodrigo.construccion.enums.PresupuestoEstado.APROBADO 
                    || estado == com.rodrigo.construccion.enums.PresupuestoEstado.TERMINADO)) {
            
            // Verificar si ya existe una obra propia creada para este presupuesto
            Optional<Obra> obraPropia = obraRepository.findByPresupuestoOriginalId(presupuestoGuardado.getId());
            
            if (!obraPropia.isPresent()) {
                try {
                    log.info("🏗️ TAREA_LEVE aprobado - Creando obra automáticamente con estado: {}", estado);
                    log.info("🔍 Obra padre (temporal): ID = {}", 
                            presupuestoGuardado.getObra() != null ? presupuestoGuardado.getObra().getId() : "NULL");
                    crearObraAutomaticamente(presupuestoGuardado);
                    // Refrescar presupuesto después de crear la obra
                    presupuestoGuardado = repository.findById(id).orElse(presupuestoGuardado);
                    log.info("✅ Obra creada exitosamente para presupuesto TAREA_LEVE ID: {} (nueva obra ID: {})", 
                            presupuestoGuardado.getId(), 
                            presupuestoGuardado.getObra() != null ? presupuestoGuardado.getObra().getId() : "NULL");
                } catch (Exception e) {
                    log.error("❌ Error al crear obra para TAREA_LEVE {}: {}", presupuestoGuardado.getId(), e.getMessage());
                    throw new RuntimeException("Error al crear obra: " + e.getMessage());
                }
            } else {
                log.info("ℹ️ TAREA_LEVE {} ya tiene obra propia creada (ID: {}), no se crea nueva obra",
                        presupuestoGuardado.getId(), obraPropia.get().getId());
            }
        }
        
        // 6. 🔄 SINCRONIZACIÓN BIDIRECCIONAL: Si el presupuesto tiene obra asociada, sincronizar estado
        sincronizarEstadoPresupuestoConObra(presupuestoGuardado, estado);

        return presupuestoGuardado;
    }

    /**
     * Actualiza SOLO las fechas de un presupuesto en estado APROBADO o EN_EJECUCION.
     * NO incrementa numeroVersion, NO modifica estado, NO afecta otros campos.
     * Útil para ajustar planificación temporal sin crear nueva versión.
     *
     * @param id        ID del presupuesto
     * @param empresaId ID de la empresa (validación multi-tenant)
     * @param dto       DTO con nuevas fechas
     * @return Presupuesto actualizado
     * @throws IllegalArgumentException si el presupuesto no existe, no pertenece a la empresa o no está en estado válido
     */
    @Transactional
    public PresupuestoNoCliente actualizarSoloFechas(Long id, Long empresaId,
                                                     com.rodrigo.construccion.dto.request.ActualizarFechasDTO dto) {

        log.info("📅 Actualizando fechas de presupuesto ID: {} (empresaId: {})", id, empresaId);

        // 1. VALIDAR que el presupuesto existe
        PresupuestoNoCliente presupuesto = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Presupuesto con ID " + id + " no encontrado"));

        // 2. VALIDAR que pertenece a la empresa especificada (multi-tenant)
        if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new IllegalArgumentException(
                    "El presupuesto no pertenece a la empresa especificada");
        }

        // 3. VALIDAR que el estado es APROBADO o EN_EJECUCION
        PresupuestoEstado estadoActual = presupuesto.getEstado();
        if (estadoActual != PresupuestoEstado.APROBADO &&
                estadoActual != PresupuestoEstado.EN_EJECUCION) {
            throw new IllegalArgumentException(
                    "Solo se pueden editar fechas de presupuestos APROBADOS o EN EJECUCIÓN. " +
                            "Estado actual: " + estadoActual.name());
        }

        log.info("✅ Validaciones OK - Estado: {}, Versión actual: {}",
                estadoActual, presupuesto.getNumeroVersion());

        // 4. GUARDAR valores antes de actualizar para logging
        LocalDate fechaAnterior = presupuesto.getFechaProbableInicio();
        Integer diasAnterior = presupuesto.getTiempoEstimadoTerminacion();

        // === AUDITORÍA: Guardar estado previo antes de modificar ===
        try {
            String datosJson = objectMapper.writeValueAsString(presupuesto);
            PresupuestoAuditoria auditoria = new PresupuestoAuditoria();
            auditoria.setPresupuestoId(presupuesto.getId());
            auditoria.setDatosJson(datosJson);
            auditoria.setFechaModificacion(java.time.LocalDateTime.now());
            // Usuario: si tienes sistema de autenticación, aquí deberías obtener el usuario actual
            auditoria.setUsuarioModificador("sistema");
            auditoriaService.guardarAuditoria(auditoria);
            log.info("📝 Auditoría registrada para presupuesto {}", presupuesto.getId());
        } catch (Exception ex) {
            log.error("❌ Error al guardar auditoría: {}", ex.getMessage());
        }

        // 5. ACTUALIZAR SOLO LOS CAMPOS DE FECHA (sin tocar versión ni estado)
        presupuesto.setFechaProbableInicio(dto.getFechaProbableInicio());
        presupuesto.setTiempoEstimadoTerminacion(dto.getTiempoEstimadoTerminacion());

        // 6. GUARDAR cambios (JPA hace UPDATE del registro existente)
        PresupuestoNoCliente actualizado = repository.save(presupuesto);

        log.info("✅ Fechas actualizadas exitosamente:");
        log.info("   📆 Fecha inicio: {} → {}", fechaAnterior, dto.getFechaProbableInicio());
        log.info("   ⏱️ Días estimados: {} → {}", diasAnterior, dto.getTiempoEstimadoTerminacion());
        log.info("   🔒 Versión: {} (SIN CAMBIO)", actualizado.getNumeroVersion());
        log.info("   🔒 Estado: {} (SIN CAMBIO)", actualizado.getEstado());

        // 7. SINCRONIZAR con obra asociada (si existe y NO es trabajo extra)
        if (actualizado.getObra() != null) {
            // IMPORTANTE: NO sincronizar si es presupuesto trabajo extra (la obra asociada es la obra PADRE)
            if (Boolean.TRUE.equals(actualizado.getEsPresupuestoTrabajoExtra())) {
                log.info("⚠️ Presupuesto trabajo extra - NO se sincronizan fechas con obra padre ID: {}", actualizado.getObra().getId());
            } else {
                try {
                    Obra obra = actualizado.getObra();
                    log.info("🔄 Sincronizando fechas con obra ID: {}", obra.getId());

                    // Actualizar fecha de inicio
                    obra.setFechaInicio(actualizado.getFechaProbableInicio());

                    // Calcular y actualizar fecha de fin
                    if (actualizado.getFechaProbableInicio() != null &&
                            actualizado.getTiempoEstimadoTerminacion() != null) {
                        LocalDate fechaFin = calcularFechaFin(
                                actualizado.getFechaProbableInicio(),
                                actualizado.getTiempoEstimadoTerminacion()
                        );
                        obra.setFechaFin(fechaFin);
                        log.info("   🏗️ Obra actualizada - Fecha inicio: {}, Fecha fin: {}",
                                obra.getFechaInicio(), obra.getFechaFin());
                    } else {
                        obra.setFechaFin(null);
                    }

                    obraRepository.save(obra);
                    log.info("✅ Obra sincronizada correctamente");

                } catch (Exception e) {
                    log.error("❌ Error al sincronizar fechas con obra: {}", e.getMessage());
                    // No lanzar excepción - presupuesto ya está actualizado
                }
            }
        } else {
            log.info("ℹ️ No hay obra asociada para sincronizar");
        }

        return actualizado;
    }

    /**
     * Actualiza SOLO el nombre de la obra de un presupuesto.
     * No afecta versión, estado ni otros campos.
     * Si el presupuesto está aprobado y tiene obra asociada, también actualiza el nombre de la obra.
     */
    @Transactional
    /**
     * Convierte un estado de presupuesto al estado equivalente de obra.
     * Mantiene sincronización entre las dos entidades.
     */
    /**
     * Convierte un PresupuestoEstado a EstadoObra.
     * Ahora que ambos enums están sincronizados, la conversión es directa.
     */
    private com.rodrigo.construccion.enums.EstadoObra convertirEstadoPresupuestoAObra(com.rodrigo.construccion.enums.PresupuestoEstado estadoPresupuesto) {
        return com.rodrigo.construccion.enums.EstadoObra.fromPresupuestoEstado(estadoPresupuesto);
    }

    /**
     * Sincroniza el estado del presupuesto con su obra asociada.
     * SINCRONIZACIÓN BIDIRECCIONAL: Presupuesto → Obra
     */
    private void sincronizarEstadoPresupuestoConObra(PresupuestoNoCliente presupuesto, com.rodrigo.construccion.enums.PresupuestoEstado nuevoEstado) {
        if (presupuesto.getObra() == null) {
            log.debug("ℹ️ Presupuesto {} no tiene obra asociada - sin sincronización", presupuesto.getId());
            return;
        }

        try {
            Obra obra = obraRepository.findById(presupuesto.getObra().getId())
                    .orElse(null);
            
            if (obra == null) {
                log.warn("⚠️ Obra {} no encontrada para presupuesto {} - sincronización omitida",
                        presupuesto.getObra().getId(), presupuesto.getId());
                return;
            }

            // Convertir estado de presupuesto a estado de obra
            com.rodrigo.construccion.enums.EstadoObra nuevoEstadoObra = convertirEstadoPresupuestoAObra(nuevoEstado);
            com.rodrigo.construccion.enums.EstadoObra estadoObraActual = obra.getEstadoEnum();

            // Solo actualizar si el estado cambió
            if (estadoObraActual != nuevoEstadoObra) {
                obra.setEstado(nuevoEstadoObra);
                obraRepository.save(obra);
                log.info("🔄 SINCRONIZACIÓN Presupuesto→Obra: Presupuesto {} ({}) → Obra {} ({} → {})",
                        presupuesto.getId(), nuevoEstado.getDisplayValue(),
                        obra.getId(), estadoObraActual != null ? estadoObraActual.getDisplayName() : "NULL",
                        nuevoEstadoObra.getDisplayName());
            } else {
                log.debug("✓ Estados ya sincronizados - Presupuesto {} y Obra {} ambos en estado {}",
                        presupuesto.getId(), obra.getId(), nuevoEstadoObra.getDisplayName());
            }
        } catch (Exception e) {
            log.error("❌ ERROR al sincronizar estado Presupuesto {} → Obra {}: {} - Stack: {}",
                    presupuesto.getId(),
                    presupuesto.getObra() != null ? presupuesto.getObra().getId() : "NULL",
                    e.getMessage(), e.getClass().getSimpleName());
            // No lanzar excepción para no interrumpir el flujo principal
        }
    }

    public java.util.List<PresupuestoNoCliente> busquedaAvanzada(
            Long empresaId, String direccionObraBarrio, String direccionObraCalle, String direccionObraAltura,
            String direccionObraTorre, String direccionObraPiso, String direccionObraDepartamento,
            String nombreSolicitante, String telefono, String mail, String direccionParticular,
            Long numeroPresupuesto, String estado, Integer numeroVersion, String descripcion,
            java.time.LocalDate fechaEmisionDesde, java.time.LocalDate fechaEmisionHasta,
            java.time.LocalDate fechaCreacionDesde, java.time.LocalDate fechaCreacionHasta,
            java.time.LocalDate fechaProbableInicioDesde, java.time.LocalDate fechaProbableInicioHasta,
            java.time.LocalDate vencimientoDesde, java.time.LocalDate vencimientoHasta,
            java.math.BigDecimal totalGeneralMinimo, java.math.BigDecimal totalGeneralMaximo,
            java.math.BigDecimal totalProfesionalesMinimo, java.math.BigDecimal totalProfesionalesMaximo,
            java.math.BigDecimal totalMaterialesMinimo, java.math.BigDecimal totalMaterialesMaximo,
            String tipoProfesionalPresupuesto, String modoPresupuesto) {

        log.info("🔍 Búsqueda avanzada - empresaId: {}, estado: {}, direccionObraCalle: {}", empresaId, estado, direccionObraCalle);

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<PresupuestoNoCliente> query = cb.createQuery(PresupuestoNoCliente.class);
        Root<PresupuestoNoCliente> root = query.from(PresupuestoNoCliente.class);

        List<Predicate> predicates = new ArrayList<>();

        // FILTRO OBLIGATORIO: empresaId (multi-tenant)
        predicates.add(cb.equal(root.get("empresa").get("id"), empresaId));

        // Filtros de dirección de obra
        if (direccionObraBarrio != null && !direccionObraBarrio.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionObraBarrio")), "%" + direccionObraBarrio.toLowerCase() + "%"));
        }
        if (direccionObraCalle != null && !direccionObraCalle.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionObraCalle")), "%" + direccionObraCalle.toLowerCase() + "%"));
        }
        if (direccionObraAltura != null && !direccionObraAltura.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionObraAltura")), "%" + direccionObraAltura.toLowerCase() + "%"));
        }
        if (direccionObraTorre != null && !direccionObraTorre.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionObraTorre")), "%" + direccionObraTorre.toLowerCase() + "%"));
        }
        if (direccionObraPiso != null && !direccionObraPiso.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionObraPiso")), "%" + direccionObraPiso.toLowerCase() + "%"));
        }
        if (direccionObraDepartamento != null && !direccionObraDepartamento.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionObraDepartamento")), "%" + direccionObraDepartamento.toLowerCase() + "%"));
        }

        // Filtros de datos del solicitante
        if (nombreSolicitante != null && !nombreSolicitante.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("nombreSolicitante")), "%" + nombreSolicitante.toLowerCase() + "%"));
        }
        if (telefono != null && !telefono.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("telefono")), "%" + telefono.toLowerCase() + "%"));
        }
        if (mail != null && !mail.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("mail")), "%" + mail.toLowerCase() + "%"));
        }
        if (direccionParticular != null && !direccionParticular.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("direccionParticular")), "%" + direccionParticular.toLowerCase() + "%"));
        }

        // Filtros de información del presupuesto
        if (numeroPresupuesto != null) {
            predicates.add(cb.equal(root.get("numeroPresupuesto"), numeroPresupuesto));
        }
        if (estado != null && !estado.trim().isEmpty()) {
            try {
                PresupuestoEstado estadoEnum = PresupuestoEstado.valueOf(estado.toUpperCase());
                predicates.add(cb.equal(root.get("estado"), estadoEnum));
            } catch (IllegalArgumentException e) {
                log.warn("Estado inválido ignorado: {}", estado);
            }
        }
        if (numeroVersion != null) {
            predicates.add(cb.equal(root.get("numeroVersion"), numeroVersion));
        }
        if (descripcion != null && !descripcion.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("descripcion")), "%" + descripcion.toLowerCase() + "%"));
        }
        if (tipoProfesionalPresupuesto != null && !tipoProfesionalPresupuesto.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("tipoProfesionalPresupuesto")), "%" + tipoProfesionalPresupuesto.toLowerCase() + "%"));
        }
        if (modoPresupuesto != null && !modoPresupuesto.trim().isEmpty()) {
            predicates.add(cb.like(cb.lower(root.get("modoPresupuesto")), "%" + modoPresupuesto.toLowerCase() + "%"));
        }

        // Filtros de fechas (rangos)
        if (fechaEmisionDesde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaEmision"), fechaEmisionDesde));
        }
        if (fechaEmisionHasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaEmision"), fechaEmisionHasta));
        }
        if (fechaCreacionDesde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaCreacion"), fechaCreacionDesde));
        }
        if (fechaCreacionHasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaCreacion"), fechaCreacionHasta));
        }
        if (fechaProbableInicioDesde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("fechaProbableInicio"), fechaProbableInicioDesde));
        }
        if (fechaProbableInicioHasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("fechaProbableInicio"), fechaProbableInicioHasta));
        }
        if (vencimientoDesde != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("vencimiento"), vencimientoDesde));
        }
        if (vencimientoHasta != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("vencimiento"), vencimientoHasta));
        }

        // Filtros de montos (rangos)
        if (totalGeneralMinimo != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalPresupuesto"), totalGeneralMinimo));
        }
        if (totalGeneralMaximo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalPresupuesto"), totalGeneralMaximo));
        }
        if (totalProfesionalesMinimo != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalProfesionales"), totalProfesionalesMinimo));
        }
        if (totalProfesionalesMaximo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalProfesionales"), totalProfesionalesMaximo));
        }
        if (totalMaterialesMinimo != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("totalMateriales"), totalMaterialesMinimo));
        }
        if (totalMaterialesMaximo != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("totalMateriales"), totalMaterialesMaximo));
        }

        // Aplicar todos los predicates con AND
        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // Ordenar por fecha de creación descendente (más recientes primero)
        query.orderBy(cb.desc(root.get("fechaCreacion")));

        // Ejecutar query
        List<PresupuestoNoCliente> resultados = entityManager.createQuery(query).getResultList();

        log.info("✅ Búsqueda avanzada completada - {} resultados encontrados", resultados.size());

        // Calcular campos calculados y enriquecer profesionalObraId si aplica
        resultados.forEach(p -> {
            p.calcularCamposCalculados();
            if (p.getEstado() == PresupuestoEstado.APROBADO && p.getObra() != null) {
                enriquecerProfesionalesConObraId(p);
            }
        });

        return resultados;
    }


    public java.util.List<PresupuestoNoCliente> buscarPorTipoProfesional(String tipoProfesional, Long empresaId) {
        // Implementación básica - puede mejorarse según necesidades
        List<PresupuestoNoCliente> presupuestos = repository.findAll();
        // Calcular campos calculados
        presupuestos.forEach(PresupuestoNoCliente::calcularCamposCalculados);
        return presupuestos;
    }

    /**
     * Sincroniza los datos del cliente desde el presupuesto hacia la obra asociada.
     * Este método es llamado automáticamente después de guardar un presupuesto.
     *
     * @param presupuesto El presupuesto con los datos actualizados
     */
    private void sincronizarDatosClienteConObra(PresupuestoNoCliente presupuesto) {
        if (presupuesto.getObra() == null) {
            return;
        }

        // IMPORTANTE: NO sincronizar si es presupuesto trabajo extra (la obra asociada es la obra PADRE)
        if (Boolean.TRUE.equals(presupuesto.getEsPresupuestoTrabajoExtra())) {
            log.info("⚠️ Presupuesto trabajo extra {} - NO se sincronizan datos con obra padre {}",
                    presupuesto.getId(), presupuesto.getObra().getId());
            return;
        }

        try {
            Optional<Obra> obraOpt = obraRepository.findById(presupuesto.getObra().getId());

            if (obraOpt.isEmpty()) {
                log.warn("⚠️ Obra con ID {} no encontrada para presupuesto {}",
                        presupuesto.getObra().getId(), presupuesto.getId());
                return;
            }

            Obra obra = obraOpt.get();

            EstadoObra estadoObra = obra.getEstadoEnum();
            if (estadoObra == EstadoObra.TERMINADO || estadoObra == EstadoObra.CANCELADO) {
                log.info("ℹ️ Obra {} está {} - No se actualizan datos del cliente",
                        obra.getId(), estadoObra.getDisplayName());
                return;
            }

            boolean actualizado = false;

            if (presupuesto.getNombreSolicitante() != null &&
                    !presupuesto.getNombreSolicitante().equals(obra.getNombreSolicitante())) {
                obra.setNombreSolicitante(presupuesto.getNombreSolicitante());
                actualizado = true;
            }

            if (presupuesto.getTelefono() != null &&
                    !presupuesto.getTelefono().equals(obra.getTelefono())) {
                obra.setTelefono(presupuesto.getTelefono());
                actualizado = true;
            }

            if (presupuesto.getMail() != null &&
                    !presupuesto.getMail().equals(obra.getMail())) {
                obra.setMail(presupuesto.getMail());
                actualizado = true;
            }

            if (presupuesto.getDireccionParticular() != null &&
                    !presupuesto.getDireccionParticular().equals(obra.getDireccionParticular())) {
                obra.setDireccionParticular(presupuesto.getDireccionParticular());
                actualizado = true;
            }

            if (actualizado) {
                obraRepository.save(obra);
                log.info("✅ Datos del cliente sincronizados: Presupuesto {} → Obra {}",
                        presupuesto.getId(), obra.getId());
            }

        } catch (Exception e) {
            log.error("❌ Error al sincronizar datos con obra {}: {}",
                    presupuesto.getObra() != null ? presupuesto.getObra().getId() : null, e.getMessage(), e);
        }
    }

    /**
     * Procesa los costos iniciales del presupuesto (crear o actualizar).
     *
     * @param presupuesto Presupuesto guardadocon datos de costos iniciales (puede ser null)
     */
    private void procesarItemsCalculadora(PresupuestoNoCliente presupuesto, List<ItemCalculadoraPresupuestoDTO> itemsDTO) {
        if (itemsDTO == null || itemsDTO.isEmpty()) {
            log.debug("No se proporcionaron items de calculadora para el presupuesto {}", presupuesto.getId());

            // ========== ELIMINAR TODOS LOS ITEMS EXISTENTES (CON ORDEN CORRECTO) ==========
            log.info("🗑️ ELIMINANDO todos los items del presupuesto ID: {} (lista vacía recibida)", presupuesto.getId());

            // PASO 1: Obtener todos los items del presupuesto
            List<ItemCalculadoraPresupuesto> itemsExistentes = itemCalculadoraRepository.findByPresupuestoNoClienteId(presupuesto.getId());

            // PASO 2: Eliminar PRIMERO las entidades hijas de cada item (para respetar foreign keys)
            for (ItemCalculadoraPresupuesto item : itemsExistentes) {
                Long itemId = item.getId();
                log.debug("🗑️ Eliminando entidades hijas del item ID: {}", itemId);

                // Eliminar profesionales, materiales, jornales y gastos generales
                profesionalCalculadoraRepository.deleteByItemCalculadoraId(itemId);
                materialCalculadoraRepository.deleteByItemCalculadoraId(itemId);
                jornalCalculadoraRepository.deleteByItemCalculadoraId(itemId);
                gastoGeneralRepository.deleteByItemCalculadoraId(itemId);
            }

            // PASO 3: AHORA SÍ eliminar los items
            itemCalculadoraRepository.deleteByPresupuestoNoClienteId(presupuesto.getId());
            log.info("✅ Todos los items eliminados para presupuesto {}", presupuesto.getId());
            return;
        }

        try {
            // ========== ESTRATEGIA MERGE/UPSERT: NO DELETE MASIVO ==========
            log.info("🔄 INICIANDO MERGE de {} items para presupuesto ID: {}", itemsDTO.size(), presupuesto.getId());

            // PASO 1: Obtener items existentes de la base de datos
            List<ItemCalculadoraPresupuesto> itemsExistentes = itemCalculadoraRepository.findByPresupuestoNoClienteId(presupuesto.getId());
            Map<Long, ItemCalculadoraPresupuesto> itemsExistentesMap = itemsExistentes.stream()
                    .collect(java.util.stream.Collectors.toMap(ItemCalculadoraPresupuesto::getId, item -> item));

            log.info("📋 Items existentes en BD: {}", itemsExistentesMap.keySet());

            // PASO 2: Crear set de IDs que vienen en el payload (solo IDs numéricos del backend)
            Set<Long> idsEnPayload = itemsDTO.stream()
                    .map(ItemCalculadoraPresupuestoDTO::getId)
                    .filter(id -> id != null && isNumericId(id)) // Solo IDs numéricos del backend
                    .map(Long::valueOf)
                    .collect(java.util.stream.Collectors.toSet());

            log.info("📥 IDs de items en payload (backend): {}", idsEnPayload);

            // PASO 3: Identificar items a eliminar (están en BD pero NO en payload)
            Set<Long> idsAEliminar = new java.util.HashSet<>(itemsExistentesMap.keySet());
            idsAEliminar.removeAll(idsEnPayload);

            if (!idsAEliminar.isEmpty()) {
                log.info("🗑️ Items a ELIMINAR: {}", idsAEliminar);
                for (Long idEliminar : idsAEliminar) {
                    ItemCalculadoraPresupuesto itemAEliminar = itemsExistentesMap.get(idEliminar);
                    log.debug("🗑️ Eliminando item ID: {} y sus entidades hijas", idEliminar);

                    // Eliminar entidades hijas primero
                    profesionalCalculadoraRepository.deleteByItemCalculadoraId(idEliminar);
                    materialCalculadoraRepository.deleteByItemCalculadoraId(idEliminar);
                    jornalCalculadoraRepository.deleteByItemCalculadoraId(idEliminar);
                    gastoGeneralRepository.deleteByItemCalculadoraId(idEliminar);

                    // Eliminar el item
                    itemCalculadoraRepository.delete(itemAEliminar);
                    log.info("✅ Item ID {} eliminado", idEliminar);
                }
            } else {
                log.info("ℹ️ No hay items para eliminar");
            }

            // 🔍 DEBUG: Mostrar qué items vienen en el payload
            for (int i = 0; i < itemsDTO.size(); i++) {
                ItemCalculadoraPresupuestoDTO dto = itemsDTO.get(i);
                log.info("📦 Item #{}: id={}, tipoProfesional={}, esGastoGeneral={}, gastosGenerales={}",
                        i + 1,
                        dto.getId(),
                        dto.getTipoProfesional(),
                        dto.getEsGastoGeneral(),
                        dto.getGastosGenerales() != null ? dto.getGastosGenerales().size() + " gastos" : "NULL");
            }

            // ========== VALIDACIÓN: NO PERMITIR RUBROS DUPLICADOS ==========
            Map<String, Long> rubrosPorNombre = new java.util.HashMap<>();
            for (ItemCalculadoraPresupuestoDTO dto : itemsDTO) {
                String tipoProfesional = dto.getTipoProfesional();
                if (tipoProfesional != null && !tipoProfesional.trim().isEmpty()) {
                    // Normalizar el nombre para comparación (eliminar espacios, convertir a minúsculas)
                    String rubroNormalizado = tipoProfesional.trim();
                    
                    if (rubrosPorNombre.containsKey(rubroNormalizado)) {
                        log.error("❌ DUPLICADO DETECTADO: Rubro '{}' aparece más de una vez en el payload", tipoProfesional);
                        throw new IllegalArgumentException(
                            String.format("No se puede guardar el presupuesto: el rubro '%s' está duplicado. " +
                                         "Cada rubro debe aparecer solo una vez.", tipoProfesional)
                        );
                    }
                    rubrosPorNombre.put(rubroNormalizado, 1L);
                }
            }
            log.info("✅ Validación anti-duplicados: {} rubros únicos detectados", rubrosPorNombre.size());

            // PASO 4: Procesar cada item del payload (UPDATE o INSERT)
            for (ItemCalculadoraPresupuestoDTO dto : itemsDTO) {
                ItemCalculadoraPresupuesto item;
                boolean esActualizacion = false;

                // Verificar si es un item existente (tiene id numérico del backend)
                if (dto.getId() != null && isNumericId(dto.getId())) {
                    Long itemId = Long.valueOf(dto.getId());
                    if (itemsExistentesMap.containsKey(itemId)) {
                        // UPDATE: Item existente
                        item = itemsExistentesMap.get(itemId);
                        esActualizacion = true;
                        log.info("🔄 UPDATE item ID: {}", itemId);
                    } else {
                        // INSERT: ID no encontrado en BD (raro, pero crear nuevo)
                        item = new ItemCalculadoraPresupuesto();
                        item.setPresupuestoNoCliente(presupuesto);
                        item.setEmpresa(presupuesto.getEmpresa());
                        log.info("➕ INSERT item (ID {} no encontrado en BD)", itemId);
                    }
                } else {
                    // INSERT: Item nuevo (sin id o con id temporal tipo "temp_123")
                    item = new ItemCalculadoraPresupuesto();
                    item.setPresupuestoNoCliente(presupuesto);
                    item.setEmpresa(presupuesto.getEmpresa());
                    log.info("➕ INSERT item nuevo (id temporal: {})", dto.getId());
                }

                // Actualizar campos del item
                item.setTipoProfesional(dto.getTipoProfesional());
                item.setDescripcion(dto.getDescripcion());
                item.setObservaciones(dto.getObservaciones());
                item.setCantidadJornales(dto.getCantidadJornales());
                item.setImporteJornal(dto.getImporteJornal());
                item.setSubtotalManoObra(dto.getSubtotalManoObra() != null ? dto.getSubtotalManoObra() : BigDecimal.ZERO);
                item.setMateriales(dto.getMateriales());
                item.setTotalManual(dto.getTotalManual());
                item.setDescripcionTotalManual(dto.getDescripcionTotalManual());
                item.setObservacionesTotalManual(dto.getObservacionesTotalManual());
                item.setTotal(dto.getTotal());
                item.setEsModoManual(dto.getEsModoManual());
                item.setIncluirEnCalculoDias(dto.getIncluirEnCalculoDias() != null ? dto.getIncluirEnCalculoDias() : true);
                item.setTrabajaEnParalelo(dto.getTrabajaEnParalelo() != null ? dto.getTrabajaEnParalelo() : true);
                item.setSubtotalMateriales(dto.getSubtotalMateriales());

                // Campos de gastos generales
                item.setEsGastoGeneral(dto.getEsGastoGeneral() != null ? dto.getEsGastoGeneral() : false);
                item.setSubtotalGastosGenerales(dto.getSubtotalGastosGenerales());
                item.setDescripcionGastosGenerales(dto.getDescripcionGastosGenerales());
                item.setObservacionesGastosGenerales(dto.getObservacionesGastosGenerales());

                // Campos de descripción/observaciones por categoría
                item.setDescripcionProfesionales(dto.getDescripcionProfesionales());
                item.setObservacionesProfesionales(dto.getObservacionesProfesionales());
                item.setDescripcionMateriales(dto.getDescripcionMateriales());
                item.setObservacionesMateriales(dto.getObservacionesMateriales());

                // Calcular totales
                item.calcularTotales();
                item.validar();

                // Guardar item principal
                ItemCalculadoraPresupuesto itemGuardado = itemCalculadoraRepository.save(item);
                log.info("✅ Item guardado ID: {} ({})", itemGuardado.getId(), esActualizacion ? "UPDATE" : "INSERT");

                // ========== PROCESAR ENTIDADES HIJAS CON MERGE/UPSERT ==========

                // Profesionales
                procesarProfesionalesDelItem(itemGuardado, dto.getProfesionales(), presupuesto.getEmpresa());

                // Materiales
                procesarMaterialesDelItem(itemGuardado, dto.getMaterialesLista(), presupuesto.getEmpresa());

                // Jornales
                procesarJornalesDelItem(itemGuardado, dto.getJornales(), presupuesto.getEmpresa());

                // Gastos generales
                procesarGastosGeneralesDelItem(itemGuardado, dto.getGastosGenerales(), presupuesto.getEmpresa());

                // ========== RECALCULAR TOTALES CONSOLIDADOS DESDE TABLAS HIJAS ==========
                recalcularTotalesItemDesdeHijos(itemGuardado);
            }

            log.info("✅ {} items de calculadora procesados para presupuesto {}",
                    itemsDTO.size(), presupuesto.getId());

        } catch (IllegalStateException e) {
            log.error("❌ Error de validación al procesar items de calculadora para presupuesto {}: {}",
                    presupuesto.getId(), e.getMessage());
            throw new IllegalArgumentException("Error de validación en items de calculadora: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("❌ Error al procesar items de calculadora para presupuesto {}: {}",
                    presupuesto.getId(), e.getMessage(), e);
            throw new RuntimeException("Error al procesar items de calculadora: " + e.getMessage(), e);
        }
    }

    /**
     * Verifica si un ID es numérico (del backend) o temporal (del frontend)
     */
    private boolean isNumericId(Object id) {
        if (id == null) return false;
        String idStr = id.toString();
        try {
            Long.parseLong(idStr);
            return true;
        } catch (NumberFormatException e) {
            return false; // Es temporal tipo "temp_123" o timestamp
        }
    }

    /**
     * Procesa profesionales de un item con lógica merge/upsert
     */
    private void procesarProfesionalesDelItem(ItemCalculadoraPresupuesto item,
                                              List<ProfesionalCalculadoraDTO> profesionalesDTO,
                                              Empresa empresa) {
        if (profesionalesDTO == null) {
            profesionalesDTO = new java.util.ArrayList<>();
        }

        // Obtener profesionales existentes
        List<ProfesionalCalculadora> existentes = profesionalCalculadoraRepository.findByItemCalculadoraId(item.getId());
        Map<Long, ProfesionalCalculadora> existentesMap = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(ProfesionalCalculadora::getId, p -> p));

        // IDs que vienen en el payload
        Set<Long> idsEnPayload = profesionalesDTO.stream()
                .map(ProfesionalCalculadoraDTO::getId)
                .filter(id -> id != null && isNumericId(id))
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toSet());

        // Eliminar profesionales que no vienen en el payload
        Set<Long> idsAEliminar = new java.util.HashSet<>(existentesMap.keySet());
        idsAEliminar.removeAll(idsEnPayload);
        for (Long id : idsAEliminar) {
            profesionalCalculadoraRepository.deleteById(id);
            log.debug("🗑️ Profesional ID {} eliminado", id);
        }

        // UPDATE o INSERT cada profesional
        for (ProfesionalCalculadoraDTO dto : profesionalesDTO) {
            ProfesionalCalculadora profesional;

            if (dto.getId() != null && isNumericId(dto.getId()) && existentesMap.containsKey(Long.valueOf(dto.getId().toString()))) {
                profesional = existentesMap.get(Long.valueOf(dto.getId().toString()));
            } else {
                profesional = new ProfesionalCalculadora();
                profesional.setItemCalculadora(item);
                profesional.setEmpresa(empresa);
            }

            profesional.setFrontendId(dto.getId());
            profesional.setTipo(dto.getTipo());
            profesional.setNombre(dto.getNombre());
            profesional.setEsGlobal(dto.getEsGlobal() != null ? dto.getEsGlobal() : false);
            profesional.setDescripcion(dto.getDescripcion());
            profesional.setObservaciones(dto.getObservaciones());
            profesional.setTelefono(dto.getTelefono());
            profesional.setUnidad(dto.getUnidad());
            profesional.setCantidadJornales(dto.getCantidadJornales());
            profesional.setImporteJornal(dto.getImporteJornal());
            profesional.setSubtotal(dto.getSubtotal());
            profesional.setSinCantidad(dto.getSinCantidad() != null ? dto.getSinCantidad() : false);
            profesional.setSinImporte(dto.getSinImporte() != null ? dto.getSinImporte() : false);

            profesionalCalculadoraRepository.save(profesional);
        }
    }

    /**
     * Procesa materiales de un item con lógica merge/upsert
     */
    private void procesarMaterialesDelItem(ItemCalculadoraPresupuesto item,
                                           List<MaterialCalculadoraDTO> materialesDTO,
                                           Empresa empresa) {
        if (materialesDTO == null) {
            materialesDTO = new java.util.ArrayList<>();
        }

        // Obtener materiales existentes
        List<MaterialCalculadora> existentes = materialCalculadoraRepository.findByItemCalculadoraId(item.getId());
        Map<Long, MaterialCalculadora> existentesMap = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(MaterialCalculadora::getId, m -> m));

        // IDs que vienen en el payload
        Set<Long> idsEnPayload = materialesDTO.stream()
                .map(MaterialCalculadoraDTO::getId)
                .filter(id -> id != null && isNumericId(id))
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toSet());

        // Eliminar materiales que no vienen en el payload
        Set<Long> idsAEliminar = new java.util.HashSet<>(existentesMap.keySet());
        idsAEliminar.removeAll(idsEnPayload);
        for (Long id : idsAEliminar) {
            materialCalculadoraRepository.deleteById(id);
            log.debug("🗑️ Material ID {} eliminado", id);
        }

        // UPDATE o INSERT cada material
        for (MaterialCalculadoraDTO dto : materialesDTO) {
            MaterialCalculadora material;

            if (dto.getId() != null && isNumericId(dto.getId()) && existentesMap.containsKey(Long.valueOf(dto.getId().toString()))) {
                material = existentesMap.get(Long.valueOf(dto.getId().toString()));
            } else {
                material = new MaterialCalculadora();
                material.setItemCalculadora(item);
                material.setEmpresa(empresa);
            }

            material.setFrontendId(dto.getId());
            material.setNombre(dto.getNombre());
            material.setEsGlobal(dto.getEsGlobal() != null ? dto.getEsGlobal() : false);
            material.setDescripcion(dto.getDescripcion());
            material.setObservaciones(dto.getObservaciones());
            material.setUnidad(dto.getUnidad());
            material.setCantidad(dto.getCantidad());
            material.setPrecio(dto.getPrecio());
            material.setSubtotal(dto.getSubtotal());

            materialCalculadoraRepository.save(material);
        }
    }

    /**
     * Procesa jornales de un item con lógica merge/upsert
     */
    private void procesarJornalesDelItem(ItemCalculadoraPresupuesto item,
                                         List<JornalCalculadoraDTO> jornalesDTO,
                                         Empresa empresa) {
        if (jornalesDTO == null) {
            jornalesDTO = new java.util.ArrayList<>();
        }

        log.info("💼 Procesando {} jornales para item ID: {}", jornalesDTO.size(), item.getId());

        // Obtener jornales existentes
        List<JornalCalculadora> existentes = jornalCalculadoraRepository.findByItemCalculadoraId(item.getId());
        Map<Long, JornalCalculadora> existentesMap = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(JornalCalculadora::getId, j -> j));

        // IDs que vienen en el payload
        Set<Long> idsEnPayload = jornalesDTO.stream()
                .map(JornalCalculadoraDTO::getId)
                .filter(id -> id != null && isNumericId(id))
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toSet());

        log.debug("📋 Jornales existentes: {}, En payload: {}", existentesMap.keySet(), idsEnPayload);

        // Eliminar jornales que no vienen en el payload
        Set<Long> idsAEliminar = new java.util.HashSet<>(existentesMap.keySet());
        idsAEliminar.removeAll(idsEnPayload);
        for (Long id : idsAEliminar) {
            jornalCalculadoraRepository.deleteById(id);
            log.debug("🗑️ Jornal ID {} eliminado", id);
        }

        // UPDATE o INSERT cada jornal
        for (JornalCalculadoraDTO dto : jornalesDTO) {
            JornalCalculadora jornal;

            if (dto.getId() != null && isNumericId(dto.getId()) && existentesMap.containsKey(Long.valueOf(dto.getId().toString()))) {
                jornal = existentesMap.get(Long.valueOf(dto.getId().toString()));
                log.debug("🔄 UPDATE jornal ID: {}", jornal.getId());
            } else {
                jornal = new JornalCalculadora();
                jornal.setItemCalculadora(item);
                jornal.setEmpresa(empresa);
                log.debug("➕ INSERT jornal nuevo (frontendId: {})", dto.getFrontendId());
            }

            jornal.setFrontendId(dto.getFrontendId());
            jornal.setRol(dto.getRol());
            jornal.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : BigDecimal.ZERO);
            jornal.setValorUnitario(dto.getValorUnitario() != null ? dto.getValorUnitario() : BigDecimal.ZERO);
            jornal.setIncluirEnCalculoDias(dto.getIncluirEnCalculoDias() != null ? dto.getIncluirEnCalculoDias() : true);

            // Calcular subtotal
            BigDecimal subtotal = jornal.getCantidad().multiply(jornal.getValorUnitario());
            jornal.setSubtotal(subtotal);
            jornal.setObservaciones(dto.getObservaciones());

            jornalCalculadoraRepository.save(jornal);
            log.debug("✅ Jornal guardado: {} - {} × {} = {}",
                    jornal.getRol(), jornal.getCantidad(), jornal.getValorUnitario(), jornal.getSubtotal());
        }
    }

    /**
     * Procesa gastos generales de un item con lógica merge/upsert
     */
    private void procesarGastosGeneralesDelItem(ItemCalculadoraPresupuesto item,
                                                List<GastoGeneralDTO> gastosDTO,
                                                Empresa empresa) {
        if (gastosDTO == null || gastosDTO.isEmpty()) {
            // Si no hay gastos en el payload, eliminar todos los existentes
            gastoGeneralRepository.deleteByItemCalculadoraId(item.getId());
            return;
        }

        log.info("💰 Procesando {} gastos generales para item ID: {}", gastosDTO.size(), item.getId());

        // Obtener gastos existentes
        List<PresupuestoGastoGeneral> existentes = gastoGeneralRepository.findByItemCalculadoraId(item.getId());
        Map<Long, PresupuestoGastoGeneral> existentesMap = existentes.stream()
                .collect(java.util.stream.Collectors.toMap(PresupuestoGastoGeneral::getId, g -> g));

        // IDs que vienen en el payload
        Set<Long> idsEnPayload = gastosDTO.stream()
                .map(GastoGeneralDTO::getId)
                .filter(id -> id != null && isNumericId(id))
                .map(id -> Long.valueOf(id.toString()))
                .collect(java.util.stream.Collectors.toSet());

        // Eliminar gastos que no vienen en el payload
        Set<Long> idsAEliminar = new java.util.HashSet<>(existentesMap.keySet());
        idsAEliminar.removeAll(idsEnPayload);
        for (Long id : idsAEliminar) {
            gastoGeneralRepository.deleteById(id);
            log.debug("🗑️ Gasto general ID {} eliminado", id);
        }

        // UPDATE o INSERT cada gasto
        int orden = 1;
        for (GastoGeneralDTO dto : gastosDTO) {
            PresupuestoGastoGeneral gasto;

            if (dto.getId() != null && isNumericId(dto.getId()) && existentesMap.containsKey(Long.valueOf(dto.getId().toString()))) {
                gasto = existentesMap.get(Long.valueOf(dto.getId().toString()));
            } else {
                gasto = new PresupuestoGastoGeneral();
                gasto.setItemCalculadora(item);
                gasto.setEmpresa(empresa);
            }

            gasto.setDescripcion(dto.getDescripcion());
            gasto.setObservaciones(dto.getObservaciones());
            gasto.setEsGlobal(dto.getEsGlobal() != null ? dto.getEsGlobal() : false);
            gasto.setCantidad(dto.getCantidad() != null ? dto.getCantidad() : BigDecimal.ONE);
            gasto.setPrecioUnitario(dto.getPrecioUnitario() != null ? dto.getPrecioUnitario() : BigDecimal.ZERO);
            gasto.setSubtotal(dto.getSubtotal() != null ? dto.getSubtotal() : BigDecimal.ZERO);
            gasto.setSinCantidad(dto.getSinCantidad() != null ? dto.getSinCantidad() : false);
            gasto.setSinPrecio(dto.getSinPrecio() != null ? dto.getSinPrecio() : false);
            gasto.setOrden(dto.getOrden() != null ? dto.getOrden() : orden++);

            gasto.configurarDefaults();
            gasto.validar();

            gastoGeneralRepository.save(gasto);
        }

        // Calcular y actualizar totales de gastos generales en el item
        BigDecimal totalGastosGenerales = BigDecimal.ZERO;
        for (GastoGeneralDTO gastoDto : gastosDTO) {
            BigDecimal subtotalGasto = gastoDto.getSubtotal() != null ? gastoDto.getSubtotal() : BigDecimal.ZERO;
            totalGastosGenerales = totalGastosGenerales.add(subtotalGasto);
        }

        item.setSubtotalGastosGenerales(totalGastosGenerales);

        // Recalcular total completo del item
        BigDecimal subtotalManoObra = item.getSubtotalManoObra() != null ? item.getSubtotalManoObra() : BigDecimal.ZERO;
        BigDecimal subtotalMateriales = item.getSubtotalMateriales() != null ? item.getSubtotalMateriales() : BigDecimal.ZERO;
        BigDecimal totalCompleto = subtotalManoObra.add(subtotalMateriales).add(totalGastosGenerales);
        item.setTotal(totalCompleto);

        itemCalculadoraRepository.save(item);
        log.info("✅ Gastos generales procesados. Total: {}", totalGastosGenerales);
    }

    /**
     * Procesa y guarda/actualiza los costos iniciales asociados a un presupuesto.
     *
     * @param presupuesto Presupuesto al que se asocian los costos
     * @param dto         DTO con datos de costos iniciales (puede ser null)
     */
    private void procesarCostosIniciales(PresupuestoNoCliente presupuesto, PresupuestoCostoInicialDTO dto) {
        if (dto == null) {
            log.debug("No se proporcionaron costos iniciales para el presupuesto {}", presupuesto.getId());
            return;
        }

        try {
            // Buscar si ya existe un costo inicial para este presupuesto
            Optional<PresupuestoCostoInicial> costoExistente =
                    costoInicialRepository.findByPresupuestoNoClienteId(presupuesto.getId());

            PresupuestoCostoInicial costoInicial;

            if (costoExistente.isPresent()) {
                // ACTUALIZAR existente
                costoInicial = costoExistente.get();
                log.debug("📝 Actualizando costos iniciales existentes para presupuesto {}", presupuesto.getId());
            } else {
                // CREAR nuevo
                costoInicial = new PresupuestoCostoInicial();
                costoInicial.setPresupuestoNoCliente(presupuesto);
                log.debug("➕ Creando nuevos costos iniciales para presupuesto {}", presupuesto.getId());
            }

            // Mapear datos del DTO a la entidad
            costoInicial.setMetrosCuadrados(dto.getMetrosCuadrados());
            costoInicial.setImportePorMetro(dto.getImportePorMetro());
            costoInicial.setPorcentajeProfesionales(dto.getPorcentajeProfesionales());
            costoInicial.setPorcentajeMateriales(dto.getPorcentajeMateriales());
            costoInicial.setPorcentajeOtrosCostos(dto.getPorcentajeOtrosCostos());

            // Calcular montos derivados
            costoInicial.calcularMontos();

            // Validar suma de porcentajes
            if (!costoInicial.validarSumaPorcentajes()) {
                throw new IllegalArgumentException(
                        "La suma de porcentajes no puede exceder 100. " +
                                "Profesionales: " + dto.getPorcentajeProfesionales() + "%, " +
                                "Materiales: " + dto.getPorcentajeMateriales() + "%, " +
                                "Otros Costos: " + dto.getPorcentajeOtrosCostos() + "%"
                );
            }

            // Guardar
            costoInicialRepository.save(costoInicial);

            log.info("✅ Costos iniciales procesados para presupuesto {}: {} m² x ${} = ${}",
                    presupuesto.getId(),
                    costoInicial.getMetrosCuadrados(),
                    costoInicial.getImportePorMetro(),
                    costoInicial.getTotalEstimado());

        } catch (Exception e) {
            log.error("❌ Error al procesar costos iniciales para presupuesto {}: {}",
                    presupuesto.getId(), e.getMessage(), e);
            throw new IllegalArgumentException("Error al procesar costos iniciales: " + e.getMessage(), e);
        }
    }

    // ========== METODOS DE RECALCULO DE TOTALES ==========

    /**
     * CRÍTICO: Recalcula los campos consolidados del item sumando desde las tablas hijas.
     * Este método debe llamarse DESPUÉS de guardar todos los hijos (materiales, jornales, gastos).
     * 
     * CORRIGE el bug donde el frontend envía valores incorrectos y el backend los guardaba sin validar.
     */
    private void recalcularTotalesItemDesdeHijos(ItemCalculadoraPresupuesto item) {
        log.debug("🔄 Recalculando totales del item ID {} desde tablas hijas", item.getId());

        // 1. Recalcular materiales desde material_calculadora
        List<MaterialCalculadora> materiales = materialCalculadoraRepository.findByItemCalculadoraId(item.getId());
        BigDecimal totalMateriales = materiales.stream()
                .map(MaterialCalculadora::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        item.setMateriales(totalMateriales);
        item.setSubtotalMateriales(totalMateriales);
        log.debug("  📦 Materiales recalculados: {} (desde {} registros)", totalMateriales, materiales.size());

        // 2. Recalcular mano de obra desde jornal_calculadora
        List<JornalCalculadora> jornales = jornalCalculadoraRepository.findByItemCalculadoraId(item.getId());
        BigDecimal totalJornales = jornales.stream()
                .map(JornalCalculadora::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        item.setSubtotalManoObra(totalJornales);
        log.debug("  👷 Jornales recalculados: {} (desde {} registros)", totalJornales, jornales.size());

        // 3. Recalcular gastos generales desde presupuesto_gasto_general
        List<PresupuestoGastoGeneral> gastos = gastoGeneralRepository.findByItemCalculadoraId(item.getId());
        BigDecimal totalGastos = gastos.stream()
                .map(PresupuestoGastoGeneral::getSubtotal)
                .filter(subtotal -> subtotal != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        item.setSubtotalGastosGenerales(totalGastos);
        log.debug("  💰 Gastos generales recalculados: {} (desde {} registros)", totalGastos, gastos.size());

        // 4. Recalcular total del item
        BigDecimal totalItem = totalMateriales.add(totalJornales).add(totalGastos);
        item.setTotal(totalItem);

        // 5. Guardar item actualizado
        itemCalculadoraRepository.save(item);
        
        log.info("✅ Item ID {} recalculado: Materiales={}, Jornales={}, Gastos={}, Total={}",
                item.getId(), totalMateriales, totalJornales, totalGastos, totalItem);
    }

    /**
     * Recalcula el total del item basado en sus subtotales consolidados.
     * CONSERVATIVO: Solo recalcula si hay subtotales válidos.
     */
    private void recalcularTotalItem(ItemCalculadoraPresupuesto item) {
        BigDecimal nuevoTotal = BigDecimal.ZERO;
        boolean haySubtotales = false;

        BigDecimal totalAnterior = item.getTotal() != null ? item.getTotal() : BigDecimal.ZERO;

        // Sumar subtotales que existan
        if (item.getSubtotalManoObra() != null) {
            nuevoTotal = nuevoTotal.add(item.getSubtotalManoObra());
            haySubtotales = true;
            log.debug("  ➕ Mano obra: {}", item.getSubtotalManoObra());
        }

        if (item.getSubtotalMateriales() != null) {
            nuevoTotal = nuevoTotal.add(item.getSubtotalMateriales());
            haySubtotales = true;
            log.debug("  ➕ Materiales: {}", item.getSubtotalMateriales());
        }

        if (item.getSubtotalGastosGenerales() != null) {
            nuevoTotal = nuevoTotal.add(item.getSubtotalGastosGenerales());
            haySubtotales = true;
            log.debug("  ➕ Gastos generales: {}", item.getSubtotalGastosGenerales());
        }

        // if (item.getSubtotalOtrosCostos() != null) {
        //     nuevoTotal = nuevoTotal.add(item.getSubtotalOtrosCostos());
        //     haySubtotales = true;
        //     log.debug("  ➕ Otros costos: {}", item.getSubtotalOtrosCostos());
        // }

        // if (item.getHonorarios() != null) {
        //     nuevoTotal = nuevoTotal.add(item.getHonorarios());
        //     haySubtotales = true;
        //     log.debug("  ➕ Honorarios: {}", item.getHonorarios());
        // }

        // Solo actualizar el total si hay subtotales válidos
        if (haySubtotales) {
            item.setTotal(nuevoTotal);

            log.debug("🧮 Item {}: Total recalculado {} → {}",
                    item.getTipoProfesional(), totalAnterior, nuevoTotal);
        } else {
            log.debug("⚠️ Item {}: No hay subtotales para recalcular", item.getTipoProfesional());
        }
    }

    /**
     * Método auxiliar para copiar items de calculadora de un presupuesto base
     * IMPORTANTE: Maneja valores null correctamente - no los convierte a 0
     */
    private void copiarItemsCalculadoraDePresupuestoBase(Long presupuestoId, Long presupuestoBaseId) {
        try {
            log.info("🔄 Iniciando copia de items calculadora desde presupuesto base ID: {} hacia presupuesto ID: {}",
                    presupuestoBaseId, presupuestoId);

            // Obtener items del presupuesto base
            List<ItemCalculadoraPresupuesto> itemsBase = itemCalculadoraRepository.findByPresupuestoNoClienteId(presupuestoBaseId);

            if (itemsBase.isEmpty()) {
                log.warn("⚠️ No se encontraron items calculadora en el presupuesto base ID: {}", presupuestoBaseId);
                return;
            }

            log.info("📊 Encontrados {} items calculadora para copiar", itemsBase.size());

            // Obtener el presupuesto destino
            PresupuestoNoCliente presupuestoDestino = repository.findById(presupuestoId)
                    .orElseThrow(() -> new RuntimeException("Presupuesto destino no encontrado: " + presupuestoId));

            for (ItemCalculadoraPresupuesto itemBase : itemsBase) {
                // Crear nuevo item copiando TODOS los valores consolidados
                ItemCalculadoraPresupuesto nuevoItem = new ItemCalculadoraPresupuesto();

                // Asignar al nuevo presupuesto  
                nuevoItem.setPresupuestoNoCliente(presupuestoDestino);
                nuevoItem.setEmpresa(presupuestoDestino.getEmpresa());

                // ========== COPIAR DATOS BÁSICOS ==========
                nuevoItem.setTipoProfesional(itemBase.getTipoProfesional());
                nuevoItem.setDescripcion(itemBase.getDescripcion());
                nuevoItem.setObservaciones(itemBase.getObservaciones());
                nuevoItem.setCantidadJornales(itemBase.getCantidadJornales());
                nuevoItem.setImporteJornal(itemBase.getImporteJornal());
                nuevoItem.setMateriales(itemBase.getMateriales());
                nuevoItem.setTotalManual(itemBase.getTotalManual());
                nuevoItem.setEsModoManual(itemBase.getEsModoManual());
                nuevoItem.setIncluirEnCalculoDias(itemBase.getIncluirEnCalculoDias());
                nuevoItem.setTrabajaEnParalelo(itemBase.getTrabajaEnParalelo());
                nuevoItem.setEsGastoGeneral(itemBase.getEsGastoGeneral());
                nuevoItem.setDescripcionGastosGenerales(itemBase.getDescripcionGastosGenerales());
                nuevoItem.setObservacionesGastosGenerales(itemBase.getObservacionesGastosGenerales());

                // ========== COPIAR CAMPOS DE DESCRIPCIÓN/OBSERVACIONES POR CATEGORÍA ==========
                nuevoItem.setDescripcionProfesionales(itemBase.getDescripcionProfesionales());
                nuevoItem.setObservacionesProfesionales(itemBase.getObservacionesProfesionales());
                nuevoItem.setDescripcionMateriales(itemBase.getDescripcionMateriales());
                nuevoItem.setObservacionesMateriales(itemBase.getObservacionesMateriales());

                // ========== COPIAR SUBTOTALES CONSOLIDADOS (CRÍTICO) ==========
                // Valores consolidados de subtotales
                nuevoItem.setSubtotalManoObra(itemBase.getSubtotalManoObra()); // VALORES CONSOLIDADOS
                nuevoItem.setSubtotalMateriales(itemBase.getSubtotalMateriales()); // VALORES CONSOLIDADOS  
                nuevoItem.setSubtotalGastosGenerales(itemBase.getSubtotalGastosGenerales()); // VALORES CONSOLIDADOS
                nuevoItem.setTotal(itemBase.getTotal()); // TOTAL CONSOLIDADO

                log.debug("📋 Copiando item {} con valores CONSOLIDADOS:", itemBase.getTipoProfesional());
                log.debug("   💼 Subtotal mano obra: {}", itemBase.getSubtotalManoObra());
                log.debug("   🧱 Subtotal materiales: {}", itemBase.getSubtotalMateriales());
                log.debug("   📦 Subtotal gastos generales: {}", itemBase.getSubtotalGastosGenerales());
                log.debug("   🏆 TOTAL: {}", itemBase.getTotal());

                // Guardar el nuevo item
                ItemCalculadoraPresupuesto itemGuardado = itemCalculadoraRepository.save(nuevoItem);

                // ========== COPIAR PROFESIONALES INDIVIDUALES ==========
                if (itemBase.getProfesionales() != null && !itemBase.getProfesionales().isEmpty()) {
                    log.debug("👥 Copiando {} profesionales para item {}",
                            itemBase.getProfesionales().size(), itemBase.getTipoProfesional());

                    for (ProfesionalCalculadora profBase : itemBase.getProfesionales()) {
                        ProfesionalCalculadora nuevoProf = new ProfesionalCalculadora();
                        nuevoProf.setItemCalculadora(itemGuardado);
                        nuevoProf.setEmpresa(presupuestoDestino.getEmpresa());
                        nuevoProf.setFrontendId(profBase.getFrontendId());
                        nuevoProf.setTipo(profBase.getTipo());
                        nuevoProf.setNombre(profBase.getNombre());
                        nuevoProf.setDescripcion(profBase.getDescripcion());
                        nuevoProf.setObservaciones(profBase.getObservaciones());
                        nuevoProf.setTelefono(profBase.getTelefono());
                        nuevoProf.setUnidad(profBase.getUnidad());
                        nuevoProf.setCantidadJornales(profBase.getCantidadJornales());
                        nuevoProf.setImporteJornal(profBase.getImporteJornal()); // VALOR CONSOLIDADO
                        nuevoProf.setSubtotal(profBase.getSubtotal()); // VALOR CONSOLIDADO
                        nuevoProf.setSinCantidad(profBase.getSinCantidad());
                        nuevoProf.setSinImporte(profBase.getSinImporte());

                        profesionalCalculadoraRepository.save(nuevoProf);
                    }
                }

                // ========== COPIAR MATERIALES INDIVIDUALES ==========
                if (itemBase.getMaterialesLista() != null && !itemBase.getMaterialesLista().isEmpty()) {
                    log.debug("🧱 Copiando {} materiales para item {}",
                            itemBase.getMaterialesLista().size(), itemBase.getTipoProfesional());

                    for (MaterialCalculadora matBase : itemBase.getMaterialesLista()) {
                        MaterialCalculadora nuevoMat = new MaterialCalculadora();
                        nuevoMat.setItemCalculadora(itemGuardado);
                        nuevoMat.setEmpresa(presupuestoDestino.getEmpresa());
                        nuevoMat.setFrontendId(matBase.getFrontendId());
                        nuevoMat.setNombre(matBase.getNombre());
                        nuevoMat.setDescripcion(matBase.getDescripcion());
                        nuevoMat.setObservaciones(matBase.getObservaciones());
                        nuevoMat.setUnidad(matBase.getUnidad());
                        nuevoMat.setCantidad(matBase.getCantidad());
                        nuevoMat.setPrecio(matBase.getPrecio()); // VALOR CONSOLIDADO
                        nuevoMat.setSubtotal(matBase.getSubtotal()); // VALOR CONSOLIDADO

                        materialCalculadoraRepository.save(nuevoMat);
                    }
                }

                // ========== COPIAR GASTOS GENERALES ==========
                if (Boolean.TRUE.equals(itemBase.getEsGastoGeneral()) &&
                        itemBase.getGastosGenerales() != null && !itemBase.getGastosGenerales().isEmpty()) {

                    log.debug("📋 Copiando {} gastos generales para item {}",
                            itemBase.getGastosGenerales().size(), itemBase.getTipoProfesional());

                    for (PresupuestoGastoGeneral gastoBase : itemBase.getGastosGenerales()) {
                        PresupuestoGastoGeneral nuevoGasto = new PresupuestoGastoGeneral();
                        nuevoGasto.setItemCalculadora(itemGuardado);
                        nuevoGasto.setEmpresa(presupuestoDestino.getEmpresa());
                        nuevoGasto.setDescripcion(gastoBase.getDescripcion());
                        nuevoGasto.setObservaciones(gastoBase.getObservaciones());
                        nuevoGasto.setCantidad(gastoBase.getCantidad());
                        nuevoGasto.setPrecioUnitario(gastoBase.getPrecioUnitario()); // VALOR CONSOLIDADO
                        nuevoGasto.setSubtotal(gastoBase.getSubtotal()); // VALOR CONSOLIDADO
                        nuevoGasto.setSinCantidad(gastoBase.getSinCantidad());
                        nuevoGasto.setSinPrecio(gastoBase.getSinPrecio());
                        nuevoGasto.setOrden(gastoBase.getOrden());

                        gastoGeneralRepository.save(nuevoGasto);
                    }
                }

                log.info("✅ Item {} copiado exitosamente con VALORES CONSOLIDADOS: Total = {}",
                        itemGuardado.getTipoProfesional(), itemGuardado.getTotal());
            }

            log.info("✅ Copia de items calculadora completada exitosamente. {} items copiados con valores ACTUALES", itemsBase.size());

        } catch (Exception e) {
            log.error("❌ Error al copiar items calculadora: {}", e.getMessage(), e);
            throw new RuntimeException("Error al copiar items calculadora: " + e.getMessage(), e);
        }
    }

    /**
     * Duplica un presupuesto existente creando una nueva versión en estado BORRADOR
     *
     * @param id        ID del presupuesto a duplicar
     * @param empresaId ID de la empresa (para validación)
     * @return El nuevo presupuesto duplicado con estado BORRADOR
     */
    @Transactional
    public PresupuestoNoCliente duplicarPresupuesto(Long id, Long empresaId) {
        log.info("🔄 Iniciando duplicación de presupuesto ID: {} para empresa: {}", id, empresaId);

        // 1. Obtener presupuesto original y validar
        PresupuestoNoCliente original = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + id));

        if (!original.getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("El presupuesto no pertenece a la empresa indicada");
        }

        // 2. Crear nuevo presupuesto (copia)
        PresupuestoNoCliente duplicado = new PresupuestoNoCliente();

        // Copiar empresa
        duplicado.setEmpresa(original.getEmpresa());

        // Mantener mismo número de presupuesto, incrementar versión
        duplicado.setNumeroPresupuesto(original.getNumeroPresupuesto());
        duplicado.setNumeroVersion(original.getNumeroVersion() + 1);

        // Estado siempre BORRADOR
        duplicado.setEstado(PresupuestoEstado.BORRADOR);

        // Copiar información básica
        duplicado.setDescripcion(original.getDescripcion());
        duplicado.setObservaciones(original.getObservaciones());

        // Copiar información del solicitante
        duplicado.setNombreSolicitante(original.getNombreSolicitante());
        duplicado.setTelefono(original.getTelefono());
        duplicado.setMail(original.getMail());
        duplicado.setDireccionParticular(original.getDireccionParticular());

        // Copiar dirección de obra (6 campos)
        duplicado.setDireccionObraBarrio(original.getDireccionObraBarrio());
        duplicado.setDireccionObraCalle(original.getDireccionObraCalle());
        duplicado.setDireccionObraAltura(original.getDireccionObraAltura());
        duplicado.setDireccionObraTorre(original.getDireccionObraTorre());
        duplicado.setDireccionObraPiso(original.getDireccionObraPiso());
        duplicado.setDireccionObraDepartamento(original.getDireccionObraDepartamento());

        // Copiar fechas y tiempos
        duplicado.setFechaProbableInicio(original.getFechaProbableInicio());
        duplicado.setTiempoEstimadoTerminacion(original.getTiempoEstimadoTerminacion());
        duplicado.setVencimiento(original.getVencimiento());

        // Copiar montos y totales
        duplicado.setTotalProfesionales(original.getTotalProfesionales());
        duplicado.setTotalMateriales(original.getTotalMateriales());
        duplicado.setTotalGeneral(original.getTotalGeneral());
        duplicado.setTotalPresupuestoConHonorarios(original.getTotalPresupuestoConHonorarios());
        duplicado.setTotalConDescuentos(original.getTotalConDescuentos());
        duplicado.setTotalMayoresCostosPorRubro(original.getTotalMayoresCostosPorRubro());
        duplicado.setTotalDescuentosPorRubro(original.getTotalDescuentosPorRubro());

        // Copiar tipo de presupuesto
        duplicado.setTipoPresupuesto(original.getTipoPresupuesto());

        // NO copiar obra (el duplicado no está asociado a ninguna obra)
        duplicado.setObra(null);

        // 3. Guardar el presupuesto duplicado
        PresupuestoNoCliente presupuestoGuardado = repository.save(duplicado);
        log.info("✅ Presupuesto duplicado creado con ID: {}, versión: {}",
                presupuestoGuardado.getId(), presupuestoGuardado.getNumeroVersion());

        // 4. Copiar items calculadora (profesionales, materiales, gastos generales)
        if (original.getItemsCalculadora() != null && !original.getItemsCalculadora().isEmpty()) {
            log.info("📋 Copiando {} items de calculadora", original.getItemsCalculadora().size());
            copiarItemsCalculadoraDePresupuestoBase(presupuestoGuardado.getId(), original.getId());
        }

        // 5. Recalcular totales
        presupuestoGuardado.calcularCamposCalculados();
        presupuestoGuardado = repository.save(presupuestoGuardado);

        log.info("✅ Duplicación completada. Nuevo presupuesto ID: {} (versión {})",
                presupuestoGuardado.getId(), presupuestoGuardado.getNumeroVersion());

        return presupuestoGuardado;
    }

    /**
     * Busca un cliente existente o crea uno nuevo basado en los datos del presupuesto.
     * <p>
     * Lógica de búsqueda/creación:
     * 1. Si tiene nombreSolicitante + (telefono o mail): busca por coincidencia exacta o parcial
     * 2. Si no hay datos del solicitante: usa nombreObra como identificador del cliente
     * 3. Si no encuentra coincidencias: crea un nuevo cliente
     *
     * @param presupuesto PresupuestoNoCliente con datos del solicitante
     * @param empresa     Empresa a la que pertenece
     * @return Cliente encontrado o recién creado
     */
    private Cliente buscarOCrearClienteDesdePresupuesto(PresupuestoNoCliente presupuesto, Empresa empresa) {
        String nombreSolicitante = presupuesto.getNombreSolicitante();
        String telefono = presupuesto.getTelefono();
        String mail = presupuesto.getMail();
        String nombreObra = presupuesto.getNombreObra();

        Cliente cliente = null;

        // CASO 1: Buscar por nombreSolicitante + telefono
        if (nombreSolicitante != null && !nombreSolicitante.trim().isEmpty() &&
                telefono != null && !telefono.trim().isEmpty()) {

            List<Cliente> candidatos = clienteRepository.findByNombreSolicitanteAndTelefono(
                    nombreSolicitante.trim(), telefono.trim());

            if (!candidatos.isEmpty()) {
                cliente = candidatos.get(0);
                log.info("📌 Cliente encontrado por nombreSolicitante + telefono: {} (ID: {})",
                        cliente.getNombreSolicitante(), cliente.getId());
            }
        }

        // CASO 2: Buscar por nombreSolicitante + mail
        if (cliente == null && nombreSolicitante != null && !nombreSolicitante.trim().isEmpty() &&
                mail != null && !mail.trim().isEmpty()) {

            List<Cliente> candidatos = clienteRepository.findByNombreSolicitanteAndEmail(
                    nombreSolicitante.trim(), mail.trim());

            if (!candidatos.isEmpty()) {
                cliente = candidatos.get(0);
                log.info("📌 Cliente encontrado por nombreSolicitante + email: {} (ID: {})",
                        cliente.getNombreSolicitante(), cliente.getId());
            }
        }

        // CASO 3: Buscar solo por telefono (si no hay nombreSolicitante)
        if (cliente == null && telefono != null && !telefono.trim().isEmpty()) {
            List<Cliente> candidatos = clienteRepository.findByTelefono(telefono.trim());

            if (!candidatos.isEmpty()) {
                cliente = candidatos.get(0);
                log.info("📌 Cliente encontrado por telefono: {} (ID: {})", telefono, cliente.getId());
            }
        }

        // CASO 4: Buscar solo por email
        if (cliente == null && mail != null && !mail.trim().isEmpty()) {
            Optional<Cliente> clienteOpt = clienteRepository.findByEmail(mail.trim());

            if (clienteOpt.isPresent()) {
                cliente = clienteOpt.get();
                log.info("📌 Cliente encontrado por email: {} (ID: {})", mail, cliente.getId());
            }
        }

        // CASO 5: Si no se encontró, crear nuevo cliente
        if (cliente == null) {
            cliente = new Cliente();

            // Prioridad: nombreSolicitante > nombreObra > "Cliente sin identificar"
            if (nombreSolicitante != null && !nombreSolicitante.trim().isEmpty()) {
                cliente.setNombreSolicitante(nombreSolicitante.trim());
                cliente.setNombre(null); // No tiene razón social formal aún
            } else if (nombreObra != null && !nombreObra.trim().isEmpty()) {
                cliente.setNombre(nombreObra.trim());
                cliente.setNombreSolicitante(null);
            } else {
                cliente.setNombre("Cliente sin identificar");
                cliente.setNombreSolicitante(null);
            }

            cliente.setTelefono(telefono != null ? telefono.trim() : null);
            cliente.setEmail(mail != null ? mail.trim() : null);
            cliente.setDireccion(presupuesto.getDireccionParticular());
            cliente.getEmpresas().add(empresa);

            cliente = clienteRepository.save(cliente);
            log.info("✅ Nuevo cliente creado: {} (ID: {})",
                    cliente.getNombre() != null ? cliente.getNombre() : cliente.getNombreSolicitante(),
                    cliente.getId());
        } else {
            // Verificar que el cliente esté asociado a esta empresa
            if (!cliente.getEmpresas().contains(empresa)) {
                cliente.getEmpresas().add(empresa);
                cliente = clienteRepository.save(cliente);
                log.info("🔗 Cliente asociado a empresa ID: {}", empresa.getId());
            }
        }


        return cliente;
    }

    /**
     * Obtiene todos los materiales de un presupuesto específico extrayendo de itemsCalculadora
     *
     * @param presupuestoId ID del presupuesto
     * @param empresaId     ID de la empresa (validación multi-tenant)
     * @return Lista de materiales del presupuesto
     */
    @Transactional(readOnly = true)
    public List<com.rodrigo.construccion.dto.response.PresupuestoMaterialResponseDTO> obtenerMaterialesPresupuesto(Long presupuestoId, Long empresaId) {
        log.info("🔍 Extrayendo materiales de itemsCalculadora del presupuesto {} para empresa {}", presupuestoId, empresaId);

        // Validar que el presupuesto existe y pertenece a la empresa
        PresupuestoNoCliente presupuesto = repository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + presupuestoId));

        if (presupuesto.getEmpresa() == null || !presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("El presupuesto no pertenece a la empresa especificada");
        }

        List<com.rodrigo.construccion.dto.response.PresupuestoMaterialResponseDTO> materiales = new ArrayList<>();
        long idCounter = 1;

        // Extraer materiales de itemsCalculadora (JSON)
        if (presupuesto.getItemsCalculadora() != null) {
            for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
                // Forzar carga de materialesLista
                if (item.getMaterialesLista() != null) {
                    item.getMaterialesLista().size();

                    for (MaterialCalculadora mat : item.getMaterialesLista()) {
                        com.rodrigo.construccion.dto.response.PresupuestoMaterialResponseDTO dto =
                                new com.rodrigo.construccion.dto.response.PresupuestoMaterialResponseDTO();

                        // ID único (usar el del material si existe, sino generar)
                        dto.setId(mat.getId() != null ? mat.getId() : idCounter++);
                        dto.setNombreMaterial(mat.getNombre());
                        dto.setCantidad(mat.getCantidad() != null ? mat.getCantidad() : BigDecimal.ZERO);
                        dto.setUnidadMedida(mat.getUnidad() != null ? mat.getUnidad() : "unidad");
                        dto.setPrecioUnitario(mat.getPrecio() != null ? mat.getPrecio() : BigDecimal.ZERO);
                        dto.setSubtotal(mat.getSubtotal() != null ? mat.getSubtotal() : BigDecimal.ZERO);
                        dto.setCategoria(item.getTipoProfesional()); // Rubro como categoría
                        dto.setDescripcion(mat.getDescripcion() != null ? mat.getDescripcion() : "");
                        dto.setObservaciones(mat.getObservaciones() != null ? mat.getObservaciones() : "");

                        // VERSIÓN SIMPLIFICADA: Agregar información básica de stock
                        try {
                            Long materialId = mat.getFrontendId();

                            if (materialId != null && materialId > 0) {
                                // Simulamos cantidades por ahora para no crashear
                                switch (materialId.intValue()) {
                                    case 1: // bolsas de cal
                                        dto.setCantidadDisponible(BigDecimal.valueOf(80.0));
                                        dto.setEstadoStock("DISPONIBLE");
                                        break;
                                    case 2: // bolsas de cemento  
                                        dto.setCantidadDisponible(BigDecimal.valueOf(150.0));
                                        dto.setEstadoStock("DISPONIBLE");
                                        break;
                                    case 3: // Latas de Latex
                                        dto.setCantidadDisponible(BigDecimal.valueOf(25.0));
                                        dto.setEstadoStock("STOCK_BAJO");
                                        break;
                                    default:
                                        dto.setCantidadDisponible(BigDecimal.valueOf(50.0));
                                        dto.setEstadoStock("DISPONIBLE");
                                }
                            } else {
                                dto.setCantidadDisponible(BigDecimal.valueOf(0.0));
                                dto.setEstadoStock("SIN_STOCK");
                            }
                        } catch (Exception e) {
                            // En caso de cualquier error, valores seguros
                            dto.setCantidadDisponible(BigDecimal.valueOf(0.0));
                            dto.setEstadoStock("SIN_STOCK");
                        }

                        materiales.add(dto);
                    }
                }
            }
        }

        log.info("✅ Extraídos {} materiales de itemsCalculadora para presupuesto {}", materiales.size(), presupuestoId);
        return materiales;
    }

    /**
     * NUEVO: Obtiene materiales del presupuesto con información de stock disponible
     *
     * @param presupuestoId ID del presupuesto
     * @param empresaId     ID de la empresa
     * @param obraId        ID de la obra (para determinar ubicación)
     * @return Lista de materiales con información de stock
     */
    @Transactional(readOnly = true)
    public List<com.rodrigo.construccion.dto.response.MaterialConStockResponseDTO> obtenerMaterialesConStock(
            Long presupuestoId, Long empresaId, Long obraId) {

        log.info("🔍 Obteniendo materiales con stock para presupuesto {} obra {} empresa {}",
                presupuestoId, obraId, empresaId);

        // Validar que el presupuesto existe y pertenece a la empresa
        PresupuestoNoCliente presupuesto = repository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + presupuestoId));

        if (presupuesto.getEmpresa() == null || !presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("El presupuesto no pertenece a la empresa especificada");
        }

        List<com.rodrigo.construccion.dto.response.MaterialConStockResponseDTO> materialesConStock = new ArrayList<>();

        // Obtener ubicación por defecto (se puede mejorar)
        String ubicacion = obraId != null ? "OBRA_" + obraId : "ALMACEN_GENERAL";

        // Extraer materiales de itemsCalculadora
        if (presupuesto.getItemsCalculadora() != null) {
            for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
                if (item.getMaterialesLista() != null) {
                    item.getMaterialesLista().size();

                    for (MaterialCalculadora mat : item.getMaterialesLista()) {
                        com.rodrigo.construccion.dto.response.MaterialConStockResponseDTO dto =
                                new com.rodrigo.construccion.dto.response.MaterialConStockResponseDTO();

                        // Información básica del material
                        dto.setId(mat.getId() != null ? mat.getId() : 0L);
                        dto.setNombreMaterial(mat.getNombre());
                        dto.setUnidadMedida(mat.getUnidad() != null ? mat.getUnidad() : "unidad");
                        dto.setPrecioUnitario(mat.getPrecio() != null ? mat.getPrecio() : BigDecimal.ZERO);
                        dto.setCategoria(item.getTipoProfesional());
                        dto.setDescripcion(mat.getDescripcion() != null ? mat.getDescripcion() : "");
                        dto.setUbicacion(ubicacion);

                        // Información de stock real
                        String ubicacionStock = obraId != null ? "OBRA_" + obraId : "ALMACEN_GENERAL";
                        Double cantidadDisponible = stockMaterialService.obtenerCantidadDisponible(
                                mat.getFrontendId(), empresaId, ubicacionStock);
                        Double cantidadAsignada = stockMaterialService.obtenerCantidadAsignada(
                                mat.getId(), empresaId);

                        dto.setCantidadDisponible(BigDecimal.valueOf(cantidadDisponible));
                        dto.setCantidadAsignada(BigDecimal.valueOf(cantidadAsignada));
                        dto.setCantidadRestante(BigDecimal.valueOf(cantidadDisponible - cantidadAsignada));

                        // Estado del stock
                        if (cantidadDisponible <= 0) {
                            dto.setEstado("AGOTADO");
                        } else if (cantidadDisponible <= 10) {
                            dto.setEstado("STOCK_BAJO");
                        } else {
                            dto.setEstado("DISPONIBLE");
                        }

                        materialesConStock.add(dto);
                    }
                }
            }
        }

        log.info("✅ Obtenidos {} materiales con stock para presupuesto {}", materialesConStock.size(), presupuestoId);
        return materialesConStock;
    }

    /**
     * Obtiene todos los otros costos (gastos generales) de un presupuesto extrayendo de itemsCalculadora
     *
     * @param presupuestoId ID del presupuesto
     * @param empresaId     ID de la empresa (validación multi-tenant)
     * @return Lista de otros costos del presupuesto
     */
    @Transactional(readOnly = true)
    public List<com.rodrigo.construccion.dto.response.PresupuestoOtroCostoResponseDTO> obtenerOtrosCostosPresupuesto(Long presupuestoId, Long empresaId) {
        log.info("🔍 Extrayendo gastos generales de itemsCalculadora del presupuesto {} para empresa {}", presupuestoId, empresaId);

        // Validar que el presupuesto existe y pertenece a la empresa
        PresupuestoNoCliente presupuesto = repository.findById(presupuestoId)
                .orElseThrow(() -> new RuntimeException("Presupuesto no encontrado con ID: " + presupuestoId));

        if (presupuesto.getEmpresa() == null || !presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new RuntimeException("El presupuesto no pertenece a la empresa especificada");
        }

        List<com.rodrigo.construccion.dto.response.PresupuestoOtroCostoResponseDTO> otrosCostos = new ArrayList<>();
        long idCounter = 1;

        // Extraer gastos generales de itemsCalculadora
        if (presupuesto.getItemsCalculadora() != null) {
            for (ItemCalculadoraPresupuesto item : presupuesto.getItemsCalculadora()) {
                // Forzar carga de gastosGenerales
                if (item.getGastosGenerales() != null) {
                    item.getGastosGenerales().size();

                    for (PresupuestoGastoGeneral gasto : item.getGastosGenerales()) {
                        com.rodrigo.construccion.dto.response.PresupuestoOtroCostoResponseDTO dto =
                                new com.rodrigo.construccion.dto.response.PresupuestoOtroCostoResponseDTO();

                        dto.setId(gasto.getId() != null ? gasto.getId() : idCounter++);
                        dto.setCategoria(item.getTipoProfesional());
                        dto.setDescripcion(gasto.getDescripcion() != null ? gasto.getDescripcion() : "");
                        dto.setImporte(gasto.getSubtotal() != null ? gasto.getSubtotal() : BigDecimal.ZERO);
                        dto.setObservaciones(gasto.getObservaciones() != null ? gasto.getObservaciones() : "");

                        otrosCostos.add(dto);
                    }
                }
            }
        }

        log.info("✅ Extraídos {} gastos generales de itemsCalculadora para presupuesto {}", otrosCostos.size(), presupuestoId);
        return otrosCostos;
    }

    /**
     * Obtener gastos generales de un presupuesto con información de stock
     */
    public List<GastoGeneralConStockResponseDTO> obtenerGastosGeneralesPresupuesto(Long presupuestoId, Long empresaId) {
        log.info("🔍 Obteniendo gastos generales con stock para presupuesto: {} y empresa: {}", presupuestoId, empresaId);

        List<GastoGeneralConStockResponseDTO> gastosConStock = new ArrayList<>();

        try {
            // Buscar items calculadora del presupuesto
            List<ItemCalculadoraPresupuesto> items = itemCalculadoraRepository
                    .findByPresupuestoNoClienteId(presupuestoId);

            if (items.isEmpty()) {
                log.warn("⚠️ No se encontraron items para el presupuesto: {}", presupuestoId);
                return gastosConStock;
            }

            for (ItemCalculadoraPresupuesto item : items) {
                // Buscar gastos generales del item
                List<PresupuestoGastoGeneral> gastos = gastoGeneralRepository
                        .findByItemCalculadoraId(item.getId());

                for (PresupuestoGastoGeneral gasto : gastos) {
                    log.info("🔧 Procesando gasto: {} con frontend_id: {}", gasto.getDescripcion(), gasto.getFrontendId());

                    GastoGeneralConStockResponseDTO dto = new GastoGeneralConStockResponseDTO();
                    dto.setId(gasto.getId());
                    dto.setNombreGastoGeneral(gasto.getDescripcion());
                    dto.setDescripcion(gasto.getDescripcion());
                    dto.setUnidadMedida("unidad"); // Valor por defecto
                    dto.setCategoria("General");   // Valor por defecto
                    dto.setPrecioUnitario(gasto.getPrecioUnitario() != null ? gasto.getPrecioUnitario() : BigDecimal.ZERO);

                    // Obtener información de stock si existe frontend_id
                    if (gasto.getFrontendId() != null) {
                        try {
                            BigDecimal cantidadDisponible = stockGastoGeneralService
                                    .obtenerCantidadDisponible(gasto.getFrontendId(), empresaId);

                            BigDecimal cantidadAsignada = gasto.getCantidad() != null ? gasto.getCantidad() : BigDecimal.ZERO;

                            dto.setCantidadDisponible(cantidadDisponible);
                            dto.setCantidadAsignada(cantidadAsignada);

                            log.info("📊 Stock encontrado - Disponible: {}, Asignado: {}", cantidadDisponible, cantidadAsignada);
                        } catch (Exception e) {
                            log.warn("⚠️ Error al obtener stock para gasto frontend_id {}: {}", gasto.getFrontendId(), e.getMessage());
                            dto.setCantidadDisponible(BigDecimal.ZERO);
                            dto.setCantidadAsignada(BigDecimal.ZERO);
                        }
                    } else {
                        log.info("ℹ️ Gasto sin frontend_id, usando valores por defecto");
                        dto.setCantidadDisponible(BigDecimal.ZERO);
                        dto.setCantidadAsignada(gasto.getCantidad() != null ? gasto.getCantidad() : BigDecimal.ZERO);
                    }

                    dto.setObservaciones(gasto.getObservaciones());

                    gastosConStock.add(dto);
                }
            }

            log.info("✅ Procesados {} gastos generales con stock para presupuesto {}", gastosConStock.size(), presupuestoId);

        } catch (Exception e) {
            log.error("❌ Error al obtener gastos generales con stock: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener gastos generales: " + e.getMessage());
        }

        return gastosConStock;
    }

    // ============================================================================
    // SINCRONIZACIÓN AUTOMÁTICA COMPLETA ENTRE PRESUPUESTO Y OBRA
    // ============================================================================

    /**
     * Sincroniza TODOS los campos relevantes del presupuesto a la obra vinculada.
     * La obra debe actuar como un "espejo" del presupuesto en tiempo real.
     * <p>
     * Este método se invoca automáticamente en:
     * 1. Creación de obra (al aprobar presupuesto)
     * 2. Actualización de presupuesto (cuando cambia cualquier campo sincronizable)
     * 3. Vinculación posterior (cuando se asigna presupuesto a obra existente)
     *
     * @param presupuesto Presupuesto origen de los datos
     */
    private void sincronizarPresupuestoConObra(PresupuestoNoCliente presupuesto) {
        // Verificar si el presupuesto tiene obra vinculada
        if (presupuesto.getObra() == null) {
            log.debug("⚠️ Presupuesto {} no tiene obra vinculada, saltando sincronización",
                    presupuesto.getId());
            return;
        }

        // IMPORTANTE: NO sincronizar si es presupuesto trabajo extra (la obra asociada es la obra PADRE)
        if (Boolean.TRUE.equals(presupuesto.getEsPresupuestoTrabajoExtra())) {
            log.info("⚠️ Presupuesto trabajo extra {} - NO se sincroniza con obra padre {}",
                    presupuesto.getId(), presupuesto.getObra().getId());
            return;
        }

        // Buscar la obra
        Optional<Obra> obraOpt = obraRepository.findById(presupuesto.getObra().getId());

        if (obraOpt.isEmpty()) {
            log.warn("⚠️ Obra {} no encontrada para presupuesto {}",
                    presupuesto.getObra().getId(),
                    presupuesto.getId());
            return;
        }

        Obra obra = obraOpt.get();

        log.info("🔄 Sincronizando obra {} con presupuesto {}", obra.getId(), presupuesto.getId());

        // Sincronizar cada campo
        sincronizarPresupuestoAObra(presupuesto, obra);

        // Guardar cambios
        obraRepository.save(obra);

        log.info("✅ Obra {} sincronizada exitosamente", obra.getId());
    }

    /**
     * Aplica todos los campos del presupuesto a la obra.
     * Este método centraliza la lógica de mapeo completo.
     * <p>
     * Campos sincronizados:
     * - nombre (desde nombreObra)
     * - direccionObraCalle, direccionObraAltura, direccionObraPiso, direccionObraDepartamento,
     * direccionObraBarrio, direccionObraTorre
     * - fechaInicio (desde fechaProbableInicio)
     * - fechaFin (calculada desde fechaInicio + tiempoEstimadoTerminacion)
     * - presupuestoEstimado (desde totalGeneral)
     * - estado (lógica especial según estado del presupuesto)
     * - presupuestoNoClienteId (referencia al presupuesto)
     *
     * @param presupuesto Origen
     * @param obra        Destino
     */
    private void sincronizarPresupuestoAObra(PresupuestoNoCliente presupuesto, Obra obra) {
        // 1. Nombre de la obra
        obra.setNombre(presupuesto.getNombreObra());

        // 2. Sincronizar tipo de presupuesto (PRINCIPAL, TAREA_LEVE, TRABAJO_EXTRA)
        obra.setTipoPresupuesto(presupuesto.getTipoPresupuesto());
        log.info("🏷️ Tipo de presupuesto copiado a obra: {}", presupuesto.getTipoPresupuesto());

        // 3. Sincronizar es_obra_trabajo_extra AUTOMÁTICAMENTE desde tipo_presupuesto (evita inconsistencias)
        obra.setEsObraTrabajoExtra(com.rodrigo.construccion.enums.TipoPresupuesto.TRABAJO_EXTRA.equals(presupuesto.getTipoPresupuesto()));
        
        // Si es trabajo extra, guardar la referencia a la obra principal
        if (Boolean.TRUE.equals(presupuesto.getEsPresupuestoTrabajoExtra()) && presupuesto.getObra() != null) {
            obra.setObraOrigenId(presupuesto.getObra().getId());
            log.info("🔗 Trabajo Extra: Vinculando obra nueva a obra origen ID: {}", presupuesto.getObra().getId());
        }

        // 4. Dirección completa (7 campos)
        obra.setDireccionObraCalle(presupuesto.getDireccionObraCalle());
        obra.setDireccionObraAltura(presupuesto.getDireccionObraAltura());
        obra.setDireccionObraPiso(presupuesto.getDireccionObraPiso());
        obra.setDireccionObraDepartamento(presupuesto.getDireccionObraDepartamento());
        obra.setDireccionObraBarrio(presupuesto.getDireccionObraBarrio());
        obra.setDireccionObraTorre(presupuesto.getDireccionObraTorre());

        // 5. Fechas
        obra.setFechaInicio(presupuesto.getFechaProbableInicio());

        // 6. Calcular fecha fin (fechaInicio + tiempoEstimadoTerminacion días hábiles)
        if (presupuesto.getFechaProbableInicio() != null && presupuesto.getTiempoEstimadoTerminacion() != null) {
            LocalDate fechaFin = calcularFechaFin(
                    presupuesto.getFechaProbableInicio(),
                    presupuesto.getTiempoEstimadoTerminacion()
            );
            obra.setFechaFin(fechaFin);
        } else {
            obra.setFechaFin(null);
        }

        // 7. Presupuesto estimado
        BigDecimal montoTotal = null;

        // Intentar obtener de totalGeneral (Double)
        if (presupuesto.getTotalGeneral() != null) {
            montoTotal = BigDecimal.valueOf(presupuesto.getTotalGeneral());
        }
        // Si no, intentar de totalPresupuestoConHonorarios (BigDecimal)
        else if (presupuesto.getTotalPresupuestoConHonorarios() != null) {
            montoTotal = presupuesto.getTotalPresupuestoConHonorarios();
        }
        // Si no, intentar de totalPresupuesto (puede ser BigDecimal)
        else if (presupuesto.getTotalPresupuesto() != null) {
            // getTotalPresupuesto() puede devolver BigDecimal o necesitar conversión
            Object totalPresup = presupuesto.getTotalPresupuesto();
            if (totalPresup instanceof BigDecimal) {
                montoTotal = (BigDecimal) totalPresup;
            } else if (totalPresup instanceof Double) {
                montoTotal = BigDecimal.valueOf((Double) totalPresup);
            }
        }

        obra.setPresupuestoEstimado(montoTotal);

        // 8. Estado (lógica especial)
        sincronizarEstado(presupuesto, obra);

        // 9. Referencia al presupuesto
        obra.setPresupuestoNoClienteId(presupuesto.getId());

        log.debug("   - nombre: {}", obra.getNombre());
        log.debug("   - tipoPresupuesto: {}", obra.getTipoPresupuesto());
        log.debug("   - dirección: {} {}", obra.getDireccionObraCalle(), obra.getDireccionObraAltura());
        log.debug("   - fechaInicio: {}", obra.getFechaInicio());
        log.debug("   - fechaFin: {}", obra.getFechaFin());
        log.debug("   - presupuestoEstimado: {}", obra.getPresupuestoEstimado());
        log.debug("   - estado: {}", obra.getEstado());
    }

    /**
     * Calcula la fecha fin sumando días hábiles (excluyendo sábados y domingos).
     *
     * @param fechaInicio Fecha de inicio
     * @param diasHabiles Cantidad de días hábiles a sumar
     * @return Fecha fin calculada, o null si algún parámetro es nulo/inválido
     */
    private LocalDate calcularFechaFin(LocalDate fechaInicio, Integer diasHabiles) {
        if (fechaInicio == null || diasHabiles == null || diasHabiles <= 0) {
            return null;
        }

        LocalDate fecha = fechaInicio;
        int diasSumados = 0;

        while (diasSumados < diasHabiles) {
            fecha = fecha.plusDays(1);

            // Saltar sábados (6) y domingos (7)
            if (fecha.getDayOfWeek().getValue() != 6 && fecha.getDayOfWeek().getValue() != 7) {
                diasSumados++;
            }
        }

        return fecha;
    }

    /**
     * Sincroniza el estado de la obra según el estado del presupuesto.
     * <p>
     * MAPEO SEGÚN ENUMS DEL FRONTEND:
     * - BORRADOR/A_ENVIAR/MODIFICADO/ENVIADO → EN_PLANIFICACION
     * - OBRA_A_CONFIRMAR → EN_PLANIFICACION
     * - APROBADO → APROBADO o EN_EJECUCION (según fechaInicio vs hoy)
     * - EN_EJECUCION → EN_EJECUCION
     * - TERMINADO → TERMINADO (que se muestra como "Terminada" en frontend)
     *
     * @param presupuesto Origen
     * @param obra        Destino
     */
    private void sincronizarEstado(PresupuestoNoCliente presupuesto, Obra obra) {
        com.rodrigo.construccion.enums.PresupuestoEstado estadoPresupuesto = presupuesto.getEstado();

        if (estadoPresupuesto == null) {
            log.warn("⚠️ Estado de presupuesto es null. Usando BORRADOR");
            obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.BORRADOR);
            return;
        }

        // Lógica de mapeo según especificación del frontend
        switch (estadoPresupuesto) {
            case BORRADOR:
            case A_ENVIAR:
            case MODIFICADO:
            case ENVIADO:
            case OBRA_A_CONFIRMAR:
                // Todos estos estados iniciales mapean a BORRADOR
                obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.BORRADOR);
                break;

            case APROBADO:
                // LÓGICA ESPECIAL: Determinar según fecha de inicio
                LocalDate hoy = LocalDate.now();
                LocalDate fechaInicio = presupuesto.getFechaProbableInicio();

                if (fechaInicio != null && !fechaInicio.isAfter(hoy)) {
                    // Si la fecha de inicio ya pasó → EN_EJECUCION
                    obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.EN_EJECUCION);
                } else {
                    // Si la fecha es futura o null → APROBADO
                    obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.APROBADO);
                }
                break;

            case EN_EJECUCION:
                obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.EN_EJECUCION);
                break;

            case TERMINADO:
                // TERMINADO del backend → TERMINADO (se muestra como "Terminada" en frontend)
                obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.TERMINADO);
                break;

            default:
                // Fallback para cualquier estado no contemplado
                log.warn("⚠️ Estado de presupuesto no mapeado: {}. Usando BORRADOR", estadoPresupuesto);
                obra.setEstado(com.rodrigo.construccion.enums.EstadoObra.BORRADOR);
        }

        log.debug("🔄 Estado sincronizado: Presupuesto {} → Obra {}",
                estadoPresupuesto,
                obra.getEstadoEnum().getDisplayName()
        );
    }

    /**
     * Obtiene los honorarios del último presupuesto (versión más reciente) vinculado a las obras especificadas.
     * Retorna solo la información relevante de honorarios, optimizado para el frontend.
     *
     * @param obraIds   Lista de IDs de obras
     * @param empresaId ID de la empresa (multi-tenant)
     * @return Lista de DTOs con información de honorarios por obra
     */
    public List<com.rodrigo.construccion.dto.response.HonorariosPresupuestoObraDTO> obtenerHonorariosPorObras(
            List<Long> obraIds, Long empresaId) {

        log.info("🔍 Obteniendo honorarios de presupuestos para {} obras de empresaId {}",
                obraIds.size(), empresaId);

        List<com.rodrigo.construccion.dto.response.HonorariosPresupuestoObraDTO> resultado = new ArrayList<>();

        for (Long obraId : obraIds) {
            try {
                // Obtener la obra
                Obra obra = obraRepository.findById(obraId)
                        .orElseThrow(() -> new IllegalArgumentException("Obra no encontrada con ID: " + obraId));

                // Validar que la obra pertenece a la empresa
                if (!obra.getEmpresaId().equals(empresaId)) {
                    log.warn("⚠️ Obra {} no pertenece a empresa {}. Saltando...", obraId, empresaId);
                    continue;
                }

                // Obtener todos los presupuestos de la obra ordenados por versión descendente
                List<PresupuestoNoCliente> presupuestos = repository.findByObra_IdOrderByNumeroVersionDesc(obraId);

                if (presupuestos.isEmpty()) {
                    log.warn("⚠️ No se encontraron presupuestos para obra {}. Saltando...", obraId);
                    continue;
                }

                // El primero es el más reciente (orden descendente)
                PresupuestoNoCliente presupuestoMasReciente = presupuestos.get(0);

                log.info("✅ Presupuesto más reciente para obra {}: ID={}, versión={}, estado={}",
                        obraId,
                        presupuestoMasReciente.getId(),
                        presupuestoMasReciente.getNumeroVersion(),
                        presupuestoMasReciente.getEstado());

                // Construir el DTO con la información de honorarios
                com.rodrigo.construccion.dto.response.HonorariosPresupuestoObraDTO dto =
                        com.rodrigo.construccion.dto.response.HonorariosPresupuestoObraDTO.builder()
                                // Información de la obra
                                .obraId(obra.getId())
                                .obraNombre(obra.getNombre())
                                .obraDireccion(obra.getDireccionCompleta())

                                // Información del presupuesto
                                .presupuestoId(presupuestoMasReciente.getId())
                                .numeroPresupuesto(presupuestoMasReciente.getNumeroPresupuesto())
                                .numeroVersion(presupuestoMasReciente.getNumeroVersion())
                                .estadoPresupuesto(presupuestoMasReciente.getEstado() != null ?
                                        presupuestoMasReciente.getEstado().toString() : null)
                                .fechaEmision(presupuestoMasReciente.getFechaEmision())

                                // Totales principales
                                .totalPresupuesto(presupuestoMasReciente.getTotalPresupuesto())
                                .totalHonorarios(presupuestoMasReciente.getTotalHonorariosCalculado())
                                .totalFinal(presupuestoMasReciente.getTotalPresupuestoConHonorarios())

                                // Configuración general de honorarios
                                .honorariosAplicarATodos(presupuestoMasReciente.getHonorariosAplicarATodos())
                                .honorariosValorGeneral(presupuestoMasReciente.getHonorariosValorGeneral())
                                .honorariosTipoGeneral(presupuestoMasReciente.getHonorariosTipoGeneral())

                                // Honorarios de dirección
                                .honorarioDireccionValorFijo(presupuestoMasReciente.getHonorarioDireccionValorFijo())
                                .honorarioDireccionPorcentaje(presupuestoMasReciente.getHonorarioDireccionPorcentaje())
                                .honorarioDireccionImporte(presupuestoMasReciente.getHonorarioDireccionImporte())

                                // Honorarios por categoría - Profesionales
                                .honorariosProfesionalesActivo(presupuestoMasReciente.getHonorariosProfesionalesActivo())
                                .honorariosProfesionalesTipo(presupuestoMasReciente.getHonorariosProfesionalesTipo())
                                .honorariosProfesionalesValor(presupuestoMasReciente.getHonorariosProfesionalesValor())

                                // Honorarios por categoría - Materiales
                                .honorariosMaterialesActivo(presupuestoMasReciente.getHonorariosMaterialesActivo())
                                .honorariosMaterialesTipo(presupuestoMasReciente.getHonorariosMaterialesTipo())
                                .honorariosMaterialesValor(presupuestoMasReciente.getHonorariosMaterialesValor())

                                // Honorarios por categoría - Jornales
                                .honorariosJornalesActivo(presupuestoMasReciente.getHonorariosJornalesActivo())
                                .honorariosJornalesTipo(presupuestoMasReciente.getHonorariosJornalesTipo())
                                .honorariosJornalesValor(presupuestoMasReciente.getHonorariosJornalesValor())

                                // Honorarios por categoría - Otros Costos
                                .honorariosOtrosCostosActivo(presupuestoMasReciente.getHonorariosOtrosCostosActivo())
                                .honorariosOtrosCostosTipo(presupuestoMasReciente.getHonorariosOtrosCostosTipo())
                                .honorariosOtrosCostosValor(presupuestoMasReciente.getHonorariosOtrosCostosValor())

                                // Honorarios por categoría - Configuración Presupuesto
                                .honorariosConfiguracionPresupuestoActivo(presupuestoMasReciente.getHonorariosConfiguracionPresupuestoActivo())
                                .honorariosConfiguracionPresupuestoTipo(presupuestoMasReciente.getHonorariosConfiguracionPresupuestoTipo())
                                .honorariosConfiguracionPresupuestoValor(presupuestoMasReciente.getHonorariosConfiguracionPresupuestoValor())

                                .build();

                resultado.add(dto);

            } catch (Exception e) {
                log.error("❌ Error procesando obra {}: {}", obraId, e.getMessage(), e);
                // Continuar con la siguiente obra en caso de error
            }
        }

        log.info("✅ Se obtuvieron honorarios de {} obras de {} solicitadas", resultado.size(), obraIds.size());

        return resultado;
    }
    
    // ============= MÉTODOS DE VALIDACIÓN POR TIPO DE PRESUPUESTO =============
    
    /**
     * Valida los datos del presupuesto según su tipo
     */
    private void validarDatosPorTipoPresupuesto(PresupuestoNoClienteRequestDTO dto, TipoPresupuesto tipo) {
        log.info("🔍 Validando datos para tipo presupuesto: {}", tipo);
        
        switch (tipo) {
            case PRINCIPAL:
            case TRABAJO_DIARIO:
                // Validar campos descriptivos de la obra (sin restricción sobre obraId)
                validarCamposRequeridosParaObraNueva(dto);
                break;

            case TRABAJOS_SEMANALES:
                // Sin restricciones - el tipo es solo una clasificación
                log.info("ℹ️ Tipo {} sin restricciones de validación.", tipo);
                break;
                
            case TRABAJO_EXTRA:
                // Validar que tenga obra asociada
                if (dto.getIdObra() == null) {
                    throw new RuntimeException("ERROR: Presupuestos tipo " + tipo + " requieren obraId obligatorio.");
                }
                validarObraExistente(dto.getIdObra());
                break;
                
            case TAREA_LEVE:
                // NUEVA FUNCIONALIDAD: TAREA_LEVE puede vincularse a:
                // - Una Obra (idObra) → comportamiento actual
                // - Un TrabajoAdicional (trabajoAdicionalId) → nuevo
                // Son mutuamente excluyentes
                
                boolean tieneObra = dto.getIdObra() != null;
                boolean tieneTrabajoAdicional = dto.getTrabajoAdicionalId() != null;
                
                if (!tieneObra && !tieneTrabajoAdicional) {
                    throw new RuntimeException("ERROR: Presupuestos tipo TAREA_LEVE requieren obraId O trabajoAdicionalId.");
                }
                
                if (tieneObra && tieneTrabajoAdicional) {
                    throw new RuntimeException("ERROR: Presupuestos tipo TAREA_LEVE no pueden tener obraId Y trabajoAdicionalId simultáneamente (son mutuamente excluyentes).");
                }
                
                if (tieneObra) {
                    log.info("🔗 TAREA_LEVE vinculado a Obra ID: {}", dto.getIdObra());
                    validarObraExistente(dto.getIdObra());
                }
                
                if (tieneTrabajoAdicional) {
                    log.info("🔗 TAREA_LEVE vinculado a TrabajoAdicional ID: {}", dto.getTrabajoAdicionalId());
                    validarTrabajoAdicionalExistente(dto.getTrabajoAdicionalId());
                }
                break;
                
            default:
                throw new RuntimeException("ERROR: Tipo de presupuesto no soportado: " + tipo);
        }
    }
    
    /**
     * Valida campos requeridos para crear obra nueva
     */
    private void validarCamposRequeridosParaObraNueva(PresupuestoNoClienteRequestDTO dto) {
        if (dto.getNombreObra() == null || dto.getNombreObra().trim().isEmpty()) {
            throw new RuntimeException("ERROR: nombreObra es obligatorio para presupuestos que crean obra nueva.");
        }
        if (dto.getDireccionObraCalle() == null || dto.getDireccionObraCalle().trim().isEmpty()) {
            throw new RuntimeException("ERROR: direccionObraCalle es obligatorio para presupuestos que crean obra nueva.");
        }
        if (dto.getDireccionObraAltura() == null || dto.getDireccionObraAltura().trim().isEmpty()) {
            throw new RuntimeException("ERROR: direccionObraAltura es obligatorio para presupuestos que crean obra nueva.");
        }
    }
    
    /**
     * Valida que la obra existe
     */
    private void validarObraExistente(Long obraId) {
        if (!obraRepository.existsById(obraId)) {
            throw new RuntimeException("ERROR: Obra con ID " + obraId + " no existe.");
        }
    }
    
    /**
     * Valida que el trabajo adicional existe
     * (NUEVA FUNCIONALIDAD - para presupuestos TAREA_LEVE vinculados a TrabajoAdicional)
     */
    private void validarTrabajoAdicionalExistente(Long trabajoAdicionalId) {
        // Necesitamos inyectar TrabajoAdicionalRepository para esta validación
        // Por ahora validamos que no sea null, la validación de existencia se hará al mapear
        if (trabajoAdicionalId == null) {
            throw new RuntimeException("ERROR: trabajoAdicionalId no puede ser null.");
        }
        log.info("✅ TrabajoAdicional ID {} será validado al mapear la entidad", trabajoAdicionalId);
    }
    
    /**
     * Configura el presupuesto según su tipo
     */
    private void configurarPresupuestoPorTipo(PresupuestoNoCliente pnc, PresupuestoNoClienteRequestDTO dto, TipoPresupuesto tipo) {
        log.info("⚙️ Configurando presupuesto según tipo: {}", tipo);
        
        // Configurar estado según tipo
        if (dto.getEstado() != null) {
            // Si viene estado específico, validarlo
            PresupuestoEstado estadoSolicitado = PresupuestoEstado.fromString(dto.getEstado());
            pnc.setEstado(estadoSolicitado != null ? estadoSolicitado : tipo.getEstadoPorDefecto());
        } else {
            // Usar estado por defecto del tipo
            pnc.setEstado(tipo.getEstadoPorDefecto());
        }
        
        // Configurar esPresupuestoTrabajoExtra según tipo
        pnc.setEsPresupuestoTrabajoExtra(tipo.getEsPresupuestoTrabajoExtra());
        
        // Heredar cliente de obra padre si corresponde
        if (tipo.requiereObraExistente() && dto.getIdObra() != null) {
            configurarClienteDesdeObra(pnc, dto.getIdObra());
        }
        
        log.info("✅ Presupuesto configurado: estado={}, esTrabajoExtra={}", 
                pnc.getEstado(), pnc.getEsPresupuestoTrabajoExtra());
    }
    
    /**
     * Configura el cliente heredándolo de la obra padre
     */
    private void configurarClienteDesdeObra(PresupuestoNoCliente pnc, Long obraId) {
        log.info("👥 Heredando cliente de obra ID: {}", obraId);
        
        Optional<com.rodrigo.construccion.model.entity.Obra> obraOpt = obraRepository.findById(obraId);
        if (obraOpt.isPresent()) {
            com.rodrigo.construccion.model.entity.Obra obra = obraOpt.get();
            if (obra.getCliente() != null) {
                pnc.setCliente(obra.getCliente());
                log.info("✅ Cliente heredado: {} (ID: {})", obra.getCliente().getNombre(), obra.getCliente().getId());
            } else {
                log.warn("⚠️ Obra {} no tiene cliente asignado", obraId);
            }
        }
    }
    
    /**
     * Crea una obra automáticamente para presupuestos que la requieren inmediatamente
     */
    private void crearObraAutomaticamente(PresupuestoNoCliente presupuesto) {
        log.info("🏗️ Creando obra automáticamente para presupuesto ID: {} (tipo: {})",
                presupuesto.getId(), presupuesto.getTipoPresupuesto());

        try {
            com.rodrigo.construccion.model.entity.Obra nuevaObra = new com.rodrigo.construccion.model.entity.Obra();

            // Datos básicos de la obra
            nuevaObra.setNombre(presupuesto.getNombreObra());
            nuevaObra.setDireccionObraCalle(presupuesto.getDireccionObraCalle());
            nuevaObra.setDireccionObraAltura(presupuesto.getDireccionObraAltura());
            nuevaObra.setDireccionObraPiso(presupuesto.getDireccionObraPiso());
            nuevaObra.setDireccionObraDepartamento(presupuesto.getDireccionObraDepartamento());
            nuevaObra.setDireccionObraBarrio(presupuesto.getDireccionObraBarrio());
            nuevaObra.setDireccionObraTorre(presupuesto.getDireccionObraTorre());

            // Configuración según tipo de presupuesto
            nuevaObra.setPresupuestoOriginalId(presupuesto.getId());
            nuevaObra.setPresupuestoNoClienteId(presupuesto.getId()); // ⭐ Relación bidireccional
            nuevaObra.setTipoOrigen(com.rodrigo.construccion.enums.TipoOrigen.fromTipoPresupuesto(presupuesto.getTipoPresupuesto()));

            // ⚡ TAREA_LEVE: vincular a obra padre (puede ser Obra Principal o Sub-Obra de TRABAJO_EXTRA)
            // El presupuesto llega con obra = obra padre (puesta en crear() desde dto.getIdObra())
            // Guardamos ese vínculo en obraOrigenId ANTES de sobreescribir presupuesto.obra
            if (presupuesto.getTipoPresupuesto() == com.rodrigo.construccion.enums.TipoPresupuesto.TAREA_LEVE
                    && presupuesto.getObra() != null) {
                Long obraOrigenId = presupuesto.getObra().getId();
                nuevaObra.setObraOrigenId(obraOrigenId);
                log.info("🔗 TAREA_LEVE: nueva obra vinculada a obra padre ID: {}", obraOrigenId);
            }

            // Cliente y empresa
            nuevaObra.setCliente(presupuesto.getCliente());
            nuevaObra.setEmpresaId(presupuesto.getEmpresa().getId());
            nuevaObra.setFechaInicio(presupuesto.getFechaProbableInicio());

            // Estado: Usar método sincronizarEstado() para lógica consistente
            // (considera fecha de inicio y estados custom)
            sincronizarEstado(presupuesto, nuevaObra);
            log.info("🔄 Obra creada con estado: {} (sincronizado con presupuesto)", nuevaObra.getEstadoEnum());

            // ========== MAPEO COMPLETO DE IMPORTES FINANCIEROS ==========
            // Total presupuesto estimado
            if (presupuesto.getTotalPresupuesto() != null) {
                nuevaObra.setPresupuestoEstimado(presupuesto.getTotalPresupuesto());
            }

            // Importes base (convertir Double a BigDecimal)
            if (presupuesto.getTotalProfesionales() != null) {
                nuevaObra.setPresupuestoJornales(BigDecimal.valueOf(presupuesto.getTotalProfesionales()));
            }
            if (presupuesto.getTotalMateriales() != null) {
                nuevaObra.setPresupuestoMateriales(BigDecimal.valueOf(presupuesto.getTotalMateriales()));
            }
            
            // Honorarios globales (configuración de presupuesto)
            nuevaObra.setPresupuestoHonorarios(presupuesto.getHonorariosConfiguracionPresupuestoValor());
            nuevaObra.setTipoHonorarioPresupuesto(presupuesto.getHonorariosConfiguracionPresupuestoTipo());
            
            // Mayores costos globales (convertir Double a BigDecimal)
            if (presupuesto.getMayoresCostosConfiguracionPresupuestoValor() != null) {
                nuevaObra.setPresupuestoMayoresCostos(BigDecimal.valueOf(presupuesto.getMayoresCostosConfiguracionPresupuestoValor()));
            }
            nuevaObra.setTipoMayoresCostosPresupuesto(presupuesto.getMayoresCostosConfiguracionPresupuestoTipo());

            // Honorarios individuales por categoría
            nuevaObra.setHonorarioJornalesObra(presupuesto.getHonorariosJornalesValor());
            nuevaObra.setTipoHonorarioJornalesObra(presupuesto.getHonorariosJornalesTipo());
            nuevaObra.setHonorarioMaterialesObra(presupuesto.getHonorariosMaterialesValor());
            nuevaObra.setTipoHonorarioMaterialesObra(presupuesto.getHonorariosMaterialesTipo());
            // Gastos generales y mayores costos no tienen equivalente directo en presupuesto

            // Descuentos sobre importes base por categoría
            nuevaObra.setDescuentoJornalesObra(presupuesto.getDescuentosJornalesValor());
            nuevaObra.setTipoDescuentoJornalesObra(presupuesto.getDescuentosJornalesTipo());
            nuevaObra.setDescuentoMaterialesObra(presupuesto.getDescuentosMaterialesValor());
            nuevaObra.setTipoDescuentoMaterialesObra(presupuesto.getDescuentosMaterialesTipo());
            nuevaObra.setDescuentoMayoresCostosObra(presupuesto.getDescuentosMayoresCostosValor());
            nuevaObra.setTipoDescuentoMayoresCostosObra(presupuesto.getDescuentosMayoresCostosTipo());

            // Descuentos sobre honorarios por categoría
            nuevaObra.setDescuentoHonorarioJornalesObra(presupuesto.getDescuentosHonorariosJornalesValor());
            nuevaObra.setTipoDescuentoHonorarioJornalesObra(presupuesto.getDescuentosHonorariosJornalesTipo());
            nuevaObra.setDescuentoHonorarioMaterialesObra(presupuesto.getDescuentosHonorariosMaterialesValor());
            nuevaObra.setTipoDescuentoHonorarioMaterialesObra(presupuesto.getDescuentosHonorariosMaterialesTipo());
            nuevaObra.setDescuentoHonorarioGastosGeneralesObra(presupuesto.getDescuentosHonorariosGastosGeneralesValor());
            nuevaObra.setTipoDescuentoHonorarioGastosGeneralesObra(presupuesto.getDescuentosHonorariosGastosGeneralesTipo());
            nuevaObra.setDescuentoHonorarioMayoresCostosObra(presupuesto.getDescuentosHonorariosValor());
            nuevaObra.setTipoDescuentoHonorarioMayoresCostosObra(presupuesto.getDescuentosHonorariosTipo());

            log.info("💰 Importes mapeados: total={}, jornales={}, materiales={}, honorarios={}",
                    nuevaObra.getPresupuestoEstimado(),
                    nuevaObra.getPresupuestoJornales(),
                    nuevaObra.getPresupuestoMateriales(),
                    nuevaObra.getPresupuestoHonorarios());

            // Flags
            nuevaObra.setEsObraManual(false);
            nuevaObra.setEsObraTrabajoExtra(presupuesto.getEsPresupuestoTrabajoExtra());

            // Guardar obra
            nuevaObra = obraRepository.save(nuevaObra);
            log.info("✅ Obra creada con ID: {} (obraOrigenId: {})", nuevaObra.getId(), nuevaObra.getObraOrigenId());

            // Asociar obra al presupuesto (reemplaza la obra padre temporal para TAREA_LEVE)
            presupuesto.setObra(nuevaObra);
            repository.save(presupuesto);
            log.info("✅ Presupuesto {} vinculado a su nueva obra {}", presupuesto.getId(), nuevaObra.getId());

        } catch (Exception e) {
            log.error("❌ Error al crear obra automáticamente para presupuesto {}: {}", presupuesto.getId(), e.getMessage());
            throw new RuntimeException("Error al crear obra automáticamente: " + e.getMessage());
        }
    }

    /**
     * Obtiene profesionales con datos financieros calculados de un presupuesto.
     * Si el presupuesto está vinculado a una obra (global), busca los profesionales 
     * en asignaciones_profesional_obra con sus totales pagados, adelantos y saldos.
     * 
     * @param presupuestoId ID del presupuesto
     * @param empresaId ID de la empresa (multi-tenant)
     * @return Lista de profesionales con datos financieros completos
     * @throws ResourceNotFoundException si el presupuesto no existe o no está vinculado a obra
     */
    @Override
    @Transactional(readOnly = true)
    public List<com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO> obtenerProfesionalesFinancierosPorPresupuesto(
            Long presupuestoId, Long empresaId) {
        
        log.info("🔍 Obteniendo profesionales financieros para presupuestoId={}, empresaId={}", presupuestoId, empresaId);
        
        // 1. Obtener el presupuesto
        PresupuestoNoCliente presupuesto = repository.findById(presupuestoId)
            .orElseThrow(() -> new ResourceNotFoundException("Presupuesto no encontrado con ID: " + presupuestoId));
        
        // 2. Validar que pertenece a la empresa
        if (!presupuesto.getEmpresa().getId().equals(empresaId)) {
            throw new ResourceNotFoundException("El presupuesto no pertenece a la empresa especificada");
        }
        
        // 3. Validar que está vinculado a una obra
        if (presupuesto.getObra() == null) {
            log.warn("⚠️ Presupuesto {} no está vinculado a ninguna obra", presupuestoId);
            throw new ResourceNotFoundException(
                "El presupuesto no está vinculado a ninguna obra. " +
                "Los profesionales de presupuestos globales se encuentran en la configuración de la obra."
            );
        }
        
        Long obraId = presupuesto.getObra().getId();
        log.info("✅ Presupuesto vinculado a obra ID: {}", obraId);
        
        // 4. Obtener profesionales con datos financieros usando el servicio especializado
        List<com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO> profesionales = 
            profesionalObraService.obtenerProfesionalesConDatosFinancieros(empresaId, obraId);
        
        log.info("📊 Encontrados {} profesionales con datos financieros para presupuesto {}", 
            profesionales.size(), presupuestoId);
        
        return profesionales;
    }

    // =========================================================================
    // VALIDACIÓN DE COHERENCIA DE TOTALES
    // =========================================================================

    /**
     * Valida que los totales del presupuesto sean coherentes matemáticamente.
     * Evita que el frontend envíe cálculos incorrectos que se guarden sin validar.
     * 
     * @param presupuesto Presupuesto a validar
     * @throws IllegalArgumentException si hay inconsistencias en los totales
     */
    private void validarCoherenciaTotales(PresupuestoNoCliente presupuesto) {
        java.math.BigDecimal totalPresupuesto = presupuesto.getTotalPresupuesto();
        java.math.BigDecimal totalHonorarios = presupuesto.getTotalHonorariosCalculado();
        java.math.BigDecimal totalMayoresCostos = presupuesto.getTotalMayoresCostos();
        java.math.BigDecimal totalMayoresCostosPorRubro = presupuesto.getTotalMayoresCostosPorRubro();
        java.math.BigDecimal totalDescuentosPorRubro = presupuesto.getTotalDescuentosPorRubro();
        java.math.BigDecimal totalConHonorarios = presupuesto.getTotalPresupuestoConHonorarios();
        java.math.BigDecimal totalConDescuentos = presupuesto.getTotalConDescuentos();

        // Permitir valores null
        if (totalPresupuesto == null || totalHonorarios == null || totalConHonorarios == null) {
            log.warn("⚠️ Algunos totales son null, omitiendo validación: base={}, honorarios={}, total={}",
                    totalPresupuesto, totalHonorarios, totalConHonorarios);
            return;
        }

        // Validar que totalPresupuestoConHonorarios = totalPresupuesto + totalHonorarios + totalMayoresCostos + totalMayoresCostosPorRubro - totalDescuentosPorRubro
        java.math.BigDecimal sumaEsperada = totalPresupuesto.add(totalHonorarios);
        
        // Agregar mayores costos tradicionales (si existe)
        if (totalMayoresCostos != null) {
            sumaEsperada = sumaEsperada.add(totalMayoresCostos);
        }
        
        // Agregar mayores costos por rubro (si existe)
        if (totalMayoresCostosPorRubro != null) {
            sumaEsperada = sumaEsperada.add(totalMayoresCostosPorRubro);
        }
        
        // Restar descuentos por rubro (si existe)
        if (totalDescuentosPorRubro != null) {
            sumaEsperada = sumaEsperada.subtract(totalDescuentosPorRubro);
        }
        
        java.math.BigDecimal diferencia = sumaEsperada.subtract(totalConHonorarios).abs();
        java.math.BigDecimal tolerancia = new java.math.BigDecimal("10.00"); // Tolerancia de $10 para redondeos

        if (diferencia.compareTo(tolerancia) > 0) {
            String error = String.format(
                "❌ ERROR DE CÁLCULO: Total con honorarios no coincide. " +
                "Esperado: %s (Base: %s + Honorarios: %s + MayoresCostos: %s + MayoresCostosPorRubro: %s - DescuentosPorRubro: %s), pero se recibió: %s (diferencia: %s)",
                sumaEsperada, totalPresupuesto, totalHonorarios, totalMayoresCostos, totalMayoresCostosPorRubro, totalDescuentosPorRubro, totalConHonorarios, diferencia
            );
            log.error(error);
            throw new IllegalArgumentException(
                "Total con honorarios incorrecto. Esperado: $" + sumaEsperada + ", recibido: $" + totalConHonorarios
            );
        }

        // Validar que totalConDescuentos <= totalConHonorarios (si existe)
        if (totalConDescuentos != null && totalConDescuentos.compareTo(totalConHonorarios) > 0) {
            String error = String.format(
                "❌ ERROR DE CÁLCULO: Total con descuentos (%s) es mayor que total sin descuentos (%s)",
                totalConDescuentos, totalConHonorarios
            );
            log.error(error);
            throw new IllegalArgumentException(
                "Total con descuentos no puede ser mayor que total sin descuentos"
            );
        }

        log.info("✅ Validación de totales OK: Base={} + Honorarios={} + MayoresCostos={} + MayoresCostosPorRubro={} - DescuentosPorRubro={} = Total={} (TotalConDescuentos={})",
                totalPresupuesto, totalHonorarios, totalMayoresCostos, totalMayoresCostosPorRubro, totalDescuentosPorRubro, totalConHonorarios, totalConDescuentos);
    }

    // ========== MAPEO DE HONORARIOS POR RUBRO ==========

    /**
     * Mapea una lista de DTOs de honorarios por rubro a entidades
     */
    private Set<com.rodrigo.construccion.model.entity.HonorarioPorRubro> mapearHonorariosPorRubroDTO(
            List<com.rodrigo.construccion.dto.request.HonorarioPorRubroDTO> dtos,
            PresupuestoNoCliente presupuesto) {
        
        Set<com.rodrigo.construccion.model.entity.HonorarioPorRubro> honorarios = new java.util.HashSet<>();
        
        if (dtos == null || dtos.isEmpty()) {
            return honorarios;
        }

        for (com.rodrigo.construccion.dto.request.HonorarioPorRubroDTO dto : dtos) {
            com.rodrigo.construccion.model.entity.HonorarioPorRubro honorario = new com.rodrigo.construccion.model.entity.HonorarioPorRubro();
            
            honorario.setId(dto.getId());
            honorario.setPresupuestoNoCliente(presupuesto);
            honorario.setNombreRubro(dto.getNombreRubro());
            honorario.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
            honorario.setTipo(dto.getTipo() != null ? dto.getTipo() : "porcentaje");
            honorario.setValor(dto.getValor());

            // Profesionales
            honorario.setProfesionalesActivo(dto.getProfesionalesActivo() != null ? dto.getProfesionalesActivo() : true);
            honorario.setProfesionalesTipo(dto.getProfesionalesTipo() != null ? dto.getProfesionalesTipo() : "porcentaje");
            honorario.setProfesionalesValor(dto.getProfesionalesValor());

            // Materiales
            honorario.setMaterialesActivo(dto.getMaterialesActivo() != null ? dto.getMaterialesActivo() : true);
            honorario.setMaterialesTipo(dto.getMaterialesTipo() != null ? dto.getMaterialesTipo() : "porcentaje");
            honorario.setMaterialesValor(dto.getMaterialesValor());

            // Otros Costos
            honorario.setOtrosCostosActivo(dto.getOtrosCostosActivo() != null ? dto.getOtrosCostosActivo() : true);
            honorario.setOtrosCostosTipo(dto.getOtrosCostosTipo() != null ? dto.getOtrosCostosTipo() : "porcentaje");
            honorario.setOtrosCostosValor(dto.getOtrosCostosValor());

            honorarios.add(honorario);
        }

        return honorarios;
    }

    // ========== MAPEO DE MAYORES COSTOS POR RUBRO ==========

    /**
     * Mapea una lista de DTOs de mayores costos por rubro a entidades
     */
    private Set<com.rodrigo.construccion.model.entity.MayorCostoPorRubro> mapearMayoresCostosPorRubroDTO(
            List<com.rodrigo.construccion.dto.request.MayorCostoPorRubroDTO> dtos,
            PresupuestoNoCliente presupuesto) {
        
        Set<com.rodrigo.construccion.model.entity.MayorCostoPorRubro> mayoresCostos = new java.util.HashSet<>();
        
        if (dtos == null || dtos.isEmpty()) {
            return mayoresCostos;
        }

        for (com.rodrigo.construccion.dto.request.MayorCostoPorRubroDTO dto : dtos) {
            com.rodrigo.construccion.model.entity.MayorCostoPorRubro mayorCosto = new com.rodrigo.construccion.model.entity.MayorCostoPorRubro();
            
            mayorCosto.setId(dto.getId());
            mayorCosto.setPresupuestoNoCliente(presupuesto);
            mayorCosto.setNombreRubro(dto.getNombreRubro());
            mayorCosto.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
            mayorCosto.setTipo(dto.getTipo() != null ? dto.getTipo() : "porcentaje");
            mayorCosto.setValor(dto.getValor());

            // Profesionales
            mayorCosto.setProfesionalesActivo(dto.getProfesionalesActivo() != null ? dto.getProfesionalesActivo() : true);
            mayorCosto.setProfesionalesTipo(dto.getProfesionalesTipo() != null ? dto.getProfesionalesTipo() : "porcentaje");
            mayorCosto.setProfesionalesValor(dto.getProfesionalesValor());

            // Materiales
            mayorCosto.setMaterialesActivo(dto.getMaterialesActivo() != null ? dto.getMaterialesActivo() : true);
            mayorCosto.setMaterialesTipo(dto.getMaterialesTipo() != null ? dto.getMaterialesTipo() : "porcentaje");
            mayorCosto.setMaterialesValor(dto.getMaterialesValor());

            // Otros Costos
            mayorCosto.setOtrosCostosActivo(dto.getOtrosCostosActivo() != null ? dto.getOtrosCostosActivo() : true);
            mayorCosto.setOtrosCostosTipo(dto.getOtrosCostosTipo() != null ? dto.getOtrosCostosTipo() : "porcentaje");
            mayorCosto.setOtrosCostosValor(dto.getOtrosCostosValor());

            // Honorarios
            mayorCosto.setHonorariosActivo(dto.getHonorariosActivo() != null ? dto.getHonorariosActivo() : true);
            mayorCosto.setHonorariosTipo(dto.getHonorariosTipo() != null ? dto.getHonorariosTipo() : "porcentaje");
            mayorCosto.setHonorariosValor(dto.getHonorariosValor());

            mayoresCostos.add(mayorCosto);
        }

        return mayoresCostos;
    }

    // ========== MAPEO DE DESCUENTOS POR RUBRO ==========

    /**
     * Mapea una lista de DTOs de descuentos por rubro a entidades
     */
    private Set<com.rodrigo.construccion.model.entity.DescuentoPorRubro> mapearDescuentosPorRubroDTO(
            List<com.rodrigo.construccion.dto.request.DescuentoPorRubroDTO> dtos,
            PresupuestoNoCliente presupuesto) {
        
        Set<com.rodrigo.construccion.model.entity.DescuentoPorRubro> descuentos = new java.util.HashSet<>();
        
        if (dtos == null || dtos.isEmpty()) {
            return descuentos;
        }

        for (com.rodrigo.construccion.dto.request.DescuentoPorRubroDTO dto : dtos) {
            com.rodrigo.construccion.model.entity.DescuentoPorRubro descuento = new com.rodrigo.construccion.model.entity.DescuentoPorRubro();
            
            descuento.setId(dto.getId());
            descuento.setPresupuestoNoCliente(presupuesto);
            descuento.setNombreRubro(dto.getNombreRubro());
            descuento.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
            descuento.setTipo(dto.getTipo() != null ? dto.getTipo() : "porcentaje");
            descuento.setValor(dto.getValor());

            // Profesionales
            descuento.setProfesionalesActivo(dto.getProfesionalesActivo() != null ? dto.getProfesionalesActivo() : true);
            descuento.setProfesionalesTipo(dto.getProfesionalesTipo() != null ? dto.getProfesionalesTipo() : "porcentaje");
            descuento.setProfesionalesValor(dto.getProfesionalesValor());

            // Materiales
            descuento.setMaterialesActivo(dto.getMaterialesActivo() != null ? dto.getMaterialesActivo() : true);
            descuento.setMaterialesTipo(dto.getMaterialesTipo() != null ? dto.getMaterialesTipo() : "porcentaje");
            descuento.setMaterialesValor(dto.getMaterialesValor());

            // Otros Costos
            descuento.setOtrosCostosActivo(dto.getOtrosCostosActivo() != null ? dto.getOtrosCostosActivo() : true);
            descuento.setOtrosCostosTipo(dto.getOtrosCostosTipo() != null ? dto.getOtrosCostosTipo() : "porcentaje");
            descuento.setOtrosCostosValor(dto.getOtrosCostosValor());

            // Honorarios
            descuento.setHonorariosActivo(dto.getHonorariosActivo() != null ? dto.getHonorariosActivo() : false);
            descuento.setHonorariosTipo(dto.getHonorariosTipo() != null ? dto.getHonorariosTipo() : "PORCENTAJE");
            descuento.setHonorariosValor(dto.getHonorariosValor());

            // Mayores Costos
            descuento.setMayoresCostosActivo(dto.getMayoresCostosActivo() != null ? dto.getMayoresCostosActivo() : false);
            descuento.setMayoresCostosTipo(dto.getMayoresCostosTipo() != null ? dto.getMayoresCostosTipo() : "PORCENTAJE");
            descuento.setMayoresCostosValor(dto.getMayoresCostosValor());

            descuentos.add(descuento);
        }

        return descuentos;
    }

}

