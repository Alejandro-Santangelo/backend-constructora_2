package com.rodrigo.construccion.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.rodrigo.construccion.enums.EstadoObra;
import com.rodrigo.construccion.enums.TipoOrigen;
import com.rodrigo.construccion.enums.TipoPresupuesto;

public class ObraSimpleDTO {
    public Long id;
    public Boolean esObraTrabajoExtra;
    public TipoPresupuesto tipoPresupuesto;
    public TipoOrigen tipoOrigen;
    public Long obraOrigenId;
    public Boolean esObraManual;
    public String nombre;
    
    // DIRECCIÓN EN 6 CAMPOS SEPARADOS (igual que PresupuestoNoCliente)
    public String direccionObraBarrio;
    public String direccionObraCalle;
    public String direccionObraAltura;
    public String direccionObraTorre;
    public String direccionObraPiso;
    public String direccionObraDepartamento;
    
    public EstadoObra estado;
    public LocalDate fechaInicio;
    public LocalDate fechaFin;
    public BigDecimal presupuestoEstimado;
    public LocalDateTime fechaCreacion;
    public Long clienteId;
    public Long empresaId;
    public String descripcion;
    public String observaciones;
    
    /**
     * Helper para obtener la dirección completa formateada
     * @return Dirección formateada como "Barrio - Calle 1234 Torre A Piso 4 Depto A"
     */
    public String getDireccionCompleta() {
        StringBuilder direccion = new StringBuilder();
        if (direccionObraBarrio != null && !direccionObraBarrio.trim().isEmpty()) {
            direccion.append(direccionObraBarrio).append(" - ");
        }
        direccion.append(direccionObraCalle != null ? direccionObraCalle : "")
                 .append(" ")
                 .append(direccionObraAltura != null ? direccionObraAltura.toString() : "0");
        if (direccionObraTorre != null && !direccionObraTorre.trim().isEmpty()) {
            direccion.append(" ").append(direccionObraTorre);
        }
        if (direccionObraPiso != null && !direccionObraPiso.trim().isEmpty()) {
            direccion.append(" Piso ").append(direccionObraPiso);
        }
        if (direccionObraDepartamento != null && !direccionObraDepartamento.trim().isEmpty()) {
            direccion.append(" Depto ").append(direccionObraDepartamento);
        }
        return direccion.toString().trim();
    }
}
