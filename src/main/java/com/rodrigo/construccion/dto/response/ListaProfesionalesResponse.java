package com.rodrigo.construccion.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Respuesta estructurada para la lista de profesionales por tipo")
public class ListaProfesionalesResponse {

    private int total;
    private String tipoProfesional;
    private List<DisponibilidadProfesionalResponse> profesionales;

}