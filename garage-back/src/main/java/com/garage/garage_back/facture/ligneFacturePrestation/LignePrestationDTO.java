package com.garage.garage_back.facture.ligneFacturePrestation;

import lombok.Data;

@Data
public class LignePrestationDTO {
    private Long id;
    private String description;
    private double prixHT;
    private double tva;
    private double totalTTC;
}
