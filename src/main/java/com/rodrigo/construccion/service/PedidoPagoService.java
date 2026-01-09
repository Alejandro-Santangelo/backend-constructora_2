package com.rodrigo.construccion.service;

import com.rodrigo.construccion.model.entity.PedidoPago;
import com.rodrigo.construccion.repository.PedidoPagoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Servicio para gestión de Pedidos de Pago
 * Maneja la lógica de negocio para pedidos de pago a proveedores
 */
@Service
@Transactional
public class PedidoPagoService {

    // Logger eliminado, usar System.out.println si es necesario
    
    @Autowired
    private PedidoPagoRepository pedidoPagoRepository;

    /**
     * Obtener todos los pedidos de pago
     */
    public List<PedidoPago> obtenerTodos() {
    System.out.println("Obteniendo todos los pedidos de pago");
        return pedidoPagoRepository.findAll();
    }

    /**
     * Obtener pedidos con paginación
     */
    public Page<PedidoPago> obtenerTodosPaginados(Pageable pageable) {
    System.out.println("Obteniendo pedidos paginados: " + pageable);
        return pedidoPagoRepository.findAll(pageable);
    }

    /**
     * Obtener pedido por ID
     */
    public Optional<PedidoPago> obtenerPorId(Long id) {
    System.out.println("Obteniendo pedido por ID: " + id);
        return pedidoPagoRepository.findById(id);
    }

    /**
     * Crear nuevo pedido de pago
     */
    public PedidoPago crear(PedidoPago pedidoPago) {
    System.out.println("Creando nuevo pedido de pago");
        if (pedidoPago.getEstado() == null) {
            pedidoPago.setEstado(PedidoPago.ESTADO_BORRADOR);
        }
        return pedidoPagoRepository.save(pedidoPago);
    }

    /**
     * Actualizar pedido existente
     */
    public PedidoPago actualizar(Long id, PedidoPago pedidoActualizado) {
    System.out.println("Actualizando pedido ID: " + id);
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
                    // Solo permitir actualización si está en borrador o pendiente
                    if (!pedido.puedeSerModificado()) {
                        throw new RuntimeException("No se puede modificar un pedido en estado: " + pedido.getEstado());
                    }
                    
                    // Actualizar campos disponibles
                    if (pedidoActualizado.getNumeroPedido() != null) {
                        pedido.setNumeroPedido(pedidoActualizado.getNumeroPedido());
                    }
                    if (pedidoActualizado.getFechaPedido() != null) {
                        pedido.setFechaPedido(pedidoActualizado.getFechaPedido());
                    }
                    if (pedidoActualizado.getFechaVencimiento() != null) {
                        pedido.setFechaVencimiento(pedidoActualizado.getFechaVencimiento());
                    }
                    if (pedidoActualizado.getImporte() != null) {
                        pedido.setImporte(pedidoActualizado.getImporte());
                    }
                    if (pedidoActualizado.getConcepto() != null) {
                        pedido.setConcepto(pedidoActualizado.getConcepto());
                    }
                    if (pedidoActualizado.getTipoPago() != null) {
                        pedido.setTipoPago(pedidoActualizado.getTipoPago());
                    }
                    if (pedidoActualizado.getNumeroFactura() != null) {
                        pedido.setNumeroFactura(pedidoActualizado.getNumeroFactura());
                    }
                    if (pedidoActualizado.getNumeroComprobante() != null) {
                        pedido.setNumeroComprobante(pedidoActualizado.getNumeroComprobante());
                    }
                    if (pedidoActualizado.getObservaciones() != null) {
                        pedido.setObservaciones(pedidoActualizado.getObservaciones());
                    }
                    
                    pedido.setFechaModificacion(LocalDateTime.now());
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido de pago no encontrado con ID: " + id));
    }

    /**
     * Eliminar pedido
     */
    public void eliminar(Long id) {
    System.out.println("Eliminando pedido ID: " + id);
        PedidoPago pedido = pedidoPagoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido de pago no encontrado con ID: " + id));
        
        if (!pedido.esBorrador()) {
            throw new RuntimeException("Solo se pueden eliminar pedidos en estado BORRADOR");
        }
        
        pedidoPagoRepository.deleteById(id);
    }

    /**
     * Buscar pedidos por proveedor
     */
    public List<PedidoPago> buscarPorProveedor(Long proveedorId) {
    System.out.println("Buscando pedidos por proveedor: " + proveedorId);
        return pedidoPagoRepository.findByProveedor_Id(proveedorId);
    }

    /**
     * Buscar pedidos por obra
     */
    public List<PedidoPago> buscarPorObra(Long obraId) {
    System.out.println("Buscando pedidos por obra: " + obraId);
        return pedidoPagoRepository.findByObra_Id(obraId);
    }

    /**
     * Buscar pedidos por estado
     */
    public List<PedidoPago> buscarPorEstado(String estado) {
    System.out.println("Buscando pedidos por estado: " + estado);
        return pedidoPagoRepository.findByEstado(estado);
    }

    /**
     * Buscar pedidos por empresa
     */
    public List<PedidoPago> buscarPorEmpresa(Long empresaId) {
    System.out.println("Buscando pedidos por empresa: " + empresaId);
        return pedidoPagoRepository.findByEmpresa_Id(empresaId);
    }

    /**
     * Buscar pedidos por rango de fechas
     */
    public List<PedidoPago> buscarPorRangoFechas(LocalDate fechaInicio, LocalDate fechaFin) {
    System.out.println("Buscando pedidos entre " + fechaInicio + " y " + fechaFin);
        return pedidoPagoRepository.findByFechaPedidoBetween(fechaInicio, fechaFin);
    }

    /**
     * Buscar pedidos pendientes de aprobación
     */
    public List<PedidoPago> obtenerPendientesAprobacion() {
        return buscarPorEstado(PedidoPago.ESTADO_PENDIENTE);
    }

    /**
     * Buscar pedidos aprobados pendientes de autorización
     */
    public List<PedidoPago> obtenerPendientesAutorizacion() {
        return buscarPorEstado(PedidoPago.ESTADO_APROBADO);
    }

    /**
     * Buscar pedidos autorizados pendientes de pago
     */
    public List<PedidoPago> obtenerPendientesPago() {
        return buscarPorEstado(PedidoPago.ESTADO_AUTORIZADO);
    }

    /**
     * OPERACIONES DE WORKFLOW
     */

    /**
     * Enviar pedido para aprobación
     */
    public PedidoPago enviarParaAprobacion(Long id) {
    System.out.println("Enviando pedido " + id + " para aprobación");
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
                    if (!pedido.esBorrador()) {
                        throw new RuntimeException("Solo se pueden enviar pedidos en estado BORRADOR");
                    }
                    pedido.setEstado(PedidoPago.ESTADO_PENDIENTE);
                    pedido.setFechaModificacion(LocalDateTime.now());
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Aprobar pedido
     */
    public PedidoPago aprobarPedido(Long id, Long usuarioAprobadorId) {
    System.out.println("Aprobando pedido " + id + " por usuario " + usuarioAprobadorId);
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
                    if (!pedido.estaPendiente()) {
                        throw new RuntimeException("Solo se pueden aprobar pedidos en estado PENDIENTE");
                    }
                    pedido.setEstado(PedidoPago.ESTADO_APROBADO);
                    pedido.setFechaAprobacion(LocalDateTime.now());
                    pedido.setUsuarioAprobadorId(usuarioAprobadorId);
                    pedido.setFechaModificacion(LocalDateTime.now());
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Autorizar pedido
     */
    public PedidoPago autorizarPedido(Long id, Long usuarioAutorizadorId) {
    System.out.println("Autorizando pedido " + id + " por usuario " + usuarioAutorizadorId);
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
                    if (!pedido.estaAprobado()) {
                        throw new RuntimeException("Solo se pueden autorizar pedidos en estado APROBADO");
                    }
                    pedido.setEstado(PedidoPago.ESTADO_AUTORIZADO);
                    pedido.setFechaAutorizacion(LocalDateTime.now());
                    pedido.setUsuarioAutorizadorId(usuarioAutorizadorId);
                    pedido.setFechaModificacion(LocalDateTime.now());
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Marcar como pagado
     */
    public PedidoPago marcarComoPagado(Long id, Long usuarioPagadorId, String numeroComprobante) {
    System.out.println("Marcando pedido " + id + " como pagado por usuario " + usuarioPagadorId);
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
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
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Rechazar pedido
     */
    public PedidoPago rechazarPedido(Long id, String motivoRechazo) {
    System.out.println("Rechazando pedido " + id + " con motivo: " + motivoRechazo);
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
                    if (pedido.estaPagado() || pedido.estaCancelado() || pedido.estaRechazado()) {
                        throw new RuntimeException("No se puede rechazar un pedido en estado: " + pedido.getEstado());
                    }
                    pedido.setEstado(PedidoPago.ESTADO_RECHAZADO);
                    pedido.setMotivoRechazo(motivoRechazo);
                    pedido.setFechaModificacion(LocalDateTime.now());
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Cancelar pedido
     */
    public PedidoPago cancelarPedido(Long id, String motivo) {
    System.out.println("Cancelando pedido " + id + " con motivo: " + motivo);
        return pedidoPagoRepository.findById(id)
                .map(pedido -> {
                    if (pedido.estaPagado()) {
                        throw new RuntimeException("No se puede cancelar un pedido ya pagado");
                    }
                    pedido.setEstado(PedidoPago.ESTADO_CANCELADO);
                    pedido.setObservaciones(pedido.getObservaciones() + " - CANCELADO: " + motivo);
                    pedido.setFechaModificacion(LocalDateTime.now());
                    return pedidoPagoRepository.save(pedido);
                })
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    /**
     * Estadísticas básicas
     */
    public long contarTotal() {
        return pedidoPagoRepository.count();
    }

    public long contarPorEstado(String estado) {
        return buscarPorEstado(estado).size();
    }

    public Double calcularImporteTotalPorEstado(String estado) {
        return buscarPorEstado(estado).stream()
                .mapToDouble(PedidoPago::getImporte)
                .sum();
    }
}