package com.rodrigo.construccion.service;

import com.rodrigo.construccion.dto.request.RegistrarGastoRequest;
import com.rodrigo.construccion.dto.response.GastoObraProfesionalResponse;
import com.rodrigo.construccion.dto.response.SaldoCajaChicaResponse;

import java.util.List;

public interface IGastoObraProfesionalService {
    public GastoObraProfesionalResponse registrarGasto(RegistrarGastoRequest request);

    public List<GastoObraProfesionalResponse> listarGastosPorProfesional(Long profesionalObraId, Long empresaId);

    public GastoObraProfesionalResponse obtenerGasto(Long id, Long empresaId);

    public List<GastoObraProfesionalResponse> listarGastosPorObra(String calle, String altura, String piso, String depto, Long empresaId);

    public SaldoCajaChicaResponse obtenerSaldoCajaChica(Long profesionalObraId, Long empresaId);
}
