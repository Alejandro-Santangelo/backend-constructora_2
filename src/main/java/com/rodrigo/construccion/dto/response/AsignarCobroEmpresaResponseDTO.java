package com.rodrigo.construccion.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

/**
 * DTO de respuesta para asignación de cobro empresa a obras
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AsignarCobroEmpresaResponseDTO {

    private CobroEmpresaResponseDTO cobroEmpresa;
    private List<AsignacionCreadaDTO> asignacionesCreadas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class AsignacionCreadaDTO {
        private Long cobroObraId;          // presente si se asignó a una obra
        private Long obraId;               // presente si se asignó a una obra
        private Long cobroEntidadId;       // presente si se asignó a un trabajo adicional
        private Long trabajoAdicionalId;   // presente si se asignó a un trabajo adicional
        private BigDecimal montoAsignado;
        private Boolean tieneDistribucionItems;
    }
}
