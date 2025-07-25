package com.garage.garage_back.facture;

import com.garage.garage_back.facture.ligneFacturePrestation.LignePrestationDTO;
import com.garage.garage_back.facture.ligneFactureProduit.LigneProduitDTO;
import com.garage.garage_back.model.Facture;
import com.garage.garage_back.model.LigneFacturePrestation;
import com.garage.garage_back.model.LigneFactureProduit;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FactureMapper {
    FactureDTO toDto(Facture facture);
    Facture toEntity(FactureDTO dto);

    LignePrestationDTO toLignePrestationDto(LigneFacturePrestation prestation);
    LigneFacturePrestation toLignePrestationEntity(LignePrestationDTO dto);
    LigneFactureProduit toLigneProduitEntity(LigneProduitDTO dto);
    LigneProduitDTO toLigneProduitDto(LigneFactureProduit produit);


}
