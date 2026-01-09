package com.rodrigo.construccion.model.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Filter;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "clientes", uniqueConstraints = {
        @UniqueConstraint(columnNames = "cuit_cuil")
})
@Filter(name = "empresaFilter", condition = "EXISTS (SELECT 1 FROM cliente_empresa ce WHERE ce.id_cliente = id_cliente AND ce.id_empresa = :empresaId)")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long id;

    @Column(name = "nombre", nullable = true, length = 200)
    private String nombre;

    @Column(name = "nombre_solicitante", length = 200)
    private String nombreSolicitante;

    @Column(name = "cuit_cuil", unique = true, length = 20)
    private String cuitCuil;

    @Column(name = "direccion", columnDefinition = "TEXT")
    private String direccion;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "email", length = 150)
    private String email;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;

    // Relación muchos a muchos: Un cliente puede pertenecer a varias empresas
    @ManyToMany
    @JoinTable(name = "cliente_empresa", joinColumns = @JoinColumn(name = "id_cliente"), inverseJoinColumns = @JoinColumn(name = "id_empresa"))
    private List<Empresa> empresas = new ArrayList<>();

    // Relaciones
    @JsonManagedReference("cliente-obras")
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Obra> obras = new ArrayList<>();

    // Métodos de conveniencia
    public void addObra(Obra obra) {
        obras.add(obra);
        obra.setCliente(this);
    }

    public void removeObra(Obra obra) {
        obras.remove(obra);
        obra.setCliente(null);
    }

    // Métodos para empresas
    public void addEmpresa(Empresa empresa) {
        if (empresas == null)
            empresas = new ArrayList<>();
        if (!empresas.contains(empresa)) {
            empresas.add(empresa);
        }
        if (empresa.getClientes() == null)
            empresa.setClientes(new ArrayList<>());
        if (!empresa.getClientes().contains(this)) {
            empresa.getClientes().add(this);
        }
    }

    public void removeEmpresa(Empresa empresa) {
        if (empresas != null)
            empresas.remove(empresa);
        if (empresa.getClientes() != null)
            empresa.getClientes().remove(this);
    }

}
