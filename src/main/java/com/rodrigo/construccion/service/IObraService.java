package com.rodrigo.construccion.service;

import java.util.List;

import com.rodrigo.construccion.dto.request.ObraRequestDTO;
import com.rodrigo.construccion.dto.response.*;

import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.model.entity.Obra;

public interface IObraService {

    ObraSimpleDTO obtenerPorId(Long id);

    Obra encontrarObraPorIdYEmpresa(Long id, Long idEmpresa);

    Obra findById(Long id);

    Obra buscarPorIdOpcional(Long id);

    List<ObraResponseDTO> obtenerPorCliente(Long clienteId);

    List<ObraSimpleDTO> obtenerPorEstado(EstadoObra estado);

    @Deprecated
    List<ObraSimpleDTO> obtenerActivas();
    
    List<ObraSimpleDTO> obtenerActivasPorEmpresa(Long empresaId);

    List<ObraResponseDTO> obtenerPorEmpresa(Long empresaId);

    List<ObraResponseDTO> obtenerObrasManualesPorEmpresa(Long empresaId);

    List<ObraResponseDTO> obtenerTodas();

    List<ProfesionalResponseDTO> obtenerProfesionalesAsignados(Long obraId);

    ObraResponseDTO crear(ObraRequestDTO obraRequestDto, Long clienteId);

    ObraResponseDTO actualizar(Long id, ObraRequestDTO obraActualizada);

    void eliminarEnCascada(Long id, Long empresaId, String rol);

    ObraResponseDTO cambiarEstado(Long id, EstadoObra nuevoEstado);

    EstadisticasObraDTO obtenerEstadisticas();

    List<ProfesionalResponseDTO> actualizarPorcentajeGananciaTodosAsignados(Long obraId, double porcentaje);

    ProfesionalResponseDTO actualizarPorcentajeGananciaProfesionalAsignado(Long obraId, Long profesionalId,
                                                                           double porcentaje);

    List<String> obtenerEstadosObra();

    boolean existeObra(Long empresaId, Long obraId);

    // === NUEVOS MÉTODOS PARA BORRADORES ===
    
    /**
     * Crea una obra independiente en estado BORRADOR.
     * Permite ir guardando los datos del formulario por etapas.
     * @param obraRequestDto Datos parciales o completos de la obra
     * @param clienteId ID del cliente
     * @return Obra creada en estado BORRADOR
     */
    ObraResponseDTO crearBorrador(ObraRequestDTO obraRequestDto, Long clienteId);
    
    /**
     * Actualiza un borrador de obra independiente.
     * Solo permite actualizar si el estado es BORRADOR.
     * @param id ID de la obra borrador
     * @param obraRequestDto Nuevos datos de la obra
     * @return Obra borrador actualizada
     */
    ObraResponseDTO actualizarBorrador(Long id, ObraRequestDTO obraRequestDto);
    
    /**
     * Convierte un borrador en obra activa.
     * Cambia el estado de BORRADOR a A_ENVIAR u otro estado inicial.
     * @param id ID de la obra borrador
     * @return Obra transformada a estado activo
     */
    ObraResponseDTO confirmarBorrador(Long id);
    
    /**
     * Obtiene todos los borradores de obras independientes por empresa.
     * @param empresaId ID de la empresa
     * @return Lista de obras en estado BORRADOR
     */
    List<ObraResponseDTO> obtenerBorradores(Long empresaId);

}
