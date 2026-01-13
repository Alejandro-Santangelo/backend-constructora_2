package com.rodrigo.construccion.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/*
 * Cada empresa representa un tenant diferente:
 * - Constructora: maneja obras, profesionales, materiales de construcción
 * - Mueblería: maneja productos, diseñadores, materiales de muebles
 * - Seguro: maneja pólizas, clientes, reclamos
 */
@Entity
@Table(name = "empresas")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_empresa")
    private Long id;

    @Column(name = "nombre_empresa", nullable = false, length = 200)
    private String nombreEmpresa;

    @Column(name = "razon_social", length = 200)
    private String razonSocial;

    @Column(name = "cuit", length = 20)
    private String cuit;

    @Column(name = "direccion_fiscal", columnDefinition = "TEXT")
    private String direccionFiscal;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "representante_legal", length = 200)
    private String representanteLegal;

    @Column(name = "activa")
    private Boolean activa = true;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    @ManyToMany(mappedBy = "empresas")
    private List<Cliente> clientes = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Usuario> usuarios = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "empresa", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<PresupuestoNoCliente> presupuestosNoCliente = new ArrayList<>();

    // Métodos de conveniencia
    public void addCliente(Cliente cliente) {
        clientes.add(cliente);
        if (!cliente.getEmpresas().contains(this)) {
            cliente.getEmpresas().add(this);
        }
    }

    public void addUsuario(Usuario usuario) {
        usuarios.add(usuario);
        usuario.setEmpresa(this);
    }

    public void removeUsuario(Usuario usuario) {
        usuarios.remove(usuario);
        usuario.setEmpresa(null);
    }

    public void removeCliente(Cliente cliente) {
        clientes.remove(cliente);
        cliente.getEmpresas().remove(this);
    }
}