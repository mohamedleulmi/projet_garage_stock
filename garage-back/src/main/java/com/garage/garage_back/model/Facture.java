package com.garage.garage_back.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Facture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    private LocalDate dateCreation;

    private double totalHT;
    private double totalTVA;
    private double totalTTC;

    private String cheminPdf;

    @ManyToOne
    private Client client;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<LigneFactureProduit> lignesProduit;

    @OneToMany(mappedBy = "facture", cascade = CascadeType.ALL)
    private List<LigneFacturePrestation> lignesPrestation;

}
