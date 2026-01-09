package com.rodrigo.construccion.model.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.hibernate.annotations.Filter;

/**
 * Entidad que representa un Proveedor
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "proveedor", indexes = {
    @Index(name = "idx_proveedor_empresa", columnList = "id_empresa"),
    @Index(name = "idx_proveedor_rut", columnList = "rut"),
    @Index(name = "idx_proveedor_nombre", columnList = "nombre")
})
@Filter(name = "empresaFilter", condition = "id_empresa = :empresaId")
public class Proveedor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_proveedor")
    private Long id;

    @Column(name = "nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "rut", length = 20)
    private String rut;

    @Column(name = "telefono", length = 50)
    private String telefono;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "direccion", length = 300)
    private String direccion;

    @Column(name = "ciudad", length = 100)
    private String ciudad;

    @Column(name = "region", length = 100)
    private String region;

    @Column(name = "codigo_postal", length = 20)
    private String codigoPostal;

    @Column(name = "pais", length = 100)
    private String pais;

    @Column(name = "contacto_principal", length = 150)
    private String contactoPrincipal;

    @Column(name = "cargo_contacto", length = 100)
    private String cargoContacto;

    @Column(name = "telefono_contacto", length = 50)
    private String telefonoContacto;

    @Column(name = "email_contacto", length = 100)
    private String emailContacto;

    @Column(name = "sitio_web", length = 200)
    private String sitioWeb;

    @Column(name = "tipo_proveedor", length = 50)
    private String tipoProveedor;

    @Column(name = "categoria", length = 100)
    private String categoria;

    @Column(name = "condiciones_pago", length = 100)
    private String condicionesPago;

    @Column(name = "limite_credito")
    private Double limiteCredito;

    @Column(name = "descuento_maximo")
    private Double descuentoMaximo;

    @Column(name = "plazo_entrega_promedio")
    private Integer plazoEntregaPromedio;

    @Column(name = "calificacion")
    private Double calificacion;

    @Column(name = "estado", length = 20)
    private String estado;

    @Column(name = "observaciones", columnDefinition = "TEXT")
    private String observaciones;

    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @Column(name = "fecha_ultima_compra")
    private LocalDateTime fechaUltimaCompra;

    @Column(name = "numero_cuenta_bancaria", length = 50)
    private String numeroCuentaBancaria;

    @Column(name = "banco", length = 100)
    private String banco;

    @Column(name = "tipo_cuenta", length = 50)
    private String tipoCuenta;

    @Column(name = "id_empresa", nullable = false)
    private Long empresaId;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion;

    @Column(name = "fecha_modificacion")
    private LocalDateTime fechaModificacion;

    @ManyToOne
    @JoinColumn(name = "id_empresa", insertable = false, updatable = false)
    private Empresa empresa;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
        fechaCreacion = LocalDateTime.now();
        fechaModificacion = LocalDateTime.now();
        if (estado == null) {
            estado = "ACTIVO";
        }
        if (activo == null) {
            activo = true;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        fechaModificacion = LocalDateTime.now();
    }
}
