package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.PedidoPagoRequestDTO;
import com.rodrigo.construccion.dto.response.EstadisticasPedidoPagoDTO;
import com.rodrigo.construccion.dto.response.PedidoPagoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface IPedidoPagoService {

    Page<PedidoPagoResponseDTO> obtenerTodosPaginados(Long empresaId, Pageable pageable);

    PedidoPagoResponseDTO obtenerPorId(Long id, Long empresaId);

    PedidoPagoResponseDTO crear(PedidoPagoRequestDTO requestDTO, Long empresaId);

    PedidoPagoResponseDTO actualizar(Long id, PedidoPagoRequestDTO requestDTO, Long empresaId);

    void eliminar(Long id, Long empresaId);

    List<PedidoPagoResponseDTO> obtenerPendientesAprobacion(Long empresaId);

    List<PedidoPagoResponseDTO> obtenerPendientesAutorizacion(Long empresaId);

    List<PedidoPagoResponseDTO> obtenerPendientesPago(Long empresaId);

    PedidoPagoResponseDTO enviarParaAprobacion(Long id, Long empresaId);

    PedidoPagoResponseDTO aprobarPedido(Long id, Long usuarioAprobadorId, Long empresaId);

    PedidoPagoResponseDTO autorizarPedido(Long id, Long usuarioAutorizadorId, Long empresaId);

    PedidoPagoResponseDTO marcarComoPagado(Long id, Long usuarioPagadorId, String numeroComprobante, Long empresaId);

    PedidoPagoResponseDTO rechazarPedido(Long id, String motivoRechazo, Long empresaId);

    PedidoPagoResponseDTO cancelarPedido(Long id, String motivo, Long empresaId);

    EstadisticasPedidoPagoDTO obtenerEstadisticas(Long empresaId);

}
