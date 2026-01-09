package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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

/**
 * Entidad Usuario
 * 
 * Representa los usuarios del sistema que pertenecen a una empresa específica.
 * Implementa el patrón Multi-Tenant donde cada usuario solo puede ver datos de
 * su empresa.
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "usuarios", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email")
})
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

    @NotBlank(message = "El rol es obligatorio")
    @Size(max = 50, message = "El rol no puede exceder 50 caracteres")
    @Column(name = "rol", nullable = false, length = 50)
    private String rol = "user";

    @Column(name = "activo")
    private Boolean activo = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación Multi-Tenant: Cada usuario pertenece a una empresa
    @JsonBackReference("empresa-usuarios")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_empresa")
    private Empresa empresa;

    // Getter para el ID de la empresa (útil para queries)
    public Long getEmpresaId() {
        return empresa != null ? empresa.getId() : null;
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