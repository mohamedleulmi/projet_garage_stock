package com.garage.garage_back.produit;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitDTO {
    private Long id;
    private String reference;
    private String designation;
    private double prixUnitaireHT;
    private double tva;
    private int stockActuel;
    private int stockVendu;
    private int seuilAlerte;

    private double prixTTC;
}
