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
public class MoveTradeInData {

    @JsonProperty("szRepVONom_CG")
    private String nomCg;

    @JsonProperty("szRepVO_Marque")
    private String marque;

    @JsonProperty("szRepVO_Modele")
    private String modele;

    @JsonProperty("szRepVO_Chassis")
    private String chassis;

    @JsonProperty("szRepVO_Energie")
    private String energie;

    @JsonProperty("szRepVO_Km")
    private Integer km;

    @JsonProperty("szRepVO_Origine")
    private String origine;

    @JsonProperty("szRepVO_ImmaDate")
    private String immaDate;

    @JsonProperty("szRepVO_ImmaDate1")
    private String immaDate1;

    @JsonProperty("szRepVO_PrimeConv")
    private BigDecimal primeConv;

    @JsonProperty("fMontantEngagementReprise")
    private BigDecimal montantEngagementReprise;

    @JsonProperty("fSurestimationRepVO")
    private BigDecimal surestimationRepVo;

    @JsonProperty("fValeurRepriseVO")
    private BigDecimal valeurRepriseVo;
}
