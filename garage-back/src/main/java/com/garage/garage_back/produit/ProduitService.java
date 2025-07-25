package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;

import java.util.List;

public interface ProduitService {
    List<ProduitDTO> getAllProduits();
    List<ProduitDTO> getProduitsSousSeuil();
    ProduitDTO saveProduit(ProduitDTO produitDTO);
    ProduitDTO updateProduit(Long id, ProduitDTO produitDTO);
    void deleteProduit(Long id);
}
