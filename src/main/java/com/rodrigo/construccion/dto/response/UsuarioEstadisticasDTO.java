package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para las estadísticas de usuarios de una empresa
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioEstadisticasDTO {

    private Long totalUsuarios;
    private Long usuariosActivos;
    private Long usuariosInactivos;
    private Integer administradores;
    private Double porcentajeActivos;

}

