package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.model.entity.PedidoPago;
import com.rodrigo.construccion.service.PedidoPagoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controlador REST para gestión de Pedidos de Pago
 * 
 * Maneja todas las operaciones CRUD para pedidos de pago a proveedores.
 * Incluye workflow de aprobación, autorización y pago.
 */
@RestController
@RequestMapping("/pedidos-pago")
@Tag(name = "Pedidos de Pago", description = "Operaciones de gestión de pedidos de pago a proveedores")
public class PedidoPagoController {

    // Logger eliminado
    
    @Autowired
    private PedidoPagoService pedidoPagoService;

    /**
     * OPERACIONES CRUD BÁSICAS
     */

    @GetMapping
    @Operation(summary = "Listar pedidos de pago", description = "Obtiene todos los pedidos con paginación opcional")
    public ResponseEntity<Page<PedidoPago>> listarPedidos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
            @PageableDefault(size = 20) Pageable pageable) {
        
    System.out.println("Listando pedidos de pago para empresa: " + empresaId + " con paginación: " + pageable);
        Page<PedidoPago> pedidos = pedidoPagoService.obtenerTodosPaginados(pageable);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener pedido de pago", description = "Obtiene un pedido específico por su ID")
    public ResponseEntity<PedidoPago> obtenerPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedido ID: " + id + " para empresa: " + empresaId);
        return pedidoPagoService.obtenerPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @Operation(summary = "Crear pedido de pago", description = "Crea un nuevo pedido en el sistema")
    public ResponseEntity<PedidoPago> crearPedido(
            @RequestBody PedidoPago pedidoPago,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Creando nuevo pedido de pago para empresa: " + empresaId);
        PedidoPago pedidoCreado = pedidoPagoService.crear(pedidoPago);
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoCreado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar pedido de pago", description = "Actualiza un pedido existente")
    public ResponseEntity<PedidoPago> actualizarPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @RequestBody PedidoPago pedidoPago,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Actualizando pedido ID: " + id + " para empresa: " + empresaId);
        try {
            PedidoPago pedidoActualizado = pedidoPagoService.actualizar(id, pedidoPago);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar pedido de pago", description = "Elimina un pedido del sistema (solo borradores)")
    public ResponseEntity<Void> eliminarPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Eliminando pedido ID: " + id + " para empresa: " + empresaId);
        try {
            pedidoPagoService.eliminar(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * OPERACIONES ESPECIALIZADAS
     */

    @GetMapping("/por-proveedor/{proveedorId}")
    @Operation(summary = "Pedidos por proveedor", description = "Obtiene todos los pedidos de un proveedor")
    public ResponseEntity<List<PedidoPago>> obtenerPedidosPorProveedor(
            @Parameter(description = "ID del proveedor") @PathVariable Long proveedorId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos para proveedor: " + proveedorId + " de empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.buscarPorProveedor(proveedorId);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/por-obra/{obraId}")
    @Operation(summary = "Pedidos por obra", description = "Obtiene todos los pedidos de una obra")
    public ResponseEntity<List<PedidoPago>> obtenerPedidosPorObra(
            @Parameter(description = "ID de la obra") @PathVariable Long obraId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos para obra: " + obraId + " de empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.buscarPorObra(obraId);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/por-estado")
    @Operation(summary = "Pedidos por estado", description = "Obtiene pedidos filtrados por estado")
    public ResponseEntity<List<PedidoPago>> obtenerPedidosPorEstado(
            @Parameter(description = "Estado del pedido") @RequestParam String estado,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos con estado: " + estado + " para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.buscarPorEstado(estado);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/por-empresa/{empresaId}")
    @Operation(summary = "Pedidos por empresa", description = "Obtiene todos los pedidos de una empresa")
    public ResponseEntity<List<PedidoPago>> obtenerPedidosPorEmpresa(
            @Parameter(description = "ID de la empresa") @PathVariable Long empresaId) {
        
    System.out.println("Obteniendo pedidos para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.buscarPorEmpresa(empresaId);
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/por-fecha")
    @Operation(summary = "Pedidos por rango de fechas", description = "Obtiene pedidos dentro de un rango de fechas")
    public ResponseEntity<List<PedidoPago>> obtenerPedidosPorRangoFechas(
            @Parameter(description = "Fecha inicio (formato YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @Parameter(description = "Fecha fin (formato YYYY-MM-DD)") 
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos entre " + fechaInicio + " y " + fechaFin + " para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.buscarPorRangoFechas(fechaInicio, fechaFin);
        return ResponseEntity.ok(pedidos);
    }

    /**
     * OPERACIONES DE WORKFLOW
     */

    @GetMapping("/pendientes-aprobacion")
    @Operation(summary = "Pedidos pendientes de aprobación", description = "Obtiene pedidos en estado PENDIENTE")
    public ResponseEntity<List<PedidoPago>> obtenerPendientesAprobacion(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos pendientes de aprobación para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.obtenerPendientesAprobacion();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/pendientes-autorizacion")
    @Operation(summary = "Pedidos pendientes de autorización", description = "Obtiene pedidos en estado APROBADO")
    public ResponseEntity<List<PedidoPago>> obtenerPendientesAutorizacion(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos pendientes de autorización para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.obtenerPendientesAutorizacion();
        return ResponseEntity.ok(pedidos);
    }

    @GetMapping("/pendientes-pago")
    @Operation(summary = "Pedidos pendientes de pago", description = "Obtiene pedidos en estado AUTORIZADO")
    public ResponseEntity<List<PedidoPago>> obtenerPendientesPago(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo pedidos pendientes de pago para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.obtenerPendientesPago();
        return ResponseEntity.ok(pedidos);
    }

    @PutMapping("/{id}/enviar-aprobacion")
    @Operation(summary = "Enviar para aprobación", description = "Cambia el estado del pedido a PENDIENTE")
    public ResponseEntity<PedidoPago> enviarParaAprobacion(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Enviando pedido " + id + " para aprobación en empresa: " + empresaId);
        try {
            PedidoPago pedidoActualizado = pedidoPagoService.enviarParaAprobacion(id);
            return ResponseEntity.ok(pedidoActualizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/aprobar")
    @Operation(summary = "Aprobar pedido", description = "Cambia el estado del pedido a APROBADO")
    public ResponseEntity<PedidoPago> aprobarPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "ID del usuario aprobador") @RequestParam Long usuarioAprobadorId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Aprobando pedido " + id + " por usuario " + usuarioAprobadorId + " en empresa: " + empresaId);
        try {
            PedidoPago pedidoAprobado = pedidoPagoService.aprobarPedido(id, usuarioAprobadorId);
            return ResponseEntity.ok(pedidoAprobado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/autorizar")
    @Operation(summary = "Autorizar pedido", description = "Cambia el estado del pedido a AUTORIZADO")
    public ResponseEntity<PedidoPago> autorizarPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "ID del usuario autorizador") @RequestParam Long usuarioAutorizadorId,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Autorizando pedido " + id + " por usuario " + usuarioAutorizadorId + " en empresa: " + empresaId);
        try {
            PedidoPago pedidoAutorizado = pedidoPagoService.autorizarPedido(id, usuarioAutorizadorId);
            return ResponseEntity.ok(pedidoAutorizado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/pagar")
    @Operation(summary = "Marcar como pagado", description = "Cambia el estado del pedido a PAGADO")
    public ResponseEntity<PedidoPago> marcarComoPagado(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "ID del usuario pagador") @RequestParam Long usuarioPagadorId,
            @Parameter(description = "Número de comprobante de pago") @RequestParam(required = false) String numeroComprobante,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Marcando pedido " + id + " como pagado por usuario " + usuarioPagadorId + " en empresa: " + empresaId);
        try {
            PedidoPago pedidoPagado = pedidoPagoService.marcarComoPagado(id, usuarioPagadorId, numeroComprobante);
            return ResponseEntity.ok(pedidoPagado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/rechazar")
    @Operation(summary = "Rechazar pedido", description = "Cambia el estado del pedido a RECHAZADO")
    public ResponseEntity<PedidoPago> rechazarPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "Motivo del rechazo") @RequestParam String motivoRechazo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Rechazando pedido " + id + " con motivo: " + motivoRechazo + " en empresa: " + empresaId);
        try {
            PedidoPago pedidoRechazado = pedidoPagoService.rechazarPedido(id, motivoRechazo);
            return ResponseEntity.ok(pedidoRechazado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}/cancelar")
    @Operation(summary = "Cancelar pedido", description = "Cambia el estado del pedido a CANCELADO")
    public ResponseEntity<PedidoPago> cancelarPedido(
            @Parameter(description = "ID del pedido") @PathVariable Long id,
            @Parameter(description = "Motivo de cancelación") @RequestParam String motivo,
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Cancelando pedido " + id + " con motivo: " + motivo + " en empresa: " + empresaId);
        try {
            PedidoPago pedidoCancelado = pedidoPagoService.cancelarPedido(id, motivo);
            return ResponseEntity.ok(pedidoCancelado);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * OPERACIONES DE ESTADÍSTICAS Y REPORTES
     */

    @GetMapping("/estadisticas")
    @Operation(summary = "Estadísticas de pedidos", description = "Obtiene estadísticas generales de pedidos de pago")
    public ResponseEntity<Map<String, Object>> obtenerEstadisticas(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo estadísticas de pedidos para empresa: " + empresaId);
        
        Map<String, Object> estadisticas = new HashMap<>();
        estadisticas.put("totalPedidos", pedidoPagoService.contarTotal());
        estadisticas.put("pendientesAprobacion", pedidoPagoService.contarPorEstado(PedidoPago.ESTADO_PENDIENTE));
        estadisticas.put("pendientesAutorizacion", pedidoPagoService.contarPorEstado(PedidoPago.ESTADO_APROBADO));
        estadisticas.put("pendientesPago", pedidoPagoService.contarPorEstado(PedidoPago.ESTADO_AUTORIZADO));
        estadisticas.put("pagados", pedidoPagoService.contarPorEstado(PedidoPago.ESTADO_PAGADO));
        estadisticas.put("rechazados", pedidoPagoService.contarPorEstado(PedidoPago.ESTADO_RECHAZADO));
        estadisticas.put("cancelados", pedidoPagoService.contarPorEstado(PedidoPago.ESTADO_CANCELADO));
        
        // Importes por estado
        estadisticas.put("importeTotalPendientes", pedidoPagoService.calcularImporteTotalPorEstado(PedidoPago.ESTADO_PENDIENTE));
        estadisticas.put("importeTotalAprobados", pedidoPagoService.calcularImporteTotalPorEstado(PedidoPago.ESTADO_APROBADO));
        estadisticas.put("importeTotalAutorizados", pedidoPagoService.calcularImporteTotalPorEstado(PedidoPago.ESTADO_AUTORIZADO));
        estadisticas.put("importeTotalPagados", pedidoPagoService.calcularImporteTotalPorEstado(PedidoPago.ESTADO_PAGADO));
        
        estadisticas.put("fechaConsulta", LocalDate.now());
        
        return ResponseEntity.ok(estadisticas);
    }

    @GetMapping("/todos-simple")
    @Operation(summary = "Lista simple de pedidos", description = "Obtiene todos los pedidos sin paginación")
    public ResponseEntity<List<PedidoPago>> obtenerTodosPedidos(
            @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        
    System.out.println("Obteniendo todos los pedidos para empresa: " + empresaId);
        List<PedidoPago> pedidos = pedidoPagoService.obtenerTodos();
        return ResponseEntity.ok(pedidos);
    }
}