package com.garage.garage_back.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Produit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String reference;

    private String designation;

    private double prixUnitaireHT;

    private double tva;

    private int stockActuel;

    private int stockVendu;

    private int seuilAlerte;

    public double getPrixTTC() {
        return prixUnitaireHT * (1 + tva / 100);
    }
}
