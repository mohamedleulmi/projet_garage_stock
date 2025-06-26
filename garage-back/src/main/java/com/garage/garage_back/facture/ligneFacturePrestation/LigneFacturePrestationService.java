package com.garage.garage_back.facture.ligneFacturePrestation;

import com.garage.garage_back.model.LigneFacturePrestation;

import java.util.List;

public interface LigneFacturePrestationService {
    LigneFacturePrestation save(LigneFacturePrestation ligne);
    List<LigneFacturePrestation> findAll();
}
