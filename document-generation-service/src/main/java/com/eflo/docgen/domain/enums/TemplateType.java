package com.eflo.docgen.domain.enums;

/**
 * Document Template Types
 * Defines the different types of documents that can be generated
 */
public enum TemplateType {
    FEUILLE_GESTION("Feuille de Gestion", "Management Sheet for vehicle orders"),
    BON_COMMANDE("Bon de Commande", "Purchase Order"),
    CONTRAT_VENTE("Contrat de Vente", "Sales Contract"),
    FACTURE("Facture", "Invoice"),
    DEVIS("Devis", "Quote/Estimate"),
    CERTIFICAT_CESSION("Certificat de Cession", "Certificate of Transfer"),
    BON_LIVRAISON("Bon de Livraison", "Delivery Note"),
    ATTESTATION("Attestation", "Certificate/Attestation"),
    RAPPORT_EXPERTISE("Rapport d'Expertise", "Expert Report"),
    MANDAT("Mandat", "Mandate/Proxy"),
    CUSTOM("Custom Document", "Custom template");

    private final String frenchName;
    private final String description;

    TemplateType(String frenchName, String description) {
        this.frenchName = frenchName;
        this.description = description;
    }

    public String getFrenchName() {
        return frenchName;
    }

    public String getDescription() {
        return description;
    }
}
