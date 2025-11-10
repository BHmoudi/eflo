package com.eflo.order.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveServiceData {

    @JsonProperty("szTypeServices")
    private String typeServices;

    @JsonProperty("csLibelleServices")
    private String libelleServices;

    @JsonProperty("szCodeBaremeServices")
    private String codeBaremeServices;

    @JsonProperty("csLibelleBaremeServices")
    private String libelleBaremeServices;

    @JsonProperty("iDureeServices")
    private Integer dureeServices;

    @JsonProperty("iKilometrageServices")
    private Integer kilometrageServices;

    @JsonProperty("fMontantServices")
    private BigDecimal montantServices;

    @JsonProperty("fMontantRemiseServices")
    private BigDecimal montantRemiseServices;

    @JsonProperty("bComptant")
    private Integer comptant; // 0 or 1
}
