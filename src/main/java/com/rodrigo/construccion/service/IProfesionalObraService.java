package com.rodrigo.construccion.service;

import java.math.BigDecimal;
import java.util.List;

import com.rodrigo.construccion.dto.request.ActualizarAsignacionRequest;
import com.rodrigo.construccion.dto.request.AsignarProfesionalRequest;
import com.rodrigo.construccion.dto.request.AsignarProfesionalesBatchRequest;
import com.rodrigo.construccion.dto.response.AsignacionProfesionalResponse;
import com.rodrigo.construccion.dto.response.DisponibilidadProfesionalResponse;
import com.rodrigo.construccion.dto.response.ListaProfesionalesResponse;
import com.rodrigo.construccion.dto.response.ProfesionalResponseDTO;
import com.rodrigo.construccion.dto.response.ProfesionalObraFinancieroDTO;
import com.rodrigo.construccion.model.entity.Profesional;
import com.rodrigo.construccion.model.entity.ProfesionalObra;

public interface IProfesionalObraService {
    public void actualizarSaldoDisponible(Long profesionalObraId, BigDecimal nuevoSaldo);

    public List<AsignacionProfesionalResponse> obtenerTodasComoDTO();

    public List<AsignacionProfesionalResponse> obtenerTodasPorEmpresa(Long empresaId);

    public ProfesionalObra obtenerPorId(Long id);

    public ProfesionalObra buscarAsignacionEspecifica(Long profesionalId, Long obraId);

    public AsignacionProfesionalResponse asignarProfesionalPorTipoComoDTO(AsignarProfesionalRequest request);

    public List<AsignacionProfesionalResponse> asignarMultiplesProfesionales(AsignarProfesionalesBatchRequest request);

    public List<AsignacionProfesionalResponse> obtenerAsignacionesPorTipo(String tipoProfesional, Long empresaId);

    public List<DisponibilidadProfesionalResponse> obtenerDisponibilidadPorTipo(String tipoProfesional,
                                                                                Long empresaId);

    public ListaProfesionalesResponse obtenerDisponibilidadProfesionalesPorTipo(String tipoProfesional,
                                                                                Long empresaId);

    public AsignacionProfesionalResponse actualizarAsignacionComoDTO(Long asignacionId,
                                                                     ActualizarAsignacionRequest request, Long empresaId);

    public AsignacionProfesionalResponse desactivarAsignacion(Long asignacionId, Long empresaId, Long obraId);

    public List<Profesional> obtenerTodosProfesionales();

    public List<ProfesionalObra> obtenerTodasLasAsignaciones();

    public List<ProfesionalResponseDTO> obtenerProfesionalesPorObraYEmpresa(Long empresaId, Long obraId);

    public List<ProfesionalObraFinancieroDTO> obtenerProfesionalesConDatosFinancieros(Long empresaId, Long obraId);

    // Gestión de caja chica
    public ProfesionalObra asignarCajaChica(Long profesionalObraId, java.math.BigDecimal monto, Long empresaId);
}
