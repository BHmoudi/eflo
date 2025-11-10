package com.eflo.order.domain.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MoveCustomerData {

    @JsonProperty("szTypeClient")
    private String typeClient; // PA or PRO

    @JsonProperty("szCiviliteClient")
    private String civiliteClient;

    @JsonProperty("szNomClient")
    private String nomClient;

    @JsonProperty("szPrenomClient")
    private String prenomClient;

    @JsonProperty("szEmailClient")
    private String emailClient;

    @JsonProperty("szTelClientPortable")
    private String telClientPortable;

    @JsonProperty("szTelClientFixe")
    private String telClientFixe;

    @JsonProperty("szAdresseClient")
    private String adresseClient;

    @JsonProperty("szCodePostalClient")
    private String codePostalClient;

    @JsonProperty("szVilleClient")
    private String villeClient;

    @JsonProperty("szSAClient")
    private Integer saClient;

    @JsonProperty("szClientFinal")
    private String clientFinal; // Commercial name for PRO
}
