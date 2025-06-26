package com.garage.garage_back.produit;

import lombok.Data;

@Data
public class ProduitDTO {
    private Long id;
    private String reference;
    private String designation;
    private double prixUnitaireHT;
    private double tva;
    private int stockActuel;
    private int seuilAlerte;
}
