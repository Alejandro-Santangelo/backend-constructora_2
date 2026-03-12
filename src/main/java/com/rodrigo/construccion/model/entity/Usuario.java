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
    private String rol = "user";

    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Campo directo para multi-tenancy (sin relación ManyToOne para simplicidad)
    @Column(name = "id_empresa")
    private Long idEmpresa;

    // Getter para compatibilidad con código existente
    public Long getEmpresaId() {
        return idEmpresa;
    }

    public void setEmpresaId(Long idEmpresa) {
        this.idEmpresa = idEmpresa;
    }

    // Métodos de utilidad para roles
    public boolean isAdmin() {
        return ROL_ADMIN.equals(this.rol);
    }

    public boolean isManager() {
        return ROL_MANAGER.equals(this.rol) || isAdmin();
    }

    public boolean canManage() {
        return isManager() || isAdmin();
    }

    public boolean canView() {
        return activo && (ROL_VIEWER.equals(this.rol) || ROL_USER.equals(this.rol) || canManage());
    }

    // Roles del sistema
    public static final String ROL_ADMIN = "admin";
    public static final String ROL_MANAGER = "manager";
    public static final String ROL_USER = "user";
    public static final String ROL_VIEWER = "viewer";

}