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
public class MoveSupplementData {

    @JsonProperty("szLibelle")
    private String libelle;

    @JsonProperty("fMontant")
    private BigDecimal montant;

    @JsonProperty("szTypeTVA")
    private String typeTva;
}
