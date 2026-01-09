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
        private Long cobroObraId;
        private Long obraId;
        private BigDecimal montoAsignado;
        private Boolean tieneDistribucionItems;
    }
}
