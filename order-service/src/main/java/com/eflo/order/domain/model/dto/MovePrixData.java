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
public class MovePrixData {

    // User/Salesperson
    @JsonProperty("userIpn")
    private String userIpn;

    @JsonProperty("szPrenomVendeur")
    private String prenomVendeur;

    @JsonProperty("szNomVendeur")
    private String nomVendeur;

    @JsonProperty("cTypeVendeur")
    private String typeVendeur;

    // Customer
    @JsonProperty("szCiviliteClient")
    private String civiliteClient;

    @JsonProperty("szNomClient")
    private String nomClient;

    @JsonProperty("szPrenomClient")
    private String prenomClient;

    @JsonProperty("szTypeClient")
    private String typeClient;

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
    private String clientFinal;

    // Business organization
    @JsonProperty("szNomEtablissement")
    private String nomEtablissement;

    @JsonProperty("szIdentifiantRR")
    private String identifiantRr;

    @JsonProperty("szRattachement")
    private String rattachement;

    // Order
    @JsonProperty("iNumContratCommande")
    private String numContratCommande;

    @JsonProperty("szGenre")
    private String genre; // VN, VO, EVO

    // Vehicle
    @JsonProperty("szMarque")
    private String marque;

    @JsonProperty("szModele")
    private String modele;

    @JsonProperty("szVersion")
    private String version;

    @JsonProperty("szSemiClairModele")
    private String semiClairModele;

    @JsonProperty("szSemiClairVersion")
    private String semiClairVersion;

    @JsonProperty("szCouleur")
    private String couleur;

    @JsonProperty("szEnergie")
    private String energie;

    @JsonProperty("iNivCO2")
    private Integer nivCo2;

    @JsonProperty("szGamme")
    private String gamme; // Body type

    // Business metadata
    @JsonProperty("szTypeProduit")
    private String typeProduit;

    @JsonProperty("iNumTarif")
    private Integer numTarif;

    @JsonProperty("szCodeBareme")
    private String codeBareme;

    @JsonProperty("szFamilleBareme")
    private String familleBareme;

    @JsonProperty("szCodeDistrinet")
    private String codeDistrinet;

    @JsonProperty("szNumExport")
    private String numExport;

    // Pricing
    @JsonProperty("fMontantCatalogue")
    private BigDecimal montantCatalogue;

    @JsonProperty("fPrixHTPublic")
    private BigDecimal prixHtPublic;

    @JsonProperty("fPrixHTConcessionnaire")
    private BigDecimal prixHtConcessionnaire;

    @JsonProperty("fMontantRemise")
    private BigDecimal montantRemise;

    @JsonProperty("fTauxTVA")
    private BigDecimal tauxTva; // 0.2 for 20%

    // Financing
    @JsonProperty("szCategorieFinanDMS")
    private String categorieFinanDms;

    @JsonProperty("iNumContratFinanDiac")
    private String numContratFinanDiac;

    @JsonProperty("fMontantAFinancer")
    private BigDecimal montantAFinancer;

    @JsonProperty("iFinancementAvecCaution")
    private Integer financementAvecCaution; // 0 or 1

    @JsonProperty("iNbreAssurances")
    private Integer nbreAssurances;

    // Aids
    @JsonProperty("fAideRPE")
    private BigDecimal aideRpe;

    @JsonProperty("fAideAutres")
    private BigDecimal aideAutres;

    // Dates
    @JsonProperty("szDateLivraison")
    private String dateLivraison;

    @JsonProperty("dateCreate")
    private String dateCreate;

    // Trade-in
    @JsonProperty("szRepVONom_CG")
    private String repVoNomCg;

    @JsonProperty("szRepVO_Marque")
    private String repVoMarque;

    @JsonProperty("szRepVO_Modele")
    private String repVoModele;

    @JsonProperty("szRepVO_Chassis")
    private String repVoChassis;

    @JsonProperty("szRepVO_Energie")
    private String repVoEnergie;

    @JsonProperty("szRepVO_Km")
    private Integer repVoKm;

    @JsonProperty("szRepVO_Origine")
    private String repVoOrigine;

    @JsonProperty("szRepVO_ImmaDate")
    private String repVoImmaDate;

    @JsonProperty("szRepVO_ImmaDate1")
    private String repVoImmaDate1;

    @JsonProperty("szRepVO_PrimeConv")
    private BigDecimal repVoPrimeConv;

    @JsonProperty("fMontantEngagementReprise")
    private BigDecimal montantEngagementReprise;

    @JsonProperty("fSurestimationRepVO")
    private BigDecimal surestimationRepVo;

    @JsonProperty("fValeurRepriseVO")
    private BigDecimal valeurRepriseVo;
}
