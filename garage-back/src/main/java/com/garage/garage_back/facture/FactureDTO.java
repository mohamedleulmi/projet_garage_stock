package com.garage.garage_back.facture;

import com.garage.garage_back.client.ClientDTO;
import com.garage.garage_back.facture.ligneFacturePrestation.LignePrestationDTO;
import com.garage.garage_back.facture.ligneFactureProduit.LigneProduitDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FactureDTO {
    private Long id;
    private String numero;
    private LocalDate dateCreation;
    private double totalHT;
    private double totalTVA;
    private double totalTTC;
    private String cheminPdf;
    private ClientDTO client;
    private List<LigneProduitDTO> lignesProduit;
    private List<LignePrestationDTO> lignesPrestation;
}
