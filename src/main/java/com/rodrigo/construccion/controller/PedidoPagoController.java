package com.rodrigo.construccion.controller;

import com.rodrigo.construccion.dto.request.PedidoPagoRequestDTO;
import com.rodrigo.construccion.dto.response.EstadisticasPedidoPagoDTO;
import com.rodrigo.construccion.dto.response.PedidoPagoResponseDTO;
import com.rodrigo.construccion.service.IPedidoPagoService;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pedidos-pago")
public class PedidoPagoController {

    private final IPedidoPagoService pedidoPagoService;

    @GetMapping
    public ResponseEntity<Page<PedidoPagoResponseDTO>> listarPedidos(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId,
                                                                     @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(pedidoPagoService.obtenerTodosPaginados(empresaId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PedidoPagoResponseDTO> obtenerPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                               @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.obtenerPorId(id, empresaId));
    }

    @PostMapping
    public ResponseEntity<PedidoPagoResponseDTO> crearPedido(@RequestBody @Valid PedidoPagoRequestDTO requestDTO,
                                                             @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pedidoPagoService.crear(requestDTO, empresaId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PedidoPagoResponseDTO> actualizarPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                                  @RequestBody @jakarta.validation.Valid PedidoPagoRequestDTO requestDTO,
                                                                  @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.actualizar(id, requestDTO, empresaId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> eliminarPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                 @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        pedidoPagoService.eliminar(id, empresaId);
        return ResponseEntity.ok("Pedido de Pago eliminado exitosamente.");
    }


    /* OPERACIONES DE WORKFLOW QUE NO SE ESTÁN IMPLEMENTANDO EN EL FRONTEND, LOS DEJO PORQUE PUEDEN SER ÚTILES */

    @GetMapping("/pendientes-aprobacion")
    public ResponseEntity<List<PedidoPagoResponseDTO>> obtenerPendientesAprobacion(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.obtenerPendientesAprobacion(empresaId));
    }

    @GetMapping("/pendientes-autorizacion")
    public ResponseEntity<List<PedidoPagoResponseDTO>> obtenerPendientesAutorizacion(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.obtenerPendientesAutorizacion(empresaId));
    }

    @GetMapping("/pendientes-pago")
    public ResponseEntity<List<PedidoPagoResponseDTO>> obtenerPendientesPago(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.obtenerPendientesPago(empresaId));
    }

    @PutMapping("/{id}/enviar-aprobacion")
    public ResponseEntity<PedidoPagoResponseDTO> enviarParaAprobacion(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                                      @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.enviarParaAprobacion(id, empresaId));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<PedidoPagoResponseDTO> aprobarPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                               @Parameter(description = "ID del usuario aprobador") @RequestParam Long usuarioAprobadorId,
                                                               @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.aprobarPedido(id, usuarioAprobadorId, empresaId));
    }

    @PutMapping("/{id}/autorizar")
    public ResponseEntity<PedidoPagoResponseDTO> autorizarPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                                 @Parameter(description = "ID del usuario autorizador") @RequestParam Long usuarioAutorizadorId,
                                                                 @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.autorizarPedido(id, usuarioAutorizadorId, empresaId));
    }

    @PutMapping("/{id}/pagar")
    public ResponseEntity<PedidoPagoResponseDTO> marcarComoPagado(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                                  @Parameter(description = "ID del usuario pagador") @RequestParam Long usuarioPagadorId,
                                                                  @Parameter(description = "Número de comprobante de pago") @RequestParam(required = false) String numeroComprobante,
                                                                  @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.marcarComoPagado(id, usuarioPagadorId, numeroComprobante, empresaId));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<PedidoPagoResponseDTO> rechazarPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                                @Parameter(description = "Motivo del rechazo") @RequestParam String motivoRechazo,
                                                                @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.rechazarPedido(id, motivoRechazo, empresaId));
    }

    @PutMapping("/{id}/cancelar")
    public ResponseEntity<PedidoPagoResponseDTO> cancelarPedido(@Parameter(description = "ID del pedido") @PathVariable Long id,
                                                                @Parameter(description = "Motivo de cancelación") @RequestParam String motivo,
                                                                @Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.cancelarPedido(id, motivo, empresaId));
    }

    @GetMapping("/estadisticas")
    public ResponseEntity<EstadisticasPedidoPagoDTO> obtenerEstadisticas(@Parameter(description = "ID de la empresa") @RequestParam Long empresaId) {
        return ResponseEntity.ok(pedidoPagoService.obtenerEstadisticas(empresaId));
    }
}