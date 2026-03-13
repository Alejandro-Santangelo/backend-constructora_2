package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios", uniqueConstraints = {@UniqueConstraint(columnNames = "email")})
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_usuario")
    private Long id;

    @NotBlank(message = "El nombre del usuario es obligatorio")
    @Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
    @Column(name = "nombre", nullable = false, length = 150)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El formato del email no es válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Column(name = "password_hash", nullable = false, columnDefinition = "TEXT")
    private String passwordHash;

    @Size(max = 50, message = "El rol no puede exceder 50 caracteres")
    @Column(name = "rol", nullable = false, length = 50)
    private String rol = "usuario";

    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Campo directo para multi-tenancy (sin relación ManyToOne para simplicidad)
    // Empresa principal del usuario (para compatibilidad con código existente)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    // 🆕 SISTEMA MULTI-EMPRESA: Lista de empresas a las que el usuario tiene acceso
    // - Si está vacía o null: usuario tiene acceso solo a su empresa principal (idEmpresa)
    // - Si tiene valores: usuario tiene acceso a esas empresas específicas
    // - SUPER_ADMIN: lista vacía/null significa acceso a TODAS las empresas
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
        name = "usuario_empresas_permitidas",
        joinColumns = @JoinColumn(name = "usuario_id")
    )
    @Column(name = "empresa_id")
    private List<Long> empresasPermitidas = new ArrayList<>();

    // Getter para compatibilidad con código existente
    public Long getEmpresaId() {
        return idEmpresa;
    }

    public void setEmpresaId(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    // 🆕 Métodos auxiliares para multi-empresa
    
    /**
     * Verifica si el usuario tiene acceso a una empresa específica
     * @param empresaId ID de la empresa a verificar
     * @return true si el usuario tiene acceso a esa empresa
     */
    public boolean tieneAccesoAEmpresa(Long empresaId) {
        if (empresaId == null) return false;
        
        // SUPER_ADMINISTRADOR tiene acceso a todas las empresas
        if ("SUPER_ADMINISTRADOR".equalsIgnoreCase(this.rol)) {
            return true;
        }
        
        // Si tiene empresas permitidas configuradas, verificar en la lista
        if (empresasPermitidas != null && !empresasPermitidas.isEmpty()) {
            return empresasPermitidas.contains(empresaId);
        }
        
        // Fallback: verificar empresa principal
        return empresaId.equals(this.idEmpresa);
    }
    
    /**
     * Agrega acceso a una empresa
     */
    public void agregarEmpresaPermitida(Long empresaId) {
        if (empresaId != null && !empresasPermitidas.contains(empresaId)) {
            empresasPermitidas.add(empresaId);
        }
    }
    
    /**
     * Remueve acceso a una empresa
     */
    public void removerEmpresaPermitida(Long empresaId) {
        empresasPermitidas.remove(empresaId);
    }
    
    /**
     * Obtiene todas las empresas a las que tiene acceso
     */
    public List<Long> obtenerEmpresasAccesibles() {
        if (empresasPermitidas != null && !empresasPermitidas.isEmpty()) {
            return new ArrayList<>(empresasPermitidas);
        }
        // Si no tiene empresas configuradas, retornar solo la principal
        if (idEmpresa != null) {
            return List.of(idEmpresa);
        }
        return new ArrayList<>();
    }

    // Métodos de utilidad para roles
    public boolean isAdmin() {
        return ROL_ADMIN.equals(this.rol) || ROL_CONTRATISTA.equalsIgnoreCase(this.rol);
    }
    
    public boolean isSuperAdmin() {
        return ROL_SUPER_ADMIN.equals(this.rol);
    }

    public boolean isGerente() {
        return ROL_GERENTE.equals(this.rol) || isAdmin();
    }

    public boolean canManage() {
        return isGerente() || isAdmin() || isSuperAdmin();
    }

    public boolean canView() {
        return activo && (ROL_VISUALIZADOR.equals(this.rol) || ROL_USUARIO.equals(this.rol) || canManage());
    }

    // Roles del sistema
    public static final String ROL_ADMIN = "administrador";
    public static final String ROL_CONTRATISTA = "contratista"; // Equivalente a admin de empresa
    public static final String ROL_SUPER_ADMIN = "SUPER_ADMINISTRADOR"; // Acceso global
    public static final String ROL_GERENTE = "gerente";
    public static final String ROL_ARQUITECTO = "arquitecto";
    public static final String ROL_INGENIERO = "ingeniero";
    public static final String ROL_MAESTRO_OBRA = "maestro_obra";
    public static final String ROL_EMPLEADO = "empleado";
    public static final String ROL_USUARIO = "usuario";
    public static final String ROL_VISUALIZADOR = "visualizador";

}