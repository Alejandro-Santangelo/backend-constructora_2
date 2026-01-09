package com.rodrigo.construccion.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entidad para almacenar PDFs de trabajos extra
 */
@Entity
@Table(name = "trabajos_extra_pdf", indexes = {
    @Index(name = "idx_trabajos_extra_pdf_trabajo_extra", columnList = "trabajo_extra_id"),
    @Index(name = "idx_trabajos_extra_pdf_empresa", columnList = "empresa_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TrabajoExtraPdf {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pdf_trabajo_extra")
    private Long id;

    @Column(name = "trabajo_extra_id", nullable = false)
    private Long trabajoExtraId;

    @Column(name = "empresa_id", nullable = false)
    private Long empresaId;

    @Column(name = "nombre_archivo", nullable = false, length = 255)
    private String nombreArchivo;

    @Column(name = "tamanio_bytes", nullable = false)
    private Long tamanioBytes;

    @Column(name = "contenido_pdf", nullable = false, columnDefinition = "BYTEA")
    private byte[] contenidoPdf;

    @Column(name = "fecha_generacion", nullable = false)
    private LocalDateTime fechaGeneracion;

    @Column(name = "generado_por", length = 100)
    private String generadoPor;

    @Column(name = "version_trabajo_extra")
    private Integer versionTrabajoExtra;

    @Column(name = "incluye_honorarios")
    @Builder.Default
    private Boolean incluyeHonorarios = false;

    @Column(name = "incluye_configuracion")
    @Builder.Default
    private Boolean incluyeConfiguracion = false;
}
