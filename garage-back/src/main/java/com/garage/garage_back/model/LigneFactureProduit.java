package com.garage.garage_back.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LigneFactureProduit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "facture_id")
    @JsonBackReference
    private Facture facture;

    @ManyToOne
    private Produit produit;

    private int quantite;

    private double prixUnitaireHT;
    private double tva;
    private double totalHT;
    private double totalTTC;

    // Getters et Setters
}
