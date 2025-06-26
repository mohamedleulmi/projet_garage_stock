package com.garage.garage_back.facture.ligneFactureProduit;

import com.garage.garage_back.model.LigneFactureProduit;

import java.util.List;

public interface LigneFactureProduitService {
    LigneFactureProduit save(LigneFactureProduit ligne);
    List<LigneFactureProduit> findAll();
}
