package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.CheckInRequest;
import com.rodrigo.construccion.dto.request.CheckOutRequest;
import com.rodrigo.construccion.dto.response.AsistenciaObraResponse;
import com.rodrigo.construccion.dto.response.ReporteAsistenciasObraResponseDTO;

import java.util.List;

public interface IAsistenciaObraService {

    public AsistenciaObraResponse checkIn(CheckInRequest request);

    public AsistenciaObraResponse checkOut(Long asistenciaId, CheckOutRequest request);

    public List<AsistenciaObraResponse> listarAsistenciasPorProfesional(Long profesionalObraId, Long empresaId);

    public AsistenciaObraResponse obtenerAsistenciaHoy(Long profesionalObraId, Long empresaId);

    public ReporteAsistenciasObraResponseDTO obtenerReporteObra(String calle, String altura, String piso, String depto, Long empresaId);

}
