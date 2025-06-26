package com.garage.garage_back.produit;

import com.garage.garage_back.model.Produit;

import java.util.List;

public interface ProduitService {
    List<Produit> getAllProduits();
    List<Produit> getProduitsSousSeuil();
    Produit saveProduit(Produit produit);
    Produit updateProduit(Long id, Produit produit);
    void deleteProduit(Long id);
}
