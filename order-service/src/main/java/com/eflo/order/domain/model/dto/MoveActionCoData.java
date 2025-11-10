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
public class MoveActionCoData {

    @JsonProperty("szLibelle")
    private String libelle;

    @JsonProperty("fMontantPourcentage")
    private BigDecimal montantPourcentage;

    @JsonProperty("bPourcentage")
    private Integer pourcentage; // 0 or 1

    @JsonProperty("fTauxTva")
    private BigDecimal tauxTva; // 0.2 for 20%

    @JsonProperty("szTypeTVA")
    private String typeTva;
}
