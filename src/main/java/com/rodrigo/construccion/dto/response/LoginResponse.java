package com.rodrigo.construccion.dto.response;

import com.rodrigo.construccion.model.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponse {
    private Long userId;
    private String nombre;
    private String email;
    private String rol;
    private Long empresaId;
    private String empresaNombre;
    private List<EmpresaPermitida> empresasPermitidas;
    private boolean esSuperAdmin;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EmpresaPermitida {
        private Long id;
        private String nombre;
        private String cuit;
    }

    public static LoginResponse fromUsuario(Usuario usuario) {
        LoginResponse response = new LoginResponse();
        response.setUserId(usuario.getId());
        response.setNombre(usuario.getNombre());
        response.setEmail(usuario.getEmail());
        response.setRol(usuario.getRol());
        response.setEmpresaId(usuario.getEmpresaId());
        response.setEsSuperAdmin("SUPER_ADMINISTRADOR".equals(usuario.getRol()));
        return response;
    }
}
