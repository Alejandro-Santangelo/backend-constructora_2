package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.mapper.PedidoPagoMapper;
import com.rodrigo.construccion.dto.request.PedidoPagoRequestDTO;
import com.rodrigo.construccion.dto.response.EstadisticasPedidoPagoDTO;
import com.rodrigo.construccion.dto.response.PedidoPagoResponseDTO;
import com.rodrigo.construccion.exception.ResourceNotFoundException;
import com.rodrigo.construccion.model.entity.Empresa;
import com.rodrigo.construccion.model.entity.Obra;
import com.rodrigo.construccion.model.entity.PedidoPago;
import com.rodrigo.construccion.model.entity.Proveedor;
import com.rodrigo.construccion.repository.PedidoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class PedidoPagoService implements IPedidoPagoService {

    @Autowired
    private PedidoPagoRepository pedidoPagoRepository;

    @Autowired
    private PedidoPagoMapper pedidoPagoMapper;

    @Autowired
    private IProveedorService proveedorService;

    @Autowired
    private IObraService obraService;

    @Autowired
    private IEmpresaService empresaService;

    /* Obtener pedidos con paginación filtrados por empresa */
    @Override
    public Page<PedidoPagoResponseDTO> obtenerTodosPaginados(Long empresaId, Pageable pageable) {
        Page<PedidoPago> pedidosPage = pedidoPagoRepository.findByEmpresa_Id(empresaId, pageable);
        return pedidosPage.map(pedidoPagoMapper::toResponseDTO);
    }

    /* Obtener pedido por ID y empresa */
    @Override
    public PedidoPagoResponseDTO obtenerPorId(Long id, Long empresaId) {
        PedidoPago pedidoEncontrado = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pedido no encontrado con ID: " + id));
        return pedidoPagoMapper.toResponseDTO(pedidoEncontrado);
    }

    /* Crear nuevo pedido de pago */
    @Override
    public PedidoPagoResponseDTO crear(PedidoPagoRequestDTO requestDTO, Long empresaId) {
        // 1. Validar que la empresa existe
        Empresa empresa = empresaService.findEmpresaById(empresaId);

        // 2. Validar que el proveedor existe y pertenece a la empresa
        Proveedor proveedor = proveedorService.obtenerPorIdYEmpresa(requestDTO.getIdProveedor(), empresaId);


        // 3. Validar obra si está presente
        Obra obra = null;
        if (requestDTO.getIdObra() != null) {
            // El método encontrarObraPorIdYEmpresa ya valida que la obra pertenece a la empresa
            obra = obraService.encontrarObraPorIdYEmpresa(requestDTO.getIdObra(), empresaId);
        }

        // 4. Convertir DTO a entidad usando el mapper
        PedidoPago pedidoPago = pedidoPagoMapper.toEntity(requestDTO);

        // 5. Setear campos que no vienen del DTO
        pedidoPago.setEstado(PedidoPago.ESTADO_BORRADOR);
        pedidoPago.setEmpresa(empresa);
        pedidoPago.setProveedor(proveedor);
        pedidoPago.setObra(obra);
        pedidoPago.setFechaModificacion(LocalDateTime.now());

        // 6. Guardar en la base de datos
        PedidoPago pedidoGuardado = pedidoPagoRepository.save(pedidoPago);

        // 7. Convertir a DTO de respuesta y retornar
        return pedidoPagoMapper.toResponseDTO(pedidoGuardado);
    }

    /* Actualizar pedido existente
     * NOTA: NO se permite cambiar proveedor ni obra una vez creado el pedido
     */
    @Override
    public PedidoPagoResponseDTO actualizar(Long id, PedidoPagoRequestDTO requestDTO, Long empresaId) {
        // 1. Buscar el pedido validando que pertenece a la empresa
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        // 2. Validar que el pedido puede ser modificado (lógica de negocio original)
        if (!pedido.puedeSerModificado()) {
            throw new RuntimeException("No se puede modificar un pedido en estado: " + pedido.getEstado());
        }

        // 3. Actualizar campos disponibles (lógica de negocio original mantenida)
        // NOTA: Proveedor y Obra NO se modifican después de la creación
        if (requestDTO.getNumeroPedido() != null) {
            pedido.setNumeroPedido(requestDTO.getNumeroPedido());
        }
        if (requestDTO.getFechaPedido() != null) {
            pedido.setFechaPedido(requestDTO.getFechaPedido());
        }
        if (requestDTO.getFechaVencimiento() != null) {
            pedido.setFechaVencimiento(requestDTO.getFechaVencimiento());
        }
        if (requestDTO.getImporte() != null) {
            pedido.setImporte(requestDTO.getImporte());
        }
        if (requestDTO.getConcepto() != null) {
            pedido.setConcepto(requestDTO.getConcepto());
        }
        if (requestDTO.getTipoPago() != null) {
            pedido.setTipoPago(requestDTO.getTipoPago());
        }
        if (requestDTO.getNumeroFactura() != null) {
            pedido.setNumeroFactura(requestDTO.getNumeroFactura());
        }
        if (requestDTO.getNumeroComprobante() != null) {
            pedido.setNumeroComprobante(requestDTO.getNumeroComprobante());
        }
        if (requestDTO.getObservaciones() != null) {
            pedido.setObservaciones(requestDTO.getObservaciones());
        }

        // 4. Actualizar fecha de modificación
        pedido.setFechaModificacion(LocalDateTime.now());

        // 5. Guardar y retornar DTO de respuesta
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public void eliminar(Long id, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (!pedido.esBorrador()) {
            throw new RuntimeException("Solo se pueden eliminar pedidos en estado BORRADOR");
        }

        pedidoPagoRepository.deleteById(id);
    }

    @Override
    public List<PedidoPagoResponseDTO> obtenerPendientesAprobacion(Long empresaId) {
        return pedidoPagoRepository.findByEstadoAndEmpresa_Id(PedidoPago.ESTADO_PENDIENTE, empresaId)
                .stream()
                .map(pedidoPagoMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<PedidoPagoResponseDTO> obtenerPendientesAutorizacion(Long empresaId) {
        return pedidoPagoRepository.findByEstadoAndEmpresa_Id(PedidoPago.ESTADO_APROBADO, empresaId)
                .stream()
                .map(pedidoPagoMapper::toResponseDTO)
                .toList();
    }

    @Override
    public List<PedidoPagoResponseDTO> obtenerPendientesPago(Long empresaId) {
        return pedidoPagoRepository.findByEstadoAndEmpresa_Id(PedidoPago.ESTADO_AUTORIZADO, empresaId)
                .stream()
                .map(pedidoPagoMapper::toResponseDTO)
                .toList();
    }

    @Override
    public PedidoPagoResponseDTO enviarParaAprobacion(Long id, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (!pedido.esBorrador()) {
            throw new RuntimeException("Solo se pueden enviar pedidos en estado BORRADOR");
        }

        pedido.setEstado(PedidoPago.ESTADO_PENDIENTE);
        pedido.setFechaModificacion(LocalDateTime.now());
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public PedidoPagoResponseDTO aprobarPedido(Long id, Long usuarioAprobadorId, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (!pedido.estaPendiente()) {
            throw new RuntimeException("Solo se pueden aprobar pedidos en estado PENDIENTE");
        }

        pedido.setEstado(PedidoPago.ESTADO_APROBADO);
        pedido.setFechaAprobacion(LocalDateTime.now());
        pedido.setUsuarioAprobadorId(usuarioAprobadorId);
        pedido.setFechaModificacion(LocalDateTime.now());
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public PedidoPagoResponseDTO autorizarPedido(Long id, Long usuarioAutorizadorId, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (!pedido.estaAprobado()) {
            throw new RuntimeException("Solo se pueden autorizar pedidos en estado APROBADO");
        }

        pedido.setEstado(PedidoPago.ESTADO_AUTORIZADO);
        pedido.setFechaAutorizacion(LocalDateTime.now());
        pedido.setUsuarioAutorizadorId(usuarioAutorizadorId);
        pedido.setFechaModificacion(LocalDateTime.now());
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public PedidoPagoResponseDTO marcarComoPagado(Long id, Long usuarioPagadorId, String numeroComprobante, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (!pedido.estaAutorizado()) {
            throw new RuntimeException("Solo se pueden pagar pedidos en estado AUTORIZADO");
        }

        pedido.setEstado(PedidoPago.ESTADO_PAGADO);
        pedido.setFechaPago(LocalDateTime.now());
        pedido.setUsuarioPagadorId(usuarioPagadorId);
        if (numeroComprobante != null) {
            pedido.setNumeroComprobante(numeroComprobante);
        }
        pedido.setFechaModificacion(LocalDateTime.now());
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public PedidoPagoResponseDTO rechazarPedido(Long id, String motivoRechazo, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (pedido.estaPagado() || pedido.estaCancelado() || pedido.estaRechazado()) {
            throw new RuntimeException("No se puede rechazar un pedido en estado: " + pedido.getEstado());
        }

        pedido.setEstado(PedidoPago.ESTADO_RECHAZADO);
        pedido.setMotivoRechazo(motivoRechazo);
        pedido.setFechaModificacion(LocalDateTime.now());
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public PedidoPagoResponseDTO cancelarPedido(Long id, String motivo, Long empresaId) {
        PedidoPago pedido = pedidoPagoRepository.findByIdAndEmpresa_Id(id, empresaId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pedido de pago no encontrado con ID: " + id + " para la empresa: " + empresaId));

        if (pedido.estaPagado()) {
            throw new RuntimeException("No se puede cancelar un pedido ya pagado");
        }

        pedido.setEstado(PedidoPago.ESTADO_CANCELADO);
        pedido.setObservaciones(pedido.getObservaciones() + " - CANCELADO: " + motivo);
        pedido.setFechaModificacion(LocalDateTime.now());
        PedidoPago pedidoActualizado = pedidoPagoRepository.save(pedido);
        return pedidoPagoMapper.toResponseDTO(pedidoActualizado);
    }

    @Override
    public EstadisticasPedidoPagoDTO obtenerEstadisticas(Long empresaId) {
        EstadisticasPedidoPagoDTO estadisticas = new EstadisticasPedidoPagoDTO();

        estadisticas.setTotalPedidos((long) pedidoPagoRepository.findByEmpresa_Id(empresaId).size());
        estadisticas.setPendientesAprobacion(contarPorEstado(PedidoPago.ESTADO_PENDIENTE, empresaId));
        estadisticas.setPendientesAutorizacion(contarPorEstado(PedidoPago.ESTADO_APROBADO, empresaId));
        estadisticas.setPendientesPago(contarPorEstado(PedidoPago.ESTADO_AUTORIZADO, empresaId));
        estadisticas.setPagados(contarPorEstado(PedidoPago.ESTADO_PAGADO, empresaId));
        estadisticas.setRechazados(contarPorEstado(PedidoPago.ESTADO_RECHAZADO, empresaId));
        estadisticas.setCancelados(contarPorEstado(PedidoPago.ESTADO_CANCELADO, empresaId));

        estadisticas.setImporteTotalPendientes(calcularImporteTotalPorEstado(PedidoPago.ESTADO_PENDIENTE, empresaId));
        estadisticas.setImporteTotalAprobados(calcularImporteTotalPorEstado(PedidoPago.ESTADO_APROBADO, empresaId));
        estadisticas.setImporteTotalAutorizados(calcularImporteTotalPorEstado(PedidoPago.ESTADO_AUTORIZADO, empresaId));
        estadisticas.setImporteTotalPagados(calcularImporteTotalPorEstado(PedidoPago.ESTADO_PAGADO, empresaId));

        estadisticas.setFechaConsulta(LocalDate.now());

        return estadisticas;
    }

    private long contarPorEstado(String estado, Long empresaId) {
        return pedidoPagoRepository.findByEstadoAndEmpresa_Id(estado, empresaId).size();
    }

    private Double calcularImporteTotalPorEstado(String estado, Long empresaId) {
        return pedidoPagoRepository.findByEstadoAndEmpresa_Id(estado, empresaId).stream()
                .mapToDouble(PedidoPago::getImporte)
                .sum();
    }

    /* METODOS QUE NO ESTÁN SIENDO USADOS EN NINGUN LADO PERO QUE PODRIAN SER UTILES */

    /* Buscar pedidos por empresa */
    public List<PedidoPago> buscarPorEmpresa(Long empresaId) {
        return pedidoPagoRepository.findByEmpresa_Id(empresaId);
    }

    /* Buscar pedidos por rango de fechas */
    public List<PedidoPago> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
        return pedidoPagoRepository.findByFechaPedidoBetween(fechaInicio, fechaFin);
    }
}