package com.garage.garage_back.facture.ligneFactureProduit;

import lombok.Data;

@Data
public class LigneProduitDTO {
    private Long id;
    private Long produitId;
    private String reference;
    private String designation;
    private int quantite;
    private double prixUnitaireHT;
    private double tva;
    private double totalHT;
    private double totalTTC;
}
